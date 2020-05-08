 package cal.card.upload;
 
 import java.util.ArrayList;
 
 public class OrgSyncAPI {
     
     public static int usersNotFound = 0;
     public static int successfullyAdded = 0;
     public static int duplicateCards = 0;
         
     public static void upload(ArrayList list) {
         if(list != null) {
             for(int i = 0; i < list.size(); i++) {    
                 Object[] test = (Object[]) list.get(i);
                 String email_address = test[3].toString().replace("@calu.edu", "@california-university-of-pennsylvania");
                String calcard_number = test[0].toString().substring(6,22);
                 String osid = URLConnection.getOSID(email_address);
                 if(osid != null) {
                     int updateStatus = URLConnection.updateCardNumber(osid, calcard_number);
 
                     switch(updateStatus) {
                         case 200:
                             successfullyAdded++;
                             break;
                         case 422:                            
                             duplicateCards++;
                             break;
                     }   
                     
                 } else {
                     usersNotFound++;            
                 }
             }
             
             System.out.println("Users Attempted to Update: " + list.size());
             System.out.println("Users Added Succesfully: " + successfullyAdded);
             System.out.println("Users Not Found: " + usersNotFound);
             System.out.println("Duplicate Card Numbers Found: " + duplicateCards);
         
         } else {
            System.out.println("No Student Data Found!");
            System.exit(0); 
         }
 
     }
         
 }
