 /**
  * 
  */
 
 package uk.ac.cam.eng.rulebuilding.features;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.DoubleWritable;
 
 import uk.ac.cam.eng.extraction.datatypes.Rule;
 
 /**
  * @author jmp84
  */
 public class Target2SourceProbability implements Feature {
 
    // TODO add this to the config
    private final static double defaultT2s = -7;

     /*
      * (non-Javadoc)
      * @see
      * uk.ac.cam.eng.rulebuilding.features.Feature#value(uk.ac.cam.eng.extraction
      * .datatypes.Rule, org.apache.hadoop.io.ArrayWritable)
      */
     @Override
     public List<Double> value(Rule r, ArrayWritable mapReduceFeatures) {
         // TODO could use the log in the mapreduce job
         List<Double> res = new ArrayList<>();
        double t2s =
                ((DoubleWritable) mapReduceFeatures.get()[1]).get();
        res.add(t2s == 0 ? defaultT2s : Math.log(t2s));
         return res;
     }
 
     /*
      * (non-Javadoc)
      * @see
      * uk.ac.cam.eng.rulebuilding.features.Feature#valueAsciiOovDeletion(uk.
      * ac.cam.eng.extraction.datatypes.Rule, org.apache.hadoop.io.ArrayWritable)
      */
     @Override
     public List<Double>
             valueAsciiOovDeletion(Rule r, ArrayWritable mapReduceFeatures) {
         List<Double> res = new ArrayList<Double>();
         res.add((double) 0);
         return res;
     }
 
     /*
      * (non-Javadoc)
      * @see uk.ac.cam.eng.rulebuilding.features.Feature#valueGlue(uk.ac.cam.eng.
      * extraction.datatypes.Rule, org.apache.hadoop.io.ArrayWritable)
      */
     @Override
     public List<Double> valueGlue(Rule r, ArrayWritable mapReduceFeatures) {
         List<Double> res = new ArrayList<Double>();
         res.add((double) 0);
         return res;
     }
 
     /*
      * (non-Javadoc)
      * @see uk.ac.cam.eng.rulebuilding.features.Feature#getNumberOfFeatures()
      */
     @Override
     public int getNumberOfFeatures() {
         return 1;
     }
 }
