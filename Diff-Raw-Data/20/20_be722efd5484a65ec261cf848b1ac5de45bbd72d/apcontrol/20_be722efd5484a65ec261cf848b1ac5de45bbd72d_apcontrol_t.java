 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 
 public class apcontrol {
 	// ||========== GLOBAL VARIABLES ==========||
 	private final String PATH = new String(System.getProperty("user.dir"));
 	private final String MAN = new String("/MAN");
 	private final String MASTER = new String("/MASTER.lst");
 	private final String HELP = new String(" Use \'apcontrol man\' for help.");
 	private final String[] HEADER = {
 			"#================================================================================",
 			"#ESSID:         BSSID:                  CH: ENC: CIPHER: AUTH:  KEY:",
 			"#--------------------------------------------------------------------------------"};
 
 	// ||========== CONSTRUCTOR ==========||
 	public apcontrol(String[] args) {
 		/* Verify that MASTER.lst exists. If it doesn't, *
 		 * create it with the proper header.             */
 		File master = new File(PATH+MASTER);
 		if (!master.exists()) { createMaster(master); }
 
 		/* ********** FUNCTIONS ********** */
 		if (args.length < 1) { 
 			displayFile(new File(PATH+MAN)); 
 			System.exit(0);
 		}
 		// ***** ADD *****
 		if (args[0].toLowerCase().equals("add")) {
 			if (args.length < 7) { System.out.println("\'add\' requires an ESSID, BSSID and CHANNEL at minimum."+HELP); }
 			else if (args.length > 6 || args.length < 14) {
 				String essid = "", bssid = "", enc = "???", cipher = "???", auth = "???", key = "???";
 				int channel = 0;
 				int index = 1, argsSize = args.length;
 				while (index < argsSize) {
 					if (args[index].toLowerCase().equals("-e") || args[index].toLowerCase().equals("--essid")) {
 						essid = args[index+1];
 						index=index+2;
 					}
 					else if (args[index].toLowerCase().equals("-b") || args[index].toLowerCase().equals("--bssid")) {
 						bssid = args[index+1]; //TODO Verify validity of BSSID
 						index=index+2;
 					}
 					else if (args[index].toLowerCase().equals("-ch") || args[index].toLowerCase().equals("--channel")) {
 						try { channel = Integer.parseInt(args[index+1]); }
 						catch(NumberFormatException nfe) { 
 							System.out.println("Invalid channel. Please input 1-14."+HELP); 
 							System.exit(0);
 						}
 						index=index+2;
 					}
 					else if (args[index].toLowerCase().equals("-enc") || args[index].toLowerCase().equals("--encryption")) {
						if (args[index+1].toUpperCase().equals("OPEN") || args[index+1].toUpperCase().equals("OPN")) {
							enc = new String("OPN");
 							cipher = new String("---");
 							auth = new String("---");
 							key = new String("---");
 							index=index+2;
 						}
 						else if (args[index+1].toUpperCase().equals("WEP")) {
 							enc = new String("WEP");
 							cipher = new String("WEP");
 							auth = new String("---");
 							index=index+2;
 						}
 						else if (args[index+1].toUpperCase().equals("WPA")) { 
 							enc = new String("WPA"); 
 							index=index+2;
 						}
 						else if (args[index+1].toUpperCase().equals("WPA2")) {
 							enc = new String("WPA2"); 
 							index=index+2;
 						}
 						else {
 							System.out.println(args[index+1]+" is not a valid encryption method."+HELP);
 							System.exit(0);
 						}
 					}
 					else if (args[index].toLowerCase().equals("-cipher")) {
						if (args[index+1].toUpperCase().equals("CCMP")) {
							cipher = new String(args[index+1].toUpperCase());
							auth = new String("PSK");
							index=index+2;
						}
						else if (args[index+1].toUpperCase().equals("TKIP")) {
							cipher = new String(args[index+1].toUpperCase());
							auth = new String("PSK");
							index=index+2;
 						}
 						else { 
 							System.out.println(args[index+1]+" is not a valid cipher."+HELP);
 							System.exit(0);
 						}
 					}
 					else if (args[index].toLowerCase().equals("-key")) {
 						key = new String(args[index+1]);
						index=index+2;
 					}
 					else {
 						System.out.println("Malformed \'add\' function."+HELP);
 						System.exit(0);
 					}
 				}
 				if (essid.equals("") || bssid.equals("") || channel == 0) {
 					System.out.println("\'add\' function requires ESSID, BSSID and CHANNEL at a minimum."+HELP);
 					System.exit(0);
 				}
 				else { 
 					AccessPoint ap = new AccessPoint(essid, bssid, channel, enc, cipher, auth, key); 
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					aps.add(ap);
 					printMaster(aps);
 					System.out.println("Added access point: "+essid);
 				}
 			}
 			else { System.out.println("Malformed \'add\' function."+HELP); }
 		}
 		// ***** DISPLAY *****
 		else if (args[0].toLowerCase().equals("display")) {
 			if (args.length == 1) { displayFile(new File(PATH+MASTER)); }
 			else if (args[1].toLowerCase().equals("-e") || args[1].toLowerCase().equals("--essid")) {
 				try {
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					ArrayList<AccessPoint> temp = new ArrayList<AccessPoint>();
 					for (int x=0; x<aps.size(); x++) {
 						if (aps.get(x).getESSID().toLowerCase().equals(args[2].toLowerCase())) { temp.add(aps.get(x)); }
 					}
 					if (temp.size() == 0) { System.out.println("No access point matching ESSID \'"+args[2]+"\'."); }
 					else { displayAccessPoints(temp); }
 				}catch (IndexOutOfBoundsException ioobe) { System.out.println("Not enough variables."+HELP); }
 			}
 			else if (args[1].toLowerCase().equals("-b") || args[1].toLowerCase().equals("--bssid")) {
 				try {
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					ArrayList<AccessPoint> temp = new ArrayList<AccessPoint>();
 					for (int x=0; x<aps.size(); x++) {
 						if (aps.get(x).getBSSID().toLowerCase().equals(args[2].toLowerCase())) { temp.add(aps.get(x)); }
 					}
 					if (temp.size() == 0) { System.out.println("No access point matching BSSID \'"+args[2]+"\'."); }
 					else { displayAccessPoints(temp); }
 				}catch (IndexOutOfBoundsException ioobe) { System.out.println("Not enough variables."+HELP); }
 			}
 			else if (args[1].toLowerCase().equals("-ch") || args[1].toLowerCase().equals("--channel")) {
 				try {
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					ArrayList<AccessPoint> temp = new ArrayList<AccessPoint>();
 					for (int x=0; x<aps.size(); x++) {
 						int channel = 0;
 						try { channel = Integer.parseInt(args[2]); }
 						catch (NumberFormatException nfe) { 
 							System.out.println("Please enter a valid channel."+HELP); 
 							System.exit(0);
 						}
 						if (channel == aps.get(x).getChannel()) { temp.add(aps.get(x)); }
 					}
 					if (temp.size() == 0) { 
 						System.out.println("No access point that broadcast on channel \'"+args[2]+"\' found."); 
 					}
 					else { displayAccessPoints(temp); }
 				}catch (IndexOutOfBoundsException ioobe) { System.out.println("Not enough variables."+HELP); }
 			}
 			else if (args[1].toLowerCase().equals("-enc") || args[1].toLowerCase().equals("--encryption")) {
 				try {
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					ArrayList<AccessPoint> temp = new ArrayList<AccessPoint>();
 					for (int x=0; x<aps.size(); x++) {
 						if (aps.get(x).getEnc().toUpperCase().equals(args[2].toUpperCase())) { temp.add(aps.get(x)); }
 					}
 					if (temp.size() == 0) { System.out.println("No access points found using \'"+args[2]+"\' encryption."); }
 					else { displayAccessPoints(temp); }
 				}catch (IndexOutOfBoundsException ioobe) { System.out.println("Not enough variables."+HELP); }
 			}
 			else if (args[1].toLowerCase().equals("-cipher")) {
 				try {
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					ArrayList<AccessPoint> temp = new ArrayList<AccessPoint>();
 					for (int x=0; x<aps.size(); x++) {
 						if (aps.get(x).getCipher().toUpperCase().equals(args[2].toUpperCase())) { temp.add(aps.get(x)); }
 					}
 					if (temp.size() == 0) { System.out.println("No access points found using \'"+args[2]+"\' cipher."); }
 					else { displayAccessPoints(temp); }
 				}catch (IndexOutOfBoundsException ioobe) { System.out.println("Not enough variables."+HELP); }
 			}
 			else if (args[1].toLowerCase().equals("-key")) {
 				try {
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					ArrayList<AccessPoint> temp = new ArrayList<AccessPoint>();
 					for (int x=0; x<aps.size(); x++) {
 						if (aps.get(x).getKey().equals(args[2])) { temp.add(aps.get(x)); }
 					}
 					if (temp.size() == 0) { 
 						System.out.println("No access points found using \'"+args[2]+"\' key. Remember, keys are case " +
 						"sensitive.");
 					}
 				}catch (IndexOutOfBoundsException ioobe) {System.out.println("Not enough variables."+HELP); }
 			}
 			else { System.out.println(args[1]+" is not a valid option for function \'display\'."+HELP); }
 		}
 		// ***** MANUAL *****
 		else if (args[0].toLowerCase().equals("man")) { displayFile(new File(PATH+MAN)); }
 		// ***** REMOVE *****
 		else if (args[0].toLowerCase().equals("rm")) {
 			if (args.length == 1) { displayFile(new File(PATH+MAN)); }
 			else if (args[1].toLowerCase().equals("-e") || args[1].toLowerCase().equals("--essid")) {
 				try {
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					int before = aps.size();
 					for (int x=0; x<aps.size(); x++) {
 						if (aps.get(x).getESSID().toLowerCase().equals(args[2].toLowerCase())) { aps.remove(x); }
 					}
 					int after = aps.size();
 					if (before == after) { System.out.println("No access point matching ESSID: \'"+args[2]+"\'."); }
 					else { 
 						printMaster(aps);
 						System.out.println("All access points with ESSID: \'"+args[2]+"\' have been removed."); 
 					}
 				}catch (IndexOutOfBoundsException ioobe) { System.out.println("Not enough variables."+HELP); }
 			}
 			else if (args[1].toLowerCase().equals("-b") || args[1].toLowerCase().equals("--bssid")) {
 				try {
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					int before = aps.size();
 					for (int x=0; x<aps.size(); x++) {
 						if (aps.get(x).getBSSID().toLowerCase().equals(args[2].toLowerCase())) { aps.remove(x); }
 					}
 					int after = aps.size();
 					if (before == after) { System.out.println("No access point matching BSSID: \'"+args[2]+"\'."); }
 					else { 
 						printMaster(aps);
 						System.out.println("Access points with BSSID: \'"+args[2]+"\' have been removed."); 
 					}
 				}catch (IndexOutOfBoundsException ioobe) { System.out.println("Not enough variables."+HELP); }
 			}
 			else if (args[1].toLowerCase().equals("-ch") || args[1].toLowerCase().equals("--channel")) {
 				try {
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					int before = aps.size();
 					int channel = 0;
 					try { channel = Integer.parseInt(args[2]); }
 					catch (NumberFormatException nfe) {
 						System.out.println("Please enter a valid channel."+HELP);
 						System.exit(0);
 					}
 					int index = 0, stop = aps.size();
 					while (index < stop) {
 						if (aps.get(index).getChannel() == channel) {
 							aps.remove(index);
 							stop--;
 						}
 						else { index++; }
 					}
 					int after = aps.size();
 					if (before == after) { System.out.println("No access point matching ESSID: \'"+args[2]+"\'."); }
 					else { 
 						printMaster(aps);
 						System.out.println("All access points that broadcast on channel \'"+args[2]+"\' have been removed."); 
 					}
 				}catch (IndexOutOfBoundsException ioobe) { System.out.println("Not enough variables."+HELP); }
 			}
 			else if (args[1].toLowerCase().equals("-enc") || args[1].toLowerCase().equals("--encryption")) {
 				try {
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					int before = aps.size();
 					int index = 0, stop = aps.size();
 					while (index < stop) {
 						if (aps.get(index).getEnc().toUpperCase().equals(args[2].toUpperCase())) {
 							aps.remove(index);
 							stop--;
 						}
 						else { index++; }
 					}
 					int after = aps.size();
 					if (before == after) { System.out.println("No access points found with encryption \'"+args[2]+"\'."); }
 					else { 
 						printMaster(aps);
 						System.out.println("All access points using \'"+args[2]+"\' encryption have been removed."); 
 					}
 				}catch (IndexOutOfBoundsException ioobe) { System.out.println("Not enough variables."+HELP); }
 			}
 			else if (args[1].toLowerCase().equals("-cipher")) {
 				try {
 					ArrayList<AccessPoint> aps = getAccessPoints();
 					int before = aps.size();
 					int index = 0, stop = aps.size();
 					while (index < stop) {
 						if (aps.get(index).getCipher().toUpperCase().equals(args[2].toUpperCase())) {
 							aps.remove(index);
 							stop--;
 						}
 						else { index++; }
 					}
 					int after = aps.size();
 					if (before == after) { System.out.println("No access points found using \'"+args[2]+"\' cipher."); }
 					else { 
 						printMaster(aps);
 						System.out.println("All access points using \'"+args[2]+"\' cipher have been removed."); 
 					}
 				}catch (IndexOutOfBoundsException ioobe) { System.out.println("Not enough variables."+HELP); }
 			}
 			else if (args[1].toLowerCase().equals("-key")) {
 				ArrayList<AccessPoint> aps = getAccessPoints();
 				int before = aps.size();
 				int index = 0, stop = aps.size();
 				while (index < stop) {
 					if (aps.get(index).getKey().equals(args[2])) {
 						aps.remove(index);
 						stop--;
 					}
 					else { index++; }
 				}
 				int after = aps.size();
 				if (before == after) {
 					System.out.println("No access points found using \'"+args[2]+"\' key. Remember, keys are case " +
 					"sensitive.");
 				}
 				else {
 					printMaster(aps);
 					System.out.println("All access points using \'"+args[2]+"\' key have been removed.");
 				}
 			}
 			else { System.out.println(args[1]+" is not a valid option for function \'rm\'."+HELP);	}
 		}
 		else { System.out.println(args[0]+" is not a valid function."+HELP); }
 	}
 
 	// ||========== UTILITY METHODS ==========||
 	/* Creates the master file */
 	private void createMaster(File master) {
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(master));
 			writer.write(HEADER[0]); writer.newLine();
 			writer.write(HEADER[1]); writer.newLine();
 			writer.write(HEADER[2]); writer.newLine();
 			writer.close();
 		}catch (IOException ioe) { ioe.printStackTrace(); }
 	}
 	/* Displays MASTER.lst file */
 	private void displayFile(File master) {
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(master));
 			while (reader.ready()) { System.out.println(reader.readLine()); }
 			reader.close();
 		}catch (IOException ioe) { ioe.printStackTrace(); }
 	}
 	/**/
 	private void displayAccessPoints(ArrayList<AccessPoint> accessPoints) {
 		final int ESSID=16, BSSID=24, CH=4, ENC=5, CIPHER=8, AUTH=7;
 		String[] lines = new String[accessPoints.size()];
 		for (int x=0; x<lines.length; x++) {
 			String temp = new String(accessPoints.get(x).getESSID());
 			temp+=getSpaces(accessPoints.get(x).getESSID().length(), ESSID);
 			temp+=accessPoints.get(x).getBSSID();
 			temp+=getSpaces(accessPoints.get(x).getBSSID().length(), BSSID);
 			temp+=accessPoints.get(x).getChannel();
 			int ch = accessPoints.get(x).getChannel();
 			if (ch > 9) { temp+=getSpaces(2, CH); }
 			else { temp+=getSpaces(1, CH); }
 			temp+=accessPoints.get(x).getEnc();
 			temp+=getSpaces(accessPoints.get(x).getEnc().length(), ENC);
 			temp+=accessPoints.get(x).getCipher();
 			temp+=getSpaces(accessPoints.get(x).getCipher().length(), CIPHER);
 			temp+=accessPoints.get(x).getAuth();
 			temp+=getSpaces(accessPoints.get(x).getAuth().length(), AUTH);
 			temp+=accessPoints.get(x).getKey();
 			lines[x] = temp;
 		}
 		for (int x=0; x<HEADER.length; x++) { System.out.println(HEADER[x]); }
 		for (int x=0; x<lines.length; x++) { System.out.println(lines[x]); }
 	}
 	/* Prints out all information to MASTER.lst file */
 	private void printMaster(ArrayList<AccessPoint> aps) {
 		/* SORT BY ESSID */
 		String[] essids = new String[aps.size()];
 		for (int x=0; x<essids.length; x++) { essids[x] = aps.get(x).getESSID().toLowerCase(); }
 		java.util.Arrays.sort(essids);
 		ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
 		for (int x=0; x<essids.length; x++) {
 			for (int y=0; y<essids.length; y++) {
 				if (aps.get(y).getESSID().toLowerCase().equals(essids[x])) { accessPoints.add(aps.get(y)); }
 			}
 		}
 		aps = accessPoints;
 		/* Write to file */
 		final int ESSID=16, BSSID=24, CH=4, ENC=5, CIPHER=8, AUTH=7;
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(PATH+MASTER));
 			writer.write(HEADER[0]); writer.newLine();
 			writer.write(HEADER[1]); writer.newLine();
 			writer.write(HEADER[2]); writer.newLine();
 			for (int x=0; x<aps.size(); x++) {
 				writer.write(aps.get(x).getESSID()+getSpaces(aps.get(x).getESSID().length(), ESSID));
 				writer.write(aps.get(x).getBSSID()+getSpaces(aps.get(x).getBSSID().length(), BSSID));
 				writer.write(""+aps.get(x).getChannel());
 				int ch = aps.get(x).getChannel();
 				if (ch > 9) { writer.write(getSpaces(2, CH)); }
 				else { writer.write(getSpaces(1, CH)); }
 				writer.write(aps.get(x).getEnc()+getSpaces(aps.get(x).getEnc().length(), ENC));
 				writer.write(aps.get(x).getCipher()+getSpaces(aps.get(x).getCipher().length(), CIPHER));
 				writer.write(aps.get(x).getAuth()+getSpaces(aps.get(x).getAuth().length(), AUTH));
 				writer.write(aps.get(x).getKey());
 				writer.newLine();
 			}
 			writer.close();
 		}catch (IOException ioe) { ioe.printStackTrace(); }
 	}
 	/* Returns a String containing the spaces needed to align data */
 	private String getSpaces(int itemLength, int totalSpace) {
 		String spaces = new String();
 		for (int x=itemLength; x<totalSpace; x++) { spaces+=" "; }
 		return spaces;
 	}
 	/* Returns information in MASTER.lst file as AccessPoint objects */
 	private ArrayList<AccessPoint> getAccessPoints() {
 		ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(PATH+MASTER));
 			StringTokenizer st;
 			while (reader.ready()) { 
 				st = new StringTokenizer(reader.readLine());
 				String tempESSID  = new String(st.nextToken());
 				if (tempESSID.startsWith("#", 0)) {/* restart loop */}
 				else {
 					String tempBSSID  = new String(st.nextToken());
 					int tempCh	  	  = Integer.parseInt(st.nextToken());
 					String tempEnc 	  = new String(st.nextToken());
 					String tempCipher = new String(st.nextToken());
 					String tempAuth   = new String(st.nextToken());
 					String tempKey    = new String(st.nextToken());
 					accessPoints.add(new AccessPoint(tempESSID, tempBSSID, tempCh, tempEnc, tempCipher, 
 							tempAuth, tempKey));
 				}
 			}
 			reader.close();
 		}catch (IOException ioe) { ioe.printStackTrace(); }
 		return accessPoints;
 	}
 
 
 	// *************************
 	// *****! MAIN METHOD !*****
 	// *************************
 	public static void main(String[] args) {
 		new apcontrol(args);
 	}
 }
