 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ws.testcalendar;
 
 import dk.dtu.imm.ws.calendar.*;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 /**
  *
  * @author Wojtek
  */
 public class addGetAppointmentTest {
 
     @Test
     public void addGetAppointment1() throws DatatypeConfigurationException {
 
         CalendarService service = new CalendarService();
         CalendarServicePortType port = service.getCalendarServicePort();
 
         DatatypeFactory df = DatatypeFactory.newInstance();
         XMLGregorianCalendar part1 = df.newXMLGregorianCalendar("2012-09-17");
         java.lang.String part2 = "Appointment1";
 
         port.addApointment(part1, part2);
 
         java.lang.String result = port.getAppointment(part1);
         assertEquals("Appointment1", result);
 
     }
 
     @Test
     public void addGetAppointment2() throws DatatypeConfigurationException, InterruptedException {
 
         CalendarService service = new CalendarService();
         CalendarServicePortType port = service.getCalendarServicePort();
 
         DatatypeFactory df = DatatypeFactory.newInstance();
        XMLGregorianCalendar app1date = df.newXMLGregorianCalendar("2012-09-17");
        java.lang.String app1name = "Appointment1";
 
        port.addApointment(app1date, app1name);

        XMLGregorianCalendar app2date = df.newXMLGregorianCalendar("2012-09-17");
        java.lang.String app2name = "Appointment2";

        port.addApointment(app2date, app2name);
 
         // When you run both tests, what will happen if it takes,
         // e.g., 3 seconds for the Web service to add an appointment?
 
        Thread.sleep(3000); // Discuss whhat should happen; try it out and explain what really happens

        java.lang.String result = port.getAppointment(app1date);
         assertEquals("Appointment2", result);
         System.out.println(result);
 
     }
 }
