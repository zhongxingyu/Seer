 package softwareart.booking;
 
 
 import softwareart.booking.exceptions.CollidingWorkshopsException;
 import softwareart.booking.exceptions.WorkshopNotFoundException;
 import softwareart.booking.persistence.PersistenceService;
 
 import java.util.*;
 
 public class BookingService {
 
     private Map<Integer, Workshop> workshops = new HashMap<>();
     private PersistenceService persistenceService;
 
     public BookingService(PersistenceService persistenceService) {
         this.persistenceService = persistenceService;
     }
 
     public void book(Participant participant, Integer... workshopsIds) {
         Workshop[] workshops = getWorkshops(workshopsIds);
 
         verifyCollisions(workshops);
         makeBooking(participant, workshops);
     }
 
     private void verifyCollisions(Workshop[] workshops) {
         for (Workshop workshop : workshops) {
             for (Workshop testWorkshop : workshops) {
                 if (testWorkshop != workshop && workshop.collidesWith(testWorkshop)) {
                     throw CollidingWorkshopsException.forWorkshops(workshops);
                 }
             }
         }
     }
 
     private void makeBooking(Participant participant, Workshop[] workshops) {
         for (Workshop workshop : workshops) {
             workshop.addParticipant(participant);
         }
         persistenceService.saveBooking(participant, workshops);
     }
 
    public void bookLocal(Participant participant, Integer[] workshops) {
        for (Integer workshopId : workshops) {
            this.workshops.get(workshopId).addParticipant(participant);
        }
    }


     private void removeBookingFor(Participant participant) {
         for (Workshop workshop : this.workshops.values()) {
             workshop.removeParticipant(participant);
         }
         persistenceService.removeBookingFromFile(participant);
     }
 
     private Workshop[] getWorkshops(Integer... workshopIds) {
         Workshop[] workshops = new Workshop[workshopIds.length];
         for (int i = 0; i < workshops.length; i++) {
             workshops[i] = getWorkshop(workshopIds[i]);
         }
         return workshops;
     }
 
     private Workshop getWorkshop(Integer workshopId) {
         if (!workshops.containsKey(workshopId)) {
             throw new WorkshopNotFoundException("Could not find workshop with name: " + workshopId);
         }
         return workshops.get(workshopId);
     }
 
     public Collection<Participant> getParticipantsAt(Integer workshopId) {
         return getWorkshop(workshopId).getParticipants();
     }
 
     public void addWorkshop(Workshop workshop) {
         workshops.put(workshop.getId(), workshop);
     }
 
     public Collection<Workshop> getWorkshopsStartingAtSlot(int slot) {
         Collection<Workshop> result = new LinkedList<>();
         for (Workshop workshop : workshops.values()) {
             if (workshop.getStart() == slot) {
                 result.add(workshop);
             }
         }
         return result;
     }
 
     public void reloadBookings() {
         for (Workshop workshop : workshops.values()) {
             workshop.removeAllParticipants();
         }
 
         persistenceService.makeBookingsBasedOnFile(this);
     }
 
     public Collection<Workshop> getWorkshopsById(Integer... workshopIds) {
         List<Workshop> workshops = new ArrayList<>();
         for (Integer workshopId : workshopIds) {
             if (this.workshops.containsKey(workshopId)) {
                 workshops.add(this.workshops.get(workshopId));
             }
         }
         return workshops;
     }
 
     public void confirm(String mail, Integer... workshopIds) {
         List<Integer> idList = Arrays.asList(workshopIds);
         for (Integer id : workshops.keySet()) {
             if (!idList.contains(id)) {
                 workshops.get(id).removeParticipantByMail(mail);
             } else {
                 workshops.get(id).ensureOnlyOne(mail);
             }
         }
         persistenceService.confirm(mail, workshopIds);
     }
 }
