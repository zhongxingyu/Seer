 package com.egodroid.bukkit.carmod.listeners;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.vehicle.VehicleCreateEvent;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.event.vehicle.VehicleEnterEvent;
 import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
 import org.bukkit.event.vehicle.VehicleUpdateEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Step;
 import org.bukkit.material.Wool;
 import org.bukkit.util.Vector;
 
 import com.egodroid.bukkit.carmod.CarMod;
 import com.egodroid.bukkit.carmod.util.FuelManager;
 
 
 public class minecartListener implements Listener {
 
 	private HashMap<String,Integer> counter;
 	//private int mMultiplier = 2;
 	private int mStreetSpeedF = 2;
 	private int mMotorWaySpeedF = 5;
 	private boolean useWool = true;
 	private boolean shouldDestroy = true;
 	private boolean useOwnership = true;
 	private HashMap<String, Integer> mPlayerMap;
 	private HashMap<String, Float> mPlayerYawMap;
 	public HashMap<String,UUID> owners; //For Owner System
 	public ArrayList<UUID> mineCars;
 	private FuelManager mFuelM;
 	private boolean moved = false;
 	
 	//Blocks from Config
 	private List<String> mMotorwayBlock;
 	private List<String> mStreetBlock;
 	private List<String> mStreetWoolColor;
 	private List<String> mMWWoolColor;
 	//End
 	
 	private CarMod mPlugin;
 
 	
 	public HashMap<String,Boolean> canMove;
 	private HashMap<String, Boolean> wasClimbing;
 	
 	//private Location oldlightLoc;
 	//private byte oldlightLevel;
 	//private boolean lightswitch = true;
 
 
 
 public minecartListener(CarMod plugin, FuelManager pFM) {
 	this.mFuelM = pFM;
 	this.mPlugin = plugin;
 	this.mPlayerMap = new HashMap<String, Integer>();
 	this.mineCars = new ArrayList<UUID>();
 	this.owners = new HashMap<String, UUID>();
 	this.wasClimbing = new HashMap<String, Boolean>();
 	this.mPlayerYawMap = new HashMap<String, Float>();
 	this.counter = new HashMap<String, Integer>();
 	this.canMove = new HashMap<String,Boolean>();
     this.setupConfig();
 	
     for(Player p: mPlugin.getServer().getOnlinePlayers()){
     	try {
 			boolean move = mFuelM.canMove(p);
 			canMove.put(p.getName(), move);
 		} catch (SQLException e) {
 
 		}
     }
 	
 }	
 
 public void setupConfig() {
 	
 	this.useWool = this.mPlugin.getConfig().getBoolean("useWool");
 	this.shouldDestroy = this.mPlugin.getConfig().getBoolean("destroyCar");
 	this.useOwnership = this.mPlugin.getConfig().getBoolean("UseOwnership");
 	
 	mStreetWoolColor = mPlugin.getConfig().getStringList("WoolColorStreet");
 	mMWWoolColor = mPlugin.getConfig().getStringList("WoolColorMotorway");
 	mStreetBlock = mPlugin.getConfig().getStringList("streetBlock");
 	mMotorwayBlock = mPlugin.getConfig().getStringList("motorwayBlock");
 	
 	for(Player p: mPlugin.getServer().getOnlinePlayers()){
 		if (!this.wasClimbing.containsKey(p.getName())) {
 			this.wasClimbing.put(p.getName(), false);
 		}
 		if (!this.counter.containsKey(p.getName())) {
 			this.counter.put(p.getName(), 0);
 		}
 	}
 }
 
 	
 @EventHandler
 public void onPlayerLogin(PlayerLoginEvent event) {
 	
 	Player p = event.getPlayer();
 	boolean move;
 	
 	if (this.mPlayerMap.containsKey(event.getPlayer().getName())) {
 		
 	} else {
 		this.mPlayerMap.put(event.getPlayer().getName(), 2);
 	}
 	if (this.wasClimbing.containsKey(event.getPlayer().getName())) {
 		
 	} else {
 		this.wasClimbing.put(event.getPlayer().getName(), false);
 	}
 	if (this.counter.containsKey(event.getPlayer().getName())) {
 		
 	} else {
 		this.counter.put(event.getPlayer().getName(), 0);
 	}
 	//Updates players ability to move onPlayerJoin, this is a fail safe.
 	try {
 		move = this.mFuelM.canMove(p);
 		this.canMove.put(p.getName(), move);
 	} catch (SQLException e) {
 		
 		e.printStackTrace();
 	}
 }
 
 
 
 @EventHandler
 public void onVehicleUpdate(VehicleUpdateEvent event) throws SQLException {
 
     Vehicle vehicle = event.getVehicle();
     Block underblock = vehicle.getLocation().getBlock().getRelative(BlockFace.DOWN);
     Block underunderblock = underblock.getRelative(BlockFace.DOWN);
     Block normalblock = vehicle.getLocation().getBlock();
   //  Block vorderblock = vehicle.getLocation().getBlock().getRelative(arg0)
     Entity passenger = vehicle.getPassenger();
     if (!(passenger instanceof Player)) {
       return;
     }
     
     //Check for if this is a MineCar, should help reduce RAM usage in accordance with other Plugins using Minecarts.
     if(vehicle instanceof Minecart){
     	if(!mineCars.contains(vehicle.getUniqueId())){
     	    
     		return;
     	}
     }else{
     	
     	return;
     }
 
     Player player = (Player)passenger;
 
     	if (vehicle instanceof Minecart) {
     		Minecart Auto = (Minecart) vehicle ;
     		//Vector plPos = player.getLocation().getDirection();
     		if(!this.mPlayerMap.containsKey(player.getName())){
         		this.mPlayerMap.put(player.getName(), 3);	
     		}
         	int drivingspeednormal = this.mPlayerMap.get(player.getName());
 
     		Location newLoc = Auto.getLocation();
     		Vector plvelocity = Auto.getPassenger().getVelocity();
     		//plvelocity.multiply(this.mMultiplier);	
     		
     		if (this.mPlugin.getConfig().getBoolean("UseExhaust")) {
     			player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 0);
     			player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 0);
     		}
     		
     		if (player.isInsideVehicle() && CarMod.permission.has(player, "minecars.move")) {
     		    float dir = (float)player.getLocation().getYaw();
     		    BlockFace face = getClosestFace(dir);
     		    Block stepblock = vehicle.getLocation().getBlock().getRelative(face );
     			
     		    //Hit a Wall
     		    if (normalblock.getTypeId() != 0&& normalblock.getRelative(BlockFace.UP).getTypeId() != 0) {
 					Location temploc = vehicle.getLocation();
 					//Bukkit.broadcastMessage("Hit a Wall");
 					vehicle.eject();
 					vehicle.remove();
 					vehicle.getWorld().dropItem(temploc, new ItemStack(328, 1));
 					return;
     		    }
     		    
     		    
     			//Uphill Algorithms
     		    
     			if (normalblock.getTypeId() == 0) {
     				if (stepblock.getTypeId() == 44){
     					Step step = new Step(stepblock.getType(), stepblock.getData());
     					if (step.getData() == (byte) this.mPlugin.getConfig().getInt("StreetStepType")) {
     						if(this.canMove.get(player.getName()) && player.getVelocity().getX() != 0 && player.getVelocity().getY() != 0) {
     							wasClimbing.put(player.getName(), true);
     							//Bukkit.broadcastMessage("half");
     							Location newLoc2 = stepblock.getLocation();
     							newLoc2.add(0, 1.5d, 0);
     							Auto.teleport(newLoc2);
     							return;
     						}	
     					}
     				}
     				if(stepblock.getTypeId() == 0 && wasClimbing.get(player.getName())){
     					if(drivableBlock(stepblock.getRelative(BlockFace.DOWN))&&stepblock.getRelative(face).getTypeId()==0){
     						//Bukkit.broadcastMessage("set false");
 							wasClimbing.put(player.getName(), false);
 							Location newLoc2 = stepblock.getLocation();
 							newLoc2.add(0, 1.5d, 0);
 							Auto.teleport(newLoc2);
     						this.movingCar(Auto, drivingspeednormal, player, plvelocity, false);
     						
     						return;
     					}
     				}
     			}
     			
     			if (normalblock.getTypeId() == 43) {
     				if (stepblock.getTypeId() == 44){
     					Step step = new Step(stepblock.getType(), stepblock.getData());
     					if (step.getData() == (byte) this.mPlugin.getConfig().getInt("StreetStepType")) {
     						if(this.canMove.get(player.getName()) && player.getVelocity().getX() != 0 && player.getVelocity().getY() != 0) {
     							//Bukkit.broadcastMessage("full");
     							Location newLoc2 = stepblock.getLocation();
     							newLoc2.add(0, 1.5d, 0);
     							Auto.teleport(newLoc2);
     							return;
     						}	
     					}
     				}
     			}
     			
     			//Downhill Algorithms
  /*   			if (normalblock.getTypeId() == 0) {
     				if (stepblock.getTypeId() == 0&&stepblock.getRelative(BlockFace.DOWN).getTypeId() == 44&&stepblock.getRelative(BlockFace.DOWN).getRelative(face).getTypeId() == 0){
     					//Bukkit.broadcastMessage("downhill reached");
     					Step step = new Step(stepblock.getRelative(BlockFace.DOWN).getType(), stepblock.getData());
     					if (step.getData() == (byte) this.mPlugin.getConfig().getInt("StreetStepType")) {
     						if(this.canMove.get(player.getName()) && player.getVelocity().getX() != 0 && player.getVelocity().getY() != 0) {
     							wasClimbing.put(player.getName(), true);
     							Bukkit.broadcastMessage("half down");
     							Location newLoc2 = stepblock.getRelative(BlockFace.DOWN).getLocation();
     							newLoc2.add(0, 1.5d, 0);
     							Auto.teleport(newLoc2);
     							return;
     						}	
     					}
     				}
     				if(stepblock.getTypeId() == 0 && wasClimbing.get(player.getName())){
     					if(drivableBlock(stepblock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN))&&stepblock.getRelative(face).getTypeId()==0){
     						//Bukkit.broadcastMessage("climbed down");
 							Location newLoc2 = stepblock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getLocation();
 							newLoc2.add(0, 1.5d, 0);
 							Auto.teleport(newLoc2);
     						this.movingCar(Auto, drivingspeednormal, player, plvelocity, false);
     						
     						return;
     					}
     				}
     			}
     			*/
     		    
     			if(normalblock.getTypeId() != 0 && normalblock.getTypeId() != 27 && normalblock.getTypeId() != 28 && normalblock.getTypeId() != 66 && normalblock.getTypeId() != this.mPlugin.getConfig().getInt("RailingBlock") && player.getVelocity().getX() !=0 && player.getVelocity().getZ() !=0) {
     				if(normalblock.getTypeId() == 43 || normalblock.getTypeId() == 44) {	
     					Step step = new Step(normalblock.getType(), normalblock.getData());
     					if (step.getData() == (byte) this.mPlugin.getConfig().getInt("StreetStepType")) {
     						
     					}else{ 
     						this.destroyCar(player, Auto, underblock); 
     						//Bukkit.broadcastMessage("Not proper step, destroying Car");
     						
     						return; 
     					}
     				} else {
     					this.destroyCar(player, Auto, underblock);
     					//Bukkit.broadcastMessage("Not proper ramp block, destroying Car "+normalblock.getTypeId());
     					
     					return;
     				}
     			}
     			
     		/*	if(this.lightswitch) {
     				World world =  normalblock.getWorld();
     				
     				if (player.getVelocity().getX() == 0 && player.getVelocity().getZ() == 0) {
     					((CraftWorld)world).getHandle().a(EnumSkyBlock.BLOCK, (int) Auto.getLocation().getX(), (int) Auto.getLocation().getY() + 2, (int) Auto.getLocation().getZ(), 15);
     					this.oldlightLoc = Auto.getLocation();
     				} else {
     					((CraftWorld)world).getHandle().a(EnumSkyBlock.BLOCK, (int) this.oldlightLoc.getX(), (int) this.oldlightLoc.getY() + 2, (int) this.oldlightLoc.getZ(), 0);
     					((CraftWorld)world).getHandle().a(EnumSkyBlock.BLOCK, (int) Auto.getLocation().getX(), (int) Auto.getLocation().getY() + 2, (int) Auto.getLocation().getZ(), 15);
     					this.oldlightLoc = Auto.getLocation();
     					
     				}
     				
     				Block block = Auto.getLocation().getBlock();
     				block.setData(block.getData());
     				block.setType(block.getType());
     				
     				
     			} */
     			if (player.getVelocity().getX() == 0 && player.getVelocity().getZ() == 0) {
     				Auto.setDerailedVelocityMod(new Vector(0,0,0));
     			}
     			  		    
     			if (this.canMove.get(player.getName()) && player.getVelocity().getX() !=0 && player.getVelocity().getZ() !=0 && normalblock.getTypeId() == 0) {
     			
     				if(underblock.getTypeId()==0){
     					if(drivableBlock(underunderblock)){
     						this.movingCar(Auto, drivingspeednormal, player, plvelocity, false);
     						
     						return;
     					}
     					if(underunderblock.getTypeId()==0){
     						//Block ground = findGround();
     						//Location newLoc2 = ground.getLocation();
 							//newLoc2.add(0, 1.0d, 0);
 							//Auto.teleport(newLoc2);
     						this.movingCar(Auto, drivingspeednormal, player, plvelocity, false);
     						
     						return;
     					}
     				}
     				
     				if(this.useWool == true) {			
     						
     					if(underblock.getType() == Material.WOOL) {
     						
     						Wool wolle = new Wool(underblock.getType(), underblock.getData());
 
     						for(String s: mStreetWoolColor){
     							if (wolle.getColor().toString().equalsIgnoreCase(s)) {
         							this.movingCar(Auto, drivingspeednormal, player, plvelocity, false);
         							
         							return;
     							}
     						}
     						
     						for(String s: mMWWoolColor){
     							if (wolle.getColor().toString().equalsIgnoreCase(s)) {
         							this.movingCar(Auto, drivingspeednormal, player, plvelocity, true);
         							
         							return;
     							}
     						}
     					}
     				}else{
     					for(String m: mStreetBlock){
     						if (underblock.getType().equals(Material.getMaterial(m))) {
         	    				this.movingCar(Auto, drivingspeednormal, player, plvelocity, false);
         	    				
         	    				return;
     						}
     					}
     					
     					for(String m: mMotorwayBlock){
     						if (underblock.getType().equals(Material.getMaterial(m))) {
         	    				this.movingCar(Auto, drivingspeednormal, player, plvelocity, true);
         	    				
         	    				return;
     						}
     					}	    		
     				
     				}					
     				
     			if (!destroyCar(player, Auto, normalblock)) {
     				
     				int tmp = this.counter.get(player.getName());
     				if (tmp == 40) {
     					this.counter.put(player.getName(),0);
     					this.mFuelM.hasMoved(player);
     					try {
     						boolean move = this.mFuelM.canMove(player);
     						this.canMove.put(player.getName(), move);
     					}catch (SQLException e) {			
     								e.printStackTrace();
     					}
     				}	
     					
     					plvelocity.multiply(15d);
 	
     					newLoc.add(new Vector(plvelocity.getX() ,0.0D, plvelocity.getZ()));
     					this.moved = true; //HIER
     				    Auto.teleport(newLoc);
     	    	    	//Auto.setVelocity(new Vector(plvelocity.getX(),0.0D, plvelocity.getZ()));
     				    
     					//Location tempLocYaw = Auto.getLocation();
     					//tempLocYaw.setYaw(this.mPlayerYawMap.get(player.getName()));
     					//Auto.teleport(tempLocYaw);
     			}
     		}
     	}		
    	}
 }
 
 	@EventHandler
 	public void onVehicleCreate(VehicleCreateEvent event) {
 		if (event.getVehicle() instanceof Minecart) {
 
 			//Minecart cart = (Minecart) event.getVehicle();
 		
 
 		}
 	}
 
 @EventHandler
 public void onVehicleDestroy(VehicleDestroyEvent event) {
     Entity breaker = event.getAttacker();
     Vehicle v = event.getVehicle();
     Player p;
     
     if (!(breaker instanceof Player)) {
     	  
         return;
       }
     
     if (!(v instanceof Minecart)) {
     	  
         return;
       }
     
     p = (Player)breaker;
    
     if(!mineCars.contains(v.getUniqueId())){
     	
     	return;
     }
     
     if(useOwnership){ 
         if(!owners.get(p.getName()).equals(v.getUniqueId())){
         	p.sendMessage(ChatColor.DARK_GREEN+"[MineCars]"+ChatColor.WHITE+" This is not your MineCar!");
         	
         	event.setCancelled(true);
         	return;
         }
     }
     
 }
 
 @EventHandler
 public void onVehicleEnter(VehicleEnterEvent event) {
 	
     Entity passenger = event.getEntered();
     Vehicle v = event.getVehicle();
     Player p;
     
     if (!(passenger instanceof Player)) {
     	  
         return;
       }
     
     if (!(v instanceof Minecart)) {
 
         return;
       }
     
     p = (Player)passenger;
    
     if(!mineCars.contains(v.getUniqueId())){
 
     	return;
     }
 
     
     if(useOwnership){ 
     	if(!owners.containsKey(p.getName())){
         	p.sendMessage(ChatColor.DARK_GREEN+"[MineCars]"+ChatColor.WHITE+" This is not your MineCar!");
         	
         	event.setCancelled(true);
         	return;
     	}
     	
         if(!owners.get(p.getName()).equals(v.getUniqueId())){
         	p.sendMessage(ChatColor.DARK_GREEN+"[MineCars]"+ChatColor.WHITE+" This is not your MineCar!");
         	
         	event.setCancelled(true);
         	return;
         }
     }
 	
     this.mPlayerYawMap.put(((Player) event.getEntered()).getName(), new Float( event.getVehicle().getLocation().getYaw()));
 	Location locyaw = event.getVehicle().getLocation();
 	locyaw.setYaw(event.getVehicle().getLocation().getYaw());
 	v.teleport(locyaw);
 } 
 
 public void onEntityCollision(VehicleEntityCollisionEvent e){
 	
     Vehicle v = e.getVehicle();
     //Player p;
     //Bukkit.broadcastMessage("Bump");
     if (!(v instanceof Minecart)) {
 
         return;
       }
     
     if(!mineCars.contains(v.getUniqueId())){
 
     	return;
     }
     
     if(v.isEmpty()){
     	e.setCancelled(true);
     	
     	return;
     }
     
     //More to come
 }
 
 public void setSpeedMultiplier(int pMultiplier, Player pPlayer) {
 	String tempString = pPlayer.getName();
 	this.mPlayerMap.remove(tempString);
 	this.mPlayerMap.put(tempString, pMultiplier);
 
 	
 }
 
 public int getSpeedMultiplier(Player pPlayer) {
 	String tempString = pPlayer.getName();
 	
 	if (mPlayerMap.containsKey(tempString)){
 		return this.mPlayerMap.get(tempString);
 	}
 	return 0;
 	
 }
 
 
 public void setSpeedFactors(int pStreetF, int pMotorwayF) {
 	this.mStreetSpeedF = pStreetF;
 	this.mMotorWaySpeedF = pMotorwayF;
 	
 }
 
 private boolean destroyCar(Player pPlayer, Minecart pVehicle, Block underblock) {
 	
 	if (underblock.getRelative(BlockFace.DOWN).getTypeId() == 43 || underblock.getRelative(BlockFace.DOWN).getTypeId() == 44) {
 		Step step = new Step(underblock.getRelative(BlockFace.DOWN).getType(), underblock.getRelative(BlockFace.DOWN).getData());
 			if (step.getData() == (byte) this.mPlugin.getConfig().getInt("StreetStepType")) {
 				return false;
 			} else {
 				
 				if (shouldDestroy && this.moved ) {
 					Location temploc = pVehicle.getLocation();
 					pVehicle.eject();
 					pVehicle.remove();
 					pVehicle.getWorld().dropItem(temploc, new ItemStack(328, 1));
 					return true;
 				}
 				
 			}
 	
 	} else {
 		if (this.mPlugin.getConfig().getBoolean("destroyCar") && this.moved ) {
 			Location temploc = pVehicle.getLocation();
 			pVehicle.eject();
 			pVehicle.remove();
 			pVehicle.getWorld().dropItem(temploc, new ItemStack(328, 1));
 			return true;
 		}
 		return false;
 	}
 	return false;
 	
 
 	
 
 }
 
 public BlockFace getClosestFace(float direction){
 
     direction = direction % 360;
 
     if(direction < 0)
         direction += 360;
 
     direction = Math.round(direction / 45);
 
     switch((int)direction){
 
         case 0:
             return BlockFace.WEST;
         case 1:
             return BlockFace.NORTH_WEST;
         case 2:
             return BlockFace.NORTH;
         case 3:
             return BlockFace.NORTH_EAST;
         case 4:
             return BlockFace.EAST;
         case 5:
             return BlockFace.SOUTH_EAST;
         case 6:
             return BlockFace.SOUTH;
         case 7:
             return BlockFace.SOUTH_WEST;
         default:
             return BlockFace.WEST;
 
     }
 }
 
 public boolean drivableBlock(Block b){
 	
 	if(this.useWool == true) {			
 		
 		if(b.getType() == Material.WOOL) {
 			
 			Wool wolle = new Wool(b.getType(), b.getData());
 
 			for(String s: mStreetWoolColor){
 				if (wolle.getColor().toString().equalsIgnoreCase(s)) {
 					
 					return true;
 				}
 			}
 			
 			for(String s: mMWWoolColor){
 				if (wolle.getColor().toString().equalsIgnoreCase(s)) {
 					
 					return true;
 				}
 			}
 		}
 	}else{
 		for(String m: mStreetBlock){
 			if (b.getType().equals(Material.getMaterial(m))) {
 				
 				return true;
 			}
 		}
 		
 		for(String m: mMotorwayBlock){
 			if (b.getType().equals(Material.getMaterial(m))) {
 				
 				return true;
 			}
 		}	    		
 	}
 	
 	if (b.getTypeId() == 43 || b.getTypeId() == 44) {
 		Step step = new Step(b.getType(), b.getData());
 		if (step.getData() == (byte) this.mPlugin.getConfig().getInt("StreetStepType")) {
 			return true;
 		}
 	}
 	
 	return false;
 }
 
 public boolean isRightStep(Block pTestBlock) {
 	if (pTestBlock.getTypeId() == 43 || pTestBlock.getTypeId() == 44) {
 		Step step = new Step(pTestBlock.getType(), pTestBlock.getData());
 		if (step.getData() == (byte) this.mPlugin.getConfig().getInt("StreetStepType")) {
 			return true;
 		}
 	}
 	return false;
 }
 
 private void movingCar(Minecart Auto, int pGear, Player player, Vector plvelocity, boolean motorway) {
 	Location newLoc = Auto.getLocation();
 	int tmp = this.counter.get(player.getName());
 	if (tmp == 40) {
 		this.counter.put(player.getName(),0);
 		this.mFuelM.hasMoved(player);
 		try {
 			boolean move = this.mFuelM.canMove(player);
 			this.canMove.put(player.getName(), move);
 		} catch (SQLException e) {
 			
 			e.printStackTrace();
 		}
	}else{
		counter.put(player.getName(), tmp++);
 	}
 	if (motorway == false) 
 		plvelocity.multiply(this.mStreetSpeedF);
 	else
 		plvelocity.multiply(this.mMotorWaySpeedF);
 	plvelocity = this.checkRailing(Auto, plvelocity);
 	newLoc.add(new Vector(plvelocity.getX() * pGear,0.0D, plvelocity.getZ() * pGear ));
 	this.moved = true; //HIER
     Auto.teleport(newLoc);
   // 	plvelocity.multiply(pGear);
 	//Auto.setVelocity(plvelocity);
 	
 	//Auto.setMaxSpeed(40D);
 //	Auto.setFlyingVelocityMod(new Vector (1,0,0));
 	//Auto.setDerailedVelocityMod(new Vector (1,0,0));
 
 	//Auto.setVelocity(new Vector (40D,0,0));
 	
 }
 
 private Vector checkRailing(Minecart pAuto, Vector pPlayerVel) {
 	if (this.mPlugin.getConfig().getInt("RailingBlock") != 0) { 
 		int railingblock = this.mPlugin.getConfig().getInt("RailingBlock");
 		Block AutoBlock = pAuto.getLocation().getBlock();
 		//boolean zisbigger = Math.abs(pPlayerVel.getZ()) > Math.abs(pPlayerVel.getX());
 		if (AutoBlock.getRelative(BlockFace.NORTH).getTypeId() == railingblock) {
 			pPlayerVel.setX(0.2d);
 		}
 		
 		if (AutoBlock.getRelative(BlockFace.SOUTH).getTypeId() == railingblock) {
 			pPlayerVel.setX(-0.2d);
 		}
 		
 		if (AutoBlock.getRelative(BlockFace.WEST).getTypeId() == railingblock) {
 			pPlayerVel.setZ(-0.2d);
 		}
 		
 		if (AutoBlock.getRelative(BlockFace.EAST).getTypeId() == railingblock) {
 			pPlayerVel.setZ(0.2d);
 		}
 	} 
 	return pPlayerVel;
 }
 
 }
 
 
 
