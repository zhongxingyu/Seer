 /** 
  *  Copyright 2007 Applied Research in Patacriticism and the University of Virginia
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  **/
 package org.nines;
 
 import org.apache.commons.httpclient.ConnectTimeoutException;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.NoHttpResponseException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.XMLOutputter;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 public class RDFIndexer {
 
   private int numFiles = 0;
   private int numObjects = 0;
   private int numToDelete = 0;
   private String archive = null;
   private String guid = "";
   private RDFIndexerConfig config;
   private ConcurrentLinkedQueue<File> dataFileQueue;
   private ErrorReport errorReport;
   private LinkCollector linkCollector;
   private int progressMilestone = 0;
   private long lastTime;
   private String solrURL;
   private Logger log;
 
   private static final int SOLR_REQUEST_NUM_RETRIES = 5; // how many times we should try to connect with solr before giving up
   private static final int SOLR_REQUEST_RETRY_INTERVAL = 30 * 1000; // milliseconds
   public static final int HTTP_CLIENT_TIMEOUT = 2*60*1000; // 2 minutes
   private static final int NUMBER_OF_INDEXER_THREADS = 5;
   private static final int PROGRESS_MILESTONE_INTERVAL = 100;
   private static final int DOCUMENTS_PER_POST = 100;
   
   public RDFIndexer( File rdfSource, RDFIndexerConfig config )  {
 	  	
     initSystem();   
     
     log.info(config.retrieveFullText ? "Online: Indexing Full Text" : "Offline: Not Indexing Full Text"); 
 
     this.config = config;
     this.solrURL = config.solrBaseURL + "/update";
     
     File reportFile = new File(rdfSource.getPath() + File.separatorChar + "report.txt");
     
     try {
         errorReport = new ErrorReport(reportFile);
     } 
     catch (IOException e1) {
         log.error("Unable to open error report log for writing, aborting indexer.");
         return;
     }
     
     linkCollector = new LinkCollector();
 
     HttpClient client = new HttpClient();
 	
     if (rdfSource.isDirectory()) {
     	createGUID(rdfSource);
     	Date start = new Date();
     	log.info("Started indexing at " + start);
 
     	indexDirectory(rdfSource);
         if( config.commitToSolr ) commitDocumentsToSolr(client);
        else log.info("Skipping Commmit to SOLR...");
 	    
         // report done
         Date end = new Date();	
         long duration = (end.getTime() - start.getTime()) / 60000;	
         log.info("Indexed " + numFiles + " files (" + numObjects + " objects) in " + duration + " minutes");
     } 
     else {
     	errorReport.addError(new IndexerError("","","No objects found"));
     }
 
     if (config.commitToSolr && archive != null) {
       // find out how many items will be deleted
       numToDelete = numToDelete(guid, archive, client );
       log.info("Deleting "+numToDelete+" duplicate existing documents from index.");
 
       // remove content that isn't from this batch
       cleanUp(guid, archive, client);
     }
 
     errorReport.close();
     linkCollector.close();
   }
   
   private void commitDocumentsToSolr( HttpClient client ) {
     try {
         if (numObjects > 0) {
             postToSolr("<optimize waitFlush=\"true\" waitSearcher=\"true\"/>",client);
             postToSolr("<commit/>",client);
         }
     }
     catch( IOException e ) {
         errorReport.addError(new IndexerError("","","Unable to POST commit message to SOLR. "+e.getLocalizedMessage()));    		
     }
   }
   
   private void createGUID( File rdfSource ) {
       String path = rdfSource.getPath();
       String file = path.substring(path.lastIndexOf('/') + 1);
 
       try {
         guid = file.substring(0, file.indexOf('.'));
       } catch (StringIndexOutOfBoundsException e) {
         /*
            * In cases where the indexer is run manually against a directory that doesn't
            * specify the GUID, create one automatically.
            */
         guid = java.util.UUID.randomUUID().toString();
       }
   }
 
   private void initSystem() {
     // Use the SAX2-compliant Xerces parser:
     System.setProperty(
         "org.xml.sax.driver",
         "org.apache.xerces.parsers.SAXParser");
     
    
     try {
       // purge old log on startup
       File logFile = new File("indexer.log");
       if (logFile.exists()) {
          logFile.delete();
       }
       
       FileAppender fa = new FileAppender(new PatternLayout("%d{E MMM dd, HH:mm:ss} [%p] - %m\n"), "indexer.log");
       BasicConfigurator.configure( fa );
       log = Logger.getLogger(RDFIndexer.class.getName());
 
     }
     catch( IOException e ) {
         log.error("Error, unable to initialize logging, exiting.");
         System.exit(0);
     }
       
     System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
     System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
 //    System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
 //    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
   }
   
   public static void main(String[] args) {
     if (args.length < 1) {
       System.err.println("java -jar rdf-indexer.jar <rdf dir> [<solr url>] [--fulltext]");
       System.exit(-1);
     }
 
     String fullTextFlag = "--fulltext";
     
     File rdfSource = new File(args[0]);
     RDFIndexerConfig config = new RDFIndexerConfig();
 
     if( args.length > 1 && !args[1].equals(fullTextFlag)) {
         config.solrBaseURL = args[1];
     }
     
     config.retrieveFullText = ( fullTextFlag.equals(args[args.length - 1]) );
 
     new RDFIndexer(rdfSource, config);
   }
 
   private void recursivelyQueueFiles( File dir )  {
 		log.info("loading directory: "+dir.getPath());
 		
 		File fileList[] = dir.listFiles();
 		for( File entry : fileList ) {
 			if( entry.isDirectory() && !entry.getName().endsWith(".svn") ) {
 				recursivelyQueueFiles(entry);
 			}
 			if( entry.getName().endsWith(".rdf") || entry.getName().endsWith(".xml") ) {
 				dataFileQueue.add(entry);
 			}
 		}				
 	}
 	
   private void indexDirectory(File rdfDir) {
     dataFileQueue = new ConcurrentLinkedQueue<File>();
     recursivelyQueueFiles(rdfDir);
     numFiles = dataFileQueue.size();
     
     log.info("=> Indexing " + rdfDir);
     
     initSpeedReporting();
 
     // fire off the indexing threads
     ArrayList<IndexerThread> threads = new ArrayList<IndexerThread>(); 
     for( int i = 0; i < NUMBER_OF_INDEXER_THREADS; i++ ) {
         IndexerThread thread = new IndexerThread(i);
         threads.add(thread);
         thread.start();
     }
     
     waitForIndexingThreads(threads);
   }
   
   private void waitForIndexingThreads( ArrayList<IndexerThread> threads ) {
 	boolean done = false;
     while( !done ) {
     	
     	// nap between checks
     	try {
 			Thread.sleep(100);
 		} catch (InterruptedException e) {}
  
 		// check status
 		done = true;
 		for( IndexerThread thread : threads ) {
     		done = done && thread.isDone();
     	}
     }
   }
  
   private void indexFile(File file, HttpClient client ) {
     try {
     	HashMap<String, HashMap<String, ArrayList<String>>> objects = RdfDocumentParser.parse( file, errorReport, linkCollector, config );
 
 		Set<String> keys = objects.keySet();
 	    for (String uri : keys) {
 	      HashMap<String, ArrayList<String>> object = objects.get(uri);
 	      ArrayList<String> objectArray = object.get("archive");
               if( objectArray != null ) {
                 archive = objectArray.get(0);
               } else {
                 errorReport.addError(new IndexerError(file.getName(), uri, "Unable to determine archive for this object."));
               }
               
 	      ArrayList<ErrorMessage> messages = ValidationUtility.validateObject(object);
 
 	      ListIterator<ErrorMessage> lit = messages.listIterator();
 
 	      while (lit.hasNext()) {
 	        ErrorMessage message = lit.next();
 	        IndexerError e = new IndexerError(file.getName(), uri, message.getErrorMessage());
 	        errorReport.addError(e);
 	      }
 	    }
 
 	    int objectCount = objects.size();
 	    if (objectCount > 0) {
 	      postObjectsToSolr( client, objects );	
 	    } else {
 	      errorReport.addError(new IndexerError(file.getName(), "", "No objects in this file."));
 	    }
 
 	    numObjects += objectCount;
 	    reportIndexingSpeed(numObjects);
 
     } catch (IOException e) {
        errorReport.addError(new IndexerError(file.getName(), "", e.getMessage()));
  	}
     errorReport.flush();
   }
   
   private void initSpeedReporting() {
 	  lastTime = System.currentTimeMillis();
   }
   
   private void reportIndexingSpeed( int objectCount ) {  
     if( objectCount > (progressMilestone + PROGRESS_MILESTONE_INTERVAL) ) {
     	long currentTime = System.currentTimeMillis();
     	float rate = (float)(currentTime-lastTime)/(float)(objectCount-progressMilestone);
     	progressMilestone = objectCount;
     	lastTime = currentTime;
     	log.info("total objects indexed: "+numObjects+" current rate: "+rate+"ms per object."); 	
     }
   }
 
   public void postToSolr(String xml, HttpClient httpclient ) throws IOException {
     PostMethod post = new PostMethod(solrURL);
     post.setRequestEntity(new StringRequestEntity(xml, "text/xml", "utf-8"));
     post.setRequestHeader("Content-type", "text/xml; charset=utf-8");
 
     httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(HTTP_CLIENT_TIMEOUT);
     // Execute request
     try {
       int result;
       int solrRequestNumRetries = SOLR_REQUEST_NUM_RETRIES;
       do {
         result = httpclient.executeMethod(post);
         solrRequestNumRetries--;
         if(result != 200) {
           try {
             Thread.sleep(SOLR_REQUEST_RETRY_INTERVAL);
           } catch(InterruptedException e) {
             log.info(">>>> Thread Interrupted");
           }
         }
       } while(result != 200 && solrRequestNumRetries > 0);
 
       if (result != 200) {
         throw new IOException("Non-OK response: " + result + "\n\n" + xml);
       }
       String response = post.getResponseBodyAsString();
 //      log.info(response);
       Pattern pattern = Pattern.compile("status=\\\"(\\d*)\\\">(.*)\\<\\/result\\>", Pattern.DOTALL);
       Matcher matcher = pattern.matcher(response);
       while (matcher.find()) {
         String status = matcher.group(1);
         String message = matcher.group(2);
         if (!"0".equals(status)) {
           throw new IOException(message);
         }
       }
     } finally {
       // Release current connection to the connection pool once you are done
       post.releaseConnection();
     }
   }
 
   /**
    * Sends an http message to the adminapp to notify that indexing is finished for the
    * batch identified by the guid
    */
   private void sendMessage(String guid, String messageUrl, HttpClient httpclient) throws IOException {
     ErrorSummary summary = errorReport.getSummary();
 
     GetMethod get = new GetMethod(messageUrl);
     NameValuePair guidParam = new NameValuePair("guid", guid);
     NameValuePair archiveParam = new NameValuePair("archive", this.archive);
     NameValuePair stat1 = new NameValuePair("stats[totalFileCount]", Integer.toString(numFiles));
     NameValuePair stat2 = new NameValuePair("stats[totalObjectCount]", Integer.toString(numObjects));
     NameValuePair stat3 = new NameValuePair("stats[fileErrorCount]", Integer.toString(summary.getFileCount()));
     NameValuePair stat4 = new NameValuePair("stats[objectErrorCount]", Integer.toString(summary.getObjectCount()));
     NameValuePair stat5 = new NameValuePair("stats[totalErrorCount]", Integer.toString(summary.getErrorCount()));
     NameValuePair stat6 = new NameValuePair("stats[objectsDeleted]", Integer.toString(numToDelete));
     NameValuePair params[] = new NameValuePair[]{guidParam, archiveParam, stat1, stat2, stat3, stat4, stat5, stat6};
     get.setQueryString(params);
 
     int result;
     try {
       result = httpclient.executeMethod(get);
 
       if (result != 200) {
         if (result >= 400 && result < 500) {
           throw new IOException(result+" code returned for URL: " + messageUrl);
         }
       }
     } catch (NoHttpResponseException e) {
       throw new IOException("The message server didn't respond to the http request to: " + messageUrl);
     } catch (ConnectTimeoutException e) {
       throw new IOException("The message server timed out on the http request to: " + messageUrl);
     } finally {
       get.releaseConnection();
     }
   }
 
   /**
    * Connects to solr and removes all content for a given archive that is not part of the current batch,
    * as denoted by the guid.  This makes it possible to index all new content and to strip out the old as the
    * last operation.
    */
   private void cleanUp(String guid, String archive, HttpClient httpclient)  {
     String luceneQuery = "-batch:\"" + guid + "\" +archive:\"" + archive + "\"";
     String solrXml = "<delete><query>" + luceneQuery + "</query></delete>";
 
     try {
 	    postToSolr(solrXml,httpclient);
 	    postToSolr("<commit/>",httpclient);
     } catch( IOException e ) {
     	errorReport.addError(new IndexerError("","","Unable to POST commit message to SOLR during cleanup. "+e.getLocalizedMessage()));    		
     }
   }
 
   private int numToDelete(String guid, String archive, HttpClient httpclient ) {
     String luceneQuery = "-batch:\"" + guid + "\" +archive:\"" + archive + "\"";
     int numToDelete = 0;
     
     String solrUrl = config.solrBaseURL + "/select";
 
     GetMethod get = new GetMethod(solrUrl);
     NameValuePair queryParam = new NameValuePair("q", luceneQuery);
     NameValuePair params[] = new NameValuePair[]{queryParam};
     get.setQueryString(params);
 
     int result;
     try {
       int solrRequestNumRetries = SOLR_REQUEST_NUM_RETRIES;
       do {
         result = httpclient.executeMethod(get);
         solrRequestNumRetries--;
         if(result != 200) {
           try {
             Thread.sleep(SOLR_REQUEST_RETRY_INTERVAL);
           } catch(InterruptedException e) {
             log.info(">>>> Thread Interrupted");
           }
         }
       } while(result != 200 && solrRequestNumRetries > 0);
 
       if (result != 200) {
         errorReport.addError(new IndexerError("","","cannot reach URL: " + solrUrl));
       }
 
       String response = get.getResponseBodyAsString();
 
       Pattern pattern = Pattern.compile("numFound=\\\"(.)\\\"", Pattern.DOTALL);
       Matcher matcher = pattern.matcher(response);
       while (matcher.find()) {
         String numFound = matcher.group(1);
         if (numFound != null)
           numToDelete = Integer.parseInt(numFound);
       }
     } catch (NoHttpResponseException e) {
       errorReport.addError(new IndexerError("","","The SOLR server didn't respond to the http request to: " + solrUrl));
     } catch (ConnectTimeoutException e) {
       errorReport.addError(new IndexerError("","","The SOLR server timed out on the http request to: " + solrUrl));
     } catch (IOException e) {
       errorReport.addError(new IndexerError("","","An IO Error occurred attempting to access: " + solrUrl));
 	} 
     finally {
       get.releaseConnection();
     }
 
     return numToDelete;
   }
   
   private Element convertObjectToSolrDOM( String documentName, HashMap<String, ArrayList<String>> fields) {
     
 	Element doc = new Element("doc");
     Set<String> fieldVals = fields.keySet();
     for (String field : fieldVals) {
     	// loop over each value per field
         ArrayList<String> valList = fields.get(field);
 
         ListIterator<String> lit = valList.listIterator();
 
         while (lit.hasNext()) {
           Element f = new Element("field");
           f.setAttribute("name", field);
           ValidationUtility.populateTextField(f, lit.next());
           doc.addContent(f);         
         }  
     }
     
     // tag the document with the batch id
     Element f = new Element("field");
     f.setAttribute("name", "batch");
     f.setText(guid);
     doc.addContent(f);
     
     return doc;
   }
 
   private void postObjectsToSolr( HttpClient client, HashMap<String, HashMap<String, ArrayList<String>>> documents ) throws IOException {
 	XMLOutputter outputter = new XMLOutputter();
 	Set<String> keys = documents.keySet();
 
     int documentCount = 0;
     Document solrDoc = new Document();
     Element root = new Element("add");
     solrDoc.addContent(root);
     
     for (String uri : keys) { 	
       HashMap<String, ArrayList<String>> fields = documents.get(uri);   
       Element document = convertObjectToSolrDOM( uri, fields );
       root.addContent(document);	      
       
       if( documentCount++ >= DOCUMENTS_PER_POST ) {
 	      // post this set of documents
     	  String xml = outputter.outputString(solrDoc);
 	      postToSolr(xml,client);
 	      
 	      // reset for next post
 	      solrDoc = new Document();
 	      root = new Element("add");
 	      solrDoc.addContent(root);
 	      documentCount = 0;
      }
     }
     
     if( documentCount > 0 ) {
 	 String xml = outputter.outputString(solrDoc);
 	 postToSolr(xml,client);
     }
   }
 
   private class IndexerThread extends Thread {
 	  
    private boolean done;
    private HttpClient httpClient;
 
    public IndexerThread( int threadID ) {
 	   setName("IndexerThread"+threadID);
 	   httpClient = new HttpClient();
 	   httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(HTTP_CLIENT_TIMEOUT);
 	   done = false;
    }
 	  
 	@Override
 	public void run() {
 		File file = null;
 	    while( (file = dataFileQueue.poll()) != null ) {
 	    	indexFile(file,httpClient);
 	    }
 	    done = true;
 	}
 
 	public boolean isDone() {
 		return done;
 	}
   
   }
 
 
 }
