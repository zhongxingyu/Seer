 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  *
  * @author Sylwia Wnek
  */
 class UpdateMeasurement{
    static ArrayList <Integer> ids;
     ArrayList<ComplexMeasurement> complex = new ArrayList();
 
 /**
  * Pobiera dane z pomiarow prostych w MeasurementContainer do pomiarow zlozonych w MeasurementContainer.
  */
     public static void collectData(){
         ids = MeasurementContainer.getIDs(); 
         String simpleName;
         int id;
         
         Measurement currentMeasurement;
         SimpleMeasurement simple;
         ComplexMeasurement currentComplex;
         
         
         ArrayList<Data> data;
         Data d;
         
         for (int i=0;i<ids.size();i++){
             currentMeasurement = MeasurementContainer.getById(ids.get(i));
             if (currentMeasurement.getClass().equals(ComplexMeasurement.class)){
                 id = ids.get(i);
                 currentComplex = (ComplexMeasurement)currentMeasurement;
                 simpleName = currentComplex.getSimpleName();
                 simple = (SimpleMeasurement)MeasurementContainer.getByName(simpleName);
                 data = simple.getData();
 
                 d = data.get(data.size()-1);
                 currentComplex.addData((new Date()).getTime(),d.getData()); 
             }
 
         }
     }
     
     /**
      * Ustawia nowe wyniki pomiarow zlozonych w MeasurementContainer.
      */
     public static void updateResults(){
         int newResult;
         int id;
         
         Measurement currentMeasurement;        
         ComplexMeasurement currentComplex;
         
         for (int i=0;i<ids.size();i++){
             currentMeasurement = MeasurementContainer.getById(ids.get(i));
             if (currentMeasurement.getClass().equals(ComplexMeasurement.class)){
                 id = ids.get(i);
                 currentComplex = (ComplexMeasurement)currentMeasurement;
                 currentComplex.doOperation();
             }
         }             
     }
         
     public void createComplex(String name){ 
         ComplexMeasurement c1;
         SimpleMeasurement sm;
         
         for (int i=0;i<ids.size();i++){
             if (MeasurementContainer.getById(ids.get(i)).getClass().equals(SimpleMeasurement.class)){
                 sm = (SimpleMeasurement)(MeasurementContainer.getById(ids.get(i)));
                 c1 = new ComplexMeasurement(sm.getName());
                 
                 MeasurementContainer.addMeasurement(c1);
                 
             }
         }
     }
         
 }
