 package ecologylab.bigsemantics.service.metadata;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ecologylab.bigsemantics.Utils;
 import ecologylab.bigsemantics.collecting.DownloadStatus;
 import ecologylab.bigsemantics.filestorage.FileSystemStorage;
 import ecologylab.bigsemantics.metadata.builtins.Document;
 import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
 import ecologylab.bigsemantics.metametadata.MetaMetadata;
 import ecologylab.bigsemantics.service.SemanticServiceErrorMessages;
 import ecologylab.bigsemantics.service.SemanticServiceScope;
 import ecologylab.bigsemantics.service.logging.ServiceLogRecord;
 import ecologylab.generic.Debug;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.formatenums.StringFormat;
 
 /**
  * helper class for metadata.xml and metadata.json
  * 
  * @author ajit
  */
 public class MetadataServiceHelper extends Debug
     implements SemanticServiceErrorMessages
 {
 
   public static int                   CONTINUATION_TIMOUT = 60000;
 
   private static Logger               logger;
 
   private static Logger               perfLogger;
 
   private static SemanticServiceScope semanticsServiceScope;
 
   static
   {
     logger = LoggerFactory.getLogger(MetadataServiceHelper.class);
     perfLogger = LoggerFactory.getLogger("ecologylab.bigsemantics.service.PERF");
     semanticsServiceScope = SemanticServiceScope.get();
   }
 
   private Document                    document;
 
   private ServiceLogRecord            perfLogRecord;
 
   public MetadataServiceHelper()
   {
     this.perfLogRecord = new ServiceLogRecord();
   }
 
   ServiceLogRecord getServiceLogRecord()
   {
     return perfLogRecord;
   }
 
   /**
    * The entry method that accepts a URL and returns a Response with extracted metadata.
    * 
    * @param purl
    * @param format
    * @param reload
    * @return
    */
   public Response getMetadataResponse(ParsedURL purl, StringFormat format, boolean reload)
   {
     perfLogRecord.setBeginTime(new Date());
     perfLogRecord.setRequestUrl(purl);
     Response resp = null;
 
     document = null;
     getMetadata(purl, reload);
     if (document == null)
     {
       logger.error("Can't construct Document for [{}]", purl);
       resp = Response
           .status(Status.NOT_FOUND)
           .entity(METADATA_NOT_FOUND)
           .type(MediaType.TEXT_PLAIN)
           .build();
     }
     else
     {
       DownloadStatus docStatus = document.getDownloadStatus();
       switch (docStatus)
       {
       case UNPROCESSED:
       case QUEUED:
       case CONNECTING:
       case PARSING:
         logger.error("Unfinished {}, status: {}", document, docStatus);
         break;
       case DOWNLOAD_DONE:
         try
         {
          logger.error("{} downloaded and parsed, generating response", document);
           long t0 = System.currentTimeMillis();
           String responseBody = SimplTypesScope.serialize(document, format).toString();
           perfLogRecord.setMsSerialization(System.currentTimeMillis() - t0);
           resp = Response.status(Status.OK).entity(responseBody).build();
         }
         catch (SIMPLTranslationException e)
         {
           logger.error("Exception while serializing " + document, e);
         }
         break;
       case IOERROR:
       case RECYCLED:
         logger.error("Bad Document status for [{}]: {}", purl, docStatus);
         break;
       }
     }
 
     if (resp == null)
     {
       resp = Response
           .status(Status.INTERNAL_SERVER_ERROR)
           .entity(INTERNAL_ERROR)
           .type(MediaType.TEXT_PLAIN)
           .build();
     }
 
     perfLogRecord.setMsTotal(System.currentTimeMillis() - perfLogRecord.getBeginTime().getTime());
     perfLogRecord.setResponseCode(resp.getStatus());
     perfLogger.info(Utils.serializeToString(perfLogRecord, StringFormat.JSON));
 
     return resp;
   }
 
   Document getMetadata(ParsedURL purl, boolean reload)
   {
     document = semanticsServiceScope.getOrConstructDocument(purl);
     assert document != null : "Null Document returned from the semantics scope!";
 
     ParsedURL docPurl = document.getLocation();
     perfLogRecord.setDocumentUrl(document.getLocation());
     if (!docPurl.equals(purl))
     {
       logger.info("Normalizing {} to {}", purl, docPurl);
     }
 
     DownloadStatus docStatus = document.getDownloadStatus();
     logger.debug("Download status of {}: {}", document, docStatus);
     if (docStatus == DownloadStatus.DOWNLOAD_DONE)
     {
       logger.info("{} found in service in-mem document cache", document);
       perfLogRecord.setInMemDocumentCacheHit(true);
     }
 
     // take actions based on the status of the document
     DocumentClosure closure = document.getOrConstructClosure();
     closure.setLogRecord(perfLogRecord);
     switch (docStatus)
     {
     case UNPROCESSED:
     case QUEUED:
     case CONNECTING:
     case PARSING:
      logger.info("about to download {}, current status: {}", document, docStatus);
       download(closure);
       break;
     case IOERROR:
     case RECYCLED:
      logger.info("about to reload {}, current status: {}", document, docStatus);
       reload = true;
       // intentionally fall through the next case.
       // the idea is: when the document is in state IOERROR or RECYCLED, it should be reloaded.
     case DOWNLOAD_DONE:
       if (reload)
       {
         removeFromPersistentDocumentCache(docPurl);
         // redownload and parse document
         closure.reset();
         download(closure);
       }
       break;
     }
 
     // if no_cache is set, remove the document from local document collection
     MetaMetadata mmd = (MetaMetadata) document.getMetaMetadata();
     if (mmd.isNoCache())
     {
       removeFromLocalDocumentCollection(purl);
       removeFromLocalDocumentCollection(docPurl);
     }
 
     return this.document;
   }
 
   private void download(DocumentClosure closure)
   {
     try
     {
       closure.performDownloadSynchronously();
      logger.info("performed downloading on {}", document);
       Document newDoc = closure.getDocument();
       if (document != null && document != newDoc)
       {
         logger.info("Remapping old {} to new {}", document, newDoc);
         semanticsServiceScope.getLocalDocumentCollection().remap(document, newDoc);
       }
       document = newDoc;
     }
     catch (IOException e)
     {
       logger.error("Error in downloading " + document, e);
     }
   }
 
   /**
    * @param docPurl
    */
   private void removeFromLocalDocumentCollection(ParsedURL docPurl)
   {
     logger.debug("Removing document [{}] from service local document collection", docPurl);
     semanticsServiceScope.getLocalDocumentCollection().remove(docPurl);
   }
 
   /**
    * @param docPurl
    */
   private void removeFromPersistentDocumentCache(ParsedURL docPurl)
   {
     logger.debug("Removing document [{}] from persistent document caches", docPurl);
     semanticsServiceScope.getPersistentDocumentCache().removeDocument(docPurl);
     FileSystemStorage.getStorageProvider().removeFileAndMetadata(docPurl);
   }
 
 }
