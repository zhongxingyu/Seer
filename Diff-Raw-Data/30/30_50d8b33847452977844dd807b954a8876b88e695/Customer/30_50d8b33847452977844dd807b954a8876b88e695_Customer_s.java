 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package carrental;
 
 /**
  * Customer holds holds information on each customer
  * @author CNN
 * @version 30. Nov. 2011
  */
 public class Customer{
     private final int ID;
     private int telephone;
     private String name;
     private String adress;
     private String eMail;
     
     /**
      * Constructor for Customer
      * @param ID
      * @param telephone
      * @param name
      * @param adress
      * @param eMail 
      */
     public Customer(int ID, int telephone, String name, String adress, String eMail){
         this.ID = ID;
         this.telephone = telephone;
         this.name = name;
         this.adress = adress;
         this.eMail = eMail;
     }
     
     /**
      * Upgrade object information
      * @param telephone
      * @param name
      * @param adress
      * @param eMail 
      */
     public void updateObject(int telephone, String name, String adress, String eMail){
         this.telephone = telephone;
         this.name = name;
         this.adress = adress;
         this.eMail = eMail;
     }
     /**
      * 
      * @return Customer ID
      */
     public int getID(){
         return ID;
     }
     /**
      * 
      * @return Telephone number
      */
     public int getTelephone(){
         return telephone;
     }
     /**
      * 
      * @return Name
      */
     public String getName(){
         return name;
     }
     /**
      * 
      * @return Adress
      */
     public String getAdress(){
         return adress;
     }
     /**
      * 
      * @return E-Mail
      */
     public String getEMail(){
         return eMail;
     }
 }
