 
 package backend.computations.infrastructure;
 
 import java.util.List;
 
 import backend.blocks.Countable;
 import backend.blocks.Countable.DisplayType;
 
 
 /** 
  *  Implementors for Computable take in 1-2 matrices, perform a specified 
  *  operation on the matrices, and provide getters for the result
  *  
  * @author baebi
  */
 public abstract class Computable{
 	
 	
 	/** 
 	 * Returns the solution computed in the constructor of this Computable
 	 * 
 	 * @return the Solution to this Computable
 	 */
 	public abstract Solution getSolution();
 	
 	
 	/**
 	 * @return a list of LateX strings, where each one is a step. (Or whatever at your discretion)
 	 */
 	public abstract List<String> toLatex();
 	
 	
 	/** 
 	 * Given multiple countables, returns the preferential DisplayType
 	 * 
 	 * @param countables the countables with DisplayTypes
 	 * @return the dominant DisplayType (Reminder: prefer Custom > Decimal > DecimalFraction > WholeNumberFraction > WholeNumber)
 	 */
 	public static DisplayType resolveDisplayType(List<Countable> countables){
 		boolean foundCustom,foundDecimal,foundWholeNumber,foundWholeNumberFraction;
 		foundCustom = foundDecimal = foundWholeNumber = foundWholeNumberFraction = false;
 		for (Countable countable : countables){
 			DisplayType type = countable.getDisplayType();
 			switch (type){
 				case CUSTOM: {
 					foundCustom = true;
 					break;
 				}
 				case WHOLENUMBER: {
 					foundWholeNumber = true;
 					break;
 				}
 				case WHOLENUMBERFRACTION:{
 					foundWholeNumberFraction = true;
 					break;
 				}
 				case DECIMAL:{
 					foundDecimal = true;
 					break;
 				}
 				default:{
 					System.err.println("ERROR (ResolveDisplayType): Unrecognized DisplayType");
 				}
 			}
 		}
 		
 		if (foundCustom){
 			return DisplayType.CUSTOM;
 		}else if(foundDecimal){
 			return DisplayType.DECIMAL;
 		}else if (foundWholeNumberFraction){
 			return DisplayType.WHOLENUMBERFRACTION;
 		}else if (foundWholeNumber){
 			return DisplayType.WHOLENUMBER;
 		}else{
 			System.err.println("ERROR (Computable): Did not find any enums");
 			return null;
 		}
 	}
 	
 	
 	/** 
 	 * Converts the double value to the proper display-type
 	 * 
 	 * @param input the double value to convert
 	 * @return the same value as a string in the correct format
 	 */
 	public String getDisplayValue(double input,DisplayType type){
 		switch (type){
 			case DECIMAL:{
 				return Double.toString(input);
 			}
 			case WHOLENUMBERFRACTION:{
 				// TODO implement
 				return null;
 			}
 			case WHOLENUMBER:{
 				return Integer.toString((int) Math.floor(input));
 			}
 			case CUSTOM:{
 				return null;
 			}
 			default:{
 				System.err.println("ERROR: uncaught display value");
 				return null;
 			}
 		}
 	}
 	
 	/**
 	 * Shortens a decimal if it is too long
 	 * 
	 * @param num
	 * @return
 	 */
 	public String shortenDecimal(String num){
 		// truncate long decimals
 
 		boolean foundDecimal = false;
 		int numAfterDecimal = 0;
 		for (int k = 0; k < num.length(); k++){
 			if (num.charAt(k) == '.'){
 				foundDecimal = true;
 			}
 			if (foundDecimal){
 				numAfterDecimal++;
 			}
 			if (numAfterDecimal > 4){
 				num = num.substring(0, k) + "...";
 				return num;
 			}
 		}
 		return num;
 	}
 	
 }
