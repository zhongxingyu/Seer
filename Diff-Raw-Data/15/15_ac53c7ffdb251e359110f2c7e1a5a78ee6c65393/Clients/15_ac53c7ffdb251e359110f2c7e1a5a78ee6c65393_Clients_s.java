 package cyberprime.entities;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.UUID;
 
 import cyberprime.util.Algorithms;
 
 
 public class Clients {
 	
 	private String userId;
 	private String imageHash;
 	private int imageSize;
 	private String imageExtension;
 	private String email;
 	private String pattern;
 	private String token;
 	private String activation;
	private String loggedIn;
	
 	
 	public Clients(){
 		super();
 	}
 	
	public Clients(String userId, String imageHash, int imageSize, String imageExtension, String email, String pattern, String loggedIn) {
 		this();
 		this.userId = userId;
 		this.imageHash = imageHash;
 		this.imageSize = imageSize;
 		this.imageExtension = imageExtension;
 		this.email = email;
 		this.pattern = pattern;
		this.loggedIn = loggedIn;
 	}
 	
 	
 	public String getUserId() {
 		return userId;
 	}
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 	public String getImageHash() {
 		return imageHash;
 	}
 	public void setImageHash(String imageHash) {
 		this.imageHash = imageHash;
 	}
	public String getLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(String loggedIn) {
		this.loggedIn = loggedIn;
	}
 
 	public int getImageSize() {
 		return imageSize;
 	}
 	public void setImageSize(int imageSize) {
 		this.imageSize = imageSize;
 	}
 	public String getImageExtension() {
 		return imageExtension;
 	}
 	public void setImageExtension(String imageExtension) {
 		this.imageExtension = imageExtension;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	public String getPattern() {
 		return pattern;
 	}
 	
 	public void setDBPattern(String pattern){
 		this.pattern = pattern;
 	}
 	public void setPattern(String pattern) throws NoSuchAlgorithmException, UnsupportedEncodingException {
 		this.pattern = Algorithms.getHash(pattern,this.userId);
 	}
 
 	public String getToken() {
 		return token;
 	}
 
 	public void setToken() {
 		this.token =  UUID.randomUUID().toString();
 	}
 
 	public void setToken (String token){
 		this.token = token;
 	}
 	
 	public String getActivation() {
 		return activation;
 	}
 
 	public void setActivation(String activation) {
 		this.activation = activation;
 	}
 	
 	
 
 }
