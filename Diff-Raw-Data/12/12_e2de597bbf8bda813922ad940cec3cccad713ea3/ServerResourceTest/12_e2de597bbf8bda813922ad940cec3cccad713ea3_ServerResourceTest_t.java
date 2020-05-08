 package com.kdcloud.server.rest.resource;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.restlet.Application;
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.representation.Representation;
 
 import weka.core.DenseInstance;
 import weka.core.Instances;
 
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.kdcloud.lib.domain.DataSpecification;
 import com.kdcloud.lib.domain.Modality;
 import com.kdcloud.lib.domain.ModalityIndex;
 import com.kdcloud.lib.domain.ServerParameter;
 import com.kdcloud.lib.rest.ext.InstancesRepresentation;
 import com.kdcloud.server.entity.Group;
 
 public class ServerResourceTest {
 
 	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
 			new LocalDatastoreServiceTestConfig()
 	/* .setDefaultHighRepJobPolicyUnappliedJobPercentage(100) */);
 
 	public static final String USER_ID = TestContext.USER_ID;
 	
 	private Context context = new TestContext();
 
 	private Application application = new Application(context);
 	
 	private Group group;
 	
 	@Before
 	public void setUp() throws Exception {
 		helper.setUp();
 		GroupServerResource resource = new GroupServerResource(application, "test");
 		resource.create();
 		group = resource.groupDao.findByName("test");
 		assertNotNull(group);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		helper.tearDown();
 	}
 
 	@Test
 	public void testDataset() {
 		DatasetServerResource datasetResource = new DatasetServerResource(application, group);
 		double[] cells = { 1, 2 };
 		Instances data = new Instances(DataSpecification.newInstances("test", 2));
 		data.add(new DenseInstance(0, cells));
 		datasetResource.uploadData(new InstancesRepresentation(data));
 		
 		Representation out = datasetResource.getData();
 		try {
 			assertEquals(1, new InstancesRepresentation(out).getInstances().size());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		datasetResource.deleteData();
 		group = datasetResource.groupDao.findByName("test");
 		Assert.assertEquals(0, group.getData().size());
 	}
 
 	@Test
 	public void testModalities() {
 		ModalitiesServerResource modalitiesResource = new ModalitiesServerResource(application);
 		ModalityIndex index = modalitiesResource.listModalities();
 		Assert.assertFalse(index.asList().isEmpty());
 //
 //		ModEntity modality = list.get(0);
 //		modality.setName("test");
 //		ChoosenModalityServerResource choosenModalityResource = new ChoosenModalityServerResource(application, modality);
 //		choosenModalityResource.editModality(modality);
 //		modality = choosenModalityResource.modalityDao.findById(modality.getId());
 //		assertEquals("test", modality.getName());
 //		
 //		choosenModalityResource.deleteModality();
 //		modality = choosenModalityResource.modalityDao.findById(modality.getId());
 //		assertNull(modality);
 	}
 
 	@Test
 	public void testWorkflow() {
 		Instances instances = DataSpecification.newInstances("test", 1);
 		DatasetServerResource resource = new DatasetServerResource(application, group);
 		resource.uploadData(new InstancesRepresentation(instances));
 		WorkflowServerResource workflowResource = new WorkflowServerResource(application, "ecg.xml");
 		Form form = new Form();
 		form.add(ServerParameter.USER_ID.getName(), USER_ID);
 		form.add(ServerParameter.GROUP_ID.getName(), "test");
 		Representation r = workflowResource.execute(form);
//		assertNotNull(r);
 	}
 
 //	@Test
 //	public void testGlobalData() {
 //		DatasetServerResource resource = new DatasetServerResource(application, group);
 //		Instances instances = DataSpecification.newInstances("test", 1);
 //		String[] ids = { "a", "b", "c" };
 //		for (String s : ids) {
 //			User user = new User();
 //			user.setName(s);
 //			resource.user = user;
 //			resource.uploadData(new InstancesRepresentation(instances));
 //		}
 //
 //		GlobalAnalysisServerResource globalAnalysisResource = new GlobalAnalysisServerResource(application, "ecg.xml");
 //		Form form = new Form();
 //		form.add(ServerParameter.GROUP_ID.getName(), "test");
 //		globalAnalysisResource.execute(form);
 //	}
 
 	@Test
 	public void testDevices() {
 		DeviceServerResource deviceResource = new DeviceServerResource(application);
 		deviceResource.register("test");
 		deviceResource.unregister("test");
 	}
 	
 	@Test
 	public void testVFS() {
 		FileServerResource resource = new FileServerResource(application);
 		resource.saveObjectToVirtualDirectory("test", "test", new Modality());
 		Object obj = resource.getObjectFromVirtualDirectory("test", "test");
 		assertNotNull(obj);
 	}
 
 //	@Test
 //	public void testScheduling() {
 //		userDataResource.createDataset();
 //		userDataResource.getPersistenceContext().close();
 //		scheduler.requestProcess();
 //		scheduler.getPersistenceContext().close();
 //		worker.pullTask(null);
 //	}
 
 }
