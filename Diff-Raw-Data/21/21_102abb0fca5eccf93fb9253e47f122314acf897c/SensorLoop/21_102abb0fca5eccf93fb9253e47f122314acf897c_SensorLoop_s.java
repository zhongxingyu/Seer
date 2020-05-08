 package eu.liveandgov.wp1.backend.sensorLoop;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.commons.lang3.StringUtils;
 import org.supercsv.io.CsvListReader;
 
 import eu.liveandgov.wp1.backend.SensorValueObjects.AccFeatureValue;
 import eu.liveandgov.wp1.backend.SensorValueObjects.AccSampleWindow;
 import eu.liveandgov.wp1.backend.SensorValueObjects.AccSensorValue;
 import eu.liveandgov.wp1.backend.SensorValueObjects.RawSensorValue;
 import eu.liveandgov.wp1.backend.SensorValueObjects.TaggedAccFeatureValue;
 import eu.liveandgov.wp1.backend.format.SampleType;
 
 public class SensorLoop {
 	private BufferedReader reader;
 	
	private static final int WINDOW_SIZE = 4;
	private static final int STEP_SIZE = 2;
 	
 	public SensorLoop(InputStream is) {
 		reader = new BufferedReader(new InputStreamReader(is));
 	}
 	
 	public void doLoop() throws IOException {
 		String line = "";
 		AccSampleWindow sw = new AccSampleWindow(WINDOW_SIZE);
 		
 		int stepCouter = 0;
 		
 		String currentTag = "none";
 		CSVFileOutput csvOut = new CSVFileOutput("sensor.csv");
		while( (line = reader.readLine()) != null ){
 			// System.out.println("<- " + line);
 			
 			RawSensorValue rsv = RawSensorValue.fromString(line); 
 			// System.out.println(rsv.toString());
 
 			// Get tags
 			if(rsv.type == SampleType.TAG) {
 				currentTag = StringUtils.strip(rsv.value, " \"");
 			}
 			
 			// Filter accelerometer values			
 			if (rsv.type != SampleType.ACC) {
 //				System.out.println("-> Not of type ACC");
 				continue;
 			}
 			
 			AccSensorValue asv = AccSensorValue.fromRSV(rsv);
 			// System.out.println(asv.toString());			
 			
 			// Fill sample window
 			sw.add(asv);
 			if (! sw.isFull()) { continue; }
 			if (! (stepCouter++ % STEP_SIZE == 0)) { continue; }
 			//System.out.println(sw.toString());
 			
 			// sample window is full here
 			TaggedAccFeatureValue af = TaggedAccFeatureValue.fromWindow(sw, currentTag);
 			System.out.println(af.toCSV());
 		}
 	}
 
 	
 }
