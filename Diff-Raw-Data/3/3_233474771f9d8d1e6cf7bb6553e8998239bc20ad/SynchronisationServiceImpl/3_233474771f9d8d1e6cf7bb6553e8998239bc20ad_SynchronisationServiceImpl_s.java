 package ch.bergturbenthal.image.provider.service;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.net.ConnectException;
 import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.regex.Pattern;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.http.HttpStatus.Series;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.client.ResourceAccessException;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 
 import android.app.Notification;
 import android.app.Notification.Builder;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 import android.util.LruCache;
 import android.util.Pair;
 import ch.bergturbenthal.image.data.model.CaptionMutationEntry;
 import ch.bergturbenthal.image.data.model.KeywordMutationEntry;
 import ch.bergturbenthal.image.data.model.KeywordMutationEntry.KeywordMutation;
 import ch.bergturbenthal.image.data.model.MutationEntry;
 import ch.bergturbenthal.image.data.model.PingResponse;
 import ch.bergturbenthal.image.data.model.RatingMutationEntry;
 import ch.bergturbenthal.image.data.model.state.Issue;
 import ch.bergturbenthal.image.data.model.state.Progress;
 import ch.bergturbenthal.image.data.util.ExecutorServiceUtil;
 import ch.bergturbenthal.image.provider.Client;
 import ch.bergturbenthal.image.provider.map.FieldReader;
 import ch.bergturbenthal.image.provider.map.MapperUtil;
 import ch.bergturbenthal.image.provider.map.NotifyableMatrixCursor;
 import ch.bergturbenthal.image.provider.map.NumericFieldReader;
 import ch.bergturbenthal.image.provider.map.StringFieldReader;
 import ch.bergturbenthal.image.provider.model.dto.AlbumDetailData;
 import ch.bergturbenthal.image.provider.model.dto.AlbumDto;
 import ch.bergturbenthal.image.provider.model.dto.AlbumEntries;
 import ch.bergturbenthal.image.provider.model.dto.AlbumEntryDto;
 import ch.bergturbenthal.image.provider.model.dto.AlbumEntryIndex;
 import ch.bergturbenthal.image.provider.model.dto.AlbumEntryType;
 import ch.bergturbenthal.image.provider.model.dto.AlbumIndex;
 import ch.bergturbenthal.image.provider.model.dto.AlbumMeta;
 import ch.bergturbenthal.image.provider.model.dto.AlbumState;
 import ch.bergturbenthal.image.provider.service.MDnsListener.ResultListener;
 import ch.bergturbenthal.image.provider.state.ServerListActivity;
 import ch.bergturbenthal.image.provider.store.FileBackend;
 import ch.bergturbenthal.image.provider.store.FileStorage;
 import ch.bergturbenthal.image.provider.store.JacksonBackend;
 import ch.bergturbenthal.image.provider.store.ParcelableBackend;
 import ch.bergturbenthal.image.provider.util.Quad;
 
 public class SynchronisationServiceImpl extends Service implements ResultListener, SynchronisationService {
 
   /**
    * Class used for the client Binder. Because we know this service always runs
    * in the same process as its clients, we don't need to deal with IPC.
    */
   public class LocalBinder extends Binder {
     public SynchronisationService getService() {
       // Return this instance of LocalService so clients can call public methods
       return SynchronisationServiceImpl.this;
     }
   }
 
   private static final String THUMBNAIL_SUFFIX = ".thumbnail";
 
   // Binder given to clients
   private final IBinder binder = new LocalBinder();
   private final static String SERVICE_TAG = "Synchronisation Service";
   private final AtomicBoolean running = new AtomicBoolean(false);
   private final AtomicReference<Map<String, ArchiveConnection>> connectionMap =
                                                                                 new AtomicReference<Map<String, ArchiveConnection>>(
                                                                                                                                     Collections.<String, ArchiveConnection> emptyMap());
 
   private MDnsListener dnsListener;
   private ScheduledThreadPoolExecutor executorService;
   private final int NOTIFICATION = 0;
 
   private NotificationManager notificationManager;
   private ScheduledFuture<?> slowUpdatePollingFuture = null;
   private ExecutorService wrappedExecutorService;
   private File tempDir;
   private File thumbnailsTempDir;
   private File dataDir;
   private final ConcurrentMap<String, ConcurrentMap<String, String>> visibleAlbums = new ConcurrentHashMap<String, ConcurrentMap<String, String>>();
 
   private final CursorNotification cursorNotifications = new CursorNotification();
   private ScheduledFuture<?> fastUpdatePollingFuture;
 
   private final Semaphore updateLockSempahore = new Semaphore(1);
 
   private LruCache<AlbumEntryIndex, File> thumbnailCache;
 
   private File thumbnailsSyncDir;
 
   private final LruCache<String, Long> idCache = new LruCache<String, Long>(100) {
 
     private final AtomicLong idGenerator = new AtomicLong(0);
 
     @Override
     protected Long create(final String key) {
       return idGenerator.incrementAndGet();
     }
 
   };
 
   private final Semaphore pollServerSemaphore = new Semaphore(1);
 
   private FileStorage store;
 
   private final ObjectMapper mapper = new ObjectMapper();
 
   private final AtomicInteger tempFileId = new AtomicInteger();
 
   @Override
   public void createAlbumOnServer(final String serverId, final String fullAlbumName, final Date autoAddDate) {
     final ServerConnection serverConnection = getConnectionForServer(serverId);
     if (serverConnection == null)
       return;
     serverConnection.createAlbum(fullAlbumName, autoAddDate);
   }
 
   @Override
   public String getContenttype(final String archive, final String albumId, final String image) {
     return callInTransaction(new Callable<String>() {
 
       @Override
       public String call() throws Exception {
         final AlbumEntries entriesReadOnly = getAlbumEntriesReadOnly(archive, albumId);
         if (entriesReadOnly == null)
           return null;
         final AlbumEntryDto entryDto = entriesReadOnly.getEntries().get(image);
         if (entryDto == null)
           return null;
         switch (entryDto.getEntryType()) {
         case IMAGE:
           return "image/jpeg";
         case VIDEO:
           return "video/mp4";
         default:
           return null;
         }
       }
     });
   }
 
   @Override
   public File getLoadedThumbnail(final String archiveName, final String albumId, final String albumEntryId) {
     final long startTime = System.currentTimeMillis();
     Log.i("Performance", "Start load Thumbnail " + archiveName + ":" + albumId + ":" + albumEntryId);
     try {
       return thumbnailCache.get(new AlbumEntryIndex(archiveName, albumId, albumEntryId));
     } finally {
       Log.i("Performance", "Returned Thumbnail " + archiveName + ":" + albumId + ":" + albumEntryId + " in "
                            + (System.currentTimeMillis() - startTime) + " ms");
     }
   }
 
   @Override
   public void notifyServices(final Collection<InetSocketAddress> knownServiceEndpoints, final boolean withProgressUpdate) {
     for (final InetSocketAddress inetSocketAddress : knownServiceEndpoints) {
       Log.i(SERVICE_TAG, "Addr: " + inetSocketAddress);
     }
     final Map<String, Map<URL, PingResponse>> pingResponses = new HashMap<String, Map<URL, PingResponse>>();
     for (final InetSocketAddress inetSocketAddress : knownServiceEndpoints) {
       final URL url = makeUrl(inetSocketAddress);
       try {
         final PingResponse response = pingService(url);
         if (response != null) {
           if (pingResponses.containsKey(response.getArchiveId())) {
             pingResponses.get(response.getArchiveId()).put(url, response);
           } else {
             final Map<URL, PingResponse> map = new HashMap<URL, PingResponse>();
             map.put(url, response);
             pingResponses.put(response.getArchiveId(), map);
           }
         }
       } catch (final Throwable ex) {
         Log.e(SERVICE_TAG, "Exception while polling " + url, ex);
       }
     }
     final HashMap<String, ArchiveConnection> oldConnectionMap = new HashMap<String, ArchiveConnection>(connectionMap.get());
     final HashMap<String, ArchiveConnection> newConnectionMap = new HashMap<String, ArchiveConnection>();
     for (final Entry<String, Map<URL, PingResponse>> responseEntry : pingResponses.entrySet()) {
       final String archiveId = responseEntry.getKey();
       final ArchiveConnection connection =
                                            oldConnectionMap.containsKey(archiveId) ? oldConnectionMap.get(archiveId)
                                                                                   : new ArchiveConnection(archiveId, wrappedExecutorService);
       connection.updateServerConnections(responseEntry.getValue());
       newConnectionMap.put(archiveId, connection);
     }
     connectionMap.set(newConnectionMap);
     updateServerCursors();
     executorService.submit(new Runnable() {
 
       @Override
       public void run() {
         updateAlbumsOnDB();
       }
     });
     Log.i(SERVICE_TAG, pingResponses.toString());
   }
 
   @Override
   public IBinder onBind(final Intent arg0) {
     return binder;
   }
 
   @Override
   public void onCreate() {
     super.onCreate();
 
     executorService = new ScheduledThreadPoolExecutor(4, new ThreadFactory() {
       final AtomicInteger nextThreadIndex = new AtomicInteger(0);
 
       @Override
       public Thread newThread(final Runnable r) {
         return new Thread(r, "synchronisation-worker-" + nextThreadIndex.getAndIncrement());
       }
     });
 
     registerScreenOnOff();
     notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
     wrappedExecutorService = ExecutorServiceUtil.wrap(executorService);
 
     dnsListener = new MDnsListener(getApplicationContext(), this, executorService);
 
     dataDir = new File(getFilesDir(), "data");
     ParcelableBackend.checkVersion(dataDir, 2);
     store =
             new FileStorage(Arrays.asList((FileBackend<?>) new ParcelableBackend<AlbumEntries>(dataDir, AlbumEntries.class),
                                           (FileBackend<?>) new ParcelableBackend<AlbumMeta>(dataDir, AlbumMeta.class),
                                           (FileBackend<?>) new ParcelableBackend<AlbumDetailData>(dataDir, AlbumDetailData.class),
                                           (FileBackend<?>) new JacksonBackend<AlbumState>(dataDir, AlbumState.class)
 
             ));
 
     // setup and clean temp-dir
     tempDir = new File(getCacheDir(), "temp");
     if (!tempDir.exists())
       tempDir.mkdirs();
     for (final File file : tempDir.listFiles()) {
       file.delete();
     }
 
     // setup thumbnails-dir
     // temporary files
     thumbnailsTempDir = new File(getCacheDir(), "thumbnails");
     if (!thumbnailsTempDir.exists())
       thumbnailsTempDir.mkdirs();
     // explicit synced thumbnails
     thumbnailsSyncDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "royalarchive");
     if (!thumbnailsSyncDir.exists())
       thumbnailsSyncDir.mkdirs();
     // preload thumbnail-cache
     initThumbnailCache(2 * 1024 * 1024);
 
     executorService.schedule(new Runnable() {
 
       @Override
       public void run() {
         NetworkReceiver.notifyNetworkState(getApplicationContext());
       }
     }, 2, TimeUnit.SECONDS);
   }
 
   @Override
   public void onDestroy() {
     executorService.shutdownNow();
     notificationManager.cancel(NOTIFICATION);
     dnsListener.stopListening();
     stopSlowPolling();
   }
 
   @Override
   public int onStartCommand(final Intent intent, final int flags, final int startId) {
     if (intent != null) {
       final ServiceCommand command = intent.getParcelableExtra("command");
       if (command != null)
         switch (command) {
         case START:
           startRunning();
           break;
         case STOP:
           stopRunning();
           break;
         case POLL:
           pollServers();
           break;
         case SCREEN_ON:
           startFastPolling();
           break;
         case SCREEN_OFF:
           stopFastPolling();
           break;
         default:
           break;
         }
     }
     return START_STICKY;
   }
 
   @Override
   public Cursor readAlbumEntryList(final String archiveName, final String albumId, final String[] projection) {
     return callInTransaction(new Callable<Cursor>() {
 
       @Override
       public Cursor call() throws Exception {
         final AlbumEntries albumDetail = getOrMakeAlbumDetail(archiveName, albumId);
         if (albumDetail.getEntries() == null || albumDetail.getEntries().isEmpty())
           return cursorNotifications.addSingleAlbumCursor(new AlbumIndex(archiveName, albumId), new NotifyableMatrixCursor(new String[] {}));
         final Collection<AlbumEntryDto> albumEntries = albumDetail.getEntries().values();
         return makeCursorForAlbumEntries(albumEntries, archiveName, albumId, projection);
 
       }
     });
   }
 
   @Override
   public Cursor readAlbumList(final String[] projection) {
     return callInTransaction(new Callable<Cursor>() {
 
       @Override
       public Cursor call() throws Exception {
         return makeCursorForAlbums(collectVisibleAlbums(), projection, true);
       }
 
     });
   }
 
   @Override
   public Cursor readServerIssueList(final String serverId, final String[] projection) {
 
     final ServerConnection serverConnection = getConnectionForServer(serverId);
     if (serverConnection == null)
       return null;
     final Collection<Issue> progressValues = new ArrayList<Issue>(serverConnection.getServerState().getIssues());
 
     final Map<String, String> mappedFields = new HashMap<String, String>();
     mappedFields.put(Client.IssueEntry.CAN_ACK, "acknowledgable");
     mappedFields.put(Client.IssueEntry.ALBUM_NAME, "albumName");
     mappedFields.put(Client.IssueEntry.ALBUM_ENTRY_NAME, "imageName");
     mappedFields.put(Client.IssueEntry.ISSUE_TIME, "issueTime");
     mappedFields.put(Client.IssueEntry.STACK_TRACE, "stackTrace");
     mappedFields.put(Client.IssueEntry.ISSUE_TYPE, "type");
 
     final Map<String, FieldReader<Issue>> fieldReaders = MapperUtil.makeNamedFieldReaders(Issue.class, mappedFields);
     fieldReaders.put(Client.IssueEntry.ID, new NumericFieldReader<Issue>(Cursor.FIELD_TYPE_INTEGER) {
       @Override
       public Number getNumber(final Issue value) {
         return Long.valueOf(makeLongId(value.getIssueId()));
       }
     });
     return cursorNotifications.addStateCursor(MapperUtil.loadCollectionIntoCursor(progressValues, projection, fieldReaders));
   }
 
   @Override
   public Cursor readServerList(final String[] projection) {
     final Map<String, ArchiveConnection> archives = connectionMap.get();
 
     final Collection<Pair<String, ServerConnection>> connections = new ArrayList<Pair<String, ServerConnection>>();
     for (final Entry<String, ArchiveConnection> archiveEntry : archives.entrySet()) {
       for (final ServerConnection server : archiveEntry.getValue().listServers().values()) {
         connections.add(new Pair<String, ServerConnection>(archiveEntry.getKey(), server));
       }
     }
 
     final Map<String, FieldReader<Pair<String, ServerConnection>>> fieldReaders = new HashMap<String, FieldReader<Pair<String, ServerConnection>>>();
     fieldReaders.put(Client.ServerEntry.ARCHIVE_NAME, new StringFieldReader<Pair<String, ServerConnection>>() {
       @Override
       public String getString(final Pair<String, ServerConnection> value) {
         return value.first;
       }
     });
     fieldReaders.put(Client.ServerEntry.SERVER_ID, new StringFieldReader<Pair<String, ServerConnection>>() {
 
       @Override
       public String getString(final Pair<String, ServerConnection> value) {
         return value.second.getInstanceId();
       }
     });
     fieldReaders.put(Client.ServerEntry.SERVER_NAME, new StringFieldReader<Pair<String, ServerConnection>>() {
 
       @Override
       public String getString(final Pair<String, ServerConnection> value) {
         return value.second.getServerName();
       }
     });
     fieldReaders.put(Client.ServerEntry.ID, new NumericFieldReader<Pair<String, ServerConnection>>(Cursor.FIELD_TYPE_INTEGER) {
 
       @Override
       public Number getNumber(final Pair<String, ServerConnection> value) {
         return Long.valueOf(makeLongId(value.second.getInstanceId()));
       }
     });
 
     return cursorNotifications.addStateCursor(MapperUtil.loadCollectionIntoCursor(connections, projection, fieldReaders));
 
   }
 
   @Override
   public Cursor readServerProgresList(final String serverId, final String[] projection) {
     final ServerConnection serverConnection = getConnectionForServer(serverId);
     if (serverConnection == null)
       return null;
     final Collection<Progress> progressValues = new ArrayList<Progress>(serverConnection.getServerState().getProgress());
 
     final Map<String, String> mappedFields = new HashMap<String, String>();
     mappedFields.put(Client.ProgressEntry.STEP_COUNT, "stepCount");
     mappedFields.put(Client.ProgressEntry.CURRENT_STEP_NR, "currentStepNr");
     mappedFields.put(Client.ProgressEntry.PROGRESS_DESCRIPTION, "progressDescription");
     mappedFields.put(Client.ProgressEntry.CURRENT_STATE_DESCRIPTION, "currentStepDescription");
     mappedFields.put(Client.ProgressEntry.PROGRESS_TYPE, "type");
     final Map<String, FieldReader<Progress>> fieldReaders = MapperUtil.makeNamedFieldReaders(Progress.class, mappedFields);
     fieldReaders.put(Client.ProgressEntry.ID, new NumericFieldReader<Progress>(Cursor.FIELD_TYPE_INTEGER) {
 
       @Override
       public Number getNumber(final Progress value) {
         return Long.valueOf(makeLongId(value.getProgressId()));
       }
     });
     return cursorNotifications.addStateCursor(MapperUtil.loadCollectionIntoCursor(progressValues, projection, fieldReaders));
   }
 
   @Override
   public Cursor readSingleAlbum(final String archiveName, final String albumId, final String[] projection) {
     return callInTransaction(new Callable<Cursor>() {
       @Override
       public Cursor call() throws Exception {
         final Collection<AlbumIndex> visible = collectVisibleAlbums();
         if (visible.contains(new AlbumIndex(archiveName, albumId)))
           return makeCursorForAlbums(Collections.singletonList(new AlbumIndex(archiveName, albumId)), projection, false);
         else
           return makeCursorForAlbums(Collections.<AlbumIndex> emptyList(), projection, false);
       }
     });
   }
 
   @Override
   public Cursor readSingleAlbumEntry(final String archiveName, final String albumId, final String archiveEntryId, final String[] projection) {
     return callInTransaction(new Callable<Cursor>() {
       @Override
       public Cursor call() throws Exception {
         final AlbumEntries albumEntries = getAlbumEntriesReadOnly(archiveName, albumId);
         if (albumEntries == null || albumEntries.getEntries() == null || albumEntries.getEntries().get(archiveEntryId) == null)
           return makeCursorForAlbumEntries(Collections.<AlbumEntryDto> emptyList(), archiveName, albumId, projection);
         final AlbumEntryDto entryDto = albumEntries.getEntries().get(archiveEntryId);
         return makeCursorForAlbumEntries(Collections.singletonList(entryDto), archiveName, albumId, projection);
       }
     });
   }
 
   @Override
   public int updateAlbum(final String archiveName, final String albumId, final ContentValues values) {
     final Collection<String> albumEntriesToClear = new ArrayList<String>();
     final int updatedCount = callInTransaction(new Callable<Integer>() {
       @Override
       public Integer call() throws Exception {
         final AlbumDetailData albumMeta = getAlbumEntriesList(archiveName, albumId);
         final AlbumState albumState = getOrMakeAlbumState(archiveName, albumId);
         if (albumMeta == null)
           return Integer.valueOf(0);
         final Boolean shouldSync = values.getAsBoolean(Client.Album.SHOULD_SYNC);
         if (shouldSync != null && albumState.isShouldSync() != shouldSync.booleanValue()) {
           albumState.setShouldSync(shouldSync.booleanValue());
           final AlbumEntries albumEntries = getAlbumEntriesReadOnly(archiveName, albumId);
           if (albumEntries != null && albumEntries.getEntries() != null) {
             albumEntriesToClear.addAll(albumEntries.getEntries().keySet());
           }
         }
         return Integer.valueOf(1);
       }
 
     }).intValue();
     for (final String entryId : albumEntriesToClear) {
       thumbnailCache.remove(new AlbumEntryIndex(archiveName, albumId, entryId));
     }
     return updatedCount;
   }
 
   @Override
   public int updateAlbumEntry(final String archiveName, final String albumId, final String albumEntryId, final ContentValues values) {
     return callInTransaction(new Callable<Integer>() {
       @Override
       public Integer call() throws Exception {
         final AlbumEntries albumEntries = getAlbumEntries(archiveName, albumId);
         if (albumEntries == null)
           return Integer.valueOf(0);
         final Map<String, AlbumEntryDto> entries = albumEntries.getEntries();
         if (entries == null && !entries.containsKey(albumEntryId))
           return Integer.valueOf(0);
         final AlbumEntryDto albumEntryDto = entries.get(albumEntryId);
         if (albumEntryDto == null)
           return Integer.valueOf(0);
         final AlbumMeta albumMeta = getAlbumMeta(archiveName, albumId);
         final AlbumDetailData mutationList = getOrMakeAlbumMutationList(archiveName, albumId);
 
         final Collection<MutationEntry> mutations = mutationList.getMutations();
         if (values.containsKey(Client.AlbumEntry.META_RATING)) {
           for (final Iterator<MutationEntry> entryIterator = mutations.iterator(); entryIterator.hasNext();) {
             final MutationEntry mutationEntry = entryIterator.next();
             if (mutationEntry instanceof RatingMutationEntry && mutationEntry.getAlbumEntryId().equals(albumEntryId))
               entryIterator.remove();
           }
           final Integer newRating = values.getAsInteger(Client.AlbumEntry.META_RATING);
           if (!objectEquals(albumEntryDto.getRating(), newRating)) {
             final RatingMutationEntry newEntry = new RatingMutationEntry();
             newEntry.setAlbumEntryId(albumEntryId);
             newEntry.setBaseVersion(albumEntryDto.getEditableMetadataHash());
             newEntry.setRating(newRating);
             mutations.add(newEntry);
           }
         }
         if (values.containsKey(Client.AlbumEntry.META_CAPTION)) {
           for (final Iterator<MutationEntry> entryIterator = mutations.iterator(); entryIterator.hasNext();) {
             final MutationEntry mutationEntry = entryIterator.next();
             if (mutationEntry instanceof CaptionMutationEntry && mutationEntry.getAlbumEntryId().equals(albumEntryId))
               entryIterator.remove();
           }
           final String newCaption = values.getAsString(Client.AlbumEntry.META_CAPTION);
           if (!objectEquals(albumEntryDto.getCaption(), newCaption)) {
             final CaptionMutationEntry mutationEntry = new CaptionMutationEntry();
             mutationEntry.setAlbumEntryId(albumEntryId);
             mutationEntry.setBaseVersion(albumEntryDto.getEditableMetadataHash());
             mutationEntry.setCaption(newCaption);
             mutations.add(mutationEntry);
           }
         }
 
         if (values.containsKey(Client.AlbumEntry.META_KEYWORDS)) {
           for (final Iterator<MutationEntry> entryIterator = mutations.iterator(); entryIterator.hasNext();) {
             final MutationEntry mutationEntry = entryIterator.next();
             if (mutationEntry instanceof KeywordMutationEntry && mutationEntry.getAlbumEntryId().equals(albumEntryId))
               entryIterator.remove();
           }
           final Collection<String> remainingKeywords =
                                                        new HashSet<String>(
                                                                            Client.AlbumEntry.decodeKeywords(values.getAsString(Client.AlbumEntry.META_KEYWORDS)));
           for (final String existingKeyword : albumEntryDto.getKeywords()) {
             final boolean removeThisKeyword = !remainingKeywords.remove(existingKeyword);
             if (removeThisKeyword) {
               final KeywordMutationEntry mutationEntry = new KeywordMutationEntry();
               mutationEntry.setAlbumEntryId(albumEntryId);
               mutationEntry.setBaseVersion(albumEntryDto.getEditableMetadataHash());
               mutationEntry.setKeyword(existingKeyword);
               mutationEntry.setMutation(KeywordMutation.REMOVE);
               mutations.add(mutationEntry);
             }
           }
           for (final String newKeyword : remainingKeywords) {
             final KeywordMutationEntry mutationEntry = new KeywordMutationEntry();
             mutationEntry.setAlbumEntryId(albumEntryId);
             mutationEntry.setBaseVersion(albumEntryDto.getEditableMetadataHash());
             mutationEntry.setKeyword(newKeyword);
             mutationEntry.setMutation(KeywordMutation.ADD);
             mutations.add(mutationEntry);
           }
         }
         return Integer.valueOf(1);
       }
     }).intValue();
   }
 
   private <V> V callInTransaction(final Callable<V> callable) {
     return cursorNotifications.doWithNotify(new Callable<V>() {
       @Override
       public V call() throws Exception {
         return store.callInTransaction(callable);
       }
     });
   }
 
   private Collection<AlbumIndex> collectVisibleAlbums() {
     final Collection<AlbumIndex> ret = new LinkedHashSet<AlbumIndex>();
     for (final Entry<String, ConcurrentMap<String, String>> archiveEntry : visibleAlbums.entrySet()) {
       final String archiveName = archiveEntry.getKey();
       for (final String albumId : archiveEntry.getValue().values()) {
         ret.add(new AlbumIndex(archiveName, albumId));
       }
     }
     return ret;
   }
 
   private int dateCompare(final Date date1, final Date date2) {
     return (date1 == null ? new Date(0) : date1).compareTo(date2 == null ? new Date(0) : date2);
   }
 
   private boolean dateEquals(final Date date1, final Date date2) {
     if (date1 == null)
       return date2 == null;
     if (date2 == null)
       return false;
     return Math.abs(date1.getTime() - date2.getTime()) < 1000;
   }
 
   private AlbumEntries getAlbumEntries(final String archiveName, final String albumId) {
     return store.getObject(makeAlbumEntriesPath(archiveName, albumId), AlbumEntries.class);
   }
 
   private AlbumDetailData getAlbumEntriesList(final String archiveName, final String albumId) {
     return store.getObject(makeAlbumEntriesPath(archiveName, albumId), AlbumDetailData.class);
   }
 
   private AlbumEntries getAlbumEntriesReadOnly(final String archiveName, final String commId) {
     final long start = System.currentTimeMillis();
     try {
       return store.getObjectReadOnly(makeAlbumEntriesPath(archiveName, commId), AlbumEntries.class);
     } finally {
       Log.i("Performance", "Loaded entries for " + archiveName + ":" + commId + " in " + (System.currentTimeMillis() - start) + " ms");
     }
   }
 
   private AlbumMeta getAlbumMeta(final String archiveName, final String commId) {
     return store.getObject(makeAlbumMetaPath(archiveName, commId), AlbumMeta.class);
   }
 
   private String getBasename(final String fileName) {
     final int lastPt = fileName.lastIndexOf('.');
     if (lastPt < 0)
       return fileName;
     return fileName.substring(0, lastPt);
   }
 
   private ServerConnection getConnectionForServer(final String serverId) {
     final Map<String, ArchiveConnection> archives = connectionMap.get();
 
     ServerConnection serverConnection = null;
     for (final Entry<String, ArchiveConnection> archiveEntry : archives.entrySet()) {
       for (final ServerConnection server : archiveEntry.getValue().listServers().values()) {
         if (server.getInstanceId().equals(serverId))
           serverConnection = server;
       }
     }
     return serverConnection;
   }
 
   private AlbumEntries getOrMakeAlbumDetail(final String archiveName, final String commId) {
     final AlbumEntries existingEntry = getAlbumEntries(archiveName, commId);
     if (existingEntry != null)
       return existingEntry;
     final AlbumEntries newEntries = new AlbumEntries();
     store.putObject(makeAlbumEntriesPath(archiveName, commId), newEntries);
     return newEntries;
   }
 
   private AlbumMeta getOrMakeAlbumMeta(final String archiveName, final String albumId) {
     final AlbumMeta existingEntry = store.getObject(makeAlbumMetaPath(archiveName, albumId), AlbumMeta.class);
     if (existingEntry != null)
       return existingEntry;
     final AlbumMeta newAlbumMeta = new AlbumMeta();
     newAlbumMeta.setArchiveName(archiveName);
     newAlbumMeta.setAlbumId(albumId);
     store.putObject(makeAlbumMetaPath(archiveName, albumId), newAlbumMeta);
     return newAlbumMeta;
   }
 
   private AlbumDetailData getOrMakeAlbumMutationList(final String archiveName, final String albumId) {
     final AlbumDetailData existingList = getAlbumEntriesList(archiveName, albumId);
     if (existingList != null)
       return existingList;
     final AlbumDetailData newList = new AlbumDetailData();
     newList.setMutations(new ArrayList<MutationEntry>());
     store.putObject(makeAlbumMutationPath(archiveName, albumId), newList);
     return newList;
   }
 
   private AlbumState getOrMakeAlbumState(final String archiveName, final String albumId) {
     final String relativePath = archiveName + "/" + albumId + "-state";
     final AlbumState savedState = store.getObject(relativePath, AlbumState.class);
     if (savedState != null)
       return savedState;
     final AlbumState newState = new AlbumState();
     store.putObject(relativePath, newState);
     return newState;
   }
 
   private File ifExsists(final File file) {
     return file.exists() ? file : null;
   }
 
   /**
    * 
    * initializes thumbnail-cache
    * 
    * @param size
    *          Cache-Size in kB
    */
   private void initThumbnailCache(final int size) {
     thumbnailCache = new LruCache<AlbumEntryIndex, File>(size) {
 
       @Override
       protected File create(final AlbumEntryIndex key) {
         return loadThumbnail(key.getArchiveName(), key.getAlbumId(), key.getAlbumEntryId());
       }
 
       @Override
       protected void entryRemoved(final boolean evicted, final AlbumEntryIndex key, final File oldValue, final File newValue) {
         if (!thumbnailsTempDir.equals(oldValue.getParentFile()))
           return;
         final boolean deleted = oldValue.delete();
         if (!deleted)
           throw new RuntimeException("Cannot delete cache-file " + oldValue);
       }
 
       @Override
       protected int sizeOf(final AlbumEntryIndex key, final File value) {
         if (!thumbnailsTempDir.equals(value.getParentFile()))
           // count only temporary entries
           return 0;
         return (int) value.length() / 1024;
       }
     };
     for (final File archiveDir : thumbnailsTempDir.listFiles(new FileFilter() {
 
       @Override
       public boolean accept(final File pathname) {
         return pathname.isDirectory();
       }
     })) {
       final String archiveName = archiveDir.getName();
       for (final File albumDir : archiveDir.listFiles(new FileFilter() {
         @Override
         public boolean accept(final File pathname) {
           return pathname.isDirectory();
         }
       })) {
         final String albumId = albumDir.getName();
         for (final File thumbnailFile : albumDir.listFiles(new FileFilter() {
 
           @Override
           public boolean accept(final File pathname) {
             return pathname.isFile() && pathname.getName().endsWith(THUMBNAIL_SUFFIX);
           }
         })) {
           final String filename = thumbnailFile.getName();
           final String albumEntryId = filename.substring(0, filename.length() - THUMBNAIL_SUFFIX.length());
           final File loadedFile = thumbnailCache.get(new AlbumEntryIndex(archiveName, albumId, albumEntryId));
           if (loadedFile == null)
             thumbnailFile.delete();
         }
       }
     }
   }
 
   private String lastPart(final String[] split) {
     if (split == null || split.length == 0)
       return null;
     return split[split.length - 1];
   }
 
   private Collection<String> listAllAlbumEntries(final String archiveName, final String albumId) {
     final Collection<String> albumEntries = callInTransaction(new Callable<Collection<String>>() {
       @Override
       public Collection<String> call() throws Exception {
         final AlbumEntries albumEntries = getAlbumEntriesReadOnly(archiveName, albumId);
         if (albumEntries == null || albumEntries.getEntries() == null)
           return Collections.emptyList();
         return new ArrayList<String>(albumEntries.getEntries().keySet());
       }
     });
     return albumEntries;
   }
 
   private File loadThumbnail(final String archiveName, final String albumId, final String albumEntryId) {
     final long startTime = System.currentTimeMillis();
     try {
       final Quad<AlbumMeta, AlbumEntries, AlbumDetailData, AlbumState> transactionResult =
                                                                                            callInTransaction(new Callable<Quad<AlbumMeta, AlbumEntries, AlbumDetailData, AlbumState>>() {
 
                                                                                              @Override
                                                                                              public
                                                                                                  Quad<AlbumMeta, AlbumEntries, AlbumDetailData, AlbumState>
                                                                                                  call() throws Exception {
                                                                                                final AlbumMeta albumMeta =
                                                                                                                            getAlbumMeta(archiveName,
                                                                                                                                         albumId);
                                                                                                final AlbumEntries albumEntries =
                                                                                                                                  getAlbumEntriesReadOnly(archiveName,
                                                                                                                                                          albumId);
                                                                                                final AlbumDetailData localData =
                                                                                                                                  getAlbumEntriesList(archiveName,
                                                                                                                                                      albumId);
                                                                                                final AlbumState albumState =
                                                                                                                              getOrMakeAlbumState(archiveName,
                                                                                                                                                  albumId);
                                                                                                return new Quad<AlbumMeta, AlbumEntries, AlbumDetailData, AlbumState>(
                                                                                                                                                                      albumMeta,
                                                                                                                                                                      albumEntries,
                                                                                                                                                                      localData,
                                                                                                                                                                      albumState);
                                                                                              }
                                                                                            });
       final AlbumMeta albumMeta = transactionResult.first;
       final AlbumEntries albumEntries = transactionResult.second;
       final AlbumDetailData localData = transactionResult.third;
       final AlbumState albumState = transactionResult.fourth;
 
       if (albumMeta == null)
         return null;
       final boolean permanentDownload = albumState.isShouldSync();
       if (albumEntries == null || albumEntries.getEntries() == null)
         return null;
       final AlbumEntryDto albumEntryDto = albumEntries.getEntries().get(albumEntryId);
       if (albumEntryDto == null)
         return null;
       final String externalSuffix = albumEntryDto.getEntryType() == AlbumEntryType.IMAGE ? ".jpg" : ".mp4";
       final File temporaryTargetFile = new File(thumbnailsTempDir, archiveName + "/" + albumId + "/" + albumEntryId + THUMBNAIL_SUFFIX);
       final String fileName = getBasename(albumEntryDto.getFileName());
       final File permanentTargetFile = new File(thumbnailsSyncDir, archiveName + "/" + albumMeta.getName() + "/" + fileName + externalSuffix);
       final File targetFile = permanentDownload ? permanentTargetFile : temporaryTargetFile;
       final File otherTargetFile = permanentDownload ? temporaryTargetFile : permanentTargetFile;
       // check if the file in the current cache is valid
       if (targetFile.exists() && targetFile.lastModified() >= albumEntryDto.getLastModified().getTime()) {
         if (otherTargetFile.exists())
           otherTargetFile.delete();
         return targetFile;
       }
       // check if there is a valid file in the other cache
       if (otherTargetFile.exists()) {
         if (otherTargetFile.lastModified() >= albumEntryDto.getLastModified().getTime()) {
           final long oldLastModified = otherTargetFile.lastModified();
           otherTargetFile.renameTo(targetFile);
           targetFile.setLastModified(oldLastModified);
           if (targetFile.exists())
             return targetFile;
 
         }
         // remove the invalid file of the other cache
         otherTargetFile.delete();
       }
       final File parentDir = targetFile.getParentFile();
       if (!parentDir.exists())
         parentDir.mkdirs();
       final Map<String, ArchiveConnection> archive = connectionMap.get();
       if (archive == null)
         return ifExsists(targetFile);
       final ArchiveConnection archiveConnection = archive.get(archiveName);
       if (archiveConnection == null)
         return ifExsists(targetFile);
       final AlbumConnection albumConnection = archiveConnection.getAlbums().get(albumMeta.getName());
       if (albumConnection == null)
         return ifExsists(targetFile);
 
       final File tempFile = new File(parentDir, tempFileId.incrementAndGet() + ".thumbnail-temp");
       if (tempFile.exists())
         tempFile.delete();
       try {
         albumConnection.readThumbnail(albumEntryId, tempFile, targetFile);
       } finally {
         if (tempFile.exists())
           tempFile.delete();
       }
       return ifExsists(targetFile);
     } finally {
       Log.i("Performance", "Loaded Thumbnail " + archiveName + ":" + albumId + ":" + albumEntryId + " in " + (System.currentTimeMillis() - startTime)
                            + " ms");
     }
   }
 
   private void loadThumbnailsOfAlbum(final String archiveName, final String albumId) {
     final Collection<String> albumEntries = listAllAlbumEntries(archiveName, albumId);
     boolean allOk = true;
     for (final String thumbnailId : albumEntries) {
       allOk &= thumbnailCache.get(new AlbumEntryIndex(archiveName, albumId, thumbnailId)) != null;
     }
     if (allOk)
       callInTransaction(new Callable<Void>() {
         @Override
         public Void call() throws Exception {
           getOrMakeAlbumState(archiveName, albumId).setSynced(true);
           return null;
         }
       });
   }
 
   private String makeAlbumDetailPath(final String archiveName, final String albumId) {
     return makeAlbumEntriesPath(archiveName, albumId);
   }
 
   private String makeAlbumEntriesPath(final String archiveName, final String commId) {
     return archiveName + "/" + commId + "-entries";
   }
 
   private String makeAlbumMetaPath(final String archiveName, final String commId) {
     return archiveName + "/" + commId + "-metadata";
   }
 
   private String makeAlbumMutationPath(final String archiveName, final String albumId) {
     return archiveName + "/" + albumId + "-mutation";
   }
 
   private Cursor makeCursorForAlbumEntries(final Collection<AlbumEntryDto> albumEntries, final String archiveName, final String albumId,
                                            final String[] projection) {
     final List<AlbumEntryDto> values = new ArrayList(albumEntries);
     Collections.sort(values, new Comparator<AlbumEntryDto>() {
 
       @Override
       public int compare(final AlbumEntryDto lhs, final AlbumEntryDto rhs) {
         return dateCompare(lhs.getCaptureDate(), rhs.getCaptureDate());
       }
 
     });
 
     final Map<String, FieldReader<AlbumEntryDto>> fieldReaders = MapperUtil.makeAnnotaedFieldReaders(AlbumEntryDto.class);
 
     fieldReaders.put(Client.AlbumEntry.THUMBNAIL, new StringFieldReader<AlbumEntryDto>() {
 
       @Override
       public String getString(final AlbumEntryDto value) {
         return Client.makeThumbnailUri(archiveName, albumId, value.getCommId()).toString();
       }
     });
     fieldReaders.put(Client.AlbumEntry.META_KEYWORDS, new StringFieldReader<AlbumEntryDto>() {
 
       @Override
       public String getString(final AlbumEntryDto value) {
         final Collection<String> keywords = value.getKeywords();
         return Client.AlbumEntry.encodeKeywords(keywords);
       }
     });
     fieldReaders.put(Client.AlbumEntry.ENTRY_URI, new StringFieldReader<AlbumEntryDto>() {
 
       @Override
       public String getString(final AlbumEntryDto value) {
         return Client.makeAlbumEntryUri(archiveName, albumId, value.getCommId()).toString();
       }
     });
 
     return cursorNotifications.addSingleAlbumCursor(new AlbumIndex(archiveName, albumId),
                                                     MapperUtil.loadCollectionIntoCursor(values, projection, fieldReaders));
   }
 
   private Cursor
       makeCursorForAlbums(final Collection<AlbumIndex> visibleAlbums, final String[] projection, final boolean alsoSynced) throws SQLException {
     final Map<AlbumIndex, AlbumMeta> loadedAlbums = new HashMap<AlbumIndex, AlbumMeta>();
     if (alsoSynced) {
       final Collection<String> entryNames =
                                             store.listRelativePath(Arrays.asList(Pattern.compile(".*"), Pattern.compile(".*-metadata")),
                                                                    AlbumMeta.class);
       for (final String relativePath : entryNames) {
         final AlbumMeta albumEntry = store.getObject(relativePath, AlbumMeta.class);
         final String archiveName = albumEntry.getArchiveName();
         final String albumId = albumEntry.getAlbumId();
         if (!getOrMakeAlbumState(archiveName, albumId).isSynced())
           continue;
         loadedAlbums.put(new AlbumIndex(archiveName, albumId), albumEntry);
       }
     }
     for (final AlbumIndex visibleAlbumIndex : visibleAlbums) {
       final AlbumMeta visibleAlbum = getAlbumMeta(visibleAlbumIndex.getArchiveName(), visibleAlbumIndex.getAlbumId());
       if (visibleAlbum != null)
         loadedAlbums.put(visibleAlbumIndex, visibleAlbum);
     }
     final ArrayList<AlbumMeta> albums = new ArrayList<AlbumMeta>(loadedAlbums.values());
     Collections.sort(albums, new Comparator<AlbumMeta>() {
       @Override
       public int compare(final AlbumMeta lhs, final AlbumMeta rhs) {
         final Date leftDate = lhs.getAlbumDate() == null ? new Date(0) : lhs.getAlbumDate();
         final Date rightDate = rhs.getAlbumDate() == null ? new Date(0) : rhs.getAlbumDate();
         return rightDate.compareTo(leftDate);
       }
     });
 
     final Map<String, FieldReader<AlbumMeta>> fieldReaders = MapperUtil.makeAnnotaedFieldReaders(AlbumMeta.class);
     fieldReaders.put(Client.Album.THUMBNAIL, new StringFieldReader<AlbumMeta>() {
       @Override
       public String getString(final AlbumMeta value) {
         if (value.getThumbnailId() == null)
           return null;
         return Client.makeThumbnailUri(value.getArchiveName(), value.getAlbumId(), value.getThumbnailId()).toString();
       }
     });
     fieldReaders.put(Client.Album.ENTRY_URI, new StringFieldReader<AlbumMeta>() {
       @Override
       public String getString(final AlbumMeta value) {
         return Client.makeAlbumUri(value.getArchiveName(), value.getAlbumId()).toString();
       }
     });
     fieldReaders.put(Client.Album.ALBUM_ENTRIES_URI, new StringFieldReader<AlbumMeta>() {
 
       @Override
       public String getString(final AlbumMeta value) {
         return Client.makeAlbumEntriesUri(value.getArchiveName(), value.getAlbumId()).toString();
       }
     });
 
     return cursorNotifications.addAllAlbumCursor(MapperUtil.loadCollectionIntoCursor(albums, projection, fieldReaders));
   }
 
   private long makeLongId(final String stringId) {
     return idCache.get(stringId).longValue();
   }
 
   private Builder makeNotificationBuilder() {
     final PendingIntent intent =
                                  PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), ServerListActivity.class),
                                                            0);
     final Builder builder =
                             new Notification.Builder(this).setContentTitle("Syncing").setSmallIcon(android.R.drawable.ic_dialog_info)
                                                           .setContentIntent(intent);
     return builder;
   }
 
   private URL makeUrl(final InetSocketAddress inetSocketAddress) {
     try {
       final InetAddress targetAddress = inetSocketAddress.getAddress();
       if (targetAddress instanceof Inet6Address) {
         final int scopedInterface = ((Inet6Address) targetAddress).getScopeId();
         if (scopedInterface != 0) {
           return new URL("http", inetSocketAddress.getAddress().getHostAddress(), inetSocketAddress.getPort(), "rest");
         }
       }
       return new URL("http", inetSocketAddress.getAddress().getHostAddress(), inetSocketAddress.getPort(), "rest");
     } catch (final MalformedURLException e) {
       throw new RuntimeException("Cannot create URL for Socket " + inetSocketAddress, e);
     }
   }
 
   private void notifyAlbumChanged(final int id) {
     cursorNotifications.notifySingleAlbumCursorChanged(id);
   }
 
   private void notifyAlbumListChanged() {
     cursorNotifications.notifyAllAlbumCursorsChanged();
   }
 
   private <O> boolean objectEquals(final O v1, final O v2) {
     if (v1 == v2)
       return true;
     if (v1 == null || v2 == null)
       return false;
     return v1.equals(v2);
   }
 
   private PingResponse pingService(final URL url) {
     final RestTemplate restTemplate = new RestTemplate(true);
     try {
       try {
         final ResponseEntity<PingResponse> entity = restTemplate.getForEntity(url + "/ping.json", PingResponse.class);
         final boolean pingOk = entity.getStatusCode().series() == Series.SUCCESSFUL;
         if (pingOk)
           return entity.getBody();
         else {
           Log.i(SERVICE_TAG, "Error connecting Service at " + url + ", " + entity.getStatusCode() + " " + entity.getStatusCode().getReasonPhrase());
           return null;
         }
       } catch (final ResourceAccessException ex) {
         final Throwable cause = ex.getCause();
         if (cause != null && cause instanceof ConnectException) {
           // try next
           Log.d(SERVICE_TAG, "Connect to " + url + "/ failed, try more");
           return null;
         } else if (cause != null && cause instanceof UnknownHostException) {
           Log.d(SERVICE_TAG, "Connect to " + url + "/ failed cause of spring-bug with ipv6, try more");
           return null;
         } else
           throw ex;
       } catch (final RestClientException ex) {
         Log.d(SERVICE_TAG, "Connect to " + url + "/ failed, try more");
         return null;
       }
     } catch (final Exception ex) {
       throw new RuntimeException("Cannot connect to " + url, ex);
     }
   }
 
   private void pollServers() {
     if (pollServerSemaphore.tryAcquire())
       try {
         final MDnsListener listener = dnsListener;
         if (listener != null) {
           listener.pollForServices(true);
         }
       } catch (final Throwable t) {
         Log.w(SERVICE_TAG, "Exception while polling", t);
       } finally {
         pollServerSemaphore.release();
       }
     else
       // refresh server-state anyway
       updateServerCursors();
   }
 
   /**
    * Collect all pending Mutations and send it to the server
    * 
    * @param albumConnection
    * @param albumId
    * @param albumId
    */
   private void pushPendingMetadataUpdate(final AlbumConnection albumConnection, final String archiveName, final String albumId) {
     final AlbumDetailData mutations = callInTransaction(new Callable<AlbumDetailData>() {
 
       @Override
       public AlbumDetailData call() throws Exception {
         return getAlbumEntriesList(archiveName, albumId);
       }
     });
     if (mutations == null || mutations.getMutations().isEmpty())
       // no pending mutation found
       return;
     albumConnection.updateMetadata(mutations.getMutations());
   }
 
   private <K, V> V putIfNotExists(final ConcurrentMap<K, V> map, final K key, final V emptyValue) {
     final V existingValue = map.putIfAbsent(key, emptyValue);
     if (existingValue != null)
       return existingValue;
     return emptyValue;
   }
 
   private void refreshAlbumDetail(final AlbumConnection albumConnection, final String archiveName, final String albumId) {
     // read data from Server
     final AlbumDto albumDto = albumConnection.getAlbumDetail();
     callInTransaction(new Callable<Void>() {
 
       @Override
       public Void call() throws Exception {
         // clear pending mutation-data if it exists
         final AlbumDetailData mutationList = getAlbumEntriesList(archiveName, albumId);
         final Collection<MutationEntry> mutations =
                                                     mutationList != null && mutationList.getMutations() != null
                                                                                                                ? mutationList.getMutations()
                                                                                                                : Collections.<MutationEntry> emptyList();
         final AlbumMeta albumMeta = getAlbumMeta(archiveName, albumId);
         final Map<String, AlbumEntryDto> entries = getOrMakeAlbumDetail(archiveName, albumId).getEntries();
         entries.clear();
         entries.putAll(albumDto.getEntries());
         albumMeta.setLastModified(albumDto.getLastModified());
         albumMeta.setAutoAddDate(albumDto.getAutoAddDate());
         albumMeta.setEntryCount(albumDto.getEntries().size());
 
         final AtomicLong dateSum = new AtomicLong(0);
         final AtomicInteger dateCount = new AtomicInteger(0);
 
         String thumbnailId = albumMeta.getThumbnailId();
         for (final Entry<String, AlbumEntryDto> albumImageEntry : albumDto.getEntries().entrySet()) {
 
           final AlbumEntryDto entryDto = albumImageEntry.getValue();
           final String imageId = albumImageEntry.getKey();
           final String editableMetadataHash = entryDto.getEditableMetadataHash();
           for (final Iterator<MutationEntry> entryIterator = mutations.iterator(); entryIterator.hasNext();) {
             final MutationEntry mutationEntry = entryIterator.next();
             if (mutationEntry.getAlbumEntryId().equals(imageId) && !mutationEntry.getBaseVersion().equals(editableMetadataHash))
               entryIterator.remove();
           }
           if (thumbnailId == null)
             thumbnailId = albumImageEntry.getKey();
 
           if (entryDto.getCaptureDate() != null) {
             dateCount.incrementAndGet();
             dateSum.addAndGet(entryDto.getCaptureDate().getTime());
           }
         }
         albumMeta.setThumbnailId(thumbnailId);
         if (dateCount.get() > 0) {
           albumMeta.setAlbumDate(new Date(dateSum.longValue() / dateCount.longValue()));
         }
         return null;
       }
     });
   }
 
   private void registerScreenOnOff() {
     final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
     intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
     registerReceiver(new PowerStateReceiver(), intentFilter);
     executorService.schedule(new Runnable() {
 
       @Override
       public void run() {
         PowerStateReceiver.notifyPowerState(getApplicationContext());
       }
     }, 5, TimeUnit.SECONDS);
   }
 
   private void saveAlbumMeta(final String archiveName, final String commId, final AlbumMeta albumMeta) {
     store.putObject(makeAlbumMetaPath(archiveName, commId), albumMeta);
   }
 
   private synchronized void startFastPolling() {
     if (fastUpdatePollingFuture == null || fastUpdatePollingFuture.isCancelled())
       fastUpdatePollingFuture = executorService.scheduleWithFixedDelay(new Runnable() {
 
         @Override
         public void run() {
           pollServers();
         }
       }, 2, 3, TimeUnit.SECONDS);
 
   }
 
   private synchronized void startRunning() {
     if (running.get())
       return;
     running.set(true);
     Log.i(SERVICE_TAG, "Synchronisation started");
     dnsListener.startListening();
     final Notification notification = makeNotificationBuilder().getNotification();
     notificationManager.notify(NOTIFICATION, notification);
     startSlowPolling();
   }
 
   private synchronized void startSlowPolling() {
     if (slowUpdatePollingFuture == null || slowUpdatePollingFuture.isCancelled())
       slowUpdatePollingFuture = executorService.scheduleWithFixedDelay(new Runnable() {
         @Override
         public void run() {
           pollServers();
         }
       }, 10, 20, TimeUnit.MINUTES);
 
   }
 
   private synchronized void stopFastPolling() {
     if (fastUpdatePollingFuture != null)
       fastUpdatePollingFuture.cancel(false);
   }
 
   private synchronized void stopRunning() {
     dnsListener.stopListening();
     notificationManager.cancel(NOTIFICATION);
     stopSlowPolling();
     stopFastPolling();
     running.set(false);
   }
 
   private synchronized void stopSlowPolling() {
     if (slowUpdatePollingFuture != null)
       slowUpdatePollingFuture.cancel(false);
   }
 
   private void updateAlbumDetail(final String archiveName, final String albumName, final AlbumConnection albumConnection, final int totalAlbumCount,
                                  final AtomicInteger albumCounter) {
     final ConcurrentMap<String, String> visibleAlbumsOfArchive = putIfNotExists(visibleAlbums, archiveName, new ConcurrentHashMap<String, String>());
 
     final Notification.Builder builder = makeNotificationBuilder();
     builder.setContentTitle("DB Update");
     builder.setContentText("Downloading " + lastPart(albumName.split("/")) + " from " + archiveName);
     builder.setProgress(totalAlbumCount, albumCounter.incrementAndGet(), false);
     notificationManager.notify(NOTIFICATION, builder.getNotification());
 
     final AtomicReference<String> albumId = new AtomicReference<String>();
     final AtomicBoolean shouldUpdateMeta = new AtomicBoolean(false);
     final AtomicBoolean shouldLoadThumbnails = new AtomicBoolean(false);
     callInTransaction(new Callable<Void>() {
       @Override
       public Void call() throws Exception {
 
         final String commId = albumConnection.getCommId();
         final AlbumMeta existingAlbumMeta = getOrMakeAlbumMeta(archiveName, commId);
         final AlbumState mutationList = getOrMakeAlbumState(archiveName, commId);
         existingAlbumMeta.setName(albumName);
         final boolean albumModified = !dateEquals(existingAlbumMeta.getLastModified(), albumConnection.lastModified());
         shouldUpdateMeta.set(albumModified);
         if (albumModified) {
           mutationList.setSynced(false);
         }
         shouldLoadThumbnails.set(mutationList.isShouldSync() && !mutationList.isSynced());
 
         visibleAlbumsOfArchive.put(albumName, commId);
         albumId.set(commId);
         return null;
       }
 
     });
     if (shouldUpdateMeta.get())
       refreshAlbumDetail(albumConnection, archiveName, albumId.get());
     pushPendingMetadataUpdate(albumConnection, archiveName, albumId.get());
     if (shouldLoadThumbnails.get())
       loadThumbnailsOfAlbum(archiveName, albumId.get());
   }
 
   private void updateAlbumsOnDB() {
     final boolean hasLock = updateLockSempahore.tryAcquire();
     if (hasLock) {
       try {
         final Notification.Builder builder = makeNotificationBuilder();
         builder.setContentTitle("DB Update");
         notificationManager.notify(NOTIFICATION, builder.getNotification());
 
         // remove invisible archives
         visibleAlbums.keySet().retainAll(connectionMap.get().keySet());
 
         final Collection<Callable<Void>> updateDetailRunnables = new ArrayList<Callable<Void>>();
 
         for (final Entry<String, ArchiveConnection> archive : connectionMap.get().entrySet()) {
           if (!running.get())
             break;
           final String archiveName = archive.getKey();
           final Map<String, AlbumConnection> albums = archive.getValue().listAlbums();
           // remove invisible albums
           putIfNotExists(visibleAlbums, archiveName, new ConcurrentHashMap<String, String>()).keySet().retainAll(albums.keySet());
 
           final AtomicInteger albumCounter = new AtomicInteger();
           builder.setContentText("Downloading from " + archiveName);
           for (final Entry<String, AlbumConnection> albumEntry : albums.entrySet()) {
             if (!running.get())
               break;
             final String albumName = albumEntry.getKey();
             final AlbumConnection albumConnection = albumEntry.getValue();
             updateDetailRunnables.add(new Callable<Void>() {
               @Override
               public Void call() throws Exception {
                 try {
                   updateAlbumDetail(archiveName, albumName, albumConnection, albums.size(), albumCounter);
                 } catch (final Throwable t) {
                   Log.e(SERVICE_TAG, "Exception while updateing data", t);
                 }
                 return null;
               }
             });
           }
         }
         wrappedExecutorService.invokeAll(updateDetailRunnables);
       } catch (final Throwable t) {
         Log.e(SERVICE_TAG, "Exception while updateing data", t);
       } finally {
         updateLockSempahore.release();
         notificationManager.notify(NOTIFICATION, makeNotificationBuilder().getNotification());
       }
     }
   }
 
   private void updateServerCursors() {
     cursorNotifications.notifyServerStateModified();
   }
 }
