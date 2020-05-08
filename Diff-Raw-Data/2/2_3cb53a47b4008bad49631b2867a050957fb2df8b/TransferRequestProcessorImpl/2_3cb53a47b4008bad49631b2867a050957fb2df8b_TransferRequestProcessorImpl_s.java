 package de.uniluebeck.itm.mdcf.remote.locationtracker.server;
 
 import java.util.Iterator;
 import java.util.List;
 
import com.google.gwt.thirdparty.guava.common.base.Objects;
 import com.google.inject.Inject;
 
 import de.uniluebeck.itm.mdcf.remote.TransferRequestProcessor;
 import de.uniluebeck.itm.mdcf.remote.locationtracker.server.domain.Location;
 import de.uniluebeck.itm.mdcf.remote.locationtracker.server.domain.Participant;
 import de.uniluebeck.itm.mdcf.remote.model.Node;
 import de.uniluebeck.itm.mdcf.remote.model.TransferRequest;
 
 public class TransferRequestProcessorImpl implements TransferRequestProcessor {
 	
 	private final ParticipantRepository repository;
 	
 	@Inject
 	public TransferRequestProcessorImpl(ParticipantRepository repository) {
 		this.repository = repository;
 	}
 	
 	public void process(TransferRequest request) {
 		String id = request.getId();
 		Participant participant = Objects.firstNonNull(repository.findById(id), new Participant(id));
 		
 		Node workspace = request.getWorkspace();
 		Iterator<Node> nodes = workspace.getNodes();
 		List<Location> locations = participant.getLocations();
 		while (nodes.hasNext()) {
 			Node node = nodes.next();
 			Location location = new Location();
 			location.setTimestamp(node.getTimestamp());
 			location.setLatitude(node.getProperty("Latitude").getValue().getDouble());
 			location.setLongitude(node.getProperty("Longitude").getValue().getDouble());
 			location.setAltitude(node.getProperty("Altitude").getValue().getDouble());
 			location.setBearing((float) node.getProperty("Bearing").getValue().getDouble());
 			location.setAccuracy((float) node.getProperty("Accuracy").getValue().getDouble());
 			location.setSpeed((float) node.getProperty("Speed").getValue().getDouble());
 			location.setProvider(node.getProperty("Provider").getValue().getString());
 			locations.add(location);
 		}
 		repository.persist(participant);
 	}
 }
