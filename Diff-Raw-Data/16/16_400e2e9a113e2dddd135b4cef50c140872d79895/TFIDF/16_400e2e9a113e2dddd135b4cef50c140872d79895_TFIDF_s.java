 import java.util.List;
 
 import weka.core.Instances;
 import weka.core.converters.TextDirectoryLoader;
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.StringToWordVector;
 import weka.classifiers.bayes.NaiveBayesMultinomial;
 
 public class TFIDF implements CustomModel {
 	
 	private StringToWordVector swv; 
 	
 	public TFIDF (){
 	
 		this.swv = new StringToWordVector();  
 	}
 	
 	public Instances runModel(Instances is) {
 		
 		//wordvector instances
 		Instances wordVectors = null;
 		
 		//Set filter options
		String swvOptions[] = {"-W 1000","-I","-L"};
 		
 		System.out.println("(TFIDF Model): Applying...");
 		
 		try {
 		
 			this.swv.setInputFormat(is);
 					
 			//Set attribute indices
			this.swv.setAttributeIndicesArray(new int[] { 1 });
 		
 			this.swv.setOptions(swvOptions);
 		
 			wordVectors = this.swv.useFilter(is, this.swv);
 			
 			System.out.println("(TFIDF Model): Text Model Created");
 			
			System.out.println(wordVectors.firstInstance());
 
 		
 		} catch (Exception err) {
 		
 			System.out.println("(TFIDF Model)(Error): " + err.toString());
 		
 		}
 		
 		return wordVectors;
 		
 	}	
 
 }
