 package org.neuro4j.nms.client.j;
 
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.util.EntityUtils;
 import org.neuro4j.core.Network;
 import org.neuro4j.storage.StorageBase;
 import org.neuro4j.storage.StorageException;
 import org.neuro4j.utils.IOUtils;
 import org.neuro4j.xml.ConvertationException;
 import org.neuro4j.xml.NetworkConverter;
 
 /**
  * 
  * Network Management System API Client
  *
  */
 public class NMSClient extends StorageBase {
 	
 	private static final Log logger = LogFactory.getLog(NMSClient.class);
 	private String serverBaseURL; // http://localhost:8080/n4j-nms                        
 
     HttpClient httpClient = null;
     
 	private boolean queryWireXML = false; // true - wire is XML, false - wire is binary 
 	private boolean updateWireXML = true; // true - wire is XML, false - wire is binary 
     
 	private int httpPoolSize = 5;
 
 	
 	/**
 	 * URLs
 	 * query - /api/query
 	 * update - /api/update/xml
 	 *        - /api/update/bin
 	 * 
 	 */
 	public NMSClient()
 	{
 		super();	    
 	}
 
 	public void init(Properties properties) throws StorageException {
 		
 		super.init(properties);
 		
 		this.serverBaseURL = properties.getProperty("org.neuro4j.nms.client.server_url");
 		
 		try {
 			queryWireXML = Boolean.parseBoolean(properties.getProperty("org.neuro4j.nms.client.query_wire_xml").trim());
 		} catch (Exception e) {
 			logger.error("Can't parse org.neuro4j.nms.client.query_wire_xml. Use default [false]");
 		}
 
 		HttpParams params = new BasicHttpParams();
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 		
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
	    schemeRegistry.register(new Scheme("http", 18080, PlainSocketFactory.getSocketFactory()));
 
 	    PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
 	    
 
 	    cm.setDefaultMaxPerRoute(httpPoolSize);
 	    cm.setMaxTotal(httpPoolSize);
 
 
 	    httpClient = new DefaultHttpClient(cm, params);
 
 	    
 //		try {
 //			updateWireXML = Boolean.parseBoolean(properties.getProperty("org.neuro4j.nms.client.update_wire_xml").trim());
 //		} catch (Exception e) {
 //			logger.error("Can't parse org.neuro4j.nms.client.update_wire_xml. Use default [true]");
 //		}
 		
 		return;
 	}
 
 
 	public Network query(String q) throws StorageException
 	{
 		logger.info("Query " + q);
 //		HttpClient httpClient = new DefaultHttpClient();
 
 		List<NameValuePair> params = new LinkedList<NameValuePair>();
 		params.add(new BasicNameValuePair("cmd","query"));
 		params.add(new BasicNameValuePair("q",q));
 		if (!queryWireXML)
 			params.add(new BasicNameValuePair("output","bin"));
 		HttpGet method = new HttpGet(serverBaseURL + "/api/query?" + URLEncodedUtils.format(params, "utf-8"));
 		try 
 		{
 			 HttpResponse response = httpClient.execute(method);
 			if (queryWireXML)
 			{
 				HttpEntity entity = response.getEntity();
 				
 			    InputStream instream = entity.getContent();
 			    Network n = null;
 			    String errorMsg = "can't parse server response";
 			    try {
 			    	{ // check if 
 				    	byte[] b = new byte[5]; 
 				        instream.read(b, 0, 5);
 				        String start = new String(b);
 				        if (!"<?xml".equals(start))
 				        	errorMsg = new String(IOUtils.toByteArray(instream));
 			    	}
 			        
 			    	// In case of exception input stream are closed by JAXB
 					n = NetworkConverter.xml2network(instream);
 				} catch (ConvertationException e) {
 					throw new StorageException(errorMsg);
 				}
 
 			    EntityUtils.consume(entity);
 				
 			    if (200 != response.getStatusLine().getStatusCode())
 					throw new StorageException("Can't query " + method.getURI() + " response code " + response.getStatusLine().getStatusCode());
 
 			    return n;
 			} else {
 				
 				// query response in binary mode
 				HttpEntity entity = response.getEntity();
 			    InputStream instream = entity.getContent();
 			    ObjectInputStream ois = new ObjectInputStream(instream);
 			    Network net;
 			    try {
 			    	net = (Network) ois.readObject();
 			    } catch (ClassNotFoundException e) {
 					throw new StorageException("Can't query " + method.getURI(), e);
 				} finally {
 			        ois.close();
 			    }
 			    EntityUtils.consume(entity);
 			    
 			    if (200 != response.getStatusLine().getStatusCode())
 					throw new StorageException("Can't query " + method.getURI() + " response code " + response.getStatusLine().getStatusCode());
 			    
 		    	return net;
 			}
 		} catch (UnsupportedEncodingException e) {
 			logger.error(e.getMessage(), e);
 		} catch (ClientProtocolException e) {
 			logger.error(e.getMessage(), e);
 			method.abort();
 			throw new StorageException("Can't query " + method.getURI(), e);
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 			method.abort();
 			throw new StorageException("Can't query " + method.getURI(), e);
 		} finally {
 		}		
 
 		return null;
 	}
 
 
 	public boolean save(Network network) throws StorageException {
 		if (updateWireXML)
 			return updateXML(network);
 		else 
 			return updateBin(network);
 	}
 
 	private boolean updateBin(Network network) throws StorageException
 	{
 //		HttpClient httpClient = new DefaultHttpClient();
 
 		HttpPost method = new HttpPost(serverBaseURL + "/api/update/bin");
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		try 
 		{
 			ObjectOutputStream output = new ObjectOutputStream(baos);
 			output.writeObject(network);
 			output.flush();
 			output.close();
 
 			byte[] ba = baos.toByteArray();
 
 			InputStreamEntity isre = new InputStreamEntity(new ByteArrayInputStream(ba), ba.length);
 			method.setEntity(isre);
 			method.setHeader("Content-Type", "application/x-java-serialized-object");
 
 		} catch (IOException e1) {
 			logger.error(e1.getMessage(), e1);
 		}
 
 		// Execute the method.
 		try {
 			HttpResponse response = httpClient.execute(method);
 			
 			EntityUtils.consume(response.getEntity());
 			if (200 != response.getStatusLine().getStatusCode())
 					throw new StorageException("Can't save network to " + method.getURI() + " response code " + response.getStatusLine().getStatusCode());
 
 			return true;
 		} catch (UnsupportedEncodingException e) {
 			logger.error(e.getMessage(), e);
 		} catch (ClientProtocolException e) {
 			logger.error(e.getMessage(), e);
 			method.abort();
 			throw new StorageException("Can't save network to " + method.getURI(), e);
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 			method.abort();
 			throw new StorageException("Can't save network to " + method.getURI(), e);
 		} finally {
 		}		
 		
 		return false;
 	}
 	
 	private boolean updateXML(Network network) throws StorageException {
 	      // Execute the method.
 		HttpPost method = new HttpPost(serverBaseURL + "/api/update/xml");
 		try {
 			String netXml = NetworkConverter.network2xml(network);
 
 
 			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
 			formparams.add(new BasicNameValuePair("cmd", "save"));
 			formparams.add(new BasicNameValuePair("network", netXml));
 			
 			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
 			method.setEntity(entity);
 
 			HttpResponse response = httpClient.execute(method);
 			EntityUtils.consume(response.getEntity());
 
 			if (200 != response.getStatusLine().getStatusCode())
 				throw new StorageException("Can't save network to " + method.getURI() + " response code " + response.getStatusLine().getStatusCode());
 
 			return true;
 		} catch (UnsupportedEncodingException e) {
 			logger.error(e.getMessage(), e);
 		} catch (ClientProtocolException e) {
 			logger.error(e.getMessage(), e);
 			method.abort();
 			throw new StorageException("Can't save network to " + method.getURI(), e);
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 			method.abort();
 			throw new StorageException("Can't save network to " + method.getURI(), e);
 		} finally {
 		}		
 	    return false;
 	}
 
 	public void close()
 	{
 //		httpClient.getConnectionManager().shutdown();					
 	}
 
 	public InputStream getRepresentationInputStream(String id) throws StorageException
 	{
 		InputStream repInput = new NMSRepresentationInputStream(
 											serverBaseURL + "/api/representation/read", id);
 		return repInput;
 	}
 	
 	public OutputStream getRepresentationOutputStream(String id) throws StorageException
 	{
 		OutputStream repOutput = new BufferedOutputStream(
 										new NMSRepresentationOutputStream(
 												serverBaseURL + "/api/representation/update", id));
 		return repOutput;
 	}
 	
 		
 }
