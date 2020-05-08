 package be.uclouvain.jail.vm;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import net.chefbe.javautils.adapt.AdaptUtils;
 import net.chefbe.javautils.robust.exceptions.CoreException;
 import be.uclouvain.jail.vm.JailVMException.ERROR_TYPE;
 
 /**
  * Stack for command invocation.
  * 
  * @author blambeau
  */
 public class JailVMStack {
 
 	/** VM stack. */
 	private Stack<Object> stack;
 	
 	/** Creates a stack from values. */
 	public JailVMStack(Object[] values) {
 		stack = new Stack<Object>();
 		int size = values.length;
 		for (int i=size-1; i>=0; i--) {
 			stack.push(values[i]);
 		}
 	}
 	
 	/** Pushes a value on the stack. */
 	public void push(Object value) {
 		stack.push(value);
 	}
 	
 	/** Pops and return top value from/of the stack. */
 	public Object pop() {
 		return stack.pop();
 	}
 	
 	/** Returns top value of the stack. */
 	public Object top() {
 		return stack.peek();
 	}
 	
 	/** Extracts the class for an array of classes. */
 	public static Class extractArrayClass(Class c) {
 		String s = c.getName();
 		s = s.substring(2,s.length()-1);
 		try {
 			return Class.forName(s);
 		} catch (ClassNotFoundException e) {
 			throw new CoreException("Unable to retrieve class: " + s);
 		}
 	}
 
 	/** Pops an array argument. */
 	private int popArrayArg(Class<?> type, int offset, Object[] args, int index) throws JailVMException {
 		// extract requested type
 		type = extractArrayClass(type);
 
 		// adapted values
 		List<Object> values = new ArrayList<Object>();
 		boolean ok=true;
 		int i=1;
 		while (ok) {
 			// compute stack index
 			int where = stack.size()-offset-i;
 			if (where < 0) { break; }
 			
 			// get from stack
 			Object arg = stack.get(where);
 
 			// adapt it
 			Object adapted = AdaptUtils.adapt(arg,type);
 			if (adapted == null) {
 				ok = false;
 			} else {
 				values.add(adapted);
 				i++;
 			}
 		}
 		
 		// put created array at the correct place
 		Object[] array = (Object[]) Array.newInstance(type,i-1);
 		args[index] = values.toArray(array);
 		
 		return i-1;
 	}
 	
 	/** Pops an argument from the stack and place it at index-th position 
 	 * in the args array. */
 	private int popArg(Class<?> type, int offset, Object[] args, int index) throws JailVMException {
 		if (type.isArray()) {
 			return popArrayArg(type,offset,args,index);
 		}
 		
 		// get from stack
 		Object arg = stack.get(stack.size()-offset-1);
 		
 		// adapt it
 		Object adapted = AdaptUtils.adapt(arg,type);
 		if (adapted == null) {
			throw new JailVMException(ERROR_TYPE.ADAPTABILITY_ERROR,null,"Unable to adapt argument " + offset + " [" + arg.getClass().getSimpleName() + "] to " + type.getSimpleName());
 		}
 		
 		args[index] = adapted;
 		return 1;
 	}
 	
 	/** Pops some arguments of the stack. */
 	public Object[] popArgs(Class<?>[] types, JailVM vm, JailVMOptions options) throws JailVMException {
 		// first check stack size
 		int size = types.length;
 		if (stack.size() < size-2) {
 			throw new JailVMException(ERROR_TYPE.BAD_COMMAND_USAGE,null);
 		}
 		
 		// count is the actual number of eat args on stack
 		int count = 0;
 		
 		// try to create args
 		Object[] args = new Object[size];
 		int i=0;
 		for (Class<?> type: types) {
 			if (JailVM.class.equals(type)) {
 				args[i++] = vm;
 			} else if (JailVMOptions.class.equals(type)) {
 				args[i++] = options;
 			} else {
 				count += popArg(type,count,args,i++);
 			}
 		}
 		
 		// pop all
 		for (int j=0; j<count; j++) { pop(); }
 		
 		return args;
 	}
 
 	/** Returns stack size. */
 	public int size() {
 		return stack.size();
 	}
 	
 }
