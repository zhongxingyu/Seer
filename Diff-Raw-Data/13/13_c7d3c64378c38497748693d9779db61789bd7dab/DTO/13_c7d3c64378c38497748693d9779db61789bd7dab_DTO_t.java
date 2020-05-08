 package jGPIO;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 public class DTO {
 
 	static Boolean requireDTO = false;
 	static int INPUT = 0;
 	static int OUTPUT = 1;
 	static int PWM = 2;
 	static int ANALOGUE = 3;
 	
 	static JSONArray pinDefinitions = null;
 	static {
 		try {
 			String definitionFile = System.getProperty("gpio_definition");
 			 JSONParser parser = new JSONParser();
 			 pinDefinitions = (JSONArray) parser.parse(new FileReader(definitionFile));
 			 
 		} catch (NullPointerException NPE) {
			System.out.println("Could not read the property for gpio_definition, please set this since you are on Linux kernel 3.8 or above");
			System.exit(-1);
 		} catch (FileNotFoundException e) {
 			System.out.println("Could not read the GPIO Definitions file");
 			e.printStackTrace();
			System.exit(-1);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	JSONArray gpios = new JSONArray();
 	
 	/**
 	 * @param args
 	 */
 	public DTO() {
 		// determine the OS Version
 		try {
 			FileReader procVersion = new FileReader("/proc/version");
 			
 			char[] buffer = new char[100];
 			procVersion.read(buffer);
			procVersion.close();
 			String fullVersion = new String(buffer);
 			
 		    String re1="(Linux)";	// Word 1
 		    String re2="( )";	// Any Single Character 1
 		    String re3="(version)";	// Word 2
 		    String re4="( )";	// Any Single Character 2
 		    String re5="(\\d+)";	// Integer Number 1
 		    String re6="(\\.)";	// Any Single Character 3
 		    String re7="(\\d+)";	// Integer Number 2
 		    String re8="(\\.)";	// Any Single Character 4
 		    String re9="(\\d+)";	// Integer Number 3
 
 		    Pattern p = Pattern.compile(re1+re2+re3+re4+re5+re6+re7+re8+re9,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
 		    Matcher m = p.matcher(fullVersion);
 		    if (m.find())
 		    {
 		    	Integer linuxMajor = Integer.parseInt(m.group(5));
 		    	Integer linuxMinor = Integer.parseInt(m.group(7));
 		    	
 		    	if(linuxMajor >= 3 && linuxMinor >= 8) {
 		    		requireDTO = true;
 		    	}
 		    }
 			
 		} catch (FileNotFoundException e) {
 			System.out.println("Couldn't read the /proc/version. Please check for why it doesn't exist!\n");
 			System.exit(1);
 		} catch (IOException e) {
 			System.out.println("Couldn't read in from /proc/version. Please cat it to ensure it is valid\n");
 		}
 		
 		// no DTO generation required, so we'll exit and the customer can check for requireDTO to be false
 		if(!requireDTO) {
 			System.out.println("No need for DTO");
 			return;
 		}
 		// load the file containing the GPIO Definitions from the property file
 		try {
 			String definitionFile = System.getProperty("gpio_definition");
 			 JSONParser parser = new JSONParser();
 			 pinDefinitions = (JSONArray) parser.parse(new FileReader(definitionFile));
 			 
 		} catch (NullPointerException NPE) {
 			
 				System.out.println("Could not read the property for gpio_definition, please set this since you are on Linux kernel 3.8 or above");
 			
 		} catch (FileNotFoundException e) {
 			System.out.println("Could not read the GPIO Definitions file");
 			e.printStackTrace();
 		} catch (IOException e) {
 			
 			e.printStackTrace();
 		} catch (ParseException e) {
 			
 			e.printStackTrace();
 		}
 
 	}
 	
 	public boolean addGPIO(String GPIO, int mode) {
 		JSONObject gpioDetails = findDetails(GPIO);
 		if(gpioDetails == null) {
 			System.out.println("Could not find details for GPIO pin "+ GPIO + "\n");
 			return false;
 		}
 		
 		gpioDetails.put("UserMode", mode);
 		gpioDetails.put("UserSelectedPin", GPIO);
 		gpios.add(gpioDetails);
 		
 		return true;
 	}
 	
 	public String createFileContents() {
 		StringBuilder fileContents = new StringBuilder().append("/*\n")
 				.append("* Copyright (C) 2012 Texas Instruments Incorporated - http://www.ti.com/\n")
 				.append("*\n")
 				.append("* This program is free software; you can redistribute it and/or modify\n")
 				.append("* it under the terms of the GNU General Public License version 2 as\n")
 				.append("* published by the Free Software Foundation.\n")
 				.append("* Or something like that, I don't really care too much\n") 
 				.append("* Created automagically by jgpio, created by Doug Edey\n")
 				.append("* github.com/dougedey\n")
 				.append("*/\n")
 				.append("/dts-v1/;\n")
 				.append("/plugin/;\n");
 		
 
 		
 		// work out the values to be put into the file
 		
 		
 		fileContents.append("/ {\n")
 		    .append("\tcompatible = \"ti,beaglebone\", \"ti,beaglebone-black\";\n")
 			.append("\tpart-number = \"BB-JGPIO\";\n")
 		    .append("\tversion = \"00A0\";\n")
 		    .append("\n\texclusive-use=\n");
 		
 		// iterate the list of known GPIOs and add them
 		for(int i = 0; i < gpios.size(); i++) {
 			JSONObject gpioDetails = (JSONObject) gpios.get(i);
 		
 			String exclusive = (String) gpioDetails.get("key");
 			
 			if(i == (gpios.size() - 1) ) { // are we at the end of the list (there has to be an easier way using an iterator
 				fileContents.append("\t\t\""+exclusive+"\";\n");
 			} else {
 				fileContents.append("\t\t\""+exclusive+"\",\n");
 			}
 		}
 		
 		fileContents.append("\t\tfragment@0 {\n")
 			.append("\t\t\ttarget = <&am33xx_pinmux>;\n")
 			.append("\t\t\t__overlay__ {\n")
 			.append("\t\t\t\tjgpio_pins: pinmux_jgpio_pins {\n")
 			.append("\t\t\t\tpinctrl-single,pins = <\n");
 		
 		// iterate the list of known GPIOs and add them
 		for(Object o : gpios) {
 			JSONObject gpioDetails = (JSONObject) o;
 		
 			String exclusive = (String) gpioDetails.get("key");
 			String muxRegOffset = (String) gpioDetails.get("muxRegOffset");
 			Integer mode = (Integer) gpioDetails.get("UserMode");
 			String userSelectedPin = (String) gpioDetails.get("UserSelectedPin");
 			// we only need this for digital IO, analog inputs can be activated using the BB-ADC file
 			int pinmux = 0x00;
 			
 			if(mode == DTO.INPUT) {
 				pinmux = 0x30;
 			}
 			
 			if(mode == DTO.OUTPUT) {
 				pinmux = 0x00;
 			}
 			
 			JSONArray options = (JSONArray) gpioDetails.get("options");
 			for (int i = 0; i < options.size(); i++) {
 				String option = (String) options.get(i);
 				if(option.equalsIgnoreCase(userSelectedPin)) {
 					pinmux += i;
 				}
 			}
 			
 			fileContents.append("\t\t\t\t\t" + muxRegOffset + " 0x" + Integer.toHexString(pinmux) + " /* " + exclusive + "*/\n");
 		}
 		
 		fileContents.append("\t\t\t\t>;\n")
 			.append("\t\t\t};\n")
 			.append("\t\t};\n")
 			.append("\t};\n\n");
 
 		// Start the fragment to initialize it all
 		fileContents.append("\tfragment@1 {\n")
 			.append("\t\ttarget = <&ocp>;\n")
 			.append("\t\t__overlay__ {\n")
             .append("\t\t\tstatus          = \"okay\";\n")
             .append("\t\t\tpinctrl-names=\"default\";\n")
             .append("\t\t\tpinctrl-0 = <&jgpio_pins>;\n")
             .append("\t\t};\n")
 			.append("\t};\n")
 			.append("};\n");
 		
 		return fileContents.toString();
 		
 	}
 	
 	public static JSONObject findDetails(String gpio_name) {
 		// Do we have a valid definition file, or should we just direct map?
 		if (pinDefinitions == null) {
 			System.out.println("No definitions file found, assuming direct mapping");
 			return null; 
 		}
 		for (Object obj: pinDefinitions) {
 			JSONObject jObj = (JSONObject) obj;
 			String key = (String) jObj.get("key");
 			if(key.equalsIgnoreCase(gpio_name)) {
 				return jObj;
 			}
 			if(jObj.containsKey("options")) {
 				JSONArray options = (JSONArray) jObj.get("options");
 				for (int i = 0; i < options.size(); i++) {
 					String option = (String) options.get(i);
 					if(option.equalsIgnoreCase(gpio_name)) {
 						return jObj;
 					}
 				}
 			}
 		}
 		
 		// not found 
 		return null;
 	}
 	
 	public JSONObject findDetailsByOffset(String offset) {
 		for (Object obj: pinDefinitions) {
 			JSONObject jObj = (JSONObject) obj;
 			if(jObj.containsKey("muxRegOffset")) {
 				String muxRegOffset = (String) jObj.get("muxRegOffset");
 
 				if(muxRegOffset.equalsIgnoreCase(offset)) {
 					return jObj;
 				}
 			}
 		}
 	
 		return null;
 	}
 	
 
 }
