 package de.furryhome.e621api;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import de.furryhome.e621api.Exceptions.e621apiObjectException;
 
 class ApiMain {
 	private static String Username = "";
 	private static String PasswordHash = "";
 	private static boolean isLoginCredentialsSet = false;
 	
 	/*
 	 * Getter
 	 */
	public static final boolean isLoginCredentialsSet() {	return isLoginCredentialsSet; }
 	protected static final String getPasswordHash() { return PasswordHash; }
 	protected static final String getUsername() { return Username; }
 	
 	/*
 	 * Setter
 	 */
 	private static final void setPasswordHash(String passwordHash) { PasswordHash = passwordHash; }
 	private static final void setUsername(String username) { Username = username; }
 	private static final void setLoginCredentialsSet(Boolean Value) { isLoginCredentialsSet = Value; }
 	
 	public final void setLogincredentials(String Username, String Password) throws e621apiObjectException {
 		if (Username.length() == 0 || Password.length() == 0) { throw new e621apiObjectException("Username or password may not be empty."); }
 		setUsername(Username);
 		setPasswordHash(this.HashPassword(Password));
		setLoginCredentialsSet(true);
 	}
 
 	/*
 	 * Helferfunktionen
 	 */
 	private final String HashPassword(String Password) {
         MessageDigest md;
 		try {
 			md = MessageDigest.getInstance("SHA-1");
 		} catch (NoSuchAlgorithmException e) {
 			System.err.println("ERROR: Alghorithm SHA-1 not found!");
 			return "";
 		}
         md.update(Password.getBytes());
         byte byteData[] = md.digest();
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < byteData.length; i++) {
          sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
         }
         return sb.toString();
     }
 }
