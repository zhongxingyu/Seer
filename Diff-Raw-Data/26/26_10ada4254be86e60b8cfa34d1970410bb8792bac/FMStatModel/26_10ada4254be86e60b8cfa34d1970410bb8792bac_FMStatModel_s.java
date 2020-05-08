 package jp.ac.tsukuba.cs.conclave.earthquake.FMtest;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import jp.ac.tsukuba.cs.conclave.earthquake.GeoUtils;
 import jp.ac.tsukuba.cs.conclave.earthquake.StatUtils;
 import jp.ac.tsukuba.cs.conclave.earthquake.data.DataList;
 import jp.ac.tsukuba.cs.conclave.earthquake.data.DataPoint;
 
 
 /**
  * Calculates some simple statistical models for an Focal Point Model.
  * This statistical model is based:
  *   - Distance between great circle defined by a Strike and aftershocks
  *   - Minimal distance between this FM vector and the two FM vectors of an aftershock
  *   
  * @author caranha
  */
 
 public class FMStatModel {
 
 	double model[]; // 0 - Strike, 1- Dip, 2- "R"
 	DataPoint origin;
 	
 	public ArrayList<Double> distanceErrorList; // distance between an aftershock location and the great circle of 0
 	public ArrayList<Double> modelErrorList; // distance between this model and the closest of the two models of an aftershock
 	public ArrayList<Double> strikeErrorList; // distance between the closest strike only;
 	
 	// TODO: create getters and summarizers for these
 	// Averages of the above errors
 	public double distanceAvg;
 	public double distanceDev;
 	public double modelAvg;
 	public double modelDev;
 	public double strikeAvg;
 	public double strikeDev;
 	
 	/**
 	 * 
 	 * @param o The origin point that will be used in this model
 	 * @param S strike in degrees
 	 * @param D dip in degrees
 	 * @param R R in degrees
 	 */
 	public FMStatModel(DataPoint o, double S, double D, double R)
 	{
 		origin = new DataPoint(o);
 		model = new double[3];
 		model[0] = S;
 		model[1] = D;
 		model[2] = R;
 		distanceErrorList = new ArrayList<Double>();
 		modelErrorList = new ArrayList<Double>();
 		strikeErrorList = new ArrayList<Double>();
 		// TODO: Test if the strike S belongs to the origin's list of strikes
 	}
 	
 	/**
 	 * Calculate basic statistics based on a list of Aftershocks
 	 * @param as DataList of Aftershocks
 	 */
 	public void loadAftershocks(DataList as)
 	{
 		Iterator<DataPoint> it = as.data.iterator();
 		while(it.hasNext())
 		{
 			addAftershock(it.next());
 		}
 	}
 
 	/**
 	 * Calculates the distance model. This cannot be calculated if we have too few observations (>2?)
 	 * 
 	 */
 	public void calcDistModel()
 	{
 		 // TODO: Add return or exception if the model has too few observations to calculate this
 		if (sizeDist() < 3)
 			return;
 		
		double[] ret = StatUtils.averageVariance(distanceErrorList);
 		distanceAvg = ret[0];
 		distanceDev = ret[1];		
 	}
 	
 	/**
 	 * Calculates the model error model. This cannot be calculated if we have too few observations (>2?)
 	 * 
 	 */
 	public void calcMErrorModel()
 	{
 		 // TODO: Add return or exception if the model has too few observations to calculate this
 		if (sizeFM() < 3)
 			return;
 		
 		double[] ret = StatUtils.averageVariance(modelErrorList);
 		modelAvg = ret[0];
 		modelDev = ret[1];		
 	}
 	
 	/**
 	 * Calculates the strike error model. This cannot be calculated if we have too few observations (>2?)
 	 * 
 	 */
 	public void calcStrikeModel()
 	{
 		 // TODO: Add return or exception if the model has too few observations to calculate this
 		if (sizeFM() < 3)
 			return;
 		
 		double[] ret = StatUtils.averageVariance(strikeErrorList);
 		strikeAvg = ret[0];
 		strikeDev = ret[1];		
 	}
 	
 	
 	
 	/**
 	 * @return the number of events with distance errors in this model
 	 */
 	public double sizeDist()
 	{
 		return distanceErrorList.size();
 	}
 	
 	/**
 	 * @return the number of events with model errors (only fnet events have model errors)
 	 */
 	public double sizeFM()
 	{
 		return modelErrorList.size();
 	}
 	
 	/**
 	 * Adds one more aftershock to the statistics
 	 * @param event
 	 */
 	public void addAftershock(DataPoint event)
 	{
 		distanceErrorList.add(GeoUtils.crossTrackDistance(origin.latitude, origin.longitude, model[0], 
 														  event.latitude, event.longitude));			
 		if (event.FM)
 		{
 			double mdist0 = Math.sqrt(Math.pow(GeoUtils.degreeDistance(model[0], event.S[0]),2) + 
 									   Math.pow(GeoUtils.degreeDistance(model[1], event.D[0]),2) +
 									   Math.pow(GeoUtils.degreeDistance(model[2], event.R[0]),2));
 			double mdist1 = Math.sqrt(Math.pow(GeoUtils.degreeDistance(model[0], event.S[1]),2) + 
 									   Math.pow(GeoUtils.degreeDistance(model[1], event.D[1]),2) +
 									   Math.pow(GeoUtils.degreeDistance(model[2], event.R[1]),2));
 			
 			double sdist0 = GeoUtils.degreeDistance(model[0], event.S[0]);
 			double sdist1 = GeoUtils.degreeDistance(model[0], event.S[0]);
 			
 			
 			/*
 			 * FIXME: Theoretical question: I compare both the Focal models, because I assume they 
 			 * are independend. If both focal models are acquire in order using specific equations,
 			 * it might make more sense to compare only to the respective Focal model
 			 */
 			if (mdist0 < mdist1)
 				modelErrorList.add(mdist0);
 			else
 				modelErrorList.add(mdist1);
 			
 			if (sdist0 < sdist1)
 				strikeErrorList.add(sdist0);
 			else
 				strikeErrorList.add(sdist1);
 			
 		}
 	}
 	
 
 	/**
 	 * Print all stored errors to Standard output
 	 */
 	public void test()
 	{
 		System.out.println("Contains "+sizeDist()+" distance errors and "+sizeFM()+" model errors. (Distance, Model, Strike below)");
 		System.out.println("Distance error is: "+distanceAvg+" +- "+distanceDev);
 		System.out.println("Model error is: "+modelAvg+" +- "+modelDev);
 		System.out.println("Strike error is: "+strikeAvg+" +- "+strikeDev);
 		
 //		String disterr = "";
 //		for (int i = 0; i < distanceErrorList.size(); i++)
 //			disterr = disterr + distanceErrorList.get(i) + " ";
 //		String modelerr = "";
 //		for (int i = 0; i < modelErrorList.size(); i++)
 //			modelerr = modelerr + modelErrorList.get(i) + " ";
 //		String strikeerr = "";
 //		for (int i = 0; i < strikeErrorList.size(); i++)
 //			strikeerr = strikeerr + strikeErrorList.get(i) + " ";
 //		System.out.println(disterr);
 //		System.out.println(modelerr);
 //		System.out.println(strikeerr);
 	}
 }
