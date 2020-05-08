 package net.mklew.hotelms.persistance.hibernate.configuration;
 
 import net.mklew.hotelms.domain.booking.Id;
 import net.mklew.hotelms.domain.booking.ReservationStatus;
 import net.mklew.hotelms.domain.booking.reservation.Reservation;
 import net.mklew.hotelms.domain.booking.reservation.rates.*;
 import net.mklew.hotelms.domain.guests.DocumentType;
 import net.mklew.hotelms.domain.guests.Gender;
 import net.mklew.hotelms.domain.guests.Guest;
 import net.mklew.hotelms.domain.room.*;
 import org.hibernate.Session;
 import org.jcontainer.dna.Logger;
 import org.joda.money.Money;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.picocontainer.Startable;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 /**
  * Populates database with some data upon startup.
  * One place to hardcore everything
  * <p/>
  * It should not be used for production!
  *
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 11/11/12
  *        Time: 8:11 PM
  */
 public class DbBootstrap implements Startable
 {
     final private Logger logger;
     final private HibernateSessionFactory hibernateSessionFactory;
 
     public DbBootstrap(Logger logger, HibernateSessionFactory hibernateSessionFactory)
     {
         this.logger = logger;
         this.hibernateSessionFactory = hibernateSessionFactory;
     }
 
     @Override
     public void start()
     {
         bootstrap();
     }
 
     private void bootstrap()
     {
         logger.debug("Started bootstrapping database");
         Session session = hibernateSessionFactory.getCurrentSession();
 
         // bootstrapping data
 
         Collection<RoomType> types = new ArrayList<>();
         RoomType luxury = new RoomType("luxury");
         RoomType cheap = new RoomType("cheap");
         RoomType niceOne = new RoomType("nice one");
 
         types.addAll(Arrays.asList(luxury, cheap, niceOne));
         Collection<Room> rooms;
 
         Money standardPrice = Money.parse("USD 100");
         Money upchargeExtraPerson = Money.parse("USD 110");
         Money upchargeExtraBed = Money.parse("USD 120");
 //        RackRate rackRate = new RackRate(standardPrice, upchargeExtraPerson, upchargeExtraBed, null);
 //        RackRate rackRate1 = new RackRate(standardPrice.plus(10), upchargeExtraPerson.plus(10),
 //                upchargeExtraBed.plus(10), null);
 //        RackRate rackRate2 = new RackRate(standardPrice.plus(20), upchargeExtraPerson.plus(20),
 //                upchargeExtraBed.plus(20), null);
 //        RackRate rackRate3 = new RackRate(standardPrice.plus(30), upchargeExtraPerson.plus(30),
 //                upchargeExtraBed.plus(30), null);
 //        RackRate rackRate4 = new RackRate(standardPrice.plus(40), upchargeExtraPerson.plus(40),
 //                upchargeExtraBed.plus(40), null);
 //        RackRate rackRate5 = new RackRate(standardPrice.plus(50), upchargeExtraPerson.plus(50),
 //                upchargeExtraBed.plus(50), null);
 
         Room L100 = new Room("L", new RoomName("100"), luxury, HousekeepingStatus.CLEAN,
                 RoomAvailability.AVAILABLE, 1, new Occupancy(3, 4), standardPrice, upchargeExtraPerson,
                 upchargeExtraBed);
         Room L101 = new Room("L", new RoomName("101"), luxury, HousekeepingStatus.CLEAN,
                 RoomAvailability.AVAILABLE, 1, new Occupancy(2, 3), standardPrice.plus(10),
                 upchargeExtraPerson.plus(10),
                 upchargeExtraBed.plus(10));
         Room L102 = new Room("L", new RoomName("102"), luxury, HousekeepingStatus.CLEAN,
                 RoomAvailability.OCCUPIED, 3, new Occupancy(2, 4), standardPrice.plus(30),
                 upchargeExtraPerson.plus(30),
                 upchargeExtraBed.plus(30));
         Room C103 = new Room("C", new RoomName("103"), cheap, HousekeepingStatus.CLEAN,
                 RoomAvailability.AVAILABLE, 4, new Occupancy(4, 10), standardPrice.plus(30),
                 upchargeExtraPerson.plus(30),
                 upchargeExtraBed.plus(30));
         Room C104 = new Room("C", new RoomName("104"), cheap, HousekeepingStatus.CLEAN,
                 RoomAvailability.AVAILABLE, 5, new Occupancy(6, 12), standardPrice.plus(40),
                 upchargeExtraPerson.plus(40),
                 upchargeExtraBed.plus(40));
         Room N105 = new Room("N", new RoomName("105"), niceOne, HousekeepingStatus.CLEAN,
                 RoomAvailability.AVAILABLE, 2, new Occupancy(2, 5), standardPrice.plus(50),
                 upchargeExtraPerson.plus(50),
                 upchargeExtraBed.plus(50));
 
         rooms = Arrays.asList(L100, L101, L102, C103, C104, N105);
 
         Collection<Rate> rates;
 
         Season season = new BasicSeason("winter special", new AvailabilityPeriod(DateTime.now(),
                 DateTime.now().plusDays(90), true));
         Season season2 = new BasicSeason("christmas special", new AvailabilityPeriod(DateTime.now(),
                 DateTime.now().plusDays(30), true));
 
         Rate rate1_L100 = new SeasonRate(Money.parse("USD 50"), Money.parse("USD 60"), Money.parse("USD 70"), L100,
                 season);
         Rate rate2_L100 = new SeasonRate(Money.parse("USD 20"), Money.parse("USD 60"), Money.parse("USD 70"), L100,
                 season2);
         Rate rate1_L101 = new SeasonRate(Money.parse("USD 60"), Money.parse("USD 70"), Money.parse("USD 60"), L101,
                 season);
         Rate rate2_L101 = new SeasonRate(Money.parse("USD 20"), Money.parse("USD 70"), Money.parse("USD 60"), L101,
                 season2);
         Rate rate1_L102 = new SeasonRate(Money.parse("USD 60"), Money.parse("USD 70"), Money.parse("USD 60"), L102,
                 season);
         Rate rate2_L102 = new SeasonRate(Money.parse("USD 20"), Money.parse("USD 70"), Money.parse("USD 60"), L102,
                 season2);
         Rate rate1_C103 = new SeasonRate(Money.parse("USD 40"), Money.parse("USD 70"), Money.parse("USD 60"), C103,
                 season);
         Rate rate2_C103 = new SeasonRate(Money.parse("USD 30"), Money.parse("USD 70"), Money.parse("USD 60"), C103,
                 season2);
         Rate rate1_C104 = new SeasonRate(Money.parse("USD 70"), Money.parse("USD 70"), Money.parse("USD 60"), C104,
                 season);
         Rate rate2_C104 = new SeasonRate(Money.parse("USD 50"), Money.parse("USD 70"), Money.parse("USD 60"), C104,
                 season2);
         Rate rate1_N105 = new SeasonRate(Money.parse("USD 80"), Money.parse("USD 70"), Money.parse("USD 60"), N105,
                 season);
         Rate rate2_N105 = new SeasonRate(Money.parse("USD 90"), Money.parse("USD 70"), Money.parse("USD 60"), N105,
                 season2);
 
         Collection<Season> seasons = Arrays.asList(season, season2);
         rates = Arrays.asList(rate1_L100, rate2_L100, rate1_L101, rate2_L101, rate1_L102, rate2_L102, rate1_C103,
                 rate2_C103, rate1_C104, rate2_C104, rate1_N105, rate2_N105);
 
 
         Guest guest1 = new Guest("Mr", "John", "Doe", Gender.MALE, DocumentType.DRIVER_LICENSE, "Drivers123",
                 "555123123");
         Guest guest2 = new Guest("Mr", "Johnson", "Donnel", Gender.MALE, DocumentType.DRIVER_LICENSE, "DRI123",
                 "555555123");
         Guest guest3 = new Guest("Mr", "Johnathan", "Doougles", Gender.MALE, DocumentType.DRIVER_LICENSE, "DDRI1555",
                 "555123123");
         Guest guest4 = new Guest("Miss", "Joana", "Dooues", Gender.FEMALE, DocumentType.PERSONAL_ID, "APK132555",
                 "819238923");
         Guest guest5 = new Guest("Ms", "Kate", "Hudson", Gender.FEMALE, DocumentType.PERSONAL_ID, "DSA123889",
                 "534098123");
         Guest guest6 = new Guest("Mr", "Jack", "Hack", Gender.MALE, DocumentType.PERSONAL_ID, "LKK123555", "123589124");
         Guest guest7 = new Guest("Ms", "Ewa", "Kowalska", Gender.FEMALE, DocumentType.PERSONAL_ID, "PAS123553",
                 "123985332");
         Guest guest8 = new Guest("Ms", "Karolina", "Iksinska", Gender.FEMALE, DocumentType.DRIVER_LICENSE,
                 "DRI132511", "898123532");
         Guest guest9 = new Guest("Mr", "Grzegorz", "Brzeczyszczykiewicz", Gender.MALE, DocumentType.PERSONAL_ID,
                 "AAA123123", "342089123");
         Guest guest10 = new Guest("Mrs", "John", "ToBeRemoved", Gender.MALE, DocumentType.DRIVER_LICENSE, "DRI132135",
                 "12312353");
         Guest guest11 = new Guest("Mr", "John", "ToBeRemoved1", Gender.MALE, DocumentType.DRIVER_LICENSE, "DRI132136",
                 "12312353");
         Guest guest12 = new Guest("Mr", "John", "ToBeRemoved2", Gender.MALE, DocumentType.DRIVER_LICENSE, "DRI132137",
                 "12312353");
         Guest guest13 = new Guest("Mr", "John", "ToBeRemoved3", Gender.MALE, DocumentType.DRIVER_LICENSE, "DRI132138",
                 "12312353");
         Guest guest14 = new Guest("Mr", "John", "ToBeRemoved4", Gender.MALE, DocumentType.DRIVER_LICENSE, "DRI132139",
                 "12312353");
         Guest guest15 = new Guest("Mr", "John", "ToBeRemoved5", Gender.MALE, DocumentType.DRIVER_LICENSE, "DRI132110",
                 "12312353");
         Collection<Guest> guests = Arrays.asList(guest1, guest2, guest3, guest4, guest5, guest6, guest7, guest8,
                 guest9, guest10, guest11, guest12, guest13, guest14, guest15);
         for (Guest guest : guests)
         {
             guest.setNationality("Polish");
         }
 
 
         Reservation reservation = new Reservation(Id.NO_ID, guest1, L100.rackRate(),
                 new DateTime(new DateMidnight(2012, 12,
                         1)),
                 new DateTime(new DateMidnight(2012, 12, 5)), 2, 0, 0, ReservationStatus.INHOUSE);
         Reservation reservation2 = new Reservation(Id.NO_ID, guest2, L102.rackRate(),
                 new DateTime(new DateMidnight(2012,
                         12, 3)),
                 new DateTime(new DateMidnight(2012, 12, 8)), 2, 0, 0, ReservationStatus.RESERVED);
         Reservation reservation3 = new Reservation(Id.NO_ID, guest2, rate1_C103, new DateTime(new DateMidnight(2012,
                 12, 2)),
                 new DateTime(new DateMidnight(2012, 12, 15)), 3, 0, 0, ReservationStatus.RESERVED);
         Reservation reservation4 = new Reservation(Id.NO_ID, guest3, rate1_N105, new DateTime(new DateMidnight(2012,
                 12, 10)),
                 new DateTime(new DateMidnight(2012, 12, 14)), 2, 0, 0, ReservationStatus.RESERVED);
         Reservation reservation5 = new Reservation(Id.NO_ID, guest5, rate2_C104, new DateTime(new DateMidnight(2012,
                 12, 20)),
                 new DateTime(new DateMidnight(2012, 12, 28)), 1, 0, 0, ReservationStatus.RESERVED);
        Reservation reservation6 = new Reservation(Id.NO_ID, guest5, rate2_C104, new DateTime(DateMidnight.now().plusDays(1)),
                 new DateTime(DateMidnight.now().plusDays(5)), 1, 0, 0, ReservationStatus.RESERVED);
 
         Collection<Reservation> reservations = Arrays.asList(reservation, reservation2, reservation3, reservation4,
                 reservation5, reservation6);
 
         // bootstrapping data
         session.beginTransaction();
         logger.debug("adding room types:");
         for (RoomType type : types)
         {
             session.save(type);
             logger.debug("room type: " + type.toString());
         }
         logger.debug("adding rooms");
         for (Room room : rooms)
         {
             logger.debug("room: " + room.toString());
             session.save(room);
         }
 
         logger.debug("adding seasons");
         for (Season s : seasons)
         {
             logger.debug("season: " + s.toString());
             session.save(s);
         }
 
         logger.debug("adding season rates");
         for (Rate rate : rates)
         {
             logger.debug("rate: " + rate.toString());
             session.save(rate);
         }
 
         logger.debug("adding guests");
         for (Guest guest : guests)
         {
             logger.debug("guest: " + guest.toString());
             session.save(guest);
         }
 
         logger.debug("adding reservations");
         for (Reservation res : reservations)
         {
             logger.debug("reservation: " + res.toString());
             session.save(res);
         }
 
         session.getTransaction().commit();
         logger.debug("Finished bootstrapping database");
     }
 
     @Override
     public void stop()
     {
     }
 }
