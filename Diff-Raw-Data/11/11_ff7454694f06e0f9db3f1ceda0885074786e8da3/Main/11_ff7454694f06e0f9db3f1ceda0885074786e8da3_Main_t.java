 import java.util.Scanner;
 
 public class Main {
 
 	public static void main(String[] args) {
 		Scanner console = new Scanner(System.in);
 		int num;
 		System.out.print("Enter a number to convert, 0 to quit: ");
 		num = console.nextInt();
 		while(num > 0) {
 			System.out.println(parse(num));
 			System.out.print("Enter a number to convert, 0 to quit: ");
 			num = console.nextInt();
 		}
 		console.close();
 		System.out.println("Goodbye!");
 	}
 
 	public static String parse(int n) {
 		String output = reduceAndParse(n);
 		String capOut = output.substring(0, 1).toUpperCase() + output.substring(1) + ".";
 		return capOut;
 	}
 
 	public static String reduceAndParse(int n) {
 		return reduceAndParse(n, 0).trim();
 	}
 
 	public static String reduceAndParse(int n, int power) {
 		if (n < 1000) {
 			return parseNum(n, power);
 		} else {
 			return reduceAndParse(n / 1000, power + 1) + parseNum(n % 1000, power);
 		}
 	}
 
 	public static String parseNum(int n, int power) {
 		String output = "";
 		if(n <= 0) {
 			return output;
 		} else if (n % 100 >= 10 && n % 100 < 20) {
 			output += parseTeens(n);
 			n /= 100;
 		} else {
 			output = parseOnes(n % 10);
 			n /= 10;
 			output = parseTens(n % 10) + output;
 			n /= 10;
 		}
 		output = parseHundreds(n % 10) + output;
 		output += powersOfThousand(power);
 		return output;
 	}
 
 	public static String powersOfThousand(int power) {
 		switch (power) {
 		case 0:
 			return "";
 		case 1:
 			return " thousand";
 		case 2:
 			return " million";
 		case 3:
 			return " billion";
 		default:
			return "===There was an error in powersOfThousand()! power = " + power + "=== ";
 		}
 	}
 
 	public static String parseOnes(int n) {
 		switch (n) {
 		case 9:
 			return " nine";
 		case 8:
 			return " eight";
 		case 7:
 			return " seven";
 		case 6:
 			return " six";
 		case 5:
 			return " five";
 		case 4:
 			return " four";
 		case 3:
 			return " three";
 		case 2:
 			return " two";
 		case 1:
 			return " one";
 		case 0:
 			return "";
 		default:
			return " ===There was an error in parseOnes()! n = " + n + "=== ";
 		}
 	}
 
 	public static String parseTens(int n) {
 		switch (n) {
 		case 9:
 			return " ninety";
 		case 8:
 			return " eighty";
 		case 7:
 			return " seventy";
 		case 6:
 			return " sixty";
 		case 5:
 			return " fifty";
 		case 4:
 			return " fourty";
 		case 3:
 			return " thirty";
 		case 2:
 			return " twenty";
 		case 1:
 			return " ten";
 		case 0:
 			return "";
 		default:
			return " ===There was an error in parseTens()! n = " + n + "=== ";
 		}
 	}
 
 	public static String parseHundreds(int n) {
 		if (n == 0)
 			return "";
 		return parseOnes(n) + " hundred";
 	}
 
 	public static String parseTeens(int n) {
 		switch (n % 100) {
 		case 19:
 			return " nineteen";
 		case 18:
 			return " eighteen";
 		case 17:
 			return " seventeen";
 		case 16:
 			return " sixteen";
 		case 15:
 			return " fifteen";
 		case 14:
 			return " fourteen";
 		case 13:
 			return " thirteen";
 		case 12:
 			return " twelve";
 		case 11:
 			return " eleven";
 		case 10:
 			return " ten";
 		default:
			return " ===There was an error in parseTeens()! n = " + n + "=== ";
 		}
 	}
 }
