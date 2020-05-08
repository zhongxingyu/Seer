 package dk.statsbiblioteket.broadcasttranscoder.processors;
 
 import dk.statsbiblioteket.broadcasttranscoder.domscontent.*;
 import junit.framework.TestCase;
 
 /**
  * Created with IntelliJ IDEA.
  * User: csr
  * Date: 20/11/12
  * Time: 11:40
  * To change this template use File | Settings | File Templates.
  */
 public class ProgramStructureUpdaterProcessorTest extends TestCase {
 
     public void testAreSemanticallyEqualMissingStart() {
         //Empty structure are equal
         ProgramStructure s1 = new ProgramStructure();
         ProgramStructure s2 = new ProgramStructure();
         ProgramStructureUpdaterProcessor processor = new ProgramStructureUpdaterProcessor();
         assertTrue(processor.areSemanticallyEqual(s1, s2));
 
         //One with, one without missing start
         MissingStart ms1 = new MissingStart();
         MissingStart ms2 = new MissingStart();
         s1.setMissingStart(ms1);
         assertFalse(processor.areSemanticallyEqual(s1, s2));
 
         //Two different missing starts
         ms1.setMissingSeconds(30);
         ms2.setMissingSeconds(35);
         s2.setMissingStart(ms2);
         assertFalse(processor.areSemanticallyEqual(s1, s2));
 
         //Same missing start
         ms2.setMissingSeconds(30);
         assertTrue(processor.areSemanticallyEqual(s1, s2));
     }
 
     public void testAreSemanticallyEqualMissingEnd() {
         //Empty structure are equal
         ProgramStructure s1 = new ProgramStructure();
         ProgramStructure s2 = new ProgramStructure();
         ProgramStructureUpdaterProcessor processor = new ProgramStructureUpdaterProcessor();
         assertTrue(processor.areSemanticallyEqual(s1, s2));
 
         //One with, one without missing start
         MissingEnd me1 = new MissingEnd();
         MissingEnd me2 = new MissingEnd();
         s1.setMissingEnd(me1);
         assertFalse(processor.areSemanticallyEqual(s1, s2));
 
         //Two different missing starts
         me1.setMissingSeconds(30);
         me2.setMissingSeconds(35);
         s2.setMissingEnd(me2);
         assertFalse(processor.areSemanticallyEqual(s1, s2));
 
         //Same missing start
         me2.setMissingSeconds(30);
         assertTrue(processor.areSemanticallyEqual(s1, s2));
     }
 
 
     public void testAreSemanticallyEqualHoles() {
         ProgramStructure s1 = new ProgramStructure();
         ProgramStructure s2 = new ProgramStructure();
         ProgramStructureUpdaterProcessor processor = new ProgramStructureUpdaterProcessor();
 
         ProgramStructure.Holes holes1 = new ProgramStructure.Holes();
         s1.setHoles(holes1);
         assertTrue(processor.areSemanticallyEqual(s1, s2));
 
 
         Hole hole1 = new Hole();
         hole1.setFile1UUID("foobar");
         hole1.setFile2UUID("barfoo");
         hole1.setHoleLength(300);
         holes1.getHole().add(hole1);
         assertFalse(processor.areSemanticallyEqual(s1, s2));
 
         ProgramStructure.Holes holes2 = new ProgramStructure.Holes()  ;
         s2.setHoles(holes2);
 
         Hole hole2 = new Hole();
         hole2.setFile1UUID("foobar");
         hole2.setFile2UUID("barfoo");
         hole2.setHoleLength(300);
         holes2.getHole().add(hole2);
         assertTrue(processor.areSemanticallyEqual(s1, s2));
 
         hole2.setHoleLength(301);
         assertFalse(processor.areSemanticallyEqual(s1, s2));
 
         holes1.getHole().add(hole1);    //different number of holes
         assertFalse(processor.areSemanticallyEqual(s1, s2));
     }
 
     public void testAreSemanticallyEqualOverlap() {
         ProgramStructure s1 = new ProgramStructure();
         ProgramStructure s2 = new ProgramStructure();
         ProgramStructureUpdaterProcessor processor = new ProgramStructureUpdaterProcessor();
 
         ProgramStructure.Overlaps overlaps1 = new ProgramStructure.Overlaps();
         s1.setOverlaps(overlaps1);
         assertTrue(processor.areSemanticallyEqual(s1, s2));
 
         Overlap overlap1 = new Overlap();
        overlap1.setFile1UUID("foobar");
         overlap1.setFile2UUID("barfoo");
         overlap1.setOverlapLength(300);
         overlap1.setOverlapType(0);
         overlaps1.getOverlap().add(overlap1);
         assertFalse(processor.areSemanticallyEqual(s1, s2));
 
         ProgramStructure.Overlaps overlaps2 = new ProgramStructure.Overlaps();
         s2.setOverlaps(overlaps2);
         assertFalse(processor.areSemanticallyEqual(s1, s2));
 
         Overlap overlap2 = new Overlap();
        overlap2.setFile1UUID("foobar");
         overlap2.setFile2UUID("barfoo");
         overlap2.setOverlapLength(300);
         overlap2.setOverlapType(0);
         overlaps2.getOverlap().add(overlap2);
         assertTrue(processor.areSemanticallyEqual(s1, s2));
 
         overlap2.setOverlapLength(200);
         assertFalse(processor.areSemanticallyEqual(s1, s2));
 
 
     }
 
 }
