 package net.canadensys.dataportal.vascan.model;
 
 import org.apache.commons.lang3.builder.ToStringBuilder;
 
 public class NameConceptVernacularNameModel implements NameConceptModelIF{
 
	private int id;
 	private int taxonId;
 	private String name;
 	private String status;
 	private String	language;
 
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
 	public String getLang() {
 		return language;
 	}
 	public void setLang(String lang) {
 		this.language = lang;
 	}
 	
 	@Override
 	public int getTaxonId() {
 		return taxonId;
 	}
 	@Override
 	public void setTaxonId(int taxonId) {
 		this.taxonId=taxonId;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 	@Override
 	public void setName(String name) {
 		this.name=name;
 	}
 
 	@Override
 	public String getStatus() {
 		return status;
 	}
 	@Override
 	public void setStatus(String status) {
 		this.status=status;
 	}
 	
 	@Override
 	public String toString(){
 		return new ToStringBuilder(this).append("taxonId", taxonId)
 		.append("name", name).toString();
 	}
 
 }
