 package org.lds.community.CallingWorkFlow.api;
 
 import android.content.Context;
 import com.google.inject.Inject;
 import com.xtremelabs.robolectric.Robolectric;
 import junit.framework.Assert;
 import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.lds.community.CallingWorkFlow.InjectedTestRunner;
 import org.lds.community.CallingWorkFlow.Utilities.TestUtils;
 import org.lds.community.CallingWorkFlow.domain.Calling;
 import org.lds.community.CallingWorkFlow.domain.CallingViewItem;
 import org.lds.community.CallingWorkFlow.domain.WorkFlowDB;
 
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: AlexC
  * Date: 8/27/12
  * Time: 2:41 PM
  * To change this template use File | Settings | File Templates.
  */
 @RunWith(InjectedTestRunner.class)
 public class CallingManagerTest {
 
     @Inject
     CallingManager manager;
 
     @Inject
     Context context;
 
     @Inject
     WorkFlowDB db;
 
     @Inject
     private CwfNetworkUtil networkUtil;
 
     @Before
     public void setUp() throws Exception {
 
         assertNotNull( "Injection of context failed", context );
         assertNotNull( "Injection of calling manager failed", manager );
     }
 /*
     @After
     public void tearDown() throws Exception {
         for(Calling calling : existingCallings ) {
             networkUtil.updateCalling(calling);
         }
     }
 */
     @Test
     public void testDeleteCallingWithActiveNetwork() throws Exception {
 
         List<Calling> callings = TestUtils.createCallingList( db, 5, false, true );
         Calling callingToDelete = callings.get(0);
         String deleteUrl = String.format(CwfNetworkUtil.CALLING_DELETE_URL, callingToDelete.getIndividualId(), callingToDelete.getPositionId());
         TestUtils.httpMockJSONResponse("", deleteUrl, HttpDelete.METHOD_NAME);
         manager.deleteCalling( callingToDelete, context );
         Thread.sleep( 2000 );
         TestUtils.validateURLRequest( deleteUrl );
         List<CallingViewItem> callingsFromDB = db.getCallingsToDelete();
         assertTrue("Some callings were marked for deletion, but not removed", callingsFromDB.isEmpty() );
 
         callingsFromDB = db.getPendingCallings();
         callingToDelete = TestUtils.getCallingObjectFromList( callingsFromDB, callingToDelete.getIndividualId(), callingToDelete.getPositionId() );
 
         assertNull( "Calling was not removed from the db", callingToDelete );
     }
 
     @Test
     public void testDeleteWithNoNetwork() throws Exception {
         // test the scenario where a network cxn is not available when the user performs the delete
         List<Calling> callings = TestUtils.createCallingList( db, 5, false, true );
         Calling callingToDelete = callings.get(0);
         Robolectric.setDefaultHttpResponse( 404, "NOT FOUND" );
         manager.deleteCalling( callingToDelete, context );
         Thread.sleep( 2000 );
 
         // at this point the calling should be marked for deletion, but the attempt to record it on the network should
         // have failed. Validate that it's still in the db, but marked for delete
         List<CallingViewItem> callingsFromDB = db.getCallingsToDelete();
         assertFalse("No callings were marked for deletion", callingsFromDB.isEmpty());
         callingToDelete = callingsFromDB.get(0);
         assertTrue("Calling was not marked for deletion", callingToDelete.getMarkedForDelete());
 
         // now provide a mock for the network delete and perform the delete on the server
         String deleteUrl = String.format(CwfNetworkUtil.CALLING_DELETE_URL, callingToDelete.getIndividualId(), callingToDelete.getPositionId());
         TestUtils.httpMockJSONResponse("", deleteUrl, HttpDelete.METHOD_NAME);
         boolean deleted = manager.deleteCallingOnServer(callingToDelete);
         assertTrue("Attempt to delete from server returned false", deleted);
 
         TestUtils.validateURLRequest( deleteUrl );
 
         // should not be in the db
         callingsFromDB = db.getCallingsToDelete();
         assertTrue("There are outstanding callings to remove from db", callingsFromDB.isEmpty());
         callingsFromDB = db.getPendingCallings();
         callingToDelete = TestUtils.getCallingObjectFromList( callingsFromDB, callingToDelete.getIndividualId(), callingToDelete.getPositionId() );
         assertNull("Calling was not removed from the db", callingToDelete);
     }
 
     private void eraseCallingsOnServer() throws Exception {
         Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
 
         List<Calling> callings = networkUtil.getCallings();
         manager.deleteCallings( callings, context );
 
     }
 
 //    @Test
     public void testUpdateCalling() throws Exception {
         Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
 
         List<Calling> callingList=TestUtils.createCallingList(db,1,false,false);
         Calling calling = callingList.get(0);
 
         manager.saveCalling( calling, context );
         List<CallingViewItem> callings = db.getPendingCallings();
         Calling callingFromList = TestUtils.getCallingObjectFromList( callings, calling.getIndividualId(), calling.getPositionId() );
 
         assertNotNull( "Calling was not retrieved from the db", callingFromList );
 
         TestUtils.assertEntityEquals(calling, callingFromList, "Calling was not saved in db");
         Thread.sleep( 2000 );
         callings = db.getPendingCallings();
         callingFromList = TestUtils.getCallingObjectFromList( callings, calling.getIndividualId(), calling.getPositionId() );
         TestUtils.assertEntityEquals(calling, callingFromList, "Calling was not persisted to cloud");
 
         networkUtil.deleteCalling( calling );
     }
 
 //    @Test
     public void testUpdateCallingList(){
         Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
 
         List<Calling> callingListDelete=networkUtil.getCompletedCallings();
         manager.deleteCallings(callingListDelete, context);
 
         List<Calling> callingListSource=TestUtils.createCallingList(db,10,false,false);
         manager.saveCallings(callingListSource, context);
 
         List<Calling> callingListResult=networkUtil.getPendingCallings();
 
         // modify callings to completed =true
         for(Calling c:callingListResult){
             c.setStatusName(TestUtils.getStatusName(networkUtil.getStatuses(), true));
         }
 
         manager.saveCallings(callingListResult, context);
         List<Calling> callingListModify=networkUtil.getCompletedCallings();
         for(Calling c:callingListModify){
             String statusName=c.getStatusName();
             Assert.assertTrue(TestUtils.isStatusCompleted(db,statusName));
         }
         manager.deleteCallings(callingListModify, context);
     }
 
 //    @Test
     public void syncCallingsTest(){
 
         Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
 
         List<Calling> callingListDelete=networkUtil.getCallings();
         manager.deleteCallings(callingListDelete, context);
 
         List<Calling> callingListSource=TestUtils.createCallingList(db,10,false,false);
 
         manager.saveCallings(callingListSource, context);
         List<Calling> callingListSync=networkUtil.getPendingCallings();
         for(Calling c:callingListSync){
             Assert.assertFalse("Calling is not synced",c.getIsSynced());
         }
 
         manager.refreshCallingsFromServer();
 
         List<Calling> callingListSyncTrue=networkUtil.getCallings();
         for(Calling c:callingListSyncTrue){
            Assert.assertTrue("Calling is  synced",c.getIsSynced());
         }
     }
 
     @Test
     public void updateCallingOnServerTest()  {
 //        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
         List<Calling> callings = TestUtils.createCallingList( db, 5, false, false );
 
         Calling callingObj=callings.get(0);
 //        manager.saveCalling( callingObj, context );
 
         Robolectric.getFakeHttpLayer().interceptHttpRequests(true);
         String jsonCallings = TestUtils.callingToJSON(callingObj) ;
 
         String url = String.format(CwfNetworkUtil.CALLING_UPDATE_URL, callingObj.getIndividualId(), callingObj.getPositionId(), callingObj.getStatusName());
        TestUtils.httpMockJSONResponse(jsonCallings,url, HttpPut.METHOD_NAME);
 //        db.updateCalling(callingObj);
 
 
         Boolean isUpdated = manager.updateCallingOnServer(callingObj);
 //        TestUtils.validateURLRequest( url );
 
         Assert.assertTrue("Calling was Not updated",isUpdated);
     }
 
 
 
     // todo need to make a test to when NoNetwork is available for the second time and have the network the third time
     // todo areCallingsEqual test with different callings to pass and not pass
     // todo refreshCallingsFromServer pass a JSON of callings, or one calling or no calling
     // todo refreshCallingsFromServer two valid callings and the pass two different callings and the first couple is not there anymore
     // todo refreshCallingsFromServer- create them as calling objects  and convert them to JSON
     // todo use entityAssert
 
 
 }
