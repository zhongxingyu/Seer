 package controller;
 
 import javax.swing.JFrame;
 
 import model.ExerciseCatalog;
 import model.QuizCatalog;
 import view.ChangeQuizView;
 import view.CreateQuizView;
 import view.Menu;
 
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
 	
 	private CreateQuizController createQuizController;
 	private ChangeQuizController changeQuizController;
 	private DeleteQuizController deleteQuizController;
 	
 	CreateQuizView createView;
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
 	
 		int choice = startMenu.getMenuKeuze();
 		
 		switch (choice) {
 		case 1:
 			//Voeg quiz toe
 			createView = new CreateQuizView();
 			createQuizController = new CreateQuizController(createView, exerciseCatalog, quizCatalog);
 			break;
 		
 		case 2:
 			//Update quiz
 			changeView = new ChangeQuizView();
 			changeQuizController = new ChangeQuizController(changeView, quizCatalog, exerciseCatalog);
 			break;
 		
 		case 3:
 			//Delete quiz
 			deleteQuizController = new DeleteQuizController();
			deleteQuizController.makeWindowVisible();
 			break;
 
 		default:
 			break;
 		}
 	}
 	
 
 }
