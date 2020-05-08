 package calculations;
 
 import java.io.IOException;
 import java.io.File;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import calculations.data_access.DataAccess;
 
 
 /**
  * The <code>Train</code> class represents a train running on the section. Some of the
  * properties that an instance of <code>Train</code> holds are the train number, the train
  * name, the distance of the train from the home station, etc. <p>The class extends
  * <code>Thread</code>, thus enabling it to keep train-related data up-to-date
  * by making its own calculations. This means that though there <i>may</i> be delays in
  * rendering of data on-screen, the data held by an instance of <code>Train</code> is
  * always accurate.
  */
 public class Train extends Thread
 {
 	/**
 	 * A static and final <code>String</code> variable that contains
 	 * the path of the XML file that contains train info.
 	 */
	private static final String TRAIN_FILE_PATH = "Data" + File.separator;
 	
 	/**
 	 * Final variable that stores which direction a train is travelling on the section.
 	 * A train with direction HOME is travelling towards Calicut. <br>The direction is
 	 * set using the <code>Train</code> constructor.
 	 */
 	final int HOME = 1;
 
 	/**
 	 * Final variable that stores which direction a train is travelling on the section.
 	 * A train with direction AWAY is travelling towards Shoranur Junction. <br>The
 	 * direction is set using the <code>Train</code> constructor.
 	 */
 	final int AWAY = 2;
 
 	/**
 	 * The unique number of the train.
 	 * This is of <code>String</code> datatype, rather than <code>int</code>,
 	 * because some train numbers can contain alphabets, for example, 4021A.
 	 */
 	String no;
 
 	/**
 	 * The name of the train.
 	 */
 	String name;
 
 	/**
 	 * The current lag of the train.
 	 * The default lag is 0. When a train is late, the lag time increases. When
 	 * a train is early, its lag time moves into negative.
 	 */
 	int lag;
 
 	/**
 	 * A <code>String</code> collection of stations on the section where the
 	 * train will halt.
 	 */
 	Vector<String> stations;
 	
 	/**
 	 * A <code>HashMap</code> that stores the distances of stations that the
 	 * train halts.
 	 */
 	HashMap<String,Integer> distances;
 
 	/**
  	 * A <code>Hashtable</code> of arrival times of the train at its halts. The
  	 * keys of the <code>Hashtable</code> are the names of the stations where it
  	 * halts.
 	 */
 	HashMap<String,String> arrivalTimes;
 
 	/**
  	 * A <code>Hashtable</code> of departure times of the train at its halts.
  	 * The keys of the <code>Hashtable</code> are the names of the stations
  	 * where it halts.
 	 */
 	HashMap<String,String> departureTimes;
 
 	/**
 	 * The distance of the train from Calicut.
 	 */
 	float distance;
 
 	/**
 	 * The direction in which the train is travelling. This can either be HOME
 	 * or AWAY.
 	 */
 	int direction;
 
 	/**
 	 * Constructor that initializes the <code>Train</code>.
 	 * The constructor then starts the thread.
 	 *
 	 * @param trainNo   The number of the train.
 	 * @param trainName The name of the train.
 	 * @param direction The direction in which the train is travelling.
 	 */
 	public Train(String trainNo, String trainName, String direction)
 			throws IOException, SAXException, ParserConfigurationException
 	{
 		this.no = trainNo;
 		this.name = trainName;
 		stations = new Vector<String>();
 		arrivalTimes = new HashMap<String,String>();
 		departureTimes = new HashMap<String,String>();
 		distances = new HashMap<String,Integer>();
 		distance = 0;
 		lag = 0;
 		if(direction.equals("TowardsHome"))	this.direction = HOME;
 		else if(direction.equals("AwayFromHome")) this.direction = AWAY;
 		populateTrainData();
 		start();
 	}
 
 	/**
 	 * Populates the train with the list of stations where the train halts,
 	 * their distances and the arrival and departure times at those stations.
 	 * The order of the stations corresponds to the direction of the train.
 	 * For example, if the train travels from Calicut to Shoranur, then the list
 	 * of stations starts from Calicut and ends at Shoranur. If the train
 	 * travels from Shoranur to Calicut, then the list of stations starts from
 	 * Shoranur and ends at Calicut.
 	 */
 	private void populateTrainData()
 			throws IOException, SAXException, ParserConfigurationException {
 		System.out.println( "Loading data for " + no);
 		Vector<Element> stops = DataAccess.getInstance().extractData(TRAIN_FILE_PATH + no + ".xml","stoppage");
 		if(stops.size() > 0) {
 			for (Element stop : stops) {
 				stations.add( stop.getAttribute("name"));
 				arrivalTimes.put( stop.getAttribute("name"), 
 					stop.getAttribute("arrival-time"));
 				departureTimes.put( stop.getAttribute("name"), 
 					stop.getAttribute("departure-time"));
 				
 			}
 		}
 		//Reversing the distances if travelling towards home
 		if (direction == HOME)	java.util.Collections.reverse(stations);
 	}
 
 	/**
 	 * The run method of the <code>Thread</code>.
 	 * The method constantly determines the distance of the train from Calicut station.
 	 */
 	public void run()
 	{
 		//Let's get the time first, then we can calculate where each train is supposed to
 		//be at the time the game is started.
 		/*Now let's get the arrival (or departure) time of the train in the station it
 		encounters first while entering the section. If initialTime is greater, then the
 		train has already entered the section. We then calculate the number of seconds
 		between initialTime and the time at first station. Depending upon this, we
 		calculate the distance the train would have crossed as 60(the MPS) * seconds. We
 		then draw it on the screen.*/
 		String station = stations.get(0);
 		station = station.replace(' ','-');
 		String time = "";
 		if((arrivalTimes.get(station)).length() > 0)
 			time = arrivalTimes.get(station) + ":00";
 		else if((departureTimes.get(station)).length() > 0)
 			time = departureTimes.get(station) + ":00";
 		String timecomponents[] = time.split(":");
 		Calendar first_station_time = Calendar.getInstance();
 		first_station_time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timecomponents[0]));
 		first_station_time.set(Calendar.MINUTE, Integer.parseInt(timecomponents[1]));
 		first_station_time.set(Calendar.SECOND, Integer.parseInt(timecomponents[2]));
 		int count = 0;
 		while(true)
 		{
 			Calendar currentTime = Calendar.getInstance();
 			if(first_station_time.before(currentTime)) {
 				//The train is already in the section.
 				float totalseconds = getTimeDifference(currentTime,first_station_time);
 				distance = 60 * (totalseconds/3600);
 				long sleepTime = 1000;
 				if (distances.entrySet().contains(distance)) {
 					sleepTime = 2000;
 				}
 				try
 				{
 					System.out.println("sleeping..." + count++);
 					sleep(sleepTime);
 				}
 				catch(InterruptedException objInterruptedException)
 				{
 					System.out.println("InterruptedException");
 					objInterruptedException.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * A utility method that returns the difference(in seconds) between
 	 * any two times.<p> This method can be used to calculate the lag time
 	 * of the train.<p>Of the two times, <code>time1</code> is the greater
 	 * time, while <code>time2</code> is the lesser time.
 	 *
 	 * @param time1 The greater time
 	 * @param time2 The lesser time
 	 * @return The difference between the two times in seconds.
 	 */
 	private float getTimeDifference(Calendar time1, Calendar time2)
 	{
 		float hours = time1.get(Calendar.HOUR_OF_DAY) - time2.get(Calendar.HOUR_OF_DAY);
 		float minutes = time1.get(Calendar.MINUTE) - time2.get(Calendar.MINUTE);
 		float seconds = time1.get(Calendar.SECOND) - time2.get(Calendar.SECOND);
 		float totalseconds = (hours * 3600) + (minutes * 60) + seconds;
 		return totalseconds;
 	}
 
 	/**
 	 * Returns the distance of the train from Calicut station.
 	 *
 	 * @return the distance of the train.
 	 */
 	public float getDistance()
 	{
 		return distance;
 	}
 
 	/**
 	 * Returns the direction in which the train is travelling across the section.
 	 * The method returns 1 if the train is moving towards Calicut, and 2 if
 	 * the train is moving towards Shoranur.
 	 *
 	 * @return the direction of the train
 	 */
 	public int getDirection()
 	{
 		return direction;
 	}
 }
