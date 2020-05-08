 package communication;
 
 /**
  * Created with IntelliJ IDEA.
  * User: naveed
  * Date: 11/4/12
  * Time: 1:22 PM
  * To change this template use File | Settings | File Templates.
  */
 
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
         HeartbeatDelayInms = 1;
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
         Integer randomTarget = rand.nextInt(this.memberList.length());
         return this.memberList.getMember(randomTarget);
     }
 
     public void start(){
 
         new Thread(new Runnable() {
            
             public void run() {
                 try {
                     Thread.sleep(HeartbeatDelayInms);
                 } catch (InterruptedException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
                 startInfecting();
             }
         }).start();
 
     }
 
     private void startInfecting(){
 
         while(!shouldStop.get()){
             //TODO: add waiting time
             for(Integer i = 0; i < this.numOfTargets; i++){
                 ProcessIdentifier infectedProcess = selectRandomTarget();
                 sendMessage(infectedProcess);
             }
         }
     }
 
     void sendMessage(ProcessIdentifier process){
         new UDPClient().sendMessage(process);
     }
 
 
 
 }
