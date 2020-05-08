 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Request;
 import models.Tag;
 import models.User;
 import play.data.validation.Valid;
 import play.data.validation.Validation;
 import play.mvc.Controller;
 
 public class Requests extends BaseController
 {
     public static void create() {
 	render();
     }
 
     public static void doCreate(String tags, Request requestItem) {
 	User user = getConnectedUser();
 
 	String[] tagsArr = tags.split(",");
 	List<Tag> tagsList = new ArrayList<Tag>();
 	for (String tagString : tagsArr) {
 	    Tag tag = new Tag(requestItem, tagString.trim());
 	    tagsList.add(tag);
 	}
 	requestItem.tags = tagsList;
 
 	validation.valid(requestItem);
 	if (validation.hasErrors()) {
 	    params.flash();
 	    validation.keep();
 	    create();
 	}
 	requestItem.user = user;
 	requestItem.isFinalized = false;
 	requestItem.save();
 	
 	finalize(requestItem.id);
     }
 
     public static void finalize(Long requestId) {
 	Request requestItem = Request.findById(requestId);
 	render(requestItem);
     }
 
     public static void save(Long requestId) {
 	Request requestItem = Request.findById(requestId);
 	requestItem.isFinalized = true;
 	requestItem.save();
 	show(requestItem.id);
     }
 
     public static void show(Long requestId) {
 	Request requestItem = Request.findById(requestId);
 	render(requestItem);
     }
 
     public static void showDetails(Long id) {
 	User user = getConnectedUser();
 	Request requestItem = Request.findById(id);
 	Boolean someoneElsesRequest = isSomeoneElses(id);
 	render(user, requestItem);
     }
     
     public static void search() {
 	User user = getConnectedUser();
 	List<Request> requests = Request.find("isFinalized", true).fetch();
 	render(user, requests);
     }
 
     public static void edit(Long id) {
    	Request requestItem = Request.findById(id);
    	renderTemplate("Requests/form.html", requestItem);
     }
 
     public static Boolean isSomeoneElses(Long requestId) {
 	User currentUser = getConnectedUser();
 	Request currentRequest = Request.findById(requestId);
 	User owner = currentRequest.user;
 	return !(currentUser.equals(owner));
     }
 
    public static void list() {
	User user = getConnectedUser();
	List<Request> requests = Request.find("user.id", user.id).fetch();
	render(user, requests);
    }

 }
