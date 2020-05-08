 package org.jsc.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * The asynchronous version of the remote RPC service that is used to store application data.
  * @see SkaterRegistrationServiceImpl
  * 
  * @author Matt Jones
  */
 public interface SkaterRegistrationServiceAsync {
 
   void createAccount(LoginSession loginSession, Person person, AsyncCallback<Person> callback);
   void authenticate(String username, String password, AsyncCallback<LoginSession> callback);
   void logout(AsyncCallback<Boolean> callback);
   void getPerson(long pid, AsyncCallback<Person> callback);
   void getSessionClassList(LoginSession loginSession, Person person, AsyncCallback<ArrayList<SessionSkatingClass>> callback);
   void register(LoginSession loginSession, Person person, ArrayList<RosterEntry> newEntryList, boolean createMembership, AsyncCallback<RegistrationResults> callback);
   void cancelInvoice(LoginSession loginSession, long paymentid, AsyncCallback<Boolean> callback);
   void saveRoster(LoginSession loginSession, long rosterid, String newLevel, String newSection, String newClassId, AsyncCallback<Boolean> callback);  
   void getStudentRoster(LoginSession loginSession, Person person, AsyncCallback<ArrayList<RosterEntry>> callback);
   void getClassRoster(LoginSession loginSession, long classid, AsyncCallback<ArrayList<RosterEntry>> callback);
   void resetPassword(String username, AsyncCallback<Boolean> callback);
   void findUsername(String email, AsyncCallback<Boolean> callback);
   void duplicateSessionClassList(LoginSession loginSession, String oldSeason, String oldSession, String newSeason, String newSession, AsyncCallback<Boolean> callback);
   void saveSkatingClass(LoginSession loginSession, long currentClassId, ArrayList<String> newClassValues, int operation, AsyncCallback<Boolean> callback);
   void downloadRoster(LoginSession loginSession, long classId, AsyncCallback<Long> callback);
 }
