 package sfs.client.http;
 
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
 
 import sfs.client.http.shortconversation.ShortConversation;
 import sfs.entry.HostEntry;
 
 public class SingleplexHTTPClient extends HTTPClient {
 
 	private ShortConversation shortConversation;
 	private static Logger log = Logger.getLogger( SingleplexHTTPClient.class );
 
 	public SingleplexHTTPClient(HostEntry hostEntry) {
 		super( hostEntry );
 	}
 
 	public SingleplexHTTPClient(HostEntry hostEntry, ShortConversation shortConversation) {
 		super( hostEntry );
 		this.shortConversation = shortConversation;
 
 	}
 
 	public void setShortConversation(ShortConversation shortConversation) {
 		this.shortConversation = shortConversation;
 	}
 
 	public void initiate() throws IOException {
 
 		if ( shortConversation == null ) {
 			log.error( new IllegalArgumentException( "shortConversation property is null. " ) );
 		}
 
		try {
			open( 0, getHostEntry( 0 ) );
			shortConversation.writeRequest( this, getServerChannel( 0 ), getHostEntry( 0 ) );
		}
		catch ( Exception ex ) {
			 log.warn( "its parent seems to be dead, need to change the parent node ..." );
			 throw new IllegalArgumentException( "specified parent, "
			 + getServerHost( getHostEntry( 0 ).getHost(), getHostEntry( 0 ).getPort() )
			 + "is not alive. Need to change the parent node" );
 		}
 
 		log.debug( "its parent is alive" );
 	}
 
 }
