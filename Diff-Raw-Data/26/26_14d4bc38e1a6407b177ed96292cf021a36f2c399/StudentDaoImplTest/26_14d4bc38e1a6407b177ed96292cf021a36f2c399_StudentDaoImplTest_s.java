 package org.karpukhin.lab2.ex1;
 
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.handlers.ScalarHandler;
 import org.junit.*;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Pavel Karpukhin
  * @since 01.10.12
  */
 public class StudentDaoImplTest {
 
     public static final String JDBC_URL = "jdbc:derby:memory:jdbc-demo;create=true";
     public static final String SQL_INSERT_STUDENT =
             "INSERT INTO STUDENTS (FIRST_NAME, LAST_NAME, BIRTH_DATE, EMAIL) " +
             "VALUES ('Test', 'Test', CURRENT_DATE, 'test@test.ru')";
     public static final String SQL_COUNT_STUDENTS =
             "SELECT COUNT(*) FROM STUDENTS";
 
     private static Connection connection;
     private static StudentDao studentDao;
 
     private QueryRunner runner = new QueryRunner();
 
     @BeforeClass
     public static void testSetUp() throws SQLException {
         connection = DriverManager.getConnection(JDBC_URL);
         studentDao = new StudentDaoImpl(connection);
         studentDao.createTable();
     }
 
     @Before
     public void setUp() {
         try {
             connection.setAutoCommit(false);
             runner.update(connection, SQL_INSERT_STUDENT);
         } catch (SQLException e) {
             fail(e.getMessage());
         }
     }
 
     @After
     public void tearDown() {
         try {
             connection.rollback();
         } catch (SQLException e) {
             fail(e.getMessage());
         }
     }
 
    @AfterClass
    public static void testTearDown() {
        try {
            connection.close();
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

     @Test
     public void testCreateStudent() {
         Student student = new Student("First name", "Last name", new Date(), "email@host.com");
         Student result = studentDao.createStudent(student);
         assertNotNull(result);
         assertNotSame(-1, result.getId());
         assertEquals(2, countRows());
     }
 
     @Test
     public void testGetStudentById() {
         Student student = studentDao.getStudentById(3);
         assertNotNull(student);
         assertEquals(student.getFirstName(), "Test");
     }
 
     @Test
     public void testGetAllStudents() {
         List<Student> result = studentDao.getAllStudents();
         assertNotNull(result);
         assertEquals(1, result.size());
     }
 
     @Test
     public void testUpdateStudent() {
         Student student = new Student("First name", "Last name", new Date(), "email@host.com");
         student.setId(5);
         studentDao.updateStudent(student);
         studentDao.getStudentById(5);
         assertEquals(student.getFirstName(), "First name");
         assertEquals(1, countRows());
     }
 
     @Test
     public void testDeleteStudentById() {
         studentDao.deleteStudentById(6);
         List<Student> result = studentDao.getAllStudents();
         assertEquals(0, result.size());
         assertEquals(0, countRows());
     }
 
     private int countRows() {
         int res = -1;
         try {
             res = runner.query(connection, SQL_COUNT_STUDENTS, new ScalarHandler<Integer>());
         } catch (SQLException e) {
             fail(e.getMessage());
         }
         return res;
     }
 }
