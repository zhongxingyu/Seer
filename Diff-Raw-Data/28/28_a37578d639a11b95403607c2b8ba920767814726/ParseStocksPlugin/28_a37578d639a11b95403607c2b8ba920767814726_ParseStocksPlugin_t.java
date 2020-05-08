 package eai.msejdf.web;
 
 import org.apache.log4j.Logger;
 
 import eai.msejdf.data.*;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.net.URL;
 import java.text.NumberFormat;
 import java.text.ParseException;
import java.util.Calendar;
 import java.util.Locale;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 
 
 /**
  * @author NB12588
  *
  * Plugin to parse stock data from a web site and return an object that represents that data
  * It currently supports the following site:
  * 		http://www.nextbolsa.com/cotacoes.php?action=euronext
  * 
  * Data syntax for this site:
  * 
  * Ttulo 		lt. Cotao 	Hora 	Variao 	Quantidade 	Mximo 	Mnimo 	Compra 	Venda
  * BPI Hist.   	0.8316:35 	  1.34%	  991,176  0.840.820.000.00
  * ZON MULTI. Hist.2.3816:35 	  9.16%  2,621,838 2.402.190.000.00
  * 
  * Note: The "Hist" string under the title is a link in the page that is not required and shall 
  * therefore be filtered out 
  * 
  * Important: If the above syntax or number of elements changes, STOCK_ROW__ELEMENT_COUNT and
  * STOCK_ROW_INDEX__* must be update accordingly, as well as the parsing
  */
 public class ParseStocksPlugin extends Parser
 {
 	/**
 	 * Logger for this class
 	 */
 	private static final Logger logger = Logger.getLogger(ParseStocksPlugin.class);
 
 	private static final int STOCK_ROW_INDEX__COMPANY = 0;
 	private static final int STOCK_ROW_INDEX__LAST_COTATION = 1;
 	private static final int STOCK_ROW_INDEX__COTATION_TIME = 2;
 	private static final int STOCK_ROW_INDEX__VARIATION = 3;
 	private static final int STOCK_ROW_INDEX__QUANTITY = 4;
 	private static final int STOCK_ROW_INDEX__MAXIMUM = 5;
 	private static final int STOCK_ROW_INDEX__MINIMUM = 6;
 	private static final int STOCK_ROW_INDEX__BUY = 7;
 	private static final int STOCK_ROW_INDEX__SELL = 8;
 	private static final int STOCK_ROW__ELEMENT_COUNT = 9;	// NOTE: Update if number of elements changes	
 	private static final Locale STOCK_NUMBER_FORMAT_LOCALE = Locale.US;
 	private static final int CONNECTION_TIMEOUT = 10000; // In mili seconds
 
 	private String webUrl = null;
 	
 	/**
 	 * Constructs the stock parsing object
 	 * 
 	 * @param url The target URL from which HTML contend will be fetched and parsed to retrieve stock data
 	 */
 	public ParseStocksPlugin(String url)
 	{
 		if ((null == url) || url.isEmpty())
 		{
 			throw new NullPointerException();
 		}
 		this.webUrl = url;
 	}
 	
 	/* (non-Javadoc)
 	 * @see eai.msejdf.web.Parser#parse()
 	 */
 	public Object parse() throws IOException 
 	{
 		if (logger.isDebugEnabled())
 		{
 			logger.debug("parse() - start"); //$NON-NLS-1$
 		}
 
 		Document webPageDoc = null;
 		Elements cotationInfoRows = null;
 		
 		// Create a DOM representation of a web page
 		webPageDoc = Jsoup.parse(new URL(this.webUrl), ParseStocksPlugin.CONNECTION_TIMEOUT); 
 
 		// Extract all rows of the tables that have cotation information
 		cotationInfoRows = webPageDoc.select("tr:has(td[class^=tituloforumbar]):gt(0) ~ tr");		
 		if (0 == cotationInfoRows.size())
 		{
 			// The parsed data is not as we are expecting, which means that we can't make assumptions
 			// about the correctness of the fields. The safest thing to do is to not return anything and 
 			// alert the user somehow.
 			logger.error("parse(): ERROR - Web Page syntax from " + this.webUrl + " is not supported"); //$NON-NLS-1$			
 			return null;			
 		}
 		
 		Stocks stocks = new Stocks();
 		
		stocks.setTimestamp(BigInteger.valueOf(Calendar.getInstance().getTimeInMillis()));

 		try
 		{
 			// Fill out our stock information object for each individual stock information client
 			for (Element cotationInfo : cotationInfoRows)
 			{
 				Stock stockInfo = new Stock();
 
 				// Separates the different cotation information fields into a list of fields, excluding data
 				// in the first field that is irrelevant ("Hist." as described in the example presented in 
 				// this class information doc)
				Elements cotationFields = cotationInfo.select("table td:eq(0) a, >td:gt(0)");
 
 				if (ParseStocksPlugin.STOCK_ROW__ELEMENT_COUNT != cotationFields.size())
 				{
 					// The parsed data is not as we are expecting, which means that we can't make assumptions
 					// about the correctness of the fields. The safest thing to do is to not return anything and 
 					// alert the user somehow.
 					logger.error("parse(): ERROR - Web Page syntax from " + this.webUrl + " is not supported"); //$NON-NLS-1$			
 					stocks = null;
 
 					if (logger.isDebugEnabled())
 					{
 						logger.debug("parse() - end"); //$NON-NLS-1$
 					}
 					return null;			
 				}
 				
 				// Fill the sock object with the information retrieved from the page 
 				parseFields(cotationFields, stockInfo);
 				
 				stocks.getStock().add(stockInfo);
 			}
 		}
 		catch(ParseException exception)
 		{
 			// The parsed data is not as we are expecting, which means that we can't make assumptions
 			// about the correctness of the fields. The safest thing to do is to not return anything and 
 			// alert the user somehow.
 			logger.error("parse()", exception); //$NON-NLS-1$
 			stocks = null;
 
 			if (logger.isDebugEnabled())
 			{
 				logger.debug("parse() - end"); //$NON-NLS-1$
 			}
 			return null;
 		}
 
 		if (logger.isDebugEnabled())
 		{
 			logger.debug("parse() - end"); //$NON-NLS-1$
 		}
 		return stocks;
 	}
 	
 	private void parseFields(Elements cotationFields, Stock stockInfo) throws ParseException
 	{		
 		String field = null;
 		NumberFormat formatter = NumberFormat.getInstance(ParseStocksPlugin.STOCK_NUMBER_FORMAT_LOCALE);
 		
 		stockInfo.setCompany(new Company());
 		stockInfo.setCotation(new Cotation());
 		
 		Company company = stockInfo.getCompany();
 		Cotation cotation = stockInfo.getCotation();
 		
 		field = cotationFields.get(ParseStocksPlugin.STOCK_ROW_INDEX__COMPANY).text();
 		company.setName(field);
 
 		field = cotationFields.get(ParseStocksPlugin.STOCK_ROW_INDEX__LAST_COTATION).text();
 		cotation.setLastCotation(BigDecimal.valueOf(formatter.parse(field).doubleValue()));
 
 		field = cotationFields.get(ParseStocksPlugin.STOCK_ROW_INDEX__COTATION_TIME).text();
 		cotation.setTime(BigDecimal.valueOf(formatter.parse(field).doubleValue()));
 
 		field = cotationFields.get(ParseStocksPlugin.STOCK_ROW_INDEX__VARIATION).text();
 		cotation.setVariation(BigDecimal.valueOf(formatter.parse(field).doubleValue()));
 
 		field = cotationFields.get(ParseStocksPlugin.STOCK_ROW_INDEX__QUANTITY).text();
 		cotation.setQuantity(BigInteger.valueOf(formatter.parse(field).longValue()));
 		
 		field = cotationFields.get(ParseStocksPlugin.STOCK_ROW_INDEX__MAXIMUM).text();
 		cotation.setMaximum(BigDecimal.valueOf(formatter.parse(field).doubleValue()));
 
 		field = cotationFields.get(ParseStocksPlugin.STOCK_ROW_INDEX__MINIMUM).text();
 		cotation.setMinimum(BigDecimal.valueOf(formatter.parse(field).doubleValue()));
 
 		field = cotationFields.get(ParseStocksPlugin.STOCK_ROW_INDEX__BUY).text();
 		cotation.setPurchase(BigDecimal.valueOf(formatter.parse(field).doubleValue()));
 
 		field = cotationFields.get(ParseStocksPlugin.STOCK_ROW_INDEX__SELL).text();
 		cotation.setSell(BigDecimal.valueOf(formatter.parse(field).doubleValue()));
 	}
 }
