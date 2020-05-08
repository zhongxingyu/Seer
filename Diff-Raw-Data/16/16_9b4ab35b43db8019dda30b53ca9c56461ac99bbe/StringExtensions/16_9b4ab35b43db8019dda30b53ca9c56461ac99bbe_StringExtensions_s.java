 package tools;
 
 import play.templates.JavaExtensions;
 
 public class StringExtensions extends JavaExtensions {
 	public static String joinWithDot(String s){
 		String arrayString[] = s.split("\\s+");
 		String finalString = "";
 		for (int i=0; i < arrayString.length - 1; i++){
 			finalString = finalString + arrayString[i] + ".";
 		}
		if(!arrayString[arrayString.length-1].equals("de"))
			finalString = finalString + arrayString[arrayString.length-1];
		else
			finalString = finalString.substring(0, finalString.length()-1);
 		return finalString;
 	}
 	
 }
