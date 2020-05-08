 package uk.co.acuminous.julez.event.repository;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import test.JdbcTestUtils;
 import uk.co.acuminous.julez.scenario.ScenarioEvent;
 import uk.co.acuminous.julez.scenario.ScenarioEventFactory;
 
 public class JdbcScenarioEventRepositoryTest {
 
     private JdbcScenarioEventRepository repository;
     private ScenarioEventFactory scenarioEventFactory;
 
     @Before
     public void init() throws Exception {
         repository = new JdbcScenarioEventRepository(JdbcTestUtils.getDataSource());
         repository.ddl();
         
         scenarioEventFactory = new ScenarioEventFactory("foo");        
     }
 
     @After
     public void nuke() throws Exception {
         JdbcTestUtils.nukeDatabase();
     }
 
     @Test
     public void eventIsAddedToRepository() {
         repository.add(scenarioEventFactory.fail());
         assertEquals(1, repository.count());
     }
 
     @Test
     public void countsAllEventsInRepository() {
         repository.add(scenarioEventFactory.fail());
         repository.add(scenarioEventFactory.fail());
         repository.add(scenarioEventFactory.fail());
         assertEquals(3, repository.count());
     }
 
     @Test
     public void retrievesAnEventFromTheRepository() {
 
         long timestamp = System.currentTimeMillis();
 
         ScenarioEvent event = new ScenarioEvent("id", timestamp, ScenarioEvent.FAIL, "correlation");
         event.getData().put("message", "page not found");
         event.getData().put("statusCode", "404");
 
         repository.add(event);
 
         ScenarioEvent dbEvent = repository.get("id");
 
         assertEquals("id", dbEvent.getId());
         assertEquals(timestamp, dbEvent.getTimestamp());
         assertEquals(ScenarioEvent.FAIL, dbEvent.getType());
         assertEquals("correlation", dbEvent.getCorrelationId());
         
         assertEquals(2, dbEvent.getData().size());
         assertEquals("page not found", dbEvent.getData().get("message"));
        assertEquals(404, dbEvent.getData().get("statusCode"));
     }    
 }
