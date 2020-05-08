 package edu.osu.picola.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import edu.osu.picola.dataobjects.Assignment;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * This class is responsible for handling SQL statements for Assignment data.
  *
  * @author racke.3
  */
 public class AssignmentDAO extends DAO {
 
     /**
      * @param assignment_id of the assignment you want
      * @return an assignment object constructed from the assignment_id
      */
     public static Assignment getAssignment(int assignment_id) {
 
         String query = "SELECT * FROM assignment "
                 + "WHERE assignment_id='" + assignment_id
                 + "'";
         return loadAssignments(queryDB(query)).get(0);
     }
 
     /**
      * @param user_id of the user you want (including non-students)
      * @return list of all assignments for a user
      */
     public static List<Assignment> getAssignmentsByUser(int user_id) {
         String query = "SELECT * FROM assignment "
                 + "WHERE user_id='" + user_id + "'";
         ResultSet rs = queryDB(query);
         return loadAssignments(rs);
     }
 
     /**
      * @param course_id of the course you want
      * @return list of all assignments for a single course
      */
     public static List<Assignment> getAssignmentsByCourse(int course_id) {
         String query = "SELECT * FROM assignment a INNER JOIN course c on a.course_id=c.course_id "
                 + "WHERE c.course_id='" + course_id + "'";
         ResultSet rs = queryDB(query);
         return loadAssignments(rs);
     }
 
     /**
      * Helper to reduce code rewrite
      *
      * @param rs assignments to load
      * @return list of assignments
      */
     private static List<Assignment> loadAssignments(ResultSet rs) {
         List<Assignment> assignments = new ArrayList<Assignment>();
         try {
             while (rs.next()) {
                 assignments.add(new Assignment(rs));
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             e.printStackTrace();
         }
         return assignments;
     }
 
     public static void updateAssignment(Assignment as) {
         String update = "UPDATE assignment SET assignment_descr='" + as.getAssignment_descr()
                 + "', individual_start_date='" + as.getIndividual_start_date()
                 + "', individual_end_date='" + as.getIndividual_end_date()
                 + "', BP_start_date='" + as.getBP_start_date()
                 + "', BP_end_date='" + as.getBP_end_date()
                 + "', MP_start_date='" + as.getMP_start_date()
                 + "', MP_end_date='" + as.getMP_end_date()
                 + "', assignment_number='" + as.getAssignment_number()
                 + "', assignment_name='"+ as.getAssignment_name() +"'"
                 + " WHERE assignment_id ='" + as.getAssignment_id() + "'";
         System.out.println("[jakers] = " + update);
         updateDB(update);
     }
 
     public static void insertAssignment(Assignment as) {
 
         int assignment_number = 0;
         String query = "SELECT MAX(assignment_number) max FROM assignment WHERE course_id='"
                 + as.getCourse_id() + "'";
         ResultSet rs = queryDB(query);
 
         try {
             rs.next();
             assignment_number = rs.getInt("max") + 1;
         } catch (SQLException ex) {
             Logger.getLogger(AssignmentDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         as.setAssignment_number(assignment_number);
 
         String insert = "INSERT INTO assignment (assignment_descr,individual_start_date, "
                 + "individual_end_date,BP_start_date, BP_end_date, MP_start_date, "
                + "MP_end_date,user_id, course_id, assignment_number,assignment_name)"
                 + " VALUES('"
                 + as.getAssignment_descr()
                 + "', '"
                 + as.getIndividual_start_date()
                 + "', '"
                 + as.getIndividual_end_date()
                 + "', '" + as.getBP_start_date()
                 + "', '" + as.getBP_end_date()
                 + "', '" + as.getMP_start_date()
                 + "', '" + as.getMP_end_date()
                 + "', '" + as.getUser_id()
                 + "', '" + as.getCourse_id()
                 + "', '" + as.getAssignment_number()
                + "', '" + as.getAssignment_name()
                 + "')";
         insertDB(insert);
     }
 
     public static Assignment getAssignmentByDateAndIds(int user_id, int course_id, int assignment_number) {
         String query = "SELECT * FROM assignment WHERE user_id='"+user_id
                 + "' AND course_id='"+ course_id + "' AND assignment_number='"+assignment_number+"'";
         return new Assignment(queryDB(query));
     }
 
     //for assignment_viewed table
     public static void assignToStudent(int student_id, int assignment_id) {
         String insert = " INSERT INTO assignment_viewed VALUES('"
                 + student_id + "', '"
                 + assignment_id + "', null, false)";
         insertDB(insert);
     }
 
     public static void assignToGroup(int group_id, int assignment_id) {
         String insert = "INSERT INTO assign_to VALUES('"
                 + assignment_id + "', '"
                 + group_id + "')";
         insertDB(insert);
     }
     
     public static Assignment getAssignmentByQuestion(int question_id) {
             
             String query = "SELECT * FROM assignment WHERE assignment_id "
                     + "=(Select assignment_id FROM assignment_questions WHERE question_id = '"
                     + question_id + "');";
             
             try {
             ResultSet rs = queryDB(query);
             rs.next();
             System.out.print("Two things: " + question_id + " and ");
             return (new Assignment(rs));
             } catch (SQLException e) {
                 
               return (null);  
                 
             }
         }
 }
