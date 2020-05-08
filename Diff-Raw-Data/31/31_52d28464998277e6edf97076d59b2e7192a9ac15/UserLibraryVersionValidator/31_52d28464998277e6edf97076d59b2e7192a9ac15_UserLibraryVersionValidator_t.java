 /*******************************************************************************
  * Copyright (c) 2001, 2008 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 
 
 package org.eclipse.jst.jsf.common.facet.libraryprovider;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Enumeration;
import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jst.common.project.facet.core.libprov.user.KeyClassesValidator;
 import org.eclipse.jst.common.project.facet.core.libprov.user.UserLibraryProviderInstallOperationConfig;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 import org.eclipse.jst.jsf.common.facet.Messages;
 import org.eclipse.osgi.util.NLS;
 
 
 /**
  * Checks that a user library is version-compatible with the facet.
  *
  * @author Debajit Adhikary
  *
  */
 public abstract class UserLibraryVersionValidator extends KeyClassesValidator
 {
    private static final String MANIFEST_SPECIFICATION_VERSION = "Specification-Version"; //$NON-NLS-1$
     private static final String MANIFEST_IMPLEMENTATION_VERSION = "Implementation-Version"; //$NON-NLS-1$
 
     private final String classNameIdentifyingImplementationJar;
 
 
     /**
      * @param classNameIdentifyingImplementationJar
      */
     public UserLibraryVersionValidator (final String classNameIdentifyingImplementationJar)
     {
         this.classNameIdentifyingImplementationJar = classNameIdentifyingImplementationJar;
     }
 
 
     @Override
     public IStatus validate (final UserLibraryProviderInstallOperationConfig config)
     {
         // Check super validator
         final IStatus status = super.validate(config);
         if (status.getSeverity() != IStatus.OK)
             return status;
 
         // Superclass validated this lib successfully.
         // Check user library version now.
         final String facetVersion = getFacetVersion(config);
         final String libraryVersion = getLibraryVersion(config);
         return validateVersionStrings(facetVersion, libraryVersion);
     }
 
 
     /**
      * @param facetVersion
      * @param libraryVersion
      * @return the diagnostic for whether the facetVersion and libraryVersion
      *         match.
      */
     protected IStatus validateVersionStrings (final String facetVersion,
                                               final String libraryVersion)
     {
         if (facetVersion == null)
             throw new IllegalArgumentException("Cannot read facet version"); //$NON-NLS-1$
 
         if (libraryVersion == null)
             return new Status(IStatus.WARNING, JSFCommonPlugin.PLUGIN_ID, Messages.UserLibraryVersionValidator_cannotReadLibraryVersion);
 
         if (isLibraryFacetCompatible(facetVersion, libraryVersion))
             return Status.OK_STATUS;
 
         final String errorMessage = NLS.bind(Messages.UserLibraryVersionValidator_versionMismatch, libraryVersion, facetVersion);
         return new Status(IStatus.ERROR, JSFCommonPlugin.PLUGIN_ID, errorMessage);
     }
 
 
     private boolean isLibraryFacetCompatible (final String facetVersion,
                                               final String libraryVersion)
     {
         return libraryVersion.startsWith(facetVersion);
     }
 
 
     private String getFacetVersion (final UserLibraryProviderInstallOperationConfig config)
     {
         return config.getProjectFacetVersion().getVersionString();
     }
 
 
     private String getLibraryVersion (final UserLibraryProviderInstallOperationConfig config)
     {
         String libraryVersion = null;
 
         try
         {
             for (final IClasspathEntry cpe : config.resolve())
             {
                 if (isLibrary(cpe))
                 {
                     final File libraryFile = cpe.getPath().toFile();
 
                     if (libraryFile.exists() && isCorrectLibraryJar(cpe, this.classNameIdentifyingImplementationJar))
                     {
                         JarFile jarFile = null;
                         try
                         {
                             jarFile = new JarFile(libraryFile);
                             libraryVersion = getLibraryVersion(jarFile);
                         }
                         finally
                         {
                             if (jarFile != null)
                                 jarFile.close();
                         }
                     }
                 }
             }
         }
         catch (final IOException e)
         {
             JSFCommonPlugin.log(e, e.getLocalizedMessage());
         }
 
         return libraryVersion;
     }
 
 
     private boolean isLibrary (final IClasspathEntry cpe)
     {
         return cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY;
     }
 
 
     private boolean isCorrectLibraryJar (final IClasspathEntry cpe,
                                          final String classNameIdentifyingJar)
     throws IOException
     {
         final File libraryFile = cpe.getPath().toFile();
 
         if (!libraryFile.exists())
             return false;
 
         ZipFile zipFile = null;
 
         try
         {
             zipFile = new ZipFile(libraryFile);
 
             for (final Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();)
             {
                 final ZipEntry entry = entries.nextElement();
                 final String entryName = entry.getName();
                 if (entryName.equals(classNameIdentifyingJar))
                     return true;
             }
         }
         finally
         {
             if (zipFile != null)
                 zipFile.close();
         }
 
         return false;
     }
 
 
     /**
      * @param jarFile
      *            Library jar file to read
     * 
     * @return Version of the specified Jar. Uses the manifest
     *         Specification-Version entry. If that is not available, then uses
     *         the Implementation-Version entry.
     * 
      * @throws IOException
      */
     protected String getLibraryVersion (final JarFile jarFile)
     throws IOException
     {
         final Manifest manifest = jarFile.getManifest();
 
        if (manifest == null)
            return null;
 
        final Attributes attributes = manifest.getMainAttributes();

        final String specificationVersion = attributes.getValue(MANIFEST_SPECIFICATION_VERSION);
        if (specificationVersion != null)
            return specificationVersion;

        final String implementationVersion = manifest.getMainAttributes().getValue(MANIFEST_IMPLEMENTATION_VERSION);
        return implementationVersion;
     }
 }
