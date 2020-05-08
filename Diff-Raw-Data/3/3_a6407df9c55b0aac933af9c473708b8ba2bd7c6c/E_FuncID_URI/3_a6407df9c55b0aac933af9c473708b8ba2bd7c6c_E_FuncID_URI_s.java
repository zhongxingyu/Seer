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
 import at.jku.xlwrap.map.expr.func.XLExprFunction;
 import at.jku.xlwrap.map.expr.val.E_String;
 import at.jku.xlwrap.map.expr.val.XLExprValue;
 import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
 import java.util.Date;
 import org.freshwaterlife.fishlink.ZeroNullType;
 import org.freshwaterlife.fishlink.FishLinkException;
 
 /**
  * @author dorgon
  * @author Christian
  *
  */
 public class E_FuncID_URI extends E_Func_with_zero {
 
     /**
      * default constructor
      */
     public E_FuncID_URI() {
     }
 
     private boolean ignore(XLExprValue<?> expression, XLExprValue<?> zeroToNullExpr) throws XLWrapException{
         Object value;
         if (expression == null){
             value = null;
         } else {
             value = expression.getValue();
         }
         String zeroToNullString = zeroToNullExpr.getValue().toString();
         ZeroNullType zeroNullType;
         try {
             zeroNullType = ZeroNullType.parse(zeroToNullString);
         } catch (FishLinkException ex) {
             throw new XLWrapException(ex);
         }
         switch (zeroNullType){
             case KEEP: 
                 return value == null;
             case NULLS_AS_ZERO:
                 return true;
             case ZEROS_AS_NULLS:
                 return isZero(value);
             default:
                 throw new XLWrapException("Unexpected ZeroNullType: "+ zeroNullType);
         }
      }
 
     final XLExprValue<String> doEval(ExecutionContext context, String specific) throws XLWrapException, XLWrapEOFException {
         // ignores actual cell value, just use the range reference to determine row
         String url = getArg(0).eval(context).getValue().toString();
 
         //Check ID/Value column is not ignore
         if (ignore (getArg(1).eval(context), getArg(2).eval(context))){
             return null;
         }
         if (args.size() == 5){
             //Check Data column is not ignore
             if (ignore (getArg(3).eval(context), getArg(4).eval(context))){
                 return null;
             }
         }
         return new E_String(url + specific);
     }
 
     @Override
     public XLExprValue<String> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
         String prefix = getArg(0).eval(context).getValue().toString();
         XLExprValue<?> value1 = getArg(1).eval(context);
         String valueString;
         if (value1 == null){
             //Set it to "0" incase NullsAsZero. Other case will be nulled by ignore
             valueString = "0";
         } else {
             valueString = value1.toString();
         }
         return doEval(context, valueString);
     }
 	
 }
