 package eu.choreos.services;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 
 import javax.jws.WebMethod;
 import javax.jws.WebService;
 
 import org.apache.xmlbeans.XmlException;
 
 import eu.choreos.DeliveryInfo;
 import eu.choreos.PurchaseInfo;
 import eu.choreos.vv.clientgenerator.WSClient;
 import eu.choreos.vv.exceptions.FrameworkException;
 import eu.choreos.vv.exceptions.InvalidOperationNameException;
 import eu.choreos.vv.exceptions.WSDLException;
 
 @WebService
 public class ShipperWS  {
 	
 	HashMap<Integer, String> deliveries = new HashMap<Integer, String>();
 	
 	long id;
 	
 	@WebMethod
 	public String setDelivery(PurchaseInfo purchaseinfo){
 		//this.start();
 		WSClient wscustomer;
 		try {
			wscustomer = new WSClient(purchaseinfo.getCustomer().getEndpoint());
 			
 			DeliveryInfo deliveryInfo = new DeliveryInfo();
 			deliveryInfo.setPurchase(purchaseinfo);
 			deliveryInfo.setId(""+id++);
 			deliveryInfo.setDate(new Date().toString());
 			
 			
 			
 			return wscustomer.request("receiveShipmentData", deliveryInfo.getItem("arg0")).getChild("return").getContent();
 		} catch (WSDLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (XmlException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (FrameworkException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidOperationNameException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchFieldException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "erro";
 	}
 	
 	@WebMethod
 	public String getDateAndTime(String id){
 		String zipCode = deliveries.get(Integer.parseInt(id));
 		
 		Calendar c = null;
 		
 		if(Integer.parseInt(zipCode.substring(0, 3)) <= 300)
 			c = setTime(8, 12, 32);
 		else if (Integer.parseInt(zipCode.substring(0, 3)) <= 600)
 			c = setTime(15, 30, 42);
 		else if (Integer.parseInt(zipCode.substring(0, 3)) <= 750)
 			c = setTime(20, 15, 00);
 		else 
 			c = setTime(22, 30, 00);
 		
 		return c.getTime().toString();
 	}
 	
 	private Calendar setTime(int hour, int minute, int second){
 		Calendar c1 = new GregorianCalendar(2011, Calendar.DECEMBER, 24);
 		c1.set(2011, Calendar.DECEMBER, 24, hour, minute, second);
 		
 		return c1;
 	}
 	
 	
 	
 }
