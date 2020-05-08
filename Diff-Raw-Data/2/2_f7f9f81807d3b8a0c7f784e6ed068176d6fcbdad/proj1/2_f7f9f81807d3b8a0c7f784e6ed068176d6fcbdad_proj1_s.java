 /**
    Alexander Wong, Michelle Naylor
    CMPUT 291 Database Project 1
 
    Due March 11 at 5pm
    
    Tables are defined as followed:
        categories(cat, supercat)
        users(email, name, pass, last_login)
        reviews(rno, rating, text, reviewer, reviewee, rdate)
        offers(ono, ndays, price)
        ads(aid, atype, title, price, descr, location, pdate, poster, cat)
        purchases(pur_id, start_date, aid, ono)
 */
 
 
 import java.sql.*;
 import java.util.*;
 import java.util.Arrays;
 import java.io.Console;
 
 public class proj1 {
 
     public static Console console;
     public static Connection con;
     public static Statement stmt;
     public static ResultSet rset;
     public static String userstate = null;
 
     public static void main(String args[]) {	
 	// NOTE: This program relies on the use of console. If console does not exist, exit.
 	console = System.console();
 
 	if (console == null){
 	    System.out.println("Failed to get the console instance.");
 	    System.exit(0);
 	}
 	
 	try {
 	    // Step 1: Load the appropriate jdbc driver
 	    String m_drivername = "oracle.jdbc.driver.OracleDriver";
 	    Class drvClass = Class.forName(m_drivername);
 	    DriverManager.registerDriver((Driver) drvClass.newInstance());
 	    
 	    // Step 2: Establish the connection
 	    String m_url = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
 	    System.out.println("Connecting to '" + m_url + "'...");
 	    String m_userName = console.readLine("Enter username: ");
 	    char[] m_ipassword = console.readPassword("Enter password: ");
 	    String m_password = new String(m_ipassword);
 	    System.out.println("Connecting...");
 	    con = DriverManager.getConnection(m_url, m_userName, m_password);
 	    stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 				       ResultSet.CONCUR_UPDATABLE);
 
 	    Arrays.fill(m_ipassword, ' ');
 	    m_password = "";
 	    
 	    System.out.println("Sucessfully connected to the database.");
 	    
 	    // Step 3: Begin project specifications here
 	    while(true){
 		userstate = logchoice();
 		if (userstate.equals("RETRY")){
 		    // Iterate through the loop again, just calls userstate again
 		    continue;
 		} else {
 		    // User is now logged in, allow ads search, list, post
 		    // allow user search, user logout
 		    // NOTE: At this point, userstate = email
 		    
 		    System.out.println("\nYou are logged in as: " + userstate + ".");
 		    
 		    while(true){
 			// infinite loop here
 			System.out.println("\nUjiji Options:");
 			System.out.println("'0' for logout, '1' for post ad, '2' for list own ads,");
 			System.out.println("'3' for search ads, '4' for search users");
 			String raw_selection = console.readLine("Enter selection (0-4): ");
 			int selection = 255;
 			try{
 			    selection = Integer.valueOf(raw_selection);
 			} catch (Exception e) {
 			    //e.printStackTrace();
 			}
 			if (selection < 0 || selection > 4) {
 			    System.out.println("Invalid input '" + raw_selection + "'");
 			    continue;
 			}
 			if (selection == 0) {
 			    // Logout user
 			    logout();
 			    break;
 			}
 			if (selection == 1) {
 			    // Post an ad
 			    ad_post();
 			}
 			if (selection == 2) {
 			    // List own ads
 			    own_ads();
 			}
 			if (selection == 3) {
 			    ad_search();
 			}
 			if (selection == 4) {
 			    // Search for users
 			    user_search();
 			    
 			}
 		    }
 		    
 		}
 	    }
 	    
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 
     public static String logchoice(){
 	// This function allows users to login or register as a new user
 	String rvalue = "";
 	System.out.println();
 	int login_input = 255;
 	while (login_input < 0 || login_input > 2){
 	    System.out.println("Login Screen Selection");
 	    System.out.println("'0' for exit, '1' for login, '2' for registration:");
 	    String raw_input = console.readLine("Enter selection (0-2): ");
 	    try {
 		login_input = Integer.valueOf(raw_input);
 	    }catch (Exception e){
 		//e.printStackTrace();
 	    }
 	    if (login_input < 0 || login_input > 2) {
 		System.out.print("Entered value '"+raw_input+"'. ");
 		System.out.println("Invalid input.");
 	    }
 	}
 
 	if (login_input == 0) {
 	    System.out.println("\nExiting program...\n");
 	    try {
 		con.close();
 		stmt.close();
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	    System.exit(0);
 	}
 
 	else if (login_input == 1) {
 	    // Login screen here, enter email and pass
 	    System.out.println("\n1) Login Screen");
 	    while(true) {
 		System.out.println("'0' for back, else enter email:");
 		String raw_email = console.readLine("Enter email: ").replaceAll("'", "").replace('"', '\0');
 		if (raw_email.length() > 20) {
 		    System.out.println("Email too long! (length '" + raw_email.length()+ "')");
 		    continue;
 		}
 		if (raw_email.equals("0")){
 		    System.out.println("Back...");
 		    return "RETRY";
 		}
 		char[] raw_pass = console.readPassword("Enter pass: ");
 		String pass = new String(raw_pass).replaceAll("'", "").replace('"', '\0');
 		Arrays.fill(raw_pass, ' ');
 		// Query goes here		
 		String checkUser = "SELECT * FROM users WHERE LOWER(email) = LOWER('" +
 		    raw_email + "') AND pwd = '" + pass + "'";
 		try {
 		    rset = stmt.executeQuery(checkUser);
 		    while(rset.next()){
 			String email = rset.getString("email").replaceAll("\\s","");
 			if (email.equals(raw_email)) {
 			    String last_login = rset.getString("last_login");
 			    if (last_login != null){
 				last_login = last_login.substring(0, last_login.length() - 2);
 			    }
 			    System.out.println("Welcome back, " + rset.getString("name").trim() + 
 					       ", your last login was at " + last_login +".");
 			    
 			    System.out.println("Displaying reviews from last login to current time:");
 			    // Display all reviews between last_login and current system time
 			    // where reviewee = email
 			    
 			    String display_reviews = "SELECT rdate, rating, reviewer, text FROM reviews WHERE " + 
 				"lower(reviewee) = lower('" + email + 
 				"') AND rdate BETWEEN (" + 
 				"SELECT last_login FROM users WHERE LOWER(email) = LOWER('" + email +"')) AND " +
 				"CURRENT_TIMESTAMP ORDER BY CURRENT_TIMESTAMP DESC";
 			    rset = stmt.executeQuery(display_reviews);			    
 			    Integer increment = 0;
 			    String shownextreviews = null;
 			    if(rset.next()){
 				rset.previous();
 				System.out.println("\nRating | Reviewer             | Review Date           | Review Text (Up to 40 chars)");
 				while(rset.next()){
 				    String reviewshort = rset.getString("text").substring(0, Math.min(rset.getString("text").length(), 40));
 				    if (rset.getString("text").trim().length() > 40) {
 					reviewshort = reviewshort+"...";
 				    }
 				    // Longest date possible yyyy-mm-dd hh:mm:ss:n
 				    String rdateshort = rset.getString("rdate");
 				    if (rdateshort.length() < 21){
 					Integer spaces = 21 - rdateshort.length();
 					for (int i = 0; i<spaces; i++){
 					    rdateshort = rdateshort+" ";
 					}
 				    }
 				    System.out.println(rset.getInt("rating") + "      | " + 
 						       rset.getString("reviewer") + " | " + 
 						       rdateshort + " | " +
 						       reviewshort 						       
 						       );	   
 				    
 				    
 				    
 				    increment++;				
 				    if(increment%3 == 0){
 					shownextreviews = "c";
 					while (!shownextreviews.equals("y") && !shownextreviews.equals("n")){
 					    shownextreviews = console.readLine("Show next 3 reviews (y/n): ");
 					    if (!shownextreviews.equals("y") && !shownextreviews.equals("n")){
 						System.out.println("Invalid input '" + shownextreviews + "'");
 					    }
 					}
 					if (shownextreviews.equals("n")){
 					    break;
 					}
 				    }
 				}
 				System.out.println("- No more reviews to show -");
 			    } else {
 				System.out.println("- No new reviews -");
 			    }
 			    return email;			   
 			}
 		    } 
 		    System.out.println("Invalid email or pass.");
 		    
 		} catch (Exception e){
 		    e.printStackTrace();
 		}
 	    }
 	}
 
 	else if (login_input == 2) {
 	    // Register user screen here, ask for new email and pass
 	    // NOTE: Currently, length is not restricted. Will be cut off in SQL 
 	    // statememnt execution
 	    System.out.println("\n2) Register New User");
 	    System.out.println("'0' for back, else enter new email:");
 
 	    while(true){
 		String raw_email = null;
 		String raw_email2 = null;
 		String pass = null;
 		String pass2 = null;
 		String raw_name = null;
 		while(true){
 		    raw_email = console.readLine("Enter email (up to 20 chars): ").trim().
 			replaceAll("'", "").replace('"', '\0');
 		    if(raw_email.equals("0")) {
 			System.out.println("Back...");
 			return "RETRY";
 		    }
 		    if (raw_email.length() > 20) {
 			System.out.println("Email too long! (length '" + 
 					   raw_email.length()+ "')");
 			continue;
 		    }
 		    raw_email2 = console.readLine("Confirm email: ").trim().
 			replaceAll("'", "").replace('"', '\0');
 		    if(!raw_email.equals(raw_email2)){
 			System.out.println("Emails do not match.");
 			continue;
 		    }
 		    if (raw_email2.length() > 20) {
 			System.out.println("Email too long! (length '" + raw_email2.
 					   length()+ "')");
 			continue;
 		    }
 		    break;
 		}
 		while(true){
 		    char[] raw_pass = console.readPassword("Enter password (up to 4 chars): ");
 		    char[] raw_pass2 = console.readPassword("Confirm password: ");
 		    pass = new String(raw_pass).replaceAll("'", "").replace('"', '\0');
 		    pass2 = new String(raw_pass2).replaceAll("'", "").replace('"', '\0');
 		    Arrays.fill(raw_pass, ' ');
 		    Arrays.fill(raw_pass2, ' ');
 		    if (!pass.equals(pass2)){
 			System.out.println("Passwords do not match.");
 			continue;
 		    }
 		    if (pass.length() > 4 || pass2.length() > 4){
 			System.out.println("Password too long, must be up to 4 chars");
 			continue;
 		    }
 		    break;
 		}
 		while(true){
 		    raw_name = console.readLine("Enter name (up to 20 chars): ").trim().
 			replaceAll("'", "").replace('"', '\0');
 		    if(raw_name.length() > 20){
 			System.out.println("Name too long! (length'" + raw_name.length() 
 					   + "')");
 			continue;
 		    }
 		    break;
 		}
 		// Check if the email exists in the users table
 		// If the email exists make the user enter information again
 		String check_email = "SELECT email FROM users WHERE LOWER(email) = LOWER('"
 		    + raw_email + "')";
 		String create_acc = "INSERT INTO users (email, name, pwd) VALUES ('" 
 		    + raw_email + "', '" + raw_name + "', '" + pass + "')";
 		try {
 
 		    rset = stmt.executeQuery(check_email);
 		    if (rset.next()) {			
 			System.out.println("Email already exists, please try again.");
 			continue;
 			
 		    }
 		    else{
 			// Create the account
 			stmt.executeUpdate(create_acc);
 
 			// Verify the account
 			String checkUser = "SELECT * FROM users WHERE LOWER(email) = LOWER('" +
 			    raw_email + "') AND pwd = '" + pass + "'";
 			rset = stmt.executeQuery(checkUser);
 			rset.next();
 			String email = rset.getString("email").replaceAll("\\s","");
 			if (email.equals(raw_email)) {
 			    System.out.println("Welcome, " + rset.getString("name").trim() + ".");
 			    return email;
 			
 			} else {
 			    System.out.println("Invalid email or pass.");
 			}
 			
 		    }
 		} catch (SQLException e) {
 		    e.printStackTrace();
 		}		
 	    }
 	}
 	
 	// Function should never reach here
 	// my control statements should cover all cases
 	return "RETRY";
     }
 
     public static void logout(){
 	// Logs out of the system, sets current system time to last_login
  	System.out.println("\n0) Loging out...");
  	String update_lastlogin = "UPDATE users SET last_login = (SELECT CURRENT_TIMESTAMP FROM DUAL) " +
 	    "WHERE lower(email) = lower('" + userstate + "')";
 	
 	try {
 	    stmt.executeUpdate(update_lastlogin);
 	    System.out.println("You have sucessfully logged out.");
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	userstate = null;
 	return;    
     }
 
     public static void own_ads() {
 	Boolean correct_input = false;
 	String raw_selection = " ";
 	int selection = 255;
 
 	String own_ad_search = "SELECT a.aid, a.atype, a.title, a.price, a.pdate, (po.ndays - po.days_diff) AS days_left FROM ads a LEFT OUTER JOIN (SELECT p.aid, to_number(sysdate - p.start_date) AS days_diff, o.ndays FROM purchases p, offers o WHERE p.ono = o.ono) po ON po.aid = a.aid WHERE a.poster = '" + userstate + "' ORDER BY pdate DESC";
 
 	try {
 	    // execute query
 	    rset = stmt.executeQuery(own_ad_search);
 	    // if ResultSet is empty, return
 	    if (rset.first() == false) {
 		return;
 	    }
 	    rset.beforeFirst();
 	    own_ad_print(rset);
 	    
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 
 	while (correct_input == false) {
 	    System.out.println("'0' for back, '1' to delete an ad, '2' to promote an ad: ");
 	    raw_selection = console.readLine("Enter selection (0-2): ");
 	    
 	    try{
 		selection = Integer.valueOf(raw_selection);
 	    } catch (Exception e) {
 		//e.printStackTrace();
 	    }
 	    // check valid input
 	    if (selection < 0 || selection > 2) {
 		System.out.println("Invalid input '" + raw_selection + "'");
 	    }
 	    // delete ad
 	    if (selection == 1) {
 		ad_delete(rset);
 		correct_input = true;
 	    }
 	    // promote ad
 	    if (selection == 2) {
 		ad_promote(rset);
 		correct_input = true;
 	    }
 	    // return
 	    if (selection == 0) {
 		correct_input = true;
 		return;
 	    }
 	}
     }
     
     public static void own_ad_print(ResultSet rset) {
 	/** prints ads in rset in multiples of 5
         asks to print more in multiples of 5
 	NOTE: if there are no more ads to display, still asks to print more
 	*/
 	int ad_num = 1;
 	int counter = 0;
 	int selection = 255;
 	String raw_selection = " ";
 	Boolean correct_input = false;
 	
 	// print column headers
 	System.out.println("Ad num|Ad type |Title                  |Price  |Date              | Days left on promotion");
 
 	try {
 	    // print query result
 	    while (counter < 5 && rset.next()){
 		String rs_atype = rset.getString("atype");
 		String rs_title = rset.getString("title");
 		Float rs_price = rset.getFloat("price");
 		String rs_pdate = rset.getString("pdate");
 		int days_left = rset.getInt("days_left");
 		
 		// check if offer is still valid
       		if (days_left > 0) {
 		    System.out.println(rset.getRow() + ": \t" + rs_atype + "\t" + rs_title +
 				       "\t" + rs_price + "\t" + rs_pdate + "\t" 
 				       + days_left);
 		}
 		else {
 		    System.out.println(rset.getRow() + ": \t" + rs_atype + "\t" + rs_title + 
 				       "\t" + rs_price + "\t" + rs_pdate);
 		}
 		counter++;
 	    }
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 	
 	try {
 	    while (rset.isLast() == false && rset.isAfterLast() == false 
 		   && correct_input == false) {
 		// See more ads
 		System.out.println("'0' for back, '1' for more ads: ");
 		raw_selection = console.readLine("Enter selection (0-1): ");
 		try {
 		    selection = Integer.valueOf(raw_selection);
 		} catch (Exception e) {
 		}
 		
 		// valid input check
 		if (selection < 0 || selection > 1) {
 		    System.out.println("Invalid input '" + raw_selection + "'");
 		}
 		
 		// print more ads
 		if (selection == 1) {
 		    own_ad_print(rset);
 		    correct_input = true;
 		}
 		
 		if (selection == 0) {
 		    correct_input = true;
 		    return;
 		}
 	    } 
 	} catch (Exception e) {
 	    //e.printStackTrace();
 	}
     }
     
     public static void ad_promote(ResultSet rset) {
 	/**
 	   Promotes an ad, as selected by user
 	 */
 	String select_title = " ";
 	String select_aid = " ";
 	Boolean correct_input = false;
 	int selection = 0;
 	String raw_selection = " ";
 	
 	try {
 	    // move cursor to last position of rset
 	    rset.last();
 	} catch (Exception e) {}
 
 	while (correct_input == false) { 
 	    // which ad to promote
 	    raw_selection = console.readLine("Enter ad's number: ");
 	    try {
 		selection = Integer.parseInt(raw_selection);
 		System.out.println("Selection was: " + selection);
 		// return
 		if (selection == 0) {
 		    correct_input = true;
 		    return;
 		}
 		// input is valid, continue
 		if (selection <= rset.getRow()) {
 		    correct_input = true;
 		}
 
 		else {
 		    System.out.println("Selection was out of scope");
 		}
 	    } catch (Exception e) {
 		// e.printStackTrace();
 	    }
 	}
 	
 	try {
 	    // find specified ad
 	    rset.absolute(selection);
 	    select_title = rset.getString("title");
 	    select_aid = rset.getString("aid");
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 	
 	// check to see if ad is already on promotion
 	String promo_check = "SELECT * FROM purchases WHERE aid = '" + select_aid + "'";
 	try {
 	    rset = stmt.executeQuery(promo_check);
 	} catch (SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 	try {
	    if (rset.getRow() == 0) {
 		System.out.println("This ad is already on promotion");
 		return;
 	    }
 	} catch (Exception e) {}
 
 	// Display offers to choose from
 	String offers = "SELECT * FROM offers";
 	try {
 	    // execute query
 	    rset = stmt.executeQuery(offers);
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 
 	try {
 	    // print query result
 	    while (rset.next()){
 		String rs_ono = rset.getString("ono");
 		String rs_ndays = rset.getString("ndays");
 		Float rs_price = rset.getFloat("price");
 		System.out.println(rset.getRow() + ": " + rs_ono + " " + rs_ndays + " " 
 				   + rs_price);
 	    }
 	    // put cursor at last row
 	    rset.last();
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 
 	// which promotion
 	correct_input = false;
 	while (correct_input == false) {
 	    raw_selection = console.readLine("Enter number of promotion to add: ");
 	    try {
 		selection = Integer.parseInt(raw_selection);
 		System.out.println("Selection was: " + selection);
 		// return
 		if (selection == 0) {
 		    correct_input = true;
 		    return;
 		}
 		// input is valid, continue
 		if (selection <= rset.getRow()) {
 		    correct_input = true;
 		}
 		
 		else {
 		    System.out.println("Selection was out of scope");
 		}
 
 	    } catch (Exception e) {
 		// e.printStackTrace();
 	    }
 	}
 	
 	// Find the new pid
 	String findmaxaid = "SELECT MAX(pur_id) FROM purchases";
 	String newpid = null;
 	Integer pidval = null;
 	try {
 	    rset = stmt.executeQuery(findmaxaid);
 	    if (rset.next()){
 		String rawpid = rset.getString(1);
 		// remove the leading 'a'
 		rawpid = rawpid.substring(1);
 		try {
 		    pidval = Integer.parseInt(rawpid);
 		    pidval++;
 		    newpid = "p"+pidval;
 		} catch (Exception e) {
 		    e.printStackTrace();
 		    System.out.println("Parsed pid value is not numerical!");
 		}
 	    } else {
 		newpid = "p001";
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 
 	String promote = "INSERT INTO purchases VALUES('" + newpid 
 	    + "', CURRENT_TIMESTAMP, '" + select_aid + "', " + selection + ")";
 
 	try {
 	    // execute query
 	    stmt.executeUpdate(promote);
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	} 
     }
 
     public static void ad_delete(ResultSet rset) {
 	/**
 	   Deletes a specific ad, as selected by user
 	 */
 	String select_aid = " ";
 	String raw_selection = " ";
 	int selection = 0;
 	Boolean correct_input = false;
 
         // move cursor to last element
 	try {
 	    rset.last();
 	} catch (Exception e) {}
 	while (correct_input == false) { 
 	    // which ad to delete
 	    raw_selection = console.readLine("Enter ad's number: ");
 	    try {
 		selection = Integer.parseInt(raw_selection);
 		System.out.println("Selection was: " + selection);
 		if (selection == 0) {
 		    correct_input = true;
 		    return;
 		}
 		if (selection <= rset.getRow()) {
 		    correct_input = true;
 		}
 		else {
 		    System.out.println("Selection was out of scope");
 		}
 	    } catch (Exception e) {
 		// e.printStackTrace();
 	    }
 	}
 
 	try {
 	    // find specified ad
 	    rset.absolute(selection);
 	    select_aid = rset.getString("aid");
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 	String delete_purchases = "DELETE FROM purchases WHERE aid = '" + select_aid + "'";
 	String delete_ads = "DELETE FROM ads WHERE aid = '" + select_aid + "'";
 	try {
 	    // execute query
 	    stmt.executeUpdate(delete_purchases);
 	    stmt.executeUpdate(delete_ads);
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	} 
     }
     
     public static void ad_search(){
 	String delims = " ";
 	int selection = 255;
 	String raw_selection = " ";
 	Boolean correct_input = false;
 	
 	// split keywords input into an array of keywords
 	String keywords_str = console.readLine("Enter keywords: ");
 	
 	if (keywords_str.contentEquals(" ") == true) {
 	    System.out.println("Invalid input");
 	    return;
 	}
 	String[] keywords = keywords_str.trim().split(delims);
 	
 	String key_search = "SELECT atype, title, price, pdate FROM ads WHERE LOWER(title) LIKE '%" 
 	    + keywords[0].toLowerCase() + "%' OR LOWER(descr) LIKE '%" + keywords[0].toLowerCase() + "%'";
 
 	// add keywords to the SQL query
 	for (int i = 1; i < keywords.length; i++) {
 	    key_search = key_search.concat(" OR LOWER(title) LIKE '%" + keywords[i].toLowerCase() 
 					   + "%' OR LOWER(descr) LIKE '%" + keywords[i].toLowerCase() + "%'");
 	}
 
 	// add order by clause to SQL query
 	key_search = key_search.concat(" ORDER BY pdate DESC");
 	
 	try {
 	    // execute query
 	    rset = stmt.executeQuery(key_search);
 	    // if rset is empty, return
 	    if (rset.first() == false) {
 		return;
 	    }
 	    rset.beforeFirst();
 	    ad_print(rset);
 
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 
 	while (correct_input == false) {
 	    System.out.println("'0' for back, '1' for more ad detail: ");
 	    raw_selection = console.readLine("Enter selection (0-1): ");
 	    try{
 		selection = Integer.valueOf(raw_selection);
 		// return
 		if (selection == 0) {
 		    correct_input = true;
 		    return;
 		}
 		// specific ad
 		if (selection == 1) {
 		    ad_moredetail(rset);
 		    correct_input = true;
 		}
 		else {
 		    System.out.println("Selection was out of scope");
 		}
 	    } catch (Exception e) {
 		// e.printStackTrace();
 	    }
 	}
     }
 
     public static void ad_print(ResultSet rset) {
 	/** prints ads in rset in multiples of 5
         asks to print more in multiples of 5
 	NOTE: if there are no more ads to display, still asks to print more
 	*/
 	int ad_num = 1;
 	int counter = 0;
 	int selection = 255;
 	String raw_selection = " ";
 	Boolean correct_input = false;
 	
 	// print column headers
 	System.out.println("Ad num|Ad type |Title                  |Price  |Date              | Days left on promotion");
 
 	try {
 	    // print query result
 	    while (counter < 5 && rset.next()){
 		String rs_atype = rset.getString("atype");
 		String rs_title = rset.getString("title");
 		Float rs_price = rset.getFloat("price");
 		String rs_pdate = rset.getString("pdate");
 		System.out.println(rset.getRow() + ": \t" + rs_atype + "\t" + rs_title + 
 				       "\t" + rs_price + "\t" + rs_pdate);
 		counter++;
 	    }
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 	
 	try {
 	    while (rset.isLast() == false && rset.isAfterLast() == false 
 		   && correct_input == false) {
 		// See more ads
 		System.out.println("'0' for back, '1' for more ads: ");
 		raw_selection = console.readLine("Enter selection (0-1): ");
 		try {
 		    selection = Integer.valueOf(raw_selection);
 		} catch (Exception e) {
 		}
 		
 		// valid input check
 		if (selection < 0 || selection > 1) {
 		    System.out.println("Invalid input '" + raw_selection + "'");
 		}
 		
 		// print more ads
 		if (selection == 1) {
 		    ad_print(rset);
 		    correct_input = true;
 		}
 		
 		if (selection == 0) {
 		    correct_input = true;
 		    return;
 		}
 	    } 
 	} catch (Exception e) {
 	    //e.printStackTrace();
 	}
     }
     
     public static void ad_moredetail(ResultSet rset) {
 	/**
 	   Provides more detail for specific ads, as selected by user
 	 */
 	String select_title = " ";
 	String rs_poster = " ";
 	String raw_selection = " ";
 	int selection = 0;
 	Boolean correct_input = false;
 	
 	//move rset cursor to last entry
 	try {
 	    rset.last();
 	} catch (Exception e) {}
 	while (correct_input == false) { 
 	    try {
 		raw_selection = console.readLine("Enter ad's number: ");
 		selection = Integer.parseInt(raw_selection);
 		System.out.println("Selection was: " + selection);
 		if (selection == 0) {
 		    return;
 		}
 		if (selection <= rset.getRow()) {
 		    correct_input = true;
 		}
 		else {
 		    System.out.println("Selection was out of scope");
 		}
 	    } catch (Exception e) {
 		// e.printStackTrace();
 	    }
 	}
 
 	try {
 	    // find specified ad
 	    rset.absolute(selection);
 	    select_title = rset.getString("title");
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 
 	String more_detail = "SELECT title, descr, location, cat, poster FROM ads WHERE title = '" + select_title + "'";
 	try {
 	    // execute query
 	    rset = stmt.executeQuery(more_detail);
 	    while(rset.next()){
 		String rs_descr = rset.getString("descr");
 		String rs_location = rset.getString("location");
 		String rs_cat = rset.getString("cat");
 		rs_poster = rset.getString("poster");
 		System.out.print(rs_descr + " " + rs_location + " " + rs_cat + " " 
 				   + rs_poster);
 	    }
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 	String poster_reviews = "SELECT AVG(rating) AS avg_rating FROM reviews WHERE reviewee LIKE '" + rs_poster + "'";
 	
 	try {
 	    // execute query
 	    rset = stmt.executeQuery(poster_reviews);
 	    while(rset.next()){
 		String rs_avg_rating = rset.getString("avg_rating");
 		if (rs_avg_rating != null) {
 		    System.out.println(rs_avg_rating);
 		}
 		else {
 		    System.out.println();
 		}
 	    }
 	} catch(SQLException ex) {
 	    System.err.println("SQLException:" + ex.getMessage());
 	}
 	return;
     }
 
     public static void user_search() {
 	/**
 	   User should be able search by email or search by name
 	   If search by name, returns a result of all users with that name; 
 	   user should be able to select one from the list.
 	*/
 	Integer selection = 255;
 	while(selection<0 || selection>2){
 	    System.out.println("\n4) User Search:");
 	    System.out.println("'0' for back, '1' for email search, '2' for name search");
 	    String raw_selection = console.readLine("Enter selection (0-2): ");
 	    try {
 		selection = Integer.parseInt(raw_selection);
 	    } catch (Exception e) {
 	    }
 	    if (selection < 0 || selection > 2) {
 		System.out.println("Invalid input: '" + raw_selection + "'");
 	    }
 	    if (selection == 0) {
 		System.out.println("Back...");
 		return;
 	    }	    
 	    if (selection == 1) {
 		// Search by email
 		String searchemail = null;
 		while(true){
 		    System.out.println("\nSearch user email: ");
 		    System.out.println("'0' for back, else enter email:");
 		    searchemail = console.readLine("Enter email, '0' for back: ").replaceAll
 			("'", "").replace('"', '\0');
 		    if (searchemail.equals("0")){
 			System.out.println("Back...");
 			return;
 		    }
 		    // Search for a user with the specified email
 		    // If email exists, return name, email, number of ads, average rating
 		    String searchemailquery = "SELECT users.name, users.email, " + 
 			"COUNT(DISTINCT ads.aid) , AVG(reviews.rating) " +
 			"FROM (users FULL JOIN ads ON (users.email = ads.poster)) RIGHT JOIN reviews ON (reviews.reviewee = users.email) " + 
 			"WHERE lower(users.email) LIKE lower('%" + searchemail + "%') AND " + 
 			"lower(ads.poster) LIKE lower('%" + searchemail + "%') " + 
 			"GROUP BY users.name, users.email";
 		    
 		    try {
 			rset = stmt.executeQuery(searchemailquery);			
 			if (rset.next()){
 			    rset.previous();
 			    selection = 1;
 			    System.out.println("#  | User Name            | User Email           | Ads  | Avg Rating ");
 			    while(rset.next()){	
 				String tselection = String.valueOf(selection);
 				if(tselection.length() == 1) {
 				    tselection = tselection+" ";
 				}
 				String tads = String.valueOf(rset.getInt(3));
 				if(tads.length() == 1) {
 				    tads = tads+" ";
 				}
 				System.out.println(tselection + " | " + 
 						   rset.getString(1) + " | " +
 						   rset.getString(2)+ " | " +
 						   tads + "   | " +
 						   rset.getFloat(4));
 				selection++;
 			    }
 			    Integer userselect = 0;
 			    while(userselect < 1 || userselect > (selection - 1)){
 				String raw_userselect = console.readLine("Select user number (1-" + (selection - 1) +", '0' for back): ");				
 				try{
 				    userselect = Integer.parseInt(raw_userselect);
 				    if(userselect == 0){
 					break;
 				    }
 				    // Choose the user with the email at the row specified
 				    // Move the cursor back the difference of selection and
 				    // userselect
 				    // ie) if there are 3 people rows, and a user select 3,
 				    // will move row back 0 times
 				    // if the user selects 1, move the row back 3-1 = 2x
 				    Integer moveback = selection - userselect;
 				    for(int i = 0; i < moveback; i++){
 					rset.previous();
 				    }
 				    user_options(rset.getString("email").trim());
 				    return;
 				} catch (Exception e){
 				    System.out.println("Invalid input '" + 
 						       raw_userselect + "'");
 				}				
 			    }
 
 			} else {
 			    System.out.println("No user by that email.");
 			}
 		    } catch (SQLException e) {
 			e.printStackTrace();
 		    }		    		    
 		}
 	    }	    
 	    if (selection == 2) {
 		// Search by name
 		String searchname = null;
 		while(true){
 		    System.out.println("\nSearch user name: ");
 		    System.out.println("'0' for back, else enter name:");
 		    searchname = console.readLine("Enter name, '0' for back: ").replaceAll
 			("'", "").replace('"', '\0');
 		    if (searchname.equals("0")){
 			System.out.println("Back...");
 			return;
 		    }
 		    // Query begins here
 		    String searchnamequery = "SELECT users.name AS name, users.email AS email, COUNT(DISTINCT ads.aid) AS ads, AVG(reviews.rating) AS rating " +
 			"FROM (users FULL JOIN ads ON (users.email = ads.poster)) FULL JOIN reviews ON (users.email = reviews.reviewee) " + 
 			"WHERE lower(users.name) LIKE lower('%" + searchname + "%')" + 
 			"GROUP BY users.name, users.email";
 		    
 		    try {
 			rset = stmt.executeQuery(searchnamequery);
 			if (rset.next()){
 			    rset.previous();
 			    selection = 1;
 			    System.out.println("#  | User Name            | User Email           | Ads  | Avg Rating ");
 			    while(rset.next()){	
 				String tselection = String.valueOf(selection);
 				if (tselection.length() == 1){
 				    tselection = tselection+" ";
 				}
 				String tads = String.valueOf(rset.getInt("ads"));
 				if (tads.length() == 1){
 				    tads = tads+" ";
 				}
 				System.out.println(tselection + " | " + 
 						   rset.getString("name") + " | " +
 						   rset.getString("email")+ " | " +
 						   tads + "   | " +
 						   rset.getFloat("rating"));
 				selection++;
 			    }			    
 			    Integer userselect = 0;
 			    while(userselect < 1 || userselect > (selection - 1)){
 				String raw_userselect = console.readLine
 				    ("Select user number (1-" + (selection - 1) + 
 				     ", '0' for back): ");				
 				try{
 				    userselect = Integer.parseInt(raw_userselect);
 				    if(userselect == 0){
 					break;
 				    }
 				    Integer moveback = selection - userselect;
 				    for(int i = 0; i < moveback; i++){
 					rset.previous();
 				    }
 				    user_options(rset.getString("email").trim());
 				    return;
 				} catch (Exception e){
 				    //e.printStackTrace();
 				    System.out.println("Invalid input '"+ raw_userselect 
 						       + "'");
 				}				
 			    }			    
 			    
 			} else {
 			    System.out.println("No user by that name.");
 			}
 		    } catch (SQLException e) {
 			e.printStackTrace();
 		    }
 		}
 	    }
 	}
     }
 
     public static void user_options(String email){
 	/**
 	   This function takes the email given and allows options:
 	   1) See all the reviews posted about this email
 	   2) Write a review for this email
 	 */
 	System.out.println("You have selected '" + email + "'");
 	Integer choice = null;
 	while(true){
 	    String raw_choice = console.readLine("'0' for back, '1' to list all reviews, '2' to write a review: ");
 	    try {
 		choice = Integer.parseInt(raw_choice);
 		if (choice == 0){
 		    break;
 		}
 		if (choice == 1){
 		    list_reviews(email);
 		}
 		if (choice == 2){
 		    write_review(email);
 		}
 	    } catch (Exception e) {
 		e.printStackTrace();
 		System.out.println("Invalid input '"+raw_choice+"'");
 	    }
 	}
 	return;
     }
     
     public static void write_review(String reviewee) {
 	/**
 	   This function takes the class global userstate as the reviewer
 	   and writes a review for the reviewee
 	   This function assumes the two emails are valid.
 	   This function will not allow a user to write a review for himself.
 	 */
 	if(reviewee.equals(userstate)){
 	    System.out.println("You cannot write a review for yourself!");
 	    return;
 	}
 	// Write the review here
 	System.out.println("\nWriting review for '"+reviewee+"'...");
 	Integer rating = 0;
 	while(rating < 1 || rating > 5){
 	    String raw_rating = console.readLine("'0' for back, Enter rating(1-5): ").replaceAll("'", "").replace('"', '\0');
 	    try{
 		rating = Integer.parseInt(raw_rating);
 		if (rating == 0){
 		    System.out.println("Back...");
 		    return;
 		}
 	    } catch (Exception e){
 		//e.printStackTrace();
 		System.out.println("Invalid input '"+raw_rating+"'");
 	    }
 	}
 	// At this point, rating is valid. Enter text
 	String rtext = null;
 	while (true){
 	    rtext = console.readLine("Enter review text (max 80 char): ").
 		replaceAll("'", "").replace('"', '\0');
 	    if(rtext.length()>80){
 		System.out.println("Review too long! (length: '"+rtext.length()+"'");
 	    } else {
 		break;
 	    }
 	}
 	// At this point, text and rating is valid. Find new review ID
 	String RNOQuery = "SELECT MAX(RNO) FROM REVIEWS";
 	Integer newRNO = null;
 	try{
 	    rset = stmt.executeQuery(RNOQuery);
 	    if(rset.next()){
 		newRNO = rset.getInt(1) + 1;
 	    } else {
 		// This line will occur if there are no reviews in the table
 		newRNO = 1;
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	    return;
 	}
 	// Write the query/review here
 	String reviewquery = "INSERT INTO reviews (RNO, RATING, TEXT, REVIEWER, REVIEWEE, RDATE) " + 
 	    "VALUES ('" + newRNO + "', '" +
 	    rating + "', '" +
 	    rtext + "', '" + 
 	    userstate + "', '" +
 	    reviewee + "', " + 
 	    "CURRENT_TIMESTAMP)";
 	try{
 	    stmt.executeUpdate(reviewquery);
 	    System.out.println("Sucessfully wrote a review for '" + reviewee + "'!");
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	    return;
 	}
 	return;
     }
     
     public static void list_reviews(String reviewee){
 	/**
 	   This function takes the reviewee and prints
 	   all the reviews reviewing the reviewee
 	 */
 	System.out.println("\nListing reviews for '"+reviewee+"'...");
 	
 	String display_reviews = "SELECT rdate, rating, reviewer, text FROM reviews WHERE "
 	    + "lower(reviewee) = lower('" + reviewee + "')";
 	
 	try{
 	    rset = stmt.executeQuery(display_reviews);
 	    if(rset.next()){
 		rset.previous();
 		System.out.println("Review Date           | Rating | Reviewer             | Text");
 		while(rset.next()){
 		    String rdateshort = rset.getString("rdate");
 		    if (rdateshort.length() < 21){
 			Integer spaces = 21 - rdateshort.length();
 			for (int i = 0; i<spaces; i++){
 			    rdateshort = rdateshort+" ";
 			}
 		    }
 		    System.out.println(rdateshort + " | " +
 				       rset.getInt("rating") + "      | " + 
 				       rset.getString("reviewer") + " | " +
 				       rset.getString("text"));
 		}
 	    } else {
 		System.out.println("No reviews to show!");
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}	
 	return;
     }
     
     public static void ad_post(){
 	/**
 	   Posting an ad:
 	   Select an ad type (S or W):
 	   Enter in the title, price, description, location 
 	   Select a category or create a new category
 	   Have the program generate the aid and poster.
 	 */
 	// Select an ad type
 	String adtype = null;
 	while(true){
 	    adtype = console.readLine("'0' for quit, else select an Ad Type (s, w): ").
 		replaceAll("'", "").replace('"', '\0');
 	    if (adtype.equals("0")){
 		System.out.println("Back...");
 		return;
 	    }
 	    if (adtype.toUpperCase().equals("S")||adtype.toUpperCase().equals("W")){
 		adtype = adtype.toUpperCase();
 		break;
 	    } else {
 		System.out.println("Invalid input '" + adtype + "'");
 	    }
 	}
 	// Enter in the title
 	String adtitle = null;
 	while(true){
 	    adtitle = console.readLine("'0' for quit, else enter ad title (max 20 char): ").
 		replaceAll("'", "").replace('"', '\0');
 	    if (adtitle.equals("0")){
 		System.out.println("Back...");
 		return;
 	    }
 	    if (adtitle.length() > 20) {
 		System.out.println("Title too long! (length: " + adtitle.length() + ")");
 	    } else {
 		break;
 	    }
 	}
 	// Enter in description
 	String addesc = null;
 	while(true){
 	    addesc = console.readLine("'0' for quit, else enter description (max 40 char): ").replaceAll("'", "").replace('"', '\0');
 	    if (addesc.equals("0")){
 		System.out.println("Back...");
 		return;
 	    }
 	    if (addesc.length() > 40) {
 		System.out.println("Description too long! (length: " + addesc.length() 
 				   + ")");
 	    } else {
 		break;
 	    }
 	}
 	// Enter in location
 	String adlocation = null;
 	while(true){
 	    adlocation = console.readLine("'0' for quit, else enter location (max 15 char): ").replaceAll("'", "").replace('"', '\0');
 	    if (adlocation.equals("0")){
 		System.out.println("Back...");
 		return;
 	    }
 	    if (adlocation.length() > 15) {
 		System.out.println("Location too long! (length: " + adlocation.length() + ")");
 	    } else {
 		break;
 	    }
 	}
 	// Enter in price
 	Integer adprice = null;
 	while(true){
 	    String rawadprice = console.readLine("'q' for quit, else enter price: ").
 		replaceAll("'", "").replace('"', '\0');
 	    if(rawadprice.equals("q")){
 		System.out.println("Back...");
 		return;
 	    }
 	    try {
 		adprice = Integer.parseInt(rawadprice);
 		break;
 	    } catch (Exception e) {
 		// e.printStackTrace();
 		System.out.println("Invalid input '" + rawadprice + "'" );
 	    }
 	}
 
 	// Select the category to post in, or create a new category
 	String adcat = null;
 	String findcategories = "SELECT CAT, SUPERCAT from categories";
 	try {
 	    rset = stmt.executeQuery(findcategories);
 	    Integer catselect = 1;
 	    System.out.println("# | Category   | SuperCategory");
 	    while(rset.next()) {
 		System.out.println(catselect + " | " + rset.getString("CAT") + " | " + 
 				   rset.getString("SUPERCAT"));
 		catselect++;
 	    }
 	    Integer userselect = null;
 	    while(true){
 		String rawuserselect = console.readLine("'0' for back, 'n' for new category, \nelse select category number (1-" + (catselect - 1)+ "): ");
 		if (rawuserselect.equals("0")) {
 		    System.out.println("Back...");
 		    return;
 		}
 		if (rawuserselect.equals("n")) {
 		    adcat = new_category();
 		    if (adcat == null) {
 			System.out.println("No new category created...");
 			continue;
 		    } else {
 			System.out.println("Selected category '" + adcat + "'");
 			break;
 		    }
 		}
 		try {
 		    userselect = Integer.parseInt(rawuserselect);
 		    if (userselect < 1 || userselect > (catselect -1)){
 			System.out.println("Category selection out of range! (value: '" 
 					   + userselect +")");
 			continue;
 		    }
 		} catch (Exception e) {
 		    //e.printStackTrace();
 		    System.out.println("Invalid input '" + rawuserselect + "'");
 		    continue;
 		}
 		Integer moveback = catselect - userselect;
 		for (int i = 0; i < moveback; i++){
 		    rset.previous();
 		}
 		adcat = rset.getString("CAT").trim();
 		System.out.println("Selected category '" + adcat + "'");
 		break;
 	    }
 	    
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	// Find the new aid
 	String findmaxaid = "SELECT MAX(aid) FROM ads";
 	String newaid = null;
 	Integer aidval = null;
 	try {
 	    rset = stmt.executeQuery(findmaxaid);
 	    if (rset.next()){
 		String rawaid = rset.getString(1);
 		// remove the leading 'a'
 		rawaid = rawaid.substring(1);
 		try {
 		    aidval = Integer.parseInt(rawaid);
 		    aidval++;
 		    newaid = "a"+aidval;
 		} catch (Exception e) {
 		    e.printStackTrace();
 		    System.out.println("Parsed aid value is not numerical!");
 		}
 	    } else {
 		newaid = "a001";
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	// Create the query, send to database
 	String newadquery = "INSERT INTO ads (AID, ATYPE, TITLE, PRICE, DESCR, LOCATION, PDATE, CAT, POSTER) VALUES (" + 
 	    "'" + newaid + "', " + 
 	    "'" + adtype + "', " + 
 	    "'" + adtitle + "', " + 
 	    "'" + adprice + "', " + 
 	    "'" + addesc + "', " + 
 	    "'" + adlocation + "', " +
 	    "CURRENT_TIMESTAMP, " + 
 	    "'" + adcat + "', " + 
 	    "'" + userstate + "')";
 	try {
 	    stmt.executeUpdate(newadquery);
 	    System.out.println("Ad sucessfully posted!");
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	    System.out.println("Failed to post ad.");
 	}
 	return;
     }
 
     public static String new_category(){
 	/**
 	   This function allows a user to create a new category.
 	 */
 	String newcat = null;
 	String newsupcat = null;
 	String hassuper = null;
 	System.out.println("Creating new category...");
 	while (true) {
 	    newcat = console.readLine("'0' for quit, else enter new category name (max char 10): ").replaceAll("'", "").replace('"', '\0');
 	    if (newcat.equals("0")){
 		System.out.println("Back...");
 		return null;
 	    }
 	    if (newcat.length() > 10){
 		System.out.println("Category name too long (value: " + newcat.length() 
 				   + ")");
 		continue;
 	    }
 	    // check if this value is already a category
 	    String checkcat = "SELECT * from categories WHERE LOWER('CAT') LIKE LOWER('" 
 		+ newcat + "')";	
 	    try {
 		rset = stmt.executeQuery(checkcat);
 		if (rset.next()){
 		    System.out.println("Category '" + newcat + "' already exists!");
 		} else {
 		    break;
 		}
 	    } catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	}
 	// Prompt if this category is a sub category of a supercategory
 	while (true) {
 	    hassuper = console.readLine("'0' for quit, else does this category have a supercategory (y/n): ").replaceAll("'", "").replace('"', '\0');
 	    //hassuper = "y";
 	    if (hassuper.equals("0")){
 		System.out.println("Back...");
 		return null;
 	    }
 	    if (hassuper.equals("n")){
 		break;
 	    }
 	    if (hassuper.equals("y")){
 		// Find the supercategory of this category
 		// Supercategories must not have supercategories
 		String listsupercat = "SELECT cat FROM categories WHERE supercat IS null";
 		Integer scatselect = 1;
 		try {
 		    rset = stmt.executeQuery(listsupercat);
 		    System.out.println("# | Super Category");
 		    while(rset.next()){
 			System.out.println(scatselect + " | " + rset.getString("CAT"));
 			scatselect++;
 		    }
 		    Integer selectscat = null;
 		    while(true){
 			// Select the supercategory
 			String raw_selectscat = console.readLine("'0' for back, else select supercategory (1-" + (scatselect-1) + "): ");
 			try {
 			    selectscat = Integer.parseInt(raw_selectscat);
 			    if (selectscat<1 || selectscat>(scatselect-1)){
 				System.out.println("Selection out of range (value: " + selectscat + ")");
 			    } else {
 				break;
 			    }
 			} catch (Exception e){
 			    //e.printStackTrace();
 			    System.out.println("Invalid input '" + raw_selectscat + "'");
 			}
 		    }
 		    Integer moveback = scatselect - selectscat;
 		    for (int i=0; i<moveback; i++){
 			rset.previous();
 		    }
 		    newsupcat = rset.getString("CAT");		    
 		    System.out.println("Supercat assigned to be '" + newsupcat + "'");
 		    break;
 		} catch (SQLException e) {
 		    e.printStackTrace();
 		}		
 	    } else {
 		System.out.println("Invalid input '"+ hassuper +"'");
 	    } 
 	}
 	// Push the new category/supercat to the database
 	
 	String newcategory = null;
 	if (newsupcat == null) {
 	    newcategory = "INSERT INTO categories (CAT) VALUES (lower('" + newcat + "'))";
 	} else {
 	    newcategory = "INSERT INTO categories (CAT, SUPERCAT) VALUES(lower('" + newcat + 
 		"'), lower('" + newsupcat + "'))";
 	}
 	// System.out.println(newcategory);
 	try{
 	    stmt.executeUpdate(newcategory);
 	    System.out.println("Sucessfully created new category");
 	} catch (SQLException e){
 	    e.printStackTrace();
 	}
 	return newcat;
     }
 }
 
