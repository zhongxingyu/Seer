 package assemblernator;
 
 import guinator.GUIMain;
 
 import java.io.File;
 import java.util.ArrayList;
 
 /**
  * @author Ratul Khosla, Eric Smith, Noah Torrance, Josh Ventura
  * 
  */
 
 /**
  * Main class to handle command line arguments and call subsystems.
  */
 public class Main {
 	/**
 	 * Main method; called at program start. Handles command line arguments.
 	 * 
 	 * @param args
 	 *            System-passed arguments to handle.
 	 */
 	public static void main(String[] args) {
 		String a[] = { System.getProperty("user.home") + "/Desktop/test.s" };
 		if (args.length < 1) {
 			System.out.println("URBAN Legend v" + Assembler.VERSION);
 			System.out.println("Usage: java -jar urban.jar file1 file2 file3... -o executablename");
			GUIMain.launch();
			return;
 		}
 
 		String outputFile = null;
 		ArrayList<String> filesToAssemble = new ArrayList<String>();
 
 		for (int i = 0; i < args.length; i++) {
 			if (args[i].length() < 1)
 				continue;
 			if (args[i].charAt(0) == '-') {
 				if (args[i].equals("-o")) {
 					if (++i > args.length) {
 						System.err.println("Expected output filename following -o");
 						System.exit(1);
 					}
 					outputFile = args[i];
 					continue;
 				}
 				if (args[i].equals("-v") || args[i].equals("--version")) {
 					System.out.println("URBAN Legend v" + Assembler.VERSION);
 					System.exit(0);
 				}
 				System.err.println("Unknown command " + args[i]);
 				System.exit(1);
 			}
 			filesToAssemble.add(args[i]);
 		}
 		if (filesToAssemble.size() == 0) {
 			System.err.println("URBAN Legend: No input files");
 		}
 		for (int i = 0; i < filesToAssemble.size(); i++) {
 			System.out.println("Assembling " + filesToAssemble.get(i));
 			
 			File f = new File(filesToAssemble.get(i));
 			if (f.canRead()) {
 				try {
 					Module aModule = Assembler.parseFile(f);
 					System.out.println("\n" + aModule.toString());
 					
 					
 				} catch (NullPointerException npe) {
 					System.err.println("Failed to read file `"
 							+ filesToAssemble.get(i)
 							+ "': Null pointer exception");
 					npe.printStackTrace();
 				}
 			}
 			else {
 				System.err.println("Cannot open file `"
 						+ filesToAssemble.get(i) + "'");
 				System.exit(1);
 			}
 		}
 	}
 }
