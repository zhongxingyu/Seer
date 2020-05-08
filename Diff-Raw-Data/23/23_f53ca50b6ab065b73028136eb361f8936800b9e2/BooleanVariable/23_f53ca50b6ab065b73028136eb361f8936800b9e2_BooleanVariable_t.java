 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.cornerstone.scripting;
 
 
 /**
  * A BooleanVariable is used to represent local Boolean type
  * variables, as well as anonymous boolean values.
  *
  * @author Robert Englander
  */
 public class BooleanVariable extends RuleVariable {
     private static final long serialVersionUID = 1L;
 
     // the value
     private Boolean value = null;
     private Boolean parameterDefault = null;
     
     public BooleanVariable() {
     }
 
     /**
      * Constructs the variable
      * @param name the name of the variable, or null if anonymous
      */
     public BooleanVariable(String name) {
         super(name);
     }
 
     public boolean isNull() {
     	return null == value;
     }
     
     public String getType() {
         return "Boolean";
     }
     
     public void resetValue() {
	if (this.getHasParameterDefault() && value == null) {
 	    value = parameterDefault;
 	}
     }
     
     public void setParameterDefaultValue(String val) {
 	if (val == null) {
 	    parameterDefault = null;
 	} else {
 	    parameterDefault = new Boolean(val);
 	}
 	setHasParameterDefault(true);
     }
     
     public BooleanVariable(String name, boolean value) {
         super(name);
         this.value = value;
     }
 
     public void setValue(boolean val) {
         value = val;
 
         if (runtime != null) {
             runtime.variableChange(this);
         }
     }
 
     protected void setValue(IValue val) {
         if (val instanceof RuleVariable) {
             RuleVariable rv = (RuleVariable)val;
             setValue(rv);
         } else {
             setValue(val.valueAsBoolean());
         }
     }
 
     private void setValue(RuleVariable rv) {
     	if (rv instanceof BooleanVariable) {
     		BooleanVariable dv = (BooleanVariable)rv;
     		value = dv.value;
     	} else {
     		Boolean sval = rv.valueAsBoolean();
     		if (sval == null) {
     			value = null;
     		} else {
     			value = sval;
     		}
     	}
     }
 
     /**
      * @return the value as an Object
      */
     public Object valueAsObject()
     {
     	if (isNull()) {
     		return null;
     	}
 
     	return new Boolean(valueAsBoolean());
     }
 
     /**
      * @return the value as a String
      */
     public String valueAsString()
     {
     	if (isNull()) {
     		return null;
     	}
 
     	if (value == true)
         {
             return "true";
         }
         else
         {
             return "false";
         }
     }
 
     /**
      * @return the value as a double
      */
     public Double valueAsDouble()
     {
     	if (isNull()) {
     		return null;
     	}
     	
         if (value == true)
         {
             return 1.0;
         }
         else
         {
             return 0.0;
         }
     }
 
     /**
      * @return the value as an int
      */
     public Integer valueAsInteger()
     {
     	if (isNull()) {
     		return null;
     	}
 
     	if (value == true)
         {
             return 1;
         }
         else
         {
             return 0;
         }
     }
 
     /**
      * @return the value as a boolean
      */
     public Boolean valueAsBoolean()
     {
     	if (isNull()) {
     		return null;
     	}
 
     	return value;
     }
 
     @Override
     public ResultVariable valueAsResult() {
         ResultVariable res = new ResultVariable(null, valueAsBoolean());
         return res;
     }
 
     /**
      * evaluates this variable without returning its value.
      */
     public IValue evaluate()
     {
         // this is a no-op
         return this;
     }
 
 }
