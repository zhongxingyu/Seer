 package org.imirsel.nema.flowmetadataservice;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.imirsel.nema.flowmetadataservice.impl.FlowMetadataServiceImpl;
 import org.imirsel.nema.flowmetadataservice.impl.Repository;
 import org.imirsel.nema.flowservice.MeandreServerException;
 import org.imirsel.nema.flowservice.MeandreServerProxy;
 import org.imirsel.nema.flowservice.SimpleMeandreServerProxyConfig;
 import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
 import org.imirsel.nema.model.Component;
 import org.junit.Before;
 import org.junit.Test;
 
 /**Tests for flowmetadataservice
  * 
  * @author kumaramit01
  * @since 0.5.0
  *
  */
 public class FlowMetadataServiceTest{
 	
 	private final FlowMetadataServiceImpl flowMetadataService = new FlowMetadataServiceImpl();
 	private MeandreServerProxy meandreServerProxy;
 	private Repository repository;
 
 	@Before
	public void setup() throws MeandreServerException{
		String host ="128.174.154.145";
 		String password = "admin";
 		String username ="admin";
 		int port = 11709;
 		int maxConcurrentJobs =1;
 		SimpleMeandreServerProxyConfig config = new SimpleMeandreServerProxyConfig(
 				username,password,host,port,maxConcurrentJobs);
 		meandreServerProxy = new MeandreServerProxy(config);
 		repository = new Repository();
 		repository.setMeandreServerProxy(meandreServerProxy);
		repository.init();
 		flowMetadataService.setRepository(repository);
		meandreServerProxy.init();
 		flowMetadataService.setMeandreServerProxy(meandreServerProxy);
 		
 	}
 	
 
 	
 	@Test
 	public void testGetComponents() throws MeandreServerException{
 		String flowUri="http://test.org/datatypetest/";
 		List<Component>componentList=flowMetadataService.getComponents(flowUri);
 		for(int i=0;i< componentList.size();i++){
 			System.out.println("--> "+componentList.get(i).getInstanceUri() + " : " + componentList.get(i).getUri());
 		}
 		
 	}
 	
	
 	@Test 
 	public void testcreateNewFlow(){
 		String flowURI="http://test.org/datatypetest/";
 		HashMap<String,String> paramMap = new HashMap<String,String>();
 		paramMap.put("datatypetestcomponent_0_mfcc","false");
 		String fileName=null;
 		try {
 			fileName=flowMetadataService.createNewFlow(paramMap, flowURI, 0l);
 			assertTrue(fileName!=null);
 			assertTrue(fileName.length()>0);
 		} catch (MeandreServerException e) {
 			fail(e.getMessage());
 		}
 		System.out.println("filename is: " + fileName);
 	}
 	
 
 }
