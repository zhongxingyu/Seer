 package org.karmaexchange.dao;
 
 import static java.lang.String.format;
 import static org.karmaexchange.util.OfyService.ofy;
 import static org.karmaexchange.util.UserService.getCurrentUserKey;
 import static com.google.common.base.CharMatcher.WHITESPACE;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.EnumSet;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.annotation.Nullable;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.karmaexchange.dao.Organization.Role;
 import org.karmaexchange.resources.msg.ErrorResponseMsg;
 import org.karmaexchange.resources.msg.ErrorResponseMsg.ErrorInfo;
 import org.karmaexchange.resources.msg.ValidationErrorInfo;
 import org.karmaexchange.resources.msg.ValidationErrorInfo.ValidationError;
 import org.karmaexchange.resources.msg.ValidationErrorInfo.ValidationErrorType;
 import org.karmaexchange.task.ProcessRatingsServlet;
 import org.karmaexchange.util.BoundedHashSet;
 import org.karmaexchange.util.SearchUtil;
 
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 import lombok.NoArgsConstructor;
 import lombok.ToString;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.VoidWork;
 import com.googlecode.objectify.annotation.Embed;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Ignore;
 import com.googlecode.objectify.annotation.Index;
 
 // TODO(avaliani):
 //   - Fix EventSearchView for images once we revamp it.
 @XmlRootElement
 @Entity
 @Data
 @EqualsAndHashCode(callSuper=true)
 @ToString(callSuper=true)
 public final class Event extends IdBaseDao<Event> {
 
   /*
    * DESIGN DETAILS
    *
    * Event permissions
    * -----------------
    * The current permissions scheme is that there is one organization for each event. And that any
    * organizer or admin for the organization can edit the event. This simple model handles the
    * 99% usage scenario.
    */
 
   public static final int MAX_EVENT_KARMA_POINTS = 500;
 
   public static final int MAX_CACHED_PARTICIPANT_IMAGES = 10;
 
   /* Each event search token results in an index write. Put a reasonable limit on it. */
   public static final int MAX_SEARCH_TOKENS = 100;
 
   /*
    * TODO(avaliani):
    *   - look at volunteer match schema
    *   - compare this to Meetup, OneBrick, Golden Gate athletic club, etc.
    */
 
   private String title;
   private String description;
   private String specialInstructions; // See flash volunteer.
   @Index
   private List<KeyWrapper<CauseType>> causes = Lists.newArrayList();
 
   private Location location;
   @Index
   private Date startTime;
   @Index
   private Date endTime;
   @Ignore
   private Status status;
 
   private Image primaryImage;
   // BUG: This embedded list is not safe since Image has embedded objects that can be
   //      optionally null. See objectify serialization bug (issue #127).
   // TODO(avaliani): fix this embedded list.
   private List<Image> allImages = Lists.newArrayList();
 
   @Index
   private List<KeyWrapper<Skill>> skillsPreferred = Lists.newArrayList();
   @Index
   private List<KeyWrapper<Skill>> skillsRequired = Lists.newArrayList();
 
   // TODO(avaliani): Organizations can co-host events.
   @Index
   private KeyWrapper<Organization> organization;
   // Organizers can also edit the event.
 
   // Can not be explicitly set. Automatically managed.
   @Index
   private List<KeyWrapper<User>> organizers = Lists.newArrayList();
 
   // Can not be explicitly set. Automatically managed.
   @Ignore
   private RegistrationInfo registrationInfo;
 
   // The maxRegistration limit only applies to participants. The limit does not include organizers.
   private int maxRegistrations;
 
   // Can not be explicitly set. Automatically managed.
   @Index
   private List<KeyWrapper<User>> registeredUsers = Lists.newArrayList();
   // Can not be explicitly set. Automatically managed.
   @Index
   private List<KeyWrapper<User>> waitListedUsers = Lists.newArrayList();
 
   // We need a consolidated list because pagination does not support OR queries.
   // NOTE: Embedded list is safe since EventParticipant has embedded objects that are always
   //       non-null.
   @Index
   private List<EventParticipant> participants = Lists.newArrayList();
 
   // Can not be set. Automatically managed. Only includes organizers and registered users. Wait
   // listed users images are skipped.
   // NOTE: Embedded list is safe since ParticipantImage has embedded objects that are always
   //       non-null.
   private List<ParticipantImage> cachedParticipantImages = Lists.newArrayList();
 
   private IndexedAggregateRating rating;
   private DerivedRatingTracker derivedRatings;
 
   /**
    * The number of karma points earned by participating in the event. This is derived from the
    * start and end time.
    */
   @Index
   private int karmaPoints;
 
   @Index
   private List<String> searchableTokens;
 
   @Index
   private boolean completionProcessed;
   private CompletionTaskTracker completionTasks;
 
   private List<SuitableForType> suitableForTypes = Lists.newArrayList();
 
   public enum RegistrationInfo {
     ORGANIZER,
     REGISTERED,
     WAIT_LISTED,
     CAN_REGISTER,
     CAN_WAIT_LIST,
     FULL
   }
 
   public enum Status {
     UPCOMING,
     IN_PROGRESS,
     COMPLETED
   }
 
   public enum ParticipantType {
     ORGANIZER,
     REGISTERED,
     WAIT_LISTED
   }
 
   private enum MutationType {
     INSERT,
     UPDATE,
     DELETE
   }
 
   public void setOrganizers(List<KeyWrapper<User>> ignored) {
     // No-op it.
   }
   public void setRegisteredUsers(List<KeyWrapper<User>> ignored) {
     // No-op it.
   }
   public void setWaitListedUsers(List<KeyWrapper<User>> ignored) {
     // No-op it.
   }
   public void setCachedParticipantImages(List<ParticipantImage> ignored) {
     // No-op it.
   }
   public void setRegistrationInfo(RegistrationInfo ignored) {
     // No-op it.
   }
 
   public static String getParticipantPropertyName() {
     return "participants.user.key";
   }
 
   public static String getParticipantPropertyName(ParticipantType type) {
     switch (type) {
       case ORGANIZER:
         return "organizers.key";
       case REGISTERED:
         return "registeredUsers.key";
       case WAIT_LISTED:
         return "waitListedUsers.key";
     }
     throw new IllegalStateException("Unknown participant type: " + type);
   }
 
   @Override
   protected void preProcessInsert() {
     super.preProcessInsert();
     if (title != null) {
       title = WHITESPACE.trimFrom(title);
     }
     initParticipantLists();
     // Add the current user as an organizer if there are no organizers registered.
     if (organizers.isEmpty()) {
       Iterables.removeIf(participants, EventParticipant.userPredicate(getCurrentUserKey()));
       participants.add(EventParticipant.create(getCurrentUserKey(), ParticipantType.ORGANIZER));
       initParticipantLists();
     }
     processParticipants();
     processSuitableFor();
 
     validateEvent();
 
     initKarmaPoints();
     rating = IndexedAggregateRating.create();
     initDerivedRatings();
     initSearchableTokens();
     completionProcessed = false;
     completionTasks = null;
   }
 
   @Override
   protected void processUpdate(Event prevObj) {
     super.processUpdate(prevObj);
     if (title != null) {
       title = WHITESPACE.trimFrom(title);
     }
     initKarmaPoints();
     // Rating is independently and transactionally updated.
     rating = prevObj.rating;
     derivedRatings = prevObj.derivedRatings;
     // Participants is independently and transactionally updated.
     participants = Lists.newArrayList(prevObj.participants);
     processParticipants();
     processSuitableFor();
     validateEvent();
     initSearchableTokens();
     completionProcessed = prevObj.completionProcessed;
     completionTasks = prevObj.completionTasks;
 
     // Do event validation that is specific to event updates.
     validateEventUpdate(prevObj);
   }
 
   private void processParticipantMutation(EventParticipant updatedParticipant,
       MutationType mutationType) {
     processParticipantMutation(ImmutableList.of(updatedParticipant), mutationType);
   }
 
   private void processParticipantMutation(Collection<EventParticipant> mutatedParticipants,
                                           MutationType mutationType) {
     processParticipants();
     for (EventParticipant participant : mutatedParticipants) {
       derivedRatings.processParticipantMutation(this, participant, mutationType);
       if (completionTasks != null) {
         completionTasks.processParticipantMutation(this, participant, mutationType);
         updateCompletionProcessed();
       }
     }
     validateEvent();
   }
 
   private void processRatingUpdate() {
     derivedRatings.processRatingUpdate(this);
   }
 
   private void processPendingRating() {
     derivedRatings.processPendingRating(this);
   }
 
   private boolean hasPendingRatings() {
     return derivedRatings.hasPendingRatings();
   }
 
   private void processCompletionTasks() {
     // Note that we don't check the time to see if it's okay to process completion tasks.
     // Instead we trust that the caller has checked the time. This handles cases where there
     // is clock skew and the completion task was aborted and restarted on a different machine.
     if (completionTasks == null) {
       // This is the first time completion tasks are being processed. On the first pass the
       // event object state must be cleaned up.
       removeWaitListedUsers();
       // After the state is cleaned up we process the remaining event completion tasks.
       completionTasks = new CompletionTaskTracker(this);
     }
     completionTasks.processPendingTask(this);
     updateCompletionProcessed();
   }
 
   private void updateCompletionProcessed() {
     completionProcessed = !completionTasks.tasksPending();
   }
 
   private void removeWaitListedUsers() {
     List<EventParticipant> participantsRemoved = Lists.newArrayList();
     Iterator<EventParticipant> participantIter = participants.iterator();
     while (participantIter.hasNext()) {
       EventParticipant participant = participantIter.next();
       if (participant.getType() == ParticipantType.WAIT_LISTED) {
         participantIter.remove();
         participantsRemoved.add(participant);
       }
     }
     processParticipantMutation(participantsRemoved, MutationType.DELETE);
   }
 
   private void processParticipants() {
     initParticipantLists();
     processWaitList();
     updateCachedParticipantImages();
   }
 
   private void processSuitableFor() {
     if (!suitableForTypes.isEmpty()) {
       // Eliminate any duplicates.
       EnumSet<SuitableForType> suitableForSet = EnumSet.copyOf(suitableForTypes);
       suitableForTypes = Lists.newArrayList(suitableForSet);
     }
   }
 
   @Override
   protected void processLoad() {
     // initParticipantLists() must be called prior to processLoad so that updatePermissions can
     // use the participant lists to calculate the permissions.
     initParticipantLists();
     super.processLoad();
     initStatus();
     updateRegistrationInfo();
   }
 
   private void initStatus() {
     status = computeStatus(this);
   }
 
   private static Status computeStatus(Event event) {
     if (event.completionTasks != null) {
       return Status.COMPLETED;
     }
     Date now = new Date();
     if (now.before(event.startTime)) {
       return Status.UPCOMING;
     } else if (now.before(event.endTime)) {
       return Status.IN_PROGRESS;
     } else {
       return Status.COMPLETED;
     }
   }
 
   private void initKarmaPoints() {
     long eventDurationMins = (endTime.getTime() - startTime.getTime()) / (1000 * 60);
     karmaPoints = (int) Math.min(eventDurationMins, MAX_EVENT_KARMA_POINTS);
   }
 
   private void initDerivedRatings() {
     derivedRatings = new DerivedRatingTracker(this);
   }
 
   private void initSearchableTokens() {
     BoundedHashSet<String> searchableTokensSet = BoundedHashSet.create(MAX_SEARCH_TOKENS);
     for (SuitableForType suitableForType : suitableForTypes) {
       searchableTokensSet.addIfSpace(suitableForType.getTag());
     }
     // The lowest priority tokens should be added to the end of the searchableContent. Once the
     // token limit is hit the remaining tokens will be discarded.
     StringBuilder searchableContent = new StringBuilder();
     searchableContent.append(title);
     searchableContent.append(' ');
     for (KeyWrapper<CauseType> causeKeyWrapper : causes) {
       Key<CauseType> causeKey = KeyWrapper.toKey(causeKeyWrapper);
       // We add causes both as tags and text strings to be parsed since keywords like
       // homeless and animals are good for non-tag based keyword search.
       searchableContent.append(CauseType.getCauseTypeAsString(causeKey));
       searchableContent.append(' ');
       searchableTokensSet.addIfSpace(CauseType.getTag(causeKey));
     }
     if ((location != null) && (location.getTitle() != null)) {
       searchableContent.append(location.getTitle());
       searchableContent.append(' ');
     }
     searchableContent.append(description);
     SearchUtil.addSearchableTokens(searchableTokensSet, searchableContent.toString());
     searchableTokens = Lists.newArrayList(searchableTokensSet);
   }
 
   private void initParticipantLists() {
     organizers = Lists.newArrayList();
     registeredUsers = Lists.newArrayList();
     waitListedUsers = Lists.newArrayList();;
     for (EventParticipant participant : participants) {
       switch (participant.getType()) {
         case ORGANIZER:
           organizers.add(participant.getUser());
           break;
         case REGISTERED:
           registeredUsers.add(participant.getUser());
           break;
         case WAIT_LISTED:
           waitListedUsers.add(participant.getUser());
           break;
         default:
           checkState(false, "unknown participant type: " + participant.getType());
       }
     }
   }
 
   @Nullable
   private EventParticipant tryFindParticipant(Key<User> userKey) {
     return Iterables.tryFind(participants, EventParticipant.userPredicate(userKey)).orNull();
   }
 
   public List<KeyWrapper<User>> getParticipants(ParticipantType paticipantType) {
     switch (paticipantType) {
       case ORGANIZER:
         return organizers;
       case REGISTERED:
         return registeredUsers;
       case WAIT_LISTED:
         return waitListedUsers;
       default:
         throw new IllegalStateException("unknown participant type: " + paticipantType);
     }
   }
 
   private void validateEvent() {
     List<ValidationError> validationErrors = Lists.newArrayList();
 
     if ((null == title) || title.isEmpty()) {
       validationErrors.add(new ResourceValidationError(
         this, ValidationErrorType.RESOURCE_FIELD_VALUE_REQUIRED, "title"));
     }
     // TODO(avaliani): make sure the description has a minimum length. Avoiding this for now
     //   since we don't have auto event creation.
     if (null == startTime) {
       validationErrors.add(new ResourceValidationError(
         this, ValidationErrorType.RESOURCE_FIELD_VALUE_REQUIRED, "startTime"));
     }
     if (null == endTime) {
       validationErrors.add(new ResourceValidationError(
         this, ValidationErrorType.RESOURCE_FIELD_VALUE_REQUIRED, "endTime"));
     }
     if ((startTime != null) && (endTime != null) && !startTime.before(endTime)) {
       validationErrors.add(new MultiFieldResourceValidationError(
         this, ValidationErrorType.RESOURCE_FIELD_VALUE_MUST_BE_GT_SPECIFIED_FIELD,
         "endTime", "startTime"));
     }
     if (maxRegistrations < 1) {
       validationErrors.add(new LimitResourceValidationError(
         this, ValidationErrorType.RESOURCE_FIELD_VALUE_MUST_BE_GTEQ_LIMIT,
         "maxRegistrations", 1));
     }
     if (organization == null) {
       validationErrors.add(new ResourceValidationError(
         this, ValidationErrorType.RESOURCE_FIELD_VALUE_REQUIRED, "organization"));
     } else {
       if (organizers.isEmpty()) {
         validationErrors.add(new ResourceValidationError(
           this, ValidationErrorType.RESOURCE_FIELD_VALUE_REQUIRED, "organizers"));
         List<User> organizerEntities =
             BaseDao.load(KeyWrapper.toKeys(organizers), ofy().transactionless());
         for (User organizer : organizerEntities) {
           if (!organizer.hasOrgMembership(KeyWrapper.toKey(organization), Role.ORGANIZER)) {
             validationErrors.add(new ListValueValidationError(
               this, ValidationErrorType.RESOURCE_FIELD_LIST_VALUE_INVALID_PERMISSIONS, "organizers",
               organizer.getKey()));
           }
         }
       }
     }
 
     if (!validationErrors.isEmpty()) {
       throw ValidationErrorInfo.createException(validationErrors);
     }
   }
 
   private void validateEventUpdate(Event prevObj) {
     List<ValidationError> validationErrors = Lists.newArrayList();
 
     // Check the prev object's state since some of the fields of the current object can be
     // manipulated in an update.
     if (computeStatus(prevObj) == Status.COMPLETED) {
       if (!startTime.equals(prevObj.startTime)) {
         validationErrors.add(new ResourceValidationError(
           this, ValidationErrorType.RESOURCE_FIELD_VALUE_UNMODIFIABLE, "startTime"));
       }
       if (!endTime.equals(prevObj.endTime)) {
         validationErrors.add(new ResourceValidationError(
           this, ValidationErrorType.RESOURCE_FIELD_VALUE_UNMODIFIABLE, "endTime"));
       }
       // To simplify completion task processing, we don't allow organizations to be modifiable
       // after event completion.
       if (!organization.equals(prevObj.organization)) {
         validationErrors.add(new ResourceValidationError(
           this, ValidationErrorType.RESOURCE_FIELD_VALUE_UNMODIFIABLE, "organization"));
       }
     }
 
     if (!validationErrors.isEmpty()) {
       throw ValidationErrorInfo.createException(validationErrors);
     }
   }
 
   private void processWaitList() {
     if ((registeredUsers.size() < maxRegistrations) &&
         !waitListedUsers.isEmpty()) {
       int numSpots = maxRegistrations - registeredUsers.size();
       List<KeyWrapper<User>> usersToRegister =
           waitListedUsers.subList(0, Math.min(numSpots, waitListedUsers.size()));
       for (KeyWrapper<User> userToRegister : usersToRegister) {
         EventParticipant participant = Iterables.find(participants,
           EventParticipant.userPredicate(KeyWrapper.toKey(userToRegister)));
         participant.setType(ParticipantType.REGISTERED);
       }
       initParticipantLists();
     }
   }
 
   private void updateCachedParticipantImages() {
     // Prune any deleted or wait listed participants.
     Iterator<ParticipantImage> cachedImageIter = cachedParticipantImages.iterator();
     while (cachedImageIter.hasNext()) {
       ParticipantImage cachedImage = cachedImageIter.next();
       Key<User> userKey = KeyWrapper.toKey(cachedImage.getParticipant());
       EventParticipant participant = tryFindParticipant(userKey);
       if ((participant == null) || (participant.getType() == ParticipantType.WAIT_LISTED)) {
         cachedImageIter.remove();
       }
     }
 
     // Add images if there is room.
     int numParticipantImagesToCache = getNumAttending() - cachedParticipantImages.size();
     numParticipantImagesToCache = Math.min(numParticipantImagesToCache,
       MAX_CACHED_PARTICIPANT_IMAGES - cachedParticipantImages.size());
     if (numParticipantImagesToCache > 0) {
       List<Key<User>> usersToFetch = Lists.newArrayList();
       for (KeyWrapper<User> participantKey :
            getAttendingParticipants(MAX_CACHED_PARTICIPANT_IMAGES)) {
         if (!Iterables.any(cachedParticipantImages,
             ParticipantImage.userPredicate(participantKey))) {
           usersToFetch.add(KeyWrapper.toKey(participantKey));
           numParticipantImagesToCache--;
           if (numParticipantImagesToCache == 0) {
             break;
           }
         }
       }
       List<User> participantsToCache = BaseDao.load(usersToFetch, ofy().transactionless());
       for (User participantToCache : participantsToCache) {
         cachedParticipantImages.add(ParticipantImage.create(participantToCache));
       }
     }
   }
 
   private List<KeyWrapper<User>> getAttendingParticipants(int limit) {
     List<KeyWrapper<User>> attendingParticipants = Lists.newArrayList();
     for (KeyWrapper<User> organizer : organizers) {
       attendingParticipants.add(organizer);
       limit--;
       if (limit == 0) {
         break;
       }
     }
     if (limit > 0) {
       for (KeyWrapper<User> registeredUser : registeredUsers) {
         attendingParticipants.add(registeredUser);
         limit--;
         if (limit == 0) {
           break;
         }
       }
     }
     return attendingParticipants;
   }
 
   public int getNumAttending() {
     return organizers.size() + registeredUsers.size();
   }
 
   public void setNumAttending(int ignore) {
     // No-op.
   }
 
   private void updateRegistrationInfo() {
     EventParticipant participant = tryFindParticipant(getCurrentUserKey());
     if (participant == null) {
       if (registeredUsers.size() < maxRegistrations) {
         registrationInfo = RegistrationInfo.CAN_REGISTER;
       } else {
         registrationInfo = RegistrationInfo.CAN_WAIT_LIST;
       }
       // } else if (waitListedUsers.size() < maxWaitingList) {
       //  registrationInfo = RegistrationInfo.CAN_WAIT_LIST;
       // } else {
       //  registrationInfo = RegistrationInfo.FULL;
       // }
     } else {
       if (participant.type == ParticipantType.ORGANIZER) {
         registrationInfo = RegistrationInfo.ORGANIZER;
       } else if (participant.type == ParticipantType.REGISTERED) {
         registrationInfo = RegistrationInfo.REGISTERED;
       } else {
         checkState(participant.type == ParticipantType.WAIT_LISTED,
             "unknown participant type: " + participant.type);
         registrationInfo = RegistrationInfo.WAIT_LISTED;
       }
     }
   }
 
   // TODO(avaliani): Always calculating eval permission is expensive in the long run. It results
   //     in additional loads even for read operations. Since we're fetching the current user
   //     transactionless the user should be saved in the session cache, so the overhead is not
   //     terrible. But this should be eliminated if possible.
   @Override
   protected Permission evalPermission() {
     User currentUser = BaseDao.load(getCurrentUserKey(), ofy().transactionless());
     if (currentUser.hasOrgMembership(KeyWrapper.toKey(organization),
           Organization.Role.ORGANIZER)) {
       return Permission.ALL;
     }
     return Permission.READ;
   }
 
   @Data
   @EqualsAndHashCode(callSuper=false)
   public static class UpsertParticipantTxn extends VoidWork {
     private final Key<Event> eventKey;
     private final Key<User> userToUpsertKey;
     private final ParticipantType participantType;
 
     public void vrun() {
       Event event = BaseDao.load(eventKey);
       if (event == null) {
         throw ErrorResponseMsg.createException("event not found",
           ErrorInfo.Type.BAD_REQUEST);
       }
 
       EventParticipant participantToUpsert = event.tryFindParticipant(userToUpsertKey);
       // Users that can not edit the event are restricted to only adding themselves to the
       // registered list or the waiting list. Additionally, users with edit permissions
       // bypass all event registration and waiting list limits.
       if (!event.permission.canEdit()) {
         if (!userToUpsertKey.equals(getCurrentUserKey())) {
           throw ErrorResponseMsg.createException(
             "only organizers can add any participant to the event",
             ErrorInfo.Type.BAD_REQUEST);
         }
         if (participantType == ParticipantType.ORGANIZER) {
           throw ErrorResponseMsg.createException(
             "insufficent priveleges to make the current user an organizer of the event",
             ErrorInfo.Type.BAD_REQUEST);
         }
         if ((participantToUpsert != null) && (participantToUpsert.getType() == participantType)) {
           // Nothing to do.
           return;
         }
         if (participantType == ParticipantType.REGISTERED) {
           if (event.registeredUsers.size() >= event.maxRegistrations) {
             throw ErrorResponseMsg.createException(
               "the event has reached the max registration limit",
               ErrorInfo.Type.LIMIT_REACHED);
           }
         } else {
           checkState(participantType == ParticipantType.WAIT_LISTED);
           // if (event.waitListedUsers.size() >= event.maxWaitingList) {
           //  throw ErrorResponseMsg.createException(
           //    "the event has reached the max waiting list limit",
           //    ErrorInfo.Type.LIMIT_REACHED);
           // }
         }
       }
 
       if (event.status == Status.COMPLETED) {
         if (participantType == ParticipantType.ORGANIZER) {
           throw ErrorResponseMsg.createException(
             "organizers can not be added after event completion",
             ErrorInfo.Type.BAD_REQUEST);
         }
         if (participantType == ParticipantType.WAIT_LISTED) {
           throw ErrorResponseMsg.createException(
             "wait listed users can not be added after event completion",
             ErrorInfo.Type.BAD_REQUEST);
         }
       }
 
       MutationType mutationType;
       if (participantToUpsert == null) {
         // TODO(avaliani): organizers should not in theory be allowed to add arbitrary users.
         //     However, this simplifies testing so we're going to allow it for now.
         participantToUpsert = EventParticipant.create(userToUpsertKey, participantType);
         event.participants.add(participantToUpsert);
         mutationType = MutationType.INSERT;
       } else {
         ParticipantType prevParticipantType = participantToUpsert.getType();
         if (participantType == prevParticipantType) {
           // Nothing to do.
           return;
         }
         if ((event.status == Status.COMPLETED) &&
             (prevParticipantType == ParticipantType.ORGANIZER)) {
           throw ErrorResponseMsg.createException(
             "organizers can not be updated after event completion",
             ErrorInfo.Type.BAD_REQUEST);
         }
         participantToUpsert.setType(participantType);
         mutationType = MutationType.UPDATE;
       }
       // validateEvent() ensures that there is at least one organizer.
       event.processParticipantMutation(participantToUpsert, mutationType);
       BaseDao.partialUpdate(event);
     }
   }
 
   @Data
   @EqualsAndHashCode(callSuper=false)
   public static class DeleteParticipantTxn extends VoidWork {
     private final Key<Event> eventKey;
     private final Key<User> userToRemoveKey;
 
     public void vrun() {
       Event event = BaseDao.load(eventKey);
       if (event == null) {
         throw ErrorResponseMsg.createException("event not found",
           ErrorInfo.Type.BAD_REQUEST);
       }
       if (!event.permission.canEdit() && !userToRemoveKey.equals(getCurrentUserKey())) {
         throw ErrorResponseMsg.createException(
           "only organizers can remove any participant from the event",
           ErrorInfo.Type.BAD_REQUEST);
       }
       EventParticipant participant = event.tryFindParticipant(userToRemoveKey);
       if (participant != null) {
         if (event.status == Status.COMPLETED) {
           if (participant.getType() == ParticipantType.ORGANIZER) {
             throw ErrorResponseMsg.createException(
               "organizers can not be removed after event completion",
               ErrorInfo.Type.BAD_REQUEST);
           }
           if (canWriteReview(participant)) {
             Review participantReview = BaseDao.load(
               Review.getKeyForUser(Key.create(event), KeyWrapper.toKey(participant.getUser())));
             if (participantReview != null) {
               throw ErrorResponseMsg.createException(
                 "users that have written a review can not be removed after event completion",
                 ErrorInfo.Type.BAD_REQUEST);
             }
           }
         }
         event.participants.remove(participant);
         // validateEvent() ensures that there is at least one organizer.
         event.processParticipantMutation(participant, MutationType.DELETE);
         BaseDao.partialUpdate(event);
       }
     }
   }
 
   @Embed
   @Data
   @NoArgsConstructor
   public static final class EventParticipant {
     @Index
     private KeyWrapper<User> user;
     private ParticipantType type;
 
     public static EventParticipant create(Key<User> user, ParticipantType type) {
       return new EventParticipant(KeyWrapper.create(user), type);
     }
 
     private EventParticipant(KeyWrapper<User> user, ParticipantType type) {
       this.user = user;
       this.type = type;
     }
 
     public static Predicate<EventParticipant> userPredicate(final Key<User> userKey) {
       return new Predicate<EventParticipant>() {
         @Override
         public boolean apply(@Nullable EventParticipant input) {
           return KeyWrapper.toKey(input.user).equals(userKey);
         }
       };
     }
   }
 
   public static void mutateEventReviewForCurrentUser(Key<Event> eventKey, @Nullable Review review) {
     ofy().transact(new MutateEventReviewTxn(eventKey, getCurrentUserKey(), review));
   }
 
   @Data
   @EqualsAndHashCode(callSuper=false)
   private static class MutateEventReviewTxn extends VoidWork {
     private final Key<Event> eventKey;
     private final Key<User> userKey;
     @Nullable
     private final Review review;
 
     public void vrun() {
       Event event = BaseDao.load(eventKey);
       if (event == null) {
         throw ErrorResponseMsg.createException("event not found", ErrorInfo.Type.BAD_REQUEST);
       }
       EventParticipant participantDetails = event.tryFindParticipant(userKey);
       if ((participantDetails == null) || !canWriteReview(participantDetails)) {
         throw ErrorResponseMsg.createException(
           "only users that participated in the event (non-organizers) can provide an event review",
           ErrorInfo.Type.BAD_REQUEST);
       }
       if (event.status != Status.COMPLETED) {
         throw ErrorResponseMsg.createException(
           "only events that have completed can be reviewed", ErrorInfo.Type.BAD_REQUEST);
       }
       processReviewMutation(event, userKey, review);
       BaseDao.partialUpdate(event);
     }
   }
 
   public static void processReviewMutation(Event event, Key<User> userKey,
       @Nullable Review review) {
     boolean ratingMutated = false;
     Key<Review> expReviewKey = Review.getKeyForUser(Key.create(event), userKey);
     if (review != null) {
       review.initPreUpsert(Key.create(event), userKey);
       if (!Key.create(review).equals(expReviewKey)) {
         throw ErrorResponseMsg.createException(
           format("review key [%s] does not match expected review key [%s]",
             Key.create(review).toString(), expReviewKey.toString()),
           ErrorInfo.Type.BAD_REQUEST);
       }
     }
     Review existingReview = BaseDao.load(expReviewKey);
     if (existingReview != null) {
       event.rating.deleteRating(existingReview.getRating());
       ratingMutated = true;
     }
     if (review == null) {
       if (existingReview != null) {
         BaseDao.delete(Key.create(existingReview));
       }
     } else {
       event.rating.addRating(review.getRating());
       ratingMutated = true;
       // An upsert will automatically delete the old review since the key for the new and the
       // old review is the same.
       BaseDao.upsert(review);
     }
     if (ratingMutated) {
       event.processRatingUpdate();
     }
   }
 
   private static boolean canWriteReview(EventParticipant participant) {
     return participant.getType() == ParticipantType.REGISTERED;
   }
 
   /**
    * This class updates the organizer event ratings for all organizers associated with an event.
    *
    * Note that all methods in this class should be invoked from within the context of a transaction.
    */
   @Data
   @Embed
   @NoArgsConstructor
   public static class DerivedRatingTracker {
 
     // NOTE: The embedded lists are safe since DerivedRatingWrapper has been modified to avoid
     //       encountering the objectify serialization bug (issue #127).
     List<DerivedRatingWrapper> processed = Lists.newArrayList();
     List<DerivedRatingWrapper> pending = Lists.newArrayList();
 
     public DerivedRatingTracker(Event event) {
       for (EventParticipant participant : event.participants) {
         if (participant.getType() == ParticipantType.ORGANIZER) {
           processParticipantMutation(event, participant, MutationType.INSERT);
         }
       }
       processed.add(new DerivedRatingWrapper(
         new OrganizationDerivedRating(KeyWrapper.toKey(event.organization))));
     }
 
     public void processParticipantMutation(Event event, EventParticipant participant,
         MutationType mutationType) {
       processParticipantMutation(event, KeyWrapper.toKey(participant.getUser()),
         ((mutationType == MutationType.DELETE) ? false :
           (participant.getType() == ParticipantType.ORGANIZER)));
     }
 
     private void processParticipantMutation(Event event, Key<User> userKey, boolean isOrganizer) {
       boolean pendingQueueWasEmpty = pending.isEmpty();
       DerivedRatingWrapper derivedRating = tryFindOrganizerDerivedRating(pending, userKey);
       if (derivedRating != null) {
         // Nothing to do, already queued.
         return;
       }
       derivedRating = tryFindOrganizerDerivedRating(processed, userKey);
       if (derivedRating == null) {
         if (isOrganizer) {
           if (event.getRating().getCount() > 0) {
             pending.add(new DerivedRatingWrapper(new OrganizerDerivedRating(userKey)));
           } else {
             processed.add(new DerivedRatingWrapper(new OrganizerDerivedRating(userKey)));
           }
         }
       } else {
         if (isOrganizer) {
           if (!event.getRating().equals(derivedRating.getOrganizerRating().accumulatedRating)) {
             processed.remove(derivedRating);
             pending.add(derivedRating);
           }
         } else {
           processed.remove(derivedRating);
           if (derivedRating.getOrganizerRating().accumulatedRating.getCount() > 0) {
             pending.add(derivedRating);
           }
         }
       }
       queueProcessingTask(event, pendingQueueWasEmpty);
     }
 
     private static DerivedRatingWrapper tryFindOrganizerDerivedRating(
         List<DerivedRatingWrapper> list, Key<User> organizerKey) {
       return Iterables.tryFind(list, organizerPredicate(organizerKey)).orNull();
     }
 
     public static Predicate<DerivedRatingWrapper> organizerPredicate(
         final Key<User> organizerKey) {
       return new Predicate<DerivedRatingWrapper>() {
         @Override
         public boolean apply(@Nullable DerivedRatingWrapper input) {
           if (input.getOrganizerRating() != null) {
             return KeyWrapper.toKey(input.getOrganizerRating().organizer).equals(organizerKey);
           } else {
             return false;
           }
         }
       };
     }
 
     public void processRatingUpdate(Event event) {
       boolean pendingQueueWasEmpty = pending.isEmpty();
       pending.addAll(processed);
       processed.clear();
       queueProcessingTask(event, pendingQueueWasEmpty);
     }
 
     private void queueProcessingTask(Event event, boolean pendingQueueWasEmpty) {
       if (pendingQueueWasEmpty && !pending.isEmpty()) {
         ProcessRatingsServlet.enqueueTask(event);
       }
     }
 
     public void processPendingRating(Event event) {
       DerivedRatingWrapper pendingRating = pending.remove(0);
       DerivedRatingWrapper processedRating = pendingRating.processPendingRating(event);
       if (processedRating != null) {
         processed.add(processedRating);
       }
     }
 
     public boolean hasPendingRatings() {
       return !pending.isEmpty();
     }
 
     private interface DerivedRating {
       @Nullable
       public DerivedRatingWrapper processPendingRating(Event event);
     }
 
     /*
      * This class works around the objectify limitation that embedded classes can not be
      * polymorphic.
      */
     @Data
     @Embed
     @NoArgsConstructor
     private static class DerivedRatingWrapper implements DerivedRating {
 
       private OrganizerDerivedRating organizerRating = new OrganizerDerivedRating();
       private OrganizationDerivedRating organizationRating = new OrganizationDerivedRating();
 
       public DerivedRatingWrapper(OrganizerDerivedRating organizerRating) {
         this.organizerRating = organizerRating;
       }
 
       public DerivedRatingWrapper(OrganizationDerivedRating organizationRating) {
         this.organizationRating = organizationRating;
       }
 
       public OrganizerDerivedRating getOrganizerRating() {
         return organizerRating.isNull() ? null : organizerRating;
       }
 
       public OrganizationDerivedRating getOrganizationRating() {
         return organizationRating.isNull() ? null : organizationRating;
       }
 
       @Override
       public DerivedRatingWrapper processPendingRating(Event event) {
         if (!organizerRating.isNull()) {
           return organizerRating.processPendingRating(event);
         } else {
           return organizationRating.processPendingRating(event);
         }
       }
     }
 
     @Data
     @Embed
     @NoArgsConstructor
     public static class OrganizerDerivedRating implements DerivedRating {
       NullableKeyWrapper<User> organizer = NullableKeyWrapper.create();
       AggregateRating accumulatedRating = AggregateRating.create();
 
       public OrganizerDerivedRating(Key<User> organizerKey) {
         this(organizerKey, null);
       }
 
       public OrganizerDerivedRating(Key<User> organizerKey,
           @Nullable AggregateRating ratingToCopy) {
         this.organizer = NullableKeyWrapper.create(organizerKey);
         accumulatedRating = AggregateRating.create(ratingToCopy);
       }
 
       public boolean isNull() {
         return organizer.isNull();
       }
 
       @Override
       public DerivedRatingWrapper processPendingRating(Event event) {
         Key<User> userKey = KeyWrapper.toKey(organizer);
         User user = BaseDao.load(userKey);
         if (user == null) {
           // Nothing to do. User no longer exists.
           return null;
         }
         // Delete the old rating.
         user.getEventOrganizerRating().deleteAggregateRating(accumulatedRating);
         // Add the new rating.
         DerivedRatingWrapper processedRating = null;
         EventParticipant eventParticipant = event.tryFindParticipant(userKey);
         if ((eventParticipant != null) &&
             (eventParticipant.getType() == ParticipantType.ORGANIZER)) {
           user.getEventOrganizerRating().addAggregateRating(event.getRating());
           processedRating = new DerivedRatingWrapper(
             new OrganizerDerivedRating(userKey, event.getRating()));
         }
         BaseDao.partialUpdate(user);
         return processedRating;
       }
     }
 
     @Data
     @Embed
     @NoArgsConstructor
     public static class OrganizationDerivedRating implements DerivedRating {
       NullableKeyWrapper<Organization> organization = NullableKeyWrapper.create();
       AggregateRating accumulatedRating = AggregateRating.create();
 
       public OrganizationDerivedRating(Key<Organization> organizerKey) {
         this(organizerKey, null);
       }
 
       private OrganizationDerivedRating(Key<Organization> organizerKey,
           @Nullable AggregateRating ratingToCopy) {
         this.organization = NullableKeyWrapper.create(organizerKey);
         accumulatedRating = AggregateRating.create(ratingToCopy);
       }
 
       public boolean isNull() {
         return organization.isNull();
       }
 
       @Override
       public DerivedRatingWrapper processPendingRating(Event event) {
         Key<Organization> orgKey = KeyWrapper.toKey(organization);
         Organization org = BaseDao.load(orgKey);
         if (org == null) {
           // Nothing to do. Org no longer exists.
           return null;
         }
         // Delete the old rating.
         org.getEventRating().deleteAggregateRating(accumulatedRating);
         // Add the new rating.
         org.getEventRating().addAggregateRating(event.getRating());
         BaseDao.partialUpdate(org);
         return new DerivedRatingWrapper(
           new OrganizationDerivedRating(orgKey, event.getRating()));
       }
     }
   }
 
   public static void processDerivedRatings(Key<Event> eventKey) {
     ProcessDerivedRatingsTxn ratingsTxn;
     do {
       ratingsTxn = new ProcessDerivedRatingsTxn(eventKey);
       ofy().transact(ratingsTxn);
     } while (ratingsTxn.isWorkPending());
   }
 
   @Data
   @EqualsAndHashCode(callSuper=false)
   public static class ProcessDerivedRatingsTxn extends VoidWork {
     private final Key<Event> eventKey;
     private boolean workPending;
 
     public void vrun() {
       Event event = BaseDao.load(eventKey);
       if ((event == null) || !event.hasPendingRatings()) {
         // The event may no longer exist or there may be no work pending.
         return;
       }
       event.processPendingRating();
       workPending = event.hasPendingRatings();
       BaseDao.partialUpdate(event);
     }
   }
 
   @Data
   @Embed
   @NoArgsConstructor
   private static class CompletionTaskTracker {
 
     // NOTE: The embedded lists are safe since CompletionTaskWrapper has been modified to avoid
     //       encountering the objectify serialization bug (issue #127).
     List<CompletionTaskWrapper> tasksPending = Lists.newArrayList();
     List<CompletionTaskWrapper> tasksProcessed = Lists.newArrayList();
 
     public CompletionTaskTracker(Event event) {
       List<KeyWrapper<User>> participantsToProcess = Lists.newArrayList();
       participantsToProcess.addAll(event.organizers);
       participantsToProcess.addAll(event.registeredUsers);
       for (KeyWrapper<User> participant : participantsToProcess) {
         tasksPending.add(
           new CompletionTaskWrapper(
             new ParticipantCompletionTask(KeyWrapper.toKey(participant), 0)));
       }
       List<Key<Organization>> allOrgs = Lists.newArrayList();
       Key<Organization> eventOrgKey = KeyWrapper.toKey(event.organization);
       allOrgs.add(eventOrgKey);
       // Parent orgs also accrue Karma points.
       allOrgs.addAll(Organization.getAncestorOrgs(eventOrgKey));
       for (Key<Organization> orgKey : allOrgs) {
         tasksPending.add(
           new CompletionTaskWrapper(
             new OrganizationCompletionTask(orgKey)));
       }
     }
 
     public void processParticipantMutation(Event event, EventParticipant participant,
                                            MutationType mutationType) {
       Key<User> participantKey = KeyWrapper.toKey(participant.getUser());
       CompletionTaskWrapper completionTask =
           tryFindParticipantCompletionTask(tasksPending, participantKey);
       if (completionTask != null) {
         // Nothing to do, already queued.
         return;
       }
       completionTask = tryFindParticipantCompletionTask(tasksProcessed, participantKey);
       if (completionTask == null) {
         // Since participant mutation post-event completion is rare, we'll always take the hit
         // of updating the event.
         tasksPending.add(new CompletionTaskWrapper(
           new ParticipantCompletionTask(participantKey, 0)));
       } else {
         tasksProcessed.remove(completionTask);
         tasksPending.add(new CompletionTaskWrapper(
           new ParticipantCompletionTask(completionTask.getParticipantTask())));
       }
     }
 
     private void processPendingTask(Event event) {
       if (tasksPending()) {
         CompletionTaskWrapper pendingTask = tasksPending.remove(0);
         CompletionTaskWrapper completedTask = pendingTask.processPendingTask(event);
         if (completedTask != null) {
           tasksProcessed.add(completedTask);
         }
       }
     }
 
     public boolean tasksPending() {
       return tasksPending.size() > 0;
     }
 
     private static CompletionTaskWrapper tryFindParticipantCompletionTask(
         List<CompletionTaskWrapper> list, Key<User> participantKey) {
       CompletionTaskWrapper task =
           Iterables.tryFind(list, participantPredicate(participantKey)).orNull();
       return task;
     }
 
     private static Predicate<CompletionTaskWrapper> participantPredicate(
         final Key<User> participantKey) {
       return new Predicate<CompletionTaskWrapper>() {
         @Override
         public boolean apply(@Nullable CompletionTaskWrapper input) {
           if (input.getParticipantTask() != null) {
             return KeyWrapper.toKey(input.getParticipantTask().participant).equals(participantKey);
           } else {
             return false;
           }
         }
       };
     }
 
     private interface CompletionTask {
       @Nullable
       public CompletionTaskWrapper processPendingTask(Event event);
     }
 
     /*
      * This class works around the objectify limitation that embedded classes can not be
      * polymorphic.
      */
     @Data
     @Embed
     @NoArgsConstructor
     private static class CompletionTaskWrapper implements CompletionTask {
       // Instantiate each object to workaround objectify serialization bug (issue #127).
       private ParticipantCompletionTask participantTask = new ParticipantCompletionTask();
       private OrganizationCompletionTask organizationTask = new OrganizationCompletionTask();
 
       public CompletionTaskWrapper(ParticipantCompletionTask participantTask) {
         this.participantTask = participantTask;
       }
 
       public CompletionTaskWrapper(OrganizationCompletionTask organizationTask) {
         this.organizationTask = organizationTask;
       }
 
       @Override
       public CompletionTaskWrapper processPendingTask(Event event) {
         if (getParticipantTask() != null) {
           return participantTask.processPendingTask(event);
         } else {
           return organizationTask.processPendingTask(event);
         }
       }
 
       public ParticipantCompletionTask getParticipantTask() {
         return participantTask.isNull() ? null : participantTask;
       }
 
       public OrganizationCompletionTask getOrganizationTask() {
         return organizationTask.isNull() ? null : organizationTask;
       }
     }
 
     @Data
     @Embed
     @NoArgsConstructor
     private static class ParticipantCompletionTask implements CompletionTask {
 
       private NullableKeyWrapper<User> participant = NullableKeyWrapper.create();
       private int karmaPointsAssigned;
 
       public ParticipantCompletionTask(ParticipantCompletionTask completedTask) {
         participant = completedTask.participant;
         karmaPointsAssigned = completedTask.karmaPointsAssigned;
       }
 
       public ParticipantCompletionTask(Key<User> participantKey, int karmaPointsAssigned) {
         this.participant = NullableKeyWrapper.create(participantKey);
         this.karmaPointsAssigned = karmaPointsAssigned;
       }
 
       public boolean isNull() {
         return participant.isNull();
       }
 
       @Override
       public CompletionTaskWrapper processPendingTask(Event event) {
         Key<User> participantKey = KeyWrapper.toKey(participant);
         User participant = BaseDao.load(participantKey);
         if (participant == null) {
           // Nothing to do. User no longer exists.
           return null;
         }
         // Delete the previously assigned karma points.
         participant.setKarmaPoints(participant.getKarmaPoints() - karmaPointsAssigned);
         // Assign the new karma points if the participant is still part of the event.
         CompletionTaskWrapper completedTask = null;
         EventParticipant eventParticipant = event.tryFindParticipant(participantKey);
         if ((eventParticipant != null) &&
             ((eventParticipant.getType() == ParticipantType.ORGANIZER) ||
              (eventParticipant.getType() == ParticipantType.REGISTERED))) {
           participant.setKarmaPoints(participant.getKarmaPoints() + event.karmaPoints);
           completedTask = new CompletionTaskWrapper(
             new ParticipantCompletionTask(participantKey, event.karmaPoints));
         }
         BaseDao.partialUpdate(participant);
         return completedTask;
       }
 
     }
 
     @Data
     @Embed
     @NoArgsConstructor
     private static class OrganizationCompletionTask implements CompletionTask {
 
       private NullableKeyWrapper<Organization> organization = NullableKeyWrapper.create();
       private int karmaPointsAssigned;
 
       public OrganizationCompletionTask(Key<Organization> orgKey) {
         this(orgKey, 0);
       }
 
       private OrganizationCompletionTask(Key<Organization> orgKey, int karmaPointsAssigned) {
         this.organization = NullableKeyWrapper.create(orgKey);
         this.karmaPointsAssigned = karmaPointsAssigned;
       }
 
       public boolean isNull() {
         return organization.isNull();
       }
 
       @Override
       public CompletionTaskWrapper processPendingTask(Event event) {
         Key<Organization> orgKey = KeyWrapper.toKey(organization);
         Organization org = BaseDao.load(orgKey);
         if (org == null) {
           // Nothing to do. Org no longer exists.
           return null;
         }
         org.setKarmaPoints(org.getKarmaPoints() + event.karmaPoints);
         BaseDao.partialUpdate(org);
         return new CompletionTaskWrapper(new OrganizationCompletionTask(orgKey, event.karmaPoints));
       }
     }
   }
 
   public static void processEventCompletionTasks(Key<Event> eventKey) {
     ProcessEventCompletionTasksTxn completionTasksTxn;
     do {
       completionTasksTxn = new ProcessEventCompletionTasksTxn(eventKey);
       ofy().transact(completionTasksTxn);
     } while (completionTasksTxn.isWorkPending());
   }
 
   @Data
   @EqualsAndHashCode(callSuper=false)
   private static class ProcessEventCompletionTasksTxn extends VoidWork {
     private final Key<Event> eventKey;
     private boolean workPending;
 
     public void vrun() {
       Event event = BaseDao.load(eventKey);
       if ((event == null) || event.completionProcessed) {
         return;
       }
       event.processCompletionTasks();
       workPending = !event.completionProcessed;
       BaseDao.partialUpdate(event);
     }
   }
 }
