 package org.gsoft.openserv.repositories.rates;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigDecimal;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.annotation.Resource;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.gsoft.openserv.domain.rates.DailyStockQuote;
 import org.gsoft.openserv.domain.rates.Stock;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class CurrentDailyStockQuoteRepository {
 	private static final Logger LOG = LogManager.getLogger(CurrentDailyStockQuoteRepository.class);
 	
 	@Resource
 	private DailyStockQuoteRepository quoteRepository;
 	
 	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
 	
 	public DailyStockQuote findCurrentQuoteForStock(Stock stock) throws IOException, ParseException{
 		 URL yahoofin = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + stock.getSymbol() + "&f=sd1oghl1&e=.csv");
          URLConnection yc = yahoofin.openConnection();
         String inputLine = null;
          BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
         try{
        	 inputLine = in.readLine();
         }
         finally{
        	 in.close();
         }
          if(inputLine != null) {
         	 String[] yahooStockInfo = inputLine.split(",");
              Date quoteDate = sdf.parse(yahooStockInfo[1].replace("\"", ""));
              DailyStockQuote quote = quoteRepository.findDailyStockQuote(stock, quoteDate);
              if(quote == null){
             	 quote = new DailyStockQuote();
             	 quote.setStock(stock);
             	 quote.setQuoteDate(quoteDate);
             	 quoteRepository.save(quote);
              }
              try{
             	 quote.setOpenValue(new BigDecimal(yahooStockInfo[2]));
             	 quote.setLowValue(new BigDecimal(yahooStockInfo[3]));
             	 quote.setHighValue(new BigDecimal(yahooStockInfo[4]));
             	 quote.setLastValue(new BigDecimal(yahooStockInfo[5]));
             	 return quote;
              }
              catch(NumberFormatException nfe){
             	 LOG.warn("Unable to parse quote. [" + yahooStockInfo + "]");
              }
         }
        return null;
 	}
 }
