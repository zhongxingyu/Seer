 package com.undeadscythes.genebase.record;
 
 import com.undeadscythes.gedform.*;
 import com.undeadscythes.genebase.gedcom.*;
import com.undeadscythes.genebase.meta.*;
 import com.undeadscythes.genebase.specific.*;
import com.undeadscythes.metaturtle.*;
 import java.util.*;
 
 /**
  * A {@link Family} is a collection of individuals related by parentage.
  *
  * @author UndeadScythes
  */
 public class Family extends UniqueHolder {
     private static final long serialVersionUID = 1L;
 
    private final Map<String, Individual> individuals;
 
     /**
      * Load this {@link Family} with the data contained in the given
      * {@link Cluster}.
      */
     public Family(final Cluster record) {
        super(GEDTag.FAM, record);
        individuals = new HashMap<String, Individual>(0);
     }
 
     /**
      * @see #hasMember(UID)
      */
     public boolean hasMember(final String uid) {
         return hasMember(new UID(uid));
     }
 
     /**
      * Check if this {@link Family} contains an {@link Individual} with a given
      * {@link UID}.
      */
     public boolean hasMember(final UID uid) {
        return individuals.containsKey(uid.toString());
     }
 
     /**
      * Get a user friendly printout of this {@link Family}.
      */
     public String print() {
         return ""; //TODO: Implement me
     }
 
     /**
      * Get the {@link Relation} the {@link Individual} with the given
      * {@link UID} has to this {@link Family}.
      */
     public Relation getRelation(final UID uid) {
         return Relation.NONE; //TODO: Implement me
     }
 }
