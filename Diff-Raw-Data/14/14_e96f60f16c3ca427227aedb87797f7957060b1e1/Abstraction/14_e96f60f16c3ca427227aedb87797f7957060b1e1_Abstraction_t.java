 package soot.jimple.infoflow.data;
 
 
 import java.util.HashMap;
 import java.util.Stack;
 
 import soot.Local;
 import soot.Value;
 
 public class Abstraction {
 	private final AccessPath accessPath;
 	private final Value source;
 	//only used for backward-search to find matching call:
 	private Stack<HashMap<Integer, Local>> originalCallArgs;
 	private int hashCode;
 	
 
 	public Abstraction(Value taint, Value src, boolean fieldtainted){
 		source = src;
 		accessPath = new AccessPath(taint, fieldtainted);
 	}
 	
 
 //	protected Abstraction(AccessPath p, Value src, boolean fieldtainted){
 //		source = src;
 //		accessPath = new AccessPath(taint, fieldtainted);	
 //	}
 	
 	//TODO: make private and change AwP
 	protected Abstraction(AccessPath p, Value src){
 		source = src;
 		accessPath = p;
 	}
 	
 	
 	public Abstraction deriveNewAbstraction(AccessPath p){
 		Abstraction a = new Abstraction(p, source);
 		a.originalCallArgs =(Stack<HashMap<Integer, Local>>) originalCallArgs.clone();
 		return a;
 	}
 	
 	public Abstraction deriveNewAbstraction(Value taint, boolean fieldtainted){
 		Abstraction a = new Abstraction(new AccessPath(taint, fieldtainted), source);
		if(originalCallArgs != null){
			a.originalCallArgs = (Stack<HashMap<Integer, Local>>) originalCallArgs.clone();
		}
 		return a;
 	}
 	
 	public Value getSource() {
 		return source;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Abstraction other = (Abstraction) obj;
 		if (accessPath == null) {
 			if (other.accessPath != null)
 				return false;
 		} else if (!accessPath.equals(other.accessPath))
 			return false;
 		if (originalCallArgs == null) {
 			if (other.originalCallArgs != null)
 				return false;
 		} else if (!originalCallArgs.equals(other.originalCallArgs))
 			return false;
 		if (source == null) {
 			if (other.source != null)
 				return false;
 		} else if (!source.equals(other.source))
 			return false;
 		return true;
 	}
 	
 	@Override
 	public int hashCode() {
 		if(hashCode == 0){
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((accessPath == null) ? 0 : accessPath.hashCode());
 			result = prime * result + ((originalCallArgs == null) ? 0 : originalCallArgs.hashCode());
 			result = prime * result + ((source == null) ? 0 : source.hashCode());
 			hashCode = result;
 		}
 		return hashCode;
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
 	
 	public HashMap<Integer,Local> getcurrentArgs(){
		if(originalCallArgs != null && !originalCallArgs.isEmpty()){
 			return originalCallArgs.peek();
 		}
 		return null;
 	}
 	
 	public void popCurrentCallArgs(){
 		//this is possible since we start at an entryPoint and might go back in control flow
 		if(originalCallArgs != null && !originalCallArgs.isEmpty()){
 			originalCallArgs.pop();
 		}
 	}
 	
 	public void addCurrentCallArgs(HashMap<Integer, Local> callArgs){
 		if(callArgs.containsKey(null)){
 			System.out.println("alarm!");
 		}
 		
 		if(originalCallArgs == null)
 			originalCallArgs = new Stack<HashMap<Integer, Local>>();
 		originalCallArgs.push(callArgs);
 	}
 	
 }
