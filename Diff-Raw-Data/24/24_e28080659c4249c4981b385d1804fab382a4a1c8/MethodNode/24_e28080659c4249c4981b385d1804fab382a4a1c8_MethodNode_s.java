 /**
  * Copyright (c) 2013 HAN University of Applied Sciences
  * Arjan Oortgiese
  * Boyd Hofman
  * Joëll Portier
  * Michiel Westerbeek
  * Tim Waalewijn
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package nl.han.ica.ap.purify.language.java.callgraph;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Node containing all information of a method.
  * 
  * @author Tim
  */
 public class MethodNode {
 	/** RETURNTYPE is a static final int of the position of the methods return type in ArrayList modifiers. */
 	private static final int RETURNTYPE = 0;
 	
 	/** modifiers is an ArrayList containing all modifiers of the method. */
 	private ArrayList<String> modifiers;
 	
 	/** classID is the name of the class this method belongs to. */
 	private String classID;
 	
 	/** methodID is the name of this method. */
 	private String methodID;
 	
 	/** localvariables is a HashMap of all variables in this methods scope. */
 	private HashMap<String,String> localvariables;
 	
 	/** called is a boolean to check if this method is called in the program. */
 	public boolean called = false;
 	
 	/**
 	 * Creates a new MethodNode.
 	 * @param classID The name of class this method is in.
 	 * @param methodID The name of the method.
 	 * @param modifiers The modifiers of the method.
 	 */
 	public MethodNode(String classID, String methodID, ArrayList<String> modifiers) {
 		this.classID = classID;
 		this.methodID = methodID;
 		this.modifiers = modifiers;
 		localvariables = new HashMap<String, String>();
 	}
 	
 	/**
 	 * @return Returns the name of the class this method belongs to.
 	 */
 	public String getClassID() {
 		return classID;
 	}
 	
 	/**
 	 * @return Returns the name of this method.
 	 */
 	public String getMethodID() {
 		return methodID;
 	}
 	
 	/**
 	 * @return Returns the return type of this method as a String.
 	 */
 	public String getReturnType() {
 		return modifiers.get(RETURNTYPE);
 	}
 	
 	/**
 	 * @param modifier The name of the modifier you want to test on.
 	 * @return Returns whether this method has the specified modifier.
 	 */
 	public boolean hasModifier(String modifier) {
 		for(int i = 0; i < modifiers.size(); i++) {
 			if(i == RETURNTYPE) {
 				continue;
 			}
 			if(modifier.equals(modifiers.get(i))) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Method to get the type of a variable.
 	 * @param variableID Name of the variable you want the type of.
 	 * @return Returns the type of the specified variable as a String or null if not found.
 	 */
 	public String getLocalVariable(String variableID) {
 		for(String variable : localvariables.keySet()) {
 			if(variable.equals(variableID)) {
 				return localvariables.get(variable);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Add local variable map to method.
 	 * @param variables HashMap with the variables of this method.
 	 */
 	public void setVariables(HashMap<String, String> variables) {
 		localvariables = variables;
 	}
 	
 	public HashMap<String, String> getVariables() {
 		return localvariables;
 	}
 }
