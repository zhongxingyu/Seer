 package kademlia;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 
 public class Identifier implements Identifiable {
 	protected BigInteger value;
 	protected static final SecureRandom secureRandom = new SecureRandom();
 	
 	protected Identifier() {
 		
 	}
 	
 	protected Identifier(BigInteger value) {
 		this.value = value;
 	}
 	
 	public BigInteger getIntegerValue() {
 		return value;
 	}
 	
 	public static BigInteger calculateDistance(Identifier x, Identifier y) {
 		return x.getIntegerValue().xor(y.getIntegerValue());
 	}
 	
 	public static BigInteger calculateDistance(Identifiable x, Identifiable y) {
 		return calculateDistance(x.getIdentifier(), y.getIdentifier());
 	}
  	
 	public Identifier getIdentifier() {
 		return this;
 	}
 	
 	public static Identifier fromBytes(byte[] bytes) {
		return new Identifier(new BigInteger(1, bytes));
 	}
 	
 	public static Identifier generateRandom() {
 		byte[] randomBytes = new byte[20];
 		secureRandom.nextBytes(randomBytes);
 		return fromBytes(randomBytes);
 	}
 }
