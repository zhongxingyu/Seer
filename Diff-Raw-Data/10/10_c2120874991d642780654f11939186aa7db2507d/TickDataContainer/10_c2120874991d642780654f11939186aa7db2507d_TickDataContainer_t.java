 package com.fhx.strategy.java;
 
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.marketcetera.marketdata.interactivebrokers.LatestMarketData;
 
 import com.fhx.statstream.StatStreamHistoricalService;
 import com.fhx.statstream.StatStreamRealtimeService;
 import com.fhx.statstream.StatStreamServiceBase;
 
 /*
  * Singleton class that hold one sliding window worth of data across all symbols
  * When the container is full, the data is served to the StatStreamProto
  */
 public enum TickDataContainer {
 	INSTANCE;
 	
 	private static Logger log = Logger.getLogger(TickDataContainer.class);
 	
 	private int basicWindowCnt = 1;
 	private int basicWindowSize = -1;
 	private final Properties config = new Properties();
 	private static StatStreamServiceBase ssService;
 
 	/*
 	 * Use TreeMap to guarantee ordering
 	 * Example:
 	 * 	IBM -> [LatestMarketData1, LatestMarketData2, LatestMarketData3, ... ] 
 	 */
 	private Map<String, List<LatestMarketData>> basicWindowTicks = new TreeMap<String, List<LatestMarketData>>();
 	
 	public void init() {
 		try {
 			log.info("Initializing");
 			
 			PropertyConfigurator.configure("conf/log4j.properties");		
 			config.load(new FileInputStream("conf/statstream.properties"));
 			
 			String mode = config.getProperty("MD_SERVICE_MODE");
 			if(mode == null || "Historical".equals(mode)) {
 				log.info("Defaulting to use historical market data service");
 				ssService = new StatStreamHistoricalService();
 			}
 			else {
 				log.info("Setting to use realtime market data service");
 				ssService = new StatStreamRealtimeService();
 			}
 			ssService.init();
 			log.info("Done initializing");
 			
 			String basicWin = config.getProperty("BASIC_WINDOW_SIZE");
 			if(basicWin== null) {
 				log.error("Must define basic window size");
 			}
 			basicWindowSize = Integer.parseInt(basicWin);
 			
 		} catch (Exception e1) {
 			System.out.format("ERROR HERE\n");
 			log.error("Error loading config file\n");
 			e1.printStackTrace();
 			return;
 		}	
 	}
 	
 	public void flushBasicWindow() {
 		log.info("Serving basic window # " + basicWindowCnt + " to the StatStream model" );
 		
 		log.info("Content of basicWindowTicks");
 		StringBuffer sb = new StringBuffer();
 		for(Map.Entry<String, List<LatestMarketData>> entry : basicWindowTicks.entrySet()) {
 			String sym = entry.getKey();
 			sb.append(sym+"|");
 			List<LatestMarketData> val = entry.getValue();
 			
 			Iterator<LatestMarketData> iter = val.iterator();
 			while(iter.hasNext()) {
 				sb.append(iter.next().getLatestBid());
 				sb.append("|");
 			}
 			sb.append("\n");
 		}
 		log.info(sb.toString());
 		
 		/*
 		 * format basic window data and serve it to StatStream model
 		 */
 		// make a copy of basicWindowTicks and pass it along
 		final Map<String, List<LatestMarketData>> copyBasicWindowTicks = new TreeMap<String, List<LatestMarketData>>();
 		copyBasicWindowTicks.putAll(basicWindowTicks);		
 		ssService.tick(copyBasicWindowTicks, basicWindowCnt++);
 		
 		log.info("Flushing basic window " + basicWindowCnt + ", invoking StatStreamService");
 		log.info("Size of basic window = "+ basicWindowTicks.values().iterator().next().size());
 		basicWindowTicks.clear();
 	}
 	
 	public void addATick(Map<String, LatestMarketData> aTick) {
 
 		for(Map.Entry<String, LatestMarketData> tick : aTick.entrySet()) {
 			String symbol = tick.getKey();
			LatestMarketData data_ref = tick.getValue();
			
			//TODO: temporary deep copy implementation, better handling
			LatestMarketData data = new LatestMarketData(symbol);
			data.setBidPrice(data_ref.getLatestBid().getPrice());
			data.setOfferPrice(data_ref.getLatestOffer().getPrice());
			data.setTime(new Date());
 			
 			if (!basicWindowTicks.containsKey(symbol)) {
 				log.info("initializing arraylist for symbol "+symbol);
 				List<LatestMarketData> ticksPerSymbol = new ArrayList<LatestMarketData>();
 				ticksPerSymbol.add(data);
 				basicWindowTicks.put(symbol, ticksPerSymbol);
 			}
 			else {
 				log.info("adding new tick for symbol "+symbol);
 				basicWindowTicks.get(symbol).add(data);
 			}
 		}
 		
 		if(basicWindowTicks.values().iterator().next().size() >= basicWindowSize)			
 			flushBasicWindow();
 	}
 	
 	public Map<String, List<LatestMarketData>> getBasicWindowTicks() {
 		// client should not be able to get access to modify the data
 		return Collections.unmodifiableMap(basicWindowTicks);
 	}
 
 }
