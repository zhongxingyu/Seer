 /**
  * 
  */
 package app;
 
 import java.awt.Cursor;
 
 import java.util.ArrayList;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 
 import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
 
 import exception.NoResourcesSelectedException;
 import exception.NoSolutionsException;
 
 import framework.Clue;
 import framework.ClueSolver;
 import framework.SemanticSolver;
 import framework.Solution;
 import framework.SolutionScorer;
 import framework.UserInterface;
 
 /**
  * @author Ben Griffiths	
  *
  */
 public class SemanticSolverImpl implements SemanticSolver {
 	private static Logger log = Logger.getLogger(SemanticSolverImpl.class);
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private UserInterface userInterface;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private Clue clue;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private EntityRecogniserTask entityRecogniserTask;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ClueQueryTask clueQueryTask;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ClueSolver clueSolver;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private String results;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ArrayList<String> clueFragments;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ArrayList<RecognisedResource> recognisedResources;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private ArrayList<Solution> solutions;
 	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) private KnowledgeBaseManager knowledgeBaseManager;
 
 	public SemanticSolverImpl(UserInterface userInterface) {
 		this.setUserInterface(userInterface);
 		Thread instantiateKBManagerThread = new Thread(new Runnable() {
         	public void run() {
         		setKnowledgeBaseManager(KnowledgeBaseManager.getInstance());
         	}
     	});
 		instantiateKBManagerThread.start();
 	}
 
 	@Override
 	public void findEntities(Clue clue) throws QueryExceptionHTTP {
          	this.setClue(clue);
         	
          	this.setClueFragments(this.getClue().getClueFragments());
         	this.setEntityRecogniserTask(new EntityRecogniserTask(getClue()));
 	
         	Thread erThread = new Thread(new Runnable() {
                 	public void run() {
                          	getEntityRecogniserTask().addPropertyChangeListener(getUserInterface());
                          	getEntityRecogniserTask().execute();
                 	}
             	});
          	erThread.start();
          	
          	this.setRecognisedResources(null);
         	try {
                         this.setRecognisedResources(this.getEntityRecogniserTask().get()); // will block until ERTask has finished
         	} 
         	catch (QueryExceptionHTTP e) {
          	throw e;
         	}
         	catch (InterruptedException e) {
         		log.debug(e.getMessage());
                  } 
         	catch (ExecutionException e) {
         		log.debug(e.getMessage());
         	}
 
         	ArrayList<String> recognisedResourceUris = new ArrayList<String>();
         	
         	if(this.getRecognisedResources() == null) {
         		/* Notify the user that no solutions were found and then return*/
 				this.setResults("No solutions found");
 	        	SwingUtilities.invokeLater(new Runnable() {
 	        	@Override
 	                 	public void run() {
 	        				getUserInterface().getDisplayPanel().getProgressBar().setStringPainted(false);
 	        				getUserInterface().updateResults(getResults());
 	                 		
 	                 		getUserInterface().getDisplayPanel().getSubmitClueButton().setEnabled(true);
 	                     	getUserInterface().getDisplayPanel().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 	                 		getUserInterface().showNewClueOptions();
 	                 	}
 	        	});
 	        	return;
         	}
 
         	for(RecognisedResource thisResource : this.getRecognisedResources())
         		recognisedResourceUris.add(thisResource.getUri());
         	
         	this.findSolutions(recognisedResourceUris);
 	}
 	
 	@Override
 	public  void findSolutions(ArrayList<String> recognisedResourceUris) {
 			final long NANOSECONDS_IN_ONE_SECOND = 1000000000;
 			long startTime = System.nanoTime();
 		
 			/* Update the progress bar to reflect the fact that Entity Recognition phase is over */
 	     	SwingUtilities.invokeLater(new Runnable() {
 	    		@Override
 	             	public void run() {
 	    			String clueQueryInProgressMessage = "Searching for solutions on DBpedia";
 	    			getUserInterface().getDisplayPanel().getProgressBar().setValue(0);
 	    			getUserInterface().getDisplayPanel().getProgressBar().setString(clueQueryInProgressMessage);
 	    			getUserInterface().getDisplayPanel().getProgressBar().setStringPainted(true);
 	             	}
 	    	});      
                 
         	this.setClueQueryTask(new ClueQueryTask(this.getClue(), this.getClueFragments(), recognisedResourceUris));
         
         	Thread cqThread = new Thread(new Runnable() {
                 	public void run() {
                     	getClueQueryTask().addPropertyChangeListener(getUserInterface());
                     	getClueQueryTask().execute();
                 	}
             	});
         	cqThread.start();
 
         	ArrayList<Solution> proposedSolutions = null;
                  try {
                 	 proposedSolutions = this.getClueQueryTask().get(); // will block until CQTask is finished
                  }
                  catch (Exception e) {
                 	 try {
                 		 NoResourcesSelectedException castE = (NoResourcesSelectedException)e;
                 		 System.out.println("No entities were recognised in the clue " + this.getClue().getSourceClue());
                 		 log.debug(castE.getMessage());
                 	 }
                 	 catch(ClassCastException cce) {
                 		 log.debug(e.getMessage());
                 	 }
                  }
         
         	this.setClueSolver(new ClueSolverImpl());
         
         	ArrayList<Solution> solutions = null;
 			try {
 				solutions = this.getClueSolver().getSolutions(this.getClue(), proposedSolutions);
 			} catch (NoSolutionsException e) {
 				/* Notify the user that no solutions were found and then return*/
 				this.setResults(e.getMessage());
 	        	SwingUtilities.invokeLater(new Runnable() {
 	        	@Override
 	                 	public void run() {
 	        				getUserInterface().getDisplayPanel().getProgressBar().setStringPainted(false);
 	        				
 	        				String exceptionMessage = getResults();
 	        				String solutionStructure = getClue().getSolutionStructureAsString();
 	        				setResults(exceptionMessage + ": \"" + getClue().getSourceClue() + "\" " + solutionStructure);
 	                 		
 	        				getUserInterface().updateResults(getResults());
 	                 		
 	                 		getUserInterface().getDisplayPanel().getSubmitClueButton().setEnabled(true);
 	                     	getUserInterface().getDisplayPanel().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 	                 		getUserInterface().showNewClueOptions();
 	                 	}
 	        	});
 	        	return;
 			}
 			
 			/* Update the GUI on the EDT */
         	SwingUtilities.invokeLater(new Runnable() {
         	@Override
                  	public void run() {
                  		getUserInterface().updateProgressBarMessage("Calculating confidence levels for solutions");
                  	}
         	});
 			
         	String resultsBuffer = "Solutions to the clue \"" + this.getClue().getSourceClue() + " " +
         							this.getClue().getSolutionStructureAsString() + "\":\n";
 
         	SolutionScorer solutionScorer = new SolutionScorerImpl();
         	/* Score each solution */
         	for(Solution solution: solutions) {
              	solution.setScore(solutionScorer.score(solution));
         	}
         	/* Filter out any solutions that duplicate a solution with a higher confidence level */
         	ArrayList<Solution> filteredSolutions = new ArrayList<Solution>();
         	for(int i = 0; i < solutions.size(); i++) {
         		boolean notADupe = true;
         		int j = 0;
         		while(notADupe && j < solutions.size()) {
         			if(i != j && solutions.get(j).getSolutionText().equals(solutions.get(i).getSolutionText())
         				&& solutions.get(j).getConfidence() > solutions.get(i).getConfidence())
         				notADupe = false;
         			j++;
         		}
         		if(notADupe)
         			filteredSolutions.add(solutions.get(i));
         	}
         	
         	/* We still have potential dupes in our filtered solutions, where text and confidence are both exactly equal */
         	ArrayList<Solution> uniqueSolutions = new ArrayList<Solution>();
         	for(int i = 0; i < filteredSolutions.size(); i++) {
         		Solution thisSolution = filteredSolutions.get(i);
         		boolean notADupe = true;
         		for(int j = i + 1; j < filteredSolutions.size(); j++) {
         			if(thisSolution.getSolutionText().equals(filteredSolutions.get(j).getSolutionText())) {
         				notADupe = false;
         				break;
         			}
         		}
         		if(notADupe)
     				uniqueSolutions.add(thisSolution);
         	}
         	
         	this.addSolutionsToKnowledgeBase(uniqueSolutions);
         	
         	
         	for(Solution solution : uniqueSolutions)
         		resultsBuffer += solution.getSolutionText() + " (confidence level: " + 
         					solution.getConfidence() + "%)\n";
         	
         	long endTime = System.nanoTime();
 			long durationInSecs = (endTime - startTime) / NANOSECONDS_IN_ONE_SECOND;
 			resultsBuffer += "Time taken to process this clue: " + durationInSecs + "s\n";
         	
         	this.setResults(resultsBuffer);

         	/* Update the GUI on the EDT to show the scores */
         	SwingUtilities.invokeLater(new Runnable() {
         	@Override
                  	public void run() {
                  		getUserInterface().updateResults(getResults());
                  		getUserInterface().showNewClueOptions();
                  	}
         	});
 	}
 	
 	@Override
 	public void persistKnowledgeBase() {
 		this.getKnowledgeBaseManager().persistKnowledgeBase();
 	}
 	
 	private void addSolutionsToKnowledgeBase(ArrayList<Solution> solutions) {
		while(!this.getKnowledgeBaseManager().isFinished())
    		; // wait for the KnowledgeBaseManager to finish any ongoing updates
 		this.setSolutions(solutions);
 		Thread kbManagerThread = new Thread(new Runnable() {
         	public void run() {
                  	getKnowledgeBaseManager().addToKnowledgeBase(getClue(), getSolutions());
         	}
     	});
 		kbManagerThread.start();
 	}
 }
