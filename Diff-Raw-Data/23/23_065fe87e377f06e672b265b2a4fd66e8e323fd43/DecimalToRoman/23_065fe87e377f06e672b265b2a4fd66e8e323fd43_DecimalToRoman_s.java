 package decimalToRoman;
 
 public class DecimalToRoman {
 	
 	public static String decimalToRoman(int decimal){
 		//First, the basic values.
 		//M = 1000
 		//D = 500
 		//C = 100
 		//L = 50
 		//X = 10
 		//V = 5
 		//I = 1
 		
		//String buffer are great for building words and phrases.
		//This string buffer we will name "roman", and we will use it to keep track of the letters in our roman numeral number.
 		StringBuffer roman = new StringBuffer();
 		//We start out with the thousands.
 		if (decimal >= 1000){
			//If there is at least one 1000 in the number...
 			for(int mNumber=(int)(decimal/1000); mNumber>=1; mNumber --){
 				//...put an "M" on the beginning of the string buffer roman....
 				roman.append("M");
 				//Then subtract 1000 from the original amount for each time we go through the loop.
 				decimal -= 1000;
 			}
 		}
 		//Now that we have gotten rid of the thousands, we will check if there is one 900 left. If there is...
 		if (decimal >= 900){
 			//...append a "CM" on the end of the string roman.
 			//This is because 900 is 1000 - 100. 
 			//In roman numerals, if a numeral of a lesser value is placed before a numeral of greater value, the lesser value is subtracted from the greater value.
 			//For example, IX = 10-1 = 9. This is opposed to XI = 10+1 = 11. Aren't roman numerals fun? 
 			roman.append("CM");
 			decimal -= 900;
 		}
 		//Ditto for 500. If there is one...
 		if (decimal >= 500) {
 			//append a "D".
 			roman.append("D");
 			decimal -= 500;
 		}
 		//Same thing for 400 
 		if (decimal >= 400){
 			//By now, you know the drill.
 			roman.append("CD");
 			decimal -= 400;
 		}
		//The reason that I used a for loop here, is that there could be any number of thousands, up to 3 hundreds, up to 3 tens, and up to 3 ones.
 		if (decimal >= 100){
 			for(int cNumber= (int) (decimal/100); cNumber>0; cNumber --){
 				roman.append("C");
 				decimal -= 100;
 			}
 			
 		}
		//However, if we are talking about 900, 500, 400, 90, 50, 40, 9, 5, or 4; there can only be one of each level. if there is a 900, there won't be a 500. So with all of the other numbers. 
 		if(decimal >= 90){
 			roman.append("XC");
 			decimal -= 90;
 		}
 		
 		if (decimal >= 50) {
 			roman.append("L");
 			decimal -= 50;
 		}
 		
 		if(decimal >= 40){
 			roman.append("XL");
 			decimal -= 40;
 		}
 		
 		if(decimal >= 10){
 			for(int xNumber= (int) (decimal/10); xNumber>0; xNumber --){
 				roman.append("X");
 				decimal -= 10;
 			}
 		}
 		
 		if(decimal == 9){
 			roman.append("IX");
 			decimal -= 9;
 		}
 		
 		if(decimal >= 5){
 			roman.append("V");
 			decimal -= 5;
 				
 		}
 		
 		if(decimal == 4){
 			roman.append("IV");
 			decimal -= 4;
 		};
 			
 		if (decimal >= 1){
 			for(int iNumber = decimal; iNumber>0; iNumber --){
 				roman.append("I");
 				decimal -= 1;
 			}
 		}
		//Now that we have sifted to the bottom of the barrel, we can output the roman numeral string that is left.
 		return roman.toString();
 		
 	};
 }
