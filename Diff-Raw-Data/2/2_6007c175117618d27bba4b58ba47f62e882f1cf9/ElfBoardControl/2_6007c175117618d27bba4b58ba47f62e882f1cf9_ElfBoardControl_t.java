 package elfville.server.controller;
 
 import elfville.protocol.ProfileRequest;
 import elfville.protocol.ProfileResponse;
 import elfville.protocol.Response.Status;
 import elfville.protocol.models.SerializableElf;
 import elfville.server.CurrentUserProfile;
 import elfville.server.model.Elf;
 import elfville.server.model.User;
 
 public class ElfBoardControl extends Controller {
 
 	public static ProfileResponse getProfile(ProfileRequest r,
 			CurrentUserProfile currentUser) {
 		ProfileResponse resp= new ProfileResponse(Status.FAILURE);
 		
 		User user = database.userDB.findUserByModelID(currentUser
 				.getCurrentUserId());
 		if (user == null) {
 			return resp;
 		}
 
 		// check to make sure that we weren't sent modelID
 		if (r.modelID == null) {
 			return resp;
 		}
 
 		Elf elf= database.elfDB.findByEncryptedID(r.modelID);
 
 		// check to see that the requested elf actually exists
 		if (elf == null) {
 			return resp;
 		}
 		
 		SerializableElf profile= new SerializableElf();
 		profile.description= elf.getDescription();
 		profile.elfName= elf.getElfName();
 		profile.centralBoardPosts= ControllerUtils.
				buildPostList(database.postDB.getCentralPosts(), user.getElf());
 		profile.numSocks = elf.getNumSocks();
 		
 		resp.status= Status.SUCCESS;
 		resp.elf = profile;
 		return resp;
 	}
 
 }
