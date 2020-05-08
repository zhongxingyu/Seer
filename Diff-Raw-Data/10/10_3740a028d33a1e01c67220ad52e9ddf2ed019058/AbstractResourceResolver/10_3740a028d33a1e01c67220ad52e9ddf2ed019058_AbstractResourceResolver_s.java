 package au.com.sensis.mobile.crf.service;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import au.com.sensis.mobile.crf.config.ConfigurationFactory;
 import au.com.sensis.mobile.crf.config.DeploymentMetadata;
 import au.com.sensis.mobile.crf.config.Group;
 import au.com.sensis.mobile.crf.debug.ResourceResolutionTree;
 import au.com.sensis.mobile.crf.debug.ResourceResolutionTreeHolder;
 import au.com.sensis.mobile.crf.debug.ResourceTreeNodeBean;
 import au.com.sensis.mobile.crf.exception.ResourceResolutionRuntimeException;
 import au.com.sensis.mobile.crf.util.FileIoFacadeFactory;
 import au.com.sensis.wireless.common.volantis.devicerepository.api.Device;
 
 /**
  * Standard base class for {@link ResourceResolver}s.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public abstract class AbstractResourceResolver implements ResourceResolver {
 
     private static final String EXTENSION_LEADING_DOT = ".";
 
     /**
      * Separator character for resource paths.
      */
     protected static final String RESOURCE_SEPARATOR = "/";
 
     private final String abstractResourceExtension;
     private final File rootResourcesDir;
     private final ResourceResolutionWarnLogger resourceResolutionWarnLogger;
     private final DeploymentMetadata deploymentMetadata;
     private final ConfigurationFactory configurationFactory;
 
     private final ResourceCache resourceCache;
 
     /**
      * Constructor.
      *
      * @param commonParams
      *            Holds the common parameters used in constructing all {@link ResourceResolver}s.
      * @param abstractResourceExtension
      *            Extension of resources (eg. "css" or "crf") that this class
      *            knows how to resolve.
      * @param rootResourcesDir
      *            Root directory where the real resources that this resolver
      *            handles are stored.
      */
     public AbstractResourceResolver(final ResourceResolverCommonParamHolder commonParams,
             final String abstractResourceExtension,
             final File rootResourcesDir) {
 
         validateAbstractResourceExtension(abstractResourceExtension);
         validateRootResourcesDir(rootResourcesDir);
 
         resourceResolutionWarnLogger = commonParams.getResourceResolutionWarnLogger();
         deploymentMetadata = commonParams.getDeploymentMetadata();
         configurationFactory = commonParams.getConfigurationFactory();
 
         this.abstractResourceExtension =
             prefixWithLeadingDotIfRequired(abstractResourceExtension);
         this.rootResourcesDir = rootResourcesDir;
 
         resourceCache = commonParams.getResourceCache();
 
     }
 
     /**
      * Template method for resolving requested resource paths to {@link Resource}s.
      *
      * {@inheritDoc}
      */
     @Override
     public final List<Resource> resolve(final String requestedResourcePath, final Device device)
         throws ResourceResolutionRuntimeException {
 
         if (isRecognisedAbstractResourceRequest(requestedResourcePath)) {
 
             return resolveRecognisedPath(requestedResourcePath, device);
 
         } else {
 
             debugLogRequestIgnored(requestedResourcePath);
 
             return new ArrayList<Resource>();
         }
     }
 
     /**
      * {@inheritDoc}.
      */
     private List<Resource> resolveRecognisedPath(final String requestedResourcePath,
             final Device device) throws ResourceResolutionRuntimeException {
 
         final ResourceCacheKey resourceCacheKey =
                 createResourceCacheKey(requestedResourcePath, device);
         final ResourceCacheEntry cachedResources = getCachedResources(resourceCacheKey);
         if (cachedEntryIsValid(cachedResources)) {
             addResourcesToResourceResolutionTreeIfEnabled(cachedResources.getResourcesAsList());
             logWarningIfEmptyCachedResources(requestedResourcePath, device, cachedResources);
             return cachedResources.getResourcesAsList();
         } else if (cachedEntryHasEmptyResources(cachedResources)
                 && !cachedResources.maxRefreshCountReached()) {
             logWarningIfEmptyCachedResourcesToBeRefreshed(requestedResourcePath, device,
                     cachedResources);
         }
 
         final List<Resource> resolvedResources = doResolve(requestedResourcePath, device);
 
         addResourcesToResourceResolutionTreeIfEnabled(resolvedResources);
         updateResourceCache(resourceCacheKey, cachedResources, resolvedResources);
         logWarningIfEmptyResolvedResources(requestedResourcePath, device, resolvedResources);
 
         return resolvedResources;
 
     }
 
     private void logWarningIfEmptyCachedResourcesToBeRefreshed(final String requestedResourcePath,
             final Device device, final ResourceCacheEntry resourceCacheEntry) {
         if (getResourceResolutionWarnLogger().isWarnEnabled()) {
             getResourceResolutionWarnLogger().warn(
                     "Empty cached resources found for requested resource '" + requestedResourcePath
                             + "' and device " + device + " but refreshCount is "
                             + resourceCacheEntry.getRefreshCount() + ". Will refresh the entry.");
         }
     }
 
     private void logWarningIfEmptyResolvedResources(final String requestedResourcePath,
             final Device device, final List<Resource> resolvedResources) {
         if (resolvedResources != null && resolvedResources.isEmpty()
                 && getResourceResolutionWarnLogger().isWarnEnabled()) {
             getResourceResolutionWarnLogger().warn(
                     "No resource was found for requested resource '" + requestedResourcePath
                             + "' and device " + device);
         }
     }
 
     private void logWarningIfEmptyCachedResources(final String requestedResourcePath,
             final Device device, final ResourceCacheEntry cachedResources) {
         if (cachedResources != null && cachedResources.isEmptyResources()
                 && getResourceResolutionWarnLogger().isWarnEnabled()) {
             getResourceResolutionWarnLogger().warn(
                     "Cached empty resources found and returned for requested resource '"
                             + requestedResourcePath + "' and device " + device
                             + ". refreshCount is " + cachedResources.getRefreshCount());
         }
 
     }
 
     private void updateResourceCache(final ResourceCacheKey resourceCacheKey,
             final ResourceCacheEntry previousCacheEntry, final List<Resource> resolvedResources) {
         if (resourceCacheKey != null) {
             final Resource[] resolvedResourcesArray = resolvedResources.toArray(new Resource[] {});
             if (previousCacheEntry != null) {
                 previousCacheEntry.incrementRefreshCountRateLimited();
                 previousCacheEntry.setResources(resolvedResourcesArray);
                 getResourceCache().put(resourceCacheKey, previousCacheEntry);
             } else {
                 getResourceCache().put(
                         resourceCacheKey,
                         new ResourceCacheEntryBean(resolvedResourcesArray, getResourceCache()
                                 .getResourcesNotFoundMaxRefreshCount(), getResourceCache()
                                 .getResourcesNotFoundRefreshCountUpdateMilliseconds()));
             }
         }
     }
 
     private boolean cachedEntryIsValid(final ResourceCacheEntry cachedResources) {
         return cachedEntryHasNonEmptyResources(cachedResources)
                || cachedEntryHasEmptyResources(cachedResources) && cachedResources
                        .maxRefreshCountReached();
     }
 
     private boolean cachedEntryHasNonEmptyResources(final ResourceCacheEntry cachedResources) {
         return cachedResources != null && !cachedResources.isEmptyResources();
     }
 
     private boolean cachedEntryHasEmptyResources(final ResourceCacheEntry cachedResources) {
         return cachedResources != null && cachedResources.isEmptyResources();
     }
 
     /**
      * Invoked by {@link #resolve(String, Device)} if
      * {@link #isRecognisedAbstractResourceRequest(String)} resturns true.
      *
      * @param requestedResourcePath
      *            Requested path. eg. /WEB-INF/view/jsp/detal/bdp.crf.
      * @param device
      *            {@link Device} to perform the path mapping for.
      * @return List of {@link Resource}s containing the results. If no resources
      *         can be resolved, an empty list is returned. May not be null.
      */
     protected abstract List<Resource> doResolve(final String requestedResourcePath,
             final Device device);
 
     /**
      * Returns the {@link Resource}s that are resolved for the given {@link Group}.
      *
      * @param requestedResourcePath for the resource to be resolved
      * @param device
      *            {@link Device} to perform the path mapping for.
      * @param group in which to look for the given requestedResourcePath
      * @return a list of {@link Resource}s resolved for the given {@link Group}
      */
     protected final List<Resource> resolveForGroup(final String requestedResourcePath,
             final Device device, final Group group) {
 
         debugLogAttemptingResolution(requestedResourcePath);
 
         final List<Resource> resolvedResources =
             doResolveForGroup(requestedResourcePath, device, group);
 
         debugLogResolutionResults(requestedResourcePath, resolvedResources);
 
         return resolvedResources;
     }
 
     /**
      * Workhorse method that is only called if
      * {@link #isRecognisedAbstractResourceRequest(String)} is true. Resolves
      * the requested resource path to one or more {@link Resource}s if they
      * exist. Returns an empty list if none are found. The default
      * implementation simply creates a new {@link Resource} from the given
      * parameters if the path created by
      * {@link #createNewResourcePath(String, Group)} exists.
      *
      * @param requestedResourcePath
      *            The original resource that was requested.
      * @param device
      *            {@link Device} to perform the path mapping for.
      * @param group
      *            {@link Group} that the
      *            {@link au.com.sensis.wireless.common.volantis.devicerepository.api.Device}
      *            for the current request belongs to.
      *
      * @return {@link List} of {@link Resource}s that exist.
      * @throws ResourceResolutionRuntimeException
      *             Thrown if any error occurs.
      */
     protected List<Resource> doResolveForGroup(final String requestedResourcePath,
             final Device device, final Group group)
             throws ResourceResolutionRuntimeException {
 
         final String newResourcePath =
             createNewResourcePath(requestedResourcePath, group);
 
         debugLogCheckingIfPathExists(newResourcePath);
 
         if (exists(newResourcePath)) {
             return Arrays.asList(createResource(requestedResourcePath,
                     newResourcePath, group));
         } else {
             return new ArrayList<Resource>();
         }
     }
 
     /**
      * @param newResourcePath Path to test.
      * @return true if the given new path exists in
      *         {@link #getRootResourceDir()}.
      */
     protected final boolean exists(final String newResourcePath) {
         return FileIoFacadeFactory.getFileIoFacadeSingleton().fileExists(
                 getRootResourcesDir(), newResourcePath);
     }
 
     /**
      * Create a {@link Resource} from the requested path and the new path that
      * it maps to.
      *
      * @param requestedResourcePath
      *            Requested path.
      * @param newPath
      *            New path that the requested path maps to.
      * @param group {@link Group} that the new path was found in.
      * @return new {@link Resource} created from the requested path and the new
      *         path that it maps to.
      */
     protected final Resource createResource(final String requestedResourcePath,
             final String newPath, final Group group) {
         return new ResourceBean(requestedResourcePath, newPath,
                 getRootResourcesDir(), group);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean supports(final String requestedResourcePath) {
         return isRecognisedAbstractResourceRequest(requestedResourcePath);
     }
 
     /**
      * Returns true if the requested resource path is
      * for a recognised abstract resource that this {@link ResourceResolver}
      * can handle. The default implementation returns true if the requested
      * resource path ends with {@link #getAbstractResourceExtension()}.
      *
      * @param requestedResourcePath
      *            The path of the resource requested.
      * @return true if the requested resource path is for a recognised abstract
      *         resource that this {@link ResourceResolver} can handle.
      */
     protected boolean isRecognisedAbstractResourceRequest(
             final String requestedResourcePath) {
         return requestedResourcePath.endsWith(getAbstractResourceExtension());
     }
 
     /**
      * The actual algorithm for mapping the requested resource path to the
      * candidate real resource path. Will only ever be called if
      * {@link #isRecognisedAbstractResourceRequest(String)} returns true. The
      * default implementation invokes
      * {@link #insertGroupNameAndDeploymentVersionIntoPath(String, Group)} then replaces the
      * {@link #getAbstractResourceExtension()} with
      * {@link #getRealResourcePathExtension()}.
      *
      * @param requestedResourcePath
      *            The path of the resource requested.
      * @param group
      *            {@link Group} that the
      *            {@link au.com.sensis.wireless.common.volantis.devicerepository.api.Device}
      *            for the current request belongs to.
      * @return The candidate real resource path that the requested resource path
      *         maps to. May not be null.
      */
     protected String createNewResourcePath(final String requestedResourcePath,
             final Group group) {
 
         return replaceAbstractResourceExtensionWithReal(insertGroupNameAndDeploymentVersionIntoPath(
                 requestedResourcePath, group));
     }
 
     private String replaceAbstractResourceExtensionWithReal(
             final String resourcePath) {
         return StringUtils.removeEnd(resourcePath,
                 getAbstractResourceExtension())
                 + getRealResourcePathExtension();
     }
 
     /**
      * @return the extension of resources (eg. "css" or "crf") that this class
      *         knows how to resolve.
      */
     protected final String getAbstractResourceExtension() {
         return abstractResourceExtension;
     }
 
     /**
      * Returns the file extension to use for the generated,
      * real resource paths.
      *
      * @return the file extension to use for the generated, real resource paths.
      */
     protected abstract String getRealResourcePathExtension();
 
     /**
      * Insert the name of the given {@link Group}, plus the
      * {@link #getDeploymentMetadata()} version into the requested resource path
      * and return the result. The default implementation simply inserts the
      * version and group name at the start of the path, each component separated
      * from the rest of the path by {@link #RESOURCE_SEPARATOR}.
      *
      * @param requestedResourcePath
      *            The path of the resource requested.
      * @param group
      *            {@link Group} that the
      *            {@link au.com.sensis.wireless.common.volantis.devicerepository.api.Device}
      *            for the current request belongs to.
      * @return the result of inserting the the name of the given {@link Group},
      *         plus the {@link #getDeploymentMetadata()} version into the
      *         requested resource path.
      */
     protected String insertGroupNameAndDeploymentVersionIntoPath(
             final String requestedResourcePath, final Group group) {
         if (StringUtils.isNotBlank(getResourceSubDirName())) {
             return getDeploymentMetadata().getVersion() + RESOURCE_SEPARATOR
                     + getResourceSubDirName() + RESOURCE_SEPARATOR + group.getName()
                     + RESOURCE_SEPARATOR + requestedResourcePath;
         } else {
             return getDeploymentMetadata().getVersion() + RESOURCE_SEPARATOR + group.getName()
                     + RESOURCE_SEPARATOR + requestedResourcePath;
         }
     }
 
     /**
      * @return Name of an extra sub directory to be inserted into the
      *         resolved path by {@link #insertGroupNameAndDeploymentVersionIntoPath(String, Group)}.
      */
     protected abstract String getResourceSubDirName();
 
     /**
      * Debug friendly name of the type of resource that this
      * {@link ResourceResolver} handles.
      *
      * @return Debug friendly name of the type of resource that this
      *         {@link ResourceResolver} handles.
      */
     protected abstract String getDebugResourceTypeName();
 
     /**
      * The {@link Logger} to use for this
      * {@link ResourceResolver}. Allows the
      * {@link #resolve(String, Group)} to log messages that clearly
      * indicate what the actual {@link ResourceResolver} implementation being
      * executed is.
      *
      * @return The {@link Logger} to use for this {@link ResourceResolver}.
      *         Allows the {@link #resolve(String, Group)} to log
      *         messages that clearly indicate what the actual
      *         {@link ResourceResolver} implementation being executed is.
      */
     protected abstract Logger getLogger();
 
     /**
      * @return the rootResourcesDir Root directory where the real resources that
      *         this resolver handles are stored.
      */
     protected final File getRootResourcesDir() {
         return rootResourcesDir;
     }
 
     /**
      * @return the {@link ConfigurationFactory} from which to obtain the
      * {@link au.com.sensis.mobile.crf.config.UiConfiguration}.
      */
     private ConfigurationFactory getConfigurationFactory() {
         return configurationFactory;
     }
 
     /**
      * @return the {@link ResourceResolutionWarnLogger} to use to log warnings.
      */
     protected final ResourceResolutionWarnLogger getResourceResolutionWarnLogger() {
         return resourceResolutionWarnLogger;
     }
 
     private void validateAbstractResourceExtension(
             final String abstractResourceExtension) {
         if (StringUtils.isBlank(abstractResourceExtension)) {
             throw new IllegalArgumentException(
                     "abstractResourceExtension must not be blank: '"
                     + abstractResourceExtension + "'");
         }
     }
 
     private void validateRootResourcesDir(final File resourcesRootDir) {
         if (!resourcesRootDir.exists() || !resourcesRootDir.isDirectory()) {
             throw new IllegalArgumentException(
                     "rootResourcesDir must be a directory: '"
                     + resourcesRootDir + "'");
         }
     }
 
     private String prefixWithLeadingDotIfRequired(final String path) {
         if (path.startsWith(EXTENSION_LEADING_DOT)) {
             return path;
         } else {
             return EXTENSION_LEADING_DOT + path;
         }
     }
 
     /**
      * @param device {@link Device} to get iterator for.
      * @param requestedResourcePath Requested path.
      * @return Iterator for groups that match the given device.
      */
     protected final Iterator<Group> getMatchingGroupIterator(final Device device,
             final String requestedResourcePath) {
 
         return getConfigurationFactory().getUiConfiguration(requestedResourcePath)
                 .matchingGroupIterator(device);
     }
 
     /**
      * @param device
      *            {@link Device} to get iterator for.
      * @param requestedResourcePath
      *            Requested path.
      * @return groups that match the given device.
      */
     protected final Group[] getMatchingGroups(final Device device,
             final String requestedResourcePath) {
 
         return getConfigurationFactory().getUiConfiguration(requestedResourcePath).matchingGroups(
                 device);
     }
 
     private void debugLogAttemptingResolution(final String requestedResourcePath) {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug(
                     "Attempting to resolve requested resource '"
                     + requestedResourcePath + "'");
         }
     }
 
     private void debugLogCheckingIfPathExists(final String newResourcePath) {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("Checking if resource exists: '" + newResourcePath + "'");
         }
     }
 
     private void debugLogResolutionResults(final String requestedResourcePath,
             final List<Resource> resolvedResources) {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug(
                     "Resolved requested resource '" + requestedResourcePath
                     + "' to '" + resolvedResources + "'");
         }
     }
 
     private void debugLogRequestIgnored(final String requestedResourcePath) {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug(
                     "Requested resource '" + requestedResourcePath
                     + "' is not for a "
                     + getDebugResourceTypeName()
                     + " file. Ignoring the request.");
         }
     }
 
     /**
      * Log a debug message that a {@link Group} is being checked for a given requested path.
      * @param requestedResourcePath Requested resource path.
      * @param currGroup {@link Group} being checked.
      */
     protected final void debugLogCheckingGroup(final String requestedResourcePath,
             final Group currGroup) {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("Looking for '" + requestedResourcePath
                     + "' in matching group: " + currGroup);
         }
     }
 
     private DeploymentMetadata getDeploymentMetadata() {
         return deploymentMetadata;
     }
 
     /**
      * @return the resourceCache
      */
     private ResourceCache getResourceCache() {
         return resourceCache;
     }
 
     /**
      * Log a debug message that resources were found in the cache.
      */
     protected final void debugLogResourcesFoundInCache() {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("Returning resources from cache.");
         }
     }
 
     /**
      * Log a debug message that resources were not found in the cache.
      */
     protected final void debugLogResourcesNotFoundInCache() {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("Resources not found in cache. Will resolve them.");
         }
     }
 
     /**
      * Add the Resources to the {@link ResourceResolutionTree} for the current
      * thread.
      *
      * @param resources
      *            Resources to add to the {@link ResourceResolutionTree} for the
      *            current thread.
      */
     protected final void addResourcesToResourceResolutionTreeIfEnabled(
             final List<Resource> resources) {
         if (getResourceResolutionTree().isEnabled()) {
             for (final Resource currResource : resources) {
                 addResourceToResourceResolutionTreeIfEnabled(currResource);
             }
         }
     }
 
     /**
      * Add the Resource to the {@link ResourceResolutionTree} for the current
      * thread.
      *
      * @param resource
      *            Resource to add to the {@link ResourceResolutionTree} for the
      *            current thread.
      */
     protected void addResourceToResourceResolutionTreeIfEnabled(final Resource resource) {
         if (getResourceResolutionTree().isEnabled()) {
             getResourceResolutionTree().addChildToCurrentNode(new ResourceTreeNodeBean(resource));
         }
     }
 
     private ResourceResolutionTree getResourceResolutionTree() {
         return ResourceResolutionTreeHolder.getResourceResolutionTree();
     }
 
     /**
      * Create a key to be used for the {@link #getResourceCache()}.
      *
      * @param requestedResourcePath Path being requested.
      * @param device Device the request is for.
      * @return a new key instance to be used for the {@link #getResourceCache()}.
      */
     protected final ResourceCacheKey createResourceCacheKey(final String requestedResourcePath,
             final Device device) {
         final Group[] matchingGroups = getMatchingGroups(device, requestedResourcePath);
         return new ResourceCacheKeyBean(requestedResourcePath, matchingGroups);
     }
 
     /**
      * Get cached resources for the given key.
      *
      * @param resourceCacheKey Key to look up.
      * @return cached resources for the given key.
      */
     protected final ResourceCacheEntry getCachedResources(final ResourceCacheKey resourceCacheKey) {
         if (resourceCacheKey != null && getResourceCache().contains(resourceCacheKey)) {
             debugLogResourcesFoundInCache();
             return getResourceCache().get(resourceCacheKey);
         } else {
             debugLogResourcesNotFoundInCache();
             return null;
         }
 
     }
 }
