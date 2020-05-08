 package net.eonz.bukkit.psduo.signs;
 
 /*
  * This code is Copyright (C) 2011 Chris Bode, Some Rights Reserved.
  *
  * Copyright (C) 1999-2002 Technical Pursuit Inc., All Rights Reserved. Patent 
  * Pending, Technical Pursuit Inc.
  *
  * Unless explicitly acquired and licensed from Licensor under the Technical 
  * Pursuit License ("TPL") Version 1.0 or greater, the contents of this file are 
  * subject to the Reciprocal Public License ("RPL") Version 1.1, or subsequent 
  * versions as allowed by the RPL, and You may not copy or use this file in 
  * either source code or executable form, except in compliance with the terms and 
  * conditions of the RPL.
  *
  * You may obtain a copy of both the TPL and the RPL (the "Licenses") from 
  * Technical Pursuit Inc. at http://www.technicalpursuit.com.
  *
  * All software distributed under the Licenses is provided strictly on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TECHNICAL
  * PURSUIT INC. HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
  * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
  * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the Licenses for specific 
  * language governing rights and limitations under the Licenses. 
  */
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 
 import net.eonz.bukkit.psduo.Direction;
 import net.eonz.bukkit.psduo.PailStone;
 
 public class SignController {
 
 	/*
 	 * This class maintains a list of signs for each trigger type and dispatches
 	 * events to the proper signs. Signs call the register method from their
 	 * constructors to signify that they want to listen for a specific trigger.
 	 * The invalidate command can be called when a sign is broken or proven
	 * invalid to completly dereference the sign.
 	 * 
 	 * What does this need to do?
 	 */
 
 	// The main pailstone class.
 	private final PailStone main;
 	// Each item in this array is a different trigger type's sign tracker.
 	private final SignGroup[] groups = new SignGroup[TriggerType.values().length];
 
 	/**
 	 * Create the sign controller
 	 * 
 	 * @param main
 	 *            The Plugin's main class (for permissions and other static
 	 *            calls)
 	 */
 	public SignController(PailStone main) {
 		this.main = main;
 		for (int i = 0; i < TriggerType.values().length; i++) {
 			groups[i] = new SignGroup(TriggerType.values()[i], this.main);
 		}
 	}
 
 	/**
 	 * Registers a sign with a trigger. Usually called by the sign's
 	 * constructor.
 	 * 
 	 * @param sign
 	 *            The sign to register.
 	 * @param type
 	 *            The trigger to register under
 	 */
 	public void register(PSSign sign, TriggerType type) {
 		for (SignGroup group : groups) {
 			if (group.isType(type)) {
 				group.add(sign);
 			}
 		}
 	}
 
 	/**
 	 * Removes all references to a sign and prevents it from being triggered
 	 * ever again.
 	 * 
 	 * @param sign
 	 */
 	public void invalidate(PSSign sign, String reason) {
 		this.main.c("Invalidating sign at (" + sign.getLocation().getBlockX() + ", " + sign.getLocation().getBlockY() + ", " + sign.getLocation().getBlockZ() + ")");
 		Chunk signChunk = sign.getWorld().getChunkAt(sign.getLocation());
 		this.main.c("\tIn Chunk: (" + signChunk.getX() + ", " + signChunk.getZ() + ")");
 		this.main.c("\t" + reason);
 		for (SignGroup group : groups) {
 			group.invalidate(sign);
 		}
 		this.main.tickctrl.purge(sign);
 	}
 	
 	/**
 	 * Triggers all signs in a specific trigger category with the specified
 	 * arguments.
 	 * 
 	 * @param type
 	 * @param args
 	 */
 	public void trigger(TriggerType type, Object args) {
 		for (SignGroup s : groups) {
 			if (s.getType() == type) {
 				s.trigger(args);
 			}
 		}
 	}
 
 	public final static String signFile = "signs.dat";
 
 	public void load() {
 		BufferedReader load = null;
 
 		try {
 			load = new BufferedReader(new FileReader(PailStone.dataPath + signFile));
 		} catch (FileNotFoundException e) {
 			main.c("No sign data file found.");
			if (load != null) {
				try {
					load.close();
				} catch (IOException e1) {
				}
			}
 			return;
 		}
 
 		String line;
 		try {
			int lineNum = 1;
 			line = load.readLine();
 
 			if (line == null) {
 				main.c("No signs in sign data file.");
 				return;
 			}
 
 			if (line.equals("PAILSTONE-3")) {
 				// ORIGINAL SAVE FORMAT
 				while ((line = load.readLine()) != null) {
					lineNum++;
 					String[] params = decombinate(line, '\u001F');
 					if (params.length >= 11) {
 
 						String world = params[0].trim();
 
 						int x = Integer.parseInt(params[1].trim());
 						int y = Integer.parseInt(params[2].trim());
 						int z = Integer.parseInt(params[3].trim());
 
 						Location l = new Location(this.main.getServer().getWorld(world), x, y, z);
 
 						Direction facing = Direction.fromString(params[4].trim());
 
 						String owner = params[5].trim();
 
 						String[] lines = new String[4];
 						lines[0] = params[6].trim();
 						lines[1] = params[7].trim();
 						lines[2] = params[8].trim();
 						lines[3] = params[9].trim();
 
 						String data = params[10].trim();
 
 						PSSign.signFactory(lines, owner, data, world, l, facing, true, null, main);
 					} else {
 						main.d("Found " + params.length + " entries.");
 					}
 				}
 
 			} else {
 				main.e("Encountered an unknown save format while parsing the sign data file.");
 				try {
 					load.close();
 				} catch (IOException e1) {
 				}
 				;
 				return;
 			}
 		} catch (IOException e) {
 			main.e("Encountered while parsing the sign data file. Contact Hafnium on the bukkit forums with this information:");
 			e.printStackTrace();
 			try {
 				load.close();
 			} catch (IOException e1) {
 			}
 			return;
 		}
 
 		try {
 			load.close();
 		} catch (IOException e1) {
 		}
 		main.c("Loaded old signs.");
 
 	}
 
 	public static final String currentSaveFormat = "PAILSTONE-3";
 
 	public void save() {
 
 		// Ensure the file exists
 
 		File psdir = new File(PailStone.dataPath);
 		if (!psdir.exists()) {
 			psdir.mkdirs();
 		}
 
 		File pssdat = new File(PailStone.dataPath + signFile);
 		if (!pssdat.exists()) {
 			try {
 				pssdat.createNewFile(); // This does nothing if the file already
 										// exists.
 			} catch (IOException e) {
 				this.main.e("Error while saving sign data. some of your data may have been lost. Please make sure that bukkit can write to the \"pail\" subdirectory in its \"plugins\" subdirectory.");
 				e.printStackTrace();
 			}
 		}
 
 		BufferedWriter save;
 		try {
 			save = new BufferedWriter(new FileWriter(PailStone.dataPath + signFile));
 			save.write(currentSaveFormat);
 
 			ArrayList<PSSign> signs = this.getAllSigns();
 
 			for (int i = 0; i < signs.size(); i++) {
 				PSSign toadd = signs.get(i);
 				String[] params = new String[11];
 				params[0] = toadd.getWorld().getName().trim();
 				params[1] = "" + toadd.getLocation().getBlockX();
 				params[2] = "" + toadd.getLocation().getBlockY();
 				params[3] = "" + toadd.getLocation().getBlockZ();
 				params[4] = toadd.getFacing().name().trim();
 				params[5] = toadd.getOwnerName().trim();
 
 				String[] lines = toadd.getLines();
 
 				params[6] = lines[0].trim();
 				params[7] = lines[1].trim();
 				params[8] = lines[2].trim();
 				params[9] = lines[3].trim();
 
 				params[10] = toadd.getData().trim();
 
 				String signLine = combinate(params, '\u001F');
 
 				save.write('\n');
 				save.write(signLine);
 				save.flush();
 			}
 
 			save.close();
 
 		} catch (IOException e) {
 			this.main.e("Error while saving sign data. some of your data was lost. Please make sure that bukkit can write to the \"pail\" subdirectory in its \"plugins\" subdirectory.");
 			e.printStackTrace();
 			return;
 		}
 
 	}
 
 	/**
 	 * Gets an array list of all unique signs for saving.
 	 * 
 	 * @return
 	 */
 	private ArrayList<PSSign> getAllSigns() {
 		ArrayList<PSSign> all = new ArrayList<PSSign>();
 
 		ArrayList<PSSign> gsigns;
 		for (SignGroup g : groups) {
 			gsigns = g.getSigns();
 			for (int i = 0; i < gsigns.size(); i++) {
 				if (!all.contains(gsigns.get(i))) {
 					all.add(gsigns.get(i));
 				}
 			}
 		}
 
 		return all;
 	}
 
 	/**
 	 * Combines a string array into a delimited string.
 	 * 
 	 * @param array
 	 * @param delimiter
 	 * @return
 	 */
 	public static String combinate(String[] array, char delimiter) {
 		String result = "";
 		for (int i = 0; i < array.length; i++) {
 			if (i != 0) {
 				result += delimiter;
 			}
 
 			result += array[i].replaceAll(("" + delimiter), "");
 		}
 		return result;
 	}
 
 	/**
 	 * Breaks a string delimited by a specific char into an array.
 	 * 
 	 * @param string
 	 * @param delimiter
 	 * @return
 	 */
 	public static String[] decombinate(String string, char delimiter) {
 		String[] result = new String[count(string, delimiter) + 1];
 		int pos = 0;
 		
 		for (int i = 0; i < result.length; i++) {
 			result[i] = "";
 		}
 		
 		for (int i = 0; i < string.length(); i++) {
 			if (string.charAt(i) == delimiter) {
 				pos++;
 			} else {
 				result[pos] += string.charAt(i);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Counts the amount of a char in a string.
 	 * 
 	 * @param source
 	 * @param toCount
 	 * @return
 	 */
 	public static int count(String source, char toCount) {
 		int count = 0;
 		for (int i = 0; i < source.length(); i++) {
 			if (source.charAt(i) == toCount) {
 				count++;
 			}
 		}
 		return count;
 	}
 
 }
