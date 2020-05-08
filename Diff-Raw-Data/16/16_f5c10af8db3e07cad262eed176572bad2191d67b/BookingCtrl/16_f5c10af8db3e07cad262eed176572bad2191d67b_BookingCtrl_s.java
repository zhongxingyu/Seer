 package hotel.controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import hotel.core.Booking;
 import hotel.core.Guest;
 import hotel.core.Room;
 import hotel.DB.BookingDB;
 import hotel.utils.DateUtils;
 import hotel.utils.IdGenerator;
 
 public class BookingCtrl
 {
     private String hotelName;
     private ArrayList<Booking> bookings;
     private BookingDB bookingDB;
     private IdGenerator idGenerator;
     
     public BookingCtrl(String hotelName)
     {
         this.hotelName = hotelName;
         bookingDB = new BookingDB();
         bookings = bookingDB.getBookingsByHotel(hotelName);
         idGenerator = new IdGenerator();
         for (int i = 0; i < bookings.size(); i++)
             idGenerator.addId(bookings.get(i).getId());
     }
     
     public String getHotelName()
     {
         return hotelName;
     }
     
     public void setHotelName(String hotelName)
     {
         this.hotelName = hotelName;
         bookings = bookingDB.getBookingsByHotel(hotelName);
     }
     
     public void addBooking(Room room, Guest guest, Date arrivalDate, 
             Date leavingDate, int discount)
     {
         if (arrivalDate.before(DateUtils.getToday()))
             throw new IllegalStateException("Arrival date can not be in "
                     + "the past.");
         ArrayList<Booking> roomBookings = bookingDB.
                getBookingsByRoom(room.getRoomNr());
         
         
         for (int i = 0; i < roomBookings.size(); i++)
         {
             Booking booking = roomBookings.get(i);
             
             if (arrivalDate.compareTo(booking.getArrivalDate()) >= 0 && 
                     arrivalDate.compareTo(booking.getLeavingDate()) <= 0)
                 throw new IllegalStateException("The room will not be "
                         + "available at specified arrival date.");
             
             if(leavingDate.compareTo(booking.getArrivalDate()) >= 0 && 
                     leavingDate.compareTo(booking.getLeavingDate()) <= 0)
                 throw new IllegalStateException("There are some other guests "
                         + "who will be staying at the specified period.");
         }
         
         double totalPrice = room.getCost();
         ExpenseCtrl expenseCtrl = new ExpenseCtrl(guest.getName());
         
         totalPrice += expenseCtrl.getTotalExpenses();
         
         if (discount > totalPrice)
             throw new IllegalStateException("Discount can not be higher than"
                     + " total price.");
         int id = idGenerator.getNextId();
         
         Booking booking = new Booking(id, room, guest, arrivalDate, leavingDate, 
                 discount);
         bookings.add(booking);
         bookingDB.insertBooking(booking, hotelName);
     }
     
     public void addBooking(Guest guest, int nrOfGuests, Date arrivalDate, 
             Date leavingDate, int discount)
     {
         if (arrivalDate.before(DateUtils.getToday()))
             throw new IllegalStateException("Arrival date can not be in "
                     + "the past.");
         
         RoomCtrl roomCtrl = new RoomCtrl(hotelName);
         ArrayList<Room> roomsToCheck = new ArrayList<Room>();
         Room foundRoom = null;
         
         for(int i = 0; i < roomCtrl.getRoomCount(); i++)
         {
             Room room = roomCtrl.getRoomById(i);
             if (room.getNrOfBedrooms() >= nrOfGuests)
                 roomsToCheck.add(room);
         }
         
         for (int i = 0; i < roomsToCheck.size(); i++)
         {
             Room room = roomsToCheck.get(i);
             if (isRoomAvailable(room, arrivalDate, leavingDate))
             {
                 foundRoom = room;
                 break;
             }
         }
         
         if (foundRoom == null)
             throw new IllegalStateException("There are no available rooms that"
                     + " can hold the specified number of guests at the "
                     + "specified time interval.");
         
         double totalPrice = foundRoom.getCost();
         ExpenseCtrl expenseCtrl = new ExpenseCtrl(guest.getName());
         
         totalPrice += expenseCtrl.getTotalExpenses();
         
         if (discount > totalPrice)
             throw new IllegalStateException("Discount can not be higher than"
                     + " total price.");
         int id = idGenerator.getNextId();
         
         Booking booking = new Booking(id, foundRoom, guest, arrivalDate, 
                 leavingDate, discount);
         bookings.add(booking);
         bookingDB.insertBooking(booking, hotelName);
     }
     
     public void editBooking(int id, Guest guest, int nrOfGuests, 
             Date arrivalDate, Date leavingDate, int discount)
     {
         RoomCtrl roomCtrl = new RoomCtrl(hotelName);
         ArrayList<Room> roomsToCheck = new ArrayList<Room>();
         Room foundRoom = null;
         
         for(int i = 0; i < roomCtrl.getRoomCount(); i++)
         {
             Room room = roomCtrl.getRoomById(i);
             if (room.getNrOfBedrooms() == nrOfGuests)
                 roomsToCheck.add(room);
         }
         
         for (int i = 0; i < roomsToCheck.size(); i++)
         {
             Room room = roomsToCheck.get(i);
             if (isRoomAvailable(room, arrivalDate, leavingDate))
             {
                 foundRoom = room;
                 break;
             }
         }
         
         if (foundRoom == null)
             throw new IllegalStateException("There are no available rooms that"
                     + " can hold the specified number of guests.");
         
         double totalPrice = foundRoom.getCost();
         ExpenseCtrl expenseCtrl = new ExpenseCtrl(guest.getName());
         
         totalPrice += expenseCtrl.getTotalExpenses();
         
         if (discount > totalPrice)
             throw new IllegalStateException("Discount can not be higher than"
                     + " total price.");
         
         int bookingId = bookings.get(id).getId();
         Booking booking = new Booking(bookingId, foundRoom, guest, arrivalDate, 
                 leavingDate, discount);
         bookingDB.updateBooking(booking, hotelName);
         bookings.set(id, booking);
     }
     
     private boolean isRoomAvailable(Room room, Date arrivalDate,
             Date leavingDate)
     {
         Booking[] bookingsToCheck = getBookingsByRoom(room);
         
         for (int i = 0; i < bookingsToCheck.length; i++)
         {
             Booking booking = bookingsToCheck[i];
             
             if (arrivalDate.compareTo(booking.getArrivalDate()) >= 0 && 
                     arrivalDate.compareTo(booking.getLeavingDate()) <= 0)
                 return false;
             
             if(leavingDate.compareTo(booking.getArrivalDate()) >= 0 && 
                     leavingDate.compareTo(booking.getLeavingDate()) <= 0)
                 return false;
         }
         
         return true;
     }
     
     public void editBooking(int bookingId, Room room, Guest guest, Date arrivalDate, 
             Date leavingDate, int discount)
     {
         for (int i = 0; i < bookings.size(); i++)
         {
             Booking booking = bookings.get(i);
             if (booking.getId() == bookingId)
                 continue;
             if (arrivalDate.after(booking.getArrivalDate()) && 
                     arrivalDate.before(booking.getLeavingDate()))
                 throw new IllegalStateException("The room will not be "
                         + "available at specified arrival date.");
             
             if(leavingDate.after(booking.getArrivalDate()) && 
                     leavingDate.before(booking.getLeavingDate()))
                 throw new IllegalStateException("There are some other guests "
                         + "who will be staying at the specified period.");
         }
         
         double totalPrice = room.getCost();
         ExpenseCtrl expenseCtrl = new ExpenseCtrl(guest.getName());
         
         totalPrice += expenseCtrl.getTotalExpenses();
         
         if (discount > totalPrice)
             throw new IllegalStateException("Discount can not be higher than"
                     + " total price.");
         //int bookingId = bookings.get(id).getId();
         Booking booking = new Booking(bookingId, room, guest, arrivalDate, 
                 leavingDate, discount);
         bookingDB.updateBooking(booking, hotelName);
         for (int i = 0; i < bookings.size(); i++)
         {
             if (bookings.get(i).getId() == bookingId)
             {
                 bookings.set(i, booking);
                 break;
             }
         }
     }
     
     public void removeBooking(int bookingId)
     {
         for (int i = 0; i < bookings.size(); i++)
         {
             if (bookings.get(i).getId() == bookingId)
             {
                 bookings.remove(i);
                 break;
             }
         }
         idGenerator.removeId(bookingId);
         bookingDB.delete(bookingId, hotelName);
     }
     
     public Booking[] getBookingsByRoom(Room room)
     {
        ArrayList<Booking> b = bookingDB.getBookingsByRoom(room.getRoomNr());
         
         return b.toArray(new Booking[b.size()]);
     }
     
     public Booking getBooking(int id)
     {
         return bookings.get(id);
     }
     
     public Booking getBookingByBookingId(int bookingId)
     {
         return bookingDB.getBooking(bookingId, hotelName);
     }
     
     public int getBookingCount()
     {
         return bookings.size();
     }
 }
