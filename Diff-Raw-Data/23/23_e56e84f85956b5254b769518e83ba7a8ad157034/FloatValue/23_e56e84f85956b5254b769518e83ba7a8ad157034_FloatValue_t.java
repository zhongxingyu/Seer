 package cmdGA2.returnvalues;
 
 public class FloatValue extends ReturnValueParser<Float> {
 
 	@Override
 	public Float parse(String token) {
 
		return Float.valueOf(token.trim());
 		
 	}
 
 }
