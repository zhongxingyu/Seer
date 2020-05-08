 package com.ngdb.entities.reference;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 import javax.persistence.*;
 
 @Entity
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class Origin implements Comparable<Origin> {
 
    public static final Origin USA = new Origin("USA");

     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     @Column(unique = true, nullable = false)
     private String title;
 
     public Origin() {
     }
 
     public Origin(String title) {
         this.title = title;
     }
 
     public String getTitle() {
         return title;
     }
 
     @Override
     public int compareTo(Origin origin) {
         if (title == null || origin == null || origin.title == null) {
             return 0;
         }
         return title.compareToIgnoreCase(origin.title);
     }
 
     @Override
     public String toString() {
         return title;
     }
 
     @Override
     public int hashCode() {
         return title.hashCode();
     }
 
     @Override
     public boolean equals(Object o) {
        if(o == null) {
            return false;
         }
        return o instanceof Origin && title.equals(((Origin) o).getTitle());
     }
 
 }
