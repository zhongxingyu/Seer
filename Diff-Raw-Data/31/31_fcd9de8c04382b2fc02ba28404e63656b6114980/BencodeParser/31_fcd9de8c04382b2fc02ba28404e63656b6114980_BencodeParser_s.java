 package com.yuktix.data.parser;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.nio.charset.StandardCharsets;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeMap;
 
 import com.yuktix.bencode.ds.DictionaryType;
 import com.yuktix.bencode.ds.IBencodeType;
 import com.yuktix.bencode.scan.StreamScanner;
 import com.yuktix.dto.provision.Reading;
 import com.yuktix.rest.exception.ArgumentException;
 import com.yuktix.util.Log;
 
 public class BencodeParser {
 	
 	private List<Reading> readings;
 	private String projectId ;
 	private String serialNumber ;
 	
 	public BencodeParser() {
 		this.readings = new ArrayList<Reading>();
 		this.projectId = null ;
 		this.serialNumber = null ;
 		
 	}
 	
 	public List<Reading> getReadings() {
 		return readings;
 	}
 	
 	public void setReadings(List<Reading> readings) {
 		this.readings = readings;
 	}
 
 	public String getProjectId() {
 		return projectId;
 	}
 
 	public void setProjectId(String projectId) {
 		this.projectId = projectId;
 	}
 	
 	public String getSerialNumber() {
 		return serialNumber;
 	}
 
 	public void setSerialNumber(String serialNumber) {
 		this.serialNumber = serialNumber;
 	}
 
 	public void parse(String data) throws IOException {
 		
 		DictionaryType dict = null ;
 		ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes(StandardCharsets.US_ASCII)) ;
 		StreamScanner scanner = new StreamScanner() ;
 		
 		scanner.munch(bis);
 		List<IBencodeType> list = scanner.getElements() ;
 		
 		if(list.size() == 1) {
 			IBencodeType elem = list.get(0) ;
 			if(elem instanceof DictionaryType) {
 				dict = (DictionaryType) elem ;
 			}
 		}
 		
 		if(dict == null ) {
 			throw new ArgumentException("Bencoded dictionary is missing from data") ;
 		}
 		
 		TreeMap<byte[], IBencodeType> map = dict.getMap() ;
 		Set<byte[]> bkeys = map.keySet() ;
 		
 		for(byte[] bkey : bkeys) {
 			String key = new String(bkey,StandardCharsets.US_ASCII) ;
 			// required keys
 			if(key.equals("projectId")) {
 				this.projectId = map.get(bkey).toString();
			}
				
			if(key.equals("serialNumber")) {
 				this.serialNumber = map.get(bkey).toString();
			}
			
			// keys other than required ones are readings
			Reading reading = new Reading();
			reading.setName(key);
			reading.setValue(map.get(bkey).toString());
			String ts = String.format("%d", new Date().getTime());
			reading.setTimestamp(ts);
			this.readings.add(reading);
 			
			if(Log.isDebug) {
				Log.debug("sms: reading " + reading.getName() + " value " + reading.getValue());
 			}
 		}
 		
 		if(this.projectId == null || this.serialNumber == null ) {
 			throw new ArgumentException("required data key projectId or serialNumber is missing") ;
 		}
 		
 		if(Log.isDebug) {
 			Log.debug("sms: projectId " + this.projectId);
 			Log.debug("sms: serial number " + this.serialNumber);
 		}
 	}
 }
