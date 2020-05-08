 package states;
 
 import interfaces.NetworkService;
 import interfaces.WebService;
 
 import java.util.ArrayList;
 
 import webClient.Util;
 import webServer.messages.LaunchOfferRequest;
 import webServer.messages.LaunchOfferResponse;
 
 import data.Message;
 import data.Service;
 import data.Service.Status;
 import data.UserEntry;
 
 public class LaunchOfferState implements State {
 	private static final long	serialVersionUID	= 1L;
 	private Service				service;
 
 	public LaunchOfferState() {
 
 	}
 
 	public void executeNet(NetworkService net) {
 		System.out.println("[LaunchOfferState:executeNet()] Begin");
 
 		System.out.println("[LaunchOfferState:executeNet()] End");
 	}
 
 	public void executeWeb(WebService web) {
 		System.out.println("[LaunchOfferState:executeWeb()] Begin");
 
 		service.setStatus(Status.ACTIVE);
		service.setInactiveState();
 
 		// TODO
 		LaunchOfferRequest requestObj = new LaunchOfferRequest(web.getUsername(), web.getUserRole(), service);
 		LaunchOfferResponse responseObj = (LaunchOfferResponse) Util.askWebServer(requestObj);
 		responseObj.getService().setStatus(Status.ACTIVE);
 		web.notifyNetwork(responseObj.getService());
 
 
 		System.out.println("[LaunchOfferState:executeWeb()] End");
 	}
 
 	public void setState(Service service) {
 		this.service = service;
 	}
 
 	public String getName() {
 		return "Launch Offer xxx";
 	}
 
 	@Override
 	public ArrayList<Message> asMessages() {
		ArrayList<Message> list = null;
		Boolean first = true;
 
 		System.out.println("[LaunchOfferState] asMessages");
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
 
			if (first) {
				list = message.asArrayList();
			} else {
				list.add(message);
			}
 		}
 
 		return list;
 	}
 }
