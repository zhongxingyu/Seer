 package net.mklew.hotelms.inhouse.web.rest;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterators;
 import com.sun.jersey.spi.resource.Singleton;
 import net.mklew.hotelms.domain.booking.ChargeCalculatorService;
 import net.mklew.hotelms.domain.booking.reservation.rates.Rate;
 import net.mklew.hotelms.domain.booking.reservation.rates.RateRepository;
 import net.mklew.hotelms.domain.room.Room;
 import net.mklew.hotelms.domain.room.RoomName;
 import net.mklew.hotelms.domain.room.RoomRepository;
 import net.mklew.hotelms.inhouse.web.dto.MoneyDto;
 import net.mklew.hotelms.persistance.hibernate.configuration.HibernateSessionFactory;
 import org.hibernate.Session;
 import org.joda.money.Money;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import java.util.Collection;
 
 /**
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 11/30/12
  *        time 3:00 PM
  */
 @Path("charge")
 @Singleton
 public class ChargeCalculatorResource
 {
     private final RateRepository rateRepository;
     private final RoomRepository roomRepository;
     private final HibernateSessionFactory hibernateSessionFactory;
    private final DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();
 
     public ChargeCalculatorResource(RateRepository rateRepository, RoomRepository roomRepository,
                                     HibernateSessionFactory hibernateSessionFactory)
     {
         this.rateRepository = rateRepository;
         this.roomRepository = roomRepository;
         this.hibernateSessionFactory = hibernateSessionFactory;
     }
 
     @GET
     @Path("room/{roomName}/rate/{rateName}")
     @Produces(MediaType.APPLICATION_JSON)
     public MoneyDto chargeForRoomAccordingToRate(@PathParam("roomName") final String roomName,
                                                  @PathParam("rateName") final String rateName,
                                                  @QueryParam("checkin") final String checkin,
                                                 @QueryParam("checkout") final String checkout)
     {
         // TODO implement inclusions because so far they are ignored
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
 
         RoomName name = new RoomName(roomName);
        Room room = roomRepository.getRoomByName(name);
         Collection<Rate> rates = rateRepository.getAllRatesForRoom(room);
 
         Rate rate = Iterators.find(rates.iterator(), new Predicate<Rate>()
         {
             @Override
             public boolean apply(Rate rate)
             {
                 return rate.getRateName().equals(rateName);
             }
         });
 
         final DateTime checkoutDate = DateTime.parse(checkin, formatter);
         final DateTime checkinDate = DateTime.parse(checkout, formatter);
 
         Money charge = ChargeCalculatorService.calculateChargeForStay(rate, checkoutDate, checkinDate);
         MoneyDto dto = MoneyDto.fromMoney(charge);
 
         session.getTransaction().commit();
         return dto;
     }
 
 }
