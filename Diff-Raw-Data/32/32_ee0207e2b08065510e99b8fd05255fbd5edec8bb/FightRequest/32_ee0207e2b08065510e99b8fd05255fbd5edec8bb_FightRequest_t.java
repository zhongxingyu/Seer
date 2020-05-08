 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package data;
 
 /**
  *
  * @author sis13
  */
 public class FightRequest {
 
     private String senderID;
     private String recieverID;
     private String fightID;
     private String senderMonsterID;
     private String receiverMonsterID;
     private int senderServerID;
     private int recieverServerID;
    
    private final int MONEY = 20;
 
     public FightRequest(String senderID, String recieverID, String fightID, String senderMonsterID, String receiverMonsterID, int senderServerID, int recieverServerID) {
         this.senderID = senderID;
         this.recieverID = recieverID;
         this.fightID = fightID;
         this.senderMonsterID = senderMonsterID;
         this.receiverMonsterID = receiverMonsterID;
         this.senderServerID = senderServerID;
         this.recieverServerID = recieverServerID;
     }
 
     public String getSenderID() {
         return senderID;
     }
 
     public void setSenderID(String senderID) {
         this.senderID = senderID;
     }
 
     public String getRecieverID() {
         return recieverID;
     }
 
     public void setRecieverID(String recieverID) {
         this.recieverID = recieverID;
     }
 
     public String getFightID() {
         return fightID;
     }
 
     public void setFightID(String fightID) {
         this.fightID = fightID;
     }
 
     public String getSenderMonsterID() {
         return senderMonsterID;
     }
 
     public void setSenderMonsterID(String senderMonsterID) {
         this.senderMonsterID = senderMonsterID;
     }
 
     public String getReceiverMonsterID() {
         return receiverMonsterID;
     }
 
     public void setReceiverMonsterID(String receiverMonsterID) {
         this.receiverMonsterID = receiverMonsterID;
     }
 
     public int getSenderServerID() {
         return senderServerID;
     }
 
     public void setSenderServerID(int senderServerID) {
         this.senderServerID = senderServerID;
     }
 
     public int getRecieverServerID() {
         return recieverServerID;
     }
 
     public void setRecieverServerID(int recieverServerID) {
         this.recieverServerID = recieverServerID;
     }

    public int getMONEY() {
        return MONEY;
    }
 }
