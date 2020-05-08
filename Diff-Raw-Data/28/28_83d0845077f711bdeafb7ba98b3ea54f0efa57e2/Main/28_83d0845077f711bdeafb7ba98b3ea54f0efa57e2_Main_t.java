 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pritom
  * Date: 4/5/13
  * Time: 10:10 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Main {
 
     public static final int MAX_FILE_SIZE = 2000;
    public static final int MAX_NO_OF_FILES = 500;
    public static final int MAX_NO_OF_PEERS = 10;
     public static final int FILE_AVAILABILITY_MODULO = 100;
     public static final int MAX_NO_OF_ACTIONS = 3;
    public static final int MAX_NO_OF_ROUNDS_PER_PEER = 1000;
     public static final float MIN_SHARE_RATIO = 0.5f;
     public static final int FILE_SHARE_PROBABILITY = 99;
 
     private static final Map<Integer, Integer> bandwidthMap;
 
     static {
         Map bandMap = new HashMap<Integer, Integer>();
         bandMap.put(0, 128);
         bandMap.put(1, 256);
         bandMap.put(2, 512);
         bandMap.put(3, 768);
         bandMap.put(4, 1024);
         bandMap.put(5, 1536);
         bandMap.put(6, 2048);
         bandwidthMap = Collections.unmodifiableMap(bandMap);
     }
 
     public static void main(String[] args) {
 
         Map<Integer, TrackedFile> trackedFileMap = initializeTrackedFiles();
         Map<Integer, Peer> mapOfPeers = createPeerMap();
         distributeFilesAmongPeers(trackedFileMap, mapOfPeers);
         //displayFileSeeders(trackedFileMap);
 
         Tracker tracker = new Tracker();
         tracker.setAvailableFiles(trackedFileMap);
 
         for (Peer peer : mapOfPeers.values()) {
             peer.setTracker(tracker);
         }
 
         for (Peer peer : mapOfPeers.values()) {
             peer.getThread().start();
         }
 
         for (Peer peer : mapOfPeers.values()) {
             try {
                 peer.getThread().join();
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
         }
 
         for (Peer peer : mapOfPeers.values()) {
             peer.showFinalState();
         }
     }
 
     private static void displayFileSeeders(Map<Integer, TrackedFile> trackedFileMap) {
         for (int filedId : trackedFileMap.keySet()) {
             TrackedFile trackedFile = trackedFileMap.get(filedId);
             trackedFile.showSeeders();
         }
     }
 
     private static void distributeFilesAmongPeers(Map<Integer, TrackedFile> trackedFileMap, Map<Integer, Peer> mapOfPeers) {
         Random moduloGenerator = new Random();
 
         for (int fileId = 1; fileId <= MAX_NO_OF_FILES; fileId++) {
 
             int outerCoinToss = moduloGenerator.nextInt(FILE_AVAILABILITY_MODULO);
             if (outerCoinToss >= FILE_SHARE_PROBABILITY) {
                 int numberOfSharingPeers = moduloGenerator.nextInt(MAX_NO_OF_PEERS);
 
                 for (int counter = 0; counter < numberOfSharingPeers; counter++) {
                     Integer peerId = moduloGenerator.nextInt(MAX_NO_OF_PEERS) + 1;
 
                     Peer peer = mapOfPeers.get(peerId);
                     int innerCoinToss = moduloGenerator.nextInt(FILE_AVAILABILITY_MODULO);
                     //System.out.println(coinToss);
                     if (innerCoinToss >= FILE_SHARE_PROBABILITY && peer != null) {
                         TrackedFile trackedFile = trackedFileMap.get(fileId);
                         if (trackedFile != null && trackedFile.getSeeders() != null) {
                             trackedFile.getSeeders().put(peer.getId(), peer);
 
                             SharedFile file = new SharedFile();
                             file.setId(fileId);
                             file.setSize(trackedFile.getFileSize());
                             file.setUploadedSize(0);
                             peer.getSharedFiles().put(fileId, file);
                         }
                     }
                 }
             }
         }
     }
 
     private static Map<Integer, Peer> createPeerMap() {
         Map<Integer, Peer> mapOfPeers = new LinkedHashMap<Integer, Peer>();
         Random bandwidthChooser = new Random();
 
         for (int peerId = 1; peerId <= MAX_NO_OF_PEERS; peerId++) {
             Peer peer = new Peer();
             peer.setId(peerId);
             peer.setSharedFiles(new ConcurrentHashMap<Integer, SharedFile>());
 
             int bandwidthMapIndex = bandwidthChooser.nextInt(bandwidthMap.size());
             peer.setUploadSpeed(bandwidthMap.get(bandwidthMapIndex));
             peer.setDownloadSpeed(bandwidthMap.get(bandwidthMapIndex));
             peer.setAmountDownloaded(MAX_FILE_SIZE);
             peer.setAmountUploaded(MAX_FILE_SIZE);
             peer.setShareRatio(1.0f);
             mapOfPeers.put(peerId, peer);
         }
         return mapOfPeers;
     }
 
     private static Map<Integer, TrackedFile> initializeTrackedFiles() {
         Random fileSizeGenerator = new Random();
         Map<Integer, TrackedFile> trackedFileMap = new ConcurrentHashMap<Integer, TrackedFile>();
 
         for (int fileId = 1; fileId <= MAX_NO_OF_FILES; fileId++) {
             int fileSize = fileSizeGenerator.nextInt(MAX_FILE_SIZE) + 1;
 
             TrackedFile trackedFile = new TrackedFile();
             trackedFile.setId(fileId);
             trackedFile.setFileSize(fileSize);
             trackedFile.setSeeders(new ConcurrentHashMap<Integer, Peer>());
 
             trackedFileMap.put(fileId, trackedFile);
         }
         return trackedFileMap;
     }
 
 
 }
