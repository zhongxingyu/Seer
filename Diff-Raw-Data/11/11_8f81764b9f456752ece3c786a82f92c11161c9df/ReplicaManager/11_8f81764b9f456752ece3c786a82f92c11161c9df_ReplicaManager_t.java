 package filesystem;
 
 import communication.Messages.ProcessIdentifier;
 import communication.Messages.FileIdentifier;
 import membership.MemberList;
 import membership.Proc;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicBoolean;
 import membership.MemberList;
 
 public class ReplicaManager {
 
     private Proc proc;
     private AtomicBoolean shouldStop;
     private Integer replicaCount;
     private static final Integer SCAN_INTERVAL = 2000;
 
     public ReplicaManager(){
         shouldStop = new AtomicBoolean(false);
         setReplicaCount(2);    //TODO High level class should handle this
     }
 
     public void start(){
         new Thread(new Runnable() {
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
         }).start();
 
     }
 
     private FileList getFileList(){
         return proc.getSDFS().getFileList();
     }
 
     private void scanFileList(){
         HashMap<String, Integer> replicaCounter = new HashMap<String, Integer>();
         for(FileIdentifier f : getFileList()){
             if(!replicaCounter.containsKey(f.getFilepath())){
                 replicaCounter.put(f.getFilepath(), 0);
             }
             else {
                 //TODO recheck if its correct
                 replicaCounter.put(f.getFilepath(), replicaCounter.get(f.getFilepath())+1);
             }
         }
 
         for(Map.Entry<String, Integer> e : replicaCounter.entrySet()){
             Integer replicaCount = e.getValue();
             if(replicaCount < this.replicaCount){
                 Integer requiredReplicas = this.replicaCount - replicaCount;
                 createReplicas(requiredReplicas, e.getKey());
             }
         }
     }
 
     public void createReplicas(Integer requiredReplicas, String SDFSFilepath){
         Random rand = new Random();
         MemberList memberList = proc.getMemberList();
         ProcessIdentifier[] replicateTo = new ProcessIdentifier[requiredReplicas];
        for(int i = 0; i < requiredReplicas;){
             ProcessIdentifier randomProcess = selectRandomProcess();
             if(!exists(randomProcess, replicateTo)){
                 replicateTo[i] = selectRandomProcess();
                 new FileOperations().sendPutMessage(SDFSFilepath, randomProcess.getIP(), randomProcess.getPort());
                i++;
             }
         }
     }
 
     public Boolean exists(ProcessIdentifier processIdentifier, ProcessIdentifier[] replicateTo){
         synchronized (this){
             for(int i=0; i < replicateTo.length; i++){
                 ProcessIdentifier tmp = replicateTo[i];
                 if(theSameProcessIdentifier(tmp, processIdentifier)){
                     return true;
                 }
             }
             return false;
         }
     }
 
     private boolean theSameProcessIdentifier(ProcessIdentifier p1, ProcessIdentifier p2){
         return p1.getId().equals(p2.getId());
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
 
     public void stop(){
         shouldStop.set(true);
         Thread.interrupted();
     }
 
     public void setReplicaCount(Integer replicaCount){
         this.replicaCount = replicaCount;
     }
 
 }
