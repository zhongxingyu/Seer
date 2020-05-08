 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package clientmanager;
 
 /**
  *
  * @author Scare
  */
 public class InternetService extends Service{
 
     public enum internetOptions {M20,M100};
     
     private internetOptions internetSpeed;
     private String idAccount;
     private static float monthlyCostM20;
     private static float monthlyCostM100;
     
     /**
      * @Instantiate objects PhoneService
      */
     private InternetService(String id, float cost) {
         super(cost);
         this.idAccount = id;
     }
     
     /**
      * @Instantiate objects PhoneService
      */
     public InternetService(String id, internetOptions speed){
         super(0);
         if(speed.equals(internetOptions.M20)){
             this.setMonthlyCost(monthlyCostM20);
             this.idAccount = id;
            internetSpeed=speed;
         }
         else if(speed.equals(internetOptions.M100)){
             this.setMonthlyCost(monthlyCostM100);
             this.idAccount = id;
            internetSpeed=speed;
         }      
     }
     
     
     /**
      * @return the monthlyCostM20
      */
     public static float getMonthlyCostM20() {
         return monthlyCostM20;
     }
 
     /**
      * @param aMonthlyCostM20 the monthlyCostM20 to set
      */
     public static void setMonthlyCostM20(float aMonthlyCostM20) {
         monthlyCostM20 = aMonthlyCostM20;
     }
 
     /**
      * @return the monthlyCostM100
      */
     public static float getMonthlyCostM100() {
         return monthlyCostM100;
     }
 
     /**
      * @param aMonthlyCostM100 the monthlyCostM100 to set
      */
     public static void setMonthlyCostM100(float aMonthlyCostM100) {
         monthlyCostM100 = aMonthlyCostM100;
     }
     
     /**
      * @return the tipo
      */
     public internetOptions getinternetOptions() {
         return getInternetSpeed();
     }
 
     /**
      * @return the internetSpeed
      */
     public internetOptions getInternetSpeed() {
         return internetSpeed;
     }
 
     /**
      * @param internetSpeed the internetSpeed to set
      */
     public void setInternetSpeed(internetOptions internetSpeed) {
         this.internetSpeed = internetSpeed;
     }
 
     /**
      * @return the idAccount
      */
     public String getIdAccount() {
         return idAccount;
     }
 
     /**
      * @param idAccount the idAccount to set
      */
     public void setIdAccount(String idAccount) {
         this.idAccount = idAccount;
     }
 
     /**
      *
      * @return total cost of this internet service
      */
     @Override
     public float calculateServicePayment() {
         float totalCost;
         //In future internet service can have more taxes or something like that
         totalCost = this.getMonthlyCost();
         return totalCost;
     }
     
 }
