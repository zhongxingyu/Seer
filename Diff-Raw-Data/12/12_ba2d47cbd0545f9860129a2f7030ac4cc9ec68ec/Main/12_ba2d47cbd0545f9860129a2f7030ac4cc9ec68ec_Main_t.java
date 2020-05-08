 
 package cs122bp1;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.*;                              // Enable SQL processing
 
 /**
  *
  * @author tonkpils
  */
 public class Main {
 
     static Connection connection;
     static boolean isLoggedIn;
     static boolean exit;
 
     public static void main(String[] args) throws Exception {
 
         /*When this program is run, the user is asked for the the user name
         and the user password (the database user login info not the password
         in the above schema) . If all is well, the employee is granted access
         (and a message to that effect appears on the screen); if access is not
         allowed, it says why (e.g., the database is not present, the password
         is wrong). Allow a way for the employee to exit easily. */
         
         setup();
         
         while (!exit)
         {
         	if (isLoggedIn)
         		mainMenu();
         	else
         		login();
         }
     }
 
 // LOG IN/OUT {{{1
     private static void login() throws Exception {
         //TODO exit w/o logging in
         System.out.println("\nPlease log in.\n");
 
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
         String username = null;
         String password = null;
         String server = "localhost";
 
         String readLine = "";
         boolean loginScreen = true;
 
         while (loginScreen) {
             int menuOpt = -1;
             System.out.print("\n\n"
                     + "Server: " + server + "\n\n"
                     + "1) Change server\n"
                     + "2) Login\n"
                     + "0) Exit\n"
                     + ":");
             try {
                 readLine = br.readLine();
                 menuOpt = Integer.valueOf(readLine);
             } catch (IOException ioe) {
                 System.out.println("Invalid Input!");
                 pause();
             } catch (NumberFormatException ex) {
                 System.out.println("Not a valid number: " + readLine);
                 pause();
             }
 
             switch (menuOpt) {
                 case 0:
                     exit = true;
                     return;
                 case 1:
                     System.out.print("\nServer: ");
                     server = br.readLine();
                     break;
                 case 2:
                     System.out.print("\nUsername: ");
                     username = br.readLine();
                     System.out.print("\nPassword: ");
                     password = br.readLine();
                     loginScreen = false;
                     break;
                 default:
                     break;
             }
         }
 
         try {
             // Connect to the test database
             connection = DriverManager.getConnection("jdbc:mysql://" + server + ":3306/moviedb", username, password);
             isLoggedIn = true;
             System.out.println("\n\nWelcome, " + username + ".");
         } catch (SQLException ex) {
             printSQLError(ex);
         }
     }
 
     private static void logout() {
     	isLoggedIn = false;
     	System.out.println("You are now logged out.");
         try {
             connection.close();
         } catch (SQLException ex) {
             printSQLError(ex);
         }
     }
 // }}}
 // MAIN MENU {{{
     private static void mainMenu() {
         while (true) {
             System.out.print("\n\n\n\n=== Menu ===\n"
                     + "1) Search stars by name\n"
                     + "2) Search stars by ID\n"
                     + "3) Add star to database\n"
                     + "4) Add customer to database\n"
                     + "5) Delete customer from database\n"
                     + "6) Metadata of database\n"
                     + "7) SQL query\n"
                     + "8) Log out\n"
                     + "0) Exit\n"
                     + "______________________________________\n"
                     + ":");
 
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             String readLine = null;
             int menuChoice = -1;
 
             try {
                 readLine = br.readLine();
                 System.out.println(readLine);
                 menuChoice = Integer.parseInt(readLine);
             } catch (IOException ioe) {
                 System.out.println("Invalid Input!");
                 pause();
             } catch (NumberFormatException ex) {
                 System.out.println("Not a valid number: " + readLine);
                 pause();
             }
 
             switch (menuChoice) {
                 case 1:
                     searchStarNames();
                     pause();
                     break;
                 case 2:
                     searchStarIDs();
                     pause();
                     break;
                 case 3:
                     addStarMenu();
                     break;
                 case 4:
                     addCustomerMenu();
                     break;
                 case 5:
                     deleteCustomerMenu();
                     break;
                 case 6:
                     getMetadata();
                     pause();
                     break;
                 case 7:
                     //TODO openQueryMenu();
                     openQueryMenu();
                     pause();
                     break;
                 case 8:
                     logout();
                     return;
                 case 0:
                     exit();
                     return;
                 case -1:
                     continue;
                 default:
                     System.out.println("Not a valid menu option.");
                     pause();
                     break;
             }
         }
     }
 // }}}    
 // SETUP/CLEANUP {{{
     private static void setup()
     {
         exit = false;
         isLoggedIn = false;
         // Incorporate mySQL driver
         try {
             Class.forName("com.mysql.jdbc.Driver").newInstance();
         } catch (Exception e) {
         }
     }
     
     private static void exit() {
     	exit = true;
         try {
             connection.close();
         } catch (SQLException e) {
             printSQLError(e);
         }
         System.out.println("Goodbye.");
     }
 // }}}
 // STAR {{{
     //=== Search Stars
     /*Print out (to the screen) the movies featuring a given star.
     All movie attributes should appear, labeled and neatly arranged;
     the star can be queried via first name and/or last name or by ID.
     First name and/or last name means that a star should be queried by
     both a) first name AND last name b) first name or last name. */
     private static void searchStarNames() {
 
         int searchBy = -1; //0 = any; 1 = first name; 2 = last name
         boolean notSelected = true;
         String readLine = "";
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
         while (notSelected) {
 
             System.out.print("\n\n\nSearch By:\n"
                     + "0) All Names\n"
                     + "1) First Name\n"
                     + "2) Last Name\n"
                     + "______________________________________\n"
                     + ":");
 
             try {
                 searchBy = Integer.valueOf(br.readLine());
             } catch (IOException ioe) {
                 System.out.println("Invalid Input!");
                 pause();
             } catch (NumberFormatException ex) {
                 System.out.println("Not a valid number: " + readLine);
                 pause();
             }
 
             if (searchBy < 0 || searchBy > 2) {
                 notSelected = true;
             } else {
                 notSelected = false;
             }
         }
 
         System.out.print("\n\n\nEnter search term: ");
 
         readLine = null;
 
         try {
             readLine = br.readLine();
         } catch (IOException ioe) {
             System.out.println("Invalid Input!");
             pause();
         }
 
 
         ResultSet result;
         try {
             if (searchBy == 0) {
                 result = queryStarNames(readLine);
             } else if (searchBy == 1) {
                 result = queryStarFirstName(readLine);
             } else {
                 result = queryStarLastName(readLine);
             }
             System.out.println("\nThe results of the query\n");
             printStars(result);
 
         } catch (SQLException ex) {
             printSQLError(ex);
         }
     }
 
     private static ResultSet queryStarNames(String readLine) throws SQLException {
         //Search by name, returns if found in first or last name inclusive
         Statement select = connection.createStatement();
         ResultSet result = select.executeQuery("Select * from stars WHERE (first_name = '" + readLine + "' OR last_name = '" + readLine + "' )");
         return result;
     }
 
     private static ResultSet queryStarFirstName(String readLine) throws SQLException {
         //Search by name, returns if found in first or last name inclusive
         Statement select = connection.createStatement();
         ResultSet result = select.executeQuery("Select * from stars WHERE (first_name = '" + readLine + "')");
         return result;
     }
 
     private static ResultSet queryStarLastName(String readLine) throws SQLException {
         //Search by name, returns if found in first or last name inclusive
         Statement select = connection.createStatement();
         ResultSet result = select.executeQuery("Select * from stars WHERE (last_name = '" + readLine + "' )");
         return result;
     }
 
     private static void searchStarIDs() {
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String readLine = null;
         Integer starID = 0;
 
         while (starID == 0) {
             System.out.print("\n\n\nEnter star ID: ");
             try {
                 readLine = br.readLine();
                 starID = Integer.valueOf(readLine);
             } catch (IOException ioe) {
                 System.out.println("Invalid Input!");
                 pause();
             } catch (NumberFormatException ex) {
                 System.out.println("Not a valid number: " + readLine);
                 pause();
             }
         }
 
         ResultSet result;
         try {
             result = queryStarID(starID);
             System.out.println("\nThe results of the query\n");
             printStars(result);
 
         } catch (SQLException ex) {
             printSQLError(ex);
         }
     }
 
     private static ResultSet queryStarID(int readLine) throws SQLException {
         //Search by star ID
         Statement select = connection.createStatement();
         ResultSet result = select.executeQuery("Select * from stars WHERE (id = " + readLine + " )");
         return result;
     }
 
     //=== Add Star
     /*Insert a new star into the database. If the star has a single name,
     add it as his last_name and assign an empty string ("") to first_name. */
     private static void addStarMenu() {
         int id = 0;
         String firstName = "";
         String lastName = "";
         String dob = "0000/00/00";
         String imageURL = "";
 
         while (true) {
             System.out.print("\n\n\n\n=== Add Star Menu ===\n"
                     + "\n"
                     + "ID (0=Auto): " + id + "\n"
                     + "First Name : " + firstName + "\n"
                     + "Last Name  : " + lastName + "\n"
                     + "D.O.B.     : " + dob + "\n"
                     + "Image URL  : " + imageURL + "\n"
                     + "\n"
                     + "1) Set ID\n"
                     + "2) Set First and Last Name\n"
                     + "3) Set Single Name\n"
                     + "4) Set Date of Birth\n"
                     + "5) Set Image URL\n"
                     + "6) Add Star\n"
                     + "0) Cancel\n"
                     + "______________________________________\n"
                     + ":");
 
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             String readLine = null;
             int menuChoice = -1;
 
             try {
                 readLine = br.readLine();
                 menuChoice = Integer.parseInt(readLine);
 
 
                 switch (menuChoice) {
                     case 1:
                         //TODO star ID editable?
                         System.out.print("Enter ID:");
                         readLine = br.readLine();
                         id = Integer.parseInt(readLine);
                         break;
                     case 2:
                         firstName = "";
                         lastName = "";
                         while (firstName.isEmpty()) {
                             System.out.print("Enter First Name:");
                             firstName = br.readLine();
                         }
                         while (lastName.isEmpty()) {
                             System.out.print("Enter Last Name:");
                             lastName = br.readLine();
                         }
                         break;
                     case 3:
                         lastName = "";
                         while (lastName.isEmpty()) {
                             System.out.print("Enter Name:");
                             lastName = br.readLine();
                         }
                         firstName = "";
                         break;
                     case 4:
                         //TODO must match format YYYY/MM/DD
                         System.out.print("Enter Date of Birth (YYYY/MM/DD):");
                         dob = br.readLine();
                         break;
                     case 5:
                         System.out.print("Enter Image URL:");
                         imageURL = br.readLine();
                         break;
                     case 6:
                         int added = 0;
 
                         if (canAddStar(id, firstName, lastName, dob, imageURL)) {
                             added = addStar(id, firstName, lastName, dob, imageURL);
                         }
 
                         if (added != 0) {
                             System.out.println("______________________________________\n"
                                     + firstName + " " + lastName + " successfully added\n"
                                     + "______________________________________");
                             pause();
                         } else {
                             System.out.println("______________________________________\n"
                                     + firstName + " " + lastName + " NOT added\n"
                                     + "______________________________________");
                             pause();
                             break;
                         }
                         return;
                     case 0:
                         return;//Exit menu
                     default:
                         System.out.println("Not a valid menu option.");
                         pause();
                         break;
                 }
 
             } catch (IOException ioe) {
                 System.out.println("Invalid Input!");
                 pause();
             } catch (NumberFormatException ex) {
                 System.out.println("Not a valid number: " + readLine);
                 pause();
             }
 
 
 
         }
     }
 
     private static boolean canAddStar(Integer id, String firstName, String lastName, String dob, String imgURL) {
 
         if (firstName.isEmpty() && lastName.isEmpty()) {
             System.out.println("Star must have a name.");
             pause();
             return false;
         }
         if (!firstName.isEmpty() && lastName.isEmpty()) {
             System.out.println("Improperly formatted single name.\n"
                     + "A single name must be put in the last name field.");
             pause();
             return false;
         }
 
         //TODO validate dob format
 
         try {
             ResultSet result = queryStarID(id);
             if (result.next()) {
                 System.out.println("\n\nStar ID already exists.");
                 return false;
             }
         } catch (SQLException ex) {
             printSQLError(ex);
             return false;
         }
 
         return true;
     }
 
     private static int addStar(Integer id, String firstName, String lastName, String dob, String imgURL) {
         try {
             Statement update = connection.createStatement();
             int retID = update.executeUpdate("INSERT INTO stars VALUES("
                     + id + ", '"
                     + firstName + "', '"
                     + lastName + "', DATE('"
                     + dob + "'), '"
                     + imgURL + "');");
             return retID;
         } catch (SQLException ex) {
             printSQLError(ex);
         }
         return 0;
     }
 
 // }}}
 // CUSTOMER {{{
     //=== Add Customer
     /*Insert a customer into the database. Do not allow insertion of a customer
     if his credit card does not exist in the credit card table. The credit
     card table simulates the bank records. If the customer has a single name,
     add it as his last_name and and assign an empty string ("") to first_name. */
     private static void addCustomerMenu() {
         int id = 0;
         String firstName = "";
         String lastName = "";
         String cc_id = "";
         String address = "";
         String email = "";
         String password = "";
 
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
         while (true) {
             System.out.print("\n\n\n\n=== Add Customer Menu ===\n"
                     + "\n"
                     + "ID (0=Auto): " + id + "\n"
                     + "First Name : " + firstName + "\n"
                     + "Last Name  : " + lastName + "\n"
                     + "Credit Card: " + cc_id + "\n"
                     + "Address    : " + address + "\n"
                     + "E-Mail     : " + email + "\n"
                     + "Password   : " + password + "\n"
                     + "\n"
                     + "1) Set Customer ID\n"
                     + "2) Set First and Last Name\n"
                     + "3) Set Single Name\n"
                     + "4) Set Credit Card Number\n"
                     + "5) Set Address\n"
                     + "6) Set Email\n"
                     + "7) Set Password\n"
                     + "8) Add Customer\n"
                     + "0) Cancel\n"
                     + "______________________________________\n"
                     + ":");
 
             String readLine = null;
             int menuChoice = -1;
 
             try {
                 readLine = br.readLine();
                 menuChoice = Integer.parseInt(readLine);
 
 
                 switch (menuChoice) {
                     case 1:
                         //TODO customer ID editable?
                         System.out.print("Enter ID:");
                         readLine = br.readLine();
                         id = Integer.parseInt(readLine);
                         break;
                     case 2:
                         firstName = "";
                         lastName = "";
                         while (firstName.isEmpty()) {
                             System.out.print("Enter First Name:");
                             firstName = br.readLine();
                         }
                         while (lastName.isEmpty()) {
                             System.out.print("Enter Last Name:");
                             lastName = br.readLine();
                         }
                         break;
                     case 3:
                         lastName = "";
                         while (lastName.isEmpty()) {
                             System.out.print("Enter Name:");
                             lastName = br.readLine();
                         }
                         firstName = "";
                         break;
                     case 4:
                         System.out.print("Enter Credit Card Number:");
                         cc_id = br.readLine();
                         break;
                     case 5:
                         System.out.print("Enter Address:");
                         address = br.readLine();
                         break;
                     case 6:
                         System.out.print("Enter Email:");
                         email = br.readLine();
                         break;
                     case 7:
                         System.out.print("Enter Password:");
                         password = br.readLine();
                         break;
                     case 8:
                         int added = 0;
 
                         if (canAddCustomer(id, firstName, lastName, cc_id, address, email, password)) {
                             added = addCustomer(id, firstName, lastName, cc_id, address, email, password);
                         }
 
                         if (added != 0) {
                             System.out.println("______________________________________\n"
                                     + firstName + " " + lastName + " successfully added\n"
                                     + "______________________________________");
                             pause();
                         } else {
                             System.out.println("\n"
                                     + firstName + " " + lastName + " NOT added\n"
                                     + "______________________________________");
                             pause();
                             break;
                         }
                         return;
                     case 0:
                         return;//Exit menu
                     default:
                         System.out.println("Not a valid menu option.");
                         pause();
                         break;
                 }
 
             } catch (NumberFormatException ex) {
                 System.out.println("Not a valid number: " + readLine);
             } catch (IOException ioe) {
                 System.out.println("Invalid Input!");
             }
         }
         
     }
 
     private static boolean canAddCustomer(int id, String firstName, String lastName, String cc_id, String address, String email, String password) {
 
         if (firstName.isEmpty() && lastName.isEmpty()) {
             System.out.println("Customer must have a name.");
             pause();
             return false;
         }
         if (!firstName.isEmpty() && lastName.isEmpty()) {
             System.out.println("Improperly formatted single name.\n"
                     + "A single name must be put in the last name field.");
             pause();
             return false;
         }
         if (address.isEmpty()) {
             System.out.println("Customer must have an address.");
             pause();
             return false;
         }
         if (email.isEmpty()) {
             System.out.println("Customer must have an e-mail address.");
             pause();
             return false;
         }
         if (password.isEmpty()) {
             System.out.println("Customer must have a password.");
             pause();
             return false;
         }
 
         try {
             //TODO check unique customer ID
             //SELECT * FROM customers c WHERE id =
             Statement select = connection.createStatement();
             ResultSet result = select.executeQuery("SELECT * FROM customers c "
                     + "WHERE id = " + cc_id + ";");
             if (result.next()) {
                 System.out.println("\n\nInvalid customer ID number.\n"
                         + "Already in use.");
                 return false;
             }
 
 
             select = connection.createStatement();
             result = select.executeQuery("SELECT * FROM creditcards c "
                     + "WHERE id = '" + cc_id + "';");
             if (!result.next()) {
                 System.out.println("\n\nInvalid credit card number.");
                 return false;
             }
 
             //TODO remove spaces from cc_id on input?
             //TODO do we have to match the name on file to the customer name?
 
         } catch (SQLException ex) {
             printSQLError(ex);
             return false;
         }
 
         return true;
     }
 
     private static int addCustomer(int id, String firstName, String lastName, String cc_id, String address, String email, String password) {
         try {
             Statement update = connection.createStatement();
             //TODO compare provided name and cc_id to database records
             int retID = update.executeUpdate("INSERT INTO customers VALUES("
                     + id + ", '"
                     + firstName + "', '"
                     + lastName + "', '"
                     + cc_id + "', '"
                     + address + "','"
                     + email + "','"
                     + password + "');");
             return retID;
         } catch (SQLException ex) {
             printSQLError(ex);
         }
         return 0;
     }
 
     //=== Delete Customer
     /*Delete a customer from the database. */
     private static void deleteCustomerMenu() {
 
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String readLine = null;
         Integer customerID = 0;
 
         while (customerID == 0) {
             System.out.print("\n\n\nEnter customer ID: ");
             try {
                 readLine = br.readLine();
                 customerID = Integer.valueOf(readLine);
             } catch (IOException ioe) {
                 System.out.println("Invalid Input!");
                 pause();
             } catch (NumberFormatException ex) {
                 System.out.println("Not a valid number: " + readLine);
                 pause();
             }
         }
 
         ResultSet result;
         boolean resultsFound = false;
 
         try {
             result = queryCustomerID(customerID);
             System.out.println("\nThe results of the query\n");
             resultsFound = printCustomers(result);
 
         } catch (SQLException ex) {
             printSQLError(ex);
         }
 
         if (resultsFound) {
             int deleted = 0;
             System.out.print("Are you sure you want to delete this customer?\n"
                     + "(y/N):");
             try {
                 readLine = br.readLine();
                 if ((!readLine.isEmpty())
                         && (readLine.substring(0, 1).equals("Y")
                         || readLine.substring(0, 1).equals("y"))) {//check for y/Y
 
                     deleted = deleteCustomerID(customerID);// Attempt to delete
 
                     if (deleted != 0) {
                         System.out.println("______________________________________\n"
                                 + customerID + " successfully deleted\n"
                                 + "______________________________________");
                         pause();
                         return;
                     }
                 }
 
                 System.out.println("\n\n"
                         + customerID + " NOT deleted\n"
                         + "______________________________________");
                 pause();
 
             } catch (IOException ioe) {
                 System.out.println("Invalid Input!");
                 pause();
             }
         } else {
             pause();
         }
     }
 
     private static ResultSet queryCustomerID(int id) throws SQLException {
         Statement select = connection.createStatement();
         ResultSet result = select.executeQuery("Select * from customers WHERE (id = " + id + " )");
         return result;
     }
 
     private static int deleteCustomerID(int id) {
         int retID = 0;
         try {
             Statement update = connection.createStatement();
             retID = update.executeUpdate("DELETE FROM customers WHERE id = " + id);
             return retID;
         } catch (SQLException ex) {
             printSQLError(ex);
             pause();
         }
         return retID;
     }
 // }}}
// METADATA {{{
     //=== Get Metadata
     /*Provide the metadata of the database; in particular, print out the name
     of each table and, for each table, each attribute and its type. */
     private static void getMetadata() {
         boolean step = false;
         try {
             // SQL statement to select all tables
             Statement select = connection.createStatement();
             Statement tableQuery = connection.createStatement();
             ResultSet tableList = tableQuery.executeQuery("SHOW TABLES");
 
             System.out.println("\n\n\n______________________________________");
 
 
             while (tableList.next()) {
                 if (step) {
                     pause(); // Pause before each table; except the first
                     System.out.println("______________________________________");
                 } else {
                     step = true;
                 }
                 String table = tableList.getString(1);
                 System.out.println("\nTABLE: " + table);
 
                 ResultSet result = select.executeQuery("Select * from " + table);
                 // Get metatdata from stars; print # of attributes in table
                 ResultSetMetaData metadata = result.getMetaData();
                 System.out.println("\nThere are " + metadata.getColumnCount() + " columns");
                 // Print type of each attribute
                 for (int i = 1; i <= metadata.getColumnCount(); i++) {
                     System.out.println("(" + i + ") " + metadata.getColumnName(i) + " :: " + metadata.getColumnTypeName(i));
                 }
                 System.out.println("\n______________________________________");
             }
         } catch (SQLException ex) {
             printSQLError(ex);
         }
     }
// }}}
// SQL Query {{{
     //=== SQL Query
     /* Enter a valid SELECT/UPDATE/INSERT/DELETE SQL command. The system
     should take the corresponding action, and return and display the valid
     results. For a SELECT query, display the answers. For the other types
     of queries, give enough information about the status of the execution
     of the query. For instance, for an UPDATE query, show the user how many
     records have been successfully changed.*/
     private static void openQueryMenu() {
         //TODO Enter a valid SELECT/UPDATE/INSERT/DELETE SQL command.
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         System.out.print("Enter SQL Query: ");
         String query;
 		try {
             query = br.readLine();
             Statement command = connection.createStatement();
             ResultSet result = command.executeQuery(query);
             char queryType = query.trim().toLowerCase().charAt(0);
             
             if (queryType == 's') {			// SELECT
            	System.out.println("SELECT command executed.");
             } else if (queryType == 'u') {	// UPDATE
             } else if (queryType == 'i') {	// INSERT
             } else if (queryType == 'd') {	// DELETE
             }
         } catch (SQLException e) {
         	printSQLError(e);
         } catch (IOException e) {
         	
         }
     }
// }}}
 // HELPERS {{{
     //=== Extra Functions
     private static void printStars(ResultSet result) throws SQLException {
         // print table's contents, field by field
         int count = 0;
         while (result.next()) {
             count++;
             System.out.println("ID       = " + result.getInt(1));
             System.out.println("Name     = " + result.getString(2) + " " + result.getString(3));
             System.out.println("DOB      = " + result.getString(4));
             System.out.println("photoURL = " + result.getString(5));
             if (count % 3 == 0) {
                 System.out.println("______________________________________");
                 pause();
                 System.out.println("______________________________________\n");
             } else {
                 System.out.println();
             }
         }
         if (count == 0) {
             System.out.println("**** No results found ****\n");
         }
     }
 
     private static boolean printCustomers(ResultSet result) throws SQLException {
         int count = 0;
         while (result.next()) {
             count++;
             System.out.println("ID      = " + result.getInt(1));
             System.out.println("Name    = " + result.getString(2) + " " + result.getString(3));
             System.out.println("CC      = " + result.getString(4));
             System.out.println("Address = " + result.getString(5));
             System.out.println("E-mail  = " + result.getString(6));
             if (count % 3 == 0) {
                 System.out.println("______________________________________");
                 pause();
                 System.out.println("______________________________________\n");
             } else {
                 System.out.println();
             }
         }
         if (count == 0) {
             System.out.println("**** No results found ****\n");
             return false;
         }
         return true;
     }
 
     private static void printSQLError(SQLException ex) {
         /*In cases where the requested tasks cannot be accomplished, print out
         a clear, crisp error message–do not just pass along some Java exception! */
         //TODO add clean mySQL error messages
         // List of error codes:
         // http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-error-sqlstates.html
         // http://dev.mysql.com/doc/refman/5.5/en/error-messages-server.html
         // http://dev.mysql.com/doc/refman/5.5/en/error-messages-client.html
         // http://www.java2s.com/Open-Source/Java-Document/Database-JDBC-Connection-Pool/mysql/com.mysql.jdbc.exceptions.jdbc4.htm
         // http://www.java2s.com/Open-Source/Java-Document/Database-JDBC-Connection-Pool/mysql/com.mysql.jdbc.htm
 
         SQLException sqlError = ex;
         while (sqlError != null) {
 
             if (sqlError.getClass() == com.mysql.jdbc.MysqlDataTruncation.class) {
                 //Improperly formatted input
                 System.out.println("SQL Error -- Improperly formatted input.");
             } else if (sqlError.getClass() == com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException.class) {
                 //Foreign key, e.g. the credit card number, has failed.
                 // triggers for star ID conflicts too
                 System.out.println("SQL Error -- Conflict with consistency.");
             } else if (sqlError.getSQLState().equals("28000")) {
                 // (SQLState 28000) java.sql.SQLException: Access denied for user -- Invalid password
                 System.out.println("\n" + sqlError.getMessage());
             } else if (sqlError.getClass() == com.mysql.jdbc.exceptions.jdbc4.CommunicationsException.class) {
                 // No response
                 // com.mysql.jdbc.exceptions.jdbc4.CommunicationsException
                 System.out.println("\nCould not contact mySQL server");
             } else if (sqlError.getErrorCode() == 1142) {
                 // (mySQL Error 1142) 	ER_TABLEACCESS_DENIED_ERROR
                 System.out.println("\nUser does not have required permissions.\n");
             } else {
                 System.out.println("----SQLException----");
                 System.out.println("SQLState:  " + sqlError.getSQLState());
                 System.out.println("Vendor Error Code:  " + sqlError.getErrorCode());
                 System.out.println("Message:  " + sqlError.getMessage());
                 System.out.println("String:  " + sqlError.toString());
             }
             sqlError = sqlError.getNextException();
         }
     }
 
     private static void pause() {
         BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
         System.out.print("Press Enter to continue.");
         try {
             int ch = stdin.read();
         } catch (IOException ex) {
             System.out.println(ex.getMessage());
         }
 
     }
 // }}}
 }
