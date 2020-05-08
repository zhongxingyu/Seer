 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Context;
 import models.PublicMemento;
 import models.User;
 import annotations.CustomRestrict;
 import be.objectify.deadbolt.java.actions.Dynamic;
 import be.objectify.deadbolt.java.actions.Restrict;
 import be.objectify.deadbolt.java.actions.SubjectPresent;
 import delegates.ContextDelegate;
 import delegates.UtilitiesDelegate;
 import enums.LogActions;
 import enums.MyRoles;
 import enums.ResponseStatus;
 import exceptions.EntityDoesNotExist;
 import exceptions.NotEnoughInformation;
 import play.Play;
 import play.data.Form;
 import play.mvc.*;
 import pojos.ContextBean;
 import pojos.ContextPublicMementoBean;
 import pojos.LocationMinimalBean;
 import pojos.PublicMementoBean;
 import pojos.ResponseStatusBean;
 import security.SecurityModelConstants;
 import static play.libs.Json.toJson;
 
 public class ContextControl extends Controller {
 
 	static Form<ContextPublicMementoBean> contextMementoForm = Form
 			.form(ContextPublicMementoBean.class);
 	static Form<PublicMementoBean> contributedForm = Form
 			.form(PublicMementoBean.class);
 	static Form<LocationMinimalBean> locationForm = Form
 			.form(LocationMinimalBean.class);
 
 	public static List<String> categoryOptions() {
 		List<String> tmp = new ArrayList<String>();
 
 		tmp.add("SONG");
 		tmp.add("PEOPLE");
 		tmp.add("STORY");
 		tmp.add("TV");
 		tmp.add("FILM");
 		tmp.add("PICTURE");
 		// tmp.add("ARTWORK");
 		// tmp.add("BOOK");
 		// tmp.add("OBJEC");
 		return tmp;
 	}
 	
 	public static List<String> resourceTypeOptions(){
         List<String> tmp = new ArrayList<String>();
 
         tmp.add("IMAGE");
         tmp.add("VIDEO");
         tmp.add("AUDIO");
         tmp.add("TEXT");
         tmp.add("HTML");
         //tmp.add("ARTWORK");
         //tmp.add("BOOK");
         //tmp.add("OBJEC");
         return tmp;
     }
 
 	/**
 	 * Serves the context for the logged user (if exist) or initialize one if it
 	 * does not exist
 	 * 
 	 * @return
 	 */
 	@SubjectPresent
 	public static Result getContext() {
 		String userEmail = session().get("pa.u.id");
 		User user = User.getByEmail(userEmail);
 		Long id = user.getPersonId();
 		// get context for person
 		ContextBean result = ContextDelegate.getInstance().getContextForPerson(
 				id);
 
 		if (result != null) {
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_READ.toString(), request().path());
 			}
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context for " + userEmail
 							+ " has not yet been created");
 			return notFound(toJson(res));
 		}
 	}
 
 	/**
 	 * Initialize a contextual collection for logged user
 	 * 
 	 * @return
 	 */
 	@SubjectPresent
 	public static Result initContext() {
 		String userEmail = session().get("pa.u.id");
 		User user = User.getByEmail(userEmail);
 		Long id = user.getPersonId();
 		try {
 			ContextBean result = ContextDelegate.getInstance()
 					.initContextForPerson(id);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_INIT.toString(), request().path());
 			}
 			return ok(toJson(result));
 		} catch (EntityDoesNotExist e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, e.getLocalizedMessage());
 			return internalServerError(toJson(res));
 		} catch (NotEnoughInformation e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST, e.getLocalizedMessage());
 			return badRequest(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_PERSON)
 	public static Result getContextForPerson(Long id) {
 		// get context for person
 		ContextBean result = ContextDelegate.getInstance().getContextForPerson(
 				id);
 
 		if (result != null) {
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_READ.toString(), request().path());
 			}
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context for /person/" + id
 							+ " has not yet been created");
 			return notFound(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_PERSON)
 	public static Result initContextForPerson(Long id) {
 		try {
 			ContextBean result = ContextDelegate.getInstance()
 					.initContextForPerson(id);
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_INIT.toString(), request().path());
 			}
 			return ok(toJson(result));
 		} catch (EntityDoesNotExist e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, e.getLocalizedMessage());
 			return internalServerError(toJson(res));
 		} catch (NotEnoughInformation e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST, e.getLocalizedMessage());
 			return badRequest(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_PERSON)
 	public static Result getContextForPersonAndDecade(Long id, Long decade) {
 		ContextBean result = ContextDelegate.getInstance()
 				.getContextForPersonAndDecade(id, decade);
 		if (result != null) {
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_READ.toString(), request().path());
 			}
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context for /person/" + id
 							+ " has not yet been created");
 			return notFound(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_PERSON)
 	public static Result getContextForPersonAndDecadeAndCategory(Long id,
 			Long decade, String category) {
 		ContextBean result = ContextDelegate.getInstance()
 				.getContextForPersonAndDecadeAndCategory(id, decade, category);
 		if (result != null) {
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_READ.toString(), request().path());
 			}
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context for /person/" + id
 							+ " has not yet been created");
 			return notFound(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_PERSON)
 	public static Result initContextForPersonAndDecade(Long id, Long decade) {
 		try {
 			ContextBean result = ContextDelegate.getInstance()
 					.initContextForPersonAndDecade(id, decade);
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_INIT.toString(), request().path());
 			}
 			return ok(toJson(result));
 		} catch (EntityDoesNotExist e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, e.getLocalizedMessage());
 			return internalServerError(toJson(res));
 		} catch (NotEnoughInformation e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST, e.getLocalizedMessage());
 			return badRequest(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_PERSON)
 	public static Result initContextForPersonAndDecadeAndLocation(Long id,
 			Long decade) {
 		Form<LocationMinimalBean> filledForm = locationForm.bindFromRequest();
 
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST,
 					"Body of request misses some information or it is malformed");
 			return badRequest(toJson(res));
 		} else {
 
 			LocationMinimalBean location = filledForm.get();
 			try {
 				ContextBean result = ContextDelegate.getInstance()
 						.initContextForPersonAndDecadeAndLocation(id, decade,
 								location);
 				String userEmail = session().get("pa.u.id");
 				User user = User.getByEmail(userEmail);
 				// Log Action
 				int logAction = Play.application().configuration()
 						.getInt("log.actions");
 				if (logAction == 1) {
 					UtilitiesDelegate.getInstance().logActivity(user,
 							LogActions.CONTEXT_INIT.toString(),
 							request().path());
 				}
 				return ok(toJson(result));
 			} catch (EntityDoesNotExist e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.NODATA, e.getLocalizedMessage());
 				return internalServerError(toJson(res));
 			} catch (NotEnoughInformation e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.BADREQUEST, e.getLocalizedMessage());
 				return badRequest(toJson(res));
 			}
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_PERSON)
 	public static Result initContextForPersonAndDecadeAndCity(Long id,
 			Long decade, Long cityId) {
 		try {
 			ContextBean result = ContextDelegate.getInstance()
 					.initContextForPersonAndDecadeAndCity(id, decade, cityId);
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_INIT.toString(), request().path());
 			}
 			return ok(toJson(result));
 		} catch (EntityDoesNotExist e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, e.getLocalizedMessage());
 			return internalServerError(toJson(res));
 		} catch (NotEnoughInformation e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST, e.getLocalizedMessage());
 			return badRequest(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_CONTEXT)
 	public static Result getContextById(Long cid) {
 		ContextBean result = ContextDelegate.getInstance().getContext(cid);
 		if (result != null) {
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_READ.toString(), request().path());
 			}
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context with " + cid
 							+ " does not exist");
 			return notFound(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_CONTEXT)
 	public static Result getContextByIdAndDecade(Long cid, Long decade) {
 		ContextBean result = ContextDelegate.getInstance()
 				.getContextByIdAndDecade(cid, decade);
 
 		if (result != null) {
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_READ.toString(), request().path());
 			}
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context with " + cid
 							+ " does not exist");
 			return notFound(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_CONTEXT)
 	public static Result getContextByIdAndDecadeAndCategory(Long cid,
 			Long decade, String cat) {
 		ContextBean result = ContextDelegate.getInstance()
 				.getContextByIdAndDecadeAndCategory(cid, decade, cat);
 		if (result != null) {
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_READ.toString(), request().path());
 			}
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context with " + cid
 							+ " does not exist");
 			return notFound(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_CONTEXT)
 	public static Result refreshContextForDecadeAndLocation(Long id, Long decade) {
 		Form<LocationMinimalBean> filledForm = locationForm.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST,
 					"Body of request misses some information or it is malformed");
 			return badRequest(toJson(res));
 		} else {
 			LocationMinimalBean location = filledForm.get();
 			try {
 				ContextBean result = ContextDelegate.getInstance()
 						.refreshContextDecadeAndLocation(id, decade, location);
 				String userEmail = session().get("pa.u.id");
 				User user = User.getByEmail(userEmail);
 				// Log Action
 				int logAction = Play.application().configuration()
 						.getInt("log.actions");
 				if (logAction == 1) {
 					UtilitiesDelegate.getInstance().logActivity(user,
 							LogActions.CONTEXT_REFRESH.toString(),
 							request().path());
 				}
 				return ok(toJson(result));
 			} catch (EntityDoesNotExist e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.NODATA, e.getLocalizedMessage());
 				return internalServerError(toJson(res));
 			} catch (NotEnoughInformation e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.BADREQUEST, e.getLocalizedMessage());
 				return badRequest(toJson(res));
 			}
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_CONTEXT)
 	public static Result refreshContextForDecadeAndCity(Long cid, Long decade,
 			Long cityId) {
 		try {
 			ContextBean result = ContextDelegate.getInstance()
 					.refreshContextForDecadeAndCity(cid, decade, cityId);
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance()
 						.logActivity(user,
 								LogActions.CONTEXT_REFRESH.toString(),
 								request().path());
 			}
 			return ok(toJson(result));
 		} catch (EntityDoesNotExist e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, e.getLocalizedMessage());
 			return internalServerError(toJson(res));
 		} catch (NotEnoughInformation e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST, e.getLocalizedMessage());
 			return badRequest(toJson(res));
 		}
 	}
 
 	// TODO
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_CONTEXT)
 	public static Result deleteContext(Long cid) {
 		try {
 			ContextDelegate.getInstance().deleteContext(cid);
 			ResponseStatusBean res = new ResponseStatusBean(ResponseStatus.OK,
 					"Entity deleted with success");
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.CONTEXT_DELETE.toString(), request().path());
 			}
 			return ok(toJson(res));
 		} catch (Exception e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Entity does not exist",
 					e.getMessage());
 			return badRequest(toJson(res));
 		}
 	}
 
 	// TODO
 
 	@SubjectPresent
 	public static Result createContextMemento(Long cid) {
 		Form<ContextPublicMementoBean> filledForm = contextMementoForm
 				.bindFromRequest();
 		String userEmail = session().get("pa.u.id");
 		User user = User.getByEmail(userEmail);
 		Long id = user.getUserId();
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST,
 					"Body of request misses some information or it is malformed"
 							+ filledForm.errors().toString());
 			return badRequest(toJson(res));
 		} else {
 			try {
 				ContextPublicMementoBean contributed = filledForm.get();
 				PublicMementoBean p = contributed.getPublicMemento();
 				if (p != null) {
 					p.setContributor(id);
 					p.setContributorType("MEMBER");
 					ContextPublicMementoBean result = ContextDelegate
 							.getInstance()
 							.addMementoToContext(cid, contributed);
 
 					// Log Action
 					int logAction = Play.application().configuration()
 							.getInt("log.actions");
 					if (logAction == 1) {
 						UtilitiesDelegate.getInstance().logActivity(user,
 								LogActions.PUBLIC_MEMENTO_NEW.toString(),
 								request().path());
 					}
 					return ok(toJson(result));
 				} else {
 					ResponseStatusBean res = new ResponseStatusBean(
 							ResponseStatus.BADREQUEST,
 							"No memento in data sent");
 					return badRequest(toJson(res));
 				}
 			} catch (EntityDoesNotExist e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.NODATA, e.getLocalizedMessage());
 				return internalServerError(toJson(res));
 			} catch (NotEnoughInformation e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.BADREQUEST, e.getLocalizedMessage());
 				return badRequest(toJson(res));
 			}
 		}
 	}
 
 	// TODO
 	@SubjectPresent
 	public static Result updateContextMemento(Long cid, Long mid) {
 		Form<ContextPublicMementoBean> filledForm = contextMementoForm
 				.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST,
 					"Body of request misses some information or it is malformed ==> "
 							+ filledForm.errors().toString());
 			return badRequest(toJson(res));
 		} else {
 			try {
 				ContextPublicMementoBean contributed = filledForm.get();
 				PublicMementoBean p = contributed.getPublicMemento();
 				if (p != null) {
 					p.setPublicMementoId(mid);
 					ContextPublicMementoBean result = ContextDelegate
 							.getInstance().updateContextMemento(cid, mid,
 									contributed);
 					String userEmail = session().get("pa.u.id");
 					User user = User.getByEmail(userEmail);
 					// Log Action
 					int logAction = Play.application().configuration()
 							.getInt("log.actions");
 					if (logAction == 1) {
 						UtilitiesDelegate.getInstance().logActivity(user,
 								LogActions.PUBLIC_MEMENTO_MODIFY.toString(),
 								request().path());
 					}
 					return ok(toJson(result));
 				} else {
 					ResponseStatusBean res = new ResponseStatusBean(
 							ResponseStatus.BADREQUEST,
 							"No memento in data sent");
 					return badRequest(toJson(res));
 				}
 			} catch (EntityDoesNotExist e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.NODATA, e.getLocalizedMessage());
 				return internalServerError(toJson(res));
 			} catch (NotEnoughInformation e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.BADREQUEST, e.getLocalizedMessage());
 				return badRequest(toJson(res));
 			}
 		}
 	}
 
 	// TODO
 	@SubjectPresent
 	public static Result updatePublicMemento(Long mid) {
 		Form<PublicMementoBean> filledForm = contributedForm.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST,
 					"Body of request misses some information or it is malformed"
 							+ filledForm.errors().toString());
 			return badRequest(toJson(res));
 		} else {
 			try {
 				PublicMementoBean contributed = filledForm.get();
 				contributed.setPublicMementoId(mid);
 				PublicMementoBean result = ContextDelegate.getInstance()
 						.updatePublicMemento(mid, contributed);
 				String userEmail = session().get("pa.u.id");
 				User user = User.getByEmail(userEmail);
 				// Log Action
 				int logAction = Play.application().configuration()
 						.getInt("log.actions");
 				if (logAction == 1) {
 					UtilitiesDelegate.getInstance().logActivity(user,
 							LogActions.PUBLIC_MEMENTO_MODIFY.toString(),
 							request().path());
 				}
 				return ok(toJson(result));
 			} catch (EntityDoesNotExist e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.NODATA, e.getLocalizedMessage());
 				return internalServerError(toJson(res));
 			} catch (NotEnoughInformation e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.BADREQUEST, e.getLocalizedMessage());
 				return badRequest(toJson(res));
 			}
 		}
 	}
 
 	@SubjectPresent
 	public static Result getPublicMemento(Long mid) {
 		try {
 			PublicMementoBean result = ContextDelegate.getInstance()
 					.getPublicMemento(mid);
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.PUBLIC_MEMENTO_READ.toString(),
 						request().path());
 			}
 			return ok(toJson(result));
 		} catch (Exception e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, e.getLocalizedMessage());
 			return internalServerError(toJson(res));
 		}
 	}
 
 	@SubjectPresent
 	public static Result getContextMemento(Long cid, Long mid) {
 		try {
 			ContextPublicMementoBean result = ContextDelegate.getInstance()
 					.getContextMemento(cid, mid);
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.PUBLIC_MEMENTO_READ.toString(),
 						request().path());
 			}
 			return ok(toJson(result));
 		} catch (Exception e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, e.getLocalizedMessage());
 			return internalServerError(toJson(res));
 		}
 	}
 
 	@SubjectPresent
 	public static Result deleteContextMemento(Long cid, Long mid) {
 		try {
 			ContextDelegate.getInstance().deleteContextMemento(cid, mid);
 			ResponseStatusBean res = new ResponseStatusBean(ResponseStatus.OK,
 					"Entity deleted with success");
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.PUBLIC_MEMENTO_DELETE.toString(),
 						request().path());
 			}
 			return ok(toJson(res));
 		} catch (Exception e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Entity does not exist",
 					e.getMessage());
 			return badRequest(toJson(res));
 		}
 	}
 
 	public static Result createContributedMemento() {
 		Form<PublicMementoBean> filledForm = contributedForm.bindFromRequest();
 		Long id = new Long(1);	
 		User user = null;
 		String contributorType = "ANONYMOUS";
 		String userEmail = session().get("pa.u.id");
 		if (userEmail != null && !userEmail.equals("")) {
 			user = User.getByEmail(userEmail);
 			id = user.getUserId();	
 			contributorType = "MEMBER";
 		} else {
 			user = User.read(id);
 		}
 
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST,
 					"Body of request misses some information or it is malformed"
 							+ filledForm.errors().toString());
 			return badRequest(toJson(res));
 		} else {
 			try {
 				PublicMementoBean contributed = filledForm.get();
 				contributed.setContributor(id);
 				contributed.setContributorType(contributorType);
 				PublicMemento result = ContextDelegate.getInstance()
 						.createContributedMemento(contributed);
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.OK, "Entity created with success");
 				res.setNewResourceId(result.getPublicMementoId());
 				// Log Action
 				int logAction = Play.application().configuration()
 						.getInt("log.actions");
 				if (logAction == 1) {
 					UtilitiesDelegate.getInstance().logActivity(user,
 							LogActions.PUBLIC_MEMENTO_NEW.toString(),
 							request().path());
 				}
 				return ok(toJson(result));
 			} catch (Exception e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.SERVERERROR, e.getLocalizedMessage());
 				return internalServerError(toJson(res));
 			}
 		}
 	}
 
 	// TODO how to verify that this is used wisely?
 	public static Result createAnonymousContributedMemento() {
 		Form<PublicMementoBean> filledForm = contributedForm.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST,
 					"Body of request misses some information or it is malformed"
 							+ filledForm.errors().toString());
 			return badRequest(toJson(res));
 		} else {
 			try {
 				PublicMementoBean contributed = filledForm.get();
 				contributed.setContributorType("EXTERNAL");
 				PublicMemento result = ContextDelegate.getInstance()
 						.createContributedMemento(contributed);
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.OK, "Entity created with success");
 				res.setNewResourceId(result.getPublicMementoId());
 				// Log Action
 				int logAction = Play.application().configuration()
 						.getInt("log.actions");
 				if (logAction == 1) {
 					UtilitiesDelegate.getInstance().logActivity(null,
 							LogActions.PUBLIC_MEMENTO_NEW.toString(),
 							request().path());
 				}
 				return ok(toJson(result));
 			} catch (Exception e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.SERVERERROR, e.getLocalizedMessage());
 				return internalServerError(toJson(res));
 			}
 		}
 	}
 
 	@CustomRestrict(value = { MyRoles.ADMIN }, config = @Restrict({}))
 	public static Result deleteContributedMemento(Long mid) {
 		try {
 			ContextDelegate.getInstance().deleteContributedMemento(mid);
 			ResponseStatusBean res = new ResponseStatusBean(ResponseStatus.OK,
 					"Entity deleted with success");
 			String userEmail = session().get("pa.u.id");
 			User user = User.getByEmail(userEmail);
 			// Log Action
 			int logAction = Play.application().configuration()
 					.getInt("log.actions");
 			if (logAction == 1) {
 				UtilitiesDelegate.getInstance().logActivity(user,
 						LogActions.PUBLIC_MEMENTO_DELETE.toString(),
 						request().path());
 			}
 			return ok(toJson(res));
 		} catch (Exception e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.SERVERERROR,
 					"Entity does not exist or a constraint failed",
 					e.getMessage());
 			return badRequest(toJson(res));
 		}
 	}
 	
 	public static Result contribute() {
		return ok(views.html.uploadpublicmemento.render(null));
 	}
 
 	public static Result contributeToContext(String code) {
		Long contextId = ContextDelegate.getInstance().getContextIdByHashCode(code);
		return ok(views.html.uploadpublicmemento.render(contextId));
 	}
 
 	public static Result contributeMain() {
		return ok(views.html.main.render("","",views.html.uploadpublicmemento.render(null)));
 	}
 
 	public static Result addContextMementoView(Long cid) {
 		/** @TODO */
 		return TODO;
 	}
 
 	public static Result addContextMementoDetailViews(Long cid) {
 		/** @TODO */
 		return TODO;
 	}
 
 	public static Result countContextMementoEmotion(Long cid) {
 		/** @TODO */
 		return TODO;
 	}
 
 	// TODO
 	public static Result getContextCurators(Long cid) {
 		/** @TODO */
 		return TODO;
 	}
 }
