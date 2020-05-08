 package edu.osu.picola.studentbeans;
 
 import edu.osu.picola.studentbeans.StudentAssignmentMenuBean;
 import edu.osu.picola.beans.LoginBean;
 import edu.osu.picola.dao.AssignmentDAO;
 import edu.osu.picola.dao.GroupDAO;
 import edu.osu.picola.dao.MCResponseDAO;
 import edu.osu.picola.dao.PostDAO;
 import edu.osu.picola.dao.QuestionDAO;
 import edu.osu.picola.dataobjects.Assignment;
 import edu.osu.picola.dataobjects.Group;
 import edu.osu.picola.dataobjects.MCResponse;
 import edu.osu.picola.dataobjects.Post;
 import edu.osu.picola.dataobjects.Question;
 import java.io.Serializable;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ActionListener;
 import javax.faces.event.AjaxBehaviorEvent;
 import org.primefaces.component.commandlink.CommandLink;
 import org.primefaces.event.TabChangeEvent;
 
 /**
  *
  * @author Karl
  */
 public class PortionBean implements Serializable, ActionListener {
 
     private Question currentQuestion;
     private String question;
     private String selectedAnswer;
     private String option_a;
     private String option_b;
     private String option_c;
     private String option_d;
     private String option_e;
     private Group currentGroup;
     private List<Post> currentGroupDiscussion;
     private Post selectedPost;
     private String studentPost;
     private boolean summaryBit;
     private Assignment selectedAssignment;
     private String assignment_name;
     private String assignment_descr;
     private Date indivdual_start_date;
     private Date indivdual_end_date;
     private Date BP_start_date;
     private Date BP_end_date;
     private Date MP_start_date;
     private Date MP_end_date;
     private boolean showAssignment;
     private boolean showQuestion;
 
     public boolean isShowAssignment() {
         return showAssignment;
     }
 
     public void setShowAssignment(boolean showAssignment) {
         this.showAssignment = showAssignment;
     }
 
     public boolean isShowQuestion() {
         return showQuestion;
     }
 
     public void setShowQuestion(boolean showQuestion) {
         this.showQuestion = showQuestion;
     }
 
     public String getAssignment_name() {
         return assignment_name;
     }
 
     public void setAssignment_name(String assignment_name) {
         this.assignment_name = assignment_name;
     }
 
     public String getAssignment_descr() {
         return assignment_descr;
     }
 
     public void setAssignment_descr(String assignment_descr) {
         this.assignment_descr = assignment_descr;
     }
 
     public Date getIndivdual_start_date() {
         return indivdual_start_date;
     }
 
     public void setIndivdual_start_date(Date indivdual_start_date) {
         this.indivdual_start_date = indivdual_start_date;
     }
 
     public Date getIndivdual_end_date() {
         return indivdual_end_date;
     }
 
     public void setIndivdual_end_date(Date indivdual_end_date) {
         this.indivdual_end_date = indivdual_end_date;
     }
 
     public Date getBP_start_date() {
         return BP_start_date;
     }
 
     public void setBP_start_date(Date BP_start_date) {
         this.BP_start_date = BP_start_date;
     }
 
     public Date getBP_end_date() {
         return BP_end_date;
     }
 
     public void setBP_end_date(Date BP_end_date) {
         this.BP_end_date = BP_end_date;
     }
 
     public Date getMP_start_date() {
         return MP_start_date;
     }
 
     public void setMP_start_date(Date MP_start_date) {
         this.MP_start_date = MP_start_date;
     }
 
     public Date getMP_end_date() {
         return MP_end_date;
     }
 
     public void setMP_end_date(Date MP_end_date) {
         this.MP_end_date = MP_end_date;
     }
 
     public boolean isSummaryBit() {
         return summaryBit;
     }
 
     public void setSummaryBit(boolean summaryBit) {
         this.summaryBit = summaryBit;
     }
 
     public String getStudentPost() {
         return studentPost;
     }
 
     public void setStudentPost(String studentPost) {
         this.studentPost = studentPost;
     }
 
     public PortionBean() {
         currentGroupDiscussion = new ArrayList<Post>();
     }
 
     public Post getSelectedPost() {
         return selectedPost;
     }
 
     public void setSelectedPost(Post selectedPost) {
         this.selectedPost = selectedPost;
     }
 
     public List<Post> getCurrentGroupDiscussion() {
         return currentGroupDiscussion;
     }
 
     public void setCurrentGroupDiscussion(List<Post> currentGroupDiscussion) {
         this.currentGroupDiscussion = currentGroupDiscussion;
     }
 
     public Group getCurrentGroup() {
         return currentGroup;
     }
 
     public void setCurrentGroup(Group currentGroup) {
         this.currentGroup = currentGroup;
     }
 
     public String getSelectedAnswer() {
         return selectedAnswer;
     }
 
     public void setSelectedAnswer(String selectedAnswer) {
         this.selectedAnswer = selectedAnswer;
     }
 
     public Question getCurrentQuestion() {
         return currentQuestion;
     }
 
     public void setCurrentQuestion(Question currentQuestion) {
         this.currentQuestion = currentQuestion;
     }
 
     public String getQuestion() {
         return question;
     }
 
     public void setQuestion(String question) {
         this.question = question;
     }
 
     public String getOption_a() {
         return option_a;
     }
 
     public void setOption_a(String option_a) {
         this.option_a = option_a;
     }
 
     public String getOption_b() {
         return option_b;
     }
 
     public void setOption_b(String option_b) {
         this.option_b = option_b;
     }
 
     public String getOption_c() {
         return option_c;
     }
 
     public void setOption_c(String option_c) {
         this.option_c = option_c;
     }
 
     public String getOption_d() {
         return option_d;
     }
 
     public void setOption_d(String option_d) {
         this.option_d = option_d;
     }
 
     public String getOption_e() {
         return option_e;
     }
 
     public void setOption_e(String option_e) {
         this.option_e = option_e;
     }
 
     public void onTabChange(TabChangeEvent event) {
         System.out.println("ON TAB CHANGE EVENT");
 
         System.out.println("AssignmentID: " + ((StudentAssignmentMenuBean.MenuTab) event.getData()).getAssignment().getAssignment_id());
         System.out.println();
         this.selectedAssignment = ((StudentAssignmentMenuBean.MenuTab) event.getData()).getAssignment();
 
         this.assignment_name = this.selectedAssignment.getAssignment_name();
         this.assignment_descr = this.selectedAssignment.getAssignment_descr();
         this.indivdual_start_date = this.selectedAssignment.getIndividual_start_date();
         this.indivdual_end_date = this.selectedAssignment.getIndividual_end_date();
         this.BP_start_date = this.selectedAssignment.getBP_start_date();
         this.BP_end_date = this.selectedAssignment.getBP_end_date();
         this.MP_start_date = this.selectedAssignment.getMP_start_date();
         this.MP_end_date = this.selectedAssignment.getMP_end_date();
 
         showAssignment = true;
         showQuestion = false;
         clearSelection();
     }
 
     private void clearSelection() {
         System.out.println("CLEARING GROUP SELECTION!");
         this.currentQuestion = null;
         this.selectedAnswer = null;
         this.question = null;
         this.option_a = null;
         this.option_b = null;
         this.option_c = null;
         this.option_d = null;
         this.option_e = null;
         this.currentGroup = null;
         this.currentGroupDiscussion = null;
         this.studentPost = "";
     }
 
     @Override
     public void processAction(ActionEvent ae) throws AbortProcessingException {
 
         clearSelection();
         CommandLink itm = (CommandLink) ae.getSource();
         String menu_name = itm.getValue().toString();
         this.currentQuestion = (Question) itm.getValue();
         this.option_a = currentQuestion.getOption_a();
         this.option_b = currentQuestion.getOption_b();
         this.option_c = currentQuestion.getOption_c();
         this.option_d = currentQuestion.getOption_d();
         this.option_e = currentQuestion.getOption_e();
         this.question = currentQuestion.getQuestion();
         System.out.println(">>> Current Question: " + this.option_a);
         System.out.println("GROUPSELECTEDBEAN: processAction occurred!!!!");
         this.showAssignment = false;
         this.showQuestion = true;
 
     }
 
     public boolean studentSubmitPost() {
 
         System.out.println("Portion Bean: Submitting Post");
         if (this.studentPost.length() > 0) {
             System.out.println("User: " + LoginBean.user.getF_name());
             System.out.println("Post: " + this.studentPost);
             System.out.println("Group: " + this.currentGroup);
             System.out.println();
 
             Post toSubmit = new Post(this.studentPost, new Timestamp(System.currentTimeMillis()), currentQuestion.isIs_mp_question(), currentQuestion.isIs_bp_question(), summaryBit, LoginBean.user.getUser_id(), this.currentGroup.getGroup_id());
             PostDAO.insertPost(toSubmit);
             this.currentGroupDiscussion = PostDAO.getPostByGroupId(currentGroup.getGroup_id());
             this.studentPost = "";
             this.summaryBit = false;
             return true;
         }
         return false;
     }
 
     public void submitAnswer(ActionEvent ae) {
 
         System.out.println("PORTION BEAN: SUBMITANSWER: Selected answer: " + this.selectedAnswer);
 
         System.out.println(LoginBean.user.getUser_id());
 
         Assignment a = AssignmentDAO.getAssignmentByQuestion(currentQuestion.getQuestion_id());
 
         System.out.println(currentQuestion.getQuestion_id());
         System.out.println(selectedAnswer.charAt(0));
 
         MCResponse m = new MCResponse(LoginBean.user.getUser_id(),
                 a.getAssignment_id(),
                 currentQuestion.getQuestion_id(),
                 selectedAnswer.charAt(0));
 
        /* [jakers] check to see if student can submit/update their answer */
         Date now = new Date(System.currentTimeMillis());
         Date due = a.getIndividual_end_date();
         if (now.before(due)) {
             if (MCResponseDAO.hasResponded(AssignmentDAO.getAssignmentByQuestion(currentQuestion.getQuestion_id()).getAssignment_id(), currentQuestion.getQuestion_id(), LoginBean.user.getUser_id())) {
                 MCResponseDAO.updateMCResponse(m);
             } else {
                 MCResponseDAO.insertMCResponse(m);
             }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Your choice has been submitted.", "Successful Submission!!"));
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Your choice has NOT been submitted.", "Submission Unsuccesful, passed due date!"));
         }
        
        
     }
 
     public boolean renderIndiv() {
         if (currentQuestion == null) {
             return false;
         }
         System.out.println("RENDER INDIV! Result is: " + (!currentQuestion.isIs_bp_question() && !currentQuestion.isIs_mp_question()));
         if (!currentQuestion.isIs_bp_question() && !currentQuestion.isIs_mp_question()) {
             this.currentGroup = null;
             return true;
         }
         return false;
     }
 
     public boolean renderBP() {
         System.out.println("PortionBean: RenderBP: " + currentQuestion);
         if (currentQuestion == null) {
             return false;
         }
         System.out.println("PortionBean: RenderBP: CurrentQuestion ID: " + currentQuestion.getQuestion_id());
         System.out.println("PortionBean: PRENDER BP! Result is: " + (currentQuestion.isIs_bp_question()));
         if (currentQuestion.isIs_bp_question()) {
 
             if (GroupDAO.isUserInGroup(LoginBean.user.getUser_id(), AssignmentDAO.getAssignmentByQuestion(this.currentQuestion.getQuestion_id()).getAssignment_id())) {
                 System.out.println("PortionBean: Passed Check");
                 this.currentGroup = GroupDAO.getGroupByUserAndAssignment(LoginBean.user.getUser_id(), AssignmentDAO.getAssignmentByQuestion(this.currentQuestion.getQuestion_id()).getAssignment_id());
                 this.currentGroupDiscussion = PostDAO.getBpPostInGroup(currentGroup.getGroup_id());
                 System.out.println(">>> Current Group: " + this.currentGroup.getGroup_id());
 
             } else {
                 return false;
             }
             return true;
         }
         return false;
     }
 
     public boolean renderMP() {
         System.out.println("PortionBean: RenderMP: " + currentQuestion);
         if (currentQuestion == null) {
             return false;
         }
         System.out.println("PortionBean: RenderMP: CurrentQuestion ID: " + currentQuestion.getQuestion_id());
         if (currentQuestion.isIs_mp_question()) {
 
             if (GroupDAO.isUserInGroup(LoginBean.user.getUser_id(), AssignmentDAO.getAssignmentByQuestion(this.currentQuestion.getQuestion_id()).getAssignment_id())) {
                 System.out.println("PortionBean: RenderMP: IsUserInGroup == true");
                 this.currentGroup = GroupDAO.getGroupByUserAndAssignment(LoginBean.user.getUser_id(), AssignmentDAO.getAssignmentByQuestion(this.currentQuestion.getQuestion_id()).getAssignment_id());
                 this.currentGroupDiscussion = PostDAO.getMpPostInGroup(currentGroup.getGroup_id());
                 System.out.println(">>> Current Group: " + this.currentGroup.getGroup_id());
 
             } else {
                 return false;
             }
             return true;
         }
         return false;
     }
 }
