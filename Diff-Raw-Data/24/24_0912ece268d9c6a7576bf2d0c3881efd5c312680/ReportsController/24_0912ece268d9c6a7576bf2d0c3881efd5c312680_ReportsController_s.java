 package controllers.com.contactmanager;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import models.com.contactmanager.Profile;
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.contacts;
 
 public class ReportsController extends Controller {
 
 	public static Result fetchAllContacts() {
 		List<Profile> profiles = Profile.findAll();
 		String output = "";
 		for (Profile profile : profiles) {
 			Logger.info("Generating output for contact id " + profile.id);
 			output += "<tr>";
 			output += "<td>" + profile.id + "</td>";
 			output += "<td>" + profile.name + "</td>";
 			output += "<td>" + profile.login_name + "</td>";
 			output += "<td>" + profile.title + "</td>";
 			output += "<td>" + formatDate(profile.joined_date) + "</td>";
 			output += "<td>" + formatDate(profile.last_attended_date) + "</td>";
 			output += "<td>" + formatDate(profile.last_visited_date) + "</td>";
 			output += "<td>" + (profile.is_intro ? "Yes" : "No") + "</td>";
 			output += "<td>" + (profile.is_photo ? "Yes" : "No") + "</td>";
			output += "<td>" + (profile.is_assistant_organizer ? "Yes" : "No") + "</td>";
 			output += "<td>" + profile.mailing_list_type + "</td>";
 			output += "<td>" + profile.url + "</td>";
 			output += "</tr>";
 		}
 		// Html html new Html(output);
 		return ok(contacts.render(output));
 	}
 
 	private static String formatDate(Date date) {
 		String formattedDate = "";
 		if (date != null) {
			SimpleDateFormat dt = new SimpleDateFormat("mm/dd/yyyy");
			formattedDate = dt.format(date);
 		}
 
 		return formattedDate;
 	}
 
 }
