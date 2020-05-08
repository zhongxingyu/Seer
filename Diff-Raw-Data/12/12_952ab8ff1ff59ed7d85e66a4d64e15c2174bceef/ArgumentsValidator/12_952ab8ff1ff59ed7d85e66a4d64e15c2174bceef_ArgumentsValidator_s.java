 package com.nsn.uwr.panio;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.nsn.uwr.panio.inputsparser.EOperand;
 
 public class ArgumentsValidator {
 
 	public void validateArguments(String[] args) {
 		if (args.length != 3)
 			throw new RuntimeException("illegal number of arguments");
 		if (!isValidNumber(args[0]))
 			throw new IllegalArgumentException(
 					"first argument should be numeric");
 		if (!isValidNumber(args[1]))
 			throw new IllegalArgumentException(
 					"second argument should be numeric");
 		if (!isValidOperator(args[2]))
 			throw new IllegalArgumentException(
 					"third argument should be operator");
 	}
 
 	@VisibleForTesting
 	protected boolean isValidNumber(String item) {
 		try {
 			Double.parseDouble(item);
 		} catch (NumberFormatException e) {
 			return false;
 		}
 
 		return true;
 	}
 
 	@VisibleForTesting
 	protected boolean isValidOperator(String item) {
 		try {
 			EOperand.match(item);
 		} catch (IllegalArgumentException e) {
 			return false;
 		}
 		return true;
 	}
 
 }
