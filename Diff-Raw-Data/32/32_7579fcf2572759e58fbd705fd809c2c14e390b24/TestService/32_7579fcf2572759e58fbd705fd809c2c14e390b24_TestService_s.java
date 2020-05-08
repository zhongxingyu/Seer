 package com.ben.service;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.jws.WebMethod;
 
 import javax.jws.WebService;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.ParseException;
 
 import com.ben.service.GoogleScrape;
 import com.ben.service.TickerQuotes;
 import com.ben.service.NewsScrape;
 import com.google.gson.JsonArray;
 
 @WebService
 public class TestService {
 
 	 List<struct_News> _News = new ArrayList<struct_News>();
 	 List<TickerQuotes> _quotes = new ArrayList<TickerQuotes>();
 	 
 	 mainPnL _m; 
 	 GoogleScrape _g;
 	 Bloomberg_scrape _b;
 	 String _env ="PROD";
 	 
 	 public TestService(String _env) throws InterruptedException, SQLException
 	 {
 		 this._env = _env;
 		  Server S = new Server();
 		 _m = new mainPnL(_env);
 		 _g = new GoogleScrape();
 		 _b = new Bloomberg_scrape();
 	       S.WriteLog("Starting market data service...");
 		GetLatest();
 		 S.WriteLog("MDM up!");
 	 }
 	 
 	 @WebMethod
 	  public String getPrice(String Ticker) {
 		
 		 
 		  Ticker = Ticker.toUpperCase();
 		 
 		 String result  = "";
 		 for(TickerQuotes quote : _quotes)
 		 {
 			
 		 if (quote.symbol.equals(Ticker))
 		 {
 		String Symbol = quote.symbol;
 		Symbol=ConvertToHumanReadable(Symbol);
 		 result = Symbol+"#"+quote.Price+"#"+quote.Change+"#"+quote.Status;
 		  return result;
 	
 		 }
 		 }
 			
 		    	_g.getLast(Ticker,_quotes); 
 		    	for(TickerQuotes quote : _quotes)
 				 {
 					
 				 if (quote.symbol.equals(Ticker))
 				 {
 				 String Symbol = quote.symbol;
 				 Symbol=ConvertToHumanReadable(Symbol);
 				 result = Symbol+"#"+quote.Price+"#"+quote.Change+"#"+quote.Status;
 				//	 array[i]=quote.symbol+"#"+quote.Price+"#"+quote.Change+"#"+quote.Status;
 			//	 i++;
 				 }
 				 }
 			 
 	//	 }
 		    Server S = new Server();
 		       S.WriteLog("New Request : "+result);
 		 
 		 System.out.println("REQ : "+result);
 			// return array;
 		 
 		 //Split change % and price
 		 
 		 
 		 
 		 return result;
 	    }
 	private String ConvertToHumanReadable(String Symbol)
 	{
 		if (Symbol.equals("UKX"))
 			Symbol ="FTSE";
 		if (Symbol.equals("INX"))
 			Symbol ="S&P500";
 		if (Symbol.equals("IXIC"))
 			Symbol ="NASDAQ";
 		if (Symbol.equals("NI225"))
 			Symbol ="NIKKEI";
 		if (Symbol.equals("SX5E"))
 			Symbol ="STOXX50";
 		if (Symbol.equals("GLD"))
 			Symbol ="GOLD";
 		
 		return Symbol;
 		
 	}
 	
 	 
 	 public void GetLatest() throws InterruptedException, SQLException
 	 {
 		
 		 GoogleScrape GS = new GoogleScrape();
 		 List<String> Tickers = new ArrayList<String>();
 		 ResultSet rs = null;
 	
 			try {
 				rs = _m.LoadData("Select distinct Ticker from interestlist");
 				while (rs.next()) {
 			
 				GS.getLast(rs.getString(1),_quotes);
 								
 				}
 				
 				//GBPUSD addition to MDM
 				TickerQuotes TQ = new TickerQuotes();
 				TQ.symbol="GBPUSD";
 				TQ.Price = _b.getFX();
 				_quotes.add(TQ);
 				
 				//Futures addition to MDM
 				TickerQuotes TQ1 = new TickerQuotes();
 				TQ1.symbol="S&P";
 				TQ1.Price = _b.getFuture("sap");
 				_quotes.add(TQ1);
 				
 				TickerQuotes TQ2 = new TickerQuotes();
 				TQ2.symbol="NDX";
 				TQ2.Price = _b.getFuture("nasdaq");
 				_quotes.add(TQ2);
 				
 				
 			
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		
 			Timer _timerquotes = new Timer ();
 			TimerTask _hourlyTaskquotes = new TimerTask () {
 			    @Override
 			    public void run () {
 			    	 mdm _mdm = new mdm(_env);
 			    	_mdm.start(_quotes);
 			    			    	
 			    	
 			    }
 			};
 			_timerquotes.schedule(_hourlyTaskquotes, 01,300000);
 		/*	
 			Timer _timernews = new Timer ();
 			TimerTask _hourlyTasknews = new TimerTask () {
 			    @Override
 			    public void run () {
 			    	try{
 			    		System.out.println("Running News Thread");
 			    	NewsScrape NS = new NewsScrape();
 			    	NS.getNews("AAPL",_News);
 			    	NS.getNews("NOK",_News);
 			    	NS.getNews("BAC",_News);
 			    	NS.getNews("FSLR",_News);
 			   	 for(struct_News __News : _News)
 				 {
 				System.out.println(__News.symbol);
 				System.out.println(__News.articles[0]);
 				 }
 			    	}
 			 	catch (Exception e)
 		    	{
 		    		 Server S = new Server();
 		  	       S.WriteLog("ERROR : "+e.toString());
 		    		
 		    		
 		    	}
 			   	 
 			   	 
 			    }
 			    };
 		//	_timernews.schedule(_hourlyTasknews, 01,900000);
 					
 		*/
 		}
 		
 	
 	 public String[] getTwitsTrending() throws ParseException
 	 {
 		
 		 StockTwits ST = new StockTwits();
 		 ArrayList<String> al_StockTwits = new ArrayList<String>();
 		 al_StockTwits =  ST.getTrending();
 		 String[] str_st = new String[ al_StockTwits.size()];
 		 str_st  = al_StockTwits.toArray(str_st);
 		 		 return str_st;
 		 
 		 
 		 
 		 
 	 }	 
 	 
 	
 	 
 	 
 	 public String[] getTwits(String Ticker) throws ParseException
 	 {
 		
 		 StockTwits ST = new StockTwits();
 		 ArrayList<String> al_StockTwits = new ArrayList<String>();
 		 al_StockTwits =  ST.getStockTwit(Ticker.trim());
 		 String[] str_st = new String[ al_StockTwits.size()];
 		 str_st  = al_StockTwits.toArray(str_st);
 		 
 		 return str_st;
 		 
 		 
 		 
 		 
 	 }
 	 public String[] getFly() throws ParseException
 	 {
 		 FlyNews FN = new FlyNews();
 		 ArrayList<String> FlyNews = new ArrayList<String>();
 		 FlyNews =  FN.geFly();
 		 String[] FlyNews_str = new String[ FlyNews.size()];
 		 FlyNews_str = FlyNews.toArray(FlyNews_str);
 		 
 		 return FlyNews_str;
 	 }
 	 public String[] getTopNews() 
 	 
 	 {
 		 String[] array = new String[_News.size()];
 	 
 		 
 		 
 		 
 		 return array;
 	 }
 	 public String[] getNews(String Ticker) 
 	 
 	 {
 	    		 Server S = new Server();
 	  	       S.WriteLog("News request for "+Ticker);
 	    		
 	    		
 	    	
 		
 		 NewsScrape NS = new NewsScrape();
 		NS.getNews(Ticker,_News);
 		 
 		 
 	
 		
 		
 	
 		 int i=0;
 		
 		 
 		 for(struct_News __News : _News)
 		 {
 		 if (__News.symbol.equals(Ticker))
 		 {
 			 
 		 return __News.articles;
 		
 		 }
 		 }
 		
 		 
 		 NS.getNews(Ticker, _News);
 		 
 		 
 		 
 			 return null;
 		 
 	 }
 	 
 	public String call_line_main() throws SQLException
 		 {
 		
 		
 		
 	//	String result = p.Value_pie_json(true);
 		String result = _m.Value_Line_json();
 		System.out.println("HERE----"+result);
 		return result;
 	//	return "test";
 		
 		 }
 	public String call_pie_live() throws SQLException
 	 {
 	
 	
 	String result = _m.Value_pie_json(true);
 //	String result = p.Value_Line_json();
 	System.out.println("PieChartNowCall----"+result);
 	return result;
 	
 	 }
 	public String call_pie_initial() throws SQLException
 	 {
 	
 	
 	String result = _m.Value_pie_json(false);
 //	String result = p.Value_Line_json();
 	System.out.println("PieChartNowInitial----"+result);
 	return result;
 	
 	 }
 	
 	
 	
 	public String Table_holdings() throws SQLException
 	{
 		
 		String result= _m.Table_holdings();
 		
 		
 		return result;
 		
 		
 		
 	}
 	 public String call_pltotal() throws SQLException 
 	 {
 		
 			String result= _m.Table_PnL();
 			
 			
 			return result;
 		//return "IT WORKS!!"; 
 	 }
 	 
 	public String call_fx_table() throws SQLException 
 	{
 		
 		String result= _m.FXTable();
 		
 		
 		return result;
 		
 	}
 	 
 	public String call_pl_table() throws SQLException 
 	{
 		
 		String result= _m.Table_PL_Realised();
 		
 		
 		return result;
 		
 	}
 	public String call_insert_trade(String Ticker, double Quantity, double Price, String Side,String Ccy) throws SQLException 
 	{
 		
 		String result =_m.Insert_Trader(Ticker, Quantity, Price, Side,Ccy);
 		
 			return result;
 	}
 	 
 	
 	public String call_alertlevels_table() throws SQLException
 	{
 		
 		
 		String result= _m.WatchListTable();
 		
 		return result;
 		
 	}
 	public int send_delete_alert(String ticker,String price) throws SQLException
 	{
 		
 		System.out.println(ticker+price);
 		
 		try
 		{
 		_m.ExecuteQuery("Delete from AlertKeyLevels where Symbol='"+ticker+"' and Value='"+price+"'");
 		return -1;
 		}
 		catch (Exception e)
 		{
 		return 0;
 			
 		}
 			
 		
 		
 	}
 	public int send_new_alert(String ticker,String cate,String price) throws SQLException
 	{
 		
 		System.out.println(ticker+price+cate);
 		
 		try
 		{
 		_m.ExecuteQuery("Insert into AlertKeyLevels values ('"+ticker+"','"+cate+"','"+price+"','N')");
 		return -1;
 		}
 		catch (Exception e)
 		{
 		return 0;
 			
 		}
 	}
 		public String call_vol_chart(String Ticker) throws SQLException
 		{
 			
 			//mainPnL m = new mainPnL();
 			String result="";
 			try {
 				result = _m.Vol_Chart(Ticker);
 			} catch (java.text.ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			return result;	
 			
 			
 		}
 		
 		
 	
 	public String get_Favourites(String Index) throws SQLException
 	{
 		
 		
 		String result = _m.getFavourites();
 		System.out.println(result);
 		return result;
 		
 	}
 	public String getFX()
 	{
 		
 	
 		String result = getPrice("GBPUSD");
 				
 		return result;
 		
 	}
 	public String getFuture(String Index)
 	{
 	
 			String result = getPrice(Index);
 		
 		
 		return result;
 		
 	}
 	public String getGold()
 	{
 	
 			String result = _b.getGold("asd");
 		
 		
 		return result;
 		
 	}
 	
 	
 	}
 	
 
