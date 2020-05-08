 /*
  * Generate data set from given arff file and sequence files
  * License: GPL
  */
 package proteinprediction.io;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import proteinprediction.rawdata.StructuralFastaSeq;
 import weka.core.Attribute;
 import weka.core.AttributeStats;
 import weka.core.FastVector;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.converters.ArffSaver;
 import proteinprediction.ProgramEntryPoint;
 import proteinprediction.ProgramSettings;
 
 /**
  * Generate data set from given arff file and sequence files
  * for training a predictor of TML and TMH
  * @author Shen Wei
  */
 public class DatasetGenerator implements ProgramEntryPoint {
     /**
      * input arff file
      */
     private File input;
     
     /**
      * out put arff.gz file
      */
     private File output;
     
     /**
      * class labels for new class attribute
      */
     private static FastVector classLabels = null;
     
     /**
      * new class attribute
      */
     private static Attribute classAttr = null;
     
     /**
      * database for fasta sequences with structural info
      */
     private HashMap<String, StructuralFastaSeq> structuralFastaDB;
     
     /**
      * class label for non-TMH/TML residues
      */
     //public static final String CLASS_LABEL_UNKNOWN = "X";
     
     /**
     * A simple DatasetGenerator that does nothing!
     */
    public DatasetGenerator() {}
    
    /**
      * 
      * @param in
      * @param out
      * @param seqdb
      * @throws IOException 
      */
     public DatasetGenerator(File in, File out, File seqdb) 
             throws IOException 
     {
         this.input = in;
         this.output = out;
         this.structuralFastaDB = StructuralFastaLoader.loadFromFile(seqdb);
     }
     
     /**
      * generate converted arff file and write into output file
      * @throws IOException 
      */
     public void generateDataset() throws IOException {
         this.generateDataset(false);
     }
     
     /**
      * for test purpose 
      * @param args input_path output_path db_path
      */
     @Override
     public int run(String[] args) {
         try {
             DatasetGenerator dg = new DatasetGenerator(
                 new File(args[0]),
                 new File(args[1]),
                 new File(args[2]));
             if (args.length > 3 && args[3].equals("true"))
             {
                 //balance classes
                 dg.generateDataset(true);
             } else {
                 dg.generateDataset();
             }
         } catch(IOException e) {
             return ProgramSettings.PROGRAM_EXIT_IOERROR;
         } catch (Exception e) {
             return ProgramSettings.PROGRAM_EXIT_MALFORMED_ARGS;
         }
         return ProgramSettings.PROGRAM_EXIT_NORMAL;
     }
     
     /**
      * returns the usage of this entry point
      */
     @Override
     public String getUsageAndHelp() {
         return "Usage: DataGenerator <input.arff> "
             + "<output.arff.gz> <structural_fasta>"
             + " [balance_classes=false]";
     }
     
     /**
      * @return A little description of this program mode
      */
     @Override
     public String getShortDescription() {
         return "read in an arff and a fasta file and outputs an dataset file!";
     }
 
     /**
      * @return The name of this program mode
      */
     @Override
     public String getCommandLineArgumentName() {
         return "DataGenerator";
     }
 
     /**
      * get a vector of class labels
      * @return 
      */
     private static FastVector getClassLabels() {
         if (classLabels == null) {
             classLabels = new FastVector();
             classLabels.addElement("L");
             classLabels.addElement("H");
         }
         return classLabels;
     }
 
     /**
      * get class label of a residue in the given protein
      * @param protein Uniprot name of the protein
      * @param pos index of the residue, starts from 0
      * @return class label to the residue, or null if class label in unknown
      */
     private String getResidueClassLabel(String protein, int pos) {
         StructuralFastaSeq sseq = this.structuralFastaDB.get(protein);
         if (sseq == null) return null;
         String label = ""+sseq.getResidueStructure(pos);
         if (getClassLabels().contains(label))
             return label;
         return null;
     }
 
     /**
      * get class Attribute of the new dataset
      * @return 
      */
     private Attribute getClassAttribute() {
         if (classAttr == null) {
             classAttr = new Attribute("CLASS", getClassLabels());
         }
         return classAttr;
     }
 
     private void generateDataset(boolean balanceClasses) throws IOException {
         Pattern pattern = Pattern.compile("^(.*)_(\\d+)$");
        
         Instances dataset = new Instances(
                 new BufferedReader(
                 new FileReader(input)));
         
         int oldClassIdx = dataset.numAttributes() - 1;
         //add new attribute
         dataset.insertAttributeAt(getClassAttribute(), dataset.numAttributes());
         dataset.setClassIndex(dataset.numAttributes() - 1);
         
         for (int i = 0; i < dataset.numInstances(); i++) {
             Instance inst = dataset.instance(i);
             
             String idPos = inst.stringValue(0);
             Matcher matcher = pattern.matcher(idPos);
             
             if (!matcher.matches()) continue;
             String protein = matcher.group(1);
             int pos = Integer.parseInt(matcher.group(2));
             String label = getResidueClassLabel(protein, pos);
             if (label == null) {
                 inst.setClassMissing();
             } else {
                 inst.setClassValue(label);
             }
             if (i != 0 && i % 1000 == 0)
                 System.err.println("Processed " + i + " lines");
         }
         
         //remove unwanted attributes: class and ID_POS
         dataset.deleteAttributeAt(oldClassIdx);
         dataset.deleteAttributeAt(0);
         
         //remove instances whose class label is missing
         dataset.deleteWithMissingClass();
         
         //balance classes with over sampling
         if (balanceClasses) {
             AttributeStats stats = dataset.attributeStats(dataset.classIndex());
             int[] classCounts = stats.nominalCounts;
             
             //get maximum number of instances of a class
             int maxCount = 0;
             for (int count : classCounts) {
                 if (count > maxCount) maxCount = count;
             }
             
             //try to balance classes
             
             //step 1, get remaining instances to be generated
             int[] remain = new int[classCounts.length];
             for (int classId = 0; classId < classCounts.length; classId++)
             {
                 int count = classCounts[classId];
                 if(count == maxCount) {
                     remain[classId] = 0;
                     continue;
                 }
                 remain[classId] = maxCount - count;
             }
             //step 2, prepare datasets with instances from one class
             Instances oneClassDatasets[] = new Instances[classCounts.length]; 
             for (int classId = 0; classId < classCounts.length; classId++)
             {
                 oneClassDatasets[classId] = new Instances(dataset, 0);
             }
             //add instances
             Enumeration enm = dataset.enumerateInstances();
             while(enm.hasMoreElements()) {
                 Instance inst = (Instance)enm.nextElement();
                 int classIdx = (int)inst.classValue();
                 oneClassDatasets[classIdx].add(inst);
             }
             
             //step 3, generate remaining instances
             for (int klass = 0; klass < classCounts.length; klass++) {
                 if (remain[klass] == 0) continue;
                 int count = classCounts[klass];
                 int folds = (int) Math.ceil(maxCount / (double)count) - 1;
                 Instances set = oneClassDatasets[klass];
                 set.randomize(new Random());
                 
                 Enumeration enumerate = set.enumerateInstances();
                 while (remain[klass] > 0) {
                     Instance inst = (Instance) enumerate.nextElement();
                     int Folds = folds;
                     while (Folds > 0 &&  remain[klass] > 0) {
                         dataset.add(inst);
                         Folds--;
                         remain[klass]--;
                     }
                 }
             }
         }
         
         //save changed data set
         ArffSaver saver = new ArffSaver();
         saver.setCompressOutput(true);
         saver.setFile(output);
         saver.setStructure(dataset);
         saver.writeBatch();
     }
 
 }
