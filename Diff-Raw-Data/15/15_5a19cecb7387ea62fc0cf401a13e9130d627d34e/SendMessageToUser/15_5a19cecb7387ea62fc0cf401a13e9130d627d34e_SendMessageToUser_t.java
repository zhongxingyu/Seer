 package taksan;
 
 import com.skype.Friend;
 import com.skype.Skype;
 import com.skype.SkypeException;
 import com.skype.User;
 
 public class SendMessageToUser implements Runnable {
 
 	private final String userName;
 	private final String message;
 
 	public SendMessageToUser(String userName, String message) {
		this.userName = userName;
 		this.message = message;	
 	}
 
 	public void run() {
 		try {
 			final User user = getUser();
 			if (user == null)
 				throw new IllegalArgumentException("User " + userName +" not found in your contact list");
 			
 			user.send(message);
 		} catch (SkypeException e) {
 			throw new FailedDueToSkypeException();
 		}
 	}
 
 	private User getUser() throws SkypeException {
		return getUserByFullName(userName);
 	}
 
 	private User getUserByFullName(String userName2) throws SkypeException {
 		Friend[] contactList = Skype.getContactList().getAllFriends();
 		for (Friend f : contactList) {
			if (f.getFullName().equalsIgnoreCase(userName2)){
 				return f;
 			}
			if (f.getId().equalsIgnoreCase(userName2))
				return f;
 		}
 		return null;
 	}
 }
