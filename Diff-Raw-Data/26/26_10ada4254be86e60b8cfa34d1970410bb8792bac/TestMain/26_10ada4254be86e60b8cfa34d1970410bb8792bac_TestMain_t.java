 package jp.ac.tsukuba.cs.conclave.earthquake;
 
 
 import java.io.Console;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.joda.time.Duration;
 
 import jp.ac.tsukuba.cs.conclave.earthquake.FMtest.FMBase;
 import jp.ac.tsukuba.cs.conclave.earthquake.FMtest.FMStatModel;
 import jp.ac.tsukuba.cs.conclave.earthquake.RI.RIModel;
 import jp.ac.tsukuba.cs.conclave.earthquake.data.DataList;
 import jp.ac.tsukuba.cs.conclave.earthquake.data.DataPoint;
 
 public class TestMain {
 
 	
 	
 	public static void main(String[] args) {
 		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
 		logger.setLevel(Level.FINER);
 		
 		DataList total = new DataList();
 		DataList fnet = new DataList();
 
		total.loadData("jma_cat_2000_2012_Mth2.5_formatted.dat","jma");
		total.loadData("catalog_fnet_1997_20130429_f3.txt","fnet");
		fnet.loadData("catalog_fnet_1997_20130429_f3.txt","fnet");
 		
 
 		// Getting a List of points of desired Magnitude
 		ArrayList<Integer> magPoints = fnet.getEventsByMagnitude(7, 8);
 	    DataPoint centralPoint = fnet.data.get(magPoints.get(0));
 	    Duration timewindow = new Duration(Duration.standardHours(10));
 		
 		FMBase fmtester = new FMBase();		
 		fmtester.init(centralPoint);
 		fmtester.fillAfterShockList(total, timewindow);
 		DataList aftershocks = fmtester.getAfterShockList();
 		
 		FMStatModel model1 = new FMStatModel(centralPoint, centralPoint.S[0], centralPoint.D[0], centralPoint.R[0]);		
 		FMStatModel model2 = new FMStatModel(centralPoint, centralPoint.S[1], centralPoint.D[1], centralPoint.R[1]);
 
 		model1.loadAftershocks(aftershocks);
 		model2.loadAftershocks(aftershocks);
 
 		model1.calcDistModel();
 		model1.calcMErrorModel();
 		model1.calcStrikeModel();
 
 		model2.calcDistModel();
 		model2.calcMErrorModel();
 		model2.calcStrikeModel();
 		
 		model1.test();
 		model2.test();
 		
 	}
 	
 	
 	static int[] frequencyArray(double minMag, RIModel r)
 	{
 		int[] ret = new int[r.getHighCount(minMag)+1];
 		for (int i = 0; i < r.boxsizeX; i++)
 			for (int j = 0; j < r.boxsizeY; j++)
 				ret[r.eventgrid[i][j].getEvents(minMag)]++;
 		return ret;
 	}
 }
