 package xtc.oop;
 import java.util.ArrayList;
 import xtc.tree.GNode;
 
 public class Declaration{
 	public ArrayList<String> modifiers;
 	public String returntype;
 	public String name;
 	public ArrayList<Fparam> params;
 	public String ownerClass;
 	public GNode bnode;
 	public ArrayList<Variable> variables;
 	public boolean isVirtual;
 	public int overloadNum;
 	public int specificity;
 
 	
 	
 	//constructor for Class and Object methods
 	Declaration(String returntype, String name, String ownerClass, ArrayList<Fparam> params) {
 		this.modifiers = new ArrayList<String>(0);//default
 		this.returntype = returntype;
 		this.name = name;
 		this.params = new ArrayList<Fparam>(params);
 		this.ownerClass = ownerClass;
 		this.bnode = null;//default
 		this.variables = new ArrayList<Variable>(0);//default
 		this.isVirtual = true;//default
 		this.overloadNum = 0;//default
 		this.specificity = 0;//default
 	
 	}
 	//constructor for all other methods
 	Declaration(int overloadNum,ArrayList<String> modifiers, boolean isVirtual, String returntype, String name,
                     String ownerClass,ArrayList<Fparam> params,GNode bnode,ArrayList<Variable> variables){
 		this(returntype, name, ownerClass, params);
 		this.isVirtual = isVirtual;
 		this.variables.addAll(variables);
 		this.modifiers.addAll(modifiers);
 		this.overloadNum = overloadNum;
 		this.bnode =bnode;
 		
 	}
 	//constructor for blank representation of a method used to pass to ewalk when getting instancefield values
 	Declaration(String ownerClass,ArrayList<InstanceField> fields) {
 		this.modifiers = new ArrayList<String>(0);
 		this.modifiers.add("public");//default
 		this.returntype = "void";
 		this.name = ".blank";//no method can have a . in it
 		this.params = new ArrayList<Fparam>(0);
 		this.ownerClass = ownerClass;//NEED CLASSNAME
 		this.bnode = null;//default
 		this.variables = new ArrayList<Variable>(fields);//default
 		this.isVirtual = true;//default
 		this.overloadNum = 0;//default
 		this.specificity = 0;//default
 			
 	}
 	public void initializeLocalVariables(ArrayList<InstanceField> variables){
 		this.variables.addAll(variables);
 	}
 		
 	/**
 	 * will cycle through all variables for name
 	 * returns the type of that name in arraylist [java,lang,Foo]
 	 *
 	 */
 	public String[] search_for_type(String name){
             for (Variable i : variables) {
                 if(name.equals(i.var_name)){
                     return new String[]{i.packages,i.type};
                 }
             }
			System.out.println("YOU DID NOT UPDATE TYPE SOMEWHERE");
             return null; //error type does not exist
 	}
 	
 	/**
 	 * will cycle throu all variables for name
 	 * and then update the type to the new type
 	 * will create a new LocalVariable if none exists
 	 */
 	public void update_type(String name, String newpack, String newtype) {
             boolean found = false;

             for (Variable v : variables) {
                 if (name.equals(v.var_name)) {
                     found = true;
                     v.type = newtype;
                     v.packages = newpack;
                 }
             }
             if (!found) {
                 variables.add(new Variable(newpack,newtype,name));
             }
 	}
 	/*
 	 * used for checks in accessability of overloading 
 	 */
 	public boolean isprivate(){
             return modifiers.contains("private");
 	}
 	/*
 	 * possibly better than having a isVirtual field in every declaration
 	 */
 	public boolean isVirtual(){
 		boolean is=true;
 		is= !modifiers.contains("private");
 		is= !modifiers.contains("static");
 		return is;
 	}
 
 }
 /*
  * defines a Local Variable 
  * 
  */
 class Variable{
 	String packages;
 	String type;
 	String var_name;
 	Variable(String packages, String type, String name) {
 		this.packages = packages;
 		this.type = type;
 		this.var_name = name;
 	}
 
 }
 /*
  * defines a Fparam (full parameter)
  *
  */
 class Fparam {
 	public ArrayList<String> modifiers;
 	public String type;
 	public String var_name;
         Fparam(String type, String var_name) {
             this.type = type;
             this.var_name = var_name;
             this.modifiers = new ArrayList<String>(0);
         }
 	Fparam(ArrayList<String> mods, String type, String var_name) {
             this(type, var_name);
             this.modifiers = mods;   
 	}
 }
