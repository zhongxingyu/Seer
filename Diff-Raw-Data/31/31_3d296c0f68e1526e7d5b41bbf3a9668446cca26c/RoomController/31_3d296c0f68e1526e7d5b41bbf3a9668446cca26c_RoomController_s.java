 package dk.drb.blacktiger.controller;
 
 import dk.drb.blacktiger.model.ConferenceEventListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.AsyncContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import dk.drb.blacktiger.model.Participant;
 import dk.drb.blacktiger.model.ParticipantEvent;
 import dk.drb.blacktiger.service.ConferenceService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Controller for conference rooms.
  */
 @Controller
 public class RoomController {
 
     private static final Logger LOG = LoggerFactory.getLogger(RoomController.class);
     private static final long ASYNC_TIMEOUT = 30000;
     private final ConferenceService service;
     private final List<ChangeListenerEntry> changeListeners = Collections.synchronizedList(new ArrayList<ChangeListenerEntry>());
     private final ChangeReporter changeReporter = new ChangeReporter();
 
     /**
      * Class for reporting changes to awaiting Http Requests.
      */
     private class ChangeReporter implements ConferenceEventListener {
 
         @Override
         public void onParticipantEvent(ParticipantEvent event) {
             String roomNo = event.getRoomNo();
             LOG.debug("Recieved event for room '{}'.", roomNo);
 
             // We built a clone of changelisteners. We then remove those we haven't handled yet.
             // In the end we remove those left in the closed list as those are the ones we have handled.
             List<ChangeListenerEntry> clonedList = new ArrayList<ChangeListenerEntry>(changeListeners);
             Iterator<ChangeListenerEntry> it = clonedList.iterator();
             
             while (it.hasNext()) {
                 ChangeListenerEntry entry = it.next();
                 AsyncContext ctx = entry.getAsyncContext();
                 if (entry.getRoomNo().equals(roomNo)) {
                     try {
                         LOG.debug("Responded with a change. [room={}, remoteIp={}]", roomNo, ctx.getRequest().getRemoteAddr());
                         respondChanged((HttpServletResponse) ctx.getResponse(), true);
                         ctx.complete();
                     } catch (IllegalStateException ex) {
                         LOG.debug("Unable to respond in async context.", ex);
                     }
                 } else {
                     it.remove();
                 }
             }
 
             LOG.debug("Removing {} listeners.", clonedList.size());
             changeListeners.removeAll(clonedList);
 
         }
     }
 
     /**
      * Entry for awaiting HttpRequest.
      */
     private class ChangeListenerEntry {
 
         private AsyncContext asyncContext;
         private String roomNo;
         private final long timestamp;
 
         public ChangeListenerEntry(AsyncContext asyncContext, String roomNo) {
             this.asyncContext = asyncContext;
             this.roomNo = roomNo;
             this.timestamp = System.currentTimeMillis();
         }
 
         public AsyncContext getAsyncContext() {
             return asyncContext;
         }
 
         public String getRoomNo() {
             return roomNo;
         }
 
         public long getTimestamp() {
             return timestamp;
         }
     }
 
     /**
      * Constructor for new instance of RoomController.
      *
      * @param service The service which is to be used by the RoomController.
      */
     @Autowired
     public RoomController(ConferenceService service) {
         this.service = service;
     }
 
     /**
      * Init method which needs to be called beforing calling other methods in this controller.
      */
     @PostConstruct
     public void init() {
         service.addEventListener(changeReporter);
     }
 
     /**
      * Accepts requests for changes. This method add the Http Request to a queue and frees the thread handling the request. Another thread will handle
      * the request later on.
      */
     @RequestMapping(value = "/rooms/{roomNo}/changes")
     public void listenForChange(HttpServletRequest request, HttpServletResponse response, @PathVariable final String roomNo) {
         LOG.debug("Adding new listener [remoteIp={};roomNo={}]", request.getRemoteAddr(), roomNo);
         AsyncContext asyncContext = request.startAsync();
         asyncContext.setTimeout(60000);
         changeListeners.add(new ChangeListenerEntry(asyncContext, roomNo));
     }
 
     @RequestMapping(value = "/rooms/{roomNo}", headers = "Accept=application/json")
     @ResponseBody
    public List<Participant> showRoomAsJson(@PathVariable final String roomNo) {
         LOG.debug("Got JSON request for room '{}'.", roomNo);
         return service.listParticipants(roomNo);
     }
 
     @RequestMapping(value = "/rooms/{roomNo}/{participantId}", headers = "Accept=application/json")
     @ResponseBody
    public Participant showParticipanteAsJson(@PathVariable final String roomNo, @PathVariable final String participantId) {
         LOG.debug("Got JSON request for participant in room [room={};participant={}].", roomNo, participantId);
         return service.getParticipant(roomNo, participantId);
     }
 
     @RequestMapping(value = "/rooms/{roomNo}/{participantId}/kick", method = RequestMethod.POST, headers = "Accept=application/json")
     @ResponseBody
     public int kickParticipantAsJson(@PathVariable final String roomNo, @PathVariable final String participantId) {
         LOG.debug("Kicking participant from room [room={};participant={}].", roomNo, participantId);
         service.kickParticipant(roomNo, participantId);
         return 1;
     }
 
     @RequestMapping(value = "/rooms/{roomNo}/{participantId}/mute", method = RequestMethod.POST, headers = "Accept=application/json")
     @ResponseBody
     public int muteParticipantAsJson(@PathVariable final String roomNo, @PathVariable final String participantId) {
         LOG.debug("Muting participant in room [room={};participant={}].", roomNo, participantId);
         service.muteParticipant(roomNo, participantId);
         return 1;
     }
 
     @RequestMapping(value = "/rooms/{roomNo}/{participantId}/unmute", method = RequestMethod.POST, headers = "Accept=application/json")
     @ResponseBody
     public int unmuteParticipantAsJson(@PathVariable final String roomNo, @PathVariable final String participantId) {
         LOG.debug("Unmuting participant in room [room={};participant={}].", roomNo, participantId);
         service.unmuteParticipant(roomNo, participantId);
         return 1;
     }
 
     @RequestMapping("/rooms/{roomNo}")
     public String serveAppFile(@PathVariable final String roomNo, @RequestParam(required = false) String mode) {
         return "participants";
     }
 
     @RequestMapping("/rooms/assets/js/blacktiger-service-*")
     public String serveServiceFile(HttpServletResponse response) {
         return "blacktiger-service";
     }
 
     @Scheduled(fixedDelay = 5000)
     public void updateRequests() {
         List<ChangeListenerEntry> clonedList = new ArrayList<ChangeListenerEntry>(changeListeners);
         Iterator<ChangeListenerEntry> it = clonedList.iterator();
         long now = System.currentTimeMillis();
 
         while (it.hasNext()) {
             ChangeListenerEntry entry = it.next();
             AsyncContext ctx = entry.getAsyncContext();
 
             if (now - entry.getTimestamp() > ASYNC_TIMEOUT) {
                 LOG.debug("Responded with not change. The request timed out. [remoteIp={}]", ctx.getRequest().getRemoteAddr());
                 respondChanged((HttpServletResponse) ctx.getResponse(), false);
             } else {
                 it.remove();
             }
         }
 
         changeListeners.removeAll(clonedList);
 
     }
 
     private void respondChanged(HttpServletResponse response, boolean value) {
         try {
             response.setContentType("application/json");
             response.getOutputStream().print(value);
             response.getOutputStream().close();
             response.flushBuffer();
         } catch (IOException ex) {
             LOG.debug("Unable to respond.", ex);
         }
     }
 }
