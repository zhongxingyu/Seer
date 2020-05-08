 package com.playon.rpc.client;
 
 import dojo.Deferred;
 import dojo.DeferredCommand;
 
 public class OAuthLogonService implements LogonService {
 
 	private Deferred requestToken;
 
 	public OAuthLogonService() {
 		// Get a requestToken for this service instance
 		this.requestToken = this.getRequestToken();
 	}
 
 	@Override
 	public Deferred logon(String username, String password) {
 		final Deferred logon = new Deferred();
 
 		// Wait for the requestToken to be available
 		logon.addDeferred(this.requestToken);
 
 		// Authorize requestToken with username and password
 		logon.addDeferred(this.authorizeToken(username, password));
 
 		// Retrieve the accessToken
 		logon.addDeferred(this.getAccessToken());
 
 		// Kickoff the deferred chain
 		logon.callback(null);
 		return logon;
 	}
 
 	@Override
 	public Deferred logout() {
 		return new Deferred();
 	}
 
 	private Deferred getRequestToken() {
 		final Deferred dfd = new Deferred();
 
 		dfd.addCallbacks(new DeferredCommand() {
 			@Override
 			public <T> Object execute(T result) {
 				return OAuthLogonService.this.processToken(result);
 			}
 		}, new DeferredCommand() {
 			@Override
 			public <T> Object execute(T result) {
 				return new RuntimeException(
 						"Unable to retrieve OAuth Request Token");
 			}
 		});
 
 		// Now do HTTP request for Request Token
 		// Success/Error on HTTP request should invoke
 		// dfd.callback(rawToken) or dfd.errback(error)
 
 		return dfd;
 	}
 
 	private Deferred authorizeToken(final String username, final String password) {
 		// this deferred needs to return the requestToken it was called with
 		Deferred dfd = new Deferred();
		dfd.addCallbacks(new DeferredCommand() {
 			@Override
 			public <T> Deferred execute(T rToken) {
 				Deferred http = new Deferred();
 
 				// Do HTTP request to authorize token
 				// Success/Error on HTTP request should invoke
 				// dfd.callback(rToken) or dfd.errback(error)
				
				http.callback(rToken);
 				return http;
 			}
		}, new DeferredCommand() {
			@Override
			public <T> Object execute(T result) {
				return new IllegalArgumentException(
						"Invalid username or password");
			}
 		});
 
 		return dfd;
 	}
 
 	private Deferred getAccessToken() {
 		// this deferred should return true when user is logged on
 		Deferred dfd = new Deferred();
		dfd.addCallbacks(new DeferredCommand() {
 			@Override
 			public <T> Deferred execute(T rToken) {
 				Deferred http = new Deferred();
 
 				// Do HTTP request to authorize token
 				// Success/Error on HTTP request should invoke
 				// dfd.callback(true) or dfd.errback(error)
 				String rawToken = "";
 
 				Object aToken = OAuthLogonService.this.processToken(rawToken);
 				OAuthLogonService.this.persistCredentials(aToken);
 
 				http.callback(Boolean.TRUE);
 
 				return http;
 			}
		}, new DeferredCommand() {
			@Override
			public <T> Object execute(T result) {
				return new RuntimeException(
						"Unable to retrieve OAuth Access Token");
			}
 		});
 
 		return dfd;
 	}
 
 	private Object processToken(Object rawToken) {
 		Object processedToken = null;
 		return processedToken;
 	}
 
 	private void persistCredentials(Object credentials) {
 
 	}
 
 }
