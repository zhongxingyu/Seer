 package controllers;
 import java.util.LinkedList;
 import java.util.List;
 
 import play.mvc.Controller;
 import api.entities.ChoiceJSON;
 import api.entities.PollJSON;
 import api.helpers.GsonHelper;
 import api.requests.CreatePollRequest;
 import api.responses.CreatePollResponse;
 
 public class CreatePoll extends Controller {
 
 	public static void index(String question, String[] answer) {
 		if (!LoginUser.isLoggedIn()) {
 			LoginUser.forward = "createpoll";
 			LoginUser.index("");
 		}
 		String choices;
 		if (answer == null) {
 			answer = new String[] { "", "" };
 		}
 		List<ChoiceJSON> choices_json = new LinkedList<ChoiceJSON>();
 		for(String c: answer) {
 			ChoiceJSON cJSON = new ChoiceJSON();
 			cJSON.text = c;
 			choices_json.add(cJSON);
 		}
 		choices = GsonHelper.toJson(choices_json);
 		render(question, answer, choices);
 
 	}
 
 	public static void success(String token) {
 		render(token);
 	}
 
 	public static void submit(String question, String[] answer, String type, Boolean loginRequired) {
 		if (!LoginUser.isLoggedIn()) {
 			LoginUser.forward = "createpoll";
 			LoginUser.index("");
 		}
 				
 		// Remove empty lines from the answers
 		List<ChoiceJSON> choicesJson = new LinkedList<ChoiceJSON>();
 		if (answer != null) {
 			for (String s: answer) {
 				if (s != null && !s.isEmpty()) {
 					ChoiceJSON choice = new ChoiceJSON();
 					choice.text = s;
 					choicesJson.add(choice);
 				}
 			}
 		}
 
 		// Validate that the question and answers are there.
 		validation.required(question);
 		validation.required(type);
 
 		// Validate that we have at least 2 answer options.
 		if (choicesJson.size() < 2) {
 			validation.addError("choices", "validation.required.atLeastTwo", answer);
 		}
 
 		// If we have an error, go to newpollform.
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			index(question, answer);
 			return;
 		}
 		
 		// TODO: Check if the email provided belongs to an existing user
 		// If yes: Redirect the user to the login page - save the poll somewhere temporarily.
 		// If no: Create a new user and create the poll, and bind these two together.
 
 		// Finally, we're ready to send it to the server, and see if it likes it or not.
 		PollJSON p = new PollJSON();
 		p.question = question;
 		p.choices = choicesJson;
 		p.loginRequired = loginRequired;
 		if (type.equals("multiple")) {
 			p.multipleAllowed = true;
 		}
 
 		try {
 			CreatePollResponse response = (CreatePollResponse) APIClient.send(new CreatePollRequest(p));
 			PollJSON poll = response.poll;
 			if(poll != null) {
 				success(poll.token);
 			} else {
 				throw new Exception("Something went wrong, creating the poll.");
 			}
 	
 			// Redirect to success
 		} catch (Exception e) {
 			// It failed!
 			// TODO: Tell the user!
 			//e.printStackTrace();
 			params.flash();
 			validation.addError(null, e.getMessage());
 			validation.keep();
 			index(question, answer);
 		}
 	}
 }
