 package studieprogresjon;
 
 import junit.framework.TestCase;
 import studieprogresjon.EntityMapper;
 import studieprogresjon.Student;
 import studieprogresjon.Lecturer;
 
 public class EntityMapperTest extends TestCase {
     public void testRegisterEntities() {
         EntityMapper map = new EntityMapper(3);
         // Register student
         map.registerEntity(new Student(new Position(1,1)));
         // Register lecturers
         map.registerEntity(new Lecturer(new Position(1,2)));
         boolean res = map.registerEntity(new Lecturer(new Position(2,2)));
         boolean res2 = map.registerEntity(new Lecturer(new Position(2,2)));
         assertTrue(res);
         assertFalse(res2);
     }
 
     public void testGetSymbols() {
         EntityMapper map = new EntityMapper(3);
         // Register student
         map.registerEntity(new Student(new Position(1,1)));
         // Register lecturers
         map.registerEntity(new Lecturer(new Position(1,2)));
 
         assertEquals('s', map.getSymbol(1,1));
         assertEquals('*', map.getSymbol(1,2));
 
         // Register a collision between lecturers
         map.registerEntity(new Lecturer(new Position(1,2)));
 
         assertEquals('X', map.getSymbol(1,2));
 
         // Test empty spot
         assertEquals('.', map.getSymbol(3,2));
     }
 
     public void testGetLecturers() {
         EntityMapper map = new EntityMapper(3);
 
         map.registerEntity(new Student(new Position(1,1)));
         map.registerEntity(new Lecturer(new Position(1,2)));
         map.registerEntity(new Lecturer(new Position(1,2)));
 
         Lecturer[] lecturers = map.getLecturers();
         assertEquals(2, lecturers.length);
     }
 
     public void testGetStudent() {
         EntityMapper map = new EntityMapper(3);
 
         map.registerEntity(new Student(new Position(1,1)));
         map.registerEntity(new Lecturer(new Position(1,2)));
         map.registerEntity(new Lecturer(new Position(1,2)));
 
         Student stud = map.getStudent();
         assertTrue(stud instanceof studieprogresjon.Student);
     }
 
     public void testGetActiveLecturers() {
         EntityMapper map = new EntityMapper(3);
 
         map.registerEntity(new Lecturer(new Position(1,2)));
         map.registerEntity(new Lecturer(new Position(2,2)));
 
         assertEquals(2, map.getLecturers(true).length);
 
         map.registerEntity(new Lecturer(new Position(2,2)));
         assertEquals(1, map.getLecturers(true).length);
     }
 }
