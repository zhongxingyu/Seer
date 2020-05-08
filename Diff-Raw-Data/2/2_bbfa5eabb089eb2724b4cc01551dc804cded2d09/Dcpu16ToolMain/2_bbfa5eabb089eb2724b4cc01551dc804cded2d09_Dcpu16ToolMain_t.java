 package dcpu16;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 
 import dcpu16.assembler.Dcpu16Assembler;
 import dcpu16.disassembler.Dcpu16Disassembler;
 
 public class Dcpu16ToolMain {
 
 	private static final String EXTENTION_ASSEMBLY = ".dasm";
 	private static final String EXTENTION_BINARY = ".bin";
 	private static final String EXTENTION_DISASSEMBLY = ".disassembled.dasm";
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if(args.length != 1) {
 			System.err.println("Invalid number of arguments expected 1, got "+ args.length +".");
 			printHelp();
 			return;
 		}
 		
 		String inputFilename = args[0];
		File inputFile = new File(inputFilename);
 		
 		if(! inputFile.exists()) {
 			System.err.println("Input file '"+ inputFilename +"' not found.");
 			printHelp();
 			return;
 		}
 		
 		if(inputFilename.endsWith(EXTENTION_ASSEMBLY)) {
 			String outputFilename = inputFilename.substring(0, inputFilename.length() - EXTENTION_ASSEMBLY.length());
 			outputFilename += EXTENTION_BINARY;
 			assembleFile(inputFilename, outputFilename);
 		}
 		else if(inputFilename.endsWith(EXTENTION_BINARY)) {
 			String outputFilename = inputFilename.substring(0, inputFilename.length() - EXTENTION_BINARY.length());
 			outputFilename += EXTENTION_DISASSEMBLY;
 			disassembleFile(inputFilename, outputFilename);
 		}
 		else {
 			System.err.println("Unknown file extenstion");
 			printHelp();
 		}
 	}
 	
 	private static void assembleFile(String inputFilename, String outputFilename) {
 		Dcpu16Assembler assembler = new Dcpu16Assembler();
 		FileOutputStream outputStream = null;;
 		try {
 			byte[] outputBytes = assembler.assembleFile(inputFilename);
 			File outputFile = new File(outputFilename);
 			outputStream = new FileOutputStream(outputFile);
 			outputStream.write(outputBytes);
 		}
 		catch(Exception e) {
 			e.printStackTrace(System.err);
 		}
 		finally {
 			try {
 				outputStream.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private static void disassembleFile(String inputFilename, String outputFilename) {
 		Dcpu16Disassembler disassembler = new Dcpu16Disassembler();
 		FileWriter outputStream = null;
 		try {
 			String disassembledData = disassembler.disassembleFile(inputFilename);
 			File outputFile = new File(outputFilename);
 			outputStream = new FileWriter(outputFile);
 			outputStream.write(disassembledData);
 		}
 		catch(Exception e) {
 			e.printStackTrace(System.err);
 		}
 		finally {
 			try {
 				outputStream.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private static void printHelp() {
 		System.err.println();
 		System.err.println("Usage: java -jar Dcpu16Tool.jar <filename>");
 		System.err.println("  - files with '"+ EXTENTION_ASSEMBLY +"' extension will be assembled.");
 		System.err.println("  - files with '"+ EXTENTION_BINARY +"' extension will be disassembled (not implemented).");
 	}
 }
