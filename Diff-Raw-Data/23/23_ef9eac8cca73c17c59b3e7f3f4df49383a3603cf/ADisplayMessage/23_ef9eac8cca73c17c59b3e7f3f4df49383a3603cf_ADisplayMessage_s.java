 package npcs.actions;
 
 import fap_java.NPC;
 
 public class ADisplayMessage implements Action {
     private String message;
     public ADisplayMessage(String message) {
         super();
         this.message = message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void execute(NPC whoLaunches) {
         System.out.println(message);
         //Loop
        if(whoLaunches != null && whoLaunches.getIterator() < whoLaunches.getActions().size()){
             whoLaunches.execute();
         }
     }
 }
