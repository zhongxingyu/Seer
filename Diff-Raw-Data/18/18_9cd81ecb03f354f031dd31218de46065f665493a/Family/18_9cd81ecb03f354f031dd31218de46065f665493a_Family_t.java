 package com.undeadscythes.genebase.record;
 
 import com.undeadscythes.gedform.*;
 import com.undeadscythes.genebase.gedcom.*;
import com.undeadscythes.genebase.holder.*;
 import com.undeadscythes.genebase.specific.*;
import com.undeadscythes.metaturtle.unique.*;
 import java.util.*;
 
 /**
  * A {@link Family} is a collection of individuals related by parentage.
  *
  * @author UndeadScythes
  */
 public class Family extends UniqueHolder {
     private static final long serialVersionUID = 1L;
 
    private final Map<Individual, Relation> members = new HashMap<Individual, Relation>(2);
 
     /**
      * Load this {@link Family} with the data contained in the given
      * {@link Cluster}.
      */
     public Family(final Cluster record) {
        super(RecordType.FAM, record);
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
        for (Individual indi : members.keySet()) {
            if (indi.getUID().equals(uid)) return true;
        }
        return false;
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
