 package com.github.tbertell.openchannel.service;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.webservicex.StockQuote;
 import net.webservicex.StockQuoteSoap;
 
 import org.apache.camel.Exchange;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class StockQuoteWSClient {
 	private String url = "http://www.webservicex.net/stockquote.asmx";
 
	private long cacheTTL = 1000;
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(StockQuoteWSClient.class);
 
 	private final String START_ELEMENT = "<Last>";
 	private final String END_ELEMENT = "</Last>";
 	
 	private Boolean slow = true;
 
 	private static final ConcurrentHashMap<String, CacheEntry> CACHE = new ConcurrentHashMap<String, CacheEntry>();
 
 	public String getQuote(String symbol, Exchange exchange) {
 
		LOGGER.info("Start web service call with symbol: " + symbol +" url " +url +" cacheTTL " +cacheTTL);
 		long starttime = System.currentTimeMillis();
 
 		// if TTL is 0 cache is not used
		if (cacheTTL != 0 && CACHE.contains(symbol)) {
 			CacheEntry entry = CACHE.get(symbol);
 			if (isCacheEntryValid(entry, starttime)) {
 				return entry.getQuote();
 			}
 		}
 
 		// simulate slow response time
 		if (slow) {
 			waitRandomTime();
 		}
 		
 		StockQuote ss = new StockQuote();
 		StockQuoteSoap port = ss.getStockQuoteSoap();
 		String response = port.getQuote(symbol);
 
 		long endtime = System.currentTimeMillis();
 		Long responseTime = (endtime - starttime);
 		
 		String quote = parseQuote(response);
 		
 		// setup headers
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("responseTime", responseTime.toString());
 		params.put("channelId", "StockQuoteChannel");
 		
 		exchange.getIn().setHeader("params", params);
 		exchange.getIn().setHeader("quote", "<quote>" +quote +"</quote>");
 		
 		CACHE.put(symbol, new CacheEntry(symbol, endtime));
 
 		LOGGER.info("End web service call with response: " + quote + ", respose time: " + responseTime + " ms");
		return quote;
 	}
 
 	public static void main(String... args) {
 		StockQuote ss = new StockQuote();
 		StockQuoteSoap port = ss.getStockQuoteSoap();
 		String quotes = port.getQuote("NOK");
 		System.out.println(new StockQuoteWSClient().parseQuote(quotes));
 	}
 
 	private String parseQuote(String response) {
 		int beginIndex = response.indexOf(START_ELEMENT) + 6;
 		int endIndex = response.indexOf(END_ELEMENT);
 		String quote = response.substring(beginIndex, endIndex);
 
 		return quote;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	private boolean isCacheEntryValid(CacheEntry entry, long currentTime) {
 		if (entry.getLastAccessed().longValue() + cacheTTL > currentTime) {
			LOGGER.debug("Cache hit");
 			return true;
 		} else {
			LOGGER.debug("Cache invalid");
 			return false;
 		}
 	}
 	
 
 	public long getCacheTTL() {
 		return cacheTTL;
 	}
 
 	public void setCacheTTL(long cacheTTL) {
 		this.cacheTTL = cacheTTL;
 	}
 
 	public boolean getSlow() {
 		return slow;
 	}
 
 	public void setSlow(boolean isSlow) {
 		this.slow = slow;
 	}
 
 
 
 	private class CacheEntry implements Serializable {
 
 		private static final long serialVersionUID = 3113230821706755430L;
 
 		private String symbol;
 		private Long lastAccessed;
 
 
 		public CacheEntry(String symbol, Long lastAccessed) {
 			super();
 			this.symbol = symbol;
 			this.lastAccessed = lastAccessed;
 		}
 
 		public String getQuote() {
 			return symbol;
 		}
 
 		public void setQuote(String quote) {
 			this.symbol = quote;
 		}
 
 		public Long getLastAccessed() {
 			return lastAccessed;
 		}
 
 		public void setLastAccessed(Long lastAccessed) {
 			this.lastAccessed = lastAccessed;
 		}
 	}
 
 	private void waitRandomTime() {
 		int sleepTime = new Random().nextInt(5000);
 		LOGGER.info("Wait for " +sleepTime + " ms");
 		try {
 			Thread.sleep(sleepTime);
 		} catch (InterruptedException e) {
 			// don't sleep then
 			e.printStackTrace();
 		}
 	}
 
 }
