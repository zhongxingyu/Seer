 import weka.core.Attribute;
 import weka.core.FastVector;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.filters.unsupervised.attribute.StringToWordVector;
 import weka.core.stemmers.IteratedLovinsStemmer;
 import weka.core.stemmers.*;
 import weka.core.tokenizers.CharacterDelimitedTokenizer;
 import weka.core.tokenizers.Tokenizer;
 import weka.core.tokenizers.WordTokenizer;
 import weka.core.SelectedTag;
 import weka.filters.unsupervised.attribute.Reorder;
 import weka.filters.MultiFilter;
 import weka.filters.Filter;
 
 import weka.core.converters.ArffSaver;
 import java.io.*;
 
 public class preprocess {
 	private String licens;
 	private Instances instances;
 	
 	public preprocess( String text ) {
 		licens = text;
 	}
 	
 	public int start() {
 		if ( licens.length() < 2 ){
 			misc.log( "Error: empty license. " );
 			return 1;
 		}
 			
 			
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
             arffMap mapper = new arffMap();
             if(mapper.loadStructure() == 0){
                 if(mapper.map(instances) == 0){
                        instances = mapper.getFinal();
                 }
             }
                 
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
             wordVec.setWordsToKeep(1000);
             wordVec.setDoNotOperateOnPerClassBasis(false);
             wordVec.setIDFTransform(false);
             wordVec.setNormalizeDocLength(new SelectedTag(1,StringToWordVector.TAGS_FILTER));
             
             wordVec.setLowerCaseTokens(true);
             wordVec.setMinTermFreq(1);
             wordVec.setOutputWordCounts(true);
             IteratedLovinsStemmer stemmer = new IteratedLovinsStemmer();
             wordVec.setStemmer(stemmer);
             wordVec.setUseStoplist(true);
             wordVec.setTFTransform(false);
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
             saveArff(instances);
             errorCode = true;
 		}catch(Exception e){
             misc.log("Error: doFilter(); "+e.toString());
 		}
 		return errorCode;
 	}
     
     private int saveArff(Instances theCollection){
         return saveArff(theCollection, "test.arff");
     }
     
     private int saveArff(Instances theCollection, String fName){
 		try{
 			// Save data set as Strings
 			ArffSaver saver = new ArffSaver();
             saver.setInstances(theCollection);
 	        saver.setFile(new File(fName));
 	        saver.writeBatch();
 		}catch(Exception e){
 			System.err.println(e.toString());
 			return 1;
 			
 		}
 		return 0;
 	}
     
     private Instances createInstance(){
         // Create data set structure
         FastVector classes = new FastVector(2);
         classes.addElement("spyware");
         classes.addElement("legit");
         Attribute classAttr = new Attribute("category",classes);
         FastVector strings = null;
         Attribute contAttr = new Attribute("content",strings);
         
         FastVector attInfo = new FastVector();
         attInfo.addElement(classAttr);
         attInfo.addElement(contAttr);
         
         Instances dataSet = new Instances("spyware",attInfo,1);
         dataSet.setClassIndex(0);
         
         //read the content and add the instance data
         Instance f = new Instance(2);
         f.setDataset(dataSet);
         //f.setClassValue("Spyware");
         f.setValue(contAttr, licens);
         dataSet.add(f);
         
         return dataSet;
     }
 }
