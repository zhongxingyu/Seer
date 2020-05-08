 package net.kiwz.ThePlugin.utils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import net.kiwz.ThePlugin.ThePlugin;
 import net.kiwz.ThePlugin.mysql.Places;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.ChatPaginator;
 
 public class HandlePlaces {
 	private HandlePlayers hPlayers = new HandlePlayers();
 	private HandleWorlds hWorlds = new HandleWorlds();
 	private TimeFormat time = new TimeFormat();
 	private HashMap<Integer, Places> places = ThePlugin.getPlaces;
 	
 	/**
 	 * 
 	 * @param player name as String ignoring case
 	 * @param id as int
 	 * @return true if this player is an owner of the place with given id
 	 */
 	public boolean isOwner(String player, int id) {
 		if (getOwner(id).equalsIgnoreCase(player)) {
 			return true;
 		}
 		if (Bukkit.getServer().getPlayer(player).isOp()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param player name as String ignoring case
 	 * @param id as int
 	 * @return true if this player is a member of the place with given id
 	 */
 	public boolean isMember(String player, int id) {
 		for (String member : getMembers(id)) {
 			if (member.equalsIgnoreCase(player)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param player Object
 	 * @param loc as Location
 	 * @return true if this player has access to do stuff at given location
 	 */
 	public boolean hasAccess(Player player, Location loc) {
 		if (player.isOp()) {
 			return true;
 		}
 		int locX = (int) loc.getX();
 		int locY = (int) loc.getY();
 		int locZ = (int) loc.getZ();
 		if (locX < 0) locX = locX + 1;
 		if (locZ < 0) locZ = locZ + 1;
 		int id = getIDWithCoords(locX, locZ);
 		if (id != 0) {
 			if (isOwner(player.getName(), id) || isMember(player.getName(), id)) {
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
		else if (locY < 40 || !hWorlds.isClaimable(player.getWorld())) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param loc as Location
 	 * @param size as int
 	 * @return true if parts of given location + size isn't inside another place
 	 */
 	public boolean isAvailable(Location loc, int size) {
 		int x = (int) loc.getX();
 		int z = (int) loc.getZ();
 		for (int key : places.keySet()) {
 			int otherX = places.get(key).x;
 			int otherZ = places.get(key).z;
 			int otherSize = places.get(key).size;
 			if (x + size >= otherX - otherSize && x - size <= otherX + otherSize &&
 					z + size >= otherZ - otherSize && z - size <= otherZ + otherSize) {
 				return false;
 	    	}
 		}
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param id as int
 	 * @param loc as Location
 	 * @param size as int
 	 * @return true if parts of given location + size isn't inside another place
 	 */
 	public boolean isAvailable(int id, Location loc, int size) {
 		int x = (int) loc.getX();
 		int z = (int) loc.getZ();
 		for (int key : places.keySet()) {
 			int otherX = places.get(key).x;
 			int otherZ = places.get(key).z;
 			int otherSize = places.get(key).size;
 			if (x + size >= otherX - otherSize && x - size <= otherX + otherSize &&
 					z + size >= otherZ - otherSize && z - size <= otherZ + otherSize) {
 				if (places.get(key).id != id) {
 					return false;
 				}
 	    	}
 		}
 		return true;
 	}
 	
 	public boolean isNearSpawn(Player player) {
 		Location spawn = player.getWorld().getSpawnLocation();
 		int distance = 400;
 		int spawnX = spawn.getBlockX();
 		int spawnZ = spawn.getBlockZ();
 		int playerX = player.getLocation().getBlockX();
 		int playerZ = player.getLocation().getBlockZ();
 		
 		if (playerX >= spawnX + distance || playerX <= spawnX - distance ||
 				playerZ >= spawnZ + distance || playerZ <= spawnZ - distance) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param locX as double
 	 * @param locZ as double
 	 * @return id of the place that this x and z is within as int, if no place was found id = 0
 	 */
 	public int getIDWithCoords(double locX, double locZ) {
 		for (int key : places.keySet()) {
 			int placeX = places.get(key).x;
 			int placeZ = places.get(key).z;
 			int placeSize = places.get(key).size;
 			if ((placeX + placeSize) >= locX && (placeX - placeSize) <= locX &&
 					(placeZ + placeSize) >= locZ && (placeZ - placeSize) <= locZ) {
 				return places.get(key).id;
 			}
 		}
 		return 0;
 	}
 	
 	/**
 	 * 
 	 * @param owner as String
 	 * @return id's that given owner ownes as an ArrayList<Integer>
 	 */
 	public ArrayList<Integer> getIDsWithOwner(String owner) {
 		owner = hPlayers.getPlayerName(owner);
 		ArrayList<Integer> ids = new ArrayList<Integer>();
 		for (int key : places.keySet()) {
 			if (places.get(key).owner.equals(owner)) {
 				ids.add(places.get(key).id);
 			}
 		}
 		return ids;
 	}
 
 	/**
 	 * 
 	 * @param member as String
 	 * @return id's that given member is member of as an ArrayList<Integer>
 	 */
 	public ArrayList<Integer> getIDsWithMember(String member) {
 		member = hPlayers.getPlayerName(member);
 		ArrayList<Integer> ids = new ArrayList<Integer>();
 		for (int key : places.keySet()) {
 			for (String thisMember : getMembers(key)) {
 				if (thisMember.equals(member)) {
 					ids.add(places.get(key).id);
 				}
 			}
 		}
 		return ids;
 	}
 	
 	/**
 	 * 
 	 * @param name the Name of a place as String
 	 * @return id of the given place-name as int, if no place where found id = 0
 	 */
 	public int getID(String name) {
 		int id = 0;
 		for (int key : places.keySet()) {
 			if (places.get(key).name.equalsIgnoreCase(name)) {
 				id = places.get(key).id;
 			}
 		}
 		return id;
 	}
 	
 	/**
 	 * 
 	 * @param id as int
 	 * @return unix timestamp as int for when this id was added
 	 */
 	public int getTime(int id) {
 		return places.get(id).time;
 	}
 	
 	/**
 	 * 
 	 * @param id as int
 	 * @return the name of a place with given id as String
 	 */
 	public String getName(int id) {
 		return places.get(id).name;
 	}
 	
 	/**
 	 * 
 	 * @param id as int
 	 * @return the name of an owner for given id as String
 	 */
 	public String getOwner(int id) {
 		return places.get(id).owner;
 	}
 	
 	/**
 	 * 
 	 * @param owner as String
 	 * @return a String explaining wich places given owner ownes
 	 */
 	public String getOwned(String owner) {
 		if (!hPlayers.hasPlayedBefore(owner)) {
 			return ThePlugin.c2 + owner + " er ikke en spiller her";
 		}
 		owner = hPlayers.getPlayerName(owner);
 		String owned = "";
 		for (int id : getIDsWithOwner(owner)) {
 			owned = owned + "[" + places.get(id).name + "] ";
 		}
 		owned = ThePlugin.c1 + owner + " eier flgende plasser: " + owned;
 		return owned;
 	}
 	
 	/**
 	 * 
 	 * @param id as int
 	 * @return members of given id as String[]
 	 */
 	public String[] getMembers(int id) {
 		return places.get(id).members.split(" ");
 	}
 
 	/**
 	 * 
 	 * @param member as String
 	 * @return a String explaining witch places given member is member of
 	 */
 	public String getMembered(String member) {
 		if (!hPlayers.hasPlayedBefore(member)) {
 			return ThePlugin.c2 + member + " er ikke en spiller her";
 		}
 		member = hPlayers.getPlayerName(member);
 		String membered = "";
 		for (int id : getIDsWithMember(member)) {
 			membered = membered + "[" + places.get(id).name + "] ";
 		}
 		membered = ThePlugin.c1 + member + " er medlem i flgende plasser: " + membered;
 		return membered;
 	}
 	
 	/**
 	 * 
 	 * @param id as int
 	 * @return name of the world given id is in as String
 	 */
 	public String getWorld(int id) {
 		return places.get(id).world;
 	}
 	
 	/**
 	 * 
 	 * @param id as int
 	 * @return a String explaining x and z coords for given id
 	 */
 	public String getCoords(int id) {
 		return "X: " + places.get(id).x + " Z: " + places.get(id).z;
 	}
 	
 	/**
 	 * 
 	 * @param id as int
 	 * @return a String explaining the size of given id
 	 */
 	public String getSize(int id) {
 		int size = (places.get(id).size * 2) + 1;
 		return size + " x " + size + " Blokker";
 	}
 	
 	/**
 	 * 
 	 * @param id as int
 	 * @return spawn coordinates of give id as a String
 	 */
 	public String getSpawnCoords(int id) {
 		return places.get(id).spawnCoords;
 	}
 
 	/**
 	 * 
 	 * @param id as int
 	 * @return spawn pitch and yaw of give id as a String
 	 */
 	public String getSpawnPitch(int id) {
 		return places.get(id).spawnPitch;
 	}
 	
 	/**
 	 * 
 	 * @param id as int
 	 * @return spawn location of given id as Location
 	 */
 	public Location getSpawn(int id) {
 		String world = getWorld(id);
 		String[] stringCoords = getSpawnCoords(id).split(" ");
 		String[] stringPitch = getSpawnPitch(id).split(" ");
 		double x = Double.parseDouble(stringCoords[0]);
 		double y = Double.parseDouble(stringCoords[1]);
 		double z = Double.parseDouble(stringCoords[2]);
 		float pitch = Float.parseFloat(stringPitch[0]);
 		float yaw = Float.parseFloat(stringPitch[1]);
 		Location loc = new Location(Bukkit.getServer().getWorld(world), x, y, z);
 		loc.setPitch(pitch);
 		loc.setYaw(yaw);
 		return loc;
 	}
 	
 	/**
 	 * 
 	 * @param id as int
 	 * @return String "DEAKTIVERT" if pvp is not allowed, else "AKTIVERT"
 	 */
 	public String getPvP(int id) {
 		if (places.get(id).pvp == 0) {
 			return "DEAKTIVERT";
 		}
 		return "AKTIVERT";
 	}
 
 	/**
 	 * 
 	 * @param id as int
 	 * @return String "DEAKTIVERT" if monsters is not allowed, else "AKTIVERT"
 	 */
 	public String getMonsters(int id) {
 		if (places.get(id).monsters == 0) {
 			return "DEAKTIVERT";
 		}
 		return "AKTIVERT";
 	}
 
 	/**
 	 * 
 	 * @param id as int
 	 * @return String "DEAKTIVERT" if animals is not allowed, else "AKTIVERT"
 	 */
 	public String getAnimals(int id) {
 		if (places.get(id).animals == 0) {
 			return "DEAKTIVERT";
 		}
 		return "AKTIVERT";
 	}
 
 	/**
 	 * 
 	 * @param id as int
 	 * @return Enter message for given place id, null if message is empty
 	 */
 	public String getEnter(int id) {
 		return places.get(id).enter;
 	}
 
 	/**
 	 * 
 	 * @param id as int
 	 * @return Leave message for given place id, null if message is empty
 	 */
 	public String getLeave(int id) {
 		return places.get(id).leave;
 	}
 	
 	/**
 	 * 
 	 * @param player as Player
 	 * @param fromID as int
 	 * @param toID as int
 	 * 
 	 * <p>This will send Enter and Leave message to player as long as fromID not like toID</p>
 	 */
 	
 	public void sendEnterLeave(Player player, int fromID, int toID) {
 		if (fromID == toID) {
 			return;
 		}
 		if (toID == 0) {
 			if (getLeave(fromID).equals("")) {
 				player.sendMessage(ThePlugin.c1 + "Du forlater " + getName(fromID));
 			}
 			else {
 				player.sendMessage(ThePlugin.c1 + getLeave(fromID));
 			}
 		}
 		
 		else if (fromID == 0) {
 			if (getEnter(toID).equals("")) {
 				player.sendMessage(ThePlugin.c1 + "Velkommen til " + getName(toID));
 			}
 			else {
 				player.sendMessage(ThePlugin.c1 + getEnter(toID));
 			}
 		}
 		
 		else {
 			if (getLeave(fromID).equals("")) {
 				player.sendMessage(ThePlugin.c1 + "Du forlater " + getName(fromID));
 			}
 			else {
 				player.sendMessage(ThePlugin.c1 + getLeave(fromID));
 			}
 			
 			if (getEnter(toID).equals("")) {
 				player.sendMessage(ThePlugin.c1 + "Velkommen til " + getName(toID));
 			}
 			else {
 				player.sendMessage(ThePlugin.c1 + getEnter(toID));
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param sender that issued the command
 	 * @param id as int
 	 * 
 	 * <p>This will send messages to sender containing information of given id</p>
 	 */
 	public void sendPlace(CommandSender sender, int id) {
 		String members = "";
 		for (String member : getMembers(id)) {
 			members = members + member + ", ";
 		}
 		if (members.length() > 0) {
 		members = members.substring(0, members.length()-2);
 		}
 		
 		StringBuilder header = new StringBuilder();
 		header.append(ChatColor.YELLOW);
 		header.append("----- ");
 		header.append(ChatColor.WHITE);
 		header.append("Plass: " + getName(id));
 		header.append(ChatColor.YELLOW);
 		header.append(" --- ");
 		header.append(ChatColor.GRAY);
 		header.append(time.getDate(getTime(id)));
 		header.append(ChatColor.YELLOW);
 		header.append(" -----");
 		for (int i = header.length(); i < ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH; i++) {
 			header.append("-");
 		}
 		sender.sendMessage(header.toString());
 		sender.sendMessage(ThePlugin.c1 + "Eier: " + ThePlugin.c4 + getOwner(id));
 		sender.sendMessage(ThePlugin.c1 + "Medlemmer: " + ThePlugin.c4 + members);
 		sender.sendMessage(ThePlugin.c1 + "Sentrum: " + ThePlugin.c4 + getCoords(id));
 		sender.sendMessage(ThePlugin.c1 + "Strrelse: " + ThePlugin.c4 + getSize(id));
 		sender.sendMessage(ThePlugin.c1 + "PvP: " + ThePlugin.c4 + getPvP(id));
 		sender.sendMessage(ThePlugin.c1 + "Monstre: " + ThePlugin.c4 + getMonsters(id));
 		sender.sendMessage(ThePlugin.c1 + "Dyr: " + ThePlugin.c4 + getAnimals(id));
 		sender.sendMessage(ThePlugin.c1 + "Entr melding: " + ThePlugin.c4 + getEnter(id));
 		sender.sendMessage(ThePlugin.c1 + "Forlate melding: " + ThePlugin.c4 + getLeave(id));
 	}
 	
 	/**
 	 * 
 	 * @param sender that issued the command
 	 * 
 	 * <p>This will send messages to sender containing information of place where sender stands</p>
 	 */
 	public void sendPlaceHere(CommandSender sender) {
 		int locX = (int) Bukkit.getServer().getPlayer(sender.getName()).getLocation().getX();
 		int locZ = (int) Bukkit.getServer().getPlayer(sender.getName()).getLocation().getZ();
 		int id = getIDWithCoords(locX, locZ);
 		if (id != 0) {
 			String members = "";
 			for (String member : getMembers(id)) {
 				members = members + member + ", ";
 			}
 			if (members.length() > 0) {
 			members = members.substring(0, members.length()-2);
 			}
 			
 			StringBuilder header = new StringBuilder();
 			header.append(ChatColor.YELLOW);
 			header.append("----- ");
 			header.append(ChatColor.WHITE);
 			header.append("Plass: " + getName(id));
 			header.append(ChatColor.YELLOW);
 			header.append(" --- ");
 			header.append(ChatColor.GRAY);
 			header.append(time.getDate(getTime(id)));
 			header.append(ChatColor.YELLOW);
 			header.append(" -----");
 			for (int i = header.length(); i < ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH; i++) {
 				header.append("-");
 			}
 			sender.sendMessage(header.toString());
 			sender.sendMessage(ThePlugin.c1 + "Eier: " + ThePlugin.c4 + getOwner(id));
 			sender.sendMessage(ThePlugin.c1 + "Medlemmer: " + ThePlugin.c4 + members);
 			sender.sendMessage(ThePlugin.c1 + "Sentrum: " + ThePlugin.c4 + getCoords(id));
 			sender.sendMessage(ThePlugin.c1 + "Strrelse: " + ThePlugin.c4 + getSize(id));
 			sender.sendMessage(ThePlugin.c1 + "PvP: " + ThePlugin.c4 + getPvP(id));
 			sender.sendMessage(ThePlugin.c1 + "Monstre: " + ThePlugin.c4 + getMonsters(id));
 			sender.sendMessage(ThePlugin.c1 + "Dyr: " + ThePlugin.c4 + getAnimals(id));
 			sender.sendMessage(ThePlugin.c1 + "Entr melding: " + ThePlugin.c4 + getEnter(id));
 			sender.sendMessage(ThePlugin.c1 + "Forlate melding: " + ThePlugin.c4 + getLeave(id));
 		}
 		else sender.sendMessage(ThePlugin.c2 + "Ingen plass funnet");
 	}
 	
 	/**
 	 * 
 	 * @param sender that issued the command
 	 * 
 	 * <p>This will send messages to sender contaning place-names and owners</p>
 	 */
 	public void sendPlaceList(CommandSender sender) {
 		ArrayList<String> placeList = new ArrayList<String>();
 		for (int key : places.keySet()) {
 			placeList.add(ThePlugin.c1 + places.get(key).name + " [" + places.get(key).owner + "] ");
 		}
 		Collections.sort(placeList, String.CASE_INSENSITIVE_ORDER);
 		sender.sendMessage(ThePlugin.c1 + "Plass-Navn [Eier]");
 		for (String place : placeList) {
 			sender.sendMessage(place);
 		}
 	}
 
 	/**
 	 * 
 	 * @param sender that issued the command
 	 * 
 	 * <p>This will send messages to sender contaning owners and place-names</p>
 	 */
 	public void sendPlayersPlaceList(CommandSender sender) {
 		HashMap<String, String> playersPlaceList = new HashMap<String, String>();
 		ArrayList<String> playersPlacesList = new ArrayList<String>();
 		for (int key : places.keySet()) {
 			String place = "";
 			if (playersPlaceList.get(places.get(key).owner) != null) {
 				place = playersPlaceList.get(places.get(key).owner);
 			}
 			place = place + "[" + places.get(key).name + "] ";
 			String owner = places.get(key).owner;
 			playersPlaceList.put(owner, place);
 		}
 		for (String key : playersPlaceList.keySet()) {
 			playersPlacesList.add(key + ": " + playersPlaceList.get(key));
 		}
 		Collections.sort(playersPlacesList, String.CASE_INSENSITIVE_ORDER);
 		sender.sendMessage(ThePlugin.c1 + "Eier [Plass-navn]");
 		for (String playerPlaces : playersPlacesList) {
 			sender.sendMessage(ThePlugin.c1 + playerPlaces);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param player as Object
 	 * @param name of the new place as String
 	 * @param radius of the new place as int
 	 * @return String describing the result
 	 */
 	public String addPlace(Player player, String name, String radius) {
 		int size = Integer.parseInt(radius);
 		Location loc = player.getLocation();
 		String spawnCoords = loc.getX() + " " + loc.getY() + " " + loc.getZ();
 		String spawnPitch = loc.getPitch() + " " + loc.getYaw();
 		if(!player.isOp() && !hWorlds.isClaimable(player.getWorld())) {
 			return ThePlugin.c2 + "Det er ikke lov  lage plass i " + player.getWorld().getName();
 		}
 		if(!player.isOp() && getIDsWithOwner(player.getName()).size() >= 3) {
 			return ThePlugin.c2 + "Du eier " + getIDsWithOwner(player.getName()).size() + " plasser og kan ikke lage flere";
 		}
 		if (isNearSpawn(player) && (size < 10 || size > 15) && !player.isOp()) {
 			return ThePlugin.c2 + "Plassen kan ikke vre mindre enn 10 eller strre enn 15. Strre plass fr du utenfor 400 blokker fra spawnen";
 		}
 		if ((size < 10 || size > 70) && !player.isOp()) {
 			return ThePlugin.c2 + "Plassen kan ikke vre mindre enn 10 eller strre enn 70";
 		}
 		if (name.length() < 2 || name.length() > 20) {
 			return ThePlugin.c2 + "Navnet m vre 2 til 20 bokstaver langt";
 		}
 		if (name.equalsIgnoreCase("liste") || name.equalsIgnoreCase("her") || name.equalsIgnoreCase("spiller")) {
 			return ThePlugin.c2 + name + " er reservert og kan ikke brukes";
 		}
 		for (int key : places.keySet()) {
 			if (getName(key).equalsIgnoreCase(name)) {
 				return ThePlugin.c2 + "Dette navnet finnes fra fr";
 			}
 		}
 		if (!isAvailable(loc, size)) {
 			return ThePlugin.c2 + "Du er for nrme en annen plass";
     	}
 		int id = 1;
 		while (places.containsKey(id)) {
 			id++;
 		}
 		Places place = new Places();
 		place.id = id;
 		place.time = (int) (System.currentTimeMillis() / 1000);
 		place.name = name;
 		place.owner = player.getName();
 		place.members = "";
 		place.world = loc.getWorld().getName();
 		place.x = (int) loc.getX();
 		place.z = (int) loc.getZ();
 		place.size = size;
 		place.spawnCoords = spawnCoords;
 		place.spawnPitch = spawnPitch;
 		place.pvp = 0;
 		place.monsters = 0;
 		place.animals = 1;
 		place.enter = "Velkommen til " + player.getName() + " sin plass";
 		place.leave = "Du forlater " + player.getName() + " sin plass";
 		places.put(id, place);
 		for (String placeName : ThePlugin.remPlaces.keySet()) {
 			if (ThePlugin.remPlaces.get(placeName) == id) {
 				ThePlugin.remPlaces.remove(placeName);
 			}
 		}
 		int totSize = (Integer.parseInt(radius) * 2) + 1;
 		return ThePlugin.c1 + "Din nye plass heter \"" + name + "\" og er " + totSize + " x " + totSize + " blokker stor";
 	}
 	
 	/**
 	 * 
 	 * @param player as Object
 	 * @param id as int
 	 * @param radius as String
 	 * @return String describing the result
 	 */
 	public String setPlace(Player player, int id, String radius) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		int size = Integer.parseInt(radius);
 		Location loc = player.getLocation();
 		String spawnCoords = loc.getX() + " " + loc.getY() + " " + loc.getZ();
 		String spawnPitch = loc.getPitch() + " " + loc.getYaw();
 		if(!player.isOp() && !hWorlds.isClaimable(player.getWorld())) {
 			return ThePlugin.c2 + "Det er ikke lov  lage plass i " + player.getWorld().getName();
 		}
 		if (size < 10 || size > 70 || !player.isOp()) {
 			return ThePlugin.c2 + "Plassen kan ikke vre mindre enn 10 eller strre enn 70";
 		}
 		if (!isAvailable(id, loc, size)) {
 			return ThePlugin.c2 + "Du er for nrme en annen plass";
     	}
 		places.get(id).x = (int) loc.getX();
 		places.get(id).z = (int) loc.getZ();
 		places.get(id).size = size;
 		places.get(id).spawnCoords = spawnCoords;
 		places.get(id).spawnPitch = spawnPitch;
 		return ThePlugin.c1 + "Du har flyttet plassen din hit";
 	}
 	
 	/**
 	 * 
 	 * @param player as Object
 	 * @param id as int
 	 * @param name as String (the new name)
 	 * @return String describing the result
 	 */
 	public String setName(Player player, int id, String name) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		if (name.length() < 2 || name.length() > 20) {
 			return ThePlugin.c2 + "Navnet m vre 2 til 20 bokstaver langt";
 		}
 		if (name.equalsIgnoreCase("liste") || name.equalsIgnoreCase("her") || name.equalsIgnoreCase("spiller")) {
 			return ThePlugin.c2 + name + " er reservert og kan ikke brukes";
 		}
 		for (int key : places.keySet()) {
 			if (getName(key).equalsIgnoreCase(name)) {
 				return ThePlugin.c2 + "Dette navnet finnes fra fr";
 			}
 		}
 		places.get(id).name = name;
 		for (String placeName : ThePlugin.remPlaces.keySet()) {
 			if (ThePlugin.remPlaces.get(placeName) == id) {
 				ThePlugin.remPlaces.remove(placeName);
 			}
 		}
 		return ThePlugin.c1 + "Du har byttet navn p plassen din til: " + name;
 	}
 	
 	public String setOwner(Player player, int id, String owner) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		if (!hPlayers.hasPlayedBefore(owner)) {
 			return ThePlugin.c2 + owner + " er ikke en spiller her";
 		}
 		owner = hPlayers.getPlayerName(owner);
 		places.get(id).owner = owner;
 		places.get(id).enter = "Velkommen til " + owner + " sin plass";
 		places.get(id).leave = "Du forlater " + owner + " sin plass";
 		return ThePlugin.c1 + owner + " er n den nye eieren av " + getName(id);
 	}
 	
 	/**
 	 * 
 	 * @param player as Object
 	 * @param id as int
 	 * @param member as String (new member)
 	 * @return String describing the result
 	 */
 	public String addMember(Player player, int id, String member) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		if (!hPlayers.hasPlayedBefore(member)) {
 			return ThePlugin.c2 + member + " er ikke en spiller her";
 		}
 		if (isMember(member, id)) {
 			return ThePlugin.c2 + member + " er allerede medlem";
 		}
 		member = hPlayers.getPlayerName(member);
 		places.get(id).members = places.get(id).members + member + " ";
 		return ThePlugin.c1 + member + " er n medlem av " + getName(id);
 	}
 	
 	/**
 	 * 
 	 * @param player as Object
 	 * @param id as int
 	 * @param member as String (member to remove)
 	 * @return String describing the result
 	 */
 	public String remMember(Player player, int id, String member) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		if (!isMember(member, id)) {
 			return ThePlugin.c2 + member + " er ikke ett medlem";
 		}
 		member = hPlayers.getPlayerName(member);
 		places.get(id).members = places.get(id).members.replaceAll(member + " ", "");
 		return ThePlugin.c1 + member + " er n fjernet som medlem av " + getName(id);
 	}
 	
 	/**
 	 * 
 	 * @param player as Object
 	 * @param id as int
 	 * @return String describing the result
 	 */
 	public String setSpawn(Player player, int id) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		Location loc = player.getLocation();
 		int locX = (int) loc.getX();
 		int locZ = (int) loc.getZ();
 		int placeX = places.get(id).x;
 		int placeZ = places.get(id).z;
 		int placeSize = places.get(id).size;
 		if ((placeX + placeSize) >= locX && (placeX - placeSize) <= locX &&
 				(placeZ + placeSize) >= locZ && (placeZ - placeSize) <= locZ) {
 			String spawnCoords = Double.toString(loc.getX()) + " " + Double.toString(loc.getY()) + " " + Double.toString(loc.getZ());
 			String spawnPitch = Float.toString(loc.getPitch()) + " " + Float.toString(loc.getYaw());
 			places.get(id).spawnCoords = spawnCoords;
 			places.get(id).spawnPitch = spawnPitch;
 			return ThePlugin.c1 + "Du har satt ny spawn i denne plassen";
 		}
 		else {
 			return ThePlugin.c2 + "Du m st i " + places.get(id).name;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param player as Object
 	 * @param id as int
 	 * @return String describing the result
 	 */
 	public String setPvP(Player player, int id) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		if (getPvP(id).equals("DEAKTIVERT")) {
 			places.get(id).pvp = 1;
 			return ThePlugin.c1 + "PvP er AKTIVERT";
 		}
 		places.get(id).pvp = 0;
 		return ThePlugin.c1 + "PvP er DEAKTIVERT";
 	}
 
 	/**
 	 * 
 	 * @param player as Object
 	 * @param id as int
 	 * @return String describing the result
 	 */
 	public String setMonsters(Player player, int id) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		if (getMonsters(id).equals("DEAKTIVERT")) {
 			places.get(id).monsters = 1;
 			return ThePlugin.c1 + "Monstre er AKTIVERT";
 		}
 		places.get(id).monsters = 0;
 		return ThePlugin.c1 + "Monstre er DEAKTIVERT";
 	}
 
 	/**
 	 * 
 	 * @param player as Object
 	 * @param id as int
 	 * @return String describing the result
 	 */
 	public String setAnimals(Player player, int id) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		if (getAnimals(id).equals("DEAKTIVERT")) {
 			places.get(id).animals = 1;
 			return ThePlugin.c1 + "Dyr er AKTIVERT";
 		}
 		places.get(id).animals = 0;
 		return ThePlugin.c1 + "Dyr er DEAKTIVERT";
 	}
 
 	/**
 	 * 
 	 * @param player as Object
 	 * @param id as int
 	 * @param enter as String
 	 * @return String describing the result
 	 */
 	public String setEnter(Player player, int id, String enter) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		places.get(id).enter = enter;
 		if (enter.isEmpty()) {
 			return ThePlugin.c1 + "Entre meldingen er fjernet";
 		}
 		return ThePlugin.c1 + "Ny entre melding er satt";
 	}
 
 	/**
 	 * 
 	 * @param player as Object
 	 * @param id as int
 	 * @param leave as String
 	 * @return String describing the result
 	 */
 	public String setLeave(Player player, int id, String leave) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		places.get(id).leave = leave;
 		if (leave.isEmpty()) {
 			return ThePlugin.c1 + "Forlate meldingen er fjernet";
 		}
 		return ThePlugin.c1 + "Ny forlate melding er satt";
 	}
 
 	/**
 	 * 
 	 * @param player as Object
 	 * @param id as int
 	 * @return String describing the result
 	 */
 	public String remPlace(Player player, int id) {
 		if (!isOwner(player.getName(), id)) {
 			return ThePlugin.c2 + getName(id) + " er ikke din plass";
 		}
 		String placeName = places.get(id).name;
 		places.remove(id);
 		ThePlugin.remPlaces.put(placeName, id);
 		return ThePlugin.c1 + placeName + " er slettet";
 	}
 }
