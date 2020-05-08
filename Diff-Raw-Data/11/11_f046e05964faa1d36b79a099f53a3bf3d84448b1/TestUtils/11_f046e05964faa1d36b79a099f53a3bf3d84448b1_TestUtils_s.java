 package org.lds.community.CallingWorkFlow.Utilities;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import com.xtremelabs.robolectric.Robolectric;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.entity.BasicHttpEntity;
 import org.apache.http.impl.DefaultHttpResponseFactory;
 import org.junit.Assert;
 import org.lds.community.CallingWorkFlow.domain.*;
 
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Alex
  * Date: 9/17/12
  * Time: 3:42 PM
  * To change this template use File | Settings | File Templates.
  */
 
 
 
 public class TestUtils {
 
     public static Random generator=new Random();
 
 
     public static List<Member> createMembersDB(WorkFlowDB db){
         List<Member> memberList= new ArrayList<Member>();
         memberList.add(createMemberObj("Joe", "Jones", 1111L));
         memberList.add(createMemberObj("James", "Peterson", 2222L));
         memberList.add(createMemberObj("Eric", "Gonzales", 3333L) );
         memberList.add(createMemberObj("Bill", "Wiley", 4444L));
         memberList.add(createMemberObj("Steve", "Jonas", 5555L));
         memberList.add(createMemberObj("Erika", "Jasmin", 6666L));
         memberList.add(createMemberObj("Adam", "Peres", 7777L));
         memberList.add(createMemberObj("Mark", "McNelly", 8888L));
         if(db!=null){
             db.updateWardList( memberList);
 
         }
         return memberList;
     }
 
     public static List<Position> createPositionDB(WorkFlowDB db){
         List<Position> positionList= new ArrayList<Position>();
         positionList.add(createPositionObj(40L, "RS Secretary") );
         positionList.add(createPositionObj(41L, "Elder Quorum First Counselor"));
         positionList.add(createPositionObj(42L, "Elder Quorum Second Counselor"));
         positionList.add(createPositionObj(43L, "Primary sunBean teacher"));
         positionList.add(createPositionObj(44L, "Sunday School 14-15 teacher"));
         positionList.add(createPositionObj(45L, "Elder Quorum Secretary"));
         positionList.add(createPositionObj(46L, "Ward Clerk Assistant"));
         positionList.add(createPositionObj(47L, "RS 1st Counselor"));
         positionList.add(createPositionObj(48L, "RS 2st Counselor"));
         positionList.add(createPositionObj(49L, "RS President"));
         positionList.add(createPositionObj(50L, "Sunday School President"));
 
         if(db!=null){
             db.updatePositions(positionList);
         }
         return positionList;
     }
 
     public static List<WorkFlowStatus> createStatusDB(WorkFlowDB db){
         List<WorkFlowStatus> statusList= new ArrayList<WorkFlowStatus>();
         statusList.add( createStatus(false,"PENDING",null,null,1));
         statusList.add( createStatus(false,"SUBMITTED",null,null,2));
         statusList.add( createStatus(true,"SET_APART",null,null,3));
         statusList.add( createStatus(true,"DECLINED",null,null,4));
         if(db!=null){
             db.updateWorkFlowStatus(statusList);
 
         }
         return statusList;
     }
 
     public static Member createMemberObj(String firstName, String lastName, Long individualId){
         Member memberObj=new Member();
         memberObj.setFirstName(firstName);
         memberObj.setLastName(lastName);
         memberObj.setIndividualId(individualId);
         return memberObj;
     }
 
     public static Calling createCallingObj(Long positionId, String statusName, Long individualId, Boolean isSync){
         Calling callingObj=new Calling();
         callingObj.setIndividualId(individualId);
         callingObj.setPositionId(positionId);
         callingObj.setStatusName(statusName);
         callingObj.setIsSynced(isSync);
         callingObj.setAssignedTo(0);
         callingObj.setDueDate(0);
         return callingObj;
     }
 
     public static Position createPositionObj(Long positionId, String positionName){
         Position positionObj=new Position();
         positionObj.setPositionId(positionId);
         positionObj.setPositionName(positionName);
 
         return positionObj;
     }
 
     public static WorkFlowStatus createStatus(boolean complete, String statusName, ContentValues cValues, Cursor cursor, int sequence){
         WorkFlowStatus statusObj=new WorkFlowStatus();
         statusObj.setStatusName(statusName);
         statusObj.setComplete(complete);
         statusObj.setSequence(sequence);
 
         return statusObj;
     }
 
     public static boolean isCallingFoundOnList(List<Calling> callingList, Long individualId, long positionID) {
         boolean found = false;
         for( Calling c : callingList ) {
             if( c.getIndividualId() == individualId && c.getPositionId() == positionID ) {
                 found = true;
                 break;
             }
         }
         return found;
     }
 
     public static Calling getCallingObjectFromList(List<? extends Calling> callingList,Long individualId, long positionID) {
         Calling calling = null;
 
         for( Calling c : callingList ) {
             if( c.getIndividualId() == individualId && c.getPositionId() == positionID ) {
                 calling = c;
                 break;
             }
         }
         return calling;
     }
 
     public static Position getPositionObjectFromList(List<? extends Position> positionList,Long PositionId, String name) {
         Position position = null;
 
         for( Position p : positionList ) {
             if( p.getPositionId() == PositionId && p.getPositionName() == name ) {
                 position = p;
                 break;
             }
         }
         return position;
     }
 
 
     public static void assertEntityEquals(Calling sourceCalling, Calling resultCalling, String failMsg){
 
         Assert.assertEquals(failMsg + " getIndividualId did not match", sourceCalling.getIndividualId(), resultCalling.getIndividualId());
         Assert.assertEquals(failMsg + " getStatusName did not match",sourceCalling.getStatusName(), resultCalling.getStatusName());
         Assert.assertEquals(failMsg + " getAssignedToId did not match",sourceCalling.getAssignedToId(), resultCalling.getAssignedToId());
         Assert.assertEquals(failMsg + " getDueDate did not match",sourceCalling.getDueDate(), resultCalling.getDueDate());
         Assert.assertEquals(failMsg + " getIsSynced did not match",sourceCalling.getIsSynced(), resultCalling.getIsSynced());
         Assert.assertEquals(failMsg + " getPositionId did not match",sourceCalling.getPositionId(), resultCalling.getPositionId());
 
     }
 
     public static void assertEntityEquals(Position sourcePosition,Position resultPosition, String failMsg){
 
         Assert.assertEquals(failMsg + " getPositionName did not match", sourcePosition.getPositionName(), resultPosition.getPositionName());
         Assert.assertEquals(failMsg + " getPositionId did not match",sourcePosition.getPositionId(), resultPosition.getPositionId());
 
     }
 
     public static void assertEntityEquals(WorkFlowStatus sourceWfs,WorkFlowStatus resultWfs, String failMsg){
 
         Assert.assertEquals(failMsg + " getStatusName did not match", sourceWfs.getStatusName(), resultWfs.getStatusName());
         Assert.assertEquals(failMsg + " getComplete did not match",sourceWfs.getComplete(), resultWfs.getComplete());
         Assert.assertEquals(failMsg + " getSequence did not match",sourceWfs.getSequence(), resultWfs.getSequence());
 
     }
 
     public static void assertEntityEquals(Member sourceMember,Member resultMember, String failMsg){
 
         Assert.assertEquals(failMsg + " getStatusName did not match", sourceMember.getFirstName(), resultMember.getFirstName());
         Assert.assertEquals(failMsg + " getComplete did not match",sourceMember.getLastName(), resultMember.getLastName());
         Assert.assertEquals(failMsg + " getSequence did not match",sourceMember.getIndividualId(), resultMember.getIndividualId());
 
     }
 
     public static int getCallingStatusCountFromList(List<? extends Calling> callingList, String status) {
         int count=0;
 
         for( Calling c : callingList ) {
             if( c.getStatusName().equals(status)) {
                 count++;
             }
         }
         return count;
     }
 
     public static String getStatusName(List<WorkFlowStatus> statusList, Boolean isCompleted){
         String statusName = getStatusObj(statusList,isCompleted).getStatusName()  ;
         if(statusName==null){
             statusName="NOT_FOUND";
         }
         return statusName;
     }
 
     public static WorkFlowStatus getStatusObj(List<WorkFlowStatus> statuses, Boolean isCompleted){
         WorkFlowStatus status=new WorkFlowStatus();
         for( WorkFlowStatus curStatus : statuses ) {
             if( curStatus.getComplete()==isCompleted ) {
                 status = curStatus;
                 break;
             }
         }
         return status;
     }
 
     public static Long getRandomPositionID(List<Position>positionList){
         int r = generator.nextInt(positionList.size());
         return  positionList.get(r).getPositionId();
     }
 
     public static Long getRandomIndividualId(List<Member>memberList){
 
         int r = generator.nextInt(memberList.size());
         return  memberList.get(r).getIndividualId();
     }
 
     public static List<Calling> createCallingList(WorkFlowDB db, int numberOfCallings, Boolean isComplete, Boolean isSync){
         // create as many callings as the member and position permits
         List<Calling> cList=new ArrayList<Calling>();
         List<Member> memberMasterDB=createMembersDB(db);
         List<Position> positionMasterDB=createPositionDB(db);
         List<WorkFlowStatus> statusesFromDB=createStatusDB(db);
 
         HashSet hs = new HashSet();
         Long positionID=0L;
         Long individualId=0L;
         Boolean getAnother=true;
         int maxCallings = Math.min( numberOfCallings, (positionMasterDB.size() * memberMasterDB.size()));
 
         for(int i=0; i < maxCallings;i++){
             while (getAnother==true){
                 positionID= TestUtils.getRandomPositionID(positionMasterDB);
                 individualId= TestUtils.getRandomIndividualId(memberMasterDB);
 
                 if (!hs.contains(String.valueOf(positionID) + " " + String.valueOf(individualId))){
                     hs.add(String.valueOf(positionID) + " " + String.valueOf(individualId)) ;
 
                     Calling calling=TestUtils.createCallingObj( positionID,TestUtils.getStatusName(statusesFromDB,isComplete),individualId,isSync);
                     cList.add(calling);
 
                     getAnother=false;
                 }else{
                     getAnother=true;
                 }
             }
             getAnother=true;
         }
         return cList;
     }
 
     public static void httpMockJSONResponse(String json, String url){
         httpMockJSONResponse( json, url, HttpGet.METHOD_NAME);
 
     }
     public static void httpMockJSONResponse(String json, String url, String method){
 
         Robolectric.getFakeHttpLayer().interceptHttpRequests(true);
         HttpResponse res = new DefaultHttpResponseFactory().newHttpResponse(HttpVersion.HTTP_1_1, 200, null);
         BasicHttpEntity entity1 = new BasicHttpEntity();
         entity1.setContentType("application/json");
         entity1.setContent( new ByteArrayInputStream( json.getBytes() ));
         res.setEntity(entity1);
         if(url==null){
            Robolectric.addPendingHttpResponse(res );
        }else if( method == null ){
             Robolectric.addHttpResponseRule(method, url, res);
         } else {
             Robolectric.addHttpResponseRule(url, res);
         }
     }
 
     public static void validateURLRequest(String url){
 
         HttpRequest request= Robolectric.getSentHttpRequest(0);
         Assert.assertTrue("The call for " + url + " was not successful",request.getRequestLine().getUri().contentEquals(url));
         Robolectric.clearPendingHttpResponses();
 
     }
 
     public static Boolean isStatusCompleted(WorkFlowDB db, String statusName){
         List<WorkFlowStatus> statusList=createStatusDB(db);
         Boolean isCompleted=false;
         for(WorkFlowStatus w:statusList){
            if(w.getStatusName().equals(statusName)){
                isCompleted=w.getComplete();
            }
         }
         return isCompleted;
 
     }
 
     public static String callingToJSON(Calling callingObj){
 
         String jsonCallings = "{\"individualId\":\"" + callingObj.getIndividualId() + "\",\"positionId\":\"" + callingObj.getPositionId() + "\"," +
                 "\"statusName\":\"" + callingObj.getStatusName() +"\", \"assignedTo\":\""+ callingObj.getAssignedToId() + "\"," +
                 "\"synced\":\""+ callingObj.getIsSynced() + "\" } " ;
 
         return jsonCallings;
     }
 }
 
