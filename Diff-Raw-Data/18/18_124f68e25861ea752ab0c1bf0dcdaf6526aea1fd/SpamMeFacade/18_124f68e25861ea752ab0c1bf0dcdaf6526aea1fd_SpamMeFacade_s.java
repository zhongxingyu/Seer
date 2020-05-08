 package spam.me;
 
 import java.util.List;
 
 public class SpamMeFacade {
 	private List<GroupChat> groupChatList; 
 	private PhoneInterface myPhoneInterface; 
 	private SpamMeDb mySpamMeDb; 
 	
 	public SpamMeFacade () {
 		myPhoneInterface = new PhoneInterface();
 		mySpamMeDb = new SpamMeDb();
 	}
 	public void addFriend(){
 	}
 	public void removeMe(){
 		
 	}
 	public void sendMsg(String msg, String number){
 		myPhoneInterface.sendSMS(msg, number);
 		
 	}
 	public void receiveMsg(){
 		
 	}
 	public void enableStatus(){
 		
 	}
 	public void disableStatus(){
 		
 	}
 	public void loadCurrentChats(){
 		
 	}
 	public void startNewChat(){
 		
 	}
 }
