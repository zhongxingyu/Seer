 package controllers.question;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import models.EMessages;
 import models.data.Link;
 import models.dbentities.QuestionModel;
 import models.dbentities.UserModel;
 import models.management.ModelState;
 import models.question.Question;
 import models.question.QuestionBuilderException;
 import models.question.QuestionIO;
 import models.question.QuestionSet;
 import models.question.Server;
 import models.question.submits.Submit;
 import models.question.submits.SubmitsPage;
 import play.Play;
 import play.cache.Cache;
 import play.data.Form;
 import play.mvc.Result;
 import views.html.commons.noaccess;
 import views.html.question.approveQuestionForm;
 import views.html.question.editQuestionForm;
 import views.html.question.newQuestionForm;
 import views.html.question.questionManagement;
 import views.html.question.submitsManagement;
 import views.html.competition.run.questionSet;
 
 import com.avaje.ebean.annotation.Transactional;
 
 import controllers.EController;
 
 /**
  * Actions for controlling the CRUD actions for questions (including approval)
  * @author Ruben Taelman
  *
  */
 public class QuestionController extends EController{
     
     public static Result LIST = redirect(
             routes.QuestionController.list(0, "id", "asc", "")
     );
     
     public static Result LISTSUBMITS = redirect(
             routes.QuestionController.listSubmits(0, "")
     );
     
     /**
      * Check if the current user is authorized for these actions
      * @return is the user authorized
      */
     public static boolean isAuthorized() {
         // TODO: enable this authorization
         //return AuthenticationManager.getInstance().getUser().hasRole(Role.MANAGEQUESTIONS);
         return true;
     }
 
     /**
      * Make default breadcrumbs for this controller
      * @return default breadcrumbs
      */
     private static List<Link> defaultBreadcrumbs() {
         List<Link> breadcrumbs = new ArrayList<Link>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link(EMessages.get("question.questions.name"), "/questions"));
         return breadcrumbs;
     }
 
     /**
      * This result will redirect to the question list page
      *
      * @param oderBy the field to order by
      * @param order "asc" or "desc"
      * @param filter the string to filter on
      *
      * @return question list page
      */
     @Transactional(readOnly=true)
     public static Result list(int page, String orderBy, String order, String filter){
         List<Link> breadcrumbs = defaultBreadcrumbs();
         
         if(!isAuthorized()) return ok(noaccess.render(breadcrumbs));
 
         QuestionManager questionManager = new QuestionManager(ModelState.READ);
         questionManager.setOrder(order);
         questionManager.setOrderBy(orderBy);
         questionManager.setFilter(filter);
         return ok(
             questionManagement.render(questionManager.page(page), questionManager, orderBy, order, filter, breadcrumbs)
         );
     }
     
     /**
      * This result will redirect to the question submits list page
      *
      * @param page the index of the requested page
      * @param filter the string to filter on
      *
      * @return question submits list page
      */
     public static Result listSubmits(int page, String filter){
         List<Link> breadcrumbs = defaultBreadcrumbs();
         
         if(!isAuthorized()) return ok(noaccess.render(breadcrumbs));
         
         breadcrumbs.add(new Link(EMessages.get("question.questions.submissions"), "/questionsubmits"));
         
         return ok(
             submitsManagement.render(new SubmitsPage(page, filter), filter, breadcrumbs)
         );
     }
     
     /**
      * The approval page for submitted questions
      * @param userID the userID of the author
      * @param file the filename of the submitted question
      * @return question approval page
      */
     public static Result approve(String userID, String file){
         List<Link> breadcrumbs = defaultBreadcrumbs();
         breadcrumbs.add(new Link(EMessages.get("question.questions.approve"), ""));
         
         if(!isAuthorized()) return ok(noaccess.render(breadcrumbs));
 
         Form<QuestionModel> form = form(QuestionModel.class)
                 .bindFromRequest()
                 .fill(new QuestionModel(UserModel.find.byId(userID), file));
 
         QuestionManager manager = new QuestionManager(ModelState.CREATE);
         manager.setIgnoreErrors(true);
 
         return ok(approveQuestionForm.render(form, manager, breadcrumbs, userID, file));
     }
     
     /**
      * Handle the approval of the question
      * @param userID the userID of the author
      * @param file the filename of the submitted question
      * @return the question approval list
      */
     @Transactional
     public static Result saveApprove(String userID, String file){
         List<Link> breadcrumbs = defaultBreadcrumbs();
         breadcrumbs.add(new Link(EMessages.get("question.questions.approve"), ""));
         
         if(!isAuthorized()) return ok(noaccess.render(breadcrumbs));
         
         Form<QuestionModel> form = form(QuestionModel.class).bindFromRequest();
         
         QuestionManager manager = new QuestionManager(ModelState.CREATE);
         
         // Check for errors in the form
         if(form.hasErrors()) {
             return badRequest(approveQuestionForm.render(form, manager, breadcrumbs, userID, file));
         }
         
         // Check the uniqueness of the officialid
         if(!form.get().isUnique()) {
             flash("error", EMessages.get("question.error.officialidTaken"));
             return badRequest(approveQuestionForm.render(form, manager, breadcrumbs, userID, file));
         }
         
         // Fetch the submission
         Submit submit = Submit.find(userID, file);
         
         // Try to send the question to the server
         try {
             form.get().fixServer();
             Server server = form.get().server;
             server.sendFile(form.get().officialid, submit.getFile(), userID);
         } catch (Exception e) {
             flash("error", "An error occured: "+e.getMessage());
             return badRequest(approveQuestionForm.render(form, manager, breadcrumbs, userID, file));
         }
         
         // If everything went well, we can save the question
         form.get().save();
         
         // Only if the saving went well, we can delete the submitted archive file
         submit.getFile().delete();
 
         flash("success", EMessages.get("question.success.approved", form.get().officialid));
         return LISTSUBMITS;
     }
     
     /**
      * Remove a submitted question
      * @param userID the userID of the author
      * @param file the filename of the submitted question
      * @return the question approval list
      */
     public static Result removeSubmit(String userID, String file){
         if(!isAuthorized()) return ok(noaccess.render(defaultBreadcrumbs()));
         
         Submit submit = Submit.find(userID, file);
         if(submit != null){
             submit.delete();
             flash("success", EMessages.get("question.questions.submitDeleted"));
         }
         else {
             flash("error", EMessages.get("forms.unknownError"));
         }
         return LISTSUBMITS;
     }
     
     /**
      * This result will redirect to the create a question page
      * containing the corresponding form.
      *
      * @return create a question page
      */
     @Transactional(readOnly=true)
     public static Result create(){
         List<Link> breadcrumbs = defaultBreadcrumbs();
         breadcrumbs.add(new Link(EMessages.get("question.questions.new"), "/questions/create"));
         
         if(!isAuthorized()) return ok(noaccess.render(breadcrumbs));
 
         Form<QuestionModel> form = form(QuestionModel.class).bindFromRequest();
 
         QuestionManager manager = new QuestionManager(ModelState.CREATE);
         manager.setIgnoreErrors(true);
 
         return ok(newQuestionForm.render(form, manager, breadcrumbs));
     }
 
     /**
      * This will handle the creation of a new question and redirect
      * to the question list
      *
      * @return question list page
      */
     @Transactional
     public static Result save(){
         List<Link> breadcrumbs = defaultBreadcrumbs();
         breadcrumbs.add(new Link(EMessages.get("question.questions.new"), "/questions/create"));
         
         if(!isAuthorized()) return ok(noaccess.render(breadcrumbs));
 
         Form<QuestionModel> form = form(QuestionModel.class).bindFromRequest();
 
         if(form.hasErrors()) {
             return badRequest(newQuestionForm.render(form, new QuestionManager(ModelState.CREATE), breadcrumbs));
         }
         
         try {
             form.get().save();
         } catch (Exception e) {
             flash("error", e.getMessage());
             return badRequest(newQuestionForm.render(form, new QuestionManager(ModelState.CREATE), breadcrumbs));
         }
 
         flash("success", EMessages.get("question.success.added", form.get().officialid));
         return LIST;
     }
     
     /**
      * This result will redirect to the edit a question page containing the
      * corresponding form.
      *
      * @param name name of the question to be changed
      * @return edit a question page
      */
     @Transactional(readOnly=true)
     public static Result edit(String name){
         List<Link> breadcrumbs = defaultBreadcrumbs();
         breadcrumbs.add(new Link(EMessages.get("question.questions.question") + " " + name, ""));
         
         if(!isAuthorized()) return ok(noaccess.render(breadcrumbs));
 
         QuestionManager manager = new QuestionManager(name, ModelState.UPDATE);
         manager.setIgnoreErrors(true);
 
         Form<QuestionModel> form = form(QuestionModel.class).bindFromRequest().fill(manager.getFinder().ref(name));
         return ok(editQuestionForm.render(form, manager, breadcrumbs));
     }
 
     /**
      * This will handle the update of an existing question and redirect
      * to the question list
      *
      * @param name name of the question to be updated
      * @return question list page
      */
     @Transactional
     public static Result update(String name){
         List<Link> breadcrumbs = defaultBreadcrumbs();
         breadcrumbs.add(new Link(EMessages.get("question.questions.question") + " " + name, ""));
         
         if(!isAuthorized()) return ok(noaccess.render(breadcrumbs));
 
         QuestionManager manager = new QuestionManager(name, ModelState.UPDATE);
         
         // Validate form
         Form<QuestionModel> form = form(QuestionModel.class).fill(manager.getFinder().byId(name)).bindFromRequest();
         if(form.hasErrors()) {
             return badRequest(editQuestionForm.render(form, manager, breadcrumbs));
         }
         
         // Update
         try {
             form.get().update();
         } catch (Exception e) {
             flash("error", e.getMessage());
             return badRequest(editQuestionForm.render(form, manager, breadcrumbs));
         }
         
         // Result
         flash("success", EMessages.get("question.success.edited", form.get().officialid));
         return LIST;
     }
 
     /**
      * This will handle the removing of an existing question and redirect
      * to the question list
      *
      * @param name name of the question to be removed
      * @return question list page
      */
     @Transactional
     public static Result remove(String name){
         if(!isAuthorized()) return ok(noaccess.render(defaultBreadcrumbs()));
         
         QuestionModel question = new QuestionManager(ModelState.DELETE).getFinder().byId(name);
         try{
             question.delete();
         } catch (Exception e) {
             flash("error", e.getMessage());
             return LIST;
         }
         
         // Result
         flash("success", EMessages.get("question.success.removed", question.officialid));
         return LIST;
     }
     
     /**
      * This will show a file from a certain question pack
      *
      * @param name name of the question
      * @param fileName name of the file to show
      * @return question list page
      */
     public static Result showQuestionFile(String id, String fileName){
         // Make some error breadcrumbs for when an error occurs
         List<Link> errorBreadcrumbs = new ArrayList<Link>();
         errorBreadcrumbs.add(new Link("Home", "/"));
         errorBreadcrumbs.add(new Link("Error",""));
         
         // Get the cachetime from the config file
         int cacheTime = Integer.parseInt(Play.application().configuration().getString("question.proxy.cache"));
         
         // Try to get the file from our cache
         String contentCacheKey = "question.file."+fileName+".content."+id;
         String typeCacheKey = "question.file."+fileName+".type."+id;
         byte[] result = (byte[]) Cache.get(contentCacheKey);
         String contentType = (String) Cache.get(typeCacheKey);
         
         // If this file is not in our cache, we go and get the content ourselves
         if(result == null || contentType == null) {
             try {
                 QuestionModel question = new QuestionManager(ModelState.DELETE).getFinder().byId(id);
                 
                 // Very important step, authenticate via HTTP
                 question.server.setAuthentication();
                 
                 // Copy the url content
                 URL url = new URL(question.server.path + question.officialid + "/" + fileName);
                 URLConnection connection = url.openConnection();
                 InputStream is = connection.getInputStream();
                 ByteArrayOutputStream os = new ByteArrayOutputStream();
                 QuestionIO.copyStream(is, os);
                 
                 result = os.toByteArray();
                 contentType = connection.getContentType();
                 
                 // Add new data to the cache
                 Cache.set(contentCacheKey, result, cacheTime);
                 Cache.set(typeCacheKey, contentType, cacheTime);
             } catch (Exception e) {
                 return internalServerError(views.html.commons.error.render(errorBreadcrumbs, EMessages.get("error.title"), EMessages.get("error.text")));
             }
         }
         
         // Return the file with the correct header
         response().setHeader(CONTENT_TYPE, contentType);
         return ok(result);
     }
     
     //TODO: delete
     public static Result test() {
         String id = "474";
         String id2 = "454";
         try {
             // TODO: cache
             //if (true) return ok(routes.QuestionController.showQuestionFile(id, QuestionPack.QUESTIONXMLFILE).absoluteURL(request()));
             Question q = Question.fetch(id);
             Question q2 = Question.fetch(id2);
            QuestionSet set = new QuestionSet(null);
             set.addQuestion(q);
             set.addQuestion(q2);
             return ok(questionSet.render(set, new ArrayList<Link>()));
             //return ok(q.getIndexLink(Language.getLanguage(EMessages.getLang())).absoluteURL(request()));
         } catch (QuestionBuilderException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return ok(e.getMessage());
         }
     }
 }
