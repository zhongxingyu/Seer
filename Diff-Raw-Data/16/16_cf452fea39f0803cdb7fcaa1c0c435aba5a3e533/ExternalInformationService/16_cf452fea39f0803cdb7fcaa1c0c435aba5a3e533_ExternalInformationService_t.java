 package heatcontrol.java;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.client.methods.*;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.HttpClients;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 public class ExternalInformationService implements Runnable {
 
 	private String weatherURL = "http://api.worldweatheronline.com/free/v1/weather.ashx?q=Groningen&format=XML&num_of_days=1&key=8phfymv55kgpmynugrzaq84v";
 	private ArrayList<String> calendarIDs = new ArrayList<String>(0);
 	private ExternalSensors sensors = null;
 
 	private int numSensors = 4; // amount of sensors on the building containing
 								// the rooms
 
 	private final LinkedBlockingQueue<WeatherObject> weatherQueue;
 	private final LinkedBlockingQueue<ExternalSensorObject> externalSensorQueue;
 	private final LinkedBlockingQueue<CalendarObject> calendarQueue;
 
 	public ExternalInformationService(LinkedBlockingQueue<WeatherObject> wq,
 			LinkedBlockingQueue<ExternalSensorObject> eq,
 			LinkedBlockingQueue<CalendarObject> cq) {
 		calendarIDs.add("34r7otno7m7k4d50cg8j4urqps@group.calendar.google.com");
 		weatherQueue = wq;
 		externalSensorQueue = eq;
 		calendarQueue = cq;
 	}
 
 	public void run() {
 		while (true) {
 			if (sensors == null) {
 				sensors = new ExternalSensors(numSensors);
 			}
 			long startTime = System.currentTimeMillis();
 			// Each hour get information from external sensors
 			getExternalTemperature();
 
 			// Each day at 12pm get new information from calendar and weather
 			// sources
 			// if (Calendar.getInstance().HOUR == 12) {
 			getWeatherInformation();
 			System.out.println("getting weather information");
 			getCalendarInformation();
 			// }
 			// sleep for the rest of the hour
 			try {
 				// Thread.sleep(3600000 - (System.currentTimeMillis() -
 				// startTime));
				Thread.sleep(60000); // for testing sleep 6 seconds
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * getExternalTemperature calls the external sensors to provide the current
 	 * temperature measured at each sensor. The average of these temperatures is
 	 * then saved as the temperature of the environment outside the building
 	 * containing the rooms.
 	 */
 	private void getExternalTemperature() {
 		int[] temperatures = sensors.simulate();
 		int sum = 0;
 		for (int i = 0; i < temperatures.length; i++) {
 			sum += temperatures[i];
 		}
		ExternalSensorObject externalTemp = new ExternalSensorObject(sum/ temperatures.length);
		externalSensorQueue.add(externalTemp);
 	}
 
 	/**
 	 * getCalendarInformation prepares and executes a REST call to the calendar
 	 * service. For each calendar specified the information in the calendars is
 	 * retrieved through a REST call. This information is parsed from the
 	 * returned XML and stored in the database.
 	 */
 	private void getCalendarInformation() {
 		// Document calendarInformation = null;
 		//
 		// // get the data of today and add a day to represent tomorrow.
 		// //This is used to limit the amount of calendar events retrieved
 		// Calendar tomorrow = Calendar.getInstance();
 		// tomorrow.add(tomorrow.DAY_OF_YEAR, 1);
 		//
 		// for (int i = 0; i < calendarIDs.size(); i++) { // for each calendar
 		// owned by the rooms
 		// // Build URL for current calendar
 		// String calendarURL =
 		// // Get the XML containing the list of all events in the calendar
 		// try {
 		// calendarInformation = restCall(calendarURL);
 		// } catch (IOException e) {
 		// System.out.println("Calendar call failed");
 		// e.printStackTrace();
 		// }
 		//
 		// }
 
 		// TODO: parse list of calenders and get information from individual
 		// calendars
 	}
 
 	/**
 	 * getWeatherInformation uses to predefined url to make a rest call to the
 	 * worldWeatherOnline api. It receives a Document object containing the xml
 	 * returned by the server. The function then parses this Document and gets
 	 * the temperature and date from the forecast. This information is then put
 	 * into the db.
 	 */
 	private void getWeatherInformation() {
 		Document weatherInformation = null;
 		try {
 			weatherInformation = restCall(weatherURL);
 		} catch (IOException e) {
 			System.out.println("Weather call failed");
 			e.printStackTrace();
 		}
 
 		NodeList weatherList = weatherInformation
 				.getElementsByTagName("weather");
 		Element weather = (Element) weatherList.item(0);
 
 		int temp_max = getIntValue(weather, "tempMaxC");
 		int temp_min = getIntValue(weather, "tempMinC");
 		String date = getTextValue(weather, "date");
 
 		WeatherObject forecast = new WeatherObject(date, temp_max, temp_min);
 		weatherQueue.add(forecast);
 		// System.out.println("Type: " + forecast.getType());
 		// System.out.println("Date: " + forecast.getDate());
 		// System.out.println("Temp min: " + forecast.getTempMin());
 		// System.out.println("Temp max: " + forecast.getTempMax());
 	}
 
 	/**
 	 * restCall function handles the communication with rest/http api's of the
 	 * weather forecast website and the google calendar. The function takes an
 	 * url, which specifies all paramters for the call, and returns a Document
 	 * object containing the xml repsonse of the web services.
 	 * 
 	 * @param url
 	 * @return Document
 	 * @throws IOException
 	 */
 	private Document restCall(String url) throws IOException {
 		CloseableHttpClient httpClient = HttpClients.createDefault();
 		CloseableHttpResponse httpResponse = httpClient
 				.execute(new HttpGet(url));
 
 		if (httpResponse.getStatusLine().getStatusCode() != 200) {
 			throw new RuntimeException("Rest call failed");
 		}
 
 		HttpEntity entity = httpResponse.getEntity();
 
 		Document content = null;
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		try {
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			content = builder.parse(entity.getContent());
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		}
 
 		httpResponse.close();
 
 		return content;
 	}
 
 	/*
 	 * Helper functions for parsing the XML
 	 */
 
 	/**
 	 * XML parsing helper function. Takes an xml element and the tag that is
 	 * being looked for. Returns string contained in the tag
 	 * 
 	 * @param Element
 	 * @param String
 	 *            tagname
 	 * @return String textVal
 	 */
 	private String getTextValue(Element ele, String tagName) {
 		String textVal = null;
 		NodeList nl = ele.getElementsByTagName(tagName);
 		if (nl != null && nl.getLength() > 0) {
 			Element el = (Element) nl.item(0);
 			textVal = el.getFirstChild().getNodeValue();
 		}
 
 		return textVal;
 	}
 
 	/**
 	 * Calls getTextValue and returns a int value of the returned String
 	 * 
 	 * @param Element
 	 * @param String
 	 *            tagname
 	 * @return String
 	 */
 	private int getIntValue(Element ele, String tagName) {
 		// in production application you would catch the exception
 		return Integer.parseInt(getTextValue(ele, tagName));
 	}
 }
