 package eel.seprphase2.TextInterface;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import eel.seprphase2.QuitGameException;
 import eel.seprphase2.Simulator.GameManager;
 import eel.seprphase2.Simulator.PlantController;
 import eel.seprphase2.Simulator.PlantStatus;
 import eel.seprphase2.Utilities.Pressure;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  *
  * @author Marius
  */
 public class TextInterface {
 
     private PlantController plantController;
     private PlantStatus plantStatus;
     private GameManager gameManager;
     private TextRenderer textRenderer;
     private LineReader lineReader;
     private final Pressure condenserWarningPressure = new Pressure(25530000);
 
     /**
      *
      * @param plantController
      * @param plantStatus
      * @param textRenderer
      * @param lineReader
      */
     public TextInterface(PlantController plantController, PlantStatus plantStatus, GameManager gameManager,
                          TextRenderer textRenderer, LineReader lineReader) {
         this.plantController = plantController;
         this.plantStatus = plantStatus;
         this.gameManager = gameManager;
         this.textRenderer = textRenderer;
         this.lineReader = lineReader;
     }
 
     /**
      * Choose username on startup
      */
     public void askForUsername() {
         Parser parser = new Parser(plantController, gameManager, textRenderer);
         textRenderer.outputLine("Please Enter Username:");
         parser.setUsername(lineReader.readLine());
     }
 
     /**
      * Display the status of the reactor.
      */
     public void showStatus() {
         textRenderer.outputLine(String.format("%-21s : %16s    %-20s : %6s",
                                               "Control Rod Position", plantStatus.controlRodPosition(),
                                               "Reactor Water Level", plantStatus.reactorWaterLevel()));
         textRenderer.outputLine(String.format("%-21s : %16s    %-20s : %6s",
                                               "Reactor Temperature", plantStatus.reactorTemperature(),
                                               "Reactor Pressure", plantStatus.reactorPressure()));
         textRenderer.outputLine(String.format("%-21s : %16s    %-20s : %6s",
                                               "Condenser Temperature", plantStatus.condenserTemperature(),
                                               "Condenser Pressure", plantStatus.condenserPressure()));
         textRenderer.outputLine(String.format("%-21s : %16s",
                                               "Condenser Water Level", plantStatus.condenserWaterLevel()));
         textRenderer.outputLine(String.format("%-21s : %s",
                                              "Energy Generated", plantStatus.energyGenerated()));   
                 
         if (plantStatus.reactorWaterLevel().points() < plantStatus.reactorMinimumWaterLevel().points()) {
             textRenderer.outputLine("WARNING: REACTOR WATER LEVEL TOO LOW");
         }
 
         if (plantStatus.condenserPressure().greaterThan(condenserWarningPressure)) {
             textRenderer.outputLine("WARNING: CONDENSER PRESSURE TOO HIGH");
         }
 
         if (plantStatus.listFailedComponents().length > 0) {
             for (String failedComponent : plantStatus.listFailedComponents()) {
                 textRenderer.outputLine("WARNING: " + failedComponent + " HAS FAILED");
             }
 
         }
     }
 
     /**
      * Process a command from the terminal
      */
     public void processCommand() throws DoNotStep, QuitGameException {
         Parser parser = new Parser(plantController, gameManager, textRenderer);
 
         parser.executeCommand(lineReader.readLine());
     }
 
     /**
      * Display the title
      */
     public void showWelcomeMessage() {
         textRenderer.outputLine(
                 "_________ .__                              ___.          .__   \n" +
                 "\\_   ___ \\|  |__   ___________  ____   ____\\_ |__ ___.__.|  |  \n" +
                 "/    \\  \\/|  |  \\_/ __ \\_  __ \\/    \\ /  _ \\| __ <   |  ||  |  \n" +
                 "\\     \\___|   Y  \\  ___/|  | \\/   |  (  <_> ) \\_\\ \\___  ||  |__\n" +
                 " \\______  /___|  /\\___  >__|  |___|  /\\____/|___  / ____||____/\n" +
                 "        \\/     \\/     \\/           \\/           \\/\\/           \n" +
                 "                         _________        .__       .__        \n" +
                 "        A                \\_   ___ \\_______|__| _____|__| ______\n" +
                 "     TEAM EEL            /    \\  \\/\\_  __ \\  |/  ___/  |/  ___/\n" +
                 "    PRODUCTION           \\     \\____|  | \\/  |\\___ \\|  |\\___ \\ \n" +
                 "                          \\______  /|__|  |__/____  >__/____  >\n" +
                 "                                 \\/               \\/        \\/ \n");
     }
 
     /**
      * Choose outermost menu option.
      *
      * @return
      */
     public int askForAction() {
         Parser parser = new Parser(plantController, gameManager, textRenderer);
         textRenderer.outputLine("\n\nPlease choose an option and press enter: ");
         textRenderer.outputLine("\t[1] New Game ");
         textRenderer.outputLine("\t[2] Load Game");
         textRenderer.outputLine("\t[3] Quit\n\n\n");
 
         int result = parser.chooseAction(lineReader.readLine());
         while (result <= 0 || result >= 4) {
             textRenderer.outputLine("Please choose a valid option!");
             result = parser.chooseAction(lineReader.readLine());
         }
 
         return result;
     }
 
     /**
      * Display introductory help text
      */
     public void showIntroText() {
         textRenderer.outputLine("You are in control of the Chernobyl nuclear power plant.");
         textRenderer.outputLine("");
         textRenderer.outputLine("This is a diagram of the power plant:\n");
         AsciiArt.diagram(textRenderer);
         textRenderer.outputLine("Your job is to maximize power output and extract the maximum amount of energy\n" +
                                 "while preventing meltdown.\n\n" +
                                 "You can increase the power output by extracting the control rods\n" +
                                 "from the reactor, but be careful - you need to make sure that\n" +
                                 "none of the components overheats or explodes from excessive pressure.\n\n" +
                                 "Control the flow through the primary and secondary coolant loops\n" +
                                 "using the valves and pumps to keep the reactor operating.");
         textRenderer.outputLine("");
         textRenderer.outputLine("You can view the diagram above by typing 'diagram' and pressing Enter.");
         textRenderer.outputLine("For a full list of commands and more help, type 'help' and press Enter.");
         textRenderer.outputLine("");
         textRenderer.outputLine("The game is turn-based, so if you want to keep running the simulation\n" +
                                 "press enter to step.");
         textRenderer.outputLine("");
         textRenderer.outputLine("If you're unsure how to get started, try moving the control rods\n" +
                                 "out of the reactor partway (e.g. 'movecontrolrods 50')\n" +
                                 "and then stepping a few times by pressing 'Enter'");
         textRenderer.outputLine("");
         textRenderer.outputLine("Good Luck!");
         textRenderer.outputLine("");
     }
 
     /**
      * List save games to load on startup
      */
     public void showSavedGames() {
         Parser parser = new Parser(plantController, gameManager, textRenderer);
 
         textRenderer.outputLine("\fPlease enter a game number and press enter:");
         int i = 0;
         try {
             
             for (String game : gameManager.listGames()) {
                 String[] bits = game.split("\\.");
                 Timestamp t = new Timestamp(Long.parseLong(bits[3]));
                 Date d = new Date(t.getTime());
 
                 SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
                 textRenderer.outputLine(String.format("[%d] Saved: %s", i++, date.format(d)));
                 //renderer.outputLine(game);
             }
         } catch (Exception e) {
         }
 
         int result = parser.chooseAction(lineReader.readLine());
         while (result < 0 && result >i) {
             textRenderer.outputLine("Please choose a valid option!");
             result = parser.chooseAction(lineReader.readLine());
 
         }
         textRenderer.outputLine("\f");
 
         try
         {
             gameManager.loadGame(result);
         }
         catch(ArrayIndexOutOfBoundsException e)
         {
             textRenderer.outputLine("Invalid Choice");
             askForAction();
         }
 
     }
 }
