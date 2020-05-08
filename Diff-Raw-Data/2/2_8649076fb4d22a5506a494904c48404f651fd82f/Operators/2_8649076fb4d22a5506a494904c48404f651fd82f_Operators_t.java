 /**
  *
  */
 package maelstrom.funge.interpreter.operator;
 
 import java.util.Hashtable;
 
 import maelstrom.funge.interpreter.Funge;
 
 
 /**
  * @author Tim
  *
  */
 public class Operators {
 
 	private static Hashtable<Long, Operator> operators = new Hashtable<Long, Operator>();
 
 	/*
 	 * Operator list
 	 *  + - * / % - Arithmetic
 	 *  ! ` - Not, greater than 0
 	 *  " - Toggle string mode
 	 *
 	 *  , . - Output char or number
 	 *  ~ & - Get char or number
 	 *
 	 * 0 1 2 3 4 5 6 7 8 9 a b c d e f - Push a number to the stack
 	 * : \ n - Pop, clone, swap or clear the stack
 	 # { } u - Push/Pop a stack to the stack stack
 	 *
 	 * ^ < > v - Control direction
 	 * ? x r - Control direction in other ways
 	 * [ ] - Turn
 	 *
 	 # # j - Jump around the grid
 	 # k - Execute a function lots of times
 	 *
 	 # _ | w - Ifs
 	 *
 	 *   ; z - Space, skip block, or noop
 	 # @ q - Quit
 	 *
 	 # g p - Get or Set a symbol in the grid
 	 # ' s - Get or Set the next symbol
 	 *
 	 # A-Z - Use a finger print
 	 # ( ) - Load/Close a finger print
 	 *
 	 # y - Get SysInfo = - Execute command line statement
 	 *
 	 # t - Split IP - ConcurrentFunge only
 	 # i o - I/O file - FilesystemFunge only
 	 */
 
 	static {
 
 		// The arithmetic operators
 		operators.put((long) '+', new ArithmeticOperator.Add());
 		operators.put((long) '-', new ArithmeticOperator.Subtract());
 		operators.put((long) '*', new ArithmeticOperator.Multiply());
 		operators.put((long) '/', new ArithmeticOperator.Divide());
 		operators.put((long) '%', new ArithmeticOperator.Modulus());
 
 		// Some misc operators
 		operators.put((long) '!', new LogicalOperator.Not());
 		operators.put((long) '`', new LogicalOperator.GreaterThan());
 
 		operators.put((long) Funge.STRING_TOGGLE, new StringToggleOperator());
 
 		// The IO Operators
 		operators.put((long) ',', new IOOperator.OutputChar());
 		operators.put((long) '.', new IOOperator.OutputNum());
 		operators.put((long) '~', new IOOperator.InputChar());
 		operators.put((long) '&', new IOOperator.InputNum());
 
 		// The number operators
 		for (int i = 0; i < 10; i++) {
 			operators.put((long) (i + '0'), new NumberOperator(i));
 		}
 		for (int i = 0; i < 6; i++) {
 			operators.put((long) (i + 'a'), new NumberOperator(10 + i));
 		}
 		// The stack operators
		operators.put((long) '$', new StackOperator.Pop());
 		operators.put((long) ':', new StackOperator.Clone());
 		operators.put((long) '\\', new StackOperator.Swap());
 		operators.put((long) 'n', new StackOperator.Empty());
 
 		// The main movement operators
 		operators.put((long) '^', new PointerOperator.North());
 		operators.put((long) '>', new PointerOperator.East());
 		operators.put((long) 'v', new PointerOperator.South());
 		operators.put((long) '<', new PointerOperator.West());
 
 		operators.put((long) '?', new PointerOperator.Random());
 		operators.put((long) 'r', new PointerOperator.Reflect());
 		operators.put((long) 'x', new PointerOperator.Absolute());
 
 		operators.put((long) '[', new PointerOperator.Anticlockwise());
 		operators.put((long) ']', new PointerOperator.Clockwise());
 
 		operators.put((long) '#', new PointerOperator.Trampoline());
 		operators.put((long) 'j', new PointerOperator.Jump());
 
 		operators.put((long) 'k', new RepeatOperator());
 
 		// The 'if's
 		operators.put((long) '_', new IfOperator.Horizontal());
 		operators.put((long) '|', new IfOperator.Vertical());
 		operators.put((long) 'w', new IfOperator.Branch());
 
 		// The space and noop operators
 		operators.put((long) ' ', new NoopOperator(NoopOperator.SPACE_DESCRIPTION));
 		operators.put((long) ';', new NoopOperator(NoopOperator.JUMP_DESCRIPTION));
 		operators.put((long) 'z', new NoopOperator(NoopOperator.NOOP_DESCRIPTION));
 
 
 		operators.put((long) '@', new StopOperator.Stop());
 		operators.put((long) 'q', new StopOperator.Quit());
 
 		operators.put((long) 'g', new GridOperator.Get());
 		operators.put((long) 'p', new GridOperator.Put());
 		operators.put((long) '\'', new GridOperator.GetNext());
 		operators.put((long) 's', new GridOperator.PutNext());
 
 	}
 
 
 	public static Operator get(long operatorCode) {
 		Operator operator = operators.get(operatorCode);
 
 		if (operator != null) {
 			return operator;
 		} else {
 			return new PointerOperator.Reflect();
 		}
 	}
 
 	public static boolean operatorExists(long operator) {
 		return (operators.get(operator) != null);
 	}
 }
