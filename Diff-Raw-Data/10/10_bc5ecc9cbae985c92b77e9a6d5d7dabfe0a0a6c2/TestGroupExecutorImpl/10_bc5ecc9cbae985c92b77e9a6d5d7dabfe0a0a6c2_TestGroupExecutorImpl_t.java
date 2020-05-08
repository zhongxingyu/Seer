 package net.sf.testium.executor;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOError;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.testtoolinterfaces.testresult.TestCaseResult;
 import org.testtoolinterfaces.testresult.TestCaseResultLink;
 import org.testtoolinterfaces.testresult.TestExecItemIterationResult;
 import org.testtoolinterfaces.testresult.TestExecItemSelectionResult;
 import org.testtoolinterfaces.testresult.TestGroupEntryResult;
 import org.testtoolinterfaces.testresult.TestGroupResult;
 import org.testtoolinterfaces.testresult.TestResult.VERDICT;
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testresult.TestStepResultBase;
 import org.testtoolinterfaces.testresult.impl.TestCaseResultImpl;
 import org.testtoolinterfaces.testresult.impl.TestExecItemIterationResultImpl;
 import org.testtoolinterfaces.testresult.impl.TestExecItemSelectionResultImpl;
 import org.testtoolinterfaces.testresult.impl.TestGroupResultImpl;
 import org.testtoolinterfaces.testresult.impl.TestGroupResultLinkImpl;
 import org.testtoolinterfaces.testresultinterface.TestGroupResultWriter;
 import org.testtoolinterfaces.testsuite.TestCase;
 import org.testtoolinterfaces.testsuite.TestCaseImpl;
 import org.testtoolinterfaces.testsuite.TestCaseLink;
 import org.testtoolinterfaces.testsuite.TestGroup;
 import org.testtoolinterfaces.testsuite.TestGroupEntry;
 import org.testtoolinterfaces.testsuite.TestGroupEntryIteration;
 import org.testtoolinterfaces.testsuite.TestGroupEntrySelection;
 import org.testtoolinterfaces.testsuite.TestGroupEntrySequence;
 import org.testtoolinterfaces.testsuite.TestGroupImpl;
 import org.testtoolinterfaces.testsuite.TestGroupLink;
 import org.testtoolinterfaces.testsuite.TestLinkImpl;
 import org.testtoolinterfaces.testsuite.TestStep;
 import org.testtoolinterfaces.testsuite.TestStepSequence;
 import org.testtoolinterfaces.testsuiteinterface.TestGroupReader;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.RunTimeVariable;
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
 
 	/**
 	 * Executes the TestGroupLink
 	 * The preparation must have been done
 	 * 
 	 * @param aTestGroupLink
 	 * @param aParentTgResult
 	 * @param aParentEnv
 	 */
 	public void execute( TestGroupLink aTestGroupLink,
             TestGroupResult aParentTgResult,
             ExecutionEnvironment aParentEnv ) {
 
 		TestGroup testGroup = this.readTgLink(aTestGroupLink);
 		ExecutionEnvironment execEnv = this.setExecutionEnvironment(aTestGroupLink,
 				aParentEnv.getLogDir(), aParentEnv.getRtData());
 		
 		File logFile = execEnv.getLogFile( testGroup.getId() );
 		TestGroupResult tgResult = createTgResult(testGroup, execEnv, logFile);
 		tgResult.setExecutionPath( aParentTgResult.getExecutionIdPath() );
 
 		TestGroupResultLinkImpl tgResultLink = new TestGroupResultLinkImpl( aTestGroupLink,
                 logFile );
     	tgResult.register(tgResultLink);
     	aParentTgResult.addTestExecItemResultLink(tgResultLink);
 		
 		execute( testGroup, tgResult, execEnv );
 	}
 
 	private ExecutionEnvironment setExecutionEnvironment( TestGroupLink aTestGroupLink,
 	                     File aLogDir, RunTimeData aRTData ) {
 		if ( !aLogDir.isDirectory() )
 		{
 			FileNotFoundException exc = new FileNotFoundException("Directory does not exist: " + aLogDir.getPath());
 			throw new IOError( exc );
 		}
 		Trace.println(Trace.EXEC, "setExecutionEnvironment( " 
 				+ aTestGroupLink.getId() + ", "
 	            + aLogDir.getPath() + ", "
 				+ aRTData.size() + " Variables )", true );
 
 		File testGroupFile = aTestGroupLink.getLink();
 		File scriptDir = testGroupFile.getParentFile();
 
 		File groupLogDir = new File(aLogDir, aTestGroupLink.getId());
 		groupLogDir.mkdir();
 
 		return new ExecutionEnvironment(scriptDir, groupLogDir, aRTData );
 	}
 
 	private TestGroup readTgLink( TestGroupLink aTestGroupLink ) {
 		
 		File testGroupFile = aTestGroupLink.getLink();
 		return myTestGroupReader.readTgFile(testGroupFile);
 	}
 
 	/**
 	 * @param testGroup
 	 * @param execEnv
 	 * @param logFile
 	 * @return testGrouResult
 	 */
 	private TestGroupResult createTgResult(TestGroup testGroup,
 			ExecutionEnvironment execEnv, File logFile) {
 		TestGroupResult tgResult = new TestGroupResultImpl( testGroup );
     	myTestGroupResultWriter.write( tgResult, logFile );
 
     	return tgResult;
 	}
 
 	@Deprecated
 	public void execute( TestGroupLink aTestGroupLink,
 	                     File aLogDir,
 	                     TestGroupResult aTestGroupResult,
 	                     RunTimeData aRTData )
 	{
 		ExecutionEnvironment env = this.setExecutionEnvironment(aTestGroupLink, aLogDir, aRTData);
 		this.execute(aTestGroupLink, aTestGroupResult, env);
 	}
 
 	public void execute(TestGroup aTestGroup, TestGroupResult aTestGroupResult, ExecutionEnvironment anEnv) {
 		Trace.println(Trace.EXEC, "execute( " + aTestGroup.getId() + " )", true);
 
 		TestStepSequence prepareSteps = aTestGroup.getPrepareSteps();
 		executePrepareSteps(prepareSteps, aTestGroupResult, anEnv);
 
 		TestGroupEntrySequence execSteps = aTestGroup.getExecutionEntries();
 		executeExecSteps(execSteps, aTestGroupResult, anEnv);
 
 		TestStepSequence restoreSteps = aTestGroup.getRestoreSteps();
 		executeRestoreSteps(restoreSteps, aTestGroupResult, anEnv);
 	}
 
 	@Deprecated
 	public void execute(TestGroup aTestGroup, File aScriptDir, File aLogDir,
 			TestGroupResult aTestGroupResult, RunTimeData aRTData) {
 
 		ExecutionEnvironment env = new ExecutionEnvironment(aScriptDir,
 				aLogDir, aRTData);
 		this.execute(aTestGroup, aTestGroupResult, env);
 	}
 
 	public void executePrepareSteps(TestStepSequence aPrepareSteps,
 			TestGroupResult aResult, ExecutionEnvironment anEnv) {
 		Trace.println(Trace.EXEC_PLUS, "executePrepareSteps( " + aPrepareSteps
 				+ ", " + aResult + " )", true);
 
 		Iterator<TestStep> stepsItr = aPrepareSteps.iterator();
 		while (stepsItr.hasNext()) {
 			TestStep step = stepsItr.next();
 			TestStepResultBase tsResult = myTestStepExecutor.execute(step,
 					anEnv.getScriptDir(), anEnv.getLogDir(), anEnv.getRtData());
 			tsResult.setExecutionPath(aResult.getExecutionPath() + "."
 					+ aResult.getId());
 			aResult.addInitialization(tsResult);
 		}
 	}
 
 	public void executeExecSteps(TestGroupEntrySequence anExecEntries,
 			TestGroupResult aResult, ExecutionEnvironment anEnv) {
 		Trace.println(Trace.EXEC_PLUS, "executeExecSteps( " + anExecEntries
 				+ ", " + aResult + " )", true);
 
 		Iterator<TestGroupEntry> entriesItr = anExecEntries.iterator();
 		while (entriesItr.hasNext()) {
 			TestGroupEntry entry = entriesItr.next();
 			RunTimeData rtData = new RunTimeData(anEnv.getRtData());
 			ExecutionEnvironment env = new ExecutionEnvironment( anEnv.getScriptDir(),
 					anEnv.getLogDir(), rtData );
 			try {
 				if (entry instanceof TestGroupLink) {
 					executeTestGroupLink((TestGroupLink) entry, aResult, env);
 				} else if (entry instanceof TestCaseLink) {
 					executeTestCaseLink((TestCaseLink) entry, aResult, env);
 				} else if (entry instanceof TestGroupEntryIteration) {
 					executeForeach((TestGroupEntryIteration) entry, aResult, anEnv);
 				} else if (entry instanceof TestGroupEntrySelection) {
 					executeSelection((TestGroupEntrySelection) entry, aResult, anEnv);
 				} else {
 					throw new InvalidTestTypeException(entry.getClass()
 							.getSimpleName(),
 							"Cannot execute execution entries of type "
 									+ entry.getClass().getSimpleName());
 				}
 			} catch (Throwable t) // wider than strictly needed, but plugins can
 								  // be mal-implemented.
 			{
 				String message = "Execution of "
 						+ entry.getClass().getSimpleName()
 						+ " "
 						+ entry.getId()
 						+ " failed:\n"
 						+ t.getMessage()
 						+ "\nTrying to continue, but this may affect further execution...";
 				aResult.addComment(message);
 				Warning.println(message);
 				Trace.print(Trace.ALL, t);
 			}
 		}
 	}
 
 	/**
 	 * @param aResult
 	 * @param aScriptDir
 	 * @param anEnv
 	 */
 	private void executeTestGroupLink(TestGroupLink tgLink, TestGroupResult aResult,
 			ExecutionEnvironment anEnv) {
 		try
 		{
 			String fileName = tgLink.getLink().getPath();
 			String updatedFileName = anEnv.getRtData().substituteVars(fileName);
 			if ( ! fileName.equals(updatedFileName) ) {
 				TestLinkImpl newLink = new TestLinkImpl( updatedFileName, tgLink.getLinkType() );
 				tgLink.setLink(newLink);
 			} else {
 				tgLink.setLinkDir( anEnv.getScriptDir() );
 			}
 
 			myTestGroupExecutor.execute(tgLink, aResult, anEnv );
 		}
 		catch (Throwable t) // wider than strictly needed, but plugins could be mal-interpreted.
 		{
 System.out.println( "ERROR in " + tgLink.getId() );
 t.printStackTrace();
 			Trace.print(LEVEL.ALL, t);
 			throw new Error( t );
 //			String tgId = tgLink.getId();
 			
 //			TestGroup tg = new TestGroupImpl( tgId,
 //											  tgLink.getDescription(),
 //											  tgLink.getSequenceNr(),
 //											  new TestStepSequence(),
 //											  new TestGroupEntrySequence(),
 //											  new TestStepSequence() );
 			
 //			TestGroupResult tgResult = new TestGroupResultImpl( tg );
 //			tgResult.addComment(t.getMessage());
 			
 //			ExecutionEnvironment env = this.setExecutionEnvironment(tgLink, anEnv.getLogDir(), anEnv.getRtData());
 
 //			TestGroupResultLink tgResultLink = this.writeTestGroup(tgResult, tgLink.getSequenceNr(), env);
 
 //			aResult.addTestExecItemResultLink(tgResultLink);
 		}
 	}
 
 	/**
 	 * @param tcLink
 	 * @param aResult
 	 * @param anEnv
 	 * @throws Error
 	 */
 	private void executeTestCaseLink(TestCaseLink tcLink, TestGroupResult aResult, ExecutionEnvironment anEnv)
 			throws Error {
 		if ( myTestCaseExecutor == null )
 		{
 			throw new Error("No Executor is defined for TestCaseLinks");
 		}
 		
 		try
 		{
 			String fileName = tcLink.getLink().getPath();
 			String updatedFileName = anEnv.getRtData().substituteVars(fileName);
 			if ( ! fileName.equals(updatedFileName) ) {
 				TestLinkImpl newLink = new TestLinkImpl( updatedFileName, tcLink.getLinkType() );
 				tcLink.setLink(newLink);
 			} else {
 				tcLink.setLinkDir( anEnv.getScriptDir() );
 			}
 
 			TestCaseResultLink tcResult = myTestCaseExecutor.execute(tcLink, anEnv.getLogDir(), anEnv.getRtData());
 			tcResult.setExecutionPath( aResult.getExecutionPath() + "." + aResult.getId() );
 			aResult.addTestExecItemResultLink(tcResult);
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
 
 			TestCaseResult tcResult = new TestCaseResultImpl( tc );
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
 
 	/**
 	 * @param entryIteration
 	 * @param aResult
 	 * @param anEnv
 	 */
 	private void executeForeach(TestGroupEntryIteration entryIteration,
 			TestGroupResult aResult, ExecutionEnvironment anEnv) {
 		String listName = entryIteration.getListName();
 		String listElement = entryIteration.getItemName();
 		@SuppressWarnings("unchecked")
 		ArrayList<Object> list = anEnv.getRtData().getValueAs(ArrayList.class, listName);
 		TestGroupEntrySequence doSteps = new TestGroupEntrySequence( entryIteration.getSequence() );
 		TestStep untilStep = entryIteration.getUntilStep();
 		
 		TestExecItemIterationResult teiIterationResult = new TestExecItemIterationResultImpl(entryIteration);
 
		if ( list == null ) {
			teiIterationResult.addComment("List " + listName + " is not set." );
			aResult.addTgEntryResult(teiIterationResult);
			return;
		}
		
 		Iterator<Object> listItr = list.iterator();
 		int seqNr = entryIteration.getSequenceNr();
 		int foreachSeqNr = 0;
 		while (listItr.hasNext() ) {
 			Object element = listItr.next();
 			teiIterationResult.addIterationValue(element);
 			String tgId = entryIteration.getId() + "_" + foreachSeqNr++;
 
 			TestGroup tg = new TestGroupImpl(tgId, seqNr,
 					new TestStepSequence(), doSteps, new TestStepSequence());
 
 			RunTimeVariable rtVariable = new RunTimeVariable( listElement, element );
 
 			RunTimeData subRtData = new RunTimeData( anEnv.getRtData() );
 			subRtData.add(rtVariable);
 			ExecutionEnvironment env = new ExecutionEnvironment( anEnv.getScriptDir(),
 					anEnv.getLogDir(), subRtData );
 
 			List<TestGroupEntryResult> tgEntryResults = executeTestGroupEntries(tg, env);
 			
 			teiIterationResult.addExecResult(tgEntryResults);
 		
 			if ( untilStep != null ) {
 				TestStepResult untilResult = (TestStepResult) myTestStepExecutor.execute(untilStep, env.getScriptDir(), env.getLogDir(), env.getRtData());
 				if (untilResult.getResult() == VERDICT.PASSED) {
 					teiIterationResult.addUntilResult(untilResult);
 					break;
 				}
 			}
 		}
 
 		aResult.addTgEntryResult(teiIterationResult);
 	}
 
 	private void executeSelection( TestGroupEntrySelection selectionEntry,
 			TestGroupResult aResult, ExecutionEnvironment anEnv ) {
 		String tgId = selectionEntry.getId();
 
 		TestStep ifStep = selectionEntry.getIfStep();
 		TestStepResult ifResult = (TestStepResult) myTestStepExecutor.execute(ifStep,
 				anEnv.getScriptDir(), anEnv.getLogDir(), anEnv.getRtData());
 
 		TestStepSequence prepSteps = new TestStepSequence();
 		prepSteps.add(ifStep);
 
 		boolean negator = selectionEntry.getNegator();
 
 		TestGroupEntrySequence execEntries = new TestGroupEntrySequence();
 
 		TestExecItemSelectionResult teiSelectionResult = new TestExecItemSelectionResultImpl(selectionEntry);
 		teiSelectionResult.setIfResult(ifResult);
 //		String comment = "If-step evaluated to " + ifResult.getResult() + ".";
 		if ( ifResult.getResult().equals(VERDICT.ERROR)
 			 || ifResult.getResult().equals(VERDICT.UNKNOWN) ) {
 			 // NOP, we don't execute the then or else!
 			
 		} else {
 			
 			if ( ifResult.getResult().equals( negator ? VERDICT.FAILED : VERDICT.PASSED) ) {
 				execEntries = selectionEntry.getThenSteps();
 //				comment += " Then-sequence executed.";
 
 			} else {
 				execEntries = selectionEntry.getElseSteps();
 //				comment += execEntries.isEmpty() ? " Nothing executed." : " Else-sequence executed.";
 			}
 		}
 
 		TestGroup tg = new TestGroupImpl(tgId, selectionEntry.getSequenceNr(),
 				prepSteps, execEntries, new TestStepSequence());
 
 // TODO Now the if-step is lost. Make it a sub test group. Either special or with the if-step in prepare.
 //		TestGroupResultLink groupResultLink = 
 //				executeTestGroupEntries(tg, aScriptDir, aLogDir, aRTData);
 //		aResult.addTestGroup(groupResultLink);
 
 		execute(tg, teiSelectionResult, anEnv);
 
 //		TestGroupResult result = new TestGroupResult(tg);
 //		
 		aResult.addTgEntryResult(teiSelectionResult);
 //
 //		ifResult.setExecutionPath( result.getExecutionPath() + "." + result.getId() );
 //		result.addInitialization(ifResult);
 //
 //		this.executeExecSteps(execEntries, result, aScriptDir, aLogDir, aRTData);
 //		result.setComment(comment);
 	}
 
 	/**
 	 * @param tg
 	 * @param aScriptDir
 	 * @param aLogDir
 	 * @param subRtData
 	 */
 //	private TestGroupResultLink executeTestGroupEntries(TestGroup tg, ExecutionEnvironment anEnv) {
 	private List<TestGroupEntryResult> executeTestGroupEntries(TestGroup tg, ExecutionEnvironment anEnv) {
 		
 		String tgId = tg.getId();
 		TestGroupEntrySequence doSteps = tg.getExecutionEntries();
 
 		TestGroupResult groupResult = new TestGroupResultImpl( tg );
 
 		File logDir = new File (anEnv.getLogDir(), tgId );
 		logDir.mkdir();
 		ExecutionEnvironment env = new ExecutionEnvironment( anEnv.getScriptDir(), logDir, anEnv.getRtData() );
 		
 		this.executeExecSteps(doSteps, groupResult, env);
 
 		return groupResult.getTestGroupEntryResults();
 	}
 
 //	/**
 //	 * @param groupResult
 //	 * @param tgId
 //	 * @param seqNr
 //	 * @param logDir
 //	 * @return
 //	 */
 //	private TestGroupResultLink writeTestGroup(TestGroupResult groupResult,	int seqNr, ExecutionEnvironment anEnv) {
 //		
 //		String tgId = groupResult.getId();
 //		File logFile = anEnv.getLogFile(tgId);
 //		myTestGroupResultWriter.write( groupResult, logFile );
 //		
 //		TestGroupLink tgLink = new TestGroupLink(tgId, seqNr, logFile.getName());
 //
 //		TestGroupResultLinkImpl tgResultLink = new TestGroupResultLinkImpl( tgLink, logFile );
 //		tgResultLink.setSummary( groupResult.getSummary() );
 //		groupResult.register(tgResultLink);
 //		return tgResultLink;
 //	}
 
 	public void executeRestoreSteps(TestStepSequence aRestoreSteps,
 			TestGroupResult aResult, ExecutionEnvironment anEnv ) {
 		Trace.println(Trace.EXEC_PLUS, "executeRestoreSteps( " + aRestoreSteps
 				+ ", " + aResult + " )", true);
 
 		Iterator<TestStep> stepsItr = aRestoreSteps.iterator();
 		while (stepsItr.hasNext()) {
 			TestStep step = stepsItr.next();
 			TestStepResultBase tsResult = myTestStepExecutor.execute(step,
 					anEnv.getScriptDir(), anEnv.getLogDir(), anEnv.getRtData());
 			tsResult.setExecutionPath(aResult.getExecutionPath() + "."
 					+ aResult.getId());
 			aResult.addRestore(tsResult);
 		}
 	}
 	
 	public String getType()
 	{
 		return TYPE;
 	}
 }
