 package ige.integration.webservice;
 
 
 import ige.integration.domain.GuestInfo;
 import ige.integration.domain.GuestStayInfo;
 import ige.integration.domain.GuestTransactions;
 import ige.integration.messages.Messages;
 import ige.integration.service.GuestInfoService;
 import ige.integration.service.GuestStayInfoService;
 import ige.integration.service.GuestTransactionsService;
 
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 import javax.jws.soap.SOAPBinding;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 @WebService()
 @SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
 public class SoapOperationService {
     private static final String HELLO = "Hello";
     @Autowired
 	private GuestInfoService guestInfoService;
     
     @Autowired
 	private GuestStayInfoService guestInfoStayService;
     
     @Autowired
 	private GuestTransactionsService guestTransactionsService;
 
     @WebMethod(operationName = "sayHello")
     public String sayHelloToTheUser(@WebParam(name = "name") String userName) {
         return HELLO + " " + userName;
     }
     
     @WebMethod(operationName = "getBillInfo")
     public Object getBillInfo(@WebParam(name = "lastName") String userName,@WebParam(name = "email") String email,@WebParam(name = "roomNumber") String roomNumber) {
     	GuestInfo info = null;
     	Object obj = null;
     	obj = guestInfoService.findGuestBillInfo(email, userName, roomNumber);   
     	if(Messages.CREDENTIALS_MESSAGE.equalsIgnoreCase(obj.toString())){
     		return returnFaultObject(Messages.FAULT_CODE_CREDENTIALS, Messages.CREDENTIALS_MESSAGE, Messages.CREDENTIALS_REASON, Messages.GETBILLINFO_DESCRIPTION, "getBillInfo");
     	}else{
             return obj;    		
     	}
     }
     
     @WebMethod(operationName = "guestCheckIn")
     public Object guestCheckIn(@WebParam(name = "guestInfo") GuestInfo guestInfo,@WebParam(name = "guestStayInfo") GuestStayInfo guestStayInfo) {
     	GuestInfo gi = null;
     	gi = guestInfoService.saveGuestInfo(guestInfo);
     	GuestInfo guestInfo1 = guestInfoService.findGuestInfoByPrimaryKey(gi.getId());
     	if(null == guestInfo1){
     		return returnFaultObject(Messages.FAULT_CODE_DATABASE, Messages.DATABASE_MESSAGE, Messages.DATABASE_REASON, Messages.DATABASE_DESCRIPTION, "guestCheckIn");
     	}
     	guestStayInfo.setGuestInfo(guestInfo1);
     	guestInfoStayService.saveGuestStayInfo(guestStayInfo);
         return guestStayInfo;
     }
     
     @WebMethod(operationName = "placeOrder")
     public Object[] placeOrder(@WebParam(name = "lastName") String lastName,@WebParam(name = "email") String email,@WebParam(name = "roomNumber") String roomNumber,@WebParam(name = "guestTransactions") GuestTransactions[] guestTransactions) {
     	GuestInfo gi = null;
     	gi = guestInfoService.findGuestByEmailLastNameRoom(lastName,email,roomNumber);
     	if(null == gi){
     		Object [] obj = new Object[1];
     		obj[0] = returnFaultObject(Messages.FAULT_CODE_CREDENTIALS, Messages.CREDENTIALS_MESSAGE, Messages.CREDENTIALS_REASON, Messages.GETBILLINFO_DESCRIPTION, "placeOrder");
     		return obj;
     	}
     	for(GuestTransactions gT:guestTransactions){
     		Collection c = gi.getGuestStayInfos();
     		Iterator iter = c.iterator();
     		GuestStayInfo first = (GuestStayInfo) iter.next();
     		gT.setGuestStayInfo(first);
     		guestTransactionsService.saveGuestTransactions(gT);
     	}
     	return guestTransactions;
     }
     
     @WebMethod(operationName = "guestCheckout")
    public Object guestCheckout(@WebParam(name = "lastName") String userName,@WebParam(name = "email") String email,@WebParam(name = "creditCardNumber") String creditCardNumber) {
     	GuestInfo gi = null;
    	gi = guestInfoService.findGuestByEmail(email);
     	if(null != gi){
     		GuestStayInfo gsi = null;
     		Collection c = gi.getGuestStayInfos();
     		Iterator iter = c.iterator();
     		gsi = (GuestStayInfo) iter.next();
     		if(null != gsi){
     			//Set credit Card number if provided is not null or empty.
     			if(null != creditCardNumber && !"".equalsIgnoreCase(creditCardNumber)){
     				gsi.setCardNumber(creditCardNumber);
     			}
     			//Set credit amount equal to total bill
     			gsi.setCreditAmount(gsi.getTotalBill());
     			//Set balance amount to 0
     			BigDecimal bgd = new BigDecimal(0);
     			gsi.setBalanceAmount(bgd);
     			//Set departure date and time to now.
     			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     			Date today = Calendar.getInstance().getTime(); 
     			Calendar cal = Calendar.getInstance();
     			cal.setTime(today);
     			gsi.setDepartureDate(cal);
     			//Set checked out
     			gsi.setCheckedOut(true);
     		}
         	gi = guestInfoService.saveGuestInfo(gi);
         	System.out.println("DATE IS: ++++++++++++ " +gsi.getDepartureDate());
         	guestInfoStayService.saveGuestStayInfo(gsi);
     	}else{
    		return returnFaultObject(Messages.FAULT_CODE_CREDENTIALS,Messages.CREDENTIALS_MESSAGE, Messages.CREDENTIALS_REASON, Messages.CHECKOUT_DESCRIPTION, "guestCheckout");
     	}
        return gi;
     }
     
     private String returnFaultObject(String code, String message, String reason, String description, String service){
     	String msg = "<soap:Fault xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'"; 
     		   msg += "xmlns='' >";
     		   msg += "<faultcode>"+code+"</faultcode>";
     		   msg += "<faultstring>"+message+"</faultstring>";
     		   msg += "<faultreason>"+reason+"</faultreason>";
     		   msg += "<detail>";
     		   msg += description;
     		   msg += "</detail>";
     		   msg += "<service>"+service+"</service>";
     		   msg += "</soap:Fault>";
     	System.out.println(msg);
     	return msg;
     }
     
 }
