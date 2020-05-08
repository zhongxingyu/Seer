 package elfville.server.controller;
 
 import elfville.protocol.Response.Status;
 import elfville.protocol.SignInRequest;
 import elfville.protocol.SignInResponse;
 import elfville.server.model.Elf;
 
 /* 
  * Controls sign in, sign up
  */
 public class AuthenticationControl extends Controller {
 	
 	public static SignInResponse getPosts(SignInRequest inM) {
 		SignInResponse outM;
 		Elf elf= database.elfDB.findElfByUsername(inM.username);
 		if(elf != null){
 			 outM= new SignInResponse(Status.SUCCESS, "word");
 		} else {
 			 outM= new SignInResponse(Status.FAILURE, "word");
 		}
 		return outM;
 	}
 
 }
