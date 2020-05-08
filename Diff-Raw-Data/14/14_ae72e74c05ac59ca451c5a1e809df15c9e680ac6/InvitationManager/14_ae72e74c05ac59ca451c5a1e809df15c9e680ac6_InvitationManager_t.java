 package de.flower.rmt.service;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.mysema.query.types.EntityPath;
 import com.mysema.query.types.expr.BooleanExpression;
 import de.flower.common.util.Check;
 import de.flower.rmt.model.db.entity.*;
 import de.flower.rmt.model.db.entity.event.Event;
 import de.flower.rmt.model.db.type.RSVPStatus;
 import de.flower.rmt.repository.IInvitationRepo;
 import de.flower.rmt.service.mail.INotificationService;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Nullable;
 import javax.mail.internet.InternetAddress;
 import javax.persistence.metamodel.Attribute;
 import java.util.*;
 
 import static de.flower.rmt.repository.Specs.*;
 import static org.springframework.data.jpa.domain.Specifications.where;
 
 /**
  * @author flowerrrr
  */
 @Service
 @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
 public class InvitationManager extends AbstractService implements IInvitationManager {
 
     @Autowired
     private IInvitationRepo invitationRepo;
 
     @Autowired
     private IPlayerManager playerManager;
 
     @Autowired
     private IEventManager eventManager;
 
     @Autowired
     private IUserManager userManager;
 
     @Autowired
     private IActivityManager activityManager;
 
     @Autowired
     private INotificationService notificationService;
 
     @Override
     public Invitation newInstance(final Event event, User user) {
         Check.notNull(event);
         Check.notNull(user);
         return new Invitation(event, user);
     }
 
     @Override
     public Invitation newInstance(final Event event, String guestName) {
         Check.notNull(event);
         Check.notBlank(guestName);
         return new Invitation(event, guestName);
     }
 
     @Override
     public Invitation loadById(Long id, final Attribute... attributes) {
         Specification fetch = fetch(attributes);
         Invitation entity = invitationRepo.findOne(where(eq(Invitation_.id, id)).and(fetch));
         Check.notNull(entity, "No invitation found");
         return entity;
     }
 
     @Override
     public List<Invitation> findAllByEvent(final Event event, final Attribute... attributes) {
         Specification fetch = fetch(attributes);
         return invitationRepo.findAll(where(eq(Invitation_.event, event)).and(fetch));
     }
 
     @Override
     public List<Invitation> findAllByEventSortedByName(final Event event) {
         List<Invitation> list = findAllByEvent(event, Invitation_.user);
         // use in-memory sorting cause field username is derived and would required complicated sql-query to sort after.
         return sortByName(list);
     }
 
     // TODO (flowerrrr - 20.04.12) not a nice method, neither name nor implementation. refactor!
     @Override
     public List<Invitation> findAllForNotificationByEventSortedByName(final Event event) {
         List<Invitation> list = findAllByEventSortedByName(event);
 
         // filter out those that do not want to receive email notifications
         Iterable<Invitation> filtered = Iterables.filter(list, new Predicate<Invitation>() {
             private List<Player> players;
             {
                 players = playerManager.findAllByTeam(event.getTeam());
             }
             @Override
             public boolean apply(@Nullable final Invitation invitation) {
                 if (!invitation.hasEmail()) {
                     return false;
                 } else {
                     // check if player has opted out of email notifications
                     for (Player player : players) {
                         if (player.getUser().equals(invitation.getUser())) {
                             return player.isNotification();
                         }
                     }
                     // this happens for invitees who are not team-members (invitees that were later added).
                     return true;
                 }
             }
         });
         return ImmutableList.copyOf(filtered);
     }
 
     @Override
     public List<InternetAddress> getAddressesForfAllInvitees(final Event event) {
         List<Invitation> list = findAllByEventSortedByName(event);
         // convert to list of internet addresses
         List<InternetAddress[]> internetAddresses = de.flower.common.util.Collections.convert(list,
                 new de.flower.common.util.Collections.IElementConverter<Invitation, InternetAddress[]>(){
             @Override
             public InternetAddress[] convert(final Invitation element) {
                 if (element.hasEmail()) {
                     return element.getInternetAddresses();
                 } else {
                     return null;
                 }
             }
         });
         return de.flower.common.util.Collections.flattenArray(internetAddresses);
     }
 
     @Override
     public List<Invitation> findAllByEventAndStatus(Event event, RSVPStatus rsvpStatus, final Attribute... attributes) {
         List<Invitation> list = invitationRepo.findAll(where(eq(Invitation_.event, event))
                 .and(eq(Invitation_.status, rsvpStatus))
                 .and(asc(Invitation_.date))
                 .and(fetch(attributes)));
         if (rsvpStatus == RSVPStatus.NORESPONSE) {
             // no response means there is no date set yet. so sort by name instead
             sortByName(list);
         }
         return list;
     }
 
     @Override
     public Long numByEventAndStatus(final Event event, final RSVPStatus rsvpStatus) {
         return invitationRepo.numByEventAndStatus(event, rsvpStatus);
     }
 
     @Override
     public List<Invitation> findAllForNoResponseReminder(final Event event, final int hoursAfterInvitationSent) {
         DateTime now = new DateTime();
         BooleanExpression isEvent = QInvitation.invitation.event.eq(event);
         // no invitation sent yet -> cannot blame user for not having responded.
         BooleanExpression isInvitationSent = QInvitation.invitation.invitationSent.eq(true);
         BooleanExpression isNoResponse = QInvitation.invitation.status.eq(RSVPStatus.NORESPONSE);
         BooleanExpression isHoursAfterInvitationSent = QInvitation.invitation.invitationSentDate.before(now.minusHours(hoursAfterInvitationSent).toDate());
         BooleanExpression isNotReminderSent = QInvitation.invitation.noResponseReminderSent.eq(false);
         return invitationRepo.findAll(isEvent.and(isInvitationSent).and(isNoResponse).and(isHoursAfterInvitationSent).and(isNotReminderSent), QInvitation.invitation.user);
     }
 
     @Override
     public List<Invitation> findAllForUnsureReminder(final Event event) {
         BooleanExpression isEvent = QInvitation.invitation.event.eq(event);
         BooleanExpression isUnsure = QInvitation.invitation.status.eq(RSVPStatus.UNSURE);
         BooleanExpression isNotReminderSent = QInvitation.invitation.unsureReminderSent.eq(false);
         return invitationRepo.findAll(isEvent.and(isUnsure).and(isNotReminderSent), QInvitation.invitation.user);
     }
 
     /*
         @Override
         public List<Invitation> findAlByEmails(final Event event, final List<String> addressList) {
             List<Invitation> list = findAllByEvent(event);
             Iterator<Invitation> iterator = list.iterator();
             while (iterator.hasNext()) {
                 Invitation invitation = iterator.next();
                 if (invitation.getEmail() != null) {
                     if (!addressList.contains(invitation.getEmail())) {
                          iterator.remove();
                     }
                 } else {
                     // guest player
                     iterator.remove();
                 }
             }
             return list;
         }
     */
 
     @Override
     public Invitation loadByEventAndUser(Event event, User user) {
         Invitation invitation = findByEventAndUser(event, user);
         Check.notNull(invitation, "No invitation found");
         return invitation;
     }
 
     @Override
     public Invitation findByEventAndUser(Event event, User user, EntityPath<?>... attributes) {
         BooleanExpression isEvent = QInvitation.invitation.event.eq(event);
         BooleanExpression isUser = QInvitation.invitation.user.eq(user);
         Invitation invitation = invitationRepo.findOne(isEvent.and(isUser), attributes);
         return invitation;
     }
 
     @Override
     @Transactional(readOnly = false)
     public void save(final Invitation invitation) {
         validate(invitation);
         boolean isNew = invitation.isNew();
         boolean isNotifyManager = false;
         Invitation origInvitation = null;
         if (!isNew) {
             // depending on the caller of this method the invitation might be detached or attached (when called
             // by ResponseManager. If object is attached it is not possible to check for modifications against the
             // saved state in database as em.findOne() will return the attached version from session cache.
             invitationRepo.detach(invitation);
             // in case the status changes update the date of response.
             // used for early maybe-responder who later switch their status.
             // after status update the rank of an invitation is reset as if he has
             // just responded the first time.
             origInvitation = invitationRepo.findOne(invitation.getId());
             Check.isTrue(invitation != origInvitation);
             if (origInvitation.getStatus() != invitation.getStatus()) {
                 invitation.setDate(new Date());
                 if (origInvitation.getStatus() == RSVPStatus.ACCEPTED) {
                     // if status changes from accepted to any other state -> notify manager
                     // but only if it's not the manager himself who changes the status.
                    // but only if event is not cancelled -> all users will switch from ACCEPTED to DECLINED.
                    if (!securityService.getUser().isManager() && !origInvitation.getEvent().isCanceled()) {
                         isNotifyManager = true;
                     }
                 }
             }
             if (invitation.getDate() == null) {
                 invitation.setDate(new Date());
             }
             // invitations are created when event is created. that's not interesting to track. we'd only
             // like to know when invitation is updated.
             activityManager.onInvitationUpdated(invitation);
         }
         invitationRepo.save(invitation);
         if (isNotifyManager) {
             try {
                 notificationService.sendStatusChangedMessage(invitation);
             } catch (Exception e) {
                 log.error("Could not send notification.", e);
             }
         }
     }
 
     @Override
     @Transactional(readOnly = false)
     public void delete(final Long id) {
         invitationRepo.delete(id);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void markInvitationSent(final Event event, final List<String> addressList, Date date) {
         invitationRepo.markInvitationSent(event, addressList, date == null ? new Date() : date);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void markNoResponseReminderSent(final List<Invitation> invitations) {
         invitationRepo.markNoResponseReminderSent(invitations, new Date());
     }
 
     @Override
     public void markUnsureReminderSent(final List<Invitation> invitations) {
         invitationRepo.markUnsureReminderSent(invitations, new Date());
     }
 
     @Override
     @Transactional(readOnly = false)
     public void addUsers(final Event entity, final Collection<Long> userIds) {
         for (Long userId : userIds) {
             User user = userManager.loadById(userId);
             Invitation invitation = newInstance(entity, user);
             save(invitation);
         }
     }
 
     @Override
     @Transactional(readOnly = false)
     public void addGuestPlayer(final Event entity, final String guestName) {
         Invitation invitation = newInstance(entity, guestName);
         save(invitation);
     }
 
     private List<Invitation> sortByName(List<Invitation> list) {
         Collections.sort(list, new Comparator<Invitation>() {
             @Override
             public int compare(final Invitation o1, final Invitation o2) {
                 return o1.getName().compareToIgnoreCase(o2.getName());
             }
         });
         return list;
     }
 }
