 package ioMethods;
 
 import java.math.BigInteger;
 import java.util.HashMap;
 
 import core.Fraction;
 
 public class ProgramInstance {
 
 	private HashMap<String,Fraction> savedVariables;
 	private DigitNumberStringExpression expr;
 	
 	public ProgramInstance(){
 		savedVariables = new HashMap<String,Fraction>(20);
 	}
 	
 	public String getLogResult(String number, BigInteger inputBase, String variable, BigInteger outputBase){
 		if(variable.equals("<NONE>")){
 			expr = new DigitNumberStringExpression(number,inputBase);
 			if(expr.isValidInputArgs()){
 				return getLogString(number,expr,inputBase,outputBase);
 			}
 			else{
 				expr = null;
 				return "Invalid Input Arguments. Click Help for Details.";
 			}
 		}
 		else{
 			expr = new DigitNumberStringExpression(savedVariables.get(variable));
 			return getLogString(expr,outputBase);
 		}
 	}
 	
 	private String getLogString(DigitNumberStringExpression expression, BigInteger outputBase){
 		String numberString = expression.OutputFraction(new BigInteger("10"));
		String resultString = getResultString(expression,outputBase);
		return String.format("%s is the following in base %s:\n%s", numberString,outputBase.toString(),resultString);
 	}
 	private String getLogString(String number, DigitNumberStringExpression expression, BigInteger inputBase, BigInteger outputBase){
		String resultString = getResultString(expression,outputBase);
		return String.format("%s in base %s is the following in base %s:\n%s", number,inputBase.toString(),outputBase.toString(),resultString);
 	}
 	
 	private String getResultString(DigitNumberStringExpression expression, BigInteger outputBase){
 		String fractionResult = expression.OutputFraction(outputBase).trim();
 		String decimalResult = expression.OutputDecimal(outputBase).trim();
 		
 		//output is an integer, so if both are outputted, there will be a repeated result
 		if(fractionResult.equals(decimalResult)){
 			return fractionResult;
 		}
 		else{
			return String.format("%s\n%s", fractionResult,decimalResult);
 		}
 	}
 	
 	public boolean saveLastResult(String variableName){
 		if(expr != null){
 			savedVariables.put(variableName, expr.getTheNumber());
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 
 	public HashMap<String, Fraction> getSavedVariables() {
 		return savedVariables;
 	}
 	
 
 }
