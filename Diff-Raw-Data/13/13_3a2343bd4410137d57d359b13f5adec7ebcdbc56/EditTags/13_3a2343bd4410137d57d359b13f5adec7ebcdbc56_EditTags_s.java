 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package CommandInterpreter;
 
 import Entries.Entry;
 import applicationLogic.EntryStorage;
 import java.util.HashMap;
 import java.util.Map;
 import textUI.IO;
 
 /**
  *
  * @author Lauri
  */
 public class EditTags extends Command {
 
     public EditTags(IO io) {
         super(io);
     }
 
     @Override
     public void run() {
         if (EntryStorage.getEntries().isEmpty()) {
             io.printLine("Muokattavia viitteitä ei ole. ");
             return;
         }
         io.printLine("Anna muokattavan viitteen viiteavain, tyhjä lopettaa");
         new PrintIDs(io).run();
         String citationKey = io.readPossiblyEmptyString();
         if (citationKey.equals("")) {
             return;
         }
 
         for (Entry entry : EntryStorage.getEntries()) {
             if (entry.getCitationKey().equals(citationKey)) {
                 io.printLine("Valitse toiminto");
                 io.printLine("1. Lisää tagi");
                 io.printLine("2. Poista tagi");
                 io.printLine("3. Tulosta viitteen tagit");
                 int command = io.readInt();
                 getCommand(entry, command).run();
                 return;
             }
         }
         io.printLine("Viiteavainta ei valitettavasti löytynyt");
     }
 
     private Command getCommand(Entry entry, int command) {
         Map<Integer, Command> tagCommands = new HashMap();
         tagCommands.put(1, new AddTag(io, entry));
         tagCommands.put(2, new DeleteTag(io, entry));
         tagCommands.put(3, new PrintTags(io, entry));
         return tagCommands.get(command);
     }
 }
