 package oshop.model;
 
 import com.fasterxml.jackson.annotation.JsonInclude;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @Entity
 @Table(name = "itemCategory")
 @XmlRootElement
 @JsonInclude(JsonInclude.Include.NON_NULL)
 public class ItemCategory extends BaseEntity<Integer> {
 
     @NotNull
    @Size(min = 1)
     @Column(name = "name")
     private String name;
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 }
