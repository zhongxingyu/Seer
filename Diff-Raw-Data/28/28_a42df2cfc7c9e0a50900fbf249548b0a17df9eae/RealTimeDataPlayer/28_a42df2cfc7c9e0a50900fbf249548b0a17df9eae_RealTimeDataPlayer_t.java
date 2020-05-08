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
 
 import org.investovator.core.commons.utils.Portfolio;
 import org.investovator.core.commons.utils.PortfolioImpl;
 import org.investovator.core.data.api.CompanyData;
 import org.investovator.core.data.api.CompanyStockTransactionsData;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 import org.investovator.core.data.exeptions.DataAccessException;
 import org.investovator.dataplaybackengine.data.BogusCompnayDataGenerator;
 import org.investovator.dataplaybackengine.data.BogusHistoryDataGenerator;
 import org.investovator.dataplaybackengine.exceptions.InvalidOrderException;
 import org.investovator.dataplaybackengine.exceptions.UserAlreadyJoinedException;
 import org.investovator.dataplaybackengine.exceptions.UserJoinException;
 import org.investovator.dataplaybackengine.market.OrderType;
 import org.investovator.dataplaybackengine.market.TradingSystem;
 import org.investovator.dataplaybackengine.scheduler.EventTask;
 
 import java.util.*;
 
 /**
  * @author: ishan
  * @version: ${Revision}
  */
 public class RealTimeDataPlayer extends DataPlayer {
 
     //amount of money a person get at the begining
     private static int initialCredit=10000;
 
     //max amount of stocks that a person can buy/sell
     private static int maxOrderSize=5000;
 
     Timer timer;
     EventTask task;
     CompanyStockTransactionsData transactionDataAPI;
     CompanyData companyDataAPI;
     HashMap<String,Portfolio> userPortfolios;
     TradingSystem tradingSystem;
 
     private RealTimeDataPlayer(String[] stocks,ArrayList<TradingDataAttribute> attributes,TradingDataAttribute attributeToMatch) {
         this.timer = new Timer();
         userPortfolios=new HashMap<String, Portfolio>();
         tradingSystem=new TradingSystem(attributes,attributeToMatch);
         //for testing
         this.transactionDataAPI =new BogusHistoryDataGenerator();
         this.companyDataAPI=new BogusCompnayDataGenerator();
         //testing end
 
     }
 
     public RealTimeDataPlayer(String[] stocks,String startDate,
                               String dateFormat,ArrayList<TradingDataAttribute> attributes,TradingDataAttribute attributeToMatch) {
         this(stocks,attributes,attributeToMatch);
 
         task = new EventTask(stocks, startDate,dateFormat, transactionDataAPI,attributes);

        //set the trading system as an observer
        task.setObserver(this.tradingSystem);
     }
 
     public RealTimeDataPlayer(String[] stocks,Date startDate,ArrayList<TradingDataAttribute> attributes,
                               TradingDataAttribute attributeToMatch) {
         this(stocks,attributes,attributeToMatch);
 
         task = new EventTask(stocks, startDate, transactionDataAPI,attributes);

        //set the trading system as an observer
        task.setObserver(this.tradingSystem);
     }
 
 
     /**
      * To set observers
      *
      * @param observer
      */
     public void setObserver(Observer observer){
         task.setObserver(observer);
     }
 
     /**
      * Start playing the data
      * @param resolution the time gaps between pushing events
      */
     public void startPlayback(int resolution) {
 
         timer.schedule(task, 0, resolution * 1000);
         //TODO- change the EventTask to check for the resolution when incrementing its time
     }
 
     /**
      * Stop the data playback
      */
     public void stopPlayback() {
         task.cancel();
         timer.cancel();
     }
 
     /**
      *
      * @return Company StockId and Name pairs
      * @throws org.investovator.core.data.exeptions.DataAccessException
      */
     public HashMap<String,String> getStocksList() throws DataAccessException {
 
         return companyDataAPI.getCompanyIDsNames();
 
 
     }
 
     /**
      * Allows a user to join the running game
      *
      * @return
      */
     public boolean joinGame() throws UserAlreadyJoinedException {
         //todo -get from Authenticator
         String userName="test";
 
         boolean joined=false;
 
         //check whether the user has already joined the game
         if(!userPortfolios.containsKey(userName)){
             userPortfolios.put(userName,new PortfolioImpl(userName,RealTimeDataPlayer.initialCredit,
                     new HashMap<String, HashMap<String, Float>>()));
             joined=true;
         }
         else{
             throw new UserAlreadyJoinedException(userName);
         }
 
 
         return joined;
 
     }
 
 
     public boolean executeOrder(String stockId, int quantity, OrderType side) throws InvalidOrderException,
             UserJoinException {
         //todo -get from Authenticator
         String userName="test";
 
         //order validity checks
         if(quantity>RealTimeDataPlayer.maxOrderSize){
             throw new InvalidOrderException("Cannot place more than "+RealTimeDataPlayer.maxOrderSize+" orders.");
         }
         if(!task.getStocks().contains(stockId)){
             throw new InvalidOrderException("Invalid stock ID : "+stockId);
         }
 
         //if the user has not joined the game
         if(!userPortfolios.containsKey(userName)){
             throw new UserJoinException("User "+userName+ " has not joined the game");
 
         }
 
 
         float executedPrice= tradingSystem.executeOrder(stockId,quantity,userPortfolios.get(userName).getCashBalance());
 
         //update the cash balance
         if(side==OrderType.BUY){
             userPortfolios.get(userName).boughtShares(stockId,quantity,executedPrice);
         }
         else if(side==OrderType.SELL){
             userPortfolios.get(userName).soldShares(stockId,quantity,executedPrice);
         }
         return true;
 
 
     }
 
 
 }
