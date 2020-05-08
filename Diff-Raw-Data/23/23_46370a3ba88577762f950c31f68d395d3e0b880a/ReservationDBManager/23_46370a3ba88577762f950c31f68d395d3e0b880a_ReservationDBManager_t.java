 package DAL;
 
 import BE.Reservation;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class ReservationDBManager extends TieBreakDBManager
 {
 
     /**
      * Constructor of the reservation db manager class
      * @throws Exception
      */
     public ReservationDBManager() throws Exception
     {
         super();
     }
 
     /**
      * Adds a court reservation to the database
      * @param r the reservation
      * @return the reservation with id and the reservation object
      * @throws SQLException
      */
     public Reservation reserveCourt(Reservation r) throws SQLException
     {
         try (Connection con = ds.getConnection())
         {
             String sql = "INSERT INTO Reservation VALUES(?, ?, ?)";
 
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
             //ps.setInt(1, r.getId());
             ps.setInt(1, r.getCourtId());
             ps.setInt(2, r.getMemberId());
             ps.setTimestamp(3, new java.sql.Timestamp(r.getReservationTime().getTimeInMillis()));
 
             int affectedRows = ps.executeUpdate();
             if (affectedRows == 0)
             {
                 throw new SQLException("Unable to book court.");
             }
             ResultSet keys = ps.getGeneratedKeys();
             keys.next();
             int id = keys.getInt(1);
             return new Reservation(id, r);
         }
     }
 
     /**
     * Pulls an ArrayList of the names of every court
     * @return An ArrayList with names
      * @throws SQLException
      */
     public ArrayList<String> getCourtsName() throws SQLException
     {
         try (Connection con = ds.getConnection())
         {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT CourtName FROM Courts");
 
             ArrayList courts = new ArrayList<>();
             while (rs.next())
             {
                 String CourtName = rs.getString("CourtName");
 
 
                 courts.add(CourtName);
             }
             return courts;
         }
     }
 
     /**
     * Pulls the ID of a court using the court name
     * @param name The name of the court the ID is being pulled from
     * @return ID of a court
      * @throws SQLException
      */
     public int getIdByName(String name) throws SQLException
     {
         try (Connection con = ds.getConnection())
         {
             Statement st = con.createStatement();
             String sql = ("SELECT Id FROM Courts WHERE CourtName = ?");
             PreparedStatement ps = con.prepareStatement(sql);
             ps.setString(1, name);
 
             ResultSet rs = ps.executeQuery();
             if (rs.next())
             {
                 int id = rs.getInt("Id");
 
                 return id;
             }
         }
         return 0;
     }
     
      /**
     * Pulls the name of a court using the court ID
     * @param id The ID of the court the name is being pulled from
     * @return Name of a court
      * @throws SQLException
      */
     public String getNameById(int id) throws SQLException
     {
         try (Connection con = ds.getConnection())
         {
             Statement st = con.createStatement();
             String sql = ("SELECT CourtName FROM Courts WHERE ID = ?");
             PreparedStatement ps = con.prepareStatement(sql);
             ps.setInt(1, id);
 
             ResultSet rs = ps.executeQuery();
             if (rs.next())
             {
                 String name = rs.getString("CourtName");
 
                 return name;
             }
         }
         return null;
     }
      
     /**
     * Pulls an ArrayList of every court reservation in the database
     * @return All court reservations in an ArrayList
      * @throws SQLException
      */
     public ArrayList<Reservation> getReservations() throws SQLException
     {
         try (Connection con = ds.getConnection())
         {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Reservation");
 
             ArrayList<Reservation> reservations = new ArrayList<>();
             while (rs.next())
             {
                 int courtId = rs.getInt("CourtID");
                 int memberId = rs.getInt("MemberId");
                 Timestamp rd = rs.getTimestamp("ReservationDate");
                 
                 Calendar time = new GregorianCalendar();
                 time.set(rd.getYear() + 1900, rd.getMonth(), rd.getDate(), rd.getHours(), 0, 0);
                 
                 Reservation res = new Reservation(courtId, memberId, time);
                 
                 reservations.add(res);
             }
             return reservations;
         }
     }
 }
