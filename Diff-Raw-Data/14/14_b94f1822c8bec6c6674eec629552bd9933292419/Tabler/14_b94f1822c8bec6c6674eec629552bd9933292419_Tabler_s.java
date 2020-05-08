 package tabler;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.TreeSet;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /*
  * @author Calvin Liang <calvinlsliang@gmail.com>
  */
 public class Tabler {
 	
 	int NUM_WEEKDAYS = 5; // Monday, Tuesday, ..., Friday = 5 weekdays
 	int NUM_HOURSLOTS = 10; //10, 10:30, ..., 3 = 10 slots
 	Schedule schedule = new Schedule(NUM_WEEKDAYS, NUM_HOURSLOTS);
 	HashSet<String> studentNames = new HashSet<String>();
 
 	enum Type {
 		NORMAL, BLACKLIST, WHITELIST
 	}
 	
 	private int getNumberOfStudents() {
 		return studentNames.size();
 	}
 	
 	
 	private void pickStudents() {
 		if (!schedule.checkSchedule()) {
 			System.err.println("There exists inadequate tabling spots!");
 			return;
 		}
 		Schedule finalSchedule = new Schedule(NUM_WEEKDAYS, NUM_HOURSLOTS);
 		HashSet<String> chosenStudents = new HashSet<String>();
 		Hashtable<String, Cell[]> chosenStudentsHash = new Hashtable<String, Cell[]>();
 		HashSet<Cell> problemCells = schedule.getProblemCells(6);
 		
 		Vector<Cell> toAdd = new Vector<Cell>();
 		Vector<Cell> toRemove = new Vector<Cell>();
 		
 		/*
 		 * Work on problem cells first
 		 * problemCells will dynamically change size as I remove them
 		 */
 		
 		/*
 		 * 
 		 * IMPORTANT
 		 * Tabling is in one hour blocks, not whenever there's space...
 		 */
 		System.out.println(problemCells);
 		while (problemCells.size() > 0) {
			System.out.println(problemCells.size());
 			finalSchedule.print();
 //			try {
 //				Thread.sleep(10000);
 //			} catch (InterruptedException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 			Iterator<Cell> iter = problemCells.iterator();
 			while (iter.hasNext()) {
 				Cell cell = iter.next();
 				TreeSet<String> listOfStudents = finalSchedule.getCell(cell);
 				if (listOfStudents.size() < 2) {
 					Iterator<String> iter2 = schedule.getCell(cell).iterator();
 					boolean foundOne = false;
 					while (iter2.hasNext()) {
 						String name = iter2.next();
 						if (!chosenStudents.contains(name)) {
 							chosenStudents.add(name);
 							Cell[] cells = new Cell[2];
 							cells[0] = cell;
							cells[1] = schedule.getAccompanyingCell(name, cell);
 							chosenStudentsHash.put(name, cells);
 							finalSchedule.addName(name, cell);
 							foundOne = true;
 							break;
 						}
 					}
 					
 					/*
 					 * If no suitable student was found, pick a random student to add and
 					 * remove that student from his original cell. Hopefully it will the cells
 					 * will correct themselves over time?
 					 */
 					if (foundOne == false) {
 						int nameNum = new Random().nextInt(schedule.getCell(cell).size());
 						int i = 0;
 						String name = null;
 						for (String s : schedule.getCell(cell)) {
 							if (i == nameNum) {
 								name = s;
 							}
 							i++;
 						}
 						Cell[] oldCells = chosenStudentsHash.get(name);
 						finalSchedule.removeName(name, oldCells[0]);
 						finalSchedule.removeName(name, oldCells[1]);
 						
 						finalSchedule.addName(name, cell);
 						Cell secondCell = schedule.getAccompanyingCell(name, cell);
 						finalSchedule.addName(name, secondCell);
 
 						Cell[] cells = new Cell[2];
 						cells[0] = cell;
 						cells[1] = secondCell;
 						chosenStudentsHash.put(name, cells);
 						toAdd.add(oldCells[0]);
 						toAdd.add(oldCells[1]);
 
 						
 					}
 					
 				} else {
 					// Change the size of the number of problem cells
 					toRemove.add(cell);
 				}
 				
 			}
 			
 			Iterator<Cell> iter3 = toRemove.iterator();
 			while (iter3.hasNext()) {
 				problemCells.remove(iter3.next());
 			}
 			toRemove.clear();
 			
 			iter3 = toAdd.iterator();
 			while (iter3.hasNext()) {
 				problemCells.add(iter3.next());
 			}
 			toAdd.clear();
 			
 		}
 		
 		finalSchedule.print();
 		
 		
 		/*
 		 * Then tackle the rest of the cells, making sure they all have the same number
 		 * or if they're already added to the finished set
 		 */
 		int numStudents = getNumberOfStudents();
 		int counter = 0;
 //		while (counter < numStudents) {
 //			
 //		}
 		
 	}
 	
 	
 	/*
 	 * Populates the schedule. Two rules:
 	 * 	1) Doesn't schedule tabling when you have classes
 	 *  2) Schedules tabling for an hour slot after your class ends
 	 *  	2a) Corrects itself if you stack classes one after another
 	 *  
 	 *  Took out:
 	 *  3) Doesn't table if it's an hour slot in between classes. Some people
 	 *  	don't care about tabling in between that section, and can always be
 	 *  	added to the blacklist if they are troublesome.
 	 *  	Fix: Change the for-loop to iterate from startTime-2 to endTime.
 	 */
 	private void populateSchedule(String name, Day d, Time time, Type type) {
 		int startTime = time.getStartTime();
 		int endTime = time.getEndTime();
 
 		for (int i = startTime; i < endTime; i++) {
 //			schedule.addName(name, Day.value(d), i); // Populates with regular schedule
 			schedule.addDNS(name, Day.value(d), i);
 		}	
 		
 		if (type == Type.NORMAL){ 
 			// Adds potential tablers to the schedule
 			if (endTime >= 0 && endTime <= 8) { // Only need to check from 10-2pm
 				schedule.addName(name, Day.value(d), endTime);
 				schedule.addName(name, Day.value(d), endTime+1);
 			}
 		}
 	}
 		
 	
 	/* 
 	 * Parses schedules pass in the form of:
 	 * NAME [COMMITTEE]
 	 * 05253	ASAMST R2A	 01 LEC	4	 Letter Grade	-T-T-	 1100 -1230 PM	0151 BARROWS	 Enrolled 
 	 * ...
 	 * 
 	 * Regex finds the dates and times and passes them to populateSchedule()
 	 */
 	private void parseSchedule(String name, String schedule, Type type) {
 		if (name == null) {
 			return;
 		}
 		
 		studentNames.add(name);
 		
 		Pattern day_patt = Pattern.compile("[M-][T-][W-][T-][F-]");
 		Matcher day_m = day_patt.matcher(schedule);
 		
 		Pattern time_patt = Pattern.compile("\\d{4} -\\d{4} [AP]M");
 		Matcher time_m = time_patt.matcher(schedule);
 		
 		while (day_m.find() && time_m.find()) {
 			String weekdays = day_m.group();
 			String time = time_m.group();
 			for (int i = 0; i < NUM_WEEKDAYS; i++) {
 				char weekday = weekdays.charAt(i);
 				if (weekday != '-') {
 					populateSchedule(name, Day.parseDay(weekday, i), new Time(time), type);
 				}
 			}
 		}
 	}
 	
 	/*
 	 * Since Telebear schedules start with an ID, can safely check the first character to
 	 * see if it is a character. If so, it's a name and should remember it until the next
 	 * character is read, meaning the next schedule is being read.
 	 */
 	private void parseFile(String filename, Type type) {
 		try {
 			FileInputStream fstream = new FileInputStream(filename);
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String str;
 			String name = null;
 			while ((str = br.readLine()) != null) {
 				if (str.compareTo("") == 0) {
 					name = null;
 				} else if (java.lang.Character.isLetter(str.charAt(0)) &&
 						name == null) { // Remember thy name
 					name = str;
 				} else {
 					parseSchedule(name, str, type);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	void print() {
 //		schedule.printRow(0);
 		schedule.print();
 	}
 
 	public static void main(String args[]) {
 		Tabler t = new Tabler();
 		t.parseFile("src/tabler/data_full.txt", Type.NORMAL);
 //		t.parseFile("src/tabler/data_blacklist.txt", Type.BLACKLIST);
 		t.pickStudents();
 //		t.print();
 	}
 }
