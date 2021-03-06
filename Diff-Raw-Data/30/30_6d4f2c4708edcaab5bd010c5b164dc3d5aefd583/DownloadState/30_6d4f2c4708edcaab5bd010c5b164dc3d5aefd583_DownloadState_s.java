 package ecologylab.concurrent;
 
 import java.io.IOException;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.Continuation;
 import ecologylab.io.BasicSite;
 import ecologylab.io.Downloadable;
 
 /**
  * Closure that keeps state about a download, as it progresses.
  */
 public class DownloadState<T extends Downloadable>
 extends Debug
 {
 	T													downloadable;
 	
 	private Continuation<T>		continuation;
 	private DownloadMonitor		downloadMonitor;
 	private Thread						downloadingThread;
 
 	private boolean						continued;
 
 
 	DownloadState(T downloadable, Continuation<T> dispatchTarget,DownloadMonitor downloadMonitor)
 	{
 		this.downloadable			= downloadable;
 		this.continuation			= dispatchTarget;
 		this.downloadMonitor	= downloadMonitor;
 	}
 
 	boolean shouldCancel()
 	{
 		boolean result				= downloadable.isRecycled();
 		if (!result)
 		{
 			BasicSite site = downloadable.getSite();
 			if (site != null)
 				result						= site.isDown();
 		}
 		return result;
 	}
 
 	/**
 	 * Do the work to download this.
 	 * @throws IOException 
 	 * 
 	 * @throws Exception
 	 */
 	void performDownload() throws IOException
 	{
 		downloadingThread		= Thread.currentThread();
 		//TODO need a lock here to prevent recycle() while downloading!!!!!!
		if (!downloadable.isRecycled())
 		{
 			//Update site statistics if available
 			BasicSite site = downloadable.getSite();
 			if(site != null)
 				site.beginActualDownload();
 			downloadable.performDownload();
 			if(site != null)
 				site.countNormalDownload();
 		}
 	}
 	protected synchronized void callContinuation()
 	{
		if (!continued)
 		{
 			//	 debug("dispatch()"+" "+downloadable+" -> "+dispatchTarget);
 			continued		= true;
 			downloadMonitor.dispatched++;
 			if (continuation != null)
 				continuation.callback(downloadable);
 		}
 	}
 
 	public String toString()
 	{
		return super.toString() + "["+downloadable.toString() +" "+
 		downloadingThread + "]";
 	}
 	
 	public void recycle(boolean recycleDownloadable)
 	{
		if (recycleDownloadable)
			downloadable.recycle();
		downloadable	= null;
		continuation	= null;
		downloadMonitor	= null;
		downloadingThread	= null;
		
 	}
 
 }
