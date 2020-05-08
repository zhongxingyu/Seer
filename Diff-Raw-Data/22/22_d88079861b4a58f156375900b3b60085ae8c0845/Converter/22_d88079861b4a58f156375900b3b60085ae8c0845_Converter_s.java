 package org.codingdojo.roman.numeral.converter;
 
 import org.codingdojo.roman.numeral.type.DigitBoundaryType;
 
 public class Converter {
 
 	public String toNumeral(Integer input) {
 		if (input > 3000) {
 			throw new IllegalArgumentException("Sorry, we can calculate only upto 3000");
 		}
 		StringBuilder numeral = new StringBuilder();
 		numeral.append(toNumeral(input, DigitBoundaryType.THOUSAND.getValue()));
 
 		return numeral.toString();
 	}
 
 	private String toNumeral(Integer input, int limit) {
 		StringBuilder numeral = new StringBuilder();
 		int firstValue = input / limit;
 		int reminder = input % limit;
 		DigitBoundaryType limitType = DigitBoundaryType.findType(limit);
 		for (int i = 0; i < firstValue; i++) {
 			numeral.append(limitType.getSymbol());
 		}
 		numeral.append(appendRecursiveValue(reminder, limitType));
 		return numeral.toString();
 	}
 
 	private String appendRecursiveValue(int reminder, DigitBoundaryType limitType) {
 		StringBuilder numeral = new StringBuilder();
 		int limit = limitType.getValue();
 		int difference = limit - reminder;
 		if (difference <= limitType.getAllowedCurrentDifference()) {
			numeral.append(limitType.getAllowedSubtractType().getSymbol());
			numeral.append(limitType.getSymbol());
			numeral.append(toNumeral(reminder - (limit - limitType.getAllowedCurrentDifference()),
					limitType.getPreviousValue()));
		} else if (reminder > 0) {
			numeral.append(toNumeral(reminder, limitType.getPreviousValue()));
 		}
 		return numeral.toString();
 	}
 
 }
