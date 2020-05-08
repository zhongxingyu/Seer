 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Tanja Mayerhofer - initial API and implementation
  */
 package org.modelexecution.fumldebug.core.behaviorlibrary;
 
 import fUML.Debug;
 import fUML.Semantics.Classes.Kernel.IntegerValue;
 import fUML.Semantics.Classes.Kernel.Value;
 import fUML.Semantics.Classes.Kernel.ValueList;
 
 public class ListIndexOfFunctionBehaviorExecution extends
         LibraryBehavior {
 
     public void doBody(
             fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValueList inputParameters,
             fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValueList outputParameters) {
     	
     	// Get the list for which to find the element from the first argument
     	ValueList vl = (ValueList) inputParameters.getValue(0).values;
 
     	// Get the object to be found in the list from the second argument
     	Value iv = (Value) inputParameters.getValue(1).values.getValue(0);
 
 		Debug.println("[doBody] List IndexOf");
     	
		int index = vl.indexOf(iv);
 		
 		// Return the index in an IntegerValue object
 		IntegerValue result = new IntegerValue();
     	result.value = index + 1;
     	result.type = this.locus.factory.getBuiltInType("Integer");
 
         Debug.println("[doBody] List IndexOf, result=" + result.value);
 
 		// Add output to the outputParameters list
 		addValueToOutputList(result, outputParameters);
 		
     }
     
     public fUML.Semantics.Classes.Kernel.Value new_() {
         // Create a new instance of this kind of function behavior execution.
         return new ListIndexOfFunctionBehaviorExecution();
     }
 
 }
