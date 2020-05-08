 import weka.core.Attribute;
 import weka.core.FastVector;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.SparseInstance;
 import weka.core.converters.ArffSaver;
 import weka.core.converters.ArffLoader.*;
 import java.io.*;
 import weka.filters.Filter;
 import weka.filters.unsupervised.instance.NonSparseToSparse;
 
 public class arffMap{
     
     Instances defaultSet, finalSet;
      String structure = "arffMap.arff";
    
    public arffMap(){}
    
     public arffMap(String mStructure){
         structure = mStructure;
     }
 
     public int loadStructure(){
 		int errorCode = 1;
        structure = mStructure;
 		try
         {
             FileReader reader = new FileReader(structure);
             ArffReader arff = new ArffReader(reader);
             Instances instances = arff.getStructure();
             instances.setClassIndex(instances.numAttributes() - 1);
             defaultSet = instances;
             finalSet = instances;
             errorCode = 0;
         } catch (Exception e)
         {
            misc.log(e.toString());
         }
         
         return errorCode;
 	}
     
     public int map(Instances data){
         Instance f = new Instance(finalSet.numAttributes());
         f.setDataset(finalSet);
         int i=0,j=0;
         try{
         for(i=0; i<defaultSet.numAttributes()-1; i++){
             boolean mFound = false;
             for(j=0; j<data.numAttributes()-1; j++){
                 if(defaultSet.attribute(i).name().equalsIgnoreCase(data.attribute(j).name())){
                    //misc.log(defaultSet.attribute(i).name()+"="+data.attribute(j).name()+": "+data.firstInstance().value(j));
                     f.setValue(defaultSet.attribute(i), data.firstInstance().value(j));
                    //f.setValueSparse(i, data.firstInstance().value(j));
                     mFound = true;
                 }
             }
             if(mFound == false){
                 f.setValue(defaultSet.attribute(i), 0.0);
             }
         }
         }catch(Exception e){
             misc.log(e.toString()+"; "+i+"("+defaultSet.numAttributes()+"), "+j+"("+data.numAttributes()+")");
             e.printStackTrace();
         }
         finalSet.add(f);
         
         if(f.numValues()>0)        
             return 0;
         else
             return 1;
     }
     
     public Instances getFinal() throws Exception{
         NonSparseToSparse nonSparseToSparseInstance = new NonSparseToSparse(); 
         nonSparseToSparseInstance.setInputFormat(finalSet); 
         Instances sparseDataset = Filter.useFilter(finalSet, nonSparseToSparseInstance);
         finalSet = sparseDataset;
         if(finalSet == null){
             throw new Exception("No finished dataset.");
         }
         return finalSet;
         
     }
     
 }
