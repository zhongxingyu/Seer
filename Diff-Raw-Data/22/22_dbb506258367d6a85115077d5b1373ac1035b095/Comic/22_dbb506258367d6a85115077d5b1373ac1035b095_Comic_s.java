 package com.tekton.comicrate.forms;
 
 import com.tekton.comicrate.*;
 import com.tekton.comicrate.home.admin.*;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.ResultSet;
 //import java.sql.ResultSetMetaData; //left in for debugging reasons
 import java.sql.PreparedStatement;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 public class Comic extends SQLController {
 	
 	public String id;
 	public String title;
 	public String note;
 	public String overall;
 	public String art;
 	public String story;
 	public String likely_to_buy_next;
 	public String img_url;
 	public String issue_number;
 	public String volume;
 	public String publisher;
 	
 	public String year;
 	
 	public String do_not_increment;
 	public String mini_series_max;
 	public String mini_series_name;
 	public String New52;
 	
 	public String own_physical; //should only be for "user" implementations
 	
 	public String max; //slightly different than mini_series_max, for processing series as well as mini-series
 	
 	public String overall_avg;
 	public String story_avg;
 	public String art_avg;
 
 	public String user;
 	public Boolean has_rated = false;
 	public Boolean comic_exists = true;
 	
 	public Map<Integer,HomeFile> files;
 	public Map<Integer,HomeFile> confirmed_files;
 	
 	public String getId() {
 		return id;
 	}
 	public void setId(String id) {
 		this.id = id;
 	}
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public String getNote() {
 		return note;
 	}
 	public void setNote(String note) {
 		this.note = note;
 	}
 	public String getOverall() {
 		return overall;
 	}
 	public void setOverall(String overall) {
 		this.overall = overall;
 	}
 	public String getArt() {
 		return art;
 	}
 	public void setArt(String art) {
 		this.art = art;
 	}
 	public String getStory() {
 		return story;
 	}
 	public void setStory(String story) {
 		this.story = story;
 	}
 	public String getLikely_to_buy_next() {
 		return likely_to_buy_next;
 	}
 	public void setLikely_to_buy_next(String likely_to_buy_next) {
 		this.likely_to_buy_next = likely_to_buy_next;
 	}
 	public String getImg_url() {
 		return img_url;
 	}
 	public void setImg_url(String img_url) {
 		this.img_url = img_url;
 	}
 	
 	public String getIssue_number() {
 		return issue_number;
 	}
 	public void setIssue_number(String issue_number) {
 		this.issue_number = issue_number;
 	}
 	
 	public String getOwn_physical() {
 		if(this.own_physical==null) {
 			return "0";
 		}
 		return own_physical;
 	}
 	public void setOwn_physical(String own_physical) {
 		this.own_physical = own_physical;
 	}
 	public String getDo_not_increment() {
 		return do_not_increment;
 	}
 	public void setDo_not_increment(String do_not_increment) {
 		this.do_not_increment = do_not_increment;
 	}
 	public String getMini_series_max() {
 		return mini_series_max;
 	}
 	public void setMini_series_max(String mini_series_max) {
 		this.mini_series_max = mini_series_max;
 	}
 	public String getNew52() {
 		return New52;
 	}
 	public void setNew52(String new52) {
 		New52 = new52;
 	}
 	public String getMax() {
 		return max;
 	}
 	public void setMax(String max) {
 		this.max = max;
 	}
 	public String getPublisher() {
 		return publisher;
 	}
 	public void setPublisher(String publisher) {
 		this.publisher = publisher;
 	}
 	public String getMini_series_name() {
 		return mini_series_name;
 	}
 	public void setMini_series_name(String mini_series_name) {
 		this.mini_series_name = mini_series_name;
 	}
 	public String getVolume() {
 		return volume;
 	}
 	public void setVolume(String volume) {
 		this.volume = volume;
 	}
 	public String getYear() {
 		return year;
 	}
 	public void setYear(String year) {
 		this.year = year;
 	}
 	public String getOverall_avg() {
 		return overall_avg;
 	}
 	public void setOverall_avg(String overall_avg) {
 		this.overall_avg = overall_avg;
 	}
 	public String getStory_avg() {
 		return story_avg;
 	}
 	public void setStory_avg(String story_avg) {
 		this.story_avg = story_avg;
 	}
 	public String getArt_avg() {
 		return art_avg;
 	}
 	public void setArt_avg(String art_avg) {
 		this.art_avg = art_avg;
 	}
 	public String getUser() {
 		return user;
 	}
 	public void setUser(String user) {
 		this.user = user;
 	}
 	public Boolean getHas_rated() {
 		return has_rated;
 	}
 	public void setHas_rated(Boolean has_rated) {
 		this.has_rated = has_rated;
 	}
 	public Boolean getComic_exists() {
 		return comic_exists;
 	}
 	public void setComic_exists(Boolean comic_exists) {
 		this.comic_exists = comic_exists;
 	}
 	public String getCONNECTION_URL() {
 		return CONNECTION_URL;
 	}
 	public void setCONNECTION_URL(String cONNECTION_URL) {
 		CONNECTION_URL = cONNECTION_URL;
 	}
 	public Connection getConn() {
 		return conn;
 	}
 	public void setConn(Connection conn) {
 		this.conn = conn;
 	}
 	public Map<Integer, HomeFile> getFiles() {
 		return files;
 	}
 	public void setFiles(Map<Integer, HomeFile> files) {
 		this.files = files;
 	}
 	public Map<Integer, HomeFile> getConfirmed_files() {
 		return confirmed_files;
 	}
 	public void setConfirmed_files(Map<Integer, HomeFile> confirmed_files) {
 		this.confirmed_files = confirmed_files;
 	}
 	
 	/**
 	 * This constructor isn't used as much anymore due to comics being created by code, not users
 	 */
 	public Comic() {
 		/* By not having a connection get made it will break things that don't pass a connection! */
 		//this.createConnection(); //Now I remember, it's for when things are made via spring Model...but it's still not great...		
 		
 		this.New52 = "0";
 		this.do_not_increment = "0";
 		
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		this.user = auth.getName(); //get logged in username; we'll be needing that to find the comic...
 	}
 	
 	/**
 	 * Create a basic Comic object that has all of the queries and data needed
 	 * @param conn The database connection to use
 	 */
 	public Comic(Connection conn) {
 		/* Since Java doesn't really have ref's or pointers, this is "good enough" it would seem... */
 		//System.out.println("Passing a connection to save on connections!"); //not needed as everything uses this method now...leaving for debugging of old logs
 		
 		this.conn = conn;
 		
 		this.New52 = "0";
 		this.do_not_increment = "0";
 		
 		this.files = new HashMap<Integer,HomeFile>();
 		
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		this.user = auth.getName(); //get logged in username; we'll be needing that to find the comic...
 	}
 	
 	/**
 	 * Basic system.out report of the comic in question
 	 * 
 	 * @return String with basic report of title, issue number, and the overall score
 	 */
 	public String reportComic() {
 		return "Report:: " + this.title + " " + this.issue_number + " " + this.overall;
 	}
 	
 	/**
 	 * Has all of the DB logic needed to get the information about the comic, as well as averages information for all users
 	 * 
 	 * Depends on HomeFilesShow by default
 	 */
 	public void getComicFromDB() {
 		//take the info in "this" to search and populate data
 		// TODO: Add "comic_exists" flag tracking somewhere in here...
 		try {
 			//Class.forName( "com.mysql.jdbc.Driver" ).newInstance();
 			
 			//create a local version of the connection which should be released when the function is done
 			Connection conn = this.conn;
 			Statement  stmt = conn.createStatement();
 			ResultSet result = null;
 			
 			//create a query to get the comic info and the user opinions
 			String q = "SELECT u.id, c.title, u.overall, u.art, u.story, u.note, u.own_physical, u.likely_to_buy_next, c.img_url, c.issue_number, c.volume, c.year, c.New52"
 					+" FROM comic as c" 
 					+" left join (select id, comic, overall, art, story, note, own_physical, likely_to_buy_next from user_ratings where user='"+this.user+"') as u on u.comic = c.id"
 					+" where c.id = '"+this.id+"'";
 			
 			try {
 				//String q = "SELECT * FROM comic where id="+this.id;
 				System.out.println( "Query: " + q );
 				//stmt.executeUpdate( q );
 				result = stmt.executeQuery(q);
 			} catch(SQLException e) {
 				//basically "what went wrong this time?!"
 				System.out.println( "SQLException: " + e.getMessage() );
 				System.out.println( "SQLState:     " + e.getSQLState() );
 				System.out.println( "VendorError:  " + e.getErrorCode() );
 			}
 			
 			while( result.next() ) {
 				this.setTitle(result.getString("title"));
 				this.setOverall(result.getString("overall"));
 				this.setArt(result.getString("art"));
 				this.setStory(result.getString("story"));
 				this.setNote(result.getString("note"));
 				this.setImg_url(result.getString("img_url"));
 				this.setIssue_number(result.getString("issue_number"));
 				this.setLikely_to_buy_next(result.getString("likely_to_buy_next"));
 			}
 		} catch( Exception e ) {
 			System.out.println( "Something broke in COMIC.JAVA" );
 			System.out.println( e.getMessage() );
 			e.printStackTrace();
 		}
 		
 		/* to help keep code a little clean, and to allow it to be run separately and more often, averages are their own function */
 		this.get_average();
 		
 		/**
 		 * Get the data from the "local" files as well...
 		 * TODO: create a setting flag depending on where things are installed if this even runs
 		 */
 		HomeFilesShow x = new HomeFilesShow();
 		this.files = x.show_similar_files(this.title, this.issue_number, this.id);
 		this.confirmed_files = x.show_confirmed_files(this.id);
 	}
 	
 	/**
 	 * The actual act of putting a comic in a database
 	 * 
 	 * @return rtn_str is returned, though as of right now it's always blank
 	 */
 	public String putComicInDB() {
 		//take the info in "this" and create an entry
 		//need to check for exceptions, like it already exists!
 		
 		String rtn_str = "";
 		Integer s = 0;
 		
 		try {
 			Class.forName( "com.mysql.jdbc.Driver" ).newInstance();
 			Connection conn = this.conn; //DriverManager.getConnection( this.CONNECTION_URL );
 			//Statement  stmt = conn.createStatement(); //using name as it's easier when browsing documentation
 			ResultSet result = null;
 			int autoIncKeyFromApi = -1;
 			
 			PreparedStatement pst = null;
 			
 			String sql = "INSERT INTO comic (`title`, `issue_number`, `volume`,`publisher`,`year`) VALUES(?,?,?,?,?)";
 			
 			pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
 			pst.setString(1, this.title);
 			pst.setString(2, this.issue_number);
 			pst.setString(3, this.volume);
 			pst.setString(4, this.publisher);
 			pst.setString(5, this.year);
 			
 			/*
 				String q = "INSERT INTO Comic (`title`," +
 					"`issue_number`," +
 					"`overall`" +
 					",`art`" +
 					",`story`, `note`, `likely_to_buy_next`) VALUES ("+
 					"'"+this.title+"',"+
 					"'"+this.issue_number+"',"+
 					"'"+this.overall+"',"+
 					"'"+this.art+"',"+
 					"'"+this.story+"',"+
 					"'"+this.note+"',"+
 					"'"+this.likely_to_buy_next+"'"+
 					")";*/
 			
 			try {
 				//System.out.println( "Query: " + q );
 				System.out.println( pst.toString() );
 				s = pst.executeUpdate();
 				result = pst.getGeneratedKeys();
 			} catch(SQLException e) {
 				//basically "what went wrong this time?!"
 				System.out.println( "SQLException: " + e.getMessage() );
 				System.out.println( "SQLState:     " + e.getSQLState() );
 				System.out.println( "VendorError:  " + e.getErrorCode() );
 			}
 			
 			if(s > 0) {
 				System.out.println("It's in there! Should redirect to that view...");
 				
 				if(result.next()) {
 					autoIncKeyFromApi = result.getInt(1);
 					this.id = Integer.toString(autoIncKeyFromApi);
 					System.out.println("Key returned from getGeneratedKeys(): " + autoIncKeyFromApi);
 					
 					if(this.do_not_increment == "1") {
 						this.updateComicColumn("do_not_increment", this.do_not_increment);
 					}
 					
 					if(this.mini_series_max != "" && this.mini_series_max != "0") {
 						this.updateComicColumn("mini_series_max", this.mini_series_max);
 					}
 					
 					if(this.New52 != null && this.New52 != "" && this.New52 != "null" && this.New52 != "0") { //this.New52 == "1" || 
 						System.out.println(this.New52);
 						this.updateComicColumn("New52", "1");
 					}
 					
 					return Integer.toString(autoIncKeyFromApi);
 				} else {
 					System.out.println("No ID returned?!");
 				}
 			} else {
 				System.out.println("Dude, it didn't go in...");
 			}
 			
 		} catch( Exception e ) {
 			System.out.println( "Something broke...putComicInDB" );
 			System.out.println( e.getMessage() );
 			e.printStackTrace();
 		}		
 		
 		return rtn_str;
 	}
 
 	/**
 	 * If just an individual column needs to be updated, like in a JSON call, this is the function to use
 	 * 
 	 * @param column Name of the column to update
 	 * @param data Data to update to
 	 * @return Success code
 	 */
 	public String updateComicColumn(String column, String data) {
 		//TODO: check to see if column exists!
 		Integer s = 0;
 		
 		try {
 			//notes for connections are found in other classes...since most are copy pasted they all work about the same :)
 			Class.forName( "com.mysql.jdbc.Driver" ).newInstance();
 			//Connection conn = this.conn;
 			String sql = "UPDATE comic SET "+column+"=? WHERE id=?";
 			PreparedStatement pst = this.conn.prepareStatement(sql);
 			
 			//pst.setString(1, column);
 			pst.setString(1, data);
 			pst.setString(2, this.id);
 			
 			try {
 				System.out.println( pst.toString() );
 				s = pst.executeUpdate();
 			} catch(SQLException e) {
 				//basically "what went wrong this time?!"
 				System.out.println( "SQLException: " + e.getMessage() );
 				System.out.println( "SQLState:     " + e.getSQLState() );
 				System.out.println( "VendorError:  " + e.getErrorCode() );
 			}
 			
 			if(s > 0) {
 				System.out.println(column+" has been updated, see above for query values");
 			} else {
 				System.out.println("Dude, it didn't go in...a horrible looking error will be thrown now. GG.");
 				return "bad";
 			}
 			
 		} catch( Exception e ) {
 			System.out.println( "Something broke...updateComicColumn" );
 			System.out.println( e.getMessage() );
 			e.printStackTrace();
 		}
 		return "good";
 	}
 	
 	/**
 	 * Update all of the base information about a Comic all at once
 	 * Note: this is the BASE information NOT user set information
 	 * @return This can probably go away really, or become a bool...
 	 */
 	public String updateComic() {
 		//take the info in "this" and create an entry
 		//need to check for exceptions, like it already exists!
 		//TODO switch to bool
 		Integer s = 0;
 		
 		try {
 			Class.forName( "com.mysql.jdbc.Driver" ).newInstance();
 			Connection conn = this.conn; //DriverManager.getConnection( this.CONNECTION_URL );
 			//Statement  stmt = conn.createStatement(); //using name as it's easier when browsing documentation
 			
 			PreparedStatement pst = null;
 			
 			String sql = "UPDATE comic SET title=?, issue_number=?, overall=?, art=?, story=?, img_url=?, note=?, likely_to_buy_next=? WHERE id=?";
 			pst = conn.prepareStatement(sql);
 			pst.setString(1, this.title);
 			pst.setString(2, this.issue_number);
 			pst.setString(3, this.overall);
 			pst.setString(4, this.art);
 			pst.setString(5, this.story);
 			pst.setString(6, this.img_url);
 			pst.setString(7, this.note);
 			pst.setString(8, this.likely_to_buy_next);
 			pst.setString(9, this.id);
 			
 			String q = "UPDATE Comic " +
 					"SET " +
 					"title=\"" + this.title+ "\", " +
 					"note=\"" + this.note+ "\", " +
 					"overall=\"" + this.overall+ "\", " +
 					"art=\"" + this.art+ "\", " +
 					"img_url=\"" + this.img_url+ "\", " +
 					"likely_to_buy_next=\"" + this.likely_to_buy_next + "\", " +
 					"issue_number=\"" + this.issue_number + "\" " +
 					"WHERE id=" + this.id;
 			
 			try {
 				System.out.println( "Query: " + q );
 				//s = stmt.executeUpdate(q);
 				s = pst.executeUpdate();
 			} catch(SQLException e) {
 				//basically "what went wrong this time?!"
 				System.out.println( "SQLException: " + e.getMessage() );
 				System.out.println( "SQLState:     " + e.getSQLState() );
 				System.out.println( "VendorError:  " + e.getErrorCode() );
 			}
 			
 			if(s > 0) {
 				System.out.println("It's UPDATED! Will redirect to that view...");
 			} else {
 				System.out.println("Dude, it didn't go in...a horrible looking error will be thrown now. GG.");
 			}
 			
 		} catch( Exception e ) {
 			System.out.println( "Something broke...updateComic" );
 			System.out.println( e.getMessage() );
 			e.printStackTrace();
 		}		
 		
 		return "Good";
 	}
 	
 	/**
 	 * There is a need for many view to have the global average for a comic books ratings
 	 * Given how often these are to be shown, the most "parent" of the comic versions is chosen to have the function
 	 * This should allow it to be shown anywhere and everywhere easily with JSP
 	 */
 	public void get_average() {
 		String q = "select avg(overall), avg(story), avg(art) from user_ratings where comic=?";
 		
 		try {
 			System.out.println("attempting to get averages..."+q);
 			PreparedStatement pst = null;
 			ResultSet result = null;
 			pst = this.conn.prepareStatement(q);
 			
 			pst.setString(1, this.id);
 			
 			try {
 				System.out.println(pst.toString());
 				//s = stmt.executeUpdate(q);
 				result = pst.executeQuery();
 			} catch(SQLException e) {
 				//basically "what went wrong this time?!"
 				System.out.println( "SQLException: " + e.getMessage() );
 				System.out.println( "SQLState:     " + e.getSQLState() );
 				System.out.println( "VendorError:  " + e.getErrorCode() );
 			}
 			
 			while( result.next() ) {
 				
 				/* Debug Loop **
 				ResultSetMetaData md = result.getMetaData ();
 				// Get the number of columns in the result set
 				int numCols = md.getColumnCount();
 				int i;
 				for (i=1; i<=numCols; i++) {
 					//if (i > 1) System.out.print(",");
 					//System.out.println(md.getColumnLabel(i));
 					System.out.println(md.getColumnLabel(i)+" , "+result.getString(md.getColumnLabel(i)));
 				}
 				*//* End Debug Loop **/
 				
 				
 				this.setOverall_avg(result.getString("avg(overall)"));
 				this.setStory_avg(result.getString("avg(story)"));
 				this.setArt_avg(result.getString("avg(art)"));
 				
 			}
 		} catch( Exception e ) {
 			System.out.println( "Something broke...Comic::get_averages" );
 			System.out.println( e.getMessage() );
 			e.printStackTrace();
 		}		
 		
 	}
 	
 	
 	//moved over from the phased out ComicUser
 	
 	public void updateUserComic() {
 		//TODO: validate that it's the correct user doing the updating...
 		//insert, and if that doesn't work, update!
 		
 		this.createConnection();
 		
 		try {	
 		String sql = "INSERT into user_ratings"+
 				"(`user`,`comic`,`overall`,`art`,`story`,`note`,`own_physical`,`likely_to_buy_next`)"+
 				" VALUES (?,?,?,?,?,?,?,?)"+
 				"ON DUPLICATE KEY UPDATE overall=?, art=?, story=?, note=?, own_physical=?, likely_to_buy_next=?";
 		PreparedStatement pst = this.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
 		pst.setString(1, this.user);
 		pst.setString(2, this.id);
 		pst.setString(3, this.overall);
 		pst.setString(4, this.art);
 		pst.setString(5, this.story);
 		pst.setString(6, this.note);
 		pst.setString(7, this.getOwn_physical());
 		pst.setString(8, this.likely_to_buy_next);
 		
 		pst.setString(9, this.overall);
 		pst.setString(10, this.art);
 		pst.setString(11, this.story);
 		pst.setString(12, this.note);
 		pst.setString(13, this.own_physical);
 		pst.setString(14, this.likely_to_buy_next);
 		
 		System.out.println(pst.toString());
 		
 		try {
 			pst.executeUpdate(); 
 			pst.getGeneratedKeys(); //this doesn't really matter at the moment...
 		} catch(SQLException e) {
 			//basically "what went wrong this time?!"
 			System.out.println( "SQLException: " + e.getMessage() );
 			System.out.println( "SQLState:     " + e.getSQLState() );
 			System.out.println( "VendorError:  " + e.getErrorCode() );
 		}
 		
 		} catch( Exception e ) {
 			System.out.println( "Something broke in COMIC.USER.JAVA during an insert/update" );
 			System.out.println( e.getMessage() );
 			e.printStackTrace();
 		}
 		
 		this.closeConnection();
 	}
 	
 	
 	/** 
 	 * Quick and dirty validation for showing the "right" value for stars 
 	 * 
 	 * Should probably just pass each "column" of the object separately, but meh...
 	 * 
 	 * **/
 	public void validate_to_zero() {
 		//System.out.println("Validating..."); //DBUG
 		
 		this.overall = this.validate_var(this.overall);
 		this.art = this.validate_var(this.art);
 		this.story = this.validate_var(this.story);
 		this.likely_to_buy_next = this.validate_var(this.likely_to_buy_next);
 		
 		this.overall_avg = this.validate_var(this.overall_avg);
 		this.art_avg = this.validate_var(this.art_avg);
 		this.story_avg = this.validate_var(this.story_avg);
 	}
 	
 	public String validate_var(String var) {
 		if(var == null) {
 			return "0";
 		} else {
 			return var;
 		}
 	}
 	
 	/**
 	 * Really only used by the JSP's, this is all the backend html to make the javascript and css work on pages where things aren't editable...
 	 * @param var Name of the variable to use
 	 * @param c_name Column name
 	 * @return The string for the stars display
 	 */
 	public String create_stars_input(String var, String c_name) {
 		String stars = "<div id=\""+this.id+"-stars-"+c_name+"\">"+
         "<select name=\""+c_name+"-sel\" id=\""+this.id+c_name+"\">"+
 	    "    <option value=\"1\">Very poor</option>"+
 	    "    <option value=\"2\">Poor</option>"+
 	    "    <option value=\"3\">Not that bad</option>"+
 	    "    <option value=\"4\">Fair</option>"+
 	    "    <option value=\"5\">Average</option>"+
 	    "    <option value=\"6\">Almost Good</option>"+
 	    "    <option value=\"7\">Good</option>"+
 	    "    <option value=\"8\">Very Good</option>"+
 	    "    <option value=\"9\">Excellent</option>"+
 	    "    <option value=\"10\">Perfect</option>"+
 		"    </select>"+
 		"    <input type=\"hidden\" name=\""+c_name+"\" id=\""+this.id+"-hidden-"+c_name+"\" value=\""+this.validate_var(var)+
 		"\"/>"+
 		"<script>\n$(\"#"+this.id+"-stars-"+c_name+"\").stars({"+
 		"	inputType: \"select\","+
 		"	cancelShow: false,"+
 		"	split: 2,"+
 		"	disabled: true"+
 		"});\n"+
 		"$(\"#"+this.id+"-stars-"+c_name+"\").stars(\"select\", Math.round("+this.validate_var(var)+"));</script>"+
 		"</div>";
 		
 		return stars;
 	}
 	
 	/**
 	 * 
 	 * JSP output for user inputs with custom functions and jquery ajax/callback
 	 * 
 	 * @param var
 	 * @param c_name
 	 * @return
 	 */
 	public String create_stars_user_input(String var, String c_name) {
 		String stars = "<div id=\""+this.id+"-stars-"+c_name+"\">"+
 		        "<select name=\""+c_name+"-sel\" id=\""+this.id+c_name+"\">"+
 			    		"<option value=\"1\">Very poor</option>"+
 			    		"<option value=\"2\">Poor</option>"+
 			    		"<option value=\"3\">Not that bad</option>"+
 			    		"<option value=\"4\">Fair</option>"+
 			    		"<option value=\"5\">Average</option>"+
 			    		"<option value=\"6\">Almost Good</option>"+
 			    		"<option value=\"7\">Good</option>"+
 			    		"<option value=\"8\">Very Good</option>"+
 			    		"<option value=\"9\">Excellent</option>"+
 			    		"<option value=\"10\">Perfect</option>"+
 					"</select>"+
 					"<input type=\"hidden\" name=\""+c_name+"\" id=\""+this.id+"-hidden-"+c_name+"\" value=\""+this.validate_var(var)+"\"/>"+
 				"<script>"+
 				"$(\"#"+this.id+"-stars-"+c_name+"\").stars({"+
 					"inputType: \"select\","+
 					"cancelShow: false,"+
 					"split: 2,"+
 					"disabled: false,"+
 					"callback: function(ui, type, value){"+
 						"$(\""+this.id+"-stars-"+c_name+"\").hide();"+
 						"$(\""+this.id+c_name+"_loading\").show();"+
 						"$.post(\"/ComicRate/user/comic/json/update/"+c_name+"\"," +
 							"{'id':'"+this.id+"','val': value},"+
 							"function(data){" +
 								
 							"}"+
 						");"+
 					"}"+
 				"});"+
 				"$(\"#"+this.id+"-stars-"+c_name+"\").stars(\"select\", Math.round("+this.validate_var(var)+"));"+
 				"</script>"+
 				"</div>";
 		
 		stars += "<div class='loading' id='"+this.id+c_name+"_loading'>Loading...</div>";
 		
 				return stars;		
 	}
 }
