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
        params.put(MechanicSales.findParams.DEPARTURE_AIRPORT,"Тапоа");
        params.put(MechanicSales.findParams.ARRIVAL_AIRPORT,"Пашковский");
        params.put(MechanicSales.findParams.DEPARTURE_DATE_TIME_SINCE, "1356067604");
         Collection<Ticket> rez = mechanicSales.search(params);
         Thread.sleep(SLEEP_TIME);
         Assert.assertNull(ErrText, rez);
        params.put(MechanicSales.findParams.DEPARTURE_DATE_TIME_TO,"1419139604");
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
        SingleTicket singleTicket = new SingleTicket("Тапоа", "Пашковский",new Date(1384584530),50400, "934", Ticket.seatClass.SEAT_CLASS_ECONOMIC,"Boing 747-400",1);
         singleTickets.add(singleTicket);
        singleTicket = new SingleTicket("Пашковский", "Bangkok",new Date(1386676800),43200,"935", Ticket.seatClass.SEAT_CLASS_ECONOMIC,"Boing 737-800",2);
         singleTickets.add(singleTicket);
        Ticket tstTicket = new TicketImp(null, singleTickets, false, 0L);
 
         boolean rez = this.mechanicSales.buy(tstTicket,tstUser);
         Thread.sleep(SLEEP_TIME);
         Assert.assertTrue(ErrText, rez);
 
     }
 
 }
