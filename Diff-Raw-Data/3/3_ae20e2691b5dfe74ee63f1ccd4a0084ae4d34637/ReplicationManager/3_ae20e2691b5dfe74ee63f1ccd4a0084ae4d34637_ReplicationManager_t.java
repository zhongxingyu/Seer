 package filesystem;
 
 import communication.FileIdentifierFactory;
 import communication.message.Messages.ProcessIdentifier;
 import communication.message.Messages.FileIdentifier;
 import membership.Proc;
 
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * ReplicationManager is responsible for distributing replicas among system.
  */
 public class ReplicationManager {
 
     private Proc proc;
     private AtomicBoolean shouldStop;
     private Thread sleepThread;
 
     private static final Integer SCAN_INTERVAL = 35000; //scanning interval
 //    private static final Integer SCAN_INTERVAL = 99999999; //scanning interval
     private static final Integer REPLICA_COUNT = 2;     //how many replicas each file should have, this is a constant
 
     public ReplicationManager(){
         shouldStop = new AtomicBoolean(false);
     }
 
     /**
      * it scan the file list every SCAN_INTERVAL time.
      */
     public void start(){
         sleepThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 while(!shouldStop.get()){
                     try {
                         Thread.sleep(SCAN_INTERVAL);
                     } catch(InterruptedException e) {
                         //
                     }
                     scanFileList();
                 }
             }
         });
 
         sleepThread.start();
 
     }
 
     private FileList getFileList(){
         return proc.getSDFS().getFileList();
     }
 
     /**
      * Try to count up the current replicas of each files and decide whether it is required to be replicated.
      */
     private void scanFileList(){
         HashMap<String, Integer> replicaCounter = new HashMap<String, Integer>();
         List<FileIdentifier> fileShouldBeReplicated = new LinkedList<FileIdentifier>();
 
         for(FileIdentifier f: getFileList()) {
             if(!f.getFileStoringProcess().getId().equals(proc.getId())) {   //this file doesn't stored locally
                 continue;
             }
             if(proc.getSDFS().isAvailable(f)) {
                 fileShouldBeReplicated.add(f);
             }
         }
 
         for(FileIdentifier f : getFileList()){
 
             String key = f.getFileName();
 
             if(!replicaCounter.containsKey(key)) {
                 replicaCounter.put(key, 0);
             }
 
             if(proc.getSDFS().isAvailable(f)) {
                 replicaCounter.put(key, replicaCounter.get(key)+1);
             }
         }
 
         for(FileIdentifier f: fileShouldBeReplicated) {
             Integer replicaCount = replicaCounter.get(f.getFileName());
             Integer requiredReplicas = Math.min(REPLICA_COUNT, proc.getMemberList().size());
             requiredReplicas = requiredReplicas - replicaCount;
             if(requiredReplicas <=0 ) {
                 continue;
             }
             createReplicas(requiredReplicas, f);
         }
     }
 
     public void createReplicas(Integer requiredReplicas, FileIdentifier fid){
 
         String fileName = fid.getFileName();
 
         List<ProcessIdentifier> replicateTo = new ArrayList<ProcessIdentifier>(requiredReplicas);
 
         for(int i = 0; i < requiredReplicas;){
             ProcessIdentifier randomProcess = selectRandomProcess();
             if(!replicateTo.contains(randomProcess)
                     && notMySelf(randomProcess) && notStoredOnProcess(randomProcess, fileName)){
                 replicateTo.add(randomProcess);
                 new FileOperations().setProc(proc).sendPutMessage(fid, randomProcess.getIP(), randomProcess.getPort());
                 FileIdentifier f = FileIdentifierFactory.generateFileIdentifier
                         (randomProcess, fileName, FileState.syncing, fid.getLastWriteTime());
                 proc.getSDFS().addSyncEntryToFileList(f, proc.getTimeStamp());
                 i++;
             }
         }
     }
 
     private boolean notMySelf(ProcessIdentifier identifier) {
         return ! identifier.getId().equals(proc.getId());
     }
 
     private boolean notStoredOnProcess(ProcessIdentifier identifier, String fileName) {
 //        FileIdentifier fileIdentifier = FileIdentifierFactory.generateFileIdentifier(identifier, fileName, FileState.available);
 //        return proc.getSDFS().getFileList().find(fileIdentifier) == -1;
         return ! proc.getSDFS().isStoredOnProcess(identifier, fileName);
     }
 
     public ProcessIdentifier selectRandomProcess(){
         Random rand = new Random();
         Integer randomProcess = rand.nextInt(proc.getMemberList().size());
         if(randomProcess < 0) randomProcess = 0;
         return proc.getMemberList().get(randomProcess);
     }
 
     public void setProc(Proc proc){
         this.proc = proc;
     }
 
     public void interruptSleep() {
        if(sleepThread == null) {
            return;
        }
         sleepThread.interrupt();
     }
 
     public void stop(){
         shouldStop.set(true);
         Thread.interrupted();
     }
 }
