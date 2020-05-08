 package eai.msejdf.esb;
 
 import org.jboss.soa.esb.actions.AbstractActionLifecycle;
 import org.jboss.soa.esb.helpers.ConfigTree;
 import org.jboss.soa.esb.message.Message;
 import java.util.HashMap;
 import eai.msejdf.utils.SOAMessageConstants;
 
 public class GetUsersFollowingCompany extends AbstractActionLifecycle {
 
 	protected ConfigTree _config;
 
 	public GetUsersFollowingCompany(ConfigTree config) {
 		_config = config;
 	}
 
 	public Message noOperation(Message message) {
 		return message;
 	}
 
 	/*
 	 * Convert the message into a webservice request map.
 	 */
 	public Message process(Message message) throws Exception {
 		logHeader();
		String msgBody = (String) message.getBody().get();
 		HashMap requestMap = new HashMap();
 
 		// add parameters to the web service request map
 		System.out.println("################### GetUsersFollowingCompany ######################\n");
 		System.out.println("GetUsersFollowingCompany: " + "companyName = " + msgBody);
 		
 		
 		requestMap.put("getUsersFollowingCompany." + SOAMessageConstants.ESB_COMPANY_NAME , msgBody);
 
 		message.getBody().add(requestMap);
 		System.out.println("Request map is: " + requestMap.toString());
 		System.out.println("################### GetUsersFollowingCompany ######################\n");
 		logFooter();
 		return message;
 	}
 
 	public void exceptionHandler(Message message, Throwable exception) {
 		logHeader();
 		System.out.println("GetUsersFollowingCompany: " + "!ERROR!");
 		System.out.println("GetUsersFollowingCompany: " + exception.getMessage());
 		System.out.println("GetUsersFollowingCompany: " + exception.getMessage());
 		System.out.println("GetUsersFollowingCompany: " + "For Message: ");
 		System.out.println("GetUsersFollowingCompany: " + message.getBody().get());
 		logFooter();
 	}
 
 	// This makes it easier to read on the console
 	private void logHeader() {
 		System.out
 				.println("&&&&&&&&&&&&&&&&&&&&&& GetUsersFollowingCompany &&&&&&&&&&&&&&&&&&&&&&\n");
 	}
 
 	private void logFooter() {
 		System.out
 				.println("&&&&&&&&&&&&&&&&&&&&&& GetUsersFollowingCompany &&&&&&&&&&&&&&&&&&&&&&\n");
 	}
 
 }
