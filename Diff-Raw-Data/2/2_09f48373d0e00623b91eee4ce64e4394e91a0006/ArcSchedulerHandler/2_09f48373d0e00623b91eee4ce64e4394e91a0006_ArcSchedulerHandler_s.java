 package com.github.CubieX.Arctica;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.entity.Player;
 
 public class ArcSchedulerHandler
 {
     private Arctica plugin = null;
 
     ArrayList<Integer> craftedBlocksIDlist = new ArrayList<Integer>();
     ArrayList<Integer> warmBlocksIDlist = new ArrayList<Integer>();
     ArrayList<String> playersToAffect = new ArrayList<String>();
     static final int playersToHandleEachTick = 5; // set this dependent on the 'checked block count' for each player, to prevent overloading a tick time frame!
     int handledPlayers = 0;
     int cdSchedTaskID = 0; 
     int playersToAffectCount = 0;
     static final int neededCraftBlockRows = 2;  // amount of horizontal rows that must consist of crafted blocks to determine whether the player is inside or outside.
                                                 // 2 means, the scan will look for 2 blocks high walls around the player.
 
     public ArcSchedulerHandler(Arctica plugin)
     {
         this.plugin = plugin;
 
         initCraftedBlocksIDlist();
         initWarmBlocksIDlist();
     }
 
     /* These Blocks will be accepted as suitable for building a safe shelter
      * that grants the "Indoor" bonus against the cold */
     // TODO make configurable via plugin config (sub-tree that is beeing read)
     void initCraftedBlocksIDlist()
     {
         craftedBlocksIDlist.add(1);
         craftedBlocksIDlist.add(4);
         craftedBlocksIDlist.add(5);
         craftedBlocksIDlist.add(7);
         craftedBlocksIDlist.add(20);
         craftedBlocksIDlist.add(22);
         craftedBlocksIDlist.add(24);
         craftedBlocksIDlist.add(35);
         craftedBlocksIDlist.add(41);
         craftedBlocksIDlist.add(42);
         craftedBlocksIDlist.add(43);
         craftedBlocksIDlist.add(45);
         craftedBlocksIDlist.add(48);
         craftedBlocksIDlist.add(49);
         craftedBlocksIDlist.add(57);
         craftedBlocksIDlist.add(64);
         craftedBlocksIDlist.add(71);
         craftedBlocksIDlist.add(82);
         craftedBlocksIDlist.add(87);
         craftedBlocksIDlist.add(88);
         craftedBlocksIDlist.add(89);
         craftedBlocksIDlist.add(97);
         craftedBlocksIDlist.add(98);
         craftedBlocksIDlist.add(102);
         craftedBlocksIDlist.add(110);
         craftedBlocksIDlist.add(112);
         craftedBlocksIDlist.add(123);
         craftedBlocksIDlist.add(124);
         craftedBlocksIDlist.add(125);
         craftedBlocksIDlist.add(133);
     }
 
     /* These Blocks will be accepted as warm blocks that grant a warmth bonus
      * if player is near one of them */    
     void initWarmBlocksIDlist()
     {
         warmBlocksIDlist.add(10);
         warmBlocksIDlist.add(11);
         warmBlocksIDlist.add(51);
     }
 
     public void startColdDamageScheduler_SyncRep()
     {
         // this is a synchronous repeating task
         plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable()
         {
             @Override
             public void run()
             {
                 Player[] onlinePlayerList = plugin.getServer().getOnlinePlayers();
                 playersToAffect.clear();
                 playersToAffectCount = 0;
 
                 //check for all players if they are in cold biome
                 for(int i = 0; i < onlinePlayerList.length; i++)
                 {
                     Player currPlayer = onlinePlayerList[i];
 
                     if((currPlayer.hasPermission("arctica.use")) &&
                             (!currPlayer.hasPermission("arctica.immune")))
                     {
                         //String currPlayersBiomeStr = currPlayer.getWorld().getBiome((int)currPlayer.getLocation().getX(), (int)currPlayer.getLocation().getZ()).toString();
                         Biome currPlayersBiome = currPlayer.getWorld().getBiome((int)currPlayer.getLocation().getX(), (int)currPlayer.getLocation().getZ());
                         //currPlayer.sendMessage(Arctica.logPrefix + ChatColor.AQUA + "Aktuelles Biom: " + currPlayersBiomeStr);   
 
                         if((currPlayersBiome == Biome.FROZEN_OCEAN) ||
                                 (currPlayersBiome == Biome.FROZEN_RIVER) ||
                                 (currPlayersBiome == Biome.ICE_MOUNTAINS) ||
                                 (currPlayersBiome == Biome.ICE_PLAINS) ||
                                 (currPlayersBiome == Biome.TAIGA) ||
                                 (currPlayersBiome == Biome.TAIGA_HILLS))                               
                         {    
                             playersToAffect.add(currPlayer.getName());
                             playersToAffectCount++;
                         }
                     }
                 }
 
                 if(!playersToAffect.isEmpty())
                 {
                     // calculate how many ticks will be needed to handle all players that should be affected
                     int amountOfTicksNeededToHandleAllAffectedPlayers = (int)(Math.ceil((double)playersToAffectCount / (double)playersToHandleEachTick));
                     handledPlayers = 0;
 
                     for(int i = 0; i < amountOfTicksNeededToHandleAllAffectedPlayers; i++)
                     {
                         task_applyColdDamage(); // handles the given amount of players each tick. Must be called multiple times until all
                         // players have been handled
                     }
                 }
             }
         }, (20*5L), 20*Arctica.damageApplyPeriod); // 5 sec delay, configurable period in seconds
     }
 
     void task_applyColdDamage() // this will be run as many ticks as needed to handle all players.
     {   
         // this is a synchronous single task. It will be run once on the next tick.
         plugin.getServer().getScheduler().runTask(plugin, new Runnable()
         {
             @Override
             public void run() //current scan duration: approx. 2 ms/Player (limited to 5x this time per tick!)
             {
                 Player currPlayer = null;
                 boolean currPlayerIsOutside = false;
                 boolean currPlayerIsNearFire = false;
                 boolean currPlayerIsInWater = false;
                 boolean currPlayerIsHoldingTorch = false;
                 int realDamageToApply = 0;
                 int handledPlayersThisTick = 0;
 
                 double baseDamageInWaterToApply = 0.0;
                 double extraDamageInWaterWhenOutsideToApply = 0.0;
                 double warmthBonusFactorToApply = 0.0;
                 double torchBonusFactorToApply = 0.0;
                 double baseDamageInAirToApply = 0.0;
                 double extraDamageInAirWhenOutsideToApply = 0.0;
 
                 try
                 {
                     if(handledPlayers < playersToAffectCount)
                     {
                         for (int currPlayerIndex = handledPlayers; currPlayerIndex < playersToAffect.size(); currPlayerIndex++) // go through Hashmap
                         {
                             if(null != playersToAffect.get(currPlayerIndex)) // Hashmap may contain NULL values! So assure it's a real player.
                             {
                                 if((handledPlayersThisTick < playersToAffectCount) &&
                                         (handledPlayersThisTick < playersToHandleEachTick))
                                 {
                                     currPlayer = plugin.getServer().getPlayerExact(playersToAffect.get(currPlayerIndex));
 
                                     currPlayerIsOutside = checkIfOutside(currPlayer);
                                     currPlayerIsNearFire = checkIfNearFire(currPlayer);
                                     currPlayerIsInWater = checkIfInWater(currPlayer);
                                     currPlayerIsHoldingTorch = plugin.playerIsHoldingTorch(currPlayer.getName());
 
                                     if (currPlayerIsInWater)
                                     {
                                         if(currPlayerIsOutside)
                                         { // player is outside and in water
                                             if(currPlayerIsNearFire)
                                             {
                                                 if(Arctica.debug) currPlayer.sendMessage(ChatColor.AQUA + "Du bist in kaltem Wasser.");
                                                 baseDamageInWaterToApply = Arctica.baseDamageInWater;
                                                 extraDamageInWaterWhenOutsideToApply = Arctica.extraDamageInWaterWhenOutside;
                                                 warmthBonusFactorToApply = Arctica.warmthBonusFactor;
                                             }
                                             else
                                             { // player is outside and in ice water
                                                 if(Arctica.debug) currPlayer.sendMessage(ChatColor.AQUA + "Du bist in Eiswasser!");
                                                 baseDamageInWaterToApply = Arctica.baseDamageInWater;
                                                 extraDamageInWaterWhenOutsideToApply = Arctica.extraDamageInWaterWhenOutside;
                                             }                                            
                                         }
                                         else                                    
                                         { // player is inside and in water.
                                             if(currPlayerIsNearFire)
                                             {
                                                 baseDamageInWaterToApply = Arctica.baseDamageInWater;
                                                 warmthBonusFactorToApply = Arctica.warmthBonusFactor;
                                             }
                                             else
                                             {
                                                 baseDamageInWaterToApply = Arctica.baseDamageInWater;                                                   
                                             }
                                         }
                                     }
                                     else // player is in air
                                     {
                                         /*
                                                      [syntax = java]
                                                         EntityPig piggy= new EntityPig (mcWorld);
 
                                                     mcWorld.addEntity(piggy, SpawnReason.CUSTOM);
                                                     [/syntax]
 
                                                     Where mcWorld is the craftbukkit world.getHandle().
                                          * */
 
                                         /*Entity chick = null;
                                                     CraftPlayer craftPlayer = (CraftPlayer)currPlayer;
                                                     CraftWorld craftWorld = (CraftWorld)craftPlayer.getWorld();
                                                     net.minecraft.server.World mworld = (net.minecraft.server.World)(craftWorld.getHandle());
                                                     chick = new Entity(mworld);*/
 
                                         if(currPlayerIsHoldingTorch)
                                         {
                                             torchBonusFactorToApply = Arctica.torchBonusFactor;
                                         }
 
                                         if(currPlayerIsNearFire)
                                         {
                                             warmthBonusFactorToApply = Arctica.warmthBonusFactor;
 
                                             if(currPlayerIsOutside)
                                             {
                                                 baseDamageInAirToApply = Arctica.baseDamageInAir;
                                                 extraDamageInAirWhenOutsideToApply = Arctica.extraDamageInAirWhenOutside;                                                
                                             }
                                             else
                                             { // player is inside near a fire. No Damage.                                                
                                                 // No Damage.
                                             }
                                         }
                                         else
                                         { // player is in air, but not near fire
                                             baseDamageInAirToApply = Arctica.baseDamageInAir;
 
                                             if(currPlayerIsOutside)
                                             { // player is outside in air, not near fire                                                
                                                 extraDamageInAirWhenOutsideToApply = Arctica.extraDamageInAirWhenOutside;                                                
                                             }
                                             else
                                             { // player is inside, not near fire.                                                
                                                 // No extra damage. Only base Damage.
                                             }
                                         }
 
                                         //currPlayer.damage(realDamageToApply, (org.bukkit.entity.Entity) chick);
                                     }
 
                                    // Combined caculation. Some values will be 0, depending on above evaluation
                                     realDamageToApply = (int)Math.ceil(((
                                             baseDamageInAirToApply +
                                             extraDamageInAirWhenOutsideToApply +
                                             baseDamageInWaterToApply +
                                             extraDamageInWaterWhenOutsideToApply) *
                                             (1.0 - warmthBonusFactorToApply) *
                                             (1.0 - torchBonusFactorToApply) *                                            
                                             (1.0 - plugin.getDamageReduceFactorFromCloth(currPlayer))));
 
                                     // fire custom damage event ================================================                                
                                     ColdDamageEvent cdEvent = new ColdDamageEvent(currPlayer, realDamageToApply); // Create the event                                
                                     plugin.getServer().getPluginManager().callEvent(cdEvent); // fire Event         
                                     //==========================================================================
                                     if((0 < realDamageToApply) && Arctica.debug) currPlayer.sendMessage(ChatColor.AQUA + "" + realDamageToApply + " Kaelteschaden erhalten.");                                
                                     handledPlayersThisTick++; // a player was handled. Increase counter for current tick.
                                     handledPlayers++; // a player was handled. Increase counter for playersToAffect list.   
                                 }
                                 else
                                 {
                                     // limit of players for this tick is reached. Leave loop.
                                     break;
                                 } // end if/else Handled players count check
                             } // end if NULL check
                         } // end if FOR loop   
                     }// end if handledPlayers < playersToAffectCount
                 }
                 catch(Exception ex)
                 {
                     Arctica.log.info(Arctica.logPrefix + ex.getMessage());
                     // player probably no longer online or no longer meeting the necessary requirements for beeing affected
                 } // end TRY/CATCH
 
             } // end RUN()
         }); // end scheduler call
     }
 
     public void setTaskID(int id)
     {
         this.cdSchedTaskID = id;
     }
 
     boolean checkIfOutside(Player player)
     { 
         boolean playerIsOutside = true;
         boolean craftedBlockTOP = false;
         boolean craftedBlockNORTHdiagonal = false;
         boolean craftedBlockEASTdiagonal = false;
         boolean craftedBlockSOUTHdiagonal = false;
         boolean craftedBlockWESTdiagonal = false;
         boolean craftedBlockNORTH = false;
         boolean craftedBlockEAST = false;
         boolean craftedBlockSOUTH = false;
         boolean craftedBlockWEST = false;
 
         Location playerLoc = player.getLocation();
 
         // Check TOP ========================================================
         // Check if a Block straight to the TOP of the player is a crafted block
         craftedBlockTOP = TOPhasCraftedBlock(playerLoc);
 
         // Check NORTH 45 degrees ================================================================
         // Check if a Block to the NORTH in 45 degrees to the top of the player is a crafted block
         craftedBlockNORTHdiagonal = NORTHdiagnoalHasCraftedBlock(playerLoc);
         // Check EAST 45 degrees ================================================================
         // Check if a Block to the EAST in 45 degrees to the top of the player is a crafted block
         craftedBlockEASTdiagonal = EASTdiagnoalHasCraftedBlock(playerLoc);
         // Check SOUTH 45 degrees ================================================================
         // Check if a Block to the SOUTH in 45 degrees to the top of the player is a crafted block
         craftedBlockSOUTHdiagonal = SOUTHdiagnoalHasCraftedBlock(playerLoc);
         // Check WEST 45 degrees ================================================================
         // Check if a Block to the WEST in 45 degrees to the top of the player is a crafted block
         craftedBlockWESTdiagonal = WESTdiagnoalHasCraftedBlock(playerLoc);
 
         // Check NORTH ========================================================================================
         // Check if a Block to the NORTH of the player is a crafted block (block on foot and head level needed)
         craftedBlockNORTH = NORTHhasCraftedBlock(playerLoc);
         // Check EAST =========================================================================================
         // Check if a Block to the EAST of the player is a crafted block (block on foot and head level needed)
         craftedBlockEAST = EASThasCraftedBlock(playerLoc);
         // Check SOUTH ========================================================================================
         // Check if a Block to the SOUTH of the player is a crafted block (block on foot and head level needed)
         craftedBlockSOUTH = SOUTHhasCraftedBlock(playerLoc);
         // Check WEST =========================================================================================
         // Check if a Block to the WEST of the player is a crafted block (block on foot and head level needed)
         craftedBlockWEST = WESThasCraftedBlock(playerLoc);
 
         // Gather all results and combine them
         if(craftedBlockTOP &&
                 craftedBlockNORTHdiagonal &&
                 craftedBlockEASTdiagonal &&
                 craftedBlockSOUTHdiagonal &&
                 craftedBlockWESTdiagonal &&
                 craftedBlockNORTH &&
                 craftedBlockEAST &&
                 craftedBlockSOUTH &&
                 craftedBlockWEST) // player is surrounded (as far as evaluated) with valid crafted blocks
         {
             playerIsOutside = false;
             if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Du bist im Innenbereich.");
         }
         else
         {
             playerIsOutside = true;
             //if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Craftbloecke: T: " + craftedBlockTOP + " |N: " + craftedBlockNORTH + " |E: " + craftedBlockEAST + " |S: " + craftedBlockSOUTH + " |W: " + craftedBlockWEST);
             if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Du bist im Freien.");
         }
 
         return(playerIsOutside);
     }
 
     boolean TOPhasCraftedBlock(Location startLocation)
     {
         boolean res = false;
 
         // Check TOP =========================================================
         // Check if there is a block above the players position which is a valid
         // crafted block for a shelter to gain the "indoor warmth bonus"
 
         // Important for C-Programmers: Objects in JAVA are NOT copied by using '='. It just sets another object reference!
         // Objects can only be copied by instantiating a new object and then copying ALL attributes from the other object
         Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
 
         int checkLimit = (int)checkedLoc.getY() + Arctica.checkRadius;
         checkedLoc.setY(checkedLoc.getY() + 2); // set height to block above players head        
         if(checkLimit > Arctica.maxMapHeight)
         {
             checkLimit = Arctica.maxMapHeight;
         }
 
         for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY < checkLimit; checkedLocY++, checkedLoc.setY(checkedLoc.getY() + 1)) //safer than "while"
         {   
             if (!checkedLoc.getBlock().isEmpty())
             {
                 if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                 {
                     //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "TOP Block gefunden: " + checkedLoc.getBlock().getType().toString());
                     res = true;
                     break;
                 }
             }
         }
 
         return (res);
     }
 
     boolean NORTHhasCraftedBlock(Location startLocation)
     {
         boolean res = false;
         int validCraftBlockRows = 0; // amount of horizontal rows that have been successfully checked for crafted blocks.
 
         // Check NORTH =========================================================
         // Check if there is a block to the NORTH of the players position which is a valid
         // crafted block for a shelter to gain the "indoor warmth bonus"
 
         Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
         int checkLimitY = (int)checkedLoc.getY() + 1; // check should be done for foot and head level of player
         int checkLimit = (int)checkedLoc.getZ() - Arctica.checkRadius;
         checkedLoc.setZ(checkedLoc.getZ() - 1); // set start next to the player                
 
         for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY()+1))
         {
             for(int checkedLocZ = (int)checkedLoc.getZ(); checkedLocZ > checkLimit; checkedLocZ--, checkedLoc.setZ(checkedLoc.getZ() - 1)) //safer than "while"
             {   
                 //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "NORTH checkedLocZ: " + checkedLocZ + " <> checkLimit: " + checkLimit);
                 if (!checkedLoc.getBlock().isEmpty())
                 {                
                     if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                     {
                         //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "NORTH Block gefunden: " + checkedLoc.getBlock().getType().toString());                        
                         validCraftBlockRows++;
                         break;
                     }
                 }
             }
         }
 
         if(validCraftBlockRows >= neededCraftBlockRows) // all height levels have valid craft blocks
         {
             res = true;
         }
 
         return (res);
     }
 
     boolean EASThasCraftedBlock(Location startLocation)
     {
         boolean res = false;
         int validCraftBlockRows = 0; // amount of horizontal rows that have been successfully checked for crafted blocks.
 
         // Check EAST =========================================================
         // Check if there is a block to the EAST of the players position which is a valid
         // crafted block for a shelter to gain the "indoor warmth bonus"
 
         Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
         int checkLimitY = (int)checkedLoc.getY() + 1; // check should be done for foot and head level of player
         int checkLimit = (int)checkedLoc.getX() + Arctica.checkRadius;
         checkedLoc.setX(checkedLoc.getX() + 1); // set start next to the player                
 
         for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY()+1))
         {
             for(int checkedLocX = (int)checkedLoc.getX(); checkedLocX < checkLimit; checkedLocX++, checkedLoc.setX(checkedLoc.getX() + 1)) //safer than "while"
             {   
                 if (!checkedLoc.getBlock().isEmpty())
                 {
                     if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                     {
                         //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "EAST Block gefunden: " + checkedLoc.getBlock().getType().toString());
                         validCraftBlockRows++;
                         break;
                     }
                 }
             }
         }
 
         if(validCraftBlockRows >= neededCraftBlockRows) // all height levels have valid craft blocks
         {
             res = true;
         }
 
         return (res);
     }
 
     boolean SOUTHhasCraftedBlock(Location startLocation)
     {
         boolean res = false;
         int validCraftBlockRows = 0; // amount of horizontal rows that have been successfully checked for crafted blocks.
 
         // Check SOUTH =========================================================
         // Check if there is a block to the SOUTH of the players position which is a valid
         // crafted block for a shelter to gain the "indoor warmth bonus"
 
         Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
         int checkLimitY = (int)checkedLoc.getY() + 1; // check should be done for foot and head level of player
         int checkLimit = (int)checkedLoc.getZ() + Arctica.checkRadius;        
         checkedLoc.setZ(checkedLoc.getZ() + 1); // set start next to the player       
 
         for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY()+1))
         {
             for(int checkedLocZ = (int)checkedLoc.getZ(); checkedLocZ < checkLimit; checkedLocZ++, checkedLoc.setZ(checkedLoc.getZ() + 1)) //safer than "while"
             {   
                 if (!checkedLoc.getBlock().isEmpty())
                 {
                     if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                     {
                         //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "SOUTH Block gefunden: " + checkedLoc.getBlock().getType().toString());
                         validCraftBlockRows++;
                         break;
                     }
                 }
             }            
         }
 
         if(validCraftBlockRows >= neededCraftBlockRows) // all height levels have valid craft blocks
         {
             res = true;
         }
 
         return (res);
     }
 
     boolean WESThasCraftedBlock(Location startLocation)
     {
         boolean res = false;
         int validCraftBlockRows = 0; // amount of horizontal rows that have been successfully checked for crafted blocks.
 
         // Check WEST =========================================================
         // Check if there is a block to the WEST of the players position which is a valid
         // crafted block for a shelter to gain the "indoor warmth bonus"
 
         Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
         int checkLimitY = (int)checkedLoc.getY() + 1; // check should be done for foot and head level of player
         int checkLimit = (int)checkedLoc.getX() - Arctica.checkRadius;        
         checkedLoc.setX(checkedLoc.getX() - 1); // set start next to the player        
 
         for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY()+1))
         {
             for(int checkedLocX = (int)checkedLoc.getX(); checkedLocX > checkLimit; checkedLocX--, checkedLoc.setX(checkedLoc.getX() - 1)) //safer than "while"
             {   
                 if (!checkedLoc.getBlock().isEmpty())
                 {
                     if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                     {
                         //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "WEST Block gefunden: " + checkedLoc.getBlock().getType().toString());
                         validCraftBlockRows++;
                         break;
                     }
                 }
             }
         }
 
         if(validCraftBlockRows >= neededCraftBlockRows) // all height levels have valid craft blocks
         {
             res = true;
         }
 
         return (res);
     }
 
     boolean NORTHdiagnoalHasCraftedBlock(Location startLocation)
     {
         boolean res = false;
 
         // Check NORTH 45 degrees =========================================================
         // Check if there is a valid crafted ceiling block in 45 degrees upwards to the player within given distance
 
         Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
         checkedLoc.setY(checkedLoc.getY() + 2); // set height to block above players head
         int checkLimitY = (int)checkedLoc.getY() + Arctica.checkRadius;
 
         if(checkLimitY > Arctica.maxMapHeight)
         {
             checkLimitY = Arctica.maxMapHeight;
         }
 
         for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY() + 1), checkedLoc.setZ(checkedLoc.getZ() - 1)) // go one block up and to the north
         {          
             //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
             if (!checkedLoc.getBlock().isEmpty())
             {
                 if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                 {
                     //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "NORTH_TOP45 Block gefunden: " + checkedLoc.getBlock().getType().toString());
                     res = true;
                     break;
                 }
             }
         }
 
         return (res);
     }
 
     boolean EASTdiagnoalHasCraftedBlock(Location startLocation)
     {
         boolean res = false;
 
         // Check EAST 45 degrees =========================================================
         // Check if there is a valid crafted ceiling block in 45 degrees upwards to the player within given distance
 
         Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
         checkedLoc.setY(checkedLoc.getY() + 2); // set height to block above players head
         int checkLimitY = (int)checkedLoc.getY() + Arctica.checkRadius;
 
         for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY() + 1), checkedLoc.setX(checkedLoc.getX() + 1)) // go one block up and to the east
         {   
             //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
             if (!checkedLoc.getBlock().isEmpty())
             {
                 if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                 {
                     //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "EAST_TOP45 Block gefunden: " + checkedLoc.getBlock().getType().toString());
                     res = true;
                     break;
                 }
             }
         }
         return (res);
     }
 
     boolean SOUTHdiagnoalHasCraftedBlock(Location startLocation)
     {
         boolean res = false;
 
         // Check SOUTH 45 degrees =========================================================
         // Check if there is a valid crafted ceiling block in 45 degrees upwards to the player within given distance
 
         Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
         checkedLoc.setY(checkedLoc.getY() + 2); // set height to block above players head
         int checkLimitY = (int)checkedLoc.getY() + Arctica.checkRadius;
 
         for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY() + 1), checkedLoc.setZ(checkedLoc.getZ() + 1)) // go one block up and SOUTH
         {   
             //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
             if (!checkedLoc.getBlock().isEmpty())
             {
                 if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                 {
                     //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "SOUTH_TOP45 Block gefunden: " + checkedLoc.getBlock().getType().toString());
                     res = true;
                     break;
                 }
             }
         }
         return (res);
     }
 
     boolean WESTdiagnoalHasCraftedBlock(Location startLocation)
     {
         boolean res = false;
 
         // Check WEST 45 degrees =========================================================
         // Check if there is a valid crafted ceiling block in 45 degrees upwards to the player within given distance
 
         Location checkedLoc = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
         checkedLoc.setY(checkedLoc.getY() + 2); // set height to block above players head
         int checkLimitY = (int)checkedLoc.getY() + Arctica.checkRadius;
 
         for(int checkedLocY = (int)checkedLoc.getY(); checkedLocY <= checkLimitY; checkedLocY++, checkedLoc.setY(checkedLoc.getY() + 1), checkedLoc.setX(checkedLoc.getX() - 1)) // go one block up and WEST
         {   
             //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "Gecheckt: " + checkedLoc.getBlock().getX() +  " " + checkedLoc.getBlock().getY() + " " + checkedLoc.getBlock().getZ());
             if (!checkedLoc.getBlock().isEmpty())
             {
                 if(craftedBlocksIDlist.contains(checkedLoc.getBlock().getTypeId())) // its a valid crafted block
                 {
                     //if(Arctica.debug) plugin.getServer().broadcastMessage(ChatColor.AQUA + "WEST_TOP45 Block gefunden: " + checkedLoc.getBlock().getType().toString());
                     res = true;
                     break;
                 }
             }
         }
         return (res);
     }
 
     boolean checkIfNearFire(Player player)
     { 
         boolean playerIsNearFire = false;
 
         Location playerLoc = player.getLocation();
         //Location checkedWarmBlock = player.getLocation();
 
         //int distanceToNearestWarmBlock = Arctica.warmBlockSearchRadius + 1;
 
         int x1 = playerLoc.getBlockX() - Arctica.horizontalWarmBlockSearchRadius; // first corner of cube to check
         int y1 = playerLoc.getBlockY() - Arctica.verticalWarmBlockSearchRadius;
         int z1 = playerLoc.getBlockZ() - Arctica.horizontalWarmBlockSearchRadius;
         World world = player.getWorld();
 
         int x2 = x1 + (2 * Arctica.horizontalWarmBlockSearchRadius); //second corner of cube to check
         int y2 = y1 + (2 * Arctica.verticalWarmBlockSearchRadius);
         int z2 = z1 + (2 * Arctica.horizontalWarmBlockSearchRadius);
 
         //if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "SpielerPos: " + playerLoc.getBlockX() + ", " + playerLoc.getBlockY() + ", " + playerLoc.getBlockZ());
         //if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Ecke 1: " + x1 + ", " + y1 + ", " + z1);
         //if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Ecke 2: " + x2 + ", " + y2 + ", " + z2);
 
         //int checkCount = 0;
         //int foundCount = 0;
 
         for (int checkedX = x1; checkedX < x2; checkedX++)
         {
             for (int checkedY = y1; checkedY < y2; checkedY++)
             {
                 for (int checkedZ = z1; checkedZ < z2; checkedZ++)
                 {
                     //checkCount++;
                     if(warmBlocksIDlist.contains(world.getBlockTypeIdAt(checkedX, checkedY, checkedZ)))
                     {       
                         //foundCount++;
                         /*checkedWarmBlock.setWorld(world); // Exact Distance gets not evaluated until now.
                         checkedWarmBlock.setX(checkedX);
                         checkedWarmBlock.setY(checkedY);
                         checkedWarmBlock.setZ(checkedZ);
 
                         int distanceToCheckedWarmBlock = (int)checkedWarmBlock.distance(playerLoc);
                         if(distanceToCheckedWarmBlock < distanceToNearestWarmBlock)
                         {
                             distanceToNearestWarmBlock = distanceToCheckedWarmBlock;
                         }*/                        
                         playerIsNearFire = true;
                     }
                 }
             }
         }
         if(Arctica.debug && playerIsNearFire) player.sendMessage(ChatColor.AQUA + "Waermequelle gefunden.");
         //if(Arctica.debug) player.sendMessage(ChatColor.AQUA + "Blocks gecheckt: " + checkCount + " | Waermeqellen gefunden: " + foundCount);
         return(playerIsNearFire);
     }
 
     boolean checkIfInWater(Player player)
     {
         boolean playerIsInWater = false;
 
         Material mat = player.getLocation().getBlock().getType();
         if (mat == Material.STATIONARY_WATER || mat == Material.WATER)
         {
             playerIsInWater = true;
         }
 
         return (playerIsInWater);
     }
 }
