 package dsolve.lfs;
 
 import dsolve.GlobalModelBuilder;
 import dsolve.GlobalSolver;
 import dsolve.LocalSolver;
 import ilog.concert.IloException;
 import junit.framework.Assert;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: imcu
  * Date: 11/19/12
  * Time: 3:22 PM
  */
 
 public class GlobalSolverTest {
 
 	@Test
 	public void testGlobalSolverWithRandomFeasibleSystem () throws IOException, IloException {
 
 		int records = 10;
 		int dimensions = 5;
 		int blocks = 2;
 
 		GlobalModelBuilder modelBuilder = new GlobalModelBuilder();
 		String generatedFileName = LfsTestUtils.generateRandomInput( records, dimensions, null );
 
 		modelBuilder.readModelFromFile( generatedFileName );
 		List<String> blockModelFiles = modelBuilder.splitIntoBlockFiles( records/blocks, true );
 
 		String objectiveFile = LfsTestUtils.generateObjectivePoint( dimensions, null );
 
 		GlobalSolver globalSolver = new GlobalSolver( blockModelFiles, objectiveFile );
 
		Assert.assertTrue( "Localsolver didn't manage to solve the most simple system ever", globalSolver.runSolver( 200 ) );
 	}
 
 }
