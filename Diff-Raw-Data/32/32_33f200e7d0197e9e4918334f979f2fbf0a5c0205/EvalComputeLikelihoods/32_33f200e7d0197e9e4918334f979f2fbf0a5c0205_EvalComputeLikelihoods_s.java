 package de.tuberlin.dima.ml.pact.logreg.sfo.udfs;
 
 import org.apache.mahout.math.Vector;
 
 import de.tuberlin.dima.ml.logreg.LogRegMath;
 import de.tuberlin.dima.ml.logreg.sfo.IncrementalModel;
 import de.tuberlin.dima.ml.logreg.sfo.SFOGlobalSettings;
 import de.tuberlin.dima.ml.pact.logreg.sfo.PactIncrementalModel;
 import de.tuberlin.dima.ml.pact.types.PactVector;
 import eu.stratosphere.pact.common.stubs.Collector;
 import eu.stratosphere.pact.common.stubs.CrossStub;
 import eu.stratosphere.pact.common.type.PactRecord;
 import eu.stratosphere.pact.common.type.base.PactDouble;
 import eu.stratosphere.pact.common.type.base.PactInteger;
 
 public class EvalComputeLikelihoods extends CrossStub {
   
   public static final int IDX_INPUT1_INPUT_RECORD = 0;
   public static final int IDX_INPUT1_LABEL = 1;
   
   public static final int IDX_INPUT2_BASEMODEL = 0;
   public static final int IDX_INPUT2_TRAINED_COEFFICIENTS = 1;
   
   public static final int IDX_OUT_DIMENSION = EvalSumLikelihoods.IDX_DIMENSION;
   public static final int IDX_OUT_LL_BASE = EvalSumLikelihoods.IDX_LL_BASE;
   public static final int IDX_OUT_LL_NEW = EvalSumLikelihoods.IDX_LL_NEW;
   
   private boolean baseModelCached = false;
   private IncrementalModel baseModel = null;
 
   private final PactRecord recordOut = new PactRecord(3);
 
   @Override
   public void cross(PactRecord testRecord, PactRecord baseModelAndCoefficients,
       Collector<PactRecord> out) throws Exception {
 
     int y = testRecord.getField(IDX_INPUT1_INPUT_RECORD, PactInteger.class).getValue();
     Vector xi = testRecord.getField(IDX_INPUT1_LABEL, PactVector.class).getValue();
     
     // Optimization: Cache basemodel, will always be the same
     if (!baseModelCached) {
       baseModel = baseModelAndCoefficients.getField(IDX_INPUT2_BASEMODEL, PactIncrementalModel.class).getValue();
       baseModelCached = true;
     }
 
     Vector coefficients = baseModelAndCoefficients.getField(IDX_INPUT2_TRAINED_COEFFICIENTS, PactVector.class).getValue();
     
 //    System.out.println("EVAL CROSS: y=" + y + " xi-non-zeros=" + xi.getNumNonZeroElements() + " baseModel-non-zeros=" + baseModel.getW().getNumNonZeroElements() + " coefficients-non-zeros=" + coefficients.getNumNonZeroElements());
 
     double piBase = LogRegMath.predict(xi, baseModel.getW(), SFOGlobalSettings.INTERCEPT);
     double llBase = LogRegMath.logLikelihood(y, piBase); 
 
     for (Vector.Element feature : xi.nonZeroes()) {
       int dim = feature.index();
       if (! baseModel.isFeatureUsed(dim)) {
         baseModel.getW().set(dim, coefficients.get(dim));
         double piNew = LogRegMath.logisticFunction(xi.dot(baseModel.getW()) + SFOGlobalSettings.INTERCEPT);
         baseModel.getW().set(dim, 0d);    // reset to base model
 
         double llNew = LogRegMath.logLikelihood(y, piNew);
         
         recordOut.setField(IDX_OUT_DIMENSION, new PactInteger(dim));
         recordOut.setField(IDX_OUT_LL_BASE, new PactDouble(llBase));
         recordOut.setField(IDX_OUT_LL_NEW, new PactDouble(llNew));
         out.collect(recordOut);
       }
     }
     
   }
 
 }
