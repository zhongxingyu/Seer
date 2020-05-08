 package jworldgen.generator.worldStructure;
 
 import java.util.Stack;
 
 public class VoxelStack {
 	private Stack<Integer> stack;
 	
 	public VoxelStack()
 	{
 		stack = new Stack<Integer>();
 	}
 	
	public void modify(int value)
	{
		if (stack.empty())
		{
			stack.push(value);
			return;
		}
		if (value == 0)
			return;
		if (stack.peek() != -3 || value == -4)
		{
			stack.pop();
			stack.push(value);
		}
	}
 	public int pop()
 	{
 		if (!stack.empty())
 			return stack.pop();
 		return 0;
 	}
 	
 	public void push(int value)
 	{
 		if (stack.empty() || stack.peek() != -3 || value == -4)
 			stack.push(value);
 	}
 	
 	public int peek()
 	{
 		if (!stack.empty())
 			return stack.peek();
 		return 0;
 	}
 	
 	public boolean empty()
 	{
 		return stack.isEmpty();
 	}
 	
 	public int evaluate()
 	{
 		while(!stack.empty() && stack.peek() < 1)
 		{
 			switch(stack.pop())
 			{
 			case 0:
 				break;
 			case -1:
 				return 0;
 			case -2:
 				while(!stack.empty() && stack.peek() == 0)
 					stack.pop();
 				if (!stack.empty())
 					stack.pop();
 			case -3:
 				break;
 			case -4:
 				if (stack.peek() == -3)
 					stack.pop();
 				break;
 			}
 		}
 		if (stack.empty())
 			return 0;
 		int result = stack.pop();
 		if (result < 1)
 			return 0;
 		return result;
 	}
 	
 }
