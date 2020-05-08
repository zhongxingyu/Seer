 package controllers;
 
 import models.PublicMemento;
 import models.User;
 import annotations.CustomRestrict;
 import be.objectify.deadbolt.java.actions.Dynamic;
 import be.objectify.deadbolt.java.actions.Restrict;
 import be.objectify.deadbolt.java.actions.SubjectPresent;
 import delegates.ContextDelegate;
 import enums.MyRoles;
 import enums.ResponseStatus;
 import exceptions.EntityDoesNotExist;
 import exceptions.NotEnoughInformation;
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
 
 	static Form<PublicMementoBean> contributedForm = Form
 			.form(PublicMementoBean.class);
 	static Form<LocationMinimalBean> locationForm = Form
 			.form(LocationMinimalBean.class);
 
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
 		ContextBean result = ContextDelegate.getInstance().getContextForPersonAndDecade(id, decade);
 		if (result != null) {
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context for /person/" + id +" has not yet been created");
 			return notFound(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_PERSON)
 	public static Result getContextForPersonAndDecadeAndCategory(Long id, Long decade, String category) {
 		ContextBean result = ContextDelegate.getInstance().getContextForPersonAndDecadeAndCategory(id, decade, category);
 		if (result != null) {
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context for /person/" + id +" has not yet been created");
 			return notFound(toJson(res));
 		}
 	}
 	
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_PERSON)
 	public static Result initContextForPersonAndDecade(Long id, Long decade) {
 		try {
 			ContextBean result = ContextDelegate.getInstance()
 					.initContextForPersonAndDecade(id, decade);
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
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context with " + cid +" does not exist");
 			return notFound(toJson(res));
 		}
 	}
 
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_CONTEXT)
 	public static Result getContextByIdAndDecade(Long cid, Long decade) {
 		ContextBean result = ContextDelegate.getInstance()
 				.getContextByIdAndDecade(cid, decade);
 
 		if (result != null) {
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context with " + cid +" does not exist");
 			return notFound(toJson(res));
 		}
 	}
 	
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_CONTEXT)
 	public static Result getContextByIdAndDecadeAndCategory(Long cid, Long decade, String cat) {
 		ContextBean result = ContextDelegate.getInstance()
 				.getContextByIdAndDecadeAndCategory(cid, decade, cat);
 		if (result != null) {
 			return ok(toJson(result));
 		} else {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Context with " + cid +" does not exist");
 			return notFound(toJson(res));
 		}
 	}
 		
 	@Dynamic(value = "CuratorOf", meta = SecurityModelConstants.ID_FROM_CONTEXT)
 	public static Result refreshContextForDecadeAndLocation(Long id,
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
 						.refreshContextDecadeAndLocation(id,
 								decade, location);
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
 		Form<PublicMementoBean> filledForm = contributedForm
 				.bindFromRequest();
 		String userEmail = session().get("pa.u.id");
 		User user = User.getByEmail(userEmail);
 		Long id = user.getUserId();
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST,
 					"Body of request misses some information or it is malformed" + filledForm.errors().toString());
 			return badRequest(toJson(res));
 		} else {
 			try {
 				PublicMementoBean contributed = filledForm.get();
 				contributed.setContributor(id);
 				contributed.setContributorType("MEMBER");
 				ContextPublicMementoBean result = ContextDelegate.getInstance()
 						.addMementoToContext(cid, contributed);
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
 	
 	// TODO
 	@SubjectPresent
 	public static Result updateContextMemento(Long cid, Long mid) {
 		Form<PublicMementoBean> filledForm = contributedForm
 				.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST,
 					"Body of request misses some information or it is malformed");
 			return badRequest(toJson(res));
 		} else {
 			try {
 				PublicMementoBean contributed = filledForm.get();
 				PublicMementoBean result = ContextDelegate.getInstance()
 						.updatedContextMemento(cid, mid, contributed);
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
 	public static Result deleteContextMemento(Long cid, Long mid) {
 		try {
 			ContextDelegate.getInstance().deleteContextMemento(cid, mid);
 			ResponseStatusBean res = new ResponseStatusBean(ResponseStatus.OK,
 					"Entity deleted with success");
 			return ok(toJson(res));
 		} catch (Exception e) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.NODATA, "Entity does not exist",
 					e.getMessage());
 			return badRequest(toJson(res));
 		}
 	}
 	
 	@SubjectPresent
 	public static Result createContributedMemento() {
 		Form<PublicMementoBean> filledForm = contributedForm
 				.bindFromRequest();
 		String userEmail = session().get("pa.u.id");
 		User user = User.getByEmail(userEmail);
 		Long id = user.getUserId();
 		
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST,
 					"Body of request misses some information or it is malformed" + filledForm.errors().toString());
 			return badRequest(toJson(res));
 		} else {
 			try {
 				PublicMementoBean contributed = filledForm.get();
 				contributed.setContributor(id);
 				contributed.setContributorType("MEMBER");
 				PublicMemento result = ContextDelegate.getInstance()
 						.createContributedMemento(contributed);
 				ResponseStatusBean res = new ResponseStatusBean(ResponseStatus.OK, "Entity created with success");
 				res.setNewResourceId(result.getPublicMementoId());
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
 		Form<PublicMementoBean> filledForm = contributedForm
 				.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean res = new ResponseStatusBean(
 					ResponseStatus.BADREQUEST,
 					"Body of request misses some information or it is malformed" + filledForm.errors().toString());
 			return badRequest(toJson(res));
 		} else {
 			try {
 				PublicMementoBean contributed = filledForm.get();
 				contributed.setContributorType("EXTERNAL");
 				PublicMemento result = ContextDelegate.getInstance()
 						.createContributedMemento(contributed);
 				ResponseStatusBean res = new ResponseStatusBean(ResponseStatus.OK, "Entity created with success");
 				res.setNewResourceId(result.getPublicMementoId());
 				return ok(toJson(result));
 			} catch (Exception e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.SERVERERROR, e.getLocalizedMessage());
 				return internalServerError(toJson(res));
 			} 
 		}
 	}
 	
 
 	@CustomRestrict(value = {MyRoles.ADMIN}, config = @Restrict({}))
 		public static Result deleteContributedMemento(Long mid) {
 			try {
 				ContextDelegate.getInstance().deleteContributedMemento(mid);
 				ResponseStatusBean res = new ResponseStatusBean(ResponseStatus.OK,
 						"Entity deleted with success");
 				return ok(toJson(res));
 			} catch (Exception e) {
 				ResponseStatusBean res = new ResponseStatusBean(
 						ResponseStatus.NODATA, "Entity does not exist",
 						e.getMessage());
 				return badRequest(toJson(res));
 			}
 		}
 	
 	// TODO
 	public static Result getContextCurators(Long cid) {
 		/** @TODO */
 		return TODO;
 	}
 	// TODO
 	public static Result getContextMemento(Long cid, Long mid) {
 		/** @TODO */
 		return TODO;
 	}
 }
