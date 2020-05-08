 package tk.ludva.restfulchecker;
 
 /**
 * Class used for holding HTTP header.
  * @author Lu2
  *
  */
 public class Header
 {
 
 	/**
 	 * HTTP header key.
 	 */
 	private String headerKey;
 	
 	/**
 	 * HTTP header value.
 	 */
 	private String headerValue;
 	
 	/**
 	 * Tells if the header should be applied.
 	 */
 	private boolean inUse;
 
 	public Header()
 	{
 	}
 
 	/**
 	 * Constructor for creating header.
 	 * @param headerKey HTTP header key.
 	 * @param headerValue HTTP header value.
 	 * @param inUse boolean if the header should be applied.
 	 */
 	public Header(String headerKey, String headerValue, boolean inUse)
 	{
 		super();
 		this.headerKey = headerKey;
 		this.headerValue = headerValue;
 		this.inUse = inUse;
 	}
 
 	/**
 	 * Gets HTTP header key.
 	 * @return String key.
 	 */
 	public String getHeaderKey()
 	{
 		return headerKey;
 	}
 
 	/**
 	 * Sets HTTP header key.
 	 * @param headerKey String desired key
 	 */
 	public void setHeaderKey(String headerKey)
 	{
 		this.headerKey = headerKey;
 	}
 
 	/**
 	 * Gets HTTP header value.
 	 * @return String header value.
 	 */
 	public String getHeaderValue()
 	{
 		return headerValue;
 	}
 
 	/**
 	 * Sets HTTP header value.
 	 * @param headerValue String desired value.
 	 */
 	public void setHeaderValue(String headerValue)
 	{
 		this.headerValue = headerValue;
 	}
 
 	/**
 	 * Gets information about using header.
 	 * @return true if headder should be used.
 	 */
 	public boolean isInUse()
 	{
 		return inUse;
 	}
 
 	/**
 	 * Sets information about using header
 	 * @param inUse boolean true if should be used.
 	 */
 	public void setInUse(boolean inUse)
 	{
 		this.inUse = inUse;
 	}
 
 }
