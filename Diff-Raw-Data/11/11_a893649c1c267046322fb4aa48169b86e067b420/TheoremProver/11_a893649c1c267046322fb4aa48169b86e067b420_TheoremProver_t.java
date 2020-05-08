 package org.tptp.atp;
 
 import java.util.*;
 
 import org.tptp.model.*;
 
 public abstract class TheoremProver {
     private static ServiceLoader<TheoremProver> loader = ServiceLoader.load(TheoremProver.class);
     
     public static final Set<TheoremProver> getProvers() {
         Set<TheoremProver> set = new HashSet<TheoremProver>();
         for(TheoremProver tp : loader) {
             set.add(tp);
         }
         return set;        
     }
     
     public static final TheoremProver findByNameAndVersion(String name, String version) {
         for ( TheoremProver p : getProvers()) {
             if ( p.getName().equals(name) && p.getVersion().equals(version)) {
                 return p;
             }
         }
         return null;
     }
     
     /**
      * Returns the name of the supported TheoremProver.
      */
     public abstract String getName();
     
     /**
      * Returns the version of this package.
      */
     public abstract String getVersion();
     
     /**
      * Returns a helpful description for the supported TheoremProver
      */
     public abstract String getDescription();
     
     
     /**
      * Return true if this prover can really proof or false if this prover only tries a proof of the contrary.
     * If this is not a real theorem prover, no result will be saved in the backend.
      */
     public abstract boolean isProver();
     
     /**
      * Execute the this TheoremProver to search for a proof of the given goal {@link Formula} using the given Formulas as input.
      * The Formula objects are in the ATPPortal standard formula format. Each TheoremProver has to convert the input formulas
      * as well as all resulting formulas.
      */
     public abstract Result execute(Properties properties, String goal, Set<Formula> inputFormulas)
         throws AtpException;
     
     /**
      * Returns a {@link java.util.Map} containing a list of keys which must be filled into a {@link Properties}object
      * when calling the <code>execute()</code> method.
      */
     public Map<String,String> getOptionDescriptionMap() {
         return Collections.EMPTY_MAP;
     }
     
     /**
      * Like {@link #getOptionDescriptionMap()}, this returns a map of configuration keys mapped to a description text. The difference
      * is, that these are optional and must ne be set.
      */
     public Map<String,String> getOptionalOptionDescriptionMap() {
         return Collections.EMPTY_MAP;
     }
 }
