 package models;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 
 public class Method {
 	private String		 		name;
 	private Clazz 				clazz;
 	
 	private int					startChar;
 	private int					endChar;
 	
 	private String				returnType;
 	
 	private List<Method> 		methodCalls;
 	private List<Method>		calledBy;
 	
 	private List<Method> 		fuzzyCalls;
 	private List<Method>		fuzzyCalledBy;
 	
 	private List<String>		unresolvedCalls;
 	
 	private MethodDeclaration	node;
 	
 	public Method() {
 		methodCalls 	= new ArrayList<Method>();
 		calledBy 		= new ArrayList<Method>();
 		fuzzyCalls 		= new ArrayList<Method>();
 		fuzzyCalledBy 	= new ArrayList<Method>();
 		unresolvedCalls = new ArrayList<String>();
 	}
 	
 	public Method(String name, Clazz clazz, ArrayList<Method> methodCalls) {
 		this.name 		 = name;
 		this.clazz 		 = clazz;
 		this.methodCalls = methodCalls;
 		this.calledBy 		 = new ArrayList<Method>();
 		
 		fuzzyCalls 		= new ArrayList<Method>();
 		fuzzyCalledBy 	= new ArrayList<Method>();
 		
 		this.unresolvedCalls = new ArrayList<String>();
 
 	}
 	
 	public Method(String name, Clazz clazz, int start, int end)
 	{
 		this.name 			= name;
 		this.clazz 			= clazz;
 		this.startChar 		= start;
 		this.endChar 		= end;
 		this.methodCalls 	= new ArrayList<Method>();
 		this.calledBy 		= new ArrayList<Method>();
 		fuzzyCalls 		= new ArrayList<Method>();
 		fuzzyCalledBy 	= new ArrayList<Method>();
 		this.unresolvedCalls = new ArrayList<String>();
 	}
 	
 	public void addFuzzyCalledBy(Method m) {
 		if(!fuzzyCalledBy.contains(m))
 			this.fuzzyCalledBy.add(m);
 	}
 	
 	public void addFuzzyCall(Method m) {
 		if(!fuzzyCalls.contains(m))
 			this.fuzzyCalls.add(m);
 	}
 	
 	public void addCalledBy(Method m) {
 		if(!calledBy.contains(m))
 			this.calledBy.add(m);
 	}
 	
 	public void addMethodCall(Method m) {
 		if(!methodCalls.contains(m))
 			this.methodCalls.add(m);
 	}
 	
 	public void addUnresolvedCall(String method) {
 		if(!unresolvedCalls.contains(method))
 			this.unresolvedCalls.add(method);
 	}
 	
 	public void print() {
 		System.out.println("    METHOD: " + name);
 		System.out.println("      Return Type: " + returnType);
 		System.out.println("      Calls: ");
 		for(Method m: methodCalls)
 			System.out.println("        " + m.getName());
 		System.out.println("      Called By: ");
 		for(Method m: calledBy)
 			System.out.println("        " + m.getName());
 		System.out.println("      Fuzzy Calls: ");
 		for(Method m: fuzzyCalls)
 			System.out.println("        " + m.getName());
 		System.out.println("      Fuzzy Called By: ");
 		for(Method m: fuzzyCalledBy)
 			System.out.println("        " + m.getName());
 		System.out.println("      Unresolved Calls: (" + unresolvedCalls.size() + ")");
 		for(String m: unresolvedCalls)
 			System.out.println("        " + m);
 	}
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public Clazz getClazz() {
 		return clazz;
 	}
 	public void setClazz(Clazz clazz) {
 		this.clazz = clazz;
 	}
 	public List<Method> getMethodCalls() {
 		return methodCalls;
 	}
 	public void setMethodCalls(ArrayList<Method> methodCalls) {
 		this.methodCalls = methodCalls;
 	}
 
 	public List<Method> getCalledBy() {
 		return calledBy;
 	}
 
 	public void setCalledBy(List<Method> calledBy) {
 		this.calledBy = calledBy;
 	}
 
 	public int getstartChar() {
 		return startChar;
 	}
 
 	public void setstartChar(int startChar) {
 		this.startChar = startChar;
 	}
 
 	public int getendChar() {
 		return endChar;
 	}
 
 	public void setendChar(int endLine) {
 		this.endChar = endLine;
 	}
 
 	public String getReturnType() {
 		return returnType;
 	}
 
 	public void setReturnType(String returnType) {
 		this.returnType = returnType;
 	}
 
 	public MethodDeclaration getNode() {
 		return node;
 	}
 
 	public void setNode(MethodDeclaration node) {
 		this.node = node;
 	}
 
 	public List<String> getUnresolvedCalls() {
 		return unresolvedCalls;
 	}
 
 	public void setUnresolvedCalls(List<String> unresolvedCalls) {
 		this.unresolvedCalls = unresolvedCalls;
 	}
 
 	public List<Method> getFuzzyCalls() {
 		return fuzzyCalls;
 	}
 
 	public void setFuzzyCalls(List<Method> fuzzyCalls) {
 		this.fuzzyCalls = fuzzyCalls;
 	}
 
 	public List<Method> getFuzzyCalledBy() {
 		return fuzzyCalledBy;
 	}
 
 	public void setFuzzyCalledBy(List<Method> fuzzyCalledBy) {
 		this.fuzzyCalledBy = fuzzyCalledBy;
 	}
 }
