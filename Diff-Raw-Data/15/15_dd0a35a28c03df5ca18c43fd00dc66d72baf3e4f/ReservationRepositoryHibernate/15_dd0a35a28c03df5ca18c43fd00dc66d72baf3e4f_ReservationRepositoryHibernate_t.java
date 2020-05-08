 package net.mklew.hotelms.persistance;
 
 import net.mklew.hotelms.domain.booking.reservation.Reservation;
 import net.mklew.hotelms.domain.booking.reservation.ReservationRepository;
 import net.mklew.hotelms.persistance.hibernate.configuration.HibernateSessionFactory;
 import org.hibernate.Session;
 import org.joda.time.DateTime;
 
import java.util.ArrayList;
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
            "reservation join reservation.nights as night where night.date in (:dates) group by reservation";
 
     public ReservationRepositoryHibernate(HibernateSessionFactory hibernateSessionFactory)
     {
         super(hibernateSessionFactory);
     }
 
     @Override
     public Collection<Reservation> findAllReservationsAroundDates(DateTime checkIn, DateTime checkOut)
     {
         final Session session = getCurrentSession();
        Collection<DateTime> dates = new ArrayList<>();
        for (int i = 0; !checkIn.plusDays(i).equals(checkOut.plusDays(1)); ++i)
        {
            dates.add(checkIn.plusDays(i));
        }
         @SuppressWarnings("unchecked")
         List<Reservation> reservations = (List<Reservation>) session.createQuery
                (FIND_ALL_RESERVATIONS_FOR_ROOM_AROUND_DATES_QUERY).setParameterList("dates",
                dates).list();
         return reservations;
     }
 
     @Override
     public void bookNewReservation(Reservation reservation)
     {
         final Session session = getCurrentSession();
         session.save(reservation);
     }
 }
