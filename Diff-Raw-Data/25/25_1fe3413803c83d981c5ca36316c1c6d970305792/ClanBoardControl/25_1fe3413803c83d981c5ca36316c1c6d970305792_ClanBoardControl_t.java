 package elfville.server.controller;
 
 import elfville.protocol.*;
 import elfville.protocol.Response.Status;
 import elfville.server.CurrentUserProfile;
 import elfville.server.model.*;
 
 /*
  * Control all requests (POST/GET/DELETE) to Clan Board
  */
 
 public class ClanBoardControl extends Controller {
 
 	public static ClanBoardResponse getClanBoard(ClanBoardRequest inM,
 			CurrentUserProfile currentUser) {
 		// Check if the user's elf is a clan member
 		User user = database.userDB.findUserByModelID(currentUser
 				.getCurrentUserId());
 		if (user == null) {
 			return new ClanBoardResponse(Status.FAILURE);
 		}
 		Elf elf = user.getElf();
 
 		Clan clan = database.clanDB
 				.findByEncryptedModelID(inM.getClanModelID());
 		if (clan == null) {
 			return new ClanBoardResponse(Status.FAILURE);
 		}
 
 		ClanBoardResponse outM = new ClanBoardResponse(Status.SUCCESS);
 
 		// Basic board info that is visible to all elves
 		outM.clan = clan.getSerializableClan();
 
 		boolean isMember = clan.isMember(elf);
 		if (isMember) {
 			if (clan.isLeader(elf)) {
 				outM.elfStatus = ClanBoardResponse.ElfClanRelationship.LEADER;
 			} else {
 				outM.elfStatus = ClanBoardResponse.ElfClanRelationship.MEMBER;
 			}
 		} else {
 			if (clan.isApplicant(elf)) {
 				outM.elfStatus = ClanBoardResponse.ElfClanRelationship.APPLICANT;
 			} else {
 				outM.elfStatus = ClanBoardResponse.ElfClanRelationship.OUTSIDER;
 			}
 			// Posts are only visible to clan members
 			outM.clan.posts = null;
 		}
 
 		return outM;
 	}
 	
 	public static Response postClanBoard(PostClanBoardRequest req,
 			CurrentUserProfile currentUser){
 		Response resp= new Response(Status.FAILURE);
 		
 		// make sure that the current user still exists!
 		User user = database.userDB.findUserByModelID(currentUser
 				.getCurrentUserId());
 		if (user == null) {
 			return resp;
 		}
 		Elf elf = user.getElf();
 
 		// check to make sure that we were sent a clan
 		if (req.clan == null) {
 			return resp;
 		}
 
 		Clan clan = database.clanDB.findByEncryptedModelID(req.clan.modelID);
 
 		// check to see that the requested clan actually exists
 		if (clan == null) {
 			return resp;
 		}
 		
 		if(req.post == null){
 			return resp;
 		}
 		
 		//make sure we were actually sent a post
		if(req.post.content == null || req.post.content.equals("") ||
				req.post.title == null || req.post.title.equals("")){
 			return resp;
 		}
 		
 		//make sure that this elf is a part of the clan
 		if (!clan.isLeader(elf) || !clan.isMember(elf)) {
 			return resp;
 		}
 		
 		Post post= new Post(req.post, elf);
 		//now we can post
 		clan.createPost(post);
 		
 		resp.status= Status.SUCCESS;
 		return resp;
 	}
 
 	public static Response modifyClan(ModifyClanRequest req,
 			CurrentUserProfile currentUser) {
 		Response resp = new Response(Status.FAILURE);
 
 		// make sure that the current user still exists!
 		User user = database.userDB.findUserByModelID(currentUser
 				.getCurrentUserId());
 		if (user == null) {
 			return resp;
 		}
 		Elf elf = user.getElf();
 
 		// check to make sure that we were sent a clan
 		if (req.clan == null) {
 			return resp;
 		}
 
 		Clan clan = database.clanDB.findByEncryptedModelID(req.clan.modelID);
 
 		// check to see that the requested clan actually exists
 		if (clan == null) {
 			return resp;
 		}
 		
 		switch (req.requestType) {
 		case DELETE:
 			// make sure that this elf is actually the leader of the clan
 			if (!clan.isLeader(elf)) {
 				return resp;
 			}
 			clan.deleteClan();
 			break;
 
 		case JOIN:
 			// see if this elf has not already applied or is in the clan
 			if (!clan.isApplicant(elf) || !clan.isLeader(elf)
 					|| !clan.isMember(elf)) {
 				return resp;
 			}
 			clan.applyClan(elf);
 			break;
 
 		case LEAVE:
 			// see if the elf is in the clan and is not the leader
 			if (clan.isLeader(elf) || !clan.isMember(elf)) {
 				return resp;
 			}
 			clan.leaveClan(elf);
 			break;
 			
 		case ACCEPT:
 			//make sure that the accepter is actually the leader
 			if(req.applicant == null){
 				return resp;
 			}
 			
 			Elf applicant= database.elfDB.findByEncryptedID(req.applicant.elfID);
 			
 			if(applicant == null){
 				return resp;
 			}
 			
 			if(!clan.isLeader(elf)){
 				return resp;
 			}
 			//make sure that the elf being accepted is actually an applicant
 			if(clan.isApplicant(applicant)){
 				return resp;
 			}
 			
 			clan.joinClan(applicant);
 			
 			break;
 			
 		case DELETEPOST:
 			//make sure the person 
 			break;
 		}
 		
 
 		resp.status = Status.SUCCESS;
 
 		return resp;
 	}
 }
