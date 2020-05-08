 package org.kalibro;
 
 import static org.junit.Assert.*;
 import static org.kalibro.Granularity.*;
 import static org.kalibro.ProcessState.*;
 
 import org.junit.Before;
 import org.junit.experimental.theories.Theory;
 import org.junit.rules.Timeout;
 import org.kalibro.tests.AcceptanceTest;
 
 public class NormalProcessingAcceptanceTest extends AcceptanceTest {
 
 	private static final long SLEEP = 10000;
 
 	private Repository repository;
 	private Configuration configuration;
 
 	private Processing processing;
 
 	@Before
 	public void setUp() {
 		configuration = loadFixture("sc-analizo", Configuration.class);
 		String address = repositoriesDirectory().getAbsolutePath() + "/HelloWorldDirectory/";
 		repository = new Repository("Directory repository", RepositoryType.LOCAL_DIRECTORY, address);
 		repository.setConfiguration(configuration);
 		new Project("Hello World").addRepository(repository);
 	}
 
 	@Override
 	protected Timeout testTimeout() {
		// TODO remove it
 		return new Timeout(0);
 	}
 
 	@Theory
 	public void shouldProcessRepository(SupportedDatabase databaseType) throws InterruptedException {
 		resetDatabase(databaseType);
 		assertFalse(Processing.hasProcessing(repository));
 
 		long processingTime = System.currentTimeMillis();
 		repository.process();
 		assertTrue(Processing.hasProcessing(repository));
 		assertFalse(Processing.hasReadyProcessing(repository));
 		assertTrue(Processing.lastProcessingState(repository).isTemporary());
 		Thread.sleep(SLEEP);
 
 		processing = Processing.lastProcessing(repository);
 		assertEquals(processingTime, processing.getDate().getTime(), 500);
 		verifyNormalResults();
 	}
 
 	private void verifyNormalResults() {
 		assertEquals(READY, processing.getState());
 		verifyStateTime(LOADING);
 		verifyStateTime(COLLECTING);
 		verifyStateTime(ANALYZING);
 
 		Metric totalCof = metric("total_cof", false);
 		ModuleResult root = processing.getResultsRoot();
 		assertDeepEquals(new Module(SOFTWARE, "null"), root.getModule());
 		assertDoubleEquals(1.0, root.getResultFor(totalCof).getValue());
 
 		Metric sc = metric("sc", true);
 		Metric lcom4 = metric("lcom4", false);
 		ModuleResult child = root.getChildren().first();
 		assertDeepEquals(new Module(CLASS, "HelloWorld"), child.getModule());
 		assertDoubleEquals(0.0, child.getResultFor(sc).getValue());
 		assertDoubleEquals(1.0, child.getResultFor(lcom4).getValue());
 		assertDoubleEquals(10.0, child.getResultFor(lcom4).getGrade());
 	}
 
 	private void verifyStateTime(ProcessState state) {
 		Long stateTime = processing.getStateTime(state);
 		assertTrue("Time for " + state + ": " + stateTime, 0 < stateTime && stateTime < SLEEP);
 	}
 
 	private Metric metric(String name, boolean compound) {
 		return loadFixture(name, compound ? CompoundMetric.class : NativeMetric.class);
 	}
 }
