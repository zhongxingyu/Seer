 package com.undeadscythes.supergenes.validator;
 
 import com.undeadscythes.authenticmd.validator.*;
 import com.undeadscythes.genebase.*;
 import com.undeadscythes.genebase.gedcom.*;
 import com.undeadscythes.genebase.record.*;
 import com.undeadscythes.metaturtle.exception.*;
 
 /**
  * Validate a {@link String} by matching it with an {@link Individual}
 * {@link com.undeadscythes.metaturtle.UID} in a particular {@link GeneBase}.
  *
  * @author UndeadScythes
  */
 public class IndividualValidator implements Validator {
     private final GeneBase genebase;
     private Individual indi;
 
     /**
      * Load a particular {@link GeneBase} to validate {@link Individual}
     * {@link com.undeadscythes.metaturtle.UID}s.
      *
      * @param genebase
      */
     public IndividualValidator(final GeneBase genebase) {
         this.genebase = genebase;
     }
 
     public boolean isValid(final String response) {
         try {
             indi = (Individual)genebase.getUniqueMeta(GEDTag.INDI, response);
             return true;
         } catch (NoUniqueMetaException ex) {
             return false;
         }
     }
 
     /**
      * Grab the validated {@link Individual}.
      *
      * @return Validated {@link Individual}
      */
     public Individual getValidIndi() {
         return indi;
     }
 }
