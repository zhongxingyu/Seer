 package com.github.t1.webresource;
 
 import java.io.Serializable;
 import java.util.*;
 
 import javax.persistence.*;
 import javax.xml.bind.annotation.*;
 
 import lombok.*;
 
 @Entity
 @WebResource
 // JAXB
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 // lombok
 @Getter
 @Setter
 @ToString
 public class Person implements Serializable {
     private static final long serialVersionUID = 1L;
 
     @XmlTransient
     private @Id
     @GeneratedValue
     Long id;
 
     @XmlTransient
     private @Column
     @Version
     int version;
 
     private @Column
     String first;
 
     private @Column
     String last;
 
     @XmlElement(name = "tag")
     @XmlElementWrapper(name = "tags")
     private @ManyToMany(fetch = FetchType.EAGER)
     List<Tag> tags;
 
     @WebSubResource
    private @ManyToOne
     Category category;
 
     /** @deprecated required by JAXB and JPA */
     @Deprecated
     Person() {}
 
     public Person(String first, String last) {
         this.first = first;
         this.last = last;
     }
 
     public List<Tag> getTags() {
         if (tags == null)
             return Collections.emptyList();
         return Collections.unmodifiableList(tags);
     }
 
     public Person tag(Tag tag) {
         if (tags == null)
             tags = new ArrayList<>();
         tags.add(tag);
         return this;
     }
 
     public boolean untag(Tag tag) {
         if (tags == null)
             return false;
         return tags.remove(tag);
     }
 
     public boolean untag(String key) {
         if (tags == null)
             return false;
         for (Iterator<Tag> iter = tags.iterator(); iter.hasNext();) {
             Tag tag = iter.next();
             if (tag.getKey().equals(key)) {
                 iter.remove();
                 return true;
             }
         }
         return false;
     }
 }
