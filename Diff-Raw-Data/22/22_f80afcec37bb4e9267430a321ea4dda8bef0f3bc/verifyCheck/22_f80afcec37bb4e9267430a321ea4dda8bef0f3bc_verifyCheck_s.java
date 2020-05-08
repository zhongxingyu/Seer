 /*
  * HW5.1 - Adapter Design Pattern
  */
 package newgenpos;
 
 public class verifyCheck {
      
     public boolean verifyText(String input){
         if(input.isEmpty()){
             Ui_NewGenPOS.setText("All fields must be non-empty!");
             return false;
         }
         else{
             return true;
         }
     }
     public boolean verifyCheckNumber(String input){
         try{
             //Checking for only numbers
            long numberCheck = Long.parseLong(input);
         }
         catch(NumberFormatException e){
             Ui_NewGenPOS.setText("Check number must contain ONLY numbers and must NOT be blank! Try Again!");
             return false;
         }
         return true;        
     }    
     public boolean verifyLicense(String input){
         if(input.length() != 8){
             Ui_NewGenPOS.setText("License must be 8 characters long! Try Again!");
             return false;
         }
         return true;
     }
     public boolean verifyPhone(String input){
         if(input.length() != 10){
             Ui_NewGenPOS.setText("Phone number must be 10 numbers long! Try Again!");
             return false;
         }
         try{
             //Checking for only numbers
             long numberCheck = Long.parseLong(input);
         }
         catch(NumberFormatException e){
             Ui_NewGenPOS.setText("Phone number must contain ONLY numbers! Try Again!");
             return false;
         }
         return true;
     }    
 }
