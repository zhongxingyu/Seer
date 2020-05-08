 package gov.nih.nci.iso21090;
 
 import java.util.ArrayList;
 import java.util.List;
import java.util.Set;
 
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.collections.list.PredicatedList;
 
 /**
  * Represents the En ISO datatype.
  * @author lpower
  */
 public class En extends Any implements Cloneable {
 
     private static final long serialVersionUID = 2L;
 
     private transient List<Enxp> part;
     private final List<Enxp> partsInternal;
     private Predicate partRestriction;
    private Set<EntityNameUse> use;

    /**
     * @return the use
     */
    public Set<EntityNameUse> getUse() {
        return use;
    }

    /**
     * @param use the use to set
     */
    public void setUse(Set<EntityNameUse> use) {
        this.use = use;
    }

     /**
      * Default ctor.
      */
     public En() {
         partsInternal = new ArrayList<Enxp>();
     }
 
     /**
      * @param partRestriction a filter for disallowed values
      */
     protected En(Predicate partRestriction) {
         this();
         this.partRestriction = partRestriction;
     }
 
     /**
      * @return the part
      */
     @SuppressWarnings("unchecked")
     public List<Enxp> getPart() {
         if (part == null) {
             if (partRestriction != null) {
                 part = PredicatedList.decorate(partsInternal, partRestriction);
             } else {
                 part = partsInternal;
             }
         }
         return part;
     }
 
     /**
     * @param partInstance ENXP instance to be added to part collection
      */
     public void addPart(Enxp partInstance) {
         if (partInstance != null) {
             getPart().add(partInstance);
         }
     }

     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
 
         if (this == obj) {
             return true;
         }
 
         if (!(obj instanceof En)) {
             return false;
         }
 
         En x = (En) obj;
 
         return super.equals(obj) && this.getPart().equals(x.getPart());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         if (!this.getPart().isEmpty()) {
             return this.getPart().hashCode();
         }
         return super.hashCode();
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("PMD.CloneThrowsCloneNotSupportedException")
     @Override
     public En clone() {
 
         En returnVal = (En) super.clone();
 
         try {
             for (Enxp tem : this.getPart()) {
                 returnVal.getPart().add(tem.clone());
             }
         } catch (Exception e) {
             throw new IsoCloneException(e);
         }
 
         return returnVal;
     }
 }
