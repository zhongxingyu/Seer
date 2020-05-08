 import java.util.Arrays;
 import java.util.Collections;
 
 import javax.swing.JOptionPane;
 
 
 public class HC_Loopsassignment {
 
 	/**
 	 *@author HunterCaron
 	 * Mr.Marco
 	 * ICS 3U1
 	 * If StatmentsAssingment 5
 	 */
 	public static void main(String[] args) {
 		while(true)	
 		{
 			//input and conversion
 			int choice = Integer.parseInt(JOptionPane.showInputDialog(null, "Please enter the question number: (0 to quit)", "Loop Assigment", JOptionPane.QUESTION_MESSAGE));
 			//switch statement
 			switch (choice)	
 			{
 			case 0: 
 				System.exit(0);
 				break;
 			case 1:
 				for (int i = 1; i <= 16; i+=3)	
 					System.out.println(i);
 				break;
 			case 2: 
 				for (int i = 20; i >= 5; i-=5)
 					System.out.println(i);
 				break;
 			case 3:
 				for (int i = 1; i <= 500; i++){
 					System.out.print("*");}
 				System.out.println();
 				break;
 			case 4:
 				int astrix = Integer.parseInt(JOptionPane.showInputDialog("Enter the amount of astrixes you want to print"));
 				for (int i = 1; i <= astrix; i++)
 					System.out.print("*");
 				System.out.println();
 				break;
 			case 5: 
 				int odd = Integer.parseInt(JOptionPane.showInputDialog("Enter the max number"));
 				for (int i = 1; i <= odd; i+=2)
 					System.out.println(i);
 				break;
 			case 7:
 				int amount = 0;
 				float total = 0;
 				do {
 					float value = Float.parseFloat(JOptionPane.showInputDialog("Enter a number, -1 to find the average"));
 					total += value;
 					amount++;
 					System.out.println("The sum so far is: " + total + "\n#'s so far = " + amount);
 					if (value == -1) {
 						System.out.println("The average is: " + total/amount);
 						break;
 					}
 				}
 				while (true);
 				break;
 			case 8: for (char ch = 'Z' ;ch >= 'A' ; ch--)
 				System.out.println(ch);
 			for (char ch = 'z' ;ch >= 'a' ; ch--)
 				System.out.println(ch);
 			break;
 			case 9:
 				char letter = 'a';
 				String UorL = null, VCorD = null;
 				do	
 				{
 					String letterinput = JOptionPane.showInputDialog("Enter a letter: (* to exit)");
 					String lwcase = letterinput.toLowerCase();
 					char letterLW = lwcase.charAt(0);
 					letter = letterinput.charAt(0);
 					if (letter == letterLW)
 					{
 						UorL = "lowercase";
 						if (letter == '1' || letter == '2' || letter == '3' || letter == '4' || letter == '5' || letter == '6' || letter == '7' || letter == '8' || letter == '9' || letter == '0')
 							UorL = "Neither upper or lower case because:";
 					}
 					else UorL = "uppercase";
 					if (letterLW == 'a' || letterLW == 'e' || letterLW == 'i' || letterLW == 'o' || letterLW == 'u')
 						VCorD = "Vowel";
 					else if (letter == '1' || letter == '2' || letter == '3' || letter == '4' || letter == '5' || letterLW == '6' || letter == '7' || letter == '8' || letter == '9')
 						VCorD = "Digit";
 					else VCorD = "Consonant";
 					JOptionPane.showMessageDialog(null, "Your character is " + UorL + "\nYour character is a " + VCorD);
 				}
 				while (letter != '*');
 				break;
 			case 10:
 				double x = 0;
 				double denominator = 2;
 				for (int i = 2; i <= 20; i++)
 				{
 					x = 1/denominator;
 					System.out.println(x);
 					denominator++;
 				}
 				break;
 			case 11:
 				int int1 = 0;
 				do {
 					int1 = Integer.parseInt(JOptionPane.showInputDialog("Enter integer 1 (-1 to exit)"));
 					int int2 = Integer.parseInt(JOptionPane.showInputDialog("Enter integer 2"));
 					int int3 = Integer.parseInt(JOptionPane.showInputDialog("Enter integer 3"));
 					int maxVal = Collections.max(Arrays.asList(int1, int2, int3));
 					System.out.println("The max value is: " + maxVal);
 				}
 				while (int1 != 0);
 				break;
 			case 12:
 				int digit = 0;
 				int sum = 0;
 				int input = 0;
 				do {
 				input = JOptionPane.showInputDialog("Enter an integer (type exit to exit)");
 				do {
 					digit = input.charAt(0);
 					sum += digit;
 					i++;
 				}
				while (digit == '1' || digit == '2' || digit == '3' || digit == '4' || digit == '5' || digit == '6' || digit == '7' || digit == '8' || digit == '9' || digit == '0');					
				System.out.println("The sum of the digits is: " + sum);
 				}
 				while (input != "exit");
 
 			}
 
 		}
 
 	}
 
 }
