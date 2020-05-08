 package controllers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
import javax.inject.Inject;

 import models.Demotivator;
 import models.User;
 import net.sf.oval.constraint.MaxLength;
 import play.data.validation.Required;
 import play.data.validation.Validation;
 import play.i18n.Lang;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Http.Header;
 import security.SecureController;
 import services.DemotivatorCreator;
 
 
 public class Application extends Controller {
 	
 	@Inject
 	static DemotivatorCreator creator;
 
 	/**
 	 * If action is annotated with @SecureController, check whether user is
 	 * logged in.
 	 */
 	@Before
 	public static void securityCheck() {
 		if (getActionAnnotation(SecureController.class) != null) {
 			try {
 				Secure.checkAccess();
 			} catch (Throwable e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static void index() {
 		List<Demotivator> demotivators = Demotivator.find("order by id desc").fetch(25);
 		
 		render(demotivators);
 	}
 	
 	public static void single(long id) {
 		Demotivator demotivator = Demotivator.findById(id);
 		
 		if(demotivator == null){
 			index();
 		}
 		
 		render(demotivator);
 	}
 	
 	public static void locale(String language){
 		Lang.change(language);
 		
 		Header referer = request.headers.get("referer");
 	    if(referer == null){
 	        index();
 	    }else{
 	        redirect(referer.value());
 	    }
 	}
 
 	@SecureController
 	public static void add(){		
 		render();
 	}
 
 	@SecureController
 	public static void create(@Required @MaxLength(30) String title, @Required @MaxLength(120) String text, File[] image) {
 
 		validation.required(image[0]); 
 		if (Validation.hasErrors()) {
 			params.flash();
 			Validation.keep();
 			add();
 		}
 
 		String fileName = null;
 		try {
 			fileName = creator.createDemotivator(image[0], title, text);
 		} catch (IOException e) {
 			e.printStackTrace();
 			error(e.getMessage());
 		}
 		
 		//TODO
 		//User user = Security.connectedUser();
 		
 		User user = new User("a@a.com", "pass", "pubsy");
 		user.save();
 		
 		Demotivator d = new Demotivator(title, fileName, user);
 		d.save();
 		
 		single(d.id);
 	}
 }
