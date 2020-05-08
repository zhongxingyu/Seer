 package net.kiwz.ThePlugin.utils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 /**
  * Represents ThePlugin's Place system
  */
 public class Place {
 	private static HashMap<Integer, Place> places = new HashMap<Integer, Place>();
 	private static Set<Integer> usedIDs = new HashSet<Integer>();
 	private static Set<String> usedNames = new HashSet<String>();
 	
 	private int id;
 	private int time;
 	private String name;
 	private String owner;
 	private Set<String> members;
 	private Location center;
 	private int radius;
 	private Location spawn;
 	private boolean priv;
 	private boolean pvp;
 	private boolean monsters;
 	private boolean animals;
 	private String enter;
 	private String leave;
 	private boolean isChargeAble;
 	private boolean changed;
 	private boolean loaded;
 	private boolean removed;
 	private String worldName;
 	
 	public Place() {
 	}
 	
 	public Place(MyPlayer myPlayer, String name, String radius) {
 		int id = 1;
 		while (usedIDs.contains(id)) id++;
 		Player player = myPlayer.getOnlinePlayer();
 		
 		this.id = id;
 		this.time = (int) (System.currentTimeMillis() / 1000);
 		this.name = name;
 		this.owner = myPlayer.getUUID();
 		this.members = new HashSet<String>();
 		this.center = new Location(player.getWorld(), player.getLocation().getBlockX(), 100, player.getLocation().getBlockZ());
 		this.radius = Util.parseInt(radius);
 		this.spawn = player.getLocation();
 		this.priv = false;
 		this.pvp = false;
 		this.monsters = false;
 		this.animals = true;
 		this.enter = "Velkommen til " + name;
 		this.leave = "Du forlater " + name;
 		this.changed = true;
 		this.loaded = true;
 		this.removed = false;
 		this.worldName = player.getWorld().getName();
 	}
 	
 	public Place(int id, int time, String name, String owner, String members, String worldName, int x, int z, int radius,
 			String spawnCoords, String spawnDirection, boolean priv, boolean pvp, boolean monsters, boolean animals, String enter, String leave) {
 		World world = Bukkit.getServer().getWorld(worldName);
 		
 		Set<String> set = new HashSet<String>();
 		for (String member : members.split(" ")) {
 			set.add(member);
 		}
 		
 		this.id = id;
 		this.time = time;
 		this.name = name;
 		this.owner = owner;
 		this.members = set;
 		this.center = new Location(world, x, 100, z);
 		this.radius = radius;
 		this.spawn = Util.parseLocation(world, spawnCoords, spawnDirection);
 		this.priv = priv;
 		this.pvp = pvp;
 		this.monsters = monsters;
 		this.animals = animals;
 		this.enter = enter;
 		this.leave = leave;
 		this.changed = false;
 		this.loaded = false;
 		this.removed = false;
 		this.worldName = worldName;
 	}
 	
 	public static Place getTempPlace(int id) {
 		if (places.get(id) == null) return null;
 		Place place = new Place();
 		place.id = places.get(id).id;
 		place.time = places.get(id).time;
 		place.name = places.get(id).name;
 		place.owner = places.get(id).owner;
 		Set<String> members = new HashSet<String>();
 		members.addAll(places.get(id).members);
 		place.members = members;
 		place.center = places.get(id).center;
 		place.radius = places.get(id).radius;
 		place.spawn = places.get(id).spawn;
 		place.priv = places.get(id).priv;
 		place.pvp = places.get(id).pvp;
 		place.monsters = places.get(id).monsters;
 		place.animals = places.get(id).animals;
 		place.enter = places.get(id).enter;
 		place.leave = places.get(id).leave;
 		place.isChargeAble = places.get(id).isChargeAble;
 		place.changed = places.get(id).changed;
 		place.loaded = places.get(id).loaded;
 		place.removed = places.get(id).removed;
 		place.worldName = places.get(id).worldName;
 		return place;
 	}
 	
 	public static Place getPlace(int id) {
 		return places.get(id);
 	}
 	
 	public static Place getPlace(MyPlayer myPlayer, String name) {
 		Place place = null;
 		for (int key : places.keySet()) {
 			if (places.get(key).name.toLowerCase().startsWith(name.toLowerCase())) {
 				place = places.get(key);
 			}
 		}
 		if (myPlayer != null) {
 			for (int key : places.keySet()) {
 				if (places.get(key).owner.equals(myPlayer.getUUID())
 						&& places.get(key).name.toLowerCase().startsWith(name.toLowerCase())) {
 					place = places.get(key);
 				}
 			}
 		}
 		for (int key : places.keySet()) {
 			if (places.get(key).name.equalsIgnoreCase(name)) {
 				place = places.get(key);
 			}
 		}
		if (place.isLoaded()) return place;
 		return null;
 	}
 	
 	public static Place getPlace(Location loc) {
 		for (Place place : getPlaces()) {
 			if (loc.getWorld().equals(place.getCenter().getWorld())
 					&& (place.getCenter().getBlockX() + place.getRadius()) >= loc.getBlockX()
 					&& (place.getCenter().getBlockX() - place.getRadius()) <= loc.getBlockX()
 					&& (place.getCenter().getBlockZ() + place.getRadius()) >= loc.getBlockZ()
 					&& (place.getCenter().getBlockZ() - place.getRadius()) <= loc.getBlockZ()) {
 				if (place.isLoaded()) return place;
 			}
 		}
 		return null;
 	}
 	
 	public static List<Place> getPlacesByOwner(MyPlayer myPlayer) {
 		List<Place> list = new ArrayList<Place>();
 		for (Place place : getPlaces()) {
 			if (place.getOwner().equals(myPlayer.getUUID()) && !place.isRemoved()) {
 				list.add(place);
 			}
 		}
 		return list;
 	}
 	
 	public static List<Place> getPlacesByMember(MyPlayer myPlayer) {
 		List<Place> list = new ArrayList<Place>();
 		for (Place place : getPlaces()) {
 			if (place.getMembers().contains(myPlayer.getUUID()) && !place.isRemoved()) {
 				list.add(place);
 			}
 		}
 		return list;
 	}
 	
 	public static List<Place> getPlaces() {
 		List<Place> list = new ArrayList<Place>();
 		for (int key : places.keySet()) {
 			list.add(places.get(key));
 		}
 		return list;
 	}
 	
 	public static List<String> getPlaceList() {
 		List<String> list = new ArrayList<String>();
 		for (Place place : getPlaces()) {
 			if (!place.isRemoved()) {
 				list.add(place.getColorName() + " [" + MyPlayer.getColorName(MyPlayer.getPlayerById(place.getOwner())) + "] ");
 			}
 		}
 		Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
 		return list;
 	}
 	
 	public static List<String> getPlayerList() {
 		HashMap<String, String> map = new HashMap<String, String>();
 		for (Place place : getPlaces()) {
 			if (!place.isRemoved()) {
 				if (map.containsKey(place.getOwner())) {
 					String name = map.get(place.getOwner());
 					name = name + " [" + place.getColorName() + "]";
 					map.put(place.getOwner(), name);
 				}
 				else {
 					String name = " [" + place.getColorName() + "]";
 					map.put(place.getOwner(), name);
 				}
 			}
 		}
 		
 		List<String> list = new ArrayList<String>();
 		for (String key : map.keySet()) {
 			list.add(MyPlayer.getColorName(MyPlayer.getPlayerById(key)) + map.get(key));
 		}
 		Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
 		return list;
 	}
 	
 	public boolean hasAccess(MyPlayer myPlayer) {
 		if (myPlayer.isAdmin()) return true;
 		if (this.owner.equals(myPlayer.getUUID())) return true;
 		if (this.members.contains(myPlayer.getUUID())) return true;
 		return false;
 	}
 	
 	public int getId() {
 		return this.id;
 	}
 	
 	public int getTime() {
 		return this.time;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public String getColorName() {
 		if (this.isLoaded()) return Color.PLACE + this.name + Color.INFO;
 		else return Color.WARNING + this.name + Color.INFO;
 	}
 	
 	public void setOwner(MyPlayer myPlayer) {
 		this.members.add(getOwner());
 		this.members.remove(myPlayer.getUUID());
 		this.owner = myPlayer.getUUID();
 	}
 	
 	public String getOwner() {
 		return this.owner;
 	}
 	
 	public boolean setMember(MyPlayer myPlayer) {
 		return this.members.add(myPlayer.getUUID());
 	}
 	
 	public boolean removeMember(MyPlayer myPlayer) {
 		return this.members.remove(myPlayer.getUUID());
 	}
 	
 	public Set<String> getMembers() {
 		return this.members;
 	}
 	
 	public void setCenter(Location center) {
 		this.center = new Location(center.getWorld(), center.getBlockX(), 100, center.getZ());
 		setWorldName(center.getWorld().getName());
 	}
 	
 	public Location getCenter() {
 		return this.center;
 	}
 	
 	public void setRadius(String radiusString) {
 		this.radius = Util.parseInt(radiusString);
 	}
 	
 	public int getRadius() {
 		return this.radius;
 	}
 	
 	public void setSpawn(Location spawn) {
 		this.spawn = spawn;
 	}
 	
 	public Location getSpawn() {
 		return this.spawn;
 	}
 	
 	public void setPriv(boolean priv) {
 		this.priv = priv;
 	}
 	
 	public boolean getPriv() {
 		return this.priv;
 	}
 	
 	public void setPvP(boolean pvp) {
 		this.pvp = pvp;
 	}
 	
 	public boolean getPvP() {
 		return this.pvp;
 	}
 	
 	public void setMonsters(boolean monsters) {
 		this.monsters = monsters;
 	}
 	
 	public boolean getMonsters() {
 		return this.monsters;
 	}
 	
 	public void setAnimals(boolean animals) {
 		this.animals = animals;
 	}
 	
 	public boolean getAnimals() {
 		return this.animals;
 	}
 	
 	public void setEnter(String enter) {
 		this.enter = enter;
 	}
 	
 	public String getEnter() {
 		return this.enter;
 	}
 	
 	public void setLeave(String leave) {
 		this.leave = leave;
 	}
 	
 	public String getLeave() {
 		return this.leave;
 	}
 	
 	public void setChargeAble(boolean isChargeAble) {
 		this.isChargeAble = isChargeAble;
 	}
 	
 	public void setWorldName(String worldName) {
 		this.worldName = worldName;
 	}
 	
 	public String getWorldName() {
 		return this.worldName;
 	}
 	//
 	public void setChanged(boolean changed) {
 		this.changed = changed;
 	}
 	
 	public boolean isChanged() {
 		return this.changed;
 	}
 	
 	public void setLoaded(boolean loaded) {
 		this.loaded = loaded;
 	}
 	
 	public boolean isLoaded() {
 		return this.loaded;
 	}
 	
 	public void setRemoved(MyPlayer myPlayer, boolean removed) {
 			this.loaded = false;
 			this.removed = removed;
 	}
 	
 	public boolean isRemoved() {
 		return this.removed;
 	}
 	
 	public void remove() {
 		usedNames.remove(this.name.toLowerCase());
 		places.remove(id);
 	}
 	
 	public void save() {
 		usedIDs.add(this.id);
 		usedNames.add(this.name.toLowerCase());
 		if (this.center.getWorld() != null) setLoaded(true);
 		places.put(this.id, this);
 	}
 	
 	public boolean save(MyPlayer myPlayer) {
 		if (error(myPlayer)) return false;
 		if (getPlace(this.id) != null) usedNames.remove(getPlace(this.id).getName().toLowerCase());
 		usedIDs.add(this.id);
 		usedNames.add(this.name.toLowerCase());
 		places.put(this.id, this);
 		setChanged(true);
 		return true;
 	}
 	
 	private boolean error(MyPlayer myPlayer) {
 		boolean op = false;
 		if (myPlayer == null) op = true;
 		else op = myPlayer.getOfflinePlayer().isOp();
 		Place place = getPlace(this.id);
 		if (place != null) {
 			MyPlayer newOwner = MyPlayer.getPlayerById(this.owner);
 			if (!place.getOwner().equals(newOwner.getUUID())) {
 				if (!op && getPlacesByOwner(newOwner).size() >= 3) return true;
 			}
 			if (!op && !place.getOwner().equals(myPlayer.getUUID())) return true;
 			if (!place.getName().equalsIgnoreCase(this.name) && usedNames.contains(this.name.toLowerCase())) return true;
 			if (place.getSpawn() != this.spawn && place.getCenter() == this.center) {
 				if (getPlace(this.spawn) == null) return true;
 				if (getPlace(this.spawn).getId() != (this.id)) return true;
 			}
 		} else {
 			if (!op && getPlacesByOwner(myPlayer).size() >= 3) return true;
 			if (usedNames.contains(this.name.toLowerCase())) return true;
 		}
 		
 		if (this.name.equalsIgnoreCase("liste") || this.name.equalsIgnoreCase("her") || this.name.equalsIgnoreCase("spiller")) return true;
 		if (this.name.length() < 2 || this.name.length() > 12) return true;
 		if (!op && !MyWorld.getWorld(this.getCenter().getWorld()).getClaimable()) return true;
 		if (!op && this.radius < 10) return true;
 		if (!op && distanceToSpawn(this.getCenter()) <= 300 && this.radius > 15) return true;
 		if (!op && this.radius > 70) return true;
 		if (!available(this).isEmpty()) return true;
 		if (!op && this.isChargeAble) {
 			if (!myPlayer.charge(5)) return true;
 		}
 		return false;
 	}
 
 	public String getError(MyPlayer myPlayer) {
 		boolean op = false;
 		if (myPlayer == null) op = true;
 		else op = myPlayer.getOfflinePlayer().isOp();
 		Place place = getPlace(this.id);
 		if (place != null) {
 			MyPlayer newOwner = MyPlayer.getPlayerById(this.owner);
 			if (!place.getOwner().equals(newOwner.getUUID())) {
 				if (!op && getPlacesByOwner(newOwner).size() >= 3) return MyPlayer.getColorName(newOwner) + Color.WARNING + " eier allerede 3 plasser";
 			}
 			if (!op && !place.getOwner().equals(myPlayer.getUUID())) return "Dette er ikke din plass";
 			if (!place.getName().equalsIgnoreCase(this.name) && usedNames.contains(this.name.toLowerCase())) return "Dette navnet finnes fra fr, prv ett annet navn";
 			if (place.getSpawn() != this.spawn && place.getCenter() == this.center) {
 				if (getPlace(this.spawn) == null) return "Du m st i " + Color.PLACE + this.name + Color.WARNING + " nr du setter spawn";
 				if (getPlace(this.spawn).getId() != (this.id)) return "Du m st i " + Color.PLACE + this.name + Color.WARNING + " nr du setter spawn";
 			}
 		} else {
 			if (!op && getPlacesByOwner(myPlayer).size() >= 3) return "Du kan ikke lage flere enn 3 plasser";
 			if (usedNames.contains(this.name.toLowerCase())) return "Dette navnet finnes fra fr, prv ett annet navn";
 		}
 		
 		if (this.name.equalsIgnoreCase("liste") || this.name.equalsIgnoreCase("her") || this.name.equalsIgnoreCase("spiller")) return "Dette navnet er reservert, prv ett annet navn";
 		if (this.name.length() < 2 || this.name.length() > 12) return "Navnet m vre mellom 2 og 12 bokstaver";
 		if (!op && !MyWorld.getWorld(this.getCenter().getWorld()).getClaimable()) return "Du kan ikke lage plass i denne verdenen";
 		if (!op && this.radius < 10) return "Plassen du lager m ha en radius strre enn 9";
 		if (!op && distanceToSpawn(this.getCenter()) <= 300 && this.radius > 15) return "Plasser nre spawn (innenfor 300 blokker) m ha en radius mindre enn 16";
 		if (!op && this.radius > 70) return "Plassen du lager m ha en radius mindre enn 71";
 		if (!available(this).isEmpty()) {
 			String error = "Du er for nrme flgende plasser: ";
 			for (Place otherPlace : available(this)) error = error + Color.INFO + "[" + Color.PLACE + otherPlace.getName() + Color.INFO + "] ";
 			return error;
 		}
 		if (!op && this.isChargeAble) return "Det koster 5 gullbarer for  lage eller flytte en plass";
 		return "Det skjedde en feil, henvend deg til en Admin";
 	}
 	
 	private int distanceToSpawn(Location loc) {
 		Location spawn = MyWorld.getWorld(loc.getWorld()).getSpawn();
 		int locX = loc.getBlockX();
 		int locZ = loc.getBlockZ();
 		int spawnX = spawn.getBlockX();
 		int spawnZ = spawn.getBlockZ();
 		
 		int x = Math.abs(locX - spawnX);
 		int z = Math.abs(locZ - spawnZ);
 		return Math.max(x, z);
 	}
 	
 	private List<Place> available(Place place) {
 		List<Place> list = new ArrayList<Place>();
 		for (Place otherPlace : getPlaces()) {
 			if (place.getCenter().getWorld().equals(otherPlace.getCenter().getWorld())
 					&& place.getCenter().getBlockX() + place.getRadius() >= otherPlace.getCenter().getBlockX() - otherPlace.getRadius()
 					&& place.getCenter().getBlockX() - place.getRadius() <= otherPlace.getCenter().getBlockX() + otherPlace.getRadius()
 					&& place.getCenter().getBlockZ() + place.getRadius() >= otherPlace.getCenter().getBlockZ() - otherPlace.getRadius()
 					&& place.getCenter().getBlockZ() - place.getRadius() <= otherPlace.getCenter().getBlockZ() + otherPlace.getRadius()) {
 				if (place.getId() != otherPlace.getId() && !otherPlace.isRemoved()) {
 					list.add(otherPlace);
 				}
 	    	}
 		}
 		return list;
 	}
 }
