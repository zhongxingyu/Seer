 package cmdGA2.returnvalues;
 
 public class IntegerValue extends ReturnValueParser<Integer> {
 
 	@Override
 	public Integer parse(String token){
 		
		return Integer.valueOf(token.trim());
 	
 	}
 
 }
