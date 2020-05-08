 package safety.onlyoneapplicable;
 
 import general.classes.Fix;
 import general.classes.VerificationResult;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 
 
 public class Fixer {
 	
 	private String invertRuleEffect(String originalEffect)
 	{
 		if (originalEffect.equals("Permit"))
 			return "Deny";
 		
 		return "Permit"; // need to catch exception
 	}
 	
 	public ArrayList<Fix> propose(VerificationResult ver) {
 
 		ArrayList<Fix> result = new ArrayList<Fix>();
 		
 		Fix f;
 		
 		f = new Fix();
 		f.id = "1";
 		f.statement = String.format("In policy '%s', rule '%s', change the rule effect to '%s'", 
 				ver.Policy1, ver.Rule1, invertRuleEffect(ver.Rule1Effect)
 				);
 		f.est = "-";
 		result.add(f);
 
 		
 		f = new Fix();
 		f.id = "2";
 		f.statement = String.format("In policy '%s', rule '%s', change the rule effect to '%s'", 
 				ver.Policy2, ver.Rule2, invertRuleEffect(ver.Rule2Effect)
 				);
 		f.est = "-";
 		result.add(f);
 		
 		f = new Fix();
 		f.id = "3";
 		f.statement = String.format("In policy '%s' remove the rule '%s'", 
 				ver.Policy1, ver.Rule1
 				);
 		f.est = "-";
 		result.add(f);
 		
 		f = new Fix();
 		f.id = "4";
 		f.statement = String.format("In policy '%s' remove the rule '%s'", 
 				ver.Policy2, ver.Rule2
 				);
 		f.est = "-";
 		result.add(f);
 		
 		return result;
 	}
 
 	
 	public String fix(String inputFileContents, VerificationResult ver, String fixId) throws Exception {
 		
		String s = "dawfg" + inputFileContents;
 		
 		s += "OK"; // PROCESS THE FILE HERE
 		
 		return s;
 	}
 	
 	
 }
