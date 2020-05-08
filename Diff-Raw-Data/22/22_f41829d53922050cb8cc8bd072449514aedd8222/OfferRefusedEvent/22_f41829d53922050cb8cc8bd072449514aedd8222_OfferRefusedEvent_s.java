 package network;
 
 import interfaces.Command;
 import interfaces.MediatorNetwork;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import data.Service;
 import data.UserEntry;
 import data.UserEntry.Offer;
 
 public class OfferRefusedEvent implements Command {
 	private MediatorNetwork		med;
 	private Service			service;
 	private static Random	random	= new Random();
 
 	public OfferRefusedEvent(MediatorNetwork med, Service service) {
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
 
 		/* TODO */
 		if (user.getOffer() != Offer.OFFER_ACCEPTED) {
 			return;
 		}
 
 		user.setOffer(Offer.OFFER_REFUSED);

		med.putOffer(service);
 		med.changeServiceNotify(service);
 	}
 }
