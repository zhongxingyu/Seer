 package ca.ubc.magic.enph479.builder;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import ca.ubc.magic.enph479.BennuStatus;
 import ca.ubc.magic.enph479.WoTDataFetcher;
 
 public class DataRetrievalModule {
 	
 	public WoTDataFetcher retrieveFromBennu() throws Exception {
 	
		//retrieve old data from bennu
 		final int job_interval = 5 * 60;
 		final int fetch_interval = job_interval * 2; // in seconds
 		String start_datetime = "undefined";
 		BennuStatus bs = new BennuStatus();
 
 		// initialize WoTDataFetcher
 		WoTDataFetcher wdf = new WoTDataFetcher();
 		System.setProperty("jsse.enableSNIExtension", "false");
 
 		// date we started inputing data into bennu
 		start_datetime = "2013 Nov 08 13:00:00";
 
 		// prepare for fetching
 		if (!wdf.prepareForFetching(fetch_interval, job_interval,
 				start_datetime)) {
 			System.out.println("Error while initializing WotDataFetcher...");
 			return null;
 		}
 
 		System.out.println("===================================\r\nStarting to Fetch from Bennu for old data...");
 		System.out.println("please wait a moment as this process may take up to 30 sec/1 day of tweet data depending on the processor and internet speed...");
 		Thread.sleep(500);
 		System.out.println("Initializing...");
 		Thread.sleep(1*2000);
 		//bs.getBennuStatus();
 		
 		// start fetching using while/for loop
 		while (true) {
 			try {
 				
 				//check for ref_time, if catching up to current time, return
 				String ref_datetime = wdf.get_refDatetime();
 				Date date_ref_time = new Date(ref_datetime);
 				date_ref_time.setTime(date_ref_time.getTime() + 5 * 1000);
 				Date date_current_time = new Date();
 				if(date_ref_time.after(date_current_time)) {
 					System.out.println("Finished Fetching from Bennu for old data...\r\n===================================");
 					return wdf;
 				}
 				
 				String jsonData = wdf.fetchNewData(true);
 				System.out.println("continue fetching from bennu...");
 			} catch (Throwable t) {
 				t.printStackTrace();
 				System.err.println("Error in fetching from bennu...");
 			}
 		}
 	}
 	
 	public void wrappingUpRetrivalModule() throws InterruptedException {
 		
 		System.out.println("Please press stop if during test, otherwise initializing real-time jobs fetcher in");
 		for(int i = 5; i > 0; i--) {
 			System.out.println(i);
 			Thread.sleep(1000);
 		}
 	}
 	
 }
