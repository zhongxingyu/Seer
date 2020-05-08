 package control;
 
 import instructions.Instruction;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Scanner;
 
 
 /**
  * Creates a map for all of the instructions in the user defined text file of
  * each instruction's keyword to an instance of the instruction. <br>
  * <br>
  * Creating the map requires an instruction index file. <br>
  * <br>
  * To change the instruction index file, change the value of the static field
  * INSTRUCTION_INDEX_FILE
  * 
  * @author Scott Valentine
  * 
  */
 public class InstructionMapFactory {
 
     /** Default Language */
     // TODO: make one of these (currently one here and one in view)
     public static final String ENGLISH = "English";
 
     /** Location of all instruction classpath data. */
     private static final String INSTRUCTION_INDEX_FILE =
             "/src/resources/instruction_index.txt";
 
     /** Default location of the resources package. */
     // TODO: make one of these (currently one here and one in view)
     private static final String DEFAULT_RESOURCE_PACKAGE = "resources.";
    private static final String PROPERTIES_SEPERATOR = ",";
 
     /**
      * Character that indicates a comment when places at beginning of line of
      * Instruction index file.
      */
     private static final char COMMENT_CHARACTER = '#';
 
     /** Resources for SLogo */
     // TODO: we currently have two of these (one for view and one here), want
     // only one
     private ResourceBundle myResources;
 
     /**
      * Instantiates the factory based on the language to be used for the
      * commands.
      * 
      * @param language of the commands (must be file in resource
      *        folder)
      */
     public InstructionMapFactory(String language) {
         try {
             myResources = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE
                                                    + language);
         } 
         catch (MissingResourceException e) {
             myResources = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE
                                                    + ENGLISH);
         }
     }
 
     /**
      * Constructor that creates new factory based on the resource bundle of
      * instruction keywords.
      * 
      * @param resources is a resource bundle that contains keywords for
      *        instructions
      */
     public InstructionMapFactory(ResourceBundle resources) {
         myResources = resources;
     }
 
     /**
      * Builds an instruction map that maps keyword strings to their
      * instructions.
      * 
      * @return Map of keywords to instructions.
      * @throws FileNotFoundException If the instruction_index file is not
      *         found.
      */
     public Map<String, Instruction> buildInstructionMap()
         throws FileNotFoundException {
 
         String currentDirectory = System.getProperty("user.dir");
 
         FileReader fileToBeRead =
                 new FileReader(currentDirectory + INSTRUCTION_INDEX_FILE);
         Scanner line = new Scanner(fileToBeRead);
 
         Map<String, Instruction> instructionMap =
                 new HashMap<String, Instruction>();
 
         while (line.hasNextLine()) {
             String nextLine = line.nextLine();
             parseLine(instructionMap, nextLine);
         }
         return instructionMap;
     }
 
     /**
      * Parses line of instruction and keyword and adds it to the instruction
      * map.
      * 
      * @param instructionMap is a map of keywords to instructions.
      * @param line is the current line being read.
      * @throws InstantiationException
      * @throws IllegalAccessException
      * @throws ClassNotFoundException
      */
     private void parseLine(Map<String, Instruction> instructionMap, String line) {
         if (line.charAt(0) != COMMENT_CHARACTER && line.length() > 0) {
             Class<?> instruction;
             try {
                 instruction = Class.forName(line);
             } 
             catch (ClassNotFoundException e1) {
                 return;
             }
             Instruction instruct;
             try {
                 instruct = (Instruction) instruction.newInstance();
             } 
             catch (InstantiationException e) {
                 // if not possible, skip
                 return;
             } 
             catch (IllegalAccessException e) {
                 // if not possible skip
                 return;
             }
             // gets parameters from line
             String className = getClassName(line);
 
             String entry = myResources.getString(className);
 
             String[] keywords = entry.split(PROPERTIES_SEPERATOR);
 
            for (int i = 1; i < keywords.length; ++i) {
                 instructionMap.put(keywords[i], instruct);
             }
         }
     }
 
     /**
      * Determines the name of the class from the class path.
      * 
      * @param classPath is the classPath to determine the class name.
      * @return The name of the class at the given classPath.
      */
     private String getClassName(String classPath) {
        String[] path = classPath.split(".");
        return path[path.length - 1];
     }
 }
