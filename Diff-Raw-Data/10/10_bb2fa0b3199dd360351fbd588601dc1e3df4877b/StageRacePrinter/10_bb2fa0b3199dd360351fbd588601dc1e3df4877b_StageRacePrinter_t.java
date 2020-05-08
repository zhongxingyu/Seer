 package enduro.racer.printer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import enduro.racer.Racer;
 import enduro.racer.Time;
 import enduro.racer.configuration.ConfigParser;
 
 /**
  * prints a stage race with each stage included and so forth.
  */
 public class StageRacePrinter implements RacerPrinter {
 	private String extraInformation = "";
 	private int stages = ConfigParser.getInstance().getIntConf("stages");
 	private int extraRunnerInformation;
 
	/**
	 * returns a string that contains stage race info. 
	 */
 	public String print(Racer r, HashMap<String, String> extraInformation) {
 		StringBuilder out = new StringBuilder();
 		StringBuilder errorTrail = new StringBuilder();
 		printRunnerInformation(r, out, errorTrail);
 		printTotalTime(r, out, errorTrail);
 		printNbrOfStages(r, out, errorTrail);
 		printStages(r, out, errorTrail);
 		printStartAndFinish(r, out, errorTrail);
 		out.append(errorTrail.toString());
 		return out.toString();
 	}
 
 	private void printNbrOfStages(Racer r, StringBuilder out,
 			StringBuilder errorTrail) {
 		ArrayList<Time> times = getStages(r);
 		out.append(times.size());
 		out.append("; ");
 	}
 
 	private void printRunnerInformation(Racer r, StringBuilder out,
 			StringBuilder errorTrail) {
 		int i = 0;
 		for (; i < r.racerInformation.size(); i++) {
 			out.append(r.racerInformation.get(i));
 			out.append("; ");
 		}
 		for (; i < extraRunnerInformation; i++) {
 			out.append("; ");
 		}
 
 	}
 
 	private void printStartAndFinish(Racer r, StringBuilder out,
 			StringBuilder errorTrail) {
 		Object[] fTimes = r.finishTimes.toArray();
 		Object[] sTimes = r.startTimes.toArray();
 		int maxLength = Math.max(fTimes.length, sTimes.length);
 		for (int i = 0; i < maxLength; i++) {
 			out.append(((Time) sTimes[i]).toString());
 			out.append("; ");
 			out.append(((Time) fTimes[i]).toString());
 			if (i != maxLength - 1) {
 				out.append("; ");
 			}
 		}
 	}
 
 	public String printTopInformation() {
 		StringBuilder out = new StringBuilder();
 		out.append(extraInformation);
 		out.append("Totaltid; #Etapper");
 
 		for (int i = 1; i <= stages; i++) {
 			out.append("; Etapp");
 			out.append(i);
 		}
 		for (int i = 1; i <= stages; i++) {
 			out.append("; Start");
 			out.append(i);
 			out.append("; Mål");
 			out.append(i);
 		}
 		return out.toString();
 	}
 
 	public void setHeaderInformation(String[] extraInformation) {
 		extraRunnerInformation = extraInformation.length;
 		for (int i = 0; i < extraRunnerInformation; i++) {
 			this.extraInformation += extraInformation[i] + "; ";
 		}
 	}
 
 	private void printStages(Racer r, StringBuilder out,
 			StringBuilder errorTrail) {
 		if (r.startTimes.size() > 0) {
 			ArrayList<Time> stages = getStages(r);
 			for (int i = 0; i < stages.size(); i++) {
 				out.append(stages.get(i).toString());
 				out.append("; ");
 			}
 		}
 	}
 
 	private void printTotalTime(Racer r, StringBuilder out,
 			StringBuilder errorTrail) {
 		ArrayList<Time> times = getStages(r);
 		Time totalTime = new Time(0, 0, 0);
 		for (int i = 0; i < times.size(); i++) {
 			totalTime.increment(times.get(i));
 		}
 		out.append(totalTime.toString());
 		out.append("; ");
 	}
 
 	private ArrayList<Time> getStages(Racer r) {
 		Iterator<Time> startItr = r.startTimes.iterator();
 		Iterator<Time> finishItr = r.finishTimes.iterator();
 		ArrayList<Time> stages = new ArrayList<Time>();
 		while (startItr.hasNext() && finishItr.hasNext()) {
 			stages.add(startItr.next().getTotalTime(finishItr.next()));
 		}
 		return stages;
 	}
 }
