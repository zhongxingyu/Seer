 package ecologylab.bigsemantics.service.metadata;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import ecologylab.bigsemantics.collecting.DownloadStatus;
 import ecologylab.bigsemantics.filestorage.FileSystemStorage;
 import ecologylab.bigsemantics.metadata.builtins.Document;
 import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
 import ecologylab.bigsemantics.metametadata.MetaMetadata;
 import ecologylab.bigsemantics.service.SemanticServiceErrorCodes;
 import ecologylab.bigsemantics.service.SemanticServiceScope;
 import ecologylab.bigsemantics.service.downloader.controller.DPoolDownloadControllerFactory;
 import ecologylab.bigsemantics.service.logging.ServiceLogRecord;
 import ecologylab.concurrent.DownloadableLogRecord;
 import ecologylab.generic.Continuation;
 import ecologylab.generic.Debug;
 import ecologylab.logging.ILogger;
 import ecologylab.logging.ILoggerFactory;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.formatenums.StringFormat;
 
 /**
  * helper class for metadata.xml and metadata.json
  * 
  * @author ajit
  * 
  */
 public class MetadataServiceHelper extends Debug implements Continuation<DocumentClosure>
 {
 
 	public static final long						CONTINUATION_CHECK_INTERVAL	= 100;
 
 	public static int										CONTINUATION_TIMOUT_CYCLES	= 600;
 
 	static ILogger											serviceLog;
 
 	static ILogger											servicePerfLog;
 
 	private static SemanticServiceScope	semanticsServiceScope;
 
 	static
 	{
 		ILoggerFactory loggerFactory = SemanticServiceScope.get().getLoggerFactory();
 		serviceLog = loggerFactory.getLogger(MetadataServiceHelper.class);
 		servicePerfLog = loggerFactory.getLogger("ecologylab.bigsemantics.service.PERF");
 		semanticsServiceScope = SemanticServiceScope.get();
 		// visitedMetadata = new HashSet<Metadata>();
 	}
 
 	/**
 	 * main document to be returned corresponding to request url
 	 */
 	private Document										document;
 
 	private ServiceLogRecord						logRecord;
 
 	private boolean											finished;
 
 	private Object											lockFinished								= new Object();
 
 	public MetadataServiceHelper()
 	{
 		// this.urlSpanMap = new HashMap<String, Integer>();
 		this.logRecord = new ServiceLogRecord();
 	}
 
 	/**
    * The entry method that accepts a URL and returns a Response with extracted metadata.
    * 
    * @param url
    * @param format
    * @param reload
    * @return
    */
   public Response getMetadataResponse(ParsedURL url, StringFormat format, boolean reload)
   {
   	Response resp = null;
   
   	logRecord.setBeginTime(new Date());
   	Document document = getMetadata(url, reload);
   	if (document != null)
   	{
   		try
   		{
   			long millis = System.currentTimeMillis();
   			String responseBody = SimplTypesScope.serialize(document, format).toString();
   			logRecord.setmSecInSerialization(System.currentTimeMillis() - millis);
   
   			resp = Response.status(Status.OK).entity(responseBody).build();
   		}
   		catch (SIMPLTranslationException e)
   		{
   			e.printStackTrace();
   			serviceLog.error("exception while serializing document: %s", e.getMessage());
   			resp = Response
   			    .status(Status.INTERNAL_SERVER_ERROR)
   					.entity(SemanticServiceErrorCodes.INTERNAL_ERROR)
   					.type(MediaType.TEXT_PLAIN)
   					.build();
   		}
   	}
   	else
   	{
   		serviceLog.error("metadata couldn't be obtained for [%s]", url);
   		resp = Response
   		    .status(Status.NOT_FOUND)
   				.entity(SemanticServiceErrorCodes.METADATA_NOT_FOUND)
   				.type(MediaType.TEXT_PLAIN)
   				.build();
   	}
   
   	logRecord.setMsTotal(System.currentTimeMillis() - logRecord.getBeginTime().getTime());
   	logRecord.setResponseCode(resp.getStatus());
   	servicePerfLog.info(serializeToString(logRecord, StringFormat.JSON));
   
   	return resp;
   }
 
   Document getMetadata(ParsedURL url, boolean reload)
   {
   	this.finished = false;
   
   	requestMetadata(url, reload);
 //  	waitForFinish();
   
   	return this.document;
   }
 
   private void requestMetadata(ParsedURL purl, boolean reload)
   	{
   		logRecord.setRequestUrl(purl);
   
   		// get or construct the document
   		Document document = semanticsServiceScope.getOrConstructDocument(purl);
   		logRecord.setDocumentUrl(document.getLocation());
   		serviceLog.debug("Document from the service document collection for URL[%s]: %s",
   		                 purl,
   		                 document);
   		serviceLog.debug("Download status of %s: %s", document, document.getDownloadStatus());
   
   		ParsedURL docPurl = document.getLocation();
   		
   		// deal with reload and noCache
   		MetaMetadata mmd = (MetaMetadata) document.getMetaMetadata();
   		assert mmd != null;
   		boolean noCache = mmd.isNoCache();
   		reload = reload
   		         || document.getDownloadStatus() == DownloadStatus.IOERROR
   		         || document.getDownloadStatus() == DownloadStatus.RECYCLED;
   		if (noCache || reload)
   		{
   			removeFromServiceDocumentCollection(purl, docPurl);
   			document = semanticsServiceScope.getOrConstructDocument(purl);
   		}
   		if (reload)
   		{
   			removeFromCache(docPurl);
   		}
   
   		// take actions based on the status of the document
   		switch (document.getDownloadStatus())
   		{
   		case UNPROCESSED:
   		  download(document);
 //  			queueDocumentForDownload(document);
   			break;
   		case QUEUED:
   		case CONNECTING:
   		case PARSING:
   			logRecord.setDocumentCollectionCacheHit(true);
   			serviceLog.debug("%s has been cached in service global document collection; "
   			                 + "adding continuation to its closure.", document);
   			DocumentClosure closure = document.getOrConstructClosure(new DPoolDownloadControllerFactory());
  			closure.addContinuation(this);
  			this.document = document;
   			break;
   		case IOERROR:
   		case RECYCLED:
   		  reload = true;
   			// intentionally fall through the next case.
   			// the idea is: when the document is in state IOERROR or RECYCLED, it should be reloaded.
   		case DOWNLOAD_DONE:
   			if (reload)
   			{
   				// redownload and parse document
   				document = semanticsServiceScope.getOrConstructDocument(purl);
   				this.document = document;
   				download(document);
 //  				queueDocumentForDownload(document);
   			}
   			else
   			{
     			logRecord.setDocumentCollectionCacheHit(true);
     			serviceLog.debug("%s has been cached in service global document collection", document);
   
   			  // document is already in global document collection and we don't have to redownload
   			  // use it directly and return results immediately
  				this.document = document;
 //  				setFinished(true);
   			}
   			break;
   		}
   	}
 
   private void queueDocumentForDownload(Document document)
   {
     DocumentClosure closure =
         document.getOrConstructClosure(new DPoolDownloadControllerFactory());
   	                                                         
   	closure.addContinuation(this);
   	closure.setLogRecord(logRecord);
   	serviceLog.debug("Queueing %s for downloading.", document);
   	closure.queueDownload();
   }
 
   private void download(Document document)
   {
     DocumentClosure closure = document.getOrConstructClosure(new DPoolDownloadControllerFactory());
     assert closure != null : "null closure for " + document + "!";
     closure.addContinuation(this);
     try
     {
       closure.performDownload();
      callback(closure);
     }
     catch (IOException e)
     {
       serviceLog.error("error: %s\n%s", e.getMessage(), e.getStackTrace());
       e.printStackTrace();
     }
   
 //    setFinished(true);
   }
 
   @Override
 	public synchronized void callback(DocumentClosure incomingClosure)
 	{
 		Document newDoc = incomingClosure.getDocument();
 		if (document != null && document != newDoc)
 		{
 			serviceLog.debug("remapping old %s to new %s", document, newDoc);
 			semanticsServiceScope.getLocalDocumentCollection().remap(document, newDoc);
 		}
 		document = newDoc;
		// if (origDoc != newDoc)
		// {
		// semanticsServiceScope.getGlobalCollection().remap(origDoc, newDoc);
		// }
		// generateSpan(newDoc);
 
 //		setFinished(true);
 
 		DownloadableLogRecord docLogRecord = incomingClosure.getLogRecord();
 		if (docLogRecord != null)
 			serviceLog.info("%s", serializeToString(docLogRecord, StringFormat.JSON));
 	}
 	
 //  private void waitForFinish()
 //	{
 //		if (!finished)
 //		{
 //			synchronized (lockFinished)
 //			{
 //				for (int i = 0; !finished && i < CONTINUATION_TIMOUT_CYCLES; ++i)
 //				{
 //					try
 //					{
 //						lockFinished.wait(CONTINUATION_CHECK_INTERVAL);
 //					}
 //					catch (InterruptedException e)
 //					{
 //						e.printStackTrace();
 //					}
 //				}
 //				if (!finished)
 //				{
 //					serviceLog.error("Request timed out%s.", document != null ? document : "");
 //				}
 //			}
 //		}
 //	}
 //
 //	private void setFinished(boolean finished)
 //	{
 //		synchronized (lockFinished)
 //		{
 //			this.finished = finished;
 //		}
 //	}
 
 	/**
    * @param thatPurl
    * @param docPurl
    */
   private void removeFromServiceDocumentCollection(ParsedURL thatPurl, ParsedURL docPurl)
   {
     serviceLog.debug("removing document [%s] from service global collection", thatPurl);
     semanticsServiceScope.getLocalDocumentCollection().remove(thatPurl);
     if (!docPurl.equals(thatPurl))
     {
     	serviceLog.debug("removing document [%s] from service global collection", docPurl);
     	semanticsServiceScope.getLocalDocumentCollection().remove(docPurl);
     }
   }
 
   /**
    * @param docPurl
    */
   private void removeFromCache(ParsedURL docPurl)
   {
     // remove from caches
     serviceLog.debug("removing document [%s] from caches", docPurl);
     semanticsServiceScope.getPersistentDocumentCache().removeDocument(docPurl);
     FileSystemStorage.getStorageProvider().removeFileAndMetadata(docPurl);
   }
 
 //	boolean isFinished()
 //	{
 //	  synchronized (lockFinished)
 //	  {
 //  	  return finished;
 //	  }
 //	}
 
 	ServiceLogRecord getServiceLogRecord()
 	{
 		return logRecord;
 	}
 
 	private static String serializeToString(Object obj, StringFormat format)
 	{
 		try
 		{
 			return SimplTypesScope.serialize(obj, format).toString();
 		}
 		catch (SIMPLTranslationException e)
 		{
 			e.printStackTrace();
 			return "Cannot serialize " + obj + " to string: " + e.getMessage();
 		}
 	}
 
 	// private static String decodeUrl(String url)
 	// {
 	// if (url != null)
 	// {
 	// try
 	// {
 	// return URLDecoder.decode(url, "UTF-8");
 	// }
 	// catch (UnsupportedEncodingException e)
 	// {
 	// e.printStackTrace();
 	// }
 	// }
 	// return url;
 	// }
 
 	// /**
 	// * used by document iterator
 	// */
 	// private static HashSet<Metadata> visitedMetadata;
 
 	// private int span;
 
 	// private Document origDoc = null;
 
 	// /**
 	// * url to graph level map
 	// */
 	// private HashMap<String, Integer> urlSpanMap;
 
 	// private void generateSpan(Document document)
 	// {
 	// // locallocation not required in response
 	// document.setLocalLocationMetadata(null);
 	//
 	// int level = -1;
 	// String loc = getLocationInMap(document);
 	// if (loc == null)
 	// return;
 	// else
 	// level = urlSpanMap.get(loc);
 	//
 	// serviceLog.debug("span: " + level + " document: " + document);
 	//
 	// // set the document that is to be returned
 	// if (level == 0)
 	// this.document = document;
 	//
 	// if (level++ < this.span)
 	// {
 	// MetadataBase md;
 	// ClassAndCollectionIterator iter;
 	//
 	// iter = document.metadataIterator(visitedMetadata);
 	// while (iter.hasNext())
 	// {
 	// md = iter.next();
 	// // System.out.println("metadata: " + md);
 	// if (md instanceof Document)
 	// {
 	// Document doc1 = (Document) md;
 	// if (doc1.getLocation() != null)
 	// requestMetadata(doc1.getLocation(), level);
 	// else
 	// serviceLog.warn("location is null for document: " + doc1);
 	// // don't track linked documents as they get added to GlobalDocumentCollection
 	// }
 	// }
 	// }
 	//
 	// // notify main thread to return response if queue is empty
 	// synchronized (urlSpanMap)
 	// {
 	// urlSpanMap.remove(loc);
 	// if (urlSpanMap.isEmpty())
 	// urlSpanMap.notify();
 	// }
 	// }
 
 	// private String getLocationInMap(Document document)
 	// {
 	// // check level by getting span from the hashmap
 	// String loc = (document instanceof Image) ? ((Image) document).getInternetLocation().toString()
 	// : document.getLocation().toString();
 	// loc = decodeUrl(loc);
 	// if (!urlSpanMap.containsKey(loc))
 	// {
 	// List<MetadataParsedURL> additionalLocations = document.getAdditionalLocations();
 	// if (additionalLocations != null)
 	// {
 	// for (MetadataParsedURL additionalLocation : additionalLocations)
 	// {
 	// loc = additionalLocation.getValue().toString();
 	// loc = decodeUrl(loc);
 	// if (urlSpanMap.containsKey(loc))
 	// {
 	// return loc;
 	// }
 	// }
 	// }
 	//
 	// StringBuilder sb = StringBuilderUtils.acquire();
 	// for (String existingUrl : urlSpanMap.keySet())
 	// sb.append("    ").append(existingUrl).append("\n");
 	// String content = sb.toString();
 	// StringBuilderUtils.release(sb);
 	// serviceLog.warn(document
 	// + " location doesn't match with the queued location! recorded locations:\n"
 	// + content);
 	// return null;
 	// }
 	// else
 	// return loc;
 	// }
 
 }
