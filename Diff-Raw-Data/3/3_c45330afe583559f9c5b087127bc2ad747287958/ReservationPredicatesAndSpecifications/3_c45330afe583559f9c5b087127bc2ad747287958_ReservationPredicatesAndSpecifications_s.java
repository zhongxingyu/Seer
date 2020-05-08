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
 
 import static org.springframework.data.jpa.domain.Specifications.where;
 
 import java.util.List;
 
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.JoinType;
 import javax.persistence.criteria.Path;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import nl.surfnet.bod.domain.ConnectionV1_;
 import nl.surfnet.bod.domain.ConnectionV2_;
 import nl.surfnet.bod.domain.EnniPort;
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.PhysicalResourceGroup_;
 import nl.surfnet.bod.domain.ProtectionType;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.domain.ReservationEndPoint_;
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.domain.Reservation_;
 import nl.surfnet.bod.domain.UniPort_;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.domain.VirtualPort_;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.domain.VirtualResourceGroup_;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.view.ReservationFilterView;
 
 import org.joda.time.DateTime;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.data.jpa.domain.Specifications;
 
 public final class ReservationPredicatesAndSpecifications {
 
   private ReservationPredicatesAndSpecifications() {
   }
 
   static Specification<Reservation> forCurrentUser(final RichUserDetails user) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.and(root.get(Reservation_.virtualResourceGroup).get(VirtualResourceGroup_.adminGroup).in(
             user.getUserGroupIds()));
       }
     };
   }
 
   static Specification<Reservation> forManager(final RichUserDetails manager) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
 
         Long prgId = manager.getSelectedRole().getPhysicalResourceGroupId().get();
         return cb.and(cb.or(cb.equal(root.get(Reservation_.sourcePort).get(ReservationEndPoint_.virtualPort).get(VirtualPort_.physicalPort).get(
             UniPort_.physicalResourceGroup).get(PhysicalResourceGroup_.id), prgId), cb.equal(root.get(
             Reservation_.destinationPort).get(ReservationEndPoint_.virtualPort).get(VirtualPort_.physicalPort).get(UniPort_.physicalResourceGroup).get(
             PhysicalResourceGroup_.id), prgId)));
       }
     };
   }
 
   static Specification<Reservation> forStatus(final ReservationStatus... states) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.and(root.get(Reservation_.status).in((Object[]) states));
       }
     };
   }
 
   static Specification<Reservation> forVirtualResourceGroup(final VirtualResourceGroup vrg) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.equal(root.get(Reservation_.virtualResourceGroup), vrg);
       }
 
     };
   }
 
   public static Specification<Reservation> specActiveByVirtualPorts(final List<VirtualPort> virtualPorts) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.and(
             cb.or(
                 root.get(Reservation_.sourcePort).get(ReservationEndPoint_.virtualPort).in(virtualPorts),
                 root.get(Reservation_.destinationPort).get(ReservationEndPoint_.virtualPort).in(virtualPorts)),
             root.get(Reservation_.status).in(ReservationStatus.TRANSITION_STATES));
       }
     };
   }
 
   static Specification<Reservation> specActiveReservations() {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return root.get(Reservation_.status).in(ReservationStatus.TRANSITION_STATES);
       }
     };
   }
 
   static Specification<Reservation> specByPhysicalPort(final PhysicalPort port) {
     if (port instanceof UniPort) {
       return new Specification<Reservation>() {
         @Override
         public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
           return cb.or(cb.equal(root.get(Reservation_.sourcePort).get(ReservationEndPoint_.virtualPort).get(VirtualPort_.physicalPort), port), cb.equal(root
               .get(Reservation_.destinationPort).get(ReservationEndPoint_.virtualPort).get(VirtualPort_.physicalPort), port));
         }
       };

     } else if (port instanceof EnniPort) {
       return new Specification<Reservation>() {
         @Override
         public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
           return cb.or(
             cb.equal(root.get(Reservation_.sourcePort).get(ReservationEndPoint_.enniPort), port),
             cb.equal(root.get(Reservation_.destinationPort).get(ReservationEndPoint_.enniPort), port));
         }
       };
     }
     else {
       throw new IllegalArgumentException("Don't know how to handle physical port of type: " + port.getClass());
     }
   }
 
   static Specification<Reservation> specByVirtualPort(final VirtualPort port) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.or(cb.equal(root.get(Reservation_.sourcePort).get(ReservationEndPoint_.virtualPort), port), cb.equal(
             root.get(Reservation_.destinationPort).get(ReservationEndPoint_.virtualPort), port));
       }
     };
   }
 
   static Specification<Reservation> specByManager(final RichUserDetails user) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         Long prgId = user.getSelectedRole().getPhysicalResourceGroupId().get();
         return cb.and(cb.or(cb.equal(root.get(Reservation_.sourcePort).get(ReservationEndPoint_.virtualPort).get(VirtualPort_.physicalPort).get(
             UniPort_.physicalResourceGroup).get(PhysicalResourceGroup_.id), prgId), cb.equal(root.get(
             Reservation_.destinationPort).get(ReservationEndPoint_.virtualPort).get(VirtualPort_.physicalPort).get(UniPort_.physicalResourceGroup).get(
             PhysicalResourceGroup_.id), prgId)));
       }
     };
   }
 
   static Specification<Reservation> specFilteredReservations(final ReservationFilterView filter) {
     Specification<Reservation> filterSpecOnStart = new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.between(root.get(Reservation_.startDateTime), filter.getStart(), filter.getEnd());
       }
     };
 
     Specification<Reservation> filterSpecOnEnd = new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.or(cb.isNull(root.get(Reservation_.endDateTime)), cb.between(root.get(Reservation_.endDateTime),
             filter.getStart(), filter.getEnd()));
       }
     };
 
     // Filter on states in filter
     Specification<Reservation> specification = forStatus(filter.getStates());
     if (filter.isFilterOnReservationEndOnly()) {
       specification = where(specification).and(filterSpecOnEnd);
     } else if (!filter.isFilterOnStatusOnly()) {
       specification = where(specification).and(where(filterSpecOnStart).or(filterSpecOnEnd));
     }
 
     return specification;
   }
 
   static Specification<Reservation> specFilteredReservationsForManager(ReservationFilterView filter, RichUserDetails manager) {
     return Specifications.where(specFilteredReservations(filter)).and(forManager(manager));
   }
 
   static Specification<Reservation> specFilteredReservationsForUser(ReservationFilterView filter, RichUserDetails user) {
     return Specifications.where(specFilteredReservations(filter)).and(forCurrentUser(user));
   }
 
   static Specification<Reservation> specFilteredReservationsForVirtualResourceGroup(ReservationFilterView filter, VirtualResourceGroup vrg) {
     return Specifications.where(specFilteredReservations(filter)).and(forVirtualResourceGroup(vrg));
   }
 
   static Specification<Reservation> specReservationByProtectionTypeInIds(final List<Long> reservationIds, final ProtectionType protectionType) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         Predicate protectionTypeIs = cb.equal(root.get(Reservation_.protectionType), protectionType);
         Predicate reservationIdIn = root.get(Reservation_.id).in(reservationIds);
         return cb.and(protectionTypeIs, reservationIdIn);
       }
 
     };
   }
 
   /**
    * Specification to find {@link Reservation}s which have started in or before
    * the given period and which end in or after the given period.
    *
    * @param start
    *          {@link DateTime} start of the period
    * @param end
    *          {@link DateTime} end of the period
    * @return Specification<Reservation>
    */
   public static Specification<Reservation> specReservationStartBeforeAndEndInOrAfter(final DateTime start, final DateTime end) {
     Specification<Reservation> spec = new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
 
         Predicate startInOrBeforePeriod = cb.lessThanOrEqualTo(root.get(Reservation_.startDateTime), end);
 
         // Infinite reservations have no endDate
         Predicate endInOrAfterPeriod = cb.or(cb.isNull(root.get(Reservation_.endDateTime)), cb
             .greaterThanOrEqualTo(root.get(Reservation_.endDateTime), start));
 
         return cb.and(startInOrBeforePeriod, endInOrAfterPeriod);
       }
     };
 
     return spec;
   }
 
   static Specification<Reservation> specReservationsThatAreTimedOutAndTransitionally(final DateTime startDateTime) {
     return new Specification<Reservation>() {
       @Override
       public javax.persistence.criteria.Predicate toPredicate(Root<Reservation> reservation, CriteriaQuery<?> query,
           CriteriaBuilder cb) {
         // the start time has past
         return cb.and(cb.lessThan(reservation.get(Reservation_.startDateTime), startDateTime),
         // end time has past
             cb.lessThan(reservation.get(Reservation_.endDateTime), DateTime.now()),
             // but reservation is still transitional
             reservation.get(Reservation_.status).in(ReservationStatus.TRANSITION_STATES));
       }
     };
   }
 
   static Specification<Reservation> specReservationsThatCouldStart(final DateTime startDateTime) {
     return new Specification<Reservation>() {
       @Override
       public javax.persistence.criteria.Predicate toPredicate(Root<Reservation> reservation, CriteriaQuery<?> query,
           CriteriaBuilder cb) {
 
         return cb.and(cb.lessThanOrEqualTo(reservation.get(Reservation_.startDateTime), startDateTime), reservation
             .get(Reservation_.status).in(ReservationStatus.COULD_START_STATES));
       }
     };
   }
 
   static Specification<Reservation> specReservationWithConnection(final List<Long> reservationIds) {
     return new Specification<Reservation>() {
       @Override
       public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         Predicate reservationIdIn = root.get(Reservation_.id).in(reservationIds);
 
         Path<?> v1id = root.join(Reservation_.connectionV1, JoinType.LEFT).get(ConnectionV1_.id);
         Path<?> v2id = root.join(Reservation_.connectionV2, JoinType.LEFT).get(ConnectionV2_.id);
         return cb.and(reservationIdIn, cb.or(v1id.isNotNull(), v2id.isNotNull()));
       }
     };
   }
 }
