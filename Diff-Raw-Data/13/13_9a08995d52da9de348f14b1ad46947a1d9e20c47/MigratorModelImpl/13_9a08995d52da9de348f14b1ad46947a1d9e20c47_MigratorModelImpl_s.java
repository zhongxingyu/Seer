 /*
  * Created On:  23-Jan-07 4:43:12 PM
  */
 package com.thinkparity.ophelia.model.migrator;
 
 import java.io.*;
 import java.lang.reflect.Method;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import com.thinkparity.codebase.FileSystem;
 import com.thinkparity.codebase.FileUtil;
 import com.thinkparity.codebase.OSUtil;
 import com.thinkparity.codebase.ResourceUtil;
 import com.thinkparity.codebase.StringUtil;
 import com.thinkparity.codebase.ZipUtil;
 import com.thinkparity.codebase.StringUtil.Separator;
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
 import com.thinkparity.ophelia.model.io.db.hsqldb.HypersonicException;
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
 
     /** An empty process monitory for the download thread. */
     private static final ProcessMonitor DOWNLOAD_MONITOR;
 
     /** An empty process monitory for the installation thread. */
     private static final ProcessMonitor INSTALL_MONITOR;
 
     /** A workspace attribute key for the download thread. */
     private static final String WS_ATTRIBUTE_KEY_DOWNLOAD;
 
     /** A workspace attribute key for the install thread. */
     private static final String WS_ATTRIBUTE_KEY_INSTALL;
 
     /** A workspace attribute key for the migrator listener. */
     private static final String WS_ATTRIBUTE_KEY_LISTENER;
 
     static {
         DOWNLOAD_MONITOR = new ProcessAdapter() {};
         INSTALL_MONITOR = DOWNLOAD_MONITOR;
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
      * @see com.thinkparity.ophelia.model.migrator.InternalMigratorModel#handleProductReleaseDeployed(com.thinkparity.codebase.model.util.xmpp.event.ProductReleaseDeployedEvent)
      * 
      */
     public void handleProductReleaseDeployed(
             final ProductReleaseDeployedEvent event) {
         try {
             /* the scenario can exist that the event is fired _after_ the
              * release has been downloaded/installed/initialized */
             if (event.getRelease().getName().equals(Constants.Release.NAME)) {
                 logger.logInfo("Release {0} has already been installed.",
                         event.getRelease().getName());
             } else {
                 // fire event
                 notifyProductReleaseDeployed(event.getProduct(), event.getRelease(),
                         remoteEventGenerator);
             }
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
             migratorIO.createProduct(Constants.Product.NAME, Constants.Release.NAME);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.MigratorModel#initializeRelease(com.thinkparity.ophelia.model.util.ProcessMonitor)
      *
      */
     public void initializeRelease(final ProcessMonitor monitor) {
         try {
             initializeRelease();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.MigratorModel#isInstalledReleaseInitialized()
      *
      */
     public Boolean isReleaseInitialized() {
         try {
             /* determine if the release has been initialized */
             try {
                 final String release = migratorIO.readProductRelease(Constants.Product.NAME);
                 return release.equals(Constants.Release.NAME);
             } catch (final HypersonicException hx) {
                 // TODO remove after release - table does not exist error
                 final SQLException cause = (SQLException) hx.getCause();
                 if ("42X05".equals(cause.getSQLState())) {
                     return Boolean.FALSE;
                 } else {
                     throw hx;
                 }
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.MigratorModel#isReleaseInstalled()
      *
      */
     public Boolean isReleaseInstalled() {
         try {
             final String downloaded = migratorIO.readDownloadedProductRelease(
                     Constants.Product.NAME);
             // if there exists a downloaded release, it needs to be installed
             return null == downloaded;
         } catch (final Throwable t) {
             throw panic(t);
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
             if (sessionModel.isOnline()) {
                 final Error error = new Error();
                 final String[] stringArguments = new String[arguments.length];
                 for (int i = 0; i < arguments.length; i++) {
                     stringArguments[i] = null == arguments[i]
                             ? Separator.Null.toString()
                             : arguments[i].toString();
                 }
                 error.setArguments(stringArguments);
                 final StringWriter writer = new StringWriter();
                 cause.printStackTrace(new PrintWriter(writer));
                 error.setStack(writer.toString());
                 error.setMethod(method.toGenericString());
                 error.setOccuredOn(sessionModel.readDateTime());
                 final Product product = readRemoteProduct(Constants.Product.NAME);
                 final Release release = readRemoteProductRelease(
                         Constants.Product.NAME, Constants.Release.NAME);
                 sessionModel.logError(product, release, error);
             }
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
      * @see com.thinkparity.ophelia.model.migrator.MigratorModel#startDownloadRelease()
      * 
      */
     public void startDownloadRelease() {
         if (isDownloadInProgress()) {
             logger.logInfo("Release download in progress.");
         } else {
             getSessionModel().notifyClientMaintenance();
             // the product
             final Product product = readRemoteProduct(Constants.Product.NAME);
             // the download release/resources
             final Release downloadRelease = readRemoteLatestRelease();
             final List<Resource> downloadResources = readRemoteResources(downloadRelease);
             // THREAD MigratorModelImpl#startDownloadLatestRelease
             final Thread download = new Thread("TPS-OpheliaModel-DownloadRelease") {
                 @Override
                 public void run() {
                     try {
                         downloadRelease(DOWNLOAD_MONITOR, product,
                                 downloadRelease, downloadResources);
                     } catch (final IOException iox) {
                         logger.logError(iox, "Could not download release {0}.",
                                 downloadRelease);
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
      * @see com.thinkparity.ophelia.model.migrator.MigratorModel#startInstallRelease(com.thinkparity.ophelia.model.util.ProcessMonitor)
      * 
      */
     public void startInstallRelease(final ProcessMonitor monitor) {
         if (isInstallInProgress()) {
             logger.logInfo("Release install in progress.");
         } else {
             getSessionModel().notifyClientMaintenance();
             final String release = migratorIO.readProductRelease(
                     Constants.Product.NAME);
             final String downloadedRelease = migratorIO.readDownloadedProductRelease(
                     Constants.Product.NAME);
             // THREAD MigratorModelImpl#startInstallLatestRelease
             final Thread install = new Thread("TPS-OpheliaModel-InstallRelease") {
                 @Override
                 public void run() {
                     try {
                         installRelease(monitor, release, downloadedRelease,
                                 workspace.getBufferLock(), workspace
                                         .getBufferArray());
                     } catch (final IOException iox) {
                         logger.logFatal(iox, "Could not install release {0}",
                                 release);
                     } finally {
                         workspace.removeAttribute(WS_ATTRIBUTE_KEY_INSTALL);                    
                     }
                 }
             };
             workspace.setAttribute(WS_ATTRIBUTE_KEY_INSTALL, install);
             install.start();
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.InternalMigratorModel#updateDownloadedRelease(java.lang.String)
      *
      */
     public void updateDownloadedRelease(final String name) {
         try {
             migratorIO.updateDownloadedProductRelease(Constants.Product.NAME,
                     name);
             // fire event
             notifyLatestReleaseDownloaded(null, null, localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.migrator.InternalMigratorModel#updateInstalledRelease(java.lang.String)
      *
      */
     public void updateInstalledRelease(final String name) {
         try {
             migratorIO.updateProductRelease(Constants.Product.NAME, name);
             migratorIO.deleteDownloadedProductRelease(Constants.Product.NAME);
             // fire event
             notifyLatestReleaseInstalled(null, null, localEventGenerator);
         } catch (final Throwable t) {
             throw panic(t);
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
                     startDownloadRelease();
                 }
                 @Override
                 public void productReleaseDownloaded(final MigratorEvent e) {
                     startInstallRelease(INSTALL_MONITOR);
                 }
             };
             workspace.setAttribute(WS_ATTRIBUTE_KEY_LISTENER, listener);
             addListener(listener);
         }
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
      * @see com.thinkparity.ophelia.model.migrator.InternalMigratorModel#downloadRelease(com.thinkparity.ophelia.model.util.ProcessMonitor)
      * 
      */
     private void downloadRelease(final ProcessMonitor monitor,
             final Product product, final Release release,
             final List<Resource> resources) throws IOException {
         // the installed resources
         final List<Resource> localResources = readReleaseResources(
                 Constants.Release.NAME, new FileSystem(installDirectory));
         // calculate the resources that need to be downloaded
         final List<Resource> download = resolveDownloadResources(
                 localResources, resources);
         final File releaseFile = new File(downloadDirectory, release.getName()); 
         // download
         final File temp = download(product, release, download);
         try {
             // copy to a download directory
             fileToFile(temp, releaseFile);
         } finally {
             // no assertion because it is a temp file
             temp.delete();
         }
         getMigratorModel().updateDownloadedRelease(release.getName());
     }
 
     /**
      * Fiter out any comment text from the buffer. A comment is represented by a
      * beginning of a line, two dashes, whitespace, any text an end of a line.
      * 
      * @param sql
      *            An sql buffer <code>String</code>.
      * @return The filtered sql buffer <code>String</code>.
      */
     private String filterSqlComments(String sql) {
         sql = sql.replaceAll("--\\s.*", "");
         return sql;
     }
 
     /**
      * Fiter out any end-of-line characters from the statement.
      * 
      * @param statement
      *            An sql statement <code>String</code>.
      * @return The filtered statement <code>String</code>.
      */
     private String filterSqlStatementEOL(String statement) {
         statement = statement.replaceAll("\\r", "");
         statement = statement.replaceAll("\\n", "");
         return statement;
     }
 
     /**
      * Initialize the release.
      * 
      */
     private void initializeRelease() throws IOException {
         final String[] releases = new String[] {
                "v1_0-20070430-1500",
                "v1_0-20070516-1300",
                "v1_0-20070612-1800"
         };
         int beginIndex = -1;
         for (int i = 0; i < releases.length; i++) {
             if (Constants.Release.NAME.equals(releases[i])) {
                 beginIndex = i + 1;
                 break;
             }
         }
         if (-1 < beginIndex && releases.length > beginIndex) {
             for (int i = beginIndex; i < releases.length; i++) {
                 final File releaseInstall = new File(installDirectory, releases[i]);
                 // a release will only not exist in a test/dev environment
                 if (releaseInstall.exists()) {
                     logger.logInfo("Deleting release {0}.", releases[i]);
                     FileUtil.deleteTree(releaseInstall);
                 }
         
                 // migrate the schema
                 logger.logInfo("Migrating schema to {0}.", Constants.Release.NAME);
                 final String sql = loadSql(releases[i]);
                 logger.logVariable("sql", sql);
                 filterSqlComments(sql);
                 logger.logVariable("sql", sql);
                 final List<String> sqlStatements = tokenizeSqlStatements(sql);
                 logger.logVariable("sqlStatements", sqlStatements);
                 migratorIO.execute(sqlStatements);
             }
         }
 
         // delete the index
         getHelpModel().deleteIndex();
     }
 
     /**
      * Install a release.
      * @param monitor A <code>Procee
      * @param release
      * @param downloadedRelease
      * @param bufferLock
      * @param bufferArray
      * @throws IOException
      */
     private void installRelease(final ProcessMonitor monitor,
             final String release, final String downloadedRelease,
             final Object bufferLock, final byte[] bufferArray) throws IOException {
         Assert.assertNotTrue(downloadedRelease.equals(release),
                 "Installed release {0} equals downloaded release {1}.",
                 release, downloadedRelease);
 
         final File download = new File(downloadDirectory, downloadedRelease);
         // reference install
         final File install = new File(installDirectory, release);
         // create latest
         final File latest = new File(installDirectory, downloadedRelease);
         if (latest.exists())
             FileUtil.deleteTree(latest);
         Assert.assertTrue(latest.mkdir(),
                 "Could not create directory {0}.", latest);
         // extract download
         synchronized (bufferLock) {
             ZipUtil.extractZipFile(download, latest, bufferArray);
         }
 
         final List<Resource> resources = readReleaseResources(release,
                 new FileSystem(installDirectory));
         final List<Resource> downloadedResources = readReleaseResources(
                 downloadedRelease, new FileSystem(downloadDirectory));
 
         // copy resources from installed to latest
         final List<Resource> copyManifest = resolveCopyResources(
                 resources, downloadedResources);
         File from, to;
         final FileSystem latestFS = new FileSystem(latest);
         final FileSystem installFS = new FileSystem(install);
         for (final Resource copy : copyManifest) {
             from = installFS.find(copy.getPath());
             to = latestFS.createFile(copy.getPath());
             fileToFile(from, to);
         }
         // delete download
         Assert.assertTrue(download.delete(),
                 "Could not delete file {0}.", download);
         // install release
         getMigratorModel().updateInstalledRelease(downloadedRelease);
     }
 
     /**
      * Determine if the latest release download is in progress.
      * 
      * @return True if the latest release download is in progress.
      */
     private boolean isDownloadInProgress() {
         return workspace.isSetAttribute(WS_ATTRIBUTE_KEY_DOWNLOAD).booleanValue();
     }
 
     /**
      * Determine if the latest release install is in progress.
      * 
      * @return True if the latest release download is in progress.
      */
     private boolean isInstallInProgress() {
         return workspace.isSetAttribute(WS_ATTRIBUTE_KEY_INSTALL).booleanValue();
     }
 
     /**
      * Load the sql migration statements for a release.
      * 
      * @param name
      *            A release name <code>String</code>.
      * @return A sql buffer <code>String</code> containing tokenized
      *         statements.
      * @throws IOException
      */
     private String loadSql(final String name) throws IOException {
         // load the content of the sql file into a buffer
         final Reader reader = new InputStreamReader(
                 ResourceUtil.getInputStream("database/" + name + ".sql"),
                 StringUtil.Charset.ISO_8859_1.getCharset());
         try {
             final StringBuilder buffer = new StringBuilder(2048);
             int character;
             while (-1 != (character = reader.read())) {
                 buffer.append((char) character);
             }
             return buffer.toString();
         } finally {
             reader.close();
         }            
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
      * @param name
      *            The product name <code>String</code>.
      * @param releaseName
      *            The release name <code>String</code>.
      * @param meg
      *            The <code>MigratorEventGenerator</code>.
      */
     private void notifyLatestReleaseInstalled(final String name,
             final String releaseName, final MigratorEventGenerator meg) {
         notifyListeners(new EventNotifier<MigratorListener>() {
             public void notifyListener(final MigratorListener listener) {
                 listener.productReleaseInstalled(meg.generate(null, null));
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
      * Read a release's file-system resources.
      * 
      * @param name
      *            A release name <code>String</code>.
      * @param fileSystem
      *            A <code>FileSystem</code>.
      * @return A <code>List</code> of <code>Resource</code>s.
      */
     private List<Resource> readReleaseResources(final String name,
             final FileSystem fileSystem) throws IOException {
         final File[] files = fileSystem.listFiles("/", Boolean.TRUE);
         final List<Resource> resources = new ArrayList<Resource>(files.length);
         Resource resource;
         for (final File file : files) {
             resource = new Resource();
 //            resource.setChecksum(checksum(file));
 //            resource.setChecksumAlgorithm(getChecksumAlgorithm());
             resource.setPath(FileUtil.getRelativePath(fileSystem.getRoot(), file, '/'));
             resource.setReleaseName(name);
             resource.setSize(file.length());
         }
         return resources;
     }
 
     /**
      * Read the latest release for a product from the server.
      * 
      * @param product
      *            A <code>Product</code>.
      * @return A <code>Release</code>.
      */
     private Release readRemoteLatestRelease() {
         return getSessionModel().readMigratorLatestRelease(
                 Constants.Product.NAME, OSUtil.getOs());
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
      * Read a product release from the server.
      * 
      * @param name
      *            A product name <code>String</code>.
      * @param releaseName
      *            A release name <code>String</code>.
      * @return A <code>Release</code>.
      */
     private Release readRemoteProductRelease(final String name,
             final String releaseName) {
         return getSessionModel().readMigratorRelease(name, releaseName,
                 OSUtil.getOS());
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
     private List<Resource> resolveCopyResources(final List<Resource> installed,
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
     private List<Resource> resolveDownloadResources(
             final List<Resource> installed, final List<Resource> latest) {
         final List<Resource> download = new ArrayList<Resource>();
         for (final Resource resource : latest) {
             if (!installed.contains(resource))
                 download.add(resource);
         }
         return download;
     }
 
     /**
      * Tokenize an sql buffer into a list of statements. Empty text is ignored.
      * The statement separator is a semi-colon.
      * 
      * @param sql
      *            An sql buffer <code>String</code>.
      * @return A <code>List<String></code>.
      */
     private List<String> tokenizeSqlStatements(final String sql) {
         final StringTokenizer tokenizer = new StringTokenizer(sql, ";");
         final List<String> sqlStatements = new ArrayList<String>(tokenizer.countTokens());
         String token;
         while (tokenizer.hasMoreTokens()) {
             token = tokenizer.nextToken().trim();
             if (!"".equals(token)) {
                 filterSqlStatementEOL(token);
                 sqlStatements.add(token);
             }
         }
         return sqlStatements;
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
                 new FileInputStream(file), getBufferSize());
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
