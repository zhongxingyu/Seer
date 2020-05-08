 // CHECKSTYLE:OFF
 package com.nequissimus.university.k1584.logic;
 
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class PetriTransitionTest {
 
     private static final String TRANSITION_NAME = "TRANS_NAME";
     private static final String TRANSITION_ID = "TRANS_ID";
 
     private static final PetriPlace PLACE = new PetriPlace("TEST_PLACE");
     private static final PetriPlace PLACE2 = new PetriPlace("TEST_PLACE_2");
 
     private static final PetriNet NET = new PetriNet("NET");
 
     private static PetriTransition TRANSITION = null;
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
     }
 
     @Before
     public void setUp() throws Exception {
 
         PetriTransitionTest.TRANSITION =
             new PetriTransition(PetriTransitionTest.TRANSITION_NAME,
                 PetriTransitionTest.TRANSITION_ID);
 
     }
 
     @After
     public void tearDown() throws Exception {
 
         PetriTransitionTest.TRANSITION = null;
 
     }
 
     @Test
     public final void testAddInput() {
 
         PetriTransitionTest.TRANSITION.addInput(PetriTransitionTest.PLACE);
 
         Assert.assertTrue(PetriTransitionTest.TRANSITION.getInput()
             .contains(PetriTransitionTest.PLACE));
 
     }
 
     @Test
     public final void testAddOutput() {
 
         PetriTransitionTest.TRANSITION.addOutput(PetriTransitionTest.PLACE);
 
         Assert.assertTrue(PetriTransitionTest.TRANSITION.getOutput()
             .contains(PetriTransitionTest.PLACE));
 
     }
 
     @Test
     public final void testClone() {
 
         final PetriTransition clone =
             PetriTransitionTest.TRANSITION.clone();
 
         Assert.assertNotSame(PetriTransitionTest.TRANSITION, clone);
         Assert.assertNotSame(PetriTransitionTest.TRANSITION.getId(),
             clone.getId());
         Assert.assertEquals(PetriTransitionTest.TRANSITION.getName(),
             clone.getName());
         Assert.assertEquals(PetriTransitionTest.TRANSITION.getInput(),
             clone.getInput());
         Assert.assertEquals(PetriTransitionTest.TRANSITION.getOutput(),
             clone.getOutput());
         Assert.assertEquals(PetriTransitionTest.TRANSITION.getPosition(),
             clone.getPosition());
         Assert.assertEquals(PetriTransitionTest.TRANSITION.getSize(),
             clone.getSize());
 
     }
 
     @Test
     public final void testGetInput() {
 
         final Set<PetriPlace> input =
             PetriTransitionTest.TRANSITION.getInput();
 
         Assert.assertNotNull(input);
         Assert.assertTrue(input.isEmpty());
 
     }
 
     @Test
     public final void testGetOutput() {
 
         final Set<PetriPlace> output =
             PetriTransitionTest.TRANSITION.getOutput();
 
         Assert.assertNotNull(output);
         Assert.assertTrue(output.isEmpty());
 
     }
 
     @Test
     public final void testIsActive() {
 
         PetriTransitionTest.TRANSITION.addInput(PetriTransitionTest.PLACE);
 
         Assert.assertFalse(PetriTransitionTest.TRANSITION.isActive());
 
         PetriTransitionTest.PLACE.setTokens(1);
 
         Assert.assertTrue(PetriTransitionTest.TRANSITION.isActive());
 
     }
 
     @Test
     public final void testOccur() {
 
         PetriTransitionTest.TRANSITION.addInput(PetriTransitionTest.PLACE);
         PetriTransitionTest.TRANSITION
             .addOutput(PetriTransitionTest.PLACE2);
 
         PetriTransitionTest.PLACE.setTokens(2);
         PetriTransitionTest.PLACE2.setTokens(1);
 
         PetriTransitionTest.TRANSITION.occur(PetriTransitionTest.NET);
 
         Assert.assertEquals(1, PetriTransitionTest.PLACE.getTokens());
         Assert.assertEquals(2, PetriTransitionTest.PLACE2.getTokens());
 
     }
 
     @Test
     public final void testPetriTransitionStringString() {
 
         Assert.assertNotNull(PetriTransitionTest.TRANSITION);
         Assert.assertEquals(PetriTransitionTest.TRANSITION_NAME,
             PetriTransitionTest.TRANSITION.getName());
         Assert.assertEquals(PetriTransitionTest.TRANSITION_ID,
             PetriTransitionTest.TRANSITION.getId());
 
     }
 
     @Test
     public final void testRemoveInput() {
 
         PetriTransitionTest.TRANSITION.addInput(PetriTransitionTest.PLACE);
 
         Assert.assertTrue(PetriTransitionTest.TRANSITION.getInput()
             .contains(PetriTransitionTest.PLACE));
 
         PetriTransitionTest.TRANSITION
             .removeInput(PetriTransitionTest.PLACE);
 
         Assert.assertFalse(PetriTransitionTest.TRANSITION.getInput()
             .contains(PetriTransitionTest.PLACE));
 
     }
 
     @Test
     public final void testRemoveOutput() {
 
         PetriTransitionTest.TRANSITION.addOutput(PetriTransitionTest.PLACE);
 
         Assert.assertTrue(PetriTransitionTest.TRANSITION.getOutput()
             .contains(PetriTransitionTest.PLACE));
 
         PetriTransitionTest.TRANSITION
             .removeOutput(PetriTransitionTest.PLACE);
 
         Assert.assertFalse(PetriTransitionTest.TRANSITION.getOutput()
             .contains(PetriTransitionTest.PLACE));
 
     }
 
 }
