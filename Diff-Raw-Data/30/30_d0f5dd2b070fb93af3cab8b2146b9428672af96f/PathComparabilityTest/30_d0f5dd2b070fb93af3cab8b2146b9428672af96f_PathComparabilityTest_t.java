 package de.cosmicsand.webtools.path;
 
 import junitx.extensions.ComparabilityTestCase;
 
 public class PathComparabilityTest extends ComparabilityTestCase {
 
     public PathComparabilityTest(String name) {
         super(name);
     }
 
     @Override
     protected Comparable<?> createEqualInstance() throws Exception {
         return new Path("/b");
     }
 
     @Override
     protected Comparable<?> createGreaterInstance() throws Exception {
         return new Path("/c");
     }
 
     @Override
     protected Comparable<?> createLessInstance() throws Exception {
         return new Path("/a");
     }
 
    public void testEmptyPathsToBeEqual() {
        assertEquals(new Path(), new Path());
    }

    public void testEmptyPathToBeLowerComparedToANonEmptyPath() {
        assertTrue(new Path().compareTo(new Path("/")) < 0);
    }

    public void testNonEmptyPathToBeHigherComparedToAnEmptyPath() {
        assertTrue(new Path("/").compareTo(new Path()) > 0);
    }

 }
