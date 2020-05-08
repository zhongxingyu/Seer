 package forth;
 
 import java.util.Map;
 import java.util.Stack;
 
 public class InterpreterState {
 	public Tokens tokens;
 	public int token_position = 0;
 	public Stack<Token> stack = new Stack<Token>();
 	public Map<Token,Integer> jump_target;
 	
	public InterpreterState(Tokens tokens) {
		this.tokens = tokens;
 	}
 }
