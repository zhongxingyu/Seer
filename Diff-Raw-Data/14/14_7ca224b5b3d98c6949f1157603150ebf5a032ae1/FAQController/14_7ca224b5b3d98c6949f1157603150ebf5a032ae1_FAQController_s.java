 /**
  * 
  */
 package controllers.faq;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.PersistenceException;
 import com.avaje.ebean.Ebean;
 
 import models.EMessages;
 import models.data.Language;
 import models.data.Link;
 import models.dbentities.FAQModel;
 
 import play.data.Form;
 
 import models.util.OperationResultInfo;
 import play.i18n.Lang;
 
 import play.mvc.Result;
 import play.mvc.Results;
 import views.html.faq.faq;
 import controllers.EController;
 import controllers.faq.routes;
 
 import views.html.faq.faqManagement;
 import views.html.faq.newFAQForm;
 import views.html.faq.alterFAQForm;
 
 
 /**
  * @author Jens N. Rammant
  * TODO: put link to manageFAQ somewhere (admin control panel)
  */
 public class FAQController extends EController {
 	
 	/**
 	 * @return the FAQ in the correct language if available;
 	 */
 	public static Result getFAQ(){
 		List<Link> breadcrumbs = new ArrayList<Link>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link("FAQ","/FAQ"));
         
         List<FAQModel> f=new ArrayList<FAQModel>();
         String l = EMessages.getLang(); //Retrieve the user's language
         try{
         	//Retrieve all FAQ elements in that language
         	f.addAll(Ebean.find(FAQModel.class).where().eq("language", l).findList());
         }catch(PersistenceException e){
         	//add a message to say something went wrong
        	f.clear();
         	FAQModel temp = new FAQModel();
         	temp.name = EMessages.get("faq.error");
         	f.add(temp);
         }
         if(f.isEmpty()){
         	//add a message to say the FAQ is empty
        	FAQModel temp = new FAQModel();
         	temp.name = EMessages.get("faq.empty");
         	f.add(temp);
         }
         return ok(faq.render(breadcrumbs,f));
   
 	}
 
 	/**
      * This result will redirect to the FAQ list page
      *
      * @return faq list page
      */
 	public static Result list(int page, String orderBy, String order, String filter){
 		//TODO check permissions 
 		
 		List<Link> breadcrumbs = new ArrayList<Link>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link(EMessages.get("faq.managefaq"),"/manageFAQ"));
 		
 		FAQManager fm = new FAQManager();
 		return ok( //TODO try-catch
	            faqManagement.render(fm.page(page, orderBy, order, filter), fm, orderBy, order, filter, breadcrumbs)
 	        );
 	}
 	
 	/**
      * This result will redirect to the create a new FAQ page
      *
      * @return faq creation page
      */
 	public static Result create(){
 		//TODO check permissions
 		
 		List<Link> breadcrumbs = new ArrayList<Link>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link(EMessages.get("faq.managefaq"),"/manageFAQ"));
         breadcrumbs.add(new Link(EMessages.get("faq.addfaq"),"/manageFAQ/new"));
 		
 		Form<FAQModel> form = form(FAQModel.class).bindFromRequest();
 		Map<String,String> languages = new HashMap<String,String>();
 		for (Language l : Language.listLanguages()){
 			languages.put(l.getCode(), l.getName());
 		}
 	    return ok(newFAQForm.render(form, breadcrumbs,languages, new OperationResultInfo()));
 
 		
 	    
 	}
 	/**
 	 * This will save the result from the form and then redirect to the list page
 	 * @return FAQ list page
 	 */
 	public static Result save(){
 		//TODO check permissions
 		
 		List<Link> breadcrumbs = new ArrayList<Link>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link(EMessages.get("faq.managefaq"),"/manageFAQ"));
         breadcrumbs.add(new Link(EMessages.get("faq.addfaq"),"/manageFAQ/new"));
         
         Map<String,String> languages = new HashMap<String,String>();
 		for (Language l : Language.listLanguages()){
 			languages.put(l.getCode(), l.getName());
 		}
 		
 		Form<FAQModel> form = form(FAQModel.class).bindFromRequest();
         if(form.hasErrors()) {        	
         	//Form was not complete.
     		OperationResultInfo ori = new OperationResultInfo();
     		ori.add(EMessages.get("faq.error.notcomplete"), OperationResultInfo.Type.WARNING);
             return badRequest(newFAQForm.render(form, breadcrumbs,languages, ori));
         }
         FAQModel m = form.get();
         try{
         	m.save();
         }catch(Exception p){
         	//Something went wrong in the saving. Redirect back to the create page with an error alert
         	OperationResultInfo ori = new OperationResultInfo();
     		ori.add(EMessages.get("faq.error.savefail"), OperationResultInfo.Type.ERROR);
             return badRequest(newFAQForm.render(form, breadcrumbs,languages, ori));
         }
         return Results.redirect(routes.FAQController.list(0, "name", "asc", ""));
 	}
 	
 	/**
 	 * 
 	 * @param id of the to be altered FAQModel
 	 * @return FAQ alter page
 	 */
 	public static Result edit(String id){
 		//TODO check permissions
 		List<Link> breadcrumbs = new ArrayList<Link>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link(EMessages.get("faq.managefaq"),"/manageFAQ"));
         breadcrumbs.add(new Link(EMessages.get("faq.alter"),"/manageFAQ/"+id));
 		
 		Map<String,String> languages = new HashMap<String,String>();
 		for (Language l : Language.listLanguages()){
 			languages.put(l.getCode(), l.getName());
 		}
 		
 		Form<FAQModel> form = form(FAQModel.class).bindFromRequest().fill((FAQModel) new FAQManager().getFinder().ref(id));
         try{
         	Result r = 
 				ok(alterFAQForm.render(form, breadcrumbs,languages,id, new OperationResultInfo()));
         	return r;
         }catch(Exception e){
         	//TODO
         	return null;
         }
 	}
 	
 	/**
      * This will handle the update of an existing FAQ and redirect
      * to the FAQ list
      *
      * @param id id of the FAQ to be updated
      * @return FAQ list page
      */
     public static Result update(String id){
     	//TODO check permissions
     	List<Link> breadcrumbs = new ArrayList<Link>();
         breadcrumbs.add(new Link("Home", "/"));
         breadcrumbs.add(new Link(EMessages.get("faq.managefaq"),"/manageFAQ"));
         breadcrumbs.add(new Link(EMessages.get("faq.alter"),"/manageFAQ/"+id));
         
         Map<String,String> languages = new HashMap<String,String>();
 		for (Language l : Language.listLanguages()){
 			languages.put(l.getCode(), l.getName());
 		}
    	
         try{
        	Form<FAQModel> form = form(FAQModel.class).fill((FAQModel) new FAQManager().getFinder().byId(id)).bindFromRequest();
         }catch(Exception e){
         	//TODO
         }
         if(form.hasErrors()) {
     		OperationResultInfo ori = new OperationResultInfo();
     		ori.add(EMessages.get("faq.error.notcomplete"),OperationResultInfo.Type.WARNING);
             return badRequest(alterFAQForm.render(form, breadcrumbs,languages,id, ori));
         }
         FAQModel updated = form.get();
         updated.id = Integer.parseInt(id);
         try{
         	updated.update();
         }catch(Exception p){
         	//Something went wrong in the saving. Redirect back to the create page with an error alert
         	OperationResultInfo ori = new OperationResultInfo();
     		ori.add(EMessages.get("faq.error.savefail"), OperationResultInfo.Type.ERROR);
             return badRequest(alterFAQForm.render(form, breadcrumbs,languages, id, ori));
         }
         return redirect(routes.FAQController.list(0, "name", "asc", ""));
     }
 	
 	/**
 	 * This removes a FAQ from the list and then redirects to the FAQ list page
 	 * @param id The ID of the FAQ to be deleted
 	 * @return FAQ list page
 	 */
 	public static Result remove(String id){
 		//TODO check permissions
 		FAQModel fm = (FAQModel) new FAQManager().getFinder().byId(id);
 		//TODO try-catch
         fm.delete();
         return redirect(routes.FAQController.list(0, "language", "asc", ""));
 	}
 	
 	public static boolean isAuthorized(){
 		//TODO
 		return true;
 	}
 }
