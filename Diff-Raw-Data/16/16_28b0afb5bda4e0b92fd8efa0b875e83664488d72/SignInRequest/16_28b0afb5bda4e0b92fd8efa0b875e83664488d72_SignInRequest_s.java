 package elfville.protocol;
 
 import javax.crypto.SecretKey;
 
 public class SignInRequest extends Request {
 	private static final long serialVersionUID = 1L;
 	public String username;
 	public char[] password;
 	public SecretKey shared_key;
 
 	// public byte[] nonce; TODO
 
 	// TODO add time in
 
 	public SignInRequest(String u, char[] p, SecretKey s) {
 		username = u;
 		password = p;
 		shared_key = s;
 		// nonce = n;
 		// TODO send time
 	}
 
 	public void zeroPasswordArray() {
 		for (int i = 0; i < password.length; i++) {
 			password[i] = 0;
 		}
 	}
 }
