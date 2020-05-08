 package pl.gajowy.kernelEstimator;
 
 import com.google.common.base.Preconditions;
 
 import java.util.EnumMap;
 
 public class Verifier {
 
     public VerificationOutcome verify(KernelEstimatorSampling kernelEstimatorSampling, CalculationOutcome calculationOutcome) {
         CalculationOutcome reference = kernelEstimatorSampling.calculateUsing(new SimpleEstimationEngine());
         float[] errors = computeErrors(calculationOutcome.getEstimationPoints(), reference.getEstimationPoints());
         return new VerificationOutcome(errorCountsByClass(errors));
     }
 
     private float[] computeErrors(float[] estimationPoints, float[] reference) {
         Preconditions.checkArgument(estimationPoints.length == reference.length, "compared arrays must be equal in length");
         float[] errors = new float[estimationPoints.length];
         for (int i = 0; i < estimationPoints.length; i++) {
             errors[i] = estimationPoints[i] - reference[i];
         }
         return errors;
     }
 
     private EnumMap<ErrorClass, Integer> errorCountsByClass(float[] errors) {
         EnumMap<ErrorClass, Integer> errorCountsByClass = initializeCountsByClass();
         for (int i = 0; i < errors.length; i++) {
             incrementValueForClass(errorCountsByClass, ErrorClass.forValue(errors[i]));
         }
         return errorCountsByClass;
     }
 
     private EnumMap<ErrorClass, Integer> initializeCountsByClass() {
         EnumMap<ErrorClass, Integer> countsByClass = new EnumMap<ErrorClass, Integer>(ErrorClass.class);
         for (ErrorClass errorClass : ErrorClass.values()) {
             countsByClass.put(errorClass, 0);
         }
         return countsByClass;
     }
 
     private void incrementValueForClass(EnumMap<ErrorClass, Integer> errorCountsByClass, ErrorClass errorClass) {
         Integer value = errorCountsByClass.get(errorClass);
         errorCountsByClass.put(errorClass, value + 1);
     }
 
 }
