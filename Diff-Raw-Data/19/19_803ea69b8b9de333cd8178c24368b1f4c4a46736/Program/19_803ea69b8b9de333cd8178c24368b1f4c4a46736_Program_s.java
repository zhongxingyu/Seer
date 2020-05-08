 package suncertify.program;
 
 import suncertify.gui.BSJFrame;
 
 public class Program {
   private Mode mode;
 
   public Program(Mode mode) {
     this.mode = mode;
   }
 
   public static void main(String... args) {
     String mode = args.length > 0 ? args[0] : "";
     if (args.length == 0) {
      new Program(Mode.NETWORK_CLIENT_AND_GUI).runNetworkClientAndGUI();
     } else if ("server".endsWith(mode)) {
      new Program(Mode.SERVER).runServer();
     } else if ("alone".equals(mode)) {
      new Program(Mode.STANDALONE).runStandalone();
     }
   }
 
  private void runStandalone() {
    new BSJFrame(mode);
  }

  private void runServer() {
    new BSJFrame(mode);
  }

  private void runNetworkClientAndGUI() {
     new BSJFrame(mode);
   }
 }
