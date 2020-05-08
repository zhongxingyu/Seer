 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.meteorologaaguascalientes.control.historic;
 
 import java.util.*;
import org.apache.commons.math3.exception.NoDataException;
 import org.meteorologaaguascalientes.control.forecast.DefaultForecastImpl;
 import org.meteorologaaguascalientes.dao.AbstractVariableDao;
 import org.meteorologaaguascalientes.model.Variable;
 
 /**
  *
  * @author juan
  */
 public class HistoricControl {
     
     public List<SortedMap<Date, Double>> getData(AbstractVariableDao variableDao) {
         
         List<SortedMap<Date, Double>> data = new ArrayList<SortedMap<Date, Double>>();
         
         SortedMap<Date, Double> actualValues = new TreeMap<Date, Double>();
         SortedMap<Date, Double> forecast = new TreeMap<Date, Double>();
         SortedMap<Long, Double> timeSpacedActualValues = new TreeMap<Long, Double>();
         SortedMap<Long, Double> timeSpacedForecast;
         
         data.add(actualValues);
         
         ArrayList<Variable> variables = (ArrayList<Variable>) variableDao.getAllValues();
         
         for (Variable v : variables) {
             actualValues.put(v.getTime(), v.getValue());
         }
         try{
             Long firstSampleTime = actualValues.firstKey().getTime();
             for (Date d : actualValues.keySet()) {
                 timeSpacedActualValues.put(d.getTime() - firstSampleTime, actualValues.get(d));
             }
            if(actualValues.size()>2){
                timeSpacedForecast = new DefaultForecastImpl().forecast(10, timeSpacedActualValues);
                for (Long l:timeSpacedForecast.keySet()){
                    forecast.put(new Date(l + firstSampleTime), timeSpacedForecast.get(l));
                }
             }
         }catch(NoSuchElementException e){
         }
         data.add(forecast);
         
         return data;
     }
 }
