 
 package models.user;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import models.dbentities.ClassGroup;
 import models.dbentities.ClassPupil;
 import models.dbentities.UserModel;
 
 import com.avaje.ebean.Ebean;
 
 import play.mvc.Content;
 import play.mvc.Result;
 import views.html.landingPages.PupilLandingPage;
 
 /**
  * @author Sander Demeester
  * @author Jens N. Rammant
  */
 
 public class Independent extends User{
 
     private List<String> previousClassList;
 
     public Independent(UserModel data){
         super(data); //abstract class constructor could init some values
         previousClassList = new ArrayList<String>();
     }
 
     /**
      * Add an old class
      * @param oldClass
      */
     public void addPreviousClass(String oldClass){
         previousClassList.add(oldClass);
     }
 
     /**
      * Add a class to Independent user.
      * @param classGroup
      */
     public void addCurrentClass(ClassGroup classGroup){
 
     }
 
     public ClassGroup getCurrentClass(){
     	return Ebean.find(ClassGroup.class).where().eq("id", this.data.classgroup).findUnique();
     }
 
 	@Override
 	public Content getLandingPage() {
 		
 		//TODO
 		return null;
 	}
 
 	@Override
 	public Result showStatistics() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	/**
 	 * Queries the database for all previous classes the user is associated with
 	 * @return list of previous classes 
 	 */
 	public Collection<ClassGroup> getPreviousClasses(){
		ArrayList<ClassGroup> res = new ArrayList<>();
 		
 		List<ClassPupil> cp = Ebean.find(ClassPupil.class).where().eq("indid", this.data.id).findList();
 		for(ClassPupil c : cp){
 			ClassGroup cg = Ebean.find(ClassGroup.class).where().eq("id", c.classid).findUnique();
 			if(cg != null)res.add(cg);
 		}
 		
 		return res;
 	}
 
 
 }
