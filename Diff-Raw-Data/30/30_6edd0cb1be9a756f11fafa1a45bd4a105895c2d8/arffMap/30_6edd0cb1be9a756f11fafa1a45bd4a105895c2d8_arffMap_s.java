 import weka.core.Attribute;
 import weka.core.FastVector;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.SparseInstance;
 import weka.core.converters.ArffSaver;
 import weka.core.converters.ArffLoader.*;
 import java.io.*;
 
 public class arffMap{
     
     Instances defaultSet, finalSet;
     
     public arffMap(){}
 
     public int loadStructure(){
 		int errorCode = 1;
         String filename = "arffMap.arff";
 		try
         {
             FileReader reader = new FileReader(filename);
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
             for(j=0; j<data.numAttributes()-1; j++){
                 if(defaultSet.attribute(i).name().equalsIgnoreCase(data.attribute(j).name())){
                    misc.log(defaultSet.attribute(i).name()+"="+data.attribute(j).name()+": "+data.firstInstance().value(j));
                    //misc.log(Double.toString(data.firstInstance().value(j)));
                     f.setValue(defaultSet.attribute(i), data.firstInstance().value(j));
                     //f.setValueSparse(i, data.firstInstance().value(j));
                 }
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
         if(finalSet == null){
             throw new Exception("No finished dataset.");
         }
         return finalSet;
         
     }
     
 }
