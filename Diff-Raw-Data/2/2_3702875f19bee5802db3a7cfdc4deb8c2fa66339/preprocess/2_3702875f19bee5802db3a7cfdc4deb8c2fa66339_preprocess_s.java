 import weka.core.Attribute;
 import weka.core.FastVector;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.filters.unsupervised.attribute.StringToWordVector;
 import weka.core.stemmers.IteratedLovinsStemmer;
 import weka.core.tokenizers.CharacterDelimitedTokenizer;
 import weka.core.tokenizers.Tokenizer;
 import weka.core.tokenizers.WordTokenizer;
 import weka.core.SelectedTag;
 import weka.filters.unsupervised.attribute.Reorder;
 import weka.filters.MultiFilter;
 import weka.filters.Filter;
 
 public class preprocess {
 	private String licens;
 	private Instances instances;
 	
 	public preprocess() {
 		
 	}
 	
 	public preprocess( String text ) {
 		licens = text;
 	}
 	
 	public int start() {
         if(convert())
             return 0;
 		return 1;
 	}
 	
 	public Instances getInstances() {
 		return instances;
 	}
 	
 	private boolean convert() {
         Instances data = createInstance();
         if(doFilter(data)){
             return true;
         }
         
 		return false;
 	}
 	
 	private boolean doFilter(Instances dataSet) {
         boolean errorCode = false;
 		String theDelim = "@_.<>-:;+-=%/#\"\\\' &!()*";
         
 		//Filtering the dataset.
 		try{
             // Convert with stringtowordvector
             
             MultiFilter multi = new MultiFilter();
             Filter[] filters = new Filter[2];
             
             StringToWordVector wordVec = new StringToWordVector();
             wordVec.setWordsToKeep(3000);
             wordVec.setDoNotOperateOnPerClassBasis(false);
             wordVec.setIDFTransform(true);
             wordVec.setNormalizeDocLength(new SelectedTag(1,StringToWordVector.TAGS_FILTER));
             
             wordVec.setLowerCaseTokens(true);
             wordVec.setMinTermFreq(1);
             wordVec.setOutputWordCounts(true);
             IteratedLovinsStemmer stemmer = new IteratedLovinsStemmer();
             wordVec.setStemmer(stemmer);
             wordVec.setUseStoplist(true);
             wordVec.setTFTransform(true);
             WordTokenizer tokenizer = new WordTokenizer();
             tokenizer.setDelimiters(tokenizer.getDelimiters().concat(theDelim));
             wordVec.setTokenizer(tokenizer);
             wordVec.setInputFormat(dataSet);
             
             filters[0] = wordVec;
             Reorder reorder = new Reorder();
             reorder.setAttributeIndices("last-first");
             reorder.setInputFormat(dataSet);
             filters[1] = reorder;
             multi.setFilters(filters);
             multi.setInputFormat(dataSet);
             for (int i = 0; i < dataSet.numInstances(); i++)
             {
                 multi.input(dataSet.instance(i));
             }
             multi.batchFinished();
             FastVector filtered = new FastVector();
             while (multi.outputPeek() != null)
             {
                 filtered.addElement(multi.output());
             }
             Instances filSet = new Instances(((Instance)filtered.elementAt(0)).dataset());
             for (int i = 0; i < filtered.size(); i++)
             {
                 filSet.add((Instance)filtered.elementAt(i));
             }
 	        instances = filSet;
             errorCode = true;
 		}catch(Exception e){
 		}
 		return errorCode;
 	}
     
     private Instances createInstance(){
         // Create data set structure
         FastVector strings = null;
         Attribute contAttr = new Attribute("content",strings);
         
         FastVector attInfo = new FastVector();
         attInfo.addElement(contAttr);
         
        Instances dataSet = new Instances(attInfo,1);
         
         //read the content and add the instance data
         Instance f = new Instance(1);
         f.setDataset(dataSet);
         f.setValue(contAttr, licens);
         dataSet.add(f);
         
         return dataSet;
     }
 }
