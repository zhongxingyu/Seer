 package nl.surfnet.bod.web.push;
 
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.ReservationStatus;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class Events {
 
   private Events() {
   }
 
   public static Event createSimpleEvent(String groupId, String message) {
     return new SimpleEvent(groupId, message);
   }
 
   public static Event createReservationStatusChangedEvent(Reservation reservation, ReservationStatus oldStatus) {
     String message = String.format("The status of a reservation was changed from %s to %s", oldStatus, reservation.getStatus());
 
     return new JsonMessageEvent(reservation.getVirtualResourceGroup().getSurfConextGroupName(), new JsonEvent(message,
         reservation.getId(), reservation.getStatus().name()));
   }
 
   private static class JsonEvent {
     private final String message;
     private final Long id;
     private final String status;
 
     public JsonEvent(String message, Long id, String status) {
       this.message = message;
       this.id = id;
       this.status = status;
     }
 
     public String getMessage() {
       return message;
     }
 
     public Long getId() {
       return id;
     }
 
     public String getStatus() {
       return status;
     }
   }
 
   private static final class JsonMessageEvent implements Event {
 
    private static final ObjectMapper mapper = new ObjectMapper();
 
     private final String groupId;
     private final Object toJsonObject;
 
     public JsonMessageEvent(String groupId, Object toJsonObject) {
       this.groupId = groupId;
       this.toJsonObject = toJsonObject;
     }
 
     @Override
     public String getGroupId() {
       return groupId;
     }
 
     @Override
     public String getMessage() {
       try {
        return mapper.writeValueAsString(toJsonObject);
       }
       catch (Exception e) {
         return "{}";
       }
     }
 
   }
 
   public static final class SimpleEvent implements Event {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private String groupId;
     private String message;
 
     public SimpleEvent(String groupId, String message) {
       this.groupId = groupId;
       this.message = message;
 
       log.debug("Created event for groupId [{}] with message [{}]", groupId, message);
     }
 
     @Override
     public String getGroupId() {
       return groupId;
     }
 
     @Override
     public String getMessage() {
       return message;
     }
 
   }
 }
