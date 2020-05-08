 package controllers;
 
 import java.util.*;
 
import java.lang.reflect.*;
 import com.google.gson.*;
 import com.google.gson.reflect.*;
 import play.data.validation.*;
 
 import play.*;
 import play.mvc.*;
 import play.libs.*;
 import play.libs.F.*;
 
 import models.*;
 
 /**
  * A convenience class to hold common methods used
  * by the our controllers */
 public abstract class Index extends CRUD {
 	
 	/**
 	 * Catch any argument exceptions thrown by children methods, 
 	 * logs the error and returns a failed JSON response */
 	@Catch(Index.ArgumentException.class)
     public static void log(Index.ArgumentException e) {
         Logger.error("Caught Illegal Argument %s", e);
 		returnFailed(e.toString(), e.getCallback());
     }
 	
 	/**
 	 * This method is utilized by children to protected
 	 * methods we need to authenticate for*/
     protected static void checkAuthentication() {
        	if (!Security.isConnected()) {
 			try {
 				Secure.login();
 			} catch (Throwable e) {
 				Logger.error("authentication excpetion %s", e);
 			}
 		}
     }
 	
 	/**
 	 * @param msg the error message to include in the response
 	 * @return a hashmap with a JSON status => error response, and 
 	 * a message => msg. */
 	protected static HashMap<String, String> getErrorResponse (String msg) {
 		HashMap<String, String> resp = new HashMap<String, String>();
 		resp.put("status", "error");
 		resp.put("message", msg);
 		return resp;
 	}
 	
 	/**
 	 * @return a hashmap with a JSON status => okay response */
 	protected static HashMap<String, String> getOkayResponse () {
 		return getOkayResponse("okay");
 	}
 
 	/**
 	 * @return a hashmap with a JSON status => okay response */
 	protected static HashMap<String, String> getOkayResponse (String msg) {
 		HashMap<String, String> resp = new HashMap<String, String>();
 		resp.put("status", "okay");
 		resp.put("message", msg);
 		return resp;
 	}
 
  	/**
  	 * renders a JSON status error response from a list of errors
  	 * @param callback optional, used for cross domain requests */
  	protected static void returnFailed (List<play.data.validation.Error> errors, String callback) {
  	    String msg = "";
         for (play.data.validation.Error err : errors) {
             msg += err.message();
         }
  	    Logger.warn(msg);
  		Users.renderJSONP(
  			getErrorResponse(msg), 
  			new TypeToken<HashMap<String, String>>() {}.getType(),
  			callback
  		);
  	}
 
 	/**
 	 * renders a JSON status error response
 	 * @param callback optional, used for cross domain requests */
 	protected static void returnFailed (String msg, String callback) {
 	    Logger.warn(msg);
 		Users.renderJSONP(
 			getErrorResponse(msg), 
 			new TypeToken<HashMap<String, String>>() {}.getType(),
 			callback
 		);
 	}
 
 	/**
 	 * renders a JSON status okay response
 	 * @param callback optional, used for cross domain requests */
 	protected static void returnOkay (String msg, String callback) {
 		Users.renderJSONP(
 			getOkayResponse(msg), 
 			new TypeToken<HashMap<String, String>>() {}.getType(),
 			callback
 		);
 	}
 
 	/**
 	 * renders a JSON status okay response
 	 * @param callback optional, used for cross domain requests */
 	protected static void returnOkay (String callback) {
 		Users.renderJSONP(
 			getOkayResponse(), 
 			new TypeToken<HashMap<String, String>>() {}.getType(),
 			callback
 		);
 	}
 
 	/**
 	 * renders a JSON response, utilizing <code>callback</code> as necessary for
 	 * cross domain requests
 	 * @param myObj 	object to be JSONified
 	 * @param t 		describes the type of the object to be JSONified
  	 * @param callback 	optional, used for cross domain requests */
 	protected static void renderJSONP (Object myObj, Type t, String callback) {
 		String json;
 		GsonBuilder gsonBuilder = new GsonBuilder().setExclusionStrategies(new User.ChatExclusionStrategy());
 		Gson gson = gsonBuilder.create();
 		if (t != null) {
 			json = gson.toJson(myObj, t); 
 		} else {
 			json = gson.toJson(myObj); 
 		}
 		if (callback != null && !callback.equals("")) {
 			json = callback + "(" + json + ")";
 			response.contentType = "application/javascript";
 			renderText(json);
 		} else {
 			renderJSON(json);
 		}
 	}
 	
 	/**
 	 * Simple exception class used by our controllers to handle invalid
 	 * arguments.  These are thrown by individual methods, and caught and handled
 	 * in this class */
 	protected static class ArgumentException extends Exception {
 		/** The parameter that is invalid */
 		private final String field;
 		/** The error, e.g. "is null", "cant be negative" */		
 		private final String mistake;
 		/** Optional JSONP callback to be used when this exception is caught */
 		private final String callback;
 		
 		public ArgumentException (String err, String field, String callback) {
 			super(err);   
 			this.mistake = err; 
 			this.field = field; 
 			this.callback = callback;
 		}
 
 		/** 
 		 * @return the message string which will be logged and displaed to the user
 		 * when this exception occurs */
 		public String toString () {
 			return "**Invalid argument** " + this.field + " " + this.mistake;
 		}
 
 		/**
 		 * @return the JSONP callback to use when returning the error for this
 		 * exception */
 		public String getCallback () {
 			return this.callback;
 		}
 	}
 	
 }
 
