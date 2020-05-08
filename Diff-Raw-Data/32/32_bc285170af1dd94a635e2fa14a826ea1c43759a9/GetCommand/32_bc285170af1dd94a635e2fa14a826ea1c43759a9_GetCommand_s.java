 package ui.command;
 
 import spreadsheet.Application;
import spreadsheet.NoSuchSpreadsheetException;
 import spreadsheet.Position;
 import ui.ErrorStream;
 import ui.SaveFile;
 
 public final class GetCommand extends Command {
 	
 	private Position pos;
 	
 	/**
 	 * Gets the expression at a giving Position.
 	 * @param arg1 column of Position 
	 * @param arg2 row of Expression
 	 */
 	public GetCommand(int arg1, int arg2) {
 		this.pos = new Position(arg1, arg2);
 	}
 	
 	 /**
 	  * Gets Expression on a given Position in current Worksheet!
 	  * Return message "Cyclic expression" if it goes in infinity loop.
 	  * catch NullPointerException -> No Expression found on Position.
 	  */
 	public void execute() {
 		String save = 
 		             String.format("get %s %s", pos.getColumn(), pos.getRow());
 		SaveFile.saveFile.add(save);
 		if (Application.instance.getWorksheet().getCyclic().isCyclic(this.pos))
 			System.out.println("Cyclic expression");
 		else {
 		try {
 			String str = 
 			           Application.instance.getWorksheet().get(pos).toString();
 			System.out.println(str);
 
 
 		}
 		catch (NullPointerException e) {
 			ErrorStream.instance.show(String.format("No Expression at %s %s" +
 					" -> "+ e.toString(), pos.getColumn(), pos.getRow()));
 		} 
 		}
 	}
 
 }
