 package pl.kikko.jpa.entity;
 
 import pl.kikko.patterns.builder.AbstractBuildableBuilder;
 import pl.kikko.patterns.builder.Buildable;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 
 @MappedSuperclass
 public abstract class BaseEntity implements Buildable {
 
     @Id
     @GeneratedValue
     protected Long id;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public static abstract class BaseEntityBuilder<TYPE extends BaseEntity, BUILDER extends BaseEntityBuilder<TYPE, BUILDER>> extends AbstractBuildableBuilder<TYPE, BUILDER> {
        public BUILDER id(Long id) {
             buildable.setId(id);
             return builder;
         }
     }
 
 }
