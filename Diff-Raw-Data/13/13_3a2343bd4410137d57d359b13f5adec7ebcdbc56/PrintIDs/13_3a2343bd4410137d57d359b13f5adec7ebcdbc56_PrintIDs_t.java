 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package CommandInterpreter;
 
 import Entries.Entry;
 import applicationLogic.EntryStorage;
 import textUI.IO;
 
 /**
  *
  * @author lassi
  */
 class PrintIDs extends Command {
 
     public PrintIDs(IO io) {
         super(io);
     }
 
     @Override
     public void run() {
         for (Entry entry : EntryStorage.getEntries()) {
            io.printLine(entry.getCitationKey());
        }
        io.printLineChange();
     } 
 }
