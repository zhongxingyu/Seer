 package controller;
 
import view.ChangeQuizView;
import view.Menu;

 import javax.swing.JFrame;
 
 import model.ExerciseCatalog;
 import model.QuizCatalog;
 
 /**
  * 
  * @author Steve
  * @version 11/11/2013
  *
  */
 
 public class StartAppController extends JFrame{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private Menu startMenu;
 	
	private CreateQuizController addQuizController;
 	private ChangeQuizController changeQuizController;
 	private DeleteQuizController deleteQuizController;
 	
 	ChangeQuizView changeView;
 	
 	private QuizCatalog quizCatalog;
 	private ExerciseCatalog exerciseCatalog;
 	
 	public StartAppController() {
 		
 		startMenu = new Menu("Add quiz","Update quiz","Delete quiz");
 	}
 	
 	public static void main(String[] args) throws Exception {
         new StartAppController().startApp();	
 	}
 	
 	public void startApp(){
 		
 		quizCatalog = new QuizCatalog();
 		exerciseCatalog = new ExerciseCatalog();
		
		changeView = new ChangeQuizView();
		
		changeQuizController = new ChangeQuizController(changeView, quizCatalog, exerciseCatalog);
		
 		int choice = startMenu.getMenuKeuze();
 		
 		switch (choice) {
 		case 1:
 			//Voeg quiz toe
 			break;
 		
 		case 2:
 			//Update quiz
 			break;
 		
 		case 3:
 			//Delete quiz
 			break;
 
 		default:
 			break;
 		}
 	}
 	
 
 }
