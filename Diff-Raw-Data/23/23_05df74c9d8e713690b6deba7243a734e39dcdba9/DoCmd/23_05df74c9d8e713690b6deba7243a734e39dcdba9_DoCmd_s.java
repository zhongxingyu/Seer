 package temmental2.rpl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import temmental2.Reader;
 import temmental2.StackException;
 
 public class DoCmd  extends Reader implements Command {
 
 	private List<Object> loopst;
 	private List<Object> condition;
 	
 	public DoCmd() {
 		super(new ArrayList<Object>());
 		this.condition = new ArrayList<Object>();
 		this.loopst = new ArrayList<Object>();
 	}
 
 	public void tocond() {
 		this.condition = operations;
 		operations = new ArrayList<Object>();
 	}
 	
 	public void toloopst() {
 		this.loopst = operations;
 		operations = new ArrayList<Object>();
 	}
 
	@Override
 	public void apply(RplStack stack) throws StackException {
 		boolean r = true;
 		while (r) {
 			RplStack.push_operations(stack, loopst, false, true);
 			RplStack.push_operations(stack, condition, false, true);
 			r = (Boolean) stack.pop();
 		}
 	}
 
 }
