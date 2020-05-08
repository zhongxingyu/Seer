 /*******************************************************************************
  * Copyright (C) 2010, Matthias Sohn <matthias.sohn@sap.com>
  * Copyright (C) 2010, Stefan Lay <stefan.lay@sap.com>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package org.eclipse.example.calc.internal;
 
 import org.eclipse.example.calc.BinaryOperation;
 import org.eclipse.example.calc.Operation;
 import org.eclipse.example.calc.Operations;
 import org.eclipse.example.calc.UnaryOperation;
 import org.eclipse.example.calc.internal.operations.Equals;
 import org.eclipse.example.calc.internal.operations.Minus;
 import org.eclipse.example.calc.internal.operations.Plus;
 import org.eclipse.example.calc.internal.operations.Square;
 
 public class Calculator {
 
 	private TextProvider textProvider;
 
 	private String cmd;
 
 	private boolean clearText;
 
 	private float value;
 
 	public static String NAME = "Simple Calculator";
 
 	public Calculator(TextProvider textProvider) {
 		this.textProvider = textProvider;
 		setupDefaultOperations();
 	}
 
 	private void setupDefaultOperations() {
 		new Equals();
 		new Minus();
 		new Plus();
 		new Square();
 		new Divide();
 	}
 
 	private void calculate(String cmdName) {
 		float curValue;
 		float newValue = 0;
 
 		// get current value of display
 		curValue = Float.parseFloat(textProvider.getDisplayText());
 
 		Operation currentOp = Operations.INSTANCE.getOperation(cmdName);
 		if ((currentOp instanceof BinaryOperation) && (cmd == null)) {
 			// if last clicked operation was binary and there is no saved
 			// operation, store it
 			cmd = cmdName;
 			setClearText(true);
 		} else {
 			// if saved command is binary perform it
 			Operation savedOp = Operations.INSTANCE.getOperation(cmd);
 			if (savedOp instanceof BinaryOperation) {
 				BinaryOperation bop = (BinaryOperation) savedOp;
 				newValue = bop.perform(value, curValue);
 			} // if current operation is unary perform it
 			else if (currentOp instanceof UnaryOperation) {
 				UnaryOperation uop = (UnaryOperation) currentOp;
 				newValue = uop.perform(curValue);
 			}
 
 			// display the result and prepare clear on next button
 			textProvider.setDisplayText("" + newValue);
 			setClearText(true);
 			if (currentOp instanceof Equals) {
 				// do not save "=" command
 				cmd = null;
 			} else if (currentOp instanceof BinaryOperation) {
 				// save binary commands as they are executed on next operation
 				cmd = cmdName;
 			} else {
 				// clear saved command
 				cmd = null;
 			}
 		}
 
 	}
 
 	private boolean isCommand(String name) {
 		return (Operations.INSTANCE.getOperation(name) != null);
 	}
 
 	public void handleButtonClick(String str) {
 		if (isCommand(str)) {
 			calculate(str);
 		} else {
 			char digit = (str.toCharArray())[0];
 			if (Character.isDigit(digit) || digit == '.') {
 				if (clearText) {
 					// save current value and clear the display
 					value = Float.parseFloat(textProvider.getDisplayText());
 					textProvider.setDisplayText("");
 					setClearText(false);
 				}
 
 				// add new digit to display
 				textProvider.setDisplayText(textProvider.getDisplayText() + digit);
 			}
 		}
 	}
 
 	public void setClearText(boolean clearText) {
 		this.clearText = clearText;
 	}
 }
