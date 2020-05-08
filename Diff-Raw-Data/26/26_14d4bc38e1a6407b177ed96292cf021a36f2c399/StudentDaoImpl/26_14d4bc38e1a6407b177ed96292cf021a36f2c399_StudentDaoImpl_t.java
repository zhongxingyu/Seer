 package org.karpukhin.lab2.ex1;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Pavel Karpukhin
  * @since 01.10.12
  */
 public class StudentDaoImpl implements StudentDao {
 
     public static final String STUDENT_ID_COLUMN = "STUDENT_ID";
     public static final String FIRST_NAME_COLUMN = "FIRST_NAME";
     public static final String LAST_NAME_COLUMN = "LAST_NAME";
     public static final String BIRTH_DATE_COLUMN = "BIRTH_DATE";
     public static final String EMAIL_COLUMN = "EMAIL";
 
     private static final String CREATE_STUDENT_SQL =
             "INSERT INTO STUDENTS (FIRST_NAME, LAST_NAME, BIRTH_DATE, EMAIL) " +
             "VALUES (?, ?, ?, ?)";
 
     private static final String GET_STUDENT_BY_ID_SQL =
             "SELECT STUDENT_ID, FIRST_NAME, LAST_NAME, BIRTH_DATE, EMAIL " +
             "FROM STUDENTS WHERE STUDENT_ID = ?";
 
     private static final String GET_ALL_STUDENTS_SQL =
             "SELECT STUDENT_ID, FIRST_NAME, LAST_NAME, BIRTH_DATE, EMAIL " +
             "FROM STUDENTS ORDER BY LAST_NAME ASC, FIRST_NAME ASC";
 
     private static final String UPDATE_STUDENT_SQL =
             "UPDATE STUDENTS SET FIRST_NAME = ?, LAST_NAME = ?, BIRTH_DATE = ?, EMAIL = ? " +
             "WHERE STUDENT_ID = ?";
 
     private static final String DELETE_STUDENT_BY_ID_SQL =
             "DELETE FROM STUDENTS WHERE STUDENT_ID = ?";
 
     private static final String CREATE_STUDENTS_TABLE_SQL =
             "CREATE TABLE STUDENTS (" +
             "STUDENT_ID INT NOT NULL GENERATED ALWAYS AS IDENTITY, " +
             "FIRST_NAME VARCHAR(20), " +
             "LAST_NAME VARCHAR(30), " +
             "BIRTH_DATE DATE, " +
             "EMAIL VARCHAR(30)" +
             ")";
 
     private Connection connection;
 
     public StudentDaoImpl(Connection connection) {
         this.connection = connection;
     }
 
     @Override
     public Student createStudent(Student student) {
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         try {
             statement = connection.prepareStatement(CREATE_STUDENT_SQL, Statement.RETURN_GENERATED_KEYS);
             statement.setString(1, student.getFirstName());
             statement.setString(2, student.getLastName());
             statement.setDate(3, new Date(student.getBirthDate().getTime()));
             statement.setString(4, student.getEmail());
             int rows = statement.executeUpdate();
             if (rows != 1) {
                 throw new ApplicationException(String.format("%d rows were inserted but 1 was expected only", rows));
             }
             resultSet = statement.getGeneratedKeys();
             if (resultSet.next()) {
                 student.setId(resultSet.getInt(1));
             }
         } catch (SQLException e) {
             throw new ApplicationException(e.getMessage(), e);
         } finally {
             close(statement, resultSet);
         }
         return student;
     }
 
     @Override
     public Student getStudentById(int id) {
         Student student = null;
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         try {
             statement = connection.prepareStatement(GET_STUDENT_BY_ID_SQL);
             statement.setInt(1, id);
             resultSet = statement.executeQuery();
             if (resultSet.next()) {
                 student = map(resultSet);
                 if (resultSet.next()) {
                     throw new ApplicationException("More than one row was selected but 1 was expected only");
                 }
             }
         } catch (SQLException e) {
             throw new ApplicationException(e.getMessage(), e);
         } finally {
             close(statement, resultSet);
         }
         return student;
     }
 
     @Override
     public List<Student> getAllStudents() {
         List<Student> result = new ArrayList<Student>();
         Statement statement = null;
         ResultSet resultSet = null;
         try {
             statement = connection.createStatement();
             resultSet = statement.executeQuery(GET_ALL_STUDENTS_SQL);
            while (resultSet.next()) {
                 result.add(map(resultSet));
             }
         } catch (SQLException e) {
             throw new ApplicationException(e.getMessage(), e);
         } finally {
             close(statement, resultSet);
         }
         return result;
     }
 
     @Override
     public void updateStudent(Student student) {
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         try {
             statement = connection.prepareStatement(UPDATE_STUDENT_SQL);
             statement.setString(1, student.getFirstName());
             statement.setString(2, student.getLastName());
             statement.setDate(3, new Date(student.getBirthDate().getTime()));
             statement.setString(4, student.getEmail());
             statement.setInt(5, student.getId());
             int rows = statement.executeUpdate();
             if (rows != 1) {
                 throw new ApplicationException(String.format("%d rows were updated but 1 was expected only", rows));
             }
         } catch (SQLException e) {
             throw new ApplicationException(e.getMessage(), e);
         } finally {
             close(statement, resultSet);
         }
     }
 
     @Override
     public void deleteStudentById(int id) {
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         try {
             statement = connection.prepareStatement(DELETE_STUDENT_BY_ID_SQL);
             statement.setInt(1, id);
             int rows = statement.executeUpdate();
             if (rows != 1) {
                 throw new ApplicationException(String.format("%d rows were deleted but 1 was expected only", rows));
             }
         } catch (SQLException e) {
             throw new ApplicationException(e.getMessage(), e);
         } finally {
             close(statement, resultSet);
         }
     }
 
     @Override
     public void createTable() {
         Statement statement = null;
         try {
             statement = connection.createStatement();
             statement.execute(CREATE_STUDENTS_TABLE_SQL);
         } catch (SQLException e) {
             throw new ApplicationException("STUDENTS table was not created", e);
         } finally {
             close(statement, null);
         }
     }
 
     private Student map(ResultSet resultSet) throws SQLException {
         Student student = new Student();
         student.setId(resultSet.getInt(STUDENT_ID_COLUMN));
         student.setFirstName(resultSet.getString(FIRST_NAME_COLUMN));
         student.setLastName(resultSet.getString(LAST_NAME_COLUMN));
         student.setBirthDate(resultSet.getDate(BIRTH_DATE_COLUMN));
         student.setEmail(resultSet.getString(EMAIL_COLUMN));
         return student;
     }
 
     private void close(Statement statement, ResultSet resultSet) {
         try {
             if (statement != null) {
                 statement.close();
             }
             if (resultSet != null) {
                 resultSet.close();
             }
         } catch (SQLException e) {
             throw new ApplicationException(e.getMessage(), e);
         }
     }
 }
