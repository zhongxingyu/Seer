 package myQuiz.model.quiz;
 
 import myQuiz.model.session.Session;
 import myQuiz.model.user.User;
 import org.apache.commons.collections.map.MultiValueMap;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: eluibon
  * Date: 06/03/13
  * Time: 11.24
  */
 @Entity
 @Table(name = "submission")
 public class Submission implements Serializable {
 // ------------------------------ FIELDS ------------------------------
 
     private static final long serialVersionUID = 1428114395020218857L;
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
 
     @ManyToOne(fetch = FetchType.EAGER)
     @JoinColumn(name = "id_user", referencedColumnName = "id", nullable = true)
     @NotNull
     private User user;
 
     @NotNull
     @Enumerated(EnumType.ORDINAL)
     @Column(name = "status")
     private SubmissionStatus status;
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "start_timestamp")
     private Date startTimestamp;
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "end_timestamp")
     private Date endTimestamp;
 
     @Column(name = "final_score")
     private Double finalScore;
 
     @ManyToOne(fetch = FetchType.EAGER)
     @JoinColumn(name = "id_session")
     private Session session;
 
     // Answers are not saved right now, only total score
     //@OneToMany(cascade = CascadeType.ALL)
     //@JoinColumn(name = "quiz_submission_id")
     @Transient
     private List<Answer> userAnswers;
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public Submission() {
         userAnswers = new ArrayList<Answer>();
     }
 
     public Submission(User user, Session session) {
         status = SubmissionStatus.NEW;
        finalScore = 0.0;
         this.user = user;
         this.session = session;
         userAnswers = new ArrayList<Answer>();
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     public double complete() {

         double score = 0.0;
 
         Map<Question, PossibleAnswer> answerMap = new HashMap<Question, PossibleAnswer>();
         MultiValueMap mvm = MultiValueMap.decorate(answerMap, ArrayList.class);
 
         for (Answer answer : userAnswers) {
             mvm.put(answer.getQuestion(), answer.getAnswer());
         }
 
         for (Iterator iter = mvm.keySet().iterator(); iter.hasNext(); ) {
             Question q = (Question) iter.next();
             List<PossibleAnswer> pa = (List<PossibleAnswer>) mvm.get(q);
             score += q.score(pa);
         }
 
         finalScore = Math.round(score * 100.0) / 100.0;
 
         endTimestamp = Calendar.getInstance().getTime();
         status = SubmissionStatus.COMPLETED;
 
         return finalScore;
     }
 
     public Quiz getQuiz() {
         return session.getQuiz();
     }
 
     public void registerAnswer(Question question, PossibleAnswer answer) {
         userAnswers.add(new Answer(question, answer));
     }
 
     public void registerAnswers(Question question, List<PossibleAnswer> answers) {
         for (PossibleAnswer p : answers)
             userAnswers.add(new Answer(question, p));
     }
 
     public void start() {
         status = SubmissionStatus.STARTED;
         startTimestamp = Calendar.getInstance().getTime();
     }
 
 // --------------------- GETTER / SETTER METHODS ---------------------
 
     public Date getEndTimestamp() {
         return endTimestamp;
     }
 
     public void setEndTimestamp(Date endDate) {
         this.endTimestamp = endDate;
     }
 
     public Double getFinalScore() {
         return finalScore;
     }
 
     public void setFinalScore(Double finalScore) {
         this.finalScore = finalScore;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Session getSession() {
         return session;
     }
 
     public void setSession(Session session) {
         this.session = session;
     }
 
     public Date getStartTimestamp() {
         return startTimestamp;
     }
 
     public void setStartTimestamp(Date startDate) {
         this.startTimestamp = startDate;
     }
 
     public SubmissionStatus getStatus() {
         return status;
     }
 
     public void setStatus(SubmissionStatus status) {
         this.status = status;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public List<Answer> getUserAnswers() {
         return userAnswers;
     }
 
     public void setUserAnswers(List<Answer> userAnswers) {
         this.userAnswers = userAnswers;
     }
 
 // ------------------------ CANONICAL METHODS ------------------------
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Submission that = (Submission) o;
 
         if (session != null ? !session.equals(that.session) : that.session != null) return false;
         if (user != null ? !user.equals(that.user) : that.user != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = user != null ? user.hashCode() : 0;
         result = 31 * result + (session != null ? session.hashCode() : 0);
         return result;
     }
 
 // -------------------------- ENUMERATIONS --------------------------
 
     public enum SubmissionStatus {
         NEW, STARTED, COMPLETED
     }
 }
