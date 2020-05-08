 package net.sf.testium.executor;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOError;
 import java.util.Iterator;
 
 import org.testtoolinterfaces.testresult.TestCaseResult;
 import org.testtoolinterfaces.testresult.TestCaseResultLink;
 import org.testtoolinterfaces.testresult.TestGroupResult;
 import org.testtoolinterfaces.testresult.TestGroupResultLink;
 import org.testtoolinterfaces.testresult.TestResult.VERDICT;
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testresultinterface.TestGroupResultWriter;
 import org.testtoolinterfaces.testsuite.TestCase;
 import org.testtoolinterfaces.testsuite.TestCaseImpl;
 import org.testtoolinterfaces.testsuite.TestCaseLink;
 import org.testtoolinterfaces.testsuite.TestEntry;
 import org.testtoolinterfaces.testsuite.TestEntrySequence;
 import org.testtoolinterfaces.testsuite.TestGroup;
 import org.testtoolinterfaces.testsuite.TestGroupImpl;
 import org.testtoolinterfaces.testsuite.TestGroupLink;
 import org.testtoolinterfaces.testsuite.TestStep;
 import org.testtoolinterfaces.testsuite.TestStepSequence;
 import org.testtoolinterfaces.testsuiteinterface.TestGroupReader;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.Trace;
 import org.testtoolinterfaces.utils.Trace.LEVEL;
 import org.testtoolinterfaces.utils.Warning;
 
 public class TestGroupExecutorImpl implements TestGroupExecutor
 {
 	private static final String		TYPE = "tti";
 
 	private TestStepMetaExecutor	myTestStepExecutor;
 	private TestGroupResultWriter 	myTestGroupResultWriter;
 
 	private TestGroupReader			myTestGroupReader;
 	private TestGroupMetaExecutor	myTestGroupExecutor;
 	private TestCaseMetaExecutor	myTestCaseExecutor;
 
 	/**
 	 * @param aTestRunResultWriter 
 	 * @param myTestStepExecutor
 	 * @param myTestCaseScriptExecutor
 	 */
 	public TestGroupExecutorImpl( TestStepMetaExecutor aTestStepMetaExecutor,
 	                              TestCaseMetaExecutor aTestCaseMetaExecutor,
 	                              TestGroupMetaExecutor aTestGroupMetaExecutor,
 	                              TestGroupResultWriter aTestGroupResultWriter )
 	{
 		myTestStepExecutor = aTestStepMetaExecutor;
 		myTestCaseExecutor = aTestCaseMetaExecutor;
 		myTestGroupExecutor = aTestGroupMetaExecutor;
 		myTestGroupResultWriter = aTestGroupResultWriter;
 
 		myTestGroupReader = new TestGroupReader( myTestStepExecutor.getInterfaces(), true );
 	}
 
 	public void execute( TestGroupLink aTestGroupLink,
 	                     File aLogDir,
 	                     TestGroupResult aTestGroupResult,
 	                     RunTimeData aRTData )
 	{
 		if ( !aLogDir.isDirectory() )
 		{
 			FileNotFoundException exc = new FileNotFoundException("Directory does not exist: " + aLogDir.getPath());
 			throw new IOError( exc );
 		}
 		Trace.println(Trace.EXEC, "execute( " 
 				+ aTestGroupLink.getId() + ", "
 	            + aLogDir.getPath() + ", "
 	            + aTestGroupResult.getId() + ", "
 				+ aRTData.size() + " Variables )", true );
 
 		File testGroupFile = aTestGroupLink.getLink();
 		File scriptDir = testGroupFile.getParentFile();
 		TestGroup testGroup = myTestGroupReader.readTgFile(testGroupFile);
 
 		File groupLogDir = new File(aLogDir, aTestGroupLink.getId());
 		groupLogDir.mkdir();
 
 		File logFile = new File(groupLogDir, aTestGroupLink.getId() + "_log.xml");
 		TestGroupResult tgResult = new TestGroupResult( testGroup );
 		tgResult.setExecutionPath( aTestGroupResult.getExecutionPath() + "." + aTestGroupResult.getId() );
     	myTestGroupResultWriter.write( tgResult, logFile );
 
     	TestGroupResultLink tgResultLink = new TestGroupResultLink( aTestGroupLink,
     	                                                            logFile );
     	tgResult.register(tgResultLink);
     	aTestGroupResult.addTestGroup(tgResultLink);
 
 		execute( testGroup, scriptDir, groupLogDir, tgResult, aRTData );
 	}
 
 	public void execute( TestGroup aTestGroup,
 	                     File aScriptDir,
 	                     File aLogDir,
 	                     TestGroupResult aTestGroupResult,
 	                     RunTimeData aRTData )
 	{
 		Trace.println( Trace.EXEC, "execute( " 
 						+ aTestGroup.getId() + ", "
 						+ aScriptDir.getPath() + ", "
 						+ aLogDir.getPath() + ", "
 						+ aTestGroupResult.getId() + ", "
 						+ aRTData.size() + " Variables )", true );
 
     	TestStepSequence prepareSteps = aTestGroup.getPrepareSteps();
     	executePrepareSteps(prepareSteps, aTestGroupResult, aScriptDir, aLogDir, aRTData);
 
     	TestEntrySequence execSteps = aTestGroup.getExecutionEntries();
 		executeExecSteps(execSteps, aTestGroupResult, aScriptDir, aLogDir, aRTData);
 
     	TestStepSequence restoreSteps = aTestGroup.getRestoreSteps();
     	executeRestoreSteps(restoreSteps, aTestGroupResult, aScriptDir, aLogDir, aRTData);
 	}
 
 	public void executePrepareSteps( TestStepSequence aPrepareSteps,
 	                                 TestGroupResult aResult,
 	                                 File aScriptDir,
 	                                 File aLogDir,
 	                                 RunTimeData aRTData )
 	{
 		Trace.println(Trace.EXEC_PLUS, "executePrepareSteps( " 
 						+ aPrepareSteps + ", "
 						+ aResult + ", "
 			            + aLogDir.getPath()
 			            + " )", true );
 
 		Iterator<TestStep> stepsItr = aPrepareSteps.iterator();
 		while(stepsItr.hasNext())
 		{
 		    TestStep step = stepsItr.next();
 			TestStepResult tsResult = myTestStepExecutor.execute(step, aScriptDir, aLogDir, aRTData);
 			tsResult.setExecutionPath( aResult.getExecutionPath() + "." + aResult.getId() );
 			aResult.addInitialization(tsResult);
     	}
 	}
 
 	public void executeExecSteps( TestEntrySequence anExecEntries,
 	                              TestGroupResult aResult,
 	                              File aScriptDir,
 	                              File aLogDir,
 	                              RunTimeData aRTData )
 	{
 		Trace.println(Trace.EXEC_PLUS, "executeExecSteps( " 
 						+ anExecEntries + ", "
 						+ aResult + ", "
 			            + aScriptDir.getPath() + ", "
 			            + aLogDir.getPath() + ", "
 						+ aRTData.size() + " Variables )", true );
 
 		Iterator<TestEntry> entriesItr = anExecEntries.iterator();
 		while(entriesItr.hasNext())
 		{
 			TestEntry entry = entriesItr.next();
 			RunTimeData rtData = new RunTimeData( aRTData );
 			try
 			{
 				if ( entry.getType() == TestEntry.TYPE.GroupLink )
 				{
 					TestGroupLink tgLink = (TestGroupLink) entry;
 					try
 					{
						tgLink.setLinkDir( aScriptDir );
 
 						myTestGroupExecutor.execute(tgLink, aLogDir, aResult, rtData );
 						
 					}
 					catch (Throwable t) // wider than strictly needed, but plugins could be mal-interpreted.
 					{
 						Trace.print(LEVEL.ALL, t);
 						String tgId = tgLink.getId();
 						
 						TestGroup tg = new TestGroupImpl( tgId,
 														  tgLink.getDescription(),
 														  tgLink.getSequenceNr(),
 														  new TestStepSequence(),
 														  new TestEntrySequence(),
 														  new TestStepSequence() );
 						
 						TestGroupResult tgResult = new TestGroupResult( tg );
 						tgResult.setResult(VERDICT.ERROR);
 						tgResult.addComment(t.getMessage());
 						
 						File logDir = new File (aLogDir, tgId );
 						logDir.mkdir();
 						
 						File logFile = new File( logDir, tgId + "_log.xml" );
 				    	myTestGroupResultWriter.write( tgResult, logFile );
 
 				    	TestGroupResultLink tgResultLink = new TestGroupResultLink( tgLink, logFile );
 						tgResult.register(tgResultLink);
 						aResult.addTestGroup(tgResultLink);
 					}
 				}
 				else if ( entry.getType() == TestEntry.TYPE.CaseLink )
 				{
 					if ( myTestCaseExecutor == null )
 					{
 						throw new Error("No Executor is defined for TestCaseLinks");
 					}
 					
 					TestCaseLink tcLink = (TestCaseLink) entry;
 					try
 					{
 						String fileName = tcLink.getLink().getPath();
 						String updatedFileName = rtData.substituteVars(fileName);
						File linkFile = new File(updatedFileName);
						File linkDir = linkFile.getParentFile();
						if ( linkDir != null ) {
							if ( linkFile.isAbsolute() ) {
								tcLink.setLinkDir( linkDir );
							} else {
								File newParentDir = new File( aScriptDir, linkDir.getPath() );
								tcLink.setLinkDir( newParentDir );
							}
 						} else {
 							tcLink.setLinkDir( aScriptDir );
 						}
 
 						TestCaseResultLink tcResult = myTestCaseExecutor.execute(tcLink, aLogDir, rtData);
 						tcResult.setExecutionPath( aResult.getExecutionPath() + "." + aResult.getId() );
 						aResult.addTestCase(tcResult);
 					}
 					catch (Throwable t)
 					{
 						Trace.print(LEVEL.ALL, t);
 						String tcId = tcLink.getId();
 						
 						TestCase tc = new TestCaseImpl( tcId,
 								  tcLink.getDescription(),
 								  tcLink.getSequenceNr(),
 								  new TestStepSequence(),
 								  new TestStepSequence(),
 								  new TestStepSequence() );
 
 						TestCaseResult tcResult = new TestCaseResult( tc );
 						tcResult.setResult(VERDICT.ERROR);
 						tcResult.addComment(t.getMessage());
 						
 //						File logDir = new File (aLogDir, tcId );
 //						logDir.mkdir();
 //						
 //						File logFile = new File( logDir, tcId + "_log.xml" );
 //				    	myTestCaseResultWriter.write( tcResult, logFile );
 //
 //				    	TestCaseResultLink tcResultLink = new TestCaseResultLink( tcLink, VERDICT.ERROR, logFile );
 //						aResult.addTestCase(tcResultLink);
 					}
 				}
 				else
 				{
 					throw new InvalidTestTypeException( entry.getType().toString(), "Cannot execute execution entries of type " + entry.getType() );
 				}
 			}
 			catch (Throwable t) // wider than strictly needed, but plugins could be mal-interpreted.
 			{
 				String message = "Execution of " + entry.getType() + " " + entry.getId() + " failed:\n"
 					+ t.getMessage()
 					+ "\nTrying to continue, but this may affect further execution...";
 				aResult.addComment(message);
 				Warning.println(message);
 				Trace.print(Trace.ALL, t);
 			}
     	}
 	}
 
 	public void executeRestoreSteps( TestStepSequence aRestoreSteps,
 	                                 TestGroupResult aResult,
 	                                 File aScriptDir,
 	                                 File aLogDir,
 	                                 RunTimeData aRTData )
 	{
 		Trace.println(Trace.EXEC_PLUS, "executeRestoreSteps( " 
 						+ aRestoreSteps + ", "
 						+ aResult + ", "
 			            + aLogDir.getPath()
 			            + " )", true );
 
 		Iterator<TestStep> stepsItr = aRestoreSteps.iterator();
 		while(stepsItr.hasNext())
 		{
 		    TestStep step = stepsItr.next();
 			TestStepResult tsResult = myTestStepExecutor.execute(step, aScriptDir, aLogDir, aRTData);
 			tsResult.setExecutionPath( aResult.getExecutionPath() + "." + aResult.getId() );
 			aResult.addRestore(tsResult);
     	}
 	}
 	
 	public String getType()
 	{
 		return TYPE;
 	}
 }
