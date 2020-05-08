 package com.ngdb.entities.article.element;
 
 import java.io.Serializable;
 import java.util.*;
 
 import javax.persistence.*;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Embeddable
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class Tags implements Iterable<Tag> {
 
     @OrderBy("name")
     @OneToMany(mappedBy = "article")
     private Set<Tag> tags;
 
     public Set<Tag> all() {
         return Collections.unmodifiableSet(tags);
     }
 
     @Override
     public Iterator<Tag> iterator() {
         return tags.iterator();
     }
 
     public boolean contains(String tagName) {
         for (Tag tag : tags) {
             if (tag.hasName(tagName)) {
                 return true;
             }
         }
         return false;
     }
 
     public void add(Tag tag) {
         tags.add(tag);
     }
 
     public boolean contains(Tag tag) {
         return tags.contains(tag);
     }
 
     public int count() {
         return tags.size();
     }

    @Override
    public String toString() {
        if(tags == null) {
            return "";
        }
        return Arrays.toString(tags.toArray());
    }
 }
