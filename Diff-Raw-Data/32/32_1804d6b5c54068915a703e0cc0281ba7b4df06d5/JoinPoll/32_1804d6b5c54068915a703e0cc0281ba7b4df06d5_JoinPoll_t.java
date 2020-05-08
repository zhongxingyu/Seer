 package controllers;
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Http.StatusCode;
 import api.entities.PollInstanceJSON;
 import api.entities.PollJSON;
 import api.entities.VoteJSON;
 import api.requests.CreateVoteRequest;
 import api.requests.ReadPollInstanceByTokenRequest;
 import api.requests.ReadPollInstanceRequest;
 import api.requests.ReadPollRequest;
 import api.responses.ReadPollInstanceResponse;
 import api.responses.ReadPollResponse;
 
 public class JoinPoll extends Controller {
 	public static void index(String token) throws Exception {
 		Logger.debug("Initial token is %s", token);
 		if(token == null) {
 			notFound("No poll without a token.");
 		}
 		
		
		
 		/** get the Poll Instance Data */
 		ReadPollInstanceResponse res = (ReadPollInstanceResponse) APIClient.send(new ReadPollInstanceByTokenRequest(token));
 		if(res.statusCode.equals(StatusCode.NOT_FOUND)) {
 			notFound("Poll not found.");
 		} else if (StatusCode.error(res.statusCode)) {
 			notFound("Error finding the poll.");
 		}
 		
 		PollInstanceJSON pollInstance = res.pollinstance;
		
 		// TODO: Consider if this is even nessesary to get the poll, as choices are allready defined in the instance response (as VoteSummaryJSONs).
 		ReadPollResponse pollResponse = (ReadPollResponse) APIClient.send(new ReadPollRequest(pollInstance.poll_id));
 		if(pollResponse.statusCode.equals(StatusCode.NOT_FOUND)) {
 			notFound("Poll not found.");
 		} else if (StatusCode.error(pollResponse.statusCode)) {
 			notFound("Error finding the poll.");
 		}
 		
 		PollJSON poll = pollResponse.poll;
		
		// Check if login is required for the poll
		if (poll.loginRequired) {
			if (!LoginUser.isLoggedIn()) {
				LoginUser.forward = "joinpoll";
				LoginUser.pollToken = token;
				LoginUser.index("");
			}
		}
		
 		render(poll, pollInstance);
 	}
 
 	public static void submit(String token, Long pollinstance_id, Long poll_id, Long[] answers) throws Exception {
 		validation.required("answers", answers);
 		validation.required("pollinstance_id", pollinstance_id);
 		if(validation.hasErrors()) {
 			// Show the user ...
 			validation.keep();
 			index(token);
 		} else {
 			for (Long l : answers) {
 				VoteJSON vote = new VoteJSON();
 				vote.choiceid = l;
 				vote.pollInstanceid = pollinstance_id;
 				APIClient.getInstance().send(new CreateVoteRequest(vote));
 			}
 			success(pollinstance_id, poll_id);
 		}
 	}
 
 	public static void success(Long pollinstance_id, Long poll_id) throws Exception {
 		/** get the Poll Instance Data */
 		ReadPollResponse res1 = (ReadPollResponse) APIClient.send(new ReadPollRequest(poll_id));
 		ReadPollInstanceResponse res2 = (ReadPollInstanceResponse) APIClient.send(new ReadPollInstanceRequest(pollinstance_id));
 		if(res1.statusCode.equals(StatusCode.NOT_FOUND) || res2.statusCode.equals(StatusCode.NOT_FOUND)) {
 			notFound("Poll not found.");
 		} else if (StatusCode.error(res1.statusCode) || StatusCode.error(res2.statusCode)) {
 			notFound("Error finding the poll.");
 		}
 
 		PollJSON poll = res1.poll;
 		PollInstanceJSON pollInstance = res2.pollinstance;
 		
 		render(poll, pollInstance);
 	}
 }
