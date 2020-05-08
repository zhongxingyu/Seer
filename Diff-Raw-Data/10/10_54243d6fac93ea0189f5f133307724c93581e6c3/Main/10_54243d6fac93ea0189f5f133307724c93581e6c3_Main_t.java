 package nl.hanze.designpatterns.mvc;
 
 import javax.swing.SwingUtilities;
 
 import nl.hanze.designpatterns.DAO.impl.ser.QuestionDAOImpl;
 import nl.hanze.designpatterns.DAO.impl.ser.QuestionSerialize;
 import nl.hanze.designpatterns.controller.Controller;
 import nl.hanze.designpatterns.db.Executor;
 import nl.hanze.designpatterns.db.Executor.ExecutorException;
 import nl.hanze.designpatterns.domain.Answer;
 import nl.hanze.designpatterns.domain.Question;
 import nl.hanze.designpatterns.model.Model;
 import nl.hanze.designpatterns.view.RegisterTicketView;
 import nl.hanze.designpatterns.view.TicketOverview;
 import nl.hanze.designpatterns.view.View;
 
 public class Main {
 
     public static void main(String[] args) {           
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {                     
             	Executor executor = null;
 				try {
 					executor = Executor.getInstance();
 	        	   executor.setUrl("jdbc:mysql://localhost:3306/designpatterns");
 	        	   executor.setUser("root");
 	        	   executor.setPassword("");
 				} catch (ExecutorException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
             	   
                 Model model = new Model();
                 View view = new View("-"); 
                 Controller controller = new Controller(model,view);
                 
                 new QuestionSerialize();
                 new QuestionDAOImpl();
             }
         });  
     }
 }
