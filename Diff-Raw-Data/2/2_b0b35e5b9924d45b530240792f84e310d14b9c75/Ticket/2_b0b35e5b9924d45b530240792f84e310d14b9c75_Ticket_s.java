 import java.io.ByteArrayOutputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.Arrays;
 
 
 /**
  * The Ticket is an access token specific to file servers. It is given out upon connecting to a file server.
 * A ticket is specific to a file server session (aka a specific thread) and thus cannot be used outside of that session.
  * A Ticket MUST be used with a proper Token to perform actions on a file server.
  * @author Matt
  *
  */
 public class Ticket implements Serializable {
 
 	private static final long serialVersionUID = 2915526291070888005L;
 	private String issuer;
 	private int threadID;
 	private byte[] signature;
 	
 	public Ticket(String issuer_, int threadID_) {
 		issuer = issuer_;
 		threadID = threadID_;
 	}
 	
 	public String getIssuer() {
 		return issuer;
 	}
 	
 	public int getThreadID() {
 		return threadID;
 	}
 	
 	public void setSignature(byte[] signature_) {
 		signature = Arrays.copyOf(signature_, signature_.length);
 	}
 
 	public byte[] getSignature() {
 		return signature;
 	}
 	
 	public byte[] toByteArray() {
 		byte[] returnBytes = null;
 				
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		ObjectOutputStream out = null;
 		try {
 			out = new ObjectOutputStream(bos);   
 			out.writeObject(issuer);
 			out.writeObject(threadID);
 			returnBytes = bos.toByteArray();
 			out.close();
 			bos.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return returnBytes;
 	 }
 	
 	public String toString() {
     	return new StringBuilder()
     	.append("Token Information:")
 		.append("\nIssuer: " + issuer) 
 		.append("\nthreadID: " + threadID).toString();
     }
 	
 }
