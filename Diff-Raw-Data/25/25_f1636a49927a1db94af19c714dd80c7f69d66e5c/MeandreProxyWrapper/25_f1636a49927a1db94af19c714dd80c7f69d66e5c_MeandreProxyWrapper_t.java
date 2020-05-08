 package org.imirsel.nema.service.impl;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.Semaphore;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.imirsel.meandre.client.MeandreProxy;
 import org.imirsel.meandre.client.TransmissionException;
 import org.meandre.core.repository.ExecutableComponentDescription;
 import org.meandre.core.repository.FlowDescription;
 import org.meandre.core.repository.QueryableRepository;
 import org.springframework.core.io.ClassPathResource;
 
 import com.hp.hpl.jena.rdf.model.Resource;
 
 /**This class wraps the MeandreProxy provided by nema-client
  * and provides mechanism to reconnect to the meandre server incase of a failure
  * 
  * @author Amit Kumar
  *
  */
 public class MeandreProxyWrapper {
 	private static Logger log =  Logger.getLogger(MeandreProxyWrapper.class.getName());
 	private MeandreProxy meandreProxy;
 	private final Semaphore available = new Semaphore(1, true);
 	boolean connected = Boolean.FALSE;
 	
 	
 	public MeandreProxyWrapper(){
 		
 	}
 	
	public void cleanup(){
		if(meandreProxy!=null){
			meandreProxy.destroy();
		}
	}
	
 	public void init(){
		System.out.println("Initing meandre proxy wrapper....");
 		try {
 			available.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			// return and the timer will retry this..
 			available.release();
 			return;
 		}
 		if(connected == Boolean.TRUE){
 			if(meandreProxy.ping()== true){
 				available.release();
 				return;
 			}else{
 				log.severe("Meandre Proxy seems to be connected: but Not able to ping "+ meandreProxy.getBaseURL());
 			}
 		}
 		try{
 		ClassPathResource cr = new ClassPathResource("meandreproxy.properties");
 		if (cr.getInputStream() == null) {
 				log.severe("Error: cannot find meandreproxy.properties in the classpath");
 				connected= Boolean.FALSE;
 		}else{
 		Properties meandreProperties=new Properties();
 		meandreProperties.load(cr.getInputStream());
 		String host = meandreProperties.getProperty("host");
 		String _port = meandreProperties.getProperty("port");
 		String username = meandreProperties.getProperty("username");
 		String password = meandreProperties.getProperty("password");
 		System.out.println("HOST CONNECTING TO: "+ host + ":"+_port );
 		int port = Integer.parseInt(_port);
 		meandreProxy = new MeandreProxy(username,password,host,port);
 		meandreProxy.setCredentials(username, password);
 		log.setLevel(Level.WARNING);
 		meandreProxy.setLogger(log);
 		System.out.println("connecting to " + host + ":"+_port);
 		
 		if(!meandreProxy.getCallOk()){
 			log.severe("Error could not initialize MeandreProxy: callOk: "+ meandreProxy.getCallOk());
 			connected= Boolean.FALSE;
 		}else{
 			connected= Boolean.TRUE;
 		}
 		}
 		} catch (IOException e1) {
 			log.severe(e1.toString());
 			connected= Boolean.FALSE;
 		}finally{
 		available.release();
 		}
 	}
 	
 	public Set<Resource> getAvailableFlows() throws TransmissionException{
 		if(!connected){
 			log.severe("meandre proxy is not connected to the meandre server");
 			throw new TransmissionException("Meandre Proxy is disconnected from the server");
 		}
 		QueryableRepository qp= meandreProxy.getRepository();
 		Set<Resource> resources=qp.getAvailableFlows();
 		return resources;
 	}
 
 	public QueryableRepository getRepository() throws TransmissionException{
 		if(!connected){
 			log.severe("meandre proxy is not connected to the meandre server");
 			throw new TransmissionException("Meandre Proxy is disconnected from the server");
 		}
 		QueryableRepository qp= meandreProxy.getRepository();
 		return qp;
 	}
 
 	public Map<String, FlowDescription> getAvailableFlowDescriptionsMap() throws TransmissionException{
 		if(!connected){
 			log.severe("meandre proxy is not connected to the meandre server");
 			throw new TransmissionException("Meandre Proxy is disconnected from the server");
 		}
 		QueryableRepository qp= meandreProxy.getRepository();
 		Map<String, FlowDescription> map=qp.getAvailableFlowDescriptionsMap();
 		return map;
 	}
 
 	public boolean removeResource(String resourceURL) throws TransmissionException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void uploadFlow(FlowDescription flow, boolean overwrite) throws TransmissionException{
 		this.meandreProxy.uploadFlow(flow, overwrite);
 	}
 
 	public FlowDescription retrieveFlowDescriptor(String flowURL) throws TransmissionException{
 		return this.meandreProxy.retrieveFlowDescriptor(flowURL);
 	}
 
 	public Set<URI> retrieveFlowUris() throws TransmissionException{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ExecutableComponentDescription retrieveComponentDescriptor(
 			String componentURL) throws TransmissionException{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Set<URI> retrieveComponentUris() throws TransmissionException{
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	
 
 }
