 package main.java;
 
 /**
 * Created with IDEA.
  * User: Alan Derrick
  * Date: 10/13/12
  * Time: 5:53 PM
  */
 public class Stock {
     private String symbol;
     private String stockName;
     private String price;
 
     /**
      *
      * @param symbol This is the symbol on the stock exchange
      * @param name   This is the actual name of the company
      * @param price  This is the price of a share of the stock
      */
     public Stock(String symbol, String name, String price) {
         this.symbol = symbol;
         this.stockName = name;
         this.price = price;
     }
 
     /**
      *
      * @return the stock symbol
      */
     public String getSymbol() {
         return symbol;
     }
 
     /**
      *
      * @param symbol The symbol is a stock exchange symbol
      */
     public void setSymbol(String symbol) {
         this.symbol = symbol;
     }
 
     /**
      *
      * @return the actual name of the company
      */
     public String getStockName(){
         return stockName;
     }
 
     /**
      *
      * @param name The name is the actual name of the company
      */
     public void setStockName(String name) {
         this.stockName = name;
     }
 
     /**
      *
      * @return the price of the stock
      */
     public String getPrice() {
         return price;
     }
 
     /**
      *
      * @param price The price is the price of the stock
      */
     public void setPrice(String price) {
         this.price = price;
     }
 }
