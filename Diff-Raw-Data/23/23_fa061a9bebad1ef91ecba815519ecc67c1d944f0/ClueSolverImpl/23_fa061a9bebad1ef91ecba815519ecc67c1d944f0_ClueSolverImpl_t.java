 /**
  * 
  */
 package app;
 
 import java.util.ArrayList;
 
 import exception.NoSolutionsException;
 import framework.Clue;
 import framework.ClueSolver;
 import framework.Solution;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 
 /**
  * @author Ben Griffiths
  *
  */
 public class ClueSolverImpl implements ClueSolver {
 	private final int LANGUAGE_TAG_LENGTH = 3;
 	private final String LANGUAGE_TAG = "@";
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private String bestSolutionText;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private Solution bestSolution;
 	
 	/**
 	 * getSolutions - see framework.ClueSolver
 	 */
 	@Override
 	public ArrayList<Solution> getSolutions(Clue clue, ArrayList<Solution> proposedSolutions) throws NoSolutionsException {
 		if(proposedSolutions == null)
 			throw new NoSolutionsException("No solutions found for this clue");
 		proposedSolutions = filterOutNonEnglishSolutions(proposedSolutions);
 		
 		ArrayList<Solution> acceptedSolutions = new ArrayList<Solution>();
 		for(Solution proposedSolution : proposedSolutions) {
 			String solutionText = proposedSolution.getSolutionText();
			if(!acceptedSolutions.contains(proposedSolution) && isWellFormedSolution(solutionText) 
 					&& clue.matchesStructure(proposedSolution))
 				acceptedSolutions.add(proposedSolution);
 		}
 		
 		if(acceptedSolutions.size() > 0)
 			this.setBestSolution(acceptedSolutions.get(0));
 		
 		return acceptedSolutions;
 	}
 
 	/**
 	 * PRETTY MUCH DUPLICATED IN THE filterByLanguage METHOD BELOW
 	 * @param proposedSolutions
 	 * @return
 	 */
 	private ArrayList<Solution> filterOutNonEnglishSolutions(ArrayList<Solution> proposedSolutions) {
 		ArrayList<Solution> filteredSolutions = new ArrayList<Solution>();
 		for(int i = 0; i < proposedSolutions.size(); i++) {
 			Solution thisSolution = proposedSolutions.get(i);
 			String solutionText = thisSolution.getSolutionText();
 			int positionOfLanguageTag = solutionText.length() - LANGUAGE_TAG_LENGTH;
 			if(solutionText.length() > LANGUAGE_TAG_LENGTH) {
 				if(solutionText.substring(positionOfLanguageTag, positionOfLanguageTag + 1).equals(LANGUAGE_TAG) 
 					&& !solutionText.substring(positionOfLanguageTag + 1, solutionText.length()).equals("en"))
 						continue; // non-English language, so filter it out
 			}
 			filteredSolutions.add(thisSolution);
 		}
 		return filteredSolutions;
 	}
 
 	/**
 	 * Need to make this much more sophisticated... (!)
 	 * @param solutionText
 	 * @return
 	 */
 	private boolean isWellFormedSolution(String solutionText) {
 		return true;
 	}
 	
 	/**
 	 * getBestSolution - see framework.ClueSolver
 	 */
 	@Override
 	public String getBestSolution(Clue clue) {
 		return this.getBestSolutionText();
 	}
 }
