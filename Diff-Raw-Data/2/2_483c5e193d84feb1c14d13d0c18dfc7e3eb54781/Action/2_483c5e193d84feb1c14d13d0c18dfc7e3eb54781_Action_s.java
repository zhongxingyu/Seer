 package at.ac.tuwien.lsdc.actions;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import weka.core.Instances;
 import weka.core.converters.ArffSaver;
 import weka.core.converters.ConverterUtils.DataSource;
 import at.ac.tuwien.lsdc.resources.Resource;
 
 public abstract class Action {
 	
 	//initialize the action
 	public abstract void init(Resource problemApp);
 	
 	//predict the outcome of the actions over the next ticks
 	public abstract int predict();
 	
 	//estimate the direct costs of the action
 	public abstract int estimate();
 	
 	//are the preconditions fulfilled?
 	public abstract boolean preconditions();
 	
 	//execute the action 
 	public abstract void execute();
 	
 	//evaluate the action after the execution and add an entry to the knowledgebase
 	public abstract boolean evaluate();
 	
 	public abstract void terminate();
 	
 	
 	//read the knowledgebase to an arff - file
 	public static Instances loadKnowledge(String filepath) throws Exception {
 		File ftest = new File(filepath);
 		Instances knowledgeBase = null;
		
 		if (!ftest.exists()) {
 			// create an empty ARFF and save it
 			DataSource source = new DataSource(filepath);
 			Instances data = source.getDataSet();
 			if (data.classIndex() == -1) {
 				data.setClassIndex(data.numAttributes() - 1);
 			}
 			
 			ftest.createNewFile();
 			BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
 			writer.write(data.toString());
 			writer.newLine();
 			writer.flush();
 			writer.close();
 		}
 		
 		if (ftest.exists()) {
 			// load data
 			DataSource source = new DataSource(filepath);
 			 
 			knowledgeBase = source.getDataSet();
 	
 			// setting class attribute if the data format does not provide this information
 			// For example, the XRFF format saves the class attribute information as well
 			if (knowledgeBase.classIndex() == -1){
 				knowledgeBase.setClassIndex(knowledgeBase.numAttributes() - 1);
 			}
 		}
 		 
 		return knowledgeBase;
 		 
 		 
 	}
 	
 	//saves the knowledgebase to an arff - file
 	//TODO: gst: Problem bei Zur�ckschreiben in die gleiche Datei 
 	public static void saveKnowledge(String filepath, Instances knowledgeBase) throws IOException {
 		 ArffSaver saver = new ArffSaver();
 		 saver.setInstances(knowledgeBase);
 		 File file = new File(filepath);
 		 if(file.exists()) {
 			 System.out.println("delete");
 			 System.out.println(file.delete());
 		 }
 		 
 		 saver.setFile(file);
 		 saver.writeBatch();
 	}
 	
 	
 }
