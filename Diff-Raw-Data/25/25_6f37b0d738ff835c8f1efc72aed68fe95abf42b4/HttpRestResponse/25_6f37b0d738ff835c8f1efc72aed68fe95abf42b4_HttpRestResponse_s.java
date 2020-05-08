 package no.henning.restful.http.status;
 
 public class HttpRestResponse {
 	private final int statusCode;
 	private final String statusReason;
 	private final String response;
 
 	public HttpRestResponse(int statusCode, String statusReason, String response) {
 		this.statusCode = statusCode;
 		this.statusReason = statusReason;
 		this.response = response;
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
