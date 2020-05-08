 package de.mh4j.solver;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import org.testng.annotations.Test;
 
 import de.mh4j.solver.termination.StagnationTermination;
 import de.mh4j.solver.termination.TerminationCondition;
 
 public class StagnationTerminationTest {
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testShouldTerminate() {
 		int maxNrOfStagnatingSteps = 50;
 		Solver<Solution<?>> solver = mock(Solver.class);
 
 		when(solver.getCurrentSolution()).thenReturn(createSolution());
 		TerminationCondition terminator = new StagnationTermination(solver,
 				maxNrOfStagnatingSteps);
 
 		assert terminator.shouldTerminate() == false;
 
		for (int i = 0; i < maxNrOfStagnatingSteps - 1; i++) {
 			assert terminator.shouldTerminate() == false;
 		}
 
 		assert terminator.shouldTerminate() == true : "Terminator should terminate now because we asked it "
 				+ maxNrOfStagnatingSteps
 				+ " if we should terminate without any improvement in the associated solver";
 	}
 
 	private Solution<Solution<?>> createSolution() {
 		return new Solution<Solution<?>>() {
 
 			@Override
 			public boolean isBetterThan(Solution<?> otherSolution) {
 				return false;
 			}
 
 			@Override
 			public int getCosts() {
 				return 0;
 			}
 
 			@Override
 			public boolean equals(Object obj) {
 				return true;
 			}
 		};
 	}
 }
