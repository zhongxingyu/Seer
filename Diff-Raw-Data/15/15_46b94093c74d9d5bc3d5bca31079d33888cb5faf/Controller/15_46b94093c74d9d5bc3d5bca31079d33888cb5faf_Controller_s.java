 package lhs.qmaker;
 
 import java.awt.Container;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import lhs.qmaker.matching.MatchingQuestionPanel;
 import lhs.qmaker.multiplechoice.MultipleChoiceQuestionPanel;
 import lhs.qmaker.select.SelectQuestionPanel;
 
 
 public class Controller {
     public static final String QUOTATION_MARK_REPLACE = "%qm%";
     
     private static final String USERNAME = "qmaker";
     private static final String PASSWORD = "E1nste1n";
     private static final String HOSTNAME = "instance28773.db.xeround.com:18798";
     private static final String DATABASE = "qmaker";
     static QMaker app = null;
     public static QuestionPanel question;
     public static ChoicesPanel choices;
     public static AnswersPanel answers;
     public static CommentsPanel comments;
 
     public static void setQMaker(QMaker app) {
         Controller.app = app;
     }
     public static void setPane(Container pane) {
         app.setPane(pane);
     }
     public static void goToHomeScreen() {
     	//TODO make an actual home screen
     	setPane(null);
     }
     public static void newMultipleChoice() {
         question = new MultipleChoiceQuestionPanel();
         app.setPane(question);
     }
     public static void newMatching() {
     	question = new MatchingQuestionPanel();
     	app.setPane(question);
     }
     public static void newSelect() {
     	question = new SelectQuestionPanel();
     	app.setPane(question);
     }
     public static void completeQuestion() {
     	String type = "";
     	if (question instanceof MultipleChoiceQuestionPanel) {
     		type = "mc";
     	} else if (question instanceof MatchingQuestionPanel) {
     		type = "ma";
     	} else if (question instanceof SelectQuestionPanel) {
     		type = "se";
     	}/* else if (question instanceof WriteInQuestionPanel) {
     		type = "wi";
     	}*/
     	boolean success = createQuestion(question.getQuestion(),
                 choices.getChoices(), answers.getAnswers(), comments.getComments(),type);
         if (success) {
             goToHomeScreen();
             answers = null;
             choices = null;
             comments = null;
             question = null;
         } else {
           //TODO handle cases where success is false
         }
     }
     public static boolean createQuestion(String question, ArrayList<String> choices, ArrayList<String> answers,
             ArrayList<String> comments, String type) {
         Connection con = null;
         String driver = "org.gjt.mm.mysql.Driver";
         try {
             Class.forName(driver);
           con = DriverManager.getConnection
                   ("jdbc:mysql://" +HOSTNAME+"/"+DATABASE+"?user="+USERNAME+"&password="+PASSWORD); 
           Statement s=con.createStatement();
           if (comments.size() == 2) {
               
               // Handles putting the comments into the database.
               String commentids = "";
               for (int i = 0; i < comments.size(); i++) {
                   s.executeUpdate("INSERT INTO comments (comment) VALUES (\""+comments.get(i).replaceAll("\"", QUOTATION_MARK_REPLACE) +"\")");
                   s.execute("SELECT c.id FROM comments c WHERE c.comment=\""+comments.get(i).replaceAll("\"", QUOTATION_MARK_REPLACE)+"\"");
                   ResultSet r = s.getResultSet();
                   r.first();
                   commentids = commentids+r.getString(1)+",";
               }
               
               /* Handles putting the choices into the database.
                * If it is a matching type question, the answers will also
                * be put into the database as choices.
                */
               String choiceids = "";
               String answerids = "";
               for (int i = 0; i < choices.size(); i++) {
                   s.executeUpdate("INSERT INTO choices (choice) VALUES (\""+choices.get(i).replaceAll("\"", QUOTATION_MARK_REPLACE)+"\")");
                   s.execute("SELECT ch.id FROM choices ch WHERE ch.choice=\""+choices.get(i).replaceAll("\"", QUOTATION_MARK_REPLACE)+"\"");
                   ResultSet r = s.getResultSet();
                   r.first();
                   choiceids = choiceids+r.getString(1)+",";
                  if (!type.equals("ma") && answers.contains(choices.get(i))) {
                       answerids = answerids+r.getString(1)+",";
                   } else if (type.equals("ma")) { // The case where the question is matching type
                       // Put all of the answers in as choices here.
                       s.executeUpdate("INSERT INTO choices (choice) VALUES (\""+answers.get(i).replaceAll("\"", QUOTATION_MARK_REPLACE)+"\")");
                       s.execute("SELECT ch.id FROM choices ch WHERE ch.choice=\""+answers.get(i).replaceAll("\"", QUOTATION_MARK_REPLACE)+"\"");
                       r = s.getResultSet();
                       r.first();
                       answerids = answerids+r.getString(1)+",";
                   }
                   
               }
               
               /* Put the question tuple into the database that ties the
                * other components together.
                */
               s.executeUpdate("INSERT INTO questions (question, choice_ids, answer_ids, type, wrong_comment, correct_comment) VALUES (\""+
                       question.replaceAll("\"", QUOTATION_MARK_REPLACE)+"\", \""+choiceids +"\", \""+answerids+"\", \""+type+"\", \""+comments.get(1).replaceAll("\"", QUOTATION_MARK_REPLACE)+"\", \""+comments.get(0).replaceAll("\"", QUOTATION_MARK_REPLACE)+"\")");
           } else {
               String commentids[] = new String[comments.size()];
               for (int i = 0; i < comments.size(); i++) {
                   s.executeUpdate("INSERT INTO comments (comment) VALUES (\""+comments.get(i).replaceAll("\"", QUOTATION_MARK_REPLACE)+"\")");
                   s.execute("SELECT c.id FROM comments c WHERE c.comment=\""+comments.get(i).replaceAll("\"", QUOTATION_MARK_REPLACE)+"\"");
                   ResultSet r = s.getResultSet();
                   r.first();
                   commentids[i] = r.getString(1);
               }
               String answerids = new String();
               String choiceids = new String();
               for (int i = 0; i < choices.size(); i++) {
                   s.executeUpdate("INSERT INTO choices (choice, comment_id) VALUES (\""+choices.get(i).replaceAll("\"", QUOTATION_MARK_REPLACE)+"\", "+commentids[i]+")");
                   s.execute("SELECT ch.id FROM choices ch WHERE ch.choice=\""+choices.get(i).replaceAll("\"", QUOTATION_MARK_REPLACE)+"\" AND ch.comment_id=\""+commentids[i]+"\"");
                   ResultSet r = s.getResultSet();
                   r.first();
                   if (answers.contains(choices.get(i))) {
                       answerids = answerids+r.getString(1)+",";
                   }
                   choiceids = choiceids+r.getString(1)+",";
               }
               s.executeUpdate("INSERT INTO questions (question, choice_ids, answer_ids, type) VALUES (\""+
                       question.replaceAll("\"", QUOTATION_MARK_REPLACE)+"\", \""+choiceids +"\", \""+answerids+"\", \""+type+"\")");
           }
         } catch (Exception ex) {
             ex.printStackTrace();
             try {con.close();} catch (SQLException e) {e.printStackTrace();}
             return false;
         }
         try {con.close();} catch (SQLException e) {e.printStackTrace();}
         return true;
     }
 }
