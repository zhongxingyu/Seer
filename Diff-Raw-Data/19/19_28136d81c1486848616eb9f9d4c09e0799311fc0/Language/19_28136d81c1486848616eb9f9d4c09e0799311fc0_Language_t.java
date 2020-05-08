 package com.voltvoodoo.saplo4j.model;
 
 import java.io.Serializable;
 
 import org.json.simple.JSONAware;
 
 /**
  * Immutable, serializable language class. To add
  * new languages to saplo, add them as instances in
  * this class.
  * 
  * @author Jacob Hansson <jacob@voltvoodoo.com>
  *
  */
 public class Language implements Serializable, JSONAware {
 
 	public static Language ENGLISH = new Language("en");
	public static Language SWEDISH = new Language("sv");
 	
 	/**
 	 * Actual language code used by SAPLO
 	 */
 	protected String code;
 	
 	/**
 	 * Serial No
 	 */
 	private static final long serialVersionUID = -2442762969929206780L;
 	
 	public Language(String code) {
 		this.code = code;
 	}
 	
 	//
 	// PUBLIC
 	//
 	
 	public String toString() { 
 		return this.code;
 	}
 
 	public String toJSONString() {
 		return toString();
 	}
 	
 	public boolean equals(Language other) {
 		return other.toString().equals(this.toString());
 	}
 	
 }
