 // CHECKSTYLE:OFF
 package com.nequissimus.university.k1584.logic;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.io.File;
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class PetriNetTest {
 
     private static final String NAME = "PETRI_NET";
     private static final String NAME2 = "PETRI_NET2";
 
     private static final String PLACE_NAME = "PLACE_NAME";
     private static final String PLACE_NAME2 = "PLACE_NAME2";
     private static final String PLACE_ID =
         "505bc44b-ce7c-455d-88ef-38f39b9be473";
     private static final String PLACE_ID2 =
         "fe861b7e-bc30-489f-a606-a495d23750a2";
     private static final Point PLACE_POS = new Point(123, 456);
     private static final Point PLACE_POS2 = new Point(234, 567);
 
     private static final Dimension PLACE_SIZE = new Dimension(210, 185);
     private static final Dimension PLACE_SIZE2 = new Dimension(450, 350);
 
     private static final String TRANSITION_NAME = "TRANSITION_NAME";
     private static final String TRANSITION_ID =
         "3f8f9904-4416-4744-be32-077bc122281b";
 
     private static final PetriConfig CONFIG = new PetriConfig(new File(""));
 
     private static PetriNet NET = null;
     private static PetriPlace PLACE1 = null;
     private static PetriPlace PLACE2 = null;
     private static PetriPlace PLACE3 = null;
     private static PetriTransition TRANS1 = null;
     private static PetriTransition TRANS2 = null;
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
 
         PetriNetTest.NET = null;
 
     }
 
     @Before
     public void setUp() throws Exception {
 
         PetriNetTest.NET = new PetriNet(PetriNetTest.NAME);
         PetriNetTest.NET.setConfig(PetriNetTest.CONFIG);
 
         PetriNetTest.PLACE1 = PetriNetTest.NET.addPlace();
         PetriNetTest.PLACE2 = PetriNetTest.NET.addPlace();
         PetriNetTest.PLACE3 =
             PetriNetTest.NET.addPlace(PetriNetTest.PLACE_NAME,
                 PetriNetTest.PLACE_ID2);
         PetriNetTest.TRANS1 = PetriNetTest.NET.addTransition();
         PetriNetTest.TRANS2 =
             PetriNetTest.NET.addTransition(PetriNetTest.TRANSITION_NAME,
                 PetriNetTest.TRANSITION_ID);
 
         PetriNetTest.NET.connect(PetriNetTest.PLACE1, PetriNetTest.TRANS1);
         PetriNetTest.NET.connect(PetriNetTest.TRANS1, PetriNetTest.PLACE2);
 
         PetriNetTest.PLACE1.increaseTokens();
 
         PetriNetTest.PLACE3.setPosition(PetriNetTest.PLACE_POS);
 
         PetriNetTest.PLACE3.setSize(PetriNetTest.PLACE_SIZE);
 
     }
 
     @After
     public void tearDown() throws Exception {
 
         PetriNetTest.NET = null;
 
     }
 
     @Test
     public final void testAddPlace() {
 
         final PetriPlace place = PetriNetTest.NET.addPlace();
 
         Assert.assertTrue(PetriNetTest.NET.getPlaces().contains(place));
         Assert.assertEquals(PetriNetTest.CONFIG.getPlaceName(),
             place.getName());
 
     }
 
     @Test
     public final void testAddPlaceString() {
 
         final PetriPlace place =
             PetriNetTest.NET.addPlace(PetriNetTest.PLACE_NAME);
 
         Assert.assertTrue(PetriNetTest.NET.getPlaces().contains(place));
         Assert.assertEquals(PetriNetTest.PLACE_NAME, place.getName());
 
     }
 
     @Test
     public final void testAddPlaceStringString() {
 
         final PetriPlace place =
             PetriNetTest.NET.addPlace(PetriNetTest.PLACE_NAME,
                 PetriNetTest.PLACE_ID);
 
         Assert.assertTrue(PetriNetTest.NET.getPlaces().contains(place));
         Assert.assertEquals(PetriNetTest.PLACE_NAME, place.getName());
         Assert.assertEquals(PetriNetTest.PLACE_ID, place.getId());
 
     }
 
     @Test
     public final void testAddTransition() {
 
         final PetriTransition trans = PetriNetTest.NET.addTransition();
 
         Assert
             .assertTrue(PetriNetTest.NET.getTransitions().contains(trans));
         Assert.assertEquals(PetriNetTest.CONFIG.getTransitionName(),
             trans.getName());
 
     }
 
     @Test
     public final void testAddTransitionString() {
 
         final PetriTransition trans =
             PetriNetTest.NET.addTransition(PetriNetTest.TRANSITION_NAME);
 
         Assert
             .assertTrue(PetriNetTest.NET.getTransitions().contains(trans));
         Assert.assertEquals(PetriNetTest.TRANSITION_NAME, trans.getName());
 
     }
 
     @Test
     public final void testAddTransitionStringString() {
 
         final PetriTransition trans =
             PetriNetTest.NET.addTransition(PetriNetTest.TRANSITION_NAME,
                 PetriNetTest.TRANSITION_ID);
 
         Assert
             .assertTrue(PetriNetTest.NET.getTransitions().contains(trans));
         Assert.assertEquals(PetriNetTest.TRANSITION_NAME, trans.getName());
         Assert.assertEquals(PetriNetTest.TRANSITION_ID, trans.getId());
 
     }
 
     @Test
     public final void testClone() {
 
         final PetriNet clone = PetriNetTest.NET.clone();
 
         Assert.assertEquals(PetriNetTest.NET.getName(), clone.getName());
 
         final Set<PetriPlace> places = PetriNetTest.NET.getPlaces();
         final Set<PetriTransition> transitions =
             PetriNetTest.NET.getTransitions();
         final Set<PetriPlace> clonePlaces = clone.getPlaces();
         final Set<PetriTransition> cloneTransitions =
             clone.getTransitions();
 
         Assert.assertEquals(places.size(), clonePlaces.size());
 
         for (final PetriPlace clonePlace : clonePlaces) {
 
             Assert.assertFalse(places.contains(clonePlace));
 
         }
 
         Assert.assertEquals(transitions.size(), cloneTransitions.size());
 
         for (final PetriTransition cloneTransition : cloneTransitions) {
 
             Assert.assertFalse(transitions.contains(cloneTransition));
 
             final Set<PetriPlace> inputs = cloneTransition.getInput();
 
             for (final PetriPlace petriPlace : inputs) {
 
                 Assert.assertNotSame(PetriNetTest.TRANS1, cloneTransition);
                 Assert.assertEquals(PetriNetTest.TRANS1.getInput().size(),
                     inputs.size());
                 Assert.assertNotSame(PetriNetTest.TRANS1.getInput()
                     .toArray()[0], petriPlace);
 
             }
 
             final Set<PetriPlace> outputs = cloneTransition.getOutput();
 
             for (final PetriPlace petriPlace : outputs) {
 
                 Assert.assertEquals(PetriNetTest.TRANS1.getOutput().size(),
                     outputs.size());
                 Assert.assertNotSame(PetriNetTest.TRANS1.getOutput()
                     .toArray()[0], petriPlace);
 
             }
 
         }
 
     }
 
     @Test
     public final void testConnectPetriPlacePetriTransition() {
 
         Set<PetriPlace> places = PetriNetTest.TRANS2.getInput();
 
         Assert.assertFalse(places.contains(PetriNetTest.PLACE3));
 
         PetriNetTest.NET.connect(PetriNetTest.PLACE3, PetriNetTest.TRANS2);
 
         places = PetriNetTest.TRANS2.getInput();
 
         Assert.assertTrue(places.contains(PetriNetTest.PLACE3));
 
     }
 
     @Test
     public final void testConnectPetriTransitionPetriPlace() {
 
         Set<PetriPlace> places = PetriNetTest.TRANS2.getOutput();
 
         Assert.assertFalse(places.contains(PetriNetTest.PLACE3));
 
         PetriNetTest.NET.connect(PetriNetTest.TRANS2, PetriNetTest.PLACE3);
 
         places = PetriNetTest.TRANS2.getOutput();
 
         Assert.assertTrue(places.contains(PetriNetTest.PLACE3));
 
     }
 
     @Test
     public final void testDecreaseMarkings() {
 
         PetriNetTest.PLACE3.setTokens(4);
 
         PetriNetTest.NET.decreaseTokens(PetriNetTest.PLACE3);
 
         Assert.assertEquals(3, PetriNetTest.PLACE3.getTokens());
 
     }
 
     @Test
     public final void testGetId() {
 
         Assert.assertEquals(PetriNetTest.PLACE_ID2,
             PetriNetTest.NET.getId(PetriNetTest.PLACE3));
 
     }
 
     @Test
     public final void testGetMarkings() {
 
         final PetriPlace place =
             new PetriPlace(PetriNetTest.PLACE_NAME, PetriNetTest.PLACE_ID2);
 
         place.setTokens(10);
 
         Assert.assertEquals(10, PetriNetTest.NET.getTokens(place));
 
     }
 
     @Test
     public final void testGetName() {
 
         Assert.assertEquals(PetriNetTest.NAME, PetriNetTest.NET.getName());
 
     }
 
     @Test
     public final void testGetNamePetriObject() {
 
         Assert.assertEquals(PetriNetTest.PLACE_NAME,
             PetriNetTest.NET.getName(PetriNetTest.PLACE3));
 
     }
 
     @Test
     public final void testGetPlaceById() {
 
         final PetriPlace place =
             PetriNetTest.NET.getPlaceById(PetriNetTest.PLACE_ID2);
 
         Assert.assertEquals(PetriNetTest.PLACE3, place);
 
     }
 
     @Test
     public final void testGetPosition() {
 
         Assert.assertEquals(PetriNetTest.PLACE_POS,
             PetriNetTest.NET.getPosition(PetriNetTest.PLACE3));
 
     }
 
     @Test
     public final void testGetSize() {
 
         Assert.assertEquals(PetriNetTest.PLACE_SIZE,
             PetriNetTest.NET.getSize(PetriNetTest.PLACE3));
 
     }
 
     @Test
     public final void testGetTransitionById() {
 
         Assert.assertEquals(PetriNetTest.TRANS2,
             PetriNetTest.NET.getTransitionById(PetriNetTest.TRANSITION_ID));
 
     }
 
     @Test
     public final void testIncreaseMarkings() {
 
         PetriNetTest.PLACE3.setTokens(4);
 
         PetriNetTest.NET.increaseTokens(PetriNetTest.PLACE3);
 
         Assert.assertEquals(5, PetriNetTest.PLACE3.getTokens());
 
     }
 
     @Test
     public final void testIsActive() {
 
         PetriNetTest.PLACE1.setTokens(0);
 
         Assert.assertFalse(PetriNetTest.NET.isActive(PetriNetTest.TRANS1));
 
         PetriNetTest.PLACE1.setTokens(2);
 
         Assert.assertTrue(PetriNetTest.NET.isActive(PetriNetTest.TRANS1));
 
     }
 
     @Test
     public final void testOccur() {
 
         PetriNetTest.PLACE1.setTokens(0);
         PetriNetTest.PLACE2.setTokens(1);
 
         PetriNetTest.NET.occur(PetriNetTest.TRANS1);
 
         Assert.assertEquals(1, PetriNetTest.PLACE2.getTokens());
 
         PetriNetTest.PLACE1.setTokens(1);
 
         PetriNetTest.NET.occur(PetriNetTest.TRANS1);
 
         Assert.assertEquals(2, PetriNetTest.PLACE2.getTokens());
 
     }
 
     @Test
     public final void testRemoveInput() {
 
         PetriNetTest.TRANS1.removeInput(PetriNetTest.PLACE1);
 
         Assert.assertFalse(PetriNetTest.TRANS1.getInput().contains(
             PetriNetTest.PLACE1));
 
     }
 
     @Test
     public final void testRemoveOutput() {
 
         PetriNetTest.TRANS1.removeOutput(PetriNetTest.PLACE2);
 
         Assert.assertFalse(PetriNetTest.TRANS1.getOutput().contains(
             PetriNetTest.PLACE2));
 
     }
 
     @Test
     public final void testRemovePetriObject() {
 
         Assert.assertTrue(PetriNetTest.NET.getPlaces().contains(
             PetriNetTest.PLACE1));
         Assert.assertTrue(PetriNetTest.NET.getTransitions().contains(
             PetriNetTest.TRANS1));
 
         final PetriObject o1 = PetriNetTest.PLACE1;
         final PetriObject o2 = PetriNetTest.TRANS1;
 
         PetriNetTest.NET.remove(o1);
         PetriNetTest.NET.remove(o2);
 
         Assert.assertFalse(PetriNetTest.NET.getPlaces().contains(
             PetriNetTest.PLACE1));
         Assert.assertFalse(PetriNetTest.NET.getTransitions().contains(
             PetriNetTest.TRANS1));
 
     }
 
     @Test
     public final void testRemovePetriPlace() {
 
         Assert.assertTrue(PetriNetTest.NET.getPlaces().contains(
             PetriNetTest.PLACE1));
 
         PetriNetTest.NET.remove(PetriNetTest.PLACE1);
 
         Assert.assertFalse(PetriNetTest.NET.getPlaces().contains(
             PetriNetTest.PLACE1));
 
     }
 
     @Test
     public final void testRemovePetriTransition() {
 
         Assert.assertTrue(PetriNetTest.NET.getTransitions().contains(
             PetriNetTest.TRANS1));
 
         PetriNetTest.NET.remove(PetriNetTest.TRANS1);
 
         Assert.assertFalse(PetriNetTest.NET.getTransitions().contains(
             PetriNetTest.TRANS1));
 
     }
 
     @Test
     public final void testRename() {
 
         Assert.assertNotSame(PetriNetTest.PLACE_NAME2,
             PetriNetTest.PLACE3.getName());
 
         PetriNetTest.NET.rename(PetriNetTest.PLACE3,
             PetriNetTest.PLACE_NAME2);
 
         Assert.assertEquals(PetriNetTest.PLACE_NAME2,
             PetriNetTest.PLACE3.getName());
 
     }
 
     @Test
     public final void testSetName() {
 
         Assert
             .assertNotSame(PetriNetTest.NAME2, PetriNetTest.NET.getName());
 
         PetriNetTest.NET.setName(PetriNetTest.NAME2);
 
         Assert.assertEquals(PetriNetTest.NAME2, PetriNetTest.NET.getName());
 
     }
 
     @Test
     public final void testSetPosition() {
 
         Assert.assertNotSame(PetriNetTest.PLACE_POS2,
             PetriNetTest.PLACE3.getPosition());
 
         PetriNetTest.NET.setPosition(PetriNetTest.PLACE3,
             PetriNetTest.PLACE_POS2);
 
         Assert.assertEquals(PetriNetTest.PLACE_POS2,
             PetriNetTest.PLACE3.getPosition());
 
     }
 
     @Test
     public final void testSetSizeDimension() {
 
         Assert.assertNotSame(PetriNetTest.PLACE_SIZE2, PetriNetTest.PLACE3);
 
         PetriNetTest.NET.setSize(PetriNetTest.PLACE_SIZE2);
 
         Assert.assertEquals(PetriNetTest.PLACE_SIZE2,
             PetriNetTest.PLACE1.getSize());
         Assert.assertEquals(PetriNetTest.PLACE_SIZE2,
             PetriNetTest.PLACE2.getSize());
         Assert.assertEquals(PetriNetTest.PLACE_SIZE2,
             PetriNetTest.PLACE3.getSize());
         Assert.assertEquals(PetriNetTest.PLACE_SIZE2,
             PetriNetTest.TRANS1.getSize());
         Assert.assertEquals(PetriNetTest.PLACE_SIZE2,
             PetriNetTest.TRANS2.getSize());
 
     }
 
     @Test
     public final void testSetSizePetriObjectDimension() {
 
         Assert.assertNotSame(PetriNetTest.PLACE_SIZE2, PetriNetTest.PLACE3);
 
         PetriNetTest.NET.setSize(PetriNetTest.PLACE3,
             PetriNetTest.PLACE_SIZE2);
 
         Assert.assertEquals(PetriNetTest.PLACE_SIZE2,
             PetriNetTest.PLACE3.getSize());
 
     }
 
 }
