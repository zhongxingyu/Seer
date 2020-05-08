 package at.fakeroot.sepm.crawler;
 import java.io.*;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 
 import at.fakeroot.sepm.shared.client.serialize.BoundingBox;
 import at.fakeroot.sepm.shared.server.DBGeoObject;
 
 /**
  * Abstract Crawler Class. Includes all necessary functions for crawling, and the communication with the server.   
  * @author JB
  *
  */
 public abstract class ICrawler  {
 	private BoundingBox crawlArea;
 	private BoundingBox curBox;
 	
 	private double XOFFSET=0.5;
 	private double YOFFSET=0.5;
 	
 	private boolean newCircle=false;
 	
 	private int serviceID;
 	
 	// Create an instance of HttpClient.
 	private HttpClient client = new HttpClient();
 	
 	private Registry registry;
 	private GeoSave geoSaver;
 	
 	
 	//BoundingBoxes
 	public static BoundingBox AUTRIA = new BoundingBox(10.0,10.0,40.0,40.0);
 	
 	/**
 	 * Standard constructor 
 	 * * BoundingBox is AUSTRIA,
 	 * * Step size is 0.5
 	 * * ServerHost is localhost
 	 * * ServerPort is RMI-Std. Port
 	 * @param SvcName Service Name (eg. Wiki_de, Panoramio)
 	 */
	public ICrawler(String SvcName) {
		this(AUTRIA, 0.5, SvcName);
 	}	
 	
 	/**
 	 * Standard constructor 
 	 * * Step size is 0.5
 	 * * ServerHost is localhost
 	 * * ServerPort is RMI-Std. Port
 	 * @param _crawlArea Search Area (eg. Austria, Europe, Word)
 	 * @param SvcName Service Name (eg. Wiki_de, Panoramio)
 	 */
	public ICrawler(BoundingBox _crawlArea, String SvcName){
		this(_crawlArea, 0.5, SvcName);
 	}
 	
 	/**
 	 * Standard constructor 
 	 * * ServerHost is localhost
 	 * * ServerPort is RMI-Std. Port
 	 * @param _crawlArea Search Area (eg. Austria, Europe, Word)
 	 * @param _jumpXYOffset Step size 
 	 * @param SvcName Service Name (eg. Wiki_de, Panoramio)
 	 */
	public ICrawler(BoundingBox _crawlArea, double _jumpXYOffset, String SvcName) {		
		this(_crawlArea, _jumpXYOffset, "localhost", SvcName);
 		
 	}
 	
 	/**
 	 * Standard constructor 
 	 * * ServerPort is RMI-Std. Port
 	 * @param _crawlArea Search Area (eg. Austria, Europe, Word)
 	 * @param _jumpXYOffset Step size 
 	 * @param ServerHost ServerHost name (eg. localhost, www.myCrawler.com, 127.0.0.1)
 	 * @param SvcName Service Name (eg. Wiki_de, Panoramio)
 	 */
	public ICrawler(BoundingBox _crawlArea, double _jumpXYOffset, String ServerHost, String SvcName) {
		 this(_crawlArea, _jumpXYOffset, ServerHost, 1099, SvcName);
 	}
 	
 	/**
 	 * Standard constructor 
 	 * @param _crawlArea Search Area (eg. Austria, Europe, Word)
 	 * @param _jumpXYOffset Step size 
 	 * @param ServerHost ServerHost name (eg. localhost, www.myCrawler.com, 127.0.0.1)
 	 * @param ServerPort Server Port.  (Std. Port is 1099)
 	 * @param SvcName Service Name (eg. Wiki_de, Panoramio)
 	 */
	public ICrawler(BoundingBox _crawlArea, double _jumpXYOffset, String ServerHost, int ServerPort, String SvcName) {
 		crawlArea=_crawlArea;
 		XOFFSET=_jumpXYOffset;
 		YOFFSET=_jumpXYOffset;
 		curBox = new BoundingBox(
 				_crawlArea.getX1(),
 				_crawlArea.getY1(),
 				_crawlArea.getX1()+XOFFSET,
 				_crawlArea.getY1()+YOFFSET);
 		
 		try {
 			registry = LocateRegistry.getRegistry(ServerHost);
 			geoSaver = (GeoSave) registry.lookup("GeoSave");
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (NotBoundException e) {
 			e.printStackTrace();
 		} 
 		
 		serviceID=getServiceID(SvcName);
 	}
 	
 	/**
 	 * Crawls a site at the given URL.
 	 * @param crawlURL URL that should be crawled.  
 	 * @return Response String (eg. xml, json,)
 	 */
 	public String crawl(String crawlURL){
 		// Create a method instance.
 		GetMethod method = new GetMethod(crawlURL);
 		
 		// Provide custom retry handler is necessary
 		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
 				new DefaultHttpMethodRetryHandler(3, false));
 
 		try {
 			// Execute the method.
 			int statusCode = client.executeMethod(method);
 
 			if (statusCode != HttpStatus.SC_OK) {
 				System.err.println("Method failed: " + method.getStatusLine());
 			}
 
 			
 			// Read the response body.
 			byte[] responseBody = method.getResponseBody();
 
 			
 			
 			return new String(responseBody);
 			
 			/*
 			InputStream myStream = method.getResponseBodyAsStream();			
 			BufferedReader in = new BufferedReader(new InputStreamReader(myStream));
 			//in.readLine();
 			String back="";
 			while(in.ready()){
 				back+=in.readLine();
 			}
 			System.out.println(back);
 			*/
 			
 		} catch (HttpException e) {
 			System.err.println("Fatal protocol violation: " + e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.err.println("Fatal transport error: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			// Release the connection.
 			method.releaseConnection();
 		}	
 		return null;
 	}
 	
 	
 	/**
 	 * 
 	 * @return Next BoundingBox in the gives search area. 
 	 */
 	public BoundingBox nextCrawlBox(){		
 		return nextBox(curBox);
 	}
 	
 	
 	/**
 	 * Send an array of DBGeoObjects to the Server
 	 * @param newObjects Array of new Objects
 	 */
 	public void saveObject(DBGeoObject[] newObjects) {		
 		
 		try {
 			geoSaver.saveObject(newObjects);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	/**
 	 * Returns the ServiceID
 	 * @return Returns the ServiceID
 	 */
 	public int getSvcID(){
 		return serviceID;
 	}
 	
 	public String toString() {
 		return curBox.toString();
 	}
 	
 	private int getServiceID(String SvcName){
 		
 		try {
 			return geoSaver.getServiceID(SvcName);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 			return -1;
 		} 
 	}
 	
 	private BoundingBox nextBox(BoundingBox _curPos){
 		
 		curBox=nextRight(_curPos);
 		
 		if(newCircle){
 			//System.out.println("rein");
 			curBox=nextDown(curBox);
 			newCircle=false;
 		}
 		
 		return curBox;
 		
 	}
 	
 	private BoundingBox nextRight(BoundingBox _curPos){
 		if(_curPos.getX1()+XOFFSET>crawlArea.getX2()){
 			//System.err.println("rumpRight "+_curPos);
 			newCircle=true;
 			return new BoundingBox(
 					crawlArea.getX1(),
 					_curPos.getY1(),
 					crawlArea.getX1()+XOFFSET,
 					_curPos.getY2());
 		}else{
 			return new BoundingBox(
 				_curPos.getX2(),
 				_curPos.getY1(),
 				_curPos.getX2()+XOFFSET,
 				_curPos.getY2());
 		}
 		
 		
 	}
 	
 	private BoundingBox nextDown(BoundingBox _curPos){
 		if(_curPos.getY1()+YOFFSET>crawlArea.getY2()){
 			//System.err.println("JumpDown ");
 			return new BoundingBox(
 					_curPos.getX1(),
 					crawlArea.getY1(),
 					_curPos.getX2(),
 					crawlArea.getY1()+YOFFSET);
 		}else{		
 			return new BoundingBox(
 				_curPos.getX1(),
 				_curPos.getY2(),
 				_curPos.getX2(),
 				_curPos.getY2()+YOFFSET);
 		}
 	}
 	
 	private BoundingBox nextLeft(BoundingBox _curPos){
 		return new BoundingBox(
 				_curPos.getX1()-XOFFSET,
 				_curPos.getY1(),
 				_curPos.getX1(),
 				_curPos.getY2());
 	}
 	
 	private BoundingBox nextUp(BoundingBox _curPos){
 		return new BoundingBox(
 				_curPos.getX1(),
 				_curPos.getY1()-YOFFSET,
 				_curPos.getX2(),
 				_curPos.getY1());
 	}
 	
 	
 }
