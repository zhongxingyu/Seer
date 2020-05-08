 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.service;
 
 import static com.google.common.base.Preconditions.checkState;
 import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
 import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
 import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
import javax.persistence.criteria.*;
 
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.repo.ReservationRepo;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.joda.time.LocalTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class ReservationService {
 
   private final Logger log = LoggerFactory.getLogger(getClass());
 
   @Autowired
   private ReservationRepo reservationRepo;
 
   @Autowired
   private NbiService nbiService;
 
   @Autowired
   private ReservationEventPublisher reservationEventPublisher;
 
   /**
    * Reserves a reservation using the {@link NbiService} asynchronously.
   *
    * @param reservation
    */
  // FIXME AvD
//  @Async
   public void reserve(Reservation reservation) throws ReservationFailedException {
     checkState(reservation.getSourcePort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
     checkState(reservation.getDestinationPort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
 
     reservation.setStatus(ReservationStatus.SUBMITTED);
     final ReservationStatus oldStatus = reservation.getStatus();
     try {
       // First persist in db
       reservation = update(reservation);
 
       final String reservationId = nbiService.createReservation(reservation);
 
       // Was there an error?
       if (reservationId == null) {
         reservationRepo.save(reservation);
 
         throw new ReservationFailedException("Unable to create reservation: " + reservation);
       }
 
       reservation.setReservationId(reservationId);
       reservation.setStatus(getStatus(reservation));
       reservation = reservationRepo.save(reservation);
 
       log.info("Reservation created: {}", reservation);
     }
     finally {
       ReservationStatusChangeEvent createEvent = new ReservationStatusChangeEvent(oldStatus, reservation);
       reservationEventPublisher.notifyListeners(createEvent);
     }
   }
 
   public Reservation find(Long id) {
     return reservationRepo.findOne(id);
   }
 
   public Collection<Reservation> findEntries(int firstResult, int maxResults) {
     final RichUserDetails user = Security.getUserDetails();
 
     if (user.getUserGroups().isEmpty()) {
       return Collections.emptyList();
     }
 
     return reservationRepo.findAll(forCurrentUser(user), new PageRequest(firstResult / maxResults, maxResults))
         .getContent();
   }
 
   public long count() {
     final RichUserDetails user = Security.getUserDetails();
 
     if (user.getUserGroups().isEmpty()) {
       return 0;
     }
 
     return reservationRepo.count(forCurrentUser(user));
   }
 
   private Specification<Reservation> forCurrentUser(final RichUserDetails user) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.and(root.get("virtualResourceGroup").get("surfConextGroupName").in(user.getUserGroupIds()));
       }
     };
   }
 
   public Reservation update(Reservation reservation) {
     checkState(reservation.getSourcePort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
     checkState(reservation.getDestinationPort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
 
     log.debug("Updating reservation: {}", reservation.getReservationId());
     return reservationRepo.save(reservation);
   }
 
   public void cancel(Reservation reservation) {
     if (reservation.getStatus() == RUNNING || reservation.getStatus() == SCHEDULED) {
       reservation.setStatus(CANCELLED);
       nbiService.cancelReservation(reservation.getReservationId());
       reservationRepo.save(reservation);
 
     }
   }
 
   Specification<Reservation> specReservationsToPoll(final LocalDateTime startOrEndDateTime) {
     return new Specification<Reservation>() {
       @Override
       public javax.persistence.criteria.Predicate toPredicate(Root<Reservation> reservation, CriteriaQuery<?> query,
           CriteriaBuilder cb) {
 
         Expression<LocalDate> startDateExpr = reservation.get("startDate");
         Expression<LocalTime> startTimeExpr = reservation.get("startTime");
         Expression<LocalDate> endDateExpr = reservation.get("endDate");
         Expression<LocalTime> endTimeExpr = reservation.get("endTime");
         Expression<ReservationStatus> status = reservation.get("status");
 
         return cb.and(
             cb.or(
                 cb.and(cb.equal(startTimeExpr, startOrEndDateTime.toLocalTime()),
                     cb.equal(startDateExpr, startOrEndDateTime.toLocalDate())),
                 cb.and(cb.equal(endTimeExpr, startOrEndDateTime.toLocalTime()),
                     cb.equal(endDateExpr, startOrEndDateTime.toLocalDate()))),
             cb.and(status.in(ReservationStatus.TRANSITION_STATES)));
       }
     };
   }
 
   public ReservationStatus getStatus(Reservation reservation) {
     return nbiService.getReservationStatus(reservation.getReservationId());
   }
 
   /**
    * Finds all reservations which start or ends on the given dateTime and have a
    * status which can still change its status.
    *
    * @param dateTime
    *          {@link LocalDateTime} to search for
    * @return list of found Reservations
    */
   public List<Reservation> findReservationsToPoll(LocalDateTime dateTime) {
     return reservationRepo.findAll(specReservationsToPoll(dateTime));
   }
 
 }
