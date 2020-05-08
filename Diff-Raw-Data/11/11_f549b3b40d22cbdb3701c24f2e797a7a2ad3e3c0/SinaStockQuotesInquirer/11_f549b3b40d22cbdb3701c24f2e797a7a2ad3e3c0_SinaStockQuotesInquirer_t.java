 package com.ghlh.stockquotes;
 
 import java.util.regex.Pattern;
 
 public class SinaStockQuotesInquirer extends InternetStockQuotesInquirer {
 	protected String getStockQuotesPageURL(String stockId, boolean isSZ) {
 		String url = "http://hq.sinajs.cn/list=";
 		if (isSZ) {
 			url += "sz";
 		} else {
 			url += "sh";
 		}
 		url += stockId;
 		return url;
 	}
 
 	private final static int STOCKID_POSITION = 0;
 	private final static int NAME_POSITION = 0;
 	private final static int CURRENT_PRICE_POSITION = 1;
 	private final static int ZDF_POSITION = 3;
 	private final static int ZDE_POSITION = 4;
 
 	protected StockQuotesBean parseStockQuotes(String stockInfo)
 			throws StockQuotesException {
 		try {
 			StockQuotesBean result = new StockQuotesBean();
			String stockId = stockInfo.substring(0, stockInfo.indexOf("="));
 			stockId = stockId.substring(stockId.length() - 6);
 			result.setStockId(stockId);
 
 			stockInfo = stockInfo.substring(stockInfo.indexOf("\"") + 1);
 			stockInfo = stockInfo.substring(0, stockInfo.indexOf("\""));
 
 			Pattern pattern = Pattern.compile(",");
 			String[] stockInfoPieces = pattern.split(stockInfo);
 			if (stockInfoPieces.length == 0 || stockInfoPieces.length == 1) {
 				return null;
 			}
 			result.setName(stockInfoPieces[0]);
 			result.setTodayOpen(Double.parseDouble(stockInfoPieces[1]));
 			result.setYesterdayClose(Double.parseDouble(stockInfoPieces[2]));
 			result.setCurrentPrice(Double.parseDouble(stockInfoPieces[3]));
 			result.setHighestPrice(Double.parseDouble(stockInfoPieces[4]));
 			result.setLowestPrice(Double.parseDouble(stockInfoPieces[5]));
 			return result;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			throw new StockQuotesException(
 					"There come up with error while parsing " + stockInfo, ex);
 		}
 	}
 
 	public static void main(String[] args) {
 		try {
 			StockQuotesInquirer internetStockQuotesInquirer = new SinaStockQuotesInquirer();
 			StockQuotesBean stockQuotesBean = internetStockQuotesInquirer
					.getStockQuotesBean("002520");
 			System.out.println(stockQuotesBean);
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 }
