 package org.drools.planner.examples.ras2012.model;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class NodeTest {
 
     @Test
     public void testCompareTo() {
         final Node n0 = Node.getNode(0);
         final Node n1 = Node.getNode(1);
         final Node nMax = Node.getNode(Integer.MAX_VALUE);
 
         // any node should equal to itself
         Assert.assertTrue("Node should equal itself.", n0.compareTo(n0) == 0);
         Assert.assertTrue("Node should equal itself.", n1.compareTo(n1) == 0);
         Assert.assertTrue("Node should equal itself.", nMax.compareTo(nMax) == 0);
 
         // lesser ID => lesser node
         Assert.assertTrue("Node with lesser ID should be less than a node with greater ID.",
                 n0.compareTo(n1) < 0);
         Assert.assertTrue("Node with lesser ID should be less than a node with greater ID.",
                 n1.compareTo(nMax) < 0);
 
         // and vice versa
         Assert.assertTrue("Node with greater ID should be greater than a node with lesser ID.",
                 nMax.compareTo(n1) > 0);
         Assert.assertTrue("Node with greater ID should be greater than a node with lesser ID.",
                 n1.compareTo(n0) > 0);
     }
 
     @Test
     public void testEqualsObject() {
         final Node n1 = Node.getNode(0);
         Assert.assertEquals("Node should equal itself.", n1, n1);
         final Node n2 = Node.getNode(0);
         Assert.assertEquals("Node should equal other nodes with the same ID.", n1, n2);
         final Node n3 = Node.getNode(1);
         Assert.assertFalse("Node shouldn't equal nodes with different IDs.", n1.equals(n3));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testNode() {
         Node.getNode(-1);
     }
 
 }
