 package de.integrity.runner.callbacks.remoting;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.DecimalFormat;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Stack;
 
 import org.eclipse.emf.common.util.EList;
 
 import de.integrity.dsl.Call;
 import de.integrity.dsl.MethodReference;
 import de.integrity.dsl.Parameter;
 import de.integrity.dsl.Suite;
 import de.integrity.dsl.SuiteDefinition;
 import de.integrity.dsl.TableTest;
 import de.integrity.dsl.TableTestRow;
 import de.integrity.dsl.Test;
import de.integrity.dsl.ValueOrEnumValue;
 import de.integrity.dsl.Variable;
 import de.integrity.dsl.VariableEntity;
 import de.integrity.remoting.entities.setlist.SetList;
 import de.integrity.remoting.entities.setlist.SetListEntry;
 import de.integrity.remoting.entities.setlist.SetListEntryAttributeKeys;
 import de.integrity.remoting.entities.setlist.SetListEntryTypes;
 import de.integrity.remoting.server.IntegrityRemotingServer;
 import de.integrity.runner.TestModel;
 import de.integrity.runner.callbacks.TestRunnerCallback;
 import de.integrity.runner.results.SuiteResult;
 import de.integrity.runner.results.call.CallResult;
 import de.integrity.runner.results.test.TestComparisonFailureResult;
 import de.integrity.runner.results.test.TestComparisonResult;
 import de.integrity.runner.results.test.TestComparisonSuccessResult;
 import de.integrity.runner.results.test.TestExceptionSubResult;
 import de.integrity.runner.results.test.TestExecutedSubResult;
 import de.integrity.runner.results.test.TestResult;
 import de.integrity.runner.results.test.TestSubResult;
 import de.integrity.utils.IntegrityDSLUtil;
 import de.integrity.utils.ParameterUtil;
 import de.integrity.utils.TestFormatter;
 
 @SuppressWarnings("unchecked")
 public class SetListCallback implements TestRunnerCallback {
 
 	private ClassLoader classLoader;
 
 	private IntegrityRemotingServer remotingServer;
 
 	private TestFormatter formatter;
 
 	private SetList setList;
 
 	private Stack<SetListEntry> entryStack = new Stack<SetListEntry>();
 
 	private Map<VariableEntity, Object> variableStorage;
 
 	private static final DecimalFormat EXECUTION_TIME_FORMAT = new DecimalFormat("0.000");
 
 	public SetListCallback(SetList aSetList, IntegrityRemotingServer aRemotingServer, ClassLoader aClassLoader) {
 		classLoader = aClassLoader;
 		formatter = new TestFormatter(classLoader);
 		setList = aSetList;
 		remotingServer = aRemotingServer;
 	}
 
 	@Override
 	public void onExecutionStart(TestModel aModel, Map<VariableEntity, Object> aVariableMap) {
 		SetListEntry tempNewEntry = setList.createEntry(SetListEntryTypes.EXECUTION);
 		entryStack.push(tempNewEntry);
 		variableStorage = aVariableMap;
 	}
 
 	@Override
 	public void onSuiteStart(Suite aSuite) {
 		SetListEntry tempNewEntry = setList.createEntry(SetListEntryTypes.SUITE);
 		tempNewEntry.setAttribute(SetListEntryAttributeKeys.NAME,
 				IntegrityDSLUtil.getQualifiedSuiteName(aSuite.getDefinition()));
 		setList.addReference(entryStack.peek(), SetListEntryAttributeKeys.STATEMENTS, tempNewEntry);
 		entryStack.push(tempNewEntry);
 	}
 
 	@Override
 	public void onSetupStart(SuiteDefinition aSetupSuite) {
 		SetListEntry tempNewEntry = setList.createEntry(SetListEntryTypes.SUITE);
 		tempNewEntry.setAttribute(SetListEntryAttributeKeys.NAME, IntegrityDSLUtil.getQualifiedSuiteName(aSetupSuite));
 		setList.addReference(entryStack.peek(), SetListEntryAttributeKeys.SETUP, tempNewEntry);
 		entryStack.push(tempNewEntry);
 	}
 
 	@Override
 	public void onSetupFinish(SuiteDefinition aSetupSuite, SuiteResult aResult) {
 		onAnyKindOfSuiteFinish(aSetupSuite, aResult);
 	}
 
 	@Override
 	public void onTestStart(Test aTest) {
 		SetListEntry tempNewEntry = setList.createEntry(SetListEntryTypes.TEST);
 
 		SetListEntry[] tempParamEntries = addMethodAndParamsToTestOrCall(aTest.getDefinition().getFixtureMethod(),
 				aTest.getParameters(), tempNewEntry);
 
 		setList.addReference(entryStack.peek(), SetListEntryAttributeKeys.STATEMENTS, tempNewEntry);
 		entryStack.push(tempNewEntry);
 		setList.setEntryInExecutionReference(tempNewEntry.getId());
 		sendUpdateToClients(tempNewEntry.getId(), tempNewEntry, tempParamEntries);
 	}
 
 	@Override
 	public void onTableTestStart(TableTest aTableTest) {
 		SetListEntry tempNewEntry = setList.createEntry(SetListEntryTypes.TABLETEST);
 
 		SetListEntry[] tempParamEntries = addMethodAndParamsToTestOrCall(aTableTest.getDefinition().getFixtureMethod(),
 				aTableTest.getParameters(), tempNewEntry);
 
 		setList.addReference(entryStack.peek(), SetListEntryAttributeKeys.STATEMENTS, tempNewEntry);
 		entryStack.push(tempNewEntry);
 		setList.setEntryInExecutionReference(tempNewEntry.getId());
 		sendUpdateToClients(tempNewEntry.getId(), tempNewEntry, tempParamEntries);
 	}
 
 	@Override
 	public void onTableTestRowStart(TableTest aTableTest, TableTestRow aRow) {
 		// nothing to do here
 	}
 
 	@Override
 	public void onTableTestRowFinish(TableTest aTableTest, TableTestRow aRow, TestSubResult aSubResult) {
 		// nothing to do here
 	}
 
 	@Override
 	public void onTableTestFinish(TableTest aTableTest, TestResult aResult) {
 		int tempCount = 0;
 		SetListEntry tempTestEntry = entryStack.pop();
 
 		if (aResult != null) {
 			if (aResult.getExecutionTime() != null) {
 				tempTestEntry.setAttribute(SetListEntryAttributeKeys.EXECUTION_TIME,
 						nanoTimeToString(aResult.getExecutionTime()));
 
 				tempTestEntry.setAttribute(SetListEntryAttributeKeys.SUCCESS_COUNT, aResult.getSubTestSuccessCount());
 				tempTestEntry.setAttribute(SetListEntryAttributeKeys.FAILURE_COUNT, aResult.getSubTestFailCount());
 				tempTestEntry.setAttribute(SetListEntryAttributeKeys.EXCEPTION_COUNT,
 						aResult.getSubTestExceptionCount());
 			}
 		}
 
 		List<SetListEntry> tempNewEntries = new LinkedList<SetListEntry>();
 		for (TestSubResult tempSubResult : aResult.getSubResults()) {
 			tempNewEntries.addAll(onAnyKindOfSubTestFinish(aTableTest.getDefinition().getFixtureMethod(),
 					tempTestEntry, tempSubResult, IntegrityDSLUtil.createParameterMap(aTableTest, aTableTest.getRows()
 							.get(tempCount), variableStorage, true)));
 			tempCount++;
 		}
 		tempNewEntries.add(tempTestEntry);
 
 		sendUpdateToClients(null, tempNewEntries.toArray(new SetListEntry[0]));
 	}
 
 	@Override
 	public void onTestFinish(Test aTest, TestResult aResult) {
 		SetListEntry tempTestEntry = entryStack.pop();
 		List<SetListEntry> tempNewEntries = onAnyKindOfSubTestFinish(aTest.getDefinition().getFixtureMethod(),
 				tempTestEntry, aResult.getSubResults().get(0),
 				IntegrityDSLUtil.createParameterMap(aTest, variableStorage, true));
 		tempNewEntries.add(tempTestEntry);
 
 		sendUpdateToClients(null, tempNewEntries.toArray(new SetListEntry[0]));
 	}
 
 	protected List<SetListEntry> onAnyKindOfSubTestFinish(MethodReference aMethod, SetListEntry aTestEntry,
 			TestSubResult aSubResult, Map<String, Object> aParameterMap) {
 		List<SetListEntry> tempNewEntries = new LinkedList<SetListEntry>();
 		SetListEntry tempNewEntry = setList.createEntry(SetListEntryTypes.RESULT);
 		tempNewEntries.add(tempNewEntry);
 
 		if (aSubResult.getExecutionTime() != null) {
 			tempNewEntry.setAttribute(SetListEntryAttributeKeys.EXECUTION_TIME,
 					nanoTimeToString(aSubResult.getExecutionTime()));
 		}
 
 		for (Entry<String, Object> tempEntry : aParameterMap.entrySet()) {
 			SetListEntry tempParameterEntry = setList.createEntry(SetListEntryTypes.PARAMETER);
 			tempNewEntries.add(tempParameterEntry);
 
 			tempParameterEntry.setAttribute(SetListEntryAttributeKeys.NAME, tempEntry.getKey());
 			tempParameterEntry.setAttribute(SetListEntryAttributeKeys.VALUE,
 					ParameterUtil.convertValueToString(tempEntry.getValue(), variableStorage, false));
 
 			setList.addReference(tempNewEntry, SetListEntryAttributeKeys.PARAMETERS, tempParameterEntry);
 		}
 
 		try {
 			tempNewEntry.setAttribute(SetListEntryAttributeKeys.DESCRIPTION,
 					formatter.fixtureMethodToHumanReadableString(aMethod, aParameterMap, true));
 		} catch (ClassNotFoundException e) {
 			tempNewEntry.setAttribute(SetListEntryAttributeKeys.DESCRIPTION, e.getMessage());
 			e.printStackTrace();
 		}
 
 		if (aSubResult instanceof TestExceptionSubResult) {
 			tempNewEntry.setAttribute(SetListEntryAttributeKeys.RESULT_SUCCESS_FLAG, Boolean.FALSE);
 			tempNewEntry.setAttribute(SetListEntryAttributeKeys.EXCEPTION,
 					stackTraceToString(((TestExceptionSubResult) aSubResult).getException()));
 		} else if (aSubResult instanceof TestExecutedSubResult) {
 			if (!aSubResult.isUndetermined()) {
 				if (aSubResult.wereAllComparisonsSuccessful()) {
 					tempNewEntry.setAttribute(SetListEntryAttributeKeys.RESULT_SUCCESS_FLAG, Boolean.TRUE);
 				} else {
 					tempNewEntry.setAttribute(SetListEntryAttributeKeys.RESULT_SUCCESS_FLAG, Boolean.FALSE);
 				}
 			}
 		}
 
 		for (Entry<String, TestComparisonResult> tempEntry : aSubResult.getComparisonResults().entrySet()) {
 			SetListEntry tempComparisonEntry = setList.createEntry(SetListEntryTypes.COMPARISON);
 			tempNewEntries.add(tempComparisonEntry);
 
			ValueOrEnumValue tempExpectedValue = tempEntry.getValue().getExpectedValue();

			tempComparisonEntry.setAttribute(
					SetListEntryAttributeKeys.EXPECTED_RESULT,
					tempExpectedValue == null ? Boolean.TRUE.toString() : ParameterUtil.convertValueToString(tempEntry
							.getValue().getExpectedValue(), variableStorage, false));
 			if (tempEntry.getValue().getResult() != null) {
 				tempComparisonEntry.setAttribute(SetListEntryAttributeKeys.VALUE,
 						ParameterUtil.convertValueToString(tempEntry.getValue().getResult(), variableStorage, false));
 			}
 
 			if (tempEntry.getValue() instanceof TestComparisonSuccessResult) {
 				tempComparisonEntry.setAttribute(SetListEntryAttributeKeys.RESULT_SUCCESS_FLAG, Boolean.TRUE);
 			} else if (tempEntry.getValue() instanceof TestComparisonFailureResult) {
 				tempComparisonEntry.setAttribute(SetListEntryAttributeKeys.RESULT_SUCCESS_FLAG, Boolean.FALSE);
 			}
 
 			setList.addReference(tempNewEntry, SetListEntryAttributeKeys.COMPARISONS, tempComparisonEntry);
 		}
 
 		setList.addReference(aTestEntry, SetListEntryAttributeKeys.RESULT, tempNewEntry);
 
 		return tempNewEntries;
 	}
 
 	@Override
 	public void onCallStart(Call aCall) {
 		SetListEntry tempNewEntry = setList.createEntry(SetListEntryTypes.CALL);
 
 		SetListEntry[] tempParamEntries = addMethodAndParamsToTestOrCall(aCall.getDefinition().getFixtureMethod(),
 				aCall.getParameters(), tempNewEntry);
 
 		setList.addReference(entryStack.peek(), SetListEntryAttributeKeys.STATEMENTS, tempNewEntry);
 		entryStack.push(tempNewEntry);
 		setList.setEntryInExecutionReference(tempNewEntry.getId());
 		sendUpdateToClients(tempNewEntry.getId(), tempNewEntry, tempParamEntries);
 	}
 
 	@Override
 	public void onCallFinish(Call aCall, CallResult aResult) {
 		SetListEntry tempNewEntry = setList.createEntry(SetListEntryTypes.RESULT);
 		if (aResult.getExecutionTime() != null) {
 			tempNewEntry.setAttribute(SetListEntryAttributeKeys.EXECUTION_TIME,
 					nanoTimeToString(aResult.getExecutionTime()));
 		}
 
 		if (aResult instanceof de.integrity.runner.results.call.SuccessResult) {
 			tempNewEntry.setAttribute(SetListEntryAttributeKeys.RESULT_SUCCESS_FLAG, Boolean.TRUE);
 			de.integrity.runner.results.call.SuccessResult result = (de.integrity.runner.results.call.SuccessResult) aResult;
 			if (aResult.getResult() != null) {
 				tempNewEntry.setAttribute(SetListEntryAttributeKeys.VALUE,
 						ParameterUtil.convertValueToString(aResult, variableStorage, false));
 			}
 			if (result.getTargetVariable() != null) {
 				tempNewEntry
 						.setAttribute(SetListEntryAttributeKeys.VARIABLE_NAME, result.getTargetVariable().getName());
 			}
 		} else if (aResult instanceof de.integrity.runner.results.call.ExceptionResult) {
 			tempNewEntry.setAttribute(SetListEntryAttributeKeys.RESULT_SUCCESS_FLAG, Boolean.FALSE);
 			tempNewEntry.setAttribute(SetListEntryAttributeKeys.EXCEPTION,
 					stackTraceToString(((de.integrity.runner.results.call.ExceptionResult) aResult).getException()));
 		}
 		setList.addReference(entryStack.pop(), SetListEntryAttributeKeys.RESULT, tempNewEntry);
 		sendUpdateToClients(null, tempNewEntry);
 	}
 
 	@Override
 	public void onTearDownStart(SuiteDefinition aTearDownSuite) {
 		SetListEntry tempNewEntry = setList.createEntry(SetListEntryTypes.SUITE);
 		tempNewEntry.setAttribute(SetListEntryAttributeKeys.NAME,
 				IntegrityDSLUtil.getQualifiedSuiteName(aTearDownSuite));
 		setList.addReference(entryStack.peek(), SetListEntryAttributeKeys.TEARDOWN, tempNewEntry);
 		entryStack.push(tempNewEntry);
 		sendUpdateToClients(null, tempNewEntry);
 	}
 
 	@Override
 	public void onTearDownFinish(SuiteDefinition aTearDownSuite, SuiteResult aResult) {
 		onAnyKindOfSuiteFinish(aTearDownSuite, aResult);
 	}
 
 	@Override
 	public void onSuiteFinish(Suite aSuite, SuiteResult aResult) {
 		onAnyKindOfSuiteFinish(aSuite.getDefinition(), aResult);
 	}
 
 	private void onAnyKindOfSuiteFinish(SuiteDefinition aSuite, SuiteResult aResult) {
 		SetListEntry tempNewEntry = setList.createEntry(SetListEntryTypes.RESULT);
 
 		if (aResult != null) {
 			if (aResult.getExecutionTime() != null) {
 				tempNewEntry.setAttribute(SetListEntryAttributeKeys.EXECUTION_TIME,
 						nanoTimeToString(aResult.getExecutionTime()));
 			}
 			if (aResult.getResults() != null) {
 				tempNewEntry.setAttribute(SetListEntryAttributeKeys.SUCCESS_COUNT, aResult.getTestSuccessCount());
 				tempNewEntry.setAttribute(SetListEntryAttributeKeys.FAILURE_COUNT, aResult.getTestFailCount());
 				tempNewEntry.setAttribute(SetListEntryAttributeKeys.EXCEPTION_COUNT, aResult.getTestExceptionCount());
 			}
 		}
 
 		setList.addReference(entryStack.pop(), SetListEntryAttributeKeys.RESULT, tempNewEntry);
 		sendUpdateToClients(null, tempNewEntry);
 	}
 
 	@Override
 	public void onExecutionFinish(TestModel aModel, SuiteResult aResult) {
 		setList.setEntryInExecutionReference(null);
 	}
 
 	@Override
 	public void onVariableDefinition(VariableEntity aDefinition, SuiteDefinition aSuite, Object anInitialValue) {
 		SetListEntry tempNewEntry = setList.createEntry(SetListEntryTypes.VARIABLE);
 		tempNewEntry.setAttribute(SetListEntryAttributeKeys.NAME,
 				IntegrityDSLUtil.getQualifiedGlobalVariableName(aDefinition));
 		if (anInitialValue != null) {
 			tempNewEntry.setAttribute(SetListEntryAttributeKeys.VALUE,
 					ParameterUtil.convertValueToString(anInitialValue, variableStorage, false));
 		}
 
 		setList.addReference(entryStack.peek(), SetListEntryAttributeKeys.VARIABLE_DEFINITIONS, tempNewEntry);
 		sendUpdateToClients(null, tempNewEntry);
 	}
 
 	protected SetListEntry[] addMethodAndParamsToTestOrCall(MethodReference aMethod, EList<Parameter> aParamList,
 			SetListEntry anEntry) {
 		try {
 			anEntry.setAttribute(
 					SetListEntryAttributeKeys.DESCRIPTION,
 					formatter.fixtureMethodToHumanReadableString(aMethod,
 							IntegrityDSLUtil.createParameterMap(aParamList, variableStorage, true), true));
 		} catch (ClassNotFoundException e) {
 			anEntry.setAttribute(SetListEntryAttributeKeys.DESCRIPTION, e.getMessage());
 			e.printStackTrace();
 		}
 		anEntry.setAttribute(SetListEntryAttributeKeys.FIXTURE,
 				IntegrityDSLUtil.getQualifiedNameOfFixtureMethod(aMethod));
 
 		SetListEntry[] tempResultArray = new SetListEntry[aParamList.size()];
 		int tempParamCounter = 0;
 		for (Parameter parameter : aParamList) {
 			SetListEntry tempParamEntry = setList.createEntry(SetListEntryTypes.PARAMETER);
 			tempParamEntry.setAttribute(SetListEntryAttributeKeys.NAME,
 					IntegrityDSLUtil.getParamNameStringFromParameterName(parameter.getName()));
 			tempParamEntry.setAttribute(SetListEntryAttributeKeys.VALUE,
 					ParameterUtil.convertValueToString(parameter.getValue(), variableStorage, false));
 			if (parameter.getValue() instanceof Variable) {
 				tempParamEntry.setAttribute(SetListEntryAttributeKeys.VARIABLE_NAME, ((Variable) parameter.getValue())
 						.getName().getName());
 			}
 
 			setList.addReference(anEntry, SetListEntryAttributeKeys.PARAMETERS, tempParamEntry);
 			tempResultArray[tempParamCounter] = tempParamEntry;
 			tempParamCounter++;
 		}
 
 		return tempResultArray;
 	}
 
 	protected void sendUpdateToClients(Integer anEntryInExecution, SetListEntry... someUpdatedEntries) {
 		if (remotingServer != null) {
 			remotingServer.updateSetList(anEntryInExecution, someUpdatedEntries);
 		}
 	}
 
 	protected void sendUpdateToClients(Integer anEntryInExecution, SetListEntry aSingleEntry,
 			SetListEntry[] someMoreEntries) {
 		SetListEntry[] tempCombined = new SetListEntry[someMoreEntries.length + 1];
 		tempCombined[0] = aSingleEntry;
 		System.arraycopy(someMoreEntries, 0, tempCombined, 1, someMoreEntries.length);
 		sendUpdateToClients(anEntryInExecution, tempCombined);
 	}
 
 	protected static String stackTraceToString(Throwable anException) {
 		String tempResult = null;
 		StringWriter tempStringWriter = null;
 		PrintWriter tempPrintWriter = null;
 		try {
 			tempStringWriter = new StringWriter();
 			tempPrintWriter = new PrintWriter(tempStringWriter);
 			anException.printStackTrace(tempPrintWriter);
 			tempResult = tempStringWriter.toString();
 		} finally {
 			try {
 				if (tempPrintWriter != null)
 					tempPrintWriter.close();
 				if (tempStringWriter != null)
 					tempStringWriter.close();
 			} catch (IOException exc) {
 			}
 		}
 		return tempResult;
 	}
 
 	protected static String nanoTimeToString(long aNanosecondValue) {
 		return EXECUTION_TIME_FORMAT.format(((double) aNanosecondValue) / 1000000.0);
 	}
 
 }
