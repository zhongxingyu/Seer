 package controllers;
 
 import java.util.Iterator;
 
 import org.openrdf.repository.RepositoryException;
 
 import global.AssociatedPage;
 import global.CurrentRequest;
 import global.Page;
 
 import models.Hunt;
 import models.User;
 
 import pages.AdminHuntEditPage;
 import pages.AdminUserEditPage;
 import play.data.Form;
 import play.mvc.*;
 import repository.HuntRepository;
 import repository.RoleRepository;
 import repository.UserRepository;
 
 public class AdminPanelController extends Controller {
 
 	public static Page currentPage = null;
 
 	/***** HUNTS *****/
 
 	@AssociatedPage("adminhuntlist")
 	public static Result huntlist() throws Exception {
 		return ok(views.html.adminpanel.huntlist.render(HuntRepository.getAll()));
 	}
 
 	@AssociatedPage("adminhuntedit")
 	public static Result huntedit(String hid) throws Exception {
 		Hunt hunt = HuntRepository.get(hid);
 		if (hunt == null) {
 			return notFound();
 		}
 
 		forms.Hunt formHuntEdit = new forms.Hunt();
 
 		formHuntEdit.label = hunt.getLabel();
 		formHuntEdit.description = hunt.getDescription();
 		formHuntEdit.level = hunt.getLevel();
 		formHuntEdit.area = hunt.getArea().toTemplateString();
		formHuntEdit.language = hunt.getLanguage();
 
 		Iterator<models.Tag> it = hunt.getTags().iterator();
 		if (it.hasNext()) {
 			models.Tag firstTag = it.next();
 			formHuntEdit.tags = firstTag.getName();
 
 			while (it.hasNext()) {
 				formHuntEdit.tags += ", " + it.next().getName();
 			}
 		}
 
 		((AdminHuntEditPage) CurrentRequest.page()).setMenuParameters(hunt);// Menu's
 																			// parameters
 		return ok(views.html.adminpanel.editHunt.render(hunt, form(forms.Hunt.class).fill(formHuntEdit)));
 	}
 
 	@AssociatedPage("adminhuntedit")
 	public static Result submitHuntEditForm(String hid) throws Exception {
 		Hunt hunt = HuntRepository.get(hid);
 		if (hunt == null) {
 			return notFound();
 		}
 
 		Form<forms.Hunt> formHuntEdit = form(forms.Hunt.class).bindFromRequest();
 
 		if (formHuntEdit.hasErrors()) {
 			((AdminHuntEditPage) CurrentRequest.page()).setMenuParameters(hunt);// Menu's
 																				// parameters
 			return badRequest(views.html.adminpanel.editHunt.render(hunt, formHuntEdit));
 
 		} else {
 			HuntController.fillHunt(hunt, formHuntEdit.get());
 			// forms.AdmHuntEdit form = formHuntEdit.get();
 
 			hunt.save();
 
 			return redirect(routes.AdminPanelController.huntlist());
 		}
 	}
 	
 	@AssociatedPage("adminhuntedit")
 	public static Result submitHuntDelete(String hid) throws RepositoryException{
 		Hunt hunt = HuntRepository.get(hid);
 		if (hunt == null) {
 			return notFound();
 		}
 		hunt.delete();
 		//HuntController.delete(hid);
 		return redirect(routes.AdminPanelController.huntlist());	
 	}
 	
 	@AssociatedPage("adminhome")
 	public static Result home() {
 		return ok(views.html.adminpanel.home.render(UserRepository.getAll(), HuntRepository.getAll()));
 	}
 
 	/***** USERS *****/
 
 	@AssociatedPage("adminuserlist")
 	public static Result userlist() throws Exception {
 		// return forbidden("THIS IS A TEST ABOUT A FIRBIDDEN PAGE.");
 		return ok(views.html.adminpanel.userlist.render(UserRepository.getAll()));
 	}
 
 	@AssociatedPage("adminuseredit")
 	public static Result useredit(String uid) throws Exception {
 		User user = UserRepository.get(uid);
 		if (user == null) {
 			return notFound();
 		}
 		// Form<AdmUserEdit> formUserEdit = form(AdmUserEdit.class);
 		forms.AdmUserEdit formUserEdit = new forms.AdmUserEdit();
 		// System.out.println(formUserEdit);
 		formUserEdit.roleName = user.getValidRole().getName();
 		((AdminUserEditPage) CurrentRequest.page()).setMenuParameters(user);// Menu's
 																			// parameters
 		return ok(views.html.adminpanel.editUser.render(user, form(forms.AdmUserEdit.class).fill(formUserEdit)));
 	}
 
 	@AssociatedPage("adminuseredit")
 	public static Result submitUserEditForm(String uid) throws Exception {
 		User user = UserRepository.get(uid);
 		if (user == null) {
 			return notFound();
 		}
 
 		Form<forms.AdmUserEdit> formUserEdit = form(forms.AdmUserEdit.class).bindFromRequest();
 
 		if (formUserEdit.hasErrors()) {
 			((AdminUserEditPage) CurrentRequest.page()).setMenuParameters(user);// Menu's
 																				// parameters
 			return badRequest(views.html.adminpanel.editUser.render(user, formUserEdit));
 
 		} else {
 			forms.AdmUserEdit form = formUserEdit.get();
 
 			user.setRole(RoleRepository.get(form.roleName));
 			user.save();
 
 			return redirectToMain();
 		}
 	}
 
 	public static Result redirectToMain() {
 		return redirect(routes.AdminPanelController.userlist());
 	}
 }
