 package by.neb.db.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import by.neb.db.util.restClient.ResourceDescriptor;
 import by.neb.db.util.restClient.ResourceReply;
 
 /**
  * A local copy of a server-side instance. Data is stored in a JSONObject that's
  * populated on get() and posted on put()/post(). Derived classes manipulate the
  * JSON according to their knowledge of the data structure used on the server.
  * 
  * @author peter
  */
 public abstract class NebbyDbRemoteResource
 {
 	protected NebbyDbRemoteResource ( NebbyDbClient client, String uri )
 	{
 		fClient = client;
 		fUri = uri;
 		fLocalCopy = new JSONObject ();
 	}
 
 	public boolean exists () throws IOException, NebbyDbException
 	{
 		try
 		{
 			final ResourceDescriptor rd = fClient.makeResourceDescriptor ( fUri );
 			final ResourceReply rr = fClient.getRestClient ().head ( rd );
 			if ( rr.isOkay () )
 			{
 				return true;
 			}
 			else if ( rr.getStatusCode () == 404 )
 			{
 				return false;
 			}
 			else
 			{
 				throw new NebbyDbException ( rr );
 			}
 		}
 		catch ( URISyntaxException e )
 		{
 			return false;
 		}
 	}
 
 	public void get () throws NebbyDbException, IOException
 	{
 		try
 		{
 			final ResourceDescriptor rd = fClient.makeResourceDescriptor ( fUri );
 			final ResourceReply rr = fClient.getRestClient ().get ( rd );
 			if ( rr.isOkay () )
 			{
 				final byte[] bytes = rr.getEntity ();
 				final ByteArrayInputStream bais = new ByteArrayInputStream ( bytes );
 				
 				fLocalCopy = new JSONObject ( new JSONTokener ( bais ) );
 				onUpdateFromServer ();
 			}
 			else
 			{
 				throw new NebbyDbException ( rr );
 			}
 		}
 		catch ( URISyntaxException e )
 		{
 			throw new NebbyDbException ( "Bad Request" );
 		}
 		catch ( JSONException e )
 		{
 			throw new NebbyDbException ( "Bad Response from Server" );
 		}
 	}
 
 	public void put () throws NebbyDbException, IOException
 	{
 		final ResourceDescriptor rd = getResourceDescriptor ( );
 		final ResourceReply rr = getClient().getRestClient ().put ( rd, "application/json", fLocalCopy.toString ().getBytes ( utf8 ) );
 		if ( !rr.isOkay () )
 		{
 			throw new NebbyDbException ( rr );
 		}
 	}
 
 	private final NebbyDbClient fClient;
 	private final String fUri;
 	private JSONObject fLocalCopy;
 
 	protected NebbyDbClient getClient () { return fClient; }
 
 	protected ResourceDescriptor getResourceDescriptor () throws NebbyDbException
 	{
 		try
 		{
 			return fClient.makeResourceDescriptor ( fUri );
 		}
 		catch ( URISyntaxException e )
 		{
 			throw new NebbyDbException ( "Bad Request" );
 		}
 	}
 
 	protected JSONObject getJson ()
 	{
 		return fLocalCopy;
 	}
 
 	protected void onUpdateFromServer () {}
 	
 	protected static final Charset utf8 = Charset.forName ( "UTF-8" );
 }
