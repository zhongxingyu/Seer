 /* Copyright (c) 2013, Regents of the University of California.  See License.txt for details */
 
 package edu.uci.imbs.actor.fit;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.EnhancedPatternLayout;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.grayleaves.utility.fit.ParameterSpaceFixture;
import org.grayleaves.utility.Constants;
 import org.grayleaves.utility.DummyInput;
import org.grayleaves.utility.Environment;
 import org.grayleaves.utility.MockClock;
 import org.grayleaves.utility.MockHibernateScenarioSet;
 import org.grayleaves.utility.NameValuePair;
 import org.grayleaves.utility.NameValuePairBuilder;
import org.grayleaves.utility.OutputFileBuilder;
import org.grayleaves.utility.OutputFileBuilderException;
 import org.grayleaves.utility.ParameterSpace;
 import org.grayleaves.utility.ParameterSpacePersister;
 import org.grayleaves.utility.ScenarioException;
 import org.grayleaves.utility.ScenarioSet;
 
 import edu.uci.imbs.actor.ProtectionParameters;
 import edu.uci.imbs.actor.VariablePopulationProtectionStatistics;
 import edu.uci.imbs.actor.integration.ProtectionModel;
 import fitlibrary.DoFixture;
 
 public class ProtectionFixture extends DoFixture
 {
 	private static Logger logger = Logger.getLogger(ProtectionFixture.class);
 //	private ParameterSpaceFixture memoryNetworkParameterSpaceFixture;
 //	private InputOutputFixture inputOutputFixture;
 //	private ProtectionFixture memoryNetworkFixture;
 //	private MemoryNetwork network;
 	public static int INTERMEDIATE_NODES = 10; // default 
 //	private MemoryNetworkMo	delInput input;
 //	private String networkName;
 	private FileAppender appender;
 	private ParameterSpaceFixture protectionParameterSpaceFixture;
 	private static final String LAYOUT = "%p %c{2}:  %m %n";
	private ScenarioSet<String, DummyInput> scenarioSet;
 
 	
 	public ProtectionFixture() 
 	{
 		try
 		{
 			configureAppender();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		} 
 	}
 	
 	public void protectionParameterSpace()
 	{
 		protectionParameterSpaceFixture = new ParameterSpaceFixture(); 
 		setSystemUnderTest(protectionParameterSpaceFixture); 
 	}
 	protected void configureAppender() throws IOException  
 	{
 		appender = new FileAppender(new EnhancedPatternLayout(LAYOUT), "ProtectionFixture.log");
 		appender.setName(this.toString());
 		BasicConfigurator.resetConfiguration(); 
 		Logger.getRootLogger().setLevel(Level.INFO);
 		logger.addAppender(appender); 
 		
 		
 //		throw new ScenarioException("name " + appender.getName()+
 //				"file " +appender.getFile() +
 //				"immediate? " + appender.getImmediateFlush() + " numb " + logger.getAllAppenders().hasMoreElements()
 //				);
 //		BasicConfigurator.configure(appender); 
 	}
 
 	public void runScenarios() throws ScenarioException
 	{
 		ProtectionParameters.resetForTesting(); //FIXME replace once Range Parameter supported for doubles...
		scenarioSet = new MockHibernateScenarioSet<String, DummyInput>(false);		
 		scenarioSet.setName("protection"); // perhaps a different name  
 		scenarioSet.setModel(new ProtectionModel<String>());
 		scenarioSet.setInput(new DummyInput());
 		scenarioSet.setParameterSpace(protectionParameterSpaceFixture.getParameterSpace()); 
 		scenarioSet.setCalendar(MockClock.getCalendar());
 		scenarioSet.setId(buildMockId());
 		buildSummaryHeader(scenarioSet);
 		logger.info("Starting: scenarioSet.run(); ");
 		scenarioSet.batchRun(); 
 		saveParameterSpace(scenarioSet); 
 	}
 
 	protected void buildSummaryHeader(
 			ScenarioSet<String, DummyInput> scenarioSet)
 	{
 		List<String> headings = ((VariablePopulationProtectionStatistics) ((ProtectionModel<String>) scenarioSet.getModel()).getSeeker().getProtectionStatistics()).getPeasantProportionHeadings(); //TODO a little less cryptic 
 		NameValuePairBuilder b = new NameValuePairBuilder(); 
 		List<NameValuePair<?>> pairs = new ArrayList<NameValuePair<?>>(); 
 		pairs.add(b.integerPair("Stop Reason Code"));
 		pairs.add(b.integerPair("Period"));
 		pairs.add(b.integerPair("Number Bandits"));
 		pairs.add(b.integerPair("Number Peasants"));
 		pairs.add(b.integerPair("Number Bandits After Replication"));
 		pairs.add(b.integerPair("Number Peasants After Replication"));
 		pairs.add(b.doublePair("Average Bandit Payoff"));
 		pairs.add(b.doublePair("Average Peasant Payoff"));
 		pairs.add(b.doublePair("Bandit-Peasant Payoff Delta"));
 		pairs.add(b.integerPair("Actor Adjustment"));
 		pairs.add(b.doublePair("Average Protection Proportion"));
 		pairs.add(b.doublePair("Median Protection Proportion"));
 		pairs.add(b.doublePair("Mode Protection Proportion"));
 		pairs.add(b.doublePair("Average Number Peasants To Prey Upon"));
 		pairs.add(b.doublePair("Median Number Peasants To Prey Upon"));
 		pairs.add(b.integerPair("Mode Number Peasants To Prey Upon"));
 		Iterator<String> it = headings.iterator();
 		while (it.hasNext())
 		{
 			pairs.add(b.integerPair(it.next()));
 			pairs.add(b.doublePair(it.next()));
 		}
 //		NameValuePair<?>[] arrayPairs = new NameValuePair<?>[pairs.size()]; 
 		scenarioSet.buildSummaryHeader(pairs.toArray(new NameValuePair<?>[pairs.size()])); 
 //		scenarioSet.buildSummaryHeader(b.integerPair("Stop Reason Code"), b.integerPair("Period"), b.integerPair("Number Bandits"), b.integerPair("Number Peasants"), b.integerPair("Number Bandits After Replication"), b.integerPair("Number Peasants After Replication"), 
 //				b.doublePair("Average Bandit Payoff"), b.doublePair("Average Peasant Payoff"), b.doublePair("Bandit-Peasant Payoff Delta"), b.integerPair("Actor Adjustment"), 
 //				b.doublePair("Average Protection Proportion"), b.doublePair("Median Protection Proportion"),b.doublePair("Mode Protection Proportion"),
 //			    b.doublePair("Average Number Peasants To Prey Upon"), b.doublePair("Median Number Peasants To Prey Upon"),b.integerPair("Mode Number Peasants To Prey Upon"));
 	}
 	private void saveParameterSpace(ScenarioSet<String, DummyInput> scenarioSet) throws ScenarioException
 	{
 		try
 		{
 			String name = "parameterSpaceScenario_"+scenarioSet.getId(); 
			String filename = scenarioSet.getOutputFileBuilder().getRootDirectoryFullPathName()+Constants.SLASH+name+".xml"; 
 //			scenarioSet.getParameterSpace().setName(name);
 			scenarioSet.getParameterSpace().setFilename(filename); 
 			ParameterSpacePersister<ParameterSpace> spacePersister = new ParameterSpacePersister<ParameterSpace>();
 			scenarioSet.getParameterSpace().loadProperties("org/grayleaves/utility/testing.properties");  // kinda dumb
 			spacePersister.save(scenarioSet.getParameterSpace(), filename);  //
 		}
 		catch (Exception e)
 		{
 			throw new ScenarioException("ProtectionFixture.saveParameterSpace: "+e.getMessage());
 		}
 	}
 	
	private int buildMockId() throws ScenarioException
 	{
		String pathname = "";
		String envPath = Environment.getEnv("SCENARIO_ROOT");
		if (envPath != null) pathname = envPath+Constants.SLASH;
 		int id = 1;
 		File file = null; 
 		boolean idNotAvailable = true;
 		while (idNotAvailable)
 		{
			file = new File(pathname+"ScenarioSet_"+id);
 			if (!file.exists())
 			{
 				idNotAvailable = false; 
 			}
 			else id++; 
 		}
 		return id;
 	}
 	
 	//* create memorynetworkmodelinput
 	//* create small parameter space
 	// create persistent parameter space
 	// new InputOutputPairs for each trainer
 	// create persistent input
 	// mockclock? 
 	// mock hibernate scenario set?
 	//* simplescenario creates modelcontext
 //	return scenarioSet; 
 
 //	public void 
 //		TestingBean.resetForTesting(); 
 //		BasicConfigurator.resetConfiguration(); 
 //	model = new TestingModel<String>(); 
 //	*parameterSpace = buildSmallParameterSpace(ints);
 //	input = new TestingInput(3); 
 //	input.setFilename("testingInput.xml"); 
 //		MockClock.setDateForTesting("10/15/2005 12:00:14 PM");
 //	scenarioSet = buildScenarioSet(); 
 //	ScenarioSet<String, TestingInput> scenarioSet = new MockHibernateScenarioSet<String, TestingInput>(true);		
 //	scenarioSet.setModel(model);
 //	scenarioSet.setInput(input);
 //	scenarioSet.setParameterSpace(parameterSpace); 
 //	scenarioSet.setCalendar(MockClock.getCalendar());
 //	scenarioSet.setName("test scenario set"); 
 //	return scenarioSet; 
 
 //	tx = getTx(); 
 //	scenarioSet = new ScenarioSet<String, PersistentInput>(false); 
 //	scenarioSet.setName("test scenario set"); 
 //	scenarioSet.setModel(model);
 //	scenarioSet.setInput(persistentInput);
 //	scenarioSet.setParameterSpace(space); 
 //	scenarioSet.setCalendar(MockClock.getCalendar()); 
 //	scenarioSet.buildParameterSpace(); 
 //	session.save(scenarioSet.getParameterSpace());
 //	tx.commit(); 
 //	tx = getTx(); 
 //	scenarioSet.buildScenarios(); 
 //	session.save(scenarioSet); 
 //	tx.commit(); 
 //	tx=getTx();
 //	scenarioSet.runScenarios(); 
 //	session.update(scenarioSet); 
 //	tx.commit(); 
 //	int id = scenarioSet.getId(); 
 //	System.out.println("scenarioSet id: "+scenarioSet.getId());
 //	tx=getTx();
 //	ScenarioSet<String, PersistentInput> set = (ScenarioSet) session.get(ScenarioSet.class, id); 
 //	assertEquals("test scenario set", set.getName());
 //	assertEquals(id, set.getId()); 
 ////	List<ScenarioSet> scenarios = session.createQuery("from ScenarioSet").list(); 
 //	List<Scenario<String, PersistentInput>> list = set.getScenarios(); // scenarios.get(0).getScenarios();
 //	assertEquals("fred, 40", list.get(0).getParameterPoint().toString()); 
 //	assertEquals("fred, 50", list.get(1).getParameterPoint().toString());
 //	assertEquals("sam, 40", list.get(2).getParameterPoint().toString());
 //	assertEquals("sam, 50", list.get(3).getParameterPoint().toString());
 //	tx.commit();
 ////	List<ScenarioResult<String>> results = scenarioSet.getScenarioResults(); 
 ////	assertEquals(0, results.size());
 ////	results = scenarioSet.getScenarioResults(); 
 ////	assertEquals(4, results.size());
 //	assertEquals("TestingModel from input 3\n"+ 
 
 	
 	
 }
