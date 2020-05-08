 package org.einherjer.twitter.tickets.model;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 
 import lombok.EqualsAndHashCode;
 import lombok.ToString;
 
 import org.springframework.hateoas.Identifiable;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 
 @MappedSuperclass
 @ToString
 @EqualsAndHashCode
 public class AbstractEntity implements Identifiable<Long> {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private final Long id = null;
 
    //the getter could have been generated with lombok and the @JsonIgnore set on the attribute but that way the @Override of @JsonIgnore in the Attachment doesn't work
     @JsonIgnore
     @Override
     public Long getId() {
         return id;
     }
 
 }
