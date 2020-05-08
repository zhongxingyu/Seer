 package cpu.instruction;
 
 import cpu.InstructionRunner;
 import cpu.instpar.InstructionParameter;
 import cpu.instpar.InvalidParameterException;
 
 public class Sub extends ArithmeticInstruction implements Instruction {
 
 	@Override
 	public void execute(InstructionRunner runner,
 		InstructionParameter[] parameters)
 		throws InvalidInstructionException, InvalidParameterException
 	{
 		if (parameters.length != 2) {
 			throw new InvalidInstructionException();
 		}
 		
 		int paramA = parameters[0].loadValue(runner);
 		int paramB = parameters[1].loadValue(runner);
 		
 		// subtract the two parameters
		Long diff = ((long)paramA) - paramB;
 		
 		// store the result in dest
 		parameters[1].storeValue(runner, diff.intValue());
 		
 		// set flags
 		super.setFlags(diff, runner);
 	}
 
 }
