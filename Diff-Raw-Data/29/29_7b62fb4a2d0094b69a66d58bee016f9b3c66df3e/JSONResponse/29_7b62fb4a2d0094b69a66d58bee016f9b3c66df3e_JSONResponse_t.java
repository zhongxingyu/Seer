 package com.genfersco.sepbas.web.json;
 
 import java.io.Serializable;
 
 public interface JSONResponse extends Serializable {
	Object getContent();
 	String getMessage();
 	String getStatus();
 }
