 
 package com.eschers.eschermonitor;
 import com.rapplogic.xbee.api.*;
 import com.rapplogic.xbee.api.wpan.IoSample;
 import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
 
 import org.w3c.dom.*;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 // import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import javax.xml.parsers.*;
 import org.apache.log4j.*;
 import com.google.gson.Gson;
 
 public class EscherMonitor {
 	public enum storeType { FILE, WEB };
 	public static storeType store;
 	public static String storeURI = null;
 	public static int storeKey;
 	public static String configURI = null;
 	public static String controller = null;
 	public static String TEDURI = null;
 	public static File logFile = null;
 	public static boolean gotOneXbee = false;
 	public static int readInterval = 120;     // Max read interval for sensors is 2 minutes
 	
 	public static class xbeeCoordinatorConfig {
 		public String port;
 		public int speed;
 		public XBee xbee;
 	}
 	
 	public static class sensorConfig {    
 		public String id;
 		public String type;
 		public String name;
 		public int addressH;
 		public int addressL;
 		public float offset;  // Offset for measurement conversion
 		public float scale;   // Scale for offset conversion
 		public String interval;  // Interval in second between measurements, ends with an "s" possibly.
 		public int intervalSec; 
 		public XBeeAddress16 xbeeAddress;
 		public XBee xbee;
 		public sensorConfig makeSensor()                      
 	    {
     	  sensorConfig sensor = new sensorConfig();
     	  sensor.id = this.id;
     	  sensor.type = this.type;
     	  sensor.name = this.name;
     	  sensor.addressL = this.addressL;
     	  sensor.addressH = this.addressH;
     	  sensor.offset = this.offset;
     	  sensor.scale = this.scale;
     	  sensor.intervalSec = Integer.parseInt(this.interval.split("s")[0]);
           return sensor;
 	    } 
 
 	}
 	
 	public static class measurement {    //ToDo: Change to "xbeeMeasurement"
 		public XBeeAddress16 xbeeAddress;
 		public int metric;
 		public Date ts;
 		public measurement(XBeeAddress16 addr, int met, Date t) {
 			xbeeAddress = addr;
 			metric = met;
 			ts = t;
 		}
 	}
 	
 	private final static Logger logger = Logger.getLogger(EscherMonitor.class);
 	
 	private final static int request_key(String cntrl) {
 		int check_hash = 0;
 		byte[] a = cntrl.getBytes();
 		for (int i=0; i < a.length; i++) {
 			check_hash += a[i] + 0;
 		}
 		check_hash *= storeKey;
 		check_hash %= 65536;
 		logger.debug("Hash: " + check_hash);
 		return check_hash;
 	}
 	public static Queue<measurement> measurements = new LinkedList<measurement>();
 	
 	public static xbeeCoordinatorConfig xbeeCoordinator = null;
 	public static sensorConfig[] sensors = null;
 	
 	private final static void fileData(sensorConfig sensor, int metric, Date ts) {
 		// For file storage...
 		FileWriter storeFile = null;
 		BufferedWriter storeOut = null;
 		
 		// For web storage...
 		BufferedReader in = null;
 		DocumentBuilder responseBuilder = null;
 		URL url = null;
 		
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
 		if (store == storeType.FILE) {
 			try {
 				storeFile = new FileWriter(storeURI);
 				storeOut = new BufferedWriter(storeFile);
 			}  catch (Exception e) {
 				logger.error("Counld not open file " + storeURI);
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 
 		
 		String output = new String();
 
 
 		if (store == storeType.FILE) {
 			output = sensor.id + "," + sensor.name + "," + (metric*sensor.scale + sensor.offset) + "," + ts.toString();
 			try {
 				logger.debug("Filing measurement: " + output);
 				storeOut.write(output);
 				storeOut.newLine();
 			} catch (IOException e) {
 				logger.warn("Could not write to output file: " + output );
 			}
 		}
 		if (store == storeType.WEB) {
 			try {
 				output = "/measurements?" + "sensor_id=" + URLEncoder.encode(sensor.id,"UTF-8") + "&" + "value=" + URLEncoder.encode("" + metric, "UTF-8") + "&" + "ts=" + URLEncoder.encode(formatter.format(ts), "UTF-8") + "&" + "key=" + request_key("" + metric);
 				logger.debug("Filing measurement: " + output);
 				url = new URL(storeURI + output);
 				HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
 				connection.setDoOutput(true);
 				connection.setDoInput(true);
 				connection.setInstanceFollowRedirects(true); 
 				connection.setRequestMethod("POST"); 
 				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
 				connection.setRequestProperty("charset", "utf-8");
 				connection.setRequestProperty("Content-Length", "0");
 				connection.setUseCaches (false);
 
 				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
 				wr.flush();
 				wr.close();
 				connection.disconnect();
 				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 				String inputLine;
 				String responseText = "";
 				while ((inputLine = in.readLine()) != null)	responseText += inputLine;
 				in.close();
 				logger.debug("Response: " + responseText);
 				if (responseText.indexOf("Filed") > -1) {
 						logger.debug("Filed successfully.");
 				} else {
 					logger.warn("Filer error: " + responseText);
 				}
 								
 			} catch (Exception e) {
 				logger.error("Could not file output: " + e.getMessage());
 			}
 		}
 			
 		if (store == storeType.FILE) {
 			try {
 				storeOut.flush();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void main(String[] args) {
 		NodeList nl = null;
 		NodeList nl2 = null;
 		int i;
 		
 		String configFile = "";
 		if (args.length > 0 ) {
 			configFile = args[0];
 		}
 		if (configFile == "") {
 			configFile = "config.xml";
 		}
 		
 		PropertyConfigurator.configure("log4j.properties");
 		
 		try {
 			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			Document doc = builder.parse(configFile);
 			
 			// Parse the main nodes
 			nl = doc.getElementsByTagName("storeMethod");
 			if ((nl.item(0) != null)) {
 				String storeVal = nl.item(0).getTextContent();
 				if ((storeVal != null) && storeVal.matches("web")) {
 					store = storeType.WEB;
 				} else {
 					store = storeType.FILE;
 				}
 			}
 			logger.debug("Store type: " + store.toString());
 
 			nl = doc.getElementsByTagName("storeURI");
 			if ((nl.item(0) != null)) {
 				String storeVal = nl.item(0).getTextContent();
 				storeURI = storeVal;
 			}
 			logger.debug("Store URI: " + storeURI);
 		
 			nl = doc.getElementsByTagName("storeKey");
 			if ((nl.item(0) != null)) {
 				String storeVal = nl.item(0).getTextContent();
 				storeKey = Integer.parseInt(storeVal);
 			}
 			logger.debug("Store Key: " + storeKey);
 			
 			nl = doc.getElementsByTagName("configURI");
 			if ((nl.item(0) != null)) {
 				String storeVal = nl.item(0).getTextContent();
 				configURI = storeVal;
 			}
 			logger.debug("Config URI: " + configURI);
 
 			nl = doc.getElementsByTagName("TEDURI");
 			if ((nl.item(0) != null)) {
 				String storeVal = nl.item(0).getTextContent();
 				TEDURI = storeVal;
 			}
 			logger.debug("TED URI: " + TEDURI);
 			
 			nl = doc.getElementsByTagName("controller");
 			if ((nl.item(0) != null)) {
 				String storeVal = nl.item(0).getTextContent();
 				controller = storeVal;
 			}
 			logger.debug("Controller ID: " + controller);
 			
 			// Parse the xbee coordinator
 			nl = doc.getElementsByTagName("xbeeCoordinator");
 			if ((nl.item(0) != null)) {
 				xbeeCoordinator = new xbeeCoordinatorConfig();
 				nl2 = nl.item(0).getChildNodes();
 				for (i=0; i < nl2.getLength(); i++)
 				{
 					if (nl2.item(i).getNodeName() == "port")
 					{
 						xbeeCoordinator.port = nl2.item(i).getTextContent();
 					}
 					if (nl2.item(i).getNodeName() == "speed")
 					{
 						xbeeCoordinator.speed = Integer.parseInt(nl2.item(i).getTextContent());
 					}
 				}
 				logger.debug("XBee Coordinator Port: " + xbeeCoordinator.port);
 				logger.debug("XBee Coordinator Speed: " + xbeeCoordinator.speed);
 			}
 			
 //			nl = doc.getElementsByTagName("xbee");
 //			if ((nl != null) && (nl.getLength() > 0)) {
 //				xbees = new xbeeConfig[nl.getLength()];
 //				for (i=0; i < nl.getLength(); i++) {
 //					xbees[i] = new xbeeConfig();
 //					nl2 = nl.item(i).getChildNodes();
 //					for (j=0; j < nl2.getLength(); j++)
 //					{
 //						if (nl2.item(j).getNodeName() == "id")
 //						{
 //							xbees[i].id = nl2.item(j).getTextContent();
 //						}
 //						if (nl2.item(j).getNodeName() == "name")
 //						{
 //							xbees[i].name = nl2.item(j).getTextContent();
 //						}
 //						if (nl2.item(j).getNodeName() == "addressH")
 //						{
 //							xbees[i].addressH = Integer.parseInt(nl2.item(j).getTextContent(),16);
 //						}
 //						if (nl2.item(j).getNodeName() == "addressL")
 //						{
 //							xbees[i].addressL = Integer.parseInt(nl2.item(j).getTextContent(),16);
 //						}
 //						if (nl2.item(j).getNodeName() == "offset")
 //						{
 //							xbees[i].offset = Float.parseFloat(nl2.item(j).getTextContent());
 //						}
 //						if (nl2.item(j).getNodeName() == "scale")
 //						{
 //							xbees[i].scale = Float.parseFloat(nl2.item(j).getTextContent());
 //						}
 //						if (nl2.item(j).getNodeName() == "interval")
 //						{
 //							xbees[i].interval = Integer.parseInt(nl2.item(j).getTextContent(),10);
 //						}
 //					}
 //
 //					//Defaults
 //					if (xbees[i].interval < 1) xbees[i].interval = 10000;
 //					
 //					logger.debug("XBee ID: " + xbees[i].id);
 //					logger.debug("XBee AddressH: " + xbees[i].addressH);
 //					logger.debug("XBee AddressL: " + xbees[i].addressL);
 //					logger.debug("XBee Interval: " + xbees[i].interval);
 //				}
 //			}
 			
 			
 			Gson gson = new Gson();
 			String configCommand = new String();
 			URL configURL = null;
 			BufferedReader configIn = null;
 			
 			configCommand = "/sensors/getconfig?" + "cntrl=" + controller + "&key=" + request_key(controller);
 			logger.debug("Asking for config: " + configCommand);
 			configURL = new URL(configURI + configCommand);
 			configIn = new BufferedReader(new InputStreamReader(configURL.openStream()));
 			String inputLine;
 			String responseText = "";
 			while ((inputLine = configIn.readLine()) != null)	responseText += inputLine;
 			configIn.close();
 			logger.debug("Response: " + responseText);
 			sensorConfig[] sensorsLoad = gson.fromJson(responseText, sensorConfig[].class);  //ToDo++ change to generic sensor class. Get minimum interval for non-xbee sensors
 			sensors = new sensorConfig[sensorsLoad.length];
 			for (i=0; i<sensorsLoad.length; i++) {
 				//Defaults
 				sensors[i] = sensorsLoad[i].makeSensor();
 				if (sensors[i].intervalSec < 1) sensors[i].intervalSec = 120;
 				if ((sensors[i].type != "xbee temp") && (sensors[i].intervalSec < readInterval)) readInterval = sensors[i].intervalSec;
 				logger.debug("Sensor ID:" + sensors[i].id);
 				logger.debug("Sensor Type:" + sensors[i].type);
 				logger.debug("Sensor AddressL: " + sensors[i].addressL);
 				logger.debug("Sensor AddressH: " + sensors[i].addressH);
 				logger.debug("Sensor Interval: " + sensors[i].intervalSec);
 			}
 		}
 		catch (Exception e) {
 			// e.printStackTrace();
 			e.getMessage();
 		}
 		
 		// Create our coordinator Xbee and try to talk to it
 		xbeeCoordinator.xbee = new XBee();
 		try {
 			xbeeCoordinator.xbee.open(xbeeCoordinator.port, xbeeCoordinator.speed);
 		} catch (XBeeException e) {
 		    logger.error("Could not initialize Xbee coordinator.");
 			e.printStackTrace();
 			System.exit(1);
 		}
 		AtCommand at = new AtCommand("CH");
 		AtCommandResponse response = null;
 		try {
 			response = (AtCommandResponse) xbeeCoordinator.xbee.sendSynchronous(at, 5*1000);
 		} catch (XBeeTimeoutException e) {
 			// Failure, so none of this will work...
 			logger.error("Could not communicate with coordinator - timeout.");
 			e.printStackTrace();
 			System.exit(1);
 		} catch (XBeeException e) {
 			// Failure, so none of this will work...
 			logger.error("Could not communicate with coordinator - " + e.getMessage());
 			e.printStackTrace();
 			System.exit(1);
 		}
 		if (response.isOk()) {
 	        // success
 			logger.debug("The local channel is " + response.getValue()[0]);
 		} else {
 			// Failure, so none of this will work...
 			logger.error("Could not communicate with coordinator.");
 			System.exit(1);
 		}
 		
 		
 		// Create our Xbees and try to communicate with them ToDo: Only do for "xbee" type sensors
 		AtCommandResponse rresponse = null;
 		for (i=0; i < sensors.length; i++) {
 			logger.debug("Looking at sensor " + i + ": id=" + sensors[i].id + " type=" + sensors[i].type);
 			if (sensors[i].type.equals("xbee temp")) {
 				sensors[i].xbee = new XBee();
 				sensors[i].xbeeAddress = new XBeeAddress16(sensors[i].addressH, sensors[i].addressL);
 				RemoteAtRequest rat = new RemoteAtRequest(XBeeRequest.DEFAULT_FRAME_ID, XBeeAddress64.BROADCAST, sensors[i].xbeeAddress, false, "CH");
 				try {
 					rresponse = (AtCommandResponse) xbeeCoordinator.xbee.sendSynchronous(rat, 5*1000);
 				} catch (XBeeTimeoutException e) {
 					logger.warn("Could not communicate (timeout) with Xbee id: " + sensors[i].id);
 					continue;
 				} catch (XBeeException e) {
 					logger.warn("Could not initialize Xbee id: " + sensors[i].id);
 					e.printStackTrace();
 					continue;
 				}
 				// if (rresponse.isOk()) {  
 		        // success, set up listener
 				logger.debug("Found Xbee id: " + sensors[i].id);
 				
 				// set measurement interval
 				int[] intArr = new int[] {(int)(sensors[i].intervalSec*1000 >>> 8 & 0xff), (int)(sensors[i].intervalSec*1000 & 0xff)};
 				rat = new RemoteAtRequest(XBeeRequest.DEFAULT_FRAME_ID, XBeeAddress64.BROADCAST, sensors[i].xbeeAddress, true, "IR", intArr);
 				try {
 					rresponse = (AtCommandResponse) xbeeCoordinator.xbee.sendSynchronous(rat, 5*1000);
 				} catch (XBeeTimeoutException e) {
 					logger.warn("Could not communicate measurement interval to (timeout) Xbee id: " + sensors[i].id);
 				} catch (XBeeException e) {
 					logger.warn("Could not set measurement interval for Xbee id: " + sensors[i].id);
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		// Create a listener for the coordinator that puts entries on the queue
 		xbeeCoordinator.xbee.addPacketListener(new PacketListener() {
 		    public void processResponse(XBeeResponse response) {
 		    	if (response.getApiId() == ApiId.RX_16_IO_RESPONSE || response.getApiId() == ApiId.RX_64_RESPONSE) {
 		            RxResponseIoSample ioSample = (RxResponseIoSample)response;
 		            
 		            logger.debug("Received a sample from " + ioSample.getSourceAddress());
 		            logger.debug("RSSI is " + ioSample.getRssi());
 		            
 		            XBeeAddress16 addr = new XBeeAddress16(ioSample.getSourceAddress().getAddress());
 		            
 		            // loops IT times
 		            for (IoSample sample: ioSample.getSamples()) {          
 		                    logger.debug("Analog D0 (pin 20) 10-bit reading is " + sample.getAnalog0());
 		                    measurement ms = new measurement(addr, sample.getAnalog0(), new Date(new Date().getTime()));
 				            measurements.add(ms);
 		            }
 		    	}
 		    }
 		});
 		
 		// Create a filer loop that stores queue entries to the storage file/database (what about offsets and scale factors?)
 
 
 		sensorConfig xbeeFound = null;
 		while (true) {  //ToDo: create a reader loop for T5000 sensor
 			while (!measurements.isEmpty()) {
 				measurement ms = measurements.remove();
 				// Find the Xbee this came from
 				boolean found = false;
 				for (i=0; i<sensors.length & !found; i++) {
 					if (sensors[i].type.equals("xbee temp")) {
 						if (ms.xbeeAddress.equals(sensors[i].xbeeAddress)) {
 							found = true;
 							xbeeFound = sensors[i];
 						}
 					}
 				}
 				if (!found) {
 					logger.warn("Xbee not found for measurement with address " + ms.xbeeAddress.get16BitValue());
 				} else {
 					fileData(xbeeFound, ms.metric, ms.ts);
 				}
 			}
 			for (i=0;i < sensors.length; i++) {
 				if (sensors[i].type.equals("TED5000")) {
 					try {
 						URL TEDURL = new URL(TEDURI + "?INDEX=1&COUNT=1&MTU=" + sensors[i].addressH);
 //						BufferedReader TEDIn = new BufferedReader(new InputStreamReader(TEDURL.openStream()));
 //						String inputLine;
 //						String responseText = "";
 //						while ((inputLine = TEDIn.readLine()) != null)	responseText += inputLine;
 //						TEDIn.close();
 //						logger.debug("TED Data: " + responseText);
 						DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 						Document doc = builder.parse(TEDURL.openStream());
 						
 						// Parse the main nodes
 						int power = Integer.parseInt(doc.getElementsByTagName("POWER").item(0).getTextContent());
 						Date ts = new SimpleDateFormat("MM/dd/yyyy k:mm:ss").parse(doc.getElementsByTagName("DATE").item(0).getTextContent());
 
 						fileData(sensors[i], power, ts);
 
 					} catch (Exception e) {
 						if (e instanceof IOException) {
 							logger.debug("Error reading TED: " + e.getMessage()+ ": " + sensors[i].addressH);
							return;
 						} else {
 							e.printStackTrace();
 						}
 					}
 					
 				}
 			}
 			try {
 				Thread.sleep(readInterval*1000);  
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
