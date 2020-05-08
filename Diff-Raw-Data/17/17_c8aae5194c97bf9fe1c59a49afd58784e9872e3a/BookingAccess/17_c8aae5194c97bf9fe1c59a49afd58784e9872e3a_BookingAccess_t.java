 /*
  * 
  */
 package DAL;
 
 import BE.Booking;
 import BE.Court;
 import BE.Member;
 import com.microsoft.sqlserver.jdbc.SQLServerException;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 /**
  *
  * @author boinq
  */
 public class BookingAccess {
     /* Management */
     // Instance variables \\
     private Connector connector;
     private static BookingAccess instance = null;
     
     // Constructor \\
     /**
      * Constructs an instance of the MemberAccess class
      *
      * @throws FileNotFoundException if the config file is not found
      * @throws IOException if there's an error reading the config file
      */
     private BookingAccess()throws FileNotFoundException, IOException
     {
         connector = Connector.getInstance();
     }
 
     // Singleton \\
     /**
      * Returns an instance of the MemberAccess class, and creates one first, if
      * there's none available
      *
      * @return an instance of the MemberAccess class
      * @throws FileNotFoundException if the config file can't be found
      * @throws IOException if there's an error reading the config file
      */
     public static BookingAccess getInstance() throws FileNotFoundException, IOException
     {
         if (instance == null)
         {
             instance = new BookingAccess();
         }
         return instance;
      }
     
     public List<Court> getAllCourts() throws SQLServerException, SQLException
     {
         try (Connection con = connector.getConnection())
         {
             List<Court> court = new ArrayList<>();
             String sql = "SELECT * FROM Court";
             PreparedStatement ps = con.prepareStatement(sql);
             
             ResultSet rs = ps.executeQuery();
 
             while (rs.next())
             {
                 court.add(getOneCourt(rs));
             }
             
             return court;
         }
     }
     
     public List<Booking> getAllBookings() throws SQLException
     {
         try (Connection con = connector.getConnection())
         {
             String sql = 
             "select CourtBooking.*, [Member/Booking].MemberId"
             + " from CourtBooking"
             + " Left Join [Member/Booking]"
             + " on CourtBooking.Id=[Member/Booking].CourtBookingId";
             
             PreparedStatement ps = con.prepareStatement(sql);
             
             ResultSet rs = ps.executeQuery();
             
             ArrayList<Integer> memberId = new ArrayList();
             ArrayList<Booking> bookings = new ArrayList();
             
            if(!(rs.next()))return null;
            outer:
            while(true)
             {
                int id = rs.getInt("Id");
                int courtid=rs.getInt("CourtId");     
                
                Date sqldate = rs.getDate("DateFrom");
                GregorianCalendar fromDate =  (GregorianCalendar) GregorianCalendar.getInstance();               
                fromDate.setTimeInMillis(sqldate.getTime());
                
                sqldate = rs.getDate("DateTo");
                GregorianCalendar toDate =  (GregorianCalendar) GregorianCalendar.getInstance();               
                toDate.setTimeInMillis(sqldate.getTime());
                        
                memberId.add(rs.getInt("MemberId"));
                
                while(rs.next())
                {
                    
                    if(rs.getInt("Id")==id)
                    {
                        memberId.add(rs.getInt("MemberId"));                       
                    }                       
                    else
                    {
                        bookings.add(Booking.createSimple(id, courtid, fromDate, toDate, memberId));
                       
                       memberId= new ArrayList();
                       
                       continue outer;
                        
                    }
               } 
               break;
             }
             return bookings;
         }
     }
     
     public int addBooking(Booking booking) throws SQLException
     {
         try (Connection con = connector.getConnection())
         {
             String sql=
             "insert into CourtBooking "
             + " values(?,?,?)";
             
             PreparedStatement ps = con.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
             
             ps.setInt(1, booking.getCourtId());
             Date from = new Date(booking.getFromDate().getTimeInMillis());
             Date to = new Date(booking.getToDate().getTimeInMillis());
             ps.setDate(2,from);
             ps.setDate(3, to);
             
             int affectedRows = ps.executeUpdate();
             if (affectedRows < 1)
             {
                 throw new SQLException("Unable to InserBooking");
             }
             
             ResultSet rs = ps.getGeneratedKeys();
             rs.next();
             int key = rs.getInt(1);
             
             for(Integer i:booking.getMemberId())
             {
                 sql=
                 "insert into [Member/Booking]"
                 + " values(?,?)";
                 
                 ps = con.prepareStatement(sql);
                 ps.setInt(1, i);
                 ps.setInt(2, key);
                 
                 
                 affectedRows = ps.executeUpdate();
                 if (affectedRows < 1)
                 {
                     throw new SQLException("Unable to InserBooking");
                 }
             }
             
             return key;
             
         }
     }
     
     private Court getOneCourt(ResultSet rs) throws SQLException
     {
         int id = rs.getInt("Id");
         int number = rs.getInt("Number");
         String type = rs.getString("Type");
         
         return Court.fromDataBase(id,number,type);
     }
     
  
 }
