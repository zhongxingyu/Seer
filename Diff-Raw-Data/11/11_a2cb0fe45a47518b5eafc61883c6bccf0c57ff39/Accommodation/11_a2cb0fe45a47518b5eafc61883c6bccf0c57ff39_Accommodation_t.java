 
 package wielwijk;
 
 /**
  *
  * @author Sytze
  */
 public class Accommodation {
     private int id;
     private int activity_id;
     private int user_id;
     private int people;
     private int cap;
     private String address;
     
     Accommodation(long Id, int ActId, int UserId, String Addr, int People, int Cap) {
         id = (int)Id;
         activity_id = (int)ActId;
         user_id = (int)UserId;
         address = Addr;
         people = People;
         cap = Cap;
     }
     
    public int getCap() {
        return cap;
    }
    
     public int getId() {
         return id;
     }
     
     public int getUserId() {
         return user_id;
     }
     
     public int getActivityId() {
         return activity_id;
     }
     
     public String getAddress() {
         return address;
     }
     
     public String toString() {
         return address+" ("+people+"/"+cap+")";
     }
     
     
 }
