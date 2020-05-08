 package com.erman.football.client;
 
 import com.erman.football.client.json.ExceptionJso;
 import com.erman.football.client.json.UserJso;
 import com.erman.football.shared.ClientPlayer;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class FacebookUtil {
 
 	private Login login;
 	
 	public FacebookUtil(Login _login){
 		this.login = _login;
 		this.exportMethods(this);
		//this.initFacebookAPI();
 		//this.subscribeLogin();
 		//this.subscribeLogout();
 	}
 	
 	public void setLogin(Login _login){
 		this.login = _login;
 	}
 
 	private native String initFacebookAPI()
 	/*-{
		//$wnd.FB.init({appId: "334533903236590", status: true, cookie: true, xfbml: true});
 		
 		//$wnd.FB.getLoginStatus(function(response) {
 		//	
 		// 	if (response.status === "connected") {
 		//  		$doc.getElementById("info").innerHTML = "connected";
 		//    	$wnd.onLogin(); 
 		//  	} else if (response.status === 'not_authorized') {
 		//    	// the user is logged in to Facebook, 
 		//    	//but not connected to the app
 		//    	$doc.getElementById("info").innerHTML = "not auth";
 		//    	$wnd.initLogin(); 
 		//  	} else {
 		//    	// the user isn't even logged in to Facebook.
 		//    	$doc.getElementById("info").innerHTML = "not login__";
 		//   	$wnd.initLogin(); 
 		//	}
 		//});
 		
 	}-*/;
 	
 	private native void testJS()/*-{
 		$wnd.faceTest();
 	}-*/;
 	
 	private native void exportMethods(FacebookUtil instance) /*-{
 		$wnd.onLogin = function() {
 			return instance.@com.erman.football.client.FacebookUtil::onLogin()();
 		}
 		$wnd.onLogout = function() {
 			return instance.@com.erman.football.client.FacebookUtil::onLogout()();
 		}
 		$wnd.initLogin = function() {
 			return instance.@com.erman.football.client.FacebookUtil::initLogin()();
 		}
 		$wnd.onAPICall = function(callback, response, exception) {
 			return instance.@com.erman.football.client.FacebookUtil::onAPICall(Lcom/google/gwt/user/client/rpc/AsyncCallback;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;)(callback, response, exception);
 		}
 		
 	}-*/;
 	
 	public void onAPICall(AsyncCallback<JavaScriptObject> callback,
 			JavaScriptObject response, JavaScriptObject exception) {
 		if (response != null) {
 			callback.onSuccess(response);
 		} else {
 			ExceptionJso e = (ExceptionJso) exception;
 			callback.onFailure(new Exception(e.getType() + ": " + e.getMessage()));
 		}
 	}
 
 	private void initLogin(){
 		login.login(null);
 	}
 	
 	public void onLogin() {
 		//this.subscribeLogout();
 		getMe();
 	}
 	
 	/**
 	 * To be called after an XFBML-tag has been inserted into the DOM
 	 */
 	private native void parseDomTree() /*-{
 		$wnd.FB.XFBML.parse();
 	}-*/;
 
 	public void onLogout() {
 		//this.subscribeLogin();
 		login.logout();
 	}
 	
 	
 	private native void callAPI(String path, AsyncCallback<JavaScriptObject> callback) /*-{
 		$wnd.FB.api(path ,function(response) {
 			// on error, this callback is never called in Firefox - why?
 			if (!response) {
 			} else if (response.error) {
 			    // call callback with the actual error
 				$wnd.onAPICall(callback, null, response.error);
 			} else if (response.data) {			
 				// call callback with the actual json-array
 				$wnd.onAPICall(callback, response.data, null);
 			} else {
 				// call callback with the actual json-object
 				$wnd.onAPICall(callback, response, null);
 			} 
 		});
 	}-*/;
 	
 	public void getMe() {
 		callAPI("/me", new AsyncCallback<JavaScriptObject>() {
 
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 
 			public void onSuccess(JavaScriptObject result) {
 				UserJso fbUser = (UserJso) result;
 				ClientPlayer player = new ClientPlayer();
 				player.setName(fbUser.getFullName());
 				player.setFacebookId(Long.parseLong(fbUser.getId()));
 				//User is logged in from facebook
 				login.login(player);
 			}
 		});
 	}
 	
 	public static native void login() /*-{
 		 $wnd.FB.login(function(response) {
    			if (response.authResponse) {
 		    	$wnd.onLogin(); 
    			}
 		 });
 	}-*/;
 	
 	public static native void logout() /*-{
 		$wnd.FB.logout(function(response) {
   			// user is now logged out
 		});
 	}-*/;
 	
 	
 	public native void subscribeLogout() /*-{	
 		$wnd.FB.Event.subscribe("auth.logout", function() {
 			$doc.getElementById("info").innerHTML = "logout event received";
 			//The user has logged out, and the cookie has been cleared
 			$wnd.onLogout();         
 		});
 		$doc.getElementById("info").innerHTML = "subscriebed logout";
 	}-*/;
 	
 	public native void subscribeLogin() /*-{
 		$wnd.FB.Event.subscribe("auth.login", function() {
 			$doc.getElementById("info").innerHTML = "login event received";
 			//The user has logged out, and the cookie has been cleared
 			$wnd.onLogin();         
 		});
 		$doc.getElementById("info").innerHTML = "subscriebed login";
 }-*/;
 	
 }
