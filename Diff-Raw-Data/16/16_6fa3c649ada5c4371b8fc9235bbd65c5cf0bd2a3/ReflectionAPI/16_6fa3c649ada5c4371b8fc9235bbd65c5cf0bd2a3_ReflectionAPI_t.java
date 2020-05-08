 package btwmods;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import btwmods.io.Settings;
 
 public class ReflectionAPI {
 	
 	private static int lookupVer = 1;
 	private static File lookupDir = new File(new File("."), "reflection");
 	private static String srgResource = "btwmods/server_ro.srg";
 	
 	private static Map<String, String> classLookup = null;
 	
 	private static Map<String, String> fieldLookupCache = new HashMap<String, String>();
 	
 	public static void init(Settings settings) {
 		
 		// Load settings.
 		if (settings.hasKey("[reflectionapi]srgresource")) {
 			srgResource = settings.get("[reflectionapi]srgresource");
 		}
 		if (settings.hasKey("[reflectionapi]reflectiondir")) {
 			lookupDir = new File(settings.get("[reflectionapi]reflectiondir"));
 		}
 		
 		// Create the lookup if it does not exist.
 		if (lookupDir.exists() && !lookupDir.isDirectory()) {
 			ModLoader.outputError("The lookupDir specified for ReflectionAPI exists and is not a directory.");
 		}
 		else {
 			if (!lookupDir.exists())
 				lookupDir.mkdir();
 			
 			if (!new File(lookupDir, "classes.txt").exists()) {
 				splitSRG();
 			}
 		}
 		
 		loadClassLookup();
 	}
 	
 	public static Field getPrivateField(Class cls, String fieldName) {
 		return getPrivateField(cls, fieldName, true);
 	}
 	
 	public static Field getPrivateField(Class cls, String fieldName, String obfuscatedFieldName) {
 		Field field = getPrivateField(cls, fieldName, true);
 		return field == null ? getPrivateField(cls, obfuscatedFieldName, false) : field;
 	}
 	
 	private static Field getPrivateField(Class cls, String fieldName, boolean translate) {
 		try {
 			Field field = cls.getDeclaredField(fieldName);
 			field.setAccessible(true);
 			return field;
 		} catch (NoSuchFieldException e) {
 			if (translate) {
 				String obfuscatedFieldName = getObfuscatedFieldName(cls, fieldName);
 				ModLoader.outputInfo("Translated " + cls.getName() + "#" + fieldName + " to " + obfuscatedFieldName);
 				
 				if (obfuscatedFieldName != null) {
 					return getPrivateField(cls, obfuscatedFieldName, false);
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	private static String getObfuscatedFieldName(Class cls, String fieldName) {
 		// Skip classes are that not in the class lookup.
 		if (!classLookup.containsKey(cls.getName()))
 			return null;
 
 		String fieldKey = cls.getName() + "#" + fieldName;
 		BufferedReader reader = null;
 		
 		// Check if the field is in the lookup cache.
 		if (fieldLookupCache.containsKey(fieldKey))
 			return fieldLookupCache.get(fieldKey);
 		
 		// Otherwise, attempt to find it in the SRG files.
 		try {
 			reader = new BufferedReader(new FileReader(new File(lookupDir, "class_" + cls.getName() + ".txt")));
 			
 			String line;
 			while ((line = reader.readLine()) != null) {
 				if (line.startsWith("FD :")) {
 					String[] split = line.substring("FD: ".length()).split(" ");
 					if (split[1].equals(fieldName)) {
 						fieldLookupCache.put(fieldKey, split[0]);
 						return split[0];
 					}
 				}
 			}
 		}
 		catch (IOException e) {
 			ModLoader.outputError(e, "ReflectionAPI threw an exception (" + e.getClass().getSimpleName() + ") while reading the class lookup for '" + cls.getName() + "': " + e.getMessage(), Level.SEVERE);
 		}
 		finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				}
 				catch (IOException e) { }
 			}
 		}
 		
 		// Mark that we could not find the obfuscated field name.
 		fieldLookupCache.put(fieldKey, null);
 		
 		return null;
 	}
 	
 	private static void loadClassLookup() {
 		if (classLookup == null) {
 			classLookup = new HashMap<String, String>();
 			
 			BufferedReader reader = null;
 			boolean done = false;
 			
 			try {
 				reader = new BufferedReader(new FileReader(new File(lookupDir, "classes.txt")));
 				
 				String line;
 				while ((line = reader.readLine()) != null) {
 					if (line.equals("DONE")) {
 						done = true;
 					}
 					else if (line.startsWith("Version: ")) {
 						try {
 							if (Integer.parseInt(line.substring("Version: ".length())) < lookupVer) {
 								try {
 									reader.close();
 								}
 								catch (IOException e) { }
 								
 								if (splitSRG()) {
 									classLookup = null;
 									loadClassLookup();
 								}
 								else {
 									classLookup.clear();
 								}
 								return;
 							}
 						}
 						catch (NumberFormatException e) {
 							ModLoader.outputError("ReflectionAPI found an invalid 'Version' line in the classes lookup: " + line);
 						}
 					}
 					else {
 						String[] split = line.split(" ");
 						classLookup.put(split[0], split[1]);
 					}
 				}
 			}
 			catch (IOException e) {
 				ModLoader.outputError(e, "ReflectionAPI threw an exception (" + e.getClass().getSimpleName() + ") while reading the classes lookup: " + e.getMessage(), Level.SEVERE);
				return;
 			}
			finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) { }
				}
 			}
 			
 			if (!done) {
 				classLookup.clear();
				ModLoader.outputError("ReflectionAPI failed to load the class lookup as it was incomplete.");
 				new File(lookupDir, "classes.txt").delete();
 			}
 		}
 	}
 	
 	private static boolean splitSRG() {
 		InputStream resourceStream = ReflectionAPI.class.getClassLoader().getResourceAsStream(srgResource);
 		
 		if (resourceStream == null) {
 			ModLoader.outputError("ReflectionAPI could not load the reobfuscation SRG.", Level.SEVERE);
 			return false;
 		}
 		
 		BufferedReader srgReader = new BufferedReader(new InputStreamReader(resourceStream));
 		
 		BufferedWriter classLookup = null;
 		Map<String, BufferedWriter> classFiles = new LinkedHashMap<String, BufferedWriter>();
 		
 		try {
 			classLookup = new BufferedWriter(new FileWriter(new File(lookupDir, "classes.txt")));
 			
 			classLookup.write("Version: " + lookupVer);
 			classLookup.newLine();
 			
 			String line = null;
 			while ((line = srgReader.readLine()) != null) {
 				if (line.startsWith("PK: ")) {
 					
 				}
 				
 				else if (line.startsWith("CL: ")) {
 					String[] split = line.substring(4).split(" ");
 					if (split.length != 2) {
 						ModLoader.outputError("ReflectionAPI found an invalid class line in the SRG: " + line);
 					}
 					else {
 						classLookup.write(line.substring(4).replace('/', '.'));
 						classLookup.newLine();
 					}
 				}
 				
 				else if (line.startsWith("FD: ")) {
 					String[] split = line.substring(4).replace('/', '.').split(" ");
 					if (split.length != 2) {
 						ModLoader.outputError("ReflectionAPI found an invalid field description line in the SRG: " + line);
 					}
 					else {
 						String[] splitObfName = split[0].replaceFirst("^(.+)\\.([^\\.]+)$", "$1 $2").split(" ");
 						String deobfName = split[1].replaceFirst("^.+\\.([^\\.]+)$", "$1");
 						
 						BufferedWriter writer = classFiles.get(splitObfName[0]);
 						
 						if (writer == null) {
 							classFiles.put(splitObfName[0], writer = new BufferedWriter(new FileWriter(new File(lookupDir, "class_" + splitObfName[0] + ".txt"))));
 						}
 						
 						writer.write("FD: " + splitObfName[1] + " " + deobfName);
 						writer.newLine();
 					}
 				}
 				
 				else if (line.startsWith("MD: ")) {
 					String[] split = line.substring(4).replace('/', '.').split(" ");
 					if (split.length != 4) {
 						ModLoader.outputError("ReflectionAPI found an invalid method description line in the SRG: " + line);
 					}
 					else {
 						String[] splitObfName = split[0].replaceFirst("^(.+)\\.([^\\.]+)$", "$1 $2").split(" ");
 						String deobfName = split[2].replaceFirst("^.+\\.([^\\.]+)$", "$1");
 						
 						BufferedWriter writer = classFiles.get(splitObfName[0]);
 						
 						if (writer == null) {
 							classFiles.put(splitObfName[0], writer = new BufferedWriter(new FileWriter(new File(lookupDir, "class_" + splitObfName[0] + ".txt"))));
 						}
 						
 						writer.write("MD: " + splitObfName[1] + " " + split[1] + " " + deobfName);
 						writer.newLine();
 					}
 				}
 			}
 			
 			classLookup.write("DONE");
 			classLookup.newLine();
 
 		} catch (IOException e) {
 			ModLoader.outputError(e, "ReflectionAPI threw an exception (" + e.getClass().getSimpleName() + ") while reading the SRG :" + e.getMessage(), Level.SEVERE);
 			return false;
 		}
 		finally {
 			// Close all the class files.
 			for (Map.Entry<String, BufferedWriter> entry : classFiles.entrySet()) {
 				try {
 					entry.getValue().close();
 				} catch (IOException e) { }
 			}
 	
 			if (classFiles != null) {
 				try {
 					classLookup.close();
 				} catch (IOException e) { }
 			}
 		}
 		
 		ModLoader.outputInfo("ReflectionAPI created the SRG lookup files.");
 		return true;
 	}
 }
