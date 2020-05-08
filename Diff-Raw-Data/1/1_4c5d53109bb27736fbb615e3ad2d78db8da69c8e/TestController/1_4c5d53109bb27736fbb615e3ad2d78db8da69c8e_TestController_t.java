 package Hack.Controller;
 
 import javax.swing.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.Vector;
 import java.text.CharacterIterator;
 import java.text.StringCharacterIterator;
 import Hack.Utilities.*;
 import Hack.Events.*;
 import Hack.Gates.HDLException;
 
 public class TestController
  implements ControllerEventListener, ActionListener, ProgramEventListener {
 
     // NUMERIC FORMATS:
 
     /**
      * Decimal numeric format
      */
     public static final int DECIMAL_FORMAT = 0;
 
     /**
      * Hexadecimal numeric format
      */
     public static final int HEXA_FORMAT = 1;
 
     /**
      * Binary numeric format
      */
     public static final int BINARY_FORMAT = 2;
 
 
     // The default dir for loading script files
     private static final String INITIAL_SCRIPT_DIR = "scripts";
 
     // Minimum and maximum mili-seconds per script command execution
     private static final int MAX_MS = 2500;
     private static final int MIN_MS = 25;
 
     // Initial speed unit
     private static final int INITIAL_SPEED_UNIT = 3;
 
     // A helper string with spaces
     private static final String SPACES = "                                        ";
 
     // The file of the current script
     private File currentScriptFile;
 
     // The names of the output and comparison files
     private String currentOutputName;
     private String currentComparisonName;
 
     // The current speed unit.
     private int currentSpeedUnit;
 
     // The program counter
     private int currentCommandIndex;
 
     // The output desination
     private PrintWriter output;
 
     // The comparison source
     private BufferedReader comparisonFile;
 
     // Index of repeat or while start command
     private int loopCommandIndex;
 
     // Number of repeats left
     private int repeatCounter;
 
     // The condition of the current while loop.
     private ScriptCondition whileCondititon;
 
     // The current variable printing list
     private VariableFormat[] varList;
 
     // The current compared and output lines
     private int compareLinesCounter, outputLinesCounter;
 
     // times the fast forward process
     private Timer timer;
 
     // locked when single step in process
     protected boolean singleStepLocked;
 
 
     // True if the system is in fast forward.
     private boolean fastForwardRunning;
 
     // True if the system is in Single Step.
     private boolean singleStepRunning;
 
     // True if the script ended.
     private boolean scriptEnded;
     public boolean getScriptEnded(){ return scriptEnded; }
 
     // True if the program was halted.
     private boolean programHalted;
     public boolean getProgramHalted(){ return programHalted; }
 
     // true if the comparison failed at some point in the script
     private boolean comparisonFailed;
     public boolean getComparisonFailed(){ return comparisonFailed; }
 
     // The number of the line in which the comparison failed (if it failed).
     private int comparisonFailureLine;
     public int getComparisonFailureLine(){ return comparisonFailureLine; }
 
     public boolean isSuccess(){
         return scriptEnded && !programHalted && !comparisonFailed;
     }
 
     // The echo that was displayed (if any) when single step was stopped in the middle.
     private String lastEcho;
 
     public TestController() { }
 
     private Script init(HackSimulator simulator, File file){
         if (!file.exists())
             displayMessage(file.getPath() + " doesn't exist", true);
         try {
             return loadNewScript(file);
         } catch (ScriptException se) {
             displayMessage(se.getMessage(), true);
         } catch (ControllerException ce) {
             displayMessage(ce.getMessage(), true);
         }
         return null;
     }
 
     public boolean runScript(HackSimulator simulator, File scriptFile) {
         simulator.addListener(this);
         Script script = init(simulator, scriptFile);
         // Should throw exception
         if(script == null) return false;
 
         rewind(simulator);
 
         fastForwardRunning = true;
 
         while (fastForwardRunning)
              singleStep(simulator, script);
 
         return isSuccess();
     }
 
     // Restarts the current script from the beginning.
     private void rewind(HackSimulator simulator) {
         try {
             scriptEnded = false;
             programHalted = false;
 
             simulator.restart();
 
             if (output != null)
                 resetOutputFile();
             if (comparisonFile != null)
                 resetComparisonFile();
 
             lastEcho = "";
             currentCommandIndex = 0;
         } catch (ControllerException e) {
             displayMessage(e.getMessage(), true);
         }
     }
 
     // Puts the controller into stop mode
     private void stopMode() {
         if (fastForwardRunning) {
             fastForwardRunning = false;
         }
         singleStepRunning = false;
     }
 
     // Executes a single step from the script, checks for a breakpoint and
     // sets the status of the system accordingly.
     // Synchronized because of the notifyAll call.
     private void singleStep(HackSimulator simulator, Script script) {
 
         singleStepLocked = true;
 
         try {
             byte terminatorType;
             singleStepRunning = true;
 
             do {
                 terminatorType = miniStep(simulator, script);
             } while (terminatorType == Command.MINI_STEP_TERMINATOR && singleStepRunning);
 
             singleStepRunning = false;
 
             if (terminatorType == Command.STOP_TERMINATOR) {
                 displayMessage("Script reached a '!' terminator", false);
                 stopMode();
             }
         } catch (ControllerException ce) {
             stopWithError(ce);
         } catch (ProgramException pe) {
             stopWithError(pe);
         } catch (CommandException ce) {
             stopWithError(ce);
         } catch (VariableException ve) {
             stopWithError(ve);
         }
 
         singleStepLocked = false;
 //        notifyAll();
     }
 
     // Displays the message of the given exception and stops the script's execution.
     private void stopWithError(Exception e) {
         displayMessage(e.getMessage(), true);
         stopMode();
     }
 
     // Executes one command from the script and advances to the next.
     // Returns the command's terminator.
     private byte miniStep(HackSimulator simulator, Script script)
      throws ControllerException, ProgramException, CommandException, VariableException {
         Command command;
         boolean redo;
 
         do {
             command = script.getCommandAt(currentCommandIndex);
             redo = false;
 
             switch (command.getCode()) {
             case Command.SIMULATOR_COMMAND:
                 simulator.doCommand((String[])command.getArg());
                 break;
             case Command.OUTPUT_FILE_COMMAND:
                 doOutputFileCommand(command);
                 break;
             case Command.COMPARE_TO_COMMAND:
                 doCompareToCommand(command);
                 break;
             case Command.OUTPUT_LIST_COMMAND:
                 doOutputListCommand(command);
                 break;
             case Command.OUTPUT_COMMAND:
                 doOutputCommand(command, simulator);
                 break;
             case Command.ECHO_COMMAND:
                 doEchoCommand(command);
                 break;
             case Command.CLEAR_ECHO_COMMAND:
                 doClearEchoCommand(command);
                 break;
             case Command.BREAKPOINT_COMMAND:
                 break;
             case Command.CLEAR_BREAKPOINTS_COMMAND:
                 break;
             case Command.REPEAT_COMMAND:
                 repeatCounter = ((Integer)command.getArg()).intValue();
                 loopCommandIndex = currentCommandIndex + 1;
                 redo = true;
                 break;
             case Command.WHILE_COMMAND:
                 whileCondititon = (ScriptCondition)command.getArg();
                 loopCommandIndex = currentCommandIndex + 1;
                 if (!whileCondititon.compare(simulator)) {
                     // advance till the nearest end while command.
                     for (; script.getCommandAt(currentCommandIndex).getCode() !=
                            Command.END_WHILE_COMMAND; currentCommandIndex++);
         }
                 redo = true; // whether the test was successful or not,
                // the while command doesn't count
                 break;
             case Command.END_SCRIPT_COMMAND:
                 scriptEnded = true;
                 stopMode();
 
                 try {
                     if (output != null)
                         output.close();
 
                     if (comparisonFile != null) {
                         if (comparisonFailed)
                             displayMessage("End of script - Comparison failure at line "
                                                + comparisonFailureLine, true);
                         else
                             displayMessage("End of script - Comparison ended successfully",
                                                false);
 
                         comparisonFile.close();
                     }
                     else
                         displayMessage("End of script ", false);
                 } catch (IOException ioe) {
                     throw new ControllerException("Could not read comparison file");
                 }
 
                 break;
             }
 
             // advance script line pointer
             if (command.getCode() != Command.END_SCRIPT_COMMAND) {
                 currentCommandIndex++;
                 Command nextCommand = script.getCommandAt(currentCommandIndex);
                 if (nextCommand.getCode() == Command.END_REPEAT_COMMAND) {
                     if (repeatCounter == 0 || --repeatCounter > 0)
                         currentCommandIndex = loopCommandIndex;
                     else
                         currentCommandIndex++;
                 }
                 else if (nextCommand.getCode() == Command.END_WHILE_COMMAND) {
                     if (whileCondititon.compare(simulator))
                         currentCommandIndex = loopCommandIndex;
                     else
                         currentCommandIndex++;
                 }
             }
 
         } while (redo);
 
         return command.getTerminator();
     }
 
     // Executes the controller's output-file command.
     private void doOutputFileCommand(Command command) throws ControllerException {
         currentOutputName = currentScriptFile.getParent() + "/" + (String)command.getArg();
         resetOutputFile();
     }
 
     // Executes the controller's compare-to command.
     private void doCompareToCommand(Command command) throws ControllerException {
         currentComparisonName = currentScriptFile.getParent() + "/" + (String)command.getArg();
         resetComparisonFile();
     }
 
     // Executes the controller's output-list command.
     private void doOutputListCommand(Command command) throws ControllerException {
         if (output == null)
             throw new ControllerException("No output file specified");
 
         varList = (VariableFormat[])command.getArg();
         StringBuffer line = new StringBuffer("|");
 
         for (int i = 0; i < varList.length; i++) {
             int space = varList[i].padL + varList[i].padR + varList[i].len;
             String varName = varList[i].varName.length() > space ?
                              varList[i].varName.substring(0, space) : varList[i].varName;
             int leftSpace = (int)((space - varName.length()) / 2);
             int rightSpace = space - leftSpace - varName.length();
 
             line.append(SPACES.substring(0, leftSpace) + varName +
                         SPACES.substring(0, rightSpace) + '|');
         }
 
         outputAndCompare(line.toString());
     }
 
     // Executes the controller's output command.
     private void doOutputCommand(Command command, HackSimulator simulator) throws ControllerException, VariableException {
         if (output == null)
             throw new ControllerException("No output file specified");
 
         StringBuffer line = new StringBuffer("|");
 
         for (int i = 0; i < varList.length; i++) {
             // find value string (convert to require format if necessary)
             String value = simulator.getValue(varList[i].varName);
             if (varList[i].format != VariableFormat.STRING_FORMAT) {
                 int numValue;
                 try {
                     numValue = Integer.parseInt(value);
                 } catch (NumberFormatException nfe) {
                     throw new VariableException("Variable is not numeric", varList[i].varName);
                 }
                 if (varList[i].format == VariableFormat.HEX_FORMAT)
                     value = Conversions.decimalToHex(numValue, 4);
                 else if (varList[i].format == VariableFormat.BINARY_FORMAT)
                     value = Conversions.decimalToBinary(numValue, 16);
             }
 
             if (value.length() > varList[i].len)
                 value = value.substring(value.length() - varList[i].len);
 
             int leftSpace = varList[i].padL +
                             (varList[i].format == VariableFormat.STRING_FORMAT ?
                              0 : (varList[i].len - value.length()));
             int rightSpace = varList[i].padR +
                             (varList[i].format == VariableFormat.STRING_FORMAT ?
                              (varList[i].len - value.length()) : 0);
             line.append(SPACES.substring(0, leftSpace) + value +
                         SPACES.substring(0, rightSpace) + '|');
         }
 
         outputAndCompare(line.toString());
     }
 
     // Executes the controller's echo command.
     private void doEchoCommand(Command command) throws ControllerException {
         lastEcho = (String)command.getArg();
     }
 
     // Executes the controller's Clear-echo command.
     private void doClearEchoCommand(Command command) throws ControllerException {
         lastEcho = "";
     }
 
     // Compares an output line with a template line from a compare file.
     // The template must match exactly except for '*' which may match any
     // single character.
     private static boolean compareLineWithTemplate(String out, String cmp) {
         if (out.length() != cmp.length()) {
             return false;
         }
         StringCharacterIterator outi = new StringCharacterIterator(out);
         StringCharacterIterator cmpi = new StringCharacterIterator(cmp);
         for (outi.first(), cmpi.first();
              outi.current() != CharacterIterator.DONE;
              outi.next(), cmpi.next()) {
             if (cmpi.current() != '*' && outi.current() != cmpi.current()) {
                 return false;
             }
         }
         return true;
     }
 
     // Ouputs the given line into the output file and compares it to the current
     // compare file (if exists)
     private void outputAndCompare(String line) throws ControllerException {
         output.println(line);
         output.flush();
 
         outputLinesCounter++;
 
         if (comparisonFile != null) {
             try {
                 String compareLine = comparisonFile.readLine();
 
                 compareLinesCounter++;
 
                 if (!compareLineWithTemplate(line, compareLine)) {
                     comparisonFailed = true;
                     comparisonFailureLine = compareLinesCounter;
                     displayMessage("Comparison failure at line " + comparisonFailureLine,
                                        true);
                 }
             } catch (IOException ioe) {
                 throw new ControllerException("Could not read comparison file");
             }
         }
     }
 
     // loads the given script file and restarts the GUI.
     protected Script loadNewScript(File file)
      throws ControllerException, ScriptException {
         currentScriptFile = file;
         Script script = new Script(file.getPath());
         
         currentCommandIndex = 0;
         output = null;
         currentOutputName = "";
         comparisonFile = null;
         currentComparisonName = "";
 
         return script;
     }
 
     // Resets the output file.
     private void resetOutputFile() throws ControllerException {
         try {
             output = new PrintWriter(new FileWriter(currentOutputName));
             outputLinesCounter = 0;
         } catch (IOException ioe) {
             throw new ControllerException("Could not create output file " + currentOutputName);
         }
     }
 
     // Resets the comparison file.
     private void resetComparisonFile() throws ControllerException {
         try {
             comparisonFile = new BufferedReader(new FileReader(currentComparisonName));
             compareLinesCounter = 0;
             comparisonFailed = false;
         } catch (IOException ioe) {
             throw new ControllerException("Could not open comparison file " +
                                           currentComparisonName);
         }
     }
 
     // Displays the given message with the given type (error or not)
     private void displayMessage(String message, boolean error) {
         message = currentScriptFile.getName() + ": " + message;
         PrintStream s = (error ? System.err : System.out);
         s.println(message);
         s.flush(); // Manual flush to prevent intermixing of err and out stream
     }
 
     // Returns the version string
     private static String getVersionString() {
         return " (" + Definitions.version + ")";
     }
 
     public void actionPerformed(ActionEvent e) { }
 
     public void programChanged(ProgramEvent event) { }
 
     public void actionPerformed(ControllerEvent event) {
         try {
             switch (event.getAction()) {
                 case ControllerEvent.SINGLE_STEP:
                 case ControllerEvent.FAST_FORWARD:
                 case ControllerEvent.STOP:
                 case ControllerEvent.REWIND:
                 case ControllerEvent.SPEED_CHANGE:
                 case ControllerEvent.BREAKPOINTS_CHANGE:
                 case ControllerEvent.SCRIPT_CHANGE:
                 case ControllerEvent.ANIMATION_MODE_CHANGE:
                 case ControllerEvent.NUMERIC_FORMAT_CHANGE:
                 case ControllerEvent.ADDITIONAL_DISPLAY_CHANGE:
                 case ControllerEvent.DISABLE_ANIMATION_MODE_CHANGE:
                 case ControllerEvent.ENABLE_ANIMATION_MODE_CHANGE:
                 case ControllerEvent.DISABLE_SINGLE_STEP:
                 case ControllerEvent.ENABLE_SINGLE_STEP:
                 case ControllerEvent.DISABLE_FAST_FORWARD:
                 case ControllerEvent.ENABLE_FAST_FORWARD:
                 case ControllerEvent.LOAD_PROGRAM:
                 case ControllerEvent.HALT_PROGRAM:
                 case ControllerEvent.CONTINUE_PROGRAM:
                 case ControllerEvent.DISABLE_MOVEMENT:
                 case ControllerEvent.ENABLE_MOVEMENT:
                     break;
                 case ControllerEvent.DISPLAY_MESSAGE:
                     displayMessage((String)event.getData(), false);
                     break;
                 case ControllerEvent.DISPLAY_ERROR_MESSAGE:
                     displayMessage((String)event.getData(), true);
                     break;
                 default:
                     doUnknownAction(event.getAction(), event.getData());
                     break;
             }
         } catch (ControllerException e) {
             displayMessage(e.getMessage(), true);
             stopMode();
         }
     }
 
     /**
      * Executes an unknown controller action event.
      */
     protected void doUnknownAction(byte action, Object data) throws ControllerException {
     }
 }
