 package model;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.*;
 import java.util.StringTokenizer;
 
 import javax.swing.event.EventListenerList;
 
 import model.TaskTriplet.State;
 
 import view.Utils;
 import controller.TripletListener;
 
 public class TaskSpec {
 	private List<TaskTriplet> taskTripletList;
 	private EventListenerList listOfTripletListeners = new EventListenerList();
 	private Logging logg;
 	private String taskTripletListName = "TaskTripletList";
 
 	private String removeSpaces(String str) {
 		StringTokenizer tokens = new StringTokenizer(str, " ", false);
 		String newStr = "";
 		while (tokens.hasMoreElements()) {
 			newStr += tokens.nextElement();
 		}
 		return newStr;
 	}
 
 	public TaskSpec() {
 		taskTripletList = new ArrayList<TaskTriplet>();
 		logg = Logging.getInstance();
 	}
 
 	public String getTaskSpecString() {
 		String s = new String();
 		s = s.concat("<");
 		Iterator<TaskTriplet> iterator = taskTripletList.iterator();
 		while (iterator.hasNext()) {
 			s = s.concat(iterator.next().getTaskTripletString());
 			if (iterator.hasNext()) {
 				s = s.concat(",");
 			}
 		}
 		s = s.concat(">");
 		return s;
 	}
 
 	public boolean parseTaskSpecString(String tSpecStr) {
 		tSpecStr = removeSpaces(tSpecStr);
 		System.out.println("After removing spaces " + tSpecStr);
 		try {
 			Pattern pat = Pattern.compile(TaskTriplet.getValidTripletPattern());
 			Matcher m = pat.matcher(tSpecStr);
 			do {
 				TaskTriplet nextTaskTriplet = new TaskTriplet();
 				if (m.find()) {
 					nextTaskTriplet.setPlace(m.group(1));
 					nextTaskTriplet.setOrientation(m.group(2));
 					nextTaskTriplet.setPause(m.group(3));
 					addTriplet(nextTaskTriplet);
 				}
 			} while (!m.hitEnd());
 		} catch (Exception e) {
 			System.out.println("Caught exception in parseTaskSpec. Error: "
 					+ e.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	public void addTriplet(TaskTriplet triplet) {
 		taskTripletList.add(triplet);
 		logg.LoggingFileAndCompetitionFile(taskTripletListName, triplet.getTaskTripletString()
 				+ " no. " + taskTripletList.indexOf(triplet) + " added", false);
 		notifyTaskSpecChanged(new TripletEvent(triplet, taskTripletList.size(),
 				taskTripletList));
 	}
 
 	public TaskTriplet deleteTriplet(int tripletIndex) {
 		TaskTriplet triplet = taskTripletList.remove(tripletIndex);
 		logg.LoggingFileAndCompetitionFile(taskTripletListName, triplet.getTaskTripletString()
 				+ " no. " + tripletIndex + " deleted", false);
 		notifyTaskSpecChanged(new TripletEvent(triplet, tripletIndex,
 				taskTripletList));
 		return triplet;
 	}
 
 	public TaskTriplet moveUpTriplet(int tripletIndex) {
 		if (tripletIndex == 0) {
 			return null;
 		} else {
 			TaskTriplet triplet = taskTripletList.remove(tripletIndex);
 			taskTripletList.add(tripletIndex - 1, triplet);
 			logg.LoggingFileAndCompetitionFile(
 					taskTripletListName,
 					triplet.getTaskTripletString() + " no. "
 							+ taskTripletList.indexOf(triplet) + " moved up", false);
 			notifyTaskSpecChanged(new TripletEvent(triplet, tripletIndex,
 					taskTripletList));
 			return triplet;
 		}
 	}
 
 	public TaskTriplet moveDownTriplet(int tripletIndex) {
 		if (taskTripletList.size() == tripletIndex + 1) {
 			return null;
 		} else {
 			TaskTriplet triplet = taskTripletList.remove(tripletIndex);
 			taskTripletList.add(tripletIndex + 1, triplet);
 			logg.LoggingFileAndCompetitionFile(
 					taskTripletListName,
 					triplet.getTaskTripletString() + " no. "
 							+ taskTripletList.indexOf(triplet) + " moved down", false);
 			notifyTaskSpecChanged(new TripletEvent(triplet, tripletIndex,
 					taskTripletList));
 			return triplet;
 		}
 	}
 
 	public TaskTriplet editTriplet(int tripletIndex, TaskTriplet updateTriplet) {
 
 		try {
 			TaskTriplet tt = taskTripletList.set(tripletIndex, updateTriplet);
 			notifyTaskSpecChanged(new TripletEvent(updateTriplet, tripletIndex,
 					taskTripletList));
 			logg.LoggingFileAndCompetitionFile(taskTripletListName, tt.getTaskTripletString()
 					+ " no. " + taskTripletList.indexOf(updateTriplet)
 					+ " updated to " + updateTriplet.getTaskTripletString(), false);
 			return tt;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public List<TaskTriplet> getTaskTripletList() {
 		return taskTripletList;
 	}
 
 	public TaskTriplet getTaskTripletAtIndex(int index) {
 		return taskTripletList.get(index);
 	}
 
 	public void addTripletListener(TripletListener tL) {
 		listOfTripletListeners.add(TripletListener.class, tL);
 	}
 
 	public void removeTripletListener(TripletListener tL) {
 		listOfTripletListeners.remove(TripletListener.class, tL);
 	}
 
 	private void notifyTaskSpecChanged(TripletEvent evt) {
 		Object[] listeners = listOfTripletListeners.getListenerList();
 		// Each listener occupies two elements - the first is the listener class
 		// and the second is the listener instance
 		for (int i = 0; i < listeners.length; i += 2) {
 			if (listeners[i] == TripletListener.class) {
 				((TripletListener) listeners[i + 1]).taskSpecChanged(evt);
 			}
 		}
 	}
 
 	public boolean saveTaskSpec(File file) {
 
 		file = Utils.correctFile(file);
 		try {
 			FileWriter fstream = new FileWriter(file);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(getTaskSpecString());
 			out.close();
 			logg.LoggingFileAndCompetitionFile(taskTripletListName,
 					"saved actual task specification in >" + file.getName()
 							+ "<", false);
 		} catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			logg.LoggingFileAndCompetitionFile(taskTripletListName, "saving failed! >", false);
 			return false;
 		}
 		return true;
 	}
 
 	public boolean openTaskSpec(File file) {
 		try {
 			FileInputStream fstream = new FileInputStream(file);
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 			while ((strLine = br.readLine()) != null) {
 				taskTripletList = new ArrayList<TaskTriplet>();
 				if (!parseTaskSpecString(strLine))
 					return false;
 				System.out.println("Found and parsed task spec string: "
 						+ getTaskSpecString());
 				logg.LoggingFileAndCompetitionFile(taskTripletListName, "Found and parsed task spec string", false);
 				notifyTaskSpecChanged(new TripletEvent(taskTripletList.get(0),
 						taskTripletList.size(), taskTripletList));
 			}
 			in.close();
 		} catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	public TaskTriplet setTripletState(int tripletIndex, int column) {
 		TaskTriplet tT = taskTripletList.get(tripletIndex);
 		State newState;
 		if (column == 1)
 			newState = State.PASSED;
 		else
 			newState = State.FAILED;
 		if (tT.getState() == newState)
 			tT.setState(State.INIT);
 		else
 			tT.setState(newState);
 		logg.LoggingFileAndCompetitionFile(
 				taskTripletListName,
 				tT.getTaskTripletString() + " no. "
 						+ taskTripletList.indexOf(tT) + " new state: "
						+ tT.getState(), false);
 		notifyTaskSpecChanged(new TripletEvent(tT, taskTripletList.indexOf(tT),
 				taskTripletList));
 		return tT;
 	}
 
 	public void resetStates() {
 		for (TaskTriplet tT : taskTripletList) {
 			tT.setState(State.INIT);
 			logg.LoggingFileAndCompetitionFile(taskTripletListName, tT.getTaskTripletString()
 					+ " no. " + taskTripletList.indexOf(tT)
 					+ " new state: INIT", false);
 		}
 	}
 }
