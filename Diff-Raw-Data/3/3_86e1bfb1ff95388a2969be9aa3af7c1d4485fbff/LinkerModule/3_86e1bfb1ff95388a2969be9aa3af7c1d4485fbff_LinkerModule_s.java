 package assemblernator;
 
 import static assemblernator.ErrorReporting.makeError;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import assemblernator.ErrorReporting.ErrorHandler;
 import simulanator.ScanWrap;
 
 /**
  * 
  * @author Eric
  * @date May 12, 2012; 3:59:15 PM
  */
 public class LinkerModule implements Comparable<LinkerModule>{
 	/** Contains all the link records. */
 	public Map<String, Integer> linkRecord = new HashMap<String, Integer>();
 	/** Contains all the mod and text records. */
 	public List<TextModRecord> textMod = new ArrayList<TextModRecord>();
 	/** Name of program. */
 	public String progName;
 	/** Program load address. */
 	public int loadAddr;
 	/** Length of program. */
 	public int prgTotalLen;
 	/** Start address of the program */
 	public int execStart;
 	/** Date the object file was made */
 	public String date;
 	/** Version that was used to make outputfile */
 	public int version;
 	/** Total number of records */
 	public int endRec;
 	/** Total number of link records */
 	public int endLink;
 	/** Total number of text records */
 	public int endText;
 	/** Total number of mod records */
 	public int endMod;
 	/** the offset amount from program address. */
 	public int offset;
 	/** Success failure boolean */
 	public boolean success = false;
 	/** End reached */
 	public boolean done = true;
 	/**
 	 * 
 	 * @author Eric
 	 * @date May 20, 2012; 11:42:52 PM
 	 */
 	public static class TextModRecord {
 		/**Text Records*/
 		public TextRecord text = new TextRecord();
 		/**Mod Records*/
 		public List<ModRecord> mods = new ArrayList<ModRecord>();
 	}
 
 	/**
 	 * @author Eric
 	 * @date May 13, 2012; 6:07:31 PM
 	 */
 	public static class TextRecord {
 		/** Program assigned LC for text record */
 		public int assignedLC;
 		/** Assembled instruction */
 		public String instrData;
 		/** Flag for high order bits */
 		public char flagHigh;
 		/** Flag for low order bits */
 		public char flagLow;
 		/** Number of modifications for high order bits */
 		public int modHigh;
 		/** Number of modifications for low order bits */
 		public int modLow;
 	}
 
 	/**
 	 * @author Eric
 	 * @date May 13, 2012; 6:07:53 PM
 	 */
 	public static class ModRecord {
 		/** 4 hex nybbles */
 		public int hex;
 		/** H, L, or S */
 		public char HLS;
 		/** The middle of modifications records*/
 		public List<MiddleMod> midMod = new ArrayList<MiddleMod>();
 	}
 
 	/**
 	 * @author Eric
 	 * @date May 17, 2012; 5:50:57 PM
 	 */
 	public static class MiddleMod {
 		/** Plus or minus sign */
 		public char plusMin;
 		/** Flag A or E or N */
 		public char addrType;
 		/** The linkers label for mods */
 		public String linkerLabel;
 	}
 
 	/**
 	 * 
 	 * @param in
 	 *            the outputFile containing all the records
 	 * @param error errorhandler for constructor
 	 */
 	public LinkerModule(InputStream in, ErrorHandler error) {
 		// scan wrap
 		Scanner read = new Scanner(in);
 		ScanWrap reader = new ScanWrap(read, error);
 
 		//String used for name
 		String ender = "";
 
 		//Number of records
 		int mod = 0;
 		int link = 0;
 		int text = 0;
 
 		//value checking 
 		boolean isValid = true;
 		boolean add = true;
 
 		//checks for an H
 		String check = reader.readString(ScanWrap.notcolon, "loaderNoHeader");
 		if (!reader.go("disreguard"))
 			return;
 
 		//Runs through header record
 		if (check.equalsIgnoreCase("H")) {
 			this.progName = reader.readString(ScanWrap.notcolon, "loaderNoName");
 			if (!reader.go("disreguard"))
 				return;
 			this.loadAddr = reader.readInt(ScanWrap.hex4, "loaderHNoAddr", 16);
 			if (!reader.go("disreguard"))
 				return;
 			//error checking
 			isValid = OperandChecker.isValidMem(this.loadAddr);
 			if(!isValid){
 				error.reportError(makeError("invalidValue"),0,0);
 				return;
 			}
 			this.prgTotalLen = reader
 					.readInt(ScanWrap.hex4, "loaderHNoPrL", 16);
 			if (!reader.go("disreguard"))
 				return;
 			//error checking
 			isValid = OperandChecker.isValidMem(this.prgTotalLen);
 			if(!isValid){
 				error.reportError(makeError("invalidValue"),0,0);
 				return;
 			}
 			this.execStart = reader.readInt(ScanWrap.hex4, "loaderNoEXS", 16);
 			if (!reader.go("disreguard"))
 				return;
 			//error checking
 			isValid = OperandChecker.isValidMem(this.execStart);
 			if(!isValid){
 				error.reportError(makeError("invalidValue"),0,0);
 				return;
 			}
 			this.date = reader.readString(ScanWrap.datep, "loaderHNoDate");
 			if (!reader.go("disreguard"))
 				return;
 			this.version = reader.readInt(ScanWrap.dec4, "loaderHNoVer", 10);
 			if (!reader.go("disreguard"))
 				return;
 			reader.readString(ScanWrap.notcolon, "loaderHNoLLMM");
 			if (!reader.go("disreguard"))
 				return;
 			// some kind of error checking
 			ender = reader.readString(ScanWrap.notcolon, "loaderNoName");
 			if (!reader.go("disreguard"))
 				return;
 			if(!ender.equals(this.progName)){
 				error.reportWarning(makeError("noMatch"), 0, 0);
 			}
 		}else{
 			error.reportError(makeError("loaderNoHeader"),0,0); 
 			return;
 		}
 		//checks for L or T record
 		check = reader.readString(ScanWrap.notcolon, "");
 		if (!reader.go("disreguard"))
 			return;
 		//loops to get all the L and T records from object file
 		while (check.equals("L") || check.equals("T")) {
 			TextModRecord theRecordsForTextMod = new TextModRecord();
 			String entryLabel = "";
 			int entryAddr = 0;
 			//gets all information from linker record
 			if (check.equals("L")) {
 				link++;
 				entryLabel = reader.readString(ScanWrap.notcolon, "");
 				if (!reader.go("disreguard"))
 					return;
 				entryAddr = reader.readInt(ScanWrap.hex4, "loaderNoEXS",
 						16);
 				if (!reader.go("disreguard"))
 					return;
 				//error checking
 				isValid = OperandChecker.isValidMem(entryAddr);
 				if(!isValid){
 					error.reportError(makeError("invalidValue"),0,0);
 					return;
 				}
 				// some kind of error checking
 				ender = reader.readString(ScanWrap.notcolon, "loaderNoName");
 				if (!reader.go("disreguard"))
 					return;
 				if(!ender.equals(this.progName)){
 					error.reportWarning(makeError("noMatch"), 0, 0);
 				}
 				linkRecord.put(entryLabel, entryAddr);
 				check = reader.readString(ScanWrap.notcolon, "invalidRecord");
 				if (!reader.go("disreguard"))
 					return;
 				//gets all information out of Text record
 			} else if (check.equals("T")) {
 				text++;
 				theRecordsForTextMod.text.assignedLC = reader.readInt(ScanWrap.hex4, "textLC", 16);
 				if (!reader.go("disreguard"))
 					return;
 				//error checking
 				isValid = OperandChecker.isValidMem(theRecordsForTextMod.text.assignedLC);
 				if(!isValid){
 					error.reportError(makeError("invalidValue"),0,0);
 					return;
 				}
 				theRecordsForTextMod.text.instrData = reader.readString(ScanWrap.notcolon, "textData");
 				if (!reader.go("disreguard"))
 					return;
 
 				theRecordsForTextMod.text.flagHigh = reader.readString(ScanWrap.notcolon, "textStatus").charAt(0);
 				if (!reader.go("disreguard"))
 					return;
 				if(!(theRecordsForTextMod.text.flagHigh == 'A' || theRecordsForTextMod.text.flagHigh == 'R' || theRecordsForTextMod.text.flagHigh == 'E' || theRecordsForTextMod.text.flagHigh == 'C')){
 					error.reportError(makeError("modHLS"), 0, 0);
 					add = false;
 				}				
 				theRecordsForTextMod.text.flagLow = reader.readString(ScanWrap.notcolon, "textStatus")
 
 						.charAt(0);
 				if (!reader.go("disreguard"))
 					return;
 				if(!(theRecordsForTextMod.text.flagLow == 'A' || theRecordsForTextMod.text.flagLow == 'R' || theRecordsForTextMod.text.flagLow == 'E' || theRecordsForTextMod.text.flagLow == 'C')){
 					error.reportError(makeError("modHLS"), 0, 0);
 					add = false;
 				}
 				theRecordsForTextMod.text.modHigh = reader.readInt(ScanWrap.notcolon, "textMod", 16);
 				if (!reader.go("disreguard"))
 					return;
 				//check for mod high
 				if(theRecordsForTextMod.text.modHigh>16 || theRecordsForTextMod.text.modHigh<0)
 				{
 					error.reportError(makeError("invalidMods"),0,0);
 					return;
 				}
 				theRecordsForTextMod.text.modLow = reader.readInt(ScanWrap.notcolon, "textMod", 16);
 				if (!reader.go("disreguard"))
 					return;
 				//check for mod low
 				if(theRecordsForTextMod.text.modLow>16 || theRecordsForTextMod.text.modLow<0)
 				{
 					error.reportError(makeError("invalidMods"),0,0);
 					return;
 				}
 				// some kind of error checking
 				ender = reader.readString(ScanWrap.notcolon, "loaderNoName");
 				if (!reader.go("disreguard"))
 					return;
 				if(!ender.equals(this.progName)){
 					error.reportWarning(makeError("noMatch"), 0, 0);
 				}
 				check = reader.readString(ScanWrap.notcolon, "invalidRecord");
 				if (!reader.go("disreguard"))
 					return;
 				//gets all mod records for a text record
 				while (check.equals("M")) {
					MiddleMod midtemp = new MiddleMod();
 					ModRecord modification = new ModRecord();
 					mod++;
 					modification.hex = reader.readInt(ScanWrap.hex4, "modHex", 16);
 					if (!reader.go("disreguard"))
 						return;
 					//error checking
 					isValid = OperandChecker.isValidMem(modification.hex);
 					if(!isValid){
 						error.reportError(makeError("invalidValue"),0,0);
 						return;
 					}
 					boolean run = true;
 					String loop = "";
 					boolean firstRun = true;
 					while (run) {
 						if(firstRun){
 						midtemp.plusMin = reader.readString(ScanWrap.notcolon,
 								"modPm").charAt(0);
 						firstRun=false;
 						}else{
 						midtemp.plusMin = loop.charAt(0);
 						}
 						if (!reader.go("disreguard"))
 							return;
 						//error checking
 						isValid = OperandChecker.isValidPlusMin(midtemp.plusMin);
 						if(!isValid){
 							error.reportError(makeError("invalidPlus"),0,0);
 							return;
 						}
 						midtemp.addrType = reader.readString(ScanWrap.notcolon,
 								"modFlag").charAt(0);
 						if (!reader.go("disreguard"))
 							return;
 						if(!(midtemp.addrType == 'E' || midtemp.addrType == 'R' || midtemp.addrType == 'N')){
 							error.reportError(makeError("modFlag"), 0, 0);
 							add = false;
 						}
 						midtemp.linkerLabel = reader.readString(
 								ScanWrap.notcolon, "modLink");
 						if (!reader.go("disreguard"))
 							return;
 						loop = reader.readString(ScanWrap.notcolon, "modHLS");
 						if (!reader.go("disreguard"))
 							return;
 						if (loop.equals("")) {
 							run = false;
 						}
 						modification.midMod.add(midtemp);
 					}
 					loop = reader.readString(ScanWrap.notcolon, "modHLS");
 					if (!reader.go("disreguard"))
 						return;
 					modification.HLS = loop.charAt(0);
 					if(!(modification.HLS == 'H' || modification.HLS == 'L' || modification.HLS == 'S')){
 						error.reportError(makeError("modHLS"), 0, 0);
 						add = false;
 					}
 					// some kind of error checking
 					ender = reader.readString(ScanWrap.notcolon, "loaderNoName");
 					if (!reader.go("disreguard"))
 						return;
 					if(!ender.equals(this.progName)){
 						error.reportWarning(makeError("noMatch"), 0, 0);
 					}
 					theRecordsForTextMod.mods.add(modification);
 					check = reader.readString(ScanWrap.notcolon, "invalidRecord");
 					if (!reader.go("disreguard"))
 						return;
 				}// end of mod record
 				if(add){
 					textMod.add(theRecordsForTextMod);
 				}else{
 					add = true;
 				}
 			}// end of text record
 		}//end of while loop checking for linking records and text records
 
 		//checks for an end record
 		if (check.equals("E")) {
 			this.endRec = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			if (!reader.go("disreguard"))
 				return;
 			//error checking
 			isValid = OperandChecker.isValidMem(this.endRec);
 			if(!isValid){
 				error.reportError(makeError("invalidValue"),0,0);
 				return;
 			}
 			this.endLink = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			if (!reader.go("disreguard"))
 				return;
 			//error checking
 			isValid = OperandChecker.isValidMem(this.endLink);
 			if(!isValid){
 				error.reportError(makeError("invalidValue"),0,0);
 				return;
 			}
 			this.endText = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			if (!reader.go("disreguard"))
 				return;
 			//error checking
 			isValid = OperandChecker.isValidMem(this.endText);
 			if(!isValid){
 				error.reportError(makeError("invalidValue"),0,0);
 				return;
 			}
 			this.endMod = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			if (!reader.go("disreguard"))
 				return;
 			//error checking
 			isValid = OperandChecker.isValidMem(this.endMod);
 			if(!isValid){
 				error.reportError(makeError("invalidValue"),0,0);
 				return;
 			}
 			ender = reader.readString(ScanWrap.notcolon, "loaderNoName");
 			if (!reader.go("disreguard"))
 				return;
 			if(!ender.equals(this.progName)){
 				error.reportWarning(makeError("noMatch"), 0, 0);
 			}
 
 		}else{
 			error.reportError(makeError("loaderNoEnd"),0,0); 
 			return;
 		}
 
 		//warnings for amount of mod text and link records
 		if(link != this.endLink){
 			error.reportWarning(makeError("linkMatch"), 0, 0);
 		}else if(text != this.endText){
 			error.reportWarning(makeError("textMatch"), 0, 0);
 		}else if(mod != this.endMod){
 			error.reportWarning(makeError("modMatch"), 0, 0); 
 		}else if((link+text+mod+2) != this.endRec){
 			error.reportWarning(makeError("totalMatch"), 0, 0); 
 		}
 
 
 		//program ran successful. Checks for more in file
 		this.success = true;
 		if (read.hasNext()) {
 			this.done = false;
 		}
 	}
 	/**
 	 * Compares loadAddr of the LinkerModules
 	 */
 	@Override
 	public int compareTo(LinkerModule cmp) {
 		if(this.loadAddr > cmp.loadAddr){
 			return 1;
 		}else if(this.loadAddr < cmp.loadAddr){
 			return -1;
 		}else{
 			return 0;
 		}
 	}
 
 }
