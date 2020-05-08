 package de.hub.clickwatch.recorder.test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Map.Entry;
 
 import junit.framework.Assert;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 
 import com.google.inject.Module;
 
 import de.hub.clickwatch.model.Handler;
 import de.hub.clickwatch.model.Node;
 import de.hub.clickwatch.model.util.builder.NetworkBuilder;
 import de.hub.clickwatch.model.util.builder.NodeBuilder;
 import de.hub.clickwatch.recoder.cwdatabase.CWDataBaseFactory;
 import de.hub.clickwatch.recoder.cwdatabase.DataBase;
 import de.hub.clickwatch.recoder.cwdatabase.ExperimentDescr;
 import de.hub.clickwatch.recoder.cwdatabase.ExperimentRecord;
 import de.hub.clickwatch.recoder.cwdatabase.NodeRecord;
 import de.hub.clickwatch.recoder.cwdatabase.NodeRecordDescr;
 import de.hub.clickwatch.recoder.cwdatabase.util.builder.DataBaseBuilder;
 import de.hub.clickwatch.recoder.cwdatabase.util.builder.ExperimentDescrBuilder;
 import de.hub.clickwatch.recorder.CWRecorderModule;
 import de.hub.clickwatch.recorder.ExperimentRecorder;
 import de.hub.clickwatch.recorder.database.CWRecorderStandaloneSetup;
 import de.hub.clickwatch.recorder.database.DataBaseUtil;
 import de.hub.clickwatch.tests.AbstractAdapterTest;
 
 public class AbstractDBTest  extends AbstractAdapterTest {
 	
 	private DataBaseUtil dbUtil = null;	
 
 	@Override
 	protected Module[] getAdditionalModules() {
 		return new Module[] { new CWRecorderModule() };
 	}
 
 	@Override
 	protected void additionalSetUp() {
 		super.additionalSetUp();
 		CWRecorderStandaloneSetup.doSetup();
 		dbUtil = injector.getInstance(DataBaseUtil.class);
 	}
 	
 	protected void performTest(String[] nodeIds) {
 		ExperimentRecorder recorder = injector.getInstance(ExperimentRecorder.class);
 		ExperimentDescr experiment = buildDataBase(nodeIds);
 		recorder.record(experiment);	
 		assertExperiment(experiment, nodeIds);
 	}
 	
 	protected ExperimentDescr buildDataBase(String[] nodeIds) {
 		Collection<Node> nodes = new ArrayList<Node>();
 		for (String nodeId: nodeIds) {
 			nodes.add(NodeBuilder.newNodeBuilder()
 					.withINetAddress(nodeId)
 					.withPort("7777").build());
 		}
 		
 		DataBase db = DataBaseBuilder.newDataBaseBuilder()
 				.withInMemory(true)
 				.withExperiments(ExperimentDescrBuilder.newExperimentDescrBuilder()
 						.withName("test_experiment")
 						.withDescription("this is onyl for testing")
						.withDuration(getExperimentDuration())
 						.withStatistics(CWDataBaseFactory.eINSTANCE.createExperimentStatistics())
 						.withNetwork(NetworkBuilder.newNetworkBuilder()
 								.withName("test_network")
 								.withElementFilter("")
 								.withHandlerFilter("")
 								.withUpdateIntervall(0)
 								.withNodes(nodes)
 						)
 				).build();
 		
 		ResourceSet rs = new ResourceSetImpl();
 		Resource dataBaseResource = rs.createResource(URI.createURI("test_db.cwdatabase"));
 		dataBaseResource.getContents().add(db);
 		
 		return db.getExperiments().get(0);
 	}
 
	protected int getExperimentDuration() {
 		return 2000;
 	}
 	
 	private void assertExperiment(ExperimentDescr experiment, String[] nodeIds) {		
 		Assert.assertTrue(experiment.getStart() > 0);
 		Assert.assertTrue(experiment.getEnd() > experiment.getStart());
 		for (String nodeId: nodeIds) {
 			assertNode(experiment, nodeId);
 		}
 	}
 	
 	private void assertNode(ExperimentDescr experiment, String nodeId) {
 		ExperimentRecord experimentRecord = experiment.getRecord();
 		long latestEnd = 0;
 		for (Entry<Long, NodeRecordDescr> record: experimentRecord.getNodeMap().get(nodeId).getNodeMap()) {
 			NodeRecord nodeRecord = record.getValue().getRecord();
 			Assert.assertTrue(nodeRecord.getStart() >= 0);
 			Assert.assertTrue(nodeRecord.getStart() >= latestEnd);
 			Assert.assertTrue(nodeRecord.getStart() < nodeRecord.getEnd());
 			Assert.assertTrue(experiment.getStart() <= nodeRecord.getStart());
 			Assert.assertTrue(experiment.getEnd() >= nodeRecord.getEnd());
 			for (Handler handler: nodeRecord.getRecords()) {
 				Assert.assertTrue(handler.getTimestamp() > 0);
 				Assert.assertTrue(handler.getTimestamp() <= nodeRecord.getEnd());
 			}
 			latestEnd = nodeRecord.getEnd();
 		}
 		
 		long endTime = experiment.getEnd();
 		long startTime = experiment.getStart();
 		
 		assertNodeAtTime(experiment, nodeId, startTime);
 		assertNodeAtTime(experiment, nodeId, endTime);
 		assertNodeAtTime(experiment, nodeId, startTime + endTime / 2);
 	}
 	
 	private void assertNodeAtTime(ExperimentDescr experiment, String nodeId, long time) {
 		Node node = dbUtil.getNode(experiment, nodeId, time);
 		Assert.assertNotNull(node);
 		for (String handlerName: handlerNamesOfNode(node)) {
 			assertHandler(node, handlerName, time);
 		}
 	}
 	
 	protected Collection<String> handlerNamesOfNode(Node node) {
 		return Arrays.asList(new String[] {"e1_1/e_2_1/h1", "e1_1/e_2_1/h2", "e1_1/e_2_2/h1"});
 	}
 	
 	protected void assertValue(Handler handler) {
 		Assert.assertEquals("value of " + handler.getQualifiedName(), handler.getValue());
 	}
 
 	private void assertHandler(Node node, String handler, long time) {
 		Handler nodeHandler = node.getHandler(handler);
 		if (nodeHandler.getTimestamp() == 0) {
 			Assert.assertTrue(nodeHandler.getValue() == null || nodeHandler.getValue().equals(""));
 		} else {
 			Assert.assertTrue(nodeHandler.getTimestamp() <= time);
 			assertValue(nodeHandler);
 		}
 	}
 }
