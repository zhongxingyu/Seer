 package ch.bergturbenthal.image.server;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.PreferenceChangeEvent;
 import java.util.prefs.PreferenceChangeListener;
 import java.util.prefs.Preferences;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.jmdns.JmmDNS;
 import javax.jmdns.NetworkTopologyEvent;
 import javax.jmdns.NetworkTopologyListener;
 import javax.jmdns.ServiceEvent;
 import javax.jmdns.ServiceInfo;
 import javax.jmdns.ServiceListener;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.MergeResult;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.transport.FetchResult;
 import org.eclipse.jgit.transport.RefSpec;
 import org.joda.time.Duration;
 import org.joda.time.format.PeriodFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus.Series;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.client.RestTemplate;
 
 import ch.bergturbenthal.image.data.model.AlbumEntry;
 import ch.bergturbenthal.image.data.model.AlbumList;
 import ch.bergturbenthal.image.data.model.PingResponse;
 import ch.bergturbenthal.image.server.model.ArchiveData;
 
 import com.drew.imaging.ImageMetadataReader;
 import com.drew.imaging.ImageProcessingException;
 import com.drew.metadata.Metadata;
 
 public class FileAlbumAccess implements AlbumAccess, FileConfiguration, ArchiveConfiguration {
   private static final String SERVICE_TYPE = "_images._tcp.local.";
   private static final String INSTANCE_NAME_PREFERENCE = "instanceName";
   private static final String ALBUM_PATH_PREFERENCE = "album_path";
   private static final String IMPORT_BASE_PATH_REFERENCE = "import_base_path";
   private static final String META_CACHE = "cache";
   private static final String META_REPOSITORY = ".meta";
   private ArchiveData archiveData;
   private File baseDir;
   private final AtomicReference<Collection<URI>> peerServers = new AtomicReference<Collection<URI>>(Collections.<URI> emptyList());
   @Autowired
   private ScheduledExecutorService executorService;
   private File importBaseDir;

   private final String instanceId = UUID.randomUUID().toString();
   private final AtomicLong lastLoadedDate = new AtomicLong(0);
   private Map<String, Album> loadedAlbums = null;
   private final Logger logger = LoggerFactory.getLogger(FileAlbumAccess.class);

   private final ObjectMapper mapper = new ObjectMapper();
   private Git metaGit;
   private Preferences preferences = null;
   private String instanceName;
   private JmmDNS jmmDNS;
  private final RestTemplate restTemplate = new RestTemplate();
 
   @Override
   public synchronized String createAlbum(final String[] pathNames) {
     final Map<String, Album> albums = listAlbums();
     final File basePath = getBasePath();
     File newAlbumPath = basePath;
     for (final String pathComp : pathNames) {
       newAlbumPath = new File(newAlbumPath, pathComp);
     }
     if (!newAlbumPath.getAbsolutePath().startsWith(basePath.getAbsolutePath())) {
       throw new RuntimeException("Cannot create Album " + pathNames);
     }
     if (newAlbumPath.exists()) {
       for (final Entry<String, Album> albumEntry : albums.entrySet()) {
         if (Arrays.asList(pathNames).equals(albumEntry.getValue().getNameComps())) {
           // album already exists
           return albumEntry.getKey();
         }
       }
       throw new RuntimeException("Directory " + newAlbumPath + " already exsists");
     }
     if (!newAlbumPath.exists()) {
       final boolean createParent = newAlbumPath.mkdirs();
       if (!createParent)
         throw new RuntimeException("Cannot create Directory " + newAlbumPath);
     }
 
     final String[] nameComps = newAlbumPath.getAbsolutePath().substring(getBasePath().getAbsolutePath().length() + 1).split(File.pathSeparator);
     final Album newAlbum = new Album(newAlbumPath, nameComps);
     final String albumKey = Util.sha1(newAlbumPath.getAbsolutePath());
     loadedAlbums.put(albumKey, newAlbum);
     return albumKey;
  };
 
   @Override
   public Album getAlbum(final String albumId) {
     return listAlbums().get(albumId);
   }
 
   @Override
   public String getArchiveName() {
     return archiveData.getArchiveName();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see ch.bergturbenthal.image.server.FileConfiguration#getBaseDir()
    */
   @Override
   public File getBaseDir() {
     return baseDir;
   }
 
   @Override
   public String getCollectionId() {
     return archiveData.getArchiveName();
   }
 
   @Override
   public File getImportBaseDir() {
     return importBaseDir;
   }
 
   @Override
   public String getInstanceId() {
     return instanceId;
   }
 
   @Override
   public String getInstanceName() {
     return instanceName;
   }
 
   @Override
   public void importFiles(final File importDir) {
 
     try {
       if (!importDir.getAbsolutePath().startsWith(importBaseDir.getAbsolutePath())) {
         logger.error("Secutity-Error: Not allowed to read Images from " + importDir + " (Import-Path is " + importBaseDir + ")");
         return;
       }
       final HashSet<Album> modifiedAlbums = new HashSet<Album>();
       final SortedMap<Date, Album> importAlbums = new TreeMap<Date, Album>();
       for (final Album album : listAlbums().values()) {
         final Date beginDate = album.getAutoAddBeginDate();
         if (beginDate != null)
           importAlbums.put(beginDate, album);
       }
       final Collection<File> deleteFiles = new ArrayList<File>();
       final Collection<File> importCandicates = collectImportFiles(importDir);
       for (final File file : importCandicates) {
         try {
           // logger.info("Read: " + file.getName());
           final Metadata metadata = ImageMetadataReader.readMetadata(file);
           final Date createDate = MetadataUtil.readCreateDate(metadata);
           if (createDate == null)
             // skip images without creation date
             continue;
           final SortedMap<Date, Album> entriesBeforeDate = importAlbums.headMap(createDate);
           if (entriesBeforeDate.isEmpty())
             // no matching album found
             continue;
           final Album album = entriesBeforeDate.get(entriesBeforeDate.lastKey());
           // logger.info(" ->" + album.getName());
           if (album.importImage(file, createDate)) {
             modifiedAlbums.add(album);
             logger.debug("image " + file + " imported successfully to " + album.getName());
             deleteFiles.add(file);
           } else {
             logger.warn("Could not import image " + file);
           }
         } catch (final ImageProcessingException e) {
           throw new RuntimeException("Cannot import file " + file, e);
         }
       }
       for (final Album album : modifiedAlbums) {
         album.commit("automatically imported");
       }
       for (final File file : deleteFiles) {
         final boolean deleted = file.delete();
         if (!deleted)
           logger.error("Cannot delete File " + file);
       }
     } finally {
       refreshCache();
     }
   }
 
   @Override
   public Map<String, Album> listAlbums() {
     if (needToLoadAlbumList()) {
       synchronized (this) {
         if (needToLoadAlbumList())
           try {
             final Map<String, Album> ret = new HashMap<String, Album>();
             final File basePath = getBasePath();
             logger.debug("Load Repositories from: " + basePath);
             final int basePathLength = basePath.getAbsolutePath().length();
             for (final File albumDir : findAlbums(basePath)) {
               logger.debug("Load Repository " + albumDir);
               final String[] nameComps = albumDir.getAbsolutePath().substring(basePathLength + 1).split(File.pathSeparator);
               if (Arrays.equals(nameComps, new String[] { META_REPOSITORY }))
                 continue;
               ret.put(Util.encodeStringForUrl(StringUtils.join(nameComps, "/")), new Album(albumDir, nameComps));
             }
             lastLoadedDate.set(System.currentTimeMillis());
             loadedAlbums = ret;
           } catch (final Throwable e) {
             throw new RuntimeException("Troubles while accessing resource " + baseDir, e);
           }
       }
     }
     return loadedAlbums;
   }
 
   @Override
   public void setArchiveName(final String archiveName) {
     if (StringUtils.equals(archiveData.getArchiveName(), archiveName))
       return;
     archiveData.setArchiveName(archiveName);
     updateMeta("ArchiveName upated");
     executorService.submit(new Runnable() {
 
       @Override
       public void run() {
         pollCurrentKnownPeers();
       }
     });
   }
 
   @Override
   public synchronized void setBaseDir(final File baseDir) {
     if (ObjectUtils.equals(this.baseDir, baseDir))
       return;
     this.baseDir = baseDir;
     loadedAlbums = null;
     if (preferences != null) {
       preferences.put(ALBUM_PATH_PREFERENCE, baseDir.getAbsolutePath());
       flushPreferences();
     }
     initMetaRepository();
    if (executorService != null)
      executorService.submit(new Runnable() {
 
        @Override
        public void run() {
          refreshCache();
        }
      });
  }

  public void setExecutorService(final ScheduledExecutorService executorService) {
    this.executorService = executorService;
   }
 
   @Override
   public void setImportBaseDir(final File importBaseDir) {
     if (ObjectUtils.equals(this.importBaseDir, importBaseDir))
       return;
     this.importBaseDir = importBaseDir;
     if (preferences != null) {
       preferences.put(IMPORT_BASE_PATH_REFERENCE, importBaseDir.getAbsolutePath());
       flushPreferences();
     }
   }
 
   @Override
   public void setInstanceName(final String instanceName) {
     if (ObjectUtils.equals(this.instanceName, instanceName))
       return;
     this.instanceName = instanceName;
     if (preferences != null) {
       preferences.put(INSTANCE_NAME_PREFERENCE, instanceName);
       flushPreferences();
     }
   }
 
   /**
    * Start scheduling automatically after initializing
    */
   @PostConstruct
   protected void startScheduling() {
     executorService.scheduleWithFixedDelay(new Runnable() {
 
       @Override
       public void run() {
         try {
           refreshCache();
         } catch (final Throwable t) {
           logger.warn("Exception while refreshing thumbnails", t);
         }
       }
     }, 60, 2 * 60, TimeUnit.MINUTES);
   }
 
   private Collection<File> collectImportFiles(final File importDir) {
     if (!importDir.isDirectory())
       return Collections.emptyList();
     final ArrayList<File> ret = new ArrayList<File>();
     ret.addAll(Arrays.asList(importDir.listFiles(new FileFilter() {
       @Override
       public boolean accept(final File pathname) {
         if (!pathname.canRead())
           return false;
         if (!pathname.isFile())
           return false;
         final String lowerFilename = pathname.getName().toLowerCase();
         return lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".nef") || lowerFilename.endsWith(".jpeg");
       }
     })));
     for (final File dir : importDir.listFiles(new FileFilter() {
       @Override
       public boolean accept(final File pathname) {
         return pathname.isDirectory();
       }
     })) {
       ret.addAll(collectImportFiles(dir));
     }
     return ret;
   }
 
   private void configureFromPreferences() {
     if (baseDir == null) {
       preferences = Preferences.userNodeForPackage(FileAlbumAccess.class);
       readLocalSettingsFromPreferences();
       preferences.addPreferenceChangeListener(new PreferenceChangeListener() {
         @Override
         public void preferenceChange(final PreferenceChangeEvent arg0) {
           readLocalSettingsFromPreferences();
         }
       });
     }
   }
 
   private Collection<File> findAlbums(final File dir) {
     final File gitSubDir = new File(dir, ".git");
     if (gitSubDir.exists() && gitSubDir.isDirectory()) {
       return Collections.singleton(dir);
     }
     final File[] foundFiles = dir.listFiles(new FileFilter() {
       @Override
       public boolean accept(final File pathname) {
         return pathname.isDirectory();
       }
     });
     final ArrayList<File> ret = new ArrayList<File>();
     if (foundFiles != null) {
       Arrays.sort(foundFiles);
       for (final File subDir : foundFiles) {
         ret.addAll(findAlbums(subDir));
       }
     }
     return ret;
   }
 
   private void flushPreferences() {
     try {
       preferences.flush();
     } catch (final BackingStoreException e) {
       logger.warn("Cannot persist config", e);
     }
   }
 
   private File getBasePath() {
     return baseDir.getAbsoluteFile();
   }
 
   private File getConfigFile() {
     return new File(getMetaDir(), "config.json");
   }
 
   private File getMetaDir() {
     final File metaDir = new File(getBasePath(), META_REPOSITORY);
     if (!metaDir.exists())
       metaDir.mkdirs();
     return metaDir;
   }
 
   @SuppressWarnings("unused")
   @PostConstruct
   private void init() {
     configureFromPreferences();
   }
 
   private void initMetaRepository() {
     final File metaDir = getMetaDir();
 
     if (new File(metaDir, ".git").exists()) {
       try {
         metaGit = Git.open(metaDir);
       } catch (final IOException e) {
         throw new RuntimeException("Cannot access to git-repository of " + baseDir, e);
       }
     } else {
       try {
         metaGit = Git.init().setDirectory(metaDir).call();
       } catch (final GitAPIException e) {
         throw new RuntimeException("Cannot create meta repository", e);
       }
     }
     final File cacheDir = new File(metaDir, META_CACHE);
     if (!cacheDir.exists())
       cacheDir.mkdirs();
     final File configFile = new File(metaDir, "config.json");
     try {
       if (!configFile.exists()) {
         archiveData = new ArchiveData();
         final Map<String, Collection<String>> albumPerStorage = archiveData.getAlbumPerStorage();
         archiveData.setArchiveName(UUID.randomUUID().toString());
         for (final Album album : listAlbums().values()) {
           for (final String client : album.listClients()) {
             if (albumPerStorage.containsKey(client)) {
               albumPerStorage.get(client).add(album.getName());
             } else {
               final TreeSet<String> albums = new TreeSet<String>();
               albums.add(album.getName());
               albumPerStorage.put(client, albums);
             }
           }
         }
         updateMeta("config.json built");
       } else {
         archiveData = mapper.readValue(configFile, ArchiveData.class);
       }
     } catch (final IOException e) {
       throw new RuntimeException("Cannot write config to " + configFile, e);
     }
   }
 
   @PostConstruct
   @SuppressWarnings("unused")
   private void listenPeers() {
     if (jmmDNS != null)
       return;
     jmmDNS = JmmDNS.Factory.getInstance();
     final ServiceListener serviceListener = new ServiceListener() {
 
       @Override
       public void serviceAdded(final ServiceEvent event) {
         event.getDNS().requestServiceInfo(event.getType(), event.getName());
       }
 
       @Override
       public void serviceRemoved(final ServiceEvent event) {
         pollCurrentKnownPeers();
       }
 
       @Override
       public void serviceResolved(final ServiceEvent event) {
         pollCurrentKnownPeers();
       }
     };
     jmmDNS.addNetworkTopologyListener(new NetworkTopologyListener() {
 
       @Override
       public void inetAddressAdded(final NetworkTopologyEvent event) {
         event.getDNS().addServiceListener(SERVICE_TYPE, serviceListener);
       }
 
       @Override
       public void inetAddressRemoved(final NetworkTopologyEvent event) {
         pollCurrentKnownPeers();
       }
     });
   }
 
   private String makeDefaultInstanceName() {
     try {
       return InetAddress.getLocalHost().getHostName();
     } catch (final UnknownHostException e) {
       return UUID.randomUUID().toString();
     }
   }
 
   private boolean needToLoadAlbumList() {
     return loadedAlbums == null || (System.currentTimeMillis() - lastLoadedDate.get()) > TimeUnit.MINUTES.toMillis(5);
   }
 
   private ResponseEntity<PingResponse> ping(final URI uri) {
     return restTemplate.getForEntity(uri.resolve("ping.json"), PingResponse.class);
   }
 
   private synchronized void pollCurrentKnownPeers() {
     processFoundServices(jmmDNS.list(SERVICE_TYPE));
   }
 
   private void processFoundServices(final ServiceInfo[] services) {
     final Map<String, URI> foundPeers = new HashMap<String, URI>();
     for (final ServiceInfo serviceInfo : services) {
       final int peerPort = serviceInfo.getPort();
       final InetAddress[] addresses = serviceInfo.getInetAddresses();
       for (final InetAddress inetAddress : addresses) {
         try {
           final URI candidateUri = new URI("http", null, inetAddress.getHostAddress(), peerPort, "/rest/", null, null);
           final ResponseEntity<PingResponse> responseEntity = ping(candidateUri);
           if (!responseEntity.hasBody() || responseEntity.getStatusCode().series() != Series.SUCCESSFUL) {
             continue;
           }
           final PingResponse pingResponse = responseEntity.getBody();
           if (!pingResponse.getArchiveId().equals(getArchiveName()))
             continue;
           if (pingResponse.getServerId().equals(getInstanceId()))
             continue;
           foundPeers.put(pingResponse.getServerId(), candidateUri);
         } catch (final URISyntaxException e) {
           logger.warn("Cannot build URL for " + serviceInfo);
         }
       }
     }
     logger.info("Found peers: ");
     for (final Entry<String, URI> peerEntry : foundPeers.entrySet()) {
       logger.info(" - " + peerEntry.getKey() + ": " + peerEntry.getValue());
     }
     final Collection<URI> currentActiveServers = new HashSet<URI>(foundPeers.values());
     final Collection<URI> alreadyKnownServers = peerServers.get();
     if (!ObjectUtils.equals(currentActiveServers, alreadyKnownServers)) {
       peerServers.set(foundPeers.values());
       updateAllRepositories();
     }
   }
 
   private void readLocalSettingsFromPreferences() {
     setBaseDir(new File(preferences.get(ALBUM_PATH_PREFERENCE, new File(System.getProperty("user.home"), "images").getAbsolutePath())));
     setImportBaseDir(new File(preferences.get(IMPORT_BASE_PATH_REFERENCE, "nowhere")));
     setInstanceName(preferences.get(INSTANCE_NAME_PREFERENCE, makeDefaultInstanceName()));
   }
 
   private void refreshCache() {
     try {
       final AtomicInteger imageCount = new AtomicInteger();
       // limit the queue size for take not too much memory
       final Semaphore queueLimitSemaphore = new Semaphore(100);
       final long startTime = System.currentTimeMillis();
       for (final Album album : listAlbums().values()) {
         for (final AlbumImage image : album.listImages().values()) {
           queueLimitSemaphore.acquire();
           executorService.submit(new Runnable() {
 
             @Override
             public void run() {
               try {
                 // read Metadata
                 // image.captureDate();
                 // read Thumbnail
                 image.getThumbnail();
               } finally {
                 imageCount.incrementAndGet();
                 queueLimitSemaphore.release();
               }
             }
           });
         }
       }
       // wait until the end
       queueLimitSemaphore.acquire(100);
       final long endTime = System.currentTimeMillis();
       final Duration duration = new Duration(startTime, endTime);
       final StringBuffer buf = new StringBuffer("Refresh-Time: ");
       PeriodFormat.wordBased().getPrinter().printTo(buf, duration.toPeriod(), Locale.getDefault());
       buf.append(", ");
       buf.append(imageCount.intValue());
       buf.append(" Images");
       logger.info(buf.toString());
     } catch (final InterruptedException e) {
       logger.info("cache refresh interrupted");
     }
   }
 
   @PreDestroy
   private void shutdownDnsListener() throws IOException {
     if (jmmDNS != null) {
       jmmDNS.close();
       jmmDNS = null;
     }
   }
 
   private void updateAllRepositories() {
     for (final URI peerServerUri : peerServers.get()) {
       final ResponseEntity<PingResponse> responseEntity = ping(peerServerUri);
       if (!responseEntity.hasBody())
         continue;
       final PingResponse pingResponse = responseEntity.getBody();
       final String remoteHost = peerServerUri.getHost();
       final int remotePort = pingResponse.getGitPort();
       final ResponseEntity<AlbumList> albumListEntity = restTemplate.getForEntity(peerServerUri.resolve("albums.json"), AlbumList.class);
       if (!albumListEntity.hasBody())
         continue;
       final AlbumList remoteAlbumList = albumListEntity.getBody();
       final Map<String, Album> localAlbums = listAlbums();
       for (final AlbumEntry album : remoteAlbumList.getAlbumNames()) {
         try {
           final Album localAlbumForRemote = localAlbums.get(album.getId());
           final File localDir = new File(getBaseDir(), album.getName());
           final String remoteUri = new URI("git", null, remoteHost, remotePort, "/" + album.getId(), null, null).toASCIIString();
           if (!localDir.exists()) {
             Git.cloneRepository().setDirectory(localDir).setURI(remoteUri).call();
           } else {
             final Git repo;
             if (localAlbumForRemote != null)
               repo = Git.wrap(localAlbumForRemote.getRepository());
             else if (new File(localDir, ".git").exists())
               repo = Git.open(localDir);
             else
               repo = Git.init().setDirectory(localDir).call();
             final FetchResult fetchResult = repo.fetch().setRemote(remoteUri).setRefSpecs(new RefSpec("HEAD")).call();
             final Ref fetchHead = repo.getRepository().getRef("FETCH_HEAD");
             // Ref ref;
             final MergeResult mergeResult = repo.merge().include(fetchHead).call();
             mergeResult.getConflicts();
           }
         } catch (final Throwable e) {
           logger.error("Cannot sync with " + remoteHost, e);
         }
       }
     }
   }
 
   private synchronized void updateMeta(final String message) {
     try {
       mapper.writerWithDefaultPrettyPrinter().writeValue(getConfigFile(), archiveData);
       metaGit.add().addFilepattern(".").call();
       metaGit.commit().setMessage(message).call();
     } catch (final IOException e) {
       throw new RuntimeException("Cannot update Metadata", e);
     } catch (final GitAPIException e) {
       throw new RuntimeException("Cannot update Metadata", e);
     }
   }
 }
