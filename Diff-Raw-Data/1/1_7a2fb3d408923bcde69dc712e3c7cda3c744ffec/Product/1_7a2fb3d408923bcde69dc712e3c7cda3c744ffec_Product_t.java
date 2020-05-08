 package wad.tukki.models;
 
 import javax.validation.constraints.Size;
 import org.jsoup.Jsoup;
 import org.jsoup.safety.Whitelist;
 import org.springframework.data.annotation.Id;
 import org.springframework.data.mongodb.core.mapping.Document;
 
 @Document
 public class Product {
 
     @Id
     private String id;
     
     @Size(min = 1)
     private String name;
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
     
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = Jsoup.clean(name, Whitelist.none());
     }
 
     @Override
     public boolean equals(Object obj) {
         
         if (obj == null) {
             return false;
         }
         
         if (getClass() != obj.getClass()) {
             return false;
         }
         
         final Product other = (Product) obj;
         if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
             return false;
         }
         
         return true;
     }
 
     @Override
     public int hashCode() {
         
         int hash = 7;
         hash = 23 * hash + (this.id != null ? this.id.hashCode() : 0);
         return hash;
     }
 }
