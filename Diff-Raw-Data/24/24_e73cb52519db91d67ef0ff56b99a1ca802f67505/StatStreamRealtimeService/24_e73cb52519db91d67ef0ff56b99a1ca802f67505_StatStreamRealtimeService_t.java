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
 
import com.fhx.service.ib.marketdata.IBOrderService;
 import com.fhx.util.StatStreamUtil;
 
 public class StatStreamRealtimeService extends StatStreamServiceBase {
 
 	private static Logger log = Logger.getLogger(StatStreamRealtimeService.class);
 	
 	List<String> m_timeStamp = new ArrayList<String>();
 	List<Integer> m_winNum = new ArrayList<Integer>();
 	Map<String, List<Double>> m_midPx = new HashMap<String, List<Double>>();
 
 	public StatStreamRealtimeService() {
 		
 	}
 
 	@Override
 	public boolean tick(Map<String, List<LatestMarketData>> aTick, int bwNum) {
		log.info("Dump all open positions at basic window " + bwNum);
		Map<String, Integer> positions = IBOrderService.getInstance().getCurrenctPositions();
		
		for(Map.Entry<String, Integer> pos : positions.entrySet()) {
			log.info("pos -> " + pos.getKey() + "|" + pos.getValue());
		}
 		
 		RList bwList = StatStreamUtil.getBasicWindowRList(aTick, symbols, bwNum, basicWindowSize);
 
 		try {
 			// source the func file, do this everytime so the function file can be updated real-time, COOL
 			String funcRFile = config.getProperty("R_FUNC_SCRIPT");
 			String cmdStr = "source('"+funcRFile+"')";
 			
 			log.info("try to source R (func) file: " + cmdStr);
 			conn.parseAndEval(cmdStr);
 			
 			// next process func call
 			conn.assign("streamData", REXP.createDataFrame(bwList));
 			String execFunc = config.getProperty("R_FUNC_EXEC");
 
 			if("True".equals(config.getProperty("TEST_ORDER_MODE")))
 				execFunc = config.getProperty("R_FUNC_TEST_ORDER");
 			
 			String funcExpr = String.format("%s(%s, %d)", execFunc, "streamData", bwNum); 			
 			log.info("R_FUNC_EXEC: " + funcExpr);
 			
 			REXP retVal = conn.parseAndEval(funcExpr);
 		
 			String funcEval = config.getProperty("R_FUNC_EVAL");
 			log.info("R_FUNC_EVAL: " + funcEval);
 			log.info("order_list from R: " +conn.eval(funcEval).asString());
 			
 			//log.info(conn.eval("paste(capture.output(print(order_list)),collapse='\\n')").asString());
 			/*
 			 * parsing the order list
 			 * retVal.asList()[0] is the order list of R data frame
 			 * 		"Symbol",	"OrderType",	"Quantity",	"Price",	"BasicWinNum", "Time", "PnL"
 			 * 1	ABC			Buy				100			10			1			12:00:00	-
 			 * 1	CBA			Sell			100			10			1			12:00:00	-  
 			 */	
 			if(retVal == null) {
 				log.error("retVal is null");
 				return false;
 			}
 			if(retVal.asList() == null) {
 				log.error("retVal.asList is null");
 				return false;
 			}
 			if(retVal.asList().at(0) == null) {
 				log.error("retVal.asList.at(0) is null");
 				return false;
 			}
 			
 			RList orderList = retVal.asList();
 			log.info("order list size = " + orderList.size());
 			if(orderList != null && orderList.size() > 0 ) {
 				String[] symbolColVal = orderList.at(0).asStrings();
 				String[] sideColVal = orderList.at(1).asStrings();
 				double[] qtyColVal = orderList.at(2).asDoubles();	//qty will be rounded into a round lot
 				double[] priceColVal = orderList.at(3).asDoubles(); //TODO: use mid-px at this point to place order
 
 				log.info("model returned order "+symbolColVal[0]+"|"+sideColVal[0]+"|"+(int)qtyColVal[0]+"|"+priceColVal[0]);
 				addOrder(symbolColVal[0], 
 						 sideColVal[0], 
 						 (int)qtyColVal[0],
 						 priceColVal[0]);
 			}	
 			
 		} catch (RserveException e) {
 			e.printStackTrace();
 		} catch (REngineException e) {
 			e.printStackTrace();
 			log.error("Calling process_bw_ticks() ran into error, bwNum="+bwNum+", exiting...");
 			//System.exit(-4);
 		} catch (REXPMismatchException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return true;
 	}
 	
 }
