 package interfaces;
 
 import data.LoginCred;
 import data.UserProfile;
 
 public interface GuiWindow {
 	public void logIn(LoginCred cred);
 	public void logOut();
 	
 	public UserProfile getUserProfile();
 	
 	public boolean setUserProfile(UserProfile profile);
	
	
	public void registerUserStep1();
	
	public boolean registerUser(UserProfile profile);
	
	public void registerUserStep3();
 }
