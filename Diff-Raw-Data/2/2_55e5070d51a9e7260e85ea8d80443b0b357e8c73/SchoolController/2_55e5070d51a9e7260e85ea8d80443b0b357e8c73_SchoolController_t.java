 /**
  *
  */
 package controllers.schools;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.PersistenceException;
 
 import com.avaje.ebean.Ebean;
 
 import models.EMessages;
 import models.data.Link;
 import models.dbentities.SchoolModel;
 import models.management.ModelState;
 import models.user.AuthenticationManager;
 import models.user.Role;
 import models.user.Teacher;
 import models.user.UserType;
 import models.util.OperationResultInfo;
 import play.data.Form;
 import play.mvc.Result;
 import play.mvc.Results;
 import views.html.commons.noaccess;
 import views.html.schools.addschool;
 import views.html.schools.editSchool;
 import views.html.schools.schools;
 import controllers.EController;
 
 /**
  * @author Jens N. Rammant
  */
 public class SchoolController extends EController {
 
     /**
      *
      * @return a page of all the schools the teacher is somehow associated with.
      *         No Access page if user is not teacher.
      */
     public static Result viewSchools(int page, String orderBy, String order, String filter) {
         // Generate breadcrumbs & template arguments
         ArrayList<Link> breadcrumbs = getBreadcrumbs();
         if (!isAuthorized())
             return ok(noaccess.render(breadcrumbs));
         OperationResultInfo ori = new OperationResultInfo();
         String teacherID = (getTeacher()==null)?"!!NoTeacher!!":getTeacher().getID();
         //Configure the manager
         SchoolManager sm = new SchoolManager(ModelState.READ, teacherID);
         sm.setFilter(filter);
         sm.setOrder(order);
         sm.setOrderBy(orderBy);
         //Try to render list
         try{
             return ok(schools.render(sm.page(page), sm, orderBy, order,filter, breadcrumbs, ori));
         }catch(PersistenceException pe){
             ori.add(EMessages.get("schools.list.error"),OperationResultInfo.Type.ERROR);
             return ok(schools.render(null, sm, orderBy, order,filter, breadcrumbs, ori));
         }
     }
 
     /**
      *
      * @return a page for creating a new school. No Access page if user is not a
      *         teacher
      */
     public static Result create() {
         // Generate breadcrumbs
         List<Link> breadcrumbs = getBreadcrumbs();
         breadcrumbs.add(new Link(EMessages.get("schools.add"), "/schools/new"));
         OperationResultInfo ori = new OperationResultInfo();
         // Check if authorized
         if (!isAuthorized())
             return ok(noaccess.render(breadcrumbs));
         // Create & render form
         Form<SchoolModel> form = new Form<SchoolModel>(SchoolModel.class);
         return ok(addschool.render(form, breadcrumbs, ori));
     }
 
     /**
      * Saves the data from the form
      *
      * @return the list page if succesfull. Otherwise the form page with an
      *         error
      */
     public static Result save() {
         // Generate breadcrumbs
         List<Link> breadcrumbs = getBreadcrumbs();
         breadcrumbs.add(new Link(EMessages.get("schools.add"), "/schools/new"));
         if (!isAuthorized())
             return ok(noaccess.render(breadcrumbs)); // Check if authorized
 
         // Retrieve the form
         Form<SchoolModel> form = form(SchoolModel.class).bindFromRequest();
         if (form.hasErrors()) {
             // Form was not complete --> return form with a warning
             OperationResultInfo ori = new OperationResultInfo();
             ori.add(EMessages.get("schools.error.notcomplete"),
                     OperationResultInfo.Type.WARNING);
             return badRequest(addschool.render(form, breadcrumbs, ori));
         }
         // Try to save the info
         SchoolModel m = form.get();
         try {
             String teacherID = (getTeacher()==null)?"!!NoTeacher!!":getTeacher().getID();
             m.orig = teacherID; // Add teacher's id as 'originator'
             m.save();
         } catch (Exception p) {
             // Something went wrong in the saving. Redirect back to the create
             // page with an error alert
             OperationResultInfo ori = new OperationResultInfo();
             ori.add(EMessages.get("schools.error.savefail"),
                     OperationResultInfo.Type.ERROR);
             return badRequest(addschool.render(form, breadcrumbs, ori));
         }
         // Redirect back to the list
        flash("succes", Integer.toString(m.id)); // Show id of newly created
                                                     // school in message
         return Results.redirect(controllers.schools.routes.SchoolController
                 .viewSchools(0,"name","asc",""));
 
     }
     /**
      *
      * @param id of the school
      * @return edit page for the school
      */
     public static Result edit(int id){
         //Initialize template arguments
         OperationResultInfo ori = new OperationResultInfo();
         List<Link> bc = getBreadcrumbs();
         bc.add(new Link(EMessages.get("schools.edit"), "/schools/"+id));
 
         //Try to show edit page for school
         try{
             //Check if authorized
             if(!isAuthorized(id))return ok(noaccess.render(bc));
             SchoolModel sm = Ebean.find(SchoolModel.class, id);
             @SuppressWarnings("unused")
             int temp = sm.id; //will throw exception if null
             Form<SchoolModel> f = form(SchoolModel.class).bindFromRequest().fill(sm);
             return ok(editSchool.render(id, f, bc, ori));
         }catch(Exception e){
             ori.add(EMessages.get("schools.error"),OperationResultInfo.Type.ERROR);
             return ok(editSchool.render(id, null, bc, ori));
         }
 
     }
     /**
      * saves the updated school
      * @param id of the school
      * @return    list of schools page
      */
     public static Result update(int id){
         //Initialize template arguments
         OperationResultInfo ori = new OperationResultInfo();
         List<Link> bc = getBreadcrumbs();
         bc.add(new Link(EMessages.get("schools.edit"), "/schools/"+id));
         Form<SchoolModel> f = form(SchoolModel.class).bindFromRequest();
 
         //Update the database with the updated schoolmodel
         try{
             //Check if authorized
             if(!isAuthorized(id))return ok(noaccess.render(bc));
             //check if form is valid
             if(f.hasErrors()){
                 ori.add(EMessages.get("schools.error.notcomplete"), OperationResultInfo.Type.WARNING);
                 return badRequest(editSchool.render(id, f, bc, ori));
             }
             SchoolModel old = Ebean.find(SchoolModel.class, id);
             SchoolModel neww = f.get();
             neww.id = id;
             neww.orig = old.orig;
             neww.update();
             return redirect(routes.SchoolController.viewSchools(0,"name","asc",""));
         }catch(Exception e){
             ori.add(EMessages.get("schools.error.savefail"), OperationResultInfo.Type.ERROR);
             return badRequest(editSchool.render(id, f, bc, ori));
         }
     }
 
     /**
      *
      * @return the standard breadcrumbs for school management
      */
     public static ArrayList<Link> getBreadcrumbs() {
         ArrayList<Link> res = new ArrayList<Link>();
         res.add(new Link("Home", "/"));
         res.add(new Link(EMessages.get("schools.title"), "/schools"));
         return res;
     }
 
     /**
      *
      * @return whether the user is authorized to view a School Management page
      */
     private static boolean isAuthorized() {
         return AuthenticationManager.getInstance().getUser().hasRole(Role.MANAGESCHOOLS);
     }
 
     /**
      *
      * @param id of the school
      * @return whether the user is authorized to edit the class
      * @throws PersistenceException
      */
     public static boolean isAuthorized(int id) throws PersistenceException{
         if(!isAuthorized())return false;
         if(AuthenticationManager.getInstance().getUser().getType()==UserType.TEACHER){
             SchoolModel sm = Ebean.find(SchoolModel.class,id);
             return sm.orig.equals(AuthenticationManager.getInstance().getUser().getID());
         }
         return false;
     }
 
     /**
      *
      * @return the currently logged in Teacher. null if it's not a teacher
      *
      */
     private static Teacher getTeacher() {
         try{
             return (Teacher) AuthenticationManager.getInstance().getUser();
         }catch(Exception e){
             return null;
         }
 
     }
 }
