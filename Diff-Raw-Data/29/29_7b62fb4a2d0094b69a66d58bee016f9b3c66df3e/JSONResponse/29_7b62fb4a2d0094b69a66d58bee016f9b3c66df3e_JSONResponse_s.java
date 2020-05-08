 package com.genfersco.sepbas.web.json;
 
 import java.io.Serializable;
 
 public interface JSONResponse extends Serializable {
	Object getResultList();
 	String getMessage();
 	String getStatus();
 }
