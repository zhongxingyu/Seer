 package core;
 
 import java.io.File;
 import java.util.Date;
 import java.util.Properties;
 
 import persistance.schedule.Schedule;
 import persistance.schedule.Stop;
 import persistance.schedule.store.DefaultSchedule;
 import persistance.schedule.store.FileParser;
 import time.Day;
 import time.Time;
 import time.TimeRange;
 
 /** A message handler that performs a simple lookup in from a weekly schedule. */
 public class TableHandler extends MessageHandler {
 
 	/** The definition for the ALL_KEYWORD */
 	private static final String ALL_DEFINITION = "all stops";
 
 	/** The keyword for all stops in the schedule. */
 	private static final String ALL_KEYWORD = "all";
 
 	/** The message that is displayed when there is a break in service. */
	private static final String BREAK_NOTICE = "(service break)";
 
 	/**
 	 * If there are more than this number of minutes between stops, then
 	 * BREAK_NOTICE will be inserted.
 	 */
 	private static final int GAP_MINUTES = 120;
 
 	/** The schedule for stop lookups. */
 	private Schedule schedule;
 
 	/**
 	 * This method generates a formatted list of stop times seperated by
 	 * newlines. The two format options are:
 	 * 
 	 * <pre>
 	 * Short:  9:23pm
 	 *        10:44am i
 	 *        
 	 * Long    9:23pm @ Templeton
 	 *        10:44am @ Burlingame Fred Meyer
 	 * </pre>
 	 * 
 	 * @param stops
 	 *            the list of stops to be formatted
	 * @param time
	 *            the current time (used for calculating schedule breaks)
 	 * @param chars
 	 *            the maximum length of the response
 	 * @param shortFormat
 	 *            true if stops should be displayed in their short format
 	 * @return a the list of stops
 	 */
	private String formatStops(Iterable<Stop> stops, Date time,
			int maxLength, boolean shortFormat) {
 		StringBuilder response = new StringBuilder();
		Time previousStopTime = new Time(time.getTime()); // used for calculating breaks
 		for (Stop s : stops) {
 			Time currentStopTime = s.getTime();
 			// calculate if there is a break in Pio Express service
 			if (GAP_MINUTES < minutesBetween(previousStopTime, currentStopTime)) {
 				// append if within length restrictions
 				if ((response.length() + BREAK_NOTICE.length() + 1) > maxLength) {
 					break;
 				} else {
 					response.append("\n" + BREAK_NOTICE);
 				}
 			}
 			String line;
 			if (shortFormat) { // e.g. 9:23pm i
 				line = "\n" + currentStopTime.toString(false);
 				if (!s.getDirection().getMarker().isEmpty()) {
 					line += " " + s.getDirection().getMarker();
 				}
 			} else { // long format e.g. 9:23pm @ Fred Meyer's
 				String location = keywords.getDefinition(s.getKeyword());
 				line = String.format("\n%s @ %s", currentStopTime.toString(),
 						location);
 			}
 			// don't exceed the boundries of one sms
 			if (response.length() + line.length() > maxLength) {
 				break;
 			} else {
 				response.append(line);
 				previousStopTime = currentStopTime;
 			}
 		}
 		return response.toString();
 	}
 
 	/**
 	 * A helper method returning a formatted list of stops.
 	 * 
 	 * @see formatStops()
 	 * @param time
 	 *            the time after which stops should be found
 	 * @param maxLength
 	 *            the maximum length of the return String
 	 * @return a formatted list of all stops in the schedule
 	 */
 	String getAllStops(Date time, int maxLength) {
 		Time requestTime = new Time(time.getTime());
 		TimeRange range = new TimeRange(requestTime, requestTime.addDays(1));
 		Iterable<Stop> stops = schedule.getStops(range);
		String stopList = formatStops(stops, time, maxLength, false);
 		return stopList;
 	}
 
 	/**
 	 * A helper method returning a formatted list of stops.
 	 * 
 	 * @see formatStops()
 	 * @param time
 	 *            the time after which stops should be found
 	 * @param keyword
 	 *            the single stop being examined
 	 * @param maxLength
 	 *            the maximum length of the return String
 	 * @return a formatted list of times the bus will be at the stop
 	 */
 	String getOneStop(Date time, String keyword, int maxLength) {
 		// add one location declaration, because this is single stop
 		String response = "\n@ " + keywords.getDefinition(keyword);
 		// get list of stops
 		Time requestTime = new Time(time.getTime());
 		Iterable<Stop> stops = schedule.getNextStops(keyword, requestTime, 20);
 		int charactersLeft = maxLength - response.length();
		response += formatStops(stops, time, charactersLeft, true);
 		return response;
 	}
 
 	/**
 	 * Loads a schedule from a file. This should throw some sort of exception...
 	 * Also, it sets up all other necessary internal workings of this handler.
 	 * 
 	 * This is seperate / visible from initialize(props) for testing reasons.
 	 * 
 	 * @param file
 	 *            the file to load a schedule from
 	 */
 	void initialize(File file) {
 		keywords.add(ALL_KEYWORD, ALL_DEFINITION);
 		DefaultSchedule sched = new DefaultSchedule();
 		new FileParser(sched, keywords, file).parse();
 		this.schedule = sched;
 	}
 
 	@Override
 	public void initialize(Properties props) {
 		super.initialize(props);
 		String url = props.getProperty("schedule_file");
 		initialize(new File(url));
 
 	}
 
 	@Override
 	public String keywordMessage(Query query, int maxLength) {
 		StringBuilder response = new StringBuilder();
 		if (query.getKeyword().equals(ALL_KEYWORD)) {
 			int charactersLeft = maxLength - response.length();
 			String stopList = getAllStops(query.getTimeReceived(),
 					charactersLeft);
 			response.append(stopList);
 		} else {
 			int charactersLeft = maxLength - response.length();
 			String stopList = getOneStop(query.getTimeReceived(), query
 					.getKeyword(), charactersLeft);
 			response.append(stopList);
 		}
 		return response.toString();
 	}
 
 	@Override
 	public String unrecognizedKeywordMessage(Date date, int maxLength) {
 		String response = "\nKeyword unrecognized! Text HELP for info.\n";
 		int charactersLeft = maxLength - response.length();
 		String stopList = getAllStops(date, charactersLeft);
 		response += stopList;
 		return response;
 	}
 
 	public static void main(String[] args) {
 		TableHandler th = new TableHandler();
 		th.initialize(new File("resources/test_schedule.txt"));
 		for (String k : th.keywords.words()) {
 			System.out.println("Response for keyword: " + k);
 			System.out.println("------------------------------");
 			Query q = new Query(new Date(), k, "+15037777777");
 			String response = th.getResponse(q);
 			System.out.println(response + "\n\n");
 		}
 	}
 
 	/**
 	 * Calculates the minutes between two weekly times.
 	 * 
 	 * @param begin
 	 *            the earlier time
 	 * @param end
 	 *            the later time
 	 * @return the difference between the two, in minutes
 	 */
 	private static int minutesBetween(Time begin, Time end) {
 		int days;
 		if (!begin.getDay().equals(end.getDay())) {
 			days = Day.daysBetween(begin.getDay(), end.getDay()).size() - 1;
 		} else {
 			days = 0;
 		}
 		int hours = end.getHour() - begin.getHour();
 		int minutes = end.getMinute() - begin.getMinute();
 		return 1440 * days + 60 * hours + minutes;
 	}
 
 }
