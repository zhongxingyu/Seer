 package webClient;
 
 import java.util.ArrayList;
 
 import webServer.messages.GetProfileResponse;
 import webServer.messages.LoginRequest;
 import webServer.messages.LoginResponse;
 import webServer.messages.LogoutRequest;
 import webServer.messages.GetProfileRequest;
 import webServer.messages.RegisterProfileRequest;
 import webServer.messages.SetProfileRequest;
 
 import data.LoginCred;
 import data.Service;
 import data.UserProfile;
 import data.UserProfile.UserRole;
 import interfaces.MediatorWeb;
 import interfaces.WebClient;
 import interfaces.WebService;
 
 /**
  * WebClient mockup module implementation.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public class WebClientMockup extends Thread implements WebClient, WebService {
 	private MediatorWeb		med;
 	private LoginCred       cred;
 
 	private WebClientEvents	thread;
 
 	public WebClientMockup(MediatorWeb med) {
 		this.med = med;
 
 		med.registerWebClient(this);
 	}
 
 	public UserProfile logIn(LoginCred cred) {
 		System.out.println("[WebServiceClientMockup:logIn()] Begin");
 
 		Object requestObj = new LoginRequest(cred);
 		Object responseObj = Util.askWebServer(requestObj);
 
 		if (responseObj == null) {
 			System.out
 			.println("[WebServiceClientMockup:logIn()] Error");
 			return null;
 		}
 		if (responseObj instanceof LoginResponse) {
 			System.out.println("[WebServiceClientMockup:logIn()] Success");
 			
 			this.cred = cred;
 			thread = new WebClientEvents(this);
 			thread.start();
 			return ((LoginResponse) responseObj).getUserProfile();
 		} else {
 			System.out
 					.println("[WebServiceClientMockup:logIn()] Unexpected response message");
 			return null;
 		}
 	}
 
 	public void logOut() {
 		System.out.println("[WebServiceClientMockup:logOut()] Begin");
 		LogoutRequest requestMsg = new LogoutRequest(cred);
 
 		Object responseObj = Util.askWebServer(requestMsg);
 
 		thread.stopRunning();
		
 		System.out
 					.println("[WebServiceClientMockup:logOut()] Success");
 	}
 
 	public UserProfile getUserProfile(String username) {
 		Object requestObj = new GetProfileRequest(med.getLoginCred(), username);
 		Object responseObj = Util.askWebServer(requestObj);
 		
 		return ((GetProfileResponse) responseObj).getProfile();
 	}
 
 	public boolean setUserProfile(UserProfile profile) {
 		Object requestObj = new SetProfileRequest(med.getLoginCred(), profile);
 		Object responseObj = Util.askWebServer(requestObj);
 		med.changeProfileNotify(profile);
 		
 		return true;
 	}
 
 	public boolean registerUser(UserProfile profile) {
 		if (verifyUsername(profile.getUsername()))
 			return false;
 
 		Object requestObj = new RegisterProfileRequest(profile);
 		Object responseObj = Util.askWebServer(requestObj);
 
 		return true;
 	}
 
 	public boolean verifyUsername(String username) {
 		if (getUserProfile(username) != null)
 			return true;
 		return false;
 	}
 
 	public void publishService(Service service) {
 		thread.publishService(service);
 	}
 	
 	@Override
 	public void publishServices(ArrayList<Service> services) {
 		thread.publishServices(services);
 	}
 	
 	public void notifyNetwork(Service service) {
 		med.notifyNetwork(service);
 	}
 	
 	@Override
 	public String getUsername() {
 		return med.getUserProfile().getUsername();
 	}
 	
 	@Override
 	public UserRole getUserRole() {
 		return med.getUserProfile().getRole();
 	}
 }
