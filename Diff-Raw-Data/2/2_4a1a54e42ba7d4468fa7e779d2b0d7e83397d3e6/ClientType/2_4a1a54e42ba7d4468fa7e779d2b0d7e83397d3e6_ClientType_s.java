 package btwmod.centralchat;
 
 public enum ClientType {
 
	SERVER("server"), USER("client");
 	
 	private final String asString;
 	
 	private ClientType(String asString) {
 		this.asString = asString;
 	}
 	
 	public static ClientType get(String value) {
 		if (value == null)
 			return null;
 		
 		try {
 			return Enum.valueOf(ClientType.class, value.toUpperCase());
 		}
 		catch (IllegalArgumentException e) {
 			return null;
 		}
 	}
 	
 	public static boolean isValid(String value) {
 		return get(value) != null;
 	}
 	
 	@Override
 	public String toString() {
 		return asString;
 	}
 }
