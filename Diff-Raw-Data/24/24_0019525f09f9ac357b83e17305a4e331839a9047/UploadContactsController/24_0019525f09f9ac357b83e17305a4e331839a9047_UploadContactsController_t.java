 package controllers.com.contactmanager;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import models.com.contactmanager.Company;
 import models.com.contactmanager.Profile;
 import models.com.contactmanager.Rsvp;
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Result;
 import util.com.contactmanager.InputFileReader;
 import viewobject.com.contactmanager.User;
 import views.html.upload;
 import exception.com.contactmanager.ApplicationException;
 
 public class UploadContactsController extends Controller {
 
 	public static Result index() {
 
 		// return ok(index.render("Output:" + output));
 		return ok(upload.render(""));
 
 	}
 
 	public static Result uploadCsv() {
 		String error = "";
 		MultipartFormData body = request().body().asMultipartFormData();
 		FilePart usersFile = body.getFile("usersfile");
 		File file = null;
 		if (usersFile != null) {
 			String fileName = usersFile.getFilename();
 			Logger.info("File uploaded: " + fileName);
 			String contentType = usersFile.getContentType();
 			file = usersFile.getFile();
 		}
 		List<User> users = new ArrayList<User>();
 		try {
 			users = InputFileReader.getUserList(file);
 		} catch (ApplicationException e) {
 			error = e.getMessage();
 		}
 
 		if (error.length() > 0) {
 			return ok(upload.render(error));
 		} else {
 			error = saveUsers(users);
 		}
 
 		return ok(upload.render(error));
 	}
 
 	private static String saveUsers(List<User> users) {
 		String message = "File loaded!";
 		List<Company> existingCompanies = Company.findAll();
 		List<Profile> exisitingContacts = Profile.findAll();
 		for (User user : users) {
 
 			Company company = new Company(user.company);
 			if (!existingCompanies.contains(company)) {
 				existingCompanies.add(company);
				 	company.save();
 			} else {
 				company = existingCompanies.get(existingCompanies
 						.indexOf(company));
 			}
 
 			Profile profile = new Profile(user.member_id);
 			Rsvp rsvp = new Rsvp();
 			if (!exisitingContacts.contains(profile)) {
 				//exisitingContacts.add(profile);
 				// company.save();
 			} else {
 				profile = exisitingContacts.get(exisitingContacts
 						.indexOf(profile));
 				rsvp.rsvp_id = profile.rsvp.rsvp_id;
 			}
 
 			rsvp.total = user.rsvp_total;
 			rsvp.yes = user.rsvp_yes;
 			rsvp.no = user.rsvp_no;
 			rsvp.maybe = user.rsvp_maybe;
 			rsvp.meetups_attended = user.meetups_attended;
 			rsvp.no_shows = user.no_shows;
 			// rsvp.save();
 
 			// profile.id = user.member_id;
 			profile.login_name = user.user_id;
 			profile.title = user.title;
 			profile.name = user.name;
 			profile.joined_date = user.joined_date;
 			profile.last_attended_date = user.last_attended_date;
 			profile.last_visited_date = user.last_visited_date;
 			profile.is_intro = user.is_intro;
 			profile.is_assistant_organizer = user.is_assistant_organizer;
 			profile.is_photo = user.is_photo;
 			profile.url = user.url;
 			profile.mailing_list_type = user.mailing_list_type;
 			profile.rsvp = rsvp;
 			profile.company = company;
 			try {
 				if (exisitingContacts.contains(profile)) {
 					Profile.update(profile);
 				} else {
 					Profile.save(profile);
 					exisitingContacts.add(profile);
 				}
 			} catch (Exception e) {
 				Logger.error(e.getMessage());
 				// rsvp.delete();
 				// company.delete();
 				message = "Error loading file. Please check logs";
 			}
 		}
 		return message;
 	}
 } // class
