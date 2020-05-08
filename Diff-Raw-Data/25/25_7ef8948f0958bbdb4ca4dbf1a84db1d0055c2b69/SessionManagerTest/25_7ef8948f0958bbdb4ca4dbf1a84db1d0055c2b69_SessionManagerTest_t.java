 package com.mymed.tests.unit.manager;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
import com.mymed.controller.core.exception.IOBackEndException;
import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.position.PositionManager;
 import com.mymed.model.data.session.MSessionBean;
 
 /**
  * Test class for the {@link PositionManager}.
  * 
  * @author Milo Casagrande
  */
 public class SessionManagerTest extends GeneralTest {
 
   @Test
   public void testCreateSession() {
     try {
       profileManager.create(userBean);
       sessionManager.create(sessionBean);
     } catch (final Exception ex) {
       fail(ex.getMessage());
     }
   }
 
   @Test
   public void testReadSession() {
     try {
      final MSessionBean readSession = sessionManager.read(SESSION_ID);
       assertEquals("The sessions beans are not the same\n", sessionBean, readSession);
     } catch (final Exception ex) {
       fail(ex.getMessage());
     }
   }
 
   @Test
   public void testUpdateSession() {
     try {
       sessionBean.setP2P(true);
 
       sessionManager.update(sessionBean);
      final MSessionBean readSession = sessionManager.read(SESSION_ID);
       assertTrue("The session bean has not been updated correctly", readSession.isP2P());
     } catch (final Exception ex) {
       fail(ex.getMessage());
     }
   }
 
  @Test(expected = IOBackEndException.class)
  public void testDeleteSession() throws InternalBackEndException, IOBackEndException {
    sessionManager.delete(SESSION_ID);
    sessionManager.read(SESSION_ID);
   }
 }
