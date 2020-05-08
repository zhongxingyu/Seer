 package studieprogresjon;
 
 import junit.framework.TestCase;
 import studieprogresjon.Semester;
 
 public class SemesterTest extends TestCase {
     public void testNumberOfLecturers() {
         Semester sem = new Semester(
             new Student(new Position(0,0)), 1, 10, 10
         );
         assertEquals(2, sem.numberOfLecturers());
     }
 
     public void testRegisterLecturer() {
         Semester sem = new Semester(
             new Student(new Position(0,0)), 1, 10, 10
         );
         boolean res = sem.registerLecturer(new Lecturer(new Position(1,3)));
         assertTrue(res);
         assertEquals(1, sem.numberOfRegisteredLecturers());
     }
 
     public void testRegisterToManyLecturersFail() {
         // Should be room for 1 lecturer in this game
         Semester sem = new Semester(
             new Student(new Position(0,0)), 1, 5,5 
         );
         boolean res = sem.registerLecturer(new Lecturer(new Position(1,3)));
         assertTrue(res);
         boolean res2 = sem.registerLecturer(new Lecturer(new Position(2,3)));
         assertFalse(res2);
        assertEquals(1, sem.numberOfRegisteredLecturers());
     }
 
     public void testGetMap() {
         Semester sem = new Semester(
             new Student(new Position(0,0)), 1, 5, 5
         );
         sem.registerLecturer(new Lecturer(new Position(2,3)));
 
         String shouldBe = "-------\n" +
             "|s....|\n" +
             "|.....|\n" +
             "|.....|\n" +
             "|..*..|\n" +
             "|.....|\n" +
             "-------";
         assertEquals(shouldBe, sem.getMap());
     }
 
     public void testIsFree() {
         // Check if space is free on map
         Semester sem = new Semester(
             new Student(new Position(0,0)), 1, 5, 5
         );
         sem.registerLecturer(new Lecturer(new Position(2,3)));
         assertTrue(sem.isFree(0,1));
         assertFalse(sem.isFree(0,0));
         assertFalse(sem.isFree(2,3));
     }
 
     public void testGetFree() {
         // Check if space is free on map
         Semester sem = new Semester(
             new Student(new Position(0,0)), 1, 5, 5
         );
         sem.registerLecturer(new Lecturer(new Position(2,3)));
 
         Position pos = sem.getFree();
         boolean free = true;
         for (Lecturer l: sem.getLecturers()) {
             if (l.getPosition().equals(pos))
                 free = false;
         }
         if (pos.equals(sem.getStudent().getPosition()) || free == false) {
             fail("getFree() failed");
         }
     }
 }
