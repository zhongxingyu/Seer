 package controllers;
 
 import java.util.List;
 
 import models.User;
 import models.Vote;
 import api.entities.VoteJSON;
 import api.helpers.GsonHelper;
 import api.requests.CreateVoteRequest;
 import api.responses.CreateVoteResponse;
 import api.responses.EmptyResponse;
 import api.responses.ReadVoteResponse;
 import api.responses.UpdateVoteResponse;
 
 /**
  * Class that manages the responses in the API for Votes.
  * 
  * @author OpenARMS Service Team
  * 
  */
 
 public class VoteController extends APIController {
 	/**
 	 * Method that saves a new Vote in the DataBase.
 	 * 
 	 * @throws Exception
 	 */
 	public static void create() throws Exception {
 		// Takes the VoteJSON and creates a new Vote object with this VoteJSON.
 		CreateVoteRequest req = GsonHelper.fromJson(request.body,
 				CreateVoteRequest.class);
 		CreateVoteResponse res = create(req.vote);
 		String jsonresponse = GsonHelper.toJson(res);
 		renderJSON(jsonresponse);
 	}
 
 	/**
 	 * Method that saves a new Vote in the DataBase. (Helper function to be
 	 * called by other controllers)
 	 */
 	public static CreateVoteResponse create(VoteJSON voteJson) throws Exception {
 		Vote vote = Vote.fromJson(voteJson);
 
 		// If current user is not the same as the poll creator or there is no
 		// current user, throws an exception
 		User u = AuthBackend.getCurrentUser();
 		if (u != null) {
 			vote.user = u;
 			if (!vote.pollInstance.poll.multipleAllowed) {
 				Vote vote2 = Vote.find("byPollInstanceAndUser", vote.pollInstance, u).first();
 
 				if (vote2 == null) {
 					vote.save();
 				} else {
 					throw new ForbiddenException("You can't vote twice in the same Poll.");
 				}
 			} else {
 				Vote vote2 = Vote.find("byPollInstanceAndUserAndChoice", 
 						vote.pollInstance, u, vote.choice).first();
 
 				if (vote2 == null) {
 					vote.save();
 				} else {
 					throw new ForbiddenException("You can't vote twice for the same Choice.");
 				}
 			}
		} else if (!vote.pollInstance.poll.loginRequired) {
 			vote.save();
 		} else {
 			throw new UnauthorizedException("This action requires authentication.");
 		}
 
 		// Creates the VoteJSON Response.
 		return new CreateVoteResponse(vote.toJson());
 	}
 
 	/**
 	 * Method that gets a Vote from the DataBase.
 	 * 
 	 * @throws Exception
 	 */
 	public static void retrieve() throws Exception {
 		String voteid = params.get("id");
 
 		// Takes the Vote from the DataBase.
 		Vote vote = Vote.find("byID", voteid).first();
 
 		if (vote == null) {
 			throw new NotFoundException();
 		}
 
 		// Creates the VoteJSON Response.
 		ReadVoteResponse r = new ReadVoteResponse(vote.toJson());
 		String jsonresponse = GsonHelper.toJson(r);
 
 		renderJSON(jsonresponse);
 	}
 
 	/**
 	 * Method that edits a Vote already existing in the DataBase.
 	 * 
 	 * @throws Exception
 	 */
 	public static void edit() throws Exception {
 		String voteid = params.get("id");
 
 		// Takes the Vote from the DataBase.
 		Vote originalvote = Vote.find("byID", voteid).first();
 
 		if (originalvote == null) {
 			throw new NotFoundException();
 		}
 
 		// If current user is not the same as the poll creator or there is no
 		// current user, throws an exception
 		/*
 		 * User u = AuthBackend.getCurrentUser(); if (u == null ||
 		 * originalvote.user.id != u.id) { throw new UnauthorizedException(); }
 		 */
 		AuthBackend.requireUser(originalvote.user);
 
 		// Takes the edited VoteJSON and creates a new Vote object with this
 		// VoteJSON.
 		CreateVoteRequest req = GsonHelper.fromJson(request.body,
 				CreateVoteRequest.class);
 		Vote editedvote = Vote.fromJson(req.vote);
 
 		// Changes the old fields for the new ones.
 		if (editedvote.choice != null) {
 			originalvote.choice = editedvote.choice;
 		}
 		if (editedvote.pollInstance != null) {
 			originalvote.pollInstance = editedvote.pollInstance;
 		}
 
 		originalvote.save();
 
 		// Creates the VoteJSON Response.
 		UpdateVoteResponse r = new UpdateVoteResponse(originalvote.toJson());
 		String jsonresponse = GsonHelper.toJson(r);
 		renderJSON(jsonresponse);
 	}
 
 	/**
 	 * Method that deletes a Vote existing in the DataBase.
 	 * 
 	 * @throws Exception
 	 */
 	public static void delete() throws Exception {
 		String voteid = params.get("id");
 
 		// Takes the Vote from the DataBase.
 		Vote vote = Vote.find("byID", voteid).first();
 
 		if (vote == null) {
 			throw new NotFoundException();
 		}
 
 		AuthBackend.requireUser(vote.user);
 
 		// Deletes the Vote from the DataBase and creates an empty VoteJSON for
 		// the response.
 		vote.delete();
 
 		renderJSON(new EmptyResponse().toJson());
 	}
 }
