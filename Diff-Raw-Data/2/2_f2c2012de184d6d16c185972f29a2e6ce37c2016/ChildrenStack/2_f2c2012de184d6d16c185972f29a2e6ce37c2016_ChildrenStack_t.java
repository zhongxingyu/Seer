 import java.util.ArrayList;
 
 
 public class ChildrenStack {
 	
 	private ArrayList<Integer> stack;
 	
 	public ChildrenStack()
 	{
 		stack = new ArrayList<Integer>();
 	}
 	
 	public void push(int value)
 	{
 		stack.add(value);
 	}
 	
 	public void addToTop()
 	{
 		if(stack.size() >= 1) {
 	      stack.set(stack.size()-1, stack.get(stack.size()-1)+1);
 	    }
 	}
 	
 	public void pop()
 	{
 		stack.remove(stack.size()-1);
 	}
 	
 	public Integer peek()
 	{
		return stack.get(stack.size()-1);
 	}
 
 }
