 package org.esa.cci.lc.aggregation;
 
 import org.esa.beam.binning.AbstractAggregator;
 import org.esa.beam.binning.BinContext;
 import org.esa.beam.binning.Observation;
 import org.esa.beam.binning.Vector;
 import org.esa.beam.binning.WritableVector;
 
 /**
 * A simple aggregator adding bin indices to the output.
  * Mainly intended for debugging.
  *
  * @author Marco Peters
  */
 class LcBinIndexAggregator extends AbstractAggregator {
 
 
     LcBinIndexAggregator() {
         this(new String[]{"bin_idx"});
     }
 
     private LcBinIndexAggregator(String[] spatialFeatureNames) {
         super(LcBinIndexAggregatorDescriptor.NAME, spatialFeatureNames, spatialFeatureNames, spatialFeatureNames);
     }
 
     @Override
     public void initSpatial(BinContext ctx, WritableVector vector) {
         for (int i = 0; i < vector.size(); i++) {
             vector.set(i, Float.NaN);
         }
     }
 
     @Override
     public void aggregateSpatial(BinContext ctx, Observation observation, WritableVector spatialVector) {
         spatialVector.set(0, ctx.getIndex());
     }
 
     @Override
     public void completeSpatial(BinContext ctx, int numSpatialObs, WritableVector spatialVector) {
     }
 
     @Override
     public void initTemporal(BinContext ctx, WritableVector vector) {
         // Nothing to be done here
     }
 
     @Override
     public void aggregateTemporal(BinContext ctx, Vector spatialVector, int numSpatialObs,
                                   WritableVector temporalVector) {
         // simply copy the data; no temporal aggregation needed
         for (int i = 0; i < spatialVector.size(); i++) {
             temporalVector.set(i, spatialVector.get(i));
         }
     }
 
     @Override
     public void completeTemporal(BinContext ctx, int numTemporalObs, WritableVector temporalVector) {
         // Nothing to be done here
     }
 
     @Override
     public void computeOutput(Vector temporalVector, WritableVector outputVector) {
         // simply copy the data; no temporal aggregation needed
         for (int i = 0; i < temporalVector.size(); i++) {
             outputVector.set(i, temporalVector.get(i));
         }
     }
 
 }
