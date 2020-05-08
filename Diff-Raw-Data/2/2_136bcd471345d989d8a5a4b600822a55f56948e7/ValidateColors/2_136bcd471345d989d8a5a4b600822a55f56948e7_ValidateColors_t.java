 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.text.BadLocationException;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 
 public class ValidateColors {
 
 	private Sheet colorSheet;
 	private ArrayList<String> storeRightColorID = new ArrayList<String>();						//ColorIDs which begins with right letters
 	private ArrayList<String> storeWrongColorID = new ArrayList<String>();						//ColorIDs which begins with wrong letters
 	private ArrayList<String> storeDuplicateColorID = new ArrayList<String>();					//ColorIDs which are duplicates
 	private ArrayList<String> storeInvalidColorRGB = new ArrayList<String>();
 	private HTMLEditorKit kit;
 	private HTMLDocument doc;
 
 	public ValidateColors(Sheet sheet, HTMLEditorKit kit, HTMLDocument doc) {
 
 		this.colorSheet = sheet;
 		this.kit = kit;
 		this.doc = doc;
 
 		checkColorID();
 		checkRGB();
 
 		if (storeWrongColorID.isEmpty() == false
 				|| storeInvalidColorRGB.isEmpty() == false) {
 			printAllError();
 		}
 	}
 
 	// This function directly reads in the rows from the sheet and performs the
 	// specification checks. No data structures are used to store the data
 	public void checkColorID() {
 
 		boolean wrongColorID = false;
 		boolean foundDuplicate = false;
 		
 		for (int rowIndex = 4; rowIndex <= colorSheet.getLastRowNum(); rowIndex++) {
 			
 			wrongColorID = false;
 			foundDuplicate = false;
 
 			Row row = colorSheet.getRow(rowIndex);
 
 			String tempString = row.getCell(0).toString();
 
 			if (!tempString.equalsIgnoreCase("")) {
 
 				char firstChar = tempString.charAt(0);
 
 				// Ensure begins with 'C')
 				if (firstChar != 'C') {
 					wrongColorID = true;
 					storeWrongColorID.add("A"
 							+ Integer.toString(rowIndex + 1));
 				}
 				
 				if(wrongColorID == false){
 					
 					if(storeRightColorID.isEmpty() == true){
 						storeRightColorID.add(tempString);
 					}else{
 						
 						for(int count = 0; count < storeRightColorID.size(); count++){
 							
 							if(storeRightColorID.get(count).equalsIgnoreCase(tempString)){
 
 								storeDuplicateColorID.add("A" + Integer.toString(rowIndex + 1));
 								foundDuplicate = true;
 								break;
 								
 							} 
 						}
 						if(foundDuplicate == false){
 							storeRightColorID.add(tempString);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void checkRGB() {
 		
 		for (int rowIndex = 4; rowIndex <= colorSheet.getLastRowNum(); rowIndex++) {
 			
 			Row row = colorSheet.getRow(rowIndex);
 
 			String tempString = row.getCell(1).toString();
 
 			if (!tempString.equalsIgnoreCase("")) {
 
 				char firstChar = tempString.charAt(0);
 
 				// Ensure begins with '#', length of 7 and is Hexadecimal)
 				if (firstChar != '#' || tempString.length() != 7 || !tempString.substring(1, tempString.length()).matches("^[\\da-fA-F]+$")) {
 
 					storeInvalidColorRGB.add("B"
 							+ Integer.toString(rowIndex + 1));
 				}
 			}
 		}
 	}
 
 	public void printAllError() {
 
 		try {
 			
 			kit.insertHTML(doc, doc.getLength(), "<font size = 3> <font color=#0A23C4><br>Error Sheet: <font color=#ED0E3F><b>Colors</b></font color></font>", 0, 0, null);
 			kit.insertHTML(doc, doc.getLength(), "<font size = 3> <font color=#088542>-------------------------------------- </font color></font>", 0, 0, null);
 
 			if (storeWrongColorID.isEmpty() == false) {
 			
 				kit.insertHTML(doc, doc.getLength(), "<font size = 4> <font color=#0A23C4><b>-> </b><font size = 3>Color ID does not begin with 'C'</font color></font>", 0, 0,null);
 				kit.insertHTML(doc, doc.getLength(), "<font size = 3> <font color=#0A23C4>Cells: <font color=#ED0E3F>" + storeWrongColorID + "</font color></font>", 0, 0, null);
 
 			}
 			
 			if(storeDuplicateColorID.isEmpty() == false){
 				
 				kit.insertHTML(doc, doc.getLength(), "<font size = 4> <font color=#0A23C4><b>-> </b><font size = 3> Duplicate Color ID</font color></font>", 0, 0,null);
 				kit.insertHTML(doc, doc.getLength(), "<font size = 3> <font color=#0A23C4>Cells: <font color=#ED0E3F>" + storeDuplicateColorID + "</font color></font>", 0, 0, null);
 
 			}
 			
 			if (storeInvalidColorRGB.isEmpty() == false) {
 				
				kit.insertHTML(doc, doc.getLength(), "<font size = 4> <font color=#0A23C4><b>-> </b><font size = 3> sRGB is invalid (Rule 1: Begins with '#', Rule 2: 6 hexadecimal representation)</font color></font>", 0, 0,null);
 				kit.insertHTML(doc, doc.getLength(), "<font size = 3> <font color=#0A23C4>Cells: <font color=#ED0E3F>" + storeInvalidColorRGB + "</font color></font>", 0, 0, null);
 
 			}
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
