 package gov.fcc.wrv.model.util;
 
 import gov.fcc.wrv.model.*;
 import gov.fcc.wrv.model.xsd.*;
 import java.util.Calendar;
 
 public class Tester {
 	public static void main(String[] args) {
 		
 		try{
 			BiddingDashboardService_Impl svc = new BiddingDashboardService_Impl();//"http://localhost:7777/wrv/services/DashboardService?wsdl");
 			BiddingDashboardServicePortType port = svc.getBiddingDashboardServiceHttpPort();
 			port.verifyWS();
 			
 			System.out.println(" done with verifyWS() ");
 			
 			WrvEvent event = new WrvEvent();
 			event.setTimeStamp(Calendar.getInstance());
 			
 			
 			port.sendTestEvent(event);
 			System.out.println(" done with sendTestEvent() ");
 		
			event.setEventType("ANNPOST");
			event.setEventSender("BIDDING");
 			port.sendEvent(event);
 			
 			System.out.println(" done with sendEvent() ");
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 }
