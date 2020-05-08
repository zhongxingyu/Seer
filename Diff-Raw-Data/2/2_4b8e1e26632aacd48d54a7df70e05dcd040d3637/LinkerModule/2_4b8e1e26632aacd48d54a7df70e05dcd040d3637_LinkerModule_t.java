 package assemblernator;
 
 import static assemblernator.ErrorReporting.makeError;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.TreeMap;
 
 import assemblernator.ErrorReporting.ErrorHandler;
 
 import simulanator.ScanWrap;
 
 /**
  * 
  * @author Eric
  * @date May 12, 2012; 3:59:15 PM
  */
 public class LinkerModule {
 	/** Contains all the link records. */
 	List<LinkerRecord> linkRecord = new ArrayList<LinkerRecord>();
 	/** Contains all the mod and text records. */
 	Map<TextRecord, List<ModRecord>> textModRecord = new TreeMap<TextRecord, List<ModRecord>>();
 	/** Name of program. */
 	String prgname;
 	/** Program load address. */
 	int prgLoadadd;
 	/** Length of program. */
 	int prgTotalLen;
 	/** Start address of the program */
 	int prgStart;
 	/** Date the object file was made */
 	String date;
 	/** Version that was used to make outputfile */
 	int version;
 	/** Total number of records */
 	int endRec;
 	/** Total number of link records */
 	int endLink;
 	/** Total number of text records */
 	int endText;
 	/** Total number of mod records */
 	int endMod;
 	/** the offset amount from program address. */
 	int offset;
 	/** Success failure boolean */
 	boolean success = false;
 	/** End reached */
 	boolean done = true;
 
 	/**
 	 * @author Eric
 	 * @date May 13, 2012; 6:07:31 PM
 	 */
 	public static class TextRecord {
 		/** Program assigned LC for text record */
 		int assignedLC;
 		/** Assembled instruction */
 		String instrData;
 		/** Flag for high order bits */
 		char flagHigh;
 		/** Flag for low order bits */
 		char flagLow;
 		/** Number of modifications for high order bits */
 		int modHigh;
 		/** Number of modifications for low order bits */
 		int modLow;
 	}
 
 	/**
 	 * @author Eric
 	 * @date May 13, 2012; 6:07:53 PM
 	 */
 	public static class ModRecord {
 		/** 4 hex nybbles */
 		int hex;
 		/** H, L, or S */
 		char HLS;
 		/**
 		 * The middle of modifications records
 		 */
 		List<MiddleMod> midMod = new ArrayList<MiddleMod>();
 	}
 
 	public static class MiddleMod {
 		/** Plus or minus sign */
 		char plusMin;
 		/** Flag A or E */
 		char flagRE;
 		/** The linkers label for mods */
 		String linkerLabel;
 	}
 
 	/**
 	 * @author Eric
 	 * @date May 13, 2012; 6:08:12 PM
 	 */
 	public static class LinkerRecord {
 		/** Label of link */
 		String entryLabel;
 		/** Address of link */
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
 		MiddleMod midtemp = new MiddleMod();
 		LinkerRecord ltemp = new LinkerRecord();
 
 		// scan wrap
 		Scanner read = new Scanner(in);
 		//================================================================================================
 		ErrorHandler error = null; //NEEDS TO BE FIXED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		//================================================================================================
 		ScanWrap reader = new ScanWrap(read, error);

 		//checks for an H
 		String check = reader.readString(ScanWrap.notcolon, "loaderNoHeader");
 		if (!reader.go("disreguard"))
 			return;
 		//Runs through header record
 		if (check.equals("H")) {
 			this.prgname = reader.readString(ScanWrap.notcolon, "loaderNoName");
 			if (!reader.go("disreguard"))
 				return;
 			this.prgLoadadd = reader
 					.readInt(ScanWrap.hex4, "loaderHNoAddr", 16);
 			if (!reader.go("disreguard"))
 				return;
 			this.prgTotalLen = reader
 					.readInt(ScanWrap.hex4, "loaderHNoPrL", 16);
 			if (!reader.go("disreguard"))
 				return;
 			this.prgStart = reader.readInt(ScanWrap.hex4, "loaderNoEXS", 16);
 			if (!reader.go("disreguard"))
 				return;
 			this.date = reader.readString(ScanWrap.datep, "loaderHNoDate");
 			if (!reader.go("disreguard"))
 				return;
 			this.version = reader.readInt(ScanWrap.dec4, "loaderHNoVer", 10);
 			if (!reader.go("disreguard"))
 				return;
 			reader.readString(ScanWrap.notcolon, "loaderHNoLLMM");
 			if (!reader.go("disreguard"))
 				return;
 			// some kind of error checking====================================================================================
 			reader.readString(ScanWrap.notcolon, "loaderNoName");
 			if (!reader.go("disreguard"))
 				return;
 			//=================================================================================================================
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
 			//gets all information from linker record
 			if (check.equals("L")) {
 				ltemp.entryLabel = reader.readString(ScanWrap.notcolon, "");
 				if (!reader.go("disreguard"))
 					return;
 				ltemp.entryAddr = reader.readInt(ScanWrap.hex4, "loaderNoEXS",
 						16);
 				if (!reader.go("disreguard"))
 					return;
 				// some kind of error checking==========================================================================================
 				reader.readString(ScanWrap.notcolon, "loaderNoName");
 				if (!reader.go("disreguard"))
 					return;
 				//=====================================================================================================================
 				linkRecord.add(ltemp);
 				check = reader.readString(ScanWrap.notcolon, "invalidRecord");
 				if (!reader.go("disreguard"))
 					return;
 				//gets all information out of Text record
 			} else if (check.equals("T")) {
 				ttemp.assignedLC = reader.readInt(ScanWrap.hex4, "textLC", 16);
 				if (!reader.go("disreguard"))
 					return;
 				ttemp.instrData = reader.readString(ScanWrap.notcolon, "textData");
 				if (!reader.go("disreguard"))
 					return;
 				ttemp.flagHigh = reader.readString(ScanWrap.notcolon, "textStatus")
 						.charAt(0);
 				if (!reader.go("disreguard"))
 					return;
 				ttemp.flagLow = reader.readString(ScanWrap.notcolon, "textStatus")
 						.charAt(0);
 				if (!reader.go("disreguard"))
 					return;
 				ttemp.modHigh = reader.readInt(ScanWrap.notcolon, "textMod", 16);
 				if (!reader.go("disreguard"))
 					return;
 				ttemp.modLow = reader.readInt(ScanWrap.notcolon, "textMod", 16);
 				if (!reader.go("disreguard"))
 					return;
 				// some kind of error checking==========================================================================================
 				reader.readString(ScanWrap.notcolon, "loaderNoName");
 				if (!reader.go("disreguard"))
 					return;
 				//=======================================================================================================================
 				check = reader.readString(ScanWrap.notcolon, "invalidRecord");
 				if (!reader.go("disreguard"))
 					return;
 				//gets all mod records for a text record
 				while (check.equals("M")) {
 					mtemp.hex = reader.readInt(ScanWrap.hex4, "modHex", 16);
 					if (!reader.go("disreguard"))
 						return;
 					boolean run = true;
 					String loop = "";
 					while (run) {
 						midtemp.plusMin = reader.readString(ScanWrap.notcolon,
 								"modPm").charAt(0);
 						if (!reader.go("disreguard"))
 							return;
 						midtemp.flagRE = reader.readString(ScanWrap.notcolon,
 								"modFlag").charAt(0);
 						if (!reader.go("disreguard"))
 							return;
 						midtemp.linkerLabel = reader.readString(
 								ScanWrap.notcolon, "modLink");
 						if (!reader.go("disreguard"))
 							return;
 						loop = reader.readString(ScanWrap.notcolon, "modHLS");
 						if (!reader.go("disreguard"))
 							return;
 						if (loop.equals("H") || loop.equals("L")
 								|| loop.equals("S")) {
 							run = false;
 						}
 						mtemp.midMod.add(midtemp);
 					}
 					mtemp.HLS = loop.charAt(0);
 					// some kind of error checking========================================================================================
 					reader.readString(ScanWrap.notcolon, "loaderNoName");
 					if (!reader.go("disreguard"))
 						return;
 					//====================================================================================================================
 					completeMod.add(mtemp);
 					check = reader.readString(ScanWrap.notcolon, "invalidRecord");
 					if (!reader.go("disreguard"))
 						return;
 				}// end of mod record
 				textModRecord.put(ttemp, completeMod);
 			}// end of text record
 
 		}//end of while loop checking for linking records and text records
 
 		//checks for an end record
 		if (check.equals("E")) {
 			this.endRec = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			if (!reader.go("disreguard"))
 				return;
 			this.endLink = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			if (!reader.go("disreguard"))
 				return;
 			this.endText = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			if (!reader.go("disreguard"))
 				return;
 			this.endMod = reader.readInt(ScanWrap.hex4, "endRecords", 16);
 			if (!reader.go("disreguard"))
 				return;
 			// some kind of error checking================================================================================================
 			reader.readString(ScanWrap.notcolon, "loaderNoName");
 			if (!reader.go("disreguard"))
 				return;
 			//============================================================================================================================
 		}else{
 			error.reportError(makeError("loaderNoEnd"),0,0); 
 			return;
 		}
 		this.success = true;
 		if (read.hasNext()) {
 			this.done = false;
 		}
 	}
 
 }
