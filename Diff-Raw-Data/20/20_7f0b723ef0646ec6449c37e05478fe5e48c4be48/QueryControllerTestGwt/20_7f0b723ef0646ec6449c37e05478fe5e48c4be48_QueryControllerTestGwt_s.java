 package net.shrine.webclient.client.controllers;
 
 import net.shrine.webclient.client.AbstractWebclientTest;
 import net.shrine.webclient.client.state.State;
 import net.shrine.webclient.shared.domain.And;
 import net.shrine.webclient.shared.domain.Term;
 
 import org.junit.Test;
 
 /**
  * 
  * @author clint
  * @date Apr 20, 2012
  */
 public class QueryControllerTestGwt extends AbstractWebclientTest {
 
     private State state;
 
     private QueryController controller;
 
     private QueryBuildingController queryBuildingController;
 
     private MockQueryService queryService;
 
     @Override
     protected void gwtSetUp() throws Exception {
         super.gwtSetUp();
 
         state = state();
         queryService = new MockQueryService();
         controller = new QueryController(state, queryService);
         queryBuildingController = new QueryBuildingController(state);
     }
 
     @Override
     protected void gwtTearDown() throws Exception {
         super.gwtTearDown();
 
         state = null;
         queryService = null;
         controller = null;
         queryBuildingController = null;
     }
 
     @Test
     public void testRunEveryQuery() {
         try {
             controller.runEveryQuery();
 
             fail("Should have thrown when no queries to run");
         } catch (IllegalArgumentException expected) {}
 
         final Term t1 = term("nuh");
 
         final int t1Id = queryBuildingController.addNewTerm(t1).getId();
 
         controller.runEveryQuery();
 
         assertEquals(t1.toXmlString(), state.getAllExpression());
         assertEquals(t1.toXmlString(), state.getQuery(t1Id).getExpression().toXmlString());
 
        assertEquals(queryService.multiNodeResultsToReturn, state.getAllResult().get());
 
         final Term t2 = term("zuh");
 
         final int t2Id = queryBuildingController.addNewTerm(t2).getId();
 
         state.getAllResult().clear();
 
         controller.runEveryQuery();
 
         assertEquals(new And(t1, t2).toXmlString(), state.getAllExpression());
         assertEquals(t1.toXmlString(), state.getQuery(t1Id).getExpression().toXmlString());
         assertEquals(t2.toXmlString(), state.getQuery(t2Id).getExpression().toXmlString());
 
         assertEquals(QueryController.toCountMap(queryService.multiNodeResultsToReturn), state.getAllResult().get());
     }
 }
