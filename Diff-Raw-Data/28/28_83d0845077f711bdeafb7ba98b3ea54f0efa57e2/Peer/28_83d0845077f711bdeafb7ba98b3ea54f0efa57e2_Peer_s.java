 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pritom
  * Date: 4/5/13
  * Time: 10:26 AM
  * To change this template use File | Settings | File Templates.
  */
public class Peer implements Runnable{
 
     private int id;
     private float amountDownloaded;
     private float amountUploaded;
     private float shareRatio;
     private float satisfaction;
     private int satisfactionCount;
     private int disappointmentCount;
     private long storageCapacity;
     private int downloadSpeed;
     private int uploadSpeed;
     private Tracker tracker;
     private Map<Integer, SharedFile> sharedFiles;
     private Thread thread;
 
     private static final int DISSATISFACTION_PENALTY = 5;
 
     public Peer() {
         thread = new Thread(this);
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public int getDownloadSpeed() {
         return downloadSpeed;
     }
 
     public void setDownloadSpeed(int downloadSpeed) {
         this.downloadSpeed = downloadSpeed;
     }
 
     public int getUploadSpeed() {
         return uploadSpeed;
     }
 
     public void setUploadSpeed(int uploadSpeed) {
         this.uploadSpeed = uploadSpeed;
     }
 
     public float getAmountDownloaded() {
         return amountDownloaded;
     }
 
     public void setAmountDownloaded(float amountDownloaded) {
         this.amountDownloaded = amountDownloaded;
     }
 
     public float getAmountUploaded() {
         return amountUploaded;
     }
 
     public void setAmountUploaded(float amountUploaded) {
         this.amountUploaded = amountUploaded;
     }
 
     public float getSatisfaction() {
         return satisfaction;
     }
 
     public void setSatisfaction(float satisfaction) {
         this.satisfaction = satisfaction;
     }
 
     public float getShareRatio() {
         return shareRatio;
     }
 
     public void setShareRatio(float shareRatio) {
         this.shareRatio = shareRatio;
     }
 
     public long getStorageCapacity() {
         return storageCapacity;
     }
 
     public void setStorageCapacity(long storageCapacity) {
         this.storageCapacity = storageCapacity;
     }
 
     public Map<Integer, SharedFile> getSharedFiles() {
         return sharedFiles;
     }
 
     public void setSharedFiles(Map<Integer, SharedFile> sharedFiles) {
         this.sharedFiles = sharedFiles;
     }
 
     public Tracker getTracker() {
         return tracker;
     }
 
     public void setTracker(Tracker tracker) {
         this.tracker = tracker;
     }
 
     public int getSatisfactionCount() {
         return satisfactionCount;
     }
 
     public void setSatisfactionCount(int satisfactionCount) {
         this.satisfactionCount = satisfactionCount;
     }
 
     public int getDisappointmentCount() {
         return disappointmentCount;
     }
 
     public void setDisappointmentCount(int disappointmentCount) {
         this.disappointmentCount = disappointmentCount;
     }
 
     public Thread getThread() {
         return thread;
     }
 
     public void setThread(Thread thread) {
         this.thread = thread;
     }
 
     public void showSharedFiles() {
         System.out.println("TOTAL FILES : " + sharedFiles.size());
         for (int sharedFileId : sharedFiles.keySet()) {
             System.out.println("Id : " + sharedFileId + ", size : " + sharedFiles.get(sharedFileId).getSize() + ", uploadedSize : " + sharedFiles.get(sharedFileId).getUploadedSize());
         }
     }
 
     public void simulatePeer() {
 
         Random random = new Random();
         int selectedFileId = random.nextInt(Main.MAX_NO_OF_FILES) + 1;
         int selectedActionIndex = random.nextInt(Main.MAX_NO_OF_ACTIONS);
         PeerAction selectedPeerAction = PeerAction.values()[selectedActionIndex];
         String selectedAction = selectedPeerAction.getAction();
 
         if (selectedPeerAction == PeerAction.DOWNLOAD && sharedFiles.containsKey(selectedFileId)) {
             return;
         } else if (selectedPeerAction == PeerAction.SHARE && sharedFiles.containsKey(selectedFileId)) {
             return;
         } else if (selectedPeerAction == PeerAction.STOP && !sharedFiles.containsKey(selectedFileId)) {
             return;
         }
 
         tracker.sendMessage(this, selectedAction, selectedFileId);
 
     }
 
     public void downloadFile(TrackedFile fileToBeDownloaded) {
         //TODO : add download logic
         Map<Integer, Peer> seeders = fileToBeDownloaded.getSeeders();
         if (seeders.isEmpty()) {
             satisfaction -= DISSATISFACTION_PENALTY;
             ++disappointmentCount;
        }
        else {
             int totalUploadSpeedOfFile = fileToBeDownloaded.getTotalUploadSpeed();
             int fileDownloadSpeed = Math.min(downloadSpeed, totalUploadSpeedOfFile);
             int downloadedFileSize = fileToBeDownloaded.getFileSize();
 
             float downloadCompletionTime = downloadedFileSize / fileDownloadSpeed;
 
             satisfaction += downloadCompletionTime;
             ++satisfactionCount;
             amountDownloaded += downloadedFileSize;
             float probableShareRatio;
             probableShareRatio = updateShareRatio();
             if (probableShareRatio < Main.MIN_SHARE_RATIO) {
                 //System.out.println("Share Ratio: " + shareRatio);
                 return;
             }
             shareRatio = probableShareRatio;
 
             for (Peer seeder : seeders.values()) {
                 float amountUploadedBySeeder = (uploadSpeed / totalUploadSpeedOfFile) * downloadedFileSize;
                 seeder.setAmountUploaded(seeder.getAmountUploaded() + amountUploadedBySeeder);
                 seeder.updateShareRatio();
             }
 
         }
 
     }
 
     private float updateShareRatio() {
         //TODO: should add our proposed non-linear SRE logic instead of direct calculation in future
         return amountUploaded / amountDownloaded;
     }
 
     @Override
     public void run() {
        for (int i = 0; i<Main.MAX_NO_OF_ROUNDS_PER_PEER; i++) {
             simulatePeer();
         }
     }
 
     public void showFinalState() {
         System.out.println("Share Ratio: " + shareRatio + " , Satisfaction: " + satisfaction + "(" + satisfactionCount + "/" + disappointmentCount
                 + ") , AmountDownloaded: " + amountDownloaded + " , AmountUploaded: " + amountUploaded);
     }
 }
