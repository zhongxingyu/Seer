 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package clientmanager;
 
 /**
  *
  * @author Scare
  */
 public class PhoneService extends Service{
 
     private String idOperator;
     private int phoneNumber;
    private static float monthlyCost;
     
     /**
      * @Instantiate objects PhoneService
      */
     public PhoneService(String id, int number){
        super(monthlyCost);
         this.idOperator = id;
         this.phoneNumber = number;        
     }
 
     /**
      * @return the idOperator
      */
     public String getIdOperator() {
         return idOperator;
     }
 
     /**
      * @param idOperator the idOperator to set
      */
     public void setIdOperator(String idOperator) {
         this.idOperator = idOperator;
     }
 
     /**
      * @return the phoneNumber
      */
     public int getPhoneNumber() {
         return phoneNumber;
     }
 
     /**
      * @param phoneNumber the phoneNumber to set
      */
     public void setPhoneNumber(int phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
     
     /**
      * @return the monthlyCost
      */
    public static float getMonthlyCostPhoneService() {
        return monthlyCost;
     }
 
     /**
      * @param aMonthlyCost the monthlyCost to set
      */
     public static void setMonthlyCostPhoneService(float aMonthlyCost) {
        monthlyCost = aMonthlyCost;
     }
 
     /**
      * @return total cost of this phone service
      */
     @Override
     public float calculateServicePayment() {
         float totalCost;
         //In future phone service can have more taxes or something like that
        totalCost = this.getMonthlyCost();
         return totalCost;
     }
     
     
 }
