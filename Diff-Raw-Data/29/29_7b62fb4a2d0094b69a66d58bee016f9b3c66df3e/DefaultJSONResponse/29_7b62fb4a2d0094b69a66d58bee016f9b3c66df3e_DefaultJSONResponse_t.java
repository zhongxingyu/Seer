 package com.genfersco.sepbas.web.json;
 
 public class DefaultJSONResponse implements JSONResponse {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1632718909745386603L;
 
 	private String message;
 	private String status;
 
 	public DefaultJSONResponse(String status, String message) {
 		this.message = message;
 		this.status = status;
 	}
 
 	public DefaultJSONResponse() {
 
 	}
 
 	@Override
	public Object getContent() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getMessage() {
 		return message;
 	}
 
 	@Override
 	public String getStatus() {
 		return status;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 }
