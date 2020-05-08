 package elfville.protocol;
 
 import javax.crypto.SecretKey;
 
 public class SignUpRequest extends SignInRequest {
 	private static final long serialVersionUID = 1L;
 	public String description;
 
 	public SignUpRequest(String u, char[] p, SecretKey s, String d) {
 		super(u, p, s);
 		description = d;
 	}
 }
