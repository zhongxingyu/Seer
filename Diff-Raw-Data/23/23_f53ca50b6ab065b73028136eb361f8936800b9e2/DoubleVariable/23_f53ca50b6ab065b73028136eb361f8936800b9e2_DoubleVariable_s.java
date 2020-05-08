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
  *
  * @author Robert Englander
  */
 public class DoubleVariable extends RuleVariable {
     private static final long serialVersionUID = 1L;
 
     // the value
     private Double value = null;
     private Double parameterDefault = null;
     
     public DoubleVariable() {
     }
     
     public DoubleVariable(String name) {
         super(name);
     }
 
     public DoubleVariable(String name, double value) {
         super(name);
         this.value = value;
     }
 
     public DoubleVariable(String name, String value) {
         super(name);
         this.value = new Double(value);
     }
 
     public void setParameterDefaultValue(String val) {
 	if (val == null) {
 	    parameterDefault = null;
 	} else {
 	    parameterDefault = new Double(val);
 	}
 	setHasParameterDefault(true);
     }
     
     public boolean isNull() {
     	return null == value;
     }
     
     public String getType() {
         return "Double";
     }
     
     public void resetValue() {
	if (this.getHasParameterDefault()) {
 	    value = parameterDefault;
 	}
     }
     
     protected void setValue(IValue val) {
         if (val instanceof RuleVariable) {
             RuleVariable rv = (RuleVariable)val;
             setValue(rv);
         } else {
             value = val.valueAsDouble();
         }
 
         if (runtime != null) {
             runtime.variableChange(this);
         }
     }
 
     private void setValue(RuleVariable rv) {
     	if (rv instanceof DoubleVariable) {
     	    DoubleVariable dv = (DoubleVariable)rv;
     	    value = dv.value;
     	} else {
     	    Double sval = rv.valueAsDouble();
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
 
     	return new Double(value);
     }
 
     public String valueAsString() {
     	if (isNull()) {
     	    return null;
     	}
 
     	return Double.toString(value);
     }
 
     public Double valueAsDouble() {
     	if (isNull()) {
     	    return null;
     	}
 
     	return value;
     }
 
     public Integer valueAsInteger() {
     	if (isNull()) {
     	    return null;
     	}
 
     	return value.intValue();
     }
 
     public Boolean valueAsBoolean() {
     	if (isNull()) {
     	    return null;
     	}
 
     	return value == 1.0;
     }
 
     @Override
     public ResultVariable valueAsResult() {
         ResultVariable res = new ResultVariable(null, valueAsBoolean());
         return res;
     }
 
     public IValue evaluate() {
         return new DoubleVariable(null, value);
     }
 
 }
