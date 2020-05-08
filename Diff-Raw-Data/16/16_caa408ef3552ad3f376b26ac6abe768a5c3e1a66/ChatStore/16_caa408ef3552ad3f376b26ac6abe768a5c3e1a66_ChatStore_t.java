 package directi.androidteam.training.chatclient.Chat;
 
 import directi.androidteam.training.chatclient.Authentication.Account;
 import directi.androidteam.training.chatclient.Authentication.AccountManager;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 10/22/12
  * Time: 7:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ChatStore {
     private static ChatStore chatStore = new ChatStore();
     private HashMap<String,String> chatMap;
     private ChatStore() {
         chatMap = new HashMap<String, String>();
         ArrayList<Account> accounts = AccountManager.getInstance().getAccountList();
         Account[] accounts1 = accounts.toArray(new Account[accounts.size()]);
         for (Account account : accounts1) {
             chatMap.put(account.getAccountUid(),account.getAccountUid());
         }
     }
 
     public static ChatStore getInstance() {
         return chatStore;
     }
 
     public void addEntry(String from, String accountUID) {
         chatMap.put(from.split("@")[0],accountUID);
     }
 
     public String getAcctUID(String jid) {
         return chatMap.get(jid.split("@")[0]);
     }
 }
