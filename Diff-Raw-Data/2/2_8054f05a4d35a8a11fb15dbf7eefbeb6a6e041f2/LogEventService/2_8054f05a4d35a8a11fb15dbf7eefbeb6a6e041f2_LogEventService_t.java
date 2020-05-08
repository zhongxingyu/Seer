 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.service;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.collect.Iterables.toArray;
 import static nl.surfnet.bod.service.LogEventPredicatesAndSpecifications.specLogEventsByAdminGroups;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 import nl.surfnet.bod.domain.BodRole;
 import nl.surfnet.bod.domain.Institute;
 import nl.surfnet.bod.domain.Loggable;
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.domain.VirtualPortRequestLink;
 import nl.surfnet.bod.event.LogEvent;
 import nl.surfnet.bod.event.LogEventType;
 import nl.surfnet.bod.repo.LogEventRepo;
 import nl.surfnet.bod.util.Transition;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class LogEventService extends AbstractFullTextSearchService<LogEvent> {
 
   private static final String SYSTEM_USER = "System";
   private static final Collection<String> PERSISTABLE_LOG_EVENTS = ImmutableList.of(LogEvent
       .getDomainObjectName(Reservation.class), LogEvent.getDomainObjectName(VirtualPort.class), LogEvent
       .getDomainObjectName(PhysicalPort.class), LogEvent.getDomainObjectName(PhysicalResourceGroup.class), LogEvent
       .getDomainObjectName(Institute.class), LogEvent.getDomainObjectName(VirtualPortRequestLink.class));
 
   private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
   @Resource
   private LogEventRepo logEventRepo;
 
   @Resource
   private VirtualResourceGroupService virtualResourceGroupService;
 
   @Resource
   private ReservationEventPublisher reservationEventPublisher;
 
   @PersistenceContext
   private EntityManager entityManager;
 
   public long count() {
     return logEventRepo.count();
   }
 
   public long count(Specification<LogEvent> whereClause) {
     return logEventRepo.count(whereClause);
   }
 
   private long countByAdminGroups(Collection<String> adminGroups) {
     if (adminGroups.isEmpty()) {
       return 0;
     }
 
     return logEventRepo.count(specLogEventsByAdminGroups(adminGroups));
   }
 
   public long countDistinctDomainObjectId(Specification<LogEvent> whereClause) {
     return logEventRepo.countDistinctDomainObjectIdsWithWhereClause(whereClause);
   }
 
   public long countStateChangeFromOldToNewForReservationIdBetween(DateTime start, DateTime end,
       ReservationStatus oldStatus, ReservationStatus newStatus, final Collection<String> adminGroups) {
 
     Specification<LogEvent> whereClause = LogEventPredicatesAndSpecifications
         .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(oldStatus, newStatus, null, start, end,
             adminGroups);
 
     return logEventRepo.countDistinctDomainObjectIdsWithWhereClause(whereClause);
   }
 
   public List<Long> findStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(DateTime start, DateTime end,
       ReservationStatus oldStatus, ReservationStatus newStatus, final Collection<String> adminGroups) {
 
     Specification<LogEvent> whereClause = LogEventPredicatesAndSpecifications
         .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(oldStatus, newStatus, null, start, end,
             adminGroups);
 
     return logEventRepo.findDistinctDomainObjectIdsWithWhereClause(whereClause);
   }
 
   private LogEvent createReservationLogEvent(RichUserDetails user, LogEventType eventType, Reservation reservation,
       ReservationStatus oldStatus, ReservationStatus newStatus) {
 
     String details = getStateChangeMessage(oldStatus, newStatus);
     return createLogEvent(user, eventType, reservation, details, Optional.of(oldStatus), Optional.of(newStatus));
   }
 
   private LogEvent createLogEvent(RichUserDetails user, LogEventType eventType, Loggable domainObject, String details,
       Optional<ReservationStatus> oldStatus, Optional<ReservationStatus> newStatus) {
 
     return new LogEvent(user == null ? SYSTEM_USER : user.getUsername(), domainObject.getAdminGroups(), eventType,
         Optional.fromNullable(domainObject), details, oldStatus, newStatus);
   }
 
   private LogEvent createLogEvent(RichUserDetails user, LogEventType eventType, Loggable domainObject, String details) {
     return createLogEvent(user, eventType, domainObject, details, Optional.<ReservationStatus> absent(), Optional
         .<ReservationStatus> absent());
   }
 
   private List<LogEvent> createLogEvents(RichUserDetails user, LogEventType logEventType, String details,
       Loggable... domainObjects) {
 
     List<LogEvent> logEvents = new ArrayList<>();
     for (int i = 0; i < domainObjects.length; i++) {
       LogEvent logEvent = createLogEvent(user, logEventType, domainObjects[i], details);
       logEvents.add(logEvent);
     }
 
     correlateLogEvents(logEvents);
 
     return logEvents;
   }
 
   private void correlateLogEvents(List<LogEvent> logEvents) {
     if (logEvents.size() > 1) {
       for (int i = 0; i < logEvents.size(); ++i) {
         logEvents.get(i).setCorrelationId((i + 1) + "/" + logEvents.size());
       }
     }
   }
 
   public List<LogEvent> findAll(int firstResult, int maxResults, Sort sort) {
     return logEventRepo.findAll(WebUtils.createPageRequest(firstResult, maxResults, sort)).getContent();
   }
 
   private List<LogEvent> findByAdminGroups(Collection<String> adminGroups, int firstResult, int maxResults, Sort sort) {
     if (adminGroups.isEmpty()) {
       return Collections.emptyList();
     }
 
     return logEventRepo.findAll(specLogEventsByAdminGroups(adminGroups),
         WebUtils.createPageRequest(firstResult, maxResults, sort)).getContent();
   }
 
   public List<LogEvent> findByAdministratorRole(BodRole managerRole, int firstResult, int maxResults, Sort sort) {
     checkArgument(managerRole.isManagerRole());
 
     return findByAdminGroups(Lists.newArrayList(managerRole.getAdminGroup().get()), firstResult, maxResults, sort);
   }
 
   public List<Long> findDistinctDomainObjectIdsWithWhereClause(Specification<LogEvent> whereClause) {
     return logEventRepo.findDistinctDomainObjectIdsWithWhereClause(whereClause);
   }
 
   public List<Long> findReservationIdsCreatedBetweenWithStateInAdminGroups(DateTime start, DateTime end,
       Collection<String> adminGroups, ReservationStatus... state) {
 
     Specification<LogEvent> spec = LogEventPredicatesAndSpecifications
         .specForReservationBetweenForAdminGroupsWithStateIn(null, start, end, adminGroups, state);
 
     return logEventRepo.findDistinctDomainObjectIdsWithWhereClause(spec);
   }
 
   public List<Long> findReservationsIdsCreatedBetweenWithOldStateInAdminGroups(DateTime start, DateTime end,
       ReservationStatus state, Collection<String> adminGroups) {
 
     Specification<LogEvent> spec = LogEventPredicatesAndSpecifications
         .specForReservationBetweenForAdminGroupsWithOldStateIn(Collections.<Long> emptyList(), start, end, adminGroups, state);
 
     return logEventRepo.findDistinctDomainObjectIdsWithWhereClause(spec);
   }
 
   public List<Long> findIdsForManager(BodRole managerRole, Optional<Sort> sort) {
     return logEventRepo.findIdsWithWhereClause(Optional
         .<Specification<LogEvent>> of(specLogEventsByAdminGroups(ImmutableList.of(managerRole.getAdminGroup().get()))),
         sort);
   }
 
   public List<Long> findIdsForUser(RichUserDetails user, Optional<Sort> sort) {
     return logEventRepo.findIdsWithWhereClause(Optional
         .<Specification<LogEvent>> of(specLogEventsByAdminGroups(virtualResourceGroupService
             .determineAdminGroupsForUser(user))), sort);
   }
 
   public List<Long> findAllIds(Sort sort) {
     return logEventRepo.findAllIds(Optional.<Sort> fromNullable(sort));
   }
 
   public LogEvent findLatestStateChangeForReservationIdBeforeInAdminGroups(Long id, DateTime before,
       final Collection<String> adminGroups) {
 
     Specification<LogEvent> whereClause = LogEventPredicatesAndSpecifications
         .specForReservationBeforeInAdminGroupsWithStateIn(Optional.fromNullable(id), before, adminGroups);
 
     Long logEventId = logEventRepo.findMaxIdWithWhereClause(whereClause);
 
     return logEventId == null ? null : logEventRepo.findOne(logEventId);
   }
 
   @Override
   protected EntityManager getEntityManager() {
     return entityManager;
   }
 
   /**
    * Handles the event. Writes it to the given logger. Only events with a
    * domainObject with one a specific type, as determined by
    * {@link #shouldLogEventBePersisted(LogEvent)} are persisted to the
    * {@link LogEventRepo}
    *
    * @param logger
    *          Logger to write to
    *
    * @param logEvent
    *          LogEvent to handle
    */
   @VisibleForTesting
   void handleEvent(Logger log, LogEvent logEvent) {
     log.info("Event: {}", logEvent);
 
     if (shouldLogEventBePersisted(logEvent)) {
       logEventRepo.save(logEvent);
     }
   }
 
   private void handleEvents(List<LogEvent> logEvents) {
     for (LogEvent logEvent : logEvents) {
       handleEvent(logger, logEvent);
     }
   }
 
   public Collection<LogEvent> logCreateEvent(RichUserDetails user, Iterable<? extends Loggable> domainObjects) {
     return logCreateEvent(user, toArray(domainObjects, Loggable.class));
   }
 
   public Collection<LogEvent> logCreateEvent(RichUserDetails user, Loggable... domainObjects) {
     final List<LogEvent> logEvents = createLogEvents(user, LogEventType.CREATE, "", domainObjects);
     handleEvents(logEvents);
     return logEvents;
   }
 
   public Collection<LogEvent> logDeleteEvent(RichUserDetails user, String details,
       Iterable<? extends Loggable> domainObjects) {
     return logDeleteEvent(user, details, toArray(domainObjects, Loggable.class));
   }
 
   public Collection<LogEvent> logDeleteEvent(RichUserDetails user, String details, Loggable... domainObjects) {
     final List<LogEvent> logEvents = createLogEvents(user, LogEventType.DELETE, details, domainObjects);
     handleEvents(logEvents);
     return logEvents;
   }
 
   public List<LogEvent> logUpdateEvent(RichUserDetails user, String details, Loggable... domainObjects) {
     List<LogEvent> logEvents = createLogEvents(user, LogEventType.UPDATE, details, domainObjects);
     handleEvents(logEvents);
     return logEvents;
   }
 
   public List<LogEvent> logUpdateEvent(RichUserDetails user, String details, Iterable<? extends Loggable> domainObjects) {
     return logUpdateEvent(user, details, toArray(domainObjects, Loggable.class));
   }
 
   public void logReservationStatusChangeEvent(RichUserDetails user, Reservation reservation, ReservationStatus oldStatus) {
     if (oldStatus != reservation.getStatus() && !oldStatus.canTransition(reservation.getStatus())) {
      throw new IllegalArgumentException("Illegal status-transition attempt for reservation " + reservation.getReservationId() + ": " + oldStatus + " => " + reservation.getStatus());
     }
     List<LogEvent> logEvents = new ArrayList<>();
     for (Transition<ReservationStatus> transition: oldStatus.transitionPath(reservation.getStatus())) {
       LogEvent logEvent = createReservationLogEvent(user, LogEventType.UPDATE, reservation, transition.getFrom(), transition.getTo());
       logEvents.add(logEvent);
     }
     correlateLogEvents(logEvents);
     handleEvents(logEvents);
     publishReservationStateChangeEvents(reservation, logEvents);
   }
 
   private void publishReservationStateChangeEvents(Reservation reservation, List<LogEvent> logEvents) {
     for (LogEvent logEvent: logEvents) {
       reservationEventPublisher.notifyListeners(new ReservationStatusChangeEvent(reservation, logEvent.getOldReservationStatus(), logEvent.getNewReservationStatus()));
     }
   }
 
   private static String getStateChangeMessage(final ReservationStatus oldStatus, final ReservationStatus newStatus) {
     return String.format("Changed state from [%s] to [%s]", oldStatus, newStatus);
   }
 
   private boolean shouldLogEventBePersisted(LogEvent logEvent) {
     return PERSISTABLE_LOG_EVENTS.contains(logEvent.getDomainObjectClass());
   }
 
   public long countByManagerRole(BodRole selectedRole) {
     return countByAdminGroups(ImmutableList.of(selectedRole.getAdminGroup().get()));
   }
 
   public long countByUser(RichUserDetails user) {
     return countByAdminGroups(virtualResourceGroupService.determineAdminGroupsForUser(user));
   }
 
   public List<LogEvent> findByUser(RichUserDetails userDetails, int firstResult, int maxResults, Sort sort) {
     return findByAdminGroups(virtualResourceGroupService.determineAdminGroupsForUser(userDetails), firstResult,
         maxResults, sort);
   }
 }
