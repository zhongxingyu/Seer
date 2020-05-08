 /**
  * 
  */
 package uk.ac.cam.eng.rulebuilding.hadoop;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapreduce.Reducer;
 
 import uk.ac.cam.eng.extraction.hadoop.datatypes.PairWritable3;
 import uk.ac.cam.eng.rulebuilding.retrieval.RuleFileBuilder;
 
 /**
  * @author jmp84. Reducer for the rule building job. Used
  * to add features and special rules.
  *
  */
 public class RuleBuildingReducer extends Reducer<IntWritable, PairWritable3, Text, Text> {
 
     private RuleFileBuilder ruleFileBuilder;
 	
 	/* (non-Javadoc)
 	 * @see org.apache.hadoop.mapreduce.Reducer#setup(org.apache.hadoop.mapreduce.Reducer.Context)
 	 */
 	@Override
 	protected void setup(Context context)
 			throws IOException, InterruptedException {
         Configuration conf = context.getConfiguration();
         ruleFileBuilder = new RuleFileBuilder(conf);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.apache.hadoop.mapreduce.Reducer#reduce(java.lang.Object, java.lang.Iterable, org.apache.hadoop.mapreduce.Reducer.Context)
 	 */
 	@Override
 	protected void reduce(IntWritable key, Iterable<PairWritable3> values, Context context)
 			throws IOException, InterruptedException {
 		Configuration conf = context.getConfiguration();
 		List<PairWritable3> rules = new ArrayList<>();
 		for (PairWritable3 value: values) {
 			rules.add(value.copy()); // copy to avoid having the same thing
 		}
		List<PairWritable3> rulesWithFeatures = ruleFileBuilder.getRulesWithFeatures(conf, rules);
		String output = ruleFileBuilder.printSetSpecificRuleFile(rulesWithFeatures);
        context.write(new Text(output), new Text());
 	}
 }
