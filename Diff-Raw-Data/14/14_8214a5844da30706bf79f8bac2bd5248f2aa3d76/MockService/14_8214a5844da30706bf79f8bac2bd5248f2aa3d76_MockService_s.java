 package dk.drb.blacktiger.controller;
 
 import dk.drb.blacktiger.model.Participant;
 import dk.drb.blacktiger.model.User;
 import dk.drb.blacktiger.service.BlackTigerEventListener;
 import dk.drb.blacktiger.service.IBlackTigerService;
 import dk.drb.blacktiger.service.ParticipantEvent;
 import dk.drb.blacktiger.service.ParticipantJoinEvent;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author michaelkrog
  */
 public class MockService implements IBlackTigerService {
 
     private List<BlackTigerEventListener> eventListeners = new ArrayList<BlackTigerEventListener>();
     private List<Participant> participants = new ArrayList<Participant>();
             
     
     @Override
     public User getUser(String username) {
         return new User(username, "test");
     }
 
     @Override
     public List<Participant> listParticipants(String roomNo) {
         if("09991".equals(roomNo)) {
             return participants;
         } else {
             return null;
         }
     }
 
     @Override
     public Participant getParticipant(String roomNo, String participantId) {
         Iterator<Participant> it = participants.iterator();
         while(it.hasNext()) {
             Participant p = it.next();
             if(p.getUserId() == participantId) {
                 return p;
             }
         }
         return null;
     }
 
     @Override
     public void kickParticipant(String roomNo, String participantId) {
         Iterator<Participant> it = participants.iterator();
         while(it.hasNext()) {
             Participant p = it.next();
             if(p.getUserId() == participantId) {
                 it.remove();
                 break;
             }
         }
     }
 
     @Override
     public void muteParticipant(String roomNo, String participantId) {
         Iterator<Participant> it = participants.iterator();
         while(it.hasNext()) {
             Participant p = it.next();
             if(p.getUserId() == participantId) {
                Participant newP = new Participant(p.getUserId(), p.getPhoneNumber(), true);
                 participants.set(participants.indexOf(p), newP);
                 break;
             }
         }
     }
 
     @Override
     public void unmuteParticipant(String roomNo, String participantId) {
         Iterator<Participant> it = participants.iterator();
         while(it.hasNext()) {
             Participant p = it.next();
             if(p.getUserId() == participantId) {
                Participant newP = new Participant(p.getUserId(), p.getPhoneNumber(), false);
                 participants.set(participants.indexOf(p), newP);
                 break;
             }
         }
     }
 
     @Override
     public void addEventListener(BlackTigerEventListener listener) {
         eventListeners.add(listener);
     }
 
     @Override
     public void removeEventListener(BlackTigerEventListener listener) {
         eventListeners.remove(listener);
     }
     
     public void addParticipant(Participant p) {
         participants.add(p);
     }
 
     public void fireJoinEvent(String room, String participantId) {
         ParticipantEvent event = new ParticipantJoinEvent(room, participantId);
         for (BlackTigerEventListener el : eventListeners) {
             el.onParticipantEvent(event);
         }
     }
 }
