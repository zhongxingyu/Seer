 import java.util.Arrays;
 import com.google.gson.*;
 
 public class Tester {
 	public static final int NUM_DIGITS = 5;
 	public static final int NUM_CARRIES = 3;
 
 	public static void main(String[] args) {
 		String addProb = 	"{" + 
 								"probType: \"intAdd\"," + 
 								"description: {" + 
 									"numCarries: 3," + 
 									"numDigits: 5" + 
 								"}" + 
 							"}";
 
 		String subProb = 	"{" + 
 								"probType: \"intSub\"," +
 								"description: {" + 
									"borrows: 3," + 
									"numDigits: [5,5]," +
 									"borrowAccrossZero: false," +
 									"canBeNeg: false" +
 								"}" + 
 							"}";
 
 		String fracAdd = 	"{" + 
 								"probType: \"fracAdd\"," +
 								"description: {" +
 									"denomToConvert: 1," +
 									"sumAboveOne: false" + 
 								"}" +
 							"}";
 
 		
 		JsonParser parser = new JsonParser();
 		JsonObject description = parser.parse(subProb).getAsJsonObject();
 		ProblemGenerator pg = new ProblemGenerator(description);
 		
 		for (int i = 0; i < 100; i++) {
 			pg.next();
 		}
 
 	}
 	
 	private int[] arrayFromInt(int num, int numDig) {
 		String data = "" + num;
 		while (data.length() < numDig)
 			data = "0" + data;
 		int[] result = new int[data.length()];
 		for (int i = 0; i < result.length; i++)
 			result[i] = data.charAt(i) - '0';
 		return result;
 	}
 }
