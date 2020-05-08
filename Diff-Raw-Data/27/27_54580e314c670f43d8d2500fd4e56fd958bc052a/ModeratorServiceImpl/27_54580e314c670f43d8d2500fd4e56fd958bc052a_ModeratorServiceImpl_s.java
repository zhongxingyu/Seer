 package org.jboss.qpanel.server;
 
 import org.jboss.errai.bus.server.annotations.Service;
 import org.jboss.errai.bus.server.api.RpcContext;
 import org.jboss.qpanel.client.shared.AuthenticationResponse;
 import org.jboss.qpanel.client.shared.Clear;
 import org.jboss.qpanel.client.shared.Deleted;
 import org.jboss.qpanel.client.shared.MarkAnswered;
 import org.jboss.qpanel.client.shared.ModeratorService;
 import org.jboss.qpanel.client.shared.NewQuestion;
 import org.jboss.qpanel.client.shared.Question;
 import org.jboss.qpanel.client.shared.UserData;
 import org.jboss.qpanel.client.shared.UserJoin;
 import org.jboss.qpanel.client.shared.VoteEvent;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * @author Mike Brock
  */
 @ApplicationScoped
 @Service
 public class ModeratorServiceImpl implements ModeratorService {
   private static final String USER_SESSION_ATTRIB = "ModeratorSession";
 
   private final AtomicInteger idCounter = new AtomicInteger();
 
   private final ConcurrentHashMap<String, UserSession> sessions
       = new ConcurrentHashMap<String, UserSession>();
 
   private final Map<Integer, Question> questionMap
       = new ConcurrentHashMap<Integer, Question>();
 
   private String moderatorEmail;
 
   @Inject Event<NewQuestion> questionEvent;
   @Inject Event<UserJoin> userJoinEvent;
   @Inject Event<VoteEvent> voteEvent;
   @Inject Event<MarkAnswered> markAnswered;
   @Inject Event<Deleted> deleted;
   @Inject Event<Clear> clear;
 
   @PostConstruct
   public void setup() {
     try {
       final ResourceBundle moderatorConfig = ResourceBundle.getBundle("ModeratorConfig");
 
       moderatorEmail = moderatorConfig.getString("moderator.email");
     }
     catch (Throwable e) {
       e.printStackTrace();
     }
   }
 
   @Override
   public AuthenticationResponse load() {
     final UserSession userSession = getUserSession();
 
     if (userSession != null) {
       return new AuthenticationResponse(true, "", userSession.asUserData(),
           getCurrentUserData(), getQuestionList(), userSession.isModerator());
     }
     else {
       return new AuthenticationResponse(false, "", null, getCurrentUserData(),
           getQuestionList(), false);
     }
   }
 
   @Override
   public AuthenticationResponse authenticate(String email, String name) {
     final UserSession userSession = new UserSession(email, name, email.equals(moderatorEmail));
     sessions.put(email, userSession);
 
     RpcContext.getHttpSession().setAttribute(USER_SESSION_ATTRIB, userSession);
 
     userJoinEvent.fire(new UserJoin(userSession.asUserData()));
 
     return new AuthenticationResponse(true, "Hello!", userSession.asUserData(),
         getCurrentUserData(), getQuestionList(), userSession.isModerator());
   }
 
   @Override
   public void submitQuestion(final String questionText) {
     final UserSession userSession = getUserSession();
 
     if (userSession != null) {
       final Question question = new Question(idCounter.incrementAndGet(), new Date(), userSession.asUserData(), questionText, false);
       addQuestion(question);
       userSession.recordVote(question.getId());
       questionEvent.fire(new NewQuestion(question));
     }
   }
 
   @Override
   public void voteFor(int id) {
     final UserSession userSession = getUserSession();
     if (userSession != null) {
 
       if (!userSession.hasVoted(id)) {
 
         final Question question = questionMap.get(id);
         if (question != null) {
           synchronized (question) {
             question.setVotes(question.getVotes() + 1);
             userSession.recordVote(id);
             voteEvent.fire(new VoteEvent(id, question.getVotes()));
           }
         }
       }
     }
   }
 
   @Override
   public void markAnswered(int id) {
     final UserSession userSession = getUserSession();
     if (userSession != null && userSession.isModerator()) {
       final Question question = questionMap.get(id);
       if (question != null) {
         question.setAnswered(true);
         markAnswered.fire(new MarkAnswered(id));
       }
     }
   }
 
   @Override
   public void deleteQuestion(int id) {
     final UserSession userSession = getUserSession();
     if (userSession != null && userSession.isModerator()) {
       questionMap.remove(id);
       deleted.fire(new Deleted(id));
     }
   }
 
   @Override
   public void reset() {
     final UserSession userSession = getUserSession();
     if (userSession != null && userSession.isModerator()) {
       questionMap.clear();
       sessions.clear();
       clear.fire(new Clear());
     }
   }
 
   public void addQuestion(Question question) {
     questionMap.put(question.getId(), question);
   }
 
   private UserSession getUserSession() {
     UserSession session = (UserSession) RpcContext.getHttpSession().getAttribute(USER_SESSION_ATTRIB);
     if (session != null && session.isInvalid()) {
       session = null;
       RpcContext.getHttpSession().removeAttribute(USER_SESSION_ATTRIB);
     }
     return session;
   }
 
   private List<Question> getQuestionList() {
     final List<Question> question = new ArrayList<Question>();
     question.addAll(questionMap.values());
     return question;
   }
 
   private List<UserData> getCurrentUserData() {
     final List<UserData> userData = new ArrayList<UserData>();
     for (final UserSession session : sessions.values()) {
       userData.add(session.asUserData());
     }
 
     return userData;
   }
 }
