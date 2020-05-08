 
 package org.paxle.data.db;
 
 import java.net.URI;
 import java.util.ArrayList;
 
public class URIQueueEntry {
 	private final int profileID;
 	private final int commandDepth;
 	private final URI rootUri;
 	private final ArrayList<URI> refs;
 	private int known;
 
 	public URIQueueEntry(URI rootUri, int profileID, int commandDepth, ArrayList<URI> refs) {
 		this.rootUri = rootUri;
 		this.profileID = profileID;
 		this.commandDepth = commandDepth;
 		this.refs = refs;
 	}
 	
 	public void setKnown(int known) {
 		this.known = known;
 	}
 	
 	public int getKnown() {
 		return this.known;
 	}
 	
 	public int getProfileID() {
 		return this.profileID;
 	}
 
 	public int getDepth() {
 		return this.commandDepth;
 	}
 
 	public URI getRootURI() {
 		return this.rootUri;
 	}
 
 	public ArrayList<URI> getReferences() {
 		return this.refs;
 	}
}
