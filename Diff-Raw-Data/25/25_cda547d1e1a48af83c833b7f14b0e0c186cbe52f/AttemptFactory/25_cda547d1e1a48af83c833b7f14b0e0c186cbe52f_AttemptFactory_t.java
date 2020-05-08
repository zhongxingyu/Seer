 package edu.ncsu.csc.csc440.project1.objs;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Random;
 
 import edu.ncsu.csc.csc440.project1.db.DBConnection;
 
 public class AttemptFactory {
     
     private int eid;
     private String sid;
     private Random rand;
     
     public AttemptFactory(int eid, String sid) {
         this.eid = eid;
         this.sid = sid;
         this.rand = new Random();
     }
     
     private ArrayList<Question> getAllQuestions() throws Exception {
         // get all questions for this exercise
         Connection conn = null;
         try {
             conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT Q.* FROM Question Q, ExerciseQuestion EQ WHERE EQ.eid=? AND Q.qname=EQ.qname");
             stmt.setInt(1, eid);
             ResultSet rs = stmt.executeQuery();
             ArrayList<Question> questions = new ArrayList<Question>();
             while (rs.next()) {
                 questions.add(new Question(rs));
             }
             return questions;
         } finally {
             conn.close();
         }
     }
     
     // randomly select the questions from the exercise
     private ArrayList<Question> chooseQuestions(int n) throws Exception {
         // get all questions for this exercise
         ArrayList<Question> allQuestions = getAllQuestions();
         // randomly choose N different questions
         if (n > allQuestions.size()) {
             throw new RuntimeException("Not enough questions to generate attempt!");
         } else if (n <= 0) {
             throw new RuntimeException("Number of questions must be at least 1.");
         }
         ArrayList<Question> chosenQuestions = new ArrayList<Question>();
         for (int i=0; i<n; i++) {
             int k = rand.nextInt(allQuestions.size());
             Question q = allQuestions.remove(k);
             chosenQuestions.add(q);
         }
         return chosenQuestions;
     }
     
     private ArrayList<Answer> getAnswers(Question ques, boolean correct) throws Exception {
         Connection conn = null;
         try {
             conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Answer A WHERE A.qname=? AND A.correct=?");
             stmt.setString(1, ques.getQname());
             stmt.setBoolean(2, correct);
             ResultSet rs = stmt.executeQuery();
             ArrayList<Answer> answers = new ArrayList<Answer>();
             while (rs.next()) {
                 answers.add(new Answer(rs));
             }
             return answers;
         } finally {
             conn.close();
         }
     }
     
     // randomly select the answers for the given question.  Exactly one must be a correct answer.
     private ArrayList<Answer> chooseAnswers(Question ques) throws Exception {
         ArrayList<Answer> chosenAnswers = new ArrayList<Answer>();
         // get all incorrect answers for the given question
         ArrayList<Answer> incorrectAnswers = getAnswers(ques, false);
         // randomly choose three incorrect answers
         for (int i=0; i<3; i++) {
             int k = rand.nextInt(incorrectAnswers.size());
             Answer a = incorrectAnswers.remove(k);
             chosenAnswers.add(a);
         }
         // get all correct answers for the given question
         ArrayList<Answer> correctAnswers = getAnswers(ques, true);
         // randomly choose one correct answer
         Answer correct = correctAnswers.get(rand.nextInt(correctAnswers.size()));
         // randomly place correct answer in the list of chosen answers
         int corindex = rand.nextInt(chosenAnswers.size()+1);
         chosenAnswers.add(corindex, correct);
         return chosenAnswers;
     }
     
     private Attempt createAttempt() throws Exception {
         System.out.printf("Creating attempt...\n");
         Connection conn = null;
         try {
             conn = DBConnection.getConnection();
             // Get attempt number
             PreparedStatement stmt = conn.prepareStatement("SELECT count(*) FROM Attempt A WHERE A.eid=? AND A.sid=?");
             stmt.setInt(1, eid);
             stmt.setString(2, sid);
             ResultSet rs = stmt.executeQuery();
             rs.next();
             int attnum = rs.getInt(1);
             
             // Store attempt record
            String s = "INSERT INTO Attempt VALUES (attempt_ids.nextval, ?, ?, ?, NULL, 0)";
             stmt = conn.prepareStatement(s);
             stmt.setInt(1, eid);
             stmt.setString(2, sid);
             stmt.setInt(3, attnum);
             stmt.executeUpdate();
             
             // Get attempt id
             stmt = conn.prepareStatement("SELECT attempt_ids.currval FROM dual");
             rs = stmt.executeQuery();
             rs.next();
             int attid = rs.getInt(1);
             Attempt att = new Attempt(attid, eid, sid, attnum, null, 0);
             return att;
         } finally {
             conn.close();
         }
     }
     
     private AttemptQuestion createAttemptQuestion(Attempt att, Question ques, int qpos) throws Exception {
         System.out.printf("Creating attempt question...\n");
         Connection conn = null;
         try {
             conn = DBConnection.getConnection();
            String s = "INSERT INTO AttemptQuestion VALUES (?, ?, ?, NULL, '', 0)";
             PreparedStatement stmt = conn.prepareStatement(s);
             stmt.setInt(1, att.getAttid());
             stmt.setInt(2, qpos);
             stmt.setString(3, ques.getQname());
             
             stmt.executeUpdate();
             return new AttemptQuestion(att.getAttid(), qpos, ques.getQname(), -1, "", 0);
         } finally {
             conn.close();
         }
     }
     
     private AttemptAnswer createAttemptAnswer(AttemptQuestion attques, Answer ans, int qpos, int apos) throws Exception {
         System.out.printf("Creating attempt answer...\n");
         Connection conn = null;
         try {
             conn = DBConnection.getConnection();
             String s = "INSERT INTO AttemptAnswer VALUES (?, ?, ?, ?)";
             PreparedStatement stmt = conn.prepareStatement(s);
             stmt.setInt(1, attques.getAttid());
             stmt.setInt(2, qpos);
             stmt.setInt(3, apos);
             stmt.setInt(4, ans.getAnsid());
             stmt.executeUpdate();
             return new AttemptAnswer(attques.getAttid(), qpos, apos, ans.getAnsid());
         } finally {
             conn.close();
         }
     }
     
     public Attempt create() throws Exception {
         Attempt att = createAttempt();
         System.out.printf("Attempt.attid = %d\n",att.getAttid());
         // choose questions
         ArrayList<Question> questions = chooseQuestions(2);
         // choose answers
         for (int i=0; i < questions.size(); i++) {
             Question ques = questions.get(i);
             int qpos = i + 1;
             ArrayList<Answer> answers = chooseAnswers(ques);
             AttemptQuestion attques = createAttemptQuestion(att, ques, qpos);
             for (int j=0; j < answers.size(); j++) {
                 Answer ans = answers.get(j);
                 int apos = j + 1;
                 createAttemptAnswer(attques, ans, qpos, apos);
             }
         }
         return att;
     }
     
 }
