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
 
 import java.util.Date;
 import java.util.List;
 
 /**
  *
  * @author Robert Englander
  */
 public class DateVariable extends RuleVariable {
     private static final long serialVersionUID = 1L;
 
     private static enum Methods {
         isAfter,
         setNow,
         incrementDays,
         incrementHours,
         incrementMinutes,
         incrementSeconds;
         
 	public static boolean contains(String name) {
 	    boolean contains = false;
 	    for (Methods option : Methods.values()) {
 		if (option.toString().equals(name)) {
 		    contains = true;
 		    break;
 		}
 	    }
 		
 	    return contains;
 	}
     }
 
     // the value
     private Date value = null;
     private Date parameterDefault = null;
 
     public DateVariable() {
 
     }
     
     public DateVariable(String name) {
         super(name);
     }
 
     public DateVariable(String name, Date value) {
         super(name);
         this.value = value;
     }
 
     public DateVariable(String name, String value) {
         super(name);
         setValue(value);
     }
 
     public String getType() {
         return "Date";
     }
     
     public void resetValue() {
     	if (this.getHasParameterDefault() && value == null) {
     	    value = parameterDefault;
     	}
     }
     
 	public void setParameterDefaultValue(String val) {
     	if (val == null) {
     	    this.parameterDefault = null;
     	} else {
             try {
             	parameterDefault = new Date(java.sql.Date.valueOf(val).getTime());
             } catch (Exception e) {
                 throw new RuntimeException("Bad date format used to set date default value: " + val);
             }
     	}
 		setHasParameterDefault(true);
 	}
 
 	public boolean isNull() {
     	return null == value;
     }
 
     /**
      * Returns a boolean value that indicates if a
      * particular method has a return value.
      * @param name the method name
      * @return true if it has a return value, false otherwise.
      */
     public boolean methodHasReturn(String name) {
 	if (Methods.contains(name)) {
 	    switch (Methods.valueOf(name)) {
 	    	case isAfter:
 	    	    return true;
 	    	default:
 	    	    return false;
 	    }
 	}
 		
         return super.methodHasReturn(name);
     }
 
     public int getMethodParameterCount(String name) {
 
     	if (Methods.contains(name)) {
     	    switch (Methods.valueOf(name)) {
     	    	case incrementDays:
     	    	case incrementHours:
     	    	case incrementMinutes:
     	    	case incrementSeconds:
             	case isAfter:
                     return 1;
                 case setNow:
                     return 0;
                 default:
                     return 0;
             }
     	}
         
         return super.getMethodParameterCount(name);
     }
 
     /**
      * Executes the specified method.  The parameters to
      * the method are supplied as an array of values.
      * @param name the method name
      * @param params the parameters to pass to the method.
      * @return the return value of the method
      */
     public IValue executeMethod(String name, List<IValue> params) {
     	
     	if (Methods.contains(name)) {
     	    Methods m = Methods.valueOf(name);
             switch (m) {
     	    	case incrementDays:
     	    	case incrementHours:
     	    	case incrementMinutes:
     	    	case incrementSeconds:
     	    	    return increment(params, m);
                 case isAfter:
                     return isAfter(params);
                 case setNow:
                     setNow(params);
                     return new BooleanVariable(null, true);
                 default:
                     throw new RuntimeException("Unexpected method name: " + name);
             }
     	}
 
         return super.executeMethod(name, params);
     }
 
     public IValue increment(List<IValue> params, Methods m) {
         BooleanVariable result = new BooleanVariable(null, true);
         long inc = params.get(0).valueAsInteger();
         switch (m) {
             case incrementDays:
         	inc = inc * 24 * 60 * 60 * 1000;
 	    	break;
 	    case incrementHours:
 	    	inc = inc * 60 * 60 * 1000;
 	    	break;
 	    case incrementMinutes:
 	    	inc = inc * 60 * 1000;
 	    	break;
 	    case incrementSeconds:
 	    	inc = inc * 1000;
 	    	break;
         }
         
         long newVal = inc + value.getTime();
         Date temp = new Date(newVal);
         this.setValue(new DateVariable(null, temp), true);
         
         return result;
     }
     
     public void setNow(List<IValue> params) {
        Date temp = new Date();
        this.setValue(new DateVariable(null, temp), true);
     }
 
     public IValue isAfter(List<IValue> params) {
         // there should be 1 parameter and it has to
         // be a Date variable
         BooleanVariable result = new BooleanVariable(null);
         if (params.get(0) instanceof DateVariable) {
             if (value.compareTo((Date)params.get(0).valueAsObject()) > 0) {
                 result.setValue(true);
             } else {
                 result.setValue(false);
             }
         } else {
             throw new RuntimeException("Non Date value passed to after() method: " + getName());
         }
 
         return result;
     }
 
     @SuppressWarnings("deprecation")
     private void setValue(RuleVariable rv) {
     	if (rv instanceof DateVariable) {
     	    DateVariable dv = (DateVariable)rv;
     	    value = dv.value;
     	} else {
     	    String sval = rv.valueAsString();
     	    setValue(sval);
     	}
     }
     
     protected void setValue(IValue val) {
         if (val instanceof RuleVariable) {
         	RuleVariable rv = (RuleVariable)val;
         	setValue(rv);
         } else {
             String dstring = val.valueAsString();
             setValue(dstring);
         }
     }
     
     @SuppressWarnings("deprecation")
     private void setValue(String dt) {
     	if (dt == null) {
     	    value = null;
     	} else {
             try {
                 value = new Date(java.sql.Date.valueOf(dt).getTime());
             } catch (Exception e) {
                 throw new RuntimeException("Bad date format used to set date value: " + dt);
             }
     	}
         
         if (runtime != null) {
             runtime.variableChange(this);
         }
     }
 
     public Object valueAsObject() {
         return value;
     }
 
     /**
      * returns the date value as a String as formatted by the DateFormatter
      *  using the default constructor
      * @return
      */
     @SuppressWarnings("deprecation")
     public String valueAsString() {
     	if (value == null) {
     		return "null";
     	} 
     	
     	java.sql.Date dt = new java.sql.Date(value.getTime());
     	return dt.toString();
     }
 
     public Double valueAsDouble() {
     	if (value == null) {
     	    return null;
     	}
     	
         return Double.valueOf(value.getTime());
     }
 
     public Integer valueAsInteger() {
     	if (value == null) {
     	    return null;
     	}
 
     	return 0;
     }
 
     public Boolean valueAsBoolean() {
     	return null != value;
     }
 
     @Override
     public ResultVariable valueAsResult() {
         ResultVariable res = new ResultVariable(null, valueAsBoolean());
         return res;
     }
 
     public IValue evaluate() {
         return new DateVariable(null, value);
     }
 
 }
