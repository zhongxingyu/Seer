 package instructions.user_defined;
 
 import control.Environment;
 import simulation.Model;
 import exceptions.IllegalInstructionException;
 import instructions.BaseInstruction;
 import instructions.CompoundInstruction;
import instructions.ConstantInstruction;
 import instructions.Instruction;
 
 public class DoTimes extends BaseInstruction {
 
     /**
      * Eclipse auto-generated ID to implement Serializable interface.
      */
     private static final long serialVersionUID = 7665168144376932102L;
     private static final int NUMBER_OF_ARGUMENTS = 2;
     
     private static final int VARIABLE_NAME_INDEX = 0;
     private static final int END_INDEX = 1;
     
     /**
      * Initializes user defined do-times instruction.
      */
     public DoTimes () {
         setNumberOfArguments(NUMBER_OF_ARGUMENTS);
     }
 
 
     @Override
     public int execute (Model model) throws IllegalInstructionException {
         CompoundInstruction initialization = (CompoundInstruction) nextOperand();
         Instruction commandsToExecute = nextOperand();
         Instruction var = initialization.getInstruction(VARIABLE_NAME_INDEX);
        String variableName = ((VariableInstruction) var).toString();
         int end = initialization.getInstruction(END_INDEX).execute(model);
 
         Environment environment = model.getEnvironment();
         int last = 0;
        for (int i = 0; i < end; i++) {
            BaseInstruction instruct = new ConstantInstruction(i);
            environment.addInstruction(variableName, instruct);
             last = commandsToExecute.execute(model);
         }
         environment.removeInstruction(variableName);
         return last;
     }
 
 }
