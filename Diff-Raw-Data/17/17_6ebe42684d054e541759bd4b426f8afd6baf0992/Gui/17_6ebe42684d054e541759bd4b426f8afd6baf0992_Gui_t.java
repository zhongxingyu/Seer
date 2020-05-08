 package interfaces;
 
 import java.util.ArrayList;
 
 import data.LoginCred;
 import data.Pair;
 import data.Service;
 import data.UserProfile;
 
 /**
  * Gui interface.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public interface Gui {
 	public void start();
 	
 	public void logIn(LoginCred cred);
 	
 	public void logOut();
 	
 	
 	public UserProfile getUserProfile();
 	
 	public boolean setUserProfile(UserProfile profile);
 	
	public void registerUserStep1();
	
 	public boolean registerUser(UserProfile profile);
 	
	public void registerUserStep3();
	
 	public boolean verifyUsername(String username);
 	
 	
 	public ArrayList<Service> loadOffers();
 	
 	
 	public boolean launchOffer(Service service);
 	
 	public boolean launchOffers(ArrayList<Service> services);
 	
 	public boolean dropOffer(Service service);
 	
 	public boolean dropOffers(ArrayList<Service> services);
 	
 
 	public void acceptOffer(Pair<Service, Integer> pair);
 	
 	public void refuseOffer(Pair<Service, Integer> pair);
 	
 	
 	public boolean makeOffer(Pair<Service, Integer> pair, Double price);
 	
 	public boolean dropAuction(Pair<Service, Integer> pair);
 	
 	
 	public void changeServiceNotify(Service service);
 	
 	public void changeServicesNotify(ArrayList<Service> services);
 	
 	public void changeProfileNotify(UserProfile profile);
 }
