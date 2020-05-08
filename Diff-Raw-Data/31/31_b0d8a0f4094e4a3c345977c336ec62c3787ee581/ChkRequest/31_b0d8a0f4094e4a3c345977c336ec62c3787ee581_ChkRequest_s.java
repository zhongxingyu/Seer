 package messages;
 
public class ChkRequest extends Message
 {
	public final static int MAX_HTL = 5; // Maximum amount of backtracking
	
	private static int nextId = 0;
	
	public final int key; // The requested key
	public double best; // The best location seen so far
	public int htl; // Hops to live for backtracking
	
 	// Start a new request
 	public ChkRequest (int key, double location)
 	{
		id = nextId++;
		this.key = key;
		best = location;
		htl = MAX_HTL;
		size = Message.HEADER_SIZE + Message.KEY_SIZE;
 	}
 	
 	// Forward a request
	public ChkRequest (int id, int key, double best, int htl)
 	{
		this.id = id;
		this.key = key;
		this.best = best;
		this.htl = htl;
		size = Message.HEADER_SIZE + Message.KEY_SIZE;
 	}
 	
 	public String toString()
 	{
		return new String ("CHK request (" + id + "," + key + ")");
 	}
 }
