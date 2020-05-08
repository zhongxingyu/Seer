 /*
  * Copyright (c) 2012 Joe Rowley
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.mobileobservinglog.strategies;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 
 public class DatePickerDayPicker extends NumberPickerDriver {
 	DatePicker parent;
 	
 	public DatePickerDayPicker(DatePicker parent, String currentValue, Context context) {
 		super(setDays(), currentValue, context);
 		this.parent = parent;
 	}
 
 	public DatePickerDayPicker(ArrayList<String> values, String currentValue,
 			Context context) {
 		super(values, currentValue, context);
 	}
 	
 	private static ArrayList<String> setDays() {
 		ArrayList<String> retVal = new ArrayList<String>();
 		for(int i = 1; i <= 31; i++) {
 			retVal.add(String.format("%02d", i));
 		}
 		return retVal;
 	}
 	
 	public void skipToValue(String value) {
 		currentValue = value;
 		int iteratorIndex = potentialValues.indexOf(currentValue);
 		iterator = potentialValues.listIterator(iteratorIndex);
 		parent.client.updateModalTextTwo(currentValue);
 	}
 	
 	@Override
 	public void upButton() {
 		super.upButton();
 		if(Integer.parseInt(currentValue) > Integer.parseInt(parent.month.getMaxDays(parent.month.getCurrentValue()))) {
			skipToValue("01");
 		}
 	}
 	
 	@Override
 	public void downButton() {
 		super.downButton();
 		if(Integer.parseInt(currentValue) > Integer.parseInt(parent.month.getMaxDays(parent.month.getCurrentValue()))) {
 			skipToValue(parent.month.getMaxDays(parent.month.getCurrentValue()));
 		}		
 	}
 
 	@Override
 	public boolean save() {
 		return false;
 	}
 
 }
