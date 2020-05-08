 package messages;
 
public class ChkRequest extends Search
 {
 	// Start a new request
 	public ChkRequest (int key, double location)
 	{
		super (key, location);
 	}
 	
 	// Forward a request
	public ChkRequest (int id, int key, double closest, int htl)
 	{
		super (id, key, closest, htl);
 	}
 	
 	public String toString()
 	{
		return new String ("CHK request (" +id+ "," +key+ "," +htl+")");
 	}
 }
