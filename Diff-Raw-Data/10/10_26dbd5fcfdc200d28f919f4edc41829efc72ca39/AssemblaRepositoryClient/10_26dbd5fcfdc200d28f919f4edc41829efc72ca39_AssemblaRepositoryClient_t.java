 package cz.cvut.fel.reposapi.assembla;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import cz.cvut.fel.reposapi.AbstractRepositoryClient;
 import cz.cvut.fel.reposapi.Credentials;
 import cz.cvut.fel.reposapi.Repository;
 import cz.cvut.fel.reposapi.RepositoryClient;
 import cz.cvut.fel.reposapi.assembla.AssemblaCredentials.Type;
 import cz.cvut.fel.reposapi.assembla.client.ApiKey;
 import cz.cvut.fel.reposapi.assembla.client.AssemblaClient;
 import cz.cvut.fel.reposapi.assembla.client.Identity;
 import cz.cvut.fel.reposapi.assembla.client.Space;
 import cz.cvut.fel.reposapi.assembla.client.Ticket;
 import cz.cvut.fel.reposapi.assembla.client.Token;
 
 public class AssemblaRepositoryClient extends AbstractRepositoryClient<AssemblaCredentials> implements RepositoryClient {
 
 	private final AssemblaClient assemblaClient;
 	private Token token;
 
 	public AssemblaRepositoryClient(Credentials credentials) {
 		super(AssemblaCredentials.class, credentials);
 		if (getCredentials().getType() == null) {
 			throw new NullPointerException(AssemblaCredentials.class.getName() + " type is required!");
 		}
 		assemblaClient = new AssemblaClient();
 	}
 
 	private Identity getIdentity() {
 		if (getCredentials().getType() == Type.CLIENT_APPLICATION) {
 			if (token == null || token.isExpired()) {
 				token = assemblaClient.getClientToken(getCredentials().getClientId(), getCredentials().getSecret());
 			}
 			return token;
 		}
 		if (getCredentials().getType() == Type.API_KEY) {
 			return new ApiKey(getCredentials().getClientId(), getCredentials().getSecret());
 		}
 		throw new IllegalStateException("Uknown type of credentials " + getCredentials().getType());
 	}
 
 	public List<Space> getSpaces() {
 		return assemblaClient.getSpaces(getIdentity());
 	}
 
 	public Repository getRepository(String name) {
 		Space space = assemblaClient.getSpace(getIdentity(), name);
		return new AssemblaRepository(space, this);
 	}
 
 	public List<Ticket> getIssues(Space space) {
 		return assemblaClient.getSpaceTickets(getIdentity(), space);
 	}
 
 	public List<Ticket> getIssues(Space space, Ticket.State state) {
 		return assemblaClient.getSpaceTickets(getIdentity(), space, state);
 	}
 
 	public List<Ticket> getUpdatedIssues(Space space) {
 		List<Ticket> list = assemblaClient.getSpaceTickets(getIdentity(), space);
 		sortByUpdatedAtDesc(list);
 		return list;
 	}
 
 	public List<Ticket> getUpdatedIssues(Space space, int limit) {
		List<Ticket> tickets = getUpdatedIssues(space);
		return (tickets.size() < limit) ? tickets : tickets.subList(0, limit);
 	}
 
 	public List<Ticket> getUpdatedIssues(Space space, Ticket.State state) {
 		List<Ticket> list = assemblaClient.getSpaceTickets(getIdentity(), space, state);
 		sortByUpdatedAtDesc(list);
 		return list;
 	}
 
 	public List<Ticket> getUpdatedIssues(Space space, Ticket.State state, int limit) {
		List<Ticket> tickets = getUpdatedIssues(space, state);
		return (tickets.size() < limit) ? tickets : tickets.subList(0, limit);
 	}
 
 	private static void sortByUpdatedAtDesc(List<? extends Ticket> list) {
 		Collections.sort(list, Collections.reverseOrder(new Comparator<Ticket>() {
 			public int compare(Ticket o1, Ticket o2) {
 				Date d1 = o1.getUpdatedAt();
 				Date d2 = o2.getUpdatedAt();
 				return d1.before(d2) ? -1 : (d1.equals(d2) ? 0 : 1);
 			}
 		}));
 	}
 
 }
