 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.apacheextras.camel.examples.rcode.processor;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author cemmersb
  */
 public class MonthlySalesFigureCalcProcessor implements Processor {
 
   private final static Logger LOGGER = LoggerFactory.getLogger(MonthlySalesFigureCalcProcessor.class);
   public final Map<String, Map<String, String>> dailySalesCalendar =
      new LinkedHashMap<String, Map<String, String>>();
 
   @Override
   public void process(Exchange exchng) throws Exception {
    final List<ArrayList> salesDay = exchng.getIn().getBody(ArrayList.class);
    for (List<String> salesDate : salesDay) {
       LOGGER.debug("Sales date: {} and value: {}", salesDate.get(0), salesDate.get(1));
      setSalesValue(getMonthAndYearOfDate(salesDate.get(0)), salesDate.get(0), salesDate.get(1));
     }
    String rVector = toRVector(summarizeMonthlyValues());
     exchng.getIn().setBody(rVector);
   }
 
   private String toRVector(int[] monthlySales) {
     final StringBuilder rVector = new StringBuilder();
     for (int i = 0; i < monthlySales.length; i++) {
       rVector.append(monthlySales[i]);
       if ((i + 1) != monthlySales.length) {
         rVector.append(',');
       }
     }
     return rVector.toString();
   }
 
   private int[] summarizeMonthlyValues() {
    
     final Set<String> monthAndYears = dailySalesCalendar.keySet();
    
     int[] sum = new int[monthAndYears.size()];
     int i = 0;
     for (String monthAndYear : monthAndYears) {
      for(String value : dailySalesCalendar.get(monthAndYear).values()) {
         sum[i] = sum[i] + Integer.parseInt(value);
       }
       i++;
     }
     return sum;
   }
 
   private void setSalesValue(String monthAndYear, String date, String value) throws Exception {
     if (null == dailySalesCalendar.get(monthAndYear)) {
       // Initialize the month if not available
       dailySalesCalendar.put(monthAndYear, new LinkedHashMap<String, String>());
     } else {
       if (null != dailySalesCalendar.get(monthAndYear).get(date)) {
         // Remove the existing value to replace with the lates information
         dailySalesCalendar.get(monthAndYear).remove(date);
       }
     }
     // Set the date to the calendar map
     dailySalesCalendar.get(monthAndYear).put(date, value);
   }
 
   private String getMonthAndYearOfDate(String strDate) {
     LOGGER.trace("Mapping date '{}' to the apropriate month.", strDate);
     final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
     final Calendar cal = Calendar.getInstance();
 
     Date date;
     int month = -1;
     int year = -1;
 
     try {
       date = sdf.parse(strDate);
       cal.setTime(date);
       month = cal.get(Calendar.MONTH);
       year = cal.get(Calendar.YEAR);
     } catch (ParseException ex) {
       LOGGER.error("Could not parse the given date: {}", ex);
     }
     final String monthAndYear = new StringBuilder().append(year).append('-').append(month).toString();
     return monthAndYear;
   }
 }
