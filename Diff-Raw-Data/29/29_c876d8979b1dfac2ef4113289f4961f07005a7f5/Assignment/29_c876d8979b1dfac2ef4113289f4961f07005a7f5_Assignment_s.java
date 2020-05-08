 package com.github.CubieX.Assignment;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import com.alta189.sqlLibrary.SQLite.sqlCore;
 
 public class Assignment extends JavaPlugin implements Listener
 {    
     private static final Logger log = Logger.getLogger("Minecraft");
     public static String logPrefix = "[Assignment] "; // Prefix to go in front of all log entries
     private ASSConfigHandler cHandler = null;
     private ASSSchedulerHandler schedHandler = null;
     private ASSCommandHandler comHandler = null;
     public static Economy econ = null;
     public File pFolder = new File("plugins" + File.separator + "Assignment"); // Folder to store plugin settings file and database
     public sqlCore manageSQLite; // SQLite handler
 
     private Assignment aInst;
     static Player assignee = null;
     static Player assigner = null;
     static String currency = "";
     static String completedAssTag = "<E>";
     static String assignmentStateActive = "active";
     static String assignmentStateCompleted = "completed";
     static boolean debug = false;
     public Boolean blockNextBlockPlacing = false;
 
     @Override
     public void onEnable()
     {     
         this.aInst = this;
 
         if (!setupEconomy() ) {
             log.info(String.format("[%s] - Disabled due to no Vault found!", getDescription().getName()));
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         getServer().getPluginManager().registerEvents(this, this);
         log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");
 
         cHandler = new ASSConfigHandler(this);
         schedHandler = new ASSSchedulerHandler(this);
         comHandler = new ASSCommandHandler(this, log, cHandler);
         getCommand("ass").setExecutor(comHandler);
 
         setStaticValues();
 
         // Initializing SQLite ++++++++++++++++++++++++++++++++++++++++++++
         log.info(logPrefix + "SQLite Initializing");
 
         // Declare SQLite handler
         this.manageSQLite = new sqlCore(log, "AssignmentDB", pFolder.getPath());
 
         // Initialize SQLite handler
         this.manageSQLite.initialize();
 
         // Check if the table exists, if it doesn't create it
         if (!this.manageSQLite.checkTable("signs")) {
             log.info(logPrefix + "Creating table signs");
             String query = "CREATE TABLE signs (id INTEGER PRIMARY KEY AUTOINCREMENT, assigner VARCHAR(32) NOT NULL, x int NOT NULL, y int NOT NULL, z int NOT NULL, world VARCHAR(32) NOT NULL, reward int NOT NULL, state VARCHAR(32) NOT NULL)";
             this.manageSQLite.createTable(query); // Use sqlCore.createTable(query) to create tables 
         }
 
         schedHandler.startCleanupScheduler_SyncRep();
     }
 
     public void setStaticValues()
     {
         currency = this.getConfig().getString("currency");
         completedAssTag = this.getConfig().getString("CompletedAssignmentTag");
     }
 
     private boolean setupEconomy() 
     {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         econ = rsp.getProvider();
         return econ != null;
     }
 
     @Override
     public void onDisable()
     {
         assigner = null;
         assignee = null;
         cHandler = null;
         econ = null;
         pFolder = null;
         manageSQLite = null;
         schedHandler = null;
         comHandler = null;
         log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
     }        
 
     public void cleanupAssignmentsInDB(CommandSender sender)
     {        
         // order is mandatory! First clause prevents exception if sender is null for second statement (will then not be evaluated).
         if((null == sender) ||
                 sender.hasPermission("assignment.*"))
         {          
             int signCount = 0;
             int deletedCount = 0;                        
             String query = "SELECT COUNT(*) as 'count' FROM signs";
             ResultSet resSet = null;
             //World currentWorld = sender.getName()
             try
             {
                 resSet = this.manageSQLite.sqlQuery(query);
                 resSet.next();       //set pointer to first row                     
                 signCount = resSet.getInt(resSet.getRow());
             }
             catch(Exception e)
             {
                 if(null != sender) // is null if called by scheduler
                 {
                     sender.sendMessage(ChatColor.YELLOW + "Die Datenbank scheint leer zu sein.");
                 }
                 else
                 {
                     log.info(logPrefix + "Database seems to be empty.");
                 }
             }
 
             if(signCount > 0)
             {                           
                 query = "SELECT x, y, z, world FROM signs";
                 try
                 {
                     resSet = this.manageSQLite.sqlQuery(query);
                     resSet.next();  //set pointer to first row
 
                     Location loc;
                     boolean isAssSign = false;                                
 
                     loc = new Location(getServer().getWorld(resSet.getString("world")),resSet.getDouble("x"),resSet.getDouble("y"),resSet.getDouble("z"));
                     Block blToDel = loc.getBlock();
 
                     for(int i = 0; i < signCount; i++)
                     {
                         //check if block is a sign, and if not, delete it from db.
                         if((blToDel.getTypeId() == 63) || // is there a sign post or wall sign?
                                 (blToDel.getTypeId() == 68))
                         {              
                             Sign sign = (Sign) blToDel.getState();
 
                             if(sign.getLine(0).contains("<A>") || sign.getLine(0).contains("<a>") || sign.getLine(0).contains("<" + completedAssTag + ">")) // is Assignment sign?
                             {
                                 isAssSign = true;
                             }                                                                            
                         }
 
                         if(!isAssSign)
                         {   
                             query = "DELETE FROM signs WHERE x=" + resSet.getInt("x") + " AND y=" + resSet.getInt("y") + " AND z=" + resSet.getInt("z") + " AND world='" + resSet.getString("world") +"'";
                             this.manageSQLite.deleteQuery(query);                                       
                             deletedCount++;   
                         }
 
                         if(i < (signCount-1))
                         {
                             resSet.next();
                             loc = new Location(getServer().getWorld(resSet.getString("world")),resSet.getDouble("x"),resSet.getDouble("y"),resSet.getDouble("z"));
                             blToDel = loc.getBlock();
                         }     
                     }
                     if(null != sender) // is null if called by scheduler
                     {
                         sender.sendMessage(ChatColor.GREEN + "Es wurden " + deletedCount + " ungueltige Auftraege aus der DB geloescht.");
                     }
                     else // is null if called by scheduler
                     {
                         log.info(logPrefix + deletedCount + " invalid sign entries have been deleted from the database.");
                     }
                 }
                 catch(Exception e)
                 {
                     log.severe(logPrefix + e.getMessage());
                 }
             }
             else
             {
                 sender.sendMessage(ChatColor.GREEN + "Es wurden keine ungueltigen Auftraege gefunden.");
             }
         }
         else
         {
             if(null != sender) // is null if called by scheduler
             {
                 sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to cleanup the database");
             }
         }        
     } //END cleanup
 
     //================================================================================================
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onSignChange(SignChangeEvent event) //For the Assigner. Fires AFTER Sign Creation
     {      
         if(this.getConfig().getBoolean("debug"))
         {
             log.info("im onSignChangeEvent");
             event.getPlayer().sendMessage("Linie 0:" + event.getLine(0)); //debug
         }   
 
         if(event.getLine(0).contains("<A>") || event.getLine(0).contains("<a>")) //Assignment sign?
         {
             if(this.getConfig().getBoolean("debug")){log.info("Assignment-Sign erkannt");}
 
             if(event.getPlayer().hasPermission("assignment.make") || event.getPlayer().hasPermission("assignment.*"))
             {
                 assigner = event.getPlayer(); 
                 String[] lineArray;
                 int itemID = 0;
                 byte subID = -1; //byte in JAVA has -128 to 127 range!
                 int reward = 0;         
                 int amount = 0;           
                 boolean parsingOK = true;
 
                 try //correct format?
                 {
                     if(this.getConfig().getBoolean("debug")){log.info("Ist das Sign korrekt geschrieben?");}
 
                     lineArray = (event.getLine(1).split(":"));   //parse itemID:[subID]:amount
                     reward = Integer.parseInt(event.getLine(2)); //parse reward                
                     if(this.getConfig().getBoolean("debug")){log.info("ArrayLength: " + String.valueOf(lineArray.length));}
 
                     if(lineArray.length == 2) // no subID given
                     {
                         itemID = Byte.parseByte(lineArray[0]); //parse itemID
                         amount = Integer.parseInt(lineArray[1]); //parse amount  
 
                         if(!(itemID > 0 && amount > 0 && reward > 0))
                         {                        
                             parsingOK = false;
                         }
                     }
                     else
                     {
                         itemID = Integer.parseInt(lineArray[0]); //parse itemID
                         subID = Byte.parseByte(lineArray[1]); //parse subID. (in an ItemStack, subID is the "damage" argument and stored as Short)
                         amount = Integer.parseInt(lineArray[2]); //parse amount  
 
                         if(!(itemID > 0 && subID >= 0 && amount > 0 && reward > 0))
                         {                        
                             parsingOK = false;
                         }
                     }                    
                 }
                 catch (Exception e)
                 {                   
                     //not a number. Abort.                
                     event.getBlock().breakNaturally();
                     assigner.sendMessage(ChatColor.YELLOW + "Hilfe: 1. Zeile: <A> 2. Zeile: ItemID:[SubID:]Anzahl 3. Zeile: Belohnung");
                     assigner.sendMessage(ChatColor.YELLOW + "Die ItemID kann mit /iteminfo abgerufen werden, wenn du das gewollte Item in der Hand hast.");
                     parsingOK = false;
                     return; //leave method
                 }//TODO Evt. ItemName auf Schild anstatt der ID ermöglichen (ähnlich ChestShop). Wird aber problematisch wg. Länge...
                 if(parsingOK)
                 {                
                     event.setLine(2,String.valueOf(reward).concat(" " + currency));
 
                     if(econ.has(assigner.getName(), reward))  //assigner has sufficient money to set up this assignment
                     {
                         EconomyResponse ecoRes = econ.withdrawPlayer(assigner.getName(), reward); //deposit reward in database
                         if(ecoRes.transactionSuccess()) //assigner had sufficient money to pay the reward
                         {       
                             if(assigner.getName().length() > 15)
                             {
                                 event.setLine(3, assigner.getName().substring(0, 14)); //trim name to 15 characters (whole name is stored in DB, so it doesn't matter)    
                             }
                             else
                             {
                                 event.setLine(3, assigner.getName()); //trim name to 15 characters (whole name is stored in DB, so it doesn't matter)
                             }
 
                             if(this.getConfig().getBoolean("debug")){log.info("Eco transfer erfolgreich");}
                             //register sign here to protect it and know if its legal or fake
                             String query = "INSERT INTO signs (assigner, x, y, z, world, reward, state) VALUES ('" + assigner.getName() + "', " + (int)event.getBlock().getX() +  ", " + (int)event.getBlock().getY()+ ", " + (int)event.getBlock().getZ() + ", '" + event.getPlayer().getWorld().getName() + "', " + reward + ", '" + assignmentStateActive + "')";                   
                             this.manageSQLite.insertQuery(query);
                             if(this.getConfig().getBoolean("debug")){log.info("SQL Query: " + query);} 
                             assigner.sendMessage(ChatColor.GREEN + "Auftrag erfolgreich erstellt!");
                         }    
                         else
                         {
                             assigner.sendMessage(ChatColor.RED + "Fehler bem uebertragen der Belohnung in die Datenbank. Bitte melde das einem Admin!");
                             log.severe(logPrefix + "Error on depositing the reward into the DB.");
                         }                      
                     }
                     else
                     {
                         event.getBlock().breakNaturally();
                         assigner.sendMessage(ChatColor.YELLOW + "Du hast leider nicht genuegend Geld um dir diesen Auftrag leisten zu koennen.");                        
                     }                              
                 }
                 else
                 {
                     //not a number. Abort.                
                     event.getBlock().breakNaturally();
                     assigner.sendMessage(ChatColor.YELLOW + "Hilfe: 1. Zeile: <A> 2. Zeile: ItemID:[SubID:]Anzahl 3. Zeile: Belohnung");              
                 }
             }
             else
             {
                 event.getBlock().breakNaturally();
                 event.getPlayer().sendMessage(ChatColor.RED + "Du hast keine Berechtigung um Auftraege auszuschreiben.");
             }            
         }                
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent event) //For the Assignee
     {
         Action act;
         Sign sign = null; 
         String[] lineArray;
         int itemID = 0;
         short subID = 0;
         ItemStack iStack;
         int amount = 0;
         int reward = 0;
         act = event.getAction();  
 
         if(this.getConfig().getBoolean("debug")){log.info("Es klickte: " + event.getPlayer().getName());}        
         if(this.getConfig().getBoolean("debug")){log.info("onPlayerInteractEntity");}
 
         if(act == Action.RIGHT_CLICK_BLOCK)
         {
             if(this.getConfig().getBoolean("debug")){log.info("Rechtsklick auf BlockID " + String.valueOf(event.getClickedBlock().getTypeId())  + " erkannt");}
             if(event.getClickedBlock().getTypeId() == 63 ||
                     event.getClickedBlock().getTypeId() == 68) // Right clicked a sign on a sign on block (68) oder a signpost (63)?
             {
                 if(this.getConfig().getBoolean("debug")){log.info("Sign erkannt");}
                 sign = (Sign) event.getClickedBlock().getState();
 
                 if(sign.getLine(0).contains("<A>") || sign.getLine(0).contains("<a>") || sign.getLine(0).contains("<" + completedAssTag + ">")) // is Assignment sign?
                 {
                     // block next occurring BlockPlace()-action to prevent accidental placing of block in hand                                        
                     if(event.getPlayer().getItemInHand().getTypeId() < 256) //item in hand is a placable block and not an item
                     {
                         blockNextBlockPlacing = true;
                     }
 
                     String assName = getAssignerNameFromDB((int)sign.getX(), (int)sign.getY(), (int)sign.getZ(), sign.getWorld().getName());
                     if(this.getConfig().getBoolean("debug")){log.info("assignerName: " + assName.toString() + " Spieler der klickte: " + event.getPlayer().getName());}
                     if("" != assName) //sign is registered assignment sign
                     {      
                         if(this.getConfig().getBoolean("debug")){log.info("Assignment-Sign erkannt");} 
                         try
                         {
                             if(assName.equalsIgnoreCase(event.getPlayer().getName().toString())) //assigner has clicked right on his own sign to pick up its remaining items from a completed assignment
                             {
                                 assigner = getAssignerFromDB((int)sign.getX(), (int)sign.getY(), (int)sign.getZ(), sign.getWorld().getName());
 
                                 if(null != assigner) // clicking player is assigner
                                 {
                                     if(this.getConfig().getBoolean("debug")){log.info("Klickender Spieler ist Assigner (Stringvergleich)");} 
                                     if(sign.getLine(0).contains("<" + completedAssTag + ">"))
                                     {
 
                                         if(this.getConfig().getBoolean("debug")){log.info("Abgeschlossenes AssignmentSign von Assigner gerechtsklickt");}  
 
                                         lineArray = sign.getLine(1).split(":");                                     
                                         if(lineArray.length == 2) //no subID given
                                         {
                                             if(this.getConfig().getBoolean("debug")){log.info("Zeile 1: " + lineArray[0] + " | " + lineArray[1]);}
                                             itemID = Integer.parseInt(lineArray[0]);
                                             amount = Integer.parseInt(lineArray[1]); //amount of Items the assigner can still pick up from its completed assignment
                                             iStack = new ItemStack(Material.getMaterial(itemID), amount);
                                         }
                                         else
                                         {
                                             if(this.getConfig().getBoolean("debug")){log.info("Zeile 1: " + lineArray[0] + " | " + lineArray[1] + " | " + lineArray[2]);}
                                             itemID = Integer.parseInt(lineArray[0]);
                                             subID = Short.parseShort(lineArray[1]);
                                             amount = Integer.parseInt(lineArray[2]); //amount of Items the assigner can still pick up from its completed assignment
                                             iStack = new ItemStack(Material.getMaterial(itemID), amount, subID);                                    
                                         }
 
                                         //try to add the remaining items from the assignment to the assigners inventory                                     
                                         HashMap<Integer, ItemStack> couldnotAddItems = assigner.getInventory().addItem(iStack);
                                         assigner.updateInventory(); //deprecated     
 
                                         if(false == couldnotAddItems.isEmpty()) //not all items fitted in the assigners inventory
                                         {
                                             int notFitting = couldnotAddItems.get(0).getAmount(); 
                                             if(subID > 0) //subID given
                                             {
                                                 String stillAvailable = String.valueOf(itemID) + ":" + String.valueOf(subID) + ":" + String.valueOf(notFitting);
                                                 //könnte man auch zusätzlich per Kommando erlauben. /ass abholen / collect oder so....                                        
                                                 sign.setLine(1, stillAvailable);
                                             }
                                             else 
                                             {
                                                 String stillAvailable = String.valueOf(itemID) + ":" + String.valueOf(notFitting);
                                                 //könnte man auch zusätzlich per Kommando erlauben. /ass abholen / collect oder so....                                        
                                                 sign.setLine(1, stillAvailable);  
                                             }
                                             sign.setLine(0, "<" + completedAssTag + ">");
                                             sign.setLine(2, "rechtsklicken!"); //max. 15 Zeichen!     //TODO aus Lang-file nehmen!!
                                             sign.update();
 
                                             //keep Name of Assigner in Line 4                                           
 
                                             if(amount == notFitting) //Players Inventory is full
                                             {
                                                 assigner.sendMessage(ChatColor.YELLOW + "Dein Inventar ist voll! Lege etwas ab, um weitere Waren deines Auftrags einzusammeln.");
                                             }
                                             else if(subID > 0) //subID given
                                             {                                  
                                                 assigner.sendMessage(ChatColor.GREEN + "Deinem Inventar wurden " + ChatColor.YELLOW + (amount-notFitting) + " " + iStack.getType().getMaterial(itemID).toString() + ":" + String.valueOf(subID) + ChatColor.YELLOW + " hinzugefuegt.");
                                                 assigner.sendMessage(ChatColor.GREEN + "Du kannst weitere " + notFitting + " " + iStack.getType().getMaterial(itemID).toString() + ":" + String.valueOf(subID) + " bei deinem Schild abholen.");
                                                 assigner.sendMessage(ChatColor.GREEN + " (Rechtsklick)");
                                                 assigner.sendMessage(ChatColor.GREEN + "Position des Schilds: X: " + String.valueOf((int)event.getClickedBlock().getX()) + "  Z: " + String.valueOf((int)event.getClickedBlock().getZ()) + " in " + event.getClickedBlock().getWorld().getName());
                                             }
                                             else
                                             {                                  
                                                 assigner.sendMessage(ChatColor.GREEN + "Deinem Inventar wurden " + ChatColor.YELLOW + (amount-notFitting) + " " + iStack.getType().getMaterial(itemID).toString() + ChatColor.GREEN + " hinzugefuegt.");
                                                 assigner.sendMessage(ChatColor.GREEN + "Du kannst weitere " + notFitting + " " + iStack.getType().getMaterial(itemID).toString() + " bei deinem Schild abholen.");
                                                 assigner.sendMessage(ChatColor.GREEN + " (Rechtsklick)");
                                                 assigner.sendMessage(ChatColor.GREEN + "Position des Schilds: X: " + String.valueOf((int)event.getClickedBlock().getX()) + "  Z: " + String.valueOf((int)event.getClickedBlock().getZ()) + " in " + event.getClickedBlock().getWorld().getName());
                                             }
                                         }
                                         else
                                         {
                                             assigner.sendMessage(ChatColor.GREEN + "Deinem Inventar wurden " + ChatColor.YELLOW + amount + " " + iStack.getType().getMaterial(itemID).toString() + ":" + String.valueOf(subID) + ChatColor.GREEN +" hinzugefuegt.");
                                             event.getClickedBlock().setType(Material.AIR); //remove sign
                                             //TODO gatheredAllGoods in Lang file. Muss noch angepasst werden wg. Compound...Variablen + chatfarben... Die oberen auch alle!
                                             deleteSignFromDB(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ(), event.getClickedBlock().getWorld().getName()); //remove DB entry of sign
                                             //Assignment is now fulfilled successfully    
                                         }                                    
                                     }
                                     else // player who clicked is the assigner, but Assignment it's not yet completed. So it's nothing to pick up for him
                                     {
                                         assigner.sendMessage(ChatColor.YELLOW + "Dieser Auftrag wurde noch nicht abgeschlossen. Du kannst ihn abreissen wenn du willst.");
                                     }
                                 }
                                 else
                                 {
                                     event.getPlayer().sendMessage(ChatColor.RED + "Fehler beim lesen des Assigners aus der DB. Bitte melde das einem Admin!");
                                     log.severe(logPrefix +"Error on searching the assigner in DB by name.");
                                 }
                             } 
                             else //clicking Player is an assignee
                             {
                                 if(event.getPlayer().hasPermission("assignment.take") || event.getPlayer().hasPermission("assignment.*"))
                                 {
                                     if(!sign.getLine(0).contains("<" + completedAssTag + ">")) //assignee is about to complete the assignment
                                     {
                                         assignee = event.getPlayer();
 
                                         lineArray = sign.getLine(1).split(":"); //Format: amount:reward + ggf. $-Zeichen das noch weg muss!
 
                                         if(lineArray.length == 2) //no subID given
                                         {
                                             itemID = Integer.parseInt(lineArray[0]);                                        
                                             amount = Integer.parseInt(lineArray[1]);
                                             String temp = sign.getLine(2).replace(currency, "").trim();
                                             reward = Integer.parseInt(temp);
 
                                             iStack = new ItemStack(Material.getMaterial(itemID), amount);
                                             if(this.getConfig().getBoolean("debug")){log.info("Material: " + iStack.getType().toString() + " Anzahl: " + iStack.getAmount());}                                        
                                         }
                                         else
                                         {
                                             itemID = Integer.parseInt(lineArray[0]);
                                             subID = Short.parseShort(lineArray[1]);
                                             amount = Integer.parseInt(lineArray[2]);
                                             String temp = sign.getLine(2).replace(currency, "").trim();
                                             reward = Integer.parseInt(temp);  
 
                                             iStack = new ItemStack(Material.getMaterial(itemID), amount, subID);
                                             if(this.getConfig().getBoolean("debug")){log.info("Material: " + iStack.getType().toString() + " Anzahl: " + iStack.getAmount());}      
                                         }                                   
 
                                         if(this.getConfig().getBoolean("debug")){log.info("ItemID: " + String.valueOf(itemID) + " SubID: " + String.valueOf(subID) + ", Anzahl: " + String.valueOf(amount) + " , Belohnung: " + String.valueOf(reward));}
 
                                         // block next occurring BlockPlace()-action to prevent dupe bug (assignee will otherwise place the block in hand in front of the sign, causing a dupe exploit,
                                         // if it is the same block which is requested by the assignment)
                                         if(event.getPlayer().getItemInHand().getTypeId() == iStack.getTypeId())
                                         {
                                             blockNextBlockPlacing = true;
                                         }
 
                                         // look if assignee has needed amount of items in his inventory. If not: abort
                                         int missingAmount = 0;
                                         assignee.updateInventory(); //deprecated
                                         HashMap<Integer, ItemStack> couldNotRemove = assignee.getInventory().removeItem(iStack); //try to remove needed items
                                         assignee.updateInventory(); //deprecated
 
                                         if(false == couldNotRemove.isEmpty()) //there were some items missing to complete the assignment
                                         {
                                             missingAmount = couldNotRemove.get(0).getAmount(); //how much items were missing?
                                             assignee.getInventory().addItem(new ItemStack(itemID, amount-missingAmount, subID)); //give all already removed items back, because he does not have the required amount
                                             assignee.updateInventory(); //deprecated
                                             assignee.sendMessage(ChatColor.YELLOW + "Du hast nicht die benoetigten " + amount + " " + iStack.getType().getMaterial(itemID).toString() + ":" + String.valueOf(subID) + " um diesen Auftrag zu erfuellen.");
                                         }
                                         else //assignee has enough of the item to complete the assignment
                                         {
                                             if(this.getConfig().getBoolean("debug")){log.info(ChatColor.GREEN + "Du hast die noetigen Waren dabei. Sehr gut!");}                           
                                             EconomyResponse ecoRes = econ.depositPlayer(assignee.getName(), reward); //give assignee his reward
                                             if(ecoRes.transactionSuccess())
                                             {
                                                 String stillAvailable = "";
                                                 int notFittingAmount = 0; //amount of Items that do not fit into the assigners inventory
                                                 //assignee.getInventory().remove(iStack);                                                
                                                 updateSignInDB(sign.getX(), sign.getY(), sign.getZ(), sign.getWorld().getName(), assignmentStateCompleted); // change state so "completed"
                                                 assignee.sendMessage(ChatColor.GREEN + "Danke! Dir wurden " + ChatColor.YELLOW + reward + " " + currency + ChatColor.GREEN + " ausgezahlt fuer deinen erledigten Job.");                                            
                                                 //Assignee has got his reward and does no longer matter!
 
                                                 //-------------------------------------------------------          
 
                                                 // ---------- Handle Assigner payout -------
                                                 if(null != (assigner = getAssignerFromDB((int)sign.getX(), (int)sign.getY(), (int)sign.getZ(), sign.getWorld().getName())))
                                                 {// Assigner is currently online and now gets his items delivered                                                  
                                                     assigner.sendMessage(ChatColor.GREEN + assignee.getName() + " hat einen deiner Auftraege erledigt und dafuer ");
                                                     assigner.sendMessage(ChatColor.YELLOW + String.valueOf(reward) + " " + currency + ChatColor.GREEN +" von dir erhalten.");    
                                                     //try to add the items from the assignment to the assigners inventory                                                        
                                                     assignee.updateInventory(); //deprecated
                                                     HashMap<Integer, ItemStack> couldnotAdd = assigner.getInventory().addItem(iStack);
                                                     assigner.updateInventory(); //deprecated
                                                     if(false == couldnotAdd.isEmpty()) //not all items fitted in the assigners inventory
                                                     {                                                            
                                                         notFittingAmount = couldnotAdd.get(0).getAmount();  
 
                                                         if(subID > 0)
                                                         {
                                                             stillAvailable = String.valueOf(itemID) + ":" + String.valueOf(subID) + ":" + String.valueOf(notFittingAmount);
                                                             sign.setLine(1, stillAvailable);
                                                         }
                                                         else
                                                         {
                                                             stillAvailable = String.valueOf(itemID) + ":" + String.valueOf(notFittingAmount);
                                                             sign.setLine(1, stillAvailable);
                                                         }    
                                                         sign.setLine(0, "<" + completedAssTag + ">");
                                                         sign.setLine(2, "rechtsklicken!"); //max. 15 Zeichen!     
                                                         sign.update();                                                        
                                                         // keep Name of Assigner in Line 4                                           
 
                                                         if(assigner.isOnline())
                                                         {
                                                             if(amount == notFittingAmount) //Players Inventory is full
                                                             {
                                                                 assigner.sendMessage(ChatColor.YELLOW + "Dein Inventar ist voll! Lege etwas ab, um weitere Waren deines Auftrags einzusammeln.");
                                                             }
                                                             else if(subID > 0) //subID given
                                                             {                                  
                                                                 assigner.sendMessage(ChatColor.GREEN + "Deinem Inventar wurden " + ChatColor.YELLOW + (amount-notFittingAmount) + " " + iStack.getType().getMaterial(itemID).toString() + ":" + String.valueOf(subID) + ChatColor.YELLOW + " hinzugefuegt.");
                                                                 assigner.sendMessage(ChatColor.GREEN + "Du kannst weitere " + notFittingAmount + " " + iStack.getType().getMaterial(itemID).toString() + ":" + String.valueOf(subID) + " bei deinem Schild abholen.");
                                                                 assigner.sendMessage(ChatColor.GREEN + " (Rechtsklick)");
                                                                 assigner.sendMessage(ChatColor.GREEN + "Position des Schilds: X: " + String.valueOf((int)event.getClickedBlock().getX()) + "  Z: " + String.valueOf((int)event.getClickedBlock().getZ()) + " in " + event.getClickedBlock().getWorld().getName());
                                                             }
                                                             else
                                                             {                                  
                                                                 assigner.sendMessage(ChatColor.GREEN + "Deinem Inventar wurden " + ChatColor.YELLOW + (amount-notFittingAmount) + " " + iStack.getType().getMaterial(itemID).toString() + ChatColor.GREEN + " hinzugefuegt.");
                                                                 assigner.sendMessage(ChatColor.GREEN + "Du kannst weitere " + notFittingAmount + " " + iStack.getType().getMaterial(itemID).toString() + " bei deinem Schild abholen.");
                                                                 assigner.sendMessage(ChatColor.GREEN + " (Rechtsklick)");
                                                                 assigner.sendMessage(ChatColor.GREEN + "Position des Schilds: X: " + String.valueOf((int)event.getClickedBlock().getX()) + "  Z: " + String.valueOf((int)event.getClickedBlock().getZ()) + " in " + event.getClickedBlock().getWorld().getName());
                                                             }
                                                         }                                                       
                                                     }
                                                     else
                                                     {
                                                         assigner.sendMessage(ChatColor.GREEN + "Deinem Inventar wurden " + ChatColor.YELLOW + amount + " " + iStack.getType().getMaterial(itemID).toString() + ":" + String.valueOf(subID) + ChatColor.GREEN + " hinzugefuegt.");
                                                         event.getClickedBlock().setType(Material.AIR); //remove sign
                                                         deleteSignFromDB(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ(), event.getClickedBlock().getWorld().getName()); //remove DB entry of sign
                                                         //Assignment is now fulfilled successfully    
                                                     }  
                                                 }
                                                 else // Assigner is currently not online
                                                 {                                                    
                                                     // he will be informed about his fulfilled assignments upon login
                                                 }
 
                                                 // Set up sign for showing the completed assignment state
                                                 //TODO könnte man auch zusätzlich per Kommando erlauben. /ass abholen / collect oder so....
                                                 notFittingAmount = iStack.getAmount();
                                                 if(subID > 0)
                                                 {
                                                     stillAvailable = String.valueOf(itemID) + ":" + String.valueOf(subID) + ":" + String.valueOf(notFittingAmount);
                                                 }
                                                 else
                                                 {
                                                     stillAvailable = String.valueOf(itemID) + ":" + String.valueOf(notFittingAmount);
                                                 }                                                 
                                                 sign.setLine(0, "<" + completedAssTag + ">");
                                                 sign.setLine(1, stillAvailable);
                                                 sign.setLine(2, "rechtsklicken!"); //max. 15 Zeichen!     
                                                 sign.update();
                                                 //keep Name of Assigner in Line 4 
                                             }
                                             else
                                             {
                                                 assignee.sendMessage(ChatColor.RED + "Leider gab es einen Fehler beim Ueberweisen des Geldes. Bitte melde das einem Admin!");
                                                 log.severe(logPrefix +"Error on transferring the reward to the assignee.");                                
                                             }
                                         }
                                     }
                                     else // player who clicked is an assignee, but assignment is already completed
                                     {
                                         event.getPlayer().sendMessage(ChatColor.YELLOW + "Dieser Auftrag wurde schon erfuellt.");
                                     }
                                 }
                                 else
                                 {
                                     event.getPlayer().sendMessage(ChatColor.RED + "Du hast nicht die Berechtigung um Auftraege anzunehmen.");    
                                 }                                    
                             }
                         }
                         catch (Exception e)
                         {
                             log.severe(logPrefix + "An error occured while fetching sign position out of DB. ERROR: " + e.toString());                       
                         }  
                     }
                     else
                     {
                         event.getPlayer().sendMessage(ChatColor.YELLOW + "Dieses Schild ist ein Fake und gehoert nicht zu einem registrierten Auftrag!");
                     }
                 }
             }            
         }
         else if(act == Action.LEFT_CLICK_BLOCK)
         {
             if(event.getClickedBlock().getTypeId() == 63 ||
                     event.getClickedBlock().getTypeId() == 68) // Left clicked a sign on a block (68) oder a signpost (63)?
             {
                 sign = (Sign) event.getClickedBlock().getState();                
 
                 if(sign.getLine(0).contains("<A>") || sign.getLine(0).contains("<a>")) // is Assignment sign?
                 {
                     String assignerName = "";
                     assignerName = getAssignerNameFromDB((int)sign.getX(), (int)sign.getY(), (int)sign.getZ(), sign.getWorld().getName());
 
                     if(assignerName != "") // sign is registered assignment sign
                     {
                         try
                         {
                             lineArray = sign.getLine(1).split(":"); //Format: amount:reward + $-Zeichen das noch weg muss!
 
                             if(lineArray.length == 2) //no subID given
                             {
                                 itemID = Integer.parseInt(lineArray[0]);                                        
                                 amount = Integer.parseInt(lineArray[1]);
                                 String temp = sign.getLine(2).replace(currency, "").trim();
                                 reward = Integer.parseInt(temp);  
                             }
                             else
                             {
                                 itemID = Integer.parseInt(lineArray[0]);
                                 subID = Short.parseShort(lineArray[1]);
                                 amount = Integer.parseInt(lineArray[2]);
                                 String temp = sign.getLine(2).replace(currency, "").trim();
                                 reward = Integer.parseInt(temp);  
                             }        
                             event.getPlayer().sendMessage(ChatColor.GREEN + "Der Auftraggeber " + ChatColor.YELLOW + sign.getLine(3) + ChatColor.GREEN + " bezahlt " + String.valueOf(reward) + " " + currency + " fuer " + String.valueOf(amount) + " " + Material.getMaterial(itemID).toString() + ":" + String.valueOf(subID) + ".");
                         }
                         catch(Exception e)
                         {
                             if(this.getConfig().getBoolean("debug")){log.info("Fehler beim parsen des Schilds. " + e.toString());}
                             event.getPlayer().sendMessage(ChatColor.RED + "Dieses Schild ist nicht vollstaendig oder nicht korrekt.");
                         }                    
                     }
                     else //sign is a fake
                     {
                         event.getPlayer().sendMessage(ChatColor.YELLOW + "Dieses Schild ist ein Fake und gehoert nicht zu einem registrierten Auftrag!");
                     }
                 }
 
             }
         }        
     }
 
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false) // -> priority MUST BE HIGHER to protect the sign
     public void onBlockBreak(BlockBreakEvent event) //For the Assignee
     {        
         if(this.getConfig().getBoolean("debug")){log.info("im onBlockBreakEvent");}
         if(event.getBlock().getTypeId() == 63 ||
                 event.getBlock().getTypeId() == 68) // a sign on a sign on block (68) oder a signpost (63)?
         {
             if(this.getConfig().getBoolean("debug")){log.info("Sign erkannt");}
             Sign bSign = (Sign)event.getBlock().getState();
             if(this.getConfig().getBoolean("debug")){log.info("Sign Line 0: " + bSign.getLine(0));}
 
             if(bSign.getLine(0).contains("<A>") || bSign.getLine(0).contains("<a>") || bSign.getLine(0).contains("<" + completedAssTag + ">")) // is Assignment sign?
             {
                 String assignerName = getAssignerNameFromDB((int)event.getBlock().getX(), (int)event.getBlock().getY(), (int)event.getBlock().getZ(), event.getBlock().getWorld().getName());
                 assigner = getAssignerFromDB((int)event.getBlock().getX(), (int)event.getBlock().getY(), (int)event.getBlock().getZ(), event.getBlock().getWorld().getName());
 
                 if("" != assignerName) // sign is registered assignment sign
                 {            
                     if(this.getConfig().getBoolean("debug")){log.info("Assignment Sign erkannt");}
 
                     try
                     {   
                         if(event.getPlayer().getName().equalsIgnoreCase(assignerName) ||
                                 event.getPlayer().hasPermission("assignment.break") ||
                                 event.getPlayer().hasPermission("assignment.*")) //player is the assigner or a player with permission to break the sign
                         {     
                             if(bSign.getLine(0).contains("<" + completedAssTag + ">")) //he cancels an allready fullfilled asignment and loses his not yet picked up items and gains no money back
                             { 
                                 //delete sign from DB
                                 String dquery = "DELETE FROM signs WHERE x=" + (int)bSign.getX() + " AND y=" + (int)event.getBlock().getY() + " AND z=" + (int)bSign.getZ() + " AND world='" + event.getPlayer().getWorld().getName() + "'";
                                 this.manageSQLite.deleteQuery(dquery);
                                 event.getPlayer().sendMessage(ChatColor.YELLOW + "Die restlichen Items sind hiermit verfallen.");
                                 event.getPlayer().sendMessage(ChatColor.YELLOW + "Dieser Auftrag wurde geloescht.");
                                 if(this.getConfig().getBoolean("debug")){log.info("player has permission to break it");}
                             }
                             else // he cancels the unfullfilled assignment, so pay his money back
                             {
                                 try
                                 {
                                     String query = "SELECT reward FROM signs WHERE x=" + (int)bSign.getX() + " AND y=" + (int)event.getBlock().getY() + " AND z=" + (int)bSign.getZ() + " AND world='" + event.getPlayer().getWorld().getName() + "'"; 
                                     ResultSet resSet = this.manageSQLite.sqlQuery(query);
                                     resSet.next(); //set pointer to first row
                                     int payback = resSet.getInt(resSet.getRow());
                                     EconomyResponse ecoRes = econ.depositPlayer(assigner.getName(), payback); //payback reward
                                     if(ecoRes.transactionSuccess()) 
                                     {
                                         if((null != assigner) && (assigner.isOnline()))
                                         {
                                             assigner.sendMessage(ChatColor.YELLOW + "Du hast " + String.valueOf(payback) + " " + currency + " zurueckerstattet bekommen.");    
                                         }                                        
                                         //delete sign from DB                                        
                                         deleteSignFromDB((int)bSign.getX(), (int)bSign.getY(), (int)bSign.getZ(), bSign.getWorld().getName());
                                         event.getPlayer().sendMessage(ChatColor.YELLOW + "Dieser Auftrag wurde hiermit geloescht.");
                                         if(this.getConfig().getBoolean("debug")){log.info("player has permission to break it");}
                                     }
                                 }
                                 catch (Exception e)
                                 {
                                     log.severe(logPrefix + "Error on payback of the assigner. Could not find sign in DB." + e);
                                 }
                             }
                         }
                         else
                         {
                             event.setCancelled(true); //cancel BlockBreak if its a registeres Assignment sign and it's not the assigner or an OP who tries to break it
                             event.getPlayer().sendMessage(ChatColor.RED + "Du hast keine Berechtigung um dieses Auftragsschild abzureissen!");    
                         }  
                     }
                     catch (Exception e)
                     {
                         log.severe(logPrefix + "An error occured while fetching sign out of DB at BlockBreakEvent. ERROR: " + e.toString());                       
                     }
                 }
                 else
                 {
                     event.getPlayer().sendMessage(ChatColor.YELLOW + "Dieses Schild ist ein Fake und gehoert nicht zu einem registrierten Auftrag!");
                 }
             }
         }
     }
 
     // Needed to prevent dupe bug when assignee clicks Assignment sign with the Block that is requested by the assignment in hand.
    // The block would be placed without beeing removed from his inventory properly, causing a dupe. So placing must be blocked in this case!
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false) // -> priority MUST BE HIGHER to protect the sign
     public void onBlockPlace(BlockPlaceEvent event) //For the Assignee
     {
         if(blockNextBlockPlacing)
         {
             event.setCancelled(true); //prevent placing as long as Assignee-Handling is running to prevent duping blocks.
             blockNextBlockPlacing = false; //release blockPlacing lock
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true) // event has MONITOR priority and will be skipped if it has been cancelled before
     public void onPlayerJoin(PlayerJoinEvent event)
     {      
         final PlayerJoinEvent pjEvent = event;        
 
         // Check if player has unfulfilled assignments to pick up
         final int assRdyForPickup = getCompletedAssignmentsFromDB(event.getPlayer().getName());
 
         if(assRdyForPickup > 0)
         {
             if(assRdyForPickup == 1) // there are completed assignments for this player to pick up
             { 
                 // DELAYED TASK (only called once)
                 getServer().getScheduler().scheduleSyncDelayedTask(aInst, new Runnable()
                 {
                     public void run()
                     {
                         pjEvent.getPlayer().sendMessage(ChatColor.GREEN + "Du hast noch einen erfuellten Auftrag. Du kannst am Schild deine Waren abholen.");
                         pjEvent.getPlayer().sendMessage(ChatColor.GREEN + "Mit " + ChatColor.WHITE + "/ass list" + ChatColor.GREEN + " kannst du deine Auftraege ansehen.");
                     }
                 }, 200L); 
 
             }
             else
             {
                 // DELAYED TASK (only called once)
                 getServer().getScheduler().scheduleSyncDelayedTask(aInst, new Runnable()
                 {
                     public void run()
                     {
                         pjEvent.getPlayer().sendMessage(ChatColor.GREEN + "Du hast noch " + Integer.toString(assRdyForPickup) + " erfuellte Auftraege. Bitte hole deine Waren ab.");
                         pjEvent.getPlayer().sendMessage(ChatColor.GREEN + "Mit " + ChatColor.WHITE + "/ass list" + ChatColor.GREEN + " kannst du deine Auftraege ansehen.");
                     }
                 }, 200L);                
             } 
         }
         // --------------------------------------------             
     }
 
     //==============================================================================
     // database queries
     public Player getAssignerFromDB(int x, int y, int z, String world)
     {
         String query = "SELECT assigner FROM signs WHERE x='" + x + "' AND y='" + y + "' AND z='" + z+ "' AND world='" + world + "'";        
 
         try
         {            
             ResultSet queryRes = this.manageSQLite.sqlQuery(query);
             queryRes.next(); //move cursor to first row of result (should only have one though) 
             if(queryRes.getRow() == 1) // query has a result, so it's a registeres sign
             { 
                 String assignerName = queryRes.getString("assigner");
                 assigner = Bukkit.getServer().getPlayer(assignerName); //ACHTUNG: DAS GEHT NUR GUT, WENN DER ASSIGNER ONLINE IST!!!
                 //Wenn er auch nicht online sein kann, dann die Methode "getAssignerNameFromDB" nutzen!
                 if(this.getConfig().getBoolean("debug")){log.info("Schild ist registriertes Assignment Schild");}  
             }
         }
         catch (Exception e)
         {
             assigner = null;            
         }       
         return assigner;
     }
 
     public String getAssignerNameFromDB(int x, int y, int z, String world)
     {
         String assignerName = "";
         String query = "SELECT assigner FROM signs WHERE x=" + x + " AND y=" + y + " AND z=" + z+ " AND world='" + world + "'";        
 
         try
         {            
             ResultSet queryRes = this.manageSQLite.sqlQuery(query);
             queryRes.next(); //move cursor to first row of result (should only have one though) 
             if(queryRes.getRow() == 1) // query has a result, so it's a registeres sign
             { 
                 assignerName = queryRes.getString("assigner");                    
                 if(this.getConfig().getBoolean("debug")){log.info("Schild ist registriertes Assignment Schild");}  
             }
         }
         catch (Exception e)
         {
             assignerName = "";            
         }       
         return assignerName;
     }
 
     public void deleteSignFromDB(int x, int y, int z, String world)
     {
         String query = "DELETE FROM signs WHERE x=" + x + " AND y=" + y + " AND z=" + z + " AND world='" + world + "'";       
 
         try
         {            
             this.manageSQLite.deleteQuery(query);
             if(this.getConfig().getBoolean("debug")){log.info("Schild wurde erfolgreich geloescht.");}  
         }
         catch (Exception e)
         {
             log.severe(logPrefix + "Error on deleting sign from DB. No sign found here.");
         }   
     }
 
     public void updateSignInDB(int x, int y, int z, String world, String state)
     {
         String query = "UPDATE signs SET state='" + state + "' WHERE x=" + x + " AND y=" + y + " AND z=" + z + " AND world='" + world + "'";       
 
         try
         {            
             this.manageSQLite.updateQuery(query);
             if(this.getConfig().getBoolean("debug")){log.info("Schild wurde erfolgreich geloescht.");}  
         }
         catch (Exception e)
         {
             log.severe(logPrefix + "Error on updating sign in DB. No sign found here.");
         }   
     }
 
     public int getCompletedAssignmentsFromDB(String loggedInPlayerName)
     {
         int assignmentsReadyForPickup = 0;
         String query = "SELECT COUNT(*) AS amount FROM signs WHERE assigner='" + loggedInPlayerName + "' AND state='" + assignmentStateCompleted + "'";        
 
         try
         {            
             ResultSet queryRes = this.manageSQLite.sqlQuery(query);
             queryRes.next(); //move cursor to first row of result (should only have one though) 
             if(queryRes.getRow() == 1) // query has a result, so there are completed assignments for the newly logged in player to pick up
             { 
                 assignmentsReadyForPickup = queryRes.getInt("amount");                    
                 if(this.getConfig().getBoolean("debug")){log.info("Neu eingeloggter Spieler hat beendete Aufträge, die er abholen kann");}  
             }
         }
         catch (Exception e)
         {
             log.severe(e.getMessage());
         }       
         return assignmentsReadyForPickup;
     }
 
     public ResultSet getAssignmentList(String queriedPlayerName)
     {        
         String query = "SELECT x, y, z, world, state FROM signs WHERE assigner='" + queriedPlayerName + "'";
 
         try
         {            
             ResultSet queryRes = this.manageSQLite.sqlQuery(query);            
             return (queryRes);
         }
         catch (Exception e)
         {           
             return null;
         }
     }
 }
 
