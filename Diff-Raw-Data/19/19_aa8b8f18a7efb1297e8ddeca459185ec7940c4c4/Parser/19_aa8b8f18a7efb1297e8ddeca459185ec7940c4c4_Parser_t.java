 package eel.seprphase2.TextInterface;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import eel.seprphase2.QuitGameException;
 import eel.seprphase2.Simulator.ControlException;
 import eel.seprphase2.Simulator.GameManager;
 import eel.seprphase2.Simulator.KeyNotFoundException;
 import eel.seprphase2.Simulator.PlantController;
 import eel.seprphase2.Utilities.Percentage;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  *
  * @author David
  */
 public class Parser {
 
     private PlantController controller;
     private GameManager manager;
     private TextRenderer renderer;
 
     Parser(PlantController controller, GameManager manager, TextRenderer renderer) {
         this.controller = controller;
         this.manager = manager;
         this.renderer = renderer;
     }
 
     /**
      * Execute a user command
      *
      * @param commandLine the textual command line to execute
      *
      * @throws DoNotStep when the interface should not step the reactor after a command
      */
     public void executeCommand(String commandLine) throws DoNotStep, QuitGameException {
         String[] words = commandLine.trim().split(" ");
 
         String command = words[0];
         ArgumentList arguments = new ArgumentList(command);
         for (int i = 1; i < words.length; i++) {
             arguments.add(words[i]);
         }
 
         try {
             parseCommand(command, arguments);
         } catch (ArgumentException e) {
             print("ERROR: " + e.getMessage());
             throw new DoNotStep();
         } catch (KeyNotFoundException e) {
             print(e.getMessage());
             throw new DoNotStep();
         } catch (ControlException e) {
             print(e.getMessage());
             throw new DoNotStep();
         }
     }
 
     private void parseCommand(String command, ArgumentList arguments)
             throws DoNotStep,
                    ArgumentException,
                    KeyNotFoundException,
                    ControlException,
                    QuitGameException {
 
         if (command.isEmpty()) {
             return;
         }
 
         if (command.equals("movecontrolrods")) {
             Percentage position = arguments.at(0).asPercentage();
             controller.moveControlRods(position);
             print("Moved control rods to " + position);
         } else if (command.equals("openvalve")) {
             int valveNumber = arguments.at(0).asPositiveInteger();
             controller.changeValveState(valveNumber, true);
             print("Opened valve " + valveNumber);
         } else if (command.equals("closevalve")) {
             int valveNumber = arguments.at(0).asPositiveInteger();
             controller.changeValveState(valveNumber, false);
             print("Closed valve " + valveNumber);
         } else if (command.equals("pumpon")) {
             int pumpNumber = arguments.at(0).asPositiveInteger();
             controller.changePumpState(pumpNumber, true);
             print("Turned on pump " + pumpNumber);
         } else if (command.equals("pumpoff")) {
             int pumpNumber = arguments.at(0).asPositiveInteger();
             controller.changePumpState(pumpNumber, false);
             print("Turned off pump " + pumpNumber);
         } else if (command.equals("repair")) {
             repair(arguments);
         } else if (command.equals("save")) {
             saveGame();
             throw new DoNotStep();
         } else if (command.equals("load")) {
             if (arguments.count() == 1) {
                 manager.loadGame(arguments.at(0).asPositiveInteger());
                 throw new DoNotStep();
             } else if (arguments.count() == 1) {
                 chooseGame();
                 throw new DoNotStep();
             }
         } else if (command.equals("help")) {
             showHelp();
             throw new DoNotStep();
         } else if (command.equals("diagram")) {
             showDiagram();
             throw new DoNotStep();
         } else if (command.equals("quit")) {
             throw new QuitGameException();
         } else {
             print("Error: Unknown command '" + command + "'");
             throw new DoNotStep();
         }
     }
 
     /**
      * Set the username
      * @param username
      */
     public void setUsername(String username) {
         manager.setUsername(username);
     }
 
     /**
      * Convenience function for user menus
      * @param action the input string
      * @return the integer representing the user's choice
      */
     public int chooseAction(String action) {
 
         try {
             return Integer.parseInt(action);
         } catch (NumberFormatException n) {
             return 0;
         }
     }
 
     private void chooseGame() throws NumberFormatException {
         print("Please choose a game to load and enter the following command:");
         print("load <game id>");
         int i = 0;
         for (String game : manager.listGames()) {
             String[] bits = game.split("\\.");
             Timestamp t = new Timestamp(Long.parseLong(bits[3]));
             Date d = new Date(t.getTime());
 
             SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
             print(String.format("[%d] Saved: %s", i++, date.format(d)));
         }
     }
 
     private void saveGame() throws DoNotStep {
         try {
             manager.saveGame();
             print("Game Saved!");
         } catch (JsonProcessingException err) {
             print("Unable to save file");
         }
     }
 
     private void repair(ArgumentList arguments)
             throws KeyNotFoundException,
                    ArgumentException,
                    ControlException {
         if (arguments.at(0).equals("pump")) {
             controller.repairPump(arguments.at(1).asPositiveInteger());
         } else if (arguments.at(0).equals("condenser")) {
             controller.repairCondenser();
         } else if (arguments.at(0).equals("turbine")) {
             controller.repairTurbine();
         } else {
             print("Invalid Component to repair");
         }
     }
 
     private void showHelp() {
         print("");
         showCommandHelp("movecontrolrods <Percentage>",
                         "Move the control rods in the reactor vessel to the position\n" +
                         "specified by <Percentage>, which is between 0 and 100%.\n" +
                         "\n" +
                         "A value of 100% means that the control rods are fully extracted\n" +
                         "(the reactor is producing maximum power);\n" +
                         "a value of 0% means that the control rods are fully inserted\n" +
                         "(the reactor is producing minimum power)");
         showCommandHelp("openvalve <ValveNumber>",
                         "Opens the valve specified with <ValveNumber>\n" +
                         "Refer to the diagram to see which valves are available.");
         showCommandHelp("closevalve <ValveNumber>",
                         "Closes the valve specified with <ValveNumber>\n" +
                         "Refer to the diagram to see which valves are available.");
         showCommandHelp("pumpon <PumpNumber>",
                         "Turns on the pump specified with <PumpNumber>\n" +
                         "Refer to the diagram to see which pumps are available.");
         showCommandHelp("pumpoff <PumpNumber>",
                         "Turns off the pump specified with <PumpNumber>\n" +
                         "Refer to the diagram to see which pumps are available.");
         showCommandHelp("repair pump <PumpNumber>",
                         "Repairs the pump specified with <PumpNumber>\n" +
                         "Refer to the diagram to see which pumps are available.");
         showCommandHelp("repair turbine",
                         "Repairs the turbine.");
         showCommandHelp("repair condenser",
                         "Repairs the condenser.");
         showCommandHelp("save", "Saves the game.");
         showCommandHelp("load", "Lists the games available to load.");
         showCommandHelp("load <GameNumber>",
                         "Load the game specified with <GameNumber>.\n" +
                         "Use 'load' with no arguments to get a list of games to load.");
         showCommandHelp("diagram", "Display a diagram of the reactor.");
         showCommandHelp("quit", "End this game and return to the top-level menu");
         showCommandHelp("help", "Display this help.");
        print("");
        print("Pressing 'Enter' on its own will step the simulation once.");
     }
 
     private void showDiagram() {
        AsciiArt.diagram(renderer);
     }
 
     private void print(String output) {
         renderer.outputLine(output);
     }
 
     private void showCommandHelp(String command, String documentation) {
         print(command);
         String[] lines = documentation.split("\n");
         for (String l : lines) {
             print("\t" + l);
         }
         print("");
     }
 }
