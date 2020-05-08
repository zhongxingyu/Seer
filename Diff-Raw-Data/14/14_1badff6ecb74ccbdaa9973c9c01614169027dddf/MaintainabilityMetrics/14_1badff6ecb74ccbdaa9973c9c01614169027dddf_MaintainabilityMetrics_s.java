 package mx.itesm.mexadl.metrics;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Target;
 
 /**
  * Groups quality metrics that will be used to verify the maintainability
  * quality characteristic of a type defined in an architecture definition.
  * 
  * @author jccastrejon
  * 
  */
 @Target(value = ElementType.TYPE)
 public @interface MaintainabilityMetrics {
    ModularityMetrics modularityMetrics();
 
    ReusabilityMetrics reusabilityMetrics();
 
    AnalyzabilityMetrics analyzabilityMetrics();
 
    ModifiabilityMetrics modifiabilityMetrics();
 
    TestabilityMetrics testabilityMetrics();
 
     /**
      * Metrics associated to the Modularity sub-characteristic of the
      * Maintainability quality characteristic according to the ISO/IEC SQuaRE
      * quality model.
      * 
      * @author jccastrejon
      * 
      */
     @interface ModularityMetrics {
 
         /**
          * Maximum inheritance path from the class to the root class.
          * 
          * @return
          */
         int depthInInheritanceTree() default -1;
 
         /**
          * Number of immediate sub-classes of a class.
          * 
          * @return
          */
         int numberOfChildren() default -1;
     }
 
     /**
      * Metrics associated to the Reusability sub-characteristic of the
      * Maintainability quality characteristic according to the ISO/IEC SQuaRE
      * quality model.
      * 
      * @author jccastrejon
      * 
      */
     @interface ReusabilityMetrics {
 
         /**
          * The number of not connected method pairs in a class representing
          * independent parts having no cohesion.
          * 
          * @return
          */
         int lackOfCohesionOfMethods() default -1;
 
         /**
          * Total number of external classes coupled to classes of a package due
          * to incoming coupling.
          * 
          * @return
          */
         int afferentCoupling() default -1;
     }
 
     /**
      * Metrics associated to the Analyzability sub-characteristic of the
      * Maintainability quality characteristic according to the ISO/IEC SQuaRE
      * quality model.
      * 
      * @author jccastrejon
      * 
      */
     @interface AnalyzabilityMetrics {
 
         /**
          * The count of the executable lines of code.
          * 
          * @return
          */
         int linesOfCode() default -1;
 
         /**
          * The amount of decision logic in a single software module.
          * 
          * @return
          */
         int cyclomaticComplexityPerUnit() default -1;
 
         /**
          * Count of (public) methods in a class and methods directly called by
          * these.
          * 
          * @return
          */
         int responseForClass() default -1;
 
         /**
          * The sum of the complexities of the methods defined in a class.
          * 
          * @return
          */
         int weightedMethodComplexity() default -1;
 
         /**
          * Number of public methods contained in a type.
          * 
          * @return
          */
         int numberOfPublicMethods() default -1;
     }
 
     /**
      * Metrics associated to the Modifiability sub-characteristic of the
      * Maintainability quality characteristic according to the ISO/IEC SQuaRE
      * quality model.
      * 
      * @author jccastrejon
      * 
      */
     @interface ModifiabilityMetrics {
         /**
          * The number of duplicated blocks found in source code.
          * 
          * @return
          */
         int duplicatedBlocks() default -1;
 
         /**
          * The number of other classes that a class is coupled to.
          * 
          * @return
          */
         int couplingBetweenObjects() default -1;
     }
 
     /**
      * Metrics associated to the Testability sub-characteristic of the
      * Maintainability quality characteristic according to the ISO/IEC SQuaRE
      * quality model.
      * 
      * @author jccastrejon
      * 
      */
     @interface TestabilityMetrics {
 
         /**
          * How complete is the built-in test capability.
          * 
          * @return
          */
         int completenessOfBuiltInFunction() default -1;
 
         /**
          * How independently can the software be tested?
          * 
          * @return
          */
         int autonomyOfTestability() default -1;
 
         /**
          * Unit test coverage.
          * 
          * @return
          */
         int unitTestCoverage() default -1;
     }
 }
