 /**
  * 
  */
 package app;
 
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 
 import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
 
 import exception.InvalidClueException;
 import framework.Clue;
 import framework.SemanticSolver;
 import framework.UserInterface;
 
 /**
  * @author Ben Griffiths
  *
  */
 @SuppressWarnings("serial")
 public class GraphicalUserInterface extends JFrame implements UserInterface, ActionListener, PropertyChangeListener {
 	private final String EXIT_REQUEST = "EXIT";
 	private final String ENTITY_RECOGNITION_IN_PROGRESS_MESSAGE = "Searching for known entities in the clue";
 	private final Dimension FRAME_DIMENSION = new Dimension(550, 600); // width and height of the GUI frame
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private String userResponse;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private DisplayPanel displayPanel;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private SemanticSolver semanticSolver;
 	
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private EntityRecogniserTask entityRecogniserTask;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private Clue clue;
 	
 	@Override
 	public void createAndShow() {
 		this.setSemanticSolver(new SemanticSolverImpl(this));
 		this.setTitle("Semantic Crossword Solver");
 		
 		this.setDisplayPanel(new DisplayPanel());
 		this.getDisplayPanel().getSubmitClueButton().addActionListener(this);
 		this.getDisplayPanel().setOpaque(true);
 		
 		this.setContentPane(this.getDisplayPanel());
 		
 		this.setPreferredSize(this.FRAME_DIMENSION);
 		this.setMinimumSize(this.FRAME_DIMENSION);
 		
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.pack();
 		this.setVisible(true);
 		this.start();
 	}
 	
 	public void solveClue(String userResponse) {
 		
 		/*this.userResponse = userResponse;
 		final GraphicalUserInterface THIS_UI = this;
 		Thread thread = new Thread(new Runnable() {
     		@Override
 			public void run() {		
     			SemanticSolver semanticSolver = new SemanticSolverImpl(THIS_UI);
     			Clue clue;
     			try {
 					clue = new ClueImpl(THIS_UI.getUserResponse());
 					semanticSolver.solve(clue);
 				} catch (InvalidClueException e) {
 					System.out.println("The clue you entered was invalid: " + e.getMessage());
 				}
 				catch(QueryExceptionHTTP e) {
 					System.out.println("DBpedia is unavailable at this time. Please try again");
 				}
     		}	
 
 		});
 		thread.start(); */
 	}
 	
 	private void start() {
 		
 		Thread thread = new Thread(new Runnable() {
     		@Override
 			public void run() {
     			String userResponse = "";
     			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
     			while(!userResponse.equals(EXIT_REQUEST)) {
     				System.out.println("Please enter a clue: (e.g. \"member of The Beatles [4, 6]\") or EXIT to finish");
     				try {
     					userResponse = in.readLine();
     				}
     				catch(IOException e) {
     					e.printStackTrace();
     					continue;
     				}
     				if(!userResponse.equals(EXIT_REQUEST)) {
     					
     					try {
     						setClue(new ClueImpl(userResponse));
     					} catch (InvalidClueException e) {
     						System.out.println("The clue you entered was invalid: " + e.getMessage());
     						continue;
     					}
     					try {
     						getSemanticSolver().solve(getClue());
     					}
     					catch(QueryExceptionHTTP e) {
     						System.out.println("DBpedia is unavailable at this time. Please try again");
     					}
     				}
     			}	
     			
     			
 			}
 		});
 		thread.start();
 	}
 	
 	@Override
 	public void updateResults(String resultsMessage) {
 		this.getDisplayPanel().getMessageArea().append(resultsMessage + "\n");
 		this.repaint();
 	}
 
 	/**
      * Invoked when task's progress property changes.
      */
     @Override
     public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
         if (propertyChangeEvent.getPropertyName().equals("progress")) {
             int progress = (Integer) propertyChangeEvent.getNewValue();
             this.getDisplayPanel().getProgressBar().setValue(progress);
             if(this.getDisplayPanel().getProgressBar().getValue() == 100)
             	this.getDisplayPanel().getProgressBar().setStringPainted(false);
         } 
     }
 
     /**
      * Invoked when the user presses the "Submit clue" button.
      */
     @Override
     public void actionPerformed(ActionEvent actionEvent) {
         this.getDisplayPanel().getSubmitClueButton().setEnabled(false);
         this.getDisplayPanel().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         this.getDisplayPanel().getProgressBar().setString(ENTITY_RECOGNITION_IN_PROGRESS_MESSAGE);
         this.getDisplayPanel().getProgressBar().setStringPainted(true);
         
        
         
         String clueAsText = this.getDisplayPanel().getClueInputField().getText();
         
         Clue clue = null;
 		try {
 			clue = new ClueImpl(clueAsText);
 		} catch (InvalidClueException e) {
 			this.getDisplayPanel().getMessageArea().append("The clue \"" + clueAsText + "\"" + " is invalid. Please try again\n");
 			this.getDisplayPanel().getSubmitClueButton().setEnabled(true);
	        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			return;
 		}
 		
 		this.setClue(clue);
         
 		this.getDisplayPanel().getProgressBar().setString(this.ENTITY_RECOGNITION_IN_PROGRESS_MESSAGE);
         this.getDisplayPanel().getProgressBar().setStringPainted(true);
 		
 	    Thread solverThread = new Thread(new Runnable() {
 		        public void run() {
 		        	getSemanticSolver().solve(getClue());
 		        }
 		    });
 	    solverThread.start();
         
         //this.getUiFrame().solveClue(clueAsText);
         
         this.getDisplayPanel().getSubmitClueButton().setEnabled(true); // NEEDS TO BE DONE AFTER THE TASK IS FINISHED - i.e. in the GUI object, not here
         this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); // NEEDS TO BE DONE AFTER THE TASK IS FINISHED - i.e. in the GUI object, not here
     }
     
     /**
      * Provided for convenience - used by SemanticSolverImpl
      * @return
      */
     public JButton getSubmitClueButton() {
     	return this.getDisplayPanel().getSubmitClueButton();
     }
 	
 }
