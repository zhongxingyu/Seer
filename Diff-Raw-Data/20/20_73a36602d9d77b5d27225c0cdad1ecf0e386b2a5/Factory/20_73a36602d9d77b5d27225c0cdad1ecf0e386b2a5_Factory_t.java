 package simulator;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import simulator.strategy.*;
 
 public class Factory {
 
 	private OrderBooks books = null;
 	private TradeEngine te = null;
 
 	private static final double EMPTY_DOUBLE_FIELD = -1;
 	private static final long   EMPTY_LONG_FIELD = -1;
 	private static final int   EMPTY_INTEGER_FIELD = -1;
 
 	/* The index positions of each data field in the csv file */
 	private int INSTRUMENT;
 	private int DATE;
 	private int TIME;
 	private int RECORD_TYPE;
 	private int PRICE;
 	private int VOLUME;
 	private int QUALIFIERS;
 	private int TRANSACTION_ID;
 	private int BID_ID;
 	private int ASK_ID;
 	private int BID_ASK;
 	
 	private long tradeID = 0;
 
 	public OrderBooks makeOrderBooks() {
 		if (books == null) {
 			books = new OrderBooks();
 		}
 		return books;
 	}
 
 	public TradeEngine makeTradeEngine() {
 		if (te == null) {
 			te = new TradeEngine(makeOrderBooks(), this);
 		}
 		return te;
 	}
 	
 	public Strategy makeNullStrategy() {
 		return new NullStrategy(null);
 		//giving null won't make a difference since this won't ever use orderBooks 
 	}
 
 	public Strategy makeDumbStrategy() {
 		return new DumbStrategy(makeOrderBooks());
 	}
 	
 	public Strategy makeNewStrategy() {
 		return new NewStrategy(makeOrderBooks(), makeTradeEngine());
 	}
 
 	public Strategy makeMeanReversionStrategy() {
 		return new MeanReversion(makeOrderBooks(), makeTradeEngine());
 	}
 	
 	public Strategy makeMomentumStrategy() {
 		return new Momentum( makeOrderBooks(), makeTradeEngine() );
 	}
 	
 	public Reader makeReader(String filepath) throws FileNotFoundException, IOException {
 		return new Reader(filepath);
 	}
 
 	public void setCSVColumns(String line) throws IOException {
 		//read first line and determine the index positions of columns
 
 		List<String> fieldNames = new ArrayList<String>();
 		if (line != null) {
 			// Remove the leading Hash symbol before splitting
 			fieldNames = Arrays.asList( line.replace("#", "").split(",", -1) );
 		}
 
 		INSTRUMENT     = fieldNames.indexOf("Instrument");
 		DATE           = fieldNames.indexOf("Date");
 		TIME           = fieldNames.indexOf("Time");
 		RECORD_TYPE    = fieldNames.indexOf("Record Type");
 		PRICE          = fieldNames.indexOf("Price");
 		VOLUME         = fieldNames.indexOf("Volume");
 		QUALIFIERS     = fieldNames.indexOf("Qualifiers");
 		TRANSACTION_ID = fieldNames.indexOf("Trans ID");
 		BID_ID         = fieldNames.indexOf("Bid ID");
 		ASK_ID         = fieldNames.indexOf("Ask ID");
 		BID_ASK        = fieldNames.indexOf("Bid/Ask");
 
 		// Check that all fields were successfully found
 		if ( INSTRUMENT     == -1 ||
 				DATE           == -1 ||
 				TIME           == -1 ||
 				RECORD_TYPE    == -1 ||
 				PRICE          == -1 ||
 				VOLUME         == -1 ||
 				QUALIFIERS     == -1 ||
 				TRANSACTION_ID == -1 ||
 				BID_ID         == -1 ||
 				ASK_ID         == -1 ||
 				BID_ASK        == -1 ) {
 			throw new IOException();
 		}
 	} 
 
 	public void resetCSVColumns() {
 		INSTRUMENT     = -1;
 		DATE           = -1;
 		TIME           = -1;
 		RECORD_TYPE    = -1;
 		PRICE          = -1;
 		VOLUME         = -1;
 		QUALIFIERS     = -1;
 		TRANSACTION_ID = -1;
 		BID_ID         = -1;
 		ASK_ID         = -1;
 		BID_ASK        = -1; 
 	}
 
 	//makes actual order object
 	public Order makeOrder(String time,
 			String recordType, double price, int volume,
 			String qualifiers, long   transactionID,
 			String bidAsk) {
 
 		return new Order(time, recordType, price,
 				volume, qualifiers, transactionID, bidAsk);
 	}
 
 	//converts string to order object, calling makeOrder()
 	public Order makeOrderFromCSV(String line) {
 		Order o = null;
 
 		String[] entry = line.split(",", -1); // Break line into individual fields
 
 		if (entry[RECORD_TYPE].equals("TRADE") ||
 				entry[RECORD_TYPE].equals("OFFTR") ||
 				entry[RECORD_TYPE].equals("CANCEL_TRADE")
 				
 				) {
 			o = null;
 		} else {
 			int ID_index = -1;
 			if (entry[BID_ASK].equals("B")) {
 				ID_index = BID_ID;
 			}
 			else if (entry[BID_ASK].equals("A")) {
 				ID_index = ASK_ID;
 			}
 			if (ID_index == -1) {
 				o = null;
 			} else {
 				o = makeOrder(
 						entry[TIME],
 						entry[RECORD_TYPE],
 						parseDouble(entry[PRICE]),
 						parseInteger(entry[VOLUME]),
 						entry[QUALIFIERS],
 						parseLong(entry[ID_index]),
 						entry[BID_ASK]);
 			}
 		}
 
 		return o;
 	}
 
 	// Parser which handles the case of an empty string
 	private double parseDouble(String s) {
 		if ( s.equals("") ) {
 			return EMPTY_DOUBLE_FIELD;
 		} else {
 			return Double.parseDouble(s);    
 		}
 	}
 
 	// Parser which handles the case of an empty string
 	private long parseLong(String s) {
 		if ( s.equals("") ) {
 			return EMPTY_LONG_FIELD;
 		} else {
			try {
 			return Long.parseLong(s);    
			} catch (Exception e) {
				//TODO current error, 2^63 < ID < 2^64
				return Long.MAX_VALUE;
			}
 		} 
 	}
 	
 	private int parseInteger(String s) {
 		if ( s.equals("") ) {
 			return EMPTY_INTEGER_FIELD;
 		} else {
 			return Integer.parseInt(s);    
 		} 
 	}
 
 	public Trade makeTrade(String simulatedTime, String recordType,
 			double tradePrice, int volume, String qualifier,
 			String bidAsk, Order bid, Order ask) {
 
 		return new Trade(simulatedTime, recordType, tradePrice,
 				volume, qualifier, tradeID++, bidAsk, bid, ask);
 	}
 
 }
