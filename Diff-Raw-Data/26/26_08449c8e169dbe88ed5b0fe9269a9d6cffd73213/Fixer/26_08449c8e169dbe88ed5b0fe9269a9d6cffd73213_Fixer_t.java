 package safety.onlyoneapplicable;
 
 import general.classes.Fix;
 import general.classes.VerificationResult;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
 
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
 		
		String s = inputFileContents;
		
		if (fixId.equals("1"))
			s = replaceRuleEffect(s, ver.Rule1, ver.Rule1Effect);
 		
 		s += "OK"; // PROCESS THE FILE HERE
 		
 		return s;
 	}

	private String replaceRuleEffect(String source, String rule1, String rule1Effect) {
		// TODO Auto-generated method stub
		String s = source;
		
		return s; /// TMP
/*
		String pattern = String.format("(one sig \\%s extends Rule {}{)(.*)(})", rule1);
		
		Pattern patt = Pattern.compile(pattern);
		Matcher m = patt.matcher(s);
		String ruleContents = m.group(2);
		System.out.println(ruleContents);
		
		return s;
*/		
	}
 	
 	
 }
