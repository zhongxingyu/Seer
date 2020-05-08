 /**
  * 
  */
 package app;
 
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 
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
 	private final String ENTITY_RECOGNITION_IN_PROGRESS_MESSAGE = "Searching for known entities in the clue";
 	private final String CLUE_QUERY_IN_PROGRESS_MESSAGE = "Searching for solutions on DBpedia";
 	private final Dimension FRAME_DIMENSION = new Dimension(1000, 600); // width and height of the GUI frame
 	private final Dimension DISPLAY_PANEL_DIMENSION = new Dimension(950, 575);
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private String userResponse;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private DisplayPanel displayPanel;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private SemanticSolver semanticSolver;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private EntityRecogniserTask entityRecogniserTask;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private Clue clue;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private ArrayList<JCheckBox> checkBoxes;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private ArrayList<String> recognisedResourceUris;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private ArrayList<String> chosenResourceUris;
 	
 	/**
 	 * createAndShow - Creates the GUI. Must be called from the EDT. See framework.UserInterface
 	 */
 	@Override
 	public void createAndShow() {
 		this.setSemanticSolver(new SemanticSolverImpl(this));
 		this.setTitle("Semantic Crossword Solver");
 
 		this.setDisplayPanel(new DisplayPanel());
 		this.getDisplayPanel().getSubmitClueButton().addActionListener(this);
 		
 		this.getDisplayPanel().setPreferredSize(this.DISPLAY_PANEL_DIMENSION);
 		
 		this.getDisplayPanel().setSize(this.DISPLAY_PANEL_DIMENSION);
 		
 		this.getDisplayPanel().setOpaque(true);
 
 		this.setContentPane(this.getDisplayPanel());
 
 		this.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				requestFocusInWindow();
 			}
 		});
 
 		this.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent keyEvent) {
 				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
 					if(getDisplayPanel().getSubmitClueButton().getText().equals("Submit clue"))
 						solveClue();
 					else {
 						if(getChosenResourceUris() == null || getChosenResourceUris().size() == 0) {
 				        	updateResults("You didn't select any resources!");
 				     }
 					 else findSolutions();
 					}
 				}
 			}
 		});
 
 		this.setPreferredSize(this.FRAME_DIMENSION);
 		this.setMinimumSize(this.FRAME_DIMENSION);
 
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.pack();
 		this.setVisible(true);
 	}
 	
 	/**
 	 * updateResults - Adds the specified String to the message area. See framework.UserInterface
 	 * @argument resultsMessage - the message to be displayed to the user
 	 */
 	@Override
 	public void updateResults(String resultsMessage) {
 		this.getDisplayPanel().getMessageArea().append(resultsMessage + "\n");
 		this.repaint();
 	}
 
 	/**
      * propertyChange - Invoked when task's progress property changes. See java.beans.PropertyChangeListener
      * @argument propertyChangeEvent - the event to be handled
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
      * solveClue - updates the GUI to reflect to the user that a clue has been submitted. Retrieves the specification of a clue
      * entered by the user and instantiates a new Clue object to represent the clue. Then calls the SemanticSolver member's
      * findEntities method on a separate thread in order to begin extracting recognised entities from the clue.
      * See framework.UserInterface
      */
     @Override
     public void solveClue() {
     	this.getDisplayPanel().getSubmitClueButton().setEnabled(false);
         this.getDisplayPanel().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         this.getDisplayPanel().getProgressBar().setString(ENTITY_RECOGNITION_IN_PROGRESS_MESSAGE);
         this.getDisplayPanel().getProgressBar().setStringPainted(true);
         
         String clueText = this.getDisplayPanel().getClueInputField().getText();
         int[] solutionStructure = new int[(Integer)this.getDisplayPanel().getWordNumberSpinnerModel().getValue()];
         
         ArrayList<JTextField> solutionStructureInputFields = this.getDisplayPanel().getSolutionStructureInputFields();
         for(int i = 0; i < solutionStructureInputFields.size(); i++) {
         	solutionStructure[i] = Integer.parseInt(solutionStructureInputFields.get(i).getText()); // NEED TO ADD EXCEPTION CHECK
         }
         
         Clue clue = null;
 		try {
 			clue = new ClueImpl(clueText, solutionStructure);
 		} catch (InvalidClueException e) {
 			this.getDisplayPanel().getMessageArea().append(e.getMessage() + "\n");
 			this.getDisplayPanel().getSubmitClueButton().setEnabled(true);
 	        this.getDisplayPanel().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			return;
 		}
 
 		this.setClue(clue);
         
 		this.getDisplayPanel().getProgressBar().setString(this.ENTITY_RECOGNITION_IN_PROGRESS_MESSAGE);
         this.getDisplayPanel().getProgressBar().setStringPainted(true);
 
 	    Thread solverThread = new Thread(new Runnable() {
 		        public void run() {
 		        	getSemanticSolver().findEntities(getClue());
 		        }
 		    });
 	    solverThread.start();
         
         this.getDisplayPanel().getSubmitClueButton().setEnabled(true);
         this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
     }
     
     /**
      * getChosenEntitiesFromUser - presents the user with a list of resources that have been recognised in the clue text of the clue
      * currently being solved, together with information about their types. See framework.UserInterface
      * @argument recognisedResources - a list of RecognisedResource objects to present to the user for selection of the most 
      * relevant entities.
      */
     @Override
     public void getChosenEntitiesFromUser(ArrayList<RecognisedResource> recognisedResources) {
     	this.getDisplayPanel().getSubmitClueButton().setActionCommand("submitChosenResources");
    	 	this.getDisplayPanel().getSubmitClueButton().setText("Solve clue");
   
         this.setCheckBoxes(new ArrayList<JCheckBox>());
         this.setRecognisedResourceUris(new ArrayList<String>());
         this.setChosenResourceUris(new ArrayList<String>());
 
         GridBagConstraints resourceSelectorConstraints = this.getDisplayPanel().getSolutionStructureConstraints(); 
         resourceSelectorConstraints.gridx = 0;
         resourceSelectorConstraints.gridy = 0;
 		/* Add title label to first row of resourceSelector panel */
         JLabel titleLabel = this.getDisplayPanel().getResourceSelectorTitleLabel();
 		this.getDisplayPanel().getResourceSelectorPanel().add(titleLabel, resourceSelectorConstraints);
 		/* Remove solutionStructurePanel and add resourceSelectorPanel in its place */
         this.getDisplayPanel().getPanelScrollPane().setViewportView(this.getDisplayPanel().getResourceSelectorPanel());
         this.getDisplayPanel().getPanelScrollPane().revalidate();
         this.getDisplayPanel().getPanelScrollPane().repaint();
         
         if(recognisedResources == null || recognisedResources.size() == 0) {
         	this.updateResults("Recognition of entities in the clue text failed. Please try again");
         	this.showNewClueOptions();
         	this.revalidate();
         	this.repaint();
         	return;
         }
         
         for(RecognisedResource thisResource : recognisedResources) {
                  String resourceLabel = thisResource.getResourceLabel();
                  String typeLabels = thisResource.getConcatenatedTypeLabels();
                  String uri = thisResource.getUri();
                  this.getRecognisedResourceUris().add(uri);
                  JCheckBox checkBox = new JCheckBox(resourceLabel + " (type: " + typeLabels + ")");
                  this.getCheckBoxes().add(checkBox);
                  
                  checkBox.addItemListener(new ItemListener() {
                           @Override
                           public void itemStateChanged(ItemEvent itemEvent) {
                                JCheckBox thisCheckBox = (JCheckBox)itemEvent.getSource();
                                int index = getCheckBoxes().indexOf(thisCheckBox);
                                String thisUri = getRecognisedResourceUris().get(index);
                                if(itemEvent.getStateChange() == ItemEvent.SELECTED)
                             	   getChosenResourceUris().add(thisUri);                     
                                else getChosenResourceUris().remove(thisUri);
                        }
                });
          }
         resourceSelectorConstraints.gridy = 1;
         for(JCheckBox checkBox : this.getCheckBoxes()) {
             this.getDisplayPanel().getResourceSelectorPanel().add(checkBox, resourceSelectorConstraints); // add new checkbox to next row of resourceSelectorPanel
             resourceSelectorConstraints.gridy += 1;
         }
         this.getDisplayPanel().getPanelScrollPane().revalidate();
         this.getDisplayPanel().getPanelScrollPane().repaint();
     }
     
     /**
      * findSolutions - updates the GUI to reflect to the user that a clue is being solved. Calls the SemanticSolver member's
      * findSolutions method on a new thread, passing it the list of resources chosen by the user. See framework.UserInterface
      */
     @Override
 	public void findSolutions() {
     	
     	this.getDisplayPanel().getSubmitClueButton().setEnabled(false);
         this.getDisplayPanel().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
     	
     	this.getDisplayPanel().getProgressBar().setValue(0);
     	this.getDisplayPanel().getProgressBar().setString(this.CLUE_QUERY_IN_PROGRESS_MESSAGE);
     	this.getDisplayPanel().getProgressBar().setStringPainted(true);
     	
 		Thread findSolutionsThread = new Thread(new Runnable() {
                          public void run() {
                           getSemanticSolver().findSolutions(getChosenResourceUris());
                          }
                      });
 		findSolutionsThread.start();
 	}
 
     /**
      * actionPerformed - invoked when the user presses the "Submit clue" button in order to submit a clue for entity recognition or
      * submit a list of chosen resources to use in solving the previously submitted clue. See java.awt.event.ActionListener.
      * @argument actionEvent - the event to be handled
      */
     @Override
     public void actionPerformed(ActionEvent actionEvent) {
         if(actionEvent.getActionCommand().equals("submitClue"))
         	this.solveClue();
         else {
         	if(actionEvent.getActionCommand().equals("submitChosenResources")) {
 				 if(this.getChosenResourceUris() == null || this.getChosenResourceUris().size() == 0) {
 			        	this.updateResults("You didn't select any resources!");
 			     }
 				 else this.findSolutions();
         	}
        }
     }
     
     /**
      * showNewClueOptions - presents the user with the solutionStructurePanel, allowing the structure of the solution to a clue
      * to be specified, and enables the submission of a new clue using the "Submit clue" button or the Enter key.
      * See framework.UserInterface
      */
     @Override
 	public void showNewClueOptions() {
 		this.getDisplayPanel().getSubmitClueButton().setEnabled(true);
 		this.getDisplayPanel().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 		this.getDisplayPanel().getSubmitClueButton().setActionCommand("submitClue");
 		this.getDisplayPanel().getSubmitClueButton().setText("Submit clue");
  
      	/* Remove checkboxes from resourceSelectorPanel */
      	for(JCheckBox thisCheckBox : this.getCheckBoxes())
      		this.getDisplayPanel().getResourceSelectorPanel().remove(thisCheckBox);
      	/* Remove resourceSelectorPanel and add solutionStructurePanel in its place */
         this.getDisplayPanel().getPanelScrollPane().setViewportView(this.getDisplayPanel().getSolutionStructurePanel());
         
         this.getDisplayPanel().getPanelScrollPane().revalidate();
      	this.getDisplayPanel().getPanelScrollPane().repaint();
 	}
 }
