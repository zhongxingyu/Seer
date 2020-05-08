 package edu.columbia.e6998.cloudexchange.client;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.blobstore.BlobInfoFactory;
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobInfo;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.users.UserServiceFactory;
 
 import edu.columbia.e6998.cloudexchange.aws.LaunchServlet;
 import edu.columbia.e6998.cloudexchange.toolkit.GenericToolkit;
 
 @SuppressWarnings("serial")
 public class AccountServlet extends HttpServlet {
 	
 	private static final Logger log = Logger.getLogger(AccountServlet.class.getName());
 	private Double totalCharge = 0.0;
 
 	private final String destination = "/views/account.jsp";
 	private final String[] amiList = {"ami-8c1fece5",
 			"ami-31814f58",
 			"ami-1b814f72",
 			"ami-3d599754",
 			"ami-ab844dc2", 
 			"ami-fbf93092",
 			"ami-fdf93094",
 			"ami-13f9307a",
 			"ami-17f9307e",
 			"ami-e4a7558d",
 			"ami-0da96764",
 			"ami-c7d81aae",
 			"ami-33a96b5a"};
 
 	@SuppressWarnings("unchecked")
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		
 		String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
 		
 		 /**
 		  * 1. Get name of the file uploaded
 		  * 2. Get the default ami
 		  * 3. Get the default key pair
 		  * 4. Get the default security group
 		  * */
 		
 		String fileName = null;
 		String defaultAmi = null;
 		String keyPair = null;
 		String securityGroup = null;
 		
 		Entity userProfile = GenericToolkit.getInstance().getUserProfileForUser(userId);
 
 		// if userProfile does not exist, create entry in the database
 		if (userProfile == null) {
 			fileName = "No AWS credentials file uploaded";
 			defaultAmi = amiList[0];
 			keyPair = "N/A";
 			securityGroup = "N/A";
 		} else {
 			String blobKeyString = (String) userProfile.getProperty("CredentialsBlobKey");
 			defaultAmi = (String) userProfile.getProperty("defaultAmi");
 			keyPair = (String) userProfile.getProperty("keyPair");
 			securityGroup = (String) userProfile.getProperty("securityGroup");
 			
 			BlobKey blobKey = new BlobKey(blobKeyString);
 			BlobInfoFactory bFactory = new BlobInfoFactory();
 			BlobInfo bInfo = bFactory.loadBlobInfo(blobKey);
 			fileName = bInfo.getFilename() + " uploaded at "+ bInfo.getCreation().toString();
 		}
 		
 		/**
 		 * 5. Get the list of open positions 
 		 */
 		List<PositionEntry> positions = getPositionsList(userId);
 		
 		/**
 		 * 6. Get list of charges
 		 */
 	
 		List<String> chargeList = getChargesList(userId);
 		
 		req.setAttribute("fileName", fileName);
 		req.setAttribute("positions", positions);
 		req.setAttribute("amis", amiList);
 		req.setAttribute("defaultAmi", defaultAmi);
 		req.setAttribute("keyPair", keyPair);
 		req.setAttribute("securityGroup", securityGroup);
 		req.setAttribute("totalCharge", totalCharge);
 		req.setAttribute("charges", chargeList);
 		
 		RequestDispatcher rd = getServletContext().getRequestDispatcher(destination);
 		
 		try {
 			rd.forward(req, resp);
 		} catch (ServletException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		
 		String keyPair = (String) req.getParameter("keyPair");
 		String securityGroup = (String) req.getParameter("securityGroup");
 		Enumeration<String> attributes = req.getParameterNames();
 		String transactionKey = null;
 		while (attributes.hasMoreElements()) {
 			String attribute = attributes.nextElement();
 			log.info("attribute");
 			if (attribute.contains("save")) {
 				transactionKey = attribute.substring(5);
 				break;
 			}
 		}
 		
 		String ami = (String) req.getParameter("ami" + transactionKey);
 		
 		log.info("[AccountServlet doPost]");
		try {
			log.info(ami);
			log.info(keyPair);
			log.info(securityGroup);
		} catch (Exception e) {}

 		String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
 		if (ami != null)
 			GenericToolkit.getInstance().updateTransaction(KeyFactory.stringToKey(transactionKey), 
 					"ami", ami);
 		if (keyPair != null) 
 			GenericToolkit.getInstance().updateUserProfile(userId, "keyPair", keyPair.trim());
 		if (securityGroup != null) 
 			GenericToolkit.getInstance().updateUserProfile(userId, "securityGroup", securityGroup.trim());
 		doGet(req, resp);
 	}
 	
 	public List<String> getChargesList(String userId) {
 		totalCharge = 0.0;
 		ArrayList<String> chargeList = new ArrayList<String>();
 		List<Entity> charges = GenericToolkit.getInstance().getChargesForUser(userId);
 		try {
 			for (Entity charge : charges) {
 				String chargeString = "";
 				String type = (String) charge.getProperty("type");
 				if (type.equals("delivery")) {
 					chargeString += "Delivered instance:";
 				}
 				String amount = (String) charge.getProperty("amount");
				chargeString += " <span style='float:right;'> -$" + amount + "</span>";
 				chargeList.add(chargeString);
 				totalCharge += Double.parseDouble(amount);
 			}
 		} catch (Exception e) {
 			
 		}
 		return chargeList;
 	}
 	
 	public List<PositionEntry> getPositionsList(String userId) {
 		ArrayList<PositionEntry> positions = new ArrayList<PositionEntry>();
 		List<Entity> transactions = GenericToolkit.getInstance().getOpenTransactions();
 		for (Entity transaction : transactions) {
 			String buyerId = (String) transaction.getProperty("buyer");
 			String sellerId = (String) transaction.getProperty("seller");
 			if (userId.equals(buyerId) || userId.equals(sellerId))  {
 				PositionEntry entry = new PositionEntry();
 				if (userId.equals(buyerId))
 					entry.buyOrSell = "Buy";
 				else 
 					entry.buyOrSell = "Sell";
 
 				entry.transactionKey = (String) transaction.getKey().getName();
 				entry.zone = (String) transaction.getProperty("zone");
 				entry.region = (String) transaction.getProperty("region");
 				entry.ami = (String) transaction.getProperty("ami");
 				entry.instance = (String) transaction.getProperty("instanceType");
 				entry.contractPrice = (Double) transaction.getProperty("price");
 				
 				Date date = (Date) transaction.getProperty("date");
 				//alan: seems we're using dates now
 				/**
 				String dateString = (String) transaction.getProperty("date");
 				*/
 				try {
 					Calendar dateCalendar = Calendar.getInstance();
 					dateCalendar.setTime(date);
 					String timeString = (String) transaction.getProperty("time");
 					dateCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeString));
 					date = dateCalendar.getTime();
 				} catch (NumberFormatException e) {
 				  
 				}
 				SimpleDateFormat sfd = new SimpleDateFormat("MM/dd/yyyy hh a");
 				entry.date = sfd.format(date);
 				// TODO: get current spot price
 				entry.spotPrice = null;
 				
 				positions.add(entry);
 			}
 		}
 		return positions;
 	}
 }
 
