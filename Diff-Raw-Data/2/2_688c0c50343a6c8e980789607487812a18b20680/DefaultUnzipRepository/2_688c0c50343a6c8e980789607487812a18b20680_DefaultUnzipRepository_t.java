 /*******************************************************************************
  * Copyright (c) 2010, 2012 SAP AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tycho.nexus.internal.plugin;
 
 import java.io.File;
 import java.util.Arrays;
 
 import org.codehaus.plexus.component.annotations.Component;
 import org.codehaus.plexus.component.annotations.Requirement;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 import org.eclipse.tycho.nexus.internal.plugin.cache.ConversionResult;
 import org.eclipse.tycho.nexus.internal.plugin.cache.RequestPathConverter;
 import org.eclipse.tycho.nexus.internal.plugin.cache.UnzipCache;
 import org.eclipse.tycho.nexus.internal.plugin.storage.Util;
 import org.eclipse.tycho.nexus.internal.plugin.storage.ZipAwareStorageCollectionItem;
 import org.eclipse.tycho.nexus.internal.plugin.storage.ZippedItem;
 import org.sonatype.nexus.configuration.Configurator;
 import org.sonatype.nexus.configuration.model.CRepository;
 import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
 import org.sonatype.nexus.proxy.IllegalOperationException;
 import org.sonatype.nexus.proxy.ItemNotFoundException;
 import org.sonatype.nexus.proxy.LocalStorageException;
 import org.sonatype.nexus.proxy.NoSuchRepositoryException;
 import org.sonatype.nexus.proxy.ResourceStoreRequest;
 import org.sonatype.nexus.proxy.events.NexusStartedEvent;
 import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
 import org.sonatype.nexus.proxy.item.StorageCollectionItem;
 import org.sonatype.nexus.proxy.item.StorageItem;
 import org.sonatype.nexus.proxy.item.StorageLinkItem;
 import org.sonatype.nexus.proxy.registry.ContentClass;
 import org.sonatype.nexus.proxy.repository.AbstractShadowRepository;
 import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
 import org.sonatype.nexus.proxy.repository.IncompatibleMasterRepositoryException;
 import org.sonatype.nexus.proxy.repository.LocalStatus;
 import org.sonatype.nexus.proxy.repository.Repository;
 import org.sonatype.nexus.proxy.repository.RepositoryKind;
 import org.sonatype.nexus.proxy.repository.ShadowRepository;
 import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
 import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
 import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
 
 import com.google.common.eventbus.Subscribe;
 
 /**
  * Shadow repository that allows to directly browse and access the content of archive files (e.g.
  * zip, jar files) that are stored in the master repository. In the shadow repository all files and
  * folders of the master repository can be accessed in the same way as in the master repository. The
  * additional functionality is that <br>
  * 1. archives can be browsed under their path + trailing slash.<br>
  * 2. files and folders in an archive can be browsed under the path of the archive + slash + path
  * within archive
  */
 /*
  * Within Nexus 1.6 the plugin worked without the annotation. Within Nexus 1.7 is still working, but
  * a warning is logged that the plugin misses to register it's type correctly.
  * 
  * Reference: Nexus Book - Chapter 18. Developing Nexus Plugins -
  * http://www.sonatype.com/books/nexus-book/reference/plugdev.html The documentation describes to
  * tag the repository interface (UnzipRepository) with the RepositoryType annotation. This does not
  * work as described. (Neither with 1.6, nor with 1.7) Tagging the implementation class works in
  * Nexus 1.7 as described. (incl. the promised repository appearance in .../nexus/content..) Within
  * Nexus 1.6 an exception will be logged during startup, but the plugin still works functional
  * correct.
  */
 @Component(role = UnzipRepository.class, hint = DefaultUnzipRepository.REPOSITORY_HINT, instantiationStrategy = "per-lookup", description = "Unzip Repository")
 public class DefaultUnzipRepository extends AbstractShadowRepository implements UnzipRepository {
     static final String REPOSITORY_HINT = "org.eclipse.tycho.nexus.plugin.DefaultUnzipRepository";
 
     @Requirement
     private UnzipRepositoryConfigurator configurator;
 
     @Requirement(hint = "maven2")
     private ContentClass contentClass;
 
     @Requirement(hint = "maven2")
     private ContentClass masterContentClass;
 
     private RepositoryKind repositoryKind;
 
     private boolean isNexusStarted = false;
 
     private boolean isMasterAvailable = false;
 
     private UnzipCache cache;
 
     @Override
     protected Configurator getConfigurator() {
         return configurator;
     }
 
     @Override
     public ContentClass getRepositoryContentClass() {
         return contentClass;
     }
 
     @Override
     public ContentClass getMasterRepositoryContentClass() {
         return masterContentClass;
     }
 
     @Override
     public RepositoryKind getRepositoryKind() {
         if (repositoryKind == null) {
             repositoryKind = new DefaultRepositoryKind(UnzipRepository.class,
                     Arrays.asList(new Class<?>[] { ShadowRepository.class }));
         }
 
         return repositoryKind;
     }
 
     @Override
     protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory() {
         return new CRepositoryExternalConfigurationHolderFactory<UnzipRepositoryConfiguration>() {
             @Override
             public UnzipRepositoryConfiguration createExternalConfigurationHolder(final CRepository config) {
                 return new UnzipRepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
             }
         };
     }
 
     /*
      * Need to overwrite setMasterRepositoryId(String id) getMasterRepository() getLocalStatus()
      * onEvent(Event<?> evt) in order to allow Unzip repositories in front of repository groups.
      * During Nexus startup repository creation repository groups are explicitly created AFTER all
      * other repositories. see
      * org.sonatype.nexus.configuration.application.DefaultNexusConfiguration#createRepositories()
      * As a result at creation time of this repository the master is not yet available in case the
      * master is a group. After Nexus startup is complete all methods behave like default. In the
      * meantime the master repository id will be stored without availability, compatibility checks
      * avoiding error logs.
      */
     @SuppressWarnings("deprecation")
     @Override
     public void setMasterRepositoryId(final String id) throws NoSuchRepositoryException,
             IncompatibleMasterRepositoryException {
         try {
             super.setMasterRepositoryId(id);
             isMasterAvailable = true;
         } catch (final NoSuchRepositoryException e) {
             if (isNexusStarted) {
                 throw e;
             } else {
                 // NoSuchRepositoryException yet. Just remember the id
                 getExternalConfiguration(true).setMasterRepositoryId(id);
             }
         }
     }
 
     // see comment at setMasterRepositoryId(String id)
     @Override
     public void setMasterRepository(final Repository masterRepository) throws IncompatibleMasterRepositoryException {
         isMasterAvailable = true;
         super.setMasterRepository(masterRepository);
     }
 
     // see comment at setMasterRepositoryId(String id)
     @Override
     public Repository getMasterRepository() {
         if (isNexusStarted || isMasterAvailable) {
             return super.getMasterRepository();
         } else {
             return null;
         }
     }
 
     // see comment at setMasterRepositoryId(String id)
     @Override
     public LocalStatus getLocalStatus() {
         if (isNexusStarted || isMasterAvailable) {
             return super.getLocalStatus();
         } else {
             if (getCurrentConfiguration(false).getLocalStatus() == null) {
                 return LocalStatus.OUT_OF_SERVICE;
             }
             return LocalStatus.valueOf(getCurrentConfiguration(false).getLocalStatus());
         }
     }
 
     @Subscribe
     public void onNexusStartedEvent(NexusStartedEvent evt) {
         isNexusStarted = true;
     }
 
     @Subscribe
     public void onRepositoryRegistryEventAdd(RepositoryRegistryEventAdd evt) {
        if (getMasterRepository() != null && evt.getRepository().getId().equals(getMasterRepository().getId())) {
             try {
                 setMasterRepositoryId(evt.getRepository().getId());
             } catch (final NoSuchRepositoryException e) {
                 getLogger().warn("Master Repository not available", e);
             } catch (final IncompatibleMasterRepositoryException e) {
                 getLogger().warn("Master Repository incompatible", e);
             }
         }
     }
 
     /**
      * Retrieves an item from the master repository. The method includes some workaround logic to
      * access folders (some folders are only accessible if the request URL end with a double slash).
      * In addition this method also finds file items if the request URL ends with a slash.
      * 
      * @param request
      *            the request that defines which item be retrieved
      * @return the item from the master repository
      * @throws ItemNotFoundException
      *             is thrown if there is no item under the specified request path in the master
      *             repository
      * @throws LocalStorageException
      */
     StorageItem retrieveItemFromMaster(final String requestPath) throws ItemNotFoundException, LocalStorageException {
         try {
             final ResourceStoreRequest request = new ResourceStoreRequest(requestPath);
             return doRetrieveItemFromMaster(request);
         } catch (final IllegalOperationException e) {
             throw new LocalStorageException(e);
         } catch (@SuppressWarnings("deprecation") final org.sonatype.nexus.proxy.StorageException e) {
             throw new LocalStorageException(e);
         }
     }
 
     @Override
     protected StorageItem doRetrieveItem(final ResourceStoreRequest request) throws IllegalOperationException,
             ItemNotFoundException, LocalStorageException {
 
         final RequestTimeTrace timeTrace = new RequestTimeTrace(request.getRequestPath());
 
         final ConversionResult conversionResult = RequestPathConverter.convert(getMasterRepository(), request,
                 isUseVirtualVersion());
 
         if (conversionResult.isPathConverted()) {
             getLogger().debug(
                     "Resolved dynamic request: " + request.getRequestUrl() + ". Resolved request path: "
                             + conversionResult.getConvertedPath());
         }
 
         // First check for zip content to avoid unnecessary and expensive backend calls for zip content requests.
         // Due to naming conventions zippedItem creation will normally only call the backend in case it is a zip content request.
         // a) path does not point to zip content (-> null)
         // b) a path to a file/folder inside a zip file (-> ZippedItem is created and returned)
         // c) a non-existing path under an existing zip file (-> retrieving ZippedItem fails with ItemNotFoundException)
         final ZippedItem zippedItem = getZippedItem(conversionResult);
         if (zippedItem != null) {
             final StorageItem zippedStorageItem = zippedItem.getZippedStorageItem();
             getLogger().debug(timeTrace.getMessage());
             return zippedStorageItem;
         }
 
         // check if item exists in master repository
         // this call will fail with ItemNotFoundException if the item does not exist in the master repository
         final StorageItem masterItem = retrieveItemFromMaster(conversionResult.getConvertedPath());
 
         if (masterItem instanceof StorageCollectionItem) {
             // item is non-zip folder
             final ZipAwareStorageCollectionItem zipAwareStorageCollectionItem = new ZipAwareStorageCollectionItem(this,
                     (StorageCollectionItem) masterItem, getLogger());
             getLogger().debug(timeTrace.getMessage());
             return zipAwareStorageCollectionItem;
         } else {
             getLogger().debug(timeTrace.getMessage());
             // if item is a non-zip file we simply return it as it is
             return masterItem;
         }
 
     }
 
     /**
      * Checks if the request path represents a zipped item (a file or directory within a zip file)
      * and if yes returns it. If the request path does not represent a zipped item <code>null</code>
      * is returned
      * 
      * @param conversionResult
      *            the result of the snapshot path conversion, containing the converted path
      * @return item that represents a file or folder within a zip file, <code>null</code> if the
      *         requested path does not point to zip content
      * @throws LocalStorageException
      * @throws ItemNotFoundException
      *             is thrown if for non-existing or invalid request path
      */
     private ZippedItem getZippedItem(final ConversionResult conversionResult) throws LocalStorageException,
             ItemNotFoundException {
         final StringBuilder pathInZip = new StringBuilder();
         final String[] pathSegments = conversionResult.getConvertedPath().split("/");
         String zipFilePath = "";
         String zipItemPath = null;
         long zipLastModified = 0L;
 
         for (final String pathSegment : pathSegments) {
             if (zipItemPath == null) {
                 if (!zipFilePath.toString().endsWith("/")) {
                     zipFilePath = zipFilePath + "/";
                 }
                 zipFilePath = zipFilePath + pathSegment;
                 if (zipFilePath.endsWith(Util.UNZIP_TYPE_EXTENSION)) {
                     final String zipFilePathWithoutExtension = zipFilePath.substring(0, zipFilePath.length()
                             - Util.UNZIP_TYPE_EXTENSION.length());
                     getCache().cleanSnapshots(conversionResult);
                     final File zipFile = getCache().getArchive(zipFilePathWithoutExtension);
                     if (zipFile != null) {
                         zipLastModified = zipFile.lastModified();
                         zipItemPath = zipFilePathWithoutExtension;
                     }
                 }
             } else {
                 if (pathInZip.length() > 0) {
                     pathInZip.append("/");
                 }
                 pathInZip.append(pathSegment);
             }
         }
         if (zipItemPath != null) {
             // creating a new ZippedItem fails with ItemNotFoundException if a non-existing file or folder
             // inside the (existing) zip file is accessed
             getLogger().debug(conversionResult.getConvertedPath() + " points into a zip file.");
             final ZippedItem zippedItem = new ZippedItem(this, zipItemPath, pathInZip.toString(), zipLastModified,
                     getLogger());
             return zippedItem;
         }
         return null;
     }
 
     public synchronized UnzipCache getCache() {
         if (cache == null) {
             cache = new UnzipCache(this, getLogger());
         }
         return cache;
     }
 
     @Override
     protected StorageLinkItem createLink(final StorageItem item) throws UnsupportedStorageOperationException,
             IllegalOperationException, LocalStorageException {
         // abstract super methods not documented.
         // called during #onEvent processing.
         // any thrown Exception will be logged polluting the nexus log with Exception stacks.
         // So we interpret the method as automated hook allowing, but not forcing LinkItem creation.
         return null;
     }
 
     @Override
     protected void deleteLink(final StorageItem item) throws UnsupportedStorageOperationException,
             IllegalOperationException, ItemNotFoundException, LocalStorageException {
         // nothing created. Nothing to be deleted
     }
 
     @Override
     public void setLocalStorage(final LocalRepositoryStorage localStorage) {
         if (localStorage instanceof DefaultFSLocalRepositoryStorage) {
             super.setLocalStorage(localStorage);
         } else {
             throw new RuntimeException(localStorage + " is not an instance of DefaultFSLocalRepositoryStorage");
         }
     }
 
     @Override
     public boolean isUseVirtualVersion() {
         return ((UnzipRepositoryConfiguration) getExternalConfiguration(false)).isUseVirtualVersion();
     }
 
     @Override
     public void setUseVirtualVersion(final boolean val) {
         ((UnzipRepositoryConfiguration) getExternalConfiguration(true)).setUseVirtualVersion(val);
     }
 }
