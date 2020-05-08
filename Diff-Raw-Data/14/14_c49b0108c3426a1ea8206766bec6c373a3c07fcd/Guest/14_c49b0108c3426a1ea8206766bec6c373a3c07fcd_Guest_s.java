 package model.domain.guests;
 
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Id;
 import com.google.code.morphia.annotations.Indexed;
 import com.google.code.morphia.annotations.Reference;
 import model.dao.GuestDAO;
 import model.domain.Wedding;
 import org.bson.types.ObjectId;
 import play.data.validation.Constraints;
 import play.data.validation.Constraints.Required;
 
 @Entity("guests")
 public class Guest {
 

     @Id
     private ObjectId id;
 
     @Indexed @Reference
     private Wedding wedding;
 
     private String email;
     private String roomLocation;
     @Required
     private String name;
     @Required
     private InvitationStatus statusCivil;
     @Required
     private InvitationStatus statusFiesta;
     @Required
     private InvitationStatus statusIglesia;
 
     public Guest() {
         this.statusCivil   = InvitationStatus.NOINVITADO;
         this.statusFiesta  = InvitationStatus.NOINVITADO;
        this.statusIglesia = InvitationStatus.INVITADO;
         this.roomLocation  = null;
     }
 
     public void setWedding(Wedding wedding) {
         this.wedding = wedding;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getRoomLocation() {
         return roomLocation;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public InvitationStatus getStatusCivil() {
         return statusCivil;
     }
 
     public InvitationStatus getStatusFiesta() {
         return statusFiesta;
     }
 
     public InvitationStatus getStatusIglesia() {
         return statusIglesia;
     }
 
     public void setStatusIglesia(InvitationStatus statusIglesia) {
         this.statusIglesia = statusIglesia;
     }
 
     public void setStatusCivil(InvitationStatus statusCivil) {
         this.statusCivil = statusCivil;
     }
 
     public void setStatusFiesta(InvitationStatus statusFiesta) {
         this.statusFiesta = statusFiesta;
     }
 
     public static void createInDB(Guest guest){
         GuestDAO.getGuestDAO().save(guest);
         System.out.println("Guardando un nuevo usuario");
     }
 
 
 }
 
