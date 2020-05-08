 package org.rsbot.util.io;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 /**
  * @author Paris
  */
 public class IniParser {
 
 	private static final char sectionOpen = '[';
 	private static final char sectionClose = ']';
 	private static final char keyBound = '=';
 	private static final char[] comments = {'#', ';'};
 	public static final String emptySection = "";
 
 	private IniParser() {
 	}
 
 	public static void serialise(final HashMap<String, HashMap<String, String>> data, final BufferedWriter out) throws IOException {
 		if (data.containsKey(emptySection)) {
 			writeSection(emptySection, data.get(emptySection), out);
 			out.newLine();
 		}
 		for (final Entry<String, HashMap<String, String>> entry : data.entrySet()) {
 			final String section = entry.getKey();
 			if (section.equals(emptySection)) {
 				continue;
 			}
 			writeSection(section, entry.getValue(), out);
 			out.newLine();
 		}
 	}
 
 	private static void writeSection(final String section, final HashMap<String, String> map, final BufferedWriter out) throws IOException {
 		if (!(section == null || section.isEmpty())) {
 			out.write(sectionOpen);
 			out.write(section);
 			out.write(sectionClose);
 			out.newLine();
 		}
 		for (final Entry<String, String> entry : map.entrySet()) {
 			out.write(entry.getKey());
 			out.write(keyBound);
 			out.write(entry.getValue());
 			out.newLine();
 		}
 	}
 
 	public static HashMap<String, HashMap<String, String>> deserialise(final File input) throws IOException {
 		final BufferedReader reader = new BufferedReader(new FileReader(input));
 		final HashMap<String, HashMap<String, String>> data = deserialise(reader);
 		reader.close();
 		return data;
 	}
 
 	public static HashMap<String, HashMap<String, String>> deserialise(final BufferedReader input) throws IOException {
 		final HashMap<String, HashMap<String, String>> data = new HashMap<String, HashMap<String, String>>();
 		String line, section = emptySection;
 
 		while ((line = input.readLine()) != null) {
 			line = line.trim();
 			if (line.isEmpty()) {
 				continue;
 			}
 			int z;
 			final int l = line.length();
 			final char t = line.charAt(0);
 			if (t == sectionOpen) {
 				z = line.indexOf(sectionClose, 1);
 				z = z == -1 ? l : z;
				section = z == 1 ? "" : line.substring(1, z - 1).trim();
 			} else {
 				boolean skip = false;
 				for (final char c : comments) {
 					if (t == c) {
 						skip = true;
 						break;
 					}
 				}
 				if (skip) {
 					continue;
 				}
 				z = line.indexOf(keyBound);
 				z = z == -1 ? l : z;
 				String key, value = "";
 				key = line.substring(0, z).trim();
 				if (++z < l) {
 					value = line.substring(z).trim();
 				}
 				if (!data.containsKey(section)) {
 					data.put(section, new HashMap<String, String>());
 				}
 				data.get(section).put(key, value);
 			}
 		}
 
 		return data;
 	}
 
 	public static boolean parseBool(final String mode) {
 		return mode.equals("1") || mode.equalsIgnoreCase("true") || mode.equalsIgnoreCase("yes");
 	}
 }
