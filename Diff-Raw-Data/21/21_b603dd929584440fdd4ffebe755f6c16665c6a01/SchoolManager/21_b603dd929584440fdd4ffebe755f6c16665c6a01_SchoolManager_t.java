 /**
  * 
  */
 package controllers.schools;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.persistence.PersistenceException;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.Expr;
 import com.avaje.ebean.Expression;
 import com.avaje.ebean.ExpressionList;
 
 import play.mvc.Call;
 import models.dbentities.ClassGroup;
 import models.dbentities.SchoolModel;
 import models.dbentities.UserModel;
 import models.management.Manager;
 import models.management.ModelState;
 import models.user.Teacher;
 
 /**
  * @author Jens N. Rammant
  *
  */
 public class SchoolManager extends Manager<SchoolModel> {
 
 	String teacherID;
 	
 	public SchoolManager(ModelState state, String teacherID) {
 		super(SchoolModel.class, state, "name", "name");
 		this.teacherID = teacherID;
 	}
 
 	@Override
 	public Call getListRoute(int page, String orderBy, String order, String filter) {
 		return routes.SchoolController.viewSchools(page,orderBy,order,filter);
 	}
 
 	@Override
 	public Call getAddRoute() {
 		return routes.SchoolController.create();
 	}
 
 	@Override
 	public Call getEditRoute(String id) {
		int schoolID = -1;
		try{
			schoolID = Integer.parseInt(id);
		}catch(NumberFormatException nfe){}
		if(SchoolController.isAuthorized(schoolID)){
			return routes.SchoolController.edit(Integer.parseInt(id));
		}else return null;
 	}
 
 	@Override
 	public Call getRemoveRoute(String id) {
 		// not used
 		return null;
 	}
 
 	@Override
 	public play.api.mvc.Call getSaveRoute() {
 		// not used
 		return null;
 	}
 
 	@Override
 	public play.api.mvc.Call getUpdateRoute() {
 		// not used
 		return null;
 	}
 
 	@Override
 	public String getMessagesPrefix() {
 		return "schools";
 	}
 	
 	@Override
 	protected ExpressionList<SchoolModel> getDataSet(){
 		UserModel um = Ebean.find(UserModel.class,teacherID);
 		if(um==null)throw new PersistenceException("Could not find Teacher");
 		Teacher t = new Teacher(um);
 		
 		//Retrieve all the schoolids from classes the Teacher is associated with
     	HashSet<Integer> schoolIDs = new HashSet<Integer>();
     	for(ClassGroup cg : t.getClasses()){
     		schoolIDs.add(cg.schoolid);
     	}
     	
     	Expression exp1 = Expr.eq("orig", teacherID); //All schools the teacher created
     	Expression exp2 = Expr.in("id", schoolIDs); //All schools the teacher has a class in
     	
     	return super.getDataSet().or(exp1, exp2);
 	}
 	
 	@Override
 	public List<String> getColumnHeaders(){
 		
 		List<String> res = new ArrayList<String>();
 		res.add("id");
 		res.add("name");
 		res.add("address");
 		return res;
 		
 	}
 
 }
