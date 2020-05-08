 package stockmaster.manager;
 
 import java.util.ArrayList;
 
 import com.sun.org.apache.bcel.internal.generic.ALOAD;
 
 import stockmaster.algo.TradingAlgorithm;
 import stockmaster.marketdata.*;
 import stockmaster.recorder.DataRecorder;
 import stockmaster.unit.MarketDataInfo;
 import stockmaster.util.Log;
 
 //Class to handle all MarketData interactions
 
 public class MarketDataManager {
 
 	private ArrayList<MarketData> marketDataList;
 	private ArrayList<TradingAlgorithm> algoList;
 	private ArrayList<DataRecorder>  recorderList;
 	
 	public MarketDataManager() {
 	
 		marketDataList = new ArrayList<>();
 		algoList = new ArrayList<>();
 		recorderList = new ArrayList<>();
		init();
 	}
 	
 	protected void addMarketData(MarketData marketData){
 		marketDataList.add(marketData);
 	}
 	
 	protected void removeMarketData(MarketData marketData){
 		marketDataList.remove(marketData);
 	}
 	
 	public void loadAlgo(TradingAlgorithm algo) {
 		algoList.add(algo);
 		Log.info(this, "Algorithm loaded: "+algo.getClass().getName());
 	}
 	
 	public void loadRecorder(DataRecorder recorder) {
 		recorderList.add(recorder);
 		Log.info(this, "Recorder loaded: "+ recorder.getClass().getName());
 		
 	}
 	
 	//Loads all recorders into MarketData
 	protected void loadRecorders(MarketData marketData){
 		
 		for(DataRecorder recorder: recorderList){
 			marketData.subscribe(recorder);
 		}
 	}
 	
 	//Loads all algos into MarketData
 	protected void loadAlgos(MarketData marketData){
 		
 		for(TradingAlgorithm algo: algoList){
 			marketData.subscribe(algo);
 		}
 	}
 	
 	//Area to add all required Market Data preparation logic
 	protected void init(){
 		
 		//MarketDataEmulatorImpl init
 			//MarketDataEmulatorImpl emuData = new MarketDataEmulatorImpl(Market.NEUTRAL, new MarketDataInfo())
 			//addMarketData(emuData);
 				
 		
 		//Mongo Replayer init
 			//ReplayMongoMarketDataImpl mogData = new ReplayMongoMarketDataImpl("sgx", Log.getFormattedDateTime("20120904 22:00:00"), Log.getFormattedDateTime("20120924 22:15:00"), new MarketDataInfo());
 		
 		// Replayer SGX Web Market Data init
 				ArrayList<String> dateList = new ArrayList<String>();
 				dateList.add("20120904");
 				ReplayCSVMarketDataImpl rplData = new ReplayCSVMarketDataImpl("FeedData//", dateList, "SGX", new MarketDataInfo());
				addMarketData(rplData);
 				
 				
 		//SGXWebMarketDataImpl init
 				SGXWebMarketDataImpl sgxData = new SGXWebMarketDataImpl(rplData.getMarketDataInfo());
 				//loadAlgos(sgxData);
				//loadRecorders(sgxData)''
 				addMarketData(sgxData);
 				
 				
 	}
 	
 	
 	
 	public void execute(){

 		for(MarketData data : marketDataList){
 		data.start();	
 		}
 	}
 }
