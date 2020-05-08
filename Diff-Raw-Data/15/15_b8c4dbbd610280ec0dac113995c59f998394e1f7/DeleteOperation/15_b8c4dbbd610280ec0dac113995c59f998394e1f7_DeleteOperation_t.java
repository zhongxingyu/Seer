 /*
  *   DeleteOperation.java
  * 	 @Author Oleg Gorobets
  *   Created: 25.07.2007
  *   CVS-ID: $Id: 
  *************************************************************************/
 
 package org.swfparser.operation;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Stack;
 
 import org.swfparser.CodeUtil;
 import org.swfparser.Operation;
 
 public class DeleteOperation extends AbstractOperation {
 
 	protected Operation object;
 	protected Operation property;
 	
 	protected boolean simpleProperty = false;
 	
 	public DeleteOperation(Stack<Operation> stack) {
 		super(stack);
 		property = stack.pop();
 		object = stack.pop();
 	}
 
 	public int getArgsNumber() {
 		return 2;
 	}
 
 	public String getStringValue(int level) {
		StringBuilder buf = new StringBuilder();
		buf.append(CodeUtil.getIndent(level));
        buf.append("delete ");
        buf.append(CodeUtil.getSimpleValue(object, level));
        buf.append(CodeUtil.getMemberGetExpression(property, level));
        return buf.toString();
 	}
 
 	public List<Operation> getOperations() {
 		return Arrays.asList(property,object);
 	}
 	
 	@Override
 	public String toString() {
 		return "DeleteOperation("+object+","+property+")";
 	}
 
 }
