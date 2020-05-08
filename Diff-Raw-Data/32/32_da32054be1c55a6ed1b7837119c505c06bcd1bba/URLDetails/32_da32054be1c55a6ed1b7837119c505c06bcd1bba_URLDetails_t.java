 /**
  * 
  */
 package com.mimpidev.podsalinan.data;
 
 import java.net.URL;
 
 import com.mimpidev.dev.sql.field.IntegerType;
 
 /**
  * @author sbell
  *
  */
 public class URLDetails extends BaseURL {
 
 	public static final int NOT_QUEUED=0,
 							DOWNLOAD_QUEUED=1,
 							CURRENTLY_DOWNLOADING=2,
 							FINISHED=3,
 							DOWNLOAD_CANCELLED=4,
 							DO_NOT_DOWNLOAD=5,
 							INCOMPLETE_DOWNLOAD=6,
 							DOWNLOAD_FAULT=7,
 							DESTINATION_INVALID=8,
 							UNKNOWN_STATUS=9;
 	
 	/**
 	 * 
 	 */
 	public URLDetails() {
 		super();
 		fields.put("size", new IntegerType());
 		fields.put("status", new IntegerType());
 	}
 	
 	public URLDetails(String url){
 		super(url);
 		setSize("0");
 	}
 	
 	public URLDetails (String url, String length){
 		this(url);
 		setSize(length);
 	}
 
 	public URLDetails(URL url, String length){
 		this(url.toString());
 		setSize(length);
 	}
 	
 	public URLDetails(String url, boolean added){
 		super(url,added);
 	}
 	
 	public String getSize(){
 		return fields.get("size").getValue();
 	}
 	
 	public void setSize(String size){
 		fields.get("size").setValue(size);
 	}
 	
 	public int getStatus(){
 		int status;
 		try {
 			status = Integer.parseInt(fields.get("status").getValue());
 		} catch (NumberFormatException e){
 			status = UNKNOWN_STATUS;
 		}
 		return status; 
 	}
 	
 	public void setStatus(int newStatus){
 		fields.get("status").setValue(""+newStatus);
 		setUpdated(true);
 	}
 	
 	public String getCurrentStatus(){
 		return getStatusString(getStatus());
 	}
 	
 	public String getStatusString(int statusValue){
 		String status=null;
 		
 		switch (statusValue){
 			case NOT_QUEUED:
 				status = "Not Downloaded";
 				break;
 			case DOWNLOAD_QUEUED:
 				status = "Download Queued";
 				break;
 			case CURRENTLY_DOWNLOADING:
 				status = "Downloading Currently";
 				break;
 			case FINISHED:
 				status = "Completed Download";
 				break;
 			case DOWNLOAD_CANCELLED:
 				status = "Download Cancelled";
 				break;
 			case DO_NOT_DOWNLOAD:
 				status = "Marked do not Download";
 				break;
 			case INCOMPLETE_DOWNLOAD:
 				status = "Download Incomplete";
 				break;
 			case DOWNLOAD_FAULT:
 				status = "Download Fault";
 				break;
 			case DESTINATION_INVALID:
 				status = "Destination Invalid";
 				break;
			case UNKNOWN_STATUS:
				status = "Unknown Error";
				break;
 		}
 		
 		return status;
 	}
 	
 	public char getCharStatus() {
 		char status='\0';
 		
 		switch (getStatus()){
 		case NOT_QUEUED:
 			status = '\0';
 			break;
 		case DOWNLOAD_QUEUED:
 			status = 'Q';
 			break;
 		case CURRENTLY_DOWNLOADING:
 			status = 'D';
 			break;
 		case FINISHED:
 			status = 'F';
 			break;
 		case DOWNLOAD_CANCELLED:
 			status = 'C';
 			break;
 		case DO_NOT_DOWNLOAD:
 			status = '*';
 			break;
 		case INCOMPLETE_DOWNLOAD:
 			status = 'I';
 			break;
 		case DOWNLOAD_FAULT:
 			status = 'B';
 			break;
 		case DESTINATION_INVALID:
 			status = '-';
 			break;
		case UNKNOWN_STATUS:
			status = 'U';
			break;
 		}
 		
 		return status;
 	}
 }
