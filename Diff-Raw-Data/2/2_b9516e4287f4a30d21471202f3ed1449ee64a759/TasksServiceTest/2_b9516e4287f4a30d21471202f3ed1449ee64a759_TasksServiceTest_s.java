 package org.imirsel.nema.webapp.webflow;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.annotation.Resource;
 import javax.jcr.SimpleCredentials;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.imirsel.nema.contentrepository.client.ArtifactService;
 import org.imirsel.nema.contentrepository.client.ContentRepositoryServiceException;
 import org.imirsel.nema.flowservice.FlowService;
 import org.imirsel.nema.flowservice.MeandreServerException;
 import org.imirsel.nema.model.Component;
 import org.imirsel.nema.model.Flow;
 import org.imirsel.nema.model.Property;
 import org.imirsel.nema.model.ResourcePath;
 import org.imirsel.nema.model.User;
 import org.imirsel.nema.service.UserManager;
 import org.imirsel.nema.webapp.model.UploadedExecutableBundle;
 import org.imirsel.nema.webapp.service.JcrService;
 import org.imirsel.nema.webapp.service.JiniService;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.Sequence;
 import org.jmock.integration.junit4.JUnit4Mockery;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.binding.message.DefaultMessageContext;
 import org.springframework.binding.message.MessageContext;
 import org.springframework.test.annotation.DirtiesContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.webflow.test.MockExternalContext;
 import org.springframework.webflow.test.MockParameterMap;
 
 import edu.emory.mathcs.backport.java.util.Arrays;
 import edu.emory.mathcs.backport.java.util.Collections;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "/test-bean.xml",
 		"/testTasksService-bean.xml" })
 public class TasksServiceTest {
 	Mockery context = new JUnit4Mockery() ;
 	final FlowService flowService = context.mock(FlowService.class);
 
 	@Resource
 	private UserManager userManager;
 	@Resource
 	private JcrService mockJcrService;
 	@Resource
 	private JiniService mockJiniService;
 	private String uploadDirectory = "/upload";
 	private ArtifactService artifactService = context
 			.mock(ArtifactService.class);
 	private UUID uuid;
 	static private Log logger = LogFactory.getLog(TasksServiceTest.class);
 	TasksServiceImpl tasksService = new TasksServiceImpl();
 
 	@Before
 	public void setUp() throws Exception {
 		tasksService.setArtifactService(artifactService);
 		tasksService.setFlowService(flowService);
 		tasksService.setUserManager(userManager);
 		tasksService.setUploadDirectory(uploadDirectory);
 		tasksService.setJiniService(mockJiniService);
 		tasksService.setJcrService(mockJcrService);
 
 		uuid = UUID.randomUUID();
 	}
 
 	@Resource
 	Component component1;
 
 	@Resource
 	Component component2;
 
 	@Resource
 	Component component3;
 
 	@Resource
 	List< Property> properties1;
 
 	@Resource
 	List<Property> properties2;
 	@Resource
 	List<Property> properties3;
 	@Resource
 	List<Property> properties4;
 
 	
 	@Resource
 	Property propertyTrue;
 	@Resource
 	Property propertyFalse;
 	@Resource
 	Property propertyFile;
 	@Resource
 	Map<Component, List<Property>> datatypeMaps;
 
 	@Resource
 	List<Component> componentList;
 
 	@Resource
 	SimpleCredentials credentials;
 
 	@Resource
 	ResourcePath path1;
 	@Resource
 	ResourcePath path2;
 	@Resource
 	ResourcePath path3;
 	@Resource
 	ResourcePath path4;
 
 	@Resource
 	Map<Component, ResourcePath> executableMap;
 
 	@Resource
 	UploadedExecutableBundle uploadBundle; 
 	@Test
 	public final void testAddExecutable() {
 		try {
 		
 			final Sequence addExecutable=context.sequence("addExecutable");
 			context.checking(new Expectations() {
 				{
 					oneOf(artifactService).exists(credentials, path2);	will(returnValue(true));inSequence(addExecutable);
 					oneOf(artifactService).removeExecutableBundle(credentials,path2); inSequence(addExecutable);
 					oneOf(artifactService).saveExecutableBundle(credentials,uuid.toString(),uploadBundle); will(returnValue(path3));inSequence(addExecutable);
 					oneOf(artifactService).exists(credentials, path1);	will(returnValue(false)); inSequence(addExecutable);
 					oneOf(artifactService).saveExecutableBundle(credentials,uuid.toString(),uploadBundle); will(returnValue(path4));inSequence(addExecutable);
 				}
 			});
 			MessageContext messageContext=new DefaultMessageContext();
 			Map<Component, ResourcePath> map = new HashMap<Component, ResourcePath>(executableMap);
 			List<Property> data=new ArrayList<Property>(properties2);
 			tasksService.addExecutable(component2,data,uploadBundle,uuid,map,messageContext);
 			assertEquals(path3.getProtocol()+":"+ path3.getWorkspace() +"://"+path3.getPath(),findProperty(data, tasksService.EXECUTABLE_URL).getValue());
 			assertEquals(uploadBundle.getPreferredOs(),findProperty(data, tasksService.OS).getValue());
 			assertEquals(path3,map.get(component2));
 
 			tasksService.addExecutable(component3,data,uploadBundle,uuid,map,messageContext);
 			assertEquals(path4.getProtocol()+":"+ path4.getWorkspace() +"://"+path4.getPath(),findProperty(data, tasksService.EXECUTABLE_URL).getValue());
 			assertEquals(uploadBundle.getPreferredOs(), findProperty(data, tasksService.OS).getValue());
 			assertEquals(path4,map.get(component3));
 
 		} catch (ContentRepositoryServiceException e) {
 			logger.error(e, e);
 		}
 		context.assertIsSatisfied();
 	}
 
 	
 	@Test
 	public final void testClearBundles() {
 		try {
 			context.checking(new Expectations() {
 				{
 					oneOf(artifactService).exists(credentials, path2);
 					will(returnValue(true));
 					oneOf(artifactService).removeExecutableBundle(credentials,
 							path2);
 					oneOf(artifactService).exists(credentials, path1);
 					will(returnValue(false));
 				}
 			});
 			Map<Component, ResourcePath> map = new HashMap<Component, ResourcePath>(
 					executableMap);
 			tasksService.clearBundles(map);
 		} catch (ContentRepositoryServiceException e) {
 			logger.error(e, e);
 		}
 		context.assertIsSatisfied();
 	}
 
 	@Test
 	public final void testGetFlowTemplates() {
 		context.checking(new Expectations() {
 			{
 				exactly(3).of(flowService).getFlowTemplates();	will(returnValue(flowSet));
 				
 			}
 		});
 	
 		List<Flow> list=tasksService.getFlowTemplates(null);
 		Set<Flow>  set=new HashSet<Flow>(list);
 		assertEquals(flowSet, set );
 	
 		Set<Flow>  featureExtractionSet=new HashSet<Flow>();
 		featureExtractionSet.add(flow1);featureExtractionSet.add(flow2);
 		list=tasksService.getFlowTemplates("Feature Extraction");
 		set=new HashSet<Flow>(list);
 		assertEquals(featureExtractionSet,set);
 		
 		list=tasksService.getFlowTemplates("not existing type");
 		set=new HashSet<Flow>(list);
 		assertEquals(flowSet,set);
 	
 		context.assertIsSatisfied();
 	}
 
 	@Test
 	public final void testFindBundle() {
 
 		try {
 			context.checking(new Expectations() {
 				{
 					oneOf(artifactService).exists(credentials, path1);
 					will(returnValue(false));
 					oneOf(artifactService).exists(credentials, path2);
 					will(returnValue(true));
 					oneOf(artifactService).getExecutableBundle(credentials,
 							path2);
 				}
 			});
 			assertNull(tasksService.findBundle(path1, properties2));
 			UploadedExecutableBundle foundBundle = tasksService.findBundle(
 					path2, properties2);
 			assertEquals(findProperty(properties2, tasksService.OS).getValue(),
 					foundBundle.getPreferredOs());
 		} catch (ContentRepositoryServiceException e) {
 			logger.error(e, e);
 		}
 
 		context.assertIsSatisfied();
 	}
 
 	@Test
 	public final void testRemoveExecutable() {
 
 		try {
 
 			context.checking(new Expectations() {
 				{
 					oneOf(artifactService).exists(credentials, path1);
 					will(returnValue(false));
 					oneOf(artifactService).exists(credentials, path2);
 					will(returnValue(true));
 					oneOf(artifactService).removeExecutableBundle(credentials,
 							path2);
 				}
 			});
 			Map<Component, ResourcePath> map = new HashMap<Component, ResourcePath>(
 					executableMap);
 			List<Property> data3 = new ArrayList<Property>(
 					properties3);
 			List<Property> data2 = new ArrayList<Property>(
 					properties2);
 			tasksService.removeExecutable(component3, map, data3);
 			assertEquals("", findProperty(data3,tasksService.EXECUTABLE_URL).getValue());
 			assertFalse(map.containsKey(component3));
 			logger.debug(map.get(component2) == path2);
 			logger.debug(data2);
 			tasksService.removeExecutable(component2, map, data2);
 			assertEquals("", findProperty(data3,tasksService.EXECUTABLE_URL).getValue());
 			assertFalse(map.containsKey(component2));
 
 		} catch (ContentRepositoryServiceException e) {
 			logger.error(e, e);
 		}
 
 		context.assertIsSatisfied();
 	}
 
 	
 	
 	@Resource
 	Flow flow1;
 	@Resource
 	Flow flow2;
 	
 	@Resource 
 	Set<Flow> flowSet;
 
 	@DirtiesContext
 	@Test
 	public final void testLoadFlowComponents() {
 
 		
 		final Map<Component,List<Property> > fullMaps=new HashMap<Component,List<Property>>();
 		for (Component component:datatypeMaps.keySet()){
 			fullMaps.put(component, new ArrayList<Property>(datatypeMaps.get(component)));
 		}
 		context.checking(new Expectations() {
 			{
 				oneOf(flowService).getAllComponentsAndPropertyDataTypes(credentials,flow1.getUri());
 				will(returnValue(fullMaps));
 				
 			}
 		});
 		Map<Component, List<Property>> map = tasksService
 				.loadFlowComponents(flow1);
 		Map<Component, List< Property>> expected=new HashMap<Component, List<Property>>(datatypeMaps);
 		//expected.remove(component3);
 		Collections.sort(expected.get(component1));
 		Collections.sort(expected.get(component2));
 		Collections.sort(expected.get(component3));
 		
 		assertEquals(expected, map);
 		assertEquals("user:pass",findProperty(map.get(component2),tasksService.CREDENTIALS ).getValue());
 		context.assertIsSatisfied();
 	}
 
 	@DirtiesContext
 	@Test
 	public final void testReplacePropertyValue(){
 		assertEquals("field2",datatypeMaps.get(component1).get(1).getValue());
 		tasksService.replacePropertyValue(datatypeMaps, "TestField2", "200");
 		assertEquals("200",datatypeMaps.get(component1).get(1).getValue());
 		assertEquals("200",datatypeMaps.get(component2).get(1).getValue());
 		
 		//wrong field name
 		tasksService.replacePropertyValue(datatypeMaps, "testFieldThree", "300");
 		assertEquals("field3",datatypeMaps.get(component3).get(0).getValue());
 		assertEquals("field3",datatypeMaps.get(component2).get(2).getValue());
 		
 		//correct field name
 		tasksService.replacePropertyValue(datatypeMaps, "testFieldTHREE", "300");
 		assertEquals("300",datatypeMaps.get(component3).get(0).getValue());
 		assertEquals("300",datatypeMaps.get(component2).get(2).getValue());
 	}
 	
 	
 	@DirtiesContext
 	@Test
 	public final void testExtractComponentList() {
 		List<Component> list = tasksService.extractComponentList(datatypeMaps);
 		logger.debug("before sort:" + componentList.get(0).getName() + ","
 				+ componentList.get(1).getName());
 		Collections.sort(componentList);
 		logger.debug("after sort:" + componentList.get(0).getName() + ","
 				+ componentList.get(1).getName());
 		assertEquals(componentList, list);
 	}
 
 	@Resource
 	HttpServletRequest mockHttpServletRequest;
 
 	@Resource
 	MockExternalContext mockExternalContext;
 
 
 	@Test
 	public final void testBuildUploadPath() {
 		final String subStr = uploadDirectory + "/"
 				+ mockHttpServletRequest.getRemoteUser() + "/" + uuid + "/";
 
 		final ServletContext mockServletContext = context
 				.mock(ServletContext.class);
 		final String root = "/mock/home/webapp";
 		context.checking(new Expectations() {
 			{
 				oneOf(mockServletContext).getRealPath(subStr);
 				will(returnValue(root + uploadDirectory + "/"
 						+ mockHttpServletRequest.getRemoteUser() + "/" + uuid));
 			}
 		});
 		mockExternalContext.setNativeContext(mockServletContext);
 
 		final String webDirPre = "randomPreset";
 		final String physicalDirPre = "physicalPreset";
 		tasksService.setWebDir(webDirPre);
 		tasksService.setPhysicalDir(physicalDirPre);
 		assertEquals(webDirPre, tasksService.getWebDir());
 		assertEquals(physicalDirPre, tasksService.getPhysicalDir());
 		tasksService.buildUploadPath(mockExternalContext, uuid);
 		assertEquals(webDirPre, tasksService.getWebDir());
 		assertEquals(physicalDirPre, tasksService.getPhysicalDir());
 
 		tasksService.setWebDir(null);
 		tasksService.buildUploadPath(mockExternalContext, uuid);
 		assertEquals("http://mock.nema.lis.illinois.edu:1111/mock/Context"
 				+ subStr, tasksService.getWebDir());
 		assertEquals(root + uploadDirectory + "/"
 				+ mockHttpServletRequest.getRemoteUser() + "/" + uuid + File.separator,
 				tasksService.getPhysicalDir());
 		context.assertIsSatisfied();
 	}
 
 	@DirtiesContext
 	@Test
 	public final void testRemoveHiddenPropertiesForRemoteDynamicComponent() {
 		List<Property> list = new ArrayList<Property>(properties4);
 		
 		List<Property> shown = tasksService.removeHiddenPropertiesForRemoteDynamicComponent(list);
 		String[] niceKeys = { "testField1", "TestField2",
 				"testFieldTHREE","_group"};
 		
 		assertEquals(niceKeys.length, shown.size());
 		for (String key:niceKeys){
 			assertTrue("missing key:"+key,findProperty(shown, key)!=null);
 		}
 
 		
 	}
 
 	@DirtiesContext
 	@Test
 	public final void testAreFromRemoteComponent() {
 		List<Property> list1 = new ArrayList<Property>(properties1);
 		list1.add( propertyFalse);
 		assertFalse(tasksService.areFromRemoteComponent(list1));
 		assertFalse(tasksService.areFromRemoteComponent(properties1));
 		assertTrue(tasksService.areFromRemoteComponent(properties4));
 	}
 
 	@Resource
 	Map<String,String> parameters1;
 	@Resource
 	Map<String,String> parameters2;
 	
 	//TODO no multipart file upload test yet.  
 	//Not sure how to test writing a file because not to sure how to get definite directory
 	@DirtiesContext
 	@Test
 	public final void testUpdateDataMap() {
 		MockParameterMap parameterMap=new MockParameterMap();
 		for (Map.Entry<String,String> entry:parameters1.entrySet()){
 			parameterMap.put(entry.getKey(),entry.getValue());
 		}
 		List<Property> data=new ArrayList<Property>(properties1);
 		tasksService.updateProperties(parameterMap, data);
 		assertEquals(parameters1.get("testField1"), findProperty(data,"testField1").getValue());
 		assertEquals(parameters1.get("TestField2"), findProperty(data, "TestField2").getValue());
 		assertNull(findProperty(data,"testFieldTHREE"));
 		
 		parameterMap=new MockParameterMap();
 		for (Map.Entry<String,String> entry:parameters2.entrySet()){
 			parameterMap.put(entry.getKey(),entry.getValue());
 		}
 		data=new ArrayList<Property>(properties1);
 		tasksService.updateProperties(parameterMap, data);
 		assertEquals(parameters2.get("testField1"), findProperty(data,"testField1").getValue());
 		assertEquals(parameters1.get("TestField2"), findProperty(data, "TestField2").getValue());
 		assertNull(findProperty(data,"testFieldTHREE"));
 		
 		
 	}
 
 	@Resource
 	User user;
 	
 	@SuppressWarnings("unchecked")
 	@Test
 	public final void testRun() {
 		try{
 		final String name="job name";
 		final String description="job description";
 		context.checking(new Expectations() {
 			{
 				oneOf(flowService).createNewFlow(with(same(credentials)),with(aNonNull(Flow.class)), 
 						(HashMap<String,String>)with(any(HashMap.class)), with(same(flow1.getUri())), 
 						with(same(user.getId()))); will(returnValue(flow2));
 				oneOf(flowService).executeJob(with(same(credentials)),with(aNonNull(String.class)),
 						with(same(name)),with(same(description)),with(same(flow2.getId())),
 								with(same(user.getId())),with(same(user.getEmail())));
 			}
 		});
		 tasksService.run(flow1,datatypeMaps,name,description,"name");
 		}catch (MeandreServerException e) {
 			logger.error(e,e);
 		}
 	}
 
 	private Property findProperty(Collection<Property> properties, String name) {
 		if (name != null) {
 			for (Property property : properties) {
 				if (name.equals(property.getName()))
 					return property;
 			}
 		}
 		return null;
 	}
 
 }
