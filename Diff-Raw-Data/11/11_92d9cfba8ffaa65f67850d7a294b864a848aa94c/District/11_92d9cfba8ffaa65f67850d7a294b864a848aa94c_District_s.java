 package ru.yandex.hackaton.server.db.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 /**
  * @author Sergey Polovko
  */
 @Entity
 @Table(name = "districts")
 public class District extends BaseModel<Integer> {
 
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "districtsGen")
    @SequenceGenerator(name = "districtsGen", sequenceName = "sidtricts_id")
     private Integer id;
 
     @Column(nullable = false)
     private String name;
 
 
     @Override
     public Integer getId() {
         return id;
     }
 
     @Override
     public void setId(Integer id) {
         this.id = id;
     }
 }
