 /*
  * 
  */
 package BE;
 
 import java.text.SimpleDateFormat;
 
 /**
  *
  * @author boinq
  */
 public class BookingContainer 
 {
     private Booking bookingObject;
     
     public BookingContainer(Booking bookingObject)
     {
         this.bookingObject = bookingObject;
     }
     
     public String toString()
     {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy '-' HH:mm");
         SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
         return bookingObject.getMemberId() + " - " + bookingObject.getCourtId() + " - " + dateFormat.format(bookingObject.getFromDate().getTime()) + "-" + timeFormat.format(bookingObject.getToDate().getTime());
     }
 }
