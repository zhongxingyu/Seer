 package org.tekkotsu.api;
 import java.util.ArrayList;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.swt.graphics.Color;
 
 //Node class for the Tekkotsu State Machine Composer
 //Author: Albert Berk Toledo
 //Start date: 06/17/2013
 
 
 //Class Header
 public class NodeClass extends Graphical{
 	
 	//Attributes
 	private String name;						//Name of the node
 	private String definition;					//Definition of the node
 	private SetupMachine setup;					//List of the nodes contained
 	private ArrayList<ConstructorCall> parents;	//Constructors of parents
 	private ConstructorCall constructor;		//Constructor						//Color of the node
 	private ArrayList<Method> methods;			//Methods of the class
 	private ArrayList<Variable> variables;		//Variables withing the class.
 	private ArrayList<NodeClass> subClasses;	//Node classes defined in this class.
 	private NodeClass parent;					//Parent class if this is a subclass
 
 	//static ID's
 	public static final String PROPERTY_NAME = "Node_ClassName";
 	
 	
 	/*
 	//Full argument constructor
 	public NodeClass(String name, SetupMachine setup, ConstructorCall constructor, String color, String definition, ArrayList<ConstructorCall> parents){
 		
 		this.name = name;
 		this.setup = setup;
 		this.constructor = constructor;
 		this.color = color;
 		this.definition = definition;
 		this.parents = parents;
 	} */
 	
 	
 	//3 Argument Constructor. Sets default values for the rest of the fields.
 	public NodeClass(String name, ConstructorCall constructor){
 		super();
 		super.setColor(ColorConstants.yellow);
 		this.name = name;
 		this.setup = null;					//No setup machine by default.
 		this.constructor = constructor;
 		this.definition = "New Node Class: " + name;		//TODO default definition
 		this.parents = new ArrayList<ConstructorCall>();
 		this.methods = new ArrayList<Method>();
 		this.subClasses = new ArrayList<NodeClass>();
 		this.variables = new ArrayList<Variable>();
 		this.parent = null;
 	}
 	
 	//Constructor for a nodeclass that serves as a statemachine to be used as a behavior.
 	public NodeClass(String name, SetupMachine setup){
 		super();
 		super.setColor(ColorConstants.yellow);
 		this.name = name;
 		this.setup = setup;
 		this.constructor = null;
 		this.definition = "New Node Class: " + name;
 		this.parents = new ArrayList<ConstructorCall>();
 		this.methods = new ArrayList<Method>();
 		this.subClasses = new ArrayList<NodeClass>();
 		this.variables = new ArrayList<Variable>();
 		this.parent = null;
 	}
 	
 
 
 	//Accessor methods
 	public String getName(){
 		return name;
 	}
 	
 	public String getDefinition(){
 		return definition;
 	}
 	
 	public SetupMachine getSetupMachine(){
 		return setup;
 	}
 	
 	public ArrayList<Parameter> getParameters(){
 		return constructor.getParameters();
 	}
 	
 	
 	/*public Color getColor(){
 		return super.color;
 	}*/
 	
 	public int getNumOfParameters(){
 		return this.constructor.getParameters().size();
 	}
 	
 	public ArrayList<ConstructorCall> getParents(){
 		return parents;
 	}
 	
 	public ConstructorCall getConstructor(){
 		return constructor;
 	}
 	
 	public ArrayList<Method> getMethods(){
 		return methods;
 	}
 	
 	public Method getMethod(int i){
 		return methods.get(i);
 	}
 	
 	public ArrayList<NodeClass> getSubClasses(){
 		return subClasses;
 	}
 	
 	public ArrayList<Variable> getVariables(){
 		return variables;
 	}
 	
 	public NodeClass getParent(){
 		return parent;
 	}
 	
 
 
 
 
 	//Mutator methods
 	public void setName(String name){
 		this.name = name;
 		getListeners().firePropertyChange(PROPERTY_NAME, null, name);
 	}
 	
 	public void setDefinition(String definition){
 		this.definition = definition;
 	}
 	
 	public void setSetupMachine(SetupMachine setup){
 		this.setup = setup;
		this.addChild2(setup);
 	}
 	
 	public void setParameters(ArrayList<Parameter> parameters){
 		this.constructor.setParameters(parameters);
 	}
 	
 	public void setConstructor(ConstructorCall constructor){
 		this.constructor = constructor;
 	}
 	
 	/*public void setColor(String color){
 		this.color = color;
 	}*/
 	
 	public void setParents(ArrayList<ConstructorCall> parents){
 		this.parents = parents;
 	}
 	
 	public void setMethods(ArrayList<Method> methods){
 		this.methods = methods;
 	}
 	
 	public void setSubClasses(ArrayList<NodeClass> subClasses){
 		this.subClasses = subClasses;
 	}
 	
 	public void setVariables(ArrayList<Variable> vars){
 		this.variables = vars;
 	}
 	
 	public void setParent(NodeClass parent){
 		this.parent = parent;
 	}
 
 
 	//Mutator methods that adds/ removes single element to/from the list attributes.
 	public void addParameter(Parameter parameter){
 		this.constructor.addParameter(parameter);
 	}
 	
 	public void removeParameter(Parameter parameter){
 		this.constructor.removeParameter(parameter);
 	}
 	
 	public void addParent(ConstructorCall parent){
 		this.parents.add(parent);
 	}
 	
 	public void removeParent(ConstructorCall parent){
 		this.parents.remove(parent);
 	}
 	
 	public void removeParent(int index){	//Remove with index because order matters.
 		this.parents.remove(index);
 	}
 	
 	public void addMethod(Method method){
 		this.methods.add(method);
 	}
 	
 	public void removeMethod(Method method){
 		this.methods.remove(method);
 	}
 	
 	public void removeMethod(int i){
 		this.methods.remove(i);
 	}
 	
 	public void addVariable(Variable var){
 		this.variables.add(var);
 	}
 	
 	public void removeVariable(Variable var){
 		this.variables.remove(var);
 	}
 	
 	public void removeVariable(int i){
 		this.variables.remove(i);
 	}
 	
 	public void removeMethod(Variable var){
 		this.variables.remove(var);
 	}
 
 	public void addSubClass(NodeClass subClass){
 		this.subClasses.add(subClass);
 		this.addChild2(subClass);
 	}
 	
 	public void removeSubClass(NodeClass subClass){
 		this.subClasses.remove(subClass);
 		this.removeChild(subClass);
 	}
 	
 	public void removeSubClass(int i){
 		//this.subClasses.remove(i);
 	}
 	
 	
 	
 	
 	//TODO return instance from a nodeclass object (just for convenience)
 	
 	    public NodeInstance makeInstance(){
 	    	NodeInstance instance = new NodeInstance(this);
 	    	return instance;
 	    }
 	  	
 	 
 	
 	
 }
