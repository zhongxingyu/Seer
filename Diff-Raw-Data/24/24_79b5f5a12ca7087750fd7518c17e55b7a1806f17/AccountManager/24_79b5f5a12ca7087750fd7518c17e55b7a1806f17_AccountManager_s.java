 /**
  * 
  */
 package de.geotweeter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.scribe.model.Token;
 
 import android.content.SharedPreferences;
 import android.os.Handler;
 
 import de.geotweeter.apiconn.twitter.User;
 
 /**
  * @author Julian Kuerby
 *
  */
 public class AccountManager {
 
 	private List<Account> allAccounts = new ArrayList<Account>();
 	public static Account current_account = null;
 
 	public List<Account> getAllAccounts() {
 		return allAccounts;
 	}
	
 	public void init() {
 		List<User> authenticatedUsers = Geotweeter.getInstance().getAuthUsers();
		for (User u : authenticatedUsers) {
			createAccount(u, new Handler());
 		}
 	}
	
 	/**
 	 * Creates an account object for a given user object which starts the
 	 * twitter API access
 	 * 
 	 * @param u
 	 *            The user object whose account object should be created
 	 * @param handler
 	 */
 	public void createAccount(User u, Handler handler) {
 		Account acc = Account.getAccount(u);
 		if (acc == null) {
 			TimelineElementList tleList = new TimelineElementList();
			acc = new Account(tleList, getUserToken(u), u, Geotweeter.getInstance().getApplicationContext(),
					false, handler);
 		}
 		if (current_account == null) {
 			current_account = acc;
 		}
 		// TODO: register for GCM
 	}
 
 	/**
 	 * Gets the twitter access token for a given user
 	 * 
 	 * @param u
 	 *            The user object whose token is needed
 	 * @return The access token
 	 */
 	private Token getUserToken(User u) {
		SharedPreferences sp = Geotweeter.getInstance().getSharedPreferences(Constants.PREFS_APP, 0);
 		return new Token(sp.getString("access_token." + String.valueOf(u.id),
 				null), sp.getString("access_secret." + String.valueOf(u.id),
 				null));
 	}
 
 }
