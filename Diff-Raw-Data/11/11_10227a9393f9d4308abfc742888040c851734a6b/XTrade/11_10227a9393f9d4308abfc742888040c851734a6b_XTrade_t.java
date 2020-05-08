 package impl;
 
 import api.XTradeAPI;
 import java.rmi.*;
 import java.rmi.server.*;
 
 import object.User;
 import object.Record;
 import object.Stock;
 import stockData.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  *
  * @author chenliang
  */
 public class XTrade extends UnicastRemoteObject implements XTradeAPI{
 
     private static Date date=new Date();
     private static DateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
     private static ArrayList<Stock> stockList = new ArrayList<>();
     private static ArrayList<User> userList= new ArrayList<>();
     private static ArrayList<Record> recordList= new ArrayList<>();
    
     
     /*
      * Constructor of XTrade 
      */
     public XTrade() throws RemoteException
     {
         super();
     }
         
     /**
      * Test if connected.
      */
     @Override
     public String hello() throws RemoteException
     {
         return("You are connected");
     }
     
 
     /*
      * login using typed userName, if not found in the file, create new user and save
      */
     @Override
     public String login(String userName) throws RemoteException
     {
         User u=isUserExisted(userName);
         
         if(u!=null)
         {
             return("Welcome back to XTrade, "+u.getUserName());
         }
         
         else
         {
             u=new User(userName);
             userList.add(u);
             StockData.getInstance().save();
         
        return("Welcome to XTrade for the first time, "+ u.getUserName());
         }
 
 
     }
     
     
     /*
      * query user info: userName, cashBalance. If not found, create new user in the file
      * @param userName user's name to query
      */
     @Override
     public String queryUser(String userName) throws RemoteException
     {
         User u=isUserExisted(userName);
         if(u!=null)
         {
             return u.toString();
         }
         else
         {
             return("[ERROR] user "+userName+" does not exist.");
         }
     }
     
     
     /*
      * Return the stock object with given symbol
      */
     @Override
     public String queryStock(String symbol) throws RemoteException
     {       
            Stock s=isStockTracked(symbol);
            
            if(s!=null)
            {
                return s.toString();
            }
            
            else
            {
                 s=StockData.getInstance().querybyurl(symbol);
             
                 if(s!=null)
                 {       
                     stockList.add(s);
                     StockData.getInstance().save();
                     return ("[ADD] succeed -> "+s.toString());
                 }
                 else
                 {
                     return ("[ERROR] stock "+symbol+" does not exist.");
                 }                        
            }
           
     }
     
     
     
     
     /*
      * Return the record of given userName and stock symbol
      * @param userName the name of the user
      * @param symbol the stock symbol
      */
     @Override
     public String queryRecord(String userName,String symbol) throws RemoteException
     {
         Record r=isRecordExisted(userName,symbol);
         if(r!=null)
         {
             return(r.toString());
         }
         else
         {
              return("[ERROR] record does not exist.");
         }
     }
     
     
     
     
     
     /*
      * Client 1 updates the price of a stock
      * @symbol the symbol to update
      * @price the price to set
      */
     @Override
     public String update(String symbol, double price) throws RemoteException
     {
            
         
         for(int i=0;i<stockList.size();i++)
             {
                 if(stockList.get(i).getSymbol().equalsIgnoreCase(symbol))
                 {
                     stockList.get(i).setPrice(price);
                     StockData.getInstance().save();
                     
                     return ("[UPDATE] succeed -> "+stockList.get(i).toString());
                 }
             }
 
              return ("Pleas first [QUERY] "+symbol+" to track.");
                                   
     }
     
     
 
     
     
     /*
      * Client 2 buys stock 
      * @param symbol symbol of stock to buy
      * @param userName user to buy
      * @param shares number of shares to buy
      */
       
     @Override
     public String buy(String symbol,String userName,int shares) throws RemoteException
     {   
         Stock s=isStockTracked(symbol);
         
         if(s!=null)
         {   
             User u=isUserExisted(userName);
             
             if(u!=null)
             {
                 if(u.getCashBalance()>=s.getPrice()*shares)
                 {
                         u.setCashBalance(u.getCashBalance()-s.getPrice()*shares);
                         
                        s.setShareBalance(s.getShareBalance()-shares);
                        
                         Record r=isRecordExisted(userName,symbol);
                         
                     if(r!=null)
                     {
                         r.setShares(r.getShares()+shares);
                         
                     }
                     else
                     {
                         recordList.add(new Record(userName,symbol,shares));
                     }
                     
                     StockData.getInstance().save();
                     
                     return ("[BUY] succeed -> "+userName+","+symbol+","+shares);
                 }
                 else
                 {
                     return("[ERROR] No enough cash balance.");
                 }
             }
             else
             {
                 return("[ERROR] user "+userName+" does not exist.");
             }
         }
         
         else
         {
              return ("Please first [QUERY] "+symbol+" to track.");
         }
         
        
     }
     
 
     /*
      * Client 2 sells shares of stock
      * @param symbol symbol to query
      * @param userName to sell the stock
      * @param shares number of shares to sell
      */
     @Override
     public String sell(String symbol,String userName,int shares) throws RemoteException
     {
       
         Stock s=isStockTracked(symbol);
         if(s!=null)
         {
             User u=isUserExisted(userName);
             
             if(u!=null)
             {
                 Record r=isRecordExisted(userName,symbol);
                 
                 if(r!=null)
                 {
                     if(r.getShares()>=shares)
                     {
                         u.setCashBalance(u.getCashBalance()+s.getPrice()*shares);
                        s.setShareBalance(s.getShareBalance()+shares);
                        r.setShares(r.getShares()-shares);

                         StockData.getInstance().save();
                         
                         return("[SELL] succeed -> "+userName+","+symbol+","+shares);
                     }
                     else
                     {
                         return("[ERROR] No enough stock to sell.");
                     }
                 }
                 else
                 {
                     return("[ERROR] No stock to sell yet.");
                 }
             }
             else
             {
                 return("[ERROR] user"+userName+" does not exist.");
             }
         }
         else
         {
             return("Pleas first [QUERY] "+symbol+" to track.");
         }
     }
     
     
     
     
     /*
      * intialize the ArrayLists of user/stock/record
      */
     public void loadLists()
     {
               StockData.getInstance().load();
               userList=StockData.getInstance().getUserList();
               stockList=StockData.getInstance().getStockList();
               recordList=StockData.getInstance().getRecordList();
               
     }
     
     
     
     /*
      * helper function to check if a user existed
      */
     public User isUserExisted(String userName)
     {
         for(User u:userList)
         {
             if(u.getUserName().equalsIgnoreCase(userName))
             {
                 return u;
             }
         }
         
         return null;
     }
     
      /*
      * helper function to check if stock is being tracked
      */
     public Stock isStockTracked(String symbol)
     {
         for(Stock s:stockList)
         {
             if(s.getSymbol().equalsIgnoreCase(symbol))
             {
                 return s;
             }
         }
         
         return null;
     }
     
     /*
      * helper function to return record in the array list
      */
     public Record isRecordExisted(String userName,String symbol)
     {
         for(Record r:recordList)
         {
             if(r.getUserName().equalsIgnoreCase(userName)&&r.getSymbol().equalsIgnoreCase(symbol))
             {
                 return r;
             }
         }
         
         return null;
     }
 
     @Override
     public ArrayList<User> getAllUser() throws RemoteException {
         return userList;
     }
 
     @Override
     public ArrayList<Stock> getAllStock() throws RemoteException {
         return stockList;
     }
 
     @Override
     public ArrayList<Record> getAllRecord() throws RemoteException {
         return recordList;
     }
     
     
 }
