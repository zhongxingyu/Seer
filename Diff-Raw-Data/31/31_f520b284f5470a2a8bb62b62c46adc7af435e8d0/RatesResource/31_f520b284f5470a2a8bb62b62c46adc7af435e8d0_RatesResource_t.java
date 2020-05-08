 package net.mklew.hotelms.inhouse.web.rest;
 
 import com.sun.jersey.spi.resource.Singleton;
 import net.mklew.hotelms.domain.booking.reservation.rates.Rate;
 import net.mklew.hotelms.domain.booking.reservation.rates.RateRepository;
 import net.mklew.hotelms.domain.room.Room;
 import net.mklew.hotelms.domain.room.RoomName;
import net.mklew.hotelms.domain.room.RoomNotFoundException;
 import net.mklew.hotelms.domain.room.RoomRepository;
 import net.mklew.hotelms.inhouse.web.dto.RateDto;
 import net.mklew.hotelms.persistance.hibernate.configuration.HibernateSessionFactory;
 import org.hibernate.Session;
 
import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 11/24/12
 *        time 9:02 PM
  */
 @Singleton
 @Path("rates/{roomName}")
 public class RatesResource
 {
     private final RateRepository rateRepository;
     private final RoomRepository roomRepository;
     private final HibernateSessionFactory hibernateSessionFactory;
 
    public RatesResource(RateRepository rateRepository, RoomRepository roomRepository,
                         HibernateSessionFactory hibernateSessionFactory)
     {
         this.rateRepository = rateRepository;
         this.roomRepository = roomRepository;
         this.hibernateSessionFactory = hibernateSessionFactory;
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
    public Collection<RateDto> getAllRates(@PathParam("roomName") String roomName,
                                           @Context HttpServletResponse httpServletResponse)
     {
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
         RoomName name = new RoomName(roomName);
        Room room = null;
        try
        {
            room = roomRepository.getRoomByName(name);
        }
        catch (RoomNotFoundException e)
        {
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
         Collection<Rate> rates = rateRepository.getAllRatesForRoom(room);
         ArrayList<RateDto> rateDtos = new ArrayList<>(rates.size());
        for (Rate rate : rates)
         {
             rateDtos.add(RateDto.createFromRate(rate));
         }
         session.getTransaction().commit();
         return rateDtos;
     }
 
 
 }
