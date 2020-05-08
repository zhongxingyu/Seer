 /**
  * 
  */
 package models.classgroups;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Random;
 
 import javax.persistence.PersistenceException;
 
 import com.avaje.ebean.Ebean;
 
 import controllers.util.PasswordHasher;
import controllers.util.PasswordHasher.HashAndPassword;
 
 import models.EMessages;
 import models.data.Grade;
 import models.dbentities.ClassGroup;
 import models.dbentities.ClassPupil;
 import models.dbentities.SchoolModel;
 import models.dbentities.UserModel;
 import models.user.AuthenticationManager;
 import models.user.IDGenerator;
 import models.user.UserType;
 
 /**
  * @author Jens N. Rammant
  * container class for a ClassGroup object and related UserModel objects. 
  */
 public class ClassGroupContainer {
 
 	private ClassGroup classGroup=null;
 	private boolean isCGValid=false;
 	private String cgMessage="";
 	private boolean isCGNew=true;
 	
 	private List<PupilRecordTriplet> newPupils;
 	private List<PupilRecordTriplet> existingPupils;
 	
 	
 	public ClassGroupContainer(){
 		this.classGroup=null;
 		this.newPupils = new ArrayList<PupilRecordTriplet>();
 		this.existingPupils = new ArrayList<PupilRecordTriplet>();
 	}
 	
 	public void setClassGroup(ClassGroup cg, boolean isValid, String message,boolean isNew){
 		this.classGroup = cg;
 		this.isCGValid = isValid;
 		this.cgMessage = message;
 		this.isCGNew = isNew;
 	}
 	
 	public void appendCGMessage(String st){
 		this.cgMessage = this.cgMessage +"\n"+st;
 	}
 	
 	public void addNewPupil(PupilRecordTriplet prt){
 		this.newPupils.add(prt);
 	}
 	
 	public void addExistingPupil(PupilRecordTriplet prt){
 		this.existingPupils.add(prt);
 	}	
 	
 	/**
 	 * @return the classGroup
 	 */
 	public ClassGroup getClassGroup() {
 		return classGroup;
 	}
 
 	/**
 	 * @return the isCGValid
 	 */
 	public boolean isCGValid() {
 		return isCGValid;
 	}
 
 	/**
 	 * @return the isCGNew
 	 */
 	public boolean isCGNew() {
 		return isCGNew;
 	}
 
 	/**
 	 * @return the cgMessage
 	 */
 	public String getCgMessage() {
 		return cgMessage;
 	}
 
 	/**
 	 * @return the newPupils
 	 */
 	public List<PupilRecordTriplet> getNewPupils() {
 		List<PupilRecordTriplet> res = new ArrayList<PupilRecordTriplet>();
 		res.addAll(newPupils);
 		return res;
 	}
 
 	/**
 	 * @return the existingPupils
 	 */
 	public List<PupilRecordTriplet> getExistingPupils() {
 		List<PupilRecordTriplet> res = new ArrayList<PupilRecordTriplet>();
 		res.addAll(existingPupils);
 		return res;
 	}
 	
 	/**
 	 * For all the valid records in the class, check if they will be able to be saved. If not, set isValid to false
 	 * and add a message.
 	 * @throws PersistenceException when something goes wrong with the db
 	 */
 	public void validate() throws PersistenceException{		
 		//For new students, check if fields are all filled in & correct
 		for(PupilRecordTriplet prt : newPupils){
 			if(prt.isValid){
 				UserModel um = prt.user;
 				if(um.name==null||um.name.isEmpty())prt.isValid=false;
 				if(um.birthdate==null)prt.isValid=false; //TODO check if before today
 				if(um.gender==null)prt.isValid=false;
 				if(um.preflanguage==null||um.preflanguage.isEmpty())prt.isValid=false;
 				if(um.password==null||um.password.isEmpty())prt.isValid=false;
 				//TODO check if possible emailadres is valid
 				if(!prt.isValid)prt.message=EMessages.get("classes.import.newpupil.incomplete");
 			}
 		}		
 		//For existing students, check if they exist & if they're a student
 		for(PupilRecordTriplet prt : existingPupils){
 			if(prt.isValid){
 				UserModel model = Ebean.find(UserModel.class, prt.user.id);
 				if(model==null){
 					prt.isValid=false;
 					prt.message=EMessages.get("classes.import.existingpupil.notexisting");
 				}else if(model.type!=UserType.PUPIL&&model.type!=UserType.INDEPENDENT){
 					prt.isValid=false;
 					prt.message=EMessages.get("classes.import.existingpupil.nopupil");
 				}
 			}
 		}
 		//For new classgroup, check if school & level exist, and other fields are valid
 		if(this.isCGNew&&this.isCGValid){
 			if(classGroup.name==null||classGroup.name.isEmpty()
 					||classGroup.expdate==null
 					||classGroup.level==null || classGroup.level.isEmpty()){
 				this.isCGValid=false;
 				appendCGMessage(EMessages.get("classes.import.newclass.incomplete"));				
 			}
 			
 			SchoolModel sm = Ebean.find(SchoolModel.class,this.classGroup.schoolid);
 			if(sm==null){
 				this.isCGValid=false;
 				appendCGMessage(EMessages.get("classes.import.newclass.nosuchschool"));
 			}
 			
 			Grade grade = Ebean.find(Grade.class,this.classGroup.level);
 			if(grade==null){
 				this.isCGValid=false;
 				appendCGMessage(EMessages.get("classes.import.newclass.nosuchgrade"));
 			}
 		}
 	}
 
 	/**
 	 * Saves all the containers in a transaction
 	 * @return true if saving was successful
 	 */
 	public static boolean save(Collection<ClassGroupContainer> coll){
 		boolean res = true;
 		Ebean.beginTransaction();
 		try{
 			//Iterate over the list
 			for(ClassGroupContainer cgc : coll){
 				//Only save the valid ones that are new
 				if(cgc.isCGValid()){
 					if(cgc.isCGNew){
 						//Add teacherID
 						UserModel u = AuthenticationManager.getInstance().getUser().data;
 						cgc.classGroup.teacherid=u.id;
 						cgc.classGroup.save();
 					}
 					//Add the valid new pupils
 					for(PupilRecordTriplet prt : cgc.getNewPupils()){
 						if(prt.isValid){
 							prepareNewPupil(prt.user,cgc.getClassGroup());
 							prt.user.save();
 						}
 					}
 					//Link the valid existing users
 					for(PupilRecordTriplet prt : cgc.getExistingPupils()){
 						if(prt.isValid){
 							updateExistingPupil(prt.user, cgc.getClassGroup());
 						}
 					}
 				}
 			}
 			Ebean.commitTransaction();
 		}catch(PersistenceException pe){
 			pe.printStackTrace();
 			Ebean.rollbackTransaction();
 			res=false;
 		}finally{
 			Ebean.endTransaction();
 		}
 		return res;
 	}
 	/**
 	 * Hashes the password, generates username, sets registrationdate, sets class
 	 * @param model
 	 */
 	private static void prepareNewPupil(UserModel model,ClassGroup cg){
 		Calendar birthdate = Calendar.getInstance();
 		birthdate.setTime(model.birthdate);
 		
 		model.id=IDGenerator.generate(model.name,birthdate );
 		model.classgroup = cg.id;
 		model.registrationdate = Calendar.getInstance().getTime();		
		HashAndPassword hap = PasswordHasher.hashPassword(model.password);
 		model.password=hap.password;
		model.hash = hap.hash;
 		model.type = UserType.PUPIL;
 		model.active=true;
 	}
 	
 	/**
 	 * Updates the existing pupil to link to a new class, but makes
 	 * sure to remember the old linking in a ClassPupil relationship
 	 * @param model UserModel to save
 	 * @param cg ClassGroup to link
 	 */
 	private static void updateExistingPupil(UserModel modell, ClassGroup cg){
 		//TODO move this functionality to a more fitting class possibly (UserModel maybe?)
 		//Make sure you're using the most up-to-date version of the model
 		UserModel model = Ebean.find(UserModel.class, modell.id);
 		if(model==null)throw new PersistenceException();
 		if(model.classgroup!=null){
 			ClassPupil existing = Ebean.find(ClassPupil.class)
 					.where().eq("indid", model.id)
 					.where().eq("classid", model.classgroup).findUnique();
 			if(existing==null){
 				ClassPupil cp = new ClassPupil();
 				cp.indid=model.id;
 				cp.classid=model.classgroup;
 				cp.save();
 			}			
 		}
 		model.classgroup=cg.id;
 		model.update();
 	}
 	
 	/**
 	 * 
 	 * @author Jens N. Rammant
 	 *	Links a usermodel with a boolean that says if it's valid and a message (that's already been localized)
 	 * that can be shown to users
 	 */
 	public static class PupilRecordTriplet{
 		public UserModel user;
 		public boolean isValid;
 		public String message;
 	}
 	
 	
 }
