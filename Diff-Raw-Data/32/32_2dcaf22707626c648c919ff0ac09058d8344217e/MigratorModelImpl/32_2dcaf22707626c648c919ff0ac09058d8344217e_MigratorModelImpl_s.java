 /*
  * Created On:  23-Jan-07 4:43:12 PM
  */
 package com.thinkparity.ophelia.model.migrator;
 
 import java.io.*;
 import java.lang.reflect.Method;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.thinkparity.codebase.FileSystem;
 import com.thinkparity.codebase.FileUtil;
 import com.thinkparity.codebase.OSUtil;
 import com.thinkparity.codebase.StreamUtil;
 import com.thinkparity.codebase.ZipUtil;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.event.EventNotifier;
 
 import com.thinkparity.codebase.model.DownloadMonitor;
 import com.thinkparity.codebase.model.UploadMonitor;
 import com.thinkparity.codebase.model.migrator.Error;
 import com.thinkparity.codebase.model.migrator.Product;
 import com.thinkparity.codebase.model.migrator.Release;
 import com.thinkparity.codebase.model.migrator.Resource;
 import com.thinkparity.codebase.model.session.Environment;
 import com.thinkparity.codebase.model.stream.StreamSession;
 import com.thinkparity.codebase.model.util.xmpp.event.ProductReleaseDeployedEvent;
 
 import com.thinkparity.ophelia.model.Constants;
 import com.thinkparity.ophelia.model.Model;
 import com.thinkparity.ophelia.model.events.MigratorAdapter;
 import com.thinkparity.ophelia.model.events.MigratorEvent;
 import com.thinkparity.ophelia.model.events.MigratorListener;
 import com.thinkparity.ophelia.model.io.IOFactory;
 import com.thinkparity.ophelia.model.io.handler.MigratorIOHandler;
 import com.thinkparity.ophelia.model.session.InternalSessionModel;
 import com.thinkparity.ophelia.model.util.ProcessAdapter;
 import com.thinkparity.ophelia.model.util.ProcessMonitor;
 import com.thinkparity.ophelia.model.workspace.Workspace;
 
 /**
  * <b>Title:</b>thinkParity Migrator Model Implementation<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class MigratorModelImpl extends Model<MigratorListener> implements
         MigratorModel, InternalMigratorModel {
 
     private static final ProcessMonitor DOWNLOAD_MONITOR;
 
     private static final ProcessMonitor INSTALL_MONITOR;
 
     /** A workspace attribute key for the download thread. */
     private static final String WS_ATTRIBUTE_KEY_DOWNLOAD;
 
     /** A workspace attribute key for the install thread. */
     private static final String WS_ATTRIBUTE_KEY_INSTALL;
 
     /** A workspace attribute key for the migrator listener. */
     private static final String WS_ATTRIBUTE_KEY_LISTENER;
 
     static {
         DOWNLOAD_MONITOR = new ProcessAdapter() {};
         INSTALL_MONITOR = new ProcessAdapter() {};
         WS_ATTRIBUTE_KEY_DOWNLOAD = "MigratorModelImpl#download";
         WS_ATTRIBUTE_KEY_INSTALL = "MigratorModelImpl#install";
         WS_ATTRIBUTE_KEY_LISTENER = "MigratorModelImpl#listener";
     }
 
     /** A workspace download directory <code>File</code>. */
     private File downloadDirectory;
 
     /** The product install directory <code>File</code>. */
     private File installDirectory;
 
     /** A local <code>MigratorEventGenerator</code>. */
     private final MigratorEventGenerator localEventGenerator;
 
     /** A <code>MigratorIOHandler</code>. */
     private MigratorIOHandler migratorIO;
 
     /** A remote <code>MigratorEventGenerator</code>. */
     private final MigratorEventGenerator remoteEventGenerator;
 
     /**
      * Create MigratorModelImpl.
      *
      */
     public MigratorModelImpl() {
         super();
         this.localEventGenerator = new MigratorEventGenerator(MigratorEvent.Source.LOCAL);
         this.remoteEventGenerator = new MigratorEventGenerator(MigratorEvent.Source.REMOTE);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.Model#addListener(com.thinkparity.codebase.event.EventListener)
      *
      */
     @Override
     public void addListener(MigratorListener listener) {
         super.addListener(listener);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.MigratorModel#deployProduct(com.thinkparity.ophelia.model.migrator.monitor.DeployMonitor, java.lang.String, java.lang.String, java.io.File)
      *
      */
     public void deploy(final ProcessMonitor monitor, final Product product,
             final Release release, final List<Resource> resources,
             final File file) {
         try {
             // upload the stream
             final String streamId = upload(file);
             // deploy
             getSessionModel().deployMigrator(product, release, resources,
                     streamId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.InternalMigratorModel#downloadLatestRelease(com.thinkparity.ophelia.model.util.ProcessMonitor)
      * 
      */
     public void downloadLatestRelease(final ProcessMonitor monitor) {
         try {
             final Product product = readProduct();
             final Release latestRelease = readLatestRelease();
             final List<Resource> installedResources = readInstalledResources();
             final List<Resource> latestResources = readLatestResources();
             // calculate the resources that need to be downloaded
             final List<Resource> downloadManifest = createDownloadManifest(
                     installedResources, latestResources);
             final File releaseFile = new File(downloadDirectory, latestRelease.getName()); 
             // download
             final File temp = download(product, latestRelease, downloadManifest);
             try {
                 // copy to a download directory
                 final ByteBuffer buffer = workspace.getDefaultBuffer();
                 synchronized (buffer) {
                     FileUtil.copy(temp, releaseFile, buffer);
                 }
             } finally {
                 // no assertion because it is a temp file
                 temp.delete();
             }
             // fire event
             notifyLatestReleaseDownloaded(product, latestRelease,
                     localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.InternalMigratorModel#handleProductReleaseDeployed(com.thinkparity.codebase.model.util.xmpp.event.ProductReleaseDeployedEvent)
      * 
      */
     public void handleProductReleaseDeployed(
             final ProductReleaseDeployedEvent event) {
         try {
             // create release
             final Release release = event.getRelease();
             final List<Resource> resources = event.getResources();
             migratorIO.createRelease(release, resources);
             // update latest release
             final Product product = migratorIO.readProduct(Constants.Product.NAME);
             migratorIO.updateLatestRelease(product, release);
             // go offline
             getSessionModel().notifyClientMaintenance();
             // fire event
             notifyProductReleaseDeployed(event.getProduct(), event.getRelease(),
                     remoteEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.InternalMigratorModel#initialize(com.thinkparity.ophelia.model.util.ProcessMonitor)
      * 
      */
     public void initialize(final ProcessMonitor monitor) {
         try {
             initializeProduct();
             initializeInstalledRelease();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.MigratorModel#initializeInstalledRelease(com.thinkparity.ophelia.model.util.ProcessMonitor)
      *
      */
     public void initializeInstalledRelease(final ProcessMonitor monitor) {
         try {
             initializeInstalledRelease();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.InternalMigratorModel#installLatestRelease(com.thinkparity.ophelia.model.util.ProcessMonitor)
      * 
      */
     public void installLatestRelease(final ProcessMonitor monitor) {
         try {
             // ensure the migration can be done
             final Product product = migratorIO.readProduct(Constants.Product.NAME);
             final Release installedRelease = migratorIO.readInstalledRelease(product);
             final Release latestRelease = migratorIO.readLatestRelease(product);
             Assert.assertNotTrue(installedRelease.equals(latestRelease),
                     "Installed release {0} equals latest release {1}.",
                     installedRelease, latestRelease);
     
             final List<Resource> installedResources = migratorIO.readInstalledResources(product);
             final List<Resource> latestResources = migratorIO.readLatestResources(product);
     
             final File download = new File(downloadDirectory, latestRelease.getName());
             // reference install
             final File install = new File(installDirectory, installedRelease.getName());
             // createlatest
             final File latest = new File(installDirectory, latestRelease.getName());
             if (latest.exists())
                 FileUtil.deleteTree(latest);
             Assert.assertTrue(latest.mkdir(),
                     "Could not create directory {0}.", latest);
             // extract download
             final ByteBuffer buffer = workspace.getDefaultBuffer();
             synchronized (buffer) {
                 ZipUtil.extractZipFile(download, latest, buffer);
             }
             // copy resources from installed to latest
             final List<Resource> copyManifest = createCopyManifest(
                     installedResources, latestResources);
             File from, to;
             final FileSystem latestFS = new FileSystem(latest);
             final FileSystem installFS = new FileSystem(install);
             for (final Resource copy : copyManifest) {
                 from = installFS.find(copy.getPath());
                 to = latestFS.createFile(copy.getPath());
                 final InputStream fromStream = new FileInputStream(from);
                 try {
                     final OutputStream toStream = new FileOutputStream(to);
                     try {
                         synchronized (buffer) {
                             StreamUtil.copy(fromStream, toStream, buffer);
                         }
                     } finally {
                         toStream.close();
                     }
                 } finally {
                     fromStream.close();
                 }
             }
             // delete download
             Assert.assertTrue(download.delete(),
                     "Could not delete file {0}.", download);
             // install release
             migratorIO.updateInstalledRelease(product, latestRelease);
             migratorIO.updatePreviousRelease(product, installedRelease);
             // fire event
             notifyLatestReleaseInstalled(product, latestRelease, localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.MigratorModel#isInstalledReleaseInitialized()
      *
      */
     public Boolean isInstalledReleaseInitialized() {
         try {
             return migratorIO.isReleaseInitialized(readInstalledRelease());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.InternalMigratorModel#isLatestRelease()
      * 
      */
     public Boolean isLatestRelease() {
         /* NOTE if the product is not initialized the latest release cannot
          * possibly be initialized or installed */
         if (isProductInitialized()) {
             final Product product = readProduct();
             final Release installed = migratorIO.readInstalledRelease(product);
             final Release latest = migratorIO.readLatestRelease(product);
             return installed.equals(latest);
         } else {
             return Boolean.FALSE;
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.InternalMigratorModel#logError(java.lang.Throwable,
      *      java.lang.reflect.Method, java.lang.Object[])
      * 
      */
     public void logError(final Throwable cause, final Method method,
             final Object[] arguments) {
         try {
             final InternalSessionModel sessionModel = getSessionModel();
             if (sessionModel.isLoggedIn()) {
                 final Error error = new Error();
                 error.setArguments(arguments);
                error.setCause(cause);
                 error.setMethod(method);
                 error.setOccuredOn(sessionModel.readDateTime());
                 final Product product = migratorIO.readProduct(Constants.Product.NAME);
                 sessionModel.logError(product,
                         migratorIO.readInstalledRelease(product), error);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.MigratorModel#readInstalledRelease()
      * 
      */
     public Release readInstalledRelease() {
         try {
             return migratorIO.readInstalledRelease(readProduct());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.Model#removeListener(com.thinkparity.codebase.event.EventListener)
      *
      */
     @Override
     public void removeListener(MigratorListener listener) {
         super.removeListener(listener);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.MigratorModel#startDownloadLatestRelease()
      * 
      */
     public void startDownloadLatestRelease() {
         if (isLatestReleaseDownloadInProgress()) {
             logger.logInfo("Release download in progress.");
         } else {
             // THREAD MigratorModelImpl#startDownloadLatestRelease
             final Thread download = new Thread("TPS-OpheliaModel-DownloadRelease") {
                 @Override
                 public void run() {
                     try {
                         getMigratorModel().downloadLatestRelease(DOWNLOAD_MONITOR);
                     } finally {
                         workspace.removeAttribute(WS_ATTRIBUTE_KEY_DOWNLOAD);                    
                     }
                 }
             };
             workspace.setAttribute(WS_ATTRIBUTE_KEY_DOWNLOAD, download);
             download.start();
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.Model#initializeModel(com.thinkparity.codebase.model.session.Environment, com.thinkparity.ophelia.model.workspace.Workspace)
      *
      */
     @Override
     protected void initializeModel(final Environment environment,
             final Workspace workspace) {
         downloadDirectory = workspace.getDownloadDirectory();
         installDirectory = Constants.Directories.ThinkParity.DIRECTORY;
         migratorIO = IOFactory.getDefault(workspace).createMigratorHandler();
         if (!workspace.isSetAttribute(WS_ATTRIBUTE_KEY_LISTENER).booleanValue()) {
             final MigratorListener listener = new MigratorAdapter() {
                 @Override
                 public void productReleaseDeployed(final MigratorEvent e) {
                     startDownloadLatestRelease();
                 }
                 @Override
                 public void productReleaseDownloaded(final MigratorEvent e) {
                     startInstallLatestRelease();
                 }
             };
             workspace.setAttribute(WS_ATTRIBUTE_KEY_LISTENER, listener);
             addListener(listener);
         }
     }
 
     /**
      * Create a list of resources that need to be copied.  Compare the list
      * of resources in the installed list with that of the latest list.  Those
      * in the latest that are in the installed list are copied.
      * 
      * @param installed
      *            A <code>List</code> of installed <code>Resource</code>s.
      * @param latest
      *            A <code>List</code> of latest <code>Resource</code>s.
      * @return A <code>List</code> of <code>Resource</code>s.
      */
     private List<Resource> createCopyManifest(final List<Resource> installed,
             final List<Resource> latest) {
         final List<Resource> copy = new ArrayList<Resource>();
         for (final Resource resource : installed) {
             if (latest.contains(resource))
                 copy.add(resource);
         }
         return copy;
     }
 
     /**
      * Create a list of resources that need to be downloaded.  Compare the list
      * of resources in the installed list with that of the latest list.  Those
      * in the latest that are not installed need to be downloaded.
      * 
      * @param installed
      *            A <code>List</code> of installed <code>Resource</code>s.
      * @param latest
      *            A <code>List</code> of latest <code>Resource</code>s.
      * @return A <code>List</code> of <code>Resource</code>s.
      */
     private List<Resource> createDownloadManifest(final List<Resource> installed,
             final List<Resource> latest) {
         final List<Resource> download = new ArrayList<Resource>();
         for (final Resource resource : latest) {
             if (!installed.contains(resource))
                 download.add(resource);
         }
         return download;
     }
 
     /**
      * Download a list of resources for a release.
      * 
      * @param release
      *            A <code>Release</code>.
      * @param resources
      *            A <code>List</code> of <code>Resource</code>s.
      * @return A <code>File</code>.
      */
     private File download(final Product product, final Release release,
             final List<Resource> resources) throws IOException {
         final InternalSessionModel sessionModel = getSessionModel();
         final StreamSession session = sessionModel.createStreamSession();
         final String streamId = sessionModel.createStream(session);
         sessionModel.createMigratorStream(streamId, product, release, resources);
         return downloadStream(new DownloadMonitor() {
             public void chunkDownloaded(final int chunkSize) {
                 logger.logTrace("Downloading {0}/{1}", chunkSize, -1L);
             }
         }, streamId);
     }
 
     /**
      * Initialize the installed release. Here we perform any custom logic
      * required to upgrade the product from one release to another.
      * 
      * Then we update the installed release and its initialization information.
      * 
      */
     private void initializeInstalledRelease() {
         final Release installedRelease = readInstalledRelease();
         final Release previousRelease = readPreviousRelease();
 
         // delete previous release
         if (null == previousRelease) {
             logger.logInfo("First installed release {0}.", installedRelease.getName());
         } else {
             logger.logInfo("Deleting previous release {0}.", previousRelease.getName());
             final File previous = new File(installDirectory, previousRelease.getName());
             FileUtil.deleteTree(previous);
         }
 
         migratorIO.updateReleaseInitialization(installedRelease);
     }
 
     /**
      * Initialize the product. Download the product and release meta-data from
      * the server and create them locally. Also read the latest release
      * meta-data from the server and if it differs from the installed release,
      * create it and update the latest release info.
      */
     private void initializeProduct() {
         // create the product
         final Product product = readRemoteProduct(Constants.Product.NAME);
         final Release release = readRemoteRelease(product, Constants.Release.NAME);
         final List<Resource> resources = readRemoteResources(release);
         migratorIO.createProduct(product, release, resources);
         // set the latest release
         final Release latestRelease = readRemoteLatestRelease(product);
         if (!release.equals(latestRelease)) {
             final List<Resource> latestReleaseResources = readRemoteResources(
                     latestRelease);
             migratorIO.createRelease(latestRelease, latestReleaseResources);
             migratorIO.updateLatestRelease(product, latestRelease);
             // go offline
             getSessionModel().notifyClientMaintenance();
         }
     }
 
     /**
      * Determine if the latest release download is in progress.
      * 
      * @return True if the latest release download is in progress.
      */
     private boolean isLatestReleaseDownloadInProgress() {
         return workspace.isSetAttribute(WS_ATTRIBUTE_KEY_DOWNLOAD).booleanValue();
     }
 
     /**
      * Determine if the product is initialized.
      * 
      * @return True if the product is initialized.
      */
     private boolean isProductInitialized() {
         return migratorIO.doesExistProduct(Constants.Product.NAME).booleanValue();
     }
 
     /**
      * Fire a product release downloaded event.
      * 
      * @param product
      *            The <code>Product</code>.
      * @param release
      *            The <code>Release</code>.
      * @param meg
      *            The <code>MigratorEventGenerator</code>.
      */
     private void notifyLatestReleaseDownloaded(final Product product,
             final Release release, final MigratorEventGenerator meg) {
         notifyListeners(new EventNotifier<MigratorListener>() {
             public void notifyListener(final MigratorListener listener) {
                 listener.productReleaseDownloaded(meg.generate(product, release));
             }
         });
     }
 
     /**
      * Fire a product release installed event.
      * 
      * @param product
      *            The <code>Product</code>.
      * @param release
      *            The <code>Release</code>.
      * @param meg
      *            The <code>MigratorEventGenerator</code>.
      */
     private void notifyLatestReleaseInstalled(final Product product,
             final Release release, final MigratorEventGenerator meg) {
         notifyListeners(new EventNotifier<MigratorListener>() {
             public void notifyListener(final MigratorListener listener) {
                 listener.productReleaseInstalled(meg.generate(product, release));
             }
         });
     }
 
     /**
      * Fire a product release deployed event.
      * 
      * @param product
      *            A <code>Product</code>.
      * @param release
      *            A <code>Release</code>.
      * @param eventGenerator
      *            A <code>MigratorEventGenerator</code>.
      */
     private void notifyProductReleaseDeployed(final Product product,
             final Release release, final MigratorEventGenerator meg) {
         notifyListeners(new EventNotifier<MigratorListener>() {
             public void notifyListener(final MigratorListener listener) {
                 listener.productReleaseDeployed(meg.generate(product, release));
             }
         });
     }
 
     /**
      * Read the installed release's resources.
      * 
      * @return A <code>List</code> of <code>Resource</code>s.
      */
     private List<Resource> readInstalledResources() {
         return migratorIO.readInstalledResources(readProduct());
     }
 
     /**
      * Read the latest release.
      * 
      * @return A <code>Release</code>.
      */
     private Release readLatestRelease() {
         return migratorIO.readLatestRelease(readProduct());
     }
 
     /**
      * Read the latest release's resources.
      * 
      * @return A <code>List</code> of <code>Resource</code>s.
      */
     private List<Resource> readLatestResources() {
         return migratorIO.readLatestResources(readProduct());
     }
 
     /**
      * Read the previously installed release.
      * 
      * @return A <code>Release</code>.
      */
     private Release readPreviousRelease() {
         try {
             return migratorIO.readPreviousRelease(readProduct());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read the product.
      * 
      * @return A <code>Product</code>.
      */
     private Product readProduct() {
         return migratorIO.readProduct(Constants.Product.NAME);
     }
 
     /**
      * Read the latest release for a product from the server.
      * 
      * @param product
      *            A <code>Product</code>.
      * @return A <code>Release</code>.
      */
     private Release readRemoteLatestRelease(final Product product) {
         return getSessionModel().readMigratorLatestRelease(
                 product.getName(), OSUtil.getOs());
     }
 
     /**
      * Read a product from the server.
      * 
      * @param name
      *            A product name <code>String</code>.
      * @return A <code>Product</code>.
      */
     private Product readRemoteProduct(final String name) {
         return getSessionModel().readMigratorProduct(name);
     }
 
     /**
      * Read the release from the server.
      * 
      * @param product
      *            A <code>Product</code>.
      * @param name
      *            A release name <code>String</code>.
      * @return A <code>Release</code>.
      */
     private Release readRemoteRelease(final Product product, final String name) {
         return getSessionModel().readMigratorRelease(product.getName(),
                 name, OSUtil.getOs());
     }
 
     /**
      * Read the resources for a release from the server.
      * 
      * @param release
      *            A <code>Release</code>.
      * @return A <code>List</code> of <code>Resource</code>s.
      */
     private List<Resource> readRemoteResources(final Release release) {
         return getSessionModel().readMigratorResources(
                 Constants.Product.NAME, release.getName(), OSUtil.getOs());
     }
 
     /**
      * Start a thread to install the latest release.
      * 
      */
     private void startInstallLatestRelease() {
         // THREAD MigratorModelImpl#startInstallLatestRelease
         final Thread install = new Thread("TPS-OpheliaModel-InstallRelease") {
             @Override
             public void run() {
                 try {
                     getMigratorModel().installLatestRelease(INSTALL_MONITOR);
                 } finally {
                     workspace.removeAttribute(WS_ATTRIBUTE_KEY_INSTALL);                    
                 }
             }
         };
         workspace.setAttribute(WS_ATTRIBUTE_KEY_INSTALL, install);
         install.start();
     }
 
     /**
      * Upload a release to the streaming server. The release will be zipped up
      * and uploaded.
      * 
      * @param releaseRoot
      *            A release root directory <code>File</code>.
      * @return The stream id <code>String</code>.
      * @throws FileNotFoundException
      * @throws IOException
      */
     private String upload(final File file) throws FileNotFoundException,
             IOException {
         final InternalSessionModel sessionModel = getSessionModel();
         final StreamSession session = sessionModel.createStreamSession();
         final InputStream stream = new BufferedInputStream(
                 new FileInputStream(file), getDefaultBufferSize());
         final Long streamSize = file.length();
         try {
             final String streamId = sessionModel.createStream(session);
             upload(new UploadMonitor() {
                 public void chunkUploaded(final int chunkSize) {
                     logger.logTraceId();
                     logger.logVariable("chunkSize:{0}", chunkSize);
                 }
             }, streamId, session, stream, streamSize);
             return streamId;
         } finally {
             stream.close();
         }
     }
 }
