 import Global.Imps.DBServiceImp;
 import Global.MechanicSales;
 import Global.MessageSystem;
 import Global.MsgSystem.MessageSystemImp;
 import Global.Ticket;
 import Global.User;
 import Global.mechanics.MechanicSalesImp;
 import Global.mechanics.SingleTicket;
 import Global.mechanics.TicketImp;
 import Global.mechanics.UserImp;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 
 /**
  * Author: artemlobachev
  * Date: 14.12.13
  *
  * This is an Integration test. It test how DBServiceImp + MessageSystem + MechanicSales work together.
  * If something works wrong, first became sure that unit tests for that modules work correct.
  */
 public class MechanicSalesImpTest {
     public static final int SLEEP_TIME = 100;
     private MessageSystem messageSystem;
     private DBServiceImp dbServiceImp;
     private MechanicSalesImp mechanicSales;
 
     @Before
     public void setUp() throws SQLException {
         messageSystem = new MessageSystemImp();
         dbServiceImp = new DBServiceImp(this.messageSystem, "TestPlaneDB");
         Thread thread = new Thread(dbServiceImp);
         thread.start();
         mechanicSales = new MechanicSalesImp(this.messageSystem);
         thread = new Thread(mechanicSales);
         thread.start();
     }
 
     @Test
     public void searchTest() throws InterruptedException {
         final String ErrText = "Search works wrong";
         HashMap<String, String> params = new HashMap<>();
        params.put(MechanicSales.findParams.ARRIVAL_AIRPORT,"Аэропрт_вылета1");
        params.put(MechanicSales.findParams.DEPARTURE_AIRPORT,"Аэропрт_вылета1");
        params.put(MechanicSales.findParams.DEPARTURE_DATE_TIME_SINCE, "0");
         Collection<Ticket> rez = mechanicSales.search(params);
         Thread.sleep(SLEEP_TIME);
         Assert.assertNull(ErrText, rez);
        params.put(MechanicSales.findParams.DEPARTURE_DATE_TIME_TO,"1000");
         rez = mechanicSales.search(params);
         Thread.sleep(SLEEP_TIME);
         Assert.assertEquals(ErrText, rez.size(), 1);
         //System.out.println(rez.iterator().next().getArrivalAirport());
         //System.out.println(rez.size());
     }
 
     @Test
     public void buyTest() throws InterruptedException {
         final String ErrText = "Buy works wrong";
         User tstUser = new UserImp("Valera");
 
         ArrayList<SingleTicket> singleTickets = new ArrayList<>(3);
        //date = 7.12.13 15:00, flight time = 14hr
        SingleTicket singleTicket = new SingleTicket("DME", "Vegas",new Date(1386428400),50400,"hangover-1", Ticket.seatClass.SEAT_CLASS_FIRST,"Boing 747-400",1);
         singleTickets.add(singleTicket);
        //date = 10.12.13 12:00, flight time = 12hr
        singleTicket = new SingleTicket("Vegas", "Bangkok",new Date(1386676800),43200,"hangover-2", Ticket.seatClass.SEAT_CLASS_ECONOMIC,"Boing 737-800",2);
         singleTickets.add(singleTicket);
        //date = 15.12.13 10:00, flight time = 5hr
        singleTicket = new SingleTicket("Bangkok", "MIA",new Date(1387101600),18000,"hangover-3", Ticket.seatClass.SEAT_CLASS_FIRST,"Airbus A330",3);
        singleTickets.add(singleTicket);
        //total time = 8d = 691200 s
        Ticket tstTicket = new TicketImp(null, singleTickets, false);
 
         boolean rez = this.mechanicSales.buy(tstTicket,tstUser);
         Thread.sleep(SLEEP_TIME);
         Assert.assertTrue(ErrText, rez);
 
     }
 
 }
