 package org.springframework.integration.activiti.gateway;
 
 import org.activiti.engine.test.Deployment;
 import org.apache.commons.lang.time.StopWatch;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.integration.activiti.test.AbstractSpringIntegrationActivitiTestCase;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class AbstractGatewayTest extends AbstractSpringIntegrationActivitiTestCase{
   private Log log = LogFactory.getLog(getClass());
 
 
    // @Deployment(resources = "processes/si_gateway_example.bpmn20.xml")
     public void doGatewayTesting() throws Throwable {
         // setup
         processEngine.getRepositoryService().createDeployment().addClasspathResource("processes/si_gateway_example.bpmn20.xml").deploy();
 
         // launch a process
         Map<String, Object> vars = new HashMap<String, Object>();
         vars.put("customerId", 232);
 
         log.debug("about to start the business process");
         StopWatch sw = new StopWatch();
         sw.start();
         processEngine.getRuntimeService().startProcessInstanceByKey("sigatewayProcess", vars);
 
         sw.stop();
         log.debug("total time to run the process:" + sw.getTime());
 
           Thread.sleep(1000 * 5);
     }
 }
