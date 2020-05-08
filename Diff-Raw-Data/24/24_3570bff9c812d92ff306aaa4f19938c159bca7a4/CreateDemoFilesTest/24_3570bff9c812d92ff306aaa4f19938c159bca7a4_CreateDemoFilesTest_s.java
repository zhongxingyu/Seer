 // CHECKSTYLE:OFF
 package com.nequissimus.university.k1584.logic;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.io.File;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.nequissimus.library.data.Singleton;
 import com.nequissimus.university.k1584.logic.pnml.PetriMarkup;
 import com.nequissimus.university.k1584.logic.pnml.PnmlException;
 
 public class CreateDemoFilesTest {
 
     private static File DEMO1 = new File("./examples/demo1.pnml");
     private static File DEMO2 = new File("./examples/demo2.pnml");
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
 
         final PetriConfig config =
             (PetriConfig) new PetriConfig().getDefaults();
 
         Singleton.addObject(config);
 
         if (CreateDemoFilesTest.DEMO1.exists()) {
             CreateDemoFilesTest.DEMO1.delete();
         }
 
         if (CreateDemoFilesTest.DEMO2.exists()) {
             CreateDemoFilesTest.DEMO2.delete();
         }
 
         CreateDemoFilesTest.DEMO1.createNewFile();
         CreateDemoFilesTest.DEMO2.createNewFile();
 
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
 
         CreateDemoFilesTest.DEMO1 = null;
         CreateDemoFilesTest.DEMO2 = null;
 
     }
 
     @Test
     public final void createDemo1() {
 
         final PetriSnapshots snapshots = new PetriSnapshots();
         final PetriNet before = snapshots.add("Before");
 
         final Dimension iconSize = new Dimension(138, 100);
 
         final PetriPlace place1 = before.addPlace();
         place1.setTokens(1);
         place1.setPosition(new Point(120, 375));
         place1.setSize(iconSize);
 
         final PetriPlace place2 = before.addPlace();
         place2.setTokens(0);
         place2.setPosition(new Point(320, 375));
         place2.setSize(iconSize);
 
         final PetriPlace place3 = before.addPlace();
         place3.setTokens(0);
         place3.setPosition(new Point(515, 375));
         place3.setSize(iconSize);
 
         final PetriPlace place4 = before.addPlace();
         place4.setTokens(2);
         place4.setPosition(new Point(410, 10));
         place4.setSize(iconSize);
 
         final PetriPlace place5 = before.addPlace();
         place5.setTokens(1);
         place5.setPosition(new Point(230, 10));
         place5.setSize(iconSize);
 
         final PetriTransition transition = before.addTransition();
         transition.setPosition(new Point(320, 175));
         transition.setSize(iconSize);
 
         before.connect(transition, place1);
         before.connect(transition, place2);
         before.connect(transition, place3);
         before.connect(place4, transition);
         before.connect(place5, transition);
 
         final PetriNet after = before.clone();
 
         after.setName("After");
 
         final PetriTransition trans =
             after.getTransitions().iterator().next();
         after.occur(trans);
 
         snapshots.add(after);
 
         try {
             PetriMarkup.savePnmlFile(CreateDemoFilesTest.DEMO1, snapshots);
         } catch (final PnmlException e) {
             Assert.fail();
         }
 
     }
 
     @Test
     public final void createDemo2() {
 
         final PetriSnapshots snapshots = new PetriSnapshots();
         final PetriNet empty = snapshots.add("Empty");
 
         final Dimension iconSize = new Dimension(83, 45);
 
         final PetriPlace reqRefill = empty.addPlace("request for refill");
         reqRefill.setSize(iconSize);
         reqRefill.setPosition(new Point(160, 240));
 
         final PetriPlace holdCoin = empty.addPlace("holding coin");
         holdCoin.setSize(iconSize);
         holdCoin.setPosition(new Point(725, 130));
 
         final PetriPlace readyDisp = empty.addPlace("ready to dispense");
         readyDisp.setSize(iconSize);
         readyDisp.setPosition(new Point(335, 225));
 
         final PetriPlace readyIns = empty.addPlace("ready for insertion");
         readyIns.setSize(iconSize);
         readyIns.setPosition(new Point(330, 30));
         readyIns.setTokens(1);
 
         final PetriPlace candy = empty.addPlace("candy storage");
         candy.setSize(iconSize);
         candy.setPosition(new Point(180, 20));
         candy.setTokens(4);
 
         final PetriTransition insCoin = empty.addTransition("insert coin");
         insCoin.setSize(iconSize);
         insCoin.setPosition(new Point(600, 35));
 
         final PetriTransition accCoin = empty.addTransition("accept coin");
         accCoin.setSize(iconSize);
         accCoin.setPosition(new Point(600, 245));
 
         final PetriTransition rejCoin = empty.addTransition("reject coin");
         rejCoin.setSize(iconSize);
         rejCoin.setPosition(new Point(465, 140));
 
         final PetriTransition refill = empty.addTransition("refill");
         refill.setSize(iconSize);
         refill.setPosition(new Point(85, 125));
 
         final PetriTransition dispense =
             empty.addTransition("dispense candy");
         dispense.setSize(iconSize);
         dispense.setPosition(new Point(245, 135));
 
         empty.connect(reqRefill, refill);
         empty.connect(refill, candy);
         empty.connect(candy, dispense);
         empty.connect(dispense, reqRefill);
         empty.connect(dispense, readyIns);
         empty.connect(readyIns, insCoin);
         empty.connect(insCoin, holdCoin);
         empty.connect(holdCoin, rejCoin);
         empty.connect(rejCoin, readyIns);
         empty.connect(holdCoin, accCoin);
         empty.connect(accCoin, readyDisp);
         empty.connect(readyDisp, dispense);
 
         final PetriNet fig12 = empty.clone();
         fig12.setName("Fig1.2");
 
         snapshots.add(fig12);
 
         empty.occur(insCoin);
         empty.occur(accCoin);
         empty.occur(dispense);
 
         final PetriNet fig13 = empty.clone();
         fig13.setName("Fig1.3");
 
         snapshots.add(fig13);
 
         reqRefill.setTokens(0);
         candy.setTokens(0);
         readyIns.setTokens(0);
 
         try {
             PetriMarkup.savePnmlFile(CreateDemoFilesTest.DEMO2, snapshots);
         } catch (final PnmlException e) {
             Assert.fail();
         }
 
     }
 
     @Before
     public void setUp() throws Exception {
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
 }
