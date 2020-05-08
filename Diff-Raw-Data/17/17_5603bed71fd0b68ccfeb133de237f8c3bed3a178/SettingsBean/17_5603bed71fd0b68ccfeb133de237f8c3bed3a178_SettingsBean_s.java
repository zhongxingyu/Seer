 package net.mytcg.topcar.util;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import net.rim.device.api.i18n.SimpleDateFormat;
 import net.rim.device.api.io.http.HttpDateParser;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.system.PersistentObject;
 import net.rim.device.api.system.PersistentStore;
 import net.rim.device.api.util.Persistable;
 
 public final class SettingsBean implements Persistable {
 	private static SettingsBean _instance;
 	
 	private static final long GUID = 0x8802af5a5d602a5eL;
 	
 	
 	//the users username for the server
 	private String username;
 	//the users password for the server
 	private String password;
 	//users email address
 	private String email;
 	//credits
 	private String credits;
 	//the users url for the server
 	private String url;
 	//the schedule option, none, auto, daily, weekly
 	private String msisdn;
 	private boolean authenticated;
 	private String usercategories;
 	private String productcategories;
 	
 	private Hashtable albums;
 	private Hashtable products;
 	private Hashtable cards;
 	private Hashtable images;
 	
 	public boolean created = false;
 	public boolean shared = false;
 	public boolean added = false;
 	public boolean deleted = false;
 	public boolean leavegame = false;
 	public boolean notifications = false;
 	
 	public boolean loadingimage = false;
 	public String loading;
 	public String loadingflip;
 	public int deckid = -1;
 	
 	//saves precache xml data
 	private String all;
 	
 	private int size = -1;
 	
 	public void setSize(int size) {
 		this.size = size;
 	}
 	public int getSize() {
 		return size;
 	}
 	
 	private int lastloaded = 0;
 	private int noteloaded = 0;
 	
 	public void lastloaded() {
 		lastloaded=(int)((System.currentTimeMillis())/1000);
 	}
 	public int getLoaded() {
 		return lastloaded;
 	}
 	
 	public void noteloaded() {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		Calendar calender = Calendar.getInstance();
 		Date date = new Date(HttpDateParser.parse(dateFormat.format(calender.getTime())));
		System.out.println("noteloaded "+date);
		System.out.println("noteloaded "+date.getTime()/1000);
 		
 		noteloaded=(int)(date.getTime()/1000);
 	}
 	public int getNoteLoaded() {
 		return noteloaded;
 	}
 	
 	public static SettingsBean getSettings() {
 		if (_instance == null) {
 			PersistentObject store = PersistentStore.getPersistentObject(GUID);
 			synchronized(store) {
 				_instance = (SettingsBean)store.getContents();
 				if (_instance == null) {
 					_instance = new SettingsBean();
 					store.setContents(_instance);
 					store.commit();
 				}
 			}
 			store = null;
 		}
 		return _instance;
 	}
 	
 	public static void saveSettings(SettingsBean _instance) {
 		if (_instance != null) {
 			PersistentObject store = PersistentStore.getPersistentObject(GUID);
 			synchronized(store) {
 				store.setContents(_instance);
 				store.commit();
 			}
 			store = null;
 		}
 	}
 	
 	private SettingsBean(){
 		initialize();
 	}
 	
 	private void initialize() {
 		setUsername("");
 		setPassword("");
 		setEmail("");
 		setUrl(Const.url);
 		setUsercategories(Const.cat);
 		setProductcategories(Const.productcat);
 		albums = new Hashtable();
 		cards = new Hashtable();
 		images = new Hashtable();
 		setMSISDN("");
 		setAuthenticated(false);
 	}
 	public boolean getAuthenticated() {
 		return authenticated;
 	}
 	public boolean setAuthenticated(boolean authenticated) {
 		if (this.authenticated == authenticated) {
 			return false;
 		}
 		this.authenticated = authenticated;
 		return true;
 	}
 	public String getMSISDN() {
 		return msisdn;
 	}
 	public boolean setMSISDN(String msisdn) {
 		if (msisdn == null||this.msisdn == null) {
 			return false;
 		}
 		if (this.msisdn.equals(msisdn)) {
 			return false;
 		}
 		this.msisdn = msisdn;
 		return true;
 	}
 	public String getUsername() {
 		return username;
 	}
 	public boolean setUsername(String username) {
 		if ((this.username != null)&&(this.username.equals(username))) {
 			return false;
 		}
 		this.username = username;
 		return true;
 	}
 	public String getEmail() {
 		return email;
 	}
 	public boolean setEmail(String email) {
 		if ((this.email != null)&&(this.email.equals(email))) {
 			return false;
 		}
 		this.email = email;
 		return true;
 	}
 	public String getCredits() {
 		return credits;
 	}
 	public boolean setCredits(String credits) {
 		if ((this.credits != null)&&(this.credits.equals(credits))) {
 			return false;
 		}
 		this.credits = credits;
 		return true;
 	}
 	public String getPassword() {
 		return password;
 	}
 	public boolean setPassword(String password) {
 		if ((this.password != null)&&(this.password.equals(password))) {
 			return false;
 		}
 		this.password = password;
 		return true;
 	}
 	public String getUrl() {
 		return url;
 	}
 	public boolean setUrl(String url) {
 		if ((this.url != null)&&(this.url.equals(url))) {
 			return false;
 		}
 		this.url = url;
 		return true;
 	}
 	public String getUsercategories() {
 		return usercategories;
 	}
 	public boolean setUsercategories(String usercategories) {
 		if ((this.usercategories != null)&&(this.usercategories.equals(usercategories))) {
 			return false;
 		}
 		this.usercategories = usercategories;
 		return true;
 	}
 	public boolean setUsercategories(String usercategories, int id) {
 		String var = (String)albums.get(""+id);
 		if ((var != null)&&(var.equals(usercategories))) {
 			return false;
 		}
 		var = usercategories;
 		albums.put(""+id, var);
 		return true;
 	}
 	public String getUsercategories(int id) {
 		if (!(albums.containsKey(""+id))) {
 			return Const.cat;
 		}
 		return (String)albums.get(""+id);
 	}
 	public String getProductcategories() {
 		return productcategories;
 	}
 	public boolean setProductcategories(String productcategories) {
 		if ((this.productcategories != null)&&(this.productcategories.equals(productcategories))) {
 			return false;
 		}
 		this.productcategories = productcategories;
 		return true;
 	}
 	public boolean setImages(int id, Card image) {
 		Card var = (Card)images.get(""+id);
 		if ((var != null)&&(var.equals(image))) {
 			return false;
 		}
 		images.put(""+id, image);
 		return true;
 	}
 	public Card getImages(int id) {
 		if (!(images.containsKey(""+id))) {
 			return null;
 		}
 		return (Card)images.get(""+id);
 	}
 	public boolean setAll(String all) {
 		if ((this.all != null)&&(this.all.equals(all))) {
 			return false;
 		}
 		this.all = all;
 		return true;
 	}
 	public boolean setCards(String card, int id) {
 		String var = (String)cards.get(""+id);
 		if ((var != null)&&(var.equals(card))) {
 			return false;
 		}
 		var = card;
 		cards.put(""+id, var);
 		return true;
 	}
 	public String getCards(int id) {
 		if (!(cards.containsKey(""+id))) {
 			return Const.card;
 		}
 		return (String)cards.get(""+id);
 	}
 }
