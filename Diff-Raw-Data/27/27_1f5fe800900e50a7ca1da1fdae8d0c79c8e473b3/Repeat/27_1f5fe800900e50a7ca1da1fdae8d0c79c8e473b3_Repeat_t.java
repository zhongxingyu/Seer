 package instructions.user_defined;
 
 import exceptions.IllegalInstructionException;
 import instructions.BaseInstruction;
import instructions.ConstantInstruction;
 import instructions.Instruction;
 import simulation.Model;
 
 
 /**
  * Represents a for loop of another instruction as an instruction. Takes two
  * arguments: how many times to loop, and the instruction to execute with every
  * loop. Note that the second argument (the instruction) must be contained in
  * brackets.<br>
  * <br>
  * <u> Examples:</u> <br>
  * <i>repeat 4 [ fd 50 rt 90 ]</i> ---> turtle goes forward 50 pixels and then
  * turns
  * right 90 degrees 4 times. This returns the value of the last instruction
  * executed (here rt 90). This example will draw a square. <br>
  * <i>repeat -4 [ fd 50 ]</i> ---> does nothing since -4 is less than 0. <br>
  * 
  * @author Scott Valentine
  * @author Ryan Fishel
  * @author Ellango Jothimurugesan
  * 
  */
 public class Repeat extends BaseInstruction {
 
     /**
      * Eclipse auto-generated ID to implement Serializable interface.
      */
     private static final long serialVersionUID = -6196556606163979681L;
     private static final int NUMBER_OF_ARGUMENTS = 2;
     private static final String VARIABLE_NAME = ":repcount";
     
     /**
      * Initializes user defined for-loop instruction.
      */
     public Repeat () {
         setNumberOfArguments(NUMBER_OF_ARGUMENTS);
     }
 
     @Override
     public int execute(Model model) throws IllegalInstructionException {
         int numberOfIterations = nextOperand().execute(model);
         Instruction commandsToLoop = nextOperand();
         int last = 0;
         for (int i = 1; i <= numberOfIterations; ++i) {
            BaseInstruction instruct = new ConstantInstruction(i);
            model.getEnvironment().addInstruction(VARIABLE_NAME, instruct);
             last = commandsToLoop.execute(model);
         }
         model.getEnvironment().removeInstruction(VARIABLE_NAME);
         return last;
     }
 }
