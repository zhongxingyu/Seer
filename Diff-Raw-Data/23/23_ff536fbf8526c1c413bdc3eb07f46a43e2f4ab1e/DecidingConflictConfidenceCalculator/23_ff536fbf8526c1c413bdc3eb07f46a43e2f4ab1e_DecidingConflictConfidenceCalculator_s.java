 package cz.cuni.mff.odcleanstore.conflictresolution.confidence.impl;
 
 import java.util.Collection;
 
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.Value;
 
 import cz.cuni.mff.odcleanstore.conflictresolution.CRContext;
 import cz.cuni.mff.odcleanstore.conflictresolution.DistanceMeasure;
 import cz.cuni.mff.odcleanstore.conflictresolution.confidence.DecidingConfidenceCalculator;
 import cz.cuni.mff.odcleanstore.conflictresolution.confidence.SourceConfidenceCalculator;
 
 /**
  * @author Jan Michelfeit
  */
 public class DecidingConflictConfidenceCalculator extends ConflictConfidenceCalculator implements DecidingConfidenceCalculator {
     public static final double DEFAULT_AGREE_COEFICIENT = 4;
     protected final double agreeCoeficient;
     
     public DecidingConflictConfidenceCalculator(SourceConfidenceCalculator sourceConfidenceCalculator, double agreeCoeficient,  DistanceMeasure distanceMeasure) {
         super(sourceConfidenceCalculator, distanceMeasure);
         this.agreeCoeficient = agreeCoeficient;
     }
     
     public DecidingConflictConfidenceCalculator(SourceConfidenceCalculator sourceConfidenceCalculator, double agreeCoeficient) {
         super(sourceConfidenceCalculator);
         this.agreeCoeficient = agreeCoeficient;
     }
     
     public DecidingConflictConfidenceCalculator(SourceConfidenceCalculator sourceConfidenceCalculator) {
         super(sourceConfidenceCalculator);
         this.agreeCoeficient = DEFAULT_AGREE_COEFICIENT;
     }
     
     public DecidingConflictConfidenceCalculator() {
         super();
         this.agreeCoeficient = DEFAULT_AGREE_COEFICIENT;
     }
     
     @Override
     protected double valueConfidence(Value value, Collection<Resource> sources, Model metadata) {
         double max = 0;
         for (Resource source : sources) {
             double sourceConfidence = sourceConfidence(source, metadata);
             if (sourceConfidence > max) {
                 max = sourceConfidence;
             }
         }
         return max;
     }
     
     @Override
     public double getConfidence(Value value, Collection<Statement> conflictingStatements, Collection<Resource> sources, CRContext context) {
         double confidence = super.getConfidence(value, conflictingStatements, sources, context);
 
          // Increase score if multiple sources agree on the result value
          if (sources.size() > 1) {
              Model metadata = context.getMetadata();
              double sourcesConfidenceSum = 0;
             for (Resource source :sources) {
                 sourcesConfidenceSum += sourceConfidence(source, metadata);
              }
             double agreeCoef = (sourcesConfidenceSum - confidence) / agreeCoeficient;
             if (agreeCoef > 1) {
                 agreeCoef = 1;
             } else if (agreeCoef < 0) {
                 agreeCoef = 0;
              }
             confidence += (1 - confidence) * agreeCoef;
          }
 
          return confidence;
     }
 }
