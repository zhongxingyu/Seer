 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.fullerton.AcademyAdvisorAppointment.managedBean.student;
 
 import edu.fullerton.AcademyAdvisorAppointment.ejb.LoginBean;
 import edu.fullerton.AcademyAdvisorAppointment.entity.Student;
 import edu.fullerton.AcademyAdvisorAppointment.entity.Type;
 import java.io.Serializable;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 /**
  *
  * @author wujun
  */
@ManagedBean(name="studentControl")
 @SessionScoped
 public class StudentControl implements Serializable{
 
     @EJB
     private LoginBean loginBean;
     
     private Student student;
     private String loginEmail; //field used by login
     private String loginPassword;
     
     @PersistenceContext
     private EntityManager em;
     public StudentControl() {
        //model=new DefaultScheduleModel();
         student=new Student();
     }
 
     public String getLoginEmail() {
         return loginEmail;
     }
 
     public void setLoginEmail(String loginEmail) {
         this.loginEmail = loginEmail;
     }
 
     public String getLoginPassword() {
         return loginPassword;
     }
 
     public void setLoginPassword(String loginPassword) {
         this.loginPassword = loginPassword;
     }
 
     public String getEmail() {
         return student.getEmail();
     }
 
     public void setEmail(String email) {
         student.setEmail(email);
     }
 
     public String getPassword() {
         return student.getPassword();
     }
 
     public void setPassword(String password) {
         student.setPassword(password);
     }
   
     public String getCwid() {
         return student.getCwid();
     }
 
     public void setCwid(String cwid) {
         student.setCwid(cwid);
     }
 
     public String getFirstName() {
         return student.getFirstname();
     }
 
     public void setFirstName(String firstName) {
         student.setFirstname(firstName);
     }
 
     public String getLastName() {
         return student.getLastname();
     }
 
     public void setLastName(String lastName) {
         student.setLastname(lastName);
     }
 
     public String getPhone() {
         return student.getPhone();
     }
 
     public void setPhone(String phone) {
         student.setPhone(phone);
     }
 
     public Type getType() {
         return student.getType();
     }
 
     public void setType(Type type) {
         student.setType(type);;
     }
 
     public EntityManager getEm() {
         return em;
     }
 
     public void setEm(EntityManager em) {
         this.em = em;
     }
     
     
     public String login(){
         Student authStudent=loginBean.authenticateStudent(loginEmail, loginPassword);
         if (authStudent!=null){
             this.student=authStudent;        
             return "makeAppointment";
         }else{
             FacesContext context = FacesContext.getCurrentInstance();
             context.addMessage("login", new FacesMessage("Invalid UserName and Password Combination"));
             return "login";
         }
     }
     
     public void forgetPassword() {
         FacesContext context = FacesContext.getCurrentInstance();
         context.addMessage("login", new FacesMessage("forget Password has not been implemented yet"));
     }
     public String createStudent() {
         try{
             loginBean.createStudent(student);
         }
         catch (Exception e){
             FacesContext context = FacesContext.getCurrentInstance();
             context.addMessage("login", new FacesMessage("Create student error"));
             return "login";
         }
         return "makeAppointment";
     }
 
     public Student getStudent() {
         return student;
     }
 
 }
