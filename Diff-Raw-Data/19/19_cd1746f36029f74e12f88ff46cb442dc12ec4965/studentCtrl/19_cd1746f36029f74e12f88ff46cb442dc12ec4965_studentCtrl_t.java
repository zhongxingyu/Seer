 package controller;
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Named;
 import business.*;
 import model.Student;
 
 
 @Named
 @SessionScoped
 public class studentCtrl implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5184733444596178113L;
 	private int nds;
 	private String gender;
 	private String firstname;
 	private String lastname;
 	private String adresse;
 	private String email;
 	private String phone;
 	private String mobil;
 	private String keyword;
 	
 	private Student student;
 
 	@EJB
 	DbPerson dbP;
 	
 
 	
 	// ------------------------- FM -------------------------createStudent---------------------
 	// Creates a new student using all db col params
 	public String createStudent(){
 		dbP.createStudent( gender, firstname, lastname, adresse, email, phone, mobil, keyword);		
 		return "addStudent";
 	}
 	
 	// ------------------------------ FM --------------------getStudent--------------------------
 	// Finds a single Student from db, searchparam: lastname
 	//additional test logger and student object / TODO
 	public void getStudent(){
 		
 		Student a = dbP.findStudentByName("lastname");
 		String lastname = a.getFirstname();
 		
 		Student b = dbP.findStudentByNds(152);
 		String nds = b.getFirstname();
 		
 	Logger.getLogger(studentCtrl.class.getName())
     .log(Level.INFO, 
     "studentctrl  nds: "+nds+"  lastname: "+lastname, firstname);
 	 
 	}
 	
 	// --------------------- FM ------------------------getAllStudents----------------------
 	// Returns a list of all Students
 	//additional test logger for view of all students
 	public void getAllStudents(){
 		
 		List<Student> list = dbP.getAllStudents();
 		
 		for (Iterator<Student> iter = list.iterator(); iter.hasNext();) {		
 			Logger.getLogger(studentCtrl.class.getName())
 		    .log(Level.INFO, 
 		    "studentctrl Liste:   "+iter.next().getFirstname(), firstname);
 		}
 	}
 	
 	//-------------------------JL----------------deleteStudent-----------------
 	//deletes a student from db, using nds (primary key)
 	public String deleteStudent(){
 
 		if(dbP.deleteStudentByNds(this.nds) == true){
 		return "delSuccess";
 		}
 		else
 		return "delError";
 		
 	}
 	
 	
     // ----------------------- FM ---------------------editStudent----------------
     // edits some parameter of given student
     
     public Student editStudent(){
     	
 		/*Logger.getLogger(studentCtrl.class.getName())
 	    .log(Level.INFO, 
 	    "studentCtrl Liste:   "+student.getNds());
     	*/
     	
     	//Student testobjekt
     	student = dbP.findStudentByNds(2);
     	
    	student.setPhone("sonshice"); 
     	
    	dbP.editStudent(student);
     	
     	return student;
     }
 	
 
 	
 
 	
 	//----------------- Getter / Setter ------------------------------------
 
 
 	public void setFirstname(String firstname) {
 		this.firstname = firstname;
 	}
 
 	public String getFirstname() {
 		return firstname;
 	}
 
 	public void setLastname(String lastname) {
 		this.lastname = lastname;
 	}
 
 	public String getLastname() {
 		return lastname;
 	}
 
 	public void setAdresse(String adresse) {
 		this.adresse = adresse;
 	}
 
 	public String getAdresse() {
 		return adresse;
 	}
 	
 	public void setNds(int nds) {
 		this.nds = nds;
 	}
 
 	public int getNds() {
 		return nds;
 	}
 
 	public String getGender() {
 		return gender;
 	}
 
 	public void setGender(String gender) {
 		this.gender = gender;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 
 	public String getPhone() {
 		return phone;
 	}
 
 	public void setPhone(String phone) {
 		this.phone = phone;
 	}
 
 	public String getMobil() {
 		return mobil;
 	}
 
 	public void setMobil(String mobil) {
 		this.mobil = mobil;
 	}
 
 	public String getKeyword() {
 		return keyword;
 	}
 
 	public void setKeyword(String keyword) {
 		this.keyword = keyword;
 	}
 	
 
 	
 	
 	
 	
 
 
 
 
 }
 	
