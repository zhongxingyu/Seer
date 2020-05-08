 package data;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Set;
 
 public class I2b2Dataset extends Dataset {
 
 	/**
 	 * Load instances from comma-separated file. Map patient_num(s) to labels.
 	 * The file is formated using i2b2 phenotype format. Each patient_num is 
 	 * read only once (some datasets have multiple vectors for the same patient). 
 	 */
 	public void loadCSVFile(String vectorFile, String labelFile) throws FileNotFoundException {
     
 		HashSet<String> patientNumsProcessed = new HashSet<String>();
 		Map<String, String> patientNumToLabel = loadLabels(labelFile);
 		
 		File file = new File(vectorFile);
     Scanner scan = new Scanner(file);
     
     String[] columns = null;
     
     while(scan.hasNextLine()) {
     	String line = scan.nextLine();
 
     	// skip comments and empty lines
     	if(line.startsWith("#") || line.length() == 0) {
     		continue;
     	}
     	
     	// read the line containing column names
     	if(line.startsWith("patient_num")) {
     		columns = line.split("\\|");
     		continue;
     	}
     	
     	// now read the data
       String[] elements = line.split(",");
       String patientNum = elements[0];
       
       // we'll only need the vectors for which there are labels
       if(patientNumToLabel.containsKey(patientNum)) {
       	
       	// only store each patient once
       	if(patientNumsProcessed.contains(patientNum)) {
       		continue;
       	} else {
       		patientNumsProcessed.add(patientNum);
       	}
       	
       	Instance instance = new Instance();
       	instance.setLabel(patientNumToLabel.get(patientNum));
 
       	// iterate over vector elements
       	for(int i = 1; i < elements.length; i++) {
       		String feature = columns[i];
       		Float value = Float.parseFloat(elements[i]);
       		if(value != 0.0) {
       			instance.addFeature(feature, value);
       		}
       	}
 
       	instances.add(instance);
       }
     }
    
    scan.close();
 	}
 	
 	 /**
    * Load n unlabeled instances from a comma-separated file. Skip the vectors
    * that have labels. If more than n examples exist, select n at random.
    */
   public void loadFromCSVFile(String vectorFile, String labelFile, int n) throws FileNotFoundException {
     
     HashSet<String> patientNumsProcessed = new HashSet<String>();
     Map<String, String> patientNumToLabel = loadLabels(labelFile);
     File file = new File(vectorFile);
     Scanner scan = new Scanner(file);
     List<Instance> unlabeledInstances = new LinkedList<Instance>();
     String[] columns = null;
     
     while(scan.hasNextLine()) {
       String line = scan.nextLine();
 
       // skip comments and empty lines
       if(line.startsWith("#") || line.length() == 0) {
         continue;
       }
       
       // read the line containing column names
       if(line.startsWith("patient_num")) {
         columns = line.split("\\|");
         continue;
       }
       
       // now read the data
       String[] elements = line.split(",");
       String patientNum = elements[0];
       
       // skip vectors that have labels
       if(patientNumToLabel.containsKey(patientNum)) {
         continue;
       }
         
       // only store each patient once
       if(patientNumsProcessed.contains(patientNum)) {
         continue;
       } else {
         patientNumsProcessed.add(patientNum);
       }
 
       Instance instance = new Instance();
       instance.setLabel(null);
 
       // iterate over vector elements
       for(int i = 1; i < elements.length; i++) {
         String feature = columns[i];
         Float value = Float.parseFloat(elements[i]);
         if(value != 0.0) {
           instance.addFeature(feature, value);
         }
       }
 
       unlabeledInstances.add(instance);
     }
     
    scan.close();
    
     // select a random sample of n examples
     Collections.shuffle(unlabeledInstances, new Random(100));
     this.instances = unlabeledInstances.subList(0, n);
   }
 	
 	/**
 	 * Load labels from a comma-separated file: <patient_num>,<label>.
 	 */
 	private static Map<String, String> loadLabels(String inputFile) throws FileNotFoundException {
 		
 		Map<String, String> patientNumToLabel = new HashMap<String, String>();
 		
 		File file = new File(inputFile);
 		Scanner scan = new Scanner(file);
 		
 		while(scan.hasNextLine()) {
 			String line = scan.nextLine();
 			String[] elements = line.split(",");
 			patientNumToLabel.put(elements[0], elements[1]);
 		}
 		
		scan.close();
 		return patientNumToLabel;
 	}
 	
 	/**
 	 * Replace all instances whose label is in a set by another label.
 	 */
 	public void mapLabels(Set<String> from, String to) {
 	  
 	  for(Instance instance : instances) {
 	    if(from.contains(instance.getLabel())) {
 	      instance.setLabel(to);
 	    }
 	  }
 	}
 	
 	/**
 	 * Remove all instances of a label.
 	 */
 	public void removeLabel(String label) {
 	  
 	  List<Instance> newInstances = new ArrayList<Instance>();
 	  
 	  for(Instance instance : instances) {
 	    if(! instance.getLabel().equals(label)) {
 	      newInstances.add(instance);
 	    }
 	  }
 	  
 	  instances = newInstances;
 	}
 }
