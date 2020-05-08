 package cz.cvut.fel.via.zboziforandroid.client;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.Context;
 import cz.cvut.fel.via.zboziforandroid.client.words.SaveWordResponse;
 import cz.cvut.fel.via.zboziforandroid.client.words.Word;
 import cz.cvut.fel.via.zboziforandroid.client.words.WordResponse;
 import cz.cvut.fel.via.zboziforandroid.model.QueryDatabase;
 
 public class Utils {
 
	public final static String WORDS_API_URL = "http://46.255.228.229:8080";
 
 	public static void loadUserSearchedWords(final String email) {
 		if (email != null && email != "") {
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					ZboziForAndroidClient c = new ZboziForAndroidClient();
 					WordResponse response = c.getWords(email);
 					QueryDatabase.refreshQueries();
 					if (response != null && response.getWords() != null) {
 						for (Word word : response.getWords()) {
 							QueryDatabase.saveQuerry(word.getWord());
 						}
 					}
 				}
 			};
 			new Thread(runnable).start();
 		}
 	}
 
 	public static void saveSearchedWord(final String email, final String word) {
 		if (email != null && email != "" && word != null && word != "") {
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					ZboziForAndroidClient c = new ZboziForAndroidClient();
 					SaveWordResponse response = c.saveWord(email, word);
 					if (response != null && response.getStatus() == 201) {
 						// success
 					} else {
 						// word not saved
 					}
 				}
 			};
 			new Thread(runnable).start();
 		}
 	}
 
 	public static void deleteWordsHistory(final String email) {
 		if (email != null && email != "") {
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					ZboziForAndroidClient c = new ZboziForAndroidClient();
 					c.deleteWords(email);
 
 				}
 			};
 			new Thread(runnable).start();
 		}
 	}
 	
 	public static String getEmail(Context context) {
 	    AccountManager accountManager = AccountManager.get(context); 
 	    Account account = getAccount(accountManager);
 
 	    if (account == null) {
 	      return null;
 	    } else {
 	      return account.name;
 	    }
 	  }
 	
 	private static Account getAccount(AccountManager accountManager) {
 	    Account[] accounts = accountManager.getAccountsByType("com.google");
 	    Account account;
 	    if (accounts.length > 0) {
 	      account = accounts[0];      
 	    } else {
 	      account = null;
 	    }
 	    return account;
 	  }
 }
