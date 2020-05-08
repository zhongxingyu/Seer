 package com.operativus.senacrs.audit.graph.nodes;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 
 public class NodeTest {
 
 	@Test
 	public void testStart() {
 
 		Assert.assertNotNull(Node.START);
 		Assert.assertEquals(Node.START_TO_STRING, Node.START.toString());
 	}
 
 	@Test
 	public void testEnd() {
 
 		Assert.assertNotNull(Node.END);
 		Assert.assertEquals(Node.END_TO_STRING, Node.END.toString());
 	}
 
 	@Test
 	public void testStartEnd() {
 
 		Assert.assertNotEquals(Node.END, Node.START);
 		Assert.assertNotEquals(Node.END_TO_STRING, Node.START_TO_STRING);
 	}
 }
