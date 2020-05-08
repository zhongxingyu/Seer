 package com.murex.ccportal.server;
 
 import com.murex.ccportal.client.StockService;
 
 import javax.inject.Singleton;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author hsuleiman
  *         Date: 12/9/11
  *         Time: 9:37 AM
  */
 @Singleton
 public class StockServiceImpl implements StockService{
   @Override
   public List<String> getStocks() {
//    if(1 == 1) throw new AuthenticationException();
     return Arrays.asList("GOOG", "ORCL", "JAVA", "GM");
   }
 }
