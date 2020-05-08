 package com.bouncingdata.plfdemo.datastore.pojo.model;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
import javax.jdo.annotations.Element;
 import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Index;
 import javax.jdo.annotations.Join;
 import javax.jdo.annotations.NullValue;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 import javax.jdo.annotations.Unique;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 @PersistenceCapable
 public class Dataset {
   @PrimaryKey
   @Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
   private int id;
  @Persistent(table="Dataset_tags")
	@Join (column="id_OID")	
	@Element (column="id_EID")

private  Set<Tag> tags;
  
  
  
   private String name;
   private String description;  
  @Index
   @Unique
   private String guid;
   @Persistent(defaultFetchGroup="true", nullValue=NullValue.EXCEPTION)
   private User user;
   
   private String schema;
   private Date createAt;
   private Date lastUpdate;
   private int rowCount;
   private boolean isActive;
   @Persistent(defaultFetchGroup="true")
   private Scraper scraper;
   private boolean isPublic;
   @Persistent(mappedBy="dataset")
   List<AnalysisDataset> relations;
	
   
   public int getId() {
     return id;
   }
   public void setId(int id) {
     this.id = id;
   }
   public String getName() {
     return name;
   }
   public void setName(String name) {
     this.name = name;
   }
   public String getDescription() {
     return description;
   }
   public void setDescription(String description) {
     this.description = description;
   }
   public String getSchema() {
     return schema;
   }
   public void setSchema(String schema) {
     this.schema = schema;
   }
   public String getGuid() {
     return guid;
   }
   public void setGuid(String guid) {
     this.guid = guid;
   }
   /*public int getAuthor() {
     return author;
   }
   public void setAuthor(int author) {
     this.author = author;
   }*/
   public  Set<Tag>  getTags() {
     return tags;
   }
   public void setTags( Set<Tag>  tags) {
     this.tags = tags;
   }
   public Date getCreateAt() {
     return createAt;
   }
   public void setCreateAt(Date createAt) {
     this.createAt = createAt;
   }
   public Date getLastUpdate() {
     return lastUpdate;
   }
   public void setLastUpdate(Date lastUpdate) {
     this.lastUpdate = lastUpdate;
   }
   public int getRowCount() {
     return rowCount;
   }
   public void setRowCount(int rowCount) {
     this.rowCount = rowCount;
   }
   public User getUser() {
     return user;
   }
   public void setUser(User user) {
     this.user = user;
   }
 /*  public Analysis getAnalysis() {
     return analysis;
   }
   public void setAnalysis(Analysis analysis) {
     this.analysis = analysis;
   }
 */  public boolean isActive() {
     return isActive;
   }
   public void setActive(boolean isActive) {
     this.isActive = isActive;
   }
   @JsonIgnore
   public Scraper getScraper() {
     return scraper;
   }
   public void setScraper(Scraper scraper) {
     this.scraper = scraper;
   }
   public boolean isPublic() {
     return isPublic;
   }
   public void setPublic(boolean isPublic) {
     this.isPublic = isPublic;
   }
     
 }
