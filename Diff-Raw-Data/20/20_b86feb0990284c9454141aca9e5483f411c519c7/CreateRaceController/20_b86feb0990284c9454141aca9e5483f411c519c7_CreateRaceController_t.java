 package Controller;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import Beans.Race;
 import Beans.User;
 import Beans.Racer;
 import Beans.Item;
 import Beans.ItemObj;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /******************************************************
  * This is a controller class that is responsible for * setting up the race for
  * a user. It calls the model * to ensure that the database is updated
  * accordingly *
  ******************************************************/
 
 public class CreateRaceController extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	private Race raceModel;
 	private User userModel;
 	private Racer racerModel;
 	private Item itemModel;
 
 	/**
 	 * Constructor which has the responsibility for initializing the model
 	 * objects.
 	 */
 	public CreateRaceController() {
 		super();
 
 		try {
 			raceModel = new Race();
 			userModel = new User();
 			racerModel = new Racer();
 			itemModel = new Item();
 
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This is the doGet method which will take a request and a response and
 	 * work with the request then inform the page where to go next.
 	 * 
 	 * @param request
 	 *            The request which contains the JSON
 	 * @param response
 	 *            The response that will be passed back.
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		String forward = "";
 
 		try {
 			// Creates the JSON Object from the request.
 			JSONObject jsonObj = getJSON(request.getReader());
 
 			// Adds the race to the database.
 			createRace(jsonObj);
 
 			response.setStatus(HttpServletResponse.SC_OK);
 			
 			RequestDispatcher dispatcher = request.getRequestDispatcher(forward);
 			dispatcher.forward(request, response);
 		} catch (IOException e) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 					"Problem when reading the JSON object.");
 		} catch (JSONException e) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 					"Problem with the JSON object.");
 		} catch (ParseException e) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 					"Problem with the Date object.");
 		} catch (SQLException e) {
 			response.sendError(HttpServletResponse.SC_CONFLICT,
 					"Problem occured with the SQL.");
 		}
 	}
 
 	/**
 	 * Will just pass request and response on to doGet.
 	 * 
 	 * @param request
 	 *            The request which contains the JSON
 	 * @param response
 	 *            The response that will be passed back.
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 
 	/**
 	 * This function takes in a reader and tries to read a JSON Object out of
 	 * it.
 	 * 
 	 * @param reader
 	 *            predefined object that should have JSON object in it.
 	 * @return The json object is successfully parsed.
 	 * @throws IOException
 	 * @throws JSONException
 	 */
 	private JSONObject getJSON(BufferedReader reader) throws IOException,
 			JSONException {
 		StringBuffer buffer = new StringBuffer();
 		String line = null;
 		JSONObject jsonObject;
 
 		while ((line = reader.readLine()) != null) {
 			buffer.append(line);
 		}
 		
 		System.out.println(buffer.toString());
 		
 		jsonObject = new JSONObject(buffer.toString());
 
 		return jsonObject;
 	}
 
 	/**
 	 * This function is responsible for preparing the data for the race and then
 	 * adding it to the database.
 	 * 
 	 * @param json
 	 *            This is the JSON object containing everything.
 	 * @throws ParseException
 	 * @throws JSONException
 	 * @throws SQLException
 	 */
 	private void createRace(JSONObject json) throws ParseException,
 			JSONException, SQLException {
 		String name = json.getString("name");
 
 		Calendar dateTime = advDateParse(json.getString("dateTime"));
 		String[] racers = advRacersParse(json.getJSONArray("racers"));
 		ItemObj[] items = advItemsParse(json.getJSONArray("items"));
 
 		// Starts adding things to the DB.
 
 		// Adds/makes sure user are in DB.
 		userModel.addUsers(racers);
 
 		// Creator is always the first racers in the list.
 		int creator = userModel.getUserID(racers[0]);
 
 		// Creates the new race in the race DB.
 		int raceID = raceModel.createRace(name, dateTime, creator);
 
 		// Adds the items in the race to the items DB.
 		itemModel.addItems(items, raceID);
 
 		// Adds the racers of the race to the racer DB.
 		racerModel.addRacers(racers, raceID);
 
 	}
 
 	/**
 	 * This is helper function that parses through the JSONArray and makes it
 	 * into a readable format that can be sent to the DB.
 	 * 
 	 * @param itemsArray
 	 *            The items in a JSON array.
 	 * @return The items in a ItemObj array.
 	 * @throws JSONException
 	 */
 	private ItemObj[] advItemsParse(JSONArray itemsArray) throws JSONException {
 		ItemObj[] itemList = new ItemObj[itemsArray.length()];
 
 		for (int i = 0; i < itemsArray.length(); i++) {
 			JSONObject item = itemsArray.getJSONObject(i);
 
 			itemList[i] = new ItemObj();
 			itemList[i].setLocation(item.getString("location"));
 			itemList[i].setType(Integer.parseInt(item.getString("type")));
 			itemList[i].setValue(Integer.parseInt(item.getString("value")));
 		}
 
 		return itemList;
 	}
 
 	/**
 	 * This helper function parses through the racers in the JSON Array and
 	 * returns their string names in just an array.
 	 * 
 	 * @param racersArray
 	 *            JSON array containing all the user names.
 	 * @return String array of the user names.
 	 * @throws JSONException
 	 */
 	private String[] advRacersParse(JSONArray racersArray) throws JSONException {
 		String[] racerList = new String[racersArray.length()];
 
 		for (int i = 0; i < racersArray.length(); i++) {
 			JSONObject racer = racersArray.getJSONObject(i);
 
 			racerList[i] = racer.getString("name");
 		}
 
 		return racerList;
 	}
 
 	/**
 	 * Helper function that formats the date object from string into a calendar
 	 * object.
 	 * 
 	 * @param dateStr
 	 *            The date as a string.
 	 * @return The date as a Calendar.
 	 * @throws ParseException
 	 */
 	private Calendar advDateParse(String dateStr) throws ParseException {
 		Calendar date = Calendar.getInstance();
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 
 		date.setTime(formatter.parse(dateStr));
 
 		return date;
 	}
 }
