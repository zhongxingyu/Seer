 package lea;
 
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.TreeMap;
 
 import lea.syntax.*;
 import lea.types.Type;
 
 public class FunctionTable extends TreeMap<String, FunctionInfo> {
 	private static final long serialVersionUID = -2198438635232540509L;
 
 	public String toString() {
 		String str = "";
 
 		for (Map.Entry<String, FunctionInfo> entry : entrySet()) {
 			str += entry.getKey() + " " + entry.getValue().toString() + "\n\n";
 		}
 
 		return str;
 	}
 	
 	public void saveDotToDir(String directory) {
 		for (Map.Entry<String, FunctionInfo> entry : entrySet())
 		{
 		    entry.getValue().getSyntaxTree().toDot(directory + "/" + entry.getKey());
 		}
 	}
 
 	public boolean isCallPermitted(String id, Expression e) {
 		boolean isPermitted = false;

 		FunctionInfo nfi = this.get(id);
 		LinkedList<Type> args = new LinkedList<Type>();

 		SyntaxTree tmp = e;
		
 		if(tmp != null)
 		{
 			if (tmp.getLeft() == null && tmp.getRight() == null)
 				args.add(tmp.getType());
 			else 
 			{
 				while ((tmp.getLeft() != null) || (tmp.getRight() != null)) 
 				{
 					Expression ex = (Expression)tmp.getLeft();
 					Expression ex2 = (Expression)tmp.getRight();
 					
 					if(ex instanceof ListNode)
 					{
 						ListNode l = (ListNode)ex;
 						args.add(0, l.getType());
 						
 						tmp = tmp.getRight();
 						if(tmp != null)
 						{
 							if(tmp.getLeft() == null && tmp.getRight() == null)
 								args.add(1, tmp.getType());
 						}
 					}
 					else if(ex2 instanceof ListNode)
 					{
 						ListNode l = (ListNode)ex2;
 						args.add(0, l.getType());
 						
 						tmp = tmp.getLeft();
 						if(tmp != null)
 						{
 							if(tmp.getLeft() == null && tmp.getRight() == null)
 								args.add(1, tmp.getType());
 						}
 					}
 					else
 					{
 						if (tmp.getRight() != null) 
 						{						
 							if (tmp.getRight().getLeft() == null
 									&& tmp.getRight().getRight() == null)
 								args.add(0, tmp.getRight().getType());
 						}
 						if (tmp.getLeft() != null) 
 						{
 							if (tmp.getLeft().getLeft() == null
 									&& tmp.getLeft().getRight() == null)
 								args.add(0, tmp.getLeft().getType());
 						}
 						tmp = tmp.getLeft();
 					}
 				}
 			}
 		}
 
 		if (nfi != null) 
 		{
 			boolean argumentsFit = true;
 			LinkedList<ArgumentInfo> funcArgs = nfi.getArgs();
 
 			if (args.size() == funcArgs.size()) 
 			{
 				for (int i = 0; i < args.size(); i++) 
 				{
 					if (!args.get(i).getType()
 							.equals(funcArgs.get(i).getType())) 
 					{
 						argumentsFit = false;
 						break;
 					}
 				}
 			} 
 			else
 				argumentsFit = false;
 
 			if (argumentsFit) 
 			{
 				isPermitted = true;
 			}			
 		}
 		return isPermitted;
 	}
 }
