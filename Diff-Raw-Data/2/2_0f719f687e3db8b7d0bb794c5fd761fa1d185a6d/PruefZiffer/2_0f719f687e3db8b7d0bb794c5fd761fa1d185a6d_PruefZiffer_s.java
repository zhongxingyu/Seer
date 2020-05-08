 import java.util.ArrayList;
 
 public class PruefZiffer {
 	static ArrayList<String> _results = new ArrayList<String>();
 	static ArrayList<String> _gewichtungen = new ArrayList<String>();
 	static Integer _gewichtungMax = 999999999;
 	static String[] _accounts = { "0060002687", "0060003577", "0060013776",
 			"0060012657", "0060002821", "0060009432", "0060002799",
 			"0060011978", "0060004233", "0060004386", "0060013829",
 			"0060013738", "0060007767", "0060004957", "0060013254" };
 
 	public static void main(String[] args) {
		for (int i = 0; i < _gewichtungMax; i++) {
 			recordMatch(fillWithZeros(i));
 		}
 
 		for (int i = 0; i < _results.size(); i++) {
 			System.out.println("REEESULT: " + _results.get(i));
 		}
 	}
 
 	private static void recordMatch(String gewichtung) {
 		System.out.println("Checking: " + gewichtung);
 		boolean success = true;
 		for (int i = 0; i < _accounts.length; i++) {
 			int summe = 0;
 			for (int j = 0; j < gewichtung.length(); j++) {
 				int first = Integer.parseInt(gewichtung.substring(j, j + 1));
 				int second = Integer.parseInt(_accounts[i].substring(j, j + 1));
 				summe += first * second;
 			}
 			int pruefziffer = summe % 11;
 			if (pruefziffer != Integer.parseInt(_accounts[i]
 					.substring(_accounts[i].length() - 1))) {
 				success = false;
 				break;
 			}
 		}
 		if (success)
 			_results.add(gewichtung);
 	}
 
 	private static String fillWithZeros(int i) {
 		String result = "" + i;
 		while (result.length() < _gewichtungMax.toString().length()) {
 			result = "0" + result;
 		}
 		return result;
 	}
 }
