 package edu.mayo.cts2.framework.plugin.service.umls.mapper;
 
 public class CodeDTO {
 	
 	private String ui;
 
 	private String id;
 
 	private String abbreviation;
 	
 	private String name;
 	
 	private String language;
 	
	private String isPreferred;
 	
	public String isPreferred() {
 		return isPreferred;
 	}
 
	public void setPreferred(String isPreferred) {
 		this.isPreferred = isPreferred;
 	}
 
 	public String getLanguage() {
 		return language;
 	}
 
 	public void setLanguage(String language) {
 		this.language = language;
 	}
 
 	public String getSab() {
 		return abbreviation;
 	}
 
 	public void setSab(String sab) {
 		this.abbreviation = sab;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 
 	public String getUi() {
 		return ui;
 	}
 
 	public void setUi(String ui) {
 		this.ui = ui;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String namep) {
 		this.name = namep;
 	}
 }
