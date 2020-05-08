 package de.ct.client;
 
 import de.ct.client.clients.EventClient;
 import de.ct.client.clients.MessageClient;
 import de.ct.client.clients.ParticipationClient;
 import de.ct.client.clients.UserClient;
 import de.ct.shared.Event;
 import de.ct.shared.Message;
 import de.ct.shared.Participation;
 import de.ct.shared.User;
 
 public class Main {
 
 	public static void main(String[] args) {
 		performUser();
 		performEvent();
 		performMessage();
 		performParticipation();
 	}
 	
 	private static void performUser() {
 		UserClient userClient = new UserClient();
 		User user = new User();
 		User user3 = userClient.createUser(user);
 		System.out.println("User erstellt mit id: " + user3);
 		User user2 = userClient.readUser(user3.getUserId());
 		System.out.println("User gelesen mit id: " + user2.getUserId());
 		userClient.deleteUser(user3.getUserId());
 	}
 	
 	private static void performEvent() {
 		EventClient eventClient = new EventClient();
 		Event event = eventClient.createEvent(new Event());
 		System.out.println("Event erstellt mit id: " + event.getEventId());
 		System.out.println("Event gelesen mit id: "
 				+ eventClient.readEvent(event.getEventId()));
 		eventClient.deleteEvent(event.getEventId());
 	}
 
 	private static void performMessage() {
 		MessageClient messageClient = new MessageClient();
 		Message message = messageClient.createMessage(new Message());
 		System.out
 				.println("Message erstellt mit id: " + message.getMessageId());
 		System.out.println("Message gelesen mit ID : "
 				+ messageClient.readMessage(message.getMessageId()));
 		messageClient.deleteMessage(message.getMessageId());
 	}
 	
 	private static void performParticipation() {
 		ParticipationClient participationClient = new ParticipationClient();
 		Participation participation = participationClient
 				.createParticipation(new Participation());
 		System.out.println("Participation erstellt mit id: "
 				+ participation.getParticipationId());
 		System.out.println("Participation gelesen mit event id: "
				+ participationClient.ParticipationsForEventId(0).get(0)
 						.getParticipationId());
 		System.out.println("Participation gelesen mit user id: "
				+ participationClient.ParticipationsForUserId(0).get(0)
 						.getParticipationId());
		participationClient.deleteParticipation(0);
 	}
 
 }
