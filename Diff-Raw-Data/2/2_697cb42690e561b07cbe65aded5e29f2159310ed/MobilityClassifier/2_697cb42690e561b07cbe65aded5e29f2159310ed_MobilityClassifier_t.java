 package edu.ucla.cens.mobilityclassifier;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class MobilityClassifier
 {
 	private static final String STILL = "still";
 	private static final String WALK = "walk";
 	private static final String RUN = "run";
 	private static final String BIKE = "bike"; // not supported now
 	private static final String DRIVE = "drive";
 	private static final String UNKNOWN = "unknown";
 	private static final String VERSION = "1.2.8";
 	public static boolean wifiChecking = true;
 	public static String getVersion()
 	{
 		return VERSION;
 	}
 
 	/**
 	 * Take the raw sensor values and returns a classification object with the transport mode and, when applicable, features
 	 * @param accelValues
 	 * @param speed
 	 * @return
 	 */
 	public Classification classify(List<Sample> accelValues, Double speed, String wifi, String lastWifi, String lastMode)
 	{
 		// Convert from triaxial to single magnitude ArrayList in gravity units
 		ArrayList<Double> magnitudes = new ArrayList<Double>();
 		for (Sample sample : accelValues)
 		{
 			magnitudes.add(getMagnitude(sample));
 		}
 		if (lastMode == null || (!lastMode.equals("STILL") && !lastMode.equals("DRIVE")))
 			lastMode = UNKNOWN; // Not allowing any aberrant values for this
 		return getTransportMode(magnitudes, speed, wifi, lastWifi, lastMode);
 	}
 	
 	/**
 	 * Compares current WiFi point to the previous one, and returns UNKNOWN, STILL, or DRIVE
 	 * @param sample
 	 * @return Magnitude value
 	 */
 	private String checkWifi(JSONObject wifiJson, JSONObject lastWifiJson, String lastMode) throws JSONException
 	{
 		// load previous
 		
 		long time = wifiJson.getLong("time");
 		if (lastWifiJson != null)
 		{
 			long lastTime = lastWifiJson.getLong("time");
 			ArrayList<String> APsFromLastTime = JSONToList(lastWifiJson);
 
 			ArrayList<String> APs = JSONToList(wifiJson);
 			
 			// compare to APsFromLastTime
 			double same = 0;
 			double total = 0;
 			if (lastTime == time) // no new wifi data
 			{
 				return lastMode;
 			}
 
			if (lastTime < time - 1000 * 60 * 8) // if no recent wifi for comparison
 			{
 				//writeWifi(settings, time, UNKNOWN, APs);
 				return UNKNOWN;
 			}
 			// Now we can do the comparison
 			for (String AP : APs)
 			{
 				if (APsFromLastTime.contains(AP))
 					same++;
 				total++;
 			}
 			for (String AP : APsFromLastTime)
 			{
 				if (!APs.contains(AP)) // only count others that don't match. We don't count the same ones again. Change that if too many false DRIVE classifications
 					total++;
 			}
 
 			if (total > 0 && same / total < 1. / 3.)
 			{
 
 				//writeWifi(settings, time, DRIVE, APs);
 				return DRIVE;// + " " + same / total;
 			}
 			else if (total > 0)
 			{
 
 				//writeWifi(settings, time, STILL, APs);
 				return STILL;// + " " + same / total;
 			}
 			else
 			{
 
 				//writeWifi(settings, time, UNKNOWN, APs);
 				return UNKNOWN;
 			}
 		}
 		else
 		{
 
 			// no history
 			//ArrayList<String> APs = JSONToList(wifiJson);
 			//writeWifi(settings, time, UNKNOWN, APs);
 			return UNKNOWN;
 		}
 		
 
 	}
 	
 	private ArrayList<String> JSONToList(JSONObject jsonObject)
 			throws JSONException
 	{
 		ArrayList<String> list = new ArrayList<String>();
 		int strsum = 0, strcount = 0;
 		JSONObject ap;
 		JSONArray array = jsonObject.getJSONArray("scan");
 		for (int i = 0; i < array.length(); i++)
 		{
 			ap = array.getJSONObject(i);
 			strsum += ap.getInt("strength");
 			strcount++;
 			if (ap.getInt("strength") < -50)
 			{
 				list.add(ap.getString("ssid"));
 			}
 		}
 		if (list.size() == 0 && strcount > 0)
 		{
 			double avg = strsum / strcount;
 			for (int i = 0; i < array.length(); i++)
 			{
 				ap = array.getJSONObject(i);
 				strsum += ap.getInt("strength");
 				strcount++;
 				if (ap.getInt("strength") < avg)
 				{
 					list.add(ap.getString("ssid"));
 				}
 			}
 		}
 
 		return list;
 	}
 	
 	/**
 	 * Converts to gravity units and calculates the overall magnitude of the triaxial vectors.
 	 * @param sample
 	 * @return Magnitude value
 	 */
 	Double getMagnitude(Sample sample)
 	{
 		double x = sample.getX();
 	    double y = sample.getY();
 	    double z = sample.getZ();
 	    double totalForce = 0.0;
 	    double grav = 9.80665; // This is the gravity value used in the Android API
 	    
 	    totalForce += Math.pow(x/grav, 2.0);
 	    totalForce += Math.pow(y/grav, 2.0);
 	    totalForce += Math.pow(z/grav, 2.0);
 	    totalForce = Math.sqrt(totalForce);
 
 	    return totalForce;
 	}   
 	
 	/**
 	 * Calculates features (both in Android and N95 units) and calls the classifier.
 	 * @param magnitudes
 	 * @param speed
 	 * @return Classification with mode, and, if they were calculated, features
 	 */
 	private Classification getTransportMode(ArrayList<Double> magnitudes, Double speed, String wifi, String lastWifi, String lastMode)
 	{
 		double dataSize = magnitudes.size();
 		Classification classification = new Classification();
 		String wifiActivity = UNKNOWN;
 		if (wifi != null)
 		{
 			try
 			{
 				if (lastWifi == null)
 					wifiActivity = checkWifi(new JSONObject(wifi), null, lastMode);
 				else
 					wifiActivity = checkWifi(new JSONObject(wifi), new JSONObject(lastWifi), lastMode);
 			}
 			catch (JSONException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		classification.setWifiMode(wifiActivity);
 		// If there are not enough samples for feature calculation, the phone must be still
 		if (dataSize <= 10)
 		{
 			classification.setHasFeatures(false);
 			classification.setMode(STILL);
 			return classification;
 		}
 		
 		double sum = 0.0, s = 0.0;
 		double avg = 0.0, a = 0.0;
 		double var = 0.0, v = 0.0;
 		//ArrayList<Double> N95Fft = new ArrayList<Double>(5);
 		ArrayList<Double> fft = new ArrayList<Double>(10);
 
 		for(int i  = 1; i <= 10; i++)
 			fft.add(goertzel(magnitudes, (double)i, dataSize));
 
 		for (int i = 0; i < dataSize; i++)
 		{
 			s += magnitudes.get(i);
 		}
 		a = s / dataSize;
 		s = 0.0;
 		for (int i = 0; i < dataSize; i++)
 		{
 			s += Math.pow((magnitudes.get(i) - a), 2.0);
 		}
 
 		v = s / dataSize;
 
 		for (int i = 0; i < dataSize; i++)
 		{
 
 			magnitudes.set(i, magnitudes.get(i) * 310.); // convert to N95 units
 
 		}
 
 		for (int i = 0; i < dataSize; i++)
 		{
 			sum += magnitudes.get(i);
 		}
 
 		//avg = sum / dataSize;
 		//sum = 0.0;
 		/*for (int i = 0; i < dataSize; i++)
 		{
 			sum += Math.pow((magnitudes.get(i) - avg), 2.0);
 		}
 		var = sum / dataSize;
 		for (int i = 1; i <= 5; i++) 
 			N95Fft.add(goertzel(magnitudes, (double)i, dataSize));*/
 		
 		// Call classifier
 	//	classification.setMode(activity(var, N95Fft.get(0), N95Fft.get(1), N95Fft.get(2), speed, a, v, fft.get(0), fft.get(1), fft.get(2), fft.get(3), 
 				//fft.get(4), fft.get(5), fft.get(6), fft.get(7), fft.get(8), fft.get(9)));
 		
 		String activity = activity(speed,a,v, fft.get(0), fft.get(1), fft.get(2), fft.get(3), fft.get(4), fft.get(5), fft.get(6), fft.get(7), fft.get(8), fft.get(9));
 		
 		if (wifiChecking && !wifiActivity.equals(UNKNOWN))
 		{
 			if (activity.equals(DRIVE) || activity.equals(STILL))
 				activity = wifiActivity; // The other classifier is rubbish for still/drive; just use WiFi result if there is one
 		}
 		
 		classification.setMode(activity);
 		// Add features to Classification object
 		classification.setAverage(a);
 		classification.setVariance(v);
 		classification.setFft(fft);
 	//	classification.setN95Fft(N95Fft);
 	//classification.setN95Variance(var);
 		classification.setHasFeatures(true);
 		return classification;
 	}
 
 	/**
 	 * This classifier is used at very low speeds or when GPS in unavailable. It only uses the accelerometer features.
 	 * Since it was not ported from the N95, it operates only on native Android units.
 	 * @param var
 	 * @param avg
 	 * @param a1
 	 * @param a2
 	 * @param a3
 	 * @param a4
 	 * @param a5
 	 * @param a6
 	 * @param a7
 	 * @param a8
 	 * @param a9
 	 * @param a0
 	 * @return Classification object with the mode
 	 */
 	private String indoorActivity(double var, double avg, double a1, double a2, double a3, double a4, double a5, double a6, double a7, double a8, double a9, double a0)
 	{
 		if (var <= 0.0047)
 		{
 			if (var <= 0.0016)
 				return STILL;
 			else
 			{
 				if (a5 <= 0.1532)
 				{
 					if (a1 <= 0.5045)
 						return STILL;
 					else
 						return WALK;
 				} else
 					return STILL;
 			}
 		} else
 		{
 			if (a3 <= 60.3539)
 			{
 				if (var <= 0.0085)
 				{
 					if (a8 <= 0.0506)
 						return WALK;
 					else
 					{
 						if (a2 <= 2.8607)
 							return STILL;
 						else
 							return WALK;
 					}
 				} else
 				{
 					if (a2 <= 2.7725)
 					{
 						if (a1 <= 13.0396)
 							return WALK;
 						else
 							return STILL;
 					} else
 						return WALK;
 				}
 			} else
 				return RUN;
 		}
 
 	}
 
 	/**
 	 * This is the main classification method. Earlier code
 	 * @param acc_var
 	 * @param accgz1
 	 * @param accgz2
 	 * @param accgz3
 	 * @param gps_speed
 	 * @param avg
 	 * @param var
 	 * @param a1
 	 * @param a2
 	 * @param a3
 	 * @param a4
 	 * @param a5
 	 * @param a6
 	 * @param a7
 	 * @param a8
 	 * @param a9
 	 * @param a0
 	 * @return Classification object with the mode
 	 */
 	/*private String activity(double acc_var, double accgz1, double accgz2, double accgz3, double gps_speed, double avg, double var, double a1, double a2, double a3, double a4, double a5,
 			double a6, double a7, double a8, double a9, double a0)
 	{
 		String output = STILL;
 		if (gps_speed <= 0.29)
 			output = indoorActivity(var, avg, a1, a2, a3, a4, a5, a6, a7, a8, a9, a0);// STILL;
 		else if (accgz3 <= 2663606.69633)
 			if (gps_speed <= 6.37)
 				if (accgz2 <= 463400.011249)
 					if (acc_var <= 205.972492)
 						if (acc_var <= 13.084102)
 							if (gps_speed <= 0.8)
 								output = STILL;
 							else
 								output = DRIVE;// BIKE;
 						else if (gps_speed <= 1.33)
 							output = STILL;// BIKE;
 						else
 							output = DRIVE;
 					else if (gps_speed <= 1.84)
 						if (accgz1 <= 125502.942136)
 							output = WALK;// BIKE;
 						else
 							output = WALK;
 					else
 						output = BIKE;// BIKE;
 
 				else if (acc_var <= 41153.783729)
 					if (gps_speed <= 2.12)
 						output = WALK;
 					else
 						output = BIKE;
 				else
 					output = RUN;
 			else
 				output = DRIVE;
 
 		else if (accgz3 <= 5132319.94693)
 			if (gps_speed <= 1.86)
 				output = WALK;// bike
 			else
 				output = RUN;
 		else
 			output = RUN;
 	return output;
 	}*/
 	
 	/**
 	 * This is the main classification method. Updated code after retraining
 	 * @param acc_var
 	 * @param accgz1
 	 * @param accgz2
 	 * @param accgz3
 	 * @param gps_speed
 	 * @param avg
 	 * @param var
 	 * @param a1
 	 * @param a2
 	 * @param a3
 	 * @param a4
 	 * @param a5
 	 * @param a6
 	 * @param a7
 	 * @param a8
 	 * @param a9
 	 * @param a0
 	 * @return Classification object with the mode
 	 */	
 	private String activity(Double gps_speed, double avg, double var, double a1, double a2, double a3, double a4, double a5,
 			double a6, double a7, double a8, double a9, double a0)
 	{
 		String output = STILL;
 
 		if(var <= 0.016791)
 		{
 			if(a6 <= 0.002427)
 			{
 				/*if(a7 <= 0.001608)
 				{*/
 					if( gps_speed <= 0.791462 || gps_speed.isNaN())//|| gps_speed != Double.NaN)
 					{
 						
 //						if(avg <= 0.963016)
 //						{
 //							output = STILL;
 //						}
 //						else  if(avg <= 0.98282)
 //						{
 //							output = DRIVE;Log.d(TAG, "Drive 0 because gps speed is " + gps_speed + " and avg is " + avg);
 //						}
 //						else if(avg <= 1.042821)
 //						{
 //							if(avg <= 1.040987)
 //							{
 //								if(avg <= 1.037199)
 //								{
 //									if(avg <= 1.03592)
 //									{
 //										output = STILL;
 //									}
 //									else 
 //									{
 //										output = DRIVE;
 //									}
 //								}
 //								else
 //								{
 //									output = STILL;
 //								}
 //							}
 //							else
 //							{
 //								output = DRIVE;
 //							}
 //						}
 //						else
 						{
 						 	output = STILL;
 						}
 					}
 					else
 					{
 						output = DRIVE;
 					}
 				/*}
 				else
 				{
 					output = DRIVE;
 				}*/
 			}
 			else if(gps_speed <= 0.791462 || gps_speed.isNaN())//&& gps_speed != Double.NaN)
 			{
 				output = STILL;
 			}
 			else
 			{
 				output = DRIVE;
 			}
 		}
 		else
 		{
 			if(a3 <= 16.840921)
 			{
 				output = WALK;
 			}
 			else
 			{
 				output = RUN;	
 			}
 		}
 
 		return output;
 
 	}
 
 	/**
 	 * Calculates FFTs
 	 * @param accData
 	 * @param freq
 	 * @param sr
 	 * @return FFT value
 	 */
 	private double goertzel(ArrayList<Double> accData, double freq, double sr)
 	{
 		double s_prev = 0;
 		double s_prev2 = 0;
 		double coeff = 2 * Math.cos((2 * Math.PI * freq) / sr);
 		double s;
 		for (int i = 0; i < accData.size(); i++)
 		{
 			double sample = accData.get(i);
 			s = sample + coeff * s_prev - s_prev2;
 			s_prev2 = s_prev;
 			s_prev = s;
 		}
 		double power = s_prev2 * s_prev2 + s_prev * s_prev - coeff * s_prev2 * s_prev;
 
 		return power;
 	}
 	
 //	public static void main(String [] args)
 //	{
 //		// List<Sample> accelValues, Double speed, String wifi, String lastWifi, String lastMode
 //		ArrayList<Sample> accelValues = new ArrayList<Sample>();
 //		Sample sample = new Sample();
 //		sample.setX(0.);
 //		sample.setY(0.);
 //		sample.setZ(1.);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		accelValues.add(sample);
 //		String last = "{\"timezone\":\"org.apache.harmony.luni.internal.util.ZoneInfo[\\\"PST\\\",mRawOffset=-28800000,mUseDst=true]\",\"time\":1325716855277,\"scan\":[{\"ssid\":\"00:27:0d:ed:35:61\",\"strength\":-91},{\"ssid\":\"00:1a:1e:81:96:41\",\"strength\":-88},{\"ssid\":\"00:23:69:0d:7a:d8\",\"strength\":-88},{\"ssid\":\"00:11:24:a9:82:a4\",\"strength\":-87},{\"ssid\":\"00:1a:1e:81:96:43\",\"strength\":-87},{\"ssid\":\"00:1a:1e:81:96:45\",\"strength\":-87},{\"ssid\":\"00:1a:1e:1f:3a:24\",\"strength\":-84},{\"ssid\":\"00:1a:1e:1f:3a:25\",\"strength\":-83},{\"ssid\":\"00:1a:1e:1f:3a:22\",\"strength\":-83},{\"ssid\":\"00:1a:1e:89:4b:82\",\"strength\":-82},{\"ssid\":\"00:1a:1e:1f:3a:23\",\"strength\":-82},{\"ssid\":\"00:1a:1e:89:4b:83\",\"strength\":-82},{\"ssid\":\"00:1a:1e:89:4b:81\",\"strength\":-81},{\"ssid\":\"00:17:5a:b7:ef:90\",\"strength\":-60},{\"ssid\":\"00:1a:1e:1f:3c:c4\",\"strength\":-56},{\"ssid\":\"00:1a:1e:1f:3c:c5\",\"strength\":-53},{\"ssid\":\"00:1a:1e:1f:3c:c2\",\"strength\":-53},{\"ssid\":\"00:1a:1e:1f:3c:c1\",\"strength\":-51}]}";
 //		String current = "{\"timezone\":\"org.apache.harmony.luni.internal.util.ZoneInfo[\\\"PST\\\",mRawOffset=-28800000,mUseDst=true]\",\"time\":1325716976116,\"scan\":[{\"ssid\":\"00:1a:1e:81:96:41\",\"strength\":-88},{\"ssid\":\"00:27:0d:ed:35:62\",\"strength\":-88},{\"ssid\":\"00:27:0d:ed:35:60\",\"strength\":-88},{\"ssid\":\"00:1a:1e:89:4b:83\",\"strength\":-82},{\"ssid\":\"00:1a:1e:89:4b:82\",\"strength\":-81},{\"ssid\":\"00:1a:1e:89:4b:85\",\"strength\":-81},{\"ssid\":\"00:1a:1e:1f:3a:24\",\"strength\":-80},{\"ssid\":\"00:1a:1e:1f:3a:22\",\"strength\":-80},{\"ssid\":\"00:1a:1e:89:4b:81\",\"strength\":-76},{\"ssid\":\"00:17:5a:b7:ef:90\",\"strength\":-61},{\"ssid\":\"00:1a:1e:1f:3c:c5\",\"strength\":-54},{\"ssid\":\"00:1a:1e:1f:3c:c4\",\"strength\":-53},{\"ssid\":\"00:1a:1e:1f:3c:c1\",\"strength\":-53},{\"ssid\":\"00:1a:1e:1f:3c:c2\",\"strength\":-53}]}";
 //		System.out.println(new MobilityClassifier().classify(accelValues, 1.0, current, last, UNKNOWN).getMode());
 //	}
 }
