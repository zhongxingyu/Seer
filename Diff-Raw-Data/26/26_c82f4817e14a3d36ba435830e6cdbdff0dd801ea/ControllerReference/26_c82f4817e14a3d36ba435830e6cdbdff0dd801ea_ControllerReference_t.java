 package at.tuwien.sbc.feeder;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.jini.core.lease.Lease;
 
 import org.apache.log4j.Logger;
 import org.openspaces.core.GigaSpace;
 
 import at.tuwien.sbc.model.DoodleEvent;
 import at.tuwien.sbc.model.DoodleSchedule;
 import at.tuwien.sbc.model.DoodleSpaceObject;
 import at.tuwien.sbc.model.Notification;
 import at.tuwien.sbc.model.Peer;
 
 import com.j_spaces.core.LeaseContext;
 import com.j_spaces.core.client.UpdateModifiers;
 
 public class ControllerReference {
     
     private static Logger logger = Logger.getLogger(ControllerReference.class);
     
     private static ControllerReference uniqueInstance;
     
     private GigaSpace gigaSpace;
     
     private Peer user;
     
     public static synchronized ControllerReference getInstance() {
         if (uniqueInstance == null) {
             uniqueInstance = new ControllerReference();
         }
         
         return uniqueInstance;
     }
     
     private ControllerReference() {
         
     }
     
     public void setGigaSpace(GigaSpace gigaSpace) {
         this.gigaSpace = gigaSpace;
     }
     
     public GigaSpace getGigaSpace() {
         return gigaSpace;
     }
     
     @Override
     protected Object clone() throws CloneNotSupportedException {
         throw new CloneNotSupportedException("Singletons cannot be cloned");
     }
     
     public void setUser(Peer user) {
         this.user = user;
     }
     
     public Peer getUser() {
         if (user != null) {
             Peer p = new Peer(user.getName(), null, null);
             p.setEvents(null);
             p.setOrganized(null);
             Peer result = gigaSpace.read(p, 5000);
             this.setUser(result);
             return result;
         }
         return null;
     }
     
     public Peer register(String user, String pass) {
         Peer newPeer = new Peer(user, pass, "register");
         LeaseContext<Peer> ctx = this.getGigaSpace().write(newPeer, Lease.FOREVER, 1000, UpdateModifiers.WRITE_ONLY);
         return ctx.getObject();
         
     }
     
     public Peer login(String user, String pass) {
         Peer log = new Peer(user, pass, null);
         Peer peer = this.getGigaSpace().readIfExists(log);
         if (peer != null) {
             peer.setAction("login");
             this.getGigaSpace().write(peer);
         } else {
             logger.info("Peer is null");
         }
         
         this.setUser(peer);
         return peer;
         
     }
     
     public Peer takePeer(Peer peer) {
         Peer template = new Peer(peer.getName(), null, null);
         Peer result = this.gigaSpace.take(template);
         if (result == null) {
             logger.error("FRIGGIN NULL");
         }
         
         return result;
     }
     
     public List<Peer> searchByName(String regex) {
         Peer template = new Peer();
         Peer[] peers = this.getGigaSpace().readMultiple(template, Integer.MAX_VALUE);
         
         List<Peer> result = new ArrayList<Peer>();
         
         for (Peer p : peers) {
             if (p.getName().contains(regex)) {
                 result.add(p);
             }
         }
         
         return result;
         
     }
     
     public void logout() {
         Peer u = getUser();
         if (u != null) {
             u.setAction("logout");
             this.getGigaSpace().write(u);
             this.setUser(null);
         } else {
             logger.warn("USer is null");
             this.setUser(null);
         }
     }
     
     public Peer[] getAllPeers() {
         Peer template = new Peer(null, null, null);
         template.setEvents(null);
         template.setOrganized(null);
         
         return this.gigaSpace.readMultiple(template, Integer.MAX_VALUE);
     }
     
     public DoodleEvent findEventByNameAndOwner(String name) {
         logger.info("findEventByNameAndUser(" + name + ")");
         logger.info("user = " + user);
         if (name == null || this.user == null) {
             return null;
         }
         // WARNING one user can have two events with same name.. Problem...
         DoodleEvent template = new DoodleEvent();
         template.setName(name);
         template.setOwner(this.user.getName());
         
         return ControllerReference.getInstance().getGigaSpace().readIfExists(template);
     }
     
     public DoodleEvent findEventByName(String name) {
         if (name == null || this.user == null) {
             return null;
         }
         // WARNING one user can have two events with same name.. Problem...
         DoodleEvent template = new DoodleEvent();
         template.setName(name);
         
         return ControllerReference.getInstance().getGigaSpace().readIfExists(template);
     }
     
     public List<DoodleEvent> getInvitations() {
         List<DoodleEvent> eventsInvitedTo = new ArrayList<DoodleEvent>();
         if (this.getUser() != null) {
             DoodleEvent eventTemplate = new DoodleEvent();
             eventTemplate.setInvitations(null);
             eventTemplate.setParticipants(null);
             eventTemplate.setSchedules(null);
             eventTemplate.setFixSchedule(null);
             eventTemplate.setComments(null);
             
             DoodleEvent[] events = this.getGigaSpace().readMultiple(eventTemplate, 100);
             for (int i = 0; i < events.length; i++) {
                 for (String p : events[i].retrieveInvitations()) {
                     if (p.equals(user.getName())) {
                         eventsInvitedTo.add(events[i]);
                     }
                 }
             }
         }
         
         return eventsInvitedTo;
     }
     
     public void clearAll() {
         this.getGigaSpace().clean();
         
     }
     
     public List<String> getParticipantsForEvent(String selectedEventItem) {
         DoodleEvent template = new DoodleEvent();
         template.setId(selectedEventItem);
         
         DoodleEvent foundEvent = gigaSpace.readIfExists(template);
         if (foundEvent == null) {
             return new ArrayList<String>();
         }
         return foundEvent.retrieveParticipants();
     }
     
     public Object refresh(DoodleSpaceObject o) {
         if (o == null) {
             return null;
         }
         if (o instanceof DoodleEvent) {
             DoodleEvent temp = new DoodleEvent();
             temp.setId(o.getId());
             return gigaSpace.readIfExists(temp);
         }
         if (o instanceof Peer) {
             Peer temp = new Peer(o.getId(), null, null);
             return gigaSpace.readIfExists(temp);
         }
         if (o instanceof DoodleSchedule) {
         	DoodleSchedule gotObject = (DoodleSchedule)o;
         	DoodleSchedule temp = new DoodleSchedule(gotObject.getParticipant(), gotObject.getEvent());
         	return gigaSpace.readIfExists(temp);
         }
         return null;
     }
     
     public List<String> getEventNamesFromIds(List<String> retrieveOrganized) {
         List<String> result = new ArrayList<String>();
         
         for (String id : retrieveOrganized) {
             DoodleEvent template = new DoodleEvent();
             template.setId(id);
             DoodleEvent event = this.gigaSpace.readIfExists(template);
             if (event != null) {
                 result.add(event.getName());
             } else {
                 logger.warn("An event was not found.. Please inspect!");
             }
         }
         return result;
         
     }
     
     public void deleteOldSchedules(String userId, String eventId) {
         DoodleSchedule template = new DoodleSchedule(userId, eventId);
         this.getGigaSpace().takeMultiple(template, Integer.MAX_VALUE);
         
     }
     
     public List<DoodleSchedule> readSchedulesForCurrentUser(String id) {
         List<DoodleSchedule> schedules = new ArrayList<DoodleSchedule>();
         DoodleSchedule template = new DoodleSchedule(this.user.getId(), id);
         DoodleSchedule[] read = this.getGigaSpace().readMultiple(template, Integer.MAX_VALUE);
         schedules.addAll(Arrays.asList(read));
         return schedules;
     }
     
     public List<DoodleSchedule> readSchedulesForEvent(String id) {
         List<DoodleSchedule> schedules = new ArrayList<DoodleSchedule>();
         DoodleSchedule template = new DoodleSchedule(null, id);
         DoodleSchedule[] read = this.getGigaSpace().readMultiple(template, Integer.MAX_VALUE);
         schedules.addAll(Arrays.asList(read));
         return schedules;
     }
     
     public Notification[] takeNotifications() {
         Peer p = this.getUser();
         
         if (p != null) {
             Notification template = new Notification(p.getId());
             return this.getGigaSpace().takeMultiple(template, Integer.MAX_VALUE);
         }
         
         return new Notification[] {};
         
     }
     
     public Notification[] readNotifications() {
         Peer p = this.getUser();
         
         if (p != null) {
             Notification template = new Notification(p.getId());
             return this.getGigaSpace().readMultiple(template, Integer.MAX_VALUE);
         }
         
         return new Notification[] {};
     }
 
 }
