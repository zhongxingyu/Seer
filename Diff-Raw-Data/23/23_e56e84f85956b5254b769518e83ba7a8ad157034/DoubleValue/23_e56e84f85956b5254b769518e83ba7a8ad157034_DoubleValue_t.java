 package cmdGA2.returnvalues;
 
 public class DoubleValue extends ReturnValueParser<Double> {
 
 	@Override
 	public Double parse(String token) {
 		
		return Double.valueOf(token.trim());
 		
 	}
 
 }
