 package cz.muni.fi.pv168;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Collections;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sql.DataSource;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /**
  * Created by IntelliJ IDEA.
  * User: fivekeyem, janinko
  * Date: 3/14/11
  */
 public class BorrowManagerImpl implements BorrowManager {
 
     private DataSource ds;
     private static final Logger logger = Logger.getLogger(
         CustomerManagerImpl.class.getName());
     CDManager cdManager;
     CustomerManager customerManager;
 
 
     public BorrowManagerImpl(CDManager cdManager, CustomerManager customerManager) {
         this.cdManager = cdManager;
         this.customerManager = customerManager;
     }
 
 
     public void setDs(DataSource ds) {
         this.ds = ds;
     }
 
     public Borrow createBorrow(Borrow borrow) {
         if (borrow == null) {
             throw new NullPointerException("borrow");
         }
         if (borrow.getId() != 0) {
             throw new IllegalArgumentException("id is set");
         }
         if (borrow.getCd() == null) {
             throw new NullPointerException("CD isn't set");
         }
         if (borrow.getCd().getId() == 0) {
            throw new IllegalArgumentException("CD id isn't set");
         }
         if (borrow.getCustomer() == null) {
             throw new NullPointerException("customer isn't set");
         }
         if (borrow.getCustomer().getId() == 0) {
            throw new IllegalArgumentException("customer id isn't set");
         }
      /*   if (borrow.getFrom() == null) {                              // TODO
             throw new NullPointerException("From date isn't set");
         }
         if (borrow.getTo() == null) {
             throw new NullPointerException("To date isn't set");
         }*/
 
         Connection conn = null;
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement("INSERT INTO borrows (cdid, customerid, active) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
             st.setInt(1, borrow.getCd().getId());
             st.setInt(2, borrow.getCustomer().getId());
            /* Timestamp from = new Timestamp(               //    TODO
                     borrow.getFrom().get(Calendar.YEAR),
                     borrow.getFrom().get(Calendar.MONTH),
                     borrow.getFrom().get(Calendar.DAY_OF_MONTH),
                     borrow.getFrom().get(Calendar.HOUR_OF_DAY),
                     borrow.getFrom().get(Calendar.MINUTE),
                     borrow.getFrom().get(Calendar.SECOND),
                     0);
             st.setString(3, from.toString());
             Timestamp to = new Timestamp(
                     borrow.getTo().get(Calendar.YEAR),
                     borrow.getTo().get(Calendar.MONTH),
                     borrow.getTo().get(Calendar.DAY_OF_MONTH),
                     borrow.getTo().get(Calendar.HOUR_OF_DAY),
                     borrow.getTo().get(Calendar.MINUTE),
                     borrow.getTo().get(Calendar.SECOND),
                     0);
             st.setString(4, to.toString());*/
             st.setInt(3, (borrow.getActive()?1:0));
 
             int count = st.executeUpdate();
             assert count == 1;
 
             int id = HelperDB.getId(st.getGeneratedKeys());
             borrow.setId(id);
 
             return borrow;
         } catch (SQLException ex) {
             logger.log(Level.SEVERE, "Error when inserting borrow into DB", ex);
             throw new RuntimeException("Error when inserting borrow into DB", ex);
         } finally {
             HelperDB.closeConn(conn);
         }
     }
 
     public Borrow deleteBorrow(Borrow borrow) {
         if (borrow == null) {
             throw new NullPointerException("customer");
         }
         Connection conn = null;
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement("DELETE FROM borrows WHERE id=?");
             st.setInt(1, borrow.getId());
             if (st.executeUpdate() == 0) {
                 throw new IllegalArgumentException("borrow not found");
             }
             return borrow;
         } catch (SQLException ex) {
             logger.log(Level.SEVERE, "Error when deleting borrow from DB", ex);
             throw new RuntimeException("Error when deleting borrow from DB", ex);
         } finally {
             HelperDB.closeConn(conn);
         }
     }
 
     public Borrow updateBorrow(Borrow borrow) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public SortedSet<Borrow> getAllBorrows() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public Borrow getBorrowById(int id) {
         if (id == 0) {
             throw new IllegalArgumentException("id");
         }
         Connection conn = null;
         try {
             conn = ds.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT * FROM borrows WHERE id=?");
             st.setInt(1, id);
             ResultSet rs = st.executeQuery();
             Borrow borrow = null;
             if (rs.next()) {
                 borrow = new Borrow();
                 borrow.setId(rs.getInt("id"));
                 borrow.setCd(cdManager.getCDById(rs.getInt("cdid")));
                 borrow.setCustomer(customerManager.getCustomerById(rs.getInt("customerid")));
                 // TODO date
                 borrow.setActive((rs.getInt("active")==1?true:false));
                 if (rs.next()) {
                     throw new RuntimeException("borrow");
                 }
             }
             return borrow;
 
         } catch (SQLException ex) {
             logger.log(Level.SEVERE, "Error when getting borrow by id from DB", ex);
             throw new RuntimeException("Error when getting borrow by id from DB", ex);
         } finally {
             HelperDB.closeConn(conn);
         }
     }
 }
