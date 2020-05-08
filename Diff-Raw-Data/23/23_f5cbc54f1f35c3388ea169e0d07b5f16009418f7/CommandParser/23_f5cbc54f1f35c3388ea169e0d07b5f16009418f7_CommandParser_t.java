 /**
  * - Course: CPTR310 Database Application Programming
  * - Institution: Andrews University
  * - Semester: Fall 2011
  * - Instructor: Dr. Roy Villafane
  *
  * - Item: ?
  *
  * - Student: Andrew Breja
  *
  * - Description: >
  *     Command line CRUD database application using Java.
  *     CRUD is create (C), read (R), update (U) and delete (D).
  */
 
 package dbx;
 
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.security.DigestException;
 import java.security.MessageDigest;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 /**
  * Parse and execute given commands.
  */
 public class CommandParser
 {
     /**
      * Database access object used as the target of commands.
      */
     private DBX dbx;
        
     /**
      * Source of input for commands.
      */
     private InputStream is = System.in;
 
     /**
      * Target of output for results.
      */
     private PrintStream ps = System.out;
 
     /**
      * Scanner for commands.
      */
     private Scanner sc = new Scanner(is);
     
     /**
      * Set the database access object.
      * @param d Database access object
      */
     public void setDbx(DBX d)
     {
         dbx = d;
     }
 
     /**
      * Get the database access object.
      * @return Database access object.
      */
     public DBX getDbx()
     {
         return dbx;
     }
 
 
     /**
      * Process a command. Assumes that a command is available and that
      * it is formatted correctly.
      *
      * @return False if command was exit command, otherwise true.
      */
     public boolean processOneCommand() throws Exception
     {
         boolean ret = true;
         String cmd;
         cmd = sc.next();
         if(cmd.equals("EXIT"))
         {
             // End the processing of commands
             ps.println("OUTPUT EXIT");
             ret = false;
         }
         else if(cmd.equals("CREATE"))
         {
             // Create an object and save it on the database.
             String subCmd;
             subCmd = sc.next();
             if(subCmd.equals("npc"))
             {
                 // Create a test entity.
                 NPC t = new NPC(dbx);
                 
                 ps.print("Name:" );
                 t.setName(sc.next());
                 
                 ps.print("Aggressiveness: ");
                 t.setAggressiveness(sc.nextDouble());
                 
                 ps.print("Benevolence: ");
                 t.setBenevolence(sc.nextDouble());
                 
                 ps.print("Pos X: ");
                 t.setPosX(sc.nextInt());
                 
                 ps.print("Pos Y: ");
                 t.setPosY(sc.nextInt());
                 
                 ps.print("Region ID: ");
                 t.setRegionID(sc.nextInt());
                 
                 t.db_create(); // Create the object on the database.
                 ps.println("OUTPUT CREATE npc " + t.getId() + " " + t.getName() + " " + t.getAggressiveness() + " " + t.getBenevolence() + " " + t.getPosX() + " " + t.getPosY() + " " + t.getRegionID());
             }
             else if(subCmd.equals("player"))
             {
                 // Create a test entity.
                 Player t = new Player(dbx);
                 
                 ps.print("Name:" );
                 t.setName(sc.next());
                 
                 ps.print("Health: ");
                 t.setHealth(sc.nextInt());
                 
                 ps.print("Skill Level: ");
                 t.setSkillLevel(sc.nextInt());
 
                 ps.print("Birth Date: ");
                 t.setBirthDate(sc.nextInt());
                 
                 ps.print("Pos X: ");
                 t.setPosX(sc.nextInt());
                 
                 ps.print("Pos Y: ");
                 t.setPosY(sc.nextInt());
                 
                 ps.print("Region ID: ");
                 t.setRegionID(sc.nextInt());
                 
                 t.db_create(); // Create the object on the database.
                 ps.println("OUTPUT CREATE player " + t.getId() + " " + t.getName() + " " + t.getHealth() + " " + t.getSkillLevel() + " " + t.getBirthDate() + " " + t.getPosX() + " " + t.getPosY() + " " + t.getRegionID());
             }
         }
         else if(cmd.equals("READ"))
         {
             // Read an object from the database.
             String subCmd;
             subCmd = sc.next();
             if(subCmd.equals("npc"))
             {
                 // Read a test entity.
                 NPC t = new NPC(dbx);
                 
                 ps.print("ID: ");
                 t.setId(sc.nextLong());
                
                 t.db_read(); // Read the object from the database.
                 ps.println("OUTPUT READ npc " + t.getId() + " " + t.getName() + " " + t.getAggressiveness() + " " + t.getBenevolence() + " " + t.getPosX() + " " + t.getPosY() + " " + t.getRegionID());
             }
             else if(subCmd.equals("player"))
             {
                 // Read a test entity.
                 Player t = new Player(dbx);
                 
                 ps.print("ID: ");
                 t.setId(sc.nextLong());
                
                 t.db_read(); // Read the object from the database.
                 ps.println("OUTPUT READ player " + t.getId() + " " + t.getName() + " " + t.getHealth() + " " + t.getSkillLevel() + " " + t.getBirthDate() + " " + t.getPosX() + " " + t.getPosY() + " " + t.getRegionID());
             }
         }
         else if(cmd.equals("UPDATE"))
         {
             // Update an object's data on the database.
             String subCmd;
             subCmd = sc.next();
             if(subCmd.equals("npc"))
             {
                 // Update a test entity
                 NPC t = new NPC(dbx);
                 
                 ps.print("ID: ");
                 t.setId(sc.nextLong());
                 
                 ps.print("Name: ");
                 t.setName(sc.next());
                 
                 ps.print("Aggressiveness: ");
                 t.setAggressiveness(sc.nextDouble());
                 
                 ps.print("Benevolence: ");
                 t.setBenevolence(sc.nextDouble());
                 
                 ps.print("Pos X: ");
                 t.setPosX(sc.nextInt());
                 
                 ps.print("Pos Y: ");
                 t.setPosY(sc.nextInt());
                 
                 ps.print("Region ID: ");
                 t.setRegionID(sc.nextInt());
                 
                 
                 t.db_update(); // Update the object data on the database.
                 ps.println("OUTPUT UPDATE npc " + t.getId() + " " + t.getName() + " " + t.getAggressiveness() + " " + t.getBenevolence() + " " + t.getPosX() + " " + t.getPosY() + " " + t.getRegionID());
             }
             else if(subCmd.equals("player"))
             {
                 // Get ID
                 long id;
                 ps.print("ID: ");
                 id = sc.nextLong();
  
                 // If admin or editing players id
                 if(Global.is_admin_authn ||
                    (Global.is_player_authn && id == Global.authn_player_id))
                 {
                     // Update a test entity
                     Player t = new Player(dbx);
                     
                     ps.print("Name:" );
                     t.setName(sc.next());
                     
                     ps.print("Health: ");
                     t.setHealth(sc.nextInt());
                     
                     ps.print("Skill Level: ");
                     t.setSkillLevel(sc.nextInt());
 
                     ps.print("Birth Date");
                     t.setBirthDate(sc.nextInt());
                     
                     ps.print("Pos X: ");
                     t.setPosX(sc.nextInt());
                     
                     ps.print("Pos Y: ");
                     t.setPosY(sc.nextInt());
                     
                     ps.print("Region ID: ");
                     t.setRegionID(sc.nextInt());
                     
                     t.db_create(); // Create the object on the database.
                     ps.println("OUTPUT UPDATE player " + t.getId() + " " + t.getName() + " " + t.getHealth() + " " + t.getSkillLevel() + " " + t.getBirthDate() + " " + t.getPosX() + " " + t.getPosY() + " " + t.getRegionID());
                 }
                 else
                 {
                     ps.println("OUTPUT UPDATE player FAILED: incorrect id or not logged in or other problem");
                 }
             }
         }
         else if(cmd.equals("DELETE"))
         {
             // Delete an object from the database.
             String subCmd;
             subCmd = sc.next();
             if(subCmd.equals("npc"))
             {
                 // Delete a test entity
                 NPC t = new NPC(dbx);
                 
                 ps.print("ID: ");
                 t.setId(sc.nextLong());
                 
                 t.db_delete(); // Perform the database deletion operation.
                 ps.println("OUTPUT DELETE npc " + t.getId());
             }
 
             else if(subCmd.equals("player"))
             {
                 // Get ID
                 long id;
                 ps.print("ID: ");
                 id = sc.nextLong();
  
                 // If admin or editing players id
                 if(Global.is_admin_authn ||
                    (Global.is_player_authn && id == Global.authn_player_id))
                 {
                     // Delete a test entity
                     Player t = new Player(dbx);
 
                     // Set ID
                     t.setId(id);
                     
                     t.db_delete(); // Perform the database deletion operation.
                     ps.println("OUTPUT DELETE npc " + t.getId());
                 }
                 else
                 {
                     ps.println("OUTPUT DELETE player FAILED: incorrect id or not logged in or other problem");
                 }
             }
         }
         else if(cmd.equals("READ_ALL"))
         {
             // Read all objects from the database into a collection.
             String subCmd;
             subCmd = sc.next();
             if(subCmd.equals("npcs"))
             {
                 // Read all test entities
                 Collection<NPC> tc = new LinkedList<NPC>();
                 
                 // Populate the vector using the database object iterator.
                 {
                     NPC t = new NPC(dbx);
 
                     t.db_iter_begin();
                     while(t.db_iter_next())
                     {
                         tc.add(new NPC(t));
                     }
                     t.db_iter_end();
                 }
                 
                 for(NPC t: tc)
                 {
                     ps.println("OUTPUT READ ALL npcs " + t.getId() + " " + t.getName() + " " + t.getAggressiveness() + " " + t.getBenevolence() + " " + t.getPosX() + " " + t.getPosY() + " " + t.getRegionID());
                 }
             }
             else if(subCmd.equals("players"))
             {
                 // Read all test entities
                 Collection<Player> tc = new LinkedList<Player>();
                 
                 // Populate the vector using the database object iterator.
                 {
                     Player t = new Player(dbx);
 
                     t.db_iter_begin();
                     while(t.db_iter_next())
                     {
                         tc.add(new Player(t));
                     }
                     t.db_iter_end();
                 }
                 
                 for(Player t: tc)
                 {
                     ps.println("OUTPUT READ ALL player " + t.getId() + " " + t.getName() + " " + t.getHealth() + " " + t.getSkillLevel() + " " + t.getBirthDate() + " " + t.getPosX() + " " + t.getPosY() + " " + t.getRegionID());
                 }
             }
         }
         else if(cmd.equals("WORLD_DISPLAY"))
         {
             // Generates an SVG document with all the world contents
             dbx.world_display();
         }
    
         return ret;
     }
 
      
     
     /**
      * Process all commands until exit command is found
      */
     public void processAllCommands() throws Exception
     {
         boolean go = true;
         while(go)
         {
             go = processOneCommand();
         }
     }
 }
