 /*
  */
 package interiores;
 
 import interiores.core.Application;
 import interiores.core.Debug;
 import interiores.core.presentation.SwingController;
 import interiores.core.presentation.TerminalController;
 import interiores.data.MappedDataController;
 
 /**
  *
  * @author hector
  */
 public class Interiores
 {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws Exception
     {
         Debug.enable();
         
         TerminalController terminal = new TerminalController("interiores.presentation.terminal");
         terminal.addShortcut("furnitureType", "ft");
         terminal.addShortcut("furnitureTypesCatalog", "ftc");
         terminal.addShortcut("roomTypesCatalog", "rtc");
         
         Application app = new Application("interiores");
                 
         app.setDataController(new MappedDataController());
         
         app.addPresentation(new SwingController("interiores.presentation.swing.views"));
         app.addPresentation(terminal);
         
         app.addBusiness("room");
        app.addBusiness("constraint");
         app.addBusiness("furnitureType");
         app.addBusiness("furnitureTypesCatalog");
         app.addBusiness("roomTypesCatalog");
         
         app.init();
     }
 }
