 package ch.hsr.objectCaching.reporting;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 
 import ch.hsr.objectCaching.action.Action;
 import ch.hsr.objectCaching.action.IncrementAction;
 import ch.hsr.objectCaching.action.result.Result.ActionResult;
 import ch.hsr.objectCaching.action.result.Result.BasicAction;
 import ch.hsr.objectCaching.action.result.TimeRecord;
 import ch.hsr.objectCaching.scenario.Scenario;
 
 public class ReportGenerator {
 
 	private final int NANOSEC_TO_MILISEC_FACTOR = 1000000;
 	private final String READ = "READ";
 	private final String WRITE = "WRITE";
 	private final String NEWLINE;
 	private final String PARAMETER_SEPARATOR;
 	private double totalScenarioExecutionTime = 0;
 	private int totalScenarioConflicts = 0;
 	private Scenario scenario;
 	private String clientIp;
 	private String summary;
 	private int actionNumber;
 	private BufferedWriter out;
 
 	public ReportGenerator(Scenario scenario, String clientIp) {
 		this.scenario = scenario;
 		this.clientIp = clientIp;
 		NEWLINE = System.getProperty("line.separator");
 		PARAMETER_SEPARATOR = ";";
 
 		try {
 			out = new BufferedWriter(new FileWriter("Client_" + clientIp + ".txt"));
 		} catch (IOException e) {
 			System.out.println("IOException in ReportGenerator");
 		}
 		generateReport();
 	}
 
 	public String getSummary() {
 		return summary;
 	}
 
 	private void generateReport() {
 		try {
 			writeHeader();
 			writeListOfActions();
 			writeDetailedSummary();
 			finalizeReport();
 		} catch (IOException e) {
 			System.out.println("IO Error in Reportgenerator for scenario " + scenario.getId());
 		}
 	}
 
 	private void writeDetailedSummary() throws IOException {
 		int numberOfReads = 0;
 		int numberOfWrite = 0;
 		int numberOfIncrement = 0;
 		int numberOfUnsuccessfulAction = 0;
 		double readTime = 0;
 		double writeTime = 0;
 		double totalTime = 0;
 
 		int numberOfActions = scenario.getActionList().size();
 
 		for (Action action : scenario.getActionList()) {
 			ActionResult result = ActionResult.FAILED;
 			for (TimeRecord records : action.getResult().getAllIntermediateResult()) {
 				if (records.getActionTyp() == BasicAction.READ) {
 					numberOfReads++;
 					readTime += getDeltaInMilisec(records);
 					result = records.getActionResult();
 				}
 				if (records.getActionTyp() == BasicAction.WRITE) {
 					numberOfWrite++;
 					writeTime += getDeltaInMilisec(records);
 					result = records.getActionResult();
 				}
 			}
 			if (result == ActionResult.FAILED)
 				numberOfUnsuccessfulAction++;
 
 		}
 
 		for (Action action : scenario.getActionList()) {
 			TimeRecord record = action.getResult().getTotalTimeRecord();
 			totalTime += getDeltaInMilisec(record);
 			numberOfIncrement++;
 		}
 
 		out.write(NEWLINE);
 		out.write("------------------------------------------------" + NEWLINE);
 		out.write(100 - (numberOfUnsuccessfulAction / numberOfActions) + "% of all Action executed are successful" + NEWLINE);
 		out.write("Total actions executed: " + numberOfActions + ", number of unsuccessful action " + numberOfUnsuccessfulAction + NEWLINE);
 		out.write("Total getBalance calls: " + numberOfReads + ", avg. execution time " + readTime / (double) numberOfReads + NEWLINE);
 		out.write("Total setBalance calls: " + numberOfWrite + ", avg. execution time " + writeTime / (double) numberOfWrite + NEWLINE);
 		out.write("Total increment time: " + totalTime + ", avg. execution time " + totalTime / (double) numberOfIncrement + NEWLINE);
 		out.write(NEWLINE);
 	}
 
 	private void writeListOfActions() throws IOException {
 		actionNumber = 0;
 		for (Action action : scenario.getActionList()) {
 
 			int minimalNumberOfTimeRecords = action.getMinimalNumberOfTimeRecords();
 			if (action.getResult().getNumberOfTry() > minimalNumberOfTimeRecords) {
				totalScenarioConflicts += (action.getResult().getNumberOfTry() - minimalNumberOfTimeRecords)/minimalNumberOfTimeRecords;
 			}
 
 			int numberOfConflictsPerAction = 0;
 			for (TimeRecord interimTime : action.getResult().getAllIntermediateResult()) {
 				double executionTime = getDeltaInMilisec(interimTime);
 				switch (action.getActionTyp()) {
 				case READ_ACTION:
 					writeActionResult(numberOfConflictsPerAction, executionTime, READ);
 					break;
 				case WRITE_ACTION:
 					writeActionResult(numberOfConflictsPerAction, executionTime, WRITE);
 					break;
 				case INCREMENT_ACTION:
 					writeActionResult(numberOfConflictsPerAction, executionTime, buildIncrementActionDescription(action, interimTime));
 				}
 				numberOfConflictsPerAction++;
 				totalScenarioExecutionTime += executionTime;
 			}
 			actionNumber++;
 		}
 	}
 
 	private void finalizeReport() throws IOException {
 		summary = ("Total Conflict: " + totalScenarioConflicts + " / Gesamt Dauer: " + totalScenarioExecutionTime + " ms" + " / durch. Dauer pro Operation: " + totalScenarioExecutionTime
 				/ (scenario.getActionList().size() + totalScenarioConflicts));
 		out.write(summary);
 		out.flush();
 		out.close();
 	}
 
 	private void writeHeader() throws IOException {
 
 		Date now = new Date();
 
 		out.write("************************************************************" + NEWLINE);
 		out.write("Date & Time: " + now.toString() + NEWLINE);
 		out.write("Result for Client: " + clientIp + " with ScenarioID: " + scenario.getId() + NEWLINE);
 		out.write("OS: " + System.getProperty("os.name") + " / " + System.getProperty("os.version") + NEWLINE);
 		out.write("************************************************************" + NEWLINE);
 		out.write("ActionNr;#ofTries;Time[ms];ACTION" + NEWLINE);
 	}
 
 	private void writeActionResult(int conflict, double time, String specificDescription) throws IOException {
 		out.write(actionNumber + PARAMETER_SEPARATOR + conflict + PARAMETER_SEPARATOR + time + PARAMETER_SEPARATOR + specificDescription + NEWLINE);
 	}
 
 	private String buildIncrementActionDescription(Action action, TimeRecord time) {
 		IncrementAction a = (IncrementAction) action;
 		if (a.getDelay() < 1) {
 			return "INCREMENT(" + time.getActionTyp().toString() + ") WITHOUT DELAY";
 		} else {
 			return "INCREMENT(" + time.getActionTyp().toString() + ") WITH DELAY OF: " + a.getDelay() + "ms AFTER READING THE BALANCE";
 		}
 	}
 
 	private double getDeltaInMilisec(TimeRecord m) {
 		long nanoSek = m.getStopTime() - m.getStartTime();
 		return (double) nanoSek / NANOSEC_TO_MILISEC_FACTOR;
 	}
 
 }
