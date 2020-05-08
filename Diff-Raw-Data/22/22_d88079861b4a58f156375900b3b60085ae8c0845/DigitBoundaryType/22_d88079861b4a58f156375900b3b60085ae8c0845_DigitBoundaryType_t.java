 package org.codingdojo.roman.numeral.type;
 
 public enum DigitBoundaryType {
 	
 	NULL( -1, -1, "", null), ONE(1, 0, "I", NULL), FIVE(5, 1,"V", ONE),
 	TEN(10, 5, "X", ONE), FIFTY(50, 10, "L", TEN), HUNDRED(100, 50, "C", TEN),
 	FIVEHUNDRED(500, 100, "D", HUNDRED), THOUSAND(1000, 500, "M", HUNDRED),
 	INFINITY(Integer.MAX_VALUE, -1, "", THOUSAND);
 	
 	
 	
 	private final int value;
 	
 	public int getValue() {
 		return value;
 	}
 
 	
 	private final String symbol;
 	
 	private final DigitBoundaryType allowedSubtractType;
 	
 	private final int previousValue;
 
 	public String getSymbol() {
 		return symbol;
 	}
 	
 	public DigitBoundaryType getAllowedSubtractType(){
 		return allowedSubtractType;
 	}
 
 	private DigitBoundaryType(int value,int previousValue, String symbol, DigitBoundaryType allowedSubtractType) {
 		this.symbol = symbol;
 		this.previousValue = previousValue;
 		this.allowedSubtractType = allowedSubtractType;
 		this.value = value;
 	}
 	
 	public static DigitBoundaryType findType(int n){
 		for (DigitBoundaryType type : values()){
 			if (n ==type.value){
 				return type;
 			}
 		}
 		throw new IllegalArgumentException("The Romans weren't that clever to find: "+n);
 	}
 
 	public int getAllowedCurrentDifference() {
 		return getAllowedSubtractType().getValue();
 	}
 
 	public int getPreviousValue() {
 		return previousValue;
 	}
 	
	public String getPreviousAndThisSymbol(){
		return getAllowedSubtractType().getSymbol()+getSymbol();
	}
	
 
 }
