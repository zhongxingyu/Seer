 package ecologylab.net;
 
 import java.io.InputStream;
 import java.net.URLConnection;
 
 import ecologylab.generic.Debug;
 
 /**
  * Combines URLConnection with InputStream, providing convenience.
  *
  * @author andruid
  */
 public class PURLConnection extends Debug
 {
	protected	ParsedURL				purl;
 	protected InputStream			inputStream;
	protected URLConnection		urlConnection;
	protected String					mimeType;
 
 	/**
 	 * Fill out the instance of this resulting from a succcessful connect().
	 * @param purl TODO
 	 * @param urlConnection
 	 * @param inputStream
 	 */
 	//TODO change to package level access when ParsedURL moves
	public PURLConnection(ParsedURL purl, URLConnection urlConnection, InputStream inputStream)
 	{
		this.purl						= purl;
		this.inputStream		= inputStream;
 		this.urlConnection	= urlConnection;
 	}
 
 	public void recycle()
 	{
 		// parsing done. now free resources asap to avert leaking and memory fragmentation
 		// (this is a known problem w java.net.HttpURLConnection)
 		InputStream inputStream		= this.inputStream;
 		if (inputStream != null)
 		{
 			try
 			{
 				inputStream.close();
 			} catch (Exception e)
 			{
 			}
 			this.inputStream		= null;
 		}
 		URLConnection urlConnection	= this.urlConnection;
 		if (urlConnection != null)
 		{
 			NetTools.disconnect(urlConnection);
 			this.urlConnection		= null;
 		}
 		mimeType					= null;
 	}
 
 	/**
 	 * @return Returns the inputStream.
 	 */
 	public InputStream inputStream()
 	{
 		return inputStream;
 	}
 
 	/**
 	 * @return Returns the urlConnection.
 	 */
 	public URLConnection urlConnection()
 	{
 		return urlConnection;
 	}
 
 	/**
 	 * Find the mime type returned by the web server to the URLConnection, in its header.
 	 * Thus, if there is no URLConnection (as for local file system), this always returns null.
 	 * 
 	 * @return	the mime type or null
 	 */
 	public String mimeType()
 	{
 		String result				= this.mimeType;
 		if ((result == null) && (urlConnection != null))
 		{
 			result					= urlConnection.getContentType();
 			if (result != null)
 			{
 				// create the appropriate DocumentType object
 				// lookout for mime types with charset appened
 				int semicolonIndex	= result.indexOf(';');
 				if (semicolonIndex > 0)
 					result			= result.substring(0, semicolonIndex);
 				this.mimeType		= result;
 			}
 		}
 		return result;
 	}
 	
 	public String toString()
 	{
 		return urlConnection != null ? urlConnection.toString() : "PURLConnection";
 	}
	
	public ParsedURL getPurl()
	{
		return purl;
	}
 }
