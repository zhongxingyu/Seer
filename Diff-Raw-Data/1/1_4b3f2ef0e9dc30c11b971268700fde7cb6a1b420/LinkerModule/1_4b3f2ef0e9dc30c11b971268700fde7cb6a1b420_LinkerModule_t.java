 package assemblernator;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * 
  * @author Eric
  * @date May 12, 2012; 3:59:15 PM
  */
 public class LinkerModule {
 	/**
 	 * TextRecord with all ModRecords associated with that text Record.
 	 */
 	Map<TextRecord, List<ModRecord>> textMod = new TreeMap<TextRecord, List<ModRecord>>();
 	/**
 	 * List of all link records in file.
 	 */
 	List<LinkerRecord> link = new ArrayList<LinkerRecord>();
 	/**
 	 * Name of program.
 	 */
 	String prgname;
 	/**
 	 * Program load address.
 	 */
 	int prgLoadadd;
 	/**
 	 * Length of program.
 	 */
 	int prgTotalLen;
 	/**
 	 * Start address of the program
 	 */
 	int prgStart;
 	/**
 	 * Date the object file was made
 	 */
 	String date;
 	/**
 	 * Version that was used to make outputfile
 	 */
 	int version;
 	/**
 	 * Total number of records
 	 */
 	int endRec;
 	/**
 	 * Total number of link records
 	 */
 	int endLink;
 	/**
 	 * Total number of text records
 	 */
 	int endText;
 	/**
 	 * Total number of mod records
 	 */
 	int endMod;
 
 	/**
 	 * @author Eric
 	 * @date May 13, 2012; 6:07:31 PM
 	 */
 	public static class TextRecord {
 		/**
 		 * Program assigned LC for text record
 		 */
 		int assignedLC;
 		/**
 		 * Assembled instruction
 		 */
 		String instrData;
 		/**
 		 * Flag for high order bits
 		 */
 		char flagHigh;
 		/**
 		 * Flag for low order bits
 		 */
 		char flagLow;
 		/**
 		 * Number of modifications for high order bits
 		 */
 		int modHigh;
 		/**
 		 * Number of modifications for low order bits
 		 */
 		int modLow;
 	}
 
 	/**
 	 * @author Eric
 	 * @date May 13, 2012; 6:07:53 PM
 	 */
 	public static class ModRecord {
 		/**
 		 * 4 hex nybbles
 		 */
 		int hex;
 		/**
 		 * Plus or minus sign
 		 */
 		char plusMin;
 		/**
 		 * Flag A or E
 		 */
 		char flagAE;
 		/**
 		 * The linkers label for mods
 		 */
 		String linkerLabel;
 		/**
 		 * H, L, or S
 		 */
 		char HLS;
 	}
 
 	/**
 	 * @author Eric
 	 * @date May 13, 2012; 6:08:12 PM
 	 */
 	public static class LinkerRecord {
 		/**
 		 * Label of link
 		 */
 		String entryLabel;
 		/**
 		 * Address of link
 		 */
 		int entryAddr;
 	}
 
 	/**
 	 * 
 	 * @param in
 	 *            the outputFile containing all the records
 	 */
 	public LinkerModule(InputStream in) {
 		// temp holders for information
 		TextRecord ttemp = new TextRecord();
 		List<ModRecord> completeMod = new ArrayList<ModRecord>();
 		ModRecord mtemp = new ModRecord();
 		LinkerRecord ltemp = new LinkerRecord();
 
 		// dont know if i need to error check but just in case
 		try {
 			char header = (char) in.read();
 			// Gets all the header information
 			if (header == 'H') {
 				String pName = "";
 				boolean run = true;
 				in.read();
 				// Gets program Name
 				while (run) {
 					char ch = (char) in.read();
 					if (ch == ':') {
 						run = false;
 					} else {
 						pName = pName + ch;
 					}
 				}
 				this.prgname = pName;
 				run = true;
 				String lc = "";
 				// Program load address
 				while (run) {
 					char ch = (char) in.read();
 					if (ch == ':') {
 						run = false;
 					} else {
 						lc = lc + ch;
 					}
 				}
 				this.prgLoadadd = Integer.parseInt(lc, 16);
 				run = true;
 				String totalLength = "";
 				// Program total length
 				while (run) {
 					char ch = (char) in.read();
 					if (ch == ':') {
 						run = false;
 					} else {
 						totalLength = totalLength + ch;
 					}
 				}
 				this.prgTotalLen = Integer.parseInt(totalLength, 16);
 				run = true;
 				String start = "";
 				// Program execution start
 				while (run) {
 					char ch = (char) in.read();
 					if (ch == ':') {
 						run = false;
 					} else {
 						start = start + ch;
 					}
 				}
 				this.prgStart = Integer.parseInt(start, 16);
 				run = true;
 				String date = "";
 				int counter = 0;
 				// Date in header
 				while (run) {
 					char ch = (char) in.read();
 					if (ch == ':' && counter == 2) {
 						run = false;
 					} else {
 						if (ch == ':') {
 							counter++;
 						}
 						date = date + ch;
 					}
 				}
 				this.date = date;
 				run = true;
 				// gets rid of URBAN-ASM
 				while (run) {
 					char ch = (char) in.read();
 					if (ch == ':') {
 						run = false;
 					} else {
 						pName = pName + ch;
 					}
 				}
 				String endOfH = "";
 				run = true;
 				// gets rid of program name at end of record
 				while (run) {
 					char ch = (char) in.read();
 					if (endOfH.equals(this.prgname)) {
 						run = false;
 					} else {
 						endOfH = endOfH + ch;
 					}
 				}
 			}
 
 			char record = (char) in.read();
 			// gets all text records, mod records, and link records
 			while (record != 'E') {
 				// checks for a text record
 				if (record == 'T') {
 					String tLC = "";
 					boolean run = true;
 					in.read();
 					// Assigned location counter
 					while (run) {
 						char ch = (char) in.read();
 						if (ch == ':') {
 							run = false;
 						} else {
 							tLC = tLC + ch;
 						}
 					}
 					ttemp.assignedLC = Integer.parseInt(tLC, 16);
 					run = true;
 					String data = "";
 					// Assembled instruction
 					while (run) {
 						char ch = (char) in.read();
 						if (ch == ':') {
 							run = false;
 						} else {
 							data = data + ch;
 						}
 					}
 					ttemp.instrData = data;
 					run = true;
 					char highFlag = 0;
 					// High order flag
 					while (run) {
 						char ch = (char) in.read();
 						if (ch == ':') {
 							run = false;
 						} else {
 							highFlag = ch;
 						}
 					}
 					ttemp.flagHigh = highFlag;
 					run = true;
 					char lowFlag = 0;
 					// Low order flag
 					while (run) {
 						char ch = (char) in.read();
 						if (ch == ':') {
 							run = false;
 						} else {
 							lowFlag = ch;
 						}
 					}
 					ttemp.flagLow = lowFlag;
 					run = true;
 					String modHigh = "";
 					// Modifications for high order
 					while (run) {
 						char ch = (char) in.read();
 						if (ch == ':') {
 							run = false;
 						} else {
 							modHigh = modHigh + ch;
 						}
 					}
 					ttemp.modHigh = Integer.parseInt(modHigh, 16);
 					run = true;
 					String modLow = "";
 					// Modifications for low order
 					while (run) {
 						char ch = (char) in.read();
 						if (ch == ':') {
 							run = false;
 						} else {
 							modLow = modLow + ch;
 						}
 					}
 					ttemp.modLow = Integer.parseInt(modLow, 16);
 					String endOfT = "";
 					run = true;
 					// End of a Text Record
 					while (run) {
 						char ch = (char) in.read();
 						if (endOfT.equals(this.prgname)) {
 							run = false;
 						} else {
 							endOfT = endOfT + ch;
 						}
 					}
 					record = (char) in.read();
 
 					// If a mod record exists while loop runs
 					while (record == 'M') {
 						in.read();
 						run = true;
 						String hex = "";
 						// Four hex nybbles
 						while (run) {
 							char ch = (char) in.read();
 							if (ch == ':') {
 								run = false;
 							} else {
 								hex = hex + ch;
 							}
 						}
 						mtemp.hex = Integer.parseInt(hex, 16);
 						run = true;
 						char plusMin = 0;
 						// Plus or minus sign
 						while (run) {
 							char ch = (char) in.read();
 							if (ch == ':') {
 								run = false;
 							} else {
 								plusMin = ch;
 							}
 						}
 						mtemp.plusMin = plusMin;
 						run = true;
 						char AE = 0;
 						// Flag A or E
 						while (run) {
 							char ch = (char) in.read();
 							if (ch == ':') {
 								run = false;
 							} else {
 								AE = ch;
 							}
 						}
 						mtemp.flagAE = AE;
 						run = true;
 						String linkerUse = "";
 						// Label linker uses
 						while (run) {
 							char ch = (char) in.read();
 							if (ch == ':') {
 								run = false;
 							} else {
 								linkerUse = linkerUse + ch;
 							}
 						}
 						mtemp.linkerLabel = linkerUse;
 						run = true;
 						char HLS = 0;
						in.read();
 						// H, L, or S
 						while (run) {
 							char ch = (char) in.read();
 							if (ch == ':') {
 								run = false;
 							} else {
 								HLS = ch;
 							}
 						}
 						mtemp.HLS = HLS;
 						String endOfM = "";
 						run = true;
 						// End of Mod Record
 						while (run) {
 							char ch = (char) in.read();
 							if (endOfM.equals(this.prgname)) {
 								run = false;
 							} else {
 								endOfM = endOfM + ch;
 							}
 						}
 						completeMod.add(mtemp);
 						// loop keeps running till a Mod record is not found
 						record = (char) in.read();
 					}
 					textMod.put(ttemp, completeMod);
 					// checks for Linking record
 				} else if (record == 'L') {
 					String label = "";
 					boolean run = true;
 					in.read();
 					// Entry label
 					while (run) {
 						char ch = (char) in.read();
 						if (ch == ':') {
 							run = false;
 						} else {
 							label = label + ch;
 						}
 					}
 					ltemp.entryLabel = label;
 					String address = "";
 					run = true;
 					// Entry address
 					while (run) {
 						char ch = (char) in.read();
 						if (ch == ':') {
 							run = false;
 						} else {
 							address = address + ch;
 						}
 					}
 					ltemp.entryAddr = Integer.parseInt(address, 16);
 					String endOfL = "";
 					run = true;
 					// End of Linker record
 					while (run) {
 						char ch = (char) in.read();
 						if (endOfL.equals(this.prgname)) {
 							run = false;
 						} else {
 							endOfL = endOfL + ch;
 						}
 					}
 					link.add(ltemp);
 					record = (char) in.read();
 				}
 			}
 			// gets all the end record info
 			if (record == 'E') {
 				String numR = "";
 				boolean run = true;
 				in.read();
 				// Number of records
 				while (run) {
 					char ch = (char) in.read();
 					if (ch == ':') {
 						run = false;
 					} else {
 						numR = numR + ch;
 					}
 				}
 				this.endRec = Integer.parseInt(numR, 16);
 				String numL = "";
 				run = true;
 				// Number of Linking records
 				while (run) {
 					char ch = (char) in.read();
 					if (ch == ':') {
 						run = false;
 					} else {
 						numL = numL + ch;
 					}
 				}
 				this.endLink = Integer.parseInt(numL, 16);
 				String numT = "";
 				run = true;
 				// Number of Text records
 				while (run) {
 					char ch = (char) in.read();
 					if (ch == ':') {
 						run = false;
 					} else {
 						numT = numT + ch;
 					}
 				}
 				this.endText = Integer.parseInt(numT, 16);
 				String numM = "";
 				run = true;
 				// Number of Mod records
 				while (run) {
 					char ch = (char) in.read();
 					if (ch == ':') {
 						run = false;
 					} else {
 						numM = numM + ch;
 					}
 				}
 				this.endMod = Integer.parseInt(numM, 16);
 			}
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
