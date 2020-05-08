 /*******************************************************
 * Class for game instances, will be associated with
 * sessions through being the session data.
 *******************************************************/
 
 import database.DBConnections;
 import java.io.PrintWriter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.sql.*;
 //Pulled from inclass exmple
 import database.*;
 
 public class GameInstance {
     PlayerCharacter playerChar;
     AresCharacter aresChar;
     stateEnum currentState, startingState;
     String accountName;
     DBConnections dataSource = null;
     Connection conn = null;
     Statement stat = null;
 
     
     int constantPtsPerLevel = 5;
     
     GameInstance()
     {
         playerChar = null;
         aresChar = null;
         currentState = stateEnum.INIT;
         accountName = null;
     }
     
     /****************************************************
      * Connect to the database using class variables
      * SQL Commands
      ***************************************************/
     void connectDB(){
         dataSource = DBConnections.getInstance();  
         conn = dataSource.getConnection();
     }  
     
     /****************************************************
      * Disconnect from the database using class variables
      * SQL Commands
      ***************************************************/
     void disconnectDB(){
         dataSource.freeConnection(conn);
     }
     
     /****************************************************
      * Returns the result set result of your query
      * @param query the SQL query you want to execute
      * @param out the printwriter
      ***************************************************/
     ResultSet sqlQuery(String query, PrintWriter out){
         
         ResultSet result = null;
         try{
             connectDB();
              stat = conn.createStatement();
              result = stat.executeQuery(query);
         }catch(Exception ex){
             
            out.println("Query error:");
             out.println(ex);
         }finally{
             disconnectDB();
             return result;
         } 
     }
     /****************************************************
      * Returns true if your SQL command succeeded, 
      * returns false if it does not
      * @param command the SQL command you want to execute
      * @param out the printwriter
      ***************************************************/
     Boolean sqlCommand(String command, PrintWriter out){
         Boolean result = false;
         try{
             connectDB();
              stat = conn.createStatement();
              stat.execute(command);
              result = true;
              
         }catch(Exception ex){
             out.println("sqlCommand Exception: ");
             out.println(ex);
             
         }finally{
             DBUtilities.closeStatement(stat);
             disconnectDB();
             return result;
         } 
     }
     
     /****************************************************
      * state machine case switch function, called from 
      *      the servlet.
      * @param out output PrintWriter
      * @param request the servlet request
      ***************************************************/
     void advanceGame(PrintWriter out, HttpServletRequest request)
     {
         stateEnum nextState = currentState;
         startingState = currentState;
         do
         {
             currentState = nextState;
             
             switch(currentState)
             {
                 case INIT:
                     //first connection
                     nextState = initState(out, request);
                     break;
 
                 case BATTLE:
                     //battle function
                     nextState = battleState(out, request);
                     break;
 
                 case STORE:
                     //store function
                     nextState = storeState(out, request);
                     break;
 
                 case REGISTERED_CHARACTER_CREATION:
                     //character creation
                     nextState = registeredCharacterCreationState(out, request);
                     break;
 
                case UNREGISTERED_CHARACTER_CREATION:
                     //character creation
                     nextState = unregisteredCharacterCreationState(out, request);
                     break;
 
                 case IDLING:
                     //idle state
                     nextState = idling(out, request);
                     break;
 
                 case DECISION:
                     //this state is for asking what to do next
                     out.println("Decision");
                     nextState = decisionState(out, request);
                     break;
 
                 case BLACKSMITH:
                     //upgrade items
                     nextState = blackSmithState(out, request);
                     break;
                     
                 case LOGIN:
                     //login
                     nextState = loginState(out, request);
                     break;
                     
                 case ACCOUNT_CREATION:
                     try{
                     nextState = accountCreation(out, request);}
                     catch(SQLException ex)
                     {
                         out.println("What the ");
                         out.println(ex);
                     }
                     break;
                     
                     
                 default:
                     //this should go to a specified state
                     nextState = stateEnum.INIT;
                     initState(out, request);
                     break;
             }
         }while(currentState != nextState);
     }
     
     
     /****************************************************
      * Generates an inventory for the store based on the
      *      player character's level
      * @param level the level of the player character
      * @return an array of new items for the store
      ***************************************************/
     Item[] getStoreInventory(int level)
     {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     /****************************************************
      * Loads a new enemy from the database
      * @param level the players level
      ***************************************************/
     void getNextEnemy(int level)
     {
     
     }
     
     /****************************************************
      * Loads the players current character from the 
      *      database
      ***************************************************/
     void getCurrentCharacter()
     {
         
     }
     
     /****************************************************
      * Adds a newly created character to the database
      * @param name the character's name
      * @param level the level of the character
      * @param bio  a biography about the character
      * @param health the health of the character
      * @param strength the strength of the character
      * @param agility the agility of the character
      * @param magic  the magic of the character
      * @param itemsHeld the items held by the character
      * @return did it work
      ***************************************************/
     Boolean newCharacter(String name, int level, String bio, int health, int strength, int agility, int magic,Item[] itemsHeld)
     {
         return false;
     }
 
     
     /****************************************************
      * The initial state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum initState(PrintWriter out, HttpServletRequest request) {
         //can log in or unregistered user creation
         if(startingState != stateEnum.INIT)
         {
             //print the page
             out.println("<html>\n" +
 "	<head>\n" +
 "	<!-- Call normalize.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Import Font to be used in titles and buttons -->\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<title> Tarsus </title>\n" +
 "	</head>\n" +
 "	<body><form action=\"Tarsus\"> \n" +
 "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
 "			<a href=\"index.jsp\" id=\"tarsusTitle\"> TARSUS </a> \n" +
 "			<a class=\"button\" type=\"submit\" value=\"Log In\">  </div>\n" +
 "		<div class=\"grid1\"> </div>\n" +
 "		<div class=\"grid8 centered\">\n" +
 "		<h1 id=\"title\" class=\"centered\">Welcome</h1>\n" +
 "		<p align=\"justify\"> \n" +
 "			Tarsus is a web based Role Playing Game that allows you to create your own character and use it to fight progressively more difficult enemies as you try to make your way to the top. If you already have an account, click the Log In button above. If not, you can make a character using our character maker or your can sign up and start your own adventure.\n" +
 "		</p>\n" +
 "               \n" +
 "		<div align=\"center\">\n" +
                     "			<input type=\"submit\" value=\"Sign Up\" class=frontPageButton />\n" +
 "			<input type=\"Create a Character\" class=frontPageButton />\n" +
 
 "		</div>\n" +
 "		</div>\n" +
 "		<div class=\"grid1\"> </div>\n </form>" +
 "	</body>\n" +
 "	\n" +
 "</html>");
             return stateEnum.INIT;
         }
         else
         {
             String value = request.getParameter("Sign Up");
             
             //state changes
            
             if(value.equals("Sign Up"))
                 return stateEnum.ACCOUNT_CREATION;
             if(request.getParameter("Log in").equals("Log in"))
                 return stateEnum.LOGIN;
             if(request.getParameter("Create a Character").equals("Create a Character"))
                 return stateEnum.UNREGISTERED_CHARACTER_CREATION; 
             
              
                 
             
         }
        return stateEnum.INIT;
         
     }
 
     /****************************************************
      * The initial state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     private stateEnum storeState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 
 		/*
 		// have store level as well as the items be static so that it is the same each time the player comes back to the 
 		// store unless the player has increased in level
 		
 		
 		static store_level = 1;
 		final int STORE_SIZE = 20;
 		static item[] item_array = new item[STORE_SIZE];
 		
 		// if level has changed create a new item inventory for the store
 		// based on some hash function of the character's level
 		if(playerChar.getLevel() != store_level)
 		{
 			store_level = playerChar.getLevel();
 			
 			final String[] item_name_type = ["Mace", "Sword", "Axe", "Bow", "Crossbow", "Throwing Knives", "Staff", "Wand", "Orb"]; // Could have room for permutations
 			final String[] item_name_quality_description = ["Broken", "Inferior", "Common", "Slightly Better", "Ancient", "Legendary", "Actually Broken"];
 			// Ignore this next line for now as each weapon can only specialize in one area at the moment.
 			//final String[] item_name_Modifier_description = ["Warrior", "Hunter", "Wizard", "Bandit", "BattleMage", "Magic-Range Thing whatever", "Balance"] // permutation for each thing
 			for(int i = 0; i < STORE_SIZE; i++)
 				{
 				item_type = item_name_type[(i % 9)]
 				// need to place the parameters for how each item could be created
 				item_array[i] = new Item(name = "" + item_type, 
 					id = null, type = item_type, upgradeCount = 0, strength = 0, agility = 0, magic = 0 );
 				}
 		}
 		
 		
 		// if item bought, add to inventory
 		
     }
 
     /****************************************************
      * The initial state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     private stateEnum battleState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * Create a registered user character
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum registeredCharacterCreationState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     /****************************************************
      * Create an unregistered user character
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum unregisteredCharacterCreationState(PrintWriter out, HttpServletRequest request) {
         String StartPage = "<html>\n" +
 "	<head>\n" +
 "	<!-- Call normalize.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"./css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Import Font to be used in titles and buttons -->\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"../css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"../css/style.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<title> Tarsus </title>\n" +
 "	</head>\n" +
 "       <script>\n" +
 "		function validateForm()\n" +
 "		{\n" +
 "		\n" +
 "               var maxValue = %f \n" +
 "			var strength = parseInt(document.forms[\"createCharacterForm\"][\"strength\"].value); \n" +
 "			var agility = parseInt(document.forms[\"createCharacterForm\"][\"agility\"].value);\n" +
 "			var magic = parseInt(document.forms[\"createCharacterForm\"][\"magic\"].value);\n" +
 "			var total = strength + agility + magic;\n" +
 "			alert(\"Total Experience points used: \" + total);\n" +
 "			if(total > maxValue)\n" +
 "			{\n" +
 "				alert(\"Cannot use more than\" + maxValue + \" experience points.\");\n" +
 "				return false;\n" +
 "			}\n" +
 "		\n" +
 "		}\n" +
 "       </script>" + 
 "	<body>\n" +
 "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
 "			<a href=\"profile.html\" id=\"tarsusTitle\"> Unregistered User Character Creation </a> \n" +
 "			<a class=\"button\" href=\"../index.html\"> Log Out </a> </div>\n" +
 "		<div class=\"grid1\"> </div>\n" +
 "		<div class=\"grid8 centered\">\n" +
 "		<h1 id=\"title\" class=\"centered\">Character Creation</h1>\n" +
 "		\n" +
 "		<div class=\"grid2\"> </div>\n" +
 "		<form name=\"createCharacterForm\" action=\"Tarsus\" onsubmit=\"return validateForm()\" method=\"post\">\n" +
 "               <input type = \"hidden\" name = \"level\"> value=\"%f\"/>\n"+
 "		<div class=\"grid6\" align=\"center\">\n" +
 "			<h3> Level %f </h3>\n" +
 "			<p> Experience Points to Allocate: %f\n" +
 "			</p>\n" +
 "			<p> \n" +
 "				Name: <input type=\"text\" name=\"name\"/>\n" +
 "			</p>\n" +
 "			<p> \n" +
 "				Strength: <input type=\"number\" name=\"name\"min=\"0\" max=\"100\" value=\"0\"/>\n" +
 "			</p> \n" +
 "			<p> \n" +
 "				Agility: <input type=\"number\" name=\"agility\"min=\"0\" max=\"100\" value=\"0\"/>\n" +
 "			</p>  \n" +
 "			<p> \n" +
 "				Magic: <input type=\"number\" name=\"magic\" min=\"0\" max=\"100\" value=\"0\"/>\n" +
 "			</p>   \n" +
 "			<p>\n" +
 "				Biography:<textarea name=\"bio\" cols=\"35\" rows=\"3\" maxlength=\"300\"> </textarea> <br /> <a id=\"bioLimitID\">  (Max of 300 Chars)</a>\n" +
 "			</p>\n" +
 "		</div>\n" +
 "		<div class=\"grid10\" align=\"center\">\n" +
 "			<a href=\"continuechar.html\" class=frontPageButton>Create Character</a>\n" +
 "		</div>\n" +
 "		</form>\n" +
 "		</div>\n" +
 "		<div class=\"grid1\"> </div>\n" +
 "	</body>\n" +
 "	\n" +
 "</html>";
         if(startingState != stateEnum.UNREGISTERED_CHARACTER_CREATION)
         {
             //create new page for it
             int level = (int)(Math.random()*50);
             
             out.printf(StartPage, level, level*constantPtsPerLevel, level*constantPtsPerLevel, level);
             
             return stateEnum.UNREGISTERED_CHARACTER_CREATION;
         }
         else
         {
            String name = (String) request.getParameter("name");
            int level = Integer.parseInt(request.getParameter("level"));
            String bio = request.getParameter("bio");
            int health = Integer.parseInt(request.getParameter("health"));
            int strength = Integer.parseInt(request.getParameter("strength"));
            int agility = Integer.parseInt(request.getParameter("agility"));
            int magic = Integer.parseInt(request.getParameter("magic"));
       
            if(isValidString(name) & isValidString(bio))
            {
                //newCharacter(name, level,bio, health, strength, agility, magic);
                 return stateEnum.INIT;
            }
            else
            {
                 return stateEnum.UNREGISTERED_CHARACTER_CREATION;
            }
         }
     }
 
     /****************************************************
      * The idling state, logs out after a certain amount of time
      * may be removed
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum idling(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * Asking what the player wants to do next
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum decisionState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * At the blacksmith and can upgrade items
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum blackSmithState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * Registered user login state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum loginState(PrintWriter out, HttpServletRequest request) {
         if(startingState != stateEnum.LOGIN){
             out.println("<html>\n" +
             "	<head>\n" +
             "	<!-- Call normalize.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<!-- Import Font to be used in titles and buttons -->\n" +
             "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
             "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
             "	<!-- Call style.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<!-- Call style.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<title> Tarsus </title>\n" +
             "	</head>\n" +
             "	<div id=\"header\" class=\"grid10\" align=\"right\"> \n" +
             "		<a href=\"index.jsp\" id=\"tarsusTitle\"> TARSUS </a> </div>\n" +
             "	<div class=\"grid1\"> </div>\n" +
             "	<div class=\"grid8 centered\">\n" +
             "		<h1 id=\"title\" class=\"centered\"> Log In</h1>\n" +
             "		<form method=\"post\" action=\"Tarsus\"> \n" +
             "			<p align=\"center\"> \n" +
             "				Username: <input name=\"username\" type=\"text\" /> \n" +
             "			</p>\n" +
             "			<p align=\"center\"> \n" +
             "				Password: <input name=\"password\" type=\"password\" /> \n" +
             "			</p>\n" +
             "			<p align=\"center\"> \n" +
             "				<input class=\"signUpButton\" value=\"Log In\" type=\"submit\"/>\n" +
             "			</p>\n" +
             "		</form>\n" +
             "	</div>\n" +
             "</html>");
                     
         }else{
             String username = request.getParameter("username");
             int password = request.getParameter("password").hashCode();
             if(!isValidString(username)){
                 out.println("Error");
                 return stateEnum.LOGIN;
             }
             String search = "SELECT * FROM Login WHERE username='" + username +
                     "' AND password= MD5('" + password+  "');";
             ResultSet result = sqlQuery(search, out);
             try{
             if(result.isBeforeFirst()){
                     accountName = username;
                     return stateEnum.DECISION;
             }else{
                 out.println("<html>\n" +
                     "	<head>\n" +
                     "	<!-- Call normalize.css -->\n" +
                     "	<link rel=\"stylesheet\" href=\"css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
                     "	<!-- Import Font to be used in titles and buttons -->\n" +
                     "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
                     "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
                     "	<!-- Call style.css -->\n" +
                     "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
                     "	<!-- Call style.css -->\n" +
                     "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
                     "	<title> Tarsus </title>\n" +
                     "	</head>\n" +
                     "	<div id=\"header\" class=\"grid10\" align=\"right\"> \n" +
                     "		<a href=\"index.jsp\" id=\"tarsusTitle\"> TARSUS </a> </div>\n" +
                     "	<div class=\"grid1\"> </div>\n" +
                     "	<div class=\"grid8 centered\">\n" +
                     "		<h1 id=\"title\" class=\"centered\"> Log In</h1>\n" +
                     "		<h3>Invalid Login </h3> \n " +
                     "		<form method=\"post\" action=\"Tarsus\"> \n " +
                     "			<p align=\"center\"> \n" +
                     "				Username: <input name=\"username\" type=\"text\" /> \n" +
                     "			</p>\n" +
                     "			<p align=\"center\"> \n" +
                     "				Password: <input name=\"password\" type=\"password\" /> \n" +
                     "			</p>\n" +
                     "			<p align=\"center\"> \n" +
                     "				<input class=\"signUpButton\" value=\"Log In\" type=\"submit\"/>\n" +
                     "			</p>\n" +
                     "		</form>\n" +
                     "	</div>\n" +
                     "</html>");
                 return stateEnum.LOGIN;
             }
             }catch(Exception ex){
                 out.println("Login SQL Error: " + ex);
             }
         }
         return stateEnum.LOGIN;
     }
 
     /****************************************************
      * Registered user creation state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum accountCreation(PrintWriter out, HttpServletRequest request) throws SQLException {
         String accountPageBegin = "<html>\n" +
             "	<head>\n" +
             "	<!-- Call normalize.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<!-- Import Font to be used in titles and buttons -->\n" +
             "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
             "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
             "	<!-- Call style.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<!-- Call style.css -->\n" +
             "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
             "	<title> Tarsus </title>\n" +
             "	</head>\n" +
             "	<div id=\"header\" class=\"grid10\" align=\"right\"> \n" +
             "		<a href=\"index.jsp\" id=\"tarsusTitle\"> TARSUS </a> \n" +
             "		<a class=\"button\" href=\"login.html\"> Log In </a> </div>\n" +
             "	<div class=\"grid1\"> </div>\n" +
             "	<div class=\"grid8 centered\">\n" +
             "		<h1 id=\"title\" class=\"centered\"> Sign Up Below</h1>\n";
         String accountPageEnd = 
             "		<form method=\"post\" action=\"Tarsus\"> \n" +
             "			<p align=\"center\"> \n" +
             "				Username: <input name=\"username\" type=\"text\" /> \n" +
             "			</p>\n" +
             "			<p align=\"center\"> \n" +
             "				Password: <input name=\"password\" type=\"password\" /> \n" +
             "			</p>\n" +
             "			<p align=\"center\"> \n" +
             "				Confirm Password: <input name=\"confirmpassword\" type=\"password\" /> \n" +
             "			</p>\n" +
             "			<p align=\"center\"> \n" +
             "				<input class=\"signUpButton\" value=\"Sign Up\" type=\"submit\"/> \n" +
             "			</p>\n" +
             "		</form>\n" +
             "	</div>\n" +
             "	<div class=\"grid1\"> </div>\n" +
             "	\n" +
             "</html>";
         if(startingState != stateEnum.ACCOUNT_CREATION)
         {
             out.println(accountPageBegin + accountPageEnd);
             return stateEnum.ACCOUNT_CREATION;
         }
         else{
             
             String username = request.getParameter("username");
             String findUsername = "SELECT username FROM Login "
                     + "WHERE username = \"" + username + "\";";
             
             Boolean alreadyExists = false;
             try{
                 ResultSet result = sqlQuery(findUsername, out);
                 if(result.isBeforeFirst()){
                     alreadyExists= true;
                 }
                 
             }catch(Exception ex){
                 out.println("username fail");
                 out.println(ex);
                 alreadyExists=false;
             }
             
             // Check to see if the username is valid
             if(!isValidString(username) || alreadyExists)
             {
                out.println(accountPageBegin + 
                         "<h3 id=\"title\" class=\"centered\"> Invalid Username "
                        + "</h3 \n" + accountPageEnd);
                return stateEnum.ACCOUNT_CREATION;
             }
             
             int password = request.getParameter("password").hashCode();
             int confirmPassword = request.getParameter("confirmpassword").hashCode();
             if(password != confirmPassword){
                 out.println(accountPageBegin + 
                         "<h3 id=\"title\" class=\"centered\"> The Passwords Do "
                         + "Not Match </h3 \n" + accountPageEnd);
                 return stateEnum.ACCOUNT_CREATION;  
             }
             String command = "INSERT INTO Login VALUES ('" + username + "', MD5('"
                     + password +"'));";
             
             try{
             if(sqlCommand(command, out))
             {
                 return stateEnum.LOGIN;
              
             } 
             
             else{
                 out.println(accountPageBegin +"<h1> ERROR! </h1>"+ accountPageEnd);
                 return stateEnum.ACCOUNT_CREATION;
                         
             } 
             }catch(Exception ex)
             {
                 out.println("SQL Command Error:");
                 out.println(ex);
                 return stateEnum.ACCOUNT_CREATION;}
         }
     }
     
     
     /****************************************************
      * Registered user creation state
      * @param string the string to check for validity
      * @return the validity
      ***************************************************/
     Boolean isValidString(String string)
     {
         Boolean toBeReturned = true;
         
         if(string.contains("Drop"))
             toBeReturned = false;
         if(string.contains("Delete"))
             toBeReturned = false;
         if(string.contains(";"))
             toBeReturned = false;
         
         return toBeReturned;
     }
     
 	String maxValueScript(int value)
 	{
 	return ("<script> var maxValue=" + Integer.toString(value) +";</script>");
 	}
 	
 }
