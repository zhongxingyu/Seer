 package net.mklew.hotelms.inhouse.web.rest;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 import javax.inject.Inject;
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 
import net.mklew.hotelms.domain.guests.Gender;
 import org.apache.log4j.Logger;
 import org.hibernate.Hibernate;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.context.internal.ThreadLocalSessionContext;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import net.mklew.hotelms.domain.booking.GuestRepository;
 import net.mklew.hotelms.domain.booking.Id;
 import net.mklew.hotelms.domain.booking.ReservationStatus;
 import net.mklew.hotelms.domain.booking.reservation.*;
 import net.mklew.hotelms.domain.booking.reservation.rates.Rate;
 import net.mklew.hotelms.domain.booking.reservation.rates.RateRepository;
 import net.mklew.hotelms.domain.guests.Guest;
 import net.mklew.hotelms.domain.room.Room;
 import net.mklew.hotelms.domain.room.RoomName;
 import net.mklew.hotelms.domain.room.RoomNotFoundException;
 import net.mklew.hotelms.domain.room.RoomRepository;
 import net.mklew.hotelms.inhouse.web.dto.*;
 import net.mklew.hotelms.inhouse.web.dto.dates.DateParser;
 import net.mklew.hotelms.persistance.hibernate.configuration.HibernateSessionFactory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 /**
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 12/24/12 time 3:46 PM
  */
 @Path("/reservations")
 public class ReservationResource
 {
     private final Logger logger = Logger.getLogger(ReservationResource.class);
 
     private final ReservationFactory reservationFactory;
 
     private final GuestRepository guestRepository;
 
     private final HibernateSessionFactory hibernateSessionFactory;
 
     private final RoomRepository roomRepository;
 
     private final RateRepository rateRepository;
 
     private final BookingService bookingService;
 
     private final ReservationRepository reservationRepository;
 
     private final CheckInService checkInService;
 
     private final CheckOutService checkOutService;
 
     private final CancellationService cancellationService;
 
     @Inject
     public ReservationResource(ReservationFactory reservationFactory,
         GuestRepository guestRepository, HibernateSessionFactory hibernateSessionFactory,
         RoomRepository roomRepository, RateRepository rateRepository,
         BookingService bookingService, ReservationRepository reservationRepository,
         CheckInService checkInService, CheckOutService checkOutService,
         CancellationService cancellationService)
     {
         this.reservationFactory = reservationFactory;
         this.guestRepository = guestRepository;
         this.hibernateSessionFactory = hibernateSessionFactory;
         this.roomRepository = roomRepository;
         this.rateRepository = rateRepository;
         this.bookingService = bookingService;
         this.reservationRepository = reservationRepository;
         this.checkInService = checkInService;
         this.checkOutService = checkOutService;
         this.cancellationService = cancellationService;
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Collection<ReservationDto> getAllReservations()
     {
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
         final Collection<Reservation> reservations = reservationRepository.getAll();
         Collection<ReservationDto> dtos = new ArrayList<>(reservations.size());
         for(Reservation reservation : reservations)
         {
             dtos.add(ReservationDto.fromReservation(reservation));
         }
         session.getTransaction().commit();
         return dtos;
     }
 
     @GET
     @Path("/{id}")
     public Response getReservation(@PathParam("id") String reservationId)
     {
         Id id = Id.of(reservationId);
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
         final Optional<Reservation> reservationOptional = reservationRepository.lookup(id);
         if(reservationOptional.isPresent())
         {
             final ReservationDto dto = ReservationDto.fromReservation(reservationOptional.get());
             session.getTransaction().commit();
             return Response.ok(dto, MediaType.APPLICATION_JSON_TYPE).status(Response.Status.OK)
                 .build();
         }
         else
         {
             session.getTransaction().commit();
             return Response.ok().status(Response.Status.NOT_FOUND).build();
         }
     }
 
     private Response reservationOperationTemplate(String reservationId,
         ReservationOperationAction action)
     {
         Id id = Id.of(reservationId);
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
         final Optional<Reservation> reservationOptional = reservationRepository.lookup(id);
         if(reservationOptional.isPresent())
         {
             final Reservation reservation = reservationOptional.get();
             return action.doAction(reservation);
         }
         else
         {
             session.getTransaction().commit();
             return Response
                 .ok(new ErrorDto("Reservation with id " + reservationId + " has not been found.",
                         "RESERVATION-NOT-FOUND"), MediaType.APPLICATION_JSON_TYPE)
                 .status(Response.Status.BAD_REQUEST).build();
         }
     }
 
     private static abstract class ReservationOperationAction
     {
         abstract Response doAction(final Reservation reservation);
     }
 
     @Path("/{id}/checkIn")
     @POST
     public Response checkInReservation(@PathParam("id") String reservationId)
     {
         return reservationOperationTemplate(reservationId, new ReservationOperationAction()
             {
                 @Override
                 Response doAction(Reservation reservation)
                 {
                     checkInService.checkIn(reservation);
                     final Session session = hibernateSessionFactory.getCurrentSession();
                     session.saveOrUpdate(reservation);
                     session.getTransaction().commit();
                     // place for error handling depending on business conditions and returning
                     // different responses.
                     return Response.ok().status(Response.Status.OK).build();
                 }
             });
     }
 
     @Path("/{id}/checkOut")
     @POST
     public Response checkOutReservation(@PathParam("id") String reservationId)
     {
         return reservationOperationTemplate(reservationId, new ReservationOperationAction()
             {
                 @Override
                 Response doAction(Reservation reservation)
                 {
                     checkOutService.checkOut(reservation);
                     final Session session = hibernateSessionFactory.getCurrentSession();
                     session.saveOrUpdate(reservation);
                     session.getTransaction().commit();
                     // place for error handling depending on business conditions and returning
                     // different responses.
                     return Response.ok().status(Response.Status.OK).build();
                 }
             });
     }
 
     @Path("/{id}/cancel")
     @POST
     public Response cancelReservation(@PathParam("id") String reservationId)
     {
         return reservationOperationTemplate(reservationId, new ReservationOperationAction()
             {
                 @Override
                 Response doAction(Reservation reservation)
                 {
                     cancellationService.cancel(reservation);
                     final Session session = hibernateSessionFactory.getCurrentSession();
                     session.saveOrUpdate(reservation);
                     session.getTransaction().commit();
                     // place for error handling depending on business conditions and returning
                     // different responses.
                     return Response.ok().status(Response.Status.OK).build();
                 }
             });
     }
 
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.APPLICATION_JSON)
     public Response bookNewReservation(ReservationDto reservationDto)
     {
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
 
         try
         {
             reservationDto.init();
             GuestDto reservationOwner = reservationDto.getOwner();
             reservationOwner.init();
 
             Guest owner;
             if(reservationOwner.exists())
             {
                 owner = guestRepository.findGuestById(Long.parseLong(reservationOwner.id));
             }
             else
             {
                 owner = new Guest(reservationOwner.socialTitle, reservationOwner.firstName,
                    reservationOwner.surname, Gender.fromName(reservationOwner.getSex()), reservationOwner.idType,
                     reservationOwner.idNumber, reservationOwner.phoneNumber);
                 owner.setPreferences(reservationOwner.preferences);
                 owner.setDateOfBirth(reservationOwner.dateOfBirthDate);
                owner.setNationality(reservationOwner.getNationality());
                 // owner.setEmailAddress(); // todo add field to form, dto, and set it here
                 // todo nationality, address and other
                 guestRepository.saveGuest(owner);
             }
 
             // get room
             RoomName roomName = RoomName.getNameWithoutPrefix(reservationDto.getRoomName());
             final Room room = roomRepository.getRoomByName(roomName);
             // find rate
             Collection<Rate> rates = rateRepository.getAllRatesForRoom(room);
             Rate rate = getChosenRate(reservationDto, rates);
 
             // create reservation using factory
             Reservation reservation;
             if(ReservationType.fromName(reservationDto.getReservationType()).equals(
                 ReservationType.SINGLE))
             {
                 reservation = reservationFactory.createSingleReservation(owner, room, rate,
                     reservationDto.getCheckinDate(), reservationDto.getCheckoutDate(),
                     Integer.parseInt(reservationDto.getNumberOfAdults()),
                     Integer.parseInt(reservationDto.getNumberOfChildren()),
                     Integer.parseInt(reservationDto.getRoomExtraBed()));
             }
             else
             {
                 throw new OperationNotSupportedException(
                     "Other reservation types are not supported ");
             }
 
             // book reservation or fail on exception
             bookingService.bookReservation(reservation);
             ReservationDto bookedDto = ReservationDto.fromReservation(reservation);
 
             session.getTransaction().commit();
 
             return Response.ok(bookedDto, MediaType.APPLICATION_JSON_TYPE)
                 .status(Response.Status.CREATED).build();
         }
         catch(MissingGuestInformation missingGuestInformation)
         {
             logger
                 .error("Reservation owner has no sufficient information", missingGuestInformation);
             return Response
                 .ok(new ErrorDto("Missing guest information", "GUEST-MISSING-INFO"),
                         MediaType.APPLICATION_JSON_TYPE).status(Response.Status.BAD_REQUEST).build();
         }
         catch(RoomNotFoundException e)
         {
             logger.error("Room not found exception", e);
             return Response
                 .ok(new ErrorDto("Room not found", "ROOM-NOT-FOUND"),
                         MediaType.APPLICATION_JSON_TYPE).status(Response.Status.NOT_FOUND).build();
         }
         catch(OperationNotSupportedException e)
         {
             logger.error("Operation not supported", e);
             return Response
                 .ok(new ErrorDto("Operation not supported", "OPERATION-NOT-SUPPORTED"),
                         MediaType.APPLICATION_JSON_TYPE).status(HttpServletResponse.SC_NOT_IMPLEMENTED)
                 .build();
         }
         catch(RoomIsUnavailableException e)
         {
             final String message = "Room " + e.getRoomName() + " is unavailable between "
                 + DateParser.fromDate(e.getCheckIn()) + " and "
                 + DateParser.fromDate(e.getCheckOut());
             return Response
                 .ok(new ErrorDto(message, "ROOM-UNAVAILABLE"), MediaType.APPLICATION_JSON_TYPE)
                 .status(Response.Status.FORBIDDEN).build();
         }
     }
 
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     public Response createNewReservation(MultivaluedMap<String, String> formParams,
         @Context HttpServletResponse httpServletResponse)
     {
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
         logger.debug("Got new reservation with parameters: " + formParams.toString());
         try
         {
 
             GuestDto reservationOwner = GuestDto.fromReservationForm(formParams);
             ReservationDto reservationDto = ReservationDto.fromReservationForm(formParams);
             Guest owner;
             if(reservationOwner.exists())
             {
                 owner = guestRepository.findGuestById(Long.parseLong(reservationOwner.id));
             }
             else
             {
                 owner = new Guest(reservationOwner.socialTitle, reservationOwner.firstName,
                     reservationOwner.surname, reservationOwner.gender, reservationOwner.idType,
                     reservationOwner.idNumber, reservationOwner.phoneNumber);
                 owner.setPreferences(reservationOwner.preferences);
                 owner.setDateOfBirth(reservationOwner.dateOfBirthDate);
                 // owner.setEmailAddress(); // todo add field to form, dto, and set it here
                 // todo nationality, address and other
                 guestRepository.saveGuest(owner);
             }
 
             // get room
             RoomName roomName = RoomName.getNameWithoutPrefix(reservationDto.getRoomName());
             final Room room = roomRepository.getRoomByName(roomName);
             // find rate
             Collection<Rate> rates = rateRepository.getAllRatesForRoom(room);
             Rate rate = getChosenRate(reservationDto, rates);
 
             // create reservation using factory
             Reservation reservation;
             if(ReservationType.fromName(reservationDto.getReservationType()).equals(
                 ReservationType.SINGLE))
             {
                 reservation = reservationFactory.createSingleReservation(owner, room, rate,
                     reservationDto.getCheckinDate(), reservationDto.getCheckoutDate(),
                     Integer.parseInt(reservationDto.getNumberOfAdults()),
                     Integer.parseInt(reservationDto.getNumberOfChildren()),
                     Integer.parseInt(reservationDto.getRoomExtraBed()));
             }
             else
             {
                 throw new OperationNotSupportedException(
                     "Other reservation types are not supported ");
             }
 
             // book reservation or fail on exception
             bookingService.bookReservation(reservation);
             ReservationDto bookedDto = ReservationDto.fromReservation(reservation);
 
             session.getTransaction().commit();
 
             return Response.ok(bookedDto, MediaType.APPLICATION_JSON_TYPE)
                 .status(Response.Status.CREATED).build();
         }
         catch(MissingGuestInformation missingGuestInformation)
         {
             logger
                 .error("Reservation owner has no sufficient information", missingGuestInformation);
             httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             return Response
                 .ok(new ErrorDto("Missing guest information", "GUEST-MISSING-INFO"),
                         MediaType.APPLICATION_JSON_TYPE).status(Response.Status.BAD_REQUEST).build();
         }
         catch(RoomNotFoundException e)
         {
             logger.error("Room not found exception", e);
             return Response
                 .ok(new ErrorDto("Room not found", "ROOM-NOT-FOUND"),
                         MediaType.APPLICATION_JSON_TYPE).status(Response.Status.NOT_FOUND).build();
         }
         catch(OperationNotSupportedException e)
         {
             logger.error("Operation not supported", e);
             return Response
                 .ok(new ErrorDto("Operation not supported", "OPERATION-NOT-SUPPORTED"),
                         MediaType.APPLICATION_JSON_TYPE).status(HttpServletResponse.SC_NOT_IMPLEMENTED)
                 .build();
         }
         catch(RoomIsUnavailableException e)
         {
             final String message = "Room " + e.getRoomName() + " is unavailable between "
                 + DateParser.fromDate(e.getCheckIn()) + " and "
                 + DateParser.fromDate(e.getCheckOut());
             return Response
                 .ok(new ErrorDto(message, "ROOM-UNAVAILABLE"), MediaType.APPLICATION_JSON_TYPE)
                 .status(Response.Status.FORBIDDEN).build();
         }
     }
 
     @DELETE
     @Path("/{id}/")
     public Response deleteReservation(@PathParam("id") String reservationId)
     {
         Id id = Id.of(reservationId);
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
         final Optional<Reservation> reservationOptional = reservationRepository.lookup(id);
         if(reservationOptional.isPresent())
         {
             final Reservation reservation = reservationOptional.get();
             reservationRepository.deleteReservation(reservation);
             session.getTransaction().commit();
             return Response.ok().status(HttpServletResponse.SC_OK).build();
         }
         else
         {
             session.getTransaction().commit();
             return Response
                 .ok(new ErrorDto("Reservation with id " + reservationId + " has not been found.",
                         "RESERVATION-NOT-FOUND"), MediaType.APPLICATION_JSON_TYPE)
                 .status(Response.Status.NOT_FOUND).build();
         }
     }
 
     @PUT
     @Path("/{id}")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response modifyReservation(@PathParam("id") String reservationId,
         final ReservationDto reservationParam)
     {
         return reservationOperationTemplate(reservationId, new ReservationOperationAction()
             {
                 @Override
                 Response doAction(Reservation reservation)
                 {
                     reservationParam.validateRequired();
                     reservationParam.init();
                     // Only few things can be changed in edit. Other properties must be changed in a
                     // different way
                     return modify(reservation, reservationParam);
                 }
             });
     }
 
     @PUT
     @Path("/{id}/")
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     public Response modifyReservation(@PathParam("id") String reservationId,
         final MultivaluedMap<String, String> formParams)
     {
         return reservationOperationTemplate(reservationId, new ReservationOperationAction()
             {
                 @Override
                 Response doAction(Reservation reservation)
                 {
                     final ReservationDto reservationDto = ReservationDto
                         .fromReservationForm(formParams);
                     return modify(reservation, reservationDto);
 
                 }
             });
     }
 
     private static class ReservationChanges
     {
         private final Reservation reservation;
 
         private final ReservationDto reservationDto;
 
         private ReservationChanges(Reservation reservation, ReservationDto reservationDto)
         {
             this.reservation = reservation;
             this.reservationDto = reservationDto;
         }
 
         boolean rateChanged()
         {
             return !reservation.getRate().getRateName().equals(reservationDto.getRateType());
         }
 
         boolean roomChanged()
         {
             return !reservation.getRoom().getName()
                 .equals(RoomName.getNameWithoutPrefix(reservationDto.getRoomName()));
         }
 
         boolean checkInChanged()
         {
             return !reservation.getCheckIn().equals(reservationDto.getCheckinDate());
         }
 
         boolean checkOutChanged()
         {
             return !reservation.getCheckOut().equals(reservationDto.getCheckoutDate());
         }
 
         String getNewRate()
         {
             return reservationDto.getRateType();
         }
 
         public DateTime getNewCheckIn()
         {
             return reservationDto.getCheckinDate();
         }
 
         public DateTime getNewCheckOut()
         {
             return reservationDto.getCheckoutDate();
         }
 
         public String getNewRoomName()
         {
             return reservationDto.getRoomName();
         }
 
         boolean extraBedsChanged()
         {
             return reservation.getExtraBeds() != getExtraBeds();
         }
 
         int getExtraBeds()
         {
             return Integer.valueOf(reservationDto.getRoomExtraBed());
         }
 
         int getNumberOfAdults()
         {
             return Integer.valueOf(reservationDto.getNumberOfAdults());
         }
 
         boolean numberOfAdultsChanged()
         {
             return reservation.getNumberOfAdults() != getNumberOfAdults();
         }
 
         int getNumberOfChildren()
         {
             return Integer.valueOf(reservationDto.getNumberOfChildren());
         }
 
         boolean numberOfChildrenChanged()
         {
             return reservation.getNumberOfChildren() != getNumberOfChildren();
         }
     }
 
     private Response modify(final Reservation reservation, final ReservationDto reservationDto)
     {
         // Only few things can be changed in edit. Other properties must be changed in a different
         // way
         final ReservationChanges changes = new ReservationChanges(reservation, reservationDto);
 
         if(reservationDto.getCheckinDate().isAfter(reservationDto.getCheckoutDate()))
         {
             hibernateSessionFactory.getCurrentSession().getTransaction().rollback();
             return Response
                 .ok(new ErrorDto("CheckIn " + reservationDto.getCheckin() + " date cannot be after"
                         + " CheckOut date " + reservationDto.getCheckout(), "RESERVATION-WRONG-DATES"),
                         MediaType.APPLICATION_JSON_TYPE).status(HttpServletResponse.SC_FORBIDDEN)
                 .build();
         }
         // change in room implies rate change even if rates are named the same.
         try
         {
             if(changes.roomChanged())
             {
                 // change room and rate according to new checkin and new checkout
                 final RoomName roomName = RoomName.getNameWithoutPrefix(changes.getNewRoomName());
                 final String newRate = changes.getNewRate();
                 final DateTime newCheckIn = changes.getNewCheckIn();
                 final DateTime newCheckOut = changes.getNewCheckOut();
 
                 bookingService.rebookReservationRoom(reservation, roomName, newRate, newCheckIn,
                     newCheckOut);
             }
             else if(changes.rateChanged())
             {
                 final String newRate = changes.getNewRate();
                 bookingService.changeRate(reservation, newRate);
             }
 
             if(changes.checkInChanged() || changes.checkOutChanged())
             {
                 bookingService.rebookVisit(reservation, changes.getNewCheckIn(),
                     changes.getNewCheckOut());
             }
         }
         catch(RoomNotFoundException e)
         {
             logger.error("Room not found exception", e);
             return Response
                 .ok(new ErrorDto("Room not found", "ROOM-NOT-FOUND"),
                         MediaType.APPLICATION_JSON_TYPE).status(Response.Status.NOT_FOUND).build();
         }
         catch(RateNotFoundException e)
         {
             logger.error("Rate not found exception", e);
             return Response
                 .ok(new ErrorDto("Rate not found", "RATE-NOT-FOUND"),
                         MediaType.APPLICATION_JSON_TYPE).status(Response.Status.NOT_FOUND).build();
         }
         catch(RoomIsUnavailableException e)
         {
             final String message = "Room " + e.getRoomName() + " is unavailable between "
                 + DateParser.fromDate(e.getCheckIn()) + " and "
                 + DateParser.fromDate(e.getCheckOut());
             return Response
                 .ok(new ErrorDto(message, "ROOM-UNAVAILABLE"), MediaType.APPLICATION_JSON_TYPE)
                 .status(Response.Status.FORBIDDEN).build();
         }
 
         if(changes.extraBedsChanged())
         {
             reservation.setExtraBeds(changes.getExtraBeds());
         }
 
         if(changes.numberOfAdultsChanged())
         {
             reservation.setNumberOfAdults(changes.getNumberOfAdults());
         }
 
         if(changes.numberOfChildrenChanged())
         {
             reservation.setNumberOfChildren(changes.getNumberOfChildren());
         }
 
         reservationRepository.update(reservation);
         final Session session = hibernateSessionFactory.getCurrentSession();
         session.getTransaction().commit();
         return Response.ok().build();
     }
 
     private Rate getChosenRate(ReservationDto reservationDto, Collection<Rate> rates)
     {
         for(Rate rate : rates)
         {
             if(reservationDto.getRateType().equals(rate.getRateName()))
             {
                 return rate;
             }
         }
         throw new RuntimeException("Rate not found");
     }
 
     @GET
     @Path("stats")
     @Produces(MediaType.APPLICATION_JSON)
     public ReservationStats getStats()
     {
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
 
         Collection<Reservation> inhouse = reservationRepository
             .findWithStatus(ReservationStatus.INHOUSE);
         Collection<Reservation> checkin = reservationRepository
             .findWithStatus(ReservationStatus.CHECKIN);
         Collection<Reservation> checkout = reservationRepository
             .findWithStatus(ReservationStatus.CHECKOUT);
 
         session.getTransaction().commit();
         return new ReservationStats(inhouse.size(), checkin.size(), checkout.size());
     }
 
     @GET
     @Path("sheetData")
     @Produces(MediaType.APPLICATION_JSON)
     public Collection<SheetData> getBookingSheetData(@QueryParam("from") long from,
         @QueryParam("to") long to)
     {
         Date fromDate = new Date(from);
         Date toDate = new Date(to);
 
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
 
         final Collection<Room> allRooms = roomRepository.getAllRooms();
 
         final Collection<Reservation> reservationsAroundDates = reservationRepository
             .findAllReservationsAroundDates(new DateTime(fromDate), new DateTime(toDate));
 
         for(Reservation res : reservationsAroundDates)
         {
             Hibernate.initialize(res);
             Hibernate.initialize(res.getNights());
         }
 
         Collection<SheetData> sheetData = Collections2.transform(allRooms,
             new Function<Room, SheetData>()
                 {
                     @Override
                     public SheetData apply(final Room room)
                     {
                         SheetData data = new SheetData();
                         data.setRoomName(room.fullRoomName());
 
                         Iterable<Reservation> reservations = Iterables.filter(
                             reservationsAroundDates, new Predicate<Reservation>()
                                 {
                                     @Override
                                     public boolean apply(Reservation reservation)
                                     {
                                         return reservation.getRoom().equals(room);
                                     }
                                 });
 
                         Iterable<ReservationSheetData> reservationSheetDataIterable = Iterables
                             .transform(reservations,
                                     new Function<Reservation, ReservationSheetData>()
                                     {
                                         @Override
                                         public ReservationSheetData apply(Reservation reservation)
                                         {
                                             ReservationSheetData reservationSheetData = new ReservationSheetData();
                                             reservationSheetData.setFirstName(reservation
                                                     .getReservationOwner().getFirstName());
                                             reservationSheetData.setLastName(reservation
                                                     .getReservationOwner().getSurname());
                                             reservationSheetData.setReservationNumber(reservation
                                                     .getReservationId().getPrintableId());
                                             reservationSheetData.setStatus(reservation
                                                     .getReservationStatus().getName());
                                             DateTimeFormatter formatter = DateTimeFormat
                                                     .forPattern("yyyy-MM-dd");
                                             reservationSheetData.setFrom(formatter
                                                     .print(reservation.getCheckIn()));
                                             reservationSheetData.setTo(formatter.print(reservation
                                                     .getCheckOut()));
                                             return reservationSheetData;
                                         }
                                     });
 
                         data.setReservations(Lists.newArrayList(reservationSheetDataIterable));
                         return data;
                     }
                 });
 
         session.getTransaction().commit();
         return sheetData;
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @Path("status/{status}")
     public Collection<ReservationDto> getWithStatus(@PathParam("status") String status)
     {
         Session session = hibernateSessionFactory.getCurrentSession();
         session.beginTransaction();
 
         final ReservationStatus reservationStatus = ReservationStatus.fromName(status);
         final Collection<Reservation> withStatus = reservationRepository.findWithStatus(reservationStatus);
 
         final Collection<ReservationDto> dtos = Collections2.transform(withStatus, new Function<Reservation, ReservationDto>()
         {
             @Override
             public ReservationDto apply(Reservation reservation)
             {
                 return ReservationDto.fromReservation(reservation);
             }
         });
 
         session.getTransaction().commit();
         return dtos;
     }
 
 }
