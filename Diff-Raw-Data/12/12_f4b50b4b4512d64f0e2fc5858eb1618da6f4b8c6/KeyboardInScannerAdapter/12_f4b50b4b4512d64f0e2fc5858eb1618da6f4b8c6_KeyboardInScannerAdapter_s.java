 package adapters.scanner;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 import UI.UI;
 import adapters.db.sqlite.inventory.InventoryDAO;
 import adapters.db.sqlite.inventory.InventoryEntry;
 import adapters.db.sqlite.upcMap.UPCDAO;
 import adapters.db.sqlite.upcMap.UPCEntry;
 import au.com.bytecode.opencsv.CSVParser;
 
 
 
 public class KeyboardInScannerAdapter implements ScannerAdapter {
 	public final static String queryURL = "http://www.searchupc.com/handlers/upcsearch.ashx?request_type=1&access_token=1193424F-517B-4D65-95BA-DD4CDE915945&upc=";
 	private Scanner scanner;
 	private UI ui;
 	
 	public KeyboardInScannerAdapter(UI ui, InputStream in) {
 		scanner = new Scanner(in);
 		this.ui = ui;
 	}
 
 	@Override
 	public void run() throws ClassNotFoundException, SQLException, Exception {
 		// todo refactor this to singleton pattern
 		UPCDAO upcDAO = UPCDAO.getInstance();
 		ui.scanModePrompt();
 		while(scanner.hasNext()) {
 			String padded = scanner.next().trim();
 			if(padded.equals("s") || padded.equals("stop"))
 				break;
 			
 			String next = padded.replaceFirst("^0+(?!$)", "");
 
 			if(!Pattern.matches("^[0-9]*$", next)) {
 				ui.scannedItem(null);
 			}
 			else {			
 				UPCEntry upc = upcDAO.lookUp(next);
 				if(upc == null) {
 					//secondary lookup, i.e. query cloud
 					System.out.println("Not found in local DB, hitting cloud...");
 					try {
 						URL cloudDB = new URL(queryURL + padded);
 						BufferedReader in = new BufferedReader(new InputStreamReader(cloudDB.openStream()));
 						
 						ArrayList<String> response = new ArrayList<String>();
 						
 						String inputLine;
 						
 						while ((inputLine = in.readLine()) != null) {
 							response.add(inputLine);
 						}
 						
 						in.close();
 						
 						if(response.size() > 1) {
 							String entry = response.get(1);
 							CSVParser parser = new CSVParser();
 							String[] parsedEntry = parser.parseLine(entry);
 							upc = new UPCEntry(next, parsedEntry[0], "EACH");
 							upcDAO.addEntry(upc, "online");
 						}
 					}
 					catch(Exception e) {
 						upc = null;
 						e.printStackTrace();
 					}
 					
 				}
 				
 				if(upc == null) {
 					//third stage lookup, prompt user for info
 					upc = ui.promptUnknonwnUPCEntry(next);
 					upcDAO.addEntry(upc, "user");
 				}
 				
 				// Get data from db, guaranteed to be there now
 				upc = upcDAO.lookUp(upc.getUPC()); 
 				
 				
 				InventoryDAO inventory = InventoryDAO.getInstance();
				InventoryEntry scannedEntry = new InventoryEntry(upc);
 				
 				inventory.addEntry(scannedEntry);
 				
 				ui.scannedItem(upc);
 			}
 			ui.scanModePrompt();
 		}
 	}
 }
