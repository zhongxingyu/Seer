 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mmo.Money;
 
 import com.avaje.ebean.EbeanServer;
 import java.util.HashMap;
 import org.bukkit.Server;
 
 /**
  *
  * @author Xaymar
  */
 public class Money {
 
 	public MMOMoney plugin;
 	public Server server;
 	public EbeanServer database;
 	public HashMap<String, MoneyDB> loadedAccounts = new HashMap<String, MoneyDB>();
 	public HashMap<String, String> loadedTimes = new HashMap<String, String>();
 
 	public Money(MMOMoney plugin) {
 		this.plugin = plugin;
 		this.server = plugin.getServer();
 		this.database = getDatabase();
 	}
 
 	public MoneyDB getAccount(String user) {
 		if (loadedAccounts.containsKey(user)) {
 			return loadedAccounts.get(user);
 		} else {
 			return loadAccount(user);
 		}
 	}
 	
 	private void setUpTables() {
 	}
 	
 	private MoneyDB loadAccount(String Account) {
 		
 		return null;
 	}
 }
