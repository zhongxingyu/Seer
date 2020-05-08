 package controller;
 
 /**
  *  
  * @author Lieven
  * @version 23/11/2013
  *
  */
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseListener;
 
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import persistence.PersistenceFacade;
 import statePattern.StateContext;
 import model.Exercise;
 import model.ExerciseCatalog;
 import model.Quiz;
 import model.QuizCatalog;
 import model.QuizExercise;
 import view.CreateQuizView;
 import view.DeleteQuizView;
 
 public class DeleteQuizController {
 
 	private DeleteQuizView window;
 	private QuizCatalog quizCatalog;
 	private ExerciseCatalog exerciseCatalog;
 	private PersistenceFacade perFacade;
 	private StateContext stateContext;
 	
 	//CLASS CONSTRUCTORS
 	
 	public DeleteQuizController(){	
 		window = new DeleteQuizView();
 	}
 	
 	public DeleteQuizController(QuizCatalog quizCatalog, ExerciseCatalog exerciseCatalog){
 	
 		try{
 			this.perFacade = new PersistenceFacade();
 			
 			this.setQuizCatalog(quizCatalog);
 			this.setExerciseCatalog(exerciseCatalog);
 			
 			this.perFacade.load(exerciseCatalog, quizCatalog);
 			
 			this.window = new DeleteQuizView();
 			
 			this.window.addDeleteQuizListener(new DeleteQuizListener());
 			this.window.addCloseWindowListener(new CloseWindowListener());
 			this.window.addButtonUpListener(new ButtonUpListener());
 			this.window.addButtonDownListener(new ButtonDownListener());
 			this.window.addSaveAndCloseListener(new SaveAndCloseWindowListener());
 			
 			//load JTables in window
 			this.loadQuizTable();
 			this.loadExTable();
 			
 			//set column width of JTABLE for quizzes
 			this.window.setColumnWidth();
 			
 			
 			stateContext = new StateContext();
 			
 			stateContext.setStateBehavior(quizCatalog.getQuizCatalogs().get(0));
 			stateContext.behavior(DeleteQuizController.this, window);
 			
 		}
 		catch (Exception exc) {
 			System.out.println(exc);
 			window.showPopup(exc.getMessage());
 		}
 	}
 
 	//GETTERS & SETTERS
 	
 	public void setQuizCatalog(QuizCatalog quizCatalog){
 		this.quizCatalog = quizCatalog;
 	}
 		
 	public QuizCatalog getQuizCatalog(){
 		return this.quizCatalog;	
 	}	
 
 	public void setExerciseCatalog(ExerciseCatalog exerciseCatalog){
 		this.exerciseCatalog = exerciseCatalog;
 	}
 		
 	public ExerciseCatalog getExerciseCatalog(){
 		return this.exerciseCatalog;	
 	}
 	
 	//METHODS
 	//make window visible or invisible
 	public void makeWindowVisible(){
 		this.window.setVisible(true);
 	}
 	
 	public void hideWindow(){
 		this.window.setVisible(false);
 	}
 	
 	
 	//JTABLE METHODS
 	
 	//Load quizJTable from window with quiz elements: ID, Teacher, Subject, Grade, Status (per row)
 	public void loadQuizTable(){
 		
 		try{
 		
 			for(Quiz quiz : quizCatalog.getQuizCatalogs()){
 		
 				String[] dataBuilder = new String[5];
 				
 				dataBuilder[0] = Integer.toString(quiz.getQuizId());
 				dataBuilder[1] = quiz.getTeacher().toString();
 				dataBuilder[2] = quiz.getSubject();
 				dataBuilder[3] = Integer.toString(quiz.getLeerJaren());
 				dataBuilder[4] = quiz.getStatus().toString();
 				
 				window.getQModel().addRow(dataBuilder);
 			}
 			
 			window.getJTableQuiz().setModel(window.getQModel());
 			window.getJTableQuiz().setAutoCreateRowSorter(true);
 			
 			if(window.getJTableQuiz().getRowCount() != 0){
 				window.getJTableQuiz().setRowSelectionInterval(0, 0);
 			}
 		}
 		catch (Exception exc) {
 			System.out.println(exc);
 			window.showPopup(exc.getMessage());
 		}
 	}
 	
 	//Load exerciseJTable from window with exercises of the selected quiz
 	public void loadExTable(){
 		
 		try{
 			if(window.getJTableQuiz().getRowCount() != 0){
 			
 				//Select QuizID from the JTABLE
 				String quizIDtoLookup = (String) window.getJTableQuiz().getValueAt(window.getJTableQuiz().getSelectedRow(), 0);	
 				//loop through the quizzes
 				for(Quiz quiz : getQuizCatalog().getQuizCatalogs()){
 					//Find the quiz object via the ID of the selected JTable row
 					if(quiz.getQuizId() == Integer.parseInt(quizIDtoLookup)){
 								
 						for(QuizExercise qe : quiz.getQuizExercises()){
 							
 							String[] dataBuilder = new String[1];
 							
 							dataBuilder[0] = qe.getExercise().getQuestion();
 							
 							window.getEModel().addRow(dataBuilder);
 						}	
 					}
 				}		
 			}
 			window.getJTableExercises().setModel(window.getEModel());
 		}
 		catch (Exception exc) {
 			System.out.println(exc);
 			window.showPopup(exc.getMessage());
 		}
 	}
 	
 	
 	//methods to reset the DefaultTableModels and reload data
 	public void resetTable(){
 		try{
 			window.getQModel().setRowCount(0);
 			this.loadQuizTable();
 		}
 		catch (Exception exc) {
 			System.out.println(exc);
 			window.showPopup(exc.getMessage());
 		}
 	}
 		
 	public void resetExTable(){
 		try{
 			window.getEModel().setRowCount(0);
 			this.loadExTable();
 		}
 		catch (Exception exc) {
 			System.out.println(exc);
 			window.showPopup(exc.getMessage());
 		}
 	}
 	
 	
 	//EVENT LISTENERS
 	
 	class DeleteQuizListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) throws IllegalArgumentException{
 			try {
 				perFacade.deleteQuiz(window, DeleteQuizController.this, exerciseCatalog, quizCatalog);
				
				//set statebehavior after deletion
				int rowIndex = window.getJTableQuiz().getSelectedRow();
				stateContext.setStateBehavior(quizCatalog.getQuizCatalogs().get(rowIndex));
				stateContext.behavior(DeleteQuizController.this, window);
 			}
 			catch (Exception exc) {
 				System.out.println(exc);
 			}
 		}
 	}
 
 	class SaveAndCloseWindowListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		
 			try {
 				perFacade.saveAndClose(window, DeleteQuizController.this, exerciseCatalog, quizCatalog);
 			}
 			catch (Exception exc) {
 				System.out.println(exc);
 			}
 		}
 	}
 	
 	class CloseWindowListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		
 			try {
 				window.dispose();
 			}
 
 			catch (Exception exc) {
 				System.out.println(exc);
 			}
 		}
 	}
 
 	class ButtonUpListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		
 			try {
 				int rowIndex = window.getJTableQuiz().getSelectedRow();
 				
 				if(rowIndex == 0){	
					rowIndex = window.getJTableQuiz().getRowCount();			
 					window.getJTableQuiz().setRowSelectionInterval(rowIndex-1, rowIndex-1);
					rowIndex = window.getJTableQuiz().getRowCount()-1;
 				}			
 				else{
 					rowIndex--;
 					window.getJTableQuiz().setRowSelectionInterval(rowIndex, rowIndex);		
 				}
 				
				//set statebehavior after selection change
 				stateContext.setStateBehavior(quizCatalog.getQuizCatalogs().get(rowIndex));
 				stateContext.behavior(DeleteQuizController.this, window);
 				
 				resetExTable();
 			}
 
 			catch (Exception exc) {
 				System.out.println(exc);
 			}
 		}
 	}
 
 	class ButtonDownListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		
 			try {
 				int rowIndex = window.getJTableQuiz().getSelectedRow();
 				
 				if(rowIndex < window.getJTableQuiz().getRowCount()-1){
 					rowIndex++;
					window.getJTableQuiz().setRowSelectionInterval(rowIndex, rowIndex);			
 				}	
 				else{
 					window.getJTableQuiz().setRowSelectionInterval(0, 0);
					rowIndex = 0;			
 				}

				//set statebehavior after selection change
 				stateContext.setStateBehavior(quizCatalog.getQuizCatalogs().get(rowIndex));
 				stateContext.behavior(DeleteQuizController.this, window);
 				
 				resetExTable();
 			}
 
 			catch (Exception exc) {
 				System.out.println(exc);
 			}
 		}
 	}
 
 	// STATIC VOID MAIN
 	
 	public static void main(String[] args) {
 	    
 		try{
 			QuizCatalog qCatalog = new QuizCatalog();
 			ExerciseCatalog eCatalog = new ExerciseCatalog();
 			DeleteQuizController controller = new DeleteQuizController(qCatalog, eCatalog);
 			controller.window.setVisible(true);
 		}
 		catch(Exception exc){
 			System.out.println(exc);
 		}
 	}
 }
 
 
 
