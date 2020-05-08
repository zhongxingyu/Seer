 package hotel.DB;
 
 import java.sql.*;
 import java.util.ArrayList;
 import hotel.core.Booking;
 import hotel.core.Guest;
 import hotel.core.Room;
 
 public class BookingDB
 {
     private Connection con;
     
     public BookingDB()
     {
         con = DBConnection.getInstance().getDBConnection();
     }
     
     public ArrayList<Booking> getBookings()
     {
         return where("");
     }
     
     public ArrayList<Booking> getBookingsByHotel(String name)
     {
         return where("hotelName ='" + name + "'");
     }
         
     public Booking getBooking(int id, String hotelName)
     {
         return singleWhere("id = " + id + " AND hotelName='" + hotelName + "'");
     }
     
    public ArrayList<Booking> getBookingsByRoom(int roomNr, String hotelName)
     {
        return where("roomNr=" + roomNr + " AND hotelName='"+hotelName + "'");
     }
     
     private Booking singleWhere(String wClause)
     {
         ResultSet results;
         String query = buildQuery(wClause);
         Booking booking = null;
         try
         {
             Statement stmt = con.createStatement();
             stmt.setQueryTimeout(5);
             results = stmt.executeQuery(query);
             if(results.next())
             {
                 booking = createBooking(results);
             }
 
             stmt.close();
         }
         catch (Exception e)
         {
             System.out.println(e.getMessage());
         }
         
         return  booking;
     }
 
     private ArrayList<Booking> where(String wClause)
     {
         ResultSet results;
         ArrayList<Booking> list = new ArrayList<Booking>();
         String query = buildQuery(wClause);
         
         try
         {
             Statement stmt = con.createStatement();
             stmt.setQueryTimeout(5);
             results = stmt.executeQuery(query);
             while(results.next())
             {
                 Booking booking = createBooking(results);          
                 list.add(booking);
             }
             stmt.close();
         }
         catch (Exception e)
         {
             System.out.println(e.getMessage());
         }
         
         return list;
     }
 
     private String buildQuery(String whereC)
     {
         String query = "SELECT * FROM bookings";
         if (!whereC.isEmpty())
         {
             query = query + " WHERE " + whereC;
         }
         return query;
     }
     
     private Booking createBooking(ResultSet rs)
     {
         try
         {
             int id = rs.getInt("id");
             int roomNr = rs.getInt("roomNr");
             String guestName = rs.getString("guestName");
             int discount = rs.getInt("discount");
             Date arrivalDate = rs.getDate("arrivalDate");
             Date leavingDate = rs.getDate("leavingDate");
             String hotelName = rs.getString("hotelName");
             
             RoomDB roomDB = new RoomDB();
             Room room = roomDB.getRoom(roomNr, hotelName);
             GuestDB guestDB = new GuestDB();
             Guest guest = guestDB.getGuest(guestName, hotelName);
             
             Booking booking = new Booking(id, room, guest, arrivalDate, 
                     leavingDate, discount);
             
             return booking;
         }
         catch (SQLException e)
         {
             System.out.println(e.getMessage());
         }
         
         return null;
     }
     
     public int delete(int id, String hotelName)
     {
         //row count
         int rc = -1;
         String query = "DELETE FROM bookings WHERE id=" + id + " AND "
                 + "hotelName='" + hotelName + "'";
         try
         {
             Statement stmt = con.createStatement();
             stmt.setQueryTimeout(5);
             rc = stmt.executeUpdate(query);
             stmt.close();
         }
         catch (Exception e)
         {
             System.out.println(e.getMessage());
         }
         
         return rc;
     }
     
     public String getHotelName(Booking booking)
     {
         String query = "SELECT hotelName FROM bookings WHERE id =" + 
                 booking.getId();
         ResultSet results;
         String hotelName = null;
         try
         {
             Statement stmt = con.createStatement();
             stmt.setQueryTimeout(5);
             results = stmt.executeQuery(query);
             if(results.next())
             {
                 hotelName = results.getString("hotelName");
             }
 
             stmt.close();
         }
         catch (Exception e)
         {
             System.out.println(e.getMessage());
         }
         
         return hotelName;
     }
     
     //@SuppressWarnings("empty-statement")
     public int insertBooking(Booking booking, String hotelName)
     {
         int rc = -1;
         Date arrivalDate = new Date(booking.getArrivalDate().getTime());
         Date leavingDate = new Date(booking.getLeavingDate().getTime());
         String query = "INSERT INTO bookings(id, roomNr, guestName, "
                 + "arrivalDate, leavingDate, discount, hotelName) VALUES('"
                 + booking.getId() + "','" 
                 + booking.getRoom().getRoomNr() + "','" 
                 + booking.getGuest().getName() + "','" 
                 + arrivalDate + "','" 
                 + leavingDate + "','" 
                 + booking.getDiscount() + "','" 
                 + hotelName + "')";
         try
         {
             con.setAutoCommit(false);
             Statement stmt = con.createStatement();
             stmt.setQueryTimeout(5);
             rc = stmt.executeUpdate(query);
             stmt.close();
             con.commit();
             con.setAutoCommit(true);
             
         }
         catch (SQLException e)
         {
             System.out.println(e.getMessage());
             try
             {
                 rc = -1;
                 con.rollback();
                 con.setAutoCommit(true);
             }
             catch (Exception ex)
             {
                 System.out.println(ex.getMessage());
             }
         }
         return rc;
     }
     
     public int updateBooking(Booking booking, String hotelName)
     {
         int rc = -1;
         Date arrivalDate = new Date(booking.getArrivalDate().getTime());
         Date leavingDate = new Date(booking.getLeavingDate().getTime());
         
         String query = "Update bookings SET "+
                 "roomNr ='" + booking.getRoom().getRoomNr() + "', "+
                 "guestName ='" + booking.getGuest().getName() + "', "+
                 "arrivalDate ='" + arrivalDate + "', "+
                 "leavingDate ='" + leavingDate + "', "+
                 "discount ='" + booking.getDiscount() + "' "+
                 "WHERE id=" + booking.getId() + " AND hotelName='" + 
                 hotelName + "'";
         try
         {
             Statement stmt = con.createStatement();
             stmt.setQueryTimeout(5);
             rc = stmt.executeUpdate(query);
             stmt.close();
         }
         catch (Exception e)
         {
             System.out.println(e.getMessage());
         }
         return rc;
     }
 }
