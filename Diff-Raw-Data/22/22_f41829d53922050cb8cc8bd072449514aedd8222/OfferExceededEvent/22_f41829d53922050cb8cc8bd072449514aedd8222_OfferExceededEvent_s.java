 package network;
 
 import interfaces.Command;
 import interfaces.MediatorNetwork;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import data.Service;
 import data.UserEntry;
 import data.UserEntry.Offer;
 
 public class OfferExceededEvent implements Command {
 	private MediatorNetwork		med;
 	private Service			service;
 	private static Random	random	= new Random();
 
 	public OfferExceededEvent(MediatorNetwork med, Service service) {
 		this.med = med;
 		this.service = service;
 	}
 
 	@Override
 	public void execute() {
 		ArrayList<UserEntry> users = service.getUsers();
 
 		if (users == null || users.size() == 0) {
 			return;
 		}
 		
 		Integer userIndex = random.nextInt(users.size());
 
 		UserEntry user = users.get(userIndex);
 
 		if (user.getOffer() != Offer.OFFER_MADE) {
 			return;
 		}
 
 		user.setOffer(Offer.OFFER_EXCEDED);
 
		med.putOffer(service);
 		med.changeServiceNotify(service);
 
 	}
 }
