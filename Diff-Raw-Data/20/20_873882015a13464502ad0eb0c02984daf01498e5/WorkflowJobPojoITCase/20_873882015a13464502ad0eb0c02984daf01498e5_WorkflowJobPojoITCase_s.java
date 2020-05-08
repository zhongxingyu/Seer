 package eu.dm2e.ws.api;
 
 
 
 import org.junit.Test;
 
 import eu.dm2e.ws.OmnomTestCase;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 import eu.dm2e.ws.grafeo.junit.GrafeoAssert;
 
 public class WorkflowJobPojoITCase extends OmnomTestCase {
 	
 	@Test
 	public void test() {
 		
 		JobPojo finishedJob1 = new JobPojo();
 		finishedJob1.setId("http://foo");
 		
 		
 		final String wfid = "http://bar";
 		
 		WorkflowJobPojo wfjob = new WorkflowJobPojo();
 		wfjob.setId(wfid);
		wfjob.getFinishedJobs().add(finishedJob1);
 		
 		log.info(wfjob.getTerseTurtle());
 		Grafeo g = new GrafeoImpl();
 		g.getObjectMapper().addObject(wfjob);
 		
 		Grafeo g2 = g.copy();
 		WorkflowJobPojo wfjob2 = g2.getObjectMapper().getObject(WorkflowJobPojo.class, wfid);
 		
 		log.info(g2.getTerseTurtle());
 		GrafeoAssert.graphsAreEquivalent(wfjob, wfjob2);
 		
 	}
 
 }
