 package org.esa.cci.lc.aggregation;
 
 import org.esa.beam.binning.AbstractAggregator;
 import org.esa.beam.binning.BinContext;
 import org.esa.beam.binning.Observation;
 import org.esa.beam.binning.VariableContext;
 import org.esa.beam.binning.Vector;
 import org.esa.beam.binning.WritableVector;
 import org.esa.beam.binning.support.GrowableVector;
 
 import java.util.Arrays;
 
 //import org.esa.beam.util.logging.BeamLogManager;
 
 /**
  * This class implements a median average to aggregate the accuracy.
  */
 class LcAccuracyAggregator extends AbstractAggregator {
 
     private final static String[] featureNames = new String[]{"accuracy"};
 
     private final int varIndex;
     private final String contextNameSpace;
 
 
     LcAccuracyAggregator(VariableContext varCtx, String[] varNames) {
         super(LcAccuracyAggregatorDescriptor.NAME,
               featureNames,
               featureNames,
               featureNames);
         varIndex = varCtx.getVariableIndex(varNames[0]);
         contextNameSpace = featureNames[0] + hashCode();
     }
 
     @Override
     public void initSpatial(BinContext ctx, WritableVector vector) {
         ctx.put(contextNameSpace, new GrowableVector(128));
     }
 
     @Override
     public void aggregateSpatial(BinContext ctx, Observation observationVector, WritableVector spatialVector) {
         final GrowableVector growableVector = ctx.get(contextNameSpace);
         growableVector.add(observationVector.get(varIndex));
     }
 
     @Override
     public void completeSpatial(BinContext ctx, int numSpatialObs, WritableVector spatialVector) {
         final GrowableVector growableVector = ctx.get(contextNameSpace);
         final float[] elements = growableVector.getElements();
         Arrays.sort(elements);
         final float lcMedian;
         final int length = elements.length;
         if (length == 0) {
             lcMedian = Float.NaN;
         } else if (length == 1) {
             lcMedian = elements[0];
         } else if (length % 2 == 0) {
             final float lowerMedian = elements[length / 2 - 1];
             final float higherMedian = elements[length / 2];
             lcMedian = lowerMedian + ((higherMedian - lowerMedian) / 2);
         } else {
             lcMedian = elements[length / 2];
         }
         spatialVector.set(0, lcMedian);
        ctx.put(contextNameSpace, null);
     }
 
     @Override
     public void initTemporal(BinContext ctx, WritableVector vector) {
         vector.set(0, 0f);
     }
 
     @Override
     public void aggregateTemporal(BinContext ctx, Vector spatialVector, int numSpatialObs, WritableVector temporalVector) {
         temporalVector.set(0, spatialVector.get(0));
     }
 
     @Override
     public void completeTemporal(BinContext ctx, int numTemporalObs, WritableVector temporalVector) {
     }
 
     @Override
     public void computeOutput(Vector temporalVector, WritableVector outputVector) {
         outputVector.set(0, temporalVector.get(0));
     }
 }
