 package main;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import util.Time;
 import util.Time2;
 
 /**
  * The Driver class which represents a driver. The driver does not know his
  * start number. This can be accessed from the database structure instead. He
  * does know his name, start times, finish times and which classes he's a part
  * of.
  * 
  * @author Team08
  * 
  */
 public class Driver {
 	private String name;
 	private List<Time> startTime = new ArrayList<Time>();
 	private List<Time> finishTime = new ArrayList<Time>();
 	private String classes = "";
 	private int id;
 	private ArrayList<String> driverAttribute = new ArrayList<String>();
 
 	/**
 	 * The constructor which creates the Driver without a name
 	 * 
 	 */
 	public Driver() {
 
 	}
 
 	/**
 	 * The constructor which creates the Driver
 	 * 
 	 * @param name
 	 *            the drivers name
 	 */
 	public Driver(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Returns the name of the driver
 	 * 
 	 * @return The name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Inserts a new start time in the list. Multiple start times are allowed.
 	 * 
 	 * @param time
 	 *            New start time
 	 */
 	public void addStartTime(Time time) {
 		startTime.add(time);
 		time.sortArrayInOrder(startTime);
 	}
 
 	/**
 	 * Inserts a new finish time in the list. Multiple finish times are allowed
 	 * 
 	 * @param time
 	 *            New finish time
 	 */
 	public void addFinishTime(Time time) {
 		finishTime.add(time);
 		time.sortArrayInOrder(finishTime);
 	}
 
 	/**
 	 * Returns the driver's start times as a list
 	 * 
 	 * @return The start times as a list
 	 */
 	public List<Time> startTime() {
 		return startTime;
 	}
 
 	/**
 	 * Returns the driver's finish times as a list
 	 * 
 	 * @return The finish times as a list
 	 */
 	public List<Time> finishTime() {
 		return finishTime;
 	}
 
 	/**
 	 * Set the name of the driver. The current name is replaced.
 	 * 
 	 * @param name
 	 *            The new name of the driver
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Returns the time from last registered time to the lap i
 	 * 
 	 * @param i
 	 *            The lap number, starting from 0
 	 * @return A time object which represents the lap time
 	 */
 	public String getLapTime(int i) {
 		int laptime;
 		if (i == 0) {
 			
 			laptime = startTime.get(0).timeDiff(finishTime.get(0));
 		} else if (finishTime.size() > i) {
 			laptime = finishTime.get(i - 1).timeDiff(finishTime.get(i));
 		} else {
			return "";
 		}
 		return new Time(laptime).toString();
 	}
 
 	/**
 	 * The driver is added to a new class. The old class is overwritten
 	 * 
 	 * @param c
 	 *            The new class the driver is a part of
 	 * 
 	 */
 	public void addClass(String c) {
 		classes = c;
 	}
 
 	/**
 	 * Returns the current class the driver is a part of
 	 * 
 	 * @return A string with the class name
 	 */
 	public String getRaceClass() {
 		return classes;
 	}
 
 	/**
 	 * Returns the number of laps
 	 * 
 	 * @return The number of laps as an integer
 	 */
 	public int getNumberOfLaps() {
 		return finishTime.size();
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 
 	public int totalTime() {
 		return startTime.get(0).timeDiff(finishTime.get(finishTime.size() - 1));
 	}
 	public void addAttribute(String attribute){
 		driverAttribute.add(attribute);
 	}
 	public ArrayList<String> getAttributes(){
 		return driverAttribute;
 	}
 	
 	public ArrayList<String> listOfLapTimes(){
 		ArrayList<String> lapTimes = new ArrayList<String>();
 		for(int i = 0; i < finishTime.size(); i++){
 			lapTimes.add(getLapTime(i));
 		}
 		return lapTimes;
 	}
 	
 	public String getStageTime(int i) {
 		//Kolla så det finns någon tid för etappen i
 		if (i > startTime().size()-1 || i > finishTime().size()-1) {
 			return("");
 		//Annars returnera tiden etappen i
 		} else {
 			return(new Time(finishTime().get(i).timeDiff(startTime().get(i))).toString());
 		}
 	}
 	
 	
 	
 	private int addStageTime(int i) {
 		if (i > startTime().size()-1 || i > finishTime().size()-1) {
 			return 0;
 		} else {
 			return(finishTime().get(i).timeDiff(startTime().get(i)));
 		}
 	}
 	
 	
 	public String getTotStageTime() {
 		int sum = 0;
 		for(int i = 0; i < startTime.size(); i++){
 			sum += addStageTime(i);
 		}
 		return new Time(sum).toString();
 	}
 
 	public String getNbrOfStages() {
 		String s = Integer.toString(finishTime.size());
 		if(startTime.size() < finishTime.size()){
 			s =  Integer.toString(startTime.size());
 		}
 		return s;
 	}
 	
 }
