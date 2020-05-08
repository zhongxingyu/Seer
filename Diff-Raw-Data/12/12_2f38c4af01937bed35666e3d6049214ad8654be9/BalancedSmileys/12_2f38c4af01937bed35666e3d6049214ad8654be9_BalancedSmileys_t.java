 package se.wederbrand.facebook;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 public class BalancedSmileys {
 	public static void main(String... args) throws IOException {
 		String filename = "smiley_final.txt";
 		if (args.length > 0) {
 			filename = args[0];
 		}
 		String result = doAll(filename);
 		FileWriter fileWriter = new FileWriter("smiley_result.txt");
 		fileWriter.write(result);
 		fileWriter.close();
 		System.out.println(result);
 	}
 
 	public static String doAll(String arg) throws FileNotFoundException {
 		Scanner scanner = new Scanner(new FileReader(arg));
 		int count = scanner.nextInt();
 		int i = 1;
 
 		// go to next line
 		scanner.nextLine();
 		StringBuilder stringBuilder = new StringBuilder();
 		while (i <= count) {
 			boolean balanced = isBalanced(scanner.nextLine());
 			stringBuilder.append("Case #").append(i++).append(": ").append(balanced?"YES":"NO").append(System.lineSeparator());
 		}
 		return stringBuilder.toString();
 	}
 
 	public static boolean isBalanced(String str) {
 		// clean up the string, put [, ] and X instead of possible smileys and keep just [ and (
 		str = cleanUp(str);
 		char[] chars = str.toCharArray();
 
 		return isBalancedRecursive(chars, 0, 0);
 	}
 
 	private static boolean isBalancedRecursive(char[] chars, int index, int count) {
 		if (count < 0) {
 			return false;
 		}
 
 		if (chars.length == index) {
 			// if EOL return true if count is 0, else false
 			return count == 0;
 		}
 
 		char current = chars[index];
 
 		// GLOBAL: if anyone returns true just return true
 		switch (current) {
 			case '(':
 				// if ( increase count and check rest.
 				return isBalancedRecursive(chars, index+1, count+1);
 			case ')':
 				// if ) decrease count and check rest.
 				return isBalancedRecursive(chars, index+1, count-1);
 			case '[':
 				// if [ split and increase on one but not the other and check rest.
 				return isBalancedRecursive(chars, index+1, count)
 						|| isBalancedRecursive(chars, index+1, count+1);
 			case ']':
 				// if ] split and decrease on one but not the other and check rest.
 				return isBalancedRecursive(chars, index+1, count)
 						|| isBalancedRecursive(chars, index+1, count-1);
//			case 'X':
 				// if X increase with 1, decrease with 1 or leave as it is
	//			return isBalancedRecursive(chars, index+1, count)
		//				|| isBalancedRecursive(chars, index+1, count+1)
			//			|| isBalancedRecursive(chars, index+1, count-1);
 			default:
 				throw new RuntimeException("unheard of character: " + current);
 		}
 	}
 
 	/**
 	 * remove all garbage and keeps only what could be a smiley (represented as [, ]and X) and normal ( and )
 	 *
 	 */
 	public static String cleanUp(String str) {
 		String orig = str;
		// str = str.replaceAll("\\(:\\)", "X");
 		str = str.replaceAll(":\\)", "]");
 		str = str.replaceAll(":\\(", "[");
 		str = str.replaceAll("[^\\(\\)\\]\\[X]", "");
 		return str;
 	}
 }
