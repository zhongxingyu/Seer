 package burrito.test.crud;
 
 import java.util.Date;
 
 import siena.core.lifecycle.PreInsert;
 import siena.core.lifecycle.PreSave;
 import siena.core.lifecycle.PreUpdate;
 
 import burrito.BurritoModel;
 import burrito.annotations.Displayable;
 import burrito.annotations.SearchableField;
 import burrito.annotations.SearchableMethod;
 
 public class SearchTestEntity extends BurritoModel {
 	
 	@SearchableField
 	private String name;
 
 	private String notSearchField;
 	
 	@Displayable
 	private Date date;
 	
 	@Displayable
 	private String displayableField;
 
 	@Displayable
 	private Long number;
 	
	@Displayable
 	private Date lastModified;
 	
 	
 	
 	
 	public Long getNumber() {
 		return number;
 	}
 
 	public void setNumber(Long number) {
 		this.number = number;
 	}
 
 	public String getDisplayableField() {
 		return displayableField;
 	}
 
 	public void setDisplayableField(String displayableField) {
 		this.displayableField = displayableField;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public void setNotSearchField(String notSearchField) {
 		this.notSearchField = notSearchField;
 	}
 
 	public String getNotSearchField() {
 		return notSearchField;
 	}
 	
 	@SearchableMethod
 	public String searchableFunction() {
 		return "searchable function " + name;
 	}
 	
 	@Displayable
 	public String displayableFunction() {
 		return "unicorn";
 	}
 	
 	@Override
 	public String toString() {
 		return name;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 	
 	@PreSave
 	@PreInsert
 	@PreUpdate
 	public void updateLastModifiedDate() {
 		lastModified = new Date();
 	}
 	
 	public Date getLastModified() {
 		return lastModified;
 	}
 	
 }
