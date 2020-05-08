 package de.hswt.hrm.common.locking.jdbc;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.SQLTimeoutException;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.test.database.AbstractDatabaseTest;
 
 public class LockTest extends AbstractDatabaseTest {
     private static final SessionService SESSION_SERVICE = SessionService.getInstance();
     private Session session;
     private static final String PLANT = "Plant"; 
 
     @Before
     public void createSession() throws DatabaseException, SQLException {
         session = SESSION_SERVICE.startSession("Test-Session");
     }
     
     @Test
     public void testGetLock() {
         LockService service = new LockService(session.getUuid());
         Optional<Lock> lock = service.getLock(LockService.TBL_PLANT, 1);
         
         assertTrue("Could not get lock!", lock.isPresent());
         assertEquals("Lock is not for the correct table.", PLANT, lock.get().getTable());
         assertEquals("Lock is not for the correct id.", 1, lock.get().getFk());
     }
     
     @Test
     public void testGetLockTwice() {
         LockService service = new LockService(session.getUuid());
         Optional<Lock> lock = service.getLock(LockService.TBL_PLANT, 1);
         assertTrue("Could not get lock!", lock.isPresent());
         
         lock = service.getLock(LockService.TBL_PLANT, 1);
         assertFalse("Could get lock to already locked id.", lock.isPresent());
     }
     
     @Test
     public void testReleaseLock() {
         LockService service = new LockService(session.getUuid());
         Optional<Lock> lock = service.getLock(LockService.TBL_PLANT, 1);
         assertTrue("Could not get lock!", lock.isPresent());
         
         assertTrue("Could not release lock.", service.release(lock));
         
         lock = service.getLock(LockService.TBL_PLANT, 1);
         assertTrue("Could not get lock for released id.", lock.isPresent());
     }
     
     @Test
     public void testDatabaseNativeLock() throws DatabaseException, SQLException {
         try {
             // This test ensures that the database has enabled auto locking per row
             // this is MySQL / MariaDB specific feature of the InnoDB driver
             // because of this, the test should maybe be moved to an extra fragment
             // TODO: check if test could be moved to extra fragment or similar
             
             Connection outerCon = DatabaseFactory.getConnection();
             outerCon.setAutoCommit(false);
             PreparedStatement stmt = outerCon.prepareStatement(
                     "INSERT INTO `Lock` (Lock_Uuid_Fk, Lock_Table, Lock_Row_ID) "
                             + "VALUES (?, ?, ?);");
             stmt.setString(1, session.getUuid());
             stmt.setString(2, PLANT);
             stmt.setInt(3, 1);
             assertEquals("Could not insert new lock.", 1, stmt.executeUpdate());
             stmt.close();
             outerCon.commit();
             
             
             // Lock lock
             stmt = outerCon.prepareStatement(
                     "SELECT * FROM `Lock` WHERE Lock_Uuid_Fk = ? "
                             + "AND Lock_Table = ? "
                             + "AND Lock_Row_ID = ? "
                             + "LOCK IN SHARE MODE;");
             stmt.setString(1, session.getUuid());
             stmt.setString(2, PLANT);
             stmt.setInt(3, 1);
             ResultSet r = stmt.executeQuery();
             stmt.close();
             assertTrue("Could not retrieve lock.", r.next());
             
             // Inner Connection -> outer connection didn't close the transaction
             // so the line should be locked!
             Connection innerCon = DatabaseFactory.getConnection();
             stmt = innerCon.prepareStatement(
                     "DELETE FROM `Lock` "
                             + "WHERE Lock_Uuid_Fk = ? "
                             + "AND Lock_Table = ? "
                             + "AND Lock_Row_ID = ?;");
             stmt.setString(1, session.getUuid());
             stmt.setString(2, PLANT);
             stmt.setInt(3, 1);
             stmt.setQueryTimeout(5);
             assertEquals("Statement should not affect any rows.", 0 , stmt.executeUpdate());
             
             stmt.close();
             r.close();
             
             innerCon.close();
             outerCon.rollback();
             outerCon.close();
         }
         catch (SQLException e) {
             if (e.getMessage().equals("Lock wait timeout exceeded; try restarting transaction")
                     || e instanceof SQLTimeoutException) {
                 return;
             }
             
             throw e;
         }
     }
     
 }
