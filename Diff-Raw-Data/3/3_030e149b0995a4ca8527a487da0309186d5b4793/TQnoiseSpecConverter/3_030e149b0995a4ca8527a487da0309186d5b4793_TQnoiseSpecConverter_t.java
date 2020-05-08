 /*
  * Copyright (c) Qatar Computing Research Institute, 2013.
  * Licensed under the MIT license <http://www.opensource.org/licenses/MIT>.
  */
 
 package qa.qcri.qnoise.external;
 
 import qa.qcri.qnoise.constraint.Constraint;
 import qa.qcri.qnoise.constraint.ConstraintFactory;
 import qa.qcri.qnoise.internal.GranularityType;
 import qa.qcri.qnoise.internal.NoiseSpec;
 import qa.qcri.qnoise.internal.NoiseType;
 import qa.qcri.qnoise.model.NoiseModel;
 
 import java.util.List;
 
 class TNoiseTypeConverter {
     public static NoiseType convert(TNoiseType type) {
         switch (type) {
             case Missing:
                 return NoiseType.Missing;
             case Inconsistency:
                 return NoiseType.Inconsistency;
             case Outlier:
                 return NoiseType.Outlier;
             case Error:
                 return NoiseType.Error;
             case Duplicate:
                 return NoiseType.Duplicate;
             default:
                 throw new IllegalArgumentException("Unknown noise type");
         }
     }
 }
 
 class TNoiseModelConverter {
     public static NoiseModel convert(TNoiseModel type) {
         switch (type) {
             case Random:
                 return NoiseModel.Random;
             case Histogram:
                 return NoiseModel.Histogram;
             default:
                 throw new IllegalArgumentException("Unknown noise type");
         }
     }
 }
 
 public class TQnoiseSpecConverter {
     public static NoiseSpec convert(TQnoiseSpec spec) {
         NoiseSpec result = new NoiseSpec();
         result.noiseType = TNoiseTypeConverter.convert(spec.getNoiseType());
         result.percentage = spec.getPercentage();
         result.model = TNoiseModelConverter.convert(spec.getModel());
 
         if (spec.isSetDistance()) {
             List<Double> distances = spec.getDistance();
             result.distance = new double[distances.size()];
             for (int i = 0; i < distances.size(); i ++)
                 result.distance[i] = distances.get(i);
         }
 
         if (spec.isSetConstraint()) {
             result.constraint = new Constraint[spec.getConstraintSize()];
             List<String> constraintStrings = spec.getConstraint();
             for (int i = 0; i < spec.getConstraintSize(); i ++)
                 result.constraint[i] =
                     ConstraintFactory.createConstraintFromString(constraintStrings.get(i));
         }
 
         if (spec.isSetIsOnCell() && spec.isIsOnCell()) {
             result.granularity = GranularityType.Cell;
         } else {
             result.granularity = GranularityType.Row;
         }
 
         if (spec.isSetFilteredColumns()) {
            result.filteredColumns = new String[spec.getFilteredColumns().size()];
            spec.getFilteredColumns().toArray(result.filteredColumns);
         }
 
         if (spec.isSetNumberOfSeed())
             result.numberOfSeed = spec.getNumberOfSeed();
 
         if (spec.isSetLogfile())
             result.logFile = spec.getLogfile();
 
         return result;
     }
 }
