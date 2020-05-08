 /**
  * Copyright 2009 Andreas Langegger, andreas@langegger.at, Austria
  * Copyright 2011 Christian Brenninkmeijer, Brenninc@cs.man.ac.uk, UK
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.freshwaterlife.fishlink.xlwrap.expr.func.spreadsheet;
 
 import at.jku.xlwrap.common.XLWrapException;
 import at.jku.xlwrap.exec.ExecutionContext;
 import at.jku.xlwrap.map.expr.XLExpr;
 import at.jku.xlwrap.map.expr.func.XLExprFunction;
 import at.jku.xlwrap.map.expr.val.E_Boolean;
 import at.jku.xlwrap.map.expr.val.E_String;
 import at.jku.xlwrap.map.expr.val.XLExprValue;
 import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
 import java.util.Date;
 
 /**
  * @author dorgon
  * @author Christian
  *
  */
 public class E_FuncID_URI extends XLExprFunction {
 
	/**
	 * default constructor
	 */
	public E_FuncID_URI() {
	}
 
     private boolean ignore(XLExprValue<?> expression, XLExprValue<?> ignoreZeros) throws XLWrapException{
         if (expression == null){
             return true;
         }
         Object value = expression.getValue();
         if (value == null){
             return true;
         }
         if (((E_Boolean)ignoreZeros).getValue().booleanValue()){
             if (value instanceof Number){
                 int i = ((Number)value).intValue();
                 return i == 0;
             }
             if (value instanceof String){
                 return value.toString().equals("0");
             }
             if (value instanceof Boolean){
                 return false;
             }
             if (value instanceof Date){
                 return (((Date)value).getTime() == 0);
             }
             throw new XLWrapException("Expected type in Cell_URI " + value.getClass());
         } else {
             return false;
         }
     }
 
	protected final XLExprValue<String> doEval(ExecutionContext context, String specific) throws XLWrapException, XLWrapEOFException {
		// ignores actual cell value, just use the range reference to determine row
         String url = getArg(0).eval(context).getValue().toString();
 
         if (args.size() == 3){
             if (ignore (getArg(1).eval(context), getArg(2).eval(context))){
                 return null;
             }
         }
         if (args.size() == 4){
             if (ignore (getArg(1).eval(context), getArg(3).eval(context))){
                 return null;
             }
             if (ignore (getArg(2).eval(context), getArg(3).eval(context))){
                 return null;
             }
         }
		return new E_String(url + specific);
	}
 
	@Override
	public XLExprValue<String> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
         String prefix = getArg(0).eval(context).getValue().toString();
         XLExprValue<?> value1 = getArg(1).eval(context);
         if (value1 == null){
             return null;
         }
		return doEval(context, value1.toString());
	}
 	
 }
