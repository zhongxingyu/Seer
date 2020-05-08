 package cmdGA2.returnvalues;
 
 public class BooleanValue extends ReturnValueParser<Boolean> {
 
 	@Override
 	public Boolean parse(String token) {
 		
		return Boolean.valueOf(token.trim());
 		
 	}
 
 }
