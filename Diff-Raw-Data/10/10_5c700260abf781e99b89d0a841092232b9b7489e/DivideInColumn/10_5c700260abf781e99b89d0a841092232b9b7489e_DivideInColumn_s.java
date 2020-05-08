 package module1;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 public class DivideInColumn {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
 		int devident = input();
 		int divisor = input();
 		output(division(devident, divisor));
		
		
 	}
 	
 	public static int input() throws NumberFormatException, IOException
 	{
 		BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
 		int a = Integer.parseInt(bf.readLine());
 		return a;
 	}
 	
 	public static ArrayList<String> division(int devident, int divisor) throws NumberFormatException, IOException
 	{
 		ArrayList<String> pseudographics = new ArrayList<String>();
 		try{
 			
 		ArrayList<Integer> digitsInDevident = new ArrayList<Integer>();
 		ArrayList<Integer> resultByDigits = new ArrayList<Integer>();
 		boolean negativeSign = isNegativeSign(devident, divisor);
 		devident = Math.abs(devident);
 		divisor = Math.abs(divisor);
 		String indent = "  ";
 		String minus = " ¯";
 		
 		if (negativeSign == false){
 			pseudographics.add("  " + devident + "|" + divisor);
 		} else {
 			pseudographics.add(" -" + devident + "|" + divisor);
 		}
 		
 		pseudographics.add(minus + signPerDigits(howMuchDigits(devident), " "));
 		
 		digitsInDevident = splitIntoDigits(devident);
 		
 		
 		isDivisorBiggerThanDevident(devident, divisor);
 
 		
 		int firstDevident = digitsInDevident.get(0);
 		int i = 1;
 		int integerDivision;
 		int reminder;
 		int subtrahend;
 		while (firstDevident < divisor){
 			firstDevident = firstDevident * 10 + digitsInDevident.get(i);
 			i++;
 		}
 		integerDivision = firstDevident / divisor;
 		resultByDigits.add(integerDivision);
 		subtrahend =  integerDivision * divisor;
 		int subtrahendHasdigit = howMuchDigits(subtrahend);
 		pseudographics.add(indent + subtrahend);
 		reminder = firstDevident - subtrahend;
 		pseudographics.add(indent + signPerDigits(howMuchDigits(subtrahend), "¯"));
 		while(i < digitsInDevident.size()){
 			int anotherDevident = digitsInDevident.get(i);
 			if (reminder == 0){
 				pseudographics.add(indent + anotherDevident);
 				integerDivision = anotherDevident / divisor;
 				resultByDigits.add(integerDivision);
 				subtrahend =  integerDivision * divisor;
 				pseudographics.add(minus + subtrahend);
 				reminder = anotherDevident - subtrahend;
 				pseudographics.add(indent + signPerDigits(howMuchDigits(subtrahend), "¯"));
 				i++;
 			} else {
 				anotherDevident = reminder * 10 + digitsInDevident.get(i);
 				pseudographics.add(indent + anotherDevident);
 				integerDivision = anotherDevident / divisor;
 				resultByDigits.add(integerDivision);
 				subtrahend =  integerDivision * divisor;
 				pseudographics.add(minus + subtrahend);
 				reminder = anotherDevident - subtrahend;
 				pseudographics.add(indent + signPerDigits(howMuchDigits(subtrahend), "¯"));
 				i++;
 			}
 		}
 		pseudographics.add(indent + reminder);
 		
 		pseudographics = printresult( pseudographics,  resultByDigits, negativeSign, subtrahendHasdigit, howMuchDigits(divisor));
 		
 		} catch (ArithmeticException ie) {
 				System.out.println("You mustn't diveded by zero");
 				System.out.println("Please, re-enter devident and divisor");
 				devident = input();
 				divisor = input();
 				division(devident,divisor);
 			}
 		return pseudographics;
 	}
 	
 	private static void isDivisorBiggerThanDevident(int devident, int divisor) {
 		if( ((double)devident / divisor >= 0) && ((double) devident / divisor < 1) )
 		{
 			try {
 				devident = input();
 				divisor = input();
 				division(devident, divisor);
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static boolean isNegativeSign(int divident, int divisor)
 	{
		return ((divident < 0 && divisor > 0) || (divident < 0 && divisor > 0));
 	}
 	
 	private static int howMuchDigits(int number){
 		int count = 1;
 		while (number / 10 != 0)
 		{
 			count++;
 			number /= 10;
 		}
 		return count;
 	}
 	
 	private static ArrayList<Integer> splitIntoDigits(int number){
 		ArrayList <Integer> digitsInNumber = new ArrayList <Integer>();
 		do 
 		{
 			digitsInNumber.add(0, number%10);
 			number /= 10;
 			if (number / 10 == 0 & number % 10 != 0)
 				digitsInNumber.add(0, number%10);
 		} while (number / 10 != 0);
 		
 		return digitsInNumber;
 	}
 	
 	private static String signPerDigits(int digits, String sign){
 		StringBuilder sb= new StringBuilder ("");
 		int count = 0;
 		while(count < digits){
 			sb.append(sign);
 			count++;
 		}
 		return sb.toString();
 	}
 	
 	private static ArrayList<String> printresult
 		(ArrayList<String> pseudographics, ArrayList<Integer> resultByDigits, boolean negativeSign, int subtrahendHasdigit, int divisorHasDigits)
 	{
 		int res = resultByDigits.get(0);
 		
 		for(int i = 1; i < resultByDigits.size(); i++)
 		{
 			res = res * 10 + resultByDigits.get(i);
 		}
 		if (negativeSign){
 			res *= -1;
 		}
 		int count = 0;
 		StringBuilder s = new StringBuilder("¯");
 		if (divisorHasDigits > resultByDigits.size()){
 			while(count < divisorHasDigits){
 				count++;
 				s.append("¯");
 			}
 		} else {
 			while(count < resultByDigits.size()){
 				count++;
 				s.append("¯");
 			}
 		}
 		pseudographics.add(1, (pseudographics.get(1)) + s.toString());
 		pseudographics.remove(2);
 		pseudographics.add(2, (pseudographics.get(2)  + signPerDigits(subtrahendHasdigit - divisorHasDigits, " ") + "|" + res));
 		pseudographics.remove(3);
 		return pseudographics;
 	}
 	
 	private static void output (ArrayList <String> pseudographics){
 		for (String e : pseudographics){
 			System.out.println(e);
 		}
 	}
 }
 
