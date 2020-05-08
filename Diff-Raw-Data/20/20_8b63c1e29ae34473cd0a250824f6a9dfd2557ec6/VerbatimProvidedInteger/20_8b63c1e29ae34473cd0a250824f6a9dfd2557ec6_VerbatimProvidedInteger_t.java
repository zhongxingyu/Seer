 package se.netlight.dimlight.dao;
 
 public class VerbatimProvidedInteger extends ProvidedInteger {
 	private String amount;
 
 	public VerbatimProvidedInteger(String amount) {
 		this.amount = amount;
 	}
 
 	@Override
 	public int asInteger() {
		int i=0;
		for (; i < amount.length(); i++) {
			if (!Character.isDigit(amount.charAt(i)))
				break;
		}
		return Integer.parseInt(amount.substring(0, i));
 	}
 
 	@Override
 	public String toString() {
 		return amount;
 	}
 
 	@Override
 	public ProvidedInteger add(int delta) {
		// mimick the behavior of "smartly-typed languages"
		try {			
			String roundtrip = String.valueOf(Integer.parseInt(amount));
			if (roundtrip.equals(amount)) {
				return new VerbatimProvidedInteger(String.valueOf(Integer.parseInt(amount) + delta));
			}
		} catch (NumberFormatException e) {}
 		return new VerbatimProvidedInteger(delta + amount);
 	}
 }
