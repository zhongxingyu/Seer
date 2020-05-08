 /*******************************************************************************
  * Copyright (c) 2012 SAP AG
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   SAP AG - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.web.war.deployer;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import org.eclipse.gemini.web.core.InstallationOptions;
 import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
 import org.eclipse.osgi.framework.adaptor.BundleClassLoader;
 import org.eclipse.osgi.framework.internal.core.BundleHost;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.nano.core.KernelConfig;
 import org.eclipse.virgo.nano.deployer.SimpleDeployer;
 import org.eclipse.virgo.nano.deployer.StandardDeploymentIdentity;
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
 import org.eclipse.virgo.nano.deployer.util.BundleInfosUpdater;
 import org.eclipse.virgo.nano.deployer.util.BundleLocationUtil;
 import org.eclipse.virgo.nano.deployer.util.StatusFileModificator;
 import org.eclipse.virgo.util.io.FileSystemUtils;
 import org.eclipse.virgo.util.io.IOUtils;
 import org.eclipse.virgo.util.io.JarUtils;
 import org.eclipse.virgo.util.io.PathReference;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.service.component.ComponentContext;
 import org.osgi.service.packageadmin.PackageAdmin;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @SuppressWarnings("deprecation")
 public class WARDeployer implements SimpleDeployer {
 
     private static final boolean NOT_A_FRAGMENT = false;
 
     private static final String KERNEL_HOME_PROP = "org.eclipse.virgo.kernel.home";
 
     private static final String WAR = "war";
 
     private static final boolean STATUS_OK = true;
 
     private static final boolean STATUS_ERROR = false;
 
     private static final String PICKUP_DIR = "pickup";
 
     private static final String SLASH = "/";
 
     private static final char SLASH_CHAR = '/';
 
     private static final char DOT = '.';
 
     static final String LAST_MODIFIED = "last-modified";
 
     static final String EMPTY_STRING = "";
 
     private static final String WEBAPPS_DIR = "webapps";
 
     private static final String HEADER_WEB_CONTEXT_PATH = "Web-ContextPath";
 
     private static final String DEFAULT_CONTEXT_PATH = "/";
 
     private static final String ROOT_WAR_NAME = "ROOT";
 
     private static final char HASH_SIGN = '#';
 
     private static final String PROPERTY_WAB_HEADERS = "WABHeaders";
 
     private static final String PROPERTY_VALUE_WAB_HEADERS_STRICT = "strict";
 
     private static final String PROPERTY_VALUE_WAB_HEADERS_DEFAULTED = "defaulted";
 
     private static final String HEADER_DEFAULT_WAB_HEADERS = "org-eclipse-gemini-web-DefaultWABHeaders";
 
     private static final String WEB_BUNDLE_MODULE_TYPE = "web-bundle";
 
     private EventLogger eventLogger;
 
     private BundleInfosUpdater bundleInfosUpdaterUtil;
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private BundleContext bundleContext;
 
     private PackageAdmin packageAdmin;
 
     private WebBundleManifestTransformer webBundleManifestTransformer;
 
     private long largeFileCopyTimeout = 4000;
 
     private File pickupDir;
 
     private File webAppsDir;
 
     private KernelConfig kernelConfig;
 
     private File kernelHomeFile;
 
     public WARDeployer() {
         warDeployerInternalInit(null);
     }
 
     public WARDeployer(BundleContext bundleContext, PackageAdmin packageAdmin, WebBundleManifestTransformer webBundleManifestTransformer,
         EventLogger eventLogger, KernelConfig kernelConfig) {
         warDeployerInternalInit(bundleContext);
         this.packageAdmin = packageAdmin;
         this.webBundleManifestTransformer = webBundleManifestTransformer;
         this.eventLogger = eventLogger;
         this.kernelConfig = kernelConfig;
     }
 
     public void activate(ComponentContext context) {
         warDeployerInternalInit(context.getBundleContext());
     }
 
     @Override
     public final boolean deploy(URI path) {
         this.eventLogger.log(WARDeployerLogEvents.NANO_INSTALLING, new File(path).toString());
         final String warName = extractDecodedWarNameFromString(path.toString());
         final File deployedFile = new File(path);
         final File warDir = new File(this.webAppsDir, replaceHashSigns(warName, DOT));
         StatusFileModificator.deleteStatusFile(warName, this.pickupDir);
 
         long bundleId = -1L;
         final long lastModified = deployedFile.lastModified();
 
         if (!canWrite(path)) {
             this.logger.error("Cannot open the file " + path + " for writing. The configured timeout is " + this.largeFileCopyTimeout + ".");
             StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
             this.eventLogger.log(WARDeployerLogEvents.NANO_INSTALLING_ERROR, path);
             return STATUS_ERROR;
         }
         final Bundle installed;
         try {
             // Extract the war file to the webapps directory. Use always JarUtils.unpackToDestructive.
             JarUtils.unpackToDestructive(new PathReference(deployedFile), new PathReference(warDir));
             // make the manifest transformation in the unpacked location
             transformUnpackedManifest(warDir, warName);
 
             // install the bundle
             installed = this.bundleContext.installBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, warDir));
         } catch (Exception e) {
             this.eventLogger.log(WARDeployerLogEvents.NANO_INSTALLING_ERROR, e, path);
             StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
             return STATUS_ERROR;
         }
 
         this.eventLogger.log(WARDeployerLogEvents.NANO_INSTALLED, installed.getSymbolicName(), installed.getVersion());
         this.eventLogger.log(WARDeployerLogEvents.NANO_WEB_STARTING, installed.getSymbolicName(), installed.getVersion());
         try {
             installed.start();
         } catch (Exception e) {
             this.eventLogger.log(WARDeployerLogEvents.NANO_STARTING_ERROR, e, installed.getSymbolicName(), installed.getVersion());
             StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
             return STATUS_ERROR;
         }
 
         this.eventLogger.log(WARDeployerLogEvents.NANO_WEB_STARTED, installed.getSymbolicName(), installed.getVersion());
 
         bundleId = installed.getBundleId();
         // now update bundle's info
         if (this.logger.isInfoEnabled()) {
             this.logger.info("Bundles info will be updated for war with path '" + path + "'.");
         }
 
         try {
             if (this.bundleInfosUpdaterUtil != null && this.bundleInfosUpdaterUtil.isAvailable()) {
                 BundleInfosUpdater.registerToBundlesInfo(installed, getLocationForBundlesInfo(path), NOT_A_FRAGMENT);
             }
         } catch (Exception e) {
             this.eventLogger.log(WARDeployerLogEvents.NANO_PERSIST_ERROR, e, installed.getSymbolicName(), installed.getVersion());
             StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
             return STATUS_ERROR;
         }
 
         StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_OK, bundleId, lastModified);
         return STATUS_OK;
     }
 
     @Override
     public boolean isDeployFileValid(File file) {
         JarFile jarFile = null;
         try {
             jarFile = new JarFile(file);
         } catch (IOException e) {
             this.logger.error("The deployed file '" + file.getAbsolutePath() + "' is an invalid zip file.");
             return false;
         } finally {
             try {
                 if (jarFile != null) {
                     jarFile.close();
                 }
             } catch (IOException e) {
                 // do nothing
             }
         }
         return true;
     }
 
     private String extractDecodedWarNameFromString(String path) {
         final String warName = path.substring(path.lastIndexOf(SLASH) + 1, path.length() - 4);
         return URLDecoder.decode(warName);
     }
 
     @Override
     public final boolean undeploy(Bundle bundle) {
         String bundleLocation = removeTrailingFileSeparator(bundle.getLocation());
         String warPath = extractWarPath(bundleLocation);
         final File warDir = new File(replaceHashSigns(warPath, DOT));
         String warName = extractWarNameFromBundleLocation(warPath);
 
         String statusFilePrefix = calculateStatusFilePrefix(bundle, warName);
         StatusFileModificator.deleteStatusFile(statusFilePrefix, this.pickupDir);
 
         if (bundle != null) {
             try {
                 if (this.logger.isInfoEnabled()) {
                     this.logger.info("Removing bundle '" + bundle.getSymbolicName() + "' version '" + bundle.getVersion() + "' from bundles.info.");
                 }
                 if (this.bundleInfosUpdaterUtil != null && this.bundleInfosUpdaterUtil.isAvailable()) {
                     String locationForBundlesInfo = BundleLocationUtil.getRelativisedURI(kernelHomeFile, warDir).toString();
                     BundleInfosUpdater.unregisterToBundlesInfo(bundle, locationForBundlesInfo, NOT_A_FRAGMENT);
                     this.logger.info("Successfully removed bundle '" + bundle.getSymbolicName() + "' version '" + bundle.getVersion()
                         + "' from bundles.info.");
                 } else {
                     this.logger.error("BundleInfosUpdater not available. Failed to remove bundle '" + bundle.getSymbolicName() + "' version '"
                         + bundle.getVersion() + "' from bundles.info.");
                 }
                 this.eventLogger.log(WARDeployerLogEvents.NANO_STOPPING, bundle.getSymbolicName(), bundle.getVersion());
                 bundle.stop();
                 this.eventLogger.log(WARDeployerLogEvents.NANO_STOPPED, bundle.getSymbolicName(), bundle.getVersion());
                 this.eventLogger.log(WARDeployerLogEvents.NANO_UNINSTALLING, bundle.getSymbolicName(), bundle.getVersion());
                 bundle.uninstall();
                 // we need to decode the path before delete or a /webapps entry might leak
                 FileSystemUtils.deleteRecursively(new File(URLDecoder.decode(warDir.getAbsolutePath())));
                 this.eventLogger.log(WARDeployerLogEvents.NANO_UNINSTALLED, bundle.getSymbolicName(), bundle.getVersion());
             } catch (BundleException e) {
                 this.eventLogger.log(WARDeployerLogEvents.NANO_UNDEPLOY_ERROR, e, bundle.getSymbolicName(), bundle.getVersion());
                 StatusFileModificator.createStatusFile(statusFilePrefix, this.pickupDir, StatusFileModificator.OP_UNDEPLOY, STATUS_ERROR, -1, -1);
                 return STATUS_ERROR;
             } catch (IOException e) {
                 this.eventLogger.log(WARDeployerLogEvents.NANO_PERSIST_ERROR, e, bundle.getSymbolicName(), bundle.getVersion());
             } catch (URISyntaxException e) {
                 this.eventLogger.log(WARDeployerLogEvents.NANO_PERSIST_ERROR, e, bundle.getSymbolicName(), bundle.getVersion());
             }
         }
 
         StatusFileModificator.createStatusFile(statusFilePrefix, this.pickupDir, StatusFileModificator.OP_UNDEPLOY, STATUS_OK, -1, -1);
         return STATUS_OK;
     }
 
     private String calculateStatusFilePrefix(Bundle bundle, String warName) {
         if (warName.contains(String.valueOf(DOT))) {
             String webContextPath = bundle.getHeaders().get(HEADER_WEB_CONTEXT_PATH);
             if ((warName.charAt(0) == DOT && webContextPath.indexOf(DOT) == 1) || (warName.charAt(0) != DOT && webContextPath.indexOf(DOT) != 1)) {
                 webContextPath = webContextPath.substring(1);
             }
             if (webContextPath.contains(SLASH) && webContextPath.replace(SLASH_CHAR, DOT).equals(warName)) {
                 return URLDecoder.decode(webContextPath.replace(SLASH_CHAR, HASH_SIGN));
             }
         }
         return URLDecoder.decode(warName);
     }
 
     private String extractWarNameFromBundleLocation(String bundleLocation) {
         String[] pathItems = bundleLocation.split(SLASH);
         if (pathItems.length > 0) {
             return pathItems[pathItems.length - 1];
         } else {
             logger.warn("Cannot calculate war name on the given warPath [" + bundleLocation + "]");
             return "";
         }
     }
 
     private String extractWarPath(String bundleLocation) {
         String warPath;
         if (bundleLocation.startsWith(BundleLocationUtil.REFERENCE_FILE_PREFIX)) {
             warPath = bundleLocation.substring(BundleLocationUtil.REFERENCE_FILE_PREFIX.length());
         } else {
             warPath = bundleLocation;
         }
         return warPath;
     }
 
     private String removeTrailingFileSeparator(String bundleLocation) {
         if (bundleLocation.endsWith(File.separator)) {
             bundleLocation = bundleLocation.substring(0, bundleLocation.length() - 1);
         }
         return bundleLocation;
     }
 
     @Override
     public final boolean update(URI path) {
         final String warName = extractDecodedWarNameFromString(path.toString());
         final File updatedFile = new File(path);
         final File warDir = new File(this.webAppsDir, replaceHashSigns(warName, DOT));
 
         if (!warDir.exists()) {
             this.logger.info("Can't update artifact for path '" + path + "'. It is not deployed.");
         }
 
         StatusFileModificator.deleteStatusFile(warName, this.pickupDir);
 
         final long bundleId = -1L;
         final long lastModified = updatedFile.lastModified();
 
         if (!canWrite(path)) {
             this.logger.error("Cannot open the file [" + path + "] for writing. Timeout is [" + this.largeFileCopyTimeout + "].");
             StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
             this.eventLogger.log(WARDeployerLogEvents.NANO_UPDATING_ERROR, path);
             return STATUS_ERROR;
         }
 
         final Bundle bundle = this.bundleContext.getBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, warDir));
         if (bundle != null) {
             try {
             	bundle.stop();
             	if (bundle instanceof BundleHost) {
             		BundleClassLoader loader = (BundleClassLoader)((BundleHost) bundle).getClassLoader(); 
             		loader.close();
             	}
                 // Extract the war file to the webapps directory. Use always JarUtils.unpackToDestructive.
                 JarUtils.unpackToDestructive(new PathReference(updatedFile), new PathReference(warDir));
                 // make the manifest transformation in the unpacked location
                 transformUnpackedManifest(warDir, warName);
                 this.eventLogger.log(WARDeployerLogEvents.NANO_UPDATING, bundle.getSymbolicName(), bundle.getVersion());
                 bundle.update();
                 if (this.packageAdmin != null) {
                     this.packageAdmin.refreshPackages(new Bundle[] { bundle });
                     this.logger.info("Update of file with path [" + path + "] is successful.");
                 }
                 bundle.start();
                 this.eventLogger.log(WARDeployerLogEvents.NANO_UPDATED, bundle.getSymbolicName(), bundle.getVersion());
             } catch (Exception e) {
                 this.eventLogger.log(WARDeployerLogEvents.NANO_UPDATE_ERROR, e, bundle.getSymbolicName(), bundle.getVersion());
                 StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, bundleId, lastModified);
                 return STATUS_ERROR;
             }
             StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_OK, bundleId, lastModified);
         } else {
             deploy(path);
         }
         return STATUS_OK;
     }
 
     public void setLargeFileCopyTimeout(long timeout) {
         if (this.logger.isInfoEnabled()) {
             this.logger.info("setLargeFileCopyTimeout(" + timeout + ")");
         }
         this.largeFileCopyTimeout = timeout;
     }
 
     private boolean canWrite(URI path) {
         int tries = -1;
         boolean isWritable = false;
         // Some big files are copied very slowly, but the event is received
         // immediately.
         // So we will wait 0.5 x 240 i.e. 2 minutes
         final long timeout = this.largeFileCopyTimeout / 500;
         while (tries < timeout) {
             FileInputStream fis = null;
             try {
                 fis = new FileInputStream(new File(path));
                 isWritable = true;
                 break;
             } catch (FileNotFoundException e) {
                 if (this.logger.isInfoEnabled()) {
                     this.logger.info("File is still locked.", e);
                 }
             } finally {
                 IOUtils.closeQuietly(fis);
             }
             try {
                 Thread.sleep(500);
             } catch (InterruptedException e) {
                 this.logger.error("InterruptedException occurred.", e);
             }
             tries++;
         }
         return isWritable;
     }
 
     private final void transformUnpackedManifest(File srcFile, String warName) throws IOException {
         if (srcFile == null) {
             throw new NullPointerException("Source file is null.");
         }
         if (!srcFile.isDirectory() || !srcFile.canRead()) {
             throw new IllegalArgumentException("Source file must be a readable directory [" + srcFile + "].");
         }
         File destFile = new File(srcFile, JarFile.MANIFEST_NAME);
         if (!destFile.exists()) {
             destFile.getParentFile().mkdirs();
             destFile.createNewFile();
         }
         if (!destFile.isFile() || !destFile.canRead()) {
             throw new IllegalArgumentException("Destination file must be a readable file [" + destFile + "].");
         }
 
         FileOutputStream fos = null;
         InputStream mfIS = null;
         try {
             mfIS = new FileInputStream(srcFile + File.separator + JarFile.MANIFEST_NAME);
             BundleManifest manifest = BundleManifestFactory.createBundleManifest(new InputStreamReader(mfIS));
             if (manifest.getModuleType() == null || "web".equalsIgnoreCase(manifest.getModuleType())) {
                 boolean strictWABHeaders = getStrictWABHeadersValue();
                 if (!strictWABHeaders) {
                     manifest.setHeader(HEADER_DEFAULT_WAB_HEADERS, "true");
                 }
                 manifest.setModuleType(WEB_BUNDLE_MODULE_TYPE);
                 InstallationOptions installationOptions = prepareInstallationOptions(strictWABHeaders, warName, manifest);
                 boolean isWebBundle = WebBundleUtils.isWebApplicationBundle(manifest);
                 this.webBundleManifestTransformer.transform(manifest, srcFile.toURI().toURL(), installationOptions, isWebBundle);
             } else {
                 this.logger.info("Skipping transformation of application '" + warName + "' because it is already a web bundle.");
                 return;
             }
             fos = new FileOutputStream(destFile);
             toManifest(manifest.toDictionary()).write(fos);
         } finally {
             IOUtils.closeQuietly(fos);
             IOUtils.closeQuietly(mfIS);
         }
     }
 
     private InstallationOptions prepareInstallationOptions(boolean strictWABHeaders, String warName, BundleManifest manifest) {
         Map<String, String> map = new HashMap<String, String>();
         String webContextPathHeader = manifest.getHeader(HEADER_WEB_CONTEXT_PATH);
         if (webContextPathHeader == null || webContextPathHeader.trim().length() == 0) {
             if (warName.equals(ROOT_WAR_NAME)) {
                 map.put(HEADER_WEB_CONTEXT_PATH, DEFAULT_CONTEXT_PATH);
             } else {
                 map.put(HEADER_WEB_CONTEXT_PATH, replaceHashSigns(warName, SLASH_CHAR));
             }
         }
 
         InstallationOptions installationOptions = new InstallationOptions(map);
         installationOptions.setDefaultWABHeaders(!strictWABHeaders);
 
         return installationOptions;
     }
 
     private final Manifest toManifest(Dictionary<String, String> headers) {
         Manifest manifest = new Manifest();
         Attributes attributes = manifest.getMainAttributes();
         Enumeration<String> names = headers.keys();
 
         while (names.hasMoreElements()) {
             String name = names.nextElement();
             String value = headers.get(name);
 
             attributes.putValue(name, value);
         }
         return manifest;
     }
 
     @Override
     public boolean canServeFileType(String fileType) {
         return fileType.toLowerCase().equals(WAR);
     }
 
     @Override
     public boolean isDeployed(URI path) {
         final String warName = extractDecodedWarNameFromString(path.toString());
         final File warDir = new File(this.webAppsDir, replaceHashSigns(warName, DOT));
         if (!warDir.exists()) {
             return false;
         }
         if (this.bundleContext.getBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, warDir)) == null) {
             return false;
         }
         return true;
     }
 
     @Override
     public boolean isOfflineUpdated(URI path) {
         final String warName = extractDecodedWarNameFromString(path.toString());
         final File deployFile = new File(path);
         long deployFileLastModified = deployFile.lastModified();
        long lastModifiedStatus = StatusFileModificator.getLastModifiedFromStatusFile(warName, this.pickupDir);
        if (lastModifiedStatus == -1 || deployFileLastModified == lastModifiedStatus) {
             return false;
         }
         return true;
     }
 
     @Override
     public DeploymentIdentity getDeploymentIdentity(URI path) {
         final String warName = extractDecodedWarNameFromString(path.toString());
         final File warDir = new File(this.webAppsDir, replaceHashSigns(warName, DOT));
         if (!warDir.exists()) {
             return null;
         }
         Bundle bundle = this.bundleContext.getBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, warDir));
         if (bundle == null) {
             return null;
         }
         return new StandardDeploymentIdentity(WAR, bundle.getSymbolicName(), bundle.getVersion().toString());
     }
 
     @Override
     public List<String> getAcceptedFileTypes() {
         List<String> types = new ArrayList<String>();
         types.add(WAR);
         return types;
     }
 
     private void warDeployerInternalInit(BundleContext bundleContext) {
         String kernelHome = System.getProperty("org.eclipse.virgo.kernel.home");
         if (kernelHome != null) {
             this.kernelHomeFile = new File(kernelHome);
             if (this.kernelHomeFile.exists()) {
                 File bundlesInfoFile = new File(kernelHomeFile, "configuration/org.eclipse.equinox.simpleconfigurator/bundles.info");
                 this.pickupDir = new File(kernelHomeFile, PICKUP_DIR);
                 this.webAppsDir = new File(kernelHomeFile, WEBAPPS_DIR);
                 this.bundleContext = bundleContext;
                 this.bundleInfosUpdaterUtil = new BundleInfosUpdater(bundlesInfoFile, kernelHomeFile);
             } else {
                 throw new IllegalStateException("Required location '" + this.kernelHomeFile.getAbsolutePath()
                     + "' does not exist. Check the value of the '" + KERNEL_HOME_PROP + "' propery");
             }
         } else {
             throw new IllegalStateException("Missing value for required property '" + KERNEL_HOME_PROP + "'");
         }
     }
 
     private boolean getStrictWABHeadersValue() {
         boolean strictWABHeaders = true;
         String wabHeadersPropertyValue = null;
         if (this.kernelConfig.getProperty(PROPERTY_WAB_HEADERS) != null) {
             wabHeadersPropertyValue = this.kernelConfig.getProperty(PROPERTY_WAB_HEADERS).toString();
         }
         if (wabHeadersPropertyValue != null) {
             if (PROPERTY_VALUE_WAB_HEADERS_DEFAULTED.equals(wabHeadersPropertyValue)) {
                 strictWABHeaders = false;
                 this.logger.info("Property '%s' has value [defaulted]", new String[] { PROPERTY_WAB_HEADERS });
             } else if (!PROPERTY_VALUE_WAB_HEADERS_STRICT.equals(wabHeadersPropertyValue)) {
                 this.logger.error("Property '%s' has invalid value '%s'", new String[] { PROPERTY_WAB_HEADERS, wabHeadersPropertyValue });
             }
         }
 
         return strictWABHeaders;
     }
 
     public void bindWebBundleManifestTransformer(WebBundleManifestTransformer transformer) {
         this.webBundleManifestTransformer = transformer;
     }
 
     public void unbindWebBundleManifestTransformer(WebBundleManifestTransformer transformer) {
         this.webBundleManifestTransformer = null;
     }
 
     public void bindEventLogger(EventLogger logger) {
         this.eventLogger = logger;
     }
 
     public void unbindEventLogger(EventLogger logger) {
         this.eventLogger = null;
     }
 
     public void bindPackageAdmin(PackageAdmin packageAdmin) {
         this.packageAdmin = packageAdmin;
     }
 
     public void unbindPackageAdmin(PackageAdmin packageAdmin) {
         this.packageAdmin = null;
     }
 
     public void bindKernelConfig(KernelConfig kernelConfig) {
         this.kernelConfig = kernelConfig;
     }
 
     public void unbindKernelConfig(KernelConfig kernelConfig) {
         this.kernelConfig = null;
     }
 
     @Override
     public boolean install(URI uri) {
         this.eventLogger.log(WARDeployerLogEvents.NANO_INSTALLING, new File(uri).toString());
         final String warName = extractDecodedWarNameFromString(uri.toString());
         final File deployedFile = new File(uri);
         final File warDir = new File(this.webAppsDir, replaceHashSigns(warName, DOT));
         StatusFileModificator.deleteStatusFile(warName, this.pickupDir);
         final long lastModified = deployedFile.lastModified();
 
         if (!canWrite(uri)) {
             this.logger.error("Cannot open the file " + uri + " for writing. The configured timeout is " + this.largeFileCopyTimeout + ".");
             StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, -1L, lastModified);
             this.eventLogger.log(WARDeployerLogEvents.NANO_INSTALLING_ERROR, uri);
             return false;
         }
         final Bundle installed;
         try {
             // Extract the war file to the webapps directory. Use always JarUtils.unpackToDestructive.
             JarUtils.unpackToDestructive(new PathReference(deployedFile), new PathReference(warDir));
             // make the manifest transformation in the unpacked location
             transformUnpackedManifest(warDir, warName);
             // install the bundle
             installed = this.bundleContext.installBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, warDir));
             this.eventLogger.log(WARDeployerLogEvents.NANO_INSTALLED, installed.getSymbolicName(), installed.getVersion());
         } catch (Exception e) {
             this.eventLogger.log(WARDeployerLogEvents.NANO_INSTALLING_ERROR, e, uri);
             StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, -1L, lastModified);
             return false;
         }
         return true;
     }
 
     @Override
     public boolean start(URI uri) {
         Bundle bundle = getInstalledBundle(uri);
         if (bundle == null) {
             this.eventLogger.log(WARDeployerLogEvents.NANO_STARTING_ERROR, uri);
             logger.error("Cannot start deployable with URI + [" + uri + "]. There is no bundle installed with this URI.");
             return false;
         }
         final String warName = extractDecodedWarNameFromString(uri.toString());
         StatusFileModificator.deleteStatusFile(warName, this.pickupDir);
         final long lastModified = new File(uri).lastModified();
         this.eventLogger.log(WARDeployerLogEvents.NANO_WEB_STARTING, bundle.getSymbolicName(), bundle.getVersion());
         try {
             bundle.start();
         } catch (Exception e) {
             this.eventLogger.log(WARDeployerLogEvents.NANO_STARTING_ERROR, e, bundle.getSymbolicName(), bundle.getVersion());
             StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, bundle.getBundleId(),
                 lastModified);
             return STATUS_ERROR;
         }
         this.eventLogger.log(WARDeployerLogEvents.NANO_WEB_STARTED, bundle.getSymbolicName(), bundle.getVersion());
 
         // now update bundle's info
         if (!updateBundlesInfo(bundle, getLocationForBundlesInfo(uri))) {
             StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_ERROR, bundle.getBundleId(),
                 lastModified);
             return STATUS_ERROR;
         }
         StatusFileModificator.createStatusFile(warName, this.pickupDir, StatusFileModificator.OP_DEPLOY, STATUS_OK, bundle.getBundleId(),
             lastModified);
         return STATUS_OK;
     }
 
     private boolean updateBundlesInfo(Bundle bundle, String location) {
         if (this.logger.isInfoEnabled()) {
             this.logger.info("Bundles info will be updated for web app bundle with simbolic name '" + bundle.getSymbolicName() + "' .");
         }
         try {
             if (this.bundleInfosUpdaterUtil != null && this.bundleInfosUpdaterUtil.isAvailable()) {
                 BundleInfosUpdater.registerToBundlesInfo(bundle, location, NOT_A_FRAGMENT);
             }
         } catch (Exception e) {
             this.eventLogger.log(WARDeployerLogEvents.NANO_PERSIST_ERROR, e, bundle.getSymbolicName(), bundle.getVersion());
             return STATUS_ERROR;
         }
         return STATUS_OK;
     }
 
     private Bundle getInstalledBundle(URI path) {
         final String warName = extractDecodedWarNameFromString(path.toString());
 
         final File warDir = new File(this.webAppsDir, replaceHashSigns(warName, DOT));
         if (!warDir.exists()) {
             logger.warn("Directory with name [" + warName + "] cannot be found in web applications directory."
                 + " See logs for previous failures during install.");
             return null;
         }
         return this.bundleContext.getBundle(BundleLocationUtil.createInstallLocation(this.kernelHomeFile, warDir));
     }
 
     private String getLocationForBundlesInfo(URI path) {
         final String warName = extractDecodedWarNameFromString(path.toString());
         final File warDir = new File(this.webAppsDir, replaceHashSigns(warName, DOT));
         if (!warDir.exists()) {
             logger.warn("Directory with name [" + warName + "] cannot be found in web applications directory."
                 + " See logs for previous failures during install.");
             return null;
         }
         return BundleLocationUtil.getRelativisedURI(kernelHomeFile, warDir).toString();
     }
 
     private String replaceHashSigns(String str, char newChar) {
         return str.replace(HASH_SIGN, newChar);
     }
 
 }
