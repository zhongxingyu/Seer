 import java.io.InputStreamReader;
 
 /**
  * Parsimonious - a mathematical parser.
  * Known bugs: Will only process one line with each run. Does not do any maths. Does not validate grammar. No tree traversal.
  * Should use Exceptions rather than exiting.
  * @author Wilfred Hughes
  */
 
 // invalid syntax suggestions: "1.0.1" "sin" "css" "coos" "3**3" "2 co"
 
 public class Parsimonious
 {	public static void main(String[] args)
 	{	//intro
 		System.out.printf("The following operators are accepted in descending order of priority:%n");
 		System.out.printf("cos ! * + - (cos in radians, ! only on integers)%n");
 		System.out.printf("Signed floating point numbers are accepted in the forms +/- 0, 0.0 or .0 (implicit: 0. and .) %n");
 		System.out.printf("********************************************%n");
 		System.out.printf("Type a mathematical expression and hit enter. All whitespace will be ignored.%n");
 
 		InputStreamReader input = new InputStreamReader(System.in);
 
 		String inputString = ""; //need to instantiate this outside the try block to keep java happy
 
 		try
 		{	int a = input.read();
 			//put input in string
 			while (a != -1 && a != 10) //-1 is end of stream, 10 is character return
 			{	inputString = inputString + (char)a;
 				a = input.read();
 			}
 			System.out.printf("Original input is: %s%n",inputString);
 		}
 		catch (java.io.IOException e) 
 		{	System.out.println("IOException! Exiting.");
 			System.exit(1);
 		}
 
 		String strippedInput = Lexer.removeWhitespace(inputString);
 		System.out.printf("Stripping whitespace: %s%n",strippedInput);
 
 		System.out.printf("Separating String into tokens...");
 		String[] tokenArray = Lexer.separateTokens(strippedInput);
 		System.out.printf("OK%n");
 		System.out.printf("Current String array: "); printArray(tokenArray);
 
 		System.out.printf("Tokenising...");
 		Token[] mathsArray = Lexer.tokenise(tokenArray);
 		System.out.printf("OK%n");
 		System.out.printf("Current Token array: "); printArray(mathsArray);
 		
 		//validate grammar
 		//parse
 		//Parser.parse(mathsArray);
 		//System.out.printf("Parsed result: "); printArray(mathsArray);
 	}
 
 	private static void printArray(Object[] input) //we want to be able to print strings or tokens
 	{	for (int i=0; i<input.length; i++)
 		{	System.out.printf("%s ",input[i]);
 		}
 		System.out.printf("%n");
 	}
 }
 
 class Token
 {	private String operatorName;
 	private boolean isOperator;
 	private float number;
 
 	public boolean isOperator()
 	{	return isOperator;
 	}
 
 	public Token(String value)
 	{	if (value.equals("+") || value.equals("-") || value.equals("*") || value.equals("!") || value.equals("cos"))
 		{	operatorName = value;
 			isOperator = true;
 		}
 		else
		{	System.out.printf("'%s' is not a valid operator.",value);
 			System.exit(1);
 		}
 	}
 
 	public Token(float value)
 	{	number = value;
 		isOperator = false;
 	}
 
 	public String getValue()
 	{	if (isOperator)
 		{	return operatorName;
 		}
 		else
 		{	return "" + number;
 		}
 	}
 
 	public String toString()
 	{	return this.getValue();
 	}
 }
 
 class Lexer
 {	public static String removeWhitespace(String input)
 	{	String returnme = "";
 		for (int i=0; i<input.length(); i++)
 		{	if((int)input.charAt(i) != 9 && (int)input.charAt(i) != 32) //not tab or space
 			{	returnme = returnme + input.charAt(i);
 			}
 		}
 		return returnme;
 	}
 
 	public static String[] separateTokens(String input)
 	{	String[] returnme = new String[0];
 		for (int i=0; i<input.length(); i++)
 		{	if (isShortOperator(input.charAt(i)))
 			{	returnme = extendArray(returnme,input.charAt(i)+""); //single character operator token
 			}
 			else if (isNumeric(input.charAt(i)))
 			{	if (i == 0) //expression starts with a number
 				{	returnme = extendArray(returnme,input.charAt(i)+"");
 				}
 				else
 				{	if (isNumeric(returnme[returnme.length-1].charAt(0))) //first character of last token is numeric
 					{	//last token is number so far, add this digit or d.p. to it
 						returnme[returnme.length-1] = returnme[returnme.length-1] + input.charAt(i);
 					}
 					else //last token was operator, start new token
 					{	returnme = extendArray(returnme,input.charAt(i)+"");
 					}
 				}
 			}
 			else //is hopefully a valid cos token, but we haven't checked yet, so we just take the next 3 characters
 			{	//risk of IndexException here, so catch it (occurs if expression ends 'c' or 'co' etc)
 				try
 				{	String token = "" + input.charAt(i) + input.charAt(i+1) + input.charAt(i+2);
 					returnme = extendArray(returnme,token);
 					i += 2;
 				}
 				catch (StringIndexOutOfBoundsException e)
 				{	System.out.printf("Syntax error: Invalid operator length.%n");
 					System.exit(1);
 				}
 			}
 		}
 		return returnme;
 	}
 
 	public static Token[] tokenise(String[] tokenStrings)
 	{	Token[] returnme = new Token[tokenStrings.length];
 		for (int i=0; i<tokenStrings.length; i++)
 		{	if (isNumeric(tokenStrings[i].charAt(0)))
 			{	try
 				{	returnme[i] = new Token(Float.parseFloat(tokenStrings[i]));
 				}
 				catch (NumberFormatException e)
 				{	System.out.printf("Not a recognised operator or number: %s%n",e.getMessage());
 					System.exit(1);
 				}
 			}
 			else
 			{	returnme[i] = new Token(tokenStrings[i]);	
 			}
 		}
 		return returnme;
 	}
 
 	private static void validateOperatorToken(Token token)
 	{	if (token.getValue().length() == 1 && isShortOperator(token.getValue().charAt(0)))
 		{	//it's a valid single character operator token
 			return;
 		}
 		else if (token.getValue().equals("cos"))
 		{	return;
 		}
 		else
 		{	System.out.printf("Syntax error:'%s' is not a valid operator.%n",token);
 			System.exit(1);
 		}
 	}
 
 	//inefficient but quick and dirty
 	private static String[] extendArray(String[] input, String element)
 	{	//if statement due to nasty empty array corner case
 		String[] returnme = new String[input.length+1];
 		int i;
 		for (i=0; i<returnme.length; i++)
 		{	if (i == input.length)
 			{	returnme[i] = element;
 			}
 			else
 			{	returnme[i] = input[i];
 			}
 		}		
 		return returnme;
 	}
 
 	private static boolean isNumeric(char input)
 	{	if (input == '0' || input == '1' || input == '2' || input == '3' || 
 		    input == '4' || input == '5' || input == '6' || input == '7' || 
 		    input == '8' || input == '9' || input == '.')
 		{	return true;
 		}
 		else
 		{	return false;
 		}
 	}
 
 	private static boolean isShortOperator(char input) //ie operator other than cos
 	{	if (input == '!' || input == '*' || input == '+' || input == '-')
 		{	return true;
 		}
 		else
 		{	return false;
 		}
 	}
 }
 
 class Parser
 {	//coming soon
 }
 
 /* 
 simplified grammar, showing precedence:
 expr -> cos expr
 expr -> expr !
 expr -> expr * expr
 expr -> expr + expr
 expr -> expr - expr
 expr -> real
 real -> the set of real numbers
 
 full grammar:
 expr -> cos expr
 expr -> expr !
 expr -> expr * expr
 expr -> expr + expr
 expr -> expr - expr
 expr -> digits.digits
 digits -> digits digit | digit
 digit -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
 */
