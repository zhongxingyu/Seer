 package states;
 
 import interfaces.NetworkService;
 import interfaces.WebService;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import webServer.Util;
 import webServer.messages.DropOfferRequest;
 import webServer.messages.ErrorResponse;
 
 import data.Message;
 import data.Service;
 import data.UserEntry.Offer;
 import data.UserEntry;
 import data.UserProfile;
 
 public class DropOfferState extends AbstractState {
 	private static final long	serialVersionUID	= 1L;
 	private static Logger		logger				= Logger.getLogger(DropOfferState.class);
 
 	public DropOfferState(Service service) {
 //		logger.setLevel(Level.OFF);
 		this.service = service;
 	}
 
 	public DropOfferState(DropOfferState state) {
 //		logger.setLevel(Level.OFF);
 		service = state.service;
 	}
 
 	public void executeNet(NetworkService net) {
 		logger.debug("Begin");
 
 		Service clonedService = service.clone();
 		ArrayList<UserEntry> users = clonedService.getUsers();
 
 		if (users != null) {
 			for (UserEntry user : users) {
 				user.setOffer(Offer.OFFER_REFUSED);
 			}
 
 			net.changeServiceNotify(clonedService);
 		}
 
 		// service.setStatus(Status.INACTIVE);
 		// service.setUsers(null);
 		//
 		// TODO
 		// net.removeOffer(service.getName());
 		// logger.debug("service: " +service);
 		//
 		// service.setEnabledState();
 		// TODO
 		// net.stopTransfer(service);
 		// net.changeServiceNotify(service);
 		logger.debug("End");
 	}
 
 	public void executeWeb(WebService web) {
 		logger.debug("Begin");
 		logger.debug("service: " + service);
 
 		DropOfferRequest requestObj = new DropOfferRequest(web.getLoginCred(), service.getName());
 		Object responseObj = Util.askWebServer(requestObj);
 		
 		if (responseObj instanceof ErrorResponse) {
 			ErrorResponse res = (ErrorResponse) responseObj;
 			
 			System.out.println("Failed: " + res.getMsg());
 		}
 		
 		web.notifyNetwork(service);
 		logger.debug("End");
 	}
 
 	public void setState(Service service) {
 		this.service = service;
 	}
 
 	public String getName() {
 		return "DropOfferState";
 	}
 
 	@Override
 	public ArrayList<Message> asMessages(NetworkService net) {
 		logger.debug("Begin");
 		UserProfile userProfile = net.getUserProfile();
 		ArrayList<Message> list = new ArrayList<Message>();
 
 		if (service.getUsers() == null) {
 			return list;
 		}
 
 		for (UserEntry user : service.getUsers()) {
 			Message message = new Message();
 
 			message.setType(data.Message.MessageType.REFUSE);
 			message.setServiceName(service.getName());
 			message.setUsername(new String(user.getUsername()));
 			message.setPayload(userProfile.getUsername());
 			message.setDestination(user.getUsername());
 			message.setSource(userProfile.getUsername());
 
 			list.add(message);
 		}
 
 		logger.debug("End");
 		return list;
 	}
 
 	@Override
 	public DropOfferState clone() {
 		return new DropOfferState(this);
 	}
 }
