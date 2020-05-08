 package UI;
 
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Scanner;
 
 import adapters.db.sqlite.upcMap.UPCEntry;
 import adapters.scanner.KeyboardInScannerAdapter;
 import adapters.scanner.ScannerAdapter;
 
 import commands.Command;
 import commands.CommandNotFoundCommand;
 import commands.ExportCommand;
 import commands.QuitCommand;
 import commands.ShowHelpCommand;
 import commands.StartScannerCommand;
 
 
 public class ConsoleUI implements UI {
 	private boolean isRunning;
 	
 	public ConsoleUI() {
 		isRunning = false;
 	}
 	
 	@Override
 	public void showMainScreen() {
 		System.out.println("Welcome to Kitchen Inventory Tracker");
 		System.out.println("Please note that you can enter 'h' or 'help' at the prompt to get a full list of commands");
 		isRunning = true;
 	}
 
 	@Override
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	@Override
 	public Command getCommand() {
 		System.out.print("Enter command> ");
 		Scanner scanner = new Scanner(System.in);
 		String command = scanner.next();
 		Command cmd;
 		if(command.equals("scan") || command.equals("s")) {
 			cmd = new StartScannerCommand(this);
 		}
 		else if(command.equals("help") || command.equals("h")) {
 			cmd = new ShowHelpCommand(this);
 		}
 		else if(command.equals("export") || command.equals("e")) {
 			cmd = new ExportCommand(this);
 		}
 		else if(command.equals("quit") || command.equals("q")) {
 			cmd = new QuitCommand(this);
 		}
 		else {
 			cmd = new CommandNotFoundCommand(this, command);
 		}
 		return cmd;
 	}
 
 	@Override
 	public void showHelp() {
 		System.out.println("\r\nEnter one of the following commands (or shortcut):\r\n" + 
 				"scan (s) - starts scanning\r\n" + 
 				"quit (q) - quits the program\r\n" + 
 				"help (h) - shows this help\r\n" +
 				"export (e) - starts the export list process");
 		
 	}
 
 	@Override
 	public void stopRunning() {
 		System.out.println("Shutting down...");
 		isRunning = false;
 	}
 
 	@Override
 	public void commandNotFound(String c) {
 		System.out.println("Command '" + c + "' not found. Enter 'h' for help.");
 		
 	}
 
 	@Override
 	public void startScanMode() {
 		System.out.println("Starting scan mode...");
 		System.out.println("Please start scanning your items. Type 'stop' or 's' to stop scanning and return to the main screen.");
 		
 		ScannerAdapter scanner = new KeyboardInScannerAdapter(this, System.in);
 		try {
 			scanner.run();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("Exiting scan mode...");
 	}
 
 	@Override
 	public void scannedItem(UPCEntry upc) {
 		if(upc == null) {
 			System.out.println("Invalid UPC");
 		}
 		else {
 
 			System.out.println("Added: " + upc.toString());
 		}
 	}
 	
 	public void scanModePrompt() {
 		System.out.print("Scan Mode ('s' to stop scanning)> ");
 	}
 
 	@Override
 	public long[] getExportTime() {
 		long[] out = new long[2];
 		DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
 		Scanner scanner = new Scanner(System.in);
 
 		while(out[0] == 0) {
 			System.out.print("Enter start date (MM-DD-YYYY)>");
 			String start = scanner.next();
 			try {
 				out[0] = df.parse(start).getTime() / 1000;
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				System.out.println("Invalid date format");
 			} 
 		}
 		
 		while(out[1] == 0) {
 			System.out.print("Enter end date or 'now' (MM-DD-YYYY))>");
 			String end = scanner.next();
 			if(end.equals("now")) {
 				out[1] = (new java.util.Date()).getTime() / 1000;
 			}
 			else {
 				try {
 					out[0] = df.parse(end).getTime() / 1000;
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					System.out.println("Invalid date format");
 				} 
 			}
 		}
 		
 		return out;
 	}
 
 	@Override
 	public UPCEntry promptUnknonwnUPCEntry(String upc) {
 		Scanner scanner = new Scanner(System.in);
 		
 		System.out.println("Unknown UPC code.\nPlease enter the name of the item:");
 		String itemName = scanner.nextLine().trim();
 		
		System.out.println("What's the amount of the item?");
 		String itemAmount = scanner.nextLine().trim();
 				
 		return new UPCEntry(upc, itemName, itemAmount);
 	}
 
 }
