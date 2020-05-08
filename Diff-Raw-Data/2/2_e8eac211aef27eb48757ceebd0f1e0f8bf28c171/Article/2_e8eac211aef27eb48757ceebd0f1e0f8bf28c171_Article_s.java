 package ohtu.refero.models;
 
 import javax.persistence.Entity;
 import javax.persistence.OneToOne;
 import javax.validation.constraints.*;
 import org.hibernate.validator.constraints.*;
 
 @Entity
 public class Article extends JPAObject {
 
     @NotBlank(message="Author can't be empty.")
    private long author;
     @NotBlank(message="Title can't be empty.")
     private String title;
     @NotBlank(message="Journal can't be empty.")
     private String journal;
     private String publisher;
     private String address;
     //@Min(0, message = "numbers plz")
     @NotNull(message="Volume can't be empty.")
     private Integer volume; 
     private Integer number;
     @NotNull(message="Year can't be empty.")
     private Integer releaseYear;
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public String getJournal() {
         return journal;
     }
 
     public void setJournal(String journal) {
         this.journal = journal;
     }
 
     public Integer getNumber() {
         return number;
     }
 
     public void setNumber(Integer number) {
         this.number = number;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public Integer getVolume() {
         return volume;
     }
 
     public void setVolume(Integer volume) {
         this.volume = volume;
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
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
 
         if (getClass() != obj.getClass()) {
             return false;
         }
 
         Article other = (Article) obj;
         if (this.getId() == null || other.getId() == null || this.getId() != other.getId()) {
             return false;
         }
 
         return true;
     }
 }
