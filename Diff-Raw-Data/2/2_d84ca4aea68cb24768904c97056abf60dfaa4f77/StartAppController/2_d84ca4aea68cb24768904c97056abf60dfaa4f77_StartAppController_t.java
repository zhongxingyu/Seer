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
 	
 	//private AddQuizController addQuizController;
 	private ChangeQuizController changeQuizController;
	private DeleteQuizController deleteQuizController;
 	
 	//private AddExerciseController addExerciseController;
 	//private ChangeExerciseController changeExerciseController;
 	//private DeleteExerciseController deleteExerciseController;
 	
 	private QuizCatalog quizCatalog;
 	private ExerciseCatalog exerciseCatalog;
 	
 	public StartAppController() {
 		
 		startMenu = new Menu("Add exercise","Add quiz","Update exercise","Update quiz","Delete exercise","Delete quiz");
 		/*
 		createQuizController = new createQuizController();
 		addExerciseController = new AddExerciseController();
 		
 		changeQuizController = new ChangeQuizController();
 		changeExerciseController = new ChangeExerciseController();
 		
 		deleteQuizController = new DeleteQuizController();
 		deleteExerciseController = new DeleExerciseController();
 		*/
 	}
 	
 	public static void main(String[] args) throws Exception {
         new StartAppController().startApp();
         QuizCatalog quizCatalog = new QuizCatalog();
 		ExerciseCatalog exCatalog = new ExerciseCatalog();
 		ChangeQuizView changeView = new ChangeQuizView();
 		ChangeQuizController changeController = new ChangeQuizController(changeView, quizCatalog, exCatalog);
 	}
 	
 	public void startApp(){
 		
 		int choice = startMenu.getChoice();
 		
 		switch (choice) {
 		case 1:
 			//Voeg opdracht toe
 			break;
 		
 		case 2:
 			//voeg Quiz toe
 			break;
 		
 		case 3:
 			//update opdracht
 			break;
 			
 		case 4:
 			//update quiz
 			break;
 			
 		case 5:
 			//delete opdracht
 			break;
 			
 		case 6:
 			//delete quiz
 			break;
 
 		default:
 			break;
 		}
 	}
 	
 
 }
