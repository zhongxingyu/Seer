 package entities;
 
 import java.util.logging.Logger;
 import java.io.Serializable;
 import java.util.Set;
 import java.util.LinkedHashSet;
 import java.util.Date;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 // hibernate-specific option
 // import org.hibernate.annotations.LazyCollection;
 // import org.hibernate.annotations.LazyCollectionOption;
 
 @Entity
 @Table(name = "A")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "A.findAll"    , query = "SELECT x FROM A x"),
     @NamedQuery(name = "A.findById", query = "SELECT x FROM A x WHERE x.id = :MODEL")
         }) 
 public class A implements Serializable {
     
     private static final long serialVersionUID = 1L;
 
     private static final Logger l = Logger.getLogger(A.class.getName());
 
     @Id
     @Basic(optional = false)
     @GeneratedValue(strategy=GenerationType.IDENTITY)
     @Column(name = "ID")
     private Integer id;
 
     public Integer getId() { return id; }
 
     @Basic(optional = false)
     @NotNull
     @Column(name = "A1")
     private String a1;
 
 
     // @LazyCollection(LazyCollectionOption.FALSE) // I don't use that
    @OneToMany(cascade = {CascadeType.PERSIST}, mappedBy = "aId", fetch=FetchType.EAGER, orphanRemoval=true) // line-46 if you change
     // the cascade type to ALL or PERSIST it no longer works. It only works with REMOVE (however, now the other deletion method
     //    fails instead). The culprit is the CascadeType.PERSIST in that
     // it resurrects the B entity during EntityManager::commit, unless you also merge A. Note that the EntityManager is calling
     // persist on the Managed Entities, not the detached entities, so although you may have removed B from A's collection
     // it's not removed from the entity the EntityManager manages, unless you also merge A, in which case everything's solved.
     // See:
     // [1]  "Apress.Pro.JPA-2 Master the Java Persistence API" book, pg. 157 (synchronization with the Database)
     //      - and -
     // [2]  http://stackoverflow.com/questions/13145045/jpa-hibernate-removing-child-entities
     //
     private Set<B> bCollection = new LinkedHashSet<B>();
 
     public Set<B> getBCollection() {
         l.info("returning B collection");
         return bCollection;
     }
     public void setBCollection(Set<B> bCollection) {
         l.info("A#setBCollection( size: "+bCollection.size()+" )");
         this.bCollection = bCollection;
     }
 
     public A() {
         l.info("A()");
     }
 
     public A(Integer id, String a1) {
         l.info("A("+id+","+a1+")");
         this.id = id;
         this.a1 = a1;
     }
 
     public String getA1() {
         return a1;
     }
 
     public void setA1(String a1) {
         this.a1 = a1;
     }
 
     public void removeB(B b) {
         b.setAId(null);
     }
     public void addB(B b) {
         b.setAId(this);
     }
     void internalRemoveB(B b) {
         this.bCollection.remove(b);
     }
     void internalAddB(B b) {
         this.bCollection.add(b);
     }
 
 
 
     @Override
     public boolean equals(Object object) {
         if (!(object instanceof A)) {
             return false;
         }
         A other = (A) object;
         if ((this.id == null && other.id != null) || 
             (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         if ((this.a1 == null && other.a1 != null) || 
             (this.a1 != null && !this.a1.equals(other.a1))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return A.class.getName()+"[ id = " + id + ", a1 = "+a1+" * "+getBCollection().size()+" ]";
     }
 
     
 }
