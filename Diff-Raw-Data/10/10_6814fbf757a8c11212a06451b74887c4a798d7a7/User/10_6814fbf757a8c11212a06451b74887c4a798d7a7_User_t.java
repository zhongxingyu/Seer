 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.server;
 
 import com.api.UserAPI;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 
 /**
  *
  * @author w
  */
 public class User implements UserAPI {
     private final double INITIAL_BALANCE = 1000;
     private String userName;
     private double balance;
     private ArrayList<StockExchange> sEList = new ArrayList();
 
     public User() {
     }
 
     public User(String userName) {
         this.userName = userName;
         this.balance = this.INITIAL_BALANCE; 
     }
     
     /**
      *
      * @param userName
      * @param balance
      * @param sEList
      */
     public User(String userName, double balance, ArrayList<StockExchange> sEList) {
         this.userName = userName;
         this.balance = balance;
         this.sEList = sEList;
     }
 
     /**
      * @return the userName
      */
     public String getUserName() {
         return userName;
     }
 
     /**
      * @param userName the userName to set
      */
     public void setUserName(String userName) {
         this.userName = userName;
     }
 
     /**
      * @return the balance
      */
     public double getBalance() {
         return balance;
     }
 
     /**
      * @param balance the balance to set
      */
     public void setBalance(double balance) {
         this.balance = balance;
     }
 
     /**
      *
      * @param ticker_name
      * @return
      */
     public StockExchange fetchStock(String ticker_name) {
         for (int i = 0; i < sEList.size(); i++) {
             if ((sEList.get(i)).getTickerName().equals(ticker_name)) {
                 return sEList.get(i);
             }
         }
         return null;
     }
 
     /**
      * Get the available shares in the market for the specific stock.
      *
      * @param ticker_name
      * @return int Share
      * @return -1 Cannot find the stock in the market
      *
      */
     public int getAvailableShares(String ticker_name) {
         StockExchange stockEx = StockList.getStockbyName(ticker_name);
         if (stockEx==null) {
             return -1;
         } else {
             return stockEx.getShare();
         }
     }
 
     /**
      * Get the market price for the specific stock.
      *
      * @param ticker_name
      * @return double price
      * @return -1 Cannot find the stock in the market
      *
      */
     public double getMarketPrice(String ticker_name) {
         StockExchange stockEx = StockList.getStockbyName(ticker_name);
         if (stockEx==null) {
             return -1.0;
         } else {
             return stockEx.getPrice();
         }
     }
 
     /**
      * Get the shared number for the specific stock.
      *
      * @param ticker_name
      * @return int shared number for the specific stock
      * @return -1 Cannot find the stock in the market
      *
      */
     public int getNumShare(String ticker_name) {
         for (int i = 0; i < sEList.size(); i++) {
             if ((sEList.get(i)).getTickerName().equals(ticker_name)) {
                 return sEList.get(i).getShare();
             }
         }
         return -1;
     }
 
     /**
      * Return the User's personal stock list.
      *
      * @return
      */
     public ArrayList<StockExchange> getStockListofUser() {
         return sEList;
     }
 
     /**
      *
      * @param ticker_name
      * @param num_stocks
      * @return 0 Successful
      * @return -1 Cannot find the stock in Yahoo Finance
      * @return 1 Share in the Market is not enough for buying
      * @return 2 Balance the user has is not enough
      */
     @Override
     public int buy(String ticker_name, int num_stocks) {
         if (StockList.getStockbyName(ticker_name)!=(null)) {
             if (StockList.getStockbyName(ticker_name).getShare() >= num_stocks) {
                 if (StockList.getStockbyName(ticker_name).getPrice() * num_stocks > getBalance()) {
                     //Update the share in user's list
                     fetchStock(ticker_name).setShare(fetchStock(ticker_name).getShare() + num_stocks);
                     //Update the price in user's list
                     fetchStock(ticker_name).setPrice(StockList.getStockbyName(ticker_name).getPrice());
                     //Update the share in Market's list
                     StockList.getStockbyName(ticker_name).setShare(StockList.getStockbyName(ticker_name).getShare() - num_stocks);
                    this.balance=this.balance-StockList.getStockbyName(ticker_name).getPrice() * num_stocks;
                     return 0;//Successful
                 } else {
                     return 2;
                 }
             } else {
                 return 1;
             }
         } else {
             return -1;
         }
     }
 
     /**
      *
      * @param ticker_name
      * @param num_stocks
      * @return 0 Successful
      * @return -1 Cannot find the stock in the current User's account
      * @return 1 Share is not enough
      */
     @Override
     public int sell(String ticker_name, int num_stocks) {
         if (fetchStock(ticker_name)!=(null)) {
             if (fetchStock(ticker_name).getShare() >= num_stocks) {
                 //Update the share in user's list
                 fetchStock(ticker_name).setShare(fetchStock(ticker_name).getShare() - num_stocks);
                 //Update the price in user's list
                 fetchStock(ticker_name).setPrice(StockList.getStockbyName(ticker_name).getPrice());
                 //Update the share in Market's list
                 StockList.getStockbyName(ticker_name).setShare(StockList.getStockbyName(ticker_name).getShare() - num_stocks);
                this.balance=this.balance+StockList.getStockbyName(ticker_name).getPrice()*num_stocks;
                 return 0;//Successful
             } else {
                 return 1;
             }
         } else {
             return -1;
         }
     }
 
     /**
      * Display the stock the User has
      *
      * @return
      */
     @Override
     public String displayStocksHold() {
         String returnStr = "";
         returnStr += "Stock Name-----Shares Hold-----Price of Last Trade\n";
         for (int i = 0; i < sEList.size(); i++) {
             returnStr += ((sEList.get(i)).getTickerName() + "  " + (sEList.get(i)).getShare() + "   " + (sEList.get(i)).getPrice() + "\n");
         }
         return returnStr;
     }
 
     /**
      * Just for test...
      *
      * @return
      */
     public String test() {
         System.out.println("test func in UserAPI");
         return "test!";
     }
 
     @Override
     public double getUserBalance() throws RemoteException {
         return this.balance;
     }
 
     public void populateCurrentUser(String userName) throws RemoteException {
         User tempUser = UserList.fetchByUserName(userName);
         this.userName = tempUser.getUserName();
         this.balance = tempUser.getBalance();
         this.sEList = tempUser.getStockListofUser();
     }
 }
