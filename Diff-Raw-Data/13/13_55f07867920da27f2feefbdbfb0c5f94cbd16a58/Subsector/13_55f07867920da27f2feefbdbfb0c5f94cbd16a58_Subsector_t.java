 package traveller.subsector;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import traveller.Dice;
 import traveller.Utils;
 import traveller.world.World;
 
 public class Subsector {
	
 	String name;
 
 	HashMap<Location,World> worlds = new HashMap<Location, World>();
	
 	public Subsector(String name) {
 		this.name = name;
 		this.populate(0);
 	}
 
 	public Subsector(String name, String subsectorList) {
 		this.name = name;
 
 		if (subsectorList != null) {
 			parseSECFormat(subsectorList);
 		}
 	}
 
 	public Subsector(String name, File file) {
 		
 		String subsectorList = null;
 		try {
 			byte[] buffer = new byte[(int) file.length()];
 			BufferedInputStream f;
 			f = new BufferedInputStream(new FileInputStream(file));
 			f.read(buffer);
 			subsectorList = new String(buffer);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	    
 	    this.name = name;
 	    
 		if (subsectorList != null) {
 			parseSECFormat(subsectorList);
 		}
 	    
 	}
 
	public Subsector() {
		// TODO Auto-generated constructor stub
	}

 	private int positionOf(String string, String item){
 		for (int i = 0; i < string.length(); i++) {
 			String sub = string.substring(i, i+1);
 			if (sub.contains(item)) return i;
 		}
 		return -1;
 	}
 	
 	private void parseSECFormat(String secFormattedString) {
 		String[] lines = secFormattedString.split("\n");
 		// get the format line out
 		String format = lines[1];
 		int nameEnd = positionOf(format,"1");
 		int locationEnd = positionOf(format,"2");
 		int uwpEnd = positionOf(format,"3");
 		int basesEnd = positionOf(format,"4");
 		int commentsEnd = positionOf(format,"5");
 		int zoneEnd = positionOf(format,"6");
 		int stellarEnd = positionOf(format,"7");		
 
 
 		for (int i = 2; i < lines.length; i++) {
 			String line = lines[i];
 			
 			if (line.startsWith("#") == false && line.startsWith("+") == false ) { 
 				
 				String name = line.substring(0, nameEnd).trim();
 				String location = line.substring(nameEnd,locationEnd).trim();
 				String uwp = line.substring(locationEnd,uwpEnd).trim();
 				String bases = line.substring(uwpEnd,basesEnd).trim();
 				String comments = line.substring(basesEnd,commentsEnd).trim();
 				String zone = line.substring(commentsEnd,zoneEnd).trim();
 				String pbg = line.substring(zoneEnd,stellarEnd).replace(" ", "0");
 				String stelarAlliance = line.substring(stellarEnd).trim();
 
 				try {
 					Location loc = new Location(location);
 					World world = new World(name, uwp, loc);
 					world.addPBGCode(pbg);
 					if(zone.isEmpty() == false) {
 						world.addTravelZoneCode(zone);
 					}
 					addWorld(loc, world);
 				} catch (Exception e) {
 					throw new IllegalArgumentException("subsector list element incorrect, was :\n" +
 							"Name = '" + name + "'\nLocation = '" + location +
 							"'\nUWP = '" + uwp + "'\nBases = '" + bases +
 							"\nComment = '" + comments + "'\nzone = '" + zone +
 							"'\nPBG = '" + pbg + "'\nStelar Aliance = '" + stelarAlliance, e);
 				}	
 			}
 		}
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public HashMap<Location, World> getWorlds() {
 		return worlds;
 	}
 
 	public void setWorlds(HashMap<Location, World> worlds) {
 		this.worlds = worlds;
 	}
 
 	public void addWorld(Location location, World world) {
 		this.worlds.put(location, world);
 	}
 
 	private String generateWorldLine(Location location) {
 		World world = this.worlds.get(location);
 		return String.format("%-16s %4s %9s %s",world.getName(),
 				location,world.getUPP(),world.printTradeCodes());
 	}
 
 	public String generateWorldLink(Location location) {
 		return this.worlds.get(location).generateWorldLink(
 				String.format("%s %s %s",
 						this.worlds.get(location).getName(), 
 						this.worlds.get(location).getUPP(),
 						generateWorldLine(location)),null);		
 	}
 
 	private void populate(int densityDM) {
 		for(int i = 1; i < 9; i++ ) {
 			for (int j = 1; j < 11; j++) {
 				if(Utils.isIn("4+", Dice.roll1D6(densityDM))) {
 					Location location = new Location(i,j);
 					this.addWorld(location, new World(String.format("Gen%02d%02d",i,j),location));
 				}
 			}
 		}
 	}
 
 	public Location[] getSortedLocations() {
 		Location[] keys = new Location[0];
 		keys = this.worlds.keySet().toArray(keys);
 		Arrays.sort(keys);
 		return keys;		
 	}
 
 	public String toLinkString() {
 		StringBuilder result = new StringBuilder();
 		for (Location location : getSortedLocations()) {			
 			result.append(generateWorldLink(location));
 			result.append("<br>");
 		}
 		return result.toString();
 	}	
 
 	@Override
 	public String toString() {
 		StringBuilder result = new StringBuilder();
 		for (Location location : getSortedLocations()) {			
 			result.append(generateWorldLine(location));
 			result.append("\n");
 		}
 		return result.toString();
 	}	
 
 	public Location[] getJumpWorldsLocations(Location worldLoc, int jumpNumber) {
 		ArrayList<Location> locations = new ArrayList<Location>();
 
 		// first get the jump list of locations
 		Location[] jumpLocs = worldLoc.jumpLocations(jumpNumber);
 
 		// now check against each world
 		for (Location location : worlds.keySet()) {
 			if(location.isIn(jumpLocs)) locations.add(location);
 		}
 
 		return locations.toArray(new Location[0]);
 	}
 
 	public Subsector getJumpWorlds(Location worldLoc, int jumpNumber) {
 		Location[] locs = getJumpWorldsLocations(worldLoc, jumpNumber);
		Subsector worldList = new Subsector();
 		for (Location loc : locs) {
 			worldList.addWorld(loc, worlds.get(loc));
 		}
 		return worldList;
 
 	}
 
 	public World getWorld(Location location) {
 		return worlds.get(location);
 	}
 
 	public World[] getWorldArray() {
 		return worlds.values().toArray(new World[0]);
 	}
 
 	public Object toPassString() {
 		return "ENC"+toString().replace("\n", "|").replace(" ", "_");
 	}
 
 	public static void main(String[] args) throws IOException {
 
 		System.out.println("Subsector Test");		
 
 		Subsector subsector = new Subsector("Test");
 
 		System.out.println(subsector);
 
 		System.out.println("Links");
 
 		for (Location key : subsector.worlds.keySet()) {
 			System.out.println(subsector.generateWorldLink(key));
 		}
 
 		
 		Subsector SolomaniTest = new Subsector("test", new File("Resources/SolomaniRim.txt"));
 		System.out.println("Solomani");
 		System.out.println(SolomaniTest);
 		
 		
 		Subsector fileLoadTest = new Subsector("test", new File("Resources/Foreven.txt"));
 		System.out.println(fileLoadTest);
 		
 		
 		FileInputStream fis = new FileInputStream("Resources/Foreven.txt");
 		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 		StringBuilder str = new StringBuilder();
 		String line = br.readLine();
 		while (line != null) {
 			str.append(line);
 			str.append("\n");
 			line = br.readLine();
 		}
 
 		String testString = str.toString();
 
 		Subsector sub2 = new Subsector("Foreven", testString);
 		System.out.println("Foreven");
 
 		System.out.println(sub2);
 		
 		
 		fis = new FileInputStream("Resources/Spinward.txt");
 		br = new BufferedReader(new InputStreamReader(fis));
 		str = new StringBuilder();
 		line = br.readLine();
 		while (line != null) {
 			str.append(line);
 			str.append("\n");
 			line = br.readLine();
 		}
 
 		testString = str.toString();
 
 		sub2 = new Subsector("Test2", testString);
 		System.out.println("Sub2");
 
 		System.out.println(sub2);
 
 		Location location = new Location(1,1);
 		World world = sub2.getWorld(location);
 		System.out.println(world);
 
 		System.out.println("Jump 1");
 		System.out.println(sub2.getJumpWorlds(new Location(1,3), 1).toString());
 		System.out.println("Jump 2");
 		System.out.println(sub2.getJumpWorlds(new Location(1,3), 2).toString());
 		System.out.println("Jump 3");
 		System.out.println(sub2.getJumpWorlds(new Location(1,3), 3).toString());
 		
 	}
 
 }
