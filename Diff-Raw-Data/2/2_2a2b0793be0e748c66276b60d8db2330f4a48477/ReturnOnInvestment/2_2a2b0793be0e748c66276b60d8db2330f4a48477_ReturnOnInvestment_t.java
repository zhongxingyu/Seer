 package vsp.statistics;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import vsp.StockInfoServiceProvider;
 import vsp.dal.requests.PortfolioEntries;
 import vsp.dal.requests.Stocks;
 import vsp.dal.requests.Transactions;
 import vsp.dal.requests.Users;
 import vsp.dataObject.AccountData;
 import vsp.dataObject.HistoricalStockInfo;
 import vsp.dataObject.PortfolioData;
 import vsp.dataObject.Stock;
 import vsp.dataObject.StockInfo;
 import vsp.dataObject.StockTransaction;
 import vsp.exception.SqlRequestException;
 import vsp.utils.Enumeration.OrderAction;
 
 public class ReturnOnInvestment {
   AccountData userAccount;
   StockInfoServiceProvider stockService = new StockInfoServiceProvider();
   
   public static void main(String[] args){
     try {
       AccountData account = Users.requestAccountData("rob");
       ReturnOnInvestment roi = new ReturnOnInvestment(account);
       Stock stock = Stocks.getStock("GE");
       System.out.println("ROI = " + roi.getReturnOnInvestment(stock));
     } catch (SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     
     StockInfoServiceProvider stockService = new StockInfoServiceProvider();
     Calendar cal = Calendar.getInstance();
     cal.clear();
     cal.set(2008, 9, 30);
     HistoricalStockInfo stockInfo = stockService.requestHistoricalStockDataForDay("GE", cal.getTime());
     
     System.out.println(stockInfo.toString());
     
     
   }
   
   public ReturnOnInvestment(AccountData user)
   {
     userAccount = user;
   }
   
   
   public double getReturnOnInvestment(Stock stock){
     List<StockTransaction> transactions = getTransactions(stock);
     double investment = 0;
     double value = 0;
     double roi = 0; //percentage;
     for(StockTransaction trans : transactions){
       if(trans.getOrder().getAction() == OrderAction.BUY){
         investment += trans.getValue();
       }else if(trans.getOrder().getAction() == OrderAction.SELL){
         investment -= trans.getValue();
       }
     }
     
     try {
       PortfolioData entry = PortfolioEntries.getEntry(
           userAccount.getUserName(), stock.getStockSymbol());
       StockInfo stockData = stockService.requestCurrentStockData(stock.getStockSymbol());
       
       value = entry.getQuantity() * stockData.getLastTradePrice();
       
       
      roi = (value - investment)/investment; 
     } catch (SqlRequestException | SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     
     
     return roi;
   }
   
   private List<StockTransaction> getTransactions(Stock stock){
     List<StockTransaction> transactions = new ArrayList<StockTransaction>();
     try {
       transactions = Transactions.getAllExecutedTransactionsForUserAndStock(
           userAccount.getUserName(), stock.getStockSymbol());
     } catch (SqlRequestException | SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     
     return transactions;
   }
 }
