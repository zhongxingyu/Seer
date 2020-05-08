 package control;
 
 import exceptions.IllegalInstructionException;
 import instructions.BaseInstruction;
 import instructions.CompoundInstruction;
 import instructions.ConstantInstruction;
 import instructions.Instruction;
 import instructions.user_defined.VariableInstruction;
 import java.util.Scanner;
 
 
 /**
  * Converts a user inputed string into an Instruction that can execute on the model.
  * 
  * @author Scott Valentine
 * @author Ryan Fishelcake
  * @author Ellango Jothimurugesan
  * 
  */
 public class Parser {
 
     /** Indicator for the start of a list*/
     public static final String BEGINNING_OF_LIST = "[";
     
     /** Indicator for the end of a list*/
     public static final String END_OF_LIST = "]";
     
     /** Indicator for a user defined variable*/
     public static final String START_OF_VARIABLE = ":";
     private static final String ARGUMENT_ERROR_MESSAGE = "Incorrect number of arguments";
     private static final String LIST_ERROR_MESSAGE = "Incorrect bracket formatting";
     /** Environment contains the system functions and user defined variables/functions. */
     private Environment myEnvironment;
 
     /**
      * Creates new parser that uses an environment for to create new instructions from text.
      * 
      * @param environment contains information to convert text into relevant instructions.
      */
     public Parser (Environment environment) {
         myEnvironment = environment;
     }
 
     /**
      * Takes user input and converts it to either
      * <ol>
      * <li>CompoundInstruction to be executed on the model.</li>
      * <li>An exception to be displayed in the command history.</li>
      * </ol>
      * 
      * @param userInput - A string of data to be parsed into an instruction.
      * @return The instruction that represents the user input.
      * @throws IllegalInstructionException If the instruction is not mapped in the
      *         environment, throw this exception with argument of the incompatible string
      */
     public Instruction generateInstruction (String userInput) throws IllegalInstructionException {
         Preparser preparser = new Preparser(myEnvironment);
         String s = preparser.preParse(userInput);
         Scanner line = new Scanner(s);
         return generateInstruction(line);
     }
 
     /**
      * Takes user input and creates an executable instruction
      * <ol>
      * <li>CompoundInstruction to be executed on the model.</li>
      * <li>An exception to be displayed in the command history.</li>
      * </ol>
      * 
      * @param line - A scanner of data to be parsed into an instruction.
      * @return The instruction that represents the user input.
      * @throws IllegalInstructionException If the instruction is not mapped in the
      *         environment, throw this exception with argument of the incompatible string
      */
     private Instruction generateInstruction (Scanner line) throws IllegalInstructionException {
         CompoundInstruction resultInstruct = new CompoundInstruction();
         while (line.hasNext()) {
             String commandName = line.next();
 
             Instruction result;
             if (commandName.equals(BEGINNING_OF_LIST)) {
                 result = generateInstruction(new Scanner(unpackList(line)));
             }
             else if (commandName.startsWith(START_OF_VARIABLE)) {
                 result = new VariableInstruction(commandName);
             }
             else {
                 try {
                     result = new ConstantInstruction(Integer.parseInt(commandName));
                 }
                 catch (NumberFormatException e) {
                     BaseInstruction base = myEnvironment.getInstruction(commandName);
                     base.load(line, this);
                     result = base;
                 }
             }
             resultInstruct.add(result);
         }
         return resultInstruct;
     }
 
     /**
      * Parses through the next complete instruction. <br>
      * <br>
      * <u>Example:</u> <br>
      * If the input comes from the Scanner that represents the
      * string "fd 50 backward 50"
      * then
      * nextInstruction will return the instruction fd 50.
      * 
      * @param line is the data to be parsed into an instruction.
      * @return The next instruction from the scanner, line.
      * @throws IllegalInstructionException if instruction not recognized
      */
     public Instruction nextInstruction (Scanner line) throws IllegalInstructionException {
         if (!line.hasNext()) {
             throw new IllegalInstructionException(ARGUMENT_ERROR_MESSAGE);
         }
         String next = line.next();
         if (next.equals(BEGINNING_OF_LIST)) {
             next = unpackList(line);
         }
         return generateInstruction(new Scanner(next));
     }
 
     /**
      * Removes the outermost brackets from a list.
      * 
      * @param line is a scanner that iterates through the list.
      * @return String of the contents within the list
      * @throws IllegalInstructionException if list not constructed properly
      */
     public String unpackList (Scanner line) throws IllegalInstructionException {
         StringBuilder sb = new StringBuilder();
         int counterBracket = 1;
         while (counterBracket != 0) {
             if (!line.hasNext()) {
                 throw new IllegalInstructionException(LIST_ERROR_MESSAGE);
             }
             String str = line.next();
             counterBracket = updateCounterBracket(str, counterBracket);
             if (counterBracket != 0) {
                 sb.append(str);
                 sb.append(" ");
             }
         }
         return sb.toString();
     }
 
     /**
      * A helper method used for traversing lists.
      * It is used in the context of a loop to keep track of how many brackets
      * have been seen so far (left brackets count as positive, right brackets
      * are negative)
      * 
      * @param str is the current string being considered
      * @param counterBracket is the current number of brackets
      * @return the updated counterBracket
      */
     static int updateCounterBracket (String str, int counterBracket) {
         if (str.equals(BEGINNING_OF_LIST)) {
             return counterBracket + 1;
         }
         else if (str.equals(END_OF_LIST)) {
             return counterBracket - 1;
         }
         else {
             return counterBracket;
         }
     }
 }
