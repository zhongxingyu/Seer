 package jpa.persistence.model;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import javax.persistence.*;
 
 /**
  * @author irodriguezm
  */
 @Entity
 public class Parent {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     @Getter
     private Long id;
 
     private String name;
 
    @OneToOne(mappedBy="parent",cascade=CascadeType.PERSIST)
     @Getter
     @Setter
     private Child child;
 
 
 }
