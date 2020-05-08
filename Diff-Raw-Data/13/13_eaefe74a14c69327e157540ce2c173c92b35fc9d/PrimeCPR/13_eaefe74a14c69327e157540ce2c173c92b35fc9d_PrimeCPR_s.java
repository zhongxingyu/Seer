 package edu.psu.iam.cpr.utility;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import edu.psu.iam.cpr.core.database.Database;
 
 public class PrimeCPR {
 	
 	protected Database db = new Database();
 
 	public static void main(String args[]) {
 
 		PrimeCPR primeCPR = new PrimeCPR();
 		if (! primeCPR.validateCommandLine(args)) {
 			System.exit(1);
 		}
 		
 		primeCPR.loadTables(args[0], args[1]);
 		
 		System.exit(0);
 	}
 		
 	private void loadTables(String primeFile, String primeDirectory) {
 		
 		BufferedReader bufferedReader = null;
 		try {
 			bufferedReader = new BufferedReader(new FileReader(primeFile));
 			String tableName = null;
 			while ((tableName = bufferedReader.readLine()) != null) {
 				System.out.print("Loading " + tableName + "...");
 				File f = new File(primeDirectory + System.getProperty("file.separator") + tableName);
 				if (! f.exists()) {
 					System.out.println("File does not exist " + f.toString());
 					System.exit(1);
 				}
 				else {
 					new BeanLoaderFactory().getLoaderInterface(tableName).loadTable(db, primeDirectory, tableName);
 					System.out.println("Complete!");
 				}
 			}
 			
 			System.out.print("Loading UseridPool (NOTE: this could take a while)...");
 			new UseridPoolLoader().loadTable(db, primeDirectory, "UseridPool");
 			System.out.println("Complete!");
 		}
 		catch (Exception e) {
 		}
 		finally {
 			try {
 				bufferedReader.close();
 			}
 			catch (Exception e) {
 			}
 		}
 	}
 	
 	private boolean validateCommandLine(String args[]) {
 		if (args.length != 2) {
 			System.out.println("Error: missing or too many command-line arguments!");
 			System.out.println("Usage: PrimeCPR data.order PRIME-Directory");
 			return false;
 		}
 		
 		File f = new File(args[0]);
 		if (! f.exists()) {
 			System.out.println("Error: the data file " + args[0] + " does not exist!");
 			return false;
 		}
 		
 		f = new File(args[1]);
 		if (! f.exists()) {
 			System.out.println("Error the PRIME directory " + args[1] + " does not exist!");
 			return false;
 		}
 		return true;
 	}
 
 }
