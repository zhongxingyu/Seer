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
  * @author Robert Englander
  */
 public class IntegerVariable extends RuleVariable {
     private static final long serialVersionUID = 1L;
 
     // the value
     private Integer value = null;
     private Integer parameterDefault = null;
 
     public IntegerVariable() {
 
     }
     
     public IntegerVariable(String name) {
         super(name);
     }
 
     public IntegerVariable(String name, String value) {
         super(name);
         this.value = new Integer(value).intValue();
     }
 
     public IntegerVariable(String name, int value) {
         super(name);
         this.value = value;
     }
     
     public boolean isNull() {
     	return null == value;
     }
 
     public String getType() {
         return "Integer";
     }
     
     public void resetValue() {
	if (this.getHasParameterDefault()) {
 	    value = parameterDefault;
 	}
     }
     
     public void setParameterDefaultValue(String val) {
 	if (val == null) {
 	    parameterDefault = null;
 	} else {
 	    parameterDefault = new Integer(val);
 	}
 	setHasParameterDefault(true);
     }
     
     public void setValueUnchecked(int val) {
         this.value = val;
 
         if (runtime != null) {
             runtime.variableChange(this);
         }
     }
     
     public void setValue(int val) {
         checkUserMutable();
         setValueUnchecked(val);
     }
 
     protected void setValue(IValue val) {
         checkUserMutable();
         if (val instanceof RuleVariable) {
             RuleVariable rv = (RuleVariable)val;
             setValue(rv);
         } else {
             setValue(val.valueAsInteger());
         }
     }
 
     private void setValue(RuleVariable rv) {
     	if (rv instanceof IntegerVariable) {
     	    IntegerVariable dv = (IntegerVariable)rv;
     	    value = dv.value;
     	} else {
     	    Integer sval = rv.valueAsInteger();
     	    if (sval == null) {
     		value = null;
     	    } else {
     		value = sval;
     	    }
     	}
     }
 
     public Object valueAsObject() {
     	if (isNull()) {
     	    return null;
     	}
         return new Integer(value);
     }
 
     public String valueAsString() {
     	if (isNull()) {
     	    return null;
     	}
         return Integer.toString(value);
     }
 
     public Double valueAsDouble() {
     	if (isNull()) {
     	    return null;
     	}
         return (double)value.intValue();
     }
 
     public Integer valueAsInteger() {
     	if (isNull()) {
     	    return null;
     	}
         return value;
     }
 
     public Boolean valueAsBoolean() {
     	if (isNull()) {
     	    return null;
     	}
         return value.intValue() == 1;
     }
 
     @Override
     public ResultVariable valueAsResult() {
         ResultVariable res = new ResultVariable(null, valueAsBoolean());
         res.setResultCode(this.value);
         return res;
     }
 
     public IValue evaluate() {
         return new IntegerVariable(null, value);
     }
 
 }
