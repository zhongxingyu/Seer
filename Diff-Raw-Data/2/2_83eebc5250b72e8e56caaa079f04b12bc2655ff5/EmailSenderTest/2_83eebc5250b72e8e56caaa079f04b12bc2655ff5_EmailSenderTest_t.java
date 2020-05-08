 package jday.util;
 
 import static org.junit.Assert.*;
 import jday.entities.BookingNo;
 import jday.entities.Member;
 
 import org.junit.Test;
 
 public class EmailSenderTest {
//
 	@Test
 	public void testSendMessage() {
 		Member m = new Member();
 		m.setEmail("d.yuwen.yw@gmail.com");
 		BookingNo bookno =new BookingNo(); 
 		
 		EmailSender sender = new EmailSender(bookno.setBookingNo(),m);
 		
 		
 	}
 
 }
