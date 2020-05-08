 package ohtu.refero.models;
 
 import javax.persistence.MappedSuperclass;
 import javax.validation.constraints.NotNull;
 import ohtu.refero.bibtex.BibTeXProperty;
 import org.hibernate.validator.constraints.NotBlank;
 
 @MappedSuperclass
 public abstract class Reference extends JPAObject {
 
     @NotBlank(message = "Author can't be empty.")
     private String author;
     
     @NotBlank(message = "Title can't be empty.")
     private String title;
 
     @BibTeXProperty(name = "year")
     @NotNull(message = "Year can't be empty.")
     private Integer releaseYear; 
     private String publisher;
     private String address;
     
//    private String referenceID;

//    public String getReferenceID() {
//        return referenceID;
//    }
//
//    public void setReferenceID(String referenceID) {
//        this.referenceID = referenceID;
//    }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
     
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public Integer getReleaseYear() {
         return releaseYear;
     }
 
     public void setReleaseYear(Integer releaseYear) {
         this.releaseYear = releaseYear;
     }
 
     public String getPublisher() {
         return publisher;
     }
 
     public void setPublisher(String publisher) {
         this.publisher = publisher;
     }
     
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
 
         if (getClass() != obj.getClass()) {
             return false;
         }
 
         Reference other = (Reference) obj;
         if (this.getId() == null || other.getId() == null || this.getId() != other.getId()) {
             return false;
         }
 
         return true;
     }
 }
