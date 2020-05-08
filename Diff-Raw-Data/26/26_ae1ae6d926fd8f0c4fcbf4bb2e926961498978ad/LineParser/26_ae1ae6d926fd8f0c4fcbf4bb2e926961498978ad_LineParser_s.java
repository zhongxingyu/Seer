 package uk.ac.dotrural.quality.edsensor.parser;
 
 import java.util.ArrayList;
 
 import uk.ac.dotrural.quality.edsensor.observation.*;
 
 public class LineParser {
 	
 	private int count = 0;
 	
 	public ArrayList<Observation> parse(String line)
 	{
 		ArrayList<Observation> observations = new ArrayList<Observation>();
 		String[] obs = line.split(",");
 		
 		try
 		{
 			
 			//String date = obs[18];
 			String date = obs[0];
 			String time = obs[1];
 			//String timeM = obs[2];
 			//String avgX = (obs[3].equals("") ? "0" : obs[3]);
 			String avgX = (obs[5].equals("") ? "0" : obs[5]);
 			//String avgY = (obs[8].equals("") ? "0" : obs[6]);
 			String avgY = (obs[8].equals("") ? "0" : obs[8]);
 			//String avgZ = (obs[9].equals("") ? "0" : obs[9]);
 			String avgZ = (obs[11].equals("") ? "0" : obs[11]);
 			//String hum = (obs[10].equals("") ? "0" : obs[10]);
 			String hum = (obs[12].equals("") ? "0" : obs[12]);
 			//String temp = (obs[11].equals("") ? "0" : obs[11]);
 			String temp = (obs[13].equals("") ? "0" : obs[13]);
 			//String lat = (obs[12].equals("") ? "0" : obs[12]);
 			String lat = (obs[14].equals("") ? "0" : obs[14]);
 			//String lon = (obs[13].equals("") ? "0" : obs[13]);
 			String lon = (obs[15].equals("") ? "0" : obs[15]);
 			//String alt = (obs[14].equals("") ? "0" : obs[14]);
 			String alt = (obs[16].equals("") ? "0" : obs[16]);
 			//String speed = (obs[15].equals("") ? "0" : obs[15]);
 			String speed = (obs[17].equals("") ? "0" : obs[17]);
 			//String sat = (obs[16].equals("") ? "0" : obs[16]);
 			String sat = (obs[18].equals("") ? "0" : obs[18]);
 			//String prec = (obs[17].equals("") ? "0" : obs[17]);
 			String prec = (obs[19].equals("") ? "0" : obs[19]);
 			String event = (obs[20].equals("") ? "0" : obs[20]);
 			//String event = "Coastal walk";
 			
 			time = date.trim() + " " + time.trim();
 			//String time = date.trim();
 			
			if(!time.equals(" ") && (count == 2) && !time.contains("VALUE"))
 			{
				observations.add(new AccelerometerObservation(ObservationType.ACCELERATION, "X", time, avgX, event));
				observations.add(new AccelerometerObservation(ObservationType.ACCELERATION, "Y", time, avgY, event));
				observations.add(new AccelerometerObservation(ObservationType.ACCELERATION, "Z", time, avgZ, event));
				observations.add(new Observation(ObservationType.HUMIDITY, time, hum, event));
				observations.add(new Observation(ObservationType.TEMPERATURE, time, temp, event));
				observations.add(new GPSObservation(ObservationType.GPS, time, lat, lon, sat, prec, event));
				observations.add(new AltitudeObservation(ObservationType.ALTITUDE, time, alt, sat, event));
				observations.add(new SpeedObservation(ObservationType.SPEED, time, speed, event));
 				count = 0;
			} else
 				count++;
 		}
 		catch(ArrayIndexOutOfBoundsException aex)
 		{
 
 		}
 		catch(Exception ex)
 		{
 			//System.out.println("LineParser Exception: " + ex.toString());
 			ex.printStackTrace();
 		}
 		return observations;
 	}
 }
