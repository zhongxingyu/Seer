 /**
  * Database.java
  * 
  * Course: CS386
  * Term: Fall 2013
  * Assignment: Lab 4
  * Date: 10/21/13
  * Authors: Matthew Herman, Shannon Burns
  */
 
 package lab4;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  * Represents a database connection for the timeshare application.
  * Allows you to execute a defined set of operations against the
  * database that the app is connected to. This class will build
  * the queries, execute them, and output the results.
  * 
  * @author hermanm
  * @author burnssm
  */
 public class Database {
 	
 	/**
 	 * The driver used to connect to the database.
 	 */
 	private static String dbdriver = "com.mysql.jdbc.Driver";
 	
 	/**
 	 * A Connection object used to set certain properties for the
 	 * database connection.
 	 */
 	private Connection connection;
 	
 	/**
 	 * Constructor.
 	 * Ensures the database connection driver is available and connects
 	 * to a database based off of the provided information. If an error
 	 * occurs during connection, the application will exit.
 	 * @param dburl - URL of the database
 	 * @param dbname - name of the database
 	 * @param login - username for authenticating with the database
 	 * @param password - password related to the username provided
 	 */
 	public Database(String dburl, String dbname, String login, String password) {
 		try {
 			Class.forName(dbdriver);
 			System.out.println("\nConnecting as user '" + login + "' . . .\n");
 			connection = DriverManager.getConnection(dburl + "/" + dbname, login, password);
 			connection.setClientInfo("autoReconnect", "true");
 		} catch (ClassNotFoundException e) {
 			System.err.println("\nCould not load the JDBC driver. Exiting.");
 			System.exit(0);
 		} catch (SQLException e) {
 			System.err.println("\nFailed to connect to the database at " 
 								+ dburl + "/" + dbname + ". Exiting.");
 			if(connection != null) {
 				closeConnection();
 			}
 			System.exit(0);
 		}
 	}
 	
 	//TODO: Add methods for each query
 	
 	/**
 	 * Execute query #1. Loads data into the database from the user's file system.
 	 * @param filePath - the file that will be read from
 	 */
 	public void loadDataFile(String filePath) {
 		filePath = filePath.replace("\\", "/"); //fix common filepath mistake
 		String query1 = "delete from LoadData;";
 		String query2 = "load data local infile\n"
 						+ "'" + filePath + "'\n"
 						+ "into table LoadData fields terminated by '\t' lines\n"
 						+ "terminated by '\n';\n";
 		String query3 = "insert into person (fname, lname, phone)\n"
 						+ "select fname, lname, phone\n"
 						+ "from loaddata\n"
 						+ "group by fname, lname, phone;\n";
 		String query4 = "insert into unit (uid, name, maintenance_cost)\n"
 						+ "select uid, name, maintenance_cost\n"
 						+ "from loaddata\n"
 						+ "group by uid, name;\n";
 		String query5 = "insert into own_units (person_id, unit_number, week)\n"
 						+ "select p.pid, ld.uid, ld.week\n"
 						+ "from person p, loaddata ld\n"
 						+ "where p.fname=ld.fname and p.lname=ld.lname and p.phone=ld.phone\n"
 						+ "group by ld.week, ld.uid;";
 		Statement statement = null;
 		try {
 			statement = connection.createStatement();
 			statement.executeUpdate(query1);
 			int result = statement.executeUpdate(query2);
 			System.out.print("There were " + result + " records added to the database.\n");
 			statement.executeUpdate(query3);
 			statement.executeUpdate(query4);
 			statement.executeUpdate(query5);
 		} catch (SQLException e) {
 			System.err.println("\nQuery failed to execute.\n");
			e.printStackTrace();
 		} finally {
 			try {
 				if (statement != null) statement.close();
 			} catch (SQLException e) {
 				System.err.println("Encountered an error while closing the connection.");
 			}
 		}
 	}
 	
 	/**
 	 * Execute query #2, defined as 'List the names and phone numbers of the owners of all units 
 	 * in alphabetical order by last name, when two owners have the same last name, order by first name.'
 	 */
 	public void listOwners() {
 		String query = "select fname as 'First Name',lname as 'Last Name', phone as 'Phone'\n"
 						+ "from Person\n"
 						+ "group by fname, lname\n"
 						+ "order by lname, fname;";	
 		executeQuery(query);
 	}
 	
 	/**
 	 * Execute query #4, defined as 'Record with each unit the annual cost of maintenance for that unit. 
 	 * Prompt the user for the unit name and number for a particular unit and print out the share of the 
 	 * maintenance that each of the owners is responsible for. Have the owners displayed in alphabetical 
 	 * order. '
 	 * @param unitName the name of the selected unit
 	 * @param unitNumber the number of the selected unit
 	 */
 	public void listShareOfCost(String unitName, String unitNumber) {
 		String query = "select fname as 'First Name', lname as 'Last Name',CAST(count(pid)/ (select count(pid)\n"
 				+ "from Person p, Own_Units o, Unit u\n"
 				+ "where u.name = '" + unitName + "'\n"
 				+ "and u.uid = '" + unitNumber + "'\n"
 				+ "and u.uid = o.unit_number\n"
 				+ "and o.person_id = p.pid) * 100 as Decimal(9,2)) as Share\n"
 				+ "from Person p, Own_Units o, Unit u\n"
 				+ "where u.name = '" + unitName + "'\n"
 				+ "and u.uid = '" + unitNumber + "'\n"
 				+ "and u.uid = o.unit_number\n"
 				+ "and o.person_id = p.pid\n"
 				+ "group by fname, lname;";
 		executeQuery(query);
 	}
 	
 	/**
 	 * Executed query #6, defined as 'Prompt the user for a name and provide a unit names, numbers, and 
 	 * week numbers that that person owns. '
 	 * @param fname the first name of the person who info will be displayed
 	 * @param lname the last name of the person who info will be displayed
 	 */
 	public void listUsersInfo(String fname, String lname) {
 		String query = "select u.name as 'Unit Name', ou.week as 'Week Number' \n"
 				+ "from Unit u, Own_Units ou, Person p \n"
 				+ "where fname = '" + fname + "' \n"
 				+ "and lname = '" + lname + "' \n"
 				+ "and u.uid = ou.unit_number \n"
 				+ "and ou.person_id = p.pid \n"
 				+ "order by ou.week;";
 		executeQuery(query);
 	}
 	
 	/**
 	 * Executed query #8, defined as 'Prompt the user for a week number and display who owns each unit 
 	 * during that week. '
 	 * @param number the week number to be queried
 	 */
 	public void listUnitOwner(String number) {
 		String query = "select concat_ws(' ', p.fname, p.lname) as Name, u.name as 'Unit Name' \n"
 				+ "from Person p, Own_Units o, Unit u \n"
 				+ "where o.week = '" + number + "' \n"
 				+ "and o.unit_number = u.uid \n"
 				+ "and p.pid = o.person_id;";
 		executeQuery(query);
 	}
 	
 	
 	/**
 	 * Execute query #5, defined as 'Prompt the user for a unit name and report the names of the people who 
 	 * own one or more weeks in that unit and how many weeks they own.'
 	 * @param unitName - the unit that owners will be displayed for
 	 */
 	public void listOwnersOfUnit(String unitName) {
 		String query = "select concat_ws(' ', fname, lname) as Name, count(*) as 'Number of Weeks Owned'\n"
 						+ "from Unit u, Own_Units ou, Person p\n"
 						+ "where u.name='" + unitName + "' and ou.unit_number=u.uid and ou.person_id=p.pid\n"
 						+ "group by ou.person_id\n"
 						+ "having count(*)>=1;";
 		executeQuery(query);
 	}
 	
 	/**
 	 * Execute query #3, defined as 'Prompt the user for a unit name (e.g. Evergreen) and number (e.g. 3), and a 
 	 * minimum number of weeks owned. Present a list of the names of people who own 
 	 * at least that many weeks in the particular unit.'
 	 * @param unitName - name of the unit selected
 	 * @param unitNumber - number of the unit selected
 	 * @param minWeeks - the minimum number of weeks owned by an owner
 	 */
 	public void listOwnersOfUnitByMinimumWeeks(String unitName, int unitNumber, int minWeeks) {
 		String query = "select concat_ws(' ', p.fname, p.lname) as Name\n"
 						+ "from Unit u, Own_Units ou, Person p\n"
 						+ "where u.name='" + unitName + "' and u.uid=" + unitNumber 
 						+ " and ou.unit_number=u.uid and ou.person_id=p.pid\n"
 						+ "group by ou.person_id\n"
 						+ "having count(*)>=" + minWeeks + ";";
 		executeQuery(query);
 	}
 	
 	/**
 	 * Execute query #7, defined as 'Prompt the user for a unit name and number and display the owners in that unit 
 	 * sorted by week number. NOTE: A user that owns two or more non-consecutive 
 	 * weeks will appear at multiple places in the list.'
 	 * @param unitName - name of the unit selected
 	 * @param unitNumber - number of the unit selected
 	 */
 	public void listOwnersOfUnitByWeek(String unitName, int unitNumber) {
 		String query = "select concat_ws(' ', p.fname, p.lname) as Name, ou.week as Week\n"
 						+ "from Unit u, Own_Units ou, Person p\n"
 						+ "where u.name='" + unitName + "' and u.uid=" + unitNumber 
 						+ " and ou.unit_number=u.uid and ou.person_id=p.pid\n"
 						+ "order by ou.week;";
 		executeQuery(query);
 	}
 	
 	/**
 	 * Executes a given query against the currently connected database and
 	 * outputs the results. Will also close the connection created for the query.
 	 * @param query - the query to be executed
 	 */
 	private void executeQuery(String query) {
 		Statement statement = null;
 		ResultSet results = null;
 		
 		try {
 			statement = connection.createStatement();
 			results = statement.executeQuery(query);
 			outputResult(results);
 		} catch (SQLException e) {
 			System.err.println("\nQuery failed to execute.\n");
			e.printStackTrace();
 		} finally {
 			try {
 				if (statement != null) statement.close();
 				results.close();
 			} catch (SQLException e) {
 				System.err.println("Encountered an error while closing the connection.");
 			}
 		}
 	}
 	
 	/**
 	 * Will take in a ResultSet from a database call and output the data
 	 * with the appropriate column headers.
 	 * @param results - the results to be displayed
 	 * @throws SQLException
 	 */
 	private void outputResult(ResultSet results) throws SQLException {
 		ResultSetMetaData metadata = (ResultSetMetaData) results.getMetaData();
 		int columnCount = metadata.getColumnCount();
 		int rowCount = 0;
 		
 		// Output column headers
 		System.out.println();
 		for(int i=1; i<=columnCount; i++) {
 			System.out.printf("%-25s | ", metadata.getColumnLabel(i));
 		}
 		
 		// Output divider based on number of columns
 		System.out.println();
 		for(int i=1; i<=columnCount; i++) {
 			System.out.print("---------------------------");
 		}
 		
 		// Output the results
 		while(results.next()) {
 			System.out.println();
 			for(int i=1; i<=columnCount; i++) {
 				System.out.printf("%-25s | ", results.getString(metadata.getColumnLabel(i)));
 			}
 			rowCount++;
 		}
 		System.out.println("\n\nThere were " + rowCount + " records returned.");
 	}
 	
 	/**
 	 * Closes the connection to the database.
 	 */
 	public void closeConnection() {
 		try {
 			connection.close();
 		} catch (SQLException e) {
 			System.err.println("Failed to close the database connection.");
 		}
 	}
 }
