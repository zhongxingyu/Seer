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
 
 
 package org.investovator.dataplaybackengine.player;
 
 import org.investovator.core.commons.utils.Portfolio;
 import org.investovator.core.commons.utils.PortfolioImpl;
 import org.investovator.core.data.api.CompanyData;
 import org.investovator.core.data.api.CompanyStockTransactionsData;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 import org.investovator.core.data.exeptions.DataAccessException;
 import org.investovator.dataplaybackengine.data.BogusCompnayDataGenerator;
 import org.investovator.dataplaybackengine.data.BogusHistoryDataGenerator;
 import org.investovator.dataplaybackengine.events.PlaybackEventListener;
 import org.investovator.dataplaybackengine.exceptions.InvalidOrderException;
 import org.investovator.dataplaybackengine.exceptions.UserAlreadyJoinedException;
 import org.investovator.dataplaybackengine.exceptions.UserJoinException;
 import org.investovator.dataplaybackengine.market.OrderType;
 import org.investovator.dataplaybackengine.market.TradingSystem;
 import org.investovator.dataplaybackengine.scheduler.RealTimeEventTask;
 
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
     RealTimeEventTask task;
     CompanyStockTransactionsData transactionDataAPI;
     CompanyData companyDataAPI;
     HashMap<String,Portfolio> userPortfolios;
     TradingSystem tradingSystem;
 
     private boolean isMultiplayer;
 
     private RealTimeDataPlayer(String[] stocks,ArrayList<TradingDataAttribute> attributes,
                                TradingDataAttribute attributeToMatch,
                                boolean isMultiplayer) {
         this.timer = new Timer();
         userPortfolios=new HashMap<String, Portfolio>();
         tradingSystem=new TradingSystem(attributes,attributeToMatch);
         this.isMultiplayer=isMultiplayer;
         //for testing
         this.transactionDataAPI =new BogusHistoryDataGenerator();
         this.companyDataAPI=new BogusCompnayDataGenerator();
         //testing end
 
     }
 
     public RealTimeDataPlayer(String[] stocks,String startDate,
                               String dateFormat,ArrayList<TradingDataAttribute> attributes,
                               TradingDataAttribute attributeToMatch,boolean isMultiplayer) {
         this(stocks,attributes,attributeToMatch,isMultiplayer);
 
         task = new RealTimeEventTask(stocks, startDate,dateFormat, transactionDataAPI,attributes);
 
         //set the trading system as an observer
         task.setObserver(this.tradingSystem);
     }
 
     public RealTimeDataPlayer(String[] stocks,Date startDate,ArrayList<TradingDataAttribute> attributes,
                               TradingDataAttribute attributeToMatch,boolean isMultiplayer) {
         this(stocks,attributes,attributeToMatch,isMultiplayer);
 
         task = new RealTimeEventTask(stocks, startDate, transactionDataAPI,attributes);
 
         //set the trading system as an observer
         task.setObserver(this.tradingSystem);
     }
 
 
     /**
      * To set observers
      *
      * @param observer
      */
     private void setObserver(PlaybackEventListener observer){
         task.setObserver(observer);
     }
 
     /**
      * Start playing the data
      * @param resolution the time gaps between pushing events
      */
     public void startPlayback(int resolution) {
 
         timer.schedule(task, 0, resolution * 1000);
         //TODO- change the RealTimeEventTask to check for the resolution when incrementing its time
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
     public boolean joinGame(PlaybackEventListener observer,String userName) throws UserAlreadyJoinedException,
             UserJoinException {
 
         //if a non-admin user tries to connect to a single player game
         //todo - implement using authenticator
 //        if(!isMultiplayer && user!=admin){
 //            throw new UserJoinException("You don't have sufficient privileges to enter this game");
 //        }
 
         boolean joined=false;
 
         //check whether the user has already joined the game
         if(!userPortfolios.containsKey(userName)){
             userPortfolios.put(userName,new PortfolioImpl(userName,RealTimeDataPlayer.initialCredit,0));
             joined=true;
             setObserver(observer);
         }
         else{
 //            throw new UserAlreadyJoinedException(userName);
             //todo - remove the fpllowing line
             setObserver(observer);
         }
 
 
         return joined;
 
     }
 
 
     public boolean executeOrder(String stockId, int quantity, OrderType side,String userName) throws InvalidOrderException,
             UserJoinException {
 
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
 
     public Portfolio getMyPortfolio(String userName) throws UserJoinException {
 
         //if the user has not joined the game
         if(!userPortfolios.containsKey(userName)){
             throw new UserJoinException("User "+userName+ " has not joined the game");
 
         }
 
         return userPortfolios.get(userName);
     }
 
 
     public void setTransactionDataAPI(CompanyStockTransactionsData api ) {
         task.setDataApi(api);
     }
 
     /**
      * returns whether this is a multplayer game or not
      * @return
      */
     public boolean isMultiplayer() {
         return isMultiplayer;
     }
 
     /**
      * Returns the current time in the playback
      * @return
      */
     public Date getCurrentTime(){
         return task.getCurrentTime();
     }
 
    public CompanyStockTransactionsData getTransactionDataAPI() {
         return transactionDataAPI;
     }
 }
