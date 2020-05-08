 /* 
  * Copyright 2011-2013 Antidot opensource@antidot.net
  * https://github.com/antidot/db2triples
  * 
  * This file is part of DB2Triples
  *
  * DB2Triples is free software; you can redistribute it and/or 
  * modify it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation; either version 2 of 
  * the License, or (at your option) any later version.
  * 
  * DB2Triples is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /***************************************************************************
  *
  * XMLS XSD datatype : Lexical form
  *
  * All literals have a lexical form being a Unicode [UNICODE] string, which SHOULD be in Normal Form C [NFC].
  * The canonical lexical form is th lexical form with the additional constraint that the
  * canonical lexical representation.
  *
  ****************************************************************************/
 package net.antidot.semantic.xmls.xsd;
 
 public class XSDLexicalForm {
 	
 	public static String getCanonicalLexicalForm(
 			String nonCanonicalLexicalForm, XSDType type) {
 		String result = nonCanonicalLexicalForm;
 		switch (type) {
 		case DECIMAL:
 			result = getDecimalCanonicalLexicalForm(nonCanonicalLexicalForm);
 			break;
 
 		case INTEGER:
 			result = getIntegerCanonicalLexicalForm(nonCanonicalLexicalForm);
 			break;
 
 		case DOUBLE:
 			result = getDoubleCanonicalLexicalForm(nonCanonicalLexicalForm);
 			break;
 			
 		case BOOLEAN:
 			result = getBooleanCanonicalLexicalForm(nonCanonicalLexicalForm);
 			break;
 			
 		case DATETIME:
 			result = getDateTimeCanonicalLexicalForm(nonCanonicalLexicalForm);
 			break;
 			
 		default:
 			break;
 		}
 
 		return result;
 	}
 
 	private static String getDateTimeCanonicalLexicalForm(
 			String nonCanonicalLexicalForm) {
 		if (nonCanonicalLexicalForm.endsWith(".0")){
 			return nonCanonicalLexicalForm.substring(0, nonCanonicalLexicalForm.length() - 2);
 		} else if (nonCanonicalLexicalForm.endsWith(".00")){
 			return nonCanonicalLexicalForm.substring(0, nonCanonicalLexicalForm.length() - 3);
 		} else if (nonCanonicalLexicalForm.endsWith(".000")){
 			return nonCanonicalLexicalForm.substring(0, nonCanonicalLexicalForm.length() - 4);
 		} else if (nonCanonicalLexicalForm.endsWith(".000+00:00")){
 			String result = nonCanonicalLexicalForm.substring(0, nonCanonicalLexicalForm.length() - 10);
 			return result + "Z";
 		}
 		return nonCanonicalLexicalForm;
 	}
 
 	private static String getBooleanCanonicalLexicalForm(
 			String nonCanonicalLexicalForm) {
 		if (nonCanonicalLexicalForm.equals("1")) nonCanonicalLexicalForm = "true";
 		else if (nonCanonicalLexicalForm.equals("0")) nonCanonicalLexicalForm = "false";
 		else if (nonCanonicalLexicalForm.equals("t")) nonCanonicalLexicalForm = "true";
 		else if (nonCanonicalLexicalForm.equals("f")) nonCanonicalLexicalForm = "false";
 		else if (nonCanonicalLexicalForm.equals("T")) nonCanonicalLexicalForm = "true";
 		else if (nonCanonicalLexicalForm.equals("F")) nonCanonicalLexicalForm = "false";
 		Boolean result = Boolean.valueOf(nonCanonicalLexicalForm);
 		return result.toString();
 	}
 
 	private static String getDoubleCanonicalLexicalForm(
 			String nonCanonicalLexicalForm) {
 		Double value = Double.valueOf(nonCanonicalLexicalForm);
 		String minus = "";
 		if (value < 0)
 			minus = "-";
 		Double absValue = Math.abs(value);
 		if (absValue.toString().contains("E")) return absValue.toString();
 		// Exponent
 		int exponent = 0;
 		if (absValue < 1){
 			while (absValue < 0){
 				exponent++;
 				absValue *= 10;
 			}
 		} else {
 			while (absValue >= 10){
 				exponent++;
 				absValue /= 10;
 			}
 		}
		// Keep 3 decimals
		Double roundValue =  myRound(absValue,3);
		String result = minus + roundValue + "E" + exponent;
 		return result;
 	}
 
 	private static String getIntegerCanonicalLexicalForm(
 			String nonCanonicalLexicalForm) {
 		// Remove '+'
 		if (nonCanonicalLexicalForm.startsWith("+"))
 			nonCanonicalLexicalForm = nonCanonicalLexicalForm.substring(1);
 		Integer result = Integer.valueOf(nonCanonicalLexicalForm);
 		return result.toString();
 	}
 	
    private static double myRound(double value, int decimalPlaces)
    {
        if(decimalPlaces < 0) { return value; }
        double augmentation = Math.pow(10, decimalPlaces);
        return Math.round(value * augmentation) / augmentation;
    }

 	private static String getDecimalCanonicalLexicalForm(
 			String nonCanonicalLexicalForm) {
 		Float result = Float.valueOf(nonCanonicalLexicalForm);
 		if (Math.round(result) == result)
 			return Integer.toString(result.intValue());
 		else
 			return result.toString();
 	}
 
 }
