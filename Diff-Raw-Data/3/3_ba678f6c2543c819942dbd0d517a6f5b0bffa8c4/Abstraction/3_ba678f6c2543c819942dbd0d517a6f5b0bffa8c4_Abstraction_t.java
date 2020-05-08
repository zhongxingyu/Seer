 package soot.jimple.infoflow.data;
 
 
 import java.util.LinkedList;
 import java.util.Stack;
 
 import soot.SootField;
 import soot.Unit;
 import soot.Value;
 import soot.jimple.Stmt;
 
 public class Abstraction implements Cloneable {
 	private final AccessPath accessPath;
 	private final Value source;
 	private final Stmt sourceContext;
 	private final Stack<Unit> callStack;
 	private final boolean exceptionThrown;
 	private int hashCode;
 
 	public Abstraction(Value taint, Value src, Stmt srcContext, boolean exceptionThrown){
 		this.source = src;
 		this.accessPath = new AccessPath(taint);
 		this.callStack = new Stack<Unit>();
 		this.sourceContext = srcContext;
 		this.exceptionThrown = exceptionThrown;
 	}
 		
 	protected Abstraction(AccessPath p, Value src, Stmt srcContext, boolean exceptionThrown){
 		this.source = src;
 		this.sourceContext = srcContext;
 		this.accessPath = p;
 		this.callStack = new Stack<Unit>();
 		this.exceptionThrown = exceptionThrown;
 	}
 
 	/**
 	 * Creates an abstraction as a copy of an existing abstraction,
 	 * only exchanging the access path.
 	 * @param p The value to be used as the new access path
 	 * @param original The original abstraction to copy
 	 */
 	public Abstraction(Value p, Abstraction original){
 		this(new AccessPath(p), original);
 	}
 
 	/**
 	 * Creates an abstraction as a copy of an existing abstraction,
 	 * only exchanging the access path.
 	 * @param p The access path for the new abstraction
 	 * @param original The original abstraction to copy
 	 */
 	public Abstraction(AccessPath p, Abstraction original){
 		callStack = new Stack<Unit>();
 		if (original == null) {
 			source = null;
 			sourceContext = null;
 		}
 		else {
 			source = original.source;
 			sourceContext = original.sourceContext;
 			callStack.addAll(original.callStack);
 		}
 		accessPath = p;
 		exceptionThrown = original.exceptionThrown;
 	}
 	
 	public Abstraction deriveNewAbstraction(AccessPath p){
 		Abstraction a = new Abstraction(p, source, sourceContext, exceptionThrown);
 		a.callStack.addAll(this.callStack);
 		return a;
 	}
 	
 	public Abstraction deriveNewAbstraction(Value taint){
 		return this.deriveNewAbstraction(taint, false);
 	}
 	
 	public Abstraction deriveNewAbstraction(Value taint, boolean cutFirstField){
 		Abstraction a;
 		if(cutFirstField){
 			LinkedList<SootField> tempList = new LinkedList<SootField>(accessPath.getFields());
 			tempList.removeFirst();
 			a = new Abstraction(new AccessPath(taint, tempList), source, sourceContext, exceptionThrown);
 		}
 		else
 			a = new Abstraction(new AccessPath(taint,accessPath.getFields()), source, sourceContext, exceptionThrown);
 		a.callStack.addAll(this.callStack);
 		return a;
 	}
 
 	/**
 	 * Derives a new abstraction that models the current local being thrown as
 	 * an exception
 	 * @return The newly derived abstraction
 	 */
 	public Abstraction deriveNewAbstractionOnThrow(){
 		assert !this.exceptionThrown;
 		Abstraction abs = new Abstraction(accessPath, source, sourceContext, true);
 		abs.callStack.addAll(this.callStack);
 		return abs;
 	}
 	
 	/**
 	 * Derives a new abstraction that models the current local being caught as
 	 * an exception
 	 * @param taint The value in which the tainted exception is stored
 	 * @return The newly derived abstraction
 	 */
 	public Abstraction deriveNewAbstractionOnCatch(Value taint){
 		assert this.exceptionThrown;
 		Abstraction abs = new Abstraction(new AccessPath(taint), source, sourceContext, false);
 		abs.callStack.addAll(this.callStack);
 		return abs;
 	}
 
 	public Value getSource() {
 		return source;
 	}
 
 	public Stmt getSourceContext() {
 		return this.sourceContext;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (super.equals(obj))
 			return true;
 		if (obj == null || !(obj instanceof Abstraction))
 			return false;
 		Abstraction other = (Abstraction) obj;
 		if (accessPath == null) {
 			if (other.accessPath != null)
 				return false;
 		} else if (!accessPath.equals(other.accessPath))
 			return false;
 		if (source == null) {
 			if (other.source != null)
 				return false;
 		} else if (!source.equals(other.source))
 			return false;
 		if (sourceContext == null) {
 			if (other.sourceContext != null)
 				return false;
 		} else if (!sourceContext.equals(other.sourceContext))
 			return false;
 		if (callStack == null) {
 			if (other.callStack != null)
 				return false;
 		} else if (!callStack.equals(other.callStack))
 			return false;
 		if (this.exceptionThrown != other.exceptionThrown)
 			return false;
 		
 		assert this.hashCode() == obj.hashCode();	// make sure nothing all wonky is going on
 		return true;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		if (this.hashCode == 0) {
 			this.hashCode = 1;
 			this.hashCode = prime * this.hashCode + ((accessPath == null) ? 0 : accessPath.hashCode());
 			this.hashCode = prime * this.hashCode + ((source == null) ? 0 : source.hashCode());
 			this.hashCode = prime * this.hashCode + ((sourceContext == null) ? 0 : sourceContext.hashCode());
 			this.hashCode = prime * this.hashCode + (exceptionThrown ? 1231 : 1237);
 		}
 		// The call stack is not immutable, so we must not include it in the
 		// cached hash
 		return prime * this.hashCode + ((callStack == null) ? 0 : callStack.hashCode());
 	}
 	
 	public void addToStack(Unit u){
 		// Do not add a method we already have on the stack.
 		// Otherwise, recursive calls will make our analysis run in an
 		// infinite loop.
 		if (!callStack.contains(u))
 			callStack.push(u);
 	}
 	
 	public void removeFromStack(){
 		if(!callStack.isEmpty())
 			callStack.pop();
 	}
 	
 	public boolean isStackEmpty(){
 		return callStack.isEmpty();
 	}
 	
 	public Unit getElementFromStack(){
 		if(!callStack.isEmpty())
 			return callStack.peek();
 		return null;
 	}
 	
 	protected Stack<Unit> getCallStack() {
 		return this.callStack;
 	}
 	
 	@Override
 	public String toString(){
 		if(accessPath != null && source != null){
 			return accessPath.toString() + " /source: "+ source.toString();
 		}
 		if(accessPath != null){
 			return accessPath.toString();
 		}
 		return "Abstraction (null)";
 	}
 	
 	public AccessPath getAccessPath(){
 		return accessPath;
 	}
 	
 	/**
 	 * Gets whether this value has been thrown as an exception
 	 * @return True if this value has been thrown as an exception, otherwise
 	 * false
 	 */
 	public boolean getExceptionThrown() {
 		return this.exceptionThrown;
 	}
 	
 	@Override
 	public Abstraction clone(){
 		Abstraction a = new Abstraction(accessPath, source, sourceContext, exceptionThrown);
 		a.callStack.addAll(this.callStack);
 		return a;
 	}
 
 }
