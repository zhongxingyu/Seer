 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 package org.acme.example.telemetry;
 
 import gov.nasa.arc.mct.components.FeedProvider;
 import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;
 
 import java.awt.Color;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Provides sine wave sample data.
  */
 public class TestDataFeed {
 	/** Data points per second. */
	private double pointsPerSecond = 10;
 	
 	/** Period of the sine wave. */
 	private double periodInSeconds = 60;
 
 	/** Amplitude of the sine wave. */
 	private double amplitude = 1;
 	
 	/** Time between each Loss Of Signal*/
 	private double losPeriodInSeconds = 60;
 	
 	/** Phase of the Loss Of Signal, relative to the base curve. */
 	private double losPhase = 0;
 
 	/** Threshold below which data is marked as invalid.  Range is -1 to 1.  */
 	private double losThreshold = 0;
 
 	private static final Color GOOD_COLOR = new Color(0, 138, 0);
 
 	private static final Color LOS_COLOR = new Color(0, 72, 217);
 
 
 	public SortedMap<Long, Map<String, String>> getData(long startTime, long endTime, TimeUnit timeUnit) {
 		startTime=TimeUnit.MILLISECONDS.convert(startTime, timeUnit);
 		endTime=TimeUnit.MILLISECONDS.convert(endTime, timeUnit);
 
 		// Align the start and end times such that each is a multiple of the period.
 		double pointsPerMS = pointsPerSecond / 1000;
 		long start = (long) (Math.ceil(startTime * pointsPerMS) / pointsPerMS);
 		long end = (long) (Math.floor(endTime * pointsPerMS) / pointsPerMS);
 		if(start > endTime || end < startTime) {
 			// no data in this range
 			return new TreeMap<Long, Map<String, String>>();
 		}
 
 		// Generate the data points in the given range.
 		SortedMap<Long, Map<String, String>> data = new TreeMap<Long, Map<String, String>>();
 		double time = start;
 		while(time <= end) {
 			double value = amplitude * Math.sin(time * 2 * Math.PI / periodInSeconds / 1000);
 			boolean valid = Math.sin(time * 2 * Math.PI / losPeriodInSeconds / 1000 + losPhase) < losThreshold;
 			Map<String, String> datum = new HashMap<String, String>();
 			datum.put(FeedProvider.NORMALIZED_IS_VALID_KEY, Boolean.toString(valid));
 			String status = valid ? " ":"S";
 			Color c = valid ? GOOD_COLOR : LOS_COLOR;
 			RenderingInfo ri = new RenderingInfo(
 					Double.toString(value),
 					c,
 					status,
 					c,
 					valid
 			);
 			ri.setPlottable(valid);
 			datum.put(FeedProvider.NORMALIZED_RENDERING_INFO, ri.toString());
 			
 			datum.put(FeedProvider.NORMALIZED_TIME_KEY, Long.toString((long) time));
 			datum.put(FeedProvider.NORMALIZED_VALUE_KEY, Double.toString(value));
 			datum.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");
 
 			data.put((long) time, datum);
 			time = Math.ceil((time + 1) * pointsPerMS) / pointsPerMS;
 		}
 		return data;
 	}
 
 
 	/**
 	 * Sets the period of the sine wave.
 	 * @param periodInSeconds period of the sine wave, in seconds
 	 */
 	public void setPeriodInSeconds(double periodInSeconds) {
 		this.periodInSeconds = periodInSeconds;
 	}
 
 
 	/**
 	 * Returns the period of the sine wave.
 	 * @return period of the sine wave, in seconds
 	 */
 	public double getPeriodInSeconds() {
 		return periodInSeconds;
 	}
 
 
 	/**
 	 * Returns the amplitude of the sine wave.
 	 * @return amplitude of the sine wave
 	 */
 	public double getAmplitude() {
 		return amplitude;
 	}
 
 
 	/**
 	 * Sets the amplitude of the sine wave.
 	 * @param amplitude amplitude of the sine wave
 	 */
 	public void setAmplitude(double amplitude) {
 		this.amplitude = amplitude;
 	}
 }
