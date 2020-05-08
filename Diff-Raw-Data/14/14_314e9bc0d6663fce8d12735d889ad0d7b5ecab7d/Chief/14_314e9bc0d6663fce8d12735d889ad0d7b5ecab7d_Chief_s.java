 package museumassault;
 
 import java.util.HashMap;
 
 /**
  *
  * @author André
  */
 public class Chief extends Thread
 {
     static final int CHIEF_ID = -1;
 
     SharedSite exterior;
     HashMap roomsStatus = new HashMap();
 
     /**
      *
      * @param exterior
      */
     public Chief (SharedSite exterior)
     {
         this.exterior = exterior;
     }
 
     /**
      *
      */
     @Override
     public void run()
     {
         while (true) {
 
             System.out.println("[Chief] Aprraising sit..");
             Integer roomId = this.exterior.appraiseSit();
             if (roomId != null) {
 
                 System.out.println("[Chief] There is still rooms to rob, preparing assault to room #" + roomId + "..");
 
                 Integer teamId = this.exterior.prepareAssaultParty(roomId);
                 if (teamId != null) {
                     System.out.println("[Chief] Sending party #" + teamId + "..");
                     this.exterior.sendAssaultParty(teamId);
                     System.out.println("[Chief] Party #" + teamId + " sent..");
                     continue;
                } else {
                    System.out.println("[Chief] No more rooms to rob..");
                 }

             }
 
             System.out.println("[Chief] Taking a rest waiting for thiefs..");
             Integer thiefId = this.exterior.takeARest();
             if (thiefId == null) {
                 break;
             }
 
             System.out.println("[Chief] Arrived thief #" + thiefId + "..");
             System.out.println("[Chief] Collecting canvas of thief #" + thiefId + "..");
             this.exterior.collectCanvas(thiefId);
         }
     }
 }
