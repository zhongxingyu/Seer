 package controllers.competition;
 
 import com.avaje.ebean.ExpressionList;
 import controllers.EController;
 import controllers.question.QuestionSetController;
 import models.EMessages;
 import models.competition.CompetitionManager;
 import models.data.Link;
 import models.dbentities.CompetitionModel;
 import models.dbentities.QuestionSetModel;
 import models.dbentities.QuestionSetQuestion;
 import models.management.ModelState;
 import models.question.questionset.QuestionSetManager;
 import models.question.questionset.QuestionSetQuestionManager;
 import models.user.AuthenticationManager;
 import models.user.Role;
 import play.data.Form;
 import play.mvc.Result;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 
 /**
  * Controller for competitions.
  *
  * @author Kevin Stobbelaar
  */
 public class CompetitionController extends EController {
 
     /**
      * Check if the current user is authorized for this editor
      * @return is the user authorized
      */
     private static boolean isAuthorized() {
         return AuthenticationManager.getInstance().getUser().hasRole(Role.MANAGECONTESTS);
     }
 
     /**
      * Returns the default breadcrumbs for the contest pages.
      * @return breadcrumbs
      */
     private static List<Link> defaultBreadcrumbs(){
         List<Link> breadcrumbs = new ArrayList<Link>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link(EMessages.get("competition.name"), "/contests"));
         return breadcrumbs;
     }
 
     /**
      * Returns a question set manager.
      *
      * @param modelState The model state
      * @param orderBy    The column to be ordered on
      * @param order      The sort order
      * @param filter     String to be filtered on
      * @param contid     Contest id
      * @return question set manager
      */
     private static QuestionSetManager getQuestionSetManager(ModelState modelState, String orderBy, String order,
                                                             String filter, String contid){
         QuestionSetManager questionSetManager = new QuestionSetManager(modelState, contid, "");
         questionSetManager.setOrderBy(orderBy);
         questionSetManager.setOrder(order);
         questionSetManager.setFilter(filter);
         return questionSetManager;
     }
 
     /**
      * Returns the index page for contests.
      * @param page page
      * @param orderBy column to order on
      * @param order sort order
      * @param filter string to filter on
      * @return index page
      */
     public static Result index(int page, String orderBy, String order, String filter){
         if (!isAuthorized()) return redirect(controllers.routes.Application.index());
         CompetitionManager competitionManager = new CompetitionManager(ModelState.READ, orderBy, "");
         competitionManager.setOrder(order);
         competitionManager.setOrderBy(orderBy);
         competitionManager.setFilter(filter);
         return ok(views.html.competition.index.render(defaultBreadcrumbs(), competitionManager.page(page), competitionManager
                   , orderBy, order, filter));
     }
 
     /**
      * Returns the create page for contests.
      *
      * @return create contest page
      */
     public static Result create(){
         if (!isAuthorized()) return redirect(controllers.routes.Application.index());
         List<Link> breadcrumbs = defaultBreadcrumbs();
         breadcrumbs.add(new Link(EMessages.get("competition.create.breadcrumb"), "/contests/create"));
         Form<CompetitionModel> form = form(CompetitionModel.class).bindFromRequest();
         return ok(views.html.competition.create.render(form, breadcrumbs, false));
     }
 
     /**
      * Saves the newly created contest.
      * Returns the page with step 2: create question set.
      *
      * @return create question set page
      */
     public static Result save(){
         if (!isAuthorized()) return redirect(controllers.routes.Application.index());
         Form<CompetitionModel> form = form(CompetitionModel.class).bindFromRequest();
         if(form.hasErrors()) {
             List<Link> breadcrumbs = defaultBreadcrumbs();
             breadcrumbs.add(new Link(EMessages.get("competition.create.breadcrumb"), "/contests/create"));
             return badRequest(views.html.competition.create.render(form, breadcrumbs, true));
         }
         CompetitionModel competitionModel = form.get();
         competitionModel.id = UUID.randomUUID().toString();
         competitionModel.creator = AuthenticationManager.getInstance().getUser().getID();
        // TODO check startdate < enddate
         competitionModel.save();
         return redirect(controllers.question.routes.QuestionSetController.create(competitionModel.id));
     }
 
     /**
      * Updates the edited contest.
      * Redirects to the view contest page.
      *
      * @param contestid contest id
      * @return redirect to view contest page.
      */
     public static Result updateCompetition(String contestid){
         if (!isAuthorized()) return redirect(controllers.routes.Application.index());
         CompetitionManager competitionManager = new CompetitionManager(ModelState.UPDATE, "name", contestid);
         QuestionSetManager questionSetManager = getQuestionSetManager(ModelState.READ, "level", "", "", contestid);
         Form<CompetitionModel> form = form(CompetitionModel.class).fill(competitionManager.getFinder().byId(contestid)).bindFromRequest();
         if(form.hasErrors()) {
             List<Link> breadcrumbs = defaultBreadcrumbs();
             breadcrumbs.add(new Link(EMessages.get("competition.edit.breadcrumb"), "/contest/:" + contestid));
             flash("competition-error", EMessages.get("forms.error"));
             return badRequest(views.html.competition.viewCompetition.render(breadcrumbs, questionSetManager.page(0),
                     questionSetManager, "difficulty", "", "", form, competitionManager));
         }
         form.get().update();
         return redirect(routes.CompetitionController.viewCompetition(contestid, 0, "", "", ""));
     }
 
     /**
      * Returns the overview page for editing an existing contest.
      * @param contestid contest id
      * @param page page number
      * @param orderBy column to sort on
      * @param order sort order
      * @param filter string to filter on
      * @return overview page for a single contest
      */
     public static Result viewCompetition(String contestid, int page, String orderBy, String order, String filter){
         if (!isAuthorized()) return redirect(controllers.routes.Application.index());
         CompetitionManager competitionManager = new CompetitionManager(ModelState.UPDATE, "", contestid);
         Form<CompetitionModel> form = form(CompetitionModel.class).bindFromRequest().fill(competitionManager.getFinder().byId(contestid));
         List<Link> breadcrumbs = defaultBreadcrumbs();
         breadcrumbs.add(new Link(EMessages.get("competition.edit.breadcrumb"), "/contest/:" + contestid));
         QuestionSetManager questionSetManager = getQuestionSetManager(ModelState.READ, orderBy, order, filter, contestid);
         return ok(views.html.competition.viewCompetition.render(breadcrumbs, questionSetManager.page(page),
                 questionSetManager, orderBy, order, filter, form, competitionManager));
     }
 
     /**
      * Deletes the selected contest.
      * Also deletes every question set linked with the selected contest.
      * Redirects to the contest index page.
      * @param contestid contest id
      * @return redirect to contest index page.
      */
     public static Result removeCompetition(String contestid){
         if (!isAuthorized()) return redirect(controllers.routes.Application.index());
         // remove all question sets in this contest from questionsets table
         QuestionSetManager questionSetManager = new QuestionSetManager(ModelState.DELETE, contestid, "");
         List<QuestionSetModel> questionSetModels = questionSetManager.getFinder().where().ieq("contid", contestid).findList();
         for (QuestionSetModel questionSetModel : questionSetModels){
             questionSetModel.delete();
         }
         // remove competition
         CompetitionManager competitionManager = new CompetitionManager(ModelState.DELETE, "name", contestid);
         competitionManager.getFinder().byId(contestid).delete();
         return redirect(routes.CompetitionController.index(0,"name","asc",""));
     }
 
     /**
      * Deletes the selected question set.
      * Redirects to the contest overview page.
      * @param qsid      question set id
      * @param contestid contest id
      * @return redirect to the contest overview page
      */
     public static Result removeQuestionSet(String qsid, String contestid){
         if (!isAuthorized()) return redirect(controllers.routes.Application.index());
         // remove all questions in this question set from questionsetquestions table
         QuestionSetQuestionManager questionSetQuestionManager = new QuestionSetQuestionManager(ModelState.DELETE, qsid);
         List<QuestionSetQuestion> questions = questionSetQuestionManager.getFinder().where().ieq("qsid", qsid).findList();
         for (QuestionSetQuestion questionSetQuestion : questions){
             questionSetQuestion.delete();
         }
         // remove the question set
         QuestionSetManager questionSetManager = new QuestionSetManager(ModelState.DELETE, contestid, "");
         questionSetManager.getFinder().byId(qsid).delete();
         return redirect(routes.CompetitionController.viewCompetition(contestid, 0, "level", "asc", ""));
     }
 
 
 
 }
