 package services.openid;
 
 public enum OpenIdProvider {
	;
 
 	private String url;
 
 	private OpenIdProvider(String url) {
 		this.url = url;
 	}
 
 	public String url() {
 		return url;
 	}
 }
