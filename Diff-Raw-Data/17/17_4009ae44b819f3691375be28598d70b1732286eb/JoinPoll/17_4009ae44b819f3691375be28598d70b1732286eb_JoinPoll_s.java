 package controllers;
 import java.text.DecimalFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import play.mvc.Controller;
 import api.requests.CreateVoteRequest;
 import api.requests.ReadPollInstanceByTokenRequest;
 import api.requests.ReadPollInstanceRequest;
 import api.requests.ReadPollRequest;
 import api.responses.ReadPollInstanceResponse;
 import api.responses.ReadPollResponse;
 import api.entities.ChoiceJSON;
 import api.entities.VoteJSON;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonParseException;
 
 public class JoinPoll extends Controller {
 	public static void index(String token) {
 		try {
 			/** get the Poll Instance Data */
 			ReadPollInstanceResponse instanceResponse = (ReadPollInstanceResponse) APIClient.send(new ReadPollInstanceByTokenRequest(token));
 			Long poll_id = instanceResponse.pollinstance.poll_id;
 			// Long instanceId = instanceResponse.pollinstance.id;
 			// Date startDateTime = instanceResponse.pollinstance.startDateTime;
			Date endDateTime = instanceResponse.pollinstance.endDateTime;
 			
 			/** get the Poll Data */
 			ReadPollResponse pollResponse = (ReadPollResponse) APIClient.send(new ReadPollRequest(poll_id));
 			// String pollReference = pollResponse.poll.reference;
 			// Long pollUserId = pollResponse.poll.admin;
 			// Boolean pollMultipleAllowed = pollResponse.poll.multipleAllowed;
 			String pollQuestion = pollResponse.poll.question;
 			List<ChoiceJSON> pollChoices = pollResponse.poll.choices;
 			
 			/** render the JoinPoll.Index */
 			render(poll_id, endDateTime, pollQuestion, pollChoices);
 		} catch (Exception e)
 		{
 			// TODO: tell the user it failed
 		}
 	}
 
 	public static void submit(Long instanceId, Long[] votes) throws Exception {
 		if (votes != null) {
 			for (Long l : votes) {
 				VoteJSON vote = new VoteJSON();
 				vote.choiceid = l;
 				vote.pollInstanceid = instanceId;
 				APIClient.getInstance().send(new CreateVoteRequest(vote));
 			}
 		}
 		else {
 			throw new Exception("no answers submitted");
 		}
 		
 		/* TODO: validation
 		validation.required(stuff);
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			index(token);
 			return;
 		} */
 		
 		success(instanceId);
 	}
 
 	public static void success(Long instanceId) {
 		// TODO: what should happen? just show the statistics of the instance?
 		
 		/* String question = null;
 		JsonArray answersArray = null;
 		String duration = null;
 
 		try {
 			ReadPollInstanceResponse response = (ReadPollInstanceResponse) APIClient.getInstance().send(new ReadPollInstanceRequest(instanceId));
 			question = response.question;
 			answersArray = response.answersArray;
 			duration = response.duration;
 			
 		} catch (Exception e) {
 			try {
 				// Most like the poll has been inactivated, so we need the results instead
 				GetResultsResponse response = (GetResultsResponse) APIClient.getInstance().send(new GetResultsRequest(token, null));
 				question = response.question;
 				answersArray = response.answersArray;
 				duration = "0";
 			} catch (Exception e2) {
 			}
 		}
 
 		render(token, question, answersArray, duration, durationString);
 		
 	}
 
 	public static void nopoll(String token) {
 		render(token); */
 	}
 }
