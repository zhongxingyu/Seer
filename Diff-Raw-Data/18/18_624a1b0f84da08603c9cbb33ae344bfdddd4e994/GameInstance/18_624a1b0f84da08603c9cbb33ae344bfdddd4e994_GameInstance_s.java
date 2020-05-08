 /*******************************************************
 * Class for game instances, will be associated with
 * sessions through being the session data.
 *******************************************************/
 
 import java.io.PrintWriter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class GameInstance {
     PlayerCharacter playerChar;
     AresCharacter aresChar;
     stateEnum currentState, startingState;
     String accountName;
     
     GameInstance()
     {
         playerChar = null;
         aresChar = null;
         currentState = stateEnum.INIT;
         accountName = null;
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
                     nextState = accountCreation(out, request);
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
      ***************************************************/
     void newCharacter(String name, int level, String bio, int health, int strength, int agility, int magic,Item[] itemsHeld)
     {
         
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
 "			<a href=\"index.html\" id=\"tarsusTitle\"> TARSUS </a> \n" +
 "			<a class=\"button\" type=\"submit\" value=\"Log In\">  </div>\n" +
 "		<div class=\"grid1\"> </div>\n" +
 "		<div class=\"grid8 centered\">\n" +
 "		<h1 id=\"title\" class=\"centered\">Welcome</h1>\n" +
 "		<p align=\"justify\"> \n" +
 "			Tarsus is a web based Role Playing Game that allows you to create your own character and use it to fight progressively more difficult enemies as you try to make your way to the top. If you already have an account, click the Log In button above. If not, you can make a character using our character maker or your can sign up and start your own adventure.\n" +
 "		</p>\n" +
 "               \n" +
 "		<div align=\"center\">\n" +
 "			<input type=\"Create a Character\" class=frontPageButton />\n" +
 "			<input type=\"submit\" value=\"Sign Up\" class=frontPageButton />\n" +
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
             //state changes
             String value = (String)request.getAttribute("submit");
             if(value.equals("Log in"))
                 return stateEnum.LOGIN;
             else if(value.equals("Create a Character"))
                 return stateEnum.UNREGISTERED_CHARACTER_CREATION;
             else if(value == "Sign Up")
                 return stateEnum.ACCOUNT_CREATION;
             return stateEnum.INIT;
         }
         
     }
 
     /****************************************************
      * The initial state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     private stateEnum storeState(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
<<<<<<< HEAD
 		/*
 		// have store level as well as the items be static so that it is the same each time the player comes back to the 
 		// store unless the player has increased in level
 		static store_level = 1;
=======
		
		// have store level as well as the items be static so that it is the same each time the player comes back to the 
		// store unless the player has increased in level
		/*static store_level = 1;
>>>>>>> origin/master
 		const int STORE_SIZE = 20;
 		static item[] item_array = new item[STORE_SIZE];
 		
 		// if level has changed create a new item inventory for the store
 		// based on some hash function of the character's level
 		if(playerChar.getLevel() != store_level)
 		{
 			store_level = playerChar.getLevel();
 			for(int i = 0; i < STORE_SIZE; i++)
 				{
 				// need to place the parameters for how each item could be created
<<<<<<< HEAD
 				item_array[i] = new Item(name = "", id = null, type = (i % 9), upgradeCount = 0, strength = 0, agility = 0, magic = 0 );
=======
				item_array[i] = new Item(name = "", id = null, type = (i % 9), upgradeCount = 0, strength = , agility = , magic =  );
>>>>>>> origin/master
 				}
 		}
 		
 		
 		// if item bought, add to inventory
 		*/
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
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     /****************************************************
      * Registered user creation state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum accountCreation(PrintWriter out, HttpServletRequest request) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
 	String maxValueScript(int value)
 	{
	return ("<script> var maxValue=" + (String)(value) +";</script>");
 	}
 	
 }
