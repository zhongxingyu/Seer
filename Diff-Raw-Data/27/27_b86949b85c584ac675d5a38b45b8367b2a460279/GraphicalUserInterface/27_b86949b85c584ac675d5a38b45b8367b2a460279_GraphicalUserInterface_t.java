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
 	private final String CLUE_QUERY_IN_PROGRESS_MESSAGE = "Extracting data from DBpedia";
 	private final Dimension FRAME_DIMENSION = new Dimension(550, 600); // width and height of the GUI frame
 	
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private String userResponse;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private DisplayPanel displayPanel;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private SemanticSolver semanticSolver;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private EntityRecogniserTask entityRecogniserTask;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private Clue clue;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private ArrayList<JCheckBox> checkBoxes;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private ArrayList<String> recognisedResourceUris;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private ArrayList<String> chosenResourceUris;
 
 	@Override
 	public void createAndShow() {
 		this.setSemanticSolver(new SemanticSolverImpl(this));
 		this.setTitle("Semantic Crossword Solver");
 
 		this.setDisplayPanel(new DisplayPanel());
 		this.getDisplayPanel().getSubmitClueButton().addActionListener(this);
 
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
     
     public void getChosenEntitiesFromUser(ArrayList<RecognisedResource> recognisedResources) {
     	
     	this.getDisplayPanel().getSubmitClueButton().setActionCommand("submitChosenResources");
    	 	this.getDisplayPanel().getSubmitClueButton().setText("Solve clue");
   
         this.setCheckBoxes(new ArrayList<JCheckBox>());
         this.setRecognisedResourceUris(new ArrayList<String>());
         this.setChosenResourceUris(new ArrayList<String>());
 
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
         GridBagConstraints constraints = this.getDisplayPanel().getResourceSelectorConstraints();
         constraints.gridx = 0;
         constraints.gridy = 0;
         for(JCheckBox checkBox : this.getCheckBoxes()) {
             this.getDisplayPanel().getResourceSelectorPanel().add(checkBox, constraints); // add new checkbox to next row of resourceSelectorPanel
             constraints.gridy += 1;
         }
         
         this.getDisplayPanel().getPanelScrollPane().revalidate();
         this.getDisplayPanel().getPanelScrollPane().repaint();
     }
 
 
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
      * Invoked when the user presses the "Submit clue" button.
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
