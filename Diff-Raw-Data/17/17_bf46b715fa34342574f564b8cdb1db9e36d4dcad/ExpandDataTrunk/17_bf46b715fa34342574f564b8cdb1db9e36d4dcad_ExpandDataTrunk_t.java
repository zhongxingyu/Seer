 package org.jboss.pressgang.ccms.rest.v1.expansion;
 
 import java.util.List;
 
 /**
  * This class defines the information required to expand a collection of
  * entities, as well as the information required to expand the child collections.
  */
 public class ExpandDataTrunk {
     /**
      * A representation of the main collection
      */
     private ExpandDataDetails trunk;
     /**
      * A collection of expansion collections held by the trunk
      */
     private List<ExpandDataTrunk> branches;
 
     public ExpandDataDetails getTrunk() {
         return trunk;
     }
 
     public void setTrunk(final ExpandDataDetails trunk) {
         this.trunk = trunk;
     }
 
     public List<ExpandDataTrunk> getBranches() {
         return branches;
     }
 
     public void setBranches(final List<ExpandDataTrunk> branches) {
         this.branches = branches;
     }
 
     public ExpandDataTrunk get(final String name) {
        if (branches != null) {
            for (final ExpandDataTrunk branch : branches) {
                if (branch.getTrunk() != null && branch.getTrunk().getName() != null && branch.getTrunk().getName().equals(name))
                    return branch;
            }
        }
 
         return null;
     }
 
     public boolean contains(final String name) {
        if (branches != null) {
            for (final ExpandDataTrunk branch : branches) {
                if (branch.getTrunk() != null && branch.getTrunk().getName() != null && branch.getTrunk().getName().equals(name))
                    return true;
            }
        }
 
         return false;
     }
 
     public ExpandDataTrunk() {
 
     }
 
     public ExpandDataTrunk(final ExpandDataDetails trunk) {
         this.trunk = trunk;
     }
 
     public ExpandDataTrunk(final ExpandDataDetails trunk, final List<ExpandDataTrunk> branches) {
         this.trunk = trunk;
         this.branches = branches;
     }
 }
