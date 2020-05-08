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
		String swvOptions[] = {"-W 1000","-I","-L", "-M 1"};
 		
 		System.out.println("(TFIDF Model): Applying...");
 		
 		try {
 		
			System.out.println("Instances before processing: " + is.firstInstance());
			
			is.setClassIndex(1);
 			this.swv.setInputFormat(is);
 					
 			//Set attribute indices
			this.swv.setAttributeIndices("0");
			
			//Do not operate on per class basis
			//this.swv.setDoNotOperateOnPerClassBasis(true);
			
 		
 			this.swv.setOptions(swvOptions);
 		
 			wordVectors = this.swv.useFilter(is, this.swv);
 			
 			System.out.println("(TFIDF Model): Text Model Created");
 			
			System.out.println("Instances after processing: " + wordVectors.firstInstance());
 
 		
 		} catch (Exception err) {
 		
 			System.out.println("(TFIDF Model)(Error): " + err.toString());
 		
 		}
 		
 		return wordVectors;
 		
 	}	
 
 }
