 package de.flower.rmt.service;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.mysema.query.types.expr.BooleanExpression;
 import de.flower.common.util.Check;
 import de.flower.rmt.model.db.entity.*;
 import de.flower.rmt.model.db.entity.event.Event;
 import de.flower.rmt.model.db.entity.event.Event_;
 import de.flower.rmt.model.db.type.activity.EmailSentMessage;
 import de.flower.rmt.model.db.type.activity.EventUpdateMessage;
 import de.flower.rmt.model.db.type.activity.InvitationUpdateMessage;
 import de.flower.rmt.repository.IActivityRepo;
 import org.apache.commons.lang3.ObjectUtils;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Nullable;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author flowerrrr
  */
 @Service
 @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
 public class ActivityManager extends AbstractService implements IActivityManager {
 
     @Autowired
     private IActivityRepo activityRepo;
 
     @Autowired
     private IInvitationManager invitationManager;
 
     @Autowired
     private IEventManager eventManager;
 
     @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
     public void save(Activity entity) {
         validate(entity);
         activityRepo.save(entity);
     }
 
     private Activity newInstance() {
         Activity entity = new Activity(getClub());
         entity.setUser(securityService.getUser());
         entity.setDate(new Date());
         return entity;
     }
 
     @Override
     public Activity loadById(final Long id) {
         return activityRepo.findOne(id);
     }
 
     @Override
     @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
     public void onCreateOrUpdateEvent(Event event, EventUpdateMessage.Type type) {
         event = eventManager.loadById(event.getId(), Event_.team);
         Activity entity = newInstance();
         EventUpdateMessage message = new EventUpdateMessage(event);
         message.setType(type);
         message.setTeamName(event.getTeam().getName());
         message.setManagerName(entity.getUser().getFullname());
         entity.setMessage(message);
         save(entity);
     }
 
 
     @Override
     @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
     public void onInvitationMailSent(final Event event) {
         Activity entity = newInstance();
         EmailSentMessage message = new EmailSentMessage(event);
         message.setManagerName(entity.getUser().getFullname());
         entity.setMessage(message);
         save(entity);
     }
 
     @Override
     @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
     public void onInvitationUpdated(final Invitation invitation) {
         Invitation origInvitation = invitationManager.loadById(invitation.getId(), Invitation_.event, Invitation_.user);
         Check.isTrue(invitation != origInvitation);
         Activity entity = newInstance();
         InvitationUpdateMessage message = new InvitationUpdateMessage(origInvitation.getEvent());
         message.setUserName(origInvitation.getName());
         boolean changed = false;

        if (origInvitation.getEvent().isCanceled()) {
            // RMT-676: avoid useless 'user xy is not attending event ...' messages.
            return;
        }

         if (!entity.getUser().equals(origInvitation.getUser())) {
             // manager has updated invitation
             message.setManagerName(entity.getUser().getFullname());
             changed = true;
         }
         // do not track when comments are removed. what to display then?
         if (invitation.getComment() != null && ObjectUtils.notEqual(invitation.getComment(), origInvitation.getComment())) {
             message.setComment(invitation.getComment());
             changed = true;
         }
         if (invitation.getManagerComment() != null && ObjectUtils.notEqual(invitation.getManagerComment(), origInvitation.getManagerComment())) {
             message.setManagerComment(invitation.getManagerComment());
             changed = true;
         }
         if (ObjectUtils.notEqual(invitation.getStatus(), origInvitation.getStatus())) {
             message.setStatus(invitation.getStatus());
             changed = true;
         }
         entity.setMessage(message);
 
         if (changed) {
             removeDuplicates(entity, message);
             save(entity);
         }
     }
 
     private void removeDuplicates(final Activity entity, final InvitationUpdateMessage message) {
         // reading the activity table is always a bit unsafe due to the de-serialization errors
         // that might occur when the message classes are changed. so better try/catch.
         try {
             Collection<Activity> list = findDuplicates(entity, message);
             if (!list.isEmpty()) {
                 log.info("Removing duplicates {}", list);
                 activityRepo.delete(list);
             }
         } catch (Exception e) {
             log.error("Error in removeDuplicates: " + e.getMessage(), e);
         }
     }
 
     /**
      * If user updates his comment twice, e.g. to correct a wrong spelling both submits
      * will be listed in activity feed. that's not nice.
      * <p/>
      * <pre>
      * Duplicates are identified:
      * - by user
      * - message type
      * - event
      * - username (as Activity.user might be the manager updating invitations on behalf of the users).
      *
      * </pre>
      */
     private Collection<Activity> findDuplicates(final Activity entity, final InvitationUpdateMessage message) {
         BooleanExpression isSameUser = QActivity.activity.user.eq(entity.getUser());
         BooleanExpression isInsideOneHour = QActivity.activity.date.after(new DateTime().minusHours(1).toDate());
         Collection<Activity> list = activityRepo.findAll(isSameUser.and(isInsideOneHour));
         list = Collections2.filter(list, new Predicate<Activity>() {
             @Override
             public boolean apply(@Nullable final Activity activity) {
                 Serializable msg = activity.getMessage();
                 if (msg instanceof InvitationUpdateMessage) {
                     InvitationUpdateMessage ium = (InvitationUpdateMessage) msg;
                     // event and user must match. event alone is not ok cause manager might update
                     // several invitations.
                     if (ium.getEventId().equals(message.getEventId())
                             && ium.getUserName().equals(message.getUserName())) {
                         return true;
                     }
                 }
                 return false;
             }
         });
         if (list.size() > 1) {
             log.warn("Error in findDuplicates(): There should only be one duplicate.");
         }
         return list;
     }
 
     @Override
     public List<Activity> findLastN(final int page, final int size) {
         return activityRepo.findAll(new PageRequest(page, size, Sort.Direction.DESC, Activity_.date.getName())).getContent();
     }
 }
