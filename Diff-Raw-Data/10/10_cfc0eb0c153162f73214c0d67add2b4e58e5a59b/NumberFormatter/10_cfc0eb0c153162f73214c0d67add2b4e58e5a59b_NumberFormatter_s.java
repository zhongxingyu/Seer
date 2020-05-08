 /*
  * Copyright 2011-2013 Jeroen Meetsma - IJsberg
  *
  * This file is part of Iglu.
  *
  * Iglu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Iglu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ijsberg.iglu.util.formatting;
 
import java.math.BigDecimal;

 import org.ijsberg.iglu.util.misc.StringSupport;
 
 
 /**
  * Formats any float-like value to a displayable string.
  * Performs rounding if necessary.
  * Trailing zeroes are, if necessary, returned to fill up the desired number of decimals.
  */
 public class NumberFormatter {
 	private char decimalSymbol = '.';
 	private char digitGroupingSymbol = ',';
 
 	public NumberFormatter() {
 	}
 	
 	/**
 	 * @param decimalSymbol	   character that separates decimals
 	 * @param digitGroupingSymbol symbol that groups 3 digits
 	 */
 	public NumberFormatter(char decimalSymbol, char digitGroupingSymbol) {
 		this.decimalSymbol = decimalSymbol;
 		this.digitGroupingSymbol = digitGroupingSymbol;
 	}
 
 
 	/**
 	 * @param number	   number to format
 	 * @param nrofDecimals desired number of decimals
 	 * @return the formatted number
 	 */
 	public String format(float number, int nrofDecimals) {
 		return format(new BigDecimal(number), nrofDecimals);
 	}
 
 	/**
 	 * @param number	   number to format
 	 * @return the formatted number
 	 */
 	public String format(int number) {
 		return format(new BigDecimal(number), 0);
 	}
 
 	/**
 	 * @param number	   number to format
 	 * @param nrofDecimals desired number of decimals
 	 * @return the formatted number
 	 */
 	public String format(double number, int nrofDecimals) {
 		return format(new BigDecimal(number), nrofDecimals);
 	}
 
 	/**
 	 * @param part
 	 * @param part
 	 * @param nrofDecimals desired number of decimals
 	 * @return the formatted percentage
 	 */
 	public String formatPercentage(int part, int total, int nrofDecimals) {
 		return format(new BigDecimal(((1.0 * part) / total) * 100), nrofDecimals);
 	}
 
 	/**
 	 * @param number	   number to format
 	 * @param nrofDecimals desired number of decimals
 	 * @return the formatted number
 	 */
 	public String format(BigDecimal number, int nrofDecimals) {
 		BigDecimal pow = new BigDecimal(Math.pow(10, (double) nrofDecimals));
 
 		String numStr = number.multiply(pow).divide(pow, nrofDecimals, BigDecimal.ROUND_HALF_UP).toString();
 
 		int decimalSymbolPos = numStr.indexOf('.');
 		if (decimalSymbolPos > -1) {
 			numStr = numStr.substring(0, decimalSymbolPos + nrofDecimals + 1);
 			numStr = StringSupport.replaceFirst(numStr, ".", String.valueOf(decimalSymbol));
 		}
 		else {
 			decimalSymbolPos = numStr.length();
 			if(nrofDecimals > 0) {
 				numStr += decimalSymbol;
 			}
 		}
 		if(nrofDecimals > 0) {
 			int nrofMissingZeros = decimalSymbolPos + nrofDecimals - numStr.length();
 			for (int i = 0; i <= nrofMissingZeros; i++) {
 				numStr += '0';
 			}
 			numStr = StringSupport.replaceFirst(numStr, ".", String.valueOf(decimalSymbol));
 		}
 		if (decimalSymbolPos > 3) {
 			StringBuffer numStrBuf = new StringBuffer(numStr);
 			int trailingDigits = decimalSymbolPos - (numStr.startsWith("-") ? 1 : 0);
 			for (int i = 3; i < trailingDigits; i += 3) {
 				numStrBuf.insert(decimalSymbolPos - i, digitGroupingSymbol);
 			}
 			numStr = numStrBuf.toString();
 		}
 		return numStr;
 	}
 }
