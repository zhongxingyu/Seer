 package org.translator.java;
 
 import java.util.ArrayList;
 
 /**
  * @author Ramya Gadiyaram
  *
  */
 public class ComponentCall {
 	
 	private ArrayList<String> arguments;
     private String method;
 	private String calledComponentName;
 	private String var;
 	//this variable, marker, is used to mark a blank component call object if its value is set to true, which indicates that the next
     //statement in the event or method is something other than a function call, meaning that it cant be conveniently
 	//represented by this data structure. the type of the statement is passed in and set as the calledComponentName
 	//variable using the secondary constructor for this class. when the code generation algorithm encounters
 	//an componentcall object with marker set to true, it will see what the type of the next statement should be
 	//using the string in calledComponentName and then pop a value off of the front of the corresponding list
 	//in the Event object and generate the code for it in getActions in class JavaGenerator
 	private boolean marker;
 	public ComponentCall(String calledComponentName) {
 		this.setCalledComponentName(calledComponentName);
 		var=null;
 		arguments=new ArrayList<String>();
 		marker=false;
 	}
 	//secondary constructor
 	public ComponentCall(String type, String blank){
 		this.setCalledComponentName(type);
 		marker=true;
 		var=null;
 		arguments=null;
 		method=null;
 	}
 	/**
 	 * Getter and Setter methods for the members of ComponentCall Objects
 	 */
 	public boolean getMarker(){
 		return marker;
 	}
 	public void setMarker(boolean b){
 		marker=b;
 	}
 	public void setMethod(String method) {
 		this.method = method;
 	}
 	public void setVar(String var){
 		this.var=var;
 	}
 	public String getVar(){
 		return var;
 	}
 	public void setArgs(ArrayList args){
 		for (ArrayList<String> arg : (ArrayList<ArrayList>)args)
 			for(String s : arg)
			{
 					arguments.add(s);
 			}
 		//arguments=args;
 	}
 	public String getMethod() {
 		
 		return method;
 	}
 
 	public ArrayList<String> getArguments() {
 		return arguments;
 	}
 
 	public void addArgument(String arg) {
 		arguments.add(arg);
 	}
 
 	public String getCalledComponentName() {
 		return calledComponentName;
 	}
 
 	public void setCalledComponentName(String calledComponentName) {
 		this.calledComponentName = calledComponentName;
 	}
 	
 	@Override
 	public String toString() {
 		return this.calledComponentName+" "+method;
 	}
 
 }
