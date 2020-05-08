 package se.netlight.dimlight.dao;
 
 public class VerbatimProvidedInteger extends ProvidedInteger {
 	private String amount;
 
 	public VerbatimProvidedInteger(String amount) {
 		this.amount = amount;
 	}
 
 	@Override
 	public int asInteger() {
		return Integer.parseInt(amount);
 	}
 
 	@Override
 	public String toString() {
 		return amount;
 	}
 
 	@Override
 	public ProvidedInteger add(int delta) {
		// mimick the behavior of "smartly-typed languages"		
		String roundtrip = String.valueOf(Integer.parseInt(amount));
		if (roundtrip.equals(amount)) {
			return new VerbatimProvidedInteger(String.valueOf(Integer.parseInt(amount) + delta));
		}
 		return new VerbatimProvidedInteger(delta + amount);
 	}
 }
