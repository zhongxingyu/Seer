 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ua.edu.lnu.cluster.interpreters;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.openide.util.lookup.ServiceProvider;
 import ua.edu.lnu.cluster.DataInterpreter;
 
 /**
  *
  * @author pif
  */
 @ServiceProvider(service=DataInterpreter.class)
 public class CategoryInterpreter extends  DataInterpreter{
 
     private Map<String, Double> dictionary = new HashMap<String, Double>();
     
     @Override
     public double convertValue(String value) {
         if (dictionary.containsKey(value)) {
             return dictionary.get(value);
         } else {
            return Double.NaN;
         }
     }
 
     @Override
     public String getName() {
             return "Categories";
     }
 
     @Override
     public void preprocessData(List<String> data) {
         dictionary.clear();
         for (String string : data) {
             if (dictionary.containsKey(string)) {
                 dictionary.put(string, dictionary.get(string)+1d);
             } else {
                 dictionary.put(string, 1d);
             }
         }
     }
     
 }
