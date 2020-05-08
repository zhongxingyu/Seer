 package ui.command;
 
 import spreadsheet.Application;
 import java.util.Scanner;
 import spreadsheet.Position;
 import spreadsheet.Expression;
 import spreadsheet.NoSuchExpressionException;
 import spreadsheet.NoSuchPositionException;
 import spreadsheet.NoSuchSpreadsheetException;
 import ui.ExpressionInterpreter;
 
 public final class SetCommand
     extends Command {
     
   private int row;
   private int column;
   private Scanner scanner;
   private ExpressionInterpreter interpreter = new ExpressionInterpreter();
     
   public SetCommand (final Scanner scanner) {
     this.scanner = scanner;
   }
 
   public void execute() {
     row = scanner.nextInt();
     column = scanner.nextInt();
     Position position = new Position(row,column);
     
    /*
    String line = "set " + row + " " + column + " ";
    
    while (scanner.hasNext()) {    	  
  	  line = line + scanner.next();    	  
    }
    
   */
     
     try {
       Expression expression = interpreter.interpretExpression(scanner);
       Application.instance.getWorksheet().set(position,expression);
       System.out.println("Expression set successfully.");
       
      
     // Application.saveVariables.add(line);
      
       
     }
     catch (NoSuchExpressionException e) {
       System.out.println(e);
     }
     catch (NoSuchPositionException e) {
       System.out.println(e);
     }
     catch (NoSuchSpreadsheetException e) {
       System.out.println(e);
     }
   }
 
 }
