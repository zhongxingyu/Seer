 package ro.finsiel.eunis.jrfTables.species.references;
 
 
 import net.sf.jrf.domain.PersistentObject;
 
 
 public class SpeciesBooksPersist extends PersistentObject {
     private Integer id = null;
     private Integer idLink = null;
     private String name = null;
     private String editor = null;
    private java.util.Date date = null;
     private String title = null;
     private String publisher = null;
     private String url = null;
 
     public SpeciesBooksPersist() {
         super();
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getEditor() {
         return editor;
     }
 
     public void setEditor(String editor) {
         this.editor = editor;
     }
 
    public java.util.Date getDate() {
         return date;
     }
 
    public void setDate(java.util.Date date) {
         this.date = date;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getPublisher() {
         return publisher;
     }
 
     public void setPublisher(String publisher) {
         this.publisher = publisher;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public Integer getIdLink() {
         return idLink;
     }
 
     public void setIdLink(Integer idLink) {
         this.idLink = idLink;
     }
 }
