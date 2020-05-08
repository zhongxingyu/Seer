 package ca.wacos;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.UUID;
 
 import net.minecraft.server.v1_5_R2.NBTCompressedStreamTools;
 import net.minecraft.server.v1_5_R2.NBTTagCompound;
 import net.minecraft.server.v1_5_R2.NBTTagDouble;
 import net.minecraft.server.v1_5_R2.NBTTagFloat;
 import net.minecraft.server.v1_5_R2.NBTTagList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 public class NametagUtils {
 
 	static String formatColors(String str) {
 		
 		char[] chars = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
 				'a', 'b', 'c', 'd', 'e', 'f', 'n', 'r', 'l', 'k', 'o', 'm'};
 		char[] array = str.toCharArray();
 		for (int t = 0; t < array.length - 1; t++) {
 			if (array[t] == '&') {
 				for (char c : chars) {
 					if (c == array[t + 1])
 						array[t] = '§';
 				}
 			}
 		}
 		return new String(array);
 	}
 
 	static void box(String [] paragraph, String title) {
 		
 		ArrayList<String> buffer = new ArrayList<String>();
 		String at = "";
 		
 		int side1 = (int) Math.round(25 - ((title.length() + 4) / 2d));
 		int side2 = (int) (26 - ((title.length() + 4) / 2d));
 		at += '+';
 		for (int t = 0; t < side1; t++)
 			at += '-';
 		at += "{ ";
 		at += title;
 		at += " }";
 		for (int t = 0; t < side2; t++)
 			at += '-';
 		at += '+';
 		buffer.add(at);
 		at = "";
 		buffer.add("|                                                   |");
 		for (String s : paragraph) {
 			at += "| ";
 			int left = 49;
 			for (int t = 0; t < s.length(); t++) {
 				at += s.charAt(t);
 				left--;
 				if (left == 0) {
 					at += " |";
 					buffer.add(at);
 					at = "";
 					at += "| ";
 					left = 49;
 				}
 			}
 			while (left-- > 0) {
 				at += ' ';
 			}
 			at += " |";
 			buffer.add(at);
 			at = "";
 		}
 		buffer.add("|                                                   |");
 		buffer.add("+---------------------------------------------------+");
 		
 		System.out.println(" ");
 		for (String line : buffer.toArray(new String[buffer.size()])) {
 			System.out.println(line);
 		}
 		System.out.println(" ");
 	}
 	static String trim(String input) {
 		if (input.length() > 16) {
 			String temp = input;
 			input = "";
 			for (int t = 0; t < 16; t++)
 				input += temp.charAt(t);
 		}
 		return input;
 	}
 	static String getValue(String rawValue) {
 		if (!(rawValue.startsWith("\"") && rawValue.endsWith("\""))) {
 			return rawValue;
 		}
 		rawValue = rawValue.trim();
 		String f1 = "";
 		for (int t = 1; t < rawValue.length() - 1; t++) {
 			f1 += rawValue.charAt(t);
 		}
 		return f1;
 	}
 	static Location getOfflineLoc(String s) {
 		File file = new File(Bukkit.getWorlds().get(0).getName() + "/players/" + s + ".dat");
 		if (!file.exists())
 			return null;
 		try {
 			NBTTagCompound compound = NBTCompressedStreamTools.a(new FileInputStream(file));
 			
 			World w = Bukkit.getWorld(new UUID(compound.getLong("WorldUUIDMost"), compound.getLong("WorldUUIDLeast")));
 			
 			NBTTagList list = compound.getList("Pos");
 			double x = ((NBTTagDouble)list.get(0)).data;
 			double y = ((NBTTagDouble)list.get(1)).data;
 			double z =((NBTTagDouble)list.get(2)).data;
 			list = compound.getList("Rotation");
 
 			float yaw = ((NBTTagFloat)list.get(0)).data;
 			float pitch = ((NBTTagFloat)list.get(1)).data;
 			
 			if (GroupLoader.DEBUG)
 				System.out.println("Loaded location from player file: " + w.getName() + ", " + x + ", " + y + ", " + z + ", " + yaw + ", " + pitch);
 			
 			Location loc = new Location(w, x, y, z, yaw, pitch);
 			
 			return loc;
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	static void setOfflineLoc(String s, Location l) {
 		File file = new File(Bukkit.getWorlds().get(0).getName() + "/players/" + s + ".dat");
 		if (!file.exists())
 			return;
 		try {
 			NBTTagCompound compound = NBTCompressedStreamTools.a(new FileInputStream(file));
 			
 			compound.set("Pos", new NBTTagList());
 			compound.getList("Pos").add(new NBTTagDouble("", l.getX()));
 			compound.getList("Pos").add(new NBTTagDouble("", l.getY()));
 			compound.getList("Pos").add(new NBTTagDouble("", l.getZ()));
 			
 		    compound.set("Rotation", new NBTTagList());
 		    compound.getList("Rotation").add(new NBTTagFloat("", l.getYaw()));
 		    compound.getList("Rotation").add(new NBTTagFloat("", l.getPitch()));
 			
 			compound.setLong("WorldUUIDLeast", l.getWorld().getUID().getLeastSignificantBits());
 			compound.setLong("WorldUUIDMost", l.getWorld().getUID().getMostSignificantBits());
 			
 			NBTCompressedStreamTools.a(compound, new FileOutputStream(file));
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	static boolean compareVersion(String old, String newer) {
 		ArrayList<Integer> oldValues = new ArrayList<Integer>();
 		ArrayList<Integer> newValues = new ArrayList<Integer>();
 		String at = "";
 		for (char c : old.toCharArray()) {
 			if (c != '.') {
 				at += c;
 			}
 			else {
 				try {
 					oldValues.add(Integer.parseInt(at));
 				}
 				catch (Exception e) {
 					return false;
 				}
 				at = "";
 			}
 		}
 		at = "";
 		for (char c : newer.toCharArray()) {
 			if (c != '.') {
 				at += c;
 			}
 			else {
 				try {
 					newValues.add(Integer.parseInt(at));
 				}
 				catch (Exception e) {
 					return false;
 				}
 				at = "";
 			}
 		}
 		int size = oldValues.size();
 		boolean defaultToOld = true;
 		if (newValues.size() < size) {
 			size = newValues.size();
 			defaultToOld = false;
 		}
 		for (int t = 0; t < size; t++) {
 			if (oldValues.get(t) < newValues.get(t)) {
 				return true;
 			}
 			else if (oldValues.get(t) > newValues.get(t)) {
 				return false;
 			}
 		}
 		if (oldValues.size() == newValues.size())
 			return false;
 		if (defaultToOld)
 			return true;
 		else return false;
 	}
 }
