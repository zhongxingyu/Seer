 package mods.learncraft.common;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.entity.player.EntityPlayer;
 
 public class Coordinates {
 
 
 	final static String fileName = "coordinates.conf";
 	public Map<String, String> coordinates = new HashMap<String, String>();
 	
 	public Coordinates() {
 		InputStream i = Coordinates.class.getResourceAsStream(fileName);
 		ArrayList<String> lines = new ArrayList<String>();
 		BufferedReader in = null;
 		
 		try {
 			in = new BufferedReader(new InputStreamReader(i));
 			String line = "";
 			while((line = in.readLine()) != null) {
 				String[] result = line.split(":",2);
 				this.addSetting(result);
 			}
 			in.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String getSetting(String settingName) {
 		return coordinates.get(settingName);
 	}
 	
 	public void addSetting(String[] coord) {
 		if(coord.length == 2) {
 			coordinates.put(coord[0],coord[1]);
 		}
 	}
 	
 	public void addSetting(String coordName, String coordValue) {
 		coordinates.put(coordName, coordValue);
 	}
 	
 	public Double[] getCoordinates(String loc) {
		// First change the location codewords into a string of the actual coordinates
		String loc_coord;
		if(loc.matches("choose_team")) {
			// z = 604 - 592
			// x = 55.45 - 58.1
			loc_coord = "57.81, 123, 600.58";
		}
		if(loc.matches("gold_spawn")) {
			// z = 604 - 592
			// x = 218 - 202
			loc_coord = "207.96, 140, 698.06";
		}
		if(loc.matches("blue_spawn")) {
			loc_coord = "211, 116.1, 499";
		}
		if(loc.matches("gold_arena")) {
			loc_coord = "117.17, 74, 548.35";
		}
		if(loc.matches("blue_arena")) {
			loc_coord = "299.42, 74, 649.95";
		}
		if(loc.matches("maze_spawn")) {
			loc_coord = "264.65, 4.1, 464.49";
		}
 		if(coordinates.containsKey(loc)) {
 			Double[] returncoord = new Double[3];
 			String[] coords = coordinates.get(loc).split(",",3);
 			for(int a=0;a<coords.length;a++) {
 				returncoord[a] = Double.parseDouble(coords[a]);
 			}
 			return returncoord;
 		}
 		return null;
 	}
 	
 	public void TeleportPlayer(EntityPlayer player, String loc) {
 		Double[] coord = getCoordinates(loc);
 		if(coord != null && coord.length==3) {
 			System.out.println(coord[0]);
 			System.out.println(coord[1]);
 			System.out.println(coord[2]);
 			player.setPosition(coord[0],coord[1],coord[2]);
 		} else {
 			player.addChatMessage("Invalid teleporation location: "+loc);
 			if(coord==null) {player.addChatMessage("Null coord");}
 		}
 	}
 }
