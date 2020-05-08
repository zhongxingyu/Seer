 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.brown.cs32.atian.crassus.backend;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author lyzhang
  */
 public interface Stock {
     //void setTicker(String ticker);
 
     boolean initialize();   // false mean it fails to get data from data source
 
     void addEvent(Indicator event);
 
     String getTicker();
 
     String getCompanyName();
 
 //   StockHistData getStockHistData(String freq);   // freq = "daily" or "monthly" or "weekly"
 
     StockRealTimeData getStockRealTimeData();
 
     ArrayList<Indicator> getEventList();
 
 //    Indicator getEvent(String eventName);
 
     double getWeek52Low();
     double getWeek52High();    
     
     void refresh();

	void deleteEvent(Indicator event);
 }
