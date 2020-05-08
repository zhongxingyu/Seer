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
     final int STORE_SIZE = 20;    
     int gold;
     String error;
     int storeLevel;
     Item[] storeItems;
 
     
     int constantPtsPerLevel = 5;
     int constantWeaponPtsPerLevel = 3;
     int constantArmorPtsPerLevel = 5;
     int constantGoldPerLevel = 20;
     int constantHealthPerLevel = 15;
     int constantStrengthPerLevel = 10;
     int constantAgilityPerLevel = 10;
     int constantMagicPerLevel = 10;
     int constantHealthBase = 200;
 
     
     
     GameInstance()
     {
         playerChar = null;
         aresChar = null;
         currentState = stateEnum.INIT;
         accountName = "Unregistered User";
         gold = 0;
         error = null;
         int storeLevel = 0;
         Item[] storeItems = new Item[STORE_SIZE];
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
 
         DBUtilities.closeStatement(stat);
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
              stat = conn.createStatement();
              result = stat.executeQuery(query);
         }catch(Exception ex){
            out.println("Query error:");
            out.println(ex);
         }finally{
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
              stat = conn.createStatement();
              stat.execute(command);
              result = true;
              
         }catch(Exception ex){
             out.println("sqlCommand Exception: ");
             out.println(ex);
             
         }finally{
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
                     //account creation
                     try{
                     nextState = accountCreation(out, request);}
                     catch(SQLException ex)
                     {
                         out.println("What the ");
                         out.println(ex);
                     }
                     break;
                     
                 case LEVEL_UP:
                     nextState = levelUpState(out, request);
                     break;
                     
                 case PROFILE:
                     //profile
                     try{
                     nextState = profileState(out, request);}
                     catch(SQLException ex)
                     {
                         out.println("What the ");
                         out.println(ex);
                     }
                     break;
                     
                 case PAST_CHARACTERS:
                     //Look at Past Characters
                     nextState = pastCharactersState(out, request);
                     break;
                     
                 case LOGOUT:
                     //Log Out
                     nextState = LogoutState(out, request);
                     break;
                     
                 default:
                     //this should go to a specified state
                     nextState = stateEnum.INIT;
                     initState(out, request);
                     break;
             }
         }while((currentState != nextState)|(error!=null));
     }
     
     
     /****************************************************
      * Generates an inventory for the store based on the
      *      player character's level
      * @param level the level of the player character
      * @return an array of new items for the store
      ***************************************************/
     Item[] getStoreInventory(int level, int size)
     {
         final int STORE_LEVEL = level;
 	final int STORE_SIZE = size;
 	Item[] storeItems = new Item[STORE_SIZE];
 		
 		for(int i = 0; i < STORE_SIZE; i++)
 		{
 						
 			//general type index that decides one of the three
                         // item types. // not including error
 			int gi = (int)(Math.round(Math.random() * (3)) + 1);
 			
 			storeItems[i] = generateItem(gi, STORE_LEVEL);
 		}
 		
 		return storeItems;
     }
     
     Item generateItem(int type, int Level)
 	{
 		final String[] armor_name_type = {"Plate Armor", "Leather Armor", "Robe", "Mail Armor", "Magic-Strength Armor stuff", "Magic-Agility Armor stuff", "Armor"};
 		final String[] weapon_name_type = {"Sword", "Axe", "Mace", "Bow", "Crossbow", "Throwing Knives", "Staff", "Wand", "Orb"}; // Could have room for permutations
 		final String[] item_name_type = {"potion"};
                 final String[] error_name_type = {"error"};
 		final String[] item_name_quality_description = {"Broken", "Inferior", "Common", "Slightly Better", "Ancient", "Legendary", "Actually Broken"};
 		final String[][] general_item_type = {error_name_type, weapon_name_type, armor_name_type, item_name_type};
 		//final String[] item_name_Modifier_description = ["Warrior", "Hunter", "Wizard", "Bandit", "BattleMage", "Magic-Range Thing whatever", "Balance"] // permutation for each thing
 		
 		double base_stats[] = {0, 0, 0, 0};
 		
                 //general type index
 			int gi = type;
 			//System.out.println(gi);
 			// special type index
 			int si = (int)(Math.round(Math.random() * (general_item_type[gi].length - 1)));
 			//System.out.println(si);
 			
 			// armor case
 			if(gi == 2)
 			{
 				switch (si)
 				{
 				case 0: base_stats[0] = 1;
 						base_stats[1] = 0;
 						base_stats[2] = 0;
 						break;
 				case 1: base_stats[0] = 0;
 						base_stats[1] = 1;
 						base_stats[2] = 0;
 						break;
 				case 2: base_stats[0] = 0;
 						base_stats[1] = 0;
 						base_stats[2] = 1;
 						break;
 				case 3: base_stats[0] = .5;
 						base_stats[1] = .5;
 						base_stats[2] = 0;
 						break;
 				case 4: base_stats[0] = .5;
 						base_stats[1] = 0;
 						base_stats[2] = .5;
 						break;
 				case 5: base_stats[0] = 0;
 						base_stats[1] = .5;
 						base_stats[2] = .5;
 						break;
 				case 6: base_stats[0] = 0.3333;
 						base_stats[1] = 0.3333;
 						base_stats[2] = 0.3333;
 						break;
 				
 				}
 			}
 			// weapon case
 			else if(gi == 1)
 			{
 				if((si % 9) < 3)
 				{
 					base_stats[0] = 1;
 				}
 				else if((si % 9) < 6)
 				{
 					base_stats[1] = 1;
 				}
 				else if((si % 9) < 9)
 				{
 					base_stats[2] = 1;
 				}
 			}
 			// item case
 			else if(gi == 3)
 			{
 				switch(si)
 				{
 				// potions have an abitrary larger base value thing
 				case 0: base_stats[3] = 2;
 						break;
 				}
 				
 			}
 			// Higher levels will have a more balance distribution of items
 			// e.g. Cannot possibly find a legendary item until at least level 9
 			
 			double quality = getQuality(Level);
 			int index = (int) Math.round(quality * ((item_name_quality_description.length) - 1));
 			String item_quality = item_name_quality_description[index];
 			
 			String item_type = general_item_type[gi][si];
 			
 			// Get the base damage of each stat
 			// will only affect one stat at the moment
 			
 			int value_sum = 0;
 			for(int j = 0; j < 4; j++)
 			{
 				// multiples the base stat for cases where the base stat is split up in proportions
 				base_stats[j] *=(((quality) * 100) + 20);
 				base_stats[j] = Math.round(base_stats[j]);
 				value_sum += base_stats[j];
 			}
 			
 		String item_name = item_quality + " " + item_type;
 		
 		Item item = new Item(item_name, 0, gi, 0, (int)base_stats[0], (int)base_stats[1],(int)base_stats[2], (int)base_stats[3]);
 		return item;
 
 	
 	}
     /****************************************************
      * Generates an armor item based on the player 
      * character's level
      * @param level the level of the player character
      * @return an item with a type of armor
      ***************************************************/
     Item generateArmor(int level)
     {
         return generateItem(2, level);
     }
     
     /****************************************************
      * Generates a weapon item based on the player 
      * character's level
      * @param level the level of the player character
      * @return an item with a type of weapon
      ***************************************************/
     Item generateWeapon(int level)
     {
         return generateItem(1,level);
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
      * @param chrct character object to add to the database
      * @param isDead Boolean, whether or not the character is dead
      * @param out PrintWriter for the page
      * @return did it work
      ***************************************************/
     Boolean newCharacter(Character chrct, Boolean isDead, PrintWriter out)
     {
         Integer dead=0;
         if(isDead)
             dead=1;
         connectDB();
         String query = "INSERT into Characters (name, level, bio, creator, strength, health, isDead, magic, agility, timesAttacked, timesSwitchedToStrength, timesSwitchedToAgility, timesSwitchedToMagic, equippedWeapon, equippedArmor) VALUES ('"+chrct.getName()+"', '"+(((Integer)chrct.getLevel()).toString())+"', '"+chrct.getBio()+"', '"+accountName+"', '"+((Integer)(chrct.getStrength())).toString()+"', '"+((Integer)chrct.getMaxHealth()).toString()+"', b'"+dead.toString()+"', '"+((Integer)chrct.getMagic()).toString()+"', '"+((Integer)chrct.getAgility()).toString()+"', '"+((Integer)chrct.timesAttacked).toString()+"', '"+((Integer)chrct.timesSwitchedToStrength).toString()+"', '"+((Integer)chrct.timesSwitchedToAgility).toString()+"', '"+((Integer)chrct.timesSwitchedToMagic).toString()+"', '"+((Integer)chrct.weapon.getItemId()).toString()+"', '"+((Integer)chrct.armor.getItemId()).toString()+"');";
         return sqlCommand(query,out);
     }
 
     
     /****************************************************
      * update created character to the database
      * @param chrct character object to update in the database
      * @param isDead Boolean, whether or not the character is dead
      * @param out PrintWriter for the page
      * @return did it work
      ***************************************************/
     Boolean updateCharacter(Character chrct, Boolean isDead, PrintWriter out)
     {
         Integer dead=0;
         if(isDead)
             dead=1;
         connectDB();
         String query = "UPDATE Characters SET level='"+(((Integer)chrct.getLevel()).toString())+"', bio='"+chrct.getBio()+"', strength='"+((Integer)(chrct.getStrength())).toString()+"', health='"+((Integer)chrct.getMaxHealth()).toString()+"', isDead=b'"+(dead.toString())+"', magic='"+((Integer)chrct.getMagic()).toString()+"', agility='"+((Integer)chrct.getAgility()).toString()+"', timesAttacked='"+((Integer)chrct.timesAttacked).toString()+"', timesSwitchedToStrength='"+((Integer)chrct.timesSwitchedToStrength).toString()+"', timesSwitchedToAgility='"+((Integer)chrct.timesSwitchedToAgility).toString()+"', timesSwitchedToMagic='"+((Integer)chrct.timesSwitchedToMagic).toString()+"', equippedWeapon='"+((Integer)chrct.weapon.getItemId()).toString()+"', equippedArmor='"+((Integer)chrct.armor.getItemId()).toString()+"' WHERE name='"+chrct.getName()+"');";
         return sqlCommand(query,out);
     }
     
     /****************************************************
      * Adds new item to the database
      * @param item item object to add to the database
      * @param out PrintWriter for the page
      * @return did it work
      ***************************************************/
     Boolean newItem(Item item, PrintWriter out) throws SQLException
     {
         if(item.getItemId()==0)
             item.itemId=nextItemId(out);
         connectDB();
         String query = "Insert into Items (itemId, name, type, strengthVal, healthVal, upgradeCount, magicVal, agilityVal) VALUES ('"+((Integer)item.getItemId()).toString()+"', '"+item.getName()+"', '"+((Integer)item.getType()).toString()+"', '"+((Integer)item.getStrength()).toString()+"', '"+((Integer)item.getHeal()).toString()+"', '"+((Integer)item.getUpgradeCount()).toString()+"', '"+((Integer)item.getMagic()).toString()+"', '"+((Integer)item.getAgility()).toString()+"');";
         return sqlCommand(query,out);
     }
     
     int nextItemId(PrintWriter out) throws SQLException
     {
         String query="SELECT * FROM Items;";
         int max = 0;
         connectDB();
         ResultSet result = sqlQuery(query, out);
         while(result.next()){
             if(result.getInt("itemId")>max)
                 max=result.getInt("itemId");
         }
         disconnectDB();
         return max+1;
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
 "	<body>\n" +
 "            <form action=\"Tarsus\" method=\"post\">\n" +
 "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
 "			<input href=\"index.html\" id=\"tarsusTitle\" /> \n" +
 "			<input class=\"button\" type=\"submit\" value=\"Log In\" name=\"Log In\" /> </div>\n" +
 
 "		<div class=\"grid1\"> </div>\n" +
 "		<div class=\"grid8 centered\">\n" +
 "		<h1 id=\"title\" class=\"centered\">Welcome</h1>\n" +
 "		<p align=\"justify\"> \n" +
 "			Tarsus is a web based Role Playing Game that allows you to create your own character and use it to fight progressively more difficult enemies as you try to make your way to the top. If you already have an account, click the Log In button above. If not, you can make a character using our character maker or your can sign up and start your own adventure.\n" +
 "		</p>\n" +
 "		<div align=\"center\">\n" +
 "                    <input type=\"submit\" value=\"Create a Character\" name=\"Create a Character\" class=frontPageButton />\n" +
 "			<input type=\"submit\" value=\"Sign Up\" name=\"Sign Up\" class=frontPageButton />\n" +
 "		</div>\n" +
 "		</div>\n" +
 "		<div class=\"grid1\"> </div>\n" +
 "            </form>\n" +
 "	</body>\n" +
 "	\n" +
 "</html>");
             return stateEnum.INIT;
         }
         else
         {
             String value1 = request.getParameter("Sign Up");
             String value2 = request.getParameter("Log In");
             String value3 = request.getParameter("Create a Character");
 
             String value = "";
             if(value1 != null)
                 value = value1;
             if(value2!=null)
                 value = value2;
             if(value3!=null)
                 value = value3;
           
             if(value.equals("Log In"))
                 return stateEnum.LOGIN;
             if(value.equals("Create a Character"))
                 return stateEnum.UNREGISTERED_CHARACTER_CREATION;
             if(value.equals("Sign Up"))
                 return stateEnum.ACCOUNT_CREATION;
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
 		
         // have store level as well as the items be static so that it is the same each time the player comes back to the 
         // store unless the player has increased in level
 
         // if level has changed create a new item inventory for the store
         // based on some hash function of the character's level
         if(playerChar.getLevel() != storeLevel)
         {
                 storeLevel = playerChar.getLevel();
                 storeItems = getStoreInventory(storeLevel, STORE_SIZE);
 
         }
                 
 	if(startingState != stateEnum.STORE)
         {
             printStoreState(out);
             return stateEnum.STORE;
         }
         else
         {
             if(request.getParameter(accountName) != null)
                 return stateEnum.DECISION;
             // for buying items from the store
             for (int i = 0; i < storeItems.length - 1; i++)
             {
                 String buyValue = request.getParameter("Buy " + i);
                 if(buyValue != null)
                 {
                     gold -= storeItems[i].getValue();
                     // a bad way of deleting an element
                     storeItems[i] = null;
                     printStoreState(out);
                 }
             }
             // for selling items player's inventory
             for (int i = 0; i < playerChar.itemsHeld.length - 1; i++){
                 String sellValue = request.getParameter("Sell " + i);
                 if(sellValue != null)
                 {
                    gold += Math.round((.6) * playerChar.itemsHeld[i].getValue());
                    
                    // need to drop the item from the table
                    printStoreState(out);
                 }
             }
             return stateEnum.STORE;
         }
 
     }
 
     /****************************************************
      * The initial state
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     private stateEnum battleState(PrintWriter out, HttpServletRequest request) {
         if(startingState != stateEnum.BATTLE)
         {
             Item[] itemsHeld = {generateWeapon(1), generateArmor(1), generateWeapon(1), generateArmor(1)};
             playerChar = new PlayerCharacter("player", "", 1, 1000, 1, 2, 3, itemsHeld, itemsHeld[0], itemsHeld[1], 0, 0, 0, 0);
             aresChar = new AresCharacter("enemy", "", 1, 100, 1, 2, 3, itemsHeld, itemsHeld[0], itemsHeld[1], 0, 0, 0, 0);
         }
         
         String startPage = "<html>\n" +
 "	<head>\n" +
 "	<!-- Call normalize.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"../css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Import Font to be used in titles and buttons -->\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
 "	<link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<!-- Call style.css -->\n" +
 "	<link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
 "	<title> Tarsus </title>\n" +
 "	</head>\n" +
 "	<body>\n" +
 "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
 "			%s \n" +
 "	        </div>\n" +
 "		<div class=\"grid1\"> </div>\n" +
 "		<div class=\"grid8 centered\">\n" +
 "		<br />\n" +
 "		<p align=\"center\">\n" +
 "		</p>\n" +
 "		<div class=\"gridHalf\"> \n";
          String statsTable =       
 "			<h2 align=\"center\"> %s </h2>\n" +
 "			\n" +
 "			<table id=\"table\" align=\"center\">\n" +
 "				<tr>\n" +
 "					<th> Health </th>\n" +
 "					<th> Strength </th>\n" +
 "					<th> Magic </th>\n" +
 "					<th> Agility </th>\n" +
 "				</tr>\n" +
 "				<tr>\n" +
 "					<th> %d </th>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "				</tr>\n" +
 "			</table>\n";
         String equippedTable1 = 
 "			\n" +
 "			<h3 align=\"center\"> Equipped </h3>\n" +
 "			<table id=\"table\" align=\"center\">\n" +
 "				<tr>\n" +
 "					<td> </td>\n" +
 "					<th> Name </th>\n" +
 "					<th> Strength </th>\n" +
 "					<th> Magic </th>\n" +
 "					<th> Agility </th>\n" +
 "				</tr>\n" +
 "				<tr>\n" +
 "					<th> Weapon: </th>\n" +
 "					<td> %s </td>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "				</tr>\n";
         String equippedTable2 = 
 "                               <tr>\n" +
 "					<th> Armor: </th>\n" +
 "					<td> %s </td>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "					<td> %d </td>\n" +
 "				</tr>\n" +
 "			</table>\n";
         String betweenCharacters = 
 "		</div>\n" +
 "		<div class=\"gridHalf\"> \n";
         String afterTable = 
 "		\n" +
 "		</div>\n" +
 "                               <form action=\"Tarsus\" method = \"post\">";
         String attackButton =
 "				<input type = \"submit\" class=\"profileButton\" name = \"attack\" value = \"Attack\" />  \n" + 
 "                               <select name = \"itemSelected\"> \n";
         String useButton = 
 "                               </select>" + 
 "				<input type = \"submit\" class=\"profileButton\" name=\"use\" value = \"Use item\" /> \n";
         String lastPart = 
 "                               </form>" + 
 "		<div class=\"grid1\"> </div>\n" +
 "	</body>\n" +
 "	\n" +
 "</html>";
         
         int aresDamage = 0, playerDamage = 0;      
         
         if(startingState == stateEnum.BATTLE)
         {
             String value = null, valueAttack=request.getParameter("attack"), valueUse=request.getParameter("use"), valueOK=request.getParameter("OK"), itemName;
             if(valueAttack!=null)
                 value = valueAttack;
             
             if(valueUse!=null)
             {
                 value=valueUse;
                 itemName = request.getParameter("itemSelected");
             }
             if(valueOK!=null)
                 value=valueOK;
             
             if(!value.equals("OK"))
             {
                 actionEnum playerAction = playerChar.requestAction(request);
                 actionEnum aresAction = aresChar.requestAction(request);
 
                 if(playerAction == actionEnum.ATTACK)
                 {
                     if(playerChar.weapon.getStrength()!=0)
                     {
                         aresDamage = (int) ((playerChar.getStrength()+playerChar.weapon.getStrength())*(Math.random()*.4+.8)-(aresChar.getStrength()*aresChar.armor.getStrength()/100));
                     }
 
                     if(playerChar.weapon.getAgility()!=0)
                     {
                         aresDamage = (int) ((playerChar.getAgility()+playerChar.weapon.getAgility())*(Math.random()*.4+.8)-(aresChar.getAgility()*aresChar.armor.getAgility()/100));
                     }
 
                     if(playerChar.weapon.getMagic()!=0)
                     {
                         aresDamage = (int) ((playerChar.getMagic()+playerChar.weapon.getMagic())*(Math.random()*.4+.8)-(aresChar.getMagic()*aresChar.armor.getMagic()/100));
                     }
                 }
                 if(aresAction == actionEnum.ATTACK)
                 {
                     if(aresChar.weapon.getStrength()!=0)
                     {
                         playerDamage = (int) ((aresChar.getStrength()+aresChar.weapon.getStrength())*(Math.random()*.4+.8)-(playerChar.getStrength()*playerChar.armor.getStrength()/100));
                     }
                     if(aresChar.weapon.getMagic()!=0)
                     {
                         playerDamage = (int) ((aresChar.getMagic()+aresChar.weapon.getMagic())*(Math.random()*.4+.8)-(playerChar.getMagic()*playerChar.armor.getMagic()/100));
                     }
                     if(aresChar.weapon.getAgility()!=0)
                     {
                         playerDamage = (int) ((aresChar.getAgility()+aresChar.weapon.getAgility())*(Math.random()*.4+.8)-(playerChar.getAgility()*playerChar.armor.getAgility()/100));
                     }
                 }
 
                 playerChar.setHealth(playerChar.getHealth() - playerDamage);
                 aresChar.setHealth(aresChar.getHealth() - aresDamage);
 
             }
             
             else if(playerChar.getHealth()<1)
             {
                 //mark the character as dead in the database debug
                 updateCharacter(playerChar, true, out);
                 return stateEnum.PROFILE;
             }
             else if(aresChar.getHealth()<1)
                 return stateEnum.LEVEL_UP;
         }
         
         out.printf(startPage,accountName);
         out.printf(statsTable, playerChar.name, playerChar.getHealth(), playerChar.getStrength(), playerChar.getMagic(), playerChar.getAgility());
         out.printf(equippedTable1, playerChar.weapon.getName(), playerChar.weapon.getStrength(), playerChar.weapon.getMagic(), playerChar.weapon.getAgility());
         out.printf(equippedTable2, playerChar.armor.getName(), playerChar.armor.getStrength(), playerChar.armor.getMagic(), playerChar.armor.getAgility());
         out.printf(betweenCharacters);
         out.printf(statsTable, aresChar.name, aresChar.getHealth(), aresChar.getStrength(), aresChar.getMagic(), aresChar.getAgility());
         out.printf(equippedTable1, aresChar.weapon.getName(),aresChar.weapon.getStrength(), aresChar.weapon.getMagic(), aresChar.weapon.getAgility());
         out.printf(equippedTable2, aresChar.armor.getName(), aresChar.armor.getStrength(), aresChar.armor.getMagic(), aresChar.armor.getAgility());
         out.printf(afterTable);
         
         out.printf("<div>You have done %d damage to your opponent.\n Your opponent has done %d damage to you.</div>", aresDamage, playerDamage);
         
         if((playerChar.getHealth()>0) && (aresChar.getHealth()>0))
         {
             out.printf(attackButton);
             for(int i=0; i < playerChar.itemsHeld.length;i++)
             {
                 //change first string, the value parameter, to itemId
                 out.printf("<option value = \"%s\"> %s </option> \n", playerChar.itemsHeld[i].getName(),playerChar.itemsHeld[i].getName());
             }
             out.printf(useButton);
         }
         else if(playerChar.getHealth()<1)
         {
             out.printf("The valiant hero has been killed.\n");
             out.printf("<input type=\"submit\" name=\"OK\" value=\"OK\" class=\"profileButton\" /> \n");
         }
    
         
         else if(aresChar.getHealth()<1)
         {
             int newGold = (int) (constantGoldPerLevel*playerChar.getLevel()*(Math.random()*.4+.8));
             gold+=newGold;
             playerChar.setHealth(playerChar.getMaxHealth());
             out.printf("Congradulations you beat your enemy.\n You get %d gold.\n", newGold);
             out.printf("<input type=\"submit\" name=\"OK\" value=\"OK\" class=\"profileButton\" /> \n");
         }
         out.printf(lastPart);
         return stateEnum.BATTLE;
     }
 
     /****************************************************
      * Create a registered user character
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum registeredCharacterCreationState(PrintWriter out, HttpServletRequest request) {
      if(startingState != stateEnum.REGISTERED_CHARACTER_CREATION)
         {
             //create new page for it
             Integer level = 1;
             printCharacterCreation(level, out);   
             return stateEnum.REGISTERED_CHARACTER_CREATION;
         }
         else
         {
              stateEnum state;
             try
             {
                 if(checkHome(request))
                 {
                     return stateEnum.INIT;
                 }
             }
             catch(Exception e)
             {
                 state = charCreationParameters(out, request, false);
                 return state;
             }
             state = charCreationParameters(out, request, false);
             return state;
         }
     }
     
     /****************************************************
      * Create an unregistered user character
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum unregisteredCharacterCreationState(PrintWriter out, HttpServletRequest request) {
 
     if((startingState != stateEnum.UNREGISTERED_CHARACTER_CREATION)|(error!=null))
     {
         //create new page for it
         Integer level = (int)(Math.random()*49+1);
         printCharacterCreation(level, out);   
         return stateEnum.UNREGISTERED_CHARACTER_CREATION;
     }
     else
     {
         /*String value = request.getParameter("Home");
         if(value.equals("Home"))
             return stateEnum.INIT;*/
         try
         {
             if(checkHome(request))
             {
                 return stateEnum.INIT;
             }
         }
         catch(Exception e)
         {
             return charCreationParameters(out, request, true);
         }
         return charCreationParameters(out, request, true);
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
         if (startingState != stateEnum.DECISION)
         {
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
                         "	<body>\n" +
                         "	    <form action=\"Tarsus\" method=\"POST\">\n" +
                         "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
                         "			<input name=\"" + accountName + "\" value=\"" + accountName + "\" type=\"submit\" id=\"tarsusTitle\" /> \n" +
                         "			<input class=\"button\" name=\"Log Out\" value=\"Log Out\" type=\"submit\" /> </div>\n" +
                         "		<div class=\"grid1\"> </div>\n" +
                         "		<div class=\"grid8 centered\">\n" +
                         "			<h1 id=\"title\" class=\"centered\">" + playerChar.getName() + "</h1>\n" +
                         "			<p align=\"center\">\n" +
                         "				<input name=\"To Battle!\" value=\"To Battle!\" type=\"submit\" class=\"profileButton\" />\n" +
                         "				<input name=\"Store\" value=\"Store\" type=\"submit\" class=\"profileButton\" />\n" +
                         "				<input name=\"Blacksmith\" value=\"Blacksmith\" type=\"submit\" class=\"profileButton\" />\n" +
                         "			</p>\n" +
                         "		</div>\n" +
                         "		<div class=\"grid1\"> </div>\n" +
                         "	    </form>\n" +
                         "	</body>\n" +
                         "</html>");
             return stateEnum.DECISION;
         }
         else
         {
             String value1 = request.getParameter(accountName);
             String value2 = request.getParameter("Log Out");
             String value3 = request.getParameter("To Battle!");
             String value4 = request.getParameter("Store");
             String value5 = request.getParameter("Blacksmith");
             
             String value = "";
             if(value1 != null)
                 value = value1;
             if(value2 != null)
                 value = value2;
             if(value3 != null)
                 value = value3;
             if(value4 != null)
                 value = value4;
             if(value5 != null)
                 value = value5;
             
             if(value.equals(accountName))
                 return stateEnum.PROFILE;
             if(value.equals("Log Out"))
                 return stateEnum.LOGOUT;
             if(value.equals("To Battle!"))
                 return stateEnum.BATTLE;
             if(value.equals("Store"))
                 return stateEnum.STORE;
             if(value.equals("Blacksmith"))
                 return stateEnum.BLACKSMITH;
         }
         return stateEnum.PROFILE;
     }
 
     /****************************************************
      * At the blacksmith and can upgrade items
      * @param out the print writer
      * @param request the servlet request
      * @return the next state
      ***************************************************/
     stateEnum blackSmithState(PrintWriter out, HttpServletRequest request) {
         if(startingState != stateEnum.BLACKSMITH)
         {
             printBlacksmithState(out);
             return stateEnum.BLACKSMITH;
         }
         else
         {
             String value1 = request.getParameter(accountName);
             if(value1 != null)
                 return stateEnum.DECISION;
             else
             {
                 for (int i = 0; i < playerChar.itemsHeld.length - 1; i++){
                     String tempValue = request.getParameter("Upgrade" + i);
                     if(tempValue != null)
                     {
                         playerChar.itemsHeld[i].upgradeItem();
                         String query = "UPDATE Items SET upgradeCount=upgradeCount+1, ";
                         if(playerChar.itemsHeld[i].getType() == 1)
                         {
                             if(playerChar.itemsHeld[i].getStrength() > 0)
                             {
                                 query = query + "strengthVal=strengthVal+'" + playerChar.itemsHeld[i].CONSTANT_weaponUpgrade;
                             }
                             if(playerChar.itemsHeld[i].getAgility() > 0)
                             {
                                query = query + "agilityVal=alilityVal+'" + playerChar.itemsHeld[i].CONSTANT_weaponUpgrade;
                             }
                             if(playerChar.itemsHeld[i].getMagic() > 0)
                             {
                                 query = query + "magicVal=magicVal+'" + playerChar.itemsHeld[i].CONSTANT_weaponUpgrade;
                             }
                         }
                         else //playerChar.itemsHeld[i].getType() == 2
                         {
                             query = query + "strengthVal=strengthVal+'" + playerChar.itemsHeld[i].CONSTANT_armorUpgrade + 
                                     "', agilityVal=alilityVal+'" + playerChar.itemsHeld[i].CONSTANT_armorUpgrade + 
                                     "', magicVal=magicVal+'" + playerChar.itemsHeld[i].CONSTANT_armorUpgrade;
                         }
                         query = query + "' WHERE itemId='" + playerChar.itemsHeld[i].getItemId() + "';";
                         connectDB();
                         sqlCommand(query, out);
                         disconnectDB();
                         printBlacksmithState(out);
                         break;
                     }
                 }
                 return stateEnum.BLACKSMITH;
             }
         }
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
             "       <form method=\"post\" action=\"Tarsus\"> \n" +
             "		<input type=\"submit\" name=\"home\" value=\"TARSUS\" id=\"tarsusTitle\">  \n" +
             "       </form> </div>" +
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
             String value1 = request.getParameter("home");
 
             String value = "";
             if(value1 != null)
                 value = value1;
             if(value.equals("TARSUS"))
                 return stateEnum.INIT;
             String username = request.getParameter("username");
             int password = request.getParameter("password").hashCode();
             if(!isValidString(username)){
                 out.println("Error");
                 return stateEnum.LOGIN;
             }
             String search = "SELECT * FROM Login WHERE username='" + username +
                     "' AND password= MD5('" + password+  "');";
             connectDB();
             ResultSet result = sqlQuery(search, out);
             try{
             if(result.isBeforeFirst()){
                     accountName = username;
                     return stateEnum.PROFILE;
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
             "       <form method=\"post\" action=\"Tarsus\"> \n" +
             "		<input type=\"submit\" name=\"home\" value=\"TARSUS\" id=\"tarsusTitle\">  \n" +
             "		<input type=\"submit\" name=\"login\" value=\"Log In\" class=\"button\" href=\"login.html\"> \n" +
             "       </form> </div>" +
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
             String value1 = request.getParameter("home");
             String value2 = request.getParameter("login");
 
             String value = "";
             if(value1 != null)
                 value = value1;
             if(value2!=null)
                 value = value2;
           
             if(value.equals("Log In"))
                 return stateEnum.LOGIN;
             if(value.equals("TARSUS"))
                 return stateEnum.INIT;
             
             String username = request.getParameter("username");
             String findUsername = "SELECT username FROM Login "
                     + "WHERE username = \"" + username + "\";";
             
             Boolean alreadyExists = false;
             try{
                 connectDB();
                 ResultSet result = sqlQuery(findUsername, out);
                 if(result.isBeforeFirst()){
                     alreadyExists= true;
                 }
                 
             }catch(Exception ex){
                 out.println("username check failure");  //Test Check
                 out.println(ex);
                 alreadyExists=true;
             }
             
             DBUtilities.closeStatement(stat);
             disconnectDB();
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
                     + password +"'), gold=0);";
             
             try{
                 connectDB();
             if(sqlCommand(command, out))
             {                
                 DBUtilities.closeStatement(stat);
                 disconnectDB();
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
     
     stateEnum profileState(PrintWriter out, HttpServletRequest request) throws SQLException {
         if(startingState != stateEnum.PROFILE)
         {
             printProfileState(out);
         }
         else
         {
             String value1 = request.getParameter(accountName);
             String value2 = request.getParameter("Log Out");
             String value3 = request.getParameter("Create Character");
             String value4 = request.getParameter("Load Character");
             String value5 = request.getParameter("Look at Past Characters");
             
             String value = "";
             if(value1 != null)
                 value = value1;
             if(value2 != null)
                 value = value2;
             if(value3 != null)
                 value = value3;
             if(value4 != null)
                 value = value4;
             if(value5 != null)
                 value = value5;
             
             if(value.equals(accountName))
                 printProfileState(out);
             if(value.equals("Log Out"))
                 return stateEnum.LOGOUT;
             if(value.equals("Create Character"))
                 return stateEnum.REGISTERED_CHARACTER_CREATION;
             if(value.equals("Load Character"))
             {
                 String search1 = "SELECT * FROM Characters WHERE creator='" + accountName + "' AND isDead=0;";
                 connectDB();
                 ResultSet result = sqlQuery(search1, out);
                 if(result.isBeforeFirst())
                 {
                     result.next();
                     String name = result.getString("name");
                     String bio = result.getString("bio");
                     int level = result.getInt("level");
                     int health = result.getInt("health");
                     int strength = result.getInt("strength");
                     int agility = result.getInt("agility");
                     int magic = result.getInt("magic");
                     int timesAttacked = result.getInt("timesAttacked");
                     int timesSwitchedToStrength = result.getInt("timesSwitchedToStrength");
                     int timesSwitchedToAgility = result.getInt("timesSwitchedToAgility");
                     int timesSwitchedToMagic = result.getInt("timesSwitchedToMagic");
                     int equipWeaponId = result.getInt("equippedWeapon");
                     int equipArmorId = result.getInt("equippedArmor");
                     disconnectDB();
                     
                     //getting the length for itemsHeld
                     connectDB();
                     String search2 = "SELECT COUNT(I.itemId) AS rows FROM Items I, CharacterHasItem C WHERE I.itemId=C.itemId AND C.charName='" + name + "';";
                     result = sqlQuery(search2, out);
                     result.next();
                     int rows = result.getInt("rows");
                     disconnectDB();
                     
                     Item[] itemsHeld = new Item[rows];
                     Item weapon = null;
                     Item armor = null;
                     String search3 = "SELECT * FROM Items I, CharacterHasItem C WHERE I.itemId=C.itemId AND C.charName='" + name + "';";
                     connectDB();
                     result = sqlQuery(search3, out);
                     //temp varible
                     int i = 0;
                     while (result.next())
                     {
                         String iName = result.getString("name");
                         int itemId = result.getInt("itemId");
                         int type = result.getInt("type");
                         int upgradeCount = result.getInt("upgradeCount");
                         int strengthVal= result.getInt("strengthVal");
                         int agilityVal = result.getInt("agilityVal");
                         int magicVal = result.getInt("magicVal");
                         Item item = new Item(iName, itemId, type, upgradeCount, strengthVal, agilityVal, magicVal, 0);
                         itemsHeld[i] = item;
                         if (equipWeaponId == itemId)
                         {
                             weapon = new Item(iName, itemId, type, upgradeCount, strengthVal, agilityVal, magicVal, 0);
                         }
                         if (equipArmorId == itemId)
                         {
                             armor = new Item(iName, itemId, type, upgradeCount, strengthVal, agilityVal, magicVal, 0);
                         }
                         i++;
                     }
                     disconnectDB();
                     playerChar = new PlayerCharacter(name, bio, level, health, strength, agility, magic, itemsHeld, weapon, armor, timesAttacked, timesSwitchedToStrength, timesSwitchedToAgility, timesSwitchedToMagic);
                     return stateEnum.DECISION;
                 }
                 else
                 {
                     out.println("No Valid Character");
                     printProfileState(out);
                     return stateEnum.PROFILE;
                 }
             }
             if(value.equals("Look at Past Characters"))
                 return stateEnum.PAST_CHARACTERS;
         }
         return stateEnum.PROFILE;
     }
     
     stateEnum pastCharactersState(PrintWriter out, HttpServletRequest request) {
         if(startingState != stateEnum.ACCOUNT_CREATION)
         {
             String startPart = "<html>\n" +
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
                                 "	<body>\n" +
                                 "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
                                 "			<input class=\"button\" name=\"" + accountName + "\" value=\"" + accountName + "\" type=\"submit\" /> \n" +
                                 "			<input class=\"button\" name=\"Log Out\" value=\"Log Out\" type=\"submit\" /> </div>\n" +
                                 "		<div class=\"grid1\"> </div>\n" +
                                 "		<div class=\"grid8 centered\">\n" +
                                 "		<h1 id=\"title\" class=\"centered\">Past Characters</h1>\n" +
                                 "		<table id=\"table\" align=\"center\">\n" +
                                 "			<tr>\n" +
                                 "				<th> Name </th>\n" +
                                 "				<th> Level </th>\n" +
                                 "				<th> Health </th>\n" +
                                 "				<th> Strength </th>\n" +
                                 "				<th> Agility </th>\n" +
                                 "				<th> Magic </th>\n" +
                                 "				<th> Bio </th>\n" +
                                 "			</tr>\n";
             String lastPart = "			</tr>\n" +
                                 "		</table>\n" +
                                 "		</div>\n" +
                                 "		<div class=\"grid1\"> </div>\n" +
                                 "	</body>\n" +
                                 "	\n" +
                                 "</html>";
             
             out.println(startPart);
 
             ResultSet result;
             int rows = 0;
             try
             {
                             //getting the amount of dead characters
             String search1 = "SELECT COUNT(name) AS rows FROM Characters WHERE creator='" + accountName + "' AND isDead=1;";
             connectDB();
             result = sqlQuery(search1, out);
             result.next();
             rows = result.getInt("rows");
             disconnectDB();
             }
             catch(Exception ex)
             {
                 out.println("Error in getting rows: " + ex);
             }
             
             boolean noDead;
             if(rows > 0)
             {
                     noDead = false;
             }
             else
             {
                     noDead = true;
             }
             
             String search2 = "SELECT * FROM Characters WHERE creator='" + accountName + "' AND isDead=1;";
             connectDB();
             try
             {
                 result = sqlQuery(search2, out);
                 
                 if(noDead)
                 {
                         out.println("<tr>");
                         out.println("<th></th>\n" +
                                 "<th></th>\n" +
                                 "<th></th>\n" +
                                 "<th></th>\n" +
                                 "<th></th>\n" +
                                 "<th></th>\n" +
                                 "<th></th>\n");
                         out.println("</tr>");
                 }
                 else //there are one or more dead characters
                 {
                         while (result.next())
                         {
                             out.println("<td>");
                             out.println(result.getString("name"));
                             out.println("</td>");
                             out.println("<td>");
                             out.println(result.getInt("level"));
                             out.println("</td>");
                             out.println("<td>");
                             out.println(result.getInt("health"));
                             out.println("</td>");
                             out.println("<td>");
                             out.println(result.getInt("strength"));
                             out.println("</td>");
                             out.println("<td>");
                             out.println(result.getInt("agility"));
                             out.println("</td>");
                             out.println("<td>");
                             out.println(result.getInt("magic"));
                             out.println("</td>");
                             out.println("<td>");
                             out.println(result.getString("bio"));
                             out.println("</td>");
                             out.println("</tr>\n");
                         }
                 }
             }
             catch(Exception ex)
             {
                     out.println("Error grabbing dead characters: " + ex);
             }
             disconnectDB();
 
             out.println(lastPart);
             
             return stateEnum.PAST_CHARACTERS;
         }
         else
         {
             String value1 = request.getParameter(accountName);
             String value2 = request.getParameter("Log out");
 
             String value = "";
             if(value1 != null)
                 value = value1;
             if(value2!=null)
                 value = value2;
           
             if(value.equals(accountName))
                 return stateEnum.PROFILE;
             if(value.equals("Log out"))
                 return stateEnum.LOGOUT;
         }
         return stateEnum.PROFILE;
     }
     
     stateEnum LogoutState(PrintWriter out, HttpServletRequest request) {
         playerChar = null;
         aresChar = null;
         accountName = "Unregistered User";
         gold = 0;
         error = null;
         //accountName = null;
         return stateEnum.INIT;
         //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     /****************************************************
      * Checks the validity of a String for the database
      * @param string the string to check for validity
      * @return the validity
      ***************************************************/
     Boolean isValidString(String string)
     {
         Boolean toBeReturned = true;
         
         if(string.contains("drop"))
             toBeReturned = false;
         if(string.contains("delete"))
             toBeReturned = false;
         if(string.contains(";"))
             toBeReturned = false;
         
         return toBeReturned;
     }
     
     String maxValueScript(int value)
     {
         return ("<script> var maxValue=" + Integer.toString(value) +";</script>");
     }
 	
     double getQuality(int level)
     {
         double ratio = ((double)level) / ((double)level + 1.0);
 	double quality = Math.random() * ratio;
 	return quality;
     }
     
     void printBlacksmithState(PrintWriter out)
     {
         String startPart = "<html>\n" +
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
             "	<body>\n" +
             "   <form action=\"Tarsus\" method=\"post\">" + 
             "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
             "			<input value=\"" + accountName + "\" name=\"" + accountName + "\" type=\"submit\" id=\"tarsusTitle\" />\n" +
             "			<input class=\"button\" value=\"Log Out\" name=\"Log Out\" type=\"submit\" /> </div>\n" +
             "		<div class=\"grid1\"> </div>\n" +
             "		<div class=\"grid8 centered\">\n" +
             "		<h1 id=\"title\" class=\"centered\">Blacksmith</h1>\n" +
             "		<table id=\"table\" align=\"center\">\n" +
             "			<tr>\n" +
             "				<td> </td>\n" +
             "				<th> Name </th>\n" +
             "				<th> Strength </th>\n" +
             "				<th> Magic </th>\n" +
             "				<th> Agility </th>\n" +
             "				<th> Type </th>\n" +
             "                           <th> Times Upgraded </th>\n" +
             "			</tr>\n";
         String endPart = "</table>\n" +
             "		</div>\n" +
             "		<div class=\"grid1\"> </div>\n" +
             "       </form>" +
             "	</body>\n" +
             "	\n" +
             "</html>";
         
         out.println(startPart);
         boolean noItems;
         if(playerChar.itemsHeld.length > 0)
         {
             noItems = false;
         }
         else
         {
             noItems = true;
         }
 
         if(noItems)
         {
             out.println("<tr>");
             out.println("<td> </td>\n" +
                         "<th></th>\n" +
                         "<th></th>\n" +
                         "<th></th>\n" +
                         "<th></th>\n" +
                         "<th></th>\n" +
                         "<th></th>\n");
             out.println("</tr>");
         }
         else //there are one or more items
         {
             for (int i = 0; i < playerChar.itemsHeld.length; i++){
                 if(playerChar.itemsHeld[i].getUpgradeCount() < 3)
                 {
                     out.println("<tr>\n");
                     out.println("<td> <input value=\"Upgrade\" name=\"Upgrade" + i + "\" type=\"submit\" class=\"tableButton\" /> </td>");
                     out.println("<td>");
                     out.println(playerChar.itemsHeld[i].getName());
                     out.println("</td>");
                     out.println("<td>");
                     out.println(playerChar.itemsHeld[i].getStrength());
                     out.println("</td>");
                     out.println("<td>");
                     out.println(playerChar.itemsHeld[i].getAgility());
                     out.println("</td>");
                     out.println("<td>");
                     out.println(playerChar.itemsHeld[i].getMagic());
                     out.println("</td>");
                     out.println("<td>");
                     out.println(playerChar.itemsHeld[i].getType());
                     out.println("</td>");
                     out.println("<td>");
                     out.println(playerChar.itemsHeld[i].getUpgradeCount());
                     out.println("</td>");
                     out.println("</tr>\n");
                 }
             }
         }
         out.println(endPart);
     }
     
 
     void printProfileState(PrintWriter out)
     {
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
             "	<body>\n" +
             "   <form action=\"Tarsus\" method=\"post\">" + 
             "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
             "			<input name=\"" + accountName + "\" value=\"" + accountName + "\" id=\"tarsusTitle\" type=\"submit\" /> \n" +
             "			<input class=\"button\" name=\"Log Out\" value=\"Log Out\" type=\"submit\" /> </div>\n" +
             "		<div class=\"grid2\"> </div>\n" +
             "		<div class=\"grid6 centered\">\n" +
             "			<h1 id=\"title\" class=\"centered\">TARSUS</h1> <br />\n" +
             "			<div align=\"center\"> \n" +
             "				<input class=\"profileButton\" name=\"Create Character\" value=\"Create Character\" type=\"submit\" />\n" +
             "				<input class=\"profileButton\" name=\"Load Character\" value=\"Load Character\" type=\"submit\" />  \n" +
             "				<input class=\"profileButton\" name=\"Look at Past Characters\" value=\"Look at Past Characters\" type=\"submit\" /> \n" +
             "			</div>\n" +
             "		</div>\n" +
             "		<div class=\"grid1\"> </div>\n" +
             "           </form>" +
             "	</body>\n" +
             "	\n" +
             "</html>");
     }
 
     
     public void printStoreState(PrintWriter out)
     {
         String item_type_string[] = {"Error", "Weapon", "Armor", "Item"};
 
 			
 			String startPart = "<html>\n" +
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
 		            "	<body>\n" +
 		            "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
 		            "			<input value=\"Character Page\" name=\""  + "\" type=\"submit\" id=\"tarsusTitle\" />\n" +
 		            "			<input class=\"button\" type=\"submit\" value=\"Log Out\" name=\"Log Out\" /> </div>\n" +
 		            "		<div class=\"grid1\"> </div>\n" +
 		            "		<div class=\"grid8 centered\">\n" +
 		            "		<h1 id=\"title\" class=\"centered\">Store</h1>\n" +
 		            "		<table id=\"table\" align=\"center\">\n" +
 		            "			<tr>\n" +
 		            "				<td> </td>\n" +
 		            "				<th> Name </th>\n" +
 		            "				<th> Strength </th>\n" +
 		            "				<th> Magic </th>\n" +
 		            "				<th> Agility </th>\n" +
 		            "				<th> Heal </th>\n" +
 		            "				<th> Type </th>\n" + 
 		            "				<th> Price </th>\n" +
 		            "			</tr>\n" +
 		            "			<tr>";
 			String sellPart = "		</table>\n" +
                     "		</div>\n" +
                     "		<div class=\"grid1\"> </div>\n" +
 					"<div class=\"grid1\"> </div>\n" +
 		            "		<div class=\"grid8 centered\">\n" +
 		            "		<h1 id=\"title\" class=\"centered\">Your Items</h1>\n" +
 		            "		<table id=\"table\" align=\"center\">\n" +
 		            "			<tr>\n" +
 		            "				<td> </td>\n" +
 		            "				<th> Name </th>\n" +
 		            "				<th> Strength </th>\n" +
 		            "				<th> Magic </th>\n" +
 		            "				<th> Agility </th>\n" +
 		            "				<th> Heal </th>\n" +
 		            "				<th> Type </th>\n" + 
 		            "				<th> Price </th>\n" +
 		            "			</tr>\n" +
 		            "			<tr>";
 			
 			String buttonPart = ("		</table>\n" +
 	                "		</div>\n" +
 	                "		<div class=\"grid1\"> </div>\n" +
 					"		<div class=\"grid10\" align=\"center\">\n" +
 					"			<input id=\"Form\" type =\"submit\" value=\"Initiate Transaction\" class=frontPageButton /> \n" +
 					"		</div>\n" +
 					"		</form>\n");
 			
             String endPart = 
                     "	</body>\n" +
                     "	\n" +
                     "</html>";
             String script = "<script> function getFormValues() {" +
             "for(var i = 0; i < 20; i++){" +
                 "	var item = document.getElementById('i');" +
                 "	alert(item.getAttribute('name'));" +
 
                             "} return false;} </script>";
 
             out.println(startPart);
 
             out.println("<form name=\"buyItems\" action=\"Tarsus\" onsubmit=\"return getFormValues()\" method=\"post\">\n");
             for (int i = 0; i < storeItems.length; i++){
                 out.println("<td> <input id =\"" + i + "\" type=\"submit\" value=\"Buy for " + storeItems[i].getValue() + "\" name=\"Buy " + i + "\" class=\"tableButton\"> </td>");
                 out.println("<td>");
                 out.println(storeItems[i].getName());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(storeItems[i].getStrength());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(storeItems[i].getAgility());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(storeItems[i].getMagic());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(storeItems[i].getHeal());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(item_type_string[storeItems[i].getType()]);
                 out.println("</td>");
                 out.println("<td>");
                 out.println(storeItems[i].getValue());
                 out.println("</td>");
                 out.println("</tr>");
 			}
 	out.println(sellPart);
 	for (int i = 0; i < playerChar.itemsHeld.length; i++){
                 out.println("<td> <input type=\"submit\" value=\"Sell for " + (int)(0.60 * storeItems[i].getValue()) + "\" name=\"Sell " + i + "\" class=\"tableButton\"> </td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getName());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getStrength());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getAgility());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getMagic());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[i].getHeal());
                 out.println("</td>");
                 out.println("<td>");
                 out.println(playerChar.itemsHeld[storeItems[i].getType()]);
                 out.println("</td>");
                 out.println("<td>");
                 out.println((int)(0.60 * playerChar.itemsHeld[i].getValue()));
                 out.println("</td>");
                 out.println("</tr>");
 			}
         out.println(buttonPart);
         out.println(endPart);
         out.println(script);
     }
 
     private stateEnum levelUpState(PrintWriter out, HttpServletRequest request)
     {
         if(startingState != stateEnum.LEVEL_UP)
         {
             String page =  "<html>\n" +
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
 "       <script>\n" +
 "		function validateForm()\n" +
 "		{\n" +
 "		\n" +
 "               var maxValue = 5 \n" +
 "			var strength = parseInt(document.forms[\"createCharacterForm\"][\"strength\"].value); \n" +
 "			var agility = parseInt(document.forms[\"createCharacterForm\"][\"agility\"].value);\n" +
 "			var magic = parseInt(document.forms[\"createCharacterForm\"][\"magic\"].value);\n" +
 "                       var health = parseInt(document.forms[\"createCharacterForm\"][\"magic\"].value);\n" +
 "			var total = strength + agility + magic + health;\n" +
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
                 "<form name=\"createCharacterForm\" action=\"Tarsus\" onsubmit=\"return validateForm()\" method=\"post\">\n" +
 "		<div id=\"header\" class=\"grid10\" align=\"right\">\n" +
                 "<input type=\"Submit\" name=\"Home\" value=\"Home\"  class=\"FrontPageButton\" />" +
 "		<div class=\"grid1\"> </div></div>\n" +
 "		<div class=\"grid8 centered\">\n" +
 "		<h1 id=\"title\" class=\"centered\">Character Creation</h1>\n" +
 "		\n" +
 "		<div class=\"grid2\"> </div>\n" +
 "		<div class=\"grid6\" align=\"center\">\n" +
 "			<p> Experience Points to Allocate: 5\n" +
 "			</p>\n" +
 
 "			<p> \n" +
 "				Strength is currently %d add: <input type=\"number\" name=\"strength\"min=\"0\" max=\"5\" value=\"0\"/>\n" +
 "			</p> \n" +
 "			<p> \n" +
 "				Agility is currently %d add: <input type=\"number\" name=\"agility\"min=\"0\" max=\"5\" value=\"0\"/>\n" +
 "			</p>  \n" +
 "			<p> \n" +
 "				Magic is currently %d add: <input type=\"number\" name=\"magic\" min=\"0\" max=\"5\" value=\"0\"/>\n" +
 "			</p>   \n" +
 "			<p> \n" +
 "				Health is currently %d add: <input type=\"number\" name=\"health\" min=\"0\" max=\"5\" value=\"0\"/>\n" +
 "			</p>   \n" +
 "		</div>\n"+
 "		<div class=\"grid10\" align=\"center\">\n" +
 "			<input type =\"submit\" value=\"update level\" class=frontPageButton /> \n" +
 "		</div>\n" +
 "		</form>\n" +
 "		</div>\n" +
 "		<div class=\"grid1\"> </div>\n" +
 "	</body>\n" +
 "	\n" +
 "</html>";
             out.printf(page,playerChar.getStrength(),playerChar.getAgility(),playerChar.getMagic(),playerChar.getMaxHealth()/constantHealthPerLevel);
             return stateEnum.LEVEL_UP;
         }
         else
         {
             int health = Integer.parseInt(request.getParameter("health"));
             int strength = Integer.parseInt(request.getParameter("strength"));
             int agility = Integer.parseInt(request.getParameter("agility"));
             int magic = Integer.parseInt(request.getParameter("magic"));
             
             playerChar.setMaxHealth(playerChar.getMaxHealth()+health*constantHealthPerLevel);
             playerChar.setHealth(playerChar.getMaxHealth());
             playerChar.setStrength(playerChar.getStrength()+strength*constantStrengthPerLevel);
             playerChar.setAgility(playerChar.getAgility()+agility*constantAgilityPerLevel);
             playerChar.setMagic(playerChar.getMagic()+magic*constantMagicPerLevel);
                         
             //update database
             updateCharacter(playerChar, false, out);
             return stateEnum.DECISION;
         }
     }
 
     private void printCharacterCreation(Integer level, PrintWriter out) {
         String StartPage = "<html>\n" +
 "        <head>\n" +
 "        <!-- Call normalize.css -->\n" +
 "        <link rel=\"stylesheet\" href=\"css/normalize.css\" type=\"text/css\" media=\"screen\">\n" +
 "        <!-- Import Font to be used in titles and buttons -->\n" +
 "        <link href='http://fonts.googleapis.com/css?family=Sanchez' rel='stylesheet' type='text/css'>\n" +
 "        <link href='http://fonts.googleapis.com/css?family=Prosto+One' rel='stylesheet' type='text/css'>\n" +
 "        <!-- Call style.css -->\n" +
 "        <link rel=\"stylesheet\" href=\"css/grid.css\" type=\"text/css\" media=\"screen\">\n" +
 "        <!-- Call style.css -->\n" +
 "        <link rel=\"stylesheet\" href=\"css/style.css\" type=\"text/css\" media=\"screen\">\n" +
 "        <title> Tarsus </title>\n" +
 "        </head>\n" +
 "       <script>\n" +
 "                function validateForm()\n" +
 "                {\n" +
 "                \n" +
 "               var maxValue = ";
         String secondPart = "; \n" +
 "                        var strength = parseInt(document.forms[\"createCharacterForm\"][\"strength\"].value); \n" +
 "                        var agility = parseInt(document.forms[\"createCharacterForm\"][\"agility\"].value);\n" +
 "                        var magic = parseInt(document.forms[\"createCharacterForm\"][\"magic\"].value);\n" +
 "                       var health = parseInt(document.forms[\"createCharacterForm\"][\"magic\"].value);\n" +
 "                        var total = strength + agility + magic + health;\n" +
 "                        alert(\"Total Experience points used: \" + total);\n" +
 "                        if(total > maxValue)\n" +
 "                        {\n" +
 "                                alert(\"Cannot use more than\" + maxValue + \" experience points.\");\n" +
 "                                return false;\n" +
 "                        }\n" +
 "                \n" +
 "                }\n" +
 "       </script>" + 
 "        <body>\n" +
 "                <form action=\"Tarsus\" method=\"post\">" +
 "                <div id=\"header\" class=\"grid10\" align=\"right\">\n" +
                 "<input type=\"Submit\" name=\"Home\" value=\"Home\"  class=\"FrontPageButton\" />" +
 "                <div class=\"grid1\"> </div></div>\n" +
 "                <div class=\"grid8 centered\">\n" +
                 "</form>" +
 "<form name=\"createCharacterForm\" action=\"Tarsus\" onsubmit=\"return validateForm()\" method=\"post\">\n" +
 "                <h1 id=\"title\" class=\"centered\">Character Creation</h1>\n" +
 "                \n" +
 "                <div class=\"grid2\"> </div>\n" +
 "               <input type = \"hidden\" name = \"level\" value=\"";
         String thirdPart = "\"/>\n"+
 "                <div class=\"grid6\" align=\"center\">\n" +
 "                        <h3> Level ";
         String fourthPart = " </h3>\n" +
 "                        <p> Experience Points to Allocate: ";
         String fifthPart = "\n" +
 "                        </p>\n" +
 "                        <p> \n" +
 "                                Name: <input type=\"text\" name=\"name\"/>\n" +
 "                        </p>\n" +
 "                        <p> \n" +
 "                                Strength: <input type=\"number\" name=\"strength\"min=\"0\" max=\"100\" value=\"0\"/>\n" +
 "                        </p> \n" +
 "                        <p> \n" +
 "                                Agility: <input type=\"number\" name=\"agility\"min=\"0\" max=\"100\" value=\"0\"/>\n" +
 "                        </p>  \n" +
 "                        <p> \n" +
 "                                Magic: <input type=\"number\" name=\"magic\" min=\"0\" max=\"100\" value=\"0\"/>\n" +
 "                        </p>   \n" +
 "                        <p> \n" +
 "                                Health: <input type=\"number\" name=\"health\" min=\"0\" max=\"100\" value=\"0\"/>\n" +
 "                        </p>   \n" +
 "                        <p>\n" +
 "                                Biography:<textarea name=\"bio\" cols=\"35\" rows=\"3\" maxlength=\"300\"> </textarea> <br /> <a id=\"bioLimitID\">  (Max of 300 Chars)</a>\n" +
 "                        </p>\n";
                 String lastPart = 
 "                </div>\n"+
 "                <div class=\"grid10\" align=\"center\">\n" +
 "                        <input type =\"submit\" value=\"Create a Character\" class=frontPageButton /> \n" +
 "                </div>\n" +
 "                </form>\n" +
 "                </div>\n" +
 "                <div class=\"grid1\"> </div>\n" +
 "        </body>\n" +
 "        \n" +
 "</html>";
             int numItemChoices = 5;
             Item tempItem;
             String submitValue;
             
             out.printf(StartPage);
             out.println(((Integer)(level*constantPtsPerLevel)).toString());
             out.printf(secondPart);
             out.printf(level.toString());
             out.printf(thirdPart);
             out.printf(level.toString());
             out.printf(fourthPart);
             out.printf(((Integer)(level*constantPtsPerLevel)).toString());
             out.printf(fifthPart);
             out.printf("<input type=\"hidden\" name=\"level\" value=\"%d\" />\n",level);
             
             out.println("<table><tr><h2>Weapons</h2></tr><tr><th>Strength</th><th>Agility</th><th>Magic</th><th>select</th><tr>");
             for(int i=0; i<numItemChoices; i++)
             {
                 tempItem = generateWeapon(level);
                 submitValue = tempItem.getName()+"="+((Integer)tempItem.itemId).toString()+"+"+((Integer)tempItem.getStrength()).toString()+"-"+((Integer)tempItem.getAgility()).toString()+"*"+((Integer)tempItem.getMagic()).toString()+"_"+((Integer)tempItem.getType()).toString();
                 out.printf("<tr><td>%d</td><td>%d</td><td>%d</td><td><input type=\"radio\" name=\"weapon\" value=\"%s\"></td></tr>\n",tempItem.getStrength(), tempItem.getAgility(), tempItem.getMagic(), submitValue);
             }
             out.println("</table>");
             
             out.println("<table><tr><h2>Armor</h2></tr><tr><th>Strength</th><th>Agility</th><th>Magic</th><th>select</th><tr>");
             for(int i=0; i<numItemChoices; i++)
             {
                 tempItem = generateArmor(level);
                 submitValue = tempItem.getName()+"="+((Integer)tempItem.itemId).toString()+"+"+((Integer)tempItem.getStrength()).toString()+"-"+((Integer)tempItem.getAgility()).toString()+"*"+((Integer)tempItem.getMagic()).toString()+"_"+((Integer)tempItem.getType()).toString();
                 out.printf("<tr><td>%d</td><td>%d</td><td>%d</td><td><input type=\"radio\" name=\"armor\" value=\"%s\"></td></tr>\n",tempItem.getStrength(), tempItem.getAgility(), tempItem.getMagic(), submitValue);
             }
             out.println("</table>");
             out.println(lastPart);
             if(error!=null)
                 out.printf("<script>alert(\"%s\");</script>",error);
             error = null;
            
     }
 
     private boolean checkHome(HttpServletRequest request)
     {
         String value = request.getParameter("Home");
         return value.equals("Home");
     }
 
     private stateEnum charCreationParameters(PrintWriter out, HttpServletRequest request, Boolean isUnReg) {
         
         String name = (String) request.getParameter("name");
         String bio = request.getParameter("bio");
         int level = Integer.parseInt(request.getParameter("level"));
         int health = (Integer.parseInt(request.getParameter("health"))*constantHealthPerLevel + constantHealthBase);
         int strength = (Integer.parseInt(request.getParameter("strength"))*constantStrengthPerLevel);
         int agility = (Integer.parseInt(request.getParameter("agility"))*constantAgilityPerLevel);
         int magic = (Integer.parseInt(request.getParameter("magic"))*constantMagicPerLevel);
         Item[] items = {new Item(request.getParameter("weapon")), new Item(request.getParameter("armor"))};
         
         try{
             if(isValidString(name) & isValidString(bio))
             {
                newItem(items[0], out);
                newItem(items[1], out);
                PlayerCharacter chrct = new PlayerCharacter(name,bio, level, health, strength, agility, magic, items,items[0],items[1],0,0,0,0);
 
                newCharacter(chrct,isUnReg, out);
                /*
                out.println(name);
                out.printf("level: %d\n",level);
                out.println(bio);
                out.printf("health: %d\n",health);
                out.printf("strength: %d\n",strength);
                out.printf("agility: %d\n",agility);
                out.printf("magic: %d\n",magic);
                out.printf("%s\n",items[0].name);
                out.printf("%d\n",items[0].itemId);
                out.printf("%d\n",items[0].strength);
                out.printf("%d\n",items[0].agility);
                out.printf("%d\n",items[0].magic);
                out.printf("%d\n",items[0].type);
 
                out.printf("%s\n",items[1].name);
                out.printf("%d\n",items[1].itemId);
                out.printf("%d\n",items[1].strength);
                out.printf("%d\n",items[1].agility);
                out.printf("%d\n",items[1].magic);
                out.printf("%d\n",items[1].type);*/
                if(isUnReg)
                     return stateEnum.INIT;
                return stateEnum.DECISION;
             }
             else
             {
                 error = "The character name or bio is invalid or there was a database error";
                 if(isUnReg)
                     return stateEnum.UNREGISTERED_CHARACTER_CREATION;
                 return stateEnum.REGISTERED_CHARACTER_CREATION;
             }
         }
         catch(Exception e)
         {
             error = "The character name or bio is invalid or there was a database error";
             if(isUnReg)
                 return stateEnum.UNREGISTERED_CHARACTER_CREATION;
             return stateEnum.REGISTERED_CHARACTER_CREATION;
         }
     }
 }
