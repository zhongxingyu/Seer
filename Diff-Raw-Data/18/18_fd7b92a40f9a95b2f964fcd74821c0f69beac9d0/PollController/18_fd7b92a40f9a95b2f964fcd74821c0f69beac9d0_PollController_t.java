 package controllers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Random;
 
 import controllers.AuthBackend;
 import models.Choice;
 import models.Poll;
 import models.User;
 import models.Vote;
 import api.helpers.GsonHelper;
 import notifiers.MailNotifier;
 import api.requests.CreatePollRequest;
 import api.requests.UpdatePollRequest;
 import api.responses.CreatePollResponse;
 import api.responses.EmptyResponse;
 import api.responses.ReadPollByTokenResponse;
 import api.responses.ReadPollResponse;
 import api.responses.UpdatePollResponse;
 import play.Logger;
 import play.mvc.Controller;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import api.entities.PollJSON;
 import api.entities.VoteJSON;
 
 /**
  * Class that manages the responses in the API for Polls.
  * @author OpenARMS Service Team
  *
  */
 
 public class PollController extends APIController {
 
 	/**
 	 * Method that saves a new Poll in the DataBase.
 	 * @throws Exception 
 	 */
 	public static void create() throws Exception {
     	Logger.debug("PollController.create() received '%s'", request.body);
     	
     	//TODO: try fromJson with null
     	CreatePollRequest req = GsonHelper.fromJson(request.body, CreatePollRequest.class);
         Poll poll = Poll.fromJson(req.poll);
 
         // Set the admin to this user.
         poll.admin = AuthBackend.getCurrentUser();
        
        
 		StringBuilder strBuild = new StringBuilder();
	    
         // Generates a Unique ID and saves the Poll.
         // TODO: Make this more robust, what will happen if all 1.000.000 tokens are taken?
	    String SECRET_CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random random = new Random();

		do {
			strBuild.delete(0,strBuild.length());
			for (int i=0; i<10; i++)
				strBuild.append(SECRET_CHARSET.charAt(random.nextInt(SECRET_CHARSET.length()-1)));
			poll.token = strBuild.toString();
		} while (!Poll.find("byToken", poll.token).fetch().isEmpty());
         
         poll.save();
         
         // Creates the PollJSON Response.
         CreatePollResponse r = new CreatePollResponse(poll.toJson());
     	String jsonresponse = GsonHelper.toJson(r);
     	renderJSON(jsonresponse);
 	}
 
 	/**
 	 * Method that gets a Poll from the DataBase.
 	 * @throws Exception 
 	 */
 	public static void retrieve () throws Exception {
 		String pollid = params.get("id");
 
 		//Takes the Poll from the DataBase.
 		Poll poll = Poll.find("byID", pollid).first();
 		
 		if (poll == null) {
 			throw new NotFoundException();
 		}
 		
 		//Creates the PollJSON Response
 		ReadPollResponse r = new ReadPollResponse(poll.toJson());
 		String jsonresponse = GsonHelper.toJson(r);
 
 		renderJSON(jsonresponse);
 	}
 	/**
 	 * Method that gets a Poll from the DataBase.
 	 * @throws Exception 
 	 */
 	public static void retrieveByToken () throws Exception {
 			String polltoken = params.get("token");
 	
 			//Takes the Poll from the DataBase.
 			Poll poll = Poll.find("byToken", polltoken).first();
 			
 			if (poll == null) {
 				throw new NotFoundException();
 			}
 			
 			//Creates the PollJSON Response
 			ReadPollByTokenResponse r = new ReadPollByTokenResponse(poll.toJson());
 			String jsonresponse = GsonHelper.toJson(r);
 	
 			renderJSON(jsonresponse);
 	}
 	
 	/**
 	 * Method that edits a Poll already existing in the DataBase.
 	 * @throws Exception 
 	 */
 	public static void edit() throws Exception {
 		String pollid = params.get("id");
 
 		// Takes the Poll from the DataBase.
 		Poll originalpoll = Poll.find("byID", pollid).first();
 		
 		if (originalpoll == null) {
 			throw new NotFoundException();
 		}
 
 		AuthBackend.requireUser(originalpoll.admin);
 
 		// Takes the edited PollJSON and creates a new Poll object with this PollJSON.
 		UpdatePollRequest req = GsonHelper.fromJson(request.body, UpdatePollRequest.class);
         Poll editedpoll = Poll.fromJson(req.poll);
 
         // Changes the old fields for the new ones.
         if (editedpoll.question != null) {
         	originalpoll.question = editedpoll.question;
         }
         if (editedpoll.reference != null) {
         	originalpoll.reference = editedpoll.reference;
         }
         if (editedpoll.choices != null) {
         	for (Choice c : editedpoll.choices) {
         		if (c.votes != null) {
             		for (Vote v : c.votes) {
             			v.choice = c;
             		}
         		}
         		originalpoll.choices.add(c);
         		c.poll = originalpoll;
 	    	}
         }
         
         originalpoll.save();
         
         //Creates the PollJSON Response.
         UpdatePollResponse res = new UpdatePollResponse(originalpoll.toJson());
     	String jsonresponse = GsonHelper.toJson(res);
     	renderJSON(jsonresponse);
 	}
 	
 	/**
 	 * Method that creates a new Poll from an already existing one in the DataBase.
 	 * @throws Exception 
 	 */
 	public static void copy () throws Exception {
 		String oldpollid = params.get("id_old");
 
 		//Takes the Poll from the DataBase.
 		Poll oldpoll = Poll.find("byID", oldpollid).first();
 		
 		if (oldpoll == null) {
 			throw new NotFoundException();
 		}
 		
         //If current user is not the same as the poll creator or there is no current user, throws an exception
 		/*User u = AuthBackend.getCurrentUser();
 		if (u == null || oldpoll.admin.id != u.id) {
 	        throw new UnauthorizedException();
 	    }*/
 
 		AuthBackend.requireUser(oldpoll.admin);
 		Poll newpoll = new Poll(null, oldpoll);
 
         // Generates a new Unique ID and saves the Poll.
         do {
             newpoll.token = String.valueOf(new Random(System.currentTimeMillis()).nextInt(999999));
         } while (!Poll.find("byToken", newpoll.token).fetch().isEmpty());
         
         //Saves the new poll in the DataBase.
         newpoll.save();
 		
         // Creates the PollJSON Response.
         CreatePollResponse r = new CreatePollResponse(newpoll.toJson());
     	String jsonresponse = GsonHelper.toJson(r);
     	renderJSON(jsonresponse);
 	}
 
 	/**
 	 * Method that deletes a Poll existing in the DataBase.
 	 * @throws Exception 
 	 */
 	public static void delete () throws Exception {
 		String pollid = params.get("id");
 
 		//Takes the Poll from the DataBase.
 		Poll poll = Poll.find("byID", pollid).first();
 		
 		if (poll == null) {
 			throw new NotFoundException();
 		}
 		
 		AuthBackend.requireUser(poll.admin);
 		
 		//Deletes the Poll from the DataBase and creates an empty PollJSON for the response.
 		poll.delete();
 
 		renderJSON(new EmptyResponse().toJson());
 	}
 }
