 package ui;
 
 import spreadsheet.Position;
 import spreadsheet.Range;
 import ui.exception.InvalidPosition;
 
 public class RangeInterpreter {
 
 	public RangeInterpreter() {
 		//singleton
 	}
 		
 	/**
 	 * Assumes text has a :
 	 * @param text of a range A0:A5
 	 * @return a range of those two position
 	 * @throws InvalidPosition 
 	 */
 	 public static Range interpret(final String text) throws InvalidPosition {
 		 final int indexOfColon = text.indexOf(':');
 		 Position posA = PositionInterpreter.interpret(text.substring(0, indexOfColon));
		 Position posB = PositionInterpreter.interpret(text.substring(indexOfColon));
 		return new Range(posA, posB);
 		  
 	 }
 
 
 }
