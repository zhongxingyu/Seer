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
 public class Source2TargetProbability implements Feature {
 
    // TODO add this to the config
    private final static double defaultS2t = -4.7;

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
        double s2t =
                ((DoubleWritable) mapReduceFeatures.get()[0]).get();
        res.add(s2t == 0 ? defaultS2t : Math.log(s2t));
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
         List<Double> res = new ArrayList<>();
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
         List<Double> res = new ArrayList<>();
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
