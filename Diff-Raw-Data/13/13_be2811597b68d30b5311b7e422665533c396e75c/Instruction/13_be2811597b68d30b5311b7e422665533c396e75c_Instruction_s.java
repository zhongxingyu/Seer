 package welch.brainmess;
 
 /**
  * Represents a Brain Mess Instruction.
  * @author Michael Welch
  *
  */
 public enum Instruction
 {
 	/**
 	 * Represents the instruction that moves the tape forward one cell.
 	 */
 	MoveForward
 	{
 		public void execute(IExecutionContext context) { context.moveForward(); }
 	},
 	
 	/**
 	 * Represents the instruction that moves the tape backward one cell.
 	 */
 	MoveBackward
 	{
 		public void execute(IExecutionContext context) { context.moveBackward(); }
 	},
 	
 	/**
 	 * Represents the instruction that increments the value of the current cell.
 	 */
 	Increment
 	{
 		public void execute(IExecutionContext context) { context.increment(); }
 	},
 	
 	/**
 	 * Represents the instruction that decrements the value of the current cell.
 	 */
 	Decrement
 	{
 		public void execute(IExecutionContext context) { context.decrement(); }
 	},
 	
 	/**
 	 * Represents the instruction that inputs a character and stores its integer
 	 * value on the tape at the current location.
 	 */
 	Input
 	{
 		public void execute(IExecutionContext context) { context.input(); }
 	},
 	
 	/**
 	 * Represents the instruction that reads the value of the current cell and outputs
 	 * the character it represents.
 	 */
 	Output
 	{
 		public void execute(IExecutionContext context) { context.ouput(); }
 	},
 	
 	/**
 	 * Represents the test and jump forward instruction.
 	 */
 	TestAndJumpForward
 	{
 		public void execute(IExecutionContext context) { context.testAndJumpForward(); }
 	},
 	
 	/**
 	 * Represents the test and jump backward instruction.
 	 */
 	TestAndJumpBackward
 	{
 		public void execute(IExecutionContext context) { context.testAndJumpBackward(); }
 	},
 	
 	/**
 	 * A no operation instruction. Used for all non-instruction characters in the program.
 	 */
 	NOP
 	{
 		public void execute(IExecutionContext context) { }
 	};
 	
 	
 	/**
 	 * Executes the current instruction.
 	 * @param context The execution environment in which to execute the instruction.
 	 */
 	public abstract void execute(IExecutionContext context);
 	
 	/**
 	 * Parses an Instruction from the specified character value.
 	 * @param value
 	 * @return
 	 */
 	public static Instruction parseInstruction(char value)
 	{
 		switch(value)
 		{
 		case '>':
 			return Instruction.MoveForward;
 			
 		case '<':
 			return Instruction.MoveBackward;
 			
 		case '+':
 			return Instruction.Increment;
 			
 		case '-':
 			return Instruction.Decrement;
 			
 		case '.':
 			return Instruction.Output;
 			
 		case ',':
 			return Instruction.Input;
 			
 		case '[':
 			return Instruction.TestAndJumpForward;
 			
 		case ']':
 			return Instruction.TestAndJumpBackward;
 			
 		default:
 			return Instruction.NOP;
 		}
 	}
 	
 }
