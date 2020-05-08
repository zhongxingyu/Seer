 import java.io.File;
 //import java.io.FileOutputStream;
 //import java.util.Arrays;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 
 class assignment5
 {
 	static int ngram_length = 1;
 	static int ending_length = 3;
 	static String serializedModelFileName = "HMM_POS_brown.dat";
 	
 	public static void main(String[] args) throws IOException
 	{
 		if(args[0].equals("learn"))
 		{
 			CrossValidator cv;
 			CrossValidation[] allValidations = new CrossValidation[10];
 			double mean = 0.0, stdev = 0.0;
 			try {
 				cv = new CrossValidator(args[1]);
 				for(int foldIndex = 0; foldIndex < 10; foldIndex++)
 				{
 					System.out.println("Starting fold " + foldIndex + " / " + 9);
 					HMM hmm = cv.learn();
 					allValidations[foldIndex] = cv.evaluate(hmm);
 					mean += allValidations[foldIndex].accuracy;
 					System.out.println("Fold " + (foldIndex+1) + ", " + allValidations[foldIndex].numSentences + " sentences, accuracy " + allValidations[foldIndex].accuracy);
 					cv.createFolds(args[1]);	// choose randomly from training set to obtain training and test files
 				}
 				mean /= 10.0;
 				for(int foldIndex = 0; foldIndex < 10; foldIndex++)
 				{
 					stdev += Math.pow((allValidations[foldIndex].accuracy - mean), 2);
 				}
 				stdev = Math.sqrt(stdev/10.0);
 				System.out.println("Average accuracy " + mean + ", standard deviation " + stdev);
 
 				// learn final HMM on entire training set and serialize it
 				HMM hmm = learnModelOnEntireTrainingSet(args[1]);
 				hmm.serialize(serializedModelFileName);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else if(args[0].equals("annotate"))
 		{
 			try {
 				HMM hmm = new HMM(serializedModelFileName);
 				
 				File dir = new File(args[1]);
 				if(!dir.isDirectory())
 				{
 					throw new IllegalArgumentException(args[1] + " is not a directory");
 				}
 				File[] toBeAnnotatedFiles = dir.listFiles();
				File f;
				String fileAsString = new String();
 				NonAnnotatedStringParser p;
 				Vector<String> tokens, tags;
 				StringBuilder outputString = new StringBuilder(30000);	// train files contained about 20000 chars
 				for (int i = 0; i < toBeAnnotatedFiles.length; i++) {
					f = toBeAnnotatedFiles[i];
					fileAsString = CrossValidator.readFileAsString(f);
					p = new NonAnnotatedStringParser(fileAsString);
 					tokens = p.getTokens();
 					tags = hmm.decode(tokens);
 					if(tokens.size() != tags.size())
 						throw new IllegalArgumentException("tokens.size != tags.size");
 					
 					for(int j = 0; j < tokens.size(); j++)
 					{
 						outputString.append(tokens.get(j)).append("/").append(tags.get(j)).append(" ");
 					}
 					FileWriter fw = new FileWriter(f.getAbsolutePath() + ".pos");
 					fw.write(outputString.toString());
 					fw.close();
					outputString.setLength(0);
 				}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static HMM learnModelOnEntireTrainingSet(String inputDirName) throws IOException
 	{
 		File dir = new File(inputDirName);
 		if(!dir.isDirectory())
 		{
 			throw new IllegalArgumentException("path is not a directory");
 		}
 		File[] annotatedFiles = dir.listFiles();
 		StringBuilder trainString = new StringBuilder();
 		for (int i = 0; i < annotatedFiles.length; i++) {
 			File f = annotatedFiles[i];
 			trainString.append(CrossValidator.readFileAsString(f));
 		}
 		Parser p = new Parser(trainString.toString());
 		Vector<String> tokens = p.getTokens();
 		Vector<String> tags = p.getTags();
 		HMM hmm = new HMM();
 		System.out.print("training on entire data set...");
 		hmm.train(tokens, tags);
 		System.out.println("done.");
 		
 		return hmm;
 	}
 	
 	public static String join(Collection<String> s, String delimiter)
 	{
         StringBuffer buffer = new StringBuffer();
         Iterator<String> iter = s.iterator();
         while (iter.hasNext()) {
             buffer.append(iter.next());
             if (iter.hasNext()) {
                 buffer.append(delimiter);
             }
         }
         return buffer.toString();
     }
 }
