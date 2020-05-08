 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ua.edu.lnu.cluster;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
import ua.edu.lnu.cluster.interpreters.StringInterpreter;
 
 /**
  * class which represents clustering data
  * @author pif
  */
 public class DataModel {
 
     public static final String PROP_NAME = "name";
     public static final String PROP_DATA = "dataChange";
     private List<DataColumn> dataColumns = new ArrayList<DataColumn>();
     private List<Observation> observations = new ArrayList<Observation>();
     private String name = "Dataset";
    private List listeners = Collections.synchronizedList(new LinkedList());
 
     public DataModel(List<String[]> rawData, List<String> headers) {
         if (!rawData.isEmpty()) {
             String[] observation = rawData.get(0);
             int featureCount = observation.length;
 
             for (int i = 0; i < featureCount; i++) {
                 DataColumn column = new DataColumn();
                 column.setName(headers.get(i));
                 
                 dataColumns.add(column);
             }
 
             for (int i = 0; i < rawData.size(); i++) {
                 String[] chunks = rawData.get(i);
                 for (int j = 0; j < chunks.length; j++) {
                     dataColumns.get(j).addData(chunks[j]);
                 }
 
                 observations.add(new Observation(chunks));
             }
 
 
         }
     }
 
     public DataModel() {
     }
 
     public int getObservationCount() {
         return observations.size();
     }
 
     public int getFeaturesCount() {
         return dataColumns.size();
     }
 
     public void removeObservation(int id) {
         for (DataColumn dataField : dataColumns) {
             dataField.removeData(id);
         }
         observations.remove(id);
 
         fire(PROP_DATA, null, null);
     }
 
 //    public void addObservation(Object[] observation) {
 //        if (dataColumns.size() != observation.length) {
 //            throw new RuntimeException("Observation has wrong number of fields.");
 //        }
 //        
 //        for (int i = 0; i < observation.length; i++) {
 //            dataColumns.get(i).addData(observation[i]);
 //        }
 //        
 //        observations.add(new Observation(observation));
 //    }
     public Observation getObservation(int index) {
         return observations.get(index);
     }
     
     public DataColumn getDataColumn(int index) {
         return dataColumns.get(index);
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name == null ? "" : name;
     }
 
     public void addPropertyChangeListener(PropertyChangeListener pcl) {
         listeners.add(pcl);
     }
 
     public void removePropertyChangeListener(PropertyChangeListener pcl) {
         listeners.remove(pcl);
     }
 
     private void fire(String propertyName, Object old, Object nue) {
         for (Object object : listeners) {
             PropertyChangeListener pcl = (PropertyChangeListener) object;
             pcl.propertyChange(new PropertyChangeEvent(this, propertyName, old, nue));
         }
     }
 }
