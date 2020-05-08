 package features;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 
 import annotationclasses.AuxTokenInfoAnnotation;
 import dataclasses.AuxTokenInfo;
 
 import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
 import edu.stanford.nlp.ling.CoreLabel;
 
 public class IntervalFeature implements TempEvalFeature {
 	
 	private static final String INTERVAL_STRING = "__INTER__";
 
 	private static final HashSet<String> interval_strings = new HashSet<String>(Arrays.asList(new String[] {"before","after","during","on","this","in","for","until","since","between","by","advance", "at", "to","from","while","over","when","prior","early","around","ahead","within"}));
 	private int prev_window_size;
 	
 	//effective window size appears to be 2 or 3 to avoid words like "the" separating interval string and timex tag
 	public IntervalFeature(int window_size) {
 		this.prev_window_size = window_size;
 	}
 	
 	@Override
 	public void add(List<String> features, CoreLabel token1, CoreLabel token2) {
 		AuxTokenInfo auxTimeInfo = token1.get(AuxTokenInfoAnnotation.class);
 		
 		CoreLabel prev = auxTimeInfo.prev;
 		for(int i = 0; i < prev_window_size; i++){
 		
 			if (prev != null) {
 				//System.out.println(prev.get(TextAnnotation.class));
 				String s = prev.get(TextAnnotation.class);
 				if(interval_strings.contains(s.toLowerCase())){
 					features.add(INTERVAL_STRING + "=" + i + s.toLowerCase());
 				}
				prev = prev.get(AuxTokenInfoAnnotation.class).prev;
 			}
 		}
 	}
 	
 }
 
