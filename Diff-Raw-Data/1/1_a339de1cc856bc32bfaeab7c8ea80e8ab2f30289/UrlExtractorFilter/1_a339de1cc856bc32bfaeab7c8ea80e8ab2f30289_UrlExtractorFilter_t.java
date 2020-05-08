 
 package org.paxle.data.db.impl;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.paxle.core.data.IDataProvider;
 import org.paxle.core.data.IDataSink;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.LinkInfo;
 import org.paxle.core.doc.LinkInfo.Status;
 import org.paxle.core.filter.IFilter;
 import org.paxle.core.filter.IFilterContext;
 import org.paxle.core.queue.ICommand;
 import org.paxle.data.db.URIQueueEntry;
 
 public class UrlExtractorFilter implements IFilter<ICommand>, IDataProvider<URIQueueEntry> {
 	
 	private static class Counter {
 		public int total = 0;
 		public int enqueued = 0;
 	}
 	
 	/**
 	 * For logging
 	 */
 	private final Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * A queue to buffer all {@link URI} that were recently extracted
 	 * from an {@link IParserDocument} and are enqueued for insertion
 	 * into the {@link #db command-db} 
 	 */
 	private final BlockingQueue<URIQueueEntry> extractedUriQueue;
 
 	/**
 	 * A {@link Thread} used to listen for newly {@link #extractedUriQueue extracted-URI}
 	 * that should be stored into the {@link #db command-db}
 	 */
 	private final URIStorageThread storageThread;
 	
 	private IDataSink<URIQueueEntry> sink = null;
 	
 	public UrlExtractorFilter() {
 		// create the URI queue 
 		this.extractedUriQueue = new LinkedBlockingQueue<URIQueueEntry>();
 		
 		// create and start the worker thread
 		this.storageThread = new URIStorageThread();
 		this.storageThread.start();
 	}
 	
 	public synchronized void setDataSink(IDataSink<URIQueueEntry> dataSink) {
 		if (dataSink == null) throw new NullPointerException("The data-sink is null.");
 		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
 		this.sink = dataSink;
 		this.notify();
 	}
 	
 	public void filter(ICommand command, IFilterContext context) {
 		if (command == null) return;
 		
 		// getting the parser-doc
 		IParserDocument parserDoc = command.getParserDocument();
 		if (parserDoc == null) return;
 		
 		// getting the link map
 		final Counter c = new Counter();
 		this.extractLinks(command, parserDoc, c);
 		logger.info(String.format(
 				"Selected %d URI out of %d URI from '%s' for storage to DB.",
 				Integer.valueOf(c.enqueued), 
 				Integer.valueOf(c.total), 
 				command.getLocation()
 		));
 	}
 	
 	private void extractLinks(final ICommand command, IParserDocument parserDoc, final Counter c) {
 		if (parserDoc == null) return;
 		
 		// getting the link map
 		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
 		if (linkMap != null) {
 			this.extractLinks(command, linkMap, c);
 		}
 		
 		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
 		if (subDocs != null) {
 			for (IParserDocument subDoc : subDocs.values()) {
 				this.extractLinks(command, subDoc, c);
 			}
 		}
 	}
 	
 	private void extractLinks(final ICommand command, Map<URI, LinkInfo> linkMap, final Counter c) {
 		if (linkMap == null) return;
 		
 		final ArrayList<URI> refs = new ArrayList<URI>();		
 		for (Entry<URI, LinkInfo> link : linkMap.entrySet()) {
 			URI ref = link.getKey();
 			LinkInfo meta = link.getValue();
 			c.total++;
 			
 			// check if the URI exceeds max length
 			if (ref.toString().length() > 512) {
 				this.logger.debug("Skipping too long URL: " + ref);
 				continue;
 			} else if (!meta.hasStatus(Status.OK)) {
 				this.logger.debug(String.format(
 						"Skipping URL because of status '%s' (%s): %s",
 						meta.getStatus(),
 						meta.getStatusText(),
 						ref
 				));
 				continue;
 			}
 			
 			c.enqueued++;
 			refs.add(ref);
 		}
 		
 		if (refs.size() > 0) {
 			// add command into URI queue
 			this.extractedUriQueue.add(new URIQueueEntry(
 					command.getLocation(),
 					command.getProfileOID(),
 					command.getDepth() + 1,
 					refs
 			));
 		}
 	}
 	
 	public void terminate() throws InterruptedException {
 		if (this.storageThread != null) {
 			// interrupt thread
 			this.storageThread.interrupt();
 			
 			// wait for shutdown
 			this.storageThread.join(1000);
 		}
 		
 		// clear URI queue
 		this.extractedUriQueue.clear();
 	}
 
 	private class URIStorageThread extends Thread {
 		public URIStorageThread() {
 			this.setName(this.getClass().getSimpleName());
 		}
 		
 		@Override
 		public void run() {
 			try {
 				
 				synchronized (UrlExtractorFilter.this) {
 					while (sink == null) UrlExtractorFilter.this.wait();
 				}
 				
 				while(!this.isInterrupted()) {
 					try {
 						
 						// waiting for the next job
 						URIQueueEntry entry = extractedUriQueue.take();
 						
 						// store unknown URIs
 						
 						// the list is being modified by CommandDB#storeUnknownLocations, so we need to save the size first
 						final int totalLocations = entry.getReferences().size();
 						sink.putData(entry);
 						
 						logger.info(String.format(
 								"Extracted %d new and %d already known URIs from '%s'",
 								Integer.valueOf(totalLocations - entry.getKnown()), 
 								Integer.valueOf(entry.getKnown()), 
 								entry.getRootURI().toASCIIString()
 						));
 						
 					} catch (Throwable e) {
						if (e instanceof InterruptedException) throw (InterruptedException) e;
 						logger.error(String.format(
 								"Unexpected '%s' while trying to store new URI into the command-db.",
 								e.getClass().getName()
 						), e);
 					}
 				}
 			} catch (InterruptedException e) {
 				logger.info(String.format(
 						"Shutdown of' %s' finished. '%d' could not be stored.",
 						this.getName(),
 						Integer.valueOf(extractedUriQueue.size())
 				));
 				return;
 			}
 		}
 	}
 }
