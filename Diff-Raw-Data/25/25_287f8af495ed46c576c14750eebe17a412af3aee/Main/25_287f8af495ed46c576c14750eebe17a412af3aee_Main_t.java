 /*
  */
 package prosjekt;
 
import prosjekt.booking.BookingRegistry;
import prosjekt.guests.GuestRegistry;
 import prosjekt.interfaces.AdminWindow;
 import prosjekt.rooms.RoomRegistry;
 import prosjekt.rooms.types.SingleRoom;
 
 /**
  *
  * @author Kristoffer Berdal <web@flexd.net>
  */
 public class Main {
  public static RoomRegistry roomRegistry = new RoomRegistry();
  public static BookingRegistry bookingRegistry = new BookingRegistry();
  public static GuestRegistry guestRegistry = new GuestRegistry();
  
  private static void setupRooms() {
     for (int i = 0; i < 10; i++) {
       SingleRoom room = new SingleRoom(500f);
       roomRegistry.add(room);
     }
    
  }
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    setupRooms();
    // Open the login window
    //new LoginWindow();
     new AdminWindow();
   }
 }
