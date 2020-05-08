 package nlp;
 
 import weka.core.Attribute;
 import weka.core.Instances;
 import weka.core.tokenizers.NGramTokenizer;
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.StringToWordVector;
 
 public class StringVectorWrapper {
 
 	public InstancesPair ApplyStringVectorFilter(Instances data, String attribute, Instances otherData) throws Exception {
 		StringToWordVector filter_ngram = new StringToWordVector();
 		Attribute ngram = data.attribute(attribute);
 		if(ngram != null){
 			int attributes[] = new int[1];
 			attributes[0] = ngram.index();
 			
 			filter_ngram.setAttributeIndicesArray(attributes);
 			filter_ngram.setUseStoplist(false);
 			filter_ngram.setLowerCaseTokens(false);
 			filter_ngram.setAttributeNamePrefix(attribute + "_");
 			NGramTokenizer tokenizer_pos = new NGramTokenizer();		
			tokenizer_pos.setOptions(weka.core.Utils.splitOptions("-min 1 -max 2"));
 			filter_ngram.setTokenizer(tokenizer_pos);
 			
 			filter_ngram.setInputFormat(data);
 			data = Filter.useFilter(data, filter_ngram);
 
 			otherData = Filter.useFilter(otherData, filter_ngram);
 		}
 		
 		return new InstancesPair(data, otherData);
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
