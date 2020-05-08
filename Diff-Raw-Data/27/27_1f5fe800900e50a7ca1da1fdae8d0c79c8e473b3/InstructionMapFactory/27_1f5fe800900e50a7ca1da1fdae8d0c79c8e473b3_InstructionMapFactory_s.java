 package control;
 
 import exceptions.CorruptedEnvironmentException;
 import instructions.BaseInstruction;
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
 
     /** Error Message to display when a class can not be instantiated from file*/
     private static final String ERROR_MESSAGE = "Missing instruction class names";
     
     /** Default Language */
     private static final String ENGLISH_LANGUAGE = "English";
 
     /** Location of all instruction classpath data. */
     private static final String INSTRUCTION_INDEX_FILE =
             "/src/resources/instruction_index.txt";
     
     private static final String SHAPE_INDEX_FILE = 
     		"/src/resources/images/TurtleShapes";
     
     /** Default location of the resources package. */
     private static final String USER_DIR = "user.dir";
     private static final String DEFAULT_RESOURCE_PACKAGE = "resources.";
     private static final String PROPERTIES_SEPERATOR = "[,]";
 
     /**
      * Character that indicates a comment when places at beginning of line of
      * Instruction index file.
      */
     private static final char COMMENT_CHARACTER = '#';
 
     /** Resources for SLogo */
     private ResourceBundle myResources;
 
     /**
      * Instantiates the factory based on the language to be used for the
      * commands.
      * 
      * @param language of the commands (must be file in resource
      *        folder)
      */
     public InstructionMapFactory (String language) {
         try {
             myResources = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE
                                                    + language);
         }
         catch (MissingResourceException e) {
             myResources = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE
                                                    + ENGLISH_LANGUAGE);
         }
     }
 
     /**
      * Constructor that creates new factory based on the resource bundle of
      * instruction keywords.
      * 
      * @param resources is a resource bundle that contains keywords for
      *        instructions
      */
     public InstructionMapFactory (ResourceBundle resources) {
         myResources = resources;
     }
 
     /**
      * Builds an instruction map that maps keyword strings to their
      * instructions.
      * 
      * @return Map of keywords to instructions.
      */
     public Map<String, BaseInstruction> buildInstructionMap () {
 
         String currentDirectory = System.getProperty(USER_DIR);
 
         FileReader fileToBeRead = null;
         try {
             fileToBeRead = new FileReader(currentDirectory + INSTRUCTION_INDEX_FILE);
         }
         catch (FileNotFoundException e) {
             throw new MissingResourceException(ERROR_MESSAGE, "", "");
         }
         Scanner line = new Scanner(fileToBeRead);
 
         Map<String, BaseInstruction> instructionMap =
                 new HashMap<String, BaseInstruction>();
 
         while (line.hasNextLine()) {
             String nextLine = line.nextLine();
             parseLine(instructionMap, nextLine);
         }
         line.close();
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
     private void parseLine (Map<String, BaseInstruction> instructionMap, String line) {
         if (line.charAt(0) != COMMENT_CHARACTER && line.length() > 0) {
 
             BaseInstruction instruct;
             try {
                 Class<?> instructionClass = Class.forName(line);
                 instruct = (BaseInstruction) instructionClass.newInstance();
             }
             catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
               throw new CorruptedEnvironmentException();
             }
 
             // gets parameters from line
             String className = getClassName(line);
 
             String entry = myResources.getString(className);
 
             String[] keywords = entry.split(PROPERTIES_SEPERATOR);
 
             for (String keyword : keywords) {
                 if (keyword.length() > 0) {
                     instructionMap.put(keyword, instruct);
                 }
             }
         }
     }
     
     /**
      * Unused right now.  Could be used to load in turtle pictures using reflection
      * @return
      * @throws CorruptedEnvironmentException
      */
     public Map<Integer, String> buildTurtleShapeMap() throws CorruptedEnvironmentException{
     	FileReader fileToBeRead = null;
     	String currentDirectory = System.getProperty(USER_DIR);
     	
     	 try {
              fileToBeRead = new FileReader(currentDirectory + SHAPE_INDEX_FILE);
          }
          catch (FileNotFoundException e) {
              throw new MissingResourceException(ERROR_MESSAGE, "", "");
          }
          Scanner line = new Scanner(fileToBeRead);
          
          Map<Integer, String> shapeMap = new HashMap<Integer, String>();
          while(line.hasNext()) {
         	 String str = line.nextLine();
         	 String [] shapes = str.split("=");
         	 try{
         		 shapeMap.put(Integer.parseInt(shapes[0].trim()),shapes[1].trim());
         	 }
         	 catch(NumberFormatException e) {
         		throw new CorruptedEnvironmentException();
         	 }
          }
          line.close();
          return shapeMap;
 
     }
 
     /**
      * Determines the name of the class from the class path.
      * 
      * @param classPath is the classPath to determine the class name.
      * @return The name of the class at the given classPath.
      */
     private String getClassName (String classPath) {
         String[] path = classPath.split("[.]");
         String str = path[path.length - 1];
         return str;
     }
 }
