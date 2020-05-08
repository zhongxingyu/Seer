 package de.ct.client.clients;
 
 import java.util.List;
 
 import de.ct.client.creator.ParticipationCreator;
 import de.ct.client.deleter.ParticipationDeleter;
 import de.ct.client.reader.ParticipationReader;
 import de.ct.shared.Participation;
 
 public class ParticipationClient {
 
 	private ParticipationCreator participationCreator;
 	private ParticipationDeleter participationDeleter;
 	private ParticipationReader participationReader;
 	
 	public ParticipationClient() {
 		participationCreator = new ParticipationCreator();
 		participationDeleter = new ParticipationDeleter();
 		participationReader = new ParticipationReader();
 	}
 
 	public Participation createParticipation(final Participation aParticipation) {
 		Participation participation = participationCreator.createParticipation(aParticipation);
 		return participation;
 	}
 
 	public List<Participation> ParticipationsForEventId(final long aEventId) {
 		List<Participation> participations = participationReader.readParticipationsForEventId(aEventId);
 		return participations;
 	}
 	
 	public List<Participation> ParticipationsForUserId(final long aUserId) {
 		List<Participation> participations = participationReader.readParticipationsForUserId(aUserId);
 		return participations;		
 	}
 	
 
	public void deleteParticipation(int aParticipationId) {
 		participationDeleter.deleteParticipation(aParticipationId);
 	}
 
 }
