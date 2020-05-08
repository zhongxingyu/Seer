 package com.operativus.senacrs.audit.graph.edges.webdriver;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.operativus.senacrs.audit.graph.edges.IllegalSourceNodeException;
import com.operativus.senacrs.audit.graph.nodes.Node;
 
 
 public class OpenDashboardTest {
 	
 	private OpenDashboard edge = null;
 
 	@Before
 	public void setUp() throws Exception {
 
 		edge = OpenDashboard.getInstance();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 		edge = null;
 	}
 
 	@Test
 	public void testTraverseNonStartSource() {
		
		String msg = null;
		boolean condition = false;
 
 		try {
 			this.edge.traverse(null);
 			Assert.fail();
 		} catch (IllegalSourceNodeException e) {
			msg = e.getLocalizedMessage();
			condition = msg.contains(Node.START.toString());
			Assert.assertTrue(condition);			
 		}
 	}
 }
