 package states;
 
 import interfaces.NetworkService;
 import interfaces.WebService;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 import webClient.Util;
 import webServer.messages.LaunchOfferRequest;
 import webServer.messages.LaunchOfferResponse;
 
 import data.Message;
 import data.Service;
 import data.Service.Status;
 import data.UserEntry;
 import data.UserEntry.Offer;
import data.UserProfile.UserRole;
 import data.UserProfile;
 
 public class LaunchOfferState extends AbstractState {
 	private static final long	serialVersionUID	= 1L;
 	private static Logger		logger				= Logger.getLogger(LaunchOfferState.class);
 
 	public LaunchOfferState(Service service) {
 		// TODO: logger.setLevel(Level.OFF);
 		this.service = service;
 	}
 
 	public LaunchOfferState(LaunchOfferState state) {
 		// TODO: logger.setLevel(Level.OFF);
 		service = state.service;
 	}
 
 	public void executeNet(NetworkService net) {
 		logger.debug("Begin");
 
 		Service clonedService = service.clone();
 		logger.debug("clonedService: " + clonedService);
 		
 		clonedService.setUsers(null);
 		net.changeServiceNotify(clonedService);
 
 		logger.debug("End");
 	}
 
 	public void executeWeb(WebService web) {
 		logger.debug("Begin");
 		logger.debug("service: " + service);
 
 		// TODO
 		LaunchOfferRequest requestObj = new LaunchOfferRequest(web.getUsername(), web.getLoginCred(), service);
 		LaunchOfferResponse responseObj = (LaunchOfferResponse) Util.askWebServer(requestObj);
 
		if (web.getLoginCred().getRole() == UserRole.SELLER) { 
			responseObj.getService().setStatus(Status.ACTIVE);
		}
 
 		logger.debug("service.getService(): "
 				+ responseObj.getService());
 
 		web.notifyNetwork(responseObj.getService());
 
 		logger.debug("End");
 	}
 
 	public void setState(Service service) {
 		this.service = service;
 	}
 
 	public String getName() {
 		return "LaunchOfferState";
 	}
 
 	@Override
 	public ArrayList<Message> asMessages(NetworkService net) {
 		logger.debug("Begin");
 		UserProfile profile = net.getUserProfile();
 
 		ArrayList<Message> list = new ArrayList<Message>();
 
 		/**
 		 * There is no one who offer this service
 		 */
 		if (service.getUsers() == null) {
 			return new ArrayList<Message>();
 		}
 
 		for (UserEntry user : service.getUsers()) {
 			Message message = new Message();
 			message.setType(data.Message.MessageType.LAUNCH);
 			message.setServiceName(service.getName());
 			message.setUsername(user.getUsername());
 			message.setDestination(user.getUsername());
 			message.setPayload(new UserEntry(profile.getUsername(), profile.getFirstName() + " "
 					+ profile.getLastName(), Offer.NO_OFFER, service.getTime(), service.getPrice()));
 			logger.debug("user: " + user);
 			list.add(message);
 		}
 
 		logger.debug("End");
 		return list;
 	}
 
 	public LaunchOfferState clone() {
 		return new LaunchOfferState(this);
 	}
 }
