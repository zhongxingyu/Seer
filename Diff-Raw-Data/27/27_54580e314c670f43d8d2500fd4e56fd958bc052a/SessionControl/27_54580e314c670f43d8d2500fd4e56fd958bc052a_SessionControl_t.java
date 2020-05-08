 package org.jboss.qpanel.client.local;
 
 import org.jboss.errai.common.client.api.Caller;
 import org.jboss.errai.common.client.api.RemoteCallback;
 import org.jboss.errai.ioc.client.api.AfterInitialization;
 import org.jboss.qpanel.client.shared.AuthenticationResponse;
 import org.jboss.qpanel.client.shared.ModeratorService;
 import org.jboss.qpanel.client.shared.Question;
 import org.jboss.qpanel.client.shared.UserData;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Mike Brock
  */
 @ApplicationScoped
 public class SessionControl {
   @Inject Caller<ModeratorService> moderatorServiceCaller;
   @Inject Event<LoggedIn> loggedInEvent;
   @Inject Event<LoadEvent> loadEvent;
 
   private Map<String, UserData> userDataMap = new HashMap<String, UserData>();
   private Map<Integer, Question> questionsMap = new HashMap<Integer, Question>();
 
   private boolean authenticated;
   private boolean moderator;
 
   @AfterInitialization
   protected void startup() {
     moderatorServiceCaller.call(new RemoteCallback<AuthenticationResponse>() {
       @Override
       public void callback(AuthenticationResponse response) {
         handleAuth(response);
       }
     }).load();
   }
 
 
   public void authenticate(final String name, final String email) {
     moderatorServiceCaller.call(new RemoteCallback<AuthenticationResponse>() {
       @Override
       public void callback(AuthenticationResponse response) {
         handleAuth(response);
       }
     }).authenticate(name, email);
   }
 
   private void handleAuth(AuthenticationResponse response) {
     updateUserMap(response.getUserSessions());
     updateQuestions(response.getQuestions());
 
     System.out.println(response.getQuestions());
 
     if (response.isAuthenticated()) {
       authenticated = true;
       moderator = response.isModerator();
       loggedInEvent.fire(new LoggedIn(response.getUserData()));
     }
     else {
       loadEvent.fire(new LoadEvent());
     }
   }
 
   private void updateUserMap(Collection<UserData> userData) {
     final Set<String> activeSet = new HashSet<String>();
 
     for (UserData data : userData) {
       activeSet.add(data.getName());
       userDataMap.put(data.getName(), data);
     }
 
     final Iterator<String> nameIterator = userDataMap.keySet().iterator();
     while (nameIterator.hasNext()) {
       if (!activeSet.contains(nameIterator.next())) {
         nameIterator.remove();
       }
     }
   }
 
   private void updateQuestions(final Collection<Question> questions) {
     questionsMap.clear();
     for (Question question : questions) {
       addQuestion(question);
     }
   }
 
   public void logout() {
     authenticated = false;
   }
 
   public boolean isAuthenticated() {
     return authenticated;
   }
 
   public boolean isModerator() {
     return moderator;
   }
 
   public Map<String, UserData> getUserDataMap() {
     return userDataMap;
   }
 
   public Map<Integer, Question> getQuestionsMap() {
     return questionsMap;
   }
 
   public void addQuestion(Question question) {
     questionsMap.put(question.getId(), question);
   }
 
   public List<Question> getQuestionList() {
     final ArrayList<Question> list = new ArrayList<Question>(questionsMap.values());
     Collections.sort(list);
     return list;
   }
 
   public void clear() {
     questionsMap.clear();
     userDataMap.clear();
    authenticated = false;
   }
 }
