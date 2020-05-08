 package edu.pugetsound.npastor.utils;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import edu.pugetsound.npastor.riderGen.TripGenerator;
 import edu.pugetsound.npastor.utils.RiderChars.DayDivision;
 
 /**
  * Building the route cache takes forever. But what if I need to change trip
  * distributions across the day for a given data set? This utility takes an 
  * existing data set, modifies the time distributions of the trips, and spits 
  * out a new trips_vehicles file. No need to spend three days generating an 
  * entirely new data set.
  * @author Nathan P
  *
  */
 public class TripRedistributor {
 	
 	private static final String TAG = "TripRedistributor";
 
 	private String mDestinationDir;
 	private String mSourceDir;
 	private RiderChars mRiderChars;
 	private ArrayList<Trip> mTrips;
 	private Random mRandom;
 	
 	public static void main(String[] args) {
 		// Pass in the source directory and begin redistribution
 		new TripRedistributor(args[0]).redistribute();		
 	}
 	
 	public TripRedistributor(String sourceDir) {
 		mDestinationDir = Constants.SIM_BASE_DIRECTORY 
 				+ "/redistrib" 
 				+ DRTUtils.formatMillis(System.currentTimeMillis());
 		new File(mDestinationDir).mkdirs();
 		mRiderChars = new RiderChars(false);
 		mSourceDir = sourceDir;
 		mRandom = new Random();
 	}
 	
 	public void redistribute() {
 		// Parse the trips out of the file source file
 		TripGenerator tripGen = new TripGenerator(false);
 		mTrips = tripGen.generateTripsFromFile(mSourceDir 
				+ Constants.TRIPS_VEHICLES_TXT, false);
 		
 		// Do the redistribution
 		generatePickupTimes();
 		
 		// And write the result to file
 		writeTripsToFile(mTrips);
 	}
 	
 	/**
 	 * Generates a trip pickup time and the time when the trip was made known 
 	 * to the agency. Both in minute precision
 	 */
 	private void generatePickupTimes() {
 		
 		Log.iln(TAG, "Generating pickup times");
 		int minRequestableTime = Constants.BEGIN_OPERATION_HOUR * 60;
 		int maxRequestWindow = Constants.END_REQUEST_WINDOW * 60;
 		
 		Double[] percentByHour = buildDayDistribution();
 		double percentDynamicRequests = mRiderChars.getDynamicRequestPct();
 		Log.iln(TAG, "Static rides: " + (100 - percentDynamicRequests) 
 				+ "%. Dynamic rides: " + percentDynamicRequests + "%");
 		
 		int unsatisfiableConditions = 0;
 		for(Trip t : mTrips) {
 			int requestTime = 0;
 			int callInTime = 0;
 			
 			// First calculate request time			
 			double random = mRandom.nextDouble() * 100;
 			double runningTotal = 0;
 			for(int i = Constants.BEGIN_OPERATION_HOUR; i < Constants.END_OPERATION_HOUR; i++) {
 				runningTotal += percentByHour[i];
 				if(random < runningTotal) {
 					requestTime = i;
 					break;
 				}
 			} 
 			int mins = mRandom.nextInt(60);
 			requestTime = requestTime * 60 + (mins);
 	
 			// Then set time at which trip was requested
 			double requestVal = mRandom.nextDouble() * 100;
 			if(requestVal < percentDynamicRequests) {
 				// Max request time will be the agency determined cutoff, 
 				// or the requested time minus the buffer
 				int maxRequestTime = Math.min(requestTime - 
 						Constants.CALL_REQUEST_BUFFER_MINS, maxRequestWindow);
 				if(maxRequestTime < minRequestableTime) {
 					//TODO: fix this!!
 					unsatisfiableConditions++;
 				} else {
 					int windowMins = maxRequestTime - minRequestableTime;
 					callInTime = mRandom.nextInt(windowMins + 1) 
 							+ minRequestableTime;
 				}
 			}
 			t.setPickupTime(requestTime);
 			t.setCalInTime(callInTime);
 		}
 		Log.iln(TAG, "Dynamic request: There were " + unsatisfiableConditions 
 				+ " times where max request time is less than "
 				+ "minimum requestable time. Unsatisfiable condition");
 	}
 	
 	/**
 	 * Using trip distribution data from RiderChars file, makes a list of trip 
 	 * distribution by hour
 	 * @return
 	 */
 	private Double[] buildDayDistribution() {
 		Double[] percentByHour = new Double[24];
 		HashMap<Integer, DayDivision> tripsByPeriod 
 			= mRiderChars.getTripDistributions();
 		
 		// First do peak periods
 		DayDivision morningPeak = tripsByPeriod.get(Constants.MORN_PEAK_PERIOD);
 		int hrLength = morningPeak.getLength();
 		for(int i = 0; i < hrLength; i++) {
 			percentByHour[i + morningPeak.getStart()] 
 					= morningPeak.getPercentage() / hrLength;
 		}
 		DayDivision afternoonPeak 
 			= tripsByPeriod.get(Constants.AFTNOON_PEAK_PERIOD);
 		hrLength = afternoonPeak.getLength();
 		for(int i = 0; i < hrLength; i++) {
 			percentByHour[i + afternoonPeak.getStart()] 
 					= afternoonPeak.getPercentage() / hrLength;
 		}
 		
 		// Then fill in the rest of the day
 		DayDivision nonPeak = tripsByPeriod.get(Constants.MORNING_PERIOD);
 		hrLength = morningPeak.getStart() - Constants.BEGIN_OPERATION_HOUR;
 		for(int i = 0; i < hrLength; i++) {
 			percentByHour[Constants.BEGIN_OPERATION_HOUR + i] 
 					= nonPeak.getPercentage() / hrLength;
 		}
 		nonPeak = tripsByPeriod.get(Constants.DAY_PERIOD);
 		hrLength = afternoonPeak.getStart() - morningPeak.getStart() 
 				- morningPeak.getLength();
 		for(int i = 0; i < hrLength; i++) {
 			percentByHour[morningPeak.getStart() + morningPeak.getLength() + i]
 					= nonPeak.getPercentage() / hrLength;
 		}
 		nonPeak = tripsByPeriod.get(Constants.EVENING_PERIOD);
 		hrLength = Constants.END_OPERATION_HOUR - afternoonPeak.getStart() 
 				- afternoonPeak.getLength();
 		for(int i = 0; i < hrLength; i++) {
 			percentByHour[afternoonPeak.getStart() 
 			              + afternoonPeak.getLength() + i]
 	            		  = nonPeak.getPercentage() / hrLength;
 		}
 
 		return percentByHour;
 	}
 	
 	/**
 	 * Writes the generated trips to 2 files. There is a "readable" file which 
 	 * contains an easier to read representation of the trips, and a separate 
 	 * file which contains an easier to parse representation. The latter is 
 	 * used for generateTripsFromFile()
 	 */
 	private void writeTripsToFile(ArrayList<Trip> trips) {
 		
 		// Build text lists
 		ArrayList<String> parsableText = new ArrayList<String>();
 		ArrayList<String> readableText = new ArrayList<String>();
 		for(Trip t : trips) {
 			parsableText.add(TripGenerator.TRIP_FILE_LBL 
 					+ " " + t.toStringSpaceSeparated());
 			// Get rid of all line breaks
 			readableText.add(t.toString().replace("\n", "")); 
 		}
 		
 		// Write to file
 		writeTxtFile(parsableText, Constants.TRIPS_VEHICLES_TXT, false);
 		writeTxtFile(readableText, Constants.TRIPS_READABLE_TXT, false);
 	}
 	
 	private void writeTxtFile(ArrayList<String> text, String filename, boolean readOnly) {
 		
 		// Get filename
 		String path = mDestinationDir + filename;
 		
 		// Write to file
 		try {
 			FileWriter writer = new FileWriter(path, true);
 			PrintWriter lineWriter = new PrintWriter(writer);
 			
 			for(String str : text) {
 				// Write to file
 				lineWriter.println(str);
 			}
 			lineWriter.close();
 			writer.close();
 			
 			if(readOnly) {
 				boolean result = new File(path).setReadOnly();
 				if(!result)
 					Log.e(TAG, "Unable to make file read only at: " + path);
 			}
 			Log.iln(TAG, "Text file successfully written at: " + path);
 		} catch (IOException ex) {
 			Log.e(TAG, "Unable to write to file at: " + path);
 			ex.printStackTrace();
 		}
 	}
 }
