 package de.tuberlin.dima.ml.pact.logreg.sfo.udfs;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.google.common.collect.Lists;
 
 import de.tuberlin.dima.ml.logreg.sfo.FeatureGain;
 import de.tuberlin.dima.ml.logreg.sfo.IncrementalModel;
 import de.tuberlin.dima.ml.pact.logreg.sfo.EmptyBaseModelInputFormat;
 import de.tuberlin.dima.ml.pact.logreg.sfo.PactIncrementalModel;
 import de.tuberlin.dima.ml.pact.util.PactUtils;
 import eu.stratosphere.nephele.configuration.Configuration;
 import eu.stratosphere.pact.common.stubs.CoGroupStub;
 import eu.stratosphere.pact.common.stubs.Collector;
 import eu.stratosphere.pact.common.type.PactRecord;
 import eu.stratosphere.pact.common.type.base.PactDouble;
 import eu.stratosphere.pact.common.type.base.PactInteger;
 
 public class ApplyBest extends CoGroupStub {
 
   public static final int IDX_INPUT1_DIMENSION = MatchGainsAndCoefficients.IDX_OUT_DIMENSION;
   public static final int IDX_INPUT1_GAIN = MatchGainsAndCoefficients.IDX_OUT_GAIN;
   public static final int IDX_INPUT1_COEFFICIENT = MatchGainsAndCoefficients.IDX_OUT_COEFFICIENT;
   
   public static final int IDX_INPUT2_BASEMODEL = 0;
   
   public static final int IDX_OUT_BASEMODEL = TrainComputeProbabilities.IDX_INPUT2_BASEMODEL;
   public static final int IDX_OUT_KEY_CONST_ONE = EmptyBaseModelInputFormat.IDX_OUT_KEY_CONST_ONE;
   
   // ------------------------------------- Config Keys ------------------------------------------
 
   public static final String CONF_KEY_ADD_PER_ITERATION = "apply_best.add_per_iteration";
   
   private static final Log logger = LogFactory.getLog(ApplyBest.class);
   
   private int addPerIteration = 0;
   
   @Override
   public void open(Configuration parameters) throws Exception {
     this.addPerIteration = parameters.getInteger(CONF_KEY_ADD_PER_ITERATION, 1);
   }
 
   @Override
   public void coGroup(Iterator<PactRecord> gainsAndCoefficients,
       Iterator<PactRecord> modelRecord, Collector<PactRecord> out)
       throws Exception {
 
     // TODO _SFO: Finish implementation
     IncrementalModel baseModel = modelRecord.next().getField(IDX_INPUT2_BASEMODEL, PactIncrementalModel.class).getValue();
     
     PactRecord gainAndCoefficientRecord = null;
     List<FeatureGain> gains = Lists.newArrayList();
     while (gainsAndCoefficients.hasNext()) {
       gainAndCoefficientRecord = gainsAndCoefficients.next();
       int dim = gainAndCoefficientRecord.getField(IDX_INPUT1_DIMENSION, PactInteger.class).getValue();
       double gain = gainAndCoefficientRecord.getField(IDX_INPUT1_GAIN, PactDouble.class).getValue();
       double coefficient = gainAndCoefficientRecord.getField(IDX_INPUT1_COEFFICIENT, PactDouble.class).getValue();
       gains.add(new FeatureGain(dim, gain, coefficient));
     }
     Collections.sort(gains, Collections.reverseOrder());
 
     logger.info("Best coefficients:");
     printTopGains(gains);
    
     for (int i=0; i<addPerIteration; ++i) {
       logger.info("Add to basemodel: dim=" + gains.get(i).getDimension() + " coefficient=" + gains.get(i).getCoefficient());
       baseModel.addDimensionToModel(gains.get(i).getDimension(), gains.get(i).getCoefficient());
     }
     
     PactRecord recordOut = new PactRecord(2);
     recordOut.setField(IDX_OUT_BASEMODEL, new PactIncrementalModel(baseModel));
     recordOut.setField(IDX_OUT_KEY_CONST_ONE, PactUtils.pactOne);
     out.collect(recordOut);
   }
   
   private void printTopGains(List<FeatureGain> gains) {
     for (int i=0; i<10; ++i) {
       logger.info("d " + gains.get(i).getDimension() 
           + " gain: " + gains.get(i).getGain() + " coefficient(pact-only): " + gains.get(i).getCoefficient());
     }
   }
 
 
 }
