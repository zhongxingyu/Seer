 /*
  * Copyright by Apaq 2011-2013
  */
 package dk.drb.blacktiger.controller;
 
import java.util.Date;
 import dk.drb.blacktiger.model.Participant;
 import static org.junit.Assert.*;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 
 /**
  *
  */
 public class RoomControllerTest {
     
     @Test
     public void testKick() {
         MockService service = new MockService();
         String roomNo = "123";
         RoomController instance = new RoomController(service);
         instance.init();
         
         assertEquals(0, instance.showRoomAsJson("09991").size());
        service.addParticipant(new Participant("123", "12341234", true, new Date()));
         assertEquals(1, instance.showRoomAsJson("09991").size());
         service.kickParticipant("09991", "123");
     }
     
     @Test
     @Ignore
     public void testListenForChange() {
         System.out.println("listenForChange");
         MockHttpServletRequest request = new MockHttpServletRequest();
         MockHttpServletResponse response = new MockHttpServletResponse();
         MockService service = new MockService();
         String roomNo = "123";
         RoomController instance = new RoomController(service);
         instance.init();
         instance.listenForChange(request, response, roomNo);
         
         service.fireJoinEvent(roomNo, "321");
         
     }
 
     
 }
