 package com.msingleton.templecraft;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Slime;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.material.Door;
 import org.bukkit.util.config.Configuration;
 
 import com.iConomy.iConomy;
 import com.iConomy.system.Holdings;
 
 
 public class Temple {
     protected World world;
 	protected Configuration config = null;
     
     // Convenience variables.
     protected String templeName     = null;
     protected Location templeLoc    = null;
     protected Location lobbyLoc     = null;
     protected Location spectatorLoc = null;
     protected boolean isRunning     = false;
     protected boolean isSetup       = false;
     protected boolean isEnabled     = true;
     protected boolean usingClasses  = false;
     protected int specialModulo, minLevel, templeWidth, templeHeight, templeDepth, roomWidth, roomHeight, roomLength;
     protected int TempleRooms, BossRooms, SpawnRooms, ItemRooms, Rooms; 
     protected String owners               = "";
     protected String editors              = "";
     
     // Location variables for the Temple region.
     protected Location p1 = null;
     protected Location p2 = null;
     protected Location startLoc = null;
     
     // Spawn locations list and monster distribution fields.
     protected int dZombies, dSkeletons, dSpiders, dCreepers, dWolves;
     protected int dPoweredCreepers, dPigZombies, dSlimes, dMonsters,
                          dAngryWolves, dGiants, dGhasts;
     
     // Sets and Maps for storing players and their locations.
     protected Set<String> ownerSet    = new HashSet<String>();
     protected Set<String> accessorSet = new HashSet<String>();
     protected Set<Player> playerSet   = new HashSet<Player>();
     protected Set<Player> editorSet   = new HashSet<Player>();
     protected Set<Player> readySet    = new HashSet<Player>();
     protected Set<Player> deadSet     = new HashSet<Player>();
     //protected Map<Player,String> rewardMap     = new HashMap<Player,String>();
     
     // Maps for rewards.
     protected Map<Integer,String> rewardEveryWaveMap = new HashMap<Integer,String>();
     protected Map<Integer,String> rewardAfterWaveMap = new HashMap<Integer,String>();
     
     // Maps for rewards during play.
     protected Map<Integer,String> itemEveryWaveMap = new HashMap<Integer,String>();
     protected Map<Integer,String> itemAfterWaveMap = new HashMap<Integer,String>();
     
     // Special Traits
     //protected Map<String,String> specialTraitsMap = new HashMap<String,String>();
     
     // Contains Mob Spawnpoint Locations
     protected Set<Location> mobSpawnpointSet = new HashSet<Location>();
     // Contains Active Mob Spawnpoints and Creature Types
     protected Map<Location,CreatureType> mobSpawnpointMap = new HashMap<Location,CreatureType>();
     
     // Map for Death Message
     protected Map<Integer, Entity> lastDamager = new HashMap<Integer, Entity>();
     
     // Entities, blocks and items on TempleCraft floor.
     protected Map<Integer,Integer> mobGoldMap  = new HashMap<Integer,Integer>();
     protected Set<LivingEntity> monsterSet     = new HashSet<LivingEntity>();
     protected Set<Block> tempBlockSet          = new HashSet<Block>();
     
     protected Map<Location,Integer> checkpointMap = new HashMap<Location,Integer>();
     protected Map<Location,String[]> chatMap      = new HashMap<Location,String[]>();
     protected Set<Block> coordBlockSet         = new HashSet<Block>();
     protected Set<Block> endBlockSet           = new HashSet<Block>();
     protected Set<Block> lobbyBlockSet         = new HashSet<Block>();
     protected static int mobSpawner = 7;
     protected static int diamondBlock = 57;
     protected static int ironBlock = 42;
     protected static int goldBlock = 41;
     protected static int[] coordBlocks = {mobSpawner, diamondBlock, ironBlock, goldBlock, 63, 68};
     
     protected static int rejoinCost;
     
 	protected Temple(){
 	}
 
 	protected Temple(String name){		
 		config     = TCUtils.getConfig("temples");
 		templeName = name;
 		world      = TempleManager.world;
 		minLevel   = 0;
 		rejoinCost = TempleManager.rejoinCost;
 		isRunning  = false;
 		owners     = TCUtils.getString(config,"Temples."+name+".owners", "");
 		editors    = TCUtils.getString(config,"Temples."+name+".editors", "");
 		loadEditors();
 		TempleManager.templeSet.add(this);
 	}
 
 	/* ///////////////////////////////////////////////////////////////////// //
 	
     LOAD/SAVE METHODS
 
 	// ///////////////////////////////////////////////////////////////////// */
 	
 	protected void saveTemple(World w, Player p){
 		if(p1 == null || p2 == null){
 			TempleManager.tellPlayer(p, "Region not found.");
 			return;
 		}
 		
 	    int x1 = (int)p1.getX();
 	    int z1 = (int)p1.getZ();
 	    int x2 = (int)p2.getX();
 	    int z2 = (int)p2.getZ();
 		
 	    TempleManager.tellPlayer(p, "Saving...");
 	    TempleManager.tellPlayer(p, "From ("+x1+",0,"+z1+") to ("+x2+",0,"+z2+")");
 	    
 		TCRestore.saveTemple(new Location(w, x1, 0, z1), new Location(w, x2, 128, z2), this);
 		TempleManager.tellPlayer(p, "Temple Saved");
 	}
 	
 	protected void loadTemple(World w){
 		if(w.equals(world)){
 			clearTemple();
 			if(startLoc == null)
 				startLoc = getFreeLocation();
 			TCRestore.loadTemple(startLoc, this);
 		} else {
 			TCRestore.loadTemple(new Location(w,0,0,0), this);
 		}
 	}
 
 	protected void repairTemple(){
 		clearFoundation(startLoc);
 		//clearEntities(startLoc);
 		TCRestore.loadTemple(startLoc, this);
 	}
 
 	protected void clearTemple() {
 		coordBlockSet.clear();
 		lobbyBlockSet.clear();
 		endBlockSet.clear();
 		startLoc = null;
 		p1 = null;
 		p2 = null;
 	}
 
 	protected void clearFoundation(Location startLoc) {		
 		// Regenerate Chunks where temple will be
 		
 		World world = startLoc.getWorld();
 		
 		int x1 = startLoc.getBlockX() + p1.getBlockX();
 		int x2 = startLoc.getBlockX() + p2.getBlockX();
 		int y1 = 0;
 		int y2 = 128;
 		int z1 = startLoc.getBlockZ() + p1.getBlockZ();
 		int z2 = startLoc.getBlockZ() + p2.getBlockZ();
 		int level = 0;
 		
 		System.out.println("Clearing Foundation from ("+x1+","+z1+") to ("+x2+","+z2+")");
 		
 		for (int j = y1; j <= y2; j++){
 			for (int i = x1; i <= x2; i++){
 		    	for (int k = z1; k <= z2; k++){
 		    		if(TempleManager.landLevels[level] < j)
 	            		level++;
 	            	Block b = world.getBlockAt(i,j,k);
 	            	int id = TempleManager.landMats[level];
 		    		if(b.getTypeId() != id)
 	           			b.setTypeId(id);
 		    	}
 			}
 		}
 	}
 
 	private Location getFreeLocation() {
 		int MaxX = 0;
 		for(Temple temple : TempleManager.templeSet){
 			if(temple.p2 != null && !temple.equals(this) && MaxX < temple.p2.getBlockX()+2)
 				MaxX = temple.p2.getBlockX()+2;
 			
 		}
 		Location loc = new Location(world, MaxX, 0, 0);
 		return loc;
 	}
 	
 	private void loadEditors() {
 		for(String s : owners.split(",")){
 			s = s.trim();
 			ownerSet.add(s);
 		}
 		
 		for(String s : editors.split(",")){
 			s = s.trim();
 			accessorSet.add(s);
 		}
 	}
 	
 	protected boolean addOwner(String playerName) {
 		if(ownerSet.contains(playerName))
 			return false;
 		else
 			ownerSet.add(playerName);
 		updateEditors();
 		return true;
 	}
 	
 	protected boolean addEditor(String playerName) {
 		if(accessorSet.contains(playerName))
 			return false;
 		else
 			accessorSet.add(playerName);
 		updateEditors();
 		return true;
 	}
 	
 	protected boolean removeEditor(String playerName) {
 		boolean result;
 		result = (ownerSet.remove(playerName) || accessorSet.remove(playerName));
 		updateEditors();
 		return result;
 	}
 	
 	private void updateEditors(){
 		StringBuilder owners = new StringBuilder();
     	for(String s : ownerSet)
 			if(owners.length() == 0)
 				owners.append(s);
 			else
 				owners.append(","+s);
     	
     	StringBuilder editors = new StringBuilder();
     	for(String s : accessorSet)
 			if(editors.length() == 0)
 				editors.append(s);
 			else
 				editors.append(","+s);
     	
     	this.owners = owners.toString();
     	this.editors = editors.toString();
     	
     	saveConfig();
 	}
 
 	protected void saveConfig(){
 		Configuration c = config;
     	c.load();
     	System.out.println(editors);
     	c.setProperty("Temples."+templeName+".owners", owners);    	
     	c.setProperty("Temples."+templeName+".editors", editors);
     	c.save();
 	}
 	
 	/* ///////////////////////////////////////////////////////////////////// //
     
     Temple METHODS
 
 	// ///////////////////////////////////////////////////////////////////// */
 	
 	/**
 	* Starts the current TempleCraft session.
 	*/
 	protected void startTemple()
 	{		
 		isRunning = true;
 		
 		convertSpawnpoints();
 		readySet.clear();
 		for (Player p : playerSet)
 		{
 		    p.teleport(templeLoc);
 		    //rewardMap.put(p,"");
 		}
 		
 		tellAll("Let the games begin!");
 	}
 	
 	private void convertLobby(){
 		for(Block b: getBlockSet(Material.WALL_SIGN.getId())){     
 	        // Cast the block to a sign to get the text on it.
 	        Sign sign = (Sign) b.getState();
 	        handleSign(sign);
 		}
 		for(Block b: getBlockSet(Material.SIGN_POST.getId())){     
 	        // Cast the block to a sign to get the text on it.
 	        Sign sign = (Sign) b.getState();
 	        handleSign(sign);
 		}
 		
 		for(Block b: getBlockSet(goldBlock)){
     		Block rb = b.getRelative(0, -1, 0);
     		if(rb.getTypeId() == ironBlock){
     			lobbyBlockSet.add(rb);
     			b.setTypeId(0);
     			rb.setTypeId(ironBlock);
     		}
 		}
 	}
 	
 	private void handleSign(Sign sign) {
 		String[] Lines = sign.getLines();
 		Block b = sign.getBlock();
 		
 		
 		if(!Lines[0].equals("[TempleCraft]") && !Lines[0].equals("[TC]")){
 			if(Lines[0].equals("[TempleCraftM]") || Lines[0].equals("[TCM]")){
 				String[] newLines = {Lines[1]+Lines[2],Lines[3]};
 				chatMap.put(b.getLocation(), newLines);
 				b.setTypeId(0);
 			}
 			return;
 		}
 			
 		
 		if(Lines[1].toLowerCase().equals("lobby")){
 			lobbyLoc = b.getLocation();
 			b.setTypeId(0);
 		} else if(Lines[1].toLowerCase().equals("checkpoint")){
 			int range;
 			try{
 				range = Integer.parseInt(Lines[3]);
 			}catch(Exception e){
 				try{
 					range = Integer.parseInt(Lines[2]);
 				}catch(Exception e2){
 					range = 5;
 				}
 			}
 			checkpointMap.put(b.getLocation(), range);
 			b.setTypeId(0);
 		} else if(Lines[1].toLowerCase().equals("classes")){
 			if(MobArenaClasses.enabled){
 				MobArenaClasses.generateClassSigns(sign);
 				usingClasses = true;
 			} else
 				sign.getBlock().setTypeId(0);
 		} else {
 			String s = Lines[1]+Lines[2]+Lines[3];
 			for(String mob : TempleManager.mobs)
 				if(s.toLowerCase().contains(mob.toLowerCase())){
 					mobSpawnpointSet.add(b.getLocation());
 		    		mobSpawnpointMap.put(b.getLocation(),CreatureType.fromName(mob));
 		    		b.setTypeId(0);
 				}
 					
 		}
 	}
 	
 	private void convertSpawnpoints() {
 		for(Block b: getBlockSet(Material.WALL_SIGN.getId())){     
 	        // Cast the block to a sign to get the text on it.
 	        Sign sign = (Sign) b.getState();
 	        handleSign(sign);
 		}
 		for(Block b: getBlockSet(Material.SIGN_POST.getId())){     
 	        // Cast the block to a sign to get the text on it.
 	        Sign sign = (Sign) b.getState();
 	        handleSign(sign);
 		}
 		for(Block b: getBlockSet(mobSpawner)){
 			mobSpawnpointSet.add(b.getLocation());
     		mobSpawnpointMap.put(b.getLocation(),getRandomCreature());
     		b.setTypeId(0);
 		}
 	    for(Block b: getBlockSet(diamondBlock)){
     		Block rb = b.getRelative(0, -1, 0);
     		if(rb.getTypeId() == ironBlock){
     			templeLoc = rb.getLocation();
     			b.setTypeId(0);
     			rb.setTypeId(0);
     		} else if(rb.getTypeId() == goldBlock){
     			endBlockSet.add(rb);
     			b.setTypeId(0);
     			rb.setTypeId(goldBlock);
     		}
 		}
 	}
 
 	private Set<Block> getBlockSet(int id){
 	    Set<Block> result = new HashSet<Block>();
 	    
 	    if(!coordBlockSet.isEmpty())
 	    	for(Block b : coordBlockSet)
 	    		if(b.getTypeId() == id)
 	    			result.add(b);
 	    
 	    return result;
 	}
 
 	/**
 	* Ends the current TempleCraft session.
 	* Clears the Temple floor, gives all the players their rewards,
 	* and stops the spawning of monsters.
 	*/
 	protected void endTemple()
 	{
 		isRunning = false;
		readySet.clear();
 		removePlayers();
 		killMonsters();
 		repairTemple();
 		TempleManager.tellAll("Temple: \""+templeName+"\" finished.");
 	}
 	
 	protected void removeAll(){
 		removePlayers();
 		removeEditors();
 	}
 	
 	// Removes players from temple
 	protected void removePlayers(){
 		for(Player p: playerSet){
 			TemplePlayer tp = TempleManager.templePlayerMap.get(p);
 			if(tp == null)
 				return;
 			if(tp.currentTemple != null && tp.currentTemple == this)
 				TempleManager.playerLeave(p);
 		}
 	}
 	// Removes editors from temple
 	protected void removeEditors(){
 		for(Player p: editorSet){
 			TemplePlayer tp = TempleManager.templePlayerMap.get(p);
 			if(tp == null)
 				return;
 			if(tp.currentTemple == this)
 				TempleManager.playerLeave(p);
 		}
 	}
 	
 	/**
 	* Attempts to let a player join the Temple session.
 	* Players must have an empty inventory to join the Temple. Their
 	* location will be stored for when they leave.
 	*/
 	protected void playerJoin(Player p)
 	{
 		
 		if (!TempleManager.isEnabled || !this.isEnabled)
 		{
 		    tellPlayer(p, "TempleCraft is not enabled.");
 		    return;
 		}
 		if (!isSetup && !trySetup())
 		{
 			tellPlayer(p, "Temple \""+templeName+"\" has not been set up yet!");
 			return;
 		}
 		if (playerSet.contains(p))
 		{
 		    tellPlayer(p, "You are already playing!");
 		    return;
 		}
 		if (TempleManager.playerSet.contains(p))
 		{
 		    tellPlayer(p, "You are already playing in a different Temple!");
 		    return;
 		}
 		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
 		if(tp.currentTemple != null){
 			tellPlayer(p, "Please leave the current temple before joining another.");
 		    return;
 		}
 		if (isRunning)
 		{
 		    tellPlayer(p, "Temple \""+templeName+"\" in progress.");
 		    return;
 		}
 		
 		tp.currentTemple = this;
 		tp.currentCheckpoint = null;
 		TempleManager.playerSet.add(p);
 		playerSet.add(p);
 		p.setHealth(20);
 		if(world.getPlayers().isEmpty()){
 			world.setTime(8000);
 			world.setStorm(false);
 		}
 		
 		if (!TempleManager.locationMap.containsKey(p))
 		    TempleManager.locationMap.put(p, p.getLocation());
 		
 		if(!TCUtils.hasPlayerInventory(p.getName()))
 			TCUtils.keepPlayerInventory(p);
 		
 		convertLobby();
 		p.teleport(lobbyLoc);
 		tellPlayer(p, "You joined "+templeName+". Have fun!");
 	}
 	
 	protected boolean trySetup(){
 		boolean foundLobbyLoc = false;
 		boolean foundTempleLoc = false;
 		
 		for(Block b: getBlockSet(Material.WALL_SIGN.getId())){
 			if(foundLobbyLoc)
 				break;
 			Sign sign = (Sign) b.getState();
 			foundLobbyLoc = checkSign(sign);
 		}
 		for(Block b: getBlockSet(Material.SIGN_POST.getId())){     
 			if(foundLobbyLoc)
 				break;
 	        Sign sign = (Sign) b.getState();
 	        foundLobbyLoc = checkSign(sign);
 		}
 		for(Block b: getBlockSet(diamondBlock)){
 			if(foundTempleLoc)
 				break;
     		Block rb = b.getRelative(0, -1, 0);
     		if(rb.getTypeId() == ironBlock){
     			foundTempleLoc = true;
     		}
 		}
 		isSetup = foundLobbyLoc && foundTempleLoc;
 		return isSetup;
 	}
 	
 	private boolean checkSign(Sign sign) {
 		String[] Lines = sign.getLines();
 		if(!Lines[0].equals("[TC]") && !Lines[0].equals("[TempleCraft]"))
 			return false;
 		
 		if(Lines[1].toLowerCase().equals("lobby")){
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	* Adds a joined Temple player to the set of ready players.
 	*/
 	protected void playerReady(Player p)
 	{
 	readySet.add(p);
 	
 	if (readySet.equals(playerSet) && !isRunning)
 	    startTemple();
 	}
 	
 	/**
 	* Removes a dead player from the Temple session.
 	* The player is teleported safely back to the spectator area,
 	* and their health is restored. All sets and maps are updated.
 	* If this was the last player alive, the Temple session ends.
 	*/
 	protected void playerDeath(Player p)
 	{
 		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
 		Holdings balance = iConomy.getAccount(p.getName()).getHoldings();
 		
 		for(Player player : playerSet){
 			String msg = p.getName() + " died!";
 			tellPlayer(player, msg);
 		}
 		
 		deadSet.add(p);
 		MobArenaClasses.classMap.remove(p);
 		tp.tempSet.clear();
 		tp.roundDeaths++;
 		
 		String msg;
 		if(TempleCraft.iConomy == null || balance.hasEnough(rejoinCost)){
 			if(TempleCraft.iConomy != null && rejoinCost > 0){
 				msg = "To continue playing will cost you "+ChatColor.GOLD+rejoinCost+" gold.";
 				TempleManager.tellPlayer(p, msg);
 				msg = "Or type \"/tc leave\" and restart from the beginning!";
 				TempleManager.tellPlayer(p, msg);
 			} else {
 				//msg = "Rejoin your friends! :O";
 				//TempleManager.tellPlayer(p, msg);
 			}
 		} else {
 			msg = "You do not have enough gold to rejoin.";
 			TempleManager.tellPlayer(p, msg);
 			msg = "Please type \"/tc leave\" to leave the temple.";
 			TempleManager.tellPlayer(p, msg);
 		}
 		if (isRunning && playerSet.isEmpty()){
 		    endTemple();
 		}
 		
 		p.setHealth(20);
 		p.teleport(lobbyLoc);
 		p.setFireTicks(0);
 	}
 	
 	/**
 	* Prints the list of players currently in the Temple session.
 	*/
 	protected void playerList(Player p)
 	{
 	if (playerSet.isEmpty())
 	{
 	    tellPlayer(p, "There is no one in the Temple right now.");
 	    return;
 	}
 	
 	StringBuffer list = new StringBuffer();
 	final String SEPARATOR = ", ";
 	for (Player player : playerSet)
 	{
 	    list.append(player.getName());
 	    list.append(SEPARATOR);
 	}
 	
 	tellPlayer(p, ChatColor.GRAY + "Playing in " + templeName + ": " + ChatColor.WHITE + list.substring(0, list.length() - 2));
 	}
 	
 	/**
 	* Prints the list of players who aren't ready.
 	*/
 	protected void notReadyList(Player p)
 	{
 		if (playerSet.isEmpty())
 		{
 		    tellPlayer(p, "No one is in " + templeName + ".");
 		    return;
 		}
 		
 		Set<Player> notReadySet = new HashSet<Player>(playerSet);
 		notReadySet.removeAll(readySet);
 		
 		if (notReadySet.isEmpty())
 		{
 		    tellPlayer(p, "Everyone is ready in " + templeName + ".");
 		    return;
 		}
 		
 		StringBuffer list = new StringBuffer();
 		final String SEPARATOR = ", ";
 		for (Player player : notReadySet)
 		{
 		    list.append(player.getName());
 		    list.append(SEPARATOR);
 		}
 		
 		tellPlayer(p, ChatColor.GRAY + "Not ready in " + templeName + ": " + ChatColor.WHITE + list.substring(0, list.length() - 2));
 	}
 	
 	/**
 	* Forcefully starts the Temple, causing all players in the
 	* playerSet who aren't ready to leave, and starting the
 	* Temple for everyone else.
 	*/
 	protected void forceStart(Player p)
 	{
 	if (isRunning)
 	{
 	    tellPlayer(p, "Temple has already started.");
 	    return;
 	}
 	if (readySet.isEmpty())
 	{
 	    tellPlayer(p, "Can't force start, no players are ready.");
 	    return;
 	}
 	
 	Iterator<Player> iterator = playerSet.iterator();
 	while (iterator.hasNext())
 	{
 	    Player player = iterator.next();
 	    if (!readySet.contains(player))
 	    	TempleManager.playerLeave(player);
 	}
 	
 	tellPlayer(p, "Forced Temple start.");
 	}
 	
 	/**
 	* Forcefully ends the Temple, causing all players to leave and
 	* all relevant sets and maps to be cleared.
 	*/
 	protected void forceEnd(Player p)
 	{
 	if (playerSet.isEmpty() && p != null)
 	{
 	    tellPlayer(p, "No one is in the Temple.");
 	    return;
 	}
 	
 	// Just for good measure.
 	endTemple();
 	
 	if(p != null)
 		tellPlayer(p, "Forced Temple end.");
 	}
 	
 	/* ///////////////////////////////////////////////////////////////////// //
 	
 	MOB METHODS
 	
 	// ///////////////////////////////////////////////////////////////////// */
 	
 	protected void SpawnMobs(Location loc, CreatureType mob) {
 		//for (int i = 0; i < playerSet.size(); i++)
 	    //{
 			LivingEntity e = world.spawnCreature(loc,mob);
 			
 			if(e == null)
 				return;
 	        
 	        Random r = new Random();
 	        if(TempleCraft.iConomy != null && (TempleManager.mobGoldMin + TempleManager.mobGoldRan) != 0 && r.nextInt(3) == 0){
 	        	mobGoldMap.put(e.getEntityId(), r.nextInt(TempleManager.mobGoldRan)+TempleManager.mobGoldMin);
 	        }
 	        
 	        if(!(e instanceof Creature))
 	        	return;
 	        
 	        // Grab a random target.
 	        Creature c = (Creature) e;
 	        c.setTarget(TCUtils.getClosestPlayer(this, e));
 	    //}
 	}
 	
 	private CreatureType getRandomCreature() {
 		int dZombies, dSkeletons, dSpiders, dCreepers, dWolves;
 		dZombies = 5;
 		dSkeletons = dZombies + 5;
 		dSpiders = dSkeletons + 5;
 		dCreepers = dSpiders + 5;
 		dWolves = dCreepers + 5;
 		
 		CreatureType mob;
 		
 		int ran = new Random().nextInt(dWolves);
 		if      (ran < dZombies)   mob = CreatureType.ZOMBIE;
 	    else if (ran < dSkeletons) mob = CreatureType.SKELETON;
 	    else if (ran < dSpiders)   mob = CreatureType.SPIDER;
 	    else if (ran < dCreepers)  mob = CreatureType.CREEPER;
 	    else if (ran < dWolves)    mob = CreatureType.WOLF;
 	    else return null;
 		
 		return mob;
 	}
 	
 	/* ///////////////////////////////////////////////////////////////////// //
 	
 	    CLEANUP METHODS
 	
 	// ///////////////////////////////////////////////////////////////////// */
 	
 	/**
 	* Kills all monsters currently on the Temple floor.
 	*/
 	protected void killMonsters()
 	{        
 	// Remove all monsters, then clear the Set.
 	for (LivingEntity e : monsterSet)
 	{
 	    if (!e.isDead())
 	        e.remove();
 	}
 	monsterSet.clear();
 	}
 	
 	/**
 	* Removes all the blocks on the Temple floor.
 	*/
 	protected void clearTempBlocks()
 	{
 	// Remove all blocks, then clear the Set.
 	for (Block b : tempBlockSet)
 	    b.setType(Material.AIR);
 	
 	tempBlockSet.clear();
 	}
 	
 	/**
 	* Removes all items and slimes in the Temple region.
 	*/
 	protected void clearEntities(Location startLoc)
 	{
 		if(p1 == null || p2 == null || world == null)
 			return;
 	
 		int x1 = startLoc.getBlockX() + p1.getBlockX();
 		int x2 = startLoc.getBlockX() + p2.getBlockX();
 		int z1 = startLoc.getBlockZ() + p1.getBlockZ();
 		int z2 = startLoc.getBlockZ() + p2.getBlockZ();
 		
 	/* Yes, ugly nesting, but it's necessary. This bit
 	 * removes all the entities in the Temple region without
 	 * bloatfully iterating through all entities in the
 	 * world. Much faster on large servers especially. */ 
 	for (int i = x1; i <= x2; i++)
 	    for (int j = z1; j <= z2; j++)
 	    	for (Entity e : startLoc.getWorld().getChunkAt(i,j).getEntities())
 	       		if ((e instanceof Item) || (e instanceof Slime))
 	       			e.remove();
 	}
 	
 	/* ///////////////////////////////////////////////////////////////////// //
 	
     MISC METHODS
 
 	// ///////////////////////////////////////////////////////////////////// */
 	
 	/**
 	* Sends a message to a player.
 	*/
 	protected void tellPlayer(Player p, String msg)
 	{
 	if (p == null)
 	    return;
 	
 	p.sendMessage(ChatColor.GREEN + "[TC] " + ChatColor.WHITE + msg);
 	}
 	
 	/**
 	* Sends a message to all players in the Temple.
 	*/
 	protected void tellAll(String msg)
 	{
 		for(Player p: playerSet)
 			tellPlayer((Player)p, msg);	    
 	}
 }
