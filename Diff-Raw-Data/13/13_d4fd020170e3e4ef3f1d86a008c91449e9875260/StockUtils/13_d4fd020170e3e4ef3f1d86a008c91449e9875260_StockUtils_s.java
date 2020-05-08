 /*
  * investovator, Stock Market Gaming Framework
  *     Copyright (C) 2013  investovator
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package org.investovator.dataplaybackengine.utils;
 
 import org.investovator.core.data.api.CompanyData;
 import org.investovator.core.data.api.CompanyStockTransactionsData;
 import org.investovator.core.data.exeptions.DataAccessException;
 import org.investovator.dataplaybackengine.data.BogusCompnayDataGenerator;
 import org.investovator.dataplaybackengine.data.BogusHistoryDataGenerator;
 
 import java.util.*;
 
 /**
  * @author: ishan
  * @version: ${Revision}
  */
 public class StockUtils {
 
    private static CompanyStockTransactionsData transactionDataAPI;
    private static CompanyData companyDataAPI;
 
     public StockUtils() {
        //for testing
        this.transactionDataAPI = new BogusHistoryDataGenerator();
        this.companyDataAPI = new BogusCompnayDataGenerator();
        //testing end
     }
 
     /**
      * Returns the starting date/time and the ending date/time which has data for all the given set of stocks
      *
      * @param stocks
      * @return First element contains start date, second element contains the ending date,
      * a null may be returned for any of the dates if no common date is found
      */
     public static Date[] getCommonStartingAndEndDates(String[] stocks, CompanyStockTransactionsData.DataType type) throws DataAccessException {
         Date startDate=null;
         Date endDate=null;
 
         //Date(in order) - [stocks]
         TreeMap<Date,ArrayList<String>> counter=new TreeMap<Date, ArrayList<String>>();
 
         //iterate all the stocks
         for(String stock:stocks){
             //get all the dates for that stock
             Date[] dates=transactionDataAPI.getDataDaysRange(type,stock);
 
             //add them to the map
             for(Date date:dates){
                 //if the arraylist has not been initialized
                 if(!counter.containsKey(date)){
                     counter.put(date,new ArrayList<String>());
                 }
 
                 ArrayList<String> stockList=counter.get(date);
                 stockList.add(stock);
                 counter.put(date,stockList);
             }
         }
 
         //iterate the map in the ascending order and determine the largest date which has all the stocks
         for(Date date:counter.keySet()){
             if(counter.get(date).size()==stocks.length){
                 startDate=date;
                 break;
             }
         }
 
         //reverse order the collection first
         Comparator cmp = Collections.reverseOrder();
         TreeMap<Date,ArrayList<String>> reverseOrderedMap=new TreeMap<Date,ArrayList<String>>(cmp);
         reverseOrderedMap.putAll(counter);
 
         //iterate the map in the descending order and determine the biggest date which has all the stocks
         for(Date date:reverseOrderedMap.keySet()){
             if(reverseOrderedMap.get(date).size()==stocks.length){
                 endDate=date;
                 break;
             }
         }
 
         //return the array
         return new Date[] {startDate,endDate};
 
     }
 
     /**
      *Returns the starting date/time and the ending date/time for the given set of stocks. Those dates does
      *not necessarily need to contain values for every stock
      *
      * @param stocks
      * @return First element contains start date, second element contains the ending date
      */
     public static Date[] getStartingAndEndDates(String[] stocks, CompanyStockTransactionsData.DataType type) throws DataAccessException {
 
         //to store all the date
         List<Date> datesList=new ArrayList<Date>();
 
         //iterate all the stocks
         for(String stock:stocks){
             //get all the dates for that stock
             Date[] dates=transactionDataAPI.getDataDaysRange(type,stock);
 
             //add them to the map
             datesList.addAll(Arrays.asList(dates));
         }
 
         //sort in the ascending order
         Collections.sort(datesList);
 
         return  new Date[] {datesList.get(0),datesList.get(datesList.size()-1)};
 
 
     }
 
     /**
      * @return Company StockId and Name pairs
      * @throws org.investovator.core.data.exeptions.DataAccessException
      */
     public static HashMap<String, String> getStocksList() throws DataAccessException {
 
         return companyDataAPI.getCompanyIDsNames();
 
 
     }
 }
