 package UI;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.Scanner;
 
 import Config.Config.ModeType;
 import Config.Texts;
 import Controllers.Controller;
 import adapters.db.sqlite.upcMap.UPCEntry;
 import adapters.scanner.InventoryKeyboardInScannerAdapter;
 import adapters.scanner.ScannerAdapter;
 import adapters.scanner.UPCKeyboardInScannerAdapter;
 
 import commands.ExportParameters;
 import commands.ExportParameters.ExportType;
 
 public class GraphicUI implements UI {
 	private CommandPrompt prompt = null;
 	private boolean isRunning = false;
 	private Controller controller = null;
 	private boolean quiet = false;
 	
 	public GraphicUI(){
 		isRunning = false;
 		quiet = false;
 		
 		prompt = new CommandPrompt();
 		
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 prompt.showGUI();
             }
         });
 	}
 	
 	@Override
 	public void showMainScreen() {
 		StringBuffer sb = new StringBuffer();
 		if(Config.Config.Mode == ModeType.Dev)
 			sb.append(Texts.DEV_MODE_NOTIFICATION);
 		sb.append(Texts.WELCOME_MESSAGE);
 		
 		prompt.writeMessage(sb.toString());
 		isRunning = true;		
 	}
 
 	@Override
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	@Override
 	public void showHelp() {
 		prompt.writeMessage(Texts.HELP_MENU);		
 	}
 
 	@Override
 	public void stopRunning() {	
 		isRunning = false;
 		prompt.writeMessage(Texts.SHUTDOWN_MESSAGE);
 		prompt.close();
 		System.exit(0);		
 	}
 
 	@Override
 	public void commandNotFound(String c) {
 		prompt.writeMessage(Texts.COMMAND_NOT_FOUND(c));		
 	}
 
 	@Override
 	public void startScanMode() {
 		quiet = false;
 		prompt.writeMessage(Texts.START_SCANMODE);
 		scanModeUsage();
         
 		ScannerAdapter scanner = new InventoryKeyboardInScannerAdapter(this, controller);
 		try {
 			scanner.run();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		prompt.writeMessage(Texts.EXIT_SCANMODE);
 	}
 
 	@Override
 	public void scannedItem(UPCEntry upc) {
 		if(upc == null) {
 			prompt.writeMessage(Texts.INVALID_UPC);
 		}
 		else {
             if(quiet){
             	prompt.writeMessage("Added " + upc.getItemName() + "\n");
             } else {
             	prompt.writeMessage("Added: " + upc.toString() + "\n");
             }
 		}		
 	}
 
 	@Override
 	public String getScanModePrompt() {
 		if(quiet){
 			return Texts.PROMPT_SCAN_QUIET;
         } else {
         	return Texts.PROMPT_SCAN_VERBOSE;
         }
 	}
 	
 	@Override
 	public String getMainMenuPrompt(){
 		return Texts.PROMPT_CMD;
 	}
 
 	@Override
 	public ExportParameters getExportParameters() {
 		ExportParameters out = new ExportParameters();
 		DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
 
 		ExportType exportType = ExportType.Other;
 		
 		while(exportType == ExportType.Other){
 			prompt.writeMessage(Texts.EXPORT_PROMPT);
 			String type = prompt.getUserInput().trim().toLowerCase();
 			if (type.equals("file") || type.equals("f")) exportType = ExportType.File;
 			else if (type.equals("ui") || type.equals("u")) exportType = ExportType.UI;
 			else{
 				prompt.writeMessage(Texts.INVALID_EXPORT_TYPE);
 				exportType = ExportType.Other;
 			}
 		}
 				
 		out.type = exportType;
 		
 		while(out.startDate == 0) {
 			prompt.writeMessage(Texts.PROMPT_START_DATE);
 			String start = prompt.getUserInput();
 			try {
 				out.startDate = df.parse(start).getTime() / 1000;
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				prompt.writeMessage(Texts.INVALID_DATE_ERROR);
 			} 
 		}
 		
 		while(out.endDate == 0) {
 			prompt.writeMessage(Texts.PROMPT_END_DATE);
 			String end = prompt.getUserInput().toLowerCase().trim();;
 			if(end.equals("now")) {
 				out.endDate = (new java.util.Date()).getTime() / 1000;
 			}
 			else {
 				try {
 					out.endDate = df.parse(end).getTime() / 1000;
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					prompt.writeMessage(Texts.INVALID_DATE_ERROR);
 				} 
 			}
 		}
 		
 		return out;
 	}
 
 	@Override
 	public UPCEntry promptUnknonwnUPCEntry(String upc) {
         if(quiet){
         	prompt.writeMessage(Texts.PROMPT_NAME_QUIET);
         } else {
             prompt.writeMessage(Texts.PROMPT_NAME_VERBOSE);
         }
 		String itemName = prompt.getUserInput().trim();
 		
         if(quiet){
         	prompt.writeMessage(Texts.PROMPT_AMOUNT_QUIET);
         } else {
             prompt.writeMessage(Texts.PROMPT_AMOUNT_VERBOSE);
         }
         String itemAmount = prompt.getUserInput().trim();
 				
 		return new UPCEntry(upc, itemName, itemAmount);
 	}
 
     @Override
     public void setController(Controller c) {
         this.controller = c;
     }
 
     @Override
     public String getCommand(String p) {
     	prompt.writeMessage(p);
     	return prompt.getUserInput().trim().toLowerCase();
     }
 
     @Override
     public void scanModeUsage() {
         prompt.writeMessage(Texts.HELP_MENU_SCAN);
     }
 
     @Override
     public void toggleQuietMode() {
     	quiet = !quiet;
     }
 
     @Override
     public void promptNetworkQuery() {
         if(quiet){
         	prompt.writeMessage(Texts.IDLE_SEARCHING_QUIET);
         } else {
         	prompt.writeMessage(Texts.IDLE_SEARCHING_VERBOSE);
         }
     }
 
     @Override
     public void promptQuietMode() {
     	prompt.writeMessage(Texts.QUIET_MODE_NOTIFY);
     }
     public void promptClearingInventory() {
     	prompt.writeMessage(Texts.CLEARING_INV_ENTRIES);		
 	}
 
 	@Override
 	public void startModifyMode() {
 		prompt.writeMessage(Texts.START_MODIFYMODE);
 		editModeUsage();
         
 		ScannerAdapter scanner = new UPCKeyboardInScannerAdapter(this, controller);
 		try {
 			scanner.run();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		prompt.writeMessage(Texts.EXIT_MODIFYMODE);
 	}
 
 	@Override
 	public String getEditModePrompt() {
 		return Texts.EDITMODE_PROMPT;
 	}
 	
 	public void editModeUsage(){
 		prompt.writeMessage(Texts.EDITMODE_HELP);
 	}
 
 	@Override
 	public void promptEntryExists(String itemName, String amount) {
 		prompt.writeMessage("Item exists with the following information:");
 		prompt.writeMessage("Item name: " + itemName);
 		prompt.writeMessage("Item amount: "+ amount);		
 	}
 
 	@Override
 	public void listEntries(List<String> list) {
 		for(String s: list){
 			prompt.writeMessage(s + "\n");
 		}
 		
 	}
 
 	@Override
 	public void showMessage(String message) {
		if(!quiet){
 			prompt.writeMessage(message);
 		}
 	}
 }
