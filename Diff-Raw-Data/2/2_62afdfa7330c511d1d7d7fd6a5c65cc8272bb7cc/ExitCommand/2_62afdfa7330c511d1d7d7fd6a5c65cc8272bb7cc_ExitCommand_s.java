 package ui.command;
 
 import spreadsheet.Application;
 
 public final class ExitCommand
     extends Command {
 
   public void execute() {
    Application.instance.exit();
   }
 
 }
