 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.btc.mtgox;
 
 /**
  *
  * @author Paul Mumby
  */
 public class CurrencyValue {
     private long value_int;
     public String display;
     public CurrencyEnum currency;
     
     public double getValue()
     {
         switch(currency)
         {
             case BTC:
                return (double)((double)value_int/(double)10000000);                
             default:
                return (double)((double)value_int/(double)100000);                
         }
     }
     
     public void setValue(double value)
     {
         switch(currency)
         {
             case BTC:
                 this.value_int = (long)(value*10000000);
                 break;
             default:
                 this.value_int = (long)(value*100000);  
                 break;
         }        
     }
 }
