 package uk.ac.ebi.mdk.domain.annotation;
 
 import uk.ac.ebi.mdk.lang.annotation.Brief;
 import uk.ac.ebi.mdk.lang.annotation.Context;
 import uk.ac.ebi.mdk.lang.annotation.Description;
 
 /**
  * @author John May
  */
 @Context
 @Brief("Systematic Name")
 @Description("A systematic name for an entity")
 public class SystematicName extends Synonym {
 
     public SystematicName() {
     }
 
     public SystematicName(String name) {
         super(name);
     }
 
     @Override
     public SystematicName newInstance() {
        return newInstance();
     }
 
     @Override
     public SystematicName getInstance(String name) {
         return new SystematicName(name);
     }
 }
