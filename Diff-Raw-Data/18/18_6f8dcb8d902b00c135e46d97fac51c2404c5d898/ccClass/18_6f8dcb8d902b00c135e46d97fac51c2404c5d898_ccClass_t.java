 package oop;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ccClass {
 	private final String name;
 	private final String access;
 	private final boolean isStatic;
 	private ArrayList<ccConstructor> constructors;
 	private ArrayList<ccMethod> methods;
	private int mCount;
 	private HashMap<String, String> fields;
 	private ArrayList<String> packageNames;
 	private boolean constructorAdded;
 	
 	public ccClass(String clName, String clAccess){
 		name = clName;
 		access = clAccess;
 		isStatic = false;
 		packageNames = new ArrayList<String>();
 	}
 	public ccClass(String clName, String clAccess, boolean clIsStatic){
 		name = clName;
 		access = clAccess;
 		isStatic = clIsStatic;
 		methods = new ArrayList<ccMethod>();
 		constructors = new ArrayList<ccConstructor>();
 		ccConstructor emptyCon = new ccConstructor(this.getName(), "public", new String[0], new String[0], this);
 		emptyCon.setBlock(new ccManualBlock());
 		constructors.add(emptyCon);
 		constructorAdded = false;
 		fields = new HashMap<String, String>();
 		packageNames = new ArrayList<String>();
 	}
 	
 	/** Adds method to class's list of methods */
 	public void addMethod(ccMethod method){
 		methods.add(method);
		mCount++;
 	}
 	/** Adds constructor to class's list of constructors */
 	public void addConstructor(ccConstructor constructor){
 		if(!constructorAdded){
 			constructors.remove(0);
 			constructorAdded = true;
 		}
 		constructors.add(constructor);
 	}
 	
 	/** Adds field to class's hashmap of fields */
 	public void addField(String name, String type){
 		fields.put(name, type);
 	}
 	/** Sets the name of the class' package */
 	public void addPackage(String[] currentPackage){
 		for(String s : currentPackage){
 		packageNames.add(s);
 		}
 	}
 	
 	/** Gets field hashmap */
 	public HashMap getFields(){
 		return fields;
 	}
 	
 	/** Gets the name of the class. Of course. */
 	public String getName(){
 		return name;
 	}
 	/** Gets the name of the class with '__' before it. Yep. A whole class just for that. */
 	public String get_Name(){
 		return "__" + name;
 	}
 	/** Gets the access level of the class (Public, Private, Protected) */
 	public String getAccess(){
 		return access;
 	}
 	/** Static? (y/n) */
 	public boolean getIsStatic(){
 		return isStatic;
 	}
 	/** Gets the name of the class' package */
 	public ArrayList<String> getPackage(){
 		return packageNames;
 	}
 	/**
 	 * Searches for and returns method named s with parameters param[].
 	 * If the desired method is not in the list of methods, it returns a dummy method with the name "METHOD NOT FOUND".
 	 * 
 	 * @param s Name of desired method.
 	 * @return The method with name s.
 	 */
 	public ccMethod getMethod(String s, String[] param){
 		for(int i = 0; i < methods.size(); i++){
 			if(methods.get(i).match(s, param))
 				return methods.get(i);
 		}
 		return new ccMethod("METHOD NOT FOUND", this);
 	}
 	
 	public boolean hasMethod(String s, String[] param){
 		for(int i = 0; i < methods.size(); i++){
 			if(methods.get(i).match(s, param))
 				return true;
 		}
 		return false;
 	}
 	
 	public String getMethodName(String s, String[] param){
 		for(int i = 0; i < methods.size(); i++){
 			if(methods.get(i).match(s, param))
 				return methods.get(i).getName();
 		}
 		return "This is an error.                         !!!";
 	}
 	
 	/**
 	 * Searches for and returns constructor named s.
 	 * If the desired constructor is not found, it returns a dummy constructor with the name "CONSTRUCTOR NOT FOUND".
 	 *
 	 * ****************THIS METHOD IS CURRENTLY WORTHLESS!!!!!******************
 	 * ****IT'S HERE SIMPLY BECAUSE I MAY NEED IT AND FIX IT LATER!!!!!!!!!*****
 	 * 
 	 * @param s Name of desired constructor.
 	 * @return The constructor with name s.
 	 */
 	public ccConstructor getConstructor(String s){
 		for(int i = 0; i < constructors.size(); i++){
 			if(constructors.get(i).getName() == s)
 				return constructors.get(i);
 		}
 		return new ccConstructor("CONSTRUCTOR NOT FOUND", this);
 	}
 	public int getMethodCount(){
		return mCount;
 	}
 	public int getConstructorCount(){
 		return constructors.size();
 	}
 	/**
 	 * If you know the index of the method you want, somehow, then this is a marginally faster way to get it.
 	 * 
 	 * @param i Index of desired method.
 	 * @return The method at index i.
 	 */
 	public ccMethod getMethodAtIndex(int i){
 		return methods.get(i);
 	}
 	/**
 	 * If you know the index of the constructor you want, somehow, then this is a marginally faster way to get it.
 	 * 
 	 * @param i Index of desired constructor.
 	 * @return The constructor at index i.
 	 */
 	public ccConstructor getConstructorAtIndex(int i){
 		return constructors.get(i);
 	}
 	
 	public void addInheritedMethods(){
 		String[] argumentType = new String[0];
 		String[] argumentName = new String[0];
 		if(!hasMethod("hashCode", argumentType))
			methods.add(new ccMethod("hashCode", this, "public", "int32_t", argumentType, argumentName));
 		argumentType = new String[1];
 		argumentType[0] = "Object";
 		argumentName = new String[1];
 		argumentName[0] = "other";
 		if(!hasMethod("equals", argumentType)) 
			methods.add(new ccMethod("equals", this, "public", "bool", argumentType, argumentName));
 		argumentType = new String[0];
 		argumentName = new String[0];
 		if(!hasMethod("getClass", argumentType))
			methods.add(new ccMethod("getClass", this, "public", "Class", argumentType, argumentName));
 		if(!hasMethod("toString", argumentType))
			methods.add(new ccMethod("toString", this, "public", "String", argumentType, argumentName));
 	}
 	
 	public String toString(){
 
 		String s = access + " class Name:\"" + name + "\" Static:" + isStatic + " Constructors:";
 		if(constructors.size() > 0){
 			for(int i = 0; i < constructors.size(); i++){
 				s += "\r\t" + constructors.get(i);
 			}
 			s += "\r";
 		}
 		else s += "none";
 		s += " Methods:";
 		if(methods.size() > 0){
 			for(int i = 0; i < methods.size(); i++){
 				s += "\r\t" + methods.get(i);
 			}
 		}
 		else s += "none";
 
 		return s;
 	}
 	
 }
