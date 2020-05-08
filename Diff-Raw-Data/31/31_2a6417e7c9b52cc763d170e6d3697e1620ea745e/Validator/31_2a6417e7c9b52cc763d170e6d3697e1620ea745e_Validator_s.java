 package neural.parsec.validation;
 
 import java.util.List;
 
 import neural.parsec.ast.Parameter;
 
 public class Validator {
 	
 	
 	private List<String> mandantoryNames;
 	private List<String> optionalNames;
 	
 	private boolean valid = false;
 
 	public void validate(List<Parameter> parameters) {
 		valid = true;
 		for (Parameter p: parameters) {
 			if (!mandantoryNames.contains(p.getName()) && !optionalNames.contains(p.getName())) {
 				valid = false;
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
 }
