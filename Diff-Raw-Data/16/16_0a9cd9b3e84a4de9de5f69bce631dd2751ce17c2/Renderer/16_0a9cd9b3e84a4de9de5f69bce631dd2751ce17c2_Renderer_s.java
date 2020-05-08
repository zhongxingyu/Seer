 /**
  * The Section Controller game has been designed with the intent of having three layers:
  * <ol>
  *  <li>Calculation - Performs any game calculation and stores its results. It also
  * holds getter, and if required, setter methods to the results.</li>
  *  <li>Renderer - This layer has instances of all the objects that are shown to the user.
  * It uses these instances to invoke methods that display the graphical equivalent of the
  * data in the Calculation layer.</li>
  *  <li>Presentation - Contains only JFrame code.</li>
  * </ol>
  *
  * This package belongs to the Renderer layer.
  */
 package rendering;
 
 import java.io.IOException;
 import java.io.File;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.GregorianCalendar;
 import java.util.Vector;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import presentation.panes.GamePane;
 import presentation.panes.InfoPane;
 import presentation.windows.GameScreen;
 import calculations.Station;
 import calculations.Train;
 import calculations.data_access.DataAccess;
 
 /**
  * The Renderer class is the only class in the Renderer layer. It holds instances of the
  * objects which the user can see, as well as instances of the classes in the Calculation
  * layer. It uses these instances to update both the layers.
  *
  * The class extends <code>Thread</code> since the job of this class is a constant process.
  *
  */
 public final class Renderer extends Thread
 {
 	/**
 	 * Contains an instance of this class. This is the instance that is returned
 	 * when an instance of the class is requested.
 	 */
 	private static Renderer objRenderer = new Renderer();
 
 	/**
 	 * An instance of <code>GameScreen</code> class.
 	 */
 	GameScreen objGameScreen;
 
 	/**
 	 * An instance of <code>InfoPane</code> class.
 	 */
 	InfoPane objInfoPane;
 
 	/**
 	 * An instance of <code>InfoPane</code> class.
 	 */
 	GamePane objGamePane;
 
 	Vector<Float> objTrainPositions;
 
 	Vector<Train> objTrains;
 
 	Vector<Station> objStations;
 
 	Vector<String> objStationNames;
 
 	Vector<Integer[]> aspects;
 	
 	/**
 	 * A static and final <code>String</code> variable that contains
 	 * the path of the XML file that contains station info.
 	 */
	private static final String STATIONS_FILE_PATH = "Data" + 
		File.separator + "Stations.xml";
 	
 	/**
 	 * A static and final <code>String</code> variable that contains
 	 * the path of the XML file that contains train info.
 	 */
	private static final String TRAINS_FILE_PATH = "Data" + 
		File.separator + "Trains.xml";
 
 	/**
 	 * Constructor made private to disable default constructor provided by Java.
 	 */
 	private Renderer(){}
 
 	/**
 	 * Returns an instance of this class.
 	 *
 	 * @return an instance of <code>Renderer</code> class.
 	 */
 	public static Renderer getInstance()
 	{
 		return objRenderer;
 	}
 
 	/**
 	 * Opens up the game screen.
 	 *
 	 * NOTE: This code might require cleanup.
 	 */
 	public void setGameScreen() throws IOException, SAXException, ParserConfigurationException
 	{
 		getInitialData();
 		objGameScreen = new GameScreen();
 		start();
 	}
 
 	/**
 	 * Sets an instance of the <code>InfoPane</code> object.
 	 * 
 	 * @param pInfoPane An instance of the <code>InfoPane</code>
 	 * class. This is set by <code>LoginInterface</code>.
 	 */
 	public void setInfoPane(InfoPane pInfoPane)
 	{
 		objInfoPane = pInfoPane;
 	}
 
 	private void getInitialData() throws IOException, SAXException, ParserConfigurationException
 	{
 		populateStations();
 		populateTrains();
 	}
 
 	private void populateStations() throws IOException, SAXException, ParserConfigurationException
 	{
 		this.objStations = new Vector<Station>();
 		Vector<Element> objStations = DataAccess.getInstance().extractData(STATIONS_FILE_PATH,"station");
 		objStationNames = new Vector<String>();
 		Vector<Integer> objStationDistances = new Vector<Integer>();
 		Enumeration<Element> objEnumeration = objStations.elements();
 		while( objEnumeration.hasMoreElements())
 		{
 			Station individual_station = new Station(objEnumeration.nextElement());
 			this.objStations.add(individual_station);
 			objStationNames.add(individual_station.getName());
 			objStationDistances.add(individual_station.getDistance());
 		}
 		objGamePane = GamePane.getInstance();
 		objGamePane.setStationNames(objStationNames);
 		objGamePane.setStationPositions(objStationDistances);
 	}
 
 	/* We need all those trains that run today. Therefore, first we have to get today's day, then get all trains that run today as well as Daily,
 	 * and create the train objects.*/
 	private void populateTrains()
 			throws IOException, SAXException, ParserConfigurationException {
 		//Let's get the daily trains first
 		Vector<Element> temp1 = DataAccess.getInstance().extractData(TRAINS_FILE_PATH,"train[@day-of-arrival=Daily]");
 		
 		for (Element singleElement : temp1) {
 			System.out.print(singleElement.getAttribute("number"));
 			System.out.print(singleElement.getAttribute("name"));
 			System.out.println(singleElement.getAttribute("section-entry-time"));
 		}
 		
 		String day = "";
 		//Let's now find out what day it is, and then get the corresponding trains.
 		switch(new GregorianCalendar().get(Calendar.DAY_OF_WEEK))
 		{
 			case Calendar.SUNDAY:
 				day = "Su"; break;
 			case Calendar.MONDAY:
 				day = "M"; break;
 			case Calendar.TUESDAY:
 				day = "Tu"; break;
 			case Calendar.WEDNESDAY:
 				day = "W"; break;
 			case Calendar.THURSDAY:
 				day = "Th"; break;
 			case Calendar.FRIDAY:
 				day = "F"; break;
 			case Calendar.SATURDAY:
 				day = "Sa";
 		}
 		Vector<Element> temp2 = DataAccess.getInstance().extractData(TRAINS_FILE_PATH,"train.contains(@day-of-arrival," + day + ")");
 		
 		System.out.println("Day: " + day);
 		for (Element singleElement : temp2) {
 			System.out.print(singleElement.getAttribute("number"));
 			System.out.print(singleElement.getAttribute("name"));
 			System.out.println(singleElement.getAttribute("section-entry-time"));
 		}
 		
 		temp1.addAll(temp1.size(),temp2);
 		//temp1 is for Renderer; whereas the processing below is for GameScreen
 		Enumeration<Element> objEnumeration = temp1.elements();
 		Vector<String> objTrainNames = new Vector<String>();
 		objTrains = new Vector<Train>();
 		int i = 0;
 		while( objEnumeration.hasMoreElements())
 		{
 			Element train = objEnumeration.nextElement();
 			/*All trains for the day have been loaded. But we need only the trains that will
 			start from the first station in the next hour. So we check if the current time
 			falls within the train's first and last station times.*/
 			Calendar currentTime = Calendar.getInstance();
 			String fs_time = "", ls_time = "";
 			//get the first station time
 			fs_time = train.getAttribute("section-entry-time");
 			ls_time = train.getAttribute("section-leaving-time");
 			//We have got the times; let us convert them into Calendar instances.
 			String[] time1 = fs_time.split(":");
 			Calendar first_station_time = Calendar.getInstance();
 			int fs_hour = Integer.parseInt(time1[0]);
 			first_station_time.set(Calendar.HOUR_OF_DAY, fs_hour);
 			first_station_time.set(Calendar.MINUTE, Integer.parseInt(time1[1]));
 			String[] time2 = ls_time.split(":");
 			Calendar last_station_time = Calendar.getInstance();
 			int ls_hour = Integer.parseInt(time2[0]);
 			last_station_time.set(Calendar.HOUR_OF_DAY, ls_hour);
 			last_station_time.set(Calendar.MINUTE, Integer.parseInt(time2[1]));
 			
 			if (fs_hour >= 20) {
 				if (ls_hour >= 0 && ls_hour <= 6) {
 					int current_date = last_station_time.get(Calendar.DATE);
 					last_station_time.set(Calendar.DATE, current_date + 1);
 				}
 			}
 			
 			//We are ready to compare.
 			if(currentTime.after(first_station_time) && currentTime.before(last_station_time))
 			{
 				objTrainNames.add(train.getAttribute("number") + train.getAttribute("name"));
 				Train individual_train = new Train(train.getAttribute("number"),train.getAttribute("name"),train.getAttribute("direction"));
 				objTrains.add(individual_train);
 				i++;
 			}
 		}
 		System.out.println( "Total trains within the next one hour: " + i);
 		
 		for (String singleTrain : objTrainNames) {
 			System.out.println(singleTrain);
 		}
 		
 		objGamePane.setTrainNames(objTrainNames);
 	}
 
 	/**
 	 * The run method of the class.
 	 * This method constructs the <code>Runnable</code> objects which are later passed
 	 * on to <code>SwingUtilities</code>.
 	 */
 	public void run()
 	{
 		objTrainPositions = new Vector<Float>();
 		while(true)
 		{
 			objGameScreen.setUserName(objInfoPane.getUserName());
 			objGameScreen.setScore(objInfoPane.getScore());
 			objGameScreen.setTime(objInfoPane.getCurrentTime());
 			objGameScreen.setStationPositions(objGamePane.getStationPositions());
 			objGameScreen.setStationNames(objGamePane.getStationNames());
 			aspects = new Vector<Integer[]>();
 			Enumeration<Station> enumStations = objStations.elements();
 			while(enumStations.hasMoreElements())
 			{
 				Station individual_station = enumStations.nextElement();
 				aspects.add( individual_station.getAspects());
 			}
 			objGameScreen.setAspects(aspects);
 			Enumeration<Train> objEnumeration = objTrains.elements();
 			objTrainPositions.clear();
 			while(objEnumeration.hasMoreElements())
 			{
 				Train individual_train = objEnumeration.nextElement();
 				float distance = individual_train.getDistance();
 				/*The following if condition is bcoz: Consider the train is moving towards
 				home. The distance calculated shall be the distance from starting station,
 				which shall be the 'away station' in our case. But while drawing, the
 				train shall be drawn only from home. So if the train is 16km from away,
 				then while drawing, it will be 16km from home. This is not what we want.
 				So we reverse it.*/
 				if(individual_train.getDirection() == 1) // towards Home; see Train.java for details
 					distance = 86 - distance;
 				objTrainPositions.add(new Float(distance));
 			}
 			objGameScreen.setTrainPositions(objTrainPositions);
 			objGameScreen.repaint();
 
 			try
 			{
 				sleep(2000);
 			}
 			catch( InterruptedException objInterruptedException)
 			{
 				System.out.println( "InterruptedException: " + objInterruptedException.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Passes the aspects for a particular station to the Calculations
 	 * layer, which updates itself with this data.
 	 *
 	 * @param station The station whose aspects are to be updated in the
 	 *                Calculations layer
 	 * @param aspect  A <code>String</code> array that contains the aspect
 	 *                values for the station specified by <code>station</code>.
 	 */
 	public void setAspect(String station, String[] aspect)
 	{
 		int index = objStationNames.indexOf(station);
 		station = station.replace(' ','-');
 		Station objStation = objStations.get(index);
 		int i = 0;
 		while( i < 2)
 		{
 			objStation.setAspect(i,aspect[i]);
 			i++;
 		}
 	}
 }
