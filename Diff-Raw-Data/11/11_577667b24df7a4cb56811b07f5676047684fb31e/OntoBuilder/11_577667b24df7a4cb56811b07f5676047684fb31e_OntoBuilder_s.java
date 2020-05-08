 package ontologyBuilder;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.util.Collection;
 
 public interface OntoBuilder extends Remote {
 	public void addTriples(Collection<Triple> triples) throws RemoteException;
	public int suggestedMaximumNumberOfTriples();
 }
