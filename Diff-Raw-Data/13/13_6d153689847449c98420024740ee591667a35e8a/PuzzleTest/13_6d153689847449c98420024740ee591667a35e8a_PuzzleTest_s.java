 package sudokuplus;
 
 import junit.framework.TestCase;
 import java.util.ArrayList;
 
 public class PuzzleTest extends TestCase {
     public void testCreate() throws Exception {
         // *NOTE: This initialization pattern seems particularly ugly.
         // Chalk one up to java newbness.
         ArrayList<String[]> invalidRow = new ArrayList<String[]>();
         invalidRow.add("1,2,3,4".split(","));
         invalidRow.add("3,4,1,2,5".split(","));
         invalidRow.add("2,3,4,1".split(","));
         invalidRow.add("4,1,2,3".split(","));
 
         try {
             Puzzle p = new Puzzle(invalidRow);
             fail("Invalid row should have thrown a PuzzleException");
         } catch (PuzzleException e) {}
 
         ArrayList<String[]> invalidCellValue = new ArrayList<String[]>();
         invalidCellValue.add("1,2,3,4".split(","));
         invalidCellValue.add("3,0,1,2".split(","));
         invalidCellValue.add("2,3,4,1".split(","));
         invalidCellValue.add("4,1,2,3".split(","));
 
         try {
             Puzzle p = new Puzzle(invalidCellValue);
             fail("Invalid cell value should have thrown a PuzzleException");
         } catch (PuzzleException e) {}
 
 
         ArrayList<String[]> invalidDimension = new ArrayList<String[]>();
         invalidDimension.add("1,2,3,4,5".split(","));
         invalidDimension.add("3,1,1,2,5".split(","));
         invalidDimension.add("2,3,4,1,5".split(","));
         invalidDimension.add("4,1,2,3,5".split(","));
         invalidDimension.add("4,1,2,3,5".split(","));
 
         try {
             Puzzle p = new Puzzle(invalidDimension);
             fail("Invalid dimension should have thrown a PuzzleException");
         } catch (PuzzleException e) {}
 
     }
 
     public void testIsComplete() throws Exception {
         ArrayList<String[]> valid4x4 = new ArrayList<String[]>();
         valid4x4.add("1,2,3,4".split(","));
         valid4x4.add("3,4,1,2".split(","));
         valid4x4.add("2,3,4,1".split(","));
         valid4x4.add("4,1,2,3".split(","));
 
         Puzzle p = new Puzzle(valid4x4);
         assertTrue(p.isComplete());
 
         ArrayList<String[]> incorrect4x4 = new ArrayList<String[]>();
        valid4x4.add("1,2,3,4".split(","));
        valid4x4.add("3,4,1,2".split(","));
        valid4x4.add("2,3,3,1".split(","));
        valid4x4.add("4,1,2,4".split(","));
 
        Puzzle p2 = new Puzzle(valid4x4);
        assertFalse(p.isComplete());
     }
 }
