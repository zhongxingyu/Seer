 package elfville.server.controller;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import elfville.protocol.Response;
 import elfville.protocol.Response.Status;
 import elfville.protocol.SignInRequest;
 import elfville.protocol.SignUpRequest;
 import elfville.server.CurrentUserProfile;
 import elfville.server.SecurityUtils;
 import elfville.server.model.Elf;
 import elfville.server.model.User;
 
 /* 
  * Controls sign in, sign up
  */
 public class AuthenticationControl extends Controller {
 
 	public static Response signIn(SignInRequest r,
 			CurrentUserProfile currentUser) {
 
 		Response resp = new Response(Status.FAILURE);
 
 		User user = database.userDB.findByUsernameHashedPassword(
 				r.getUsername(), r.getPassword());
 		System.out.println(r.getUsername());
 
 		if (user == null) {
 			return resp;
 		}
 		if (!logInUser(user, r, currentUser)) {
 			return resp;
 		}
 
 		resp = new Response(Status.SUCCESS);
 		return resp;
 	}
 
 	private static boolean logInUser(User user, SignInRequest r,
 			CurrentUserProfile currentUser) {
 		long currTime = System.currentTimeMillis();
 		if (!user.laterThanLastLogin(currTime)
 				|| !user.laterThanLastLogout(currTime)) {
 			return false;
 		}
 		user.setLastLogin(currTime);
 
 		// sign the user in
 		currentUser.setLastLogin(currTime);
 		currentUser.setNonce(r.getNonce());
 		currentUser.setCurrentUserId(user.getModelID());
 
 		return true;
 	}
 
 	public static Response signUp(SignUpRequest r,
 			CurrentUserProfile currentUser) {
 		Response resp = new Response(Status.FAILURE);
 		User user = database.userDB.findByUsername(r.getUsername());
 
 		// check to see if user already exists
 		if (user != null) {
 			System.out.println("Username already exists");
 			return new Response(Status.FAILURE, "Username already exists");
 		}
 
 		// 20 char max, 4 char min
 		if (20 < r.getUsername().length() || r.getUsername().length() < 4) {
 			return new Response(Status.FAILURE,
					"User name must be between 4 and 20 characters");
 		}
 
 		if (r.getUsername().contains(" ")) {
 			return new Response(Status.FAILURE, "No spaces in the username");
 		}
 
 		// make sure the username contains only letters and numbers
 		Pattern p = Pattern.compile("[^a-z0-9]*", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(r.getUsername());
 		boolean b = m.matches();
 
 		if (b) {
 			return new Response(Status.FAILURE,
 					"Only letters and numbers in the username");
 		}
 
 		// 8 char min must include a number, 20 char max
 		if (20 < r.getPassword().length() || r.getPassword().length() < 8) {
 			return new Response(Status.FAILURE,
 					"Password must be between 8 and 20 characters");
 		}
 
 		// make sure that the pass contains a special character or a number
 		p = Pattern.compile("[a-z]*", Pattern.CASE_INSENSITIVE);
 		m = p.matcher(r.getPassword());
 		b = m.matches();
 
 		if (b) {
 			return new Response(Status.FAILURE,
 					"Password must contain at least one number or special character");
 		}
 
 		if (b) {
 			return new Response(Status.FAILURE,
 					"Password may not contain whitespace");
 		}
 
 		Elf elf = new Elf(r.getUsername(), r.description);
 		elf.save();
 		user = new User(elf, r.getUsername());
 		String hashedPassword;
 		try {
 			hashedPassword = SecurityUtils
 					.generateRandomPepper(r.getPassword());
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("generate random pepper failure");
 			return resp;
 		}
 		user.setPassword(hashedPassword);
 
 		if (!logInUser(user, r, currentUser)) {
 			System.out.println("login user failed");
 			return resp;
 		}
 
 		user.save();
 		resp = new Response(Status.SUCCESS);
 		return resp;
 	}
 
 }
