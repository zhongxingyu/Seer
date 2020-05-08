 package tgc2010.ui.tool;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import tgc2010.ui.Checkpoint;
 
 public class Value {
 
 	public static String serializeValue(Checkpoint c1, Checkpoint c2, String contact, String inforamtion, String from){
 		// Checkpoint1
 		String  checkpoint1 = c1.getAddress().equals("") ? "" : "+\tAddress: " + c1.getAddress();
 		checkpoint1 += c1.getZipCode().equals("") ? "" : "+\tZip Code: " + c1.getZipCode();
 		checkpoint1 += c1.getCity().equals("") ? "" : "+\tCity: " + c1.getCity();
 		checkpoint1 += "+\tTime: " + c1.getHour() + "h" + c1.getMinute();
 		// Checkpoint2
 		String checkpoint2 = c2.getAddress().equals("") ? "" : "+\tAddress: " + c2.getAddress();
 		checkpoint2 += c2.getZipCode().equals("") ? "" : "+\tZip Code: " + c2.getZipCode();
 		checkpoint2 += c2.getCity().equals("") ? "" : "+\tCity: " + c2.getCity();
 		checkpoint2 += "+\tTime: " + c2.getHour() + "h" + c2.getMinute();
 		System.out.println("FROM:" + "+" + checkpoint1 + "+TO:" + "+" +  checkpoint2 + "+*\tContact: " +contact + "+Information: " + inforamtion + "+Given by: " + from);
		return "FROM:" + checkpoint1 + "+TO:" +  checkpoint2 + "+*\tContact: " +contact + "+Information: " + inforamtion + "+Given by: " + from;
 	}
 
 	public static String deserializeValue(String value){
 		// Compile regular expression
 		Pattern pattern = Pattern.compile("\\*");
 		// Replace all occurrences of pattern in input
 		Matcher matcher = pattern.matcher(value);
 		value = matcher.replaceAll("\n");
 		// display the arguments
 		String[] args =  value.split("\\+");
 		String result = "";
 		for(String arg : args){
 			result += "\n\t" + arg;
 		}
 		result += "\n__________________________________________";
 		return result;
 	}
 
 }
