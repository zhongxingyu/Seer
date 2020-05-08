 package controllers;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import jobs.IRCMessageJob;
 
 import jobs.RulesNotificationJob;
 import org.apache.commons.lang.StringUtils;
 
 import mails.RegistrationMails;
 import models.Event;
 import models.Participant;
 import play.Logger;
 import play.Play;
 import play.data.validation.Validation;
 import play.libs.Crypto;
 import play.mvc.Controller;
 import play.mvc.Router;
 
 public class Registration extends Controller {
 	public static void embed(String community, String eventId){
 		Event event = Event.findById(eventId);
 		notFoundIfNull(event);
 		
 		render("Registration/embed.js", community, event);
 	}
 
 	public static void register(String community, String eventId, boolean embed, String returnURL){
 		Event event = Event.findById(eventId);
 		notFoundIfNull(event);
 		
 		render(community, event, embed, returnURL);
 	}
 
 	public static void registerParticipant(String community, String eventId, boolean embed, String returnURL, 
 			Participant participant){
 		Event event = Event.findById(eventId);
 		notFoundIfNull(event);
 
 		Validation.required("participant.emailAddress", participant.emailAddress);
 		Validation.email("participant.emailAddress", participant.emailAddress);
 		if(Validation.hasErrors()){
 			params.flash();
 			Validation.keep();
 			register(community, eventId, embed, returnURL);
 		}
 		
 		String emailAddress = participant.emailAddress;
 		String confirmationCode = Crypto.sign(participant.emailAddress);
 
 		Map<String, Object> args = new HashMap<String, Object>();
 		args.put("confirmationCode", confirmationCode);
 		args.put("emailAddress", emailAddress);
 		args.put("community", community);
 		args.put("eventId", eventId);
 		String confirmURL = Router.getFullUrl("Registration.confirm", args);
 		
 		if(!StringUtils.isEmpty(returnURL)){
 			URI uri;
 			try {
 				uri = new URI(returnURL);
 			} catch (URISyntaxException e) {
 				Logger.error(e, "Invalid return url: %s", returnURL);
 				badRequest();
 				return;
 			}
 			returnURL = uri.getScheme()+"://"+uri.getHost();
 			if(uri.getPort() != -1)
 				returnURL += ":"+uri.getPort();
 			returnURL += uri.getPath();
 			String query = uri.getQuery();
 			if(query == null)
 				query = "";
 			else
 				query = query+"&";
 			returnURL += "?"+query+"confirmationCode="+confirmationCode+"&emailAddress="+URLEncoder.encode(emailAddress);
 			if(uri.getFragment() != null)
 				returnURL += "#"+uri.getFragment();
 		}else
 			returnURL = confirmURL;
 		
 		RegistrationMails.confirmEmail(emailAddress, event, returnURL);
		render(community, event, confirmURL, embed, returnURL, emailAddress);
 	}
 	
 	public static void confirm(String community, String eventId, String emailAddress, String confirmationCode,
 			boolean embed){
 		Event event = Event.findById(eventId);
 		notFoundIfNull(event);
 
 		if(!Crypto.sign(emailAddress).equals(confirmationCode)){
 			String error = "Invalid confirmation code";
 			error(error, embed);
 		}
 		
 		Participant existingParticipant = Participant.find("lower(emailAddress) = lower(?)", emailAddress).first();
 		if(existingParticipant != null){
 			if(event.participants.contains(existingParticipant)){
 				String error = "You are already registered for this event";
 				error(error, embed);
 			}
 			flash.put("participant.firstName", existingParticipant.firstName);
 			flash.put("participant.lastName", existingParticipant.lastName);
 			flash.put("participant.company", existingParticipant.company);
 		}
 		
 		render(community, event, confirmationCode, emailAddress, embed);
 	}
 	
 	public static void confirmParticipant(String community, String eventId, String confirmationCode, 
 			boolean embed, Participant participant){
 		Event event = Event.findById(eventId);
 		notFoundIfNull(event);
 
 		Validation.required("participant.emailAddress", participant.emailAddress);
 		Validation.required("participant.firstName", participant.firstName);
 		Validation.required("participant.lastName", participant.lastName);
 		Validation.required("participant.company", participant.company);
 		if(Validation.hasErrors()){
 			params.flash();
 			Validation.keep();
 			confirm(community, eventId, participant.emailAddress, confirmationCode, embed);
 		}
 
 		if(!event.canStillRegister()){
 			String error = "Event is closed, try to register early next time ;)";
 			error(error, embed);
 		}
 		
 		// we must check this again here
 		if(!Crypto.sign(participant.emailAddress).equals(confirmationCode)){
 			String error = "Invalid confirmation code";
 			error(error, embed);
 		}
 
 		Participant existingParticipant = Participant.find("lower(emailAddress) = lower(?)", participant.emailAddress).first();
 		if(existingParticipant != null){
 			if(event.participants.contains(existingParticipant)){
 				String error = "You are already registered for this event";
 				error(error, embed);
 			}
 			existingParticipant.firstName = participant.firstName;
 			existingParticipant.lastName = participant.lastName;
 			existingParticipant.company = participant.company;
 			existingParticipant.comments = participant.comments;
 			participant = existingParticipant;
 		}
 		participant.events.add(event);
 		participant.tags.addAll(event.tags);
 		participant.save();
 		event.participants.add(participant);
 		event.save();
 
 		notify(participant, event);
 		
 		render(event, embed);
 	}
 
 	private static void notify(final Participant participant, final Event event) {
 		final String rulesProperty = Play.configuration.getProperty("notification.rules", "false");
 		final boolean doRules = "true".equals(rulesProperty);
 
 		Logger.info(String.format("notification.rules=[%s]", rulesProperty));
 		if (doRules)
 			notifyRulesEngine(participant, event);
 		else
 			sendIRCMessage(participant, event);
 	}
 
 	private static void sendIRCMessage(final Participant participant, final Event event) {
 		IRCMessageJob ircMessageJob = new IRCMessageJob(participant.firstName+" "+participant.lastName
 			+" ("+participant.emailAddress
 			+"), from "+participant.company+", has registered for "+event.title+", which now has "
 			+event.participants.size()+" participant(s)");
 		ircMessageJob.now();
 	}
 
 	private static void notifyRulesEngine(final Participant participant, final Event event) {
 		RulesNotificationJob rulesNotificationJob = new RulesNotificationJob (participant, event);
 		rulesNotificationJob.now();
 	}
 
 	public static void error(String message, boolean embed){
 		render(message, embed);
 	}
 	
 	public static void test(String community, String eventId){
 		render(community, eventId);
 	}
 }
