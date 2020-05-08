 package simulanator;
 
 import static assemblernator.ErrorReporting.makeError;
 import guinator.GUIUtil;
 
 import java.io.InputStream;
 import java.util.Scanner;
 
 import ulutil.HTMLOutputStream;
 import ulutil.IOFormat;
 import assemblernator.Assembler;
 import assemblernator.ErrorReporting.ErrorHandler;
 
 /**
  * @author Josh Ventura
  * @date May 12, 2012; 4:53:16 PM
  */
 public class Simulator {
 	/**
 	 * @author Josh Ventura
 	 * @date May 12, 2012; 4:53:37 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param loaderFile
 	 *            Stream containing the loader file of the program to run.
 	 * @param hErr
 	 *            An error handler to which any problems in the load will be
 	 *            reported.
 	 * @param os
 	 *            The HTMLOutputStream to write info to.
 	 * @param machine
 	 *            The URBAN Machine into which memory is loaded.
 	 * @specRef LM1
 	 * @specRef LM1.1
 	 * @specRef LM1.2
 	 * @specRef LM1.3
 	 * @specRef LM1.4
 	 * @specRef LM1.5
 	 * @specRef LM1.6
 	 * @specRef LM1.7
 	 * @specRef LM1.8
 	 * @specRef LM1.9
 	 * @specRef LM1.10
 	 * @specRef LM1.11
 	 * @specRef LM1.12
 	 * @specRef LM1.13
 	 * @specRef LM1.14
 	 * @specRef LM1.15
 	 * @specRef LM1.16
 	 * @specRef LM1.17
 	 * @specRef LM1.18
 	 */
 	public static void load(InputStream loaderFile, ErrorHandler hErr,
 			HTMLOutputStream os, Machine machine) {
 		int loadAddr;
 		int execStart;
 		int progLen;
 		int asmVer;
 
 		try {
 			Scanner s = new Scanner(loaderFile);
 			String crcStr;
 			String progName;
 			String userReportLine = "";
 			String element;
 			ScanWrap rd = new ScanWrap(s, hErr);
 
 			element = rd.readString(ScanWrap.notcolon, "loaderNoHeader");
 
 			if (!rd.go(element.equals("H"))) // LM1.1, LM1.2
 				return;
 			// os.write("Reading header record...\n");
 			userReportLine = userReportLine + element + ":";
 
 			if (!rd.go(progName = rd.readString(ScanWrap.notcolon,
 					"loaderNoName"))) { // LM1.3
 				return;
 			}
 			userReportLine = userReportLine + progName + ":";
 
 			if (!IOFormat.isValidLabel(progName)) {
 				hErr.reportError(makeError("loaderInvName"), -1, -1);
 				// I guess we don't need to add to user report line? test before
 				// sticking with this. else look below.
 				// userReportLine = "\n" + userReportLine +
 				// makeError("loaderInvName") + "\n";
 				return;
 			}
 
 
 			if (!rd.go(loadAddr = rd
 					.readInt(ScanWrap.hex4, "loaderHNoAddr", 16))) // LM1.5
 				return;
 			userReportLine += loadAddr + ":";
 
 
 			if (!rd.go(execStart = rd.readInt(ScanWrap.hex4, "loaderNoEXS", 16))) // LM1.7
 				return;
 
 			if (!rd.go(progLen = rd.readInt(ScanWrap.hex4, "loaderHNoPrL", 16))) // LM1.9
 				return;
 
 			if (progLen > 4096) {
 				hErr.reportError(makeError("loaderPrLnOOR", "" + progLen), -1,
 						-1);
 				return;
 			}
 
 			if (!rd.go(rd.readString(ScanWrap.datep, "loaderHNoPrL"))) // LM1.11
 				return;
 			if (!rd.go(rd.readString(ScanWrap.notcolon, "loaderHNoLLMM")
 					.equals("URBAN-LLM"))) // LM1.13
 				return;
 
 			if (!rd.go(asmVer = rd.readInt(ScanWrap.dec4, "loaderHNoVer", 10))) // LM1.15
 				return;
 
 			if (!rd.go(crcStr = rd.readString(ScanWrap.notcolon, "loaderNoCRC"))) // LM1.17
 				return;
 
 			if (!crcStr.equals(progName)) { // LM1.17, continued
 				hErr.reportError(makeError("datafileCRCFail"), -1, -1);
 				return;
 			}
 
 			os.write("======== Program Name: " + progName + " ========");
 			os.write(userReportLine);
 			String recname;
 			int numRecords = 0;
 			for (;;) { // LM2
 				os.write("Reading record\n");
 				if (!rd.go(recname = rd.readString(ScanWrap.notcolon,
 						"loaderNoHeader"))) // LM1.1, LM1.2
 					return;
 
 				os.write("Reading Text Records...\n ");
 
 				if (!recname.equals("T")) // LM2.1
 					break;
 
 				os.write("Readin T - signifying text record..\n");
 				int addr; // LM2.3
 				if (!rd.go(addr = rd
 						.readInt(ScanWrap.hex6, "loaderTNoAddr", 16)))
 					return;
 
 				os.write("Reading the Program Assigned Location...\n");
 
 				if (addr > 4095)
 					hErr.reportError(
 							makeError("loaderTAddOOR",
 									Integer.toString(addr, 16)), -1, -1);
 
 				String words; // LM2.5
 				if (!rd.go(words = rd.readString(ScanWrap.hexArb,
 						"loaderTNoWord")))
 					return;
 
 				os.write(" Reading the Instruction...\n");
 
 				if (words.length() % 8 != 0)
 					hErr.reportError(makeError("loaderTNoWord")
 							+ " [Extra information: Invalid word size]", -1, -1);
 
 				for (int i = 0; i < words.length(); i += 8) {
 					machine.setMemory(addr++,
 							IOFormat.parseHex32Int(words.substring(i, i + 8)));
 				}
 
 				if (!rd.go(crcStr = rd.readString(ScanWrap.notcolon,
 						"loaderNoCRC"))) // LM2.7
 					return;
 
 				os.write("Reading the Program Name ...\n");
 				if (!crcStr.equals(progName)) { // LM2.7, continued
 					hErr.reportError(makeError("datafileCRCFail"), -1, -1);
 					return;
 				}
 				++numRecords;
 			}
 
 			if (!recname.equals("E")) { // LM3.1
				hErr.reportError(makeError("loaderNoEnd"), -1, -1);
 				return;
 			}
 
 			int predNR; // LM3.3
 			if (!rd.go(predNR = rd.readInt(ScanWrap.hex4, "loaderTNoAddr", 16)))
 				return;
 			os.write("Reading the Total Number of Records...\n");
 
 			if (predNR != numRecords + 2)
 				hErr.reportError(
 						makeError("loaderInvRecC", "" + (numRecords + 2), ""
 								+ predNR), -1, -1);
 
 			if (!rd.go(predNR = rd.readInt(ScanWrap.hex4, "loaderTNoAddr", 16))) // LM3.5
 				return;
 			os.write("reading th eTotal Number of Text Records...\n");
 
 			if (predNR != numRecords)
 				hErr.reportError(
 						makeError("loaderInvRecC", "" + numRecords, "" + predNR),
 						0, 0);
 
 			if (!rd.go(crcStr = rd.readString(ScanWrap.notcolon, "loaderNoCRC"))) // LM2.7
 				return;
 			os.write("Reading the Program Name...\n");
 
 
 			if (!crcStr.equals(progName)) { // LM2.7, continued
 				hErr.reportError(
 						makeError("datafileCRCFail") + ": " + progName, -1, -1);
 				os.write("Program name does not match previously specified label ...\n");
 				return;
 			}
 
 		} catch (Exception e) {
 			hErr.reportError(makeError("loaderReadFail"), -1, -1);
 			hErr.reportError(GUIUtil.getExceptionString(e), -1, -1);
 			os.write("Failed to load Loader File...\n");
 			return;
 		}
 
 		if (asmVer > Assembler.VERSION) {
 			machine.hErr.reportWarning(
 					makeError("newerAssemler", "" + Assembler.VERSION, ""
 							+ asmVer), -1, -1);
 			os.write(" WARNING: This program was compiled with a newer assembler version than you are using....\n");
 		}
 		machine.output.putString("Loaded file.");
 		machine.setLC(loadAddr);
 		machine.setLC(execStart);
 	}
 }
