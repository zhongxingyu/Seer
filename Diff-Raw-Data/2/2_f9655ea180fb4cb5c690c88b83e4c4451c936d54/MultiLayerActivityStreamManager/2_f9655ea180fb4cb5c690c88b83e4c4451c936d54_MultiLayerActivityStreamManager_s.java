 package eu.elderspaces.activities.core;
 
 import java.util.Date;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.google.inject.Inject;
 
 import eu.elderspaces.activities.core.exceptions.ActivityManagerException;
 import eu.elderspaces.activities.core.exceptions.InvalidActivityStreamException;
 import eu.elderspaces.model.Activity;
 import eu.elderspaces.model.ActivityStream;
 import eu.elderspaces.model.Club;
 import eu.elderspaces.model.Entity;
 import eu.elderspaces.model.Event;
 import eu.elderspaces.model.Event.InvitationAnswer;
 import eu.elderspaces.model.Person;
 import eu.elderspaces.model.Verbs;
 import eu.elderspaces.persistence.ActivityStreamRepository;
 import eu.elderspaces.persistence.EntitiesRepository;
 import eu.elderspaces.persistence.SocialNetworkRepository;
 import eu.elderspaces.persistence.exceptions.ActivityStreamRepositoryException;
 
 public class MultiLayerActivityStreamManager implements ActivityStreamManager {
     
     private final ObjectMapper mapper;
     
     private final ActivityStreamRepository activityRepository;
     
     private final EntitiesRepository entitiesRepository;
     
     private final SocialNetworkRepository socialNetworkRepository;
     
     @Inject
     public MultiLayerActivityStreamManager(final ActivityStreamRepository activityRepository,
             final EntitiesRepository entitiesRepository,
             final SocialNetworkRepository socialNetworkRepository, final ObjectMapper objectMapper) {
     
         this.activityRepository = activityRepository;
         this.entitiesRepository = entitiesRepository;
         this.socialNetworkRepository = socialNetworkRepository;
         this.mapper = objectMapper;
     }
     
     @Override
     public boolean storeActivity(final String activityContent) throws ActivityManagerException {
     
         final ActivityStream activity;
         try {
             activity = mapper.readValue(activityContent, ActivityStream.class);
         } catch (final Exception e) {
             throw new ActivityManagerException("can't deserialise activity: '" + activityContent
                     + "' to json", e);
         }
         
         return storeActivity(activity);
     }
     
     @Override
     public boolean storeActivity(final ActivityStream activity) throws ActivityManagerException {
     
         final String userId = activity.getActor().getId();
         String activityId = "";
         
         try {
             activityId = activityRepository.store(activity);
         } catch (final ActivityStreamRepositoryException e) {
             throw new ActivityManagerException("can't store activity '" + activity + "'", e);
         }
         
         final boolean activityStored = !activityId.equals("");
         
         final Person user = activity.getActor();
         final String verb = activity.getVerb();
         final Entity target = activity.getTarget();
         final Date eventTime = activity.getPublished();
         
         final Entity object = activity.getObject();
         
         boolean profileUpdated = false;
         
         try {
             // entities and relations are stored according to the activity logic
             if (object.getClass() == Person.class) {
                 
                 final Person personObject = (Person) object;
                 profileUpdated = handlePersonObject(user, verb, personObject, eventTime);
                 
             } else if (object.getClass() == Activity.class) {
                 
                 final Activity postObject = (Activity) object;
                 profileUpdated = handlePostObject(user, verb, postObject, target, eventTime);
                 
             } else if (object.getClass() == Event.class) {
                 
                 final Event eventObject = (Event) object;
                 profileUpdated = handleEventObject(user, verb, eventObject, eventTime);
                 
             } else if (object.getClass() == Club.class) {
                 
                 final Club clubObject = (Club) object;
                 profileUpdated = handleClubObject(user, verb, clubObject, eventTime);
                 
             } else {
                 throw new ActivityManagerException("Not managed class type: '"
                         + object.getClass().getName() + "'");
             }
         } catch (final InvalidActivityStreamException e) {
             throw new ActivityManagerException(e.getMessage(), e);
         }
         
         return profileUpdated && activityStored;
     }
     
     private boolean handlePersonObject(final Person user, final String verb,
             final Person personObject, final Date eventTime) throws InvalidActivityStreamException {
     
         if (verb.equals(Verbs.REQUEST_FRIEND)) {
             
             // Do nothing
             
         } else if (verb.equals(Verbs.MAKE_FRIEND)) {
             
             socialNetworkRepository.addNewFriend(user.getId(), personObject.getId(), eventTime);
             
         } else if (verb.equals(Verbs.REMOVE_FRIEND)) {
             
             socialNetworkRepository.deleteFriendConnection(user.getId(), personObject.getId(),
                     eventTime);
             
         } else if (verb.equals(Verbs.UPDATE)) {
             
            entitiesRepository.updateProfile(personObject, eventTime);
             socialNetworkRepository.createNewUser(personObject.getId(), eventTime);
             
         } else if (verb.equals(Verbs.DELETE)) {
             
             entitiesRepository.deleteUser(user, eventTime);
             socialNetworkRepository.deleteUser(user.getId(), eventTime);
             
         } else {
             throw new InvalidActivityStreamException("Invalid verb");
         }
         
         return true;
     }
     
     private boolean handlePostObject(final Person user, final String verb,
             final Activity postObject, final Entity target, final Date eventTime)
             throws InvalidActivityStreamException {
     
         if (verb.equals(Verbs.CREATE)) {
             
             if (target == null) {
                 
                 entitiesRepository.postActivity(postObject, eventTime);
                 socialNetworkRepository.postActivity(user.getId(), postObject.getId(), eventTime);
                 
             } else if (target.getClass() == Event.class) {
                 
                 entitiesRepository.postActivity(postObject, eventTime);
                 socialNetworkRepository.postEventActivity(user.getId(), postObject.getId(),
                         target.getId(), eventTime);
                 
             } else if (target.getClass() == Club.class) {
                 
                 entitiesRepository.postActivity(postObject, eventTime);
                 socialNetworkRepository.postClubActivity(user.getId(), postObject.getId(),
                         target.getId(), eventTime);
                 
             } else {
                 throw new InvalidActivityStreamException("Invalid Target type");
             }
             
         } else if (verb.equals(Verbs.DELETE)) {
             
             if (target == null) {
                 
                 entitiesRepository.deleteActivity(postObject, eventTime);
                 socialNetworkRepository.deleteActivity(user.getId(), postObject.getId(), eventTime);
                 
             } else if (target.getClass() == Event.class) {
                 
                 entitiesRepository.deleteActivity(postObject, eventTime);
                 socialNetworkRepository.deleteEventActivity(user.getId(), postObject.getId(),
                         target.getId(), eventTime);
                 
             } else if (target.getClass() == Club.class) {
                 
                 entitiesRepository.deleteActivity(postObject, eventTime);
                 socialNetworkRepository.deleteClubActivity(user.getId(), postObject.getId(),
                         target.getId(), eventTime);
                 
             } else {
                 
                 throw new InvalidActivityStreamException("Invalid Target type");
             }
             
         } else {
             throw new InvalidActivityStreamException("Invalid verb");
         }
         
         return true;
     }
     
     private boolean handleEventObject(final Person user, final String verb,
             final Event eventObject, final Date eventTime) throws InvalidActivityStreamException {
     
         if (verb.equals(Verbs.CREATE)) {
             
             entitiesRepository.createEvent(eventObject, eventTime);
             socialNetworkRepository.createEvent(user.getId(), eventObject.getId(), eventTime);
             
         } else if (verb.equals(Verbs.UPDATE)) {
             
             entitiesRepository.modifyEvent(eventObject, eventTime);
             
         } else if (verb.equals(Verbs.DELETE)) {
             
             entitiesRepository.deleteEvent(eventObject, eventTime);
             socialNetworkRepository.deleteEvent(user.getId(), eventObject.getId(), eventTime);
             
         } else if (verb.equals(Verbs.YES_RSVP_RESPONSE_TO_EVENT)
                 || verb.equals(Verbs.NO_RSVP_RESPONSE_TO_EVENT)
                 || verb.equals(Verbs.MAYBE_RSVP_RESPONSE_TO_EVENT)) {
             
             InvitationAnswer answer = InvitationAnswer.NO;
             if (verb.equals(Verbs.YES_RSVP_RESPONSE_TO_EVENT)) {
                 answer = InvitationAnswer.YES;
             } else if (verb.equals(Verbs.MAYBE_RSVP_RESPONSE_TO_EVENT)) {
                 answer = InvitationAnswer.MAYBE;
             } else if (verb.equals(Verbs.NO_RSVP_RESPONSE_TO_EVENT)) {
                 answer = InvitationAnswer.NO;
             }
             
             socialNetworkRepository.respondEvent(user.getId(), eventObject.getId(), answer,
                     eventTime);
             
         } else {
             throw new InvalidActivityStreamException("Invalid verb");
         }
         
         return true;
     }
     
     private boolean handleClubObject(final Person user, final String verb, final Club clubObject,
             final Date eventTime) throws InvalidActivityStreamException {
     
         if (verb.equals(Verbs.CREATE)) {
             
             entitiesRepository.createClub(clubObject, eventTime);
             socialNetworkRepository.createClub(user.getId(), clubObject.getId(), eventTime);
             
         } else if (verb.equals(Verbs.UPDATE)) {
             
             entitiesRepository.modifyClub(clubObject, eventTime);
             
         } else if (verb.equals(Verbs.DELETE)) {
             
             entitiesRepository.deleteClub(clubObject, eventTime);
             socialNetworkRepository.deleteClub(user.getId(), clubObject.getId(), eventTime);
             
         } else if (verb.equals(Verbs.JOIN)) {
             
             socialNetworkRepository.joinClub(user.getId(), clubObject.getId(), eventTime);
             
         } else if (verb.equals(Verbs.LEAVE)) {
             
             socialNetworkRepository.leaveClub(user.getId(), clubObject.getId(), eventTime);
             
         } else {
             throw new InvalidActivityStreamException("Invalid verb");
         }
         
         return true;
     }
 }
