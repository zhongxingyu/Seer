 package org.eclipse.imp.prefspecs.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.eclipse.imp.language.ILanguageSyntaxProperties;
 
 public class PrefspecsSyntaxProperties implements ILanguageSyntaxProperties {
 
     public String getBlockCommentEnd() {
         return null;
     }
 
     public String getBlockCommentStart() {
         return null;
     }
 
     public String getSingleLineCommentPrefix() {
         return "//";
     }
 
     public String getBlockCommentContinuation() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public String[][] getFences() {
         return new String[][] { { "{", "}" } };
     }
 
     
     /**
      * This implementation returns a string that contains each of the
      * characters that may appear in a Prefspecs identifier, without
      * regard to restrictions on the kinds of characters that may appear
      * in certain positions (i.e., no digits in the first position).
      * Each character is represented once in the string.
      */
     public String getIdentifierConstituentChars() {
         return "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
     }
     
     
     /**
      * Returns an array of integers that represents a "best guess" at the
      * starting positions of independently meaningful components within a
      * given identifier (i.e., of the separate terms making up a compound
      * identifier).
      * 
      * The rules are based on changes in the case of consecutive letters
      * and on the occurrence of digits in relation to letters.  PrefSpecs
      * identifiers can contain only letters and digits, so other characters
      * do not need to be considered.
      * 
      * The first int in the returned array will always be 0, representing the
      * start of the first component in the given identifier; this will be the
      * only element in the returned array if no separable terms are found within
      * the identifier.
      * 
      * @param	ident	A string, representing an identifier, for which the
      * 					the starting positions of constituent terms are to be
      * 					returned
      * @return			An array of ints representing the starting position
      * 					of constitutent terms in the given identifier:
      * 					an array of length 0 if the given identifier is null;
      * 					an array of length > 0 in all other cases, in which the
      * 					first element will always be 0.
      */
     public int[] getIdentifierComponents(String ident)
     {
     	if (ident == null)
     		return new int[0];
     	if (ident.length() == 1)
     		return new int[] { 0 };
     	
     	int[] breaks = new int[ident.length()];
     	for (int i = 0; i < ident.length()-1; i++) {
     		
     		// Useful strings
     		String remainder = ident.substring(i);
     		String first = remainder.substring(0, 1);
     		String firstLC = first.substring(0, 1).toLowerCase();
     		String second = remainder.substring(1, 2);
     		String secondLC = second.substring(0, 1).toLowerCase();
     		
     		if (isLower(first, firstLC) && isUpper(second, secondLC)) {
     			breaks = addIfAbsent(breaks, i+1);
     			continue;		
     		}
     		if (isUpper(first, firstLC) && isLower(second, secondLC)) {
     			breaks = addIfAbsent(breaks, i);
     			continue;
     		}
     		
     		if (isDigit(ident, i) && !isDigit(ident, i+1) /* && !isSpecial(ident, i+1) */ ) {
     			breaks = addIfAbsent(breaks, i+1);
     			continue;
     		}
     		if (!isDigit(ident, i) && isDigit(ident, i+1)) {
     			breaks = addIfAbsent(breaks, i+1);
     			continue;
     		}
     		// Not used in PrefSpecs identifiers at this time
 //    		if (isSpecial(ident, i) && !isSpecial(ident, i+1)) {
 //    			breaks = addIfAbsent(breaks, i+1);
 //    		}
     	}
     	
     	int numBreakPositions = 1;
     	for (int last = breaks.length-1; last > 0; last--) {
     		if (breaks[last] > 0) {
     			numBreakPositions = last+1;
     			break;
     		}
     	}
     	
     	int[] result = new int[numBreakPositions];
     	System.arraycopy(breaks, 0, result, 0, numBreakPositions);
     	return result;
     }
     
     
     public int[] addIfAbsent(int[] a, int i) {
     	int j = 0;
     	for (; j < a.length; j++) {
     		if (a[j] == i)
     			return a;
     		if (j > 0 && a[j] == 0)
     			break;
     	}
     	a[j] = i;
     	return a;
     }
     
     
     public boolean isLower(String s, String l) {
     	boolean result = s.compareTo(l) == 0;
     	return result;
     }
     
     
     public boolean isUpper(String s, String l) {
     	boolean result = s.compareTo(l) < 0;
     	return result;
     }
     
     
     public boolean isDigit(String s, int pos) {
     	char c = s.charAt(pos);
     	boolean result = c >= '0' && c <= '9';
     	return result;
     }
     
     
     public boolean isSpecial(String s, int pos) {
     	char c = s.charAt(pos);
     	switch (c) {
 	    	case '_':
 	    	case '-':
 	    	case '.':	return true;
 	    	default:	return false;
     	}
     }
     
     
     
 
 
 }
