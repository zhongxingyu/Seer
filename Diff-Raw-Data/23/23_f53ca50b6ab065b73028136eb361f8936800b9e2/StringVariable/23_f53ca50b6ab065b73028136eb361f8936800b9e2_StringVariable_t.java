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
 
 import java.util.List;
 
 /**
  * @author Robert Englander
  */
 public class StringVariable extends RuleVariable {
     private static final long serialVersionUID = 1L;
 
     private static enum Methods {
         length,
         makeUpperCase,
         subString,
         contains;
         
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
     private String value = null;
     private String parameterDefault = null;
 
     public StringVariable() {
 
     }
     
     public StringVariable(String name) {
         super(name);
     }
 
     public StringVariable(String name, String value) {
         super(name);
         this.value = value;
     }
     
     public boolean isNull() {
     	return null == value;
     }
 
     public String getType() {
         return "String";
     }
     
     public void resetValue() {
	if (this.getHasParameterDefault() && value == null) {
 	    value = parameterDefault;
 	}
     }
     
     public void setParameterDefaultValue(String val) {
 	parameterDefault = val;
 	setHasParameterDefault(true);
     }
     
     @Override
     public boolean supportsMacroExpansion() {
 	return true;
     }
 
     public boolean methodHasReturn(String name) {
     	if (Methods.contains(name)) {
             switch (Methods.valueOf(name)) {
                 case length:
                 case subString:
                 case contains:
                     return true;
             }
     	}
 
         return super.methodHasReturn(name);
     }
 
     public int getMethodParameterCount(String name) {
     	if (Methods.contains(name)) {
             switch (Methods.valueOf(name)) {
                 case length:
                     return 0;
                 case makeUpperCase:
                     return 0;
                 case subString:
                     return 2;
                 case contains:
                     return 1;
             }
     	}
 
         return super.getMethodParameterCount(name);
     }
 
     public IValue executeMethod(String name, List<IValue> params) {
     	if (Methods.contains(name)) {
             switch (Methods.valueOf(name)) {
                 case length:
                     return getLength(params);
                 case makeUpperCase:
                     makeUpperCase(params);
                     return new BooleanVariable(null,true);
                 case subString:
                     return subString(params);
                 case contains:
                     return contains(params);
             }
     	}
 
         return super.executeMethod(name, params);
     }
 
     /**
      * determines if the passed string parameter is contained
      * within this string
      * @param params
      * @return
      */
     private IValue contains(List<IValue> params) {
         String sub = params.get(0).valueAsString();
         boolean contains = -1 != value.indexOf(sub);
         return new BooleanVariable(null, contains);
     }
 
     private IValue getLength(List<IValue> params) {
         // we don't use any parameters
         int size = value.length();
         return new IntegerVariable(null, size);
     }
 
     private void makeUpperCase(List<IValue> params) {
         // this function doesn't use any parameters
         setValue(new StringVariable(null, value.toUpperCase()));
     }
 
     /**
      * subString is used for obtaining a segment of this string, hence it's usage is akin to the underlying
      * Java method function of substring(int beginIndex, int endIndex), with the first character being index 0
      * @param params  beginIndex - the beginning index, inclusive.
      *                endIndex - the ending index, exclusive.
      * @return    a new string that is a substring of this string. The substring begins at the specified beginIndex and extends to the character at index endIndex - 1. Thus the length of the substring is endIndex-beginIndex
      * Throws:
      *       IndexOutOfBoundsException - if the beginIndex is negative, or endIndex is larger than the length of this String object, or beginIndex is larger than endIndex.
      */
     private IValue subString(List<IValue> params) {
         int beginIndex = params.get(0).valueAsInteger();
         int endIndex = params.get(1).valueAsInteger();
         String substring = value.substring(beginIndex, endIndex);
         return new StringVariable("",substring);
     }
 
     protected void setValue(IValue val) {
         if (val instanceof RuleVariable) {
             RuleVariable rv = (RuleVariable)val;
             setValue(rv);
         } else {
             value = val.valueAsString();
         }
 
         if (runtime != null) {
             runtime.variableChange(this);
         }
     }
 
     private void setValue(RuleVariable rv) {
 	value = renderAsString(rv.valueAsString());
     }
 
     protected String renderAsString(String val) {
 	String v = val;
 	if (isMacro()) {
 	    v = runtime.performMacroExpansion(v);
 	}
 	
 	return v;
     }
     
     public Object valueAsObject() {
         return value;
     }
 
     public String valueAsString() {
 	return renderAsString(value);
     }
 
     public Double valueAsDouble() {
     	if (isNull()) {
     	    return null;
     	}
 
     	double result = Double.valueOf(value).doubleValue();
         return result;
     }
 
     public Integer valueAsInteger() {
     	if (isNull()) {
     	    return null;
     	}
 
     	// we need to truncate doubles...
         int result = valueAsDouble().intValue();
         return result;
     }
 
     public Boolean valueAsBoolean() {
     	if (isNull()) {
     	    return null;
     	}
 
     	return value.equals("true");
     }
 
     @Override
     public ResultVariable valueAsResult() {
         ResultVariable res = new ResultVariable(null, valueAsBoolean());
         return res;
     }
 
     public IValue evaluate() {
         return new StringVariable(null, valueAsString());
     }
 
 }
