 package hudson.plugins.sctmexecutor;
 
 import static org.junit.Assert.*;
 
 import java.rmi.RemoteException;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 
 import com.borland.scc.sccsystem.SystemService;
 
 public class TestSessionHandler {
 
   @Test
   public void testGetNewSessionId() throws RemoteException {
     SystemService systemServiceMock = EasyMock.createStrictMock(SystemService.class);
     EasyMock.expect(systemServiceMock.logonUser((String)EasyMock.notNull(), (String)EasyMock.notNull())).andReturn(Long.valueOf(1));
     EasyMock.expectLastCall().andReturn(2);
     
     EasyMock.replay(systemServiceMock);
    SessionHandler sessionHandler = new SessionHandler(systemServiceMock);
    sessionHandler.setPwd("");
    sessionHandler.setUser("");
     assertEquals(1, sessionHandler.getSessionId(-1));
     assertEquals(2, sessionHandler.getSessionId(1));
     assertEquals(2, sessionHandler.getSessionId(1));
     EasyMock.verify(systemServiceMock);
   }
 
 }
