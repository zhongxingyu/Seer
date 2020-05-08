 package com.ACM.Conversions;
 
 public class StringCheck {
 	private String correctedExpression;
 
 	public StringCheck(String string) {
 		this.correctedExpression = "";
 		int i = 0;
 
 		while (i < string.length()) {
 			if (string.charAt(i) == 'x')
 				this.correctedExpression += "*";
 			else
 				this.correctedExpression += string.charAt(i);
 			if (string.charAt(i) == ')')
 				if (string.charAt(i + 1) != '+' || string.charAt(i + 1) != '-'
 						|| string.charAt(i + 1) != 'x'
 						|| string.charAt(i + 1) != '/'
 						|| string.charAt(i + 1) != ')')
 					this.correctedExpression += " x ";
			i++;
 		}
 	}
 	public String getCorrectedExpression (){
 		return this.correctedExpression;
 	}
 }
