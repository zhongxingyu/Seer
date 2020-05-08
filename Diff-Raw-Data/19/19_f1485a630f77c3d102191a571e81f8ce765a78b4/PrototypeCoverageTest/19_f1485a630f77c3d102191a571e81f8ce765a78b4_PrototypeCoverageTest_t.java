 /**
  * 
  */
 package test;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import prototype.SimpleClue;
 import prototype.SimpleModelLoader;
 import prototype.SimpleQuery;
 import prototype.SimpleSolver;
 
 import com.hp.hpl.jena.rdf.model.InfModel;
 
 import exception.InvalidClueException;
 import framework.Clue;
 import framework.ModelLoader;
 import framework.Query;
 import framework.Solver;
 
 /**
  * @author Ben Griffiths
  *
  */
 public class PrototypeCoverageTest {
 	private final long NANOSECONDS_IN_ONE_SECOND = 1000000000;
 	private final long NANOSECONDS_IN_ONE_MINUTE = NANOSECONDS_IN_ONE_SECOND * 60;
 	private static ArrayList<String> cluesToSolve;
 	private static ArrayList<String> solutions;
 	private static ModelLoader modelLoader;
 	private static InfModel model;
 	private Clue clue;
 		
 	@BeforeClass
 	public static void Before() {
 		
 		cluesToSolve = new ArrayList<String>();
 		solutions = new ArrayList<String>();
 		
		//cluesToSolve.add("member of The Beatles [4, 6]");
		//solutions.add("John Lennon");
		//cluesToSolve.add("member of The Beatles [4, 9]");
		//solutions.add("Paul McCartney");
		//cluesToSolve.add("member of The Beatles (4, 9)"); // not a valid clue
		//solutions.add("[NOT A VALID CLUE]");
 		cluesToSolve.add("Member of The Beatles [4, 9]"); // first letter is upper-case. At present, the basicCoverageTest will fail on this one
 		solutions.add("Paul McCartney");
 		
 		modelLoader = new SimpleModelLoader();
 		model = modelLoader.getModel();
 	}
 	
 	@Test
 	public void basicCoverageTest() {	
 				assertTrue(cluesToSolve.size() == solutions.size()); // check that a solution has been provided for each clue
 				while(!cluesToSolve.isEmpty()) {
 					long startTime = System.nanoTime();
 					String clueAsString = cluesToSolve.get(0);
 					String solution = solutions.get(0);
 					try {
 						this.clue = new SimpleClue(clueAsString);
 					}
 					catch(InvalidClueException e) {
 						boolean clueIsInvalid = (clueAsString == null || clueAsString.length() == 0 ||
 								(!clueAsString.contains("[")) || (clueAsString.charAt(clueAsString.length() - 1) != ']'));
 						assertTrue("Invalid clue exception thrown for valid clue", clueIsInvalid);
 						cluesToSolve.remove(clueAsString);
 						solutions.remove(solution);
 						continue;
 					}
 					Query query = new SimpleQuery(this.clue, model);
 					Solver solver = new SimpleSolver();
 					ArrayList<String> candidateSolutions = solver.getSolutions(clue, query.getCandidateSolutions());
 					boolean correctSolutionInList = candidateSolutions.contains(solution);
 					assertTrue("Failed to find correct solution (\"" + solution +
 								"\") for the clue \"" + clueAsString +"\"", correctSolutionInList);
 					long endTime = System.nanoTime();
 					long durationInMins = (endTime - startTime) / NANOSECONDS_IN_ONE_MINUTE;
 					System.err.println("Time taken for the clue \"" + clueAsString +"\": " + durationInMins + " mins");
 						
 					cluesToSolve.remove(clueAsString);
 					solutions.remove(solution);
 			}
 	}	
 }
