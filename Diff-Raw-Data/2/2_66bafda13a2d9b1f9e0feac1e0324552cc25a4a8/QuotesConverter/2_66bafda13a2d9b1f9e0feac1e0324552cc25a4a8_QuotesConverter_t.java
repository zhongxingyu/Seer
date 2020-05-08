 package com.stockyou.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.stockyou.data.yahoo.YahooPortfolio;
 import com.stockyou.data.yahoo.YahooQuote;
 
 public class QuotesConverter {
 	
 	public Portfolio convert(YahooPortfolio yPortfolio){
 		
 		Portfolio portfolio = new Portfolio();
 		
		if(yPortfolio != null &&  yPortfolio.getQuery().getResults() != null  &&  yPortfolio.getQuery().getResults().getQuote() != null){
 			List<Quote> quotes = new ArrayList<Quote>();
 			for (YahooQuote yQuote : yPortfolio.getQuery().getResults().getQuote()) {
 				Quote quote = new Quote();
 				quote.setUnit("Rs");
 				quote.setFiftyDayAverage(quote.getUnit() + " "+yQuote.getFiftydayMovingAverage());
 				quote.setTwoHundredDayAverage(quote.getUnit() + " "+yQuote.getTwoHundreddayMovingAverage());
 				quote.setStockCode(yQuote.getSymbol());
 				quote.setStockPrice(quote.getUnit() + " "+yQuote.getLastTradePriceOnly());
 				quote.setName(yQuote.getName());
 				quote.setUpdatedDt(yQuote.getLastTradeDate() + " - " + yQuote.getLastTradeTime());
 				quote.setDayValueChange(quote.getUnit() + " "+yQuote.getDaysValueChange());
 				quote.setDayPercentageChange(yQuote.getPercentChange());
 				quote.setLogo(getImageFile(yQuote));
 				quotes.add(quote);
 			}
 			portfolio.setQuote(quotes);
 		}
 		
 		return portfolio;
 	}
 
 	private String getImageFile(YahooQuote yQuote) {
 		return "images/"+yQuote.getSymbol().replaceAll("\\.(.)*", "") + ".png";
 	}
 
 }
