 package services.openid;
 
 public enum OpenIdProvider {

	GOOGLE("https://www.google.com/accounts/o8/id");
 
 	private String url;
 
 	private OpenIdProvider(String url) {
 		this.url = url;
 	}
 
 	public String url() {
 		return url;
 	}
 }
