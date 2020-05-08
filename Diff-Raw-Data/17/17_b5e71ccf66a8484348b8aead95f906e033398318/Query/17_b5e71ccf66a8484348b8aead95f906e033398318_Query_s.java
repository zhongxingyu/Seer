 import java.util.Properties;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.text.DateFormat;
 
 import java.io.FileInputStream;
 
 /**
  * Runs queries against a back-end database
  */
 public class Query {
     private static Properties configProps = new Properties();
 
     private static String imdbUrl;
     private static String customerUrl;
 
     private static String postgreSQLDriver;
     private static String postgreSQLUser;
     private static String postgreSQLPassword;
 
     // DB Connection
     private Connection _imdb;
     private Connection _customer_db;
 
     // Canned queries
 
     private String _search_sql = "SELECT * FROM movie WHERE name ILIKE ? ORDER BY id";
     private PreparedStatement _search_statement;
 
     private String _get_movie_sql = "SELECT * FROM movie WHERE id = ?";
     private PreparedStatement _get_movie_statement;
 
     private String _director_mid_sql = "SELECT y.* "
                      + "FROM movie_directors x, directors y "
                      + "WHERE x.mid = ? and x.did = y.id";
     private PreparedStatement _director_mid_statement;
     
     private String _actor_mid_sql = "SELECT  a.* "
             + "FROM Actor a, Casts c "
             + "WHERE c.mid = ? and c.pid = a.id";
     private PreparedStatement _actor_mid_statement;
     
     private String _rental_mid_sql = "SELECT y.uid, y.returned "
             + "FROM rental y "
             + "WHERE y.mid = ? AND y.returned = 'f' ";
     private PreparedStatement _rental_mid_statement;
     
     
 //    Fast Search Queries
     private String _search_fast_sql = "SELECT * FROM movie WHERE name ILIKE ? ORDER BY id";
     private PreparedStatement _search_fast_statement;
 
     private String _director_fast_mid_sql = "SELECT m.id, d.* "
             + "FROM movie_directors md, directors d, movie m "
             + "WHERE m.name ILIKE ? and md.mid = m.id and d.id = md.did ORDER by m.id";
     private PreparedStatement _director_fast_mid_statement;
     
 
     private String _actor_fast_mid_sql = "SELECT  m.id, a.* "
         	+ "FROM Actor a, Casts c, Movie m "
     		+ "WHERE m.name ILIKE ? and c.mid = m.id and c.pid = a.id ORDER by m.id";
     
 
     private PreparedStatement _actor_fast_mid_statement;
     
     
 
     private String _customer_login_sql = "SELECT * FROM account WHERE email = ? and password = ?";
     private PreparedStatement _customer_login_statement;
 
     private String _customer_lookup_sql = "SELECT a.fname, a.lname FROM account a WHERE a.id = ?";
     private PreparedStatement _customer_lookup_statement;
 
     private String _get_all_plans_sql = "SELECT * FROM plans";
     private PreparedStatement _get_all_plans_statement;
 
     private String _is_valid_plan_sql = "SELECT * FROM plans WHERE id = ?";
     private PreparedStatement _is_valid_plan_statement;
 
     private String _is_valid_movie_sql = "SELECT * FROM movie WHERE id = ?"; 
     private PreparedStatement _is_valid_movie_statement; 
 
     private String _set_customer_plan_id_sql = "UPDATE account SET plan_id = ? WHERE id = ?";
     private PreparedStatement _set_customer_plan_id_statement;
 
     private String _get_customer_rentals_sql = "SELECT r.mid as mid FROM (SELECT * FROM rental WHERE returned = false) r WHERE r.uid = ? ORDER BY date";
     private PreparedStatement _get_customer_rentals_statement;
 
     private String _get_current_rentals_sql = "SELECT count(r.mid) FROM account a, (SELECT * FROM rental WHERE returned = false) r WHERE a.id = ? AND r.uid = a.id";
     private PreparedStatement _get_current_rentals_statement;
 
     private String _get_plan_limit_sql = "SELECT p.max FROM account a, plans p WHERE a.id = ? AND p.id = a.plan_id";
     private PreparedStatement _get_plan_limit_statement;
 
     private String _rent_movie_for_customer_sql = "INSERT INTO rental VALUES(?, ?, ?, false)";
     private PreparedStatement _rent_movie_for_customer_statement;
 
     private String _return_movie_for_customer_sql = "UPDATE rental SET returned = true WHERE uid = ? AND mid = ?"; //UPDATE not INSERT
     private PreparedStatement _return_movie_for_customer_statement; 
 
     private String _begin_transaction_read_write_sql = "BEGIN TRANSACTION READ WRITE";
     private PreparedStatement _begin_transaction_read_write_statement;
 
     private String _commit_transaction_sql = "COMMIT TRANSACTION";
     private PreparedStatement _commit_transaction_statement;
 
     private String _rollback_transaction_sql = "ROLLBACK TRANSACTION";
     private PreparedStatement _rollback_transaction_statement;
 
 
     public Query() {
     }
     /**********************************************************/
     /* Connections to postgres databases */
 
     public void openConnection() throws Exception {
         configProps.load(new FileInputStream("dbconn.config"));
         
         
         imdbUrl        = configProps.getProperty("imdbUrl");
         customerUrl    = configProps.getProperty("customerUrl");
         postgreSQLDriver   = configProps.getProperty("postgreSQLDriver");
         postgreSQLUser     = configProps.getProperty("postgreSQLUser");
         postgreSQLPassword = configProps.getProperty("postgreSQLPassword");
 
 
         /* load jdbc drivers */
         Class.forName(postgreSQLDriver).newInstance();
 
         /* open connections to TWO databases: imdb and the customer database */
         _imdb = DriverManager.getConnection(imdbUrl, // database
                 postgreSQLUser, // user
                 postgreSQLPassword); // password
 
         _customer_db = DriverManager.getConnection(customerUrl, // database
                 postgreSQLUser, // user
                 postgreSQLPassword); // password
     }
 
     public void closeConnection() throws Exception {
         _imdb.close();
         _customer_db.close();
     }
 
     /**********************************************************/
     /* prepare all the SQL statements in this method.
       "preparing" a statement is almost like compiling it.  Note
        that the parameters (with ?) are still not filled in */
 
     public void prepareStatements() throws Exception {
 
         _search_statement = _imdb.prepareStatement(_search_sql);
         _director_mid_statement = _imdb.prepareStatement(_director_mid_sql);
         _actor_mid_statement = _imdb.prepareStatement(_actor_mid_sql);
         _get_movie_statement = _imdb.prepareStatement(_get_movie_sql);
         
         _rental_mid_statement = _customer_db.prepareStatement(_rental_mid_sql);
         
 //        Fast Search Prepared Statements
         _search_fast_statement = _imdb.prepareStatement(_search_fast_sql);
         _director_fast_mid_statement = _imdb.prepareStatement(_director_fast_mid_sql);
         _actor_fast_mid_statement = _imdb.prepareStatement(_actor_fast_mid_sql);
 
         /* uncomment after you create your customers database */
 
         _customer_login_statement = _customer_db.prepareStatement(_customer_login_sql);
         _begin_transaction_read_write_statement = _customer_db.prepareStatement(_begin_transaction_read_write_sql);
         _commit_transaction_statement = _customer_db.prepareStatement(_commit_transaction_sql);
         _rollback_transaction_statement = _customer_db.prepareStatement(_rollback_transaction_sql);
 
 
         /* add here more prepare statements for all the other queries you need */
         _customer_lookup_statement = _customer_db.prepareStatement(_customer_lookup_sql);
         _get_all_plans_statement = _customer_db.prepareStatement(_get_all_plans_sql);
         _set_customer_plan_id_statement = _customer_db.prepareStatement(_set_customer_plan_id_sql);
         _is_valid_plan_statement = _customer_db.prepareStatement(_is_valid_plan_sql);
         _rent_movie_for_customer_statement = _customer_db.prepareStatement(_rent_movie_for_customer_sql);
         _get_current_rentals_statement = _customer_db.prepareStatement(_get_current_rentals_sql);
         _get_customer_rentals_statement = _customer_db.prepareStatement(_get_customer_rentals_sql);
         _get_plan_limit_statement = _customer_db.prepareStatement(_get_plan_limit_sql);
         _return_movie_for_customer_statement = _customer_db.prepareStatement(_return_movie_for_customer_sql); //added
         _is_valid_movie_statement = _imdb.prepareStatement(_is_valid_movie_sql); //added
     }
 
 
     /**********************************************************/
     /* suggested helper functions  */
 
     public int helper_compute_remaining_rentals(int cid) throws Exception {
         /* how many movies can she/he still rent ? */
         /* you have to compute and return the difference between the customer's plan
            and the count of oustanding rentals */
         _get_current_rentals_statement.clearParameters();
         _get_current_rentals_statement.setInt(1, cid);
         ResultSet res = _get_current_rentals_statement.executeQuery();
         res.next();
         int rentals = res.getInt(1);
         _get_plan_limit_statement.clearParameters();
         _get_plan_limit_statement.setInt(1, cid);
         res = _get_plan_limit_statement.executeQuery();
         res.next();
         return res.getInt(1) - rentals;
     }
 
     public String helper_compute_customer_name(int cid) throws Exception {
         /* you find  the first + last name of the current customer */
         _customer_lookup_statement.clearParameters();
         _customer_lookup_statement.setInt(1, cid);
         ResultSet res = _customer_lookup_statement.executeQuery();
         res.next();
         return (res.getString(1) + " " + res.getString(2));
 
     }
 
     public boolean helper_check_plan(int plan_id) throws Exception {
         /* is plan_id a valid plan id ?  you have to figure out */
         _is_valid_plan_statement.clearParameters();
         _is_valid_plan_statement.setInt(1, plan_id);
         ResultSet plan = _is_valid_plan_statement.executeQuery();
         return plan.next();
     }
 
     public boolean helper_check_movie(int mid) throws Exception {
         /* is mid a valid movie id ? you have to figure out  */
         _is_valid_movie_statement.clearParameters();
         _is_valid_movie_statement.setInt(1, mid);
         ResultSet mov = _is_valid_movie_statement.executeQuery();
         return mov.next();
     }
 
     private int helper_who_has_this_movie(int mid) throws Exception {
         /* find the customer id (cid) of whoever currently rents the movie mid; return -1 if none */
         int ret;
         _rental_mid_statement.clearParameters();
         _rental_mid_statement.setInt(1, mid);
         ResultSet rental_set = _rental_mid_statement.executeQuery();
         if(rental_set.next()){
             ret = rental_set.getInt(1);
             return ret;
         }
         else return -1;
     }
 
     /**********************************************************/
     /* login transaction: invoked only once, when the app is started  */
     public int transaction_login(String name, String password) throws Exception {
         /* authenticates the user, and returns the user id, or -1 if authentication fails */
 
         /* Uncomment after you create your own customers database */
         int cid;
 
         _customer_login_statement.clearParameters();
         _customer_login_statement.setString(1,name);
         _customer_login_statement.setString(2,password);
         ResultSet cid_set = _customer_login_statement.executeQuery();
         if (cid_set.next()) cid = cid_set.getInt(1);
         else cid = -1;
         return(cid);
     }
 
     public void transaction_personal_data(int cid) throws Exception {
         /* println the customer's personal data: name, and plan number */
         System.out.println(helper_compute_customer_name(cid)+": "+helper_compute_remaining_rentals(cid)+" rentals remaining.");
     }
 
 
     /**********************************************************/
     /* main functions in this project: */
 
     public void transaction_search(int cid, String movie_title)
             throws Exception {
         /* searches for movies with matching titles: SELECT * FROM movie WHERE name LIKE movie_title */
         /* prints the movies, directors, actors, and the availability status:
            AVAILABLE, or UNAVAILABLE, or YOU CURRENTLY RENT IT */
 
         /* set the first (and single) '?' parameter */
         _search_statement.clearParameters();
         _search_statement.setString(1, '%' + movie_title + '%');
 
         ResultSet movie_set = _search_statement.executeQuery();
         while (movie_set.next()) {
             int mid = movie_set.getInt(1);
             System.out.println("ID: " + mid + " NAME: "
                     + movie_set.getString(2) + " YEAR: "
                     + movie_set.getString(3));
             /* do a dependent join with directors */
             _director_mid_statement.clearParameters();
             _director_mid_statement.setInt(1, mid);
             ResultSet director_set = _director_mid_statement.executeQuery();
             while (director_set.next()) {
                 System.out.println("\t\tDirector: " + director_set.getString(3)
                         + " " + director_set.getString(2));
             }
             director_set.close();
             
             // Return Availability
             int available = this.helper_who_has_this_movie(mid);
             if (available == -1) System.out.println("\t\tAVAILABLE");
             if (available == cid) System.out.println("\t\tYOU HAVE IT");
             if (available != -1 && available != cid) System.out.println("\t\tUNAVAILABLE");
             
             //  Return Actors
             _actor_mid_statement.clearParameters();
             _actor_mid_statement.setInt(1, mid);
             ResultSet actor_set = _actor_mid_statement.executeQuery();
             while (actor_set.next()) {
                 System.out.println("\t\tActor: " + actor_set.getString(3)
                         + " " + actor_set.getString(2));
             }
             actor_set.close();
             
             /* now you need to retrieve the actors, in the same manner */
             /* then you have to find the status: of "AVAILABLE" "YOU HAVE IT", "UNAVAILABLE" */
         }
         System.out.println();
     }
 
     public void transaction_choose_plan(int cid, int pid) throws Exception {
         /* updates the customer's plan to pid: UPDATE customers SET plid = pid */
         /* remember to enforce consistency ! */
         _set_customer_plan_id_statement.clearParameters();
         _set_customer_plan_id_statement.setInt(1, pid);
         _set_customer_plan_id_statement.setInt(2, cid);
         int res = _set_customer_plan_id_statement.executeUpdate();
         System.out.println("Success");
     }
 
     public void transaction_list_plans() throws Exception {
         /* println all available plans: SELECT * FROM plan */
         _get_all_plans_statement.clearParameters();
         ResultSet plans = _get_all_plans_statement.executeQuery();
         while(plans.next()){
             System.out.println(plans.getString(1)+": "+plans.getString(2)+", "+plans.getInt(4)+" movies for $"+plans.getFloat(3)+"0");
         }
     }
     
     public void transaction_list_user_rentals(int cid) throws Exception {
         /* println all movies rented by the current user*/
         _get_customer_rentals_statement.clearParameters();
         _get_customer_rentals_statement.setInt(1, cid);
         ResultSet rentals = _get_customer_rentals_statement.executeQuery();
         while(rentals.next()){
             _get_movie_statement.clearParameters();
             _get_movie_statement.setInt(1, rentals.getInt(1));
             ResultSet movie = _get_movie_statement.executeQuery();
             if(movie.next()){
                 System.out.println(movie.getInt(1)+": "+movie.getString(2));
             }
         }
     }
 
     public void transaction_rent(int cid, int mid) throws Exception {
         /* rend the movie mid to the customer cid */
         /* remember to enforce consistency ! */
         if(helper_check_movie(mid) != true){
             System.out.println("Error: This movie ID does not exist.");
         }
         else if(helper_compute_remaining_rentals(cid)<=0){
             System.out.println("Error: You have reached your rental limit");
         }else if(helper_who_has_this_movie(mid)!=-1){
             System.out.println("Error: That movie is out of stock");
         }else{
             _rent_movie_for_customer_statement.clearParameters();
             _rent_movie_for_customer_statement.setInt(1, cid);
             _rent_movie_for_customer_statement.setInt(2, mid);
             _rent_movie_for_customer_statement.setTimestamp(3, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
             _rent_movie_for_customer_statement.executeUpdate();
             System.out.println("Success");
         }
     }   
 
     public void transaction_return(int cid, int mid) throws Exception {
         /* return the movie mid by the customer cid */
         if(helper_check_movie(mid) != true){
             System.out.println("Error: This movie ID does not exist.");
         }
         else if(helper_who_has_this_movie(mid) != cid){
             System.out.println("Error: You can't return a movie you aren't renting.");
         }
         else{
             _return_movie_for_customer_statement.clearParameters();
             _return_movie_for_customer_statement.setInt(1, cid);
             _return_movie_for_customer_statement.setInt(2, mid);
             _return_movie_for_customer_statement.executeUpdate();
             System.out.println("Movie successfully returned");
         }
 
     }
 
     public void transaction_fast_search(int cid, String movie_title)
             throws Exception {
         /* like transaction_search, but uses joins instead of independent joins
            Needs to run three SQL queries: (a) movies, (b) movies join directors, (c) movies join actors
            Answers are sorted by mid.
            Then merge-joins the three answer sets */
     		
     	
     	
     	_search_fast_statement.clearParameters();
     	_search_fast_statement.setString(1, '%' + movie_title + '%');
     	ResultSet movie_fast_set = _search_fast_statement.executeQuery();
         
     	_actor_fast_mid_statement.clearParameters();
     	_actor_fast_mid_statement.setString(1, '%' + movie_title + '%');
     	ResultSet actor_fast_set = _actor_fast_mid_statement.executeQuery();
     	
         _director_fast_mid_statement.clearParameters();
         _director_fast_mid_statement.setString(1, '%' + movie_title + '%');
         ResultSet director_fast_set = _director_fast_mid_statement.executeQuery();
         
         while (movie_fast_set.next()){
         	int mid = movie_fast_set.getInt(1);
         	System.out.println("ID: " + mid + " NAME: "
                     + movie_fast_set.getString(2) + " YEAR: "
                     + movie_fast_set.getString(3));
         	while (director_fast_set.next() && director_fast_set.getInt(1) == mid){
         		System.out.println("\t\tDIRECTOR: " + director_fast_set.getString(4) + " "
         			+ director_fast_set.getString(3));
         	}
         	while (actor_fast_set.next() && actor_fast_set.getInt(1) == mid){
         		System.out.println("\t\tACTORS: " + actor_fast_set.getString(4) + " "
         			+ actor_fast_set.getString(3));
         	}
         }
         
         movie_fast_set.close();
         director_fast_set.close();
         actor_fast_set.close();
     	
     }
 
 }
