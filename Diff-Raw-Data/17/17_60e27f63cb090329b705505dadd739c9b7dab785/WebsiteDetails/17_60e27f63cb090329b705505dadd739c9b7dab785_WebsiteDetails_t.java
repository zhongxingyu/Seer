 package org.umit.icm.mobile.connectivity;
 
 import java.io.IOException;
 import java.net.URLConnection;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpException;
 import org.umit.icm.mobile.process.Globals;
 import org.umit.icm.mobile.process.IDGenerator;
 import org.umit.icm.mobile.proto.MessageProtos.Event;
 import org.umit.icm.mobile.proto.MessageProtos.ICMReport;
 import org.umit.icm.mobile.proto.MessageProtos.Location;
 import org.umit.icm.mobile.proto.MessageProtos.Trace;
 import org.umit.icm.mobile.proto.MessageProtos.TraceRoute;
 import org.umit.icm.mobile.proto.MessageProtos.WebsiteReport;
 import org.umit.icm.mobile.proto.MessageProtos.WebsiteReportDetail;
 import org.umit.icm.mobile.utils.CopyNative;
 
 public class WebsiteDetails {
 	
 	Website website;
 	String websiteURL;
 	URLConnection urlConnection;
 	String content;
 	int status;
 	long fetchTime;
 	double throughput;
 	Map<String,String> header;
 	
 	
 	Trace trace;
 	TraceRoute traceRoute;
 	ICMReport icmReport;
 	WebsiteReportDetail websiteReportDetail;
 	public WebsiteReport websiteReport;
 	
 	
 	
 	public WebsiteDetails(Website website){
 		this.website = website;
 		this.websiteURL=website.getUrl();
 		this.urlConnection=null;
 		this.content="";
 		this.fetchTime=0;
 		this.trace=null;
 		this.traceRoute=null;
 		this.icmReport=null;
 		this.websiteReportDetail=null;
 		this.websiteReport=null;
 		setup();
 	}
 	
 	public WebsiteDetails(String websiteURL){
 		this.website = new Website(websiteURL, "false", "true", "0000", 0);
 		this.websiteURL=websiteURL;
 		this.urlConnection=null;
 		this.content="";
 		this.fetchTime=0;
 		this.trace=null;
 		this.traceRoute=null;
 		this.icmReport=null;
 		this.websiteReportDetail=null;
 		this.websiteReport=null;
 		setup();
 	}
 	
 	
 	public void setup(){
 		setupVariables();
 		setupProtobufs();
 		checkStatus();
 	}
 	
 	private synchronized void setupVariables(){
 		try{
 			//URLConnection urlConnection = WebsiteOpen.openURLConnection(this.website);
 			
 			setupURLConnection();
 			setupHeaders();
 			setupStatus();
 			setupFetchTimeContentThroughput();
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private synchronized void setupFetchTimeContentThroughput(){
 		try{
 			long startFetchTime = System.currentTimeMillis();
 			this.content = WebsiteOpen.getContent(this.urlConnection);
 			this.fetchTime = System.currentTimeMillis()-startFetchTime;
 			this.throughput = (this.content.getBytes().length /this.fetchTime) ;
 		}catch(Exception e){
 			
 		}
 	}
 	
 	
 	private synchronized void setupURLConnection(){
 		
 		try {
			System.out.println("Opening URL Connection to this website : " + websiteURL);
 			this.urlConnection=WebsiteOpen.openURLConnection(this.websiteURL);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (HttpException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	private synchronized void setupHeaders() {
 		
 		try {
 			this.header=WebsiteOpen.getHeaders(this.urlConnection);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (HttpException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
    	}
 	
 	private synchronized void setupStatus(){
 		this.status= WebsiteOpen.getStatusCode(this.header);
 	}
 	
 	private synchronized void setupProtobufs(){
 		
 		this.trace = Trace.newBuilder()
 				.setHop(1)
 				.setIp("193.136.175.1")		
 				.addPacketsTiming(1)
 				.build();
 		
 		this.traceRoute=TraceRoute.newBuilder()
 				.setTarget("193.136.175.1")
 				.setHops(1)
 				.setPacketSize(1)
 				.addTraces(this.trace)
 				.build();
 		
 		this.websiteReportDetail = WebsiteReportDetail.newBuilder()
 				.setBandwidth((int)this.throughput)
 				.setResponseTime((int)this.fetchTime)
 				.setStatusCode(this.status)
 				.setHtmlResponse(this.content)
 				.setWebsiteURL(this.websiteURL)		
 				.build();
 		
 		List<String> listNodes = new ArrayList<String>();
 		Calendar calendar = Calendar.getInstance();
 		listNodes.add(Globals.runtimeParameters.getAgentID());
 		long timeUTC = (calendar.getTimeInMillis()/1000);
 			
 		this.icmReport = ICMReport.newBuilder()
 		.setAgentID(Globals.runtimeParameters.getAgentID())
 		.setTestID(website.getTestID())
 		.setTimeZone(Calendar.ZONE_OFFSET)
 		.setTimeUTC(timeUTC)
 		.addAllPassedNode(listNodes)
 		.setTraceroute(this.traceRoute)
 		.build();
 		
 		
 		
 		this.websiteReport = WebsiteReport.newBuilder()		
 				.setReport(websiteReportDetail)
 				.setHeader(icmReport)	
 				.build();
 		
 		
 	}
 	
 	private void checkStatus() {
 		if(this.status != 200) {
 			double lat = 0.0;
 			double lon = 0.0;
 			if(Globals.currentLocationGPS != null) {
 				lat = Globals.currentLocationGPS.getLatitude();
 				lon = Globals.currentLocationGPS.getLongitude();
 			} else if(Globals.currentLocationNetwork != null) {
 				lat = Globals.currentLocationNetwork.getLatitude();
 				lon = Globals.currentLocationNetwork.getLongitude();
 			}
 			
 			Location location = Location.newBuilder()
 			.setLatitude(lat)
 			.setLongitude(lon)
 			.build();
 			
 			Event event = Event.newBuilder()
 			.setTestType("WEB")
 			.setEventType("CENSOR")
 			.setTimeUTC(this.icmReport.getTimeUTC())
 			.setSinceTimeUTC(this.icmReport.getTimeUTC())
 			.setWebsiteReport(this.websiteReportDetail)
 			.addLocations(location)			
 			.build();
 			
 			Globals.runtimesList.addEvent(event);
 		}
 	}
 	
 	
 }
