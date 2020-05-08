 package eu.dm2e.ws.tests.unit.api;
 
 
 import eu.dm2e.grafeo.Grafeo;
 import eu.dm2e.grafeo.jena.GrafeoImpl;
 import eu.dm2e.grafeo.json.GrafeoJsonSerializer;
 import eu.dm2e.grafeo.junit.GrafeoAssert;
 import eu.dm2e.logback.LogbackMarkers;
 import eu.dm2e.ws.api.*;
 import eu.dm2e.ws.services.publish.PublishService;
 import eu.dm2e.ws.services.xslt.XsltService;
 import eu.dm2e.ws.tests.OmnomUnitTest;
 import org.joda.time.DateTime;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 
 public class WorkflowPojoTest extends OmnomUnitTest {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	private WorkflowPojo wf = null;
 	final String _ws_label = "XML -> XMLRDF -> DM3E yay";
 	final String _ws_param_provider = "providerID";
 	final String _ws_param_xmlinput = "inputXML";
 	final String _ws_param_xsltinput = "inputXSLT";
 	final String _ws_param_outgraph = "outputGraph";
 	final String _ws_param_datasetLabel = "datasetLabel";
 	final String _ws_pos1_label = "XML -> XMLRDF";
 	final String _ws_pos2_label = "XMLRDF -> Graphstore";
 	final String _ws_param_datasetID = "datasetID";
 	final DateTime _ws_modified = DateTime.now();
 	
 	String _file_xml_in;
 	String _file_xslt_in;
 
 	@Before
 	public void setUp()
 			throws Exception {
 		wf = createWorkflow();
 		Assert.assertNotNull(wf);
 //		publishWorkflow();
 //		Assert.assertNotNull(wf.getId());
 //		_file_xml_in = client.publishFile(configFile.get(OmnomTestResources.METS_EXAMPLES));
 //		_file_xslt_in = client.publishFile(configFile.get(OmnomTestResources.METS2EDM));
 	}
 	
 	@Ignore
 	@Test
 	public void testParameterJson() {
 		
 //		Gson gson = new GsonBuilder().create();
 		ParameterPojo p = new ParameterPojo();
 		ParameterPojo p2 = GrafeoJsonSerializer.deserializeFromJSON(p.toJson(), ParameterPojo.class);
 		GrafeoAssert.graphsAreEquivalent(p.getGrafeo(), p2.getGrafeo());
 		
 	}
 	@Test
 	public void testPositionJson() {
 		
 //		Gson gson = new GsonBuilder().create();
 		WorkflowPositionPojo p = new WorkflowPositionPojo();
 		p.setWebservice(new WebservicePojo());
 		WorkflowPositionPojo p2 = GrafeoJsonSerializer.deserializeFromJSON(p.toJson(), WorkflowPositionPojo.class);
 		GrafeoAssert.graphsAreEquivalent(p.getGrafeo(), p2.getGrafeo());
 		log.debug(p2.toJson());
 	}
 	
 	@Ignore
 	@Test
 	public void testJson() throws IOException {
 		
 		
 		{
 //			WorkflowPojo level1 = OmnomJsonSerializer.deserializeFromJSON(wf.toJson(), WorkflowPojo.class);
 //			WorkflowPojo level2 = OmnomJsonSerializer.deserializeFromJSON(level1.toJson(), WorkflowPojo.class);
 //			WorkflowPojo level3 = OmnomJsonSerializer.deserializeFromJSON(level2.toJson(), WorkflowPojo.class);
 //			WorkflowPojo level4 = OmnomJsonSerializer.deserializeFromJSON(level3.toJson(), WorkflowPojo.class);
 //			WorkflowPojo level5 = OmnomJsonSerializer.deserializeFromJSON(level4.toJson(), WorkflowPojo.class);
 //			WorkflowPojo level6 = OmnomJsonSerializer.deserializeFromJSON(level5.toJson(), WorkflowPojo.class);
 //			log.debug("Level 0 : " + wf.getGrafeo().size());
 //			log.debug("Level 1 : " + level1.getGrafeo().size());
 //			log.debug("Level 2 : " + level2.getGrafeo().size());
 //			log.debug("Level 3 : " + level3.getGrafeo().size());
 //			log.debug("Level 4 : " + level4.getGrafeo().size());
 //			log.debug("Level 5 : " + level5.getGrafeo().size());
 //			log.debug("Level 6 : " + level6.getGrafeo().size());
 //			if (true)
 //			throw new ComparisonFailure("", level1.getCanonicalNTriples(), level3.getCanonicalNTriples());
 ////			GrafeoAssert.sizeEquals(level1, level2);
 ////			GrafeoAssert.graphsAreEquivalent(level1, level2);
 		}
 		
 		wf.setId("http://foo");
 		log.warn("Bar " + wf.getTerseTurtle());
 		log.info("FOO: " + wf.toJson());
 //		System.in.read();
 		WorkflowPojo x = GrafeoJsonSerializer.deserializeFromJSON(wf.toJson(), WorkflowPojo.class);
 		assertEquals(wf.toJson(), x.toJson());
 		if (true) return;
 		WorkflowPojo wfCopy = wf.getGrafeo().copy().getObjectMapper().getObject(WorkflowPojo.class, wf.getId());
 		GrafeoAssert.graphsAreEquivalent(wf.getGrafeo(), wfCopy.getGrafeo());
 		
 		{
 			WorkflowPojo wfParsed = GrafeoJsonSerializer.deserializeFromJSON(wfCopy.toJson(), WorkflowPojo.class);
 			
 			long expectedSize = wf.getGrafeo().size()
 					+ (wf.getPositions().size() - 1)*4
 					+ (wf.getParameterConnectors().size() -1)*5
 					+ (wf.getInputParams().size()-1)*4
 					+ (wf.getOutputParams().size()-1)*4
 					;
 			GrafeoAssert.graphsAreEquivalent(wf.getGrafeo(), wfParsed.getGrafeo());
 			GrafeoAssert.sizeEquals(wfParsed.getGrafeo(), wfParsed.getGrafeo());
 			GrafeoAssert.sizeEquals(wfParsed.getGrafeo(), expectedSize);
 		}
 		
 //		GrafeoAssert.graphsAreStructurallyEquivalent(wf, wfParsed);
 //		log.debug(wf.toJson());
 //		GrafeoAssert.sizeEquals(wf, wfParsed);
 //		throw new ComparisonFailure("", wf.getCanonicalNTriples(), wfParsed.getCanonicalNTriples());
 
 	}
 
 		/**
 	 * @return
 	 * @throws IOException 
 	 */
 	protected WorkflowPojo createWorkflow() {
 		WorkflowPojo wf = new WorkflowPojo();
 
 		wf.setLabel(_ws_label);
 		wf.addInputParameter(_ws_param_xmlinput).setIsRequired(true);
 		wf.addInputParameter(_ws_param_provider).setIsRequired(true);
 		wf.addInputParameter(_ws_param_xsltinput).setIsRequired(true);
 		wf.addInputParameter(_ws_param_datasetLabel).setIsRequired(true);
 		wf.addInputParameter(_ws_param_datasetID).setIsRequired(true);
 		
 		wf.addOutputParameter(_ws_param_outgraph);
 
 		WorkflowPositionPojo step1_pos = new WorkflowPositionPojo();
 		WebservicePojo step1_ws = new XsltService().getWebServicePojo();
 //		WebserviceConfigPojo step1_config = new WebserviceConfigPojo();
 //		step1_config.setWebservice(step1_ws);
 		step1_pos.setWebservice(step1_ws);
 		step1_pos.setLabel(_ws_pos1_label);
 		step1_pos.setWorkflow(wf);
 		wf.addPosition(step1_pos);
 
 
 		WorkflowPositionPojo step2_pos = new WorkflowPositionPojo();
 		WebservicePojo step2_ws = new PublishService().getWebServicePojo();
 //		WebserviceConfigPojo step2_config = new WebserviceConfigPojo();
 //		step2_config.setWebservice(step2_ws);
 		step2_pos.setWebservice(step2_ws);
 		step2_pos.setLabel(_ws_pos2_label);
 		step2_pos.setWorkflow(wf);
 		wf.addPosition(step2_pos);
 
 		// workflow:inputXML => xmlrdf:xmlinput
 		wf.addConnectorFromWorkflowToPosition(
 				_ws_param_xmlinput, 
 				step1_pos, 
 				XsltService.PARAM_XML_IN);
 
 		// workflow:inputXSLT => xmlrdf:xsltinput
 		wf.addConnectorFromWorkflowToPosition(
 				_ws_param_xsltinput,
 				step1_pos,
 				XsltService.PARAM_XSLT_IN);
 		
 		// workflow:datasetID => publish:dataset-id
 		ParameterConnectorPojo x = wf.addConnectorFromWorkflowToPosition(
 				_ws_param_datasetID,
 				step2_pos, 
 				PublishService.PARAM_DATASET_ID);
 		log.debug(LogbackMarkers.DATA_DUMP, x.getTerseTurtle());
 
 		// workflow:providerID => publish:providerID
 		wf.addConnectorFromWorkflowToPosition(
 				_ws_param_provider,
 				step2_pos,
 				PublishService.PARAM_PROVIDER_ID);
 
 		// workflow:label => publish:label
 		wf.addConnectorFromWorkflowToPosition(
 				_ws_param_datasetLabel,
 				step2_pos,
 				PublishService.PARAM_LABEL);
 
 		// xmlrdf:xmloutput => publish:to-publish
 		wf.addConnectorFromPositionToPosition(
 				step1_pos,
 				XsltService.PARAM_XML_OUT,
 				step2_pos,
 				PublishService.PARAM_TO_PUBLISH);
 
 		// publish:result-dataset-id => workfow:outputGraph
 		wf.addConnectorFromPositionToWorkflow(
 				step2_pos,
 				PublishService.PARAM_RESULT_DATASET_ID,
 				_ws_param_outgraph);
 		
 		return wf;
 	}
 
 //	 TODO Review serialization, there seems to be a bug (Discuss Kai, Domi, Konstatin, 2013-11-18)
 	@Test
    @Ignore("The list serialization is not working here, object mapper thinks finishedJob1 is blank.")
 	public void test() {
 		
 		JobPojo finishedJob1 = new JobPojo();
		finishedJob1.setId("http://foo");
 		
		
		final String wfid = "http://bar";
 		
 		JobPojo wfjob = new JobPojo();
 		wfjob.setId(wfid);
 		wfjob.getFinishedJobs().add(finishedJob1);
 
 		log.info(wfjob.getTerseTurtle());
 		Grafeo g = new GrafeoImpl();
 		g.getObjectMapper().addObject(wfjob);
 		
 		Grafeo g2 = g.copy();
 		assertEquals(g.size(), g2.size());
 		GrafeoAssert.graphsAreEquivalent(g, g2);
 		JobPojo wfjob2 = g2.getObjectMapper().getObject(JobPojo.class, wfid);
 		
 		log.info(g2.getTerseTurtle());
 		GrafeoAssert.graphsAreEquivalent(wfjob.getGrafeo(), wfjob2.getGrafeo());
 		
 	}
 
     @Test
     public void testValidate() {
         WorkflowPojo workflow = createWorkflow();
         workflow.autowire(true);
         ValidationReport res = workflow.validate();
         log.debug(res.toString());
         assert(res.valid());
         for (int i=0;i<100;i++) {
             ParameterConnectorPojo cand = null;
             long take = Math.round(Math.random() * workflow.getParameterConnectors().size());
             if (take>=workflow.getParameterConnectors().size()) take =  workflow.getParameterConnectors().size()-1;
             long count = 0;
             log.debug("Randomly chosen, I take: " + take);
             for (ParameterConnectorPojo conn:workflow.getParameterConnectors()) {
                 if (take==count) {
                     cand = conn;
                     break;
                 }
                 count++;
             }
             workflow.getParameterConnectors().remove(cand);
             res = workflow.validate();
             log.debug(res.toString());
             // There is only one connection that does not (yet) invalidate the workflow:
             // The connection to the outputparameter of the workflow
             assert(cand.getToParam().getLabel().equals("outputGraph") || res.containsMessage(WorkflowPositionPojo.class, 3));
             // if (res.size()==0) log.info("Not required: " + cand);
             workflow.getParameterConnectors().add(cand);
         }
 
     }
 }
