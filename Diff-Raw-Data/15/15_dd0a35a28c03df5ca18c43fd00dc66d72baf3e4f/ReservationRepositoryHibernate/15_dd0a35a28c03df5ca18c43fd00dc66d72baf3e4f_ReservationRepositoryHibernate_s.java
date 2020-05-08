 package net.mklew.hotelms.persistance;
 
 import net.mklew.hotelms.domain.booking.reservation.Reservation;
 import net.mklew.hotelms.domain.booking.reservation.ReservationRepository;
 import net.mklew.hotelms.persistance.hibernate.configuration.HibernateSessionFactory;
 import org.hibernate.Session;
 import org.joda.time.DateTime;
 
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 12/25/12
  *        time 9:16 PM
  */
 public class ReservationRepositoryHibernate extends HibernateRepository implements ReservationRepository
 {
     private static final String FIND_ALL_RESERVATIONS_FOR_ROOM_AROUND_DATES_QUERY = "select reservation from " +
             "Reservation " +
            "reservation where checkIn between :start and :end or checkOut between :start and :end";
 
     public ReservationRepositoryHibernate(HibernateSessionFactory hibernateSessionFactory)
     {
         super(hibernateSessionFactory);
     }
 
     @Override
     public Collection<Reservation> findAllReservationsAroundDates(DateTime checkIn, DateTime checkOut)
     {
         final Session session = getCurrentSession();
         @SuppressWarnings("unchecked")
         List<Reservation> reservations = (List<Reservation>) session.createQuery
                (FIND_ALL_RESERVATIONS_FOR_ROOM_AROUND_DATES_QUERY).setParameter("start",
                checkIn).setParameter("end", checkOut).list();
         return reservations;
     }
 
     @Override
     public void bookNewReservation(Reservation reservation)
     {
         final Session session = getCurrentSession();
         session.save(reservation);
     }
 }
