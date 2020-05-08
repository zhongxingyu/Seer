 package ch.hsr.objectCaching.reporting;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import ch.hsr.objectCaching.action.Action;
 import ch.hsr.objectCaching.action.IncrementAction;
 import ch.hsr.objectCaching.action.result.TimeMeasure;
 import ch.hsr.objectCaching.scenario.Scenario;
 
 public class ReportGenerator {
 
 	private final int NANOSEC_TO_MILISEC_FACTOR = 1000000;
 	private final String READ = "READ";
 	private final String WRITE = "WRITE";
 	private final String NEWLINE;
 	private final String PARAMETER_SEPARATOR;
 	private double totalExecutionTime = 0;
 	private int totalConflicts = 0;
 	private Scenario scenario;
 	private String clientIp;
 	private String summary;
 	private int actionNumber;
 	private BufferedWriter out;
 
 	public ReportGenerator(Scenario scenario, String clientIp) {
 		this.scenario = scenario;
 		this.clientIp = clientIp;
 		NEWLINE = System.getProperty("line.separator");
 		PARAMETER_SEPARATOR = "\t";
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
 		actionNumber = 0;
 		try {
 			writeHeader();
 			for (Action action : scenario.getActionList()) {
 
				if (action.getResult().getNumberOfTry() > 1) {
					totalConflicts += action.getResult().getNumberOfTry() - 1;
 				}
 
 				int numberOfConflictsPerAction = 0;
 				for (TimeMeasure interimTime : action.getResult().getAllIntermediateResult()) {
 					double executionTime = getDeltaInMilisec(interimTime);
 					switch (action.getActionTyp()) {
 					case READ_ACTION:
 						writeActionResult(numberOfConflictsPerAction, executionTime, READ);
 						break;
 					case WRITE_ACTION:
 						writeActionResult(numberOfConflictsPerAction, executionTime, WRITE);
 						break;
 					case INCREMENT_ACTION:
						writeActionResult(numberOfConflictsPerAction, executionTime, buildIncrementString(action, interimTime));
 					}
 					numberOfConflictsPerAction++;
 					totalExecutionTime += executionTime;
 				}
 				actionNumber++;
 			}
 			finalizeReport();
 		} catch (IOException e) {
 			System.out.println("IO Error in Reportgenerator for scenario " + scenario.getId());
 		}
 	}
 
 	private void finalizeReport() throws IOException {
 		summary = ("Total Conflict: " + totalConflicts + " / Gesamt Dauer: " + totalExecutionTime + " ms" + " / durch. Dauer pro Operation: " + totalExecutionTime / (scenario.getActionList().size() + totalConflicts));
 		out.write(summary);
 		out.flush();
 		out.close();
 	}
 
 	private void writeHeader() throws IOException {
 		out.write("************************************************************" + NEWLINE);
 		out.write("Result for Client: " + clientIp + " with ScenarioID: " + scenario.getId() + NEWLINE);
 		out.write("OS: " + System.getProperty("os.name") +" / " +System.getProperty("os.version") + NEWLINE);
 		out.write("************************************************************" + NEWLINE);
 		out.write("ActionNr;#ofTries;Time[ms];ACTION" + NEWLINE);
 	}
 
 	private void writeActionResult(int conflict, double time, String specificDescription) throws IOException {
 		out.write(actionNumber + PARAMETER_SEPARATOR + conflict + PARAMETER_SEPARATOR + time + PARAMETER_SEPARATOR + specificDescription + NEWLINE);
 	}
 
	private String buildIncrementString(Action action, TimeMeasure time) {
 		IncrementAction a = (IncrementAction) action;
 		if (a.getDelay() < 1) {
 			return "INCREMENT("+ time.getActionTyp().toString() +") WITHOUT DELAY";
 		} else {
			return "INCREMENT WITH DELAY: " + a.getDelay();
 		}
 	}
 
 	private double getDeltaInMilisec(TimeMeasure m) {
 		long nanoSek = m.getStopTime() - m.getStartTime();
 		return (double) nanoSek / NANOSEC_TO_MILISEC_FACTOR;
 	}
 
 }
