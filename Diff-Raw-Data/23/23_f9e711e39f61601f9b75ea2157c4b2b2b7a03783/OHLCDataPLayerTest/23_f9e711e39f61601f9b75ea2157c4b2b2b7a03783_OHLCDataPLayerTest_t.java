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
 
 
 package org.investovator.dataplaybackengine;
 
import org.investovator.core.data.api.CompanyStockTransactionsData;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 import org.investovator.dataplaybackengine.events.StockEvent;
 import org.investovator.dataplaybackengine.utils.DateUtils;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Date;
 
 /**
  * @author: ishan
  * @version: ${Revision}
  */
 public class OHLCDataPLayerTest {
 
     OHLCDataPLayer player;
 
 
     @Before
     public void setUp() throws Exception {
 
         String[] stocks=new String[2];
         stocks[0]="GOOG";
         stocks[1]="APPL";
         String startDate="2011-12-13-15-55-32";
 
         //define the attributes needed
         TradingDataAttribute attributes[]=new TradingDataAttribute[2];
 
         //just the closing price is enough for now
         attributes[0]=TradingDataAttribute.DAY;
         attributes[1]=TradingDataAttribute.PRICE;
 
         player=new OHLCDataPLayer(stocks,attributes);
 
         //set the date
         player.setStartDate(startDate,DateUtils.DATE_FORMAT_1);
 
 
     }
 
     @Test
     public void testStartGame() throws Exception {
 
     }
 
     @Test
     public void testSetStartDate() throws Exception {
 
         StockEvent[] events=player.startGame();
 
         for(int i=0;i<5;i++){
 
             for(StockEvent event:events){
                 assert(player.getToday().equals(event.getTime()));
                 System.out.println(player.getToday()+" --> "+event.getStockId()+" : ");
                 for(TradingDataAttribute attr:event.getData().keySet()){
                     System.out.println(event.getData().get(attr));
                 }
 
                 Thread.sleep(100);
 
 
 
             }
         }
 
 
 
     }
 
     @Test
     public void testPlayNextDay() throws Exception {
 
     }
 
     @Test
     public void testGetStocksList() throws Exception {
 
     }
 
     @Test
     public void testGetCommonStartingAndEndDates() throws Exception{
 
         String[] stocks=new String[2];
         stocks[0]="GOOG";
         stocks[1]="APPL";
 
        Date[] dates=player.getCommonStartingAndEndDates(stocks, CompanyStockTransactionsData.DataType.OHLC);
 
 
         System.out.println(dates[0]+"--->"+dates[1]);
 
 
     }
 
     @Test
     public void testGetStartingAndEndDates() throws Exception{
 
 
         String[] stocks=new String[2];
         stocks[0]="GOOG";
         stocks[1]="APPL";
 
        Date[] dates=player.getStartingAndEndDates(stocks, CompanyStockTransactionsData.DataType.OHLC);
 
         System.out.println(dates[0]+"--->"+dates[1]);
 
     }
 }
