 package by.neb.db.client;
 
 import by.neb.db.util.restClient.ResourceReply;
 
 public class NebbyDbException extends Exception
 {
	public NebbyDbException ( Throwable t ) { super(t); }
	public NebbyDbException ( String msg ) { super(msg); }
	public NebbyDbException ( ResourceReply rr ) { super("" + rr.getStatusCode () + " " + rr.getStatusMessage ()); }
 	private static final long serialVersionUID = 1L;
 }
