 package dk.drb.blacktiger.repository.asterisk;
 
 import dk.drb.blacktiger.repository.ConferenceRepository;
 import dk.drb.blacktiger.model.ConferenceEventListener;
 import dk.drb.blacktiger.model.Participant;
 import dk.drb.blacktiger.model.ParticipantEvent;
 import dk.drb.blacktiger.model.ParticipantJoinEvent;
 import dk.drb.blacktiger.model.ParticipantLeaveEvent;
import dk.drb.blacktiger.util.PhoneNumber_;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 import org.asteriskjava.live.AsteriskServer;
 import org.asteriskjava.live.MeetMeRoom;
 import org.asteriskjava.live.MeetMeUser;
 import org.asteriskjava.manager.ManagerEventListener;
 import org.asteriskjava.manager.event.ManagerEvent;
 import org.asteriskjava.manager.event.MeetMeJoinEvent;
 import org.asteriskjava.manager.event.MeetMeLeaveEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 /**
  *
  */
 public class AsteriskConferenceRepository implements ConferenceRepository {
  
     private static final Logger LOG = LoggerFactory.getLogger(AsteriskConferenceRepository.class);
     private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+?\\d{2,15}$");
     private AsteriskServer asteriskServer;
     
     private List<ConferenceEventListener> eventListeners = new ArrayList<ConferenceEventListener>();
     private ManagerEventListener managerEventListener = new ManagerEventListener() {
         @Override
         public void onManagerEvent(ManagerEvent event) {
             if (event instanceof MeetMeJoinEvent) {
                 String roomNo = ((MeetMeJoinEvent) event).getMeetMe();
                 Integer index = ((MeetMeJoinEvent) event).getUserNum();
                 fireEvent(new ParticipantJoinEvent(roomNo, index.toString()));
             }
 
             if (event instanceof MeetMeLeaveEvent) {
                 String roomNo = ((MeetMeLeaveEvent) event).getMeetMe();
                 Integer index = ((MeetMeLeaveEvent) event).getUserNum();
                 fireEvent(new ParticipantLeaveEvent(roomNo, index.toString()));
             }
         }
     };
     
     private void fireEvent(ParticipantEvent event) {
         for (ConferenceEventListener listener : eventListeners) {
             listener.onParticipantEvent(event);
         }
     }
 
     public void setAsteriskServer(AsteriskServer asteriskServer) {
         if (this.asteriskServer != null) {
             this.asteriskServer.getManagerConnection().removeEventListener(managerEventListener);
         }
         this.asteriskServer = asteriskServer;
         this.asteriskServer.getManagerConnection().addEventListener(managerEventListener);
     }
 
     @Override
     public List<Participant> findByRoomNo(String roomNo) {
         LOG.debug("Listing participants. [room={}]", roomNo);
         MeetMeRoom room = asteriskServer.getMeetMeRoom(roomNo);
         List<Participant> result = new ArrayList<Participant>();
 
         for (MeetMeUser mmu : room.getUsers()) {
             result.add(participantFromMeetMeUser(mmu));
         }
         return result;
     }
 
     @Override
     public Participant findByRoomNoAndParticipantId(String roomNo, String participantId) {
         LOG.debug("Retrieving participant. [room={};participant={}]", roomNo, participantId);
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         return mmu == null ? null : participantFromMeetMeUser(mmu);
     }
 
     @Override
     public void kickParticipant(String roomNo, String participantId) {
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         if (mmu != null) {
             mmu.kick();
         }
     }
 
     @Override
     public void muteParticipant(String roomNo, String participantId) {
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         if (mmu != null) {
             mmu.mute();
         }
     }
 
     @Override
     public void unmuteParticipant(String roomNo, String participantId) {
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         if (mmu != null) {
             mmu.unmute();
         }
     }
 
     @Override
     public void addEventListener(ConferenceEventListener listener) {
         if (listener != null) {
             eventListeners.add(listener);
         }
     }
 
     @Override
     public void removeEventListener(ConferenceEventListener listener) {
                 if (listener != null) {
             eventListeners.remove(listener);
         }
     }
     
     private MeetMeUser getMeetMeUser(String roomNo, String participantId) {
         MeetMeRoom room = asteriskServer.getMeetMeRoom(roomNo);
         Integer id = Integer.parseInt(participantId);
         for (MeetMeUser mmu : room.getUsers()) {
             if (mmu.getUserNumber().equals(id)) {
                 return mmu;
             }
         }
         return null;
     }
         
     private Participant participantFromMeetMeUser(MeetMeUser user) {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String number = user.getChannel().getCallerId().getNumber();
         boolean host = false; 
         
         //'number' is of syntax 'IP-<username>'. We have to take it into consideration when matching the number.
         if(auth!=null && number.equalsIgnoreCase(auth.getName())) {
             host = true;
         }
         
         String phoneNumber = user.getChannel().getCallerId().getNumber();
         String name = user.getChannel().getCallerId().getName();
         
         if(isNumber(name)) {
             phoneNumber = name;
            phoneNumber = PhoneNumber_.normalize(phoneNumber);
         }
         
         return new Participant(user.getUserNumber().toString(), null, phoneNumber, user.isMuted(), host, user.getDateJoined());
     }
 
     /**
      * Checks whether the string is a valid phone number.
      * @param text
      * @return 
      */
     private boolean isNumber(String text) {
         return PHONE_NUMBER_PATTERN.matcher(text).matches();
     }
     
 }
