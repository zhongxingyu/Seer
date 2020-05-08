 package de.hswt.hrm.common.locking.jdbc;
 
 import java.sql.SQLException;
 
 import org.junit.Test;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import static org.junit.Assert.*;
 
public class SessionTest {
     private final String LABEL = "Test-Session";
     
     @Test
     public void testBeginSession() throws DatabaseException, SQLException {
         SessionService service = SessionService.getInstance();
         Session session = service.startSession(LABEL);
         
         assertNotNull("Session not created.", session);
         assertEquals("Label is not set correctly.", LABEL, session.getLabel());
         assertNotNull("Timestamp not set correctly.", session.getTimestamp());
         assertNotNull("UUID not set correctly.", session.getUuid());
         assertTrue("UUID not set correctly.", session.getUuid().length() > 0);
     }
     
     @Test(expected=ElementNotFoundException.class)
     public void testEndSession() throws DatabaseException, SQLException {
         SessionService service = SessionService.getInstance();
         SessionDao dao = SessionDao.getInstance();
         
         Session session = service.startSession(LABEL);
         assertNotNull("Session not created.", session);
         
         service.endSession(session);
         
         dao.findByUuid(session.getUuid());
     }
     
    @Test(expected=NullPointerException.class)
     public void testSessionWithoutLabel() throws DatabaseException, SQLException {
         SessionService service = SessionService.getInstance();
         
         service.startSession(null);
     }
 }
