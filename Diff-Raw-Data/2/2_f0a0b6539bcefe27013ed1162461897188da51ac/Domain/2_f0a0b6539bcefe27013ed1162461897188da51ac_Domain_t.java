 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2007 Xerox Corporation
 // 
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 
 package com.xerox.amazonws.sdb;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.GetMethod;
 
 import com.xerox.amazonws.common.AWSQueryConnection;
 import com.xerox.amazonws.typica.sdb.jaxb.QueryResponse;
 
 /**
  * This class provides an interface with the Amazon SDB service. It provides methods for
  * listing and deleting items.
  *
  * @author D. Kavanagh
  * @author developer@dotech.com
  */
 public class Domain extends AWSQueryConnection {
 
     private static Log logger = LogFactory.getLog(Domain.class);
 
 	private String domainName;
 	private int maxThreads = 30;
 	private ThreadPoolExecutor executor;
 
     protected Domain(String domainName, String awsAccessId,
 							String awsSecretKey, boolean isSecure,
 							String server) throws SDBException {
         super(awsAccessId, awsSecretKey, isSecure, server, isSecure ? 443 : 80);
 		this.domainName = domainName;
 		SimpleDB.setVersionHeader(this);
     }
 
 	/**
 	 * Gets the name of the domain represented by this object.
 	 *
      * @return the name of the domain
 	 */
 	public String getName() {
 		return domainName;
 	}
 
 	/**
 	 * Gets the max number of threads to use for the threaded operations.
 	 *
      * @return max number of threads being used
 	 */
 	public int getMaxThreads() {
 		return maxThreads;
 	}
 
 	/**
 	 * Sets the max number of threads to use for the threaded operations.
 	 *
 	 * @param threads the new max to set
 	 */
 	public void setMaxThreads(int threads) {
 		maxThreads = threads;
 	}
 
 	/**
 	 * Method for getting an Item object without getting a list of them.
 	 *
 	 * @param identifier id of the item
      * @return the object representing the item
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public Item getItem(String identifier) throws SDBException {
 		Item ret = new Item(identifier, domainName, getAwsAccessKeyId(), getSecretAccessKey(),
 										isSecure(), getServer());
 		ret.setSignatureVersion(getSignatureVersion());
 		ret.setHttpClient(getHttpClient());
 		return ret;
 	}
 
 	/**
 	 * Gets a list of all items in this domain
 	 *
      * @return the object containing the items, a more token, etc.
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public QueryResult listItems() throws SDBException {
 		return listItems(null);
 	}
 
 	/**
 	 * Gets a list of items in this domain filtered by the query string.
 	 *
 	 * @param queryString the filter statement
      * @return the object containing the items, a more token, etc.
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public QueryResult listItems(String queryString) throws SDBException {
 		return listItems(queryString, null);
 	}
 
 	/**
 	 * Gets a list of items in this domain filtered by the query string.
 	 *
 	 * @param queryString the filter statement
 	 * @param nextToken the token used to return more items in the query result set
      * @return the object containing the items, a more token, etc.
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public QueryResult listItems(String queryString, String nextToken) throws SDBException {
		return listItems(queryString, nextToken, 0);
 	}
 
 	/**
 	 * Gets a list of items in this domain filtered by the query string.
 	 *
 	 * @param queryString the filter statement
 	 * @param nextToken the token used to return more items in the query result set
 	 * @param maxResults a limit to the number of results to return now
      * @return the object containing the items, a more token, etc.
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public QueryResult listItems(String queryString, String nextToken, int maxResults) throws SDBException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("DomainName", domainName);
 		params.put("QueryExpression", (queryString==null)?"":queryString);
 		if (nextToken != null) {
 			params.put("NextToken", nextToken);
 		}
 		if (maxResults > 0) {
 			params.put("MaxNumberOfItems", ""+maxResults);
 		}
 		GetMethod method = new GetMethod();
 		try {
 			QueryResponse response =
 						makeRequest(method, "Query", params, QueryResponse.class);
 			return new QueryResult(response.getQueryResult().getNextToken(),
 					Item.createList(response.getQueryResult().getItemNames().toArray(new String[] {}), domainName,
 								getAwsAccessKeyId(), getSecretAccessKey(),
 								isSecure(), getServer(), getSignatureVersion(), getHttpClient()));
 		} catch (JAXBException ex) {
 			throw new SDBException("Problem parsing returned message.", ex);
 		} catch (HttpException ex) {
 			throw new SDBException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SDBException(ex.getMessage(), ex);
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Gets attributes of given items. This method threads off the get requests and
 	 * aggregates the responses.
 	 *
 	 * @param items the list of items to get attributes for
      * @return the map of items with lists of attributes
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public Map<String, List<ItemAttribute>> getItemsAttributes(List<String> items) throws SDBException {
 		Map<String, List<ItemAttribute>> results = new Hashtable<String, List<ItemAttribute>>();
 		ThreadPoolExecutor pool = getThreadPoolExecutor();
 		pool.setRejectedExecutionHandler(new RejectionHandler());
 
 		Counter running = new Counter(0);
 		for (String item : items) {
 			while (pool.getActiveCount() == pool.getMaximumPoolSize()) {
 				try { Thread.sleep(100); } catch (InterruptedException ex) { }
 			}
 			synchronized (running) {
 				running.increment();
 			}
 			pool.execute(new AttrWorker(getItem(item), running, results, null));
 			Thread.yield();
 		}
 		while (true) {
 			if (running.getValue() == 0) {
 				break;
 			}
 			try { Thread.sleep(500); } catch (InterruptedException ex) { }
 		}
 		if (this.executor == null) {
 			pool.shutdown();
 		}
 		return results;
 	}
 
 	/**
 	 * Gets attributes of given items. This method threads off the get requests and
 	 * aggregates the responses.
 	 *
 	 * @param items the list of items to get attributes for
 	 * @param listener class that will be notified when items are ready
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public void getItemsAttributes(List<String> items, ItemListener listener) throws SDBException {
 		ThreadPoolExecutor pool = getThreadPoolExecutor();
 		pool.setRejectedExecutionHandler(new RejectionHandler());
 
 		Counter running = new Counter(0);
 		for (String item : items) {
 			while (pool.getActiveCount() == pool.getMaximumPoolSize()) {
 				try { Thread.sleep(100); } catch (InterruptedException ex) { }
 			}
 			synchronized (running) {
 				running.increment();
 			}
 			pool.execute(new AttrWorker(getItem(item), running, null, listener));
 			Thread.yield();
 		}
 		while (true) {
 			if (running.getValue() == 0) {
 				break;
 			}
 			try { Thread.sleep(500); } catch (InterruptedException ex) { }
 		}
 		if (this.executor == null) {
 			pool.shutdown();
 		}
 	}
 
 	/**
 	 * Gets attributes of items specified in the query string. This method threads off the
 	 * get requests and aggregates the responses.
 	 *
 	 * @param queryString the filter statement
 	 * @param listener class that will be notified when items are ready
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public void listItemsAttributes(String queryString, ItemListener listener) throws SDBException {
 		ThreadPoolExecutor pool = getThreadPoolExecutor();
 		pool.setRejectedExecutionHandler(new RejectionHandler());
         String nextToken = "";
 		Counter running = new Counter(0);
         do {
             try {
                 QueryResult result = listItems(queryString, nextToken, 250);
 				List<Item> items = result.getItemList();
                 for (Item i : items) {
 					while (pool.getActiveCount() == pool.getMaximumPoolSize()) {
 						try { Thread.sleep(100); } catch (InterruptedException ex) { }
 					}
 					synchronized (running) {
 						running.increment();
 					}
 					pool.execute(new AttrWorker(i, running, null, listener));
 					Thread.yield();
                 }
                 nextToken = result.getNextToken();
             }
             catch (SDBException ex) {
                 System.out.println("Query '" + queryString + "' Failure: ");
                 ex.printStackTrace();
             }
         } while (nextToken != null && nextToken.trim().length() > 0);
 		while (true) {
 			if (running.getValue() == 0) {
 				break;
 			}
 			try { Thread.sleep(500); } catch (InterruptedException ex) { }
 		}
 		if (this.executor == null) {
 			pool.shutdown();
 		}
 	}
 
 	/**
 	 * Deletes an item.
 	 *
 	 * @param identifier the name of the item to be deleted
 	 * @throws SDBException wraps checked exceptions
 	 */
 	public void deleteItem(String identifier) throws SDBException {
 		getItem(identifier).deleteAttributes(null);
 	}
 
 	static List<Domain> createList(String [] domainNames, String awsAccessKeyId,
 									String awsSecretAccessKey, boolean isSecure,
 									String server, int signatureVersion, HttpClient hc)
 			throws SDBException {
 		ArrayList<Domain> ret = new ArrayList<Domain>();
 		for (int i=0; i<domainNames.length; i++) {
 			Domain dom = new Domain(domainNames[i], awsAccessKeyId, awsSecretAccessKey, isSecure, server);
 			dom.setSignatureVersion(signatureVersion);
 			dom.setHttpClient(hc);
 			ret.add(dom);
 		}
 		return ret;
 	}
 
 	public ThreadPoolExecutor getThreadPoolExecutor() {
 		if (executor != null) {
 			return executor;
 		}
 		else {
 			return new ThreadPoolExecutor(maxThreads, maxThreads, 5,
 							TimeUnit.SECONDS, new ArrayBlockingQueue(maxThreads));
 		}
 	}
 	
 	public void setThreadPoolExecutor(ThreadPoolExecutor executor) {
 		this.executor = executor;
 	}
 
 	protected class RejectionHandler implements RejectedExecutionHandler {
 		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
 			// ok, on the rare occasion, just run it here!
 			r.run();
 		}
 	}
 }
