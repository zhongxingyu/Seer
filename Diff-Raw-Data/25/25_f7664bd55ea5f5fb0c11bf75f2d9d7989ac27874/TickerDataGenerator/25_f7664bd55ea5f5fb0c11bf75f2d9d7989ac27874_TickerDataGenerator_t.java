 /*
  * investovator, Stock Market Gaming framework
  * Copyright (C) 2013  investovator
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.investovator.core.commons.utils.testutils;
 
 import au.com.bytecode.opencsv.CSVWriter;
 import org.investovator.core.data.api.CompanyStockTransactionsData;
 import org.investovator.core.data.api.CompanyStockTransactionsDataImpl;
 import org.investovator.core.data.api.utils.StockTradingData;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 import org.investovator.core.data.exeptions.DataAccessException;
 import org.investovator.core.data.exeptions.DataNotFoundException;
 
import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * @author Amila Surendra
  * @version $Revision
  */
 public class TickerDataGenerator {
 
     private String stockID;
     private double priceVariance;
     private int tradesVariance;
     private String outputFilePath;
     private String dateFormat;
     private Random randomGenerator;
     private Date startDate = null;
     private Date endDate = null;
 
     private DateFormat dateFormater;
 
 
     //CSV Headings
     String[] columns = {
             TradingDataAttribute.getAttribName(TradingDataAttribute.TIME),
             TradingDataAttribute.getAttribName(TradingDataAttribute.PRICE),
             TradingDataAttribute.getAttribName(TradingDataAttribute.SHARES),
     };
 
     public TickerDataGenerator(String stockID) {
         this.stockID = stockID;
 
         //Creating Default Settings
         priceVariance = 1.5;
         tradesVariance =10;
        outputFilePath = System.getProperty("user.home") + File.separator + stockID+".csv";
         dateFormat = "MM/dd/yyyy HH:mm:ss.SSS";
         randomGenerator = new Random(new Date().getTime());
         dateFormater = new SimpleDateFormat(dateFormat);
     }
 
     public String getStockID() {
         return stockID;
     }
 
     public void setStockID(String stockID) {
         this.stockID = stockID;
     }
 
     public double getPriceVariance() {
         return priceVariance;
     }
 
     public void setPriceVariance(double priceVariance) {
         this.priceVariance = priceVariance;
     }
 
     public String getOutputFilePath() {
         return outputFilePath;
     }
 
     public void setOutputFilePath(String outputFilePath) {
         this.outputFilePath = outputFilePath;
     }
 
 
     /**
      * Create CSV file for the given stock
      * @return output file path if success null if fails.
      */
     public String CreateCSV(){
 
         try {
            File outPutFile = new File(outputFilePath);
            outPutFile.createNewFile();

            CSVWriter csvWriter = new CSVWriter(new FileWriter(outPutFile));
             csvWriter.writeNext(columns);
             createDataRows(csvWriter);
             csvWriter.flush();
             csvWriter.close();
 
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return outputFilePath;
 
     }
 
     private void createDataRows(CSVWriter writer){
 
         try {
 
             CompanyStockTransactionsData historyData = new CompanyStockTransactionsDataImpl();
 
             Date[] dates = historyData.getDataDaysRange(CompanyStockTransactionsData.DataType.OHLC,stockID);
 
             ArrayList<TradingDataAttribute> dataAttribs = new ArrayList<>();
             dataAttribs.add(TradingDataAttribute.DAY);
             dataAttribs.add(TradingDataAttribute.CLOSING_PRICE);
             dataAttribs.add(TradingDataAttribute.TRADES);
             dataAttribs.add(TradingDataAttribute.SHARES);
 
             StockTradingData tradingData = historyData.getTradingData(CompanyStockTransactionsData.DataType.OHLC, stockID, dates[0], dates[1], Integer.MAX_VALUE,dataAttribs) ;
 
             HashMap<Date, HashMap<TradingDataAttribute, String>>  allTradingData  = tradingData.getTradingData();
 
             for(Date date : allTradingData.keySet()){
 
                 HashMap<TradingDataAttribute,String> oneDayData = allTradingData.get(date);

                String tradesString = oneDayData.get(TradingDataAttribute.TRADES);
                String sharesString = oneDayData.get(TradingDataAttribute.SHARES);
                String closingString = oneDayData.get(TradingDataAttribute.CLOSING_PRICE);

                if("".equals(tradesString) || "".equals(sharesString) || "".equals(closingString)) continue;

                int trades = Integer.parseInt(tradesString);
                int shares = Integer.parseInt(sharesString);
                double closingPrice = Double.parseDouble(closingString);

 
                 ArrayList<String[]> day = getSingleDay(date, trades ,shares,closingPrice );
                 for(String[] line : day) {
                     writer.writeNext(line);
                 }
             }
 
 
         } catch (DataAccessException e) {
             e.printStackTrace();
         } catch (DataNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     private ArrayList<String[]> getSingleDay(Date day, int tradesPerDay, int sharesPerDay, double avgPrice){
 
         tradesPerDay = tradesPerDay / 1;
 
         ArrayList<String[]> dayData = new ArrayList<>();
         long randomTimes[] = new long[tradesPerDay];
 
         int addedTrades;
         Date time;
         double price;
         int shares;
 
 
         Calendar endTime =  Calendar.getInstance();
         endTime.setTime(day);
         endTime.add(Calendar.DATE, 1);
 
         long minDate = day.getTime();
         long maxDate = endTime.getTime().getTime();
 
         for (int i = 0; i < tradesPerDay; i++) {
             randomTimes[i] = minDate + (long)randomGenerator.nextDouble()*(maxDate-minDate);
         }
 
         Arrays.sort(randomTimes);
 
         for (addedTrades = 0; addedTrades < tradesPerDay; addedTrades++) {
 
             time = new Date(randomTimes[addedTrades]);
             price = avgPrice + (randomGenerator.nextDouble()-0.5)*priceVariance ;
             shares = sharesPerDay/tradesPerDay + (int)(randomGenerator.nextDouble()-0.5)*tradesVariance ;
 
             dayData.add( new String[]{dateFormater.format(time), Double.toString(price), Integer.toString(shares)});
 
         }
 
         return dayData;
     }
 
 
 }
