 /*******************************************************************************
  * Copyright (c) 2013 Travis Ralston.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.io;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Material;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.compatibility.SelfCompatibility;
 import com.turt2live.antishare.util.ASMaterialList.ASMaterial;
 import com.turt2live.materials.MaterialAPI;
 
 /**
  * Item map from a CSV file
  * 
  * @author turt2live
  */
 public class ItemMap {
 
 	private static Map<String, ASMaterial> listing = new HashMap<String, ASMaterial>();
 
 	public static List<String> getNamesFromID(Material material) {
 		@SuppressWarnings ("deprecation")
 		// TODO: Magic value
 		int id = material.getId();
 		List<String> mats = new ArrayList<String>();
 		for(String key : listing.keySet()) {
 			ASMaterial mat = listing.get(key);
 			if (mat != null) {
 				if (mat.id == id) {
 					mats.add(key);
 				}
 			}
 		}
 		return mats;
 	}
 
 	@SuppressWarnings ("deprecation")
 	// TODO: Magic value
 	public static ASMaterial get(String string) {
 		if (listing.size() <= 0) {
 			try {
 				load();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (string == null) {
 			return null;
 		}
 		string = string.trim().toLowerCase().replace(" ", "");
 		String[] parts = string.split(":");
 		String customData = null;
 		String name = parts[0];
 		if (parts.length >= 2) {
 			customData = parts[1];
 		}
 		ASMaterial asm = listing.get(string);
 		if (asm == null) {
 			Material real = Material.matchMaterial(name);
 			if (real != null) {
 				asm = new ASMaterial();
 				asm.id = real.getId();
 				asm.name = name;
 				short d = -1;
 				if (customData != null && MaterialAPI.hasData(real)) {
 					try {
 						d = Short.parseShort(customData);
 					} catch(NumberFormatException e) {}
 				}
 				asm.data = d;
 			}
 		}
 		return asm;
 	}
 
 	@SuppressWarnings ("deprecation")
 	// TODO: Magic value
 	private static void load() throws IOException {
 		AntiShare p = AntiShare.p;
 		File items = new File(p.getDataFolder(), "items.csv");
 		if (!items.exists()) {
 			createFile(items, p);
 		}
 		listing = read(items);
 		List<ASMaterial> add = SelfCompatibility.updateItemMap(listing);
 		for(ASMaterial a : add) {
 			listing.put(a.name.trim().toLowerCase(), a);
 		}
 		for(Material material : Material.values()) {
 			String name = String.valueOf(material.getId()); // Use the ID
 			if (!listing.containsKey(name)) {
 				listing.put(name, generate(name + "," + name + ",*"));
 			}
 		}
 	}
 
 	/**
 	 * Reads a file for all ASMaterials
 	 * 
 	 * @param items the file
 	 * @return a map of pointers
 	 * @throws IOException thrown if something goes wrong
 	 */
 	public static Map<String, ASMaterial> read(File items) throws IOException {
 		Map<String, ASMaterial> listing = new HashMap<String, ASMaterial>();
 		BufferedReader in = new BufferedReader(new FileReader(items));
 		String line;
 		while((line = in.readLine()) != null) {
 			if (line.startsWith("#")) {
 				continue;
 			}
 			ASMaterial asMaterial = generate(line);
 			listing.put(asMaterial.name.trim().toLowerCase(), asMaterial);
 		}
 		in.close();
 		return listing;
 	}
 
 	/**
 	 * Generates an ASMaterial from a CSV line
 	 * 
 	 * @param line the CSV line
 	 * @return the AS Material or null if invalid
 	 */
 	public static ASMaterial generate(String line) {
 		String[] parts = line.split(",");
 		if (parts.length < 3 || parts.length > 3) {
 			return null;
 		}
 		// 0 = item name
 		// 1 = id
 		// 2 = meta, * = any
 		String name = parts[0].trim().toLowerCase();
 		int id = 0;
 		short data = 0;
 		try {
 			id = Integer.parseInt(parts[1].trim());
 			String d = parts[2].trim();
 			if (d.equalsIgnoreCase("*") || !MaterialAPI.hasData(id)) {
 				data = -1;
 			} else {
 				data = Short.parseShort(d);
 			}
 		} catch(NumberFormatException e) {
 			return null;
 		}
 		ASMaterial asMaterial = new ASMaterial();
 		asMaterial.id = id;
 		asMaterial.data = data;
 		asMaterial.name = name;
 		return asMaterial;
 	}
 
 	/**
 	 * Creates an item map file
 	 * 
 	 * @param items the item file
 	 * @param p the AntiShare plugin instance
 	 * @throws IOException thrown if something goes wrong
 	 */
 	public static void createFile(File items, AntiShare p) throws IOException {
 		InputStream input = p.getResource("items.csv");
 		FileOutputStream out = new FileOutputStream(items);
 		byte[] buf = new byte[1024];
 		int len;
 		while((len = input.read(buf)) > 0) {
 			out.write(buf, 0, len);
 		}
 		out.close();
 		input.close();
 	}
 
 }
