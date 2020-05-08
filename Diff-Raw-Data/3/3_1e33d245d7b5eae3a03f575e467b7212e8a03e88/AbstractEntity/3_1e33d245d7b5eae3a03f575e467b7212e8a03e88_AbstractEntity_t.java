 package progfun.spring.data.domain;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Version;
 
 /**
  * Base class to derive entity classes from.
  */
 @MappedSuperclass
 public class AbstractEntity {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
    @Version
     private int version;
 
     /*
      * (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (this.id == null || obj == null || !(this.getClass().equals(obj.getClass()))) {
             return false;
         }
         AbstractEntity that = (AbstractEntity) obj;
         return this.id.equals(that.getId());
     }
 
     /*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
     @Override
     public int hashCode() {
         return id == null ? 0 : id.hashCode();
     }
 
     /**
      * Returns the identifier of the entity.
      *
      * @return the id
      */
     public Long getId() {
         return id;
     }
 
     /*
      * (non-Javadoc)
      * The version is used to ensure integrity when performing the merge
      * operation and for optimistic concurrency control.
      *
      * @see http://docs.oracle.com/javaee/6/tutorial/doc/gkjhz.html
      */
     protected int getVersion() {
         return version;
     }
 
     protected void setVersion(int version) {
         this.version = version;
     }
 }
