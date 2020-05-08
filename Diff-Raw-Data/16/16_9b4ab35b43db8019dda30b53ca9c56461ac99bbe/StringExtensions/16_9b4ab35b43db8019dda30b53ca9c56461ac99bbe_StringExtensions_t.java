 package tools;
 
 import play.templates.JavaExtensions;
 
 public class StringExtensions extends JavaExtensions {
 	public static String joinWithDot(String s){
 		String arrayString[] = s.split("\\s+");
 		String finalString = "";
 		for (int i=0; i < arrayString.length - 1; i++){
 			finalString = finalString + arrayString[i] + ".";
 		}
		finalString = finalString + arrayString[arrayString.length-1];
		
 		return finalString;
 	}
 	
 }
