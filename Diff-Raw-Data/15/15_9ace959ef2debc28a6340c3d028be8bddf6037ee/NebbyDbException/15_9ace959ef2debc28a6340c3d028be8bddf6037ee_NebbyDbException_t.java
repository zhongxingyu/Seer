 package by.neb.db.client;
 
 import by.neb.db.util.restClient.ResourceReply;
 
 public class NebbyDbException extends Exception
 {
	public NebbyDbException ( Throwable t ) { super(t); fReply = null; }
	public NebbyDbException ( String msg ) { super(msg); fReply = null; }
	public NebbyDbException ( ResourceReply rr ) { super("" + rr.getStatusCode () + " " + rr.getStatusMessage ()); fReply = rr; }

	public final ResourceReply fReply;
	
 	private static final long serialVersionUID = 1L;
 }
