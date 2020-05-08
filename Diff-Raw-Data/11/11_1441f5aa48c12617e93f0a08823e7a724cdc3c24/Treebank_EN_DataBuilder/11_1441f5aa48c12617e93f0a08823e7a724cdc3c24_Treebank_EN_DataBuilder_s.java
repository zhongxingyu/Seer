 
 package openie.databuilder;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import openie.text.FeatureFactory;
 import openie.text.FeatureFactory.Node;
 
 public class Treebank_EN_DataBuilder {
 	
 	private PrintStream trainStream = null;
 	private PrintStream testStream = null;
     
 	public Treebank_EN_DataBuilder(PrintStream trainStream, PrintStream testStream) {
         this.trainStream = trainStream;
         this.testStream = testStream;
 	}
 	
 
 
 	// -------------------------------------------------
 	static public int MAX_DISTANCE = 25;
 	static public int MAX_NP = 10;
 //	static public int maxDistance = 0;
 //	static public int[] distanceMap = new int[101];
 //	static public int numNNP = 0;
 //	static public int numNNG = 0;
 	static HashMap<String, Integer> tagMap = new HashMap<String, Integer>();
 
 	public boolean printInstance(String line, BufferedWriter outputWriter, int section) throws IOException {
 		ArrayList<Node> sequence = new ArrayList<Node>();
 		
 		line = line.replace("\\/", "\\\\");
 		
 		// left context of ARG1
 		String leftContextOfARG1 = line.substring(0, line.indexOf("<ARG1"));
 		FeatureFactory.tokenizeString(leftContextOfARG1, sequence);
 		// ARG1
 		String e1String = line.substring(line.indexOf("<ARG1"), line.indexOf("</ARG1>"));
 		e1String = e1String.substring(e1String.indexOf(">")+1);
 		FeatureFactory.tokenizeString(e1String, sequence, "ENT");
 		// context between ARG1 and ARG2
 		String context = line.substring(line.indexOf("</ARG1>"), line.indexOf("<ARG2"));
 		context = context.substring(context.indexOf(">")+1);
 		FeatureFactory.tokenizeString(context, sequence);
 		// ARG2
 		String e2String = line.substring(line.indexOf("<ARG2"), line.indexOf("</ARG2>"));
 		e2String = e2String.substring(e2String.indexOf(">")+1);
 		FeatureFactory.tokenizeString(e2String, sequence, "ENT");
 		// right context of ARG2
 		String rightContextOfARG2 = line.substring(line.indexOf("</ARG2>"));
 		rightContextOfARG2 = rightContextOfARG2.substring(rightContextOfARG2.indexOf(">")+1);
 		FeatureFactory.tokenizeString(rightContextOfARG2, sequence);
 		
 		/*
 		 * Filtering unexpected instances
 		 */
 		boolean containVerb = false, isPositiveInstance = false, containOfProperNounInARG1 = false, containOfProperNounInARG2 = false, containOfNounAndNumberInARG2 = true;
 		int distanceBetweenARGs = 0, numberOfEntityNode = 0, numberOfNounPhraseNode = 0;
 		
 		for (int i = 0; i < sequence.size(); i++) {	
 			Node node = sequence.get(i);
 			
 			if (node.label == "ENT") {
 				numberOfEntityNode++;
 //				if (!tagMap.containsKey(node.postag)) {
 //					tagMap.put(node.postag, 1);
 //				} else
 //					tagMap.put(node.postag, tagMap.get(node.postag)+1);
 								
 				if (numberOfEntityNode == 1) {
 					if (node.postag.indexOf("NNP") >= 0)
 						containOfProperNounInARG1 = true;
 				} else if (numberOfEntityNode == 2) {
 					if (node.postag.indexOf("NN")< 0 && node.postag.indexOf("CD") < 0)
 						containOfNounAndNumberInARG2 = false;					
 					if (node.postag.indexOf("NNP") >= 0)
 						containOfProperNounInARG2 = true;				
 				}
 
 			}
 			if (node.label == "NP") {
 				if (numberOfEntityNode > 0)
 					numberOfNounPhraseNode++;
 			}
 			
 			if (numberOfEntityNode > 0) {
 				if (node.postag.startsWith("VB"))
 					containVerb = true;
 				if (node.label.endsWith("REL")) {
 					isPositiveInstance = true;
 					if (!tagMap.containsKey(node.postag)) 
 						tagMap.put(node.postag, 1);
 					else
 						tagMap.put(node.postag, tagMap.get(node.postag)+1);
 				}
 				if (node.label != "ENT")
 					distanceBetweenARGs++;
 			}
 			
 			if (numberOfEntityNode > 1)
 				break;
 		} // end for
 		
 		if (!containVerb)
 			return false;
 		if (distanceBetweenARGs > MAX_DISTANCE) 
 			return false;
 		if (numberOfNounPhraseNode > MAX_NP)
 			return false;
 		if (!containOfProperNounInARG1)
 			return false;
 		if (!containOfNounAndNumberInARG2)
 			return false;
 		
 //		if (isPositiveInstance && distanceBetweenARGs > maxDistance)
 //			maxDistance = distanceBetweenARGs;
 //		if (isPositiveInstance) {
 //			if (containOfProperNounInARG2) 
 //				distanceMap[numberOfNounPhraseNode]++;
 //			else
 //				numNNG++;
 //			//distanceMap[npNumber]++;
 //		}
 		
 		/*
 		 *  Generating features
 		 */
 		ArrayList<Node> wordForm = new ArrayList<Node>();
 		ArrayList<ArrayList<String>> featureForm = FeatureFactory.generateFeature(sequence, wordForm);
 		
 		/*
 		 * Saving the feature format into files
 		 */
 		numberOfEntityNode = 0;
 		StringBuffer buf1 = new StringBuffer();
 		buf1.append("# ");
 		for (Node n : wordForm)
 			buf1.append(n.word + " ");
 		if (section > TEST_SET)
 			testStream.println(buf1.toString());
 		else 
 			trainStream.println(buf1.toString());
 		
 		for (ArrayList<String> oneline : featureForm) {
 			StringBuffer buf = new StringBuffer();
 			for (String t : oneline)
 				buf.append(t + " ");
 			buf.append("\n");
 			outputWriter.write(buf.toString());
 			if (section > TEST_SET)
 				testStream.print(buf.toString());
 			else 
 				trainStream.print(buf.toString());
 		}
 		outputWriter.write("\n");
 		if (section > TEST_SET) 
 			testStream.println();
 		else 
 			trainStream.println();
 		
 		return true;
 	}
 	static int TEST_SET = 30;
 	
 	public void run(String inputPath, String outputPath) throws IOException {
 		run(new File(inputPath), new File(outputPath));
 	}
 	
 	public void run(File inputDir, File outputDir) throws IOException {
         String[] filelist = inputDir.list();
 		if (!outputDir.exists()) outputDir.mkdir();
         
         for (String filename : filelist) {
         	File inFile = new File(inputDir.getPath() + "/" + filename);
         	
         	if (inFile.isDirectory()) {
         		run(inFile, new File(outputDir.getPath() + "/" + filename));
         	} 
         	else if (inFile.isFile()) {
         		if (!filename.endsWith(".instance"))
         			continue;
         		
         		int instanceNumber = 0, positiveNumber = 0;
         		System.err.print("Parsing file: " + inFile.getPath() + " ... ");
         		
         		// for testing
         		String[] fileToken = inFile.getPath().split("\\\\");
         		int section = Integer.parseInt(fileToken[fileToken.length-2]);
        		//if (section > 1)
        		//	break;
         		
     			BufferedReader inputReader = new BufferedReader(new FileReader(inFile.getPath()));
         		final String filenamePrefix = filename.substring(0, filename.lastIndexOf('.'));
         		BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputDir.getPath() + "/" + filenamePrefix + ".feature"));
     			
     			String line = null;
     			while ((line = inputReader.readLine()) != null) {
     				if (line.length() <= 0 || line.startsWith("# ")) {
 				    	continue;
     				}
     				if (printInstance(line, outputWriter, section)) {
     					instanceNumber++;
     					if (line.contains("REL"))
     						positiveNumber++;
     				}
     			}
     			System.err.println("done (" + instanceNumber + "=" + positiveNumber + "+" + (instanceNumber-positiveNumber) +")");
     			totalInstanceNumber += instanceNumber;
     			totalPositiveNumber += positiveNumber;
     			inputReader.close();
     			outputWriter.close();
         	}
         }
 	}
 	
 	static public int totalInstanceNumber = 0, totalPositiveNumber = 0;
 	
 	static public void main(String args[]) throws IOException {
 		System.setProperty("line.separator", "\n");
 				
 		int argIndex = 0;
 		if (args.length < 1) {
 			System.err.println("usage: java " + Treebank_EN_DataBuilder.class.getName() + " [options] \n" +
 					"\t -inputDir inputDataPath \n" +
 					"\t -outputDir outputDirPath \n" +
 					"\t -trainfile trainFile \n" + 
 					"\t -testfile testFile\n");
 			return;
 		}
 			
 		String inputDirPath = null, outputDirPath = null, trainFileName = null, testFileName = null;
 		PrintStream trainStream = null, testStream = null;
 		
 	    while (argIndex < args.length && args[argIndex].charAt(0) == '-') {
 	    	
 			if (args[argIndex].equalsIgnoreCase("-inputDir")) {
 				inputDirPath = args[argIndex + 1];
 				argIndex += 2;
 			} else if (args[argIndex].equalsIgnoreCase("-outputDir")) {
 				outputDirPath = args[argIndex + 1];
 				argIndex += 2;
 			} else if (args[argIndex].equalsIgnoreCase("-trainFile")) {
 				trainFileName = args[argIndex + 1];
 				argIndex += 2;
 			} else if (args[argIndex].equalsIgnoreCase("-testFile")) {
 				testFileName = args[argIndex + 1];
 				argIndex += 2;
 			}
 	    }
 	    trainStream = new PrintStream(new FileOutputStream(trainFileName));
 	    testStream = new PrintStream(new FileOutputStream(testFileName));
 	    Treebank_EN_DataBuilder builder = new Treebank_EN_DataBuilder(trainStream, testStream);
 		
 		builder.run(inputDirPath, outputDirPath);
 		
 		System.err.println("Done (" + totalInstanceNumber + "=" + totalPositiveNumber + "+" + (totalInstanceNumber-totalPositiveNumber) +")");
 //		System.err.println(maxDistance);
 //		for (int i = 0; i < distanceMap.length; i++)
 //			System.err.println(i+ "\t" + distanceMap[i]);
 //		System.err.println("NNP: " + numNNP + " NNG: " + numNNG);
 //		for (String a : tagMap.keySet())
 //			System.err.println(a + " " + tagMap.get(a));
 		
 		if (trainStream != null)
 			trainStream.close();
 		if (testStream != null)
 			testStream.close();
 
 	}
 }
