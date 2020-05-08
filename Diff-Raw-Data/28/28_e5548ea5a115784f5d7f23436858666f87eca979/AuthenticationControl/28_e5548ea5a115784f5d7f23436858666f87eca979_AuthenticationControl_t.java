 package elfville.server.controller;
 
 import elfville.protocol.Response.Status;
 import elfville.protocol.SignInRequest;
 import elfville.protocol.SignInResponse;
 import elfville.protocol.SignUpRequest;
 import elfville.protocol.SignUpResponse;
 import elfville.server.ServerThread;
 import elfville.server.model.*;
 
 /* 
  * Controls sign in, sign up
  */
 public class AuthenticationControl extends Controller {
 	
 	public static SignInResponse signIn(SignInRequest inM, Integer currUserModelID) {
 		SignInResponse outM;
 		User user = database.userDB.findUserByUsername(inM.username);
 		if(user != null){
 			currUserModelID= user.getModelID();
 			outM = new SignInResponse(Status.SUCCESS, "word");
 		} else {
 			outM = new SignInResponse(Status.FAILURE, "word");
 		}
 		return outM;
 	}
 
 	public static SignUpResponse signUp(SignUpRequest inM) {
		System.out.println("Sign up is called!");
 		SignUpResponse outM;
 		User user = database.userDB.findUserByUsername(inM.username);
 		Elf elf;
		if (user != null){ 
 			//username is taken
			outM = new SignUpResponse(Status.FAILURE, "The username is already taken");
			System.out.println("The username is already taken");
 		} else {
 			elf = new Elf();
 			elf.setElfName(inM.username);
 			elf.setDescription(inM.description);
 			database.elfDB.insert(elf);
 			user= new User();
 			user.setElf(elf);
 			//user.setPassword("lolskates");  //TODO: password
 			user.setUsername(inM.username);
 			database.userDB.insert(user);
			System.out.println("sign up success");
 			outM= new SignUpResponse(Status.SUCCESS, "word");
 		}
 		return outM;
 	}
 	
 }
