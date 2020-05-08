 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.btc.mtgox;
 
 /**
  *
  * @author Paul Mumby
  */
 public class TradeRecord {
     public String date;
     public long price_int;
     public long amount_int;
     public long tid;
     public CurrencyEnum price_currency;
     public CurrencyEnum item;
     public TradeTypeEnum trade_type;
     public String primary;
     public String properties;
     
     public double getPrice()
     {
         switch(price_currency)
         {
             case BTC:
                return (double)(price_int/10000000);                
             default:
                return (double)(price_int/100000);                
         }
     }
 
     public double getAmount()
     {
         switch(item)
         {
             case BTC:
                return (double)(amount_int/10000000);                
             default:
                return (double)(amount_int/100000);                
         }
     }
 }
