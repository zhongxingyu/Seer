 package no.henning.restful.http.status;
 
 public class HttpRestResponse {

	// constants
	private final static int badStatusCode = 666;
	private final static String badStatusReason = "Server isn't available";
	private final static String badStatusResponse = "Could not connect to the server";

 	private final int statusCode;
 	private final String statusReason;
 	private final String response;
 
 	public HttpRestResponse(int statusCode, String statusReason, String response) {
 		this.statusCode = statusCode;
 		this.statusReason = statusReason;
 		this.response = response;
 	}
 
 	public static HttpRestResponse newBadConnectionResponse() {
		return new HttpRestResponse(badStatusCode, badStatusReason, badStatusResponse);
 	}
 
 	public int getStatusCode() {
 		return statusCode;
 	}
 
 	public String getStatusReason() {
 		return statusReason;
 	}
 
 	public String getResponse() {
 		return response;
 	}
 }
