 package smartpool.web.form;
 
 import org.joda.time.LocalTime;
 import smartpool.domain.Buddy;
 import smartpool.domain.JoinRequest;
 
 public class JoinRequestForm {
     String username;
     String carpoolName;
     String pickupPoint;
     String preferredPickupTime;
     String address;
     String contactNumber;
 
     @SuppressWarnings("unused - used by spring data binding")
     public JoinRequestForm() {
     }
 
     public JoinRequestForm(String username, String carpoolName, String pickupPoint, String preferredPickupTime,
                            String address, String contactNumber) {
         this.username = username;
         this.carpoolName = carpoolName;
         this.pickupPoint = pickupPoint;
         this.preferredPickupTime = preferredPickupTime;
         this.address = address;
         this.contactNumber = contactNumber;
     }
 
     public JoinRequestForm(Buddy buddy, String carpoolName) {
         this(buddy.getUserName(), carpoolName, buddy.getPickupPoint(),
                 buddy.getPickupTime() == null ? "" : buddy.getPickupTime().toString("h:mm"),
                 buddy.getAddress(), buddy.getContactNumber());
     }
 
     public JoinRequest createDomainObject() {
        return new JoinRequest(username, carpoolName, pickupPoint, LocalTime.parse(preferredPickupTime), null);
     }
 
     @SuppressWarnings("unused - used by spring data binding")
     public void setUsername(String username) {
         this.username = username;
     }
 
     @SuppressWarnings("unused - used by spring data binding")
     public void setCarpoolName(String carpoolName) {
         this.carpoolName = carpoolName;
     }
 
     @SuppressWarnings("unused - used by spring data binding")
     public void setPickupPoint(String pickupPoint) {
         this.pickupPoint = pickupPoint;
     }
 
     @SuppressWarnings("unused - used by spring data binding")
     public void setPreferredPickupTime(String preferredPickupTime) {
         this.preferredPickupTime = preferredPickupTime;
     }
 
     @SuppressWarnings("unused - used by spring data binding")
     public void setAddress(String address) {
         this.address = address;
     }
 
     @SuppressWarnings("unused - used by spring data binding")
     public void setContactNumber(String contactNumber) {
         this.contactNumber = contactNumber;
     }
 
     @SuppressWarnings("unused - used to render pre populated form in jsp")
     public String getUsername() {
         return username;
     }
 
     @SuppressWarnings("unused - used to render pre populated form in jsp")
     public String getCarpoolName() {
         return carpoolName;
     }
 
     @SuppressWarnings("unused - used to render pre populated form in jsp")
     public String getPickupPoint() {
         return pickupPoint;
     }
 
     @SuppressWarnings("unused - used to render pre populated form in jsp")
     public String getPreferredPickupTime() {
         return preferredPickupTime;
     }
 
     @SuppressWarnings("unused - used to render pre populated form in jsp")
     public String getAddress() {
         return address;
     }
 
     @SuppressWarnings("unused - used to render pre populated form in jsp")
     public String getContactNumber() {
         return contactNumber;
     }
 }
