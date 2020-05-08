 import java.util.*;
 /**
 * Given any integer, print an English phrase that describes the integer (e.g., "One Thousand, Two Hundred Thirty Four").
 */
 public class HumanizeNumber {
 	
 	public static String humanize(int number) {
 		List<Integer> composites = splitIntoDecComposites(number);
 		StringBuilder sb = new StringBuilder();
 		for (Integer composite : composites) {
 			if (composite == 0 ) continue;
 			sb.append(wordFor(composite)).append(" ");
 		}
 		return sb.toString();
 	}
 
 	private static String wordFor(int i) {
 		String s = Integer.toString(i);
 		int leading = leadingDigit(i);
 		switch(s.length()) {
 			case 1:
 				return wordForSmall(leading);
 			case 2:
 				return wordForTens(i);
 			case 3:
 				return wordForSmall(leading) + " Hundred";
 			case 4:
 				return wordForSmall(leading) + " Thousand";
 			case 5:
 				return wordForTens(leading * 10) + " Thousand";
 			case 6:
 				return wordForSmall(leading) + " Hundred Thousand";
 			case 7:
 				return wordForSmall(leading) + " Million";
 			case 8:
 				return wordForTens(leading * 10) + " Million";
 			case 9:
 				return wordForSmall(leading) + " Hundred Million";
 			case 10:
 				return wordForSmall(leading) + " Billion";
 			default:
 				throw new IllegalArgumentException("Unable to understand " + i);
 		}
 	}
 
 	private static String wordForSmall(int i) {
 		switch (i) {
 			case 1: return "One";
 			case 2: return "Two";
 			case 3: return "Three";
 			case 4: return "Four";
 			case 5: return "Five";
 			case 6: return "Six";
 			case 7: return "Seven";
 			case 8: return "Eight";
 			case 9: return "Nine";
 			default: throw new IllegalArgumentException(i + " exceeds range of small");
 		}
 	}
 	private static String wordForTeen(int i) {
 		switch (i) {
 			case 10: return "Ten";
 			case 11: return "Eleven";
 			case 12: return "Twelve";
 			case 13: return "Thirteen";
 			case 14: return "Fourten";
 			case 15: return "Fifteen";
 			case 16: return "Sixteen";
 			case 17: return "Seventeen";
 			case 18: return "Eighteen";
 			case 19: return "Nineteen";
 			default: throw new IllegalArgumentException(i + " exceeds range of small");
 		}
 	}
 	private static String wordForTens(int i) {
 		switch (i) {
 			case 10: return "Ten";
 			case 20: return "Twenty";
 			case 30: return "Thirty";
			case 40: return "Fourty";
 			case 50: return "Fifty";
 			case 60: return "Sixty";
 			case 70: return "Seventy";
 			case 80: return "Eighty";
 			case 90: return "Ninety";
 			default: throw new IllegalArgumentException(i + " exceeds range of small");
 		}
 	}
 
 	private static List<Integer> splitIntoDecComposites(int number) {
 		List<Integer> composites = new ArrayList<Integer>();
 		String text = Integer.toString(number);
 		for (int i =0; i < text.length(); i++) {
 			Integer sigDigit = Integer.parseInt(Character.toString(text.charAt(i)));
 			int decScale = text.length() - i - 1;
 			composites.add(sigDigit * (int) Math.pow(10, decScale)); 
 		}
 		return composites;
 	}
 
 	private static int leadingDigit(int i) {
 		String text = Integer.toString(i);
 		return Integer.parseInt(Character.toString(text.charAt(0)));
 	}
 
 	public static void main(String... args) {
 		for (int i = 1; i < 40; i++) {
 			int number = (int)Math.pow(2,i);
 			System.out.println(number + " " + humanize(number));
 		}
 	}
 
 }
