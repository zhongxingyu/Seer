 package com.futsall.db;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.futsall.city.ListOfCities;
 import com.futsall.company.Company;
 import com.futsall.country.ListOfCountries;
 import com.futsall.playGround.ListOfPlayGrounds;
 import com.futsall.playGround.PlayGround;
 import com.futsall.schedule.ListOfSchedules;
 import com.futsall.schedule.Schedule;
 import com.futsall.user.User;
 
 /**
  * The class manages connections to the database
  * 
  * @author Ivan
  * 
  */
 public class DBManager {
 	private static final String MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";
 
 	private static final String DATABASE_NAME = "minifootball";
 
 	private static String DB_USERNAME;
 
 	private static String DB_PASS;
	private static final String DB_PASS = "123";
 	
 	private static final Logger LOGGER = Logger.getLogger(DBManager.class.getName());
 	
 	private static final String CONFIG_FILE = 
 			"/com/futsall/configuration.properties";
 	
 	static {
 		Properties properties = new Properties();
 		InputStream is = DBManager.class.getResourceAsStream(CONFIG_FILE);
 		try {
 			properties.load(is);
 			is.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		DB_USERNAME = properties.getProperty("db.username");
 		DB_PASS = properties.getProperty("db.password");
 	}
 
 	private static final String SELECT_PLAYGROUNDS_BY_CITY = "" +
 			"SELECT c.id as companyId,c.name as companyName, c.address as companyAddress,"
 			+ "c.email as companyEmail, c.telephone as companyPhone, c.city as companyCity,"
 			+ "c.country as companyCountry, p.id as playGroundId, p.name as playGroundName,"
 			+ "p.address as playGroundAddress, p.telephone as playGroundPhone,"
 			+ "p.email as playGroundEmail, p.width as playGroundWidth,"
 			+ "p.Length as playGroundLength, p.flooring as playGroundFlooring,"
 			+ "p.city as playGroundCity, p.country as playGroundCountry "
 			+ "FROM minifootball.company as c "
 			+ "INNER JOIN  minifootball.playGround as p ON p.companyId = c.id "
 			+ "WHERE p.city = ? and p.country= ? ";
 
 	private static final String SELECT_PlAYGROUND_SCHEDULE_BY_DATE =
 			"SELECT r.startTime,r.endTime,u.id,u.userName,u.pass,u.firstName,u.lastName,"+
 					"u.telephone,u.address, u.email "+
 			"FROM minifootball.reserved r "+
 			"INNER JOIN minifootball.playGround p ON p.id = r.playGroundId "+
 			"INNER JOIN minifootball.userprofile u ON u.id = r.userId "+
 			"WHERE r.startTime LIKE CONCAT(?,'%') and p.id=? "+
 			"ORDER BY r.startTime";
 
 	private static final String SELECT_ALL_COUNTRIES = 
 			"SELECT DISTINCT p.country as country FROM minifootball.playground p;";
 
 	private static final String SELECT_CITIES_BY_COUNTRY = 
 			"SELECT DISTINCT p.city as city FROM minifootball.playground p WHERE p.country =?";
 	
 	private static final String SELECT_USER_BY_USERNAME_AND_PASS = 
 			"SELECT u.id,u.userName,u.pass,u.firstName,u.lastName,u.telephone,u.address,u.email "+
 			"FROM minifootball.userprofile u " +
 			"WHERE u.username = ? AND u.pass = ?";
 	
 	private static final String SELECT_EXISTING_SCHEDULES =
 			"SELECT COUNT(*) as rowcnt FROM minifootball.reserved r "+
 			"WHERE r.playGroundId=? AND "+
 			"((r.startTime<=? AND r.endTime>?) OR "+
 			"(r.startTime<? AND r.endTime>=?) OR " +
 			"(r.startTime>=? AND r.endTime<=?)) ";	
 	
 	private static final String RESERVE_PLAYGROUND =
 			"INSERT INTO minifootball.reserved(userId,playGroundId,startTime,endTime)"+
 			"VALUES(?,?,?,?)";
 	
 	private static final String INSERT_USER = "INSERT INTO userprofile(username, pass, firstName, lastName, telephone, " +
 			"email, address) VALUES(?, ?, ?, ?, ?, ?, ?)";
 
 	private Connection connection;
 
 	private PreparedStatement preparedStatement;
 
 	private ResultSet resultSet;
 
 	public DBManager() {
 		openConnection();
 		LOGGER.setLevel(Level.INFO);
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		if (connection != null) {
 			closeConnection();
 		}
 
 		super.finalize();
 	}
 
 	/**
 	 * The method returns the playgrounds for a given country and city
 	 * 
 	 * @param inCity
 	 *            the city
 	 * @param inCountry
 	 *            the country
 	 * @return the playgrounds for the selected country and city
 	 */
 	public ListOfPlayGrounds getPlayGrounds(String inCity, String inCountry) {
 		ListOfPlayGrounds resultList = new ListOfPlayGrounds();
 		List<PlayGround> playGrounds = new ArrayList<>();
 
 		try {
 			preparedStatement = connection
 					.prepareStatement(SELECT_PLAYGROUNDS_BY_CITY);
 			preparedStatement.setString(1, inCity);
 			preparedStatement.setString(2, inCountry);
 			resultSet = preparedStatement.executeQuery();
 			readPlayGrounds(playGrounds);
 			resultList.setPlayGrounds(playGrounds);
 		} catch (SQLException sqle) {
 			LOGGER.log(Level.WARNING,sqle.getMessage());
 		}
 
 		return resultList;
 	}
 
 	/**
 	 * The method returns the schedule for a playground on a given date
 	 * 
 	 * @param inDate
 	 *            the selected date
 	 * @param inPlayGroundId
 	 *            the selected playground
 	 * @return the schedule for the selected date and playground
 	 */
 	public ListOfSchedules getSchedule(Date inDate, int inPlayGroundId) {
 		ListOfSchedules resultList = new ListOfSchedules();
 		List<Schedule> schedules = new ArrayList<>();
 
 		try {
 			preparedStatement = connection
 					.prepareStatement(SELECT_PlAYGROUND_SCHEDULE_BY_DATE);
 			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 			String formatedDate = sdf.format(inDate.getTime());
 			preparedStatement.setString(1, formatedDate);
 			preparedStatement.setInt(2, inPlayGroundId);
 			resultSet = preparedStatement.executeQuery();
 			readSchedules(schedules,inPlayGroundId);
 			resultList.setSchedules(schedules);
 		} catch (SQLException sqle) {
 			LOGGER.log(Level.WARNING,sqle.getMessage());
 		} catch (ParseException pe) {
 			LOGGER.log(Level.WARNING,pe.getMessage());
 		}
 
 		return resultList;
 	}
 
 	/**
 	 * Gets all the countries in the database
 	 * 
 	 * @return the available countries in the database
 	 */
 	public ListOfCountries getAllCountries() {
 		ListOfCountries resultList = new ListOfCountries();
 		List<String> countries = new ArrayList<>();
 
 		try {
 			Statement statement = connection.createStatement();
 			resultSet = statement.executeQuery(SELECT_ALL_COUNTRIES);
 			readCountries(countries);
 			resultList.setCountries(countries);
 		} catch (SQLException sqle) {
 			LOGGER.log(Level.WARNING,sqle.getMessage());
 		}
 
 		return resultList;
 	}
 
 	/**
 	 * The method extracts all the cities for a specified country
 	 * 
 	 * @param inCoutryName the country name
 	 * @return all the cities for the given country
 	 */
 	public ListOfCities getCities(String inCoutryName) {
 		ListOfCities resultList = new ListOfCities();
 		List<String> cities = new ArrayList<>();
 
 		try {
 			preparedStatement = connection
 					.prepareStatement(SELECT_CITIES_BY_COUNTRY);
 			preparedStatement.setString(1, inCoutryName);
 			resultSet = preparedStatement.executeQuery();
 			readCities(cities);
 			resultList.setCities(cities);
 		} catch (SQLException sqle) {
 			LOGGER.log(Level.WARNING,sqle.getMessage());
 		}
 		
 		return resultList;
 	}
 	
 	/**
 	 * The method checks if a user with the specified username and password exists
 	 * 
 	 * @param username the user's username
 	 * @param password the user's password
 	 * @return true if the user is valid, false otherwise
 	 */
 	public User getValidUser(String username, String password)
 	{
 		User resultUser = null;
 		try {
 			preparedStatement = connection
 					.prepareStatement(SELECT_USER_BY_USERNAME_AND_PASS);
 			preparedStatement.setString(1, username);
 			preparedStatement.setString(2, password);
 			resultSet = preparedStatement.executeQuery();
 			
 			resultUser = readUser(); 
 
 		} catch (SQLException sqle) {
 			LOGGER.log(Level.WARNING,sqle.getMessage());
 		}
 
 		return resultUser;
 	}
 	
 	/**
 	 * The method adds a new user to the database
 	 * 
 	 * @return true if the user was added successfully, false otherwise
 	 */
 	public boolean addNewUser(String username, String password, String firstName, String lastName,
 			String telephone, String email, String address)
 	{
 		boolean result = false;
 		
 		try {
 			preparedStatement = connection
 					.prepareStatement(INSERT_USER);
 			preparedStatement.setString(1, username);
 			preparedStatement.setString(2, password);
 			preparedStatement.setString(3, firstName);
 			preparedStatement.setString(4, lastName);
 			preparedStatement.setString(5, telephone);
 			preparedStatement.setString(6, email);
 			preparedStatement.setString(7, address);
 			preparedStatement.executeUpdate();
 			result = true;
 		} catch (SQLException sqle) {
 			LOGGER.log(Level.WARNING,sqle.getMessage());
 		}
 		
 		return result;
 	}
 
 	/**
 	 * The method is used to reserve a playground for a given time
 	 * 
 	 * @param inScheduleToReserve contains information about the playground, time and user
 	 * @return true if reservation is successful and false if the playground has been already reserved
 	 * for the specified time
 	 * @throws SQLException the exception occurs when there is an error while
 	 * searching or adding into the database 
 	 */
 	public boolean reservePlayground(Schedule inScheduleToReserve) 
 			throws SQLException {
 		if(!checkTimeInfoCorrectness(inScheduleToReserve)) {
 			return false;
 		}
 		
 		preparedStatement = connection.prepareStatement(RESERVE_PLAYGROUND);
 		preparedStatement.setInt(1, inScheduleToReserve.getUser().getId());
 		preparedStatement.setInt(2, inScheduleToReserve.getPlayGroundId());
 		
 		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String startTime = sdf.format(inScheduleToReserve.getStartTime().getTime());
 		String endTime = sdf.format(inScheduleToReserve.getEndTime().getTime());
 		preparedStatement.setString(3, startTime);
 		preparedStatement.setString(4, endTime);
 		
 		preparedStatement.executeUpdate();
 		
 		return true;
 	}
 	
 	/**
 	 * The method checks the correctness of the time information in the schedule
 	 * to be reserved
 	 * 
 	 * @param inScheduleToReserve the schedule to be reserved
 	 * @return true if the playground is reserved in the given time intrerval
 	 * @throws SQLException if an error occurs while searching in the database
 	 */
 	private boolean checkTimeInfoCorrectness(Schedule inScheduleToReserve) 
 			throws SQLException {
 		preparedStatement = connection
 				.prepareStatement(SELECT_EXISTING_SCHEDULES);
 		preparedStatement.setInt(1, inScheduleToReserve.getPlayGroundId());
 		
 		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String startTime = sdf.format(inScheduleToReserve.getStartTime().getTime());
 		String endTime = sdf.format(inScheduleToReserve.getEndTime().getTime());
 		preparedStatement.setString(2, startTime);
 		preparedStatement.setString(3, startTime);
 		preparedStatement.setString(4, endTime);
 		preparedStatement.setString(5, endTime);
 		preparedStatement.setString(6, startTime);
 		preparedStatement.setString(7, endTime);
 		
 		resultSet = preparedStatement.executeQuery();
 		
 		resultSet.next();
 		int rowsCnt = resultSet.getInt("rowcnt");
 		resultSet.close();
 		
 		return rowsCnt==0;
 	}
 	
 	private User readUser() throws SQLException{
 		User newUser = new User();
 		
 		while(resultSet.next()) {
 			newUser.setId(resultSet.getInt("id"));
 			newUser.setAddress(resultSet.getString("address"));
 			newUser.setEmail(resultSet.getString("email"));
 			newUser.setFirstName(resultSet.getString("firstName"));
 			newUser.setLastName(resultSet.getString("lastName"));
 			newUser.setTelephone(resultSet.getString("telephone"));
 			newUser.setUsername(resultSet.getString("userName"));
 		}
 		
 		return newUser;
 	}
 	
 	private List<String> readCities(List<String> inCities)
 		throws SQLException{
 		while(resultSet.next()) {
 			inCities.add(resultSet.getString("city"));
 		}
 		
 		return inCities;
 	}
 	
 	private List<String> readCountries(List<String> inCountries)
 			throws SQLException {
 		while (resultSet.next()) {
 			inCountries.add(resultSet.getString("country"));
 		}
 
 		return inCountries;
 	}
 
 	private void openConnection(String inDbName, String inUsername,
 			String inPassword) {
 		try {
 			Class.forName(MYSQL_DRIVER_CLASS);
 
 		} catch (ClassNotFoundException cnfe) {
 			System.out.println(cnfe.getStackTrace());
 		}
 
 		try {
 			connection = DriverManager.getConnection("jdbc:mysql://localhost/"
 					+ inDbName, inUsername, inPassword);
 		} catch (SQLException sqle) {
 			System.out.println(sqle.getStackTrace());
 		}
 	}
 
 	private void openConnection() {
 		openConnection(DATABASE_NAME, DB_USERNAME, DB_PASS);
 	}
 
 	private void closeConnection() {
 		try {
 			if (resultSet != null) {
 				resultSet.close();
 			}
 
 			if (preparedStatement != null) {
 				preparedStatement.close();
 			}
 
 			if (connection != null) {
 				connection.close();
 			}
 		} catch (SQLException sqle) {
 			System.out.println(sqle.getStackTrace());
 		}
 	}
 
 	private List<Schedule> readSchedules(List<Schedule> inSchedules,int inPlaygroundId)
 			throws SQLException, ParseException {
 		while (resultSet.next()) {
 			Schedule schedule = new Schedule();
 			User user = new User();
 			user.setId(resultSet.getInt("id"));
 			user.setUsername(resultSet.getString("userName"));
 			user.setFirstName(resultSet.getString("firstName"));
 			user.setLastName(resultSet.getString("lastName"));
 			user.setEmail(resultSet.getString("email"));
 			user.setAddress(resultSet.getString("address"));
 			user.setTelephone(resultSet.getString("telephone"));
 			schedule.setUser(user);
 			schedule.setPlayGroundId(inPlaygroundId);
 			DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
 			schedule.setStartTime(df.parse(resultSet.getString("startTime")));
 			schedule.setEndTime(df.parse(resultSet.getString("endTime")));
 
 			inSchedules.add(schedule);
 		}
 
 		return inSchedules;
 	}
 
 	private List<PlayGround> readPlayGrounds(List<PlayGround> inPlayGrounds)
 			throws SQLException {
 		while (resultSet.next()) {
 			Company company = new Company();
 			company.setId(resultSet.getInt("companyId"));
 			company.setName(resultSet.getString("companyName"));
 			company.setAddress(resultSet.getString("companyAddress"));
 			company.setEmail(resultSet.getString("companyEmail"));
 			company.setTelephone(resultSet.getString("companyPhone"));
 			company.setCity(resultSet.getString("companyCity"));
 			company.setCountry(resultSet.getString("companyCountry"));
 
 			PlayGround playGround = new PlayGround();
 			playGround.setId(resultSet.getInt("playGroundId"));
 			playGround.setCompany(company);
 			playGround.setName(resultSet.getString("playGroundName"));
 			playGround.setAddress(resultSet.getString("playGroundAddress"));
 			playGround.setTelephone(resultSet.getString("playGroundPhone"));
 			playGround.setEmail(resultSet.getString("playGroundEmail"));
 			playGround.setWidth(resultSet.getDouble("playGroundWidth"));
 			playGround.setLength(resultSet.getDouble("playGroundLength"));
 			playGround.setFlooring(resultSet.getString("playGroundFlooring"));
 			playGround.setCity(resultSet.getString("playGroundCity"));
 			playGround.setCountry(resultSet.getString("playGroundCountry"));
 
 			inPlayGrounds.add(playGround);
 		}
 
 		return inPlayGrounds;
 	}
 }
