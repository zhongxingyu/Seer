 package edu.harvard.cs262.grading;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 public class ConfigReaderImpl implements ConfigReader {
 	
 	private static final String DEFAULT_CONFIG = "config/services.config";
 	private Map<String, List<String>> Service2IP = new HashMap<String, List<String>>(); 
 	
 	public ConfigReaderImpl() {
 		this(DEFAULT_CONFIG);
 	}
 	
 	public ConfigReaderImpl(String file) {
 		try {
 			// Set Up File IO
 			FileInputStream fstream = new FileInputStream(file);
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			
			Pattern p = Pattern.compile("//\\d{1,3}.*");
             String strLine = "", currService = "";
             ArrayList<String> IPs = new ArrayList<String>();
 			while((strLine = br.readLine()) != null) {
				if (!p.matcher(strLine).matches()) {
 					if(!IPs.isEmpty()) {
 						Service2IP.put(currService, IPs);
 					}
 					IPs = new ArrayList<String>();
 					currService = strLine;
 				} else {
 					IPs.add(strLine);
 				}
 			}
 			if(!IPs.isEmpty()) {
 				Service2IP.put(currService, IPs);
 			}
 		}
 		catch (Exception e) { // generic
 			System.err.println("Error: cannot open file " + file);
		}
			
 	}
 
 	@Override
 	public List<String> getService(String service) {
 		if(Service2IP.containsKey(service)) {
 			return Service2IP.get(service);
 		}
 		return new ArrayList<String>();
 	}
 }
