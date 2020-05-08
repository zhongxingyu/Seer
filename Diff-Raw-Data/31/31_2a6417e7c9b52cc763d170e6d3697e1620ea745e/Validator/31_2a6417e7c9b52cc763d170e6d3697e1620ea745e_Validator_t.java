 package neural.parsec.validation;
 
import java.util.ArrayList;
 import java.util.List;
 
 import neural.parsec.ast.Parameter;
 
 public class Validator {
 	
 	
 	private List<String> mandantoryNames;
 	private List<String> optionalNames;
	private List<String> issues;
 	
 	private boolean valid = false;
 
 	public void validate(List<Parameter> parameters) {
 		valid = true;
		issues = new ArrayList<String>();
 		for (Parameter p: parameters) {
 			if (!mandantoryNames.contains(p.getName()) && !optionalNames.contains(p.getName())) {
 				valid = false;
				issues.add("Parameter '"+p.getName()+"' is present but not allowed as option or mandantory");
 			}
 		}
 		for (String m: mandantoryNames) {
 			boolean here = false;
 			for (Parameter p: parameters) {
 				if (p.getName().equals(m))
 					here = true;
 			}
 			if (!here)
 				valid = false;
				issues.add("Mandantory parameter '"+m+"' is missing");
 		}
 	}
 	
 	public boolean isValid() {
 		return valid;
 	}
 
 	public void setMandantory(List<String> names) {
 		this.mandantoryNames = names;
 	}
 	
 	public void setOptional(List<String> names) {
 		this.optionalNames = names;
 	}
	
	public String[] getIssues() {
		return issues.toArray(new String[0]);
	}
 }
