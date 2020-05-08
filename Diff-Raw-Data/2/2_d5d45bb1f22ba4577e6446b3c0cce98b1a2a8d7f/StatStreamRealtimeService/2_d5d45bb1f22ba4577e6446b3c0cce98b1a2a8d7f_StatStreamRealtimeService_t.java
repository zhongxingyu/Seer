 package com.fhx.statstream;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.marketcetera.marketdata.interactivebrokers.LatestMarketData;
 import org.rosuda.REngine.REXP;
 import org.rosuda.REngine.REXPMismatchException;
 import org.rosuda.REngine.REngineException;
 import org.rosuda.REngine.RList;
 import org.rosuda.REngine.Rserve.RserveException;
 
 import com.fhx.util.StatStreamUtil;
 
 public class StatStreamRealtimeService extends StatStreamServiceBase {
 
 	private static Logger log = Logger
 			.getLogger(StatStreamRealtimeService.class);
 	
 	List<String> m_timeStamp = new ArrayList<String>();
 	List<Integer> m_winNum = new ArrayList<Integer>();
 	Map<String, List<Double>> m_midPx = new HashMap<String, List<Double>>();
 
 	public StatStreamRealtimeService() {
 	}
 
 	@Override
 	public boolean tick(Map<String, List<LatestMarketData>> aTick, int bwNum) {
 		log.info("Processing basic window " + bwNum);
 		
 		RList bwList = StatStreamUtil.getBasicWindowRList(aTick, symbols, bwNum, basicWindowSize);
 
 		try {
 			conn.assign("streamData", REXP.createDataFrame(bwList));
 
 			String corrFunc = "corr_report <- process_basic_window4(streamData, "+bwNum+")";
 			
 			log.info("calling: " + corrFunc);	
 			
 			REXP retVal = conn.parseAndEval(corrFunc);
 			conn.assign("prev_value_list", retVal);
 		
 			log.info(conn.eval("paste(capture.output(print(chopChunk)),collapse='\\n')").asString());
 			
 			//log.info(conn.eval("paste(capture.output(print(order_list)),collapse='\\n')").asString());
 			/*
 			 * parsing the order list
 			 * retVal.asList()[0] is the order list of R data frame
 			 * 		"Symbol",	"OrderType",	"Quantity",	"Price",	"BasicWinNum", "Time", "PnL"
 			 * 1	ABC			Buy				100			10			1			12:00:00	-
 			 * 1	CBA			Sell			100			10			1			12:00:00	-  
 			 */			
 			RList orderList = retVal.asList().at(0).asList();
 			if(orderList != null && orderList.size() > 0 ) {
 				int numRows = orderList.at(0).asStrings().length;
 				
 				String[] symbolColVal = orderList.at(0).asStrings();
 				String[] sideColVal = orderList.at(1).asStrings();
 				double[] qtyColVal = orderList.at(2).asDoubles();	//qty will be rounded into a round lot
 				double[] priceColVal = orderList.at(3).asDoubles(); //TODO: use mid-px at this point to place order
 
 				for(int i = 0; i < numRows; i++) {
 					addOrder(symbolColVal[i], 
 							 sideColVal[i], 
 							 (int)qtyColVal[i],
 							 priceColVal[i]);
 				}
 			}	
 			
 		} catch (RserveException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (REngineException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			log.error("Calling process_basic_window() ran into error, bwNum="+bwNum+" exiting...");
			//System.exit(-4);
 		} catch (REXPMismatchException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 }
