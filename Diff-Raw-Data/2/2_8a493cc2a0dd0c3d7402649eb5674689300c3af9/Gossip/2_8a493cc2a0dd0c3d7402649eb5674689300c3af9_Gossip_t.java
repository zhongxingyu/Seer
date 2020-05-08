 package communication;
 
 import membership.MemberList;
 import communication.Messages.ProcessIdentifier;
 
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 public class Gossip {
 
     private Integer numOfTargets;
     private MemberList memberList;
     private AtomicBoolean shouldStop;
     private long HeartbeatDelayInms;
 
 
 
     public Gossip(){
         this.shouldStop.set(false);
         HeartbeatDelayInms = 200;
     }
 
     public void setNumOfTargets(Integer numOfTargets){
         this.numOfTargets = numOfTargets;
     }
 
     public void setMemberList(MemberList memberList){
         this.memberList = memberList;
     }
 
     public MemberList getMemberList(){
         return this.memberList;
     }
 
     public ProcessIdentifier selectRandomTarget(){
         Random rand = new Random();
         Integer randomTarget = rand.nextInt(this.memberList.size());
        return this.memberList.getProcessIdentifier(randomTarget);
     }
 
     public void start(){
 
         new Thread(new Runnable() {
 
             public void run() {
                 while(!shouldStop.get()) {
                     try {
                         Thread.sleep(HeartbeatDelayInms);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                     startInfecting();
                 }
             }
         }).start();
 
     }
 
     private void startInfecting(){
         for(Integer i = 0; i < this.numOfTargets; i++){
             ProcessIdentifier infectedProcess = selectRandomTarget();
             sendMessage(infectedProcess);
         }
     }
 
     void sendMessage(ProcessIdentifier process){
         UDPClient udpClient = new UDPClient(process);
         udpClient.sendMessage("111");   //TODO: this is just test code
     }
 
     public void stop() {
         shouldStop.set(true);
         Thread.interrupted();
     }
 
 
 
 }
