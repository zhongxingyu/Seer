 
 package edu.osu.picola.beans;
 
 import edu.osu.picola.dao.AssignmentDAO;
 import edu.osu.picola.dao.GroupDAO;
 import edu.osu.picola.dao.QuestionDAO;
 import edu.osu.picola.dao.UserDAO;
 import edu.osu.picola.dataobjects.Assignment;
 import edu.osu.picola.dataobjects.Group;
 import edu.osu.picola.dataobjects.Question;
 import edu.osu.picola.dataobjects.User;
 import java.sql.Date;
 import java.sql.Timestamp;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.event.ActionEvent;
 
 /**
  *
  * @author akers.79
  */
 @ManagedBean(name="assignmentBean")
 @SessionScoped
 public class AssignmentBean {
 
     private String assignment_descr;
     private Date indivdual_start_date;
     private Date indivdual_end_date;
     private Date BP_start_date;
     private Date BP_end_date;
     private Date MP_start_date;
     private Date MP_end_date;
     private int user_id;
     private int course_id;
     private int assignment_number;
     private Assignment assignment;
     private int assignment_id;
 
     public String getAssignment_name() {
         return assignment_name;
     }
 
     public void setAssignment_name(String assignment_name) {
         this.assignment_name = assignment_name;
     }
     private String assignment_name;
     
     public List<Question> getBPQuestions(){
         return QuestionDAO.getBPQuestionByAssignment(assignment.getAssignment_id());
     }
     
     public List<Question> getMPQuestions(){
         return QuestionDAO.getMPQuestionByAssignment(assignment.getAssignment_id());
     }
     
     public Assignment getAssignment(){
         return assignment;
     }
     public List<Group> getGroups(){
         return GroupDAO.getAllGroupsAssignedAssignment(assignment.getAssignment_id());
     }
     public void viewAssignment(ActionEvent event){
         //String ehruser = (String) event.getComponent().getAttributes().get("ehrid");
         //System.out.println("ehrusermap="+ehruser);
         
         assignment_id= (Integer)event.getComponent().getAttributes().get("aid");
         setAssignmentId(assignment_id);
   }
     public void setAssignmentId(int assignmentId) {
         this.assignment_id = assignmentId;
         assignment = AssignmentDAO.getAssignment(assignment_id);
     }
     
     public int getAssignmentId(){
         return assignment_id;
     }
     
      public void setAssignment_descr(String description) {
         assignment_descr = description;
     }
      
     public String getAssignment_descr() {
         return assignment_descr;
     }
 
     public Date getIndivdual_start_date() {
         return indivdual_start_date;
     }
 
     public void setIndivdual_start_date(Date indivdual_start_date) {
         
         System.out.println("[jakers]" + indivdual_start_date.getTime());
         
         this.indivdual_start_date = new Date(indivdual_start_date.getTime());
     }
 
     public Date getIndivdual_end_date() {
         return indivdual_end_date;
     }
 
     public void setIndivdual_end_date(String indivdual_end_date) {
         this.indivdual_end_date = new Date(Date.valueOf(indivdual_end_date).getTime());
     }
 
     public Date getBP_start_date() {
         return BP_start_date;
     }
 
     public void setBP_start_date(String BP_start_date) {
         this.BP_start_date = new Date(Date.valueOf(BP_start_date).getTime());
     }
 
     public Date getBP_end_date() {
         return BP_end_date;
     }
 
     public void setBP_end_date(String BP_end_date) {
         this.BP_end_date = new Date(Date.valueOf(BP_end_date).getTime());
     }
 
     public Date getMP_start_date() {
         return MP_start_date;
     }
 
     public void setMP_start_date(String MP_start_date) {
         this.MP_start_date = new Date(Date.valueOf(MP_start_date).getTime());
     }
 
     public Date getMP_end_date() {
         return MP_end_date;
     }
 
     public void setMP_end_date(String MP_end_date) {
         this.MP_end_date = new Date(Date.valueOf(MP_end_date).getTime());
     }
 
     public int getUser_id() {
         return user_id;
     }
 
     public void setUser_id(int user_id) {
         this.user_id = user_id;
     }
 
     public int getCourse_id() {
         return course_id;
     }
 
     public void setCourse_id(int course_id) {
         this.course_id = course_id;
     }
 
     public int getAssignment_number() {
         return assignment_number;
     }
 
     public void setAssignment_number(int assignment_number) {
         this.assignment_number = assignment_number;
     }
 
     public void updateAssignment() {
         /* convert string to timestamp */
         Timestamp is = new Timestamp (indivdual_start_date.getTime());
         Timestamp ie = new Timestamp (indivdual_end_date.getTime());
         Timestamp bs = new Timestamp (BP_start_date.getTime());
         Timestamp be = new Timestamp (BP_end_date.getTime());
         Timestamp ms = new Timestamp (MP_start_date.getTime());
         Timestamp me = new Timestamp (MP_end_date.getTime());
         Assignment as = new Assignment(assignment_descr, is, ie, bs,
                 be, ms, me, user_id, course_id, assignment_number, assignment_name);
         AssignmentDAO.updateAssignment(as);
     }
     
     public void createAssignment(int course_id, int user_id) {
         /* convert string to timestamp */
         Timestamp is = new Timestamp (indivdual_start_date.getTime());
         Timestamp ie = new Timestamp (indivdual_end_date.getTime());
         Timestamp bs = new Timestamp (BP_start_date.getTime());
         Timestamp be = new Timestamp (BP_end_date.getTime());
         Timestamp ms = new Timestamp (MP_start_date.getTime());
         Timestamp me = new Timestamp (MP_end_date.getTime());
 
         /* insert assignment */
         Assignment assignment = new Assignment(assignment_descr, is, ie, bs,
                 be, ms, me, user_id, course_id, assignment_number,assignment_name);
         AssignmentDAO.insertAssignment(assignment);
        assignment = AssignmentDAO.getAssignmentByDateAndIds(is, user_id,
                course_id);
 
         /* assign assignment to each student in the course */
         List<User> roster = UserDAO.getCourseRoster(course_id);
         for (int i = 0; i < roster.size(); i++) {
             AssignmentDAO.assignToStudent(roster.get(i).getUser_id(),
                     assignment.getAssignment_id());
         }
     }
 }
