 /**
  * 
  */
 package ecologylab.io;
 
 import java.util.Random;
 
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.ElementState;
 import ecologylab.serialization.ElementState.xml_tag;
 import ecologylab.serialization.types.element.Mappable;
 
 /**
  * 
  *
  * @author andruid 
  */
 
 @xml_tag("site")
 public 
 class BasicSite extends ElementState implements Mappable<String>
 {
 	
 	static Random random = new Random(System.currentTimeMillis());
 	
 	@simpl_scalar protected String	domain;
 
 	@simpl_scalar
   int															numTimeouts;
 
 	@simpl_scalar
   int															numFileNotFounds;
 
 	@simpl_scalar
   int															numOtherIoErrors;
 
 	@simpl_scalar
   int															numNormalDownloads;
 
 	protected int										downloadsQueuedOrInProgress;
 
   boolean													ignore;
   
   static final int							MAX_TIMEOUTS	= 6;
 
   /**
    * Minimum time to wait between downloads for this domain
    * Specified in seconds
    */
   @simpl_scalar protected float							minDownloadInterval;
   
   /**
    * Timestamp of last download from this site;
    */
   long														lastDownloadAt;
   
   @simpl_scalar protected boolean ignoreSemanticBoost;
   
   boolean													isDownloading;
   
 	/**
 	 * Use for XML Translation
 	 */
 	public BasicSite()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	public void countTimeout(ParsedURL location)
 	{
 		numTimeouts++;
 		warning("Timeout " + numTimeouts + "\t" + location);
 	}
 	
 	public void countFileNotFound(ParsedURL location)
 	{
		numTimeouts++;
 		warning("FileNotFound " + numFileNotFounds + "\t" + location);
 	}
 	
 	public void countOtherIoError(ParsedURL location)
 	{
		numTimeouts++;
 		warning("Other IO Error " + numOtherIoErrors + "\t" + location);
 	}
 	
 	public void countNormalDownload()
 	{
		numTimeouts++;
 	}
 	
 	public synchronized void queuedDownload()
 	{
 		downloadsQueuedOrInProgress++;
 	}
 
 	public synchronized void endDownload()
 	{
 		isDownloading	= false;
 		downloadsQueuedOrInProgress--;
 	}
 
 	
 	public boolean tooManyTimeouts()
 	{
 		return numTimeouts >= MAX_TIMEOUTS;
 	}
 	
 	public boolean isDown()
 	{
 		boolean result = tooManyTimeouts();
 		if (result)
 			warning("Cancelling because " + numTimeouts + " timeouts");
 		return result;
 	}
 	/**
 	 *	
 	 * @return (numTimeouts == 0) ? 1 : 1.0 / (numTimeouts + 1);
 	 */
 	public double timeoutsFactor()
 	{
 		return (numTimeouts == 0) ? 1 : 1.0 / (numTimeouts + 1);
 	}
 
 	/**
 	 * @return the minDownloadInterval
 	 */
 	public float getMinDownloadInterval()
 	{
 		return minDownloadInterval;
 	}
 
 	/**
 	 * @param minDownloadInterval the minDownloadInterval to set
 	 */
 	public void setMinDownloadInterval(float minDownloadInterval)
 	{
 		this.minDownloadInterval = minDownloadInterval;
 	}
 
 	/**
 	 * @return the lastDownloadAt
 	 */
 	public long getLastDownloadAt()
 	{
 		return lastDownloadAt;
 	}
 
 	/**
 	 * Register that we are down loading, and the current time as when the down load started.
 	 */
 	public void beginActualDownload()
 	{
 		this.isDownloading	= true;
 		this.lastDownloadAt = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Swing delays between downloads for a site by a large margin. 
 	 * @return time in millis 
 	 */
 	public long getDecentDownloadInterval()
 	{
 		int millis = (int) (minDownloadInterval * 1000);
 		return millis + random.nextInt(millis / 2);
 	}
 	public boolean constrainDownloadInterval()
 	{
 		return minDownloadInterval > 0;
 	}
 	public String key()
 	{
 		return domain;
 	}
 
 	/**
 	 * 
 	 * 
 	 * @return the isDownloading
 	 */
 	public boolean isDownloading()
 	{
 		return isDownloading;
 	}
 	
 	/**
 	 * Call this when a download to a site was actually fulfilled by a local copy.
 	 */
 	public void resetLastDownloadAt()
 	{
 		this.lastDownloadAt	= 0;
 	}
 
 	public String domain()
 	{
 		return domain;
 	}
  
 	public boolean ignoreSemanticBoost()
 	{
 		return ignoreSemanticBoost;
 	}
 
 	public boolean shouldIgnore()
 	{
 		return ignore;
 	}
 
 	/**
 	 * Recycle and Mark this site as recycled.
 	 * 
 	 */
 	public void setIgnored(boolean ignore)
 	{
 		this.ignore = ignore;
 	}
 }
