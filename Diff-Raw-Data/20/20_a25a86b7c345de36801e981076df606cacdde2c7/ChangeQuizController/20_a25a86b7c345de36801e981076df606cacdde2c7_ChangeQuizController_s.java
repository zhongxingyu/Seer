 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import model.Exercise;
 import model.ExerciseCatalog;
 import model.Quiz;
 import model.QuizCatalog;
 import view.ChangeQuizView;
 import view.CreateQuizView;
 
 /**
  * 
  * @author Steve
  * @version 4/11/2013
 * 
  */
 
 public class ChangeQuizController {
 
 	private ChangeQuizView view;
 	private QuizCatalog quizModel;
 	private ExerciseCatalog exerciseModel;
 
 	public ChangeQuizController(ChangeQuizView view, QuizCatalog quizModel,
 			ExerciseCatalog exerciseModel) {
 		this.view = view;
 		this.quizModel = quizModel;
 		this.exerciseModel = exerciseModel;
 
 		// Load exercises and quizzes
 		
 		this.exerciseModel.readExercisesFromFile();
 		this.quizModel.readQuizzesFromFile();
 		this.exerciseModel.createQuizExercises(exerciseModel.getExercises(),
 				quizModel.getQuizCatalogs());
 
 		// Add exercises to exercisesList (JList)
 
 		this.view.setListExercise(exerciseModel.getExercises());
 		this.view.setListQuiz(quizModel.getQuizCatalogs());
 
 		// Add listeners
 		
 		this.view.addUpdateListener(new QuizListener());
 		this.view.addDeleteListener(new DeleteListener());
 		this.view.addEditListener(new EditListener());
 		this.view.addSearchListener(new SearchListener());
 	}
 
 	class QuizListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			// dataMembers
 
 			String quizTitle;
 			String status;
 			Integer grade;
 			String category;
 
 			try {
 
 				System.out.println("Updatebutton");
 
 				quizTitle = view.getQuizTitle();
 				status = view.getStatus();
 				grade = view.getGrade();
 				category = view.getCategory();
 
 				// view.(dataModel.getQuizCatalogs());
 
 			}
 
 			catch (Exception ex) {
 
 				System.out.println(ex);
 
 				view.displayErrorMessage("Error.");
 
 			}
 
 		}
 
 	}
 
 	class DeleteListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			// dataMembers
 
 			try {
 
 				System.out.println("Deletebutton");
 
 				// for(Quiz q : quizModel.getExercises()){
 
 				// }
 
 			}
 
 			catch (Exception ex) {
 
 				System.out.println(ex);
 
 				view.displayErrorMessage("Error.");
 
 			}
 
 		}
 	}
 
 	class EditListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			// dataMembers
 
 			try {
 
 				System.out.println("Editbutton");
 
 			}
 
 			catch (Exception ex) {
 
 				System.out.println(ex);
 
 				view.displayErrorMessage("Error.");
 
 			}
 
 		}
 	}
 
 		class SearchListener implements ActionListener {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				// dataMembers
 
 				try {
 
 					System.out.println("Searchbutton");
 
 				}
 
 				catch (Exception ex) {
 
 					System.out.println(ex);
 
 					view.displayErrorMessage("Error.");
 
 				}
 			}
 		}
 	
 		public static void main(String[] args) {
 	        
 			ExerciseCatalog em = new ExerciseCatalog();
 			QuizCatalog qm = new QuizCatalog();
 			ChangeQuizView view = new ChangeQuizView();
 			
 			
 			ChangeQuizController controller = new ChangeQuizController(view, qm, em);
 			
 			view.setVisible(true);
 	    }
 		
 }
