 package smartpool.domain;
 
 import org.joda.time.LocalTime;
 
 public class JoinRequest {
     String username, carpoolName, pickupPoint;
     LocalTime preferredPickupTime;
     String address;
 
     @SuppressWarnings("unused - used by MyBatis")
     public JoinRequest() {
     }
 
     public JoinRequest(String username, String carpoolName, String address, String pickupPoint, LocalTime preferredPickupTime) {
         this.username = username;
         this.carpoolName = carpoolName;
        this.address = address;
         this.pickupPoint = pickupPoint;
         this.preferredPickupTime = preferredPickupTime;
     }
 
     public String getUsername() {
         return username;
     }
 
     public String getCarpoolName() {
         return carpoolName;
     }
 
     @Override
     public String toString() {
         return  "carpoolName='" + carpoolName + '\'' +
                 ", pickupPoint='" + pickupPoint + '\'' +
                 ", preferredPickupTime=" + preferredPickupTime +
                 ", address='" + address ;
     }
 }
