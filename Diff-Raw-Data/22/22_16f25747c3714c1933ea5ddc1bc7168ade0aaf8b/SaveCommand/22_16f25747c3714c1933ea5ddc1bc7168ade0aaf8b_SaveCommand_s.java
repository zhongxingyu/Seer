 package ui.command;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 import spreadsheet.Application;
 import spreadsheet.NoSuchSpreadsheetException;
 import spreadsheet.Position;
 import spreadsheet.Spreadsheet;
 
 public final class SaveCommand
 	extends Command {
 
 private Scanner scanner;
 
 /* Saves the input-commands saved in the saveVariables collection
  * to a file using the FileWriter object to create or overwrite the
  * file. Each command is printed to a new line in the file.
  * If an error occurs an IOException is thrown.
  */
 
 public SaveCommand(Scanner scanner) {
 	
 	this.scanner = scanner;
 }
 	@Override
 	public void execute() {
 	
 			
 			
 		
 		
 	
 		try {		
 			
 			FileWriter file = new FileWriter(scanner.next());
 			
 			String saveContent = "";
 			
			for (String s : Application.saveVariables) {
 				
				
				Spreadsheet ws = Application.instance.getSpreadsheet(s);
 				saveContent += "ns \n";
 				
 				for (int i=0; i<100;i++) {
 					for (int j=0; j<100; j++) {
 						
						if(ws.get(new Position(i,j)) != null) {
 							
							saveContent += "set " + i + " " + j + " " + ws.get(new Position(i,j));
 						
 					}
 					
 					
 						
 					}
 					
 					
 				}
 				
 				
 		
 		}
  
 			file.write(saveContent);
 			file.close();		
  
 			System.out.println("Saved successfully");			
 			
		} catch (IOException | NoSuchSpreadsheetException e) {
 			
 			System.out.println(e.getMessage());
 		}
 		
 		
 	}
 }
 
