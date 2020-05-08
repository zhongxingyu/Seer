 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.drools.mas;
 
 import java.sql.SQLException;
 import java.util.LinkedHashMap;
 import org.drools.mas.body.content.Action;
 import org.drools.mas.core.DroolsAgent;
import org.drools.mas.mock.MockResponseInformer;
 import org.drools.mas.util.ACLMessageFactory;
 import org.drools.mas.util.MessageContentFactory;
 import org.h2.tools.DeleteDbFiles;
 import org.h2.tools.Server;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  *
  * @author salaboy
  */
 public class KnowledgeResourcesCompilationTest {
     private static Logger logger = LoggerFactory.getLogger(KnowledgeResourcesCompilationTest.class);
     private Server server;
     public KnowledgeResourcesCompilationTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
          DeleteDbFiles.execute("~", "mydb", false);
 
         logger.info("Staring DB for white pages ...");
         try {
             server = Server.createTcpServer(null).start();
         } catch (SQLException ex) {
             logger.error(ex.getMessage());
         }
         logger.info("DB for white pages started! ");
     }
 
     @After
     public void tearDown() {
         
         logger.info("Stopping DB ...");
         server.stop();
         logger.info("DB Stopped!");
     }
     /*
      * Test for check that the resources provided inside this agent 
      * at least compile without errors. To ensure that the agent can be 
      * initialized correctly
      */
     @Test
     public void compilationTest() {
         
         ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
         DroolsAgent agent = (DroolsAgent) context.getBean("agent");
         
         assertNotNull(agent);
         
         agent.dispose();
         
     }
     
      @Test
     public void testSimpleRequestToDeliverMessage() {
         
         ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
         DroolsAgent agent = (DroolsAgent) context.getBean("agent");
         assertNotNull(agent);
         
         LinkedHashMap<String, Object> args = new LinkedHashMap<String, Object>();
         args.put("refId", "284d7e8d-6853-46cb-bef2-3c71e565f90d");
         args.put("conversationId", "502ed27e-682d-43b3-ac2a-8bba3b597d13");
         args.put("subjectAbout", new String[] { "patient1", "docx", "id1", "id2", "id3" } );
         args.put("sender", "docx");
         args.put("mainRecipients", new String[] {"id1"} );
         args.put("secondaryRecipients", new String[] {"id2"});
         args.put("hiddenRecipients", new String[] {"id3"});
         args.put("type", "ALERT");
         args.put("header", "Risk threshold exceeded : MockPTSD (30%)");
         args.put("body", "<h2>MockPTSD</h2><br/>(This is free form HTML with an optionally embedded survey)<br/>Dear @{recipient.displayName},<br/>"
                 + "<p>Your patient @{patient.displayName} has a high risk of developing the disease known as MockPTSD. <br/>"
                 + "     The estimated rate is around 30.000000000000004%."
                 + "</p>"
                 + "<p>"
                 + "They will contact you shortly."
                 + "</p>"
                 + "MockPTSD:<p class='agentEmbed' type='survey' id='01f0bb3d-7f67-450a-ad8d-be29c5611055' /p>"
                 + "<br/>Thank you very much, <br/><p>Your Friendly Clinical Decision Support Agent, on behalf of @{provider.displayName}</p>");
         args.put("priority", "Critical");
         args.put("deliveryDate", "Tue Oct 11 23:46:36 CEST 2011");
         args.put("status", "New");
 
 
         
         ACLMessageFactory factory = new ACLMessageFactory(Encodings.XML);
 
         Action action = MessageContentFactory.newActionContent("deliverMessage", args);
         ACLMessage req = factory.newRequestMessage("", "", action);
 
         
         agent.tell(req);
 
         Object result = agent.getAgentAnswers(req.getId());
         assertNotNull(result);
 
         System.out.println("Results = "+result);
 
         assertEquals(true, result.toString().contains("refId"));
         assertEquals(true, result.toString().contains("convoId"));
         agent.dispose();
     }
 }
