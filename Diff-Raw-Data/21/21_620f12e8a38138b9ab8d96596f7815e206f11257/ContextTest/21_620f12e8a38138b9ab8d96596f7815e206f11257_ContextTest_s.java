 /*******************************************************************************
  * Copyright (c) 2004 - 2005 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 /*
  * Created on May 22, 2005
   */
 package org.eclipse.mylar.core.tests;
 
 import java.io.File;
 
 import org.eclipse.mylar.core.IMylarContext;
 import org.eclipse.mylar.core.IMylarContextEdge;
 import org.eclipse.mylar.core.IMylarContextNode;
 import org.eclipse.mylar.core.internal.ScalingFactors;
 import org.eclipse.mylar.core.internal.MylarContext;
 import org.eclipse.mylar.core.internal.MylarContextExternalizer;
 
 
 public class ContextTest extends AbstractTaskscapeTest {
 
     private MylarContext taskscape;
     private ScalingFactors scaling;
     
     @Override
     protected void setUp() throws Exception {
         scaling = new ScalingFactors();
         taskscape = new MylarContext("0", scaling);
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
     
     public void testManipulation() {
         IMylarContextNode node = taskscape.parseEvent(mockSelection("1"));
         taskscape.parseEvent(mockSelection("1"));
         taskscape.parseEvent(mockInterestContribution("1", 40));
         assertEquals(42-(scaling.getDecay().getValue()*1), node.getDegreeOfInterest().getValue());
         
         taskscape.parseEvent(mockInterestContribution("1", -20));
         assertEquals(22-(scaling.getDecay().getValue()*1), node.getDegreeOfInterest().getValue());
     }
     
     public void testEdges() {
         IMylarContextNode node = taskscape.parseEvent(mockSelection("1"));
         taskscape.parseEvent(mockNavigation("2"));
         IMylarContextEdge edge = node.getEdge("2");
         assertNotNull(edge);
         assertEquals(edge.getTarget().getElementHandle(), "2");
     }
     
     public void testDecay() {
         float decay = scaling.getDecay().getValue();
         IMylarContextNode node1 = taskscape.parseEvent(mockSelection("1"));
         
         taskscape.parseEvent(mockSelection("2"));
         for (int i = 0; i < 98; i++) taskscape.parseEvent(mockSelection("1"));
         assertEquals(99-(decay*99), node1.getDegreeOfInterest().getValue());
     }
      
    public void testLandmarks() {
         IMylarContextNode node1 = taskscape.parseEvent(mockSelection("1"));
         for (int i = 0; i < scaling.getLandmark()-2 + (scaling.getLandmark()* scaling.getDecay().getValue()); i++) {
             taskscape.parseEvent(mockSelection("1"));
         }
         assertTrue(node1.getDegreeOfInterest().isInteresting());
         assertFalse(node1.getDegreeOfInterest().isLandmark());
         taskscape.parseEvent(mockSelection("1"));
         taskscape.parseEvent(mockSelection("1"));
         assertTrue(node1.getDegreeOfInterest().isLandmark());
        assertEquals(1, taskscape.getLandmarks().size());
        assertTrue(taskscape.getLandmarks().contains(node1));
     }
    
     public void testExternalization() {
         MylarContextExternalizer externalizer = new MylarContextExternalizer();
         String path = "test-tTaskscape.xml";
         File file = new File(path);
         file.deleteOnExit();   
         
         IMylarContextNode node = taskscape.parseEvent(mockSelection("1"));
         taskscape.parseEvent(mockNavigation("2"));
         IMylarContextEdge edge = node.getEdge("2");
         assertNotNull(edge);
         assertEquals(1, node.getEdges().size());
         taskscape.parseEvent(mockInterestContribution("3", scaling.getLandmark() + scaling.getDecay().getValue()*3));
         assertTrue("interest: " + taskscape.get("3").getDegreeOfInterest().getValue(), taskscape.get("3").getDegreeOfInterest().isLandmark());
         float doi = node.getDegreeOfInterest().getValue();
         assertNotNull(taskscape.getLandmarks());
         assertEquals("2", taskscape.getActiveNode().getElementHandle()); // "3" not a user event
         
         externalizer.writeXMLTaskscapeToFile(taskscape, file);
         IMylarContext loaded = externalizer.readXMLTaskscapeFromFile(file);
         assertNotNull(loaded);
         assertEquals(3, loaded.getInteractionHistory().size());
         IMylarContextNode loadedNode = loaded.get("1");
         IMylarContextEdge edgeNode = loadedNode.getEdge("2");
         assertNotNull(edgeNode);
         assertEquals(1, loadedNode.getEdges().size());
         
         IMylarContextNode landmark = loaded.get("3");
         assertNotNull(loadedNode); 
         assertEquals(doi, loadedNode.getDegreeOfInterest().getValue());
         assertTrue(landmark.getDegreeOfInterest().isLandmark());
         assertNotNull(loaded.getLandmarks());
         
         assertEquals("2", loaded.getActiveNode().getElementHandle());
     }
     
     public void testSelections() {
         IMylarContextNode missing = taskscape.get("0");
         assertNull(missing);
         
         IMylarContextNode node = taskscape.parseEvent(mockSelection());
         assertTrue(node.getDegreeOfInterest().isInteresting());
         taskscape.parseEvent(mockSelection());
         assertTrue(node.getDegreeOfInterest().isInteresting()); 
         taskscape.parseEvent(mockSelection());
         
         float doi = node.getDegreeOfInterest().getEncodedValue();
         assertEquals(3.0f -(2*scaling.getDecay().getValue()), doi);  
     }
 }
