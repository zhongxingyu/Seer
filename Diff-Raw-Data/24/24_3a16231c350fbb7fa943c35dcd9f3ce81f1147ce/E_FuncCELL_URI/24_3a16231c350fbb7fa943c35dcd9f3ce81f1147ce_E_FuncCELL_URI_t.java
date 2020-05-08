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
 
 import at.jku.xlwrap.common.Utils;
 import at.jku.xlwrap.common.XLWrapException;
 import at.jku.xlwrap.exec.ExecutionContext;
 import at.jku.xlwrap.map.expr.E_RangeRef;
 import at.jku.xlwrap.map.expr.func.FunctionRegistry;
 import at.jku.xlwrap.map.expr.val.XLExprValue;
 import at.jku.xlwrap.map.range.CellRange;
 import at.jku.xlwrap.map.range.Range;
 import at.jku.xlwrap.spreadsheet.XLWrapEOFException;
 
 /**
 * @author Christian
  *
  */
 public class E_FuncCELL_URI extends E_FuncID_URI {
 
 	/**
 	 * default constructor
 	 */
 	public E_FuncCELL_URI() {
 	}
 
     @Override
 	public XLExprValue<String> eval(ExecutionContext context) throws XLWrapException, XLWrapEOFException {
 		// ignores actual cell value, just use the range reference to determine row
 
 		if (!(args.get(1) instanceof E_RangeRef))
 			throw new XLWrapException("Argument " + args.get(1) + " of " + FunctionRegistry.getFunctionName(this.getClass()) + " must be a cell range reference.");
 		Range absolute = ((E_RangeRef) args.get(1)).getRange().getAbsoluteRange(context);
 		if (!(absolute instanceof CellRange))
 			throw new XLWrapException("Argument " + args.get(1) + " of " + FunctionRegistry.getFunctionName(this.getClass()) + " must be a cell range reference.");
  		int row = ((CellRange) absolute).getRow() + 1;
         String column = Utils.indexToAlpha(((CellRange) absolute).getColumn());
 		return doEval(context, column + row);
 	}
 	
 }
