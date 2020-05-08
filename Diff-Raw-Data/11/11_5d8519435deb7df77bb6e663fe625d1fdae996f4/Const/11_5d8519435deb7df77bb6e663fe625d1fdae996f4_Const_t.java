 package net.mytcg.topcar.util;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 import net.mytcg.topcar.http.ConnectionGet;
 import net.mytcg.topcar.http.ConnectionHandler;
 import net.mytcg.topcar.ui.GameCardsHome;
 import net.mytcg.topcar.ui.custom.ImageLoader;
 import net.rim.blackberry.api.phone.Phone;
 import net.rim.device.api.io.file.FileIOException;
 import net.rim.device.api.math.Fixed32;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.system.CDMAInfo;
 import net.rim.device.api.system.DeviceInfo;
 import net.rim.device.api.system.Display;
 import net.rim.device.api.system.EncodedImage;
 import net.rim.device.api.system.GPRSInfo;
 import net.rim.device.api.system.IDENInfo;
 import net.rim.device.api.system.RadioInfo;
 import net.rim.device.api.system.SIMCardInfo;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Font;
 
 public final class Const {
 	
 	public static String VERSION = "1.0";
 	private static boolean PORTRAIT = true;
 	private static final double ratio = 1.40625;
 	public static String PREFIX = "topcar_";
 	public static int THREADS = 0;
 	public static ConnectionHandler connect = null;
 	public static ImageLoader load = null;
 	public static ImageLoader load2 = null;
 	
 	public static ConnectionHandler getConnection() {
 		
 		if ((connect == null)||(!connect.isBusy())) {
 			connect = new ConnectionHandler();   
 		}
 		return connect;
 	}
 	
 	public static void setPortrait(boolean portrait) {
 		PORTRAIT = portrait;
 	}
 	public static boolean getPortrait() {
 		return PORTRAIT;
 	}
 	public static String getVersion() {
 		return VERSION;
 	}
 	public static String getMake() {
 		return "&make="+DeviceInfo.getManufacturerName();
 	}
 	public static String getModel() {
 		return "&model="+DeviceInfo.getDeviceName(); 
 	}
 	public static String getOSVer() {
 		return "&osver="+DeviceInfo.getSoftwareVersion();
 	}
 	public static String getTouch() {
 		return "&touch="+0;
 	}
 	
 	private static EncodedImage image;
 	private static Bitmap b;
 	
 	private static Bitmap getScaledBitmapImage(String imagename, int ratioX, int ratioY){
 		image = EncodedImage.getEncodedImageResource(imagename);
 		try {	
 			int currentWidthFixed32 = Fixed32.toFP(image.getWidth());
 			int currentHeightFixed32 = Fixed32.toFP(image.getHeight());
 			double ratio = (double)ratioX / (double) ratioY;
 			double w = (double) image.getWidth() * ratio;
 			double h = (double)image.getHeight() * ratio;
 			int width = (int) w;
 			int height = (int) h;
 			int requiredWidthFixed32 = Fixed32.toFP(width);
 			int requiredHeightFixed32 = Fixed32.toFP(height);
 			int scaleXFixed32 = Fixed32.div(currentWidthFixed32, requiredWidthFixed32);
 			int scaleYFixed32 = Fixed32.div(currentHeightFixed32, requiredHeightFixed32);
 			image = image.scaleImage32(scaleXFixed32, scaleYFixed32);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return image.getBitmap();
 	}
 	public static Bitmap getScaledBitmapImage(EncodedImage image, int ratioX, int ratioY) {
 		try {	
 			int currentWidthFixed32 = Fixed32.toFP(image.getWidth());
 			int currentHeightFixed32 = Fixed32.toFP(image.getHeight());
 			double ratio = (double)ratioX / (double) ratioY;
 			double w = (double) image.getWidth() * ratio;
 			double h = (double)image.getHeight() * ratio;
 			int width = (int) w;
 			int height = (int) h;
 			int requiredWidthFixed32 = Fixed32.toFP(width);
 			int requiredHeightFixed32 = Fixed32.toFP(height);
 			int scaleXFixed32 = Fixed32.div(currentWidthFixed32, requiredWidthFixed32);
 			int scaleYFixed32 = Fixed32.div(currentHeightFixed32, requiredHeightFixed32);
 			image = image.scaleImage32(scaleXFixed32, scaleYFixed32);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return image.getBitmap();
 	}
 	public static Bitmap getScaledBitmapImage(EncodedImage image, double ratioX, double ratioY) {
 		try {	
 			int currentWidthFixed32 = Fixed32.toFP(image.getWidth());
 			int currentHeightFixed32 = Fixed32.toFP(image.getHeight());
 			double ratio = (double)ratioX / (double) ratioY;
 			double w = (double) image.getWidth() * ratioX;
 			double h = (double)image.getHeight() * ratioY;
 			int width = (int) w;
 			int height = (int) h;
 			int requiredWidthFixed32 = Fixed32.toFP(width);
 			int requiredHeightFixed32 = Fixed32.toFP(height);
 			int scaleXFixed32 = Fixed32.div(currentWidthFixed32, requiredWidthFixed32);
 			int scaleYFixed32 = Fixed32.div(currentHeightFixed32, requiredHeightFixed32);
 			image = image.scaleImage32(scaleXFixed32, scaleYFixed32);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return image.getBitmap();
 	}
 	private static Bitmap getSizeImage(String bitmap) {
 		switch (Const.SCREEN) {
 			case Const.LARGE_SCREEN:
 				b = Bitmap.getBitmapResource(bitmap);
 				break;
 			case Const.MEDIUM_SCREEN:
 				b = getScaledBitmapImage(bitmap, 3, 4);
 				break;
 			default:
 				b = getScaledBitmapImage(bitmap, 2, 3);
 				break;
 		}
 		return b;
 	}
 	public static Bitmap getSizeImage(EncodedImage bitmap) {
 		switch (Const.SCREEN) {
 			case Const.LARGE_SCREEN:
 				b = bitmap.getBitmap();
 				break;
 			case Const.MEDIUM_SCREEN:
 				b = getScaledBitmapImage(bitmap, 3, 4);
 				break;
 			default:
 				b = getScaledBitmapImage(bitmap, 2, 3);
 				break;
 		}
 		return b;
 	}
 	
 	
 	public static final short SMALL_SCREEN = 0;
 	public static final short MEDIUM_SCREEN = 1;
 	public static final short LARGE_SCREEN = 2;
 	public static short SCREEN = LARGE_SCREEN;
 	
 	public static void getSettings() {
 		SettingsBean _instance = SettingsBean.getSettings();
 		int width = Const.getWidth();
 		if (width <= 240) {
 			_instance.setSize(Const.SMALL_SCREEN);
 			Const.SCREEN = Const.SMALL_SCREEN;
 			Const.FONT = Const.SMALL_FONT;
 		} else if (width >= 360) {
 			_instance.setSize(Const.LARGE_SCREEN);
 			Const.SCREEN = Const.LARGE_SCREEN;
 			Const.FONT = Const.LARGE_FONT;
 		} else {
 			_instance.setSize(Const.MEDIUM_SCREEN);
 			Const.SCREEN = Const.MEDIUM_SCREEN;
 			Const.FONT = Const.MEDIUM_FONT;
 		}
 		if (Const.getWidth() > Const.getHeight()) {
 			Const.PORTRAIT = false;
 		}
 	}
 	
 	/*
 	 * NAVIGATION
 	 */
 	public static int GOTOSCREEN;
 	public static int FROMSCREEN;
 	public static final short LOGINSCREEN = 0;
 	public static final short ALBUMSCREEN = 1;
 	public static final short MENUSCREEN = 2;
 	public static final short REGISTERSCREEN = 3;
 	public static final short PROFILESCREEN = 4;
 	public static final short BALANCESCREEN = 5;
 	public static final short NOTIFICATIONSCREEN = 6;
 	public static final short FRIENDSSCREEN = 7;
 	
 	public static final short USERDET = 0;
 	public static final short USERCAT = 1;
 	public static final short SUBCAT = 2;
 	public static final short CARDS = 3;
 	
 	public static final short LOGOUT = -1;
 	public static final short ALBUMS = -11;
 	public static final short PLAY = -12;
 	public static final short DECKS = -17;
 	public static final short SHOP = -13;
 	public static final short AUCTIONS = -14;
 	public static final short BALANCE = -15;
 	public static final short PROFILE = -16;
 	public static final short NOTIFICATIONS = -17;
 	public static final short RANKINGS = -18;
 	public static final short FRIENDS = -19;
 	public static final short REDEEM = -10;
 	
 	public static final short CACHE = -1;
 	public static final short MYCARD = -2;
 	public static final short NEWCARDS = -3;
 	public static final short UPDATES = -4;
 	public static final short BACK = -5;
 	
 	public static final short PADDING = 20;
 	
 	/*
 	 * DISPLAY VALUES
 	 */
 	public static final int SMALL_FONT = 10;
 	public static final int MEDIUM_FONT = 12;
 	public static final int LARGE_FONT = 14;
 	public static int FONT = LARGE_FONT;
 	public static int TYPE = Font.PLAIN;
 	public static final int INCREASE_FONT = 2;
 	
 	public static final int FONTCOLOR = Color.GRAY;
 	public static final int BACKCOLOR = Color.BLACK;
 	public static final int BUTTONCOLOR = Color.WHITE;
 	public static final int SELECTEDCOLOR = Color.RED;
 	
 	public static final int getWidth() {
 		return Display.getWidth();
 	}
 	public static final int getLogoHeight() {
 		return getLogo().getHeight();
 	}
 	public static final int getHeight() {
 		return Display.getHeight();
 	}
 	public static final int getCardHeight() {
 		if (Const.PORTRAIT) {
 			return ((Const.getHeight()-getButtonCentre().getHeight()));
 		} else {
 			return Const.getHeight();
 		}
 	}
 	public static final int getAppHeight() {
 		if (Const.PORTRAIT) {
 			return ((Const.getHeight()-getButtonCentre().getHeight()));
 		} else {
 			return (int)((((double)((Const.getHeight()-getButtonCentre().getHeight())))*ratio));
 		}
 	}
 	public static final int getCardWidth() {
 		if (Const.PORTRAIT) {
 			return Const.getWidth();
 		} else {
 			return Const.getHeight();
 			//return ((Const.getWidth()-getButtonCentre().getHeight())-Const.PADDING);
 		}
 	}
 	public static final int getUsableHeight() {
 		return getHeight()-(getButtonCentre().getHeight()+getLogo().getHeight()+PADDING);
 	}
 	public static final int getButtonHeight() {
 		return getListboxSelLeftEdge().getHeight();
 	}
 	
 	public static Bitmap rotate(Bitmap bitmap, int angle) throws Exception
 	{
 		if(angle == 0)
 		{
 			return null; 
 		}
 		else if(angle != 180 && angle != 90 && angle != 270)
 		{
 			throw new Exception("Invalid angle");
 		}
 	 
 		int width = bitmap.getWidth();
 		int height = bitmap.getHeight();
 	 
 		int[] rowData = new int[width];
 		int[] rotatedData = new int[width * height];
 	 
 		int rotatedIndex = 0;
 	 
 		for(int i = 0; i < height; i++)
 		{
 			bitmap.getARGB(rowData, 0, width, 0, i, width, 1);
 	 
 			for(int j = 0; j < width; j++)
 			{
 				rotatedIndex = 
 					angle == 90 ? (height - i - 1) + j * height : 
 					(angle == 270 ? i + height * (width - j - 1) : 
 						width * height - (i * width + j) - 1
 					);
 	 
 				rotatedData[rotatedIndex] = rowData[j];
 			}
 		}
 
 		javax.microedition.lcdui.Image tmpJ2MEImage = null;
 		if(angle == 90 || angle == 270)
 		{
 			tmpJ2MEImage = javax.microedition.lcdui.Image.createRGBImage(rotatedData, height, width, true);
 		}
 		else
 		{
 			tmpJ2MEImage = javax.microedition.lcdui.Image.createRGBImage(rotatedData, width, height, true);
 		}
 		
 		rowData = null;
 		rotatedData = null;
 		
 		int rotWidth = tmpJ2MEImage.getWidth();
 		int rotHeight = tmpJ2MEImage.getHeight();
 		rotatedData = new int[rotWidth * rotHeight];
 		tmpJ2MEImage.getRGB(rotatedData, 0, rotWidth, 0, 0, rotWidth, rotHeight);
 		tmpJ2MEImage = null;
 		
 		Bitmap rotatedBitmap = new Bitmap(rotWidth, rotHeight);
 		rotatedBitmap.setARGB(rotatedData, 0, rotWidth, 0, 0, rotWidth, rotHeight);
 		rotatedData = null;
 		return rotatedBitmap;		
 	}
 	/*
 	 * STRING VALUES - Localization values
 	 */
 	public static String login = "Login";
 	public static String register = "Register";
 	public static String exit = "Exit";
 	public static String back = "Back";
 	public static String purchase = "purchase";
 	public static String home = "Home";
 	public static String confirm = "Confirm";
 	public static String bid = "Bid";
 	public static String newupdate = " Update Available";
 	public static String updatemsg = "New update available. Select \"Update\" to install, or \"Later\" to continue.";
 	public static String contacts = "Contacts";
 	public static String send = "Share";
 	public static String auction = "Auction";
 	public static String search = "Search";
 	public static String term = " Search Term:";
 	public static String code = " Redeem Code:";
 	public static String usern = " Username:";
 	public static String email = " Email:";
 	public static String credits = "Credits";
 	public static String openingbid = " Opening Bid: ";
 	public static String currentbid = " Current Bid: ";
 	public static String buynowprice = " Buy Now Price: ";
 	public static String enddate = " End Date: ";
 	public static String yourbid = " Your Bid:";
 	public static String conti = "Continue";
 	public static String down = "Update";
 	public static String later = "Later";
 	public static String yes = "Yes";
 	public static String no = "No";
 	public static String save = "Save";
 	public static String create = "Create";
 	public static String flip = "Flip";
 	public static String options = "Options";
 	public static String logout = "Logout";
 	public static String logOut = "Log Out";
 	public static String albums = "Albums";
 	public static String play = "Play";
 	public static String decks = "Decks";
 	public static String con = "Continue";
 	public static String shop = "Shop";
 	public static String auctions = "Auctions";
 	public static String redeem = "Redeem";
 	public static String balance = "Credits";
 	public static String profile = "Profile";
 	public static String notification = "Notifications";
 	public static String friend = "Friends";
 	public static String invitefriend = "Invite Friends";
 	public static String rankings = "Rankings";
 	public static String friendranks = "Friend Ranks";
 	public static String all_auctions = "All Auctions";
 	public static String my_auctions = "My Auctions";
 	public static String create_auction = "Create New Auction";
 	public static String newgame = "New Game";
 	public static String newdeck = "New Deck";
 	public static String delete_deck = "Delete Deck";
 	public static String addcard = "Add Card";
 	public static String placebid = "Place bid";
 	public static String buynow = "Buy now";
 	public static String close = "Close";
 	public static String accept = "Accept";
 	public static String reject = "Reject";
 	
 	public static String quantity = "Quantity: ";
 	
 	public static String notes = " Note";
 	public static String name = " Full Name";
 	public static String surname = " Username";
 	public static String cell = " Cell Number";
 	public static String age = " Email Address";
 	public static String gender = " Password";
 	public static String referrer = " Referrer";
 	
 	public static String remove = "Are you sure you wish to delete this card?";
 	
 	public static final String getOS() {
 		return "&os=Blackberry";
 	}
 	public static final String getIMEI() {
 		String imei = "";
 		if (isCDMA()) {
 			imei = ""+CDMAInfo.getESN();
 		} else if (isIDEN()){
 			imei = IDENInfo.imeiToString(IDENInfo.getIMEI());
 		} else {
 			imei = GPRSInfo.imeiToString(GPRSInfo.getIMEI());
 		}
 		return "&imei="+imei.trim();
 	}
 	public static final String getIMSI() {
 		String imsi = "";
 		if (isCDMA()) {
 			imsi = new String(CDMAInfo.getIMSI());
 		} else {
 			imsi = "";
 			try {
 				imsi = GPRSInfo.imeiToString(SIMCardInfo.getIMSI());
 			} catch (Exception e) {
 				imsi = "";
 			}
 		}
 		return "&imsi="+imsi.trim();
 	}
 	public static final String getMSISDN() {
 		return "&msisdn="+Phone.getDevicePhoneNumber(false);
 	}
 	public final static boolean isCDMA() {
         return RadioInfo.getNetworkType() == RadioInfo.NETWORK_CDMA;
     }
 	public final static boolean isIDEN() {
         return RadioInfo.getNetworkType() == RadioInfo.NETWORK_IDEN;
     }
 	
 	/*
 	 * UTILITIES
 	 */
 	public static String err_null_url = "Null URL passed in";
 	public static String http = "http";
 	public static String err_url_htt = "URL not http or https";
 	
	public static String url = "http://mytcg.net/_phone/topcar/?";
 	public static String userdetails = "userdetails=1";
 	public static String profiledetails = "profiledetails=1";
 	public static String creditlog = "creditlog=1";
 	public static String notifications = "notifications=1";
 	public static String friends = "&friends=1";
 	public static String saveprofiledetail = "saveprofiledetail=1";
 	public static String viewgamelog = "viewgamelog=1";
 	public static String registeruser = "registeruser=1";
 	/*
 	 * $username = $_REQUEST['username'];
 	$password = $_REQUEST['password'];
 	$email = $_REQUEST['email'];
 	$name = $_REQUEST['name'];
 	$cell = $_REQUEST['cell'];
 	 * 
 	 */
 	public static String userfullname = "&username=";
 	public static String useremail = "&email=";
 	public static String userpassword = "&password=";
 	public static String userreferrer = "&referer=";
 	public static String usercategories = "usercategories=1";
 	public static String productcategories = "productcategories=2";
 	public static String freebiecategories = "productcategories=1";
 	public static String leaders = "leaders=1";
 	public static String leaderboard = "leaderboard=";
 	public static String categoryproducts = "categoryproducts=2";
 	public static String freebieproducts = "categoryproducts=1";
 	public static String auctioncategories="auctioncategories=1";
 	public static String getusergames="getusergames=1";
 	public static String playablecategories="playablecategories=1";
 	public static String startnewgame = "newgame=1";
 	public static String loadgame = "loadgame=1";
 	public static String continuegame = "continuegame=1";
 	public static String selectstat = "selectstat=1";
 	public static String getuserdecks="getuserdecks=1";
 	public static String getdecks="getdecks=1";
 	public static String getcardsindeck="getcardsindeck=1";
 	public static String cardsincategorynotdeck= "cardsincategorynotdeck=";
 	public static String getalldecks="getalldecks=1";
 	public static String addtodeck="addtodeck=1";
 	public static String categoryauction = "categoryauction=1";
 	public static String createauction = "createauction=1";
 	public static String createdeck = "createdeck=1";
 	public static String deletedeck = "deletedeck=1";
 	public static String removefromdeck = "removefromdeck=1";
 	public static String userauction = "userauction=1";
 	public static String buyauctionnow = "buyauctionnow=1";
 	public static String auctionbid = "auctionbid=1";
 	public static String savecard = "savecard=";
 	public static String deletecard = "deletecard=";
 	public static String rejectcard = "rejectcard=";
 	public static String showall = "&showall=0";
 	public static String cardsincategory = "cardsincategory=";
 	public static String buyproduct = "buyproduct=";
 	public static String second = "&seconds=";
 	public static String savenote = "savenote=";
 	public static String trade = "tradecard=";
 	public static String seek = "search=";
 	public static String rdm = "redeemcode=";
 	public static String update = "update=";
 	public static String trademethod = "&trademethod=phone_number&detail=";
 	public static String sendnote = "&note=";
 	public static String usercard_id = "&usercard_id=";
 	public static String cardid = "&cardid=";
 	public static String card_id = "&card_id=";
 	public static String deckid = "&deckid=";
 	public static String deck_id = "&deck_id=";
 	public static String description = "&description=";
 	public static String category_id = "&category_id=";
 	public static String height = "&height=";
 	public static String bbheight = "&bbheight=";
 	public static String width = "&width=";
 	public static String freebie = "&freebie=";
 	public static String subcategories = "usersubcategories=1&category=";
 	public static String cat = "<usercategories><album><album><albumid>-999</albumid><hascards>false</hascards><albumname>Empty</albumname></album></usercategories>";
 	public static String productcat = "<cardcategories><album><albumid>-999</albumid><albumname>Empty</albumname></album></cardcategories>";
 	public static String card = "<cardsincategory><card><cardid>-999</cardid><description>Empty</description><quantity>0</quantity><backurl></backurl></card></cardsincategory>";
 	
 	
 	public static String web = "Web Address";
 	public static String phone = "Mobile No";
 	public static String user = " Username";
 	public static String fullname = " Full Name";
 	public static String password = " Password";
 	public static String eml = "Email";
 	public static String sig = "\n\n\nSent from my Mobidex";
 	
 	
 	public static String cards = "/cards/";
 	public static String products = "/products/";
 	public static final int cards_length = cards.length();
 	public static final int products_length = products.length();
 	public static String png = ".png";
 	
 	public static boolean store = false;
 	public static int first = 0;
 	public static String storage = "file:///SDCard/BlackBerry/MAStore/";
 	
 	public static String getStorage() {
 		while (!store) {
 			FileConnection _file = null;
 			try {
 				_file = (FileConnection)Connector.open(storage);
 				if (!_file.exists()) {
 					_file.mkdir();
 				}
 				_file.close();
 				store = true;
 			} catch (FileIOException e) {
 				if (first==0) {
 					storage = System.getProperty("fileconn.dir.photos") + "MAStore/";
 					++first;
 				} else if (first==1) {
 					storage = System.getProperty("fileconn.dir.memorycard.photo") + "MAStore/";
 					++first;
 				} else {
 					storage = "file:///store/home/user/pictures/";
 					store = false;
 				}
 			} catch (Exception e) {
 				storage = "file:///store/home/user/pictures/";
 				store = true;
 			}
 		}
 		return storage;
 	}
 	
 	public static boolean debug = true;
 	public static GameCardsHome app;
 	
 	public static final String tru = "true";
 	
 	public static String bes_con = ";deviceside=false";
 	public static String bis_con = bes_con+";connectionUID=";
 	public static String wifi_con = ";deviceside=true;interface=wifi";
 	public static String wap_con = ";deviceside=true;ConnectionUID=";
 	public static String tcp_con = ";deviceside=true";
 	
 	public static String ippp = "ippp";
 	public static String gpmds = "gpmds";
 	public static String wptcp = "wptcp";
 	public static String wifi = "wifi";
 	public static String wap2 = "wap2";
 	public static String waptrans = "waptrans";
 	public static String wap = "wap";
 	public static String browserconfig = "browserconfig";
 	public static String sms1992 = "sms://:1992";
 	public static String cmime = "CMIME";
 	public static String smsdata = "sms://";
 	public static String mmsdata = "mms://";
 	public static String internet = "internet";
 	
 	public static String download_url = "";
 	public static String loadingurl = "";
 	public static String loadingurlflip = "";
 	
 	public static boolean processUserDetails(String val) {
 		int fromIndex;
     	if ((fromIndex = val.indexOf(xml_userdetails)) != -1) {
     		SettingsBean _instance = SettingsBean.getSettings();
     		if ((fromIndex = val.indexOf(xml_email)) != -1) {
     			_instance.setEmail(val.substring(fromIndex+xml_email_length, val.indexOf(xml_email_end, fromIndex)));
     		}
     		if ((fromIndex = val.indexOf(xml_credits)) != -1) {
     			_instance.setCredits(val.substring(fromIndex+xml_credits_length, val.indexOf(xml_credits_end, fromIndex)));
     		}
     		if ((fromIndex = val.indexOf(Const.xml_loadingurl)) != -1) {
 				loadingurl = val.substring(fromIndex+Const.xml_loadingurl_length, val.indexOf(Const.xml_loadingurl_end, fromIndex));
 			}
 			if ((fromIndex = val.indexOf(Const.xml_loadingflipurl)) != -1) {
     			loadingurlflip = val.substring(fromIndex+Const.xml_loadingflipurl_length, val.indexOf(Const.xml_loadingflipurl_end, fromIndex));
     		}
 			load = new ImageLoader(loadingurl);
 			load2 = new ImageLoader(loadingurlflip);
 			_instance.loading = loadingurl.substring(loadingurl.indexOf(Const.cards)+Const.cards_length, loadingurl.indexOf(Const.png));
 			_instance.loadingflip = loadingurlflip.substring(loadingurlflip.indexOf(Const.cards)+Const.cards_length, loadingurlflip.indexOf(Const.png));
     		_instance.setAuthenticated(true);
     		SettingsBean.saveSettings(_instance);
     		_instance = null;
     		return true;
     	}
     	return false;
 	}
 	
 	/*
 	 * ICONS
 	 */
 	private static Bitmap missingcardthumb;
 	private static String sz_missingcardthumb = "missingcardthumb.png";
 	public static Bitmap getEmptyThumb() {
 		if (missingcardthumb == null) {
 			missingcardthumb = getSizeImage(sz_missingcardthumb);   
 		}
 		return missingcardthumb;
 	}
 	
 	private static Bitmap note;
 	private static String sz_note = "note.png";
 	public static Bitmap getNote() {
 		if (note == null) {
 			note = getSizeImage(sz_note);   
 		}
 		return note;
 	}
 	
 	private static Bitmap thumbnaildisplaycentre;
 	private static String sz_thumbnaildisplaycentre = "thumbnail_display_centre.png";
 	public static Bitmap getThumbCentre() {
 		if (thumbnaildisplaycentre == null) {
 			thumbnaildisplaycentre = getSizeImage(sz_thumbnaildisplaycentre);   
 		}
 		return thumbnaildisplaycentre;
 	}
 	
 	private static Bitmap thumbnaildisplayrightedge;
 	private static String sz_thumbnaildisplayrightedge = "thumbnail_display_right_edge.png";
 	public static Bitmap getThumbRightEdge() {
 		if (thumbnaildisplayrightedge == null) {
 			thumbnaildisplayrightedge = getSizeImage(sz_thumbnaildisplayrightedge);   
 		}
 		return thumbnaildisplayrightedge;
 	}
 	
 	private static Bitmap loading;
 	private static String sz_loading = "loading.png";
 	public static Bitmap getLoading() {
 		if (loading == null) {
 			loading = getSizeImage(sz_loading);   
 		}
 		return loading;
 	}
 	
 	private static Bitmap loadingthumb;
 	private static String sz_loadingthumb = "loadingthumb.png";
 	public static Bitmap getThumbLoading() {
 		if (loadingthumb == null) {
 			loadingthumb = getSizeImage(sz_loadingthumb);   
 		}
 		return loadingthumb;
 	}
 	
 	private static Bitmap textmiddle;
 	private static String sz_textmiddle = "text_middle.png";
 	public static Bitmap getTextMiddle() {
 		if (textmiddle == null) {
 			textmiddle = getSizeImage(sz_textmiddle);   
 		}
 		return textmiddle;
 	}
 	
 	private static Bitmap textmiddletop;
 	private static String sz_textmiddletop = "text_middle_top.png";
 	public static Bitmap getTextMiddleTop() {
 		if (textmiddletop == null) {
 			textmiddletop = getSizeImage(sz_textmiddletop);   
 		}
 		return textmiddletop;
 	}
 	
 	private static Bitmap textmiddlebottom;
 	private static String sz_textmiddlebottom = "text_middle_bottom.png";
 	public static Bitmap getTextMiddleBottom() {
 		if (textmiddlebottom == null) {
 			textmiddlebottom = getSizeImage(sz_textmiddlebottom);   
 		}
 		return textmiddlebottom;
 	}
 	
 	private static Bitmap textleftmiddle;
 	private static String sz_textleftmiddle = "text_left_middle.png";
 	public static Bitmap getTextLeftMiddle() {
 		if (textleftmiddle == null) {
 			textleftmiddle = getSizeImage(sz_textleftmiddle);   
 		}
 		return textleftmiddle;
 	}
 	
 	private static Bitmap textlefttop;
 	private static String sz_textlefttop = "text_left_top.png";
 	public static Bitmap getTextLeftTop() {
 		if (textlefttop == null) {
 			textlefttop = getSizeImage(sz_textlefttop);   
 		}
 		return textlefttop;
 	}
 	
 	private static Bitmap textleftbottom;
 	private static String sz_textleftbottom = "text_left_bottom.png";
 	public static Bitmap getTextLeftBottom() {
 		if (textleftbottom == null) {
 			textleftbottom = getSizeImage(sz_textleftbottom);   
 		}
 		return textleftbottom;
 	}
 	
 	private static Bitmap textrightmiddle;
 	private static String sz_textrightmiddle = "text_right_middle.png";
 	public static Bitmap getTextRightMiddle() {
 		if (textrightmiddle == null) {
 			textrightmiddle = getSizeImage(sz_textrightmiddle);   
 		}
 		return textrightmiddle;
 	}
 	
 	private static Bitmap textrightttop;
 	private static String sz_textrighttop = "text_right_top.png";
 	public static Bitmap getTextRightTop() {
 		if (textrightttop == null) {
 			textrightttop = getSizeImage(sz_textrighttop);   
 		}
 		return textrightttop;
 	}
 	
 	private static Bitmap textrightbottom;
 	private static String sz_textrightbottom = "text_right_bottom.png";
 	public static Bitmap getTextRightBottom() {
 		if (textrightbottom == null) {
 			textrightbottom = getSizeImage(sz_textrightbottom);   
 		}
 		return textrightbottom;
 	}
 	
 	private static Bitmap listboxcentre;
 	private static String sz_listboxcentre = "listbox_centre.png";
 	public static Bitmap getListboxCentre() {
 		if (listboxcentre == null) {
 			listboxcentre = getSizeImage(sz_listboxcentre);   
 		}
 		return listboxcentre;
 	}
 	
 	private static Bitmap listboxselcentre;
 	private static String sz_listboxselcentre = "listbox_sel_centre.png";
 	public static Bitmap getListboxSelCentre() {
 		if (listboxselcentre == null) {
 			listboxselcentre = getSizeImage(sz_listboxselcentre);   
 		}
 		return listboxselcentre;
 	}
 	
 	private static Bitmap listboxleftedge;
 	private static String sz_listboxleftedge = "listbox_left_edge.png";
 	public static Bitmap getListboxLeftEdge() {
 		if (listboxleftedge == null) {
 			listboxleftedge = getSizeImage(sz_listboxleftedge);   
 		}
 		return listboxleftedge;
 	}
 	
 	private static Bitmap listboxrightedge;
 	private static String sz_listboxrightedge = "listbox_right_edge.png";
 	public static Bitmap getListboxRightEdge() {
 		if (listboxrightedge == null) {
 			listboxrightedge = getSizeImage(sz_listboxrightedge);   
 		}
 		return listboxrightedge;
 	}
 	
 	private static Bitmap listboxselrightedge;
 	private static String sz_listboxselrightedge = "listbox_sel_right_edge.png";
 	public static Bitmap getListboxSelRightEdge() {
 		if (listboxselrightedge == null) {
 			listboxselrightedge = getSizeImage(sz_listboxselrightedge);   
 		}
 		return listboxselrightedge;
 	}
 	
 	private static Bitmap listboxselleftedge;
 	private static String sz_listboxselleftedge = "listbox_sel_left_edge.png";
 	public static Bitmap getListboxSelLeftEdge() {
 		if (listboxselleftedge == null) {
 			listboxselleftedge = getSizeImage(sz_listboxselleftedge);   
 		}
 		return listboxselleftedge;
 	}
 	
 	private static Bitmap buttoncentre;
 	private static String sz_buttoncentre = "button_centre.png";
 	public static Bitmap getButtonCentre() {
 		if (buttoncentre == null) {
 			buttoncentre = getSizeImage(sz_buttoncentre);   
 		}
 		return buttoncentre;
 	}
 	
 	private static Bitmap buttonleftedge;
 	private static String sz_buttonleftedge = "button_left_edge.png";
 	public static Bitmap getButtonLeftEdge() {
 		if (buttonleftedge == null) {
 			buttonleftedge = getSizeImage(sz_buttonleftedge);   
 		}
 		return buttonleftedge;
 	}
 	
 	private static Bitmap buttonrightedge;
 	private static String sz_buttonrightedge = "button_right_edge.png";
 	public static Bitmap getButtonRightEdge() {
 		if (buttonrightedge == null) {
 			buttonrightedge = getSizeImage(sz_buttonrightedge);   
 		}
 		return buttonrightedge;
 	}
 	
 	private static Bitmap buttonselcentre;
 	private static String sz_buttonselcentre = "button_sel_centre.png";
 	public static Bitmap getButtonSelCentre() {
 		if (buttonselcentre == null) {
 			buttonselcentre = getSizeImage(sz_buttonselcentre);   
 		}
 		return buttonselcentre;
 	}
 	
 	private static Bitmap buttonselleftedge;
 	private static String sz_buttonselleftedge = "button_sel_left_edge.png";
 	public static Bitmap getButtonSelLeftEdge() {
 		if (buttonselleftedge == null) {
 			buttonselleftedge = getSizeImage(sz_buttonselleftedge);   
 		}
 		return buttonselleftedge;
 	}
 	
 	private static Bitmap buttonselrightedge;
 	private static String sz_buttonselrightedge = "button_sel_right_edge.png";
 	public static Bitmap getButtonSelRightEdge() {
 		if (buttonselrightedge == null) {
 			buttonselrightedge = getSizeImage(sz_buttonselrightedge);   
 		}
 		return buttonselrightedge;
 	}
 	
 	private static Bitmap background;
 	private static String sz_background = "background.png";
 	public static Bitmap getBackground() {
 		if (background == null) {
 			background = getSizeImage(sz_background);   
 		}
 		return background;
 	}
 	
 	private static Bitmap logo;
 	private static String sz_logo = "crosslogo.png";
 	public static Bitmap getLogo() {
 		if (logo == null) {
 			logo = getSizeImage(sz_logo);   
 		}
 		return logo;
 	}
 	
 	private static Bitmap logoleft;
 	private static String sz_logoleft = "crosslogo_left.png";
 	public static Bitmap getLogoLeft() {
 		if (logoleft == null) {
 			logoleft = getSizeImage(sz_logoleft);   
 		}
 		return logoleft;
 	}
 	private static Bitmap logoright;
 	private static String sz_logoright = "crosslogo_right.png";
 	public static Bitmap getLogoRight() {
 		if (logoright == null) {
 			logoright = getSizeImage(sz_logoright);   
 		}
 		return logoright;
 	}
 	
 	private static Bitmap editboxlefttop;
 	private static String sz_editboxlefttop = "editbox_left_top.png";
 	public static Bitmap getEditboxLeftTop() {
 		if (editboxlefttop == null) {
 			editboxlefttop = getSizeImage(sz_editboxlefttop);   
 		}
 		return editboxlefttop;
 	}
 	private static Bitmap editboxleftmiddle;
 	private static String sz_editboxleftmiddle = "editbox_left_middle.png";
 	public static Bitmap getEditboxLeftMiddle() {
 		if (editboxleftmiddle == null) {
 			editboxleftmiddle = getSizeImage(sz_editboxleftmiddle);   
 		}
 		return editboxleftmiddle;
 	}
 	private static Bitmap editboxleftbottom;
 	private static String sz_editboxleftbottom = "editbox_left_bottom.png";
 	public static Bitmap getEditboxLeftBottom() {
 		if (editboxleftbottom == null) {
 			editboxleftbottom = getSizeImage(sz_editboxleftbottom);   
 		}
 		return editboxleftbottom;
 	}
 	
 	private static Bitmap editboxrighttop;
 	private static String sz_editboxrighttop = "editbox_right_top.png";
 	public static Bitmap getEditboxRightTop() {
 		if (editboxrighttop == null) {
 			editboxrighttop = getSizeImage(sz_editboxrighttop);   
 		}
 		return editboxrighttop;
 	}
 	private static Bitmap editboxrightmiddle;
 	private static String sz_editboxrightmiddle = "editbox_right_middle.png";
 	public static Bitmap getEditboxRightMiddle() {
 		if (editboxrightmiddle == null) {
 			editboxrightmiddle = getSizeImage(sz_editboxrightmiddle);   
 		}
 		return editboxrightmiddle;
 	}
 	private static Bitmap editboxrightbottom;
 	private static String sz_editboxrightbottom = "editbox_right_bottom.png";
 	public static Bitmap getEditboxRightBottom() {
 		if (editboxrightbottom == null) {
 			editboxrightbottom = getSizeImage(sz_editboxrightbottom);   
 		}
 		return editboxrightbottom;
 	}
 	
 	private static Bitmap editboxmiddletop;
 	private static String sz_editboxmiddletop = "editbox_middle_top.png";
 	public static Bitmap getEditboxMiddleTop() {
 		if (editboxmiddletop == null) {
 			editboxmiddletop = getSizeImage(sz_editboxmiddletop);   
 		}
 		return editboxmiddletop;
 	}
 	private static Bitmap editboxmiddlemiddle;
 	private static String sz_editboxmiddlemiddle = "editbox_middle_middle.png";
 	public static Bitmap getEditboxMiddleMiddle() {
 		if (editboxmiddlemiddle == null) {
 			editboxmiddlemiddle = getSizeImage(sz_editboxmiddlemiddle);   
 		}
 		return editboxmiddlemiddle;
 	}
 	private static Bitmap editboxmiddlebottom;
 	private static String sz_editboxmiddlebottom = "editbox_middle_bottom.png";
 	public static Bitmap getEditboxMiddleBottom() {
 		if (editboxmiddlebottom == null) {
 			editboxmiddlebottom = getSizeImage(sz_editboxmiddlebottom);   
 		}
 		return editboxmiddlebottom;
 	}
 
 	private static Bitmap editboxsellefttop;
 	private static String sz_editboxsellefttop = "editbox_sel_left_top.png";
 	public static Bitmap getEditboxSelLeftTop() {
 		if (editboxsellefttop == null) {
 			editboxsellefttop = getSizeImage(sz_editboxsellefttop);   
 		}
 		return editboxsellefttop;
 	}
 	private static Bitmap editboxselleftmiddle;
 	private static String sz_editboxselleftmiddle = "editbox_sel_left_middle.png";
 	public static Bitmap getEditboxSelLeftMiddle() {
 		if (editboxselleftmiddle == null) {
 			editboxselleftmiddle = getSizeImage(sz_editboxselleftmiddle);   
 		}
 		return editboxselleftmiddle;
 	}
 	private static Bitmap editboxselleftbottom;
 	private static String sz_editboxselleftbottom = "editbox_sel_left_bottom.png";
 	public static Bitmap getEditboxSelLeftBottom() {
 		if (editboxselleftbottom == null) {
 			editboxselleftbottom = getSizeImage(sz_editboxselleftbottom);   
 		}
 		return editboxselleftbottom;
 	}
 	
 	private static Bitmap editboxselrighttop;
 	private static String sz_editboxselrighttop = "editbox_sel_right_top.png";
 	public static Bitmap getEditboxSelRightTop() {
 		if (editboxselrighttop == null) {
 			editboxselrighttop = getSizeImage(sz_editboxselrighttop);   
 		}
 		return editboxselrighttop;
 	}
 	private static Bitmap editboxselrightmiddle;
 	private static String sz_editboxselrightmiddle = "editbox_sel_right_middle.png";
 	public static Bitmap getEditboxSelRightMiddle() {
 		if (editboxselrightmiddle == null) {
 			editboxselrightmiddle = getSizeImage(sz_editboxselrightmiddle);   
 		}
 		return editboxselrightmiddle;
 	}
 	private static Bitmap editboxselrightbottom;
 	private static String sz_editboxselrightbottom = "editbox_sel_right_bottom.png";
 	public static Bitmap getEditboxSelRightBottom() {
 		if (editboxselrightbottom == null) {
 			editboxselrightbottom = getSizeImage(sz_editboxselrightbottom);   
 		}
 		return editboxselrightbottom;
 	}
 	
 	private static Bitmap editboxselmiddletop;
 	private static String sz_editboxselmiddletop = "editbox_sel_middle_top.png";
 	public static Bitmap getEditboxSelMiddleTop() {
 		if (editboxselmiddletop == null) {
 			editboxselmiddletop = getSizeImage(sz_editboxselmiddletop);   
 		}
 		return editboxselmiddletop;
 	}
 	private static Bitmap editboxselmiddlemiddle;
 	private static String sz_editboxselmiddlemiddle = "editbox_sel_middle_middle.png";
 	public static Bitmap getEditboxSelMiddleMiddle() {
 		if (editboxselmiddlemiddle == null) {
 			editboxselmiddlemiddle = getSizeImage(sz_editboxselmiddlemiddle);   
 		}
 		return editboxselmiddlemiddle;
 	}
 	private static Bitmap editboxselmiddlebottom;
 	private static String sz_editboxselmiddlebottom = "editbox_sel_middle_bottom.png";
 	public static Bitmap getEditboxSelMiddleBottom() {
 		if (editboxselmiddlebottom == null) {
 			editboxselmiddlebottom = getSizeImage(sz_editboxselmiddlebottom);   
 		}
 		return editboxselmiddlebottom;
 	}
 	private static Bitmap log_out;
 	private static String sz_log_out = "logout.png";
 	public static Bitmap getLogOut() {
 		if (log_out == null) {
 			log_out = getSizeImage(sz_log_out);   
 		}
 		return log_out;
 	}
 	/*
 	 * XML
 	 */
 	
 	public static final String xml_result = "<result>";
 	public static final int xml_result_length = xml_result.length();
 	public static final String xml_result_end = "</result>";
 	public static final String xml_success = "<success>";
 	public static final int xml_success_length = xml_success.length();
 	public static final String xml_success_end = "</success>";
 	public static final int xml_error_end_length = xml_result_end.length();
 	public static final String xml_categoryproducts = "<categoryproducts>";
 	public static final int xml_categoryproducts_length = xml_categoryproducts.length(); 
 	public static final String xml_categoryproducts_end = "</categoryproducts>";
 	public static final int xml_categoryproducts_end_length = xml_categoryproducts_end.length();
 	public static final String xml_productcategories = "<cardcategories>";
 	public static final int xml_productcategories_length = xml_productcategories.length(); 
 	public static final String xml_productcategories_end = "</cardcategories>";
 	public static final int xml_productcategories_end_length = xml_productcategories_end.length();
 	public static final String xml_usercategories = "<usercategories>";
 	public static final int xml_usercategories_length = xml_usercategories.length(); 
 	public static final String xml_usercategories_end = "</usercategories>";
 	public static final int xml_usercategories_end_length = xml_usercategories_end.length();
 	public static final String xml_cardcategories = "<cardcategories>";
 	public static final int xml_cardcategories_length = xml_cardcategories.length(); 
 	public static final String xml_cardcategories_end = "</cardcategories>";
 	public static final int xml_cardcategories_end_length = xml_cardcategories_end.length();
 	public static final String xml_categories = "<categories>";
 	public static final int xml_categories_length = xml_categories.length(); 
 	public static final String xml_categories_end = "</categories>";
 	public static final int xml_categories_end_length = xml_categories_end.length();
 	public static final String xml_logs = "<logs>";
 	public static final int xml_logs_length = xml_logs.length(); 
 	public static final String xml_logs_end = "</logs>";
 	public static final int xml_logs_end_length = xml_logs_end.length();
 	public static final String xml_log = "<log>";
 	public static final int xml_log_length = xml_log.length(); 
 	public static final String xml_log_end = "</log>";
 	public static final int xml_log_end_length = xml_log_end.length();
 	public static final String xml_category = "<category>";
 	public static final int xml_category_length = xml_category.length(); 
 	public static final String xml_category_end = "</category>";
 	public static final int xml_category_end_length = xml_category_end.length();
 	public static final String xml_profiledetails = "<profiledetails>";
 	public static final int xml_profiledetails_length = xml_profiledetails.length(); 
 	public static final String xml_profiledetails_end = "</profiledetails>";
 	public static final int xml_profiledetails_end_length = xml_profiledetails_end.length();
 	public static final String xml_transactions = "<transactions>";
 	public static final int xml_transactions_length = xml_transactions.length(); 
 	public static final String xml_transactions_end = "</transactions>";
 	public static final int xml_transactions_end_length = xml_transactions_end.length();
 	public static final String xml_notifications = "<notifications>";
 	public static final int xml_notifications_length = xml_notifications.length(); 
 	public static final String xml_notifications_end = "</notifications>";
 	public static final int xml_notifications_end_length = xml_notifications_end.length();
 	public static final String xml_friends = "<friends>";
 	public static final int xml_friends_length = xml_friends.length(); 
 	public static final String xml_friends_end = "</friends>";
 	public static final int xml_friends_end_length = xml_friends_end.length();
 	public static final String xml_leaderboard = "<leaderboard>";
 	public static final int xml_leaderboard_length = xml_leaderboard.length(); 
 	public static final String xml_leaderboard_end = "</leaderboard>";
 	public static final int xml_leaderboard_end_length = xml_leaderboard_end.length();
 	public static final String xml_decks = "<decks>";
 	public static final int xml_decks_length = xml_decks.length(); 
 	public static final String xml_decks_end = "</decks>";
 	public static final int xml_decks_end_length = xml_decks_end.length();
 	public static final String xml_deck = "<deck>";
 	public static final int xml_deck_length = xml_deck.length(); 
 	public static final String xml_games = "<games>";
 	public static final int xml_games_length = xml_games.length(); 
 	public static final String xml_games_end = "</games>";
 	public static final int xml_games_end_length = xml_games_end.length();
 	public static final String xml_game = "<game>";
 	public static final int xml_game_length = xml_game.length(); 
 	public static final String xml_productid = "<productid>";
 	public static final int xml_productid_length = xml_productid.length();
 	public static final String xml_productid_end = "</productid>";
 	public static final int xml_productid_end_length = xml_productid_end.length();
 	public static final String xml_productname = "<productname>";
 	public static final int xml_productname_length = xml_productname.length();
 	public static final String xml_productname_end = "</productname>";
 	public static final int xml_productname_end_length = xml_productname_end.length();
 	public static final String xml_producttype = "<producttype>";
 	public static final int xml_producttype_length = xml_producttype.length();
 	public static final String xml_producttype_end = "</producttype>";
 	public static final int xml_producttype_end_length = xml_producttype_end.length();
 	public static final String xml_productprice = "<productprice>";
 	public static final int xml_productprice_length = xml_productprice.length();
 	public static final String xml_productprice_end = "</productprice>";
 	public static final int price = xml_productprice_end.length();
 	public static final String xml_productnumcards = "<productnumcards>";
 	public static final int xml_productnumcards_length = xml_productnumcards.length();
 	public static final String xml_productnumcards_end = "</productnumcards>";
 	public static final int xml_productnumcards_end_length = xml_productnumcards_end.length();
 	public static final String xml_productthumb = "<productthumb>";
 	public static final int xml_productthumb_length = xml_productthumb.length();
 	public static final String xml_productthumb_end = "</productthumb>";
 	public static final int xml_productthumb_end_length = xml_productthumb_end.length();
 	public static final String xml_categoryid = "<categoryid>";
 	public static final int xml_categoryid_length = xml_categoryid.length();
 	public static final String xml_categoryid_end = "</categoryid>";
 	public static final int xml_categoryid_end_length = xml_categoryid_end.length();
 	public static final String xml_category_id = "<category_id>";
 	public static final int xml_category_id_length = xml_category_id.length();
 	public static final String xml_category_id_end = "</category_id>";
 	public static final int xml_category_id_end_length = xml_category_id_end.length();
 	public static final String xml_categoryname = "<categoryname>";
 	public static final int xml_categoryname_length = xml_categoryname.length();
 	public static final String xml_categoryname_end = "</categoryname>";
 	public static final int xml_categoryname_end_length = xml_categoryname_end.length();
 	public static final String xml_deckid = "<deckid>";
 	public static final int xml_deckid_length = xml_deckid.length();
 	public static final String xml_deckid_end = "</deckid>";
 	public static final int xml_deckid_end_length = xml_deckid_end.length();
 	public static final String xml_deck_id = "<deck_id>";
 	public static final int xml_deck_id_length = xml_deck_id.length();
 	public static final String xml_deck_id_end = "</deck_id>";
 	public static final int xml_deck_id_end_length = xml_deck_id_end.length();
 	public static final String xml_gameid = "<gameid>";
 	public static final int xml_gameid_length = xml_gameid.length();
 	public static final String xml_gameid_end = "</gameid>";
 	public static final int xml_gameid_end_length = xml_gameid_end.length();
 	public static final String xml_gamename = "<gamedescription>";
 	public static final int xml_gamename_length = xml_gamename.length();
 	public static final String xml_gamename_end = "</gamedescription>";
 	public static final int xml_gamename_end_length = xml_gamename_end.length();
 	public static final String xml_gcurl = "<gcurl>";
 	public static final int xml_gcurl_length = xml_gcurl.length();
 	public static final String xml_gcurl_end = "</gcurl>";
 	public static final int xml_gcurl_end_length = xml_gcurl_end.length();
 	public static final String xml_active = "<active>";
 	public static final int xml_active_length = xml_active.length();
 	public static final String xml_active_end = "</active>";
 	public static final int xml_active_end_length = xml_active_end.length();
 	public static final String xml_explanation = "<explanation>";
 	public static final int xml_explanation_length = xml_explanation.length();
 	public static final String xml_explanation_end = "</explanation>";
 	public static final int xml_explanation_end_length = xml_explanation_end.length();
 	public static final String xml_phase = "<phase>";
 	public static final int xml_phase_length = xml_phase.length();
 	public static final String xml_phase_end = "</phase>";
 	public static final int xml_phase_end_length = xml_phase_end.length();
 	public static final String xml_lastmove = "<lastmove>";
 	public static final int xml_lastmove_length = xml_lastmove.length();
 	public static final String xml_lastmove_end = "</lastmove>";
 	public static final int xml_lastmove_end_length = xml_lastmove_end.length();
 	public static final String xml_oppcards = "<oppcards>";
 	public static final int xml_oppcards_length = xml_oppcards.length();
 	public static final String xml_oppcards_end = "</oppcards>";
 	public static final int xml_oppcards_end_length = xml_oppcards_end.length();
 	public static final String xml_oppname = "<oppname>";
 	public static final int xml_oppname_length = xml_oppname.length();
 	public static final String xml_oppname_end = "</oppname>";
 	public static final int xml_oppname_end_length = xml_oppname_end.length();
 	public static final String xml_usercards = "<usercards>";
 	public static final int xml_usercards_length = xml_usercards.length();
 	public static final String xml_usercards_end = "</usercards>";
 	public static final int xml_usercards_end_length = xml_usercards_end.length();
 	public static final String xml_gcurlflip = "<gcurlflip>";
 	public static final int xml_gcurlflip_length = xml_gcurlflip.length();
 	public static final String xml_gcurlflip_end = "</gcurlflip>";
 	public static final int xml_gcurlflip_end_length = xml_gcurlflip_end.length();
 	public static final String xml_albumid = "<albumid>";
 	public static final int xml_albumid_length = xml_albumid.length();
 	public static final String xml_albumid_end = "</albumid>";
 	public static final int xml_albumid_end_length = xml_albumid_end.length();
 	public static final String xml_answerid = "<answer_id>";
 	public static final int xml_answerid_length = xml_answerid.length();
 	public static final String xml_answerid_end = "</answer_id>";
 	public static final int xml_answerid_end_length = xml_answerid_end.length();
 	public static final String xml_hascards = "<hascards>";
 	public static final int xml_hascards_length = xml_hascards.length();
 	public static final String xml_hascards_end = "</hascards>";
 	public static final int xml_hascards_end_length = xml_hascards_end.length();
 	public static final String xml_albumname = "<albumname>";
 	public static final int xml_albumname_length = xml_albumname.length();
 	public static final String xml_albumname_end = "</albumname>";
 	public static final int xml_albumname_end_length = xml_albumname_end.length();
 	public static final String xml_album_end = "</album>";
 	public static final int xml_album_end_length = xml_album_end.length();
 	public static final String xml_detail_end = "</detail>";
 	public static final int xml_detail_end_length = xml_detail_end.length();
 	public static final String xml_transaction_end = "</transaction>";
 	public static final int xml_transaction_end_length = xml_transaction_end.length();
 	public static final String xml_game_end = "</game>";
 	public static final int xml_game_end_length = xml_game_end.length();
 	public static final String xml_deck_end = "</deck>";
 	public static final int xml_deck_end_length = xml_deck_end.length();
 	public static final String xml_product_end = "</product>";
 	public static final int xml_product_end_length = xml_product_end.length();
 	public static final String xml_auction_end = "</auction>";
 	public static final int xml_auction_end_length = xml_auction_end.length();
 	public static final String xml_userdetails = "<userdetails>";
 	public static final int xml_userdetails_length = xml_userdetails.length();
 	public static final String xml_userdetails_end = "</userdetails>";
 	public static final int xml_userdetails_end_length = xml_userdetails_end.length();
 	public static final String xml_descr = "<desc>";
 	public static final int xml_descr_length = xml_descr.length();
 	public static final String xml_descr_end = "</desc>";
 	public static final int xml_descr_end_length = xml_descr_end.length();
 	public static final String xml_usr = "<usr>";
 	public static final int xml_usr_length = xml_usr.length();
 	public static final String xml_usr_end = "</usr>";
 	public static final int xml_usr_end_length = xml_usr_end.length();
 	public static final String xml_date = "<date>";
 	public static final int xml_date_length = xml_date.length();
 	public static final String xml_date_end = "</date>";
 	public static final int xml_date_end_length = xml_date_end.length();
 	public static final String xml_answer = "<answer>";
 	public static final int xml_answer_length = xml_answer.length();
 	public static final String xml_answer_end = "</answer>";
 	public static final int xml_answer_end_length = xml_answer_end.length();
 	public static final String xml_answered = "<answered>";
 	public static final int xml_answered_length = xml_answered.length();
 	public static final String xml_answered_end = "</answered>";
 	public static final int xml_answered_end_length = xml_answered_end.length();
 	public static final String xml_creditvalue = "<creditvalue>";
 	public static final int xml_creditvalue_length = xml_creditvalue.length();
 	public static final String xml_creditvalue_end = "</creditvalue>";
 	public static final int xml_creditvalue_end_length = xml_creditvalue_end.length();
 	public static final String xml_email = "<email>";
 	public static final int xml_email_length = xml_email.length();
 	public static final String xml_email_end = "</email>";
 	public static final int xml_email_end_length = xml_email_end.length();
 	public static final String xml_credits = "<credits>";
 	public static final int xml_credits_length = xml_credits.length();
 	public static final String xml_credits_end = "</credits>";
 	public static final int xml_credits_end_length = xml_credits_end.length();
 	public static final String xml_freebie = "<freebie>";
 	public static final int xml_freebie_length = xml_freebie.length();
 	public static final String xml_freebie_end = "</freebie>";
 	public static final int xml_freebie_end_length = xml_freebie_end.length();
 	
 	public static final String xml_cardsincategory = "<cardsincategory>";
 	public static final int xml_cardsincategory_length = xml_cardsincategory.length();
 	public static final String xml_cardsincategory_end = "</cardsincategory>";
 	public static final int xml_cardsincategory_end_length = xml_cardsincategory_end.length();
 	
 	public static final String xml_auctionsincategory = "<auctionsincategory>";
 	public static final int xml_auctionsincategory_length = xml_auctionsincategory.length();
 	public static final String xml_auctionsincategory_end = "</auctionsincategory>";
 	public static final int xml_auctionsincategory_end_length = xml_auctionsincategory_end.length();
 	public static final String xml_auctioncardid = "<auctioncardid>";
 	public static final int xml_auctioncardid_length = xml_auctioncardid.length();
 	public static final String xml_auctioncardid_end = "</auctioncardid>";
 	public static final int xml_auctioncardid_end_length = xml_auctioncardid_end.length();
 	public static final String xml_usercardid = "<usercardid>";
 	public static final int xml_usercardid_length = xml_usercardid.length();
 	public static final String xml_usercardid_end = "</usercardid>";
 	public static final int xml_usercardid_end_length = xml_usercardid_end.length();
 	public static final String xml_openingbid = "<openingbid>";
 	public static final int xml_openingbid_length = xml_openingbid.length();
 	public static final String xml_openingbid_end = "</openingbid>";
 	public static final int xml_openingbid_end_length = xml_openingbid_end.length();
 	public static final String xml_buynowprice = "<buynowprice>";
 	public static final int xml_buynowprice_length = xml_buynowprice.length();
 	public static final String xml_buynowprice_end = "</buynowprice>";
 	public static final int xml_buynowprice_end_length = xml_buynowprice_end.length();
 	public static final String xml_price = "<price>";
 	public static final int xml_price_length = xml_price.length();
 	public static final String xml_price_end = "</price>";
 	public static final int xml_price_end_length = xml_price_end.length();
 	public static final String xml_username = "<username>";
 	public static final int xml_username_length = xml_username.length();
 	public static final String xml_username_end = "</username>";
 	public static final int xml_username_end_length = xml_username_end.length();
 	public static final String xml_endDate = "<endDate>";
 	public static final int xml_endDate_length = xml_endDate.length();
 	public static final String xml_endDate_end = "</endDate>";
 	public static final int xml_endDate_end_length = xml_endDate_end.length();
 	public static final String xml_lastBidUser = "<lastBidUser>";
 	public static final int xml_lastBidUser_length = xml_lastBidUser.length();
 	public static final String xml_lastBidUser_end = "</lastBidUser>";
 	public static final int xml_lastBidUser_end_length = xml_lastBidUser_end.length();
 	
 	public static final String xml_cards = "<cards>";
 	public static final int xml_cards_length = xml_cards.length();
 	public static final String xml_cards_end = "</cards>";
 	public static final int xml_cards_end_length = xml_cards_end.length();
 	
 	public static final String xml_card = "<card>";
 	public static final int xml_card_length = xml_card.length();
 	
 	public static final String xml_cardid = "<cardid>";
 	public static final int xml_cardid_length = xml_cardid.length();
 	public static final String xml_cardid_end = "</cardid>";
 	public static final int xml_cardid_end_length = xml_cardid_end.length();
 	public static final String xml_gameplaycardid = "<gameplaycard_id>";
 	public static final int xml_gameplaycardid_length = xml_gameplaycardid.length();
 	public static final String xml_gameplaycardid_end = "</gameplaycard_id>";
 	public static final int xml_gameplaycardid_end_length = xml_gameplaycardid_end.length();
 	public static final String xml_id = "<id>";
 	public static final int xml_id_length = xml_id.length();
 	public static final String xml_id_end = "</id>";
 	public static final int xml_id_end_length = xml_id_end.length();
 	public static final String xml_valt = "<val>";
 	public static final int xml_valt_length = xml_valt.length();
 	public static final String xml_valt_end = "</val>";
 	public static final int xml_valt_end_length = xml_valt_end.length();
 	public static final String xml_description = "<description>";
 	public static final int xml_description_length = xml_description.length();
 	public static final String xml_description_end = "</description>";
 	public static final int xml_description_end_length = xml_description_end.length();
 	public static final String xml_statdescription = "<stat_description>";
 	public static final int xml_statdescription_length = xml_statdescription.length();
 	public static final String xml_statdescription_end = "</stat_description>";
 	public static final int xml_statdescription_end_length = xml_statdescription_end.length();
 	public static final String xml_stattype = "<stat_type>";
 	public static final int xml_stattype_length = xml_stattype.length();
 	public static final String xml_stattype_end = "</stat_type>";
 	public static final int xml_stattype_end_length = xml_stattype_end.length();
 	public static final String xml_cardstatid = "<cardstat_id>";
 	public static final int xml_cardstatid_length = xml_cardstatid.length();
 	public static final String xml_cardstatid_end = "</cardstat_id>";
 	public static final int xml_cardstatid_end_length = xml_cardstatid_end.length();
 	public static final String xml_categorystatid = "<categorystat_id>";
 	public static final int xml_categorystatid_length = xml_categorystatid.length();
 	public static final String xml_categorystatid_end = "</categorystat_id>";
 	public static final int xml_categorystatid_end_length = xml_categorystatid_end.length();
 	public static final String xml_quality = "<quality>";
 	public static final int xml_quality_length = xml_quality.length();
 	public static final String xml_quality_end = "</quality>";
 	public static final int xml_quality_end_length = xml_quality_end.length();
 	public static final String xml_quantity = "<quantity>";
 	public static final int xml_quantity_length = xml_quantity.length();
 	public static final String xml_quantity_end = "</quantity>";
 	public static final int xml_quantity_end_length = xml_quantity_end.length();
 	public static final String xml_rating = "<ranking>";
 	public static final int xml_rating_length = xml_rating.length();
 	public static final String xml_rating_end = "</ranking>";
 	public static final int xml_rating_end_length = xml_rating_end.length();
 	public static final String xml_thumburl = "<thumburl>";
 	public static final int xml_thumburl_length = xml_thumburl.length();
 	public static final String xml_thumburl_end = "</thumburl>";
 	public static final int xml_thumburl_end_length = xml_thumburl_end.length();
 	public static final String xml_fronturl = "<fronturl>";
 	public static final int xml_fronturl_length = xml_fronturl.length();
 	public static final String xml_fronturl_end = "</fronturl>";
 	public static final int xml_fronturl_end_length = xml_fronturl_end.length();
 	public static final String xml_frontflipurl = "<frontflipurl>";
 	public static final int xml_frontflipurl_length = xml_frontflipurl.length();
 	public static final String xml_frontflipurl_end = "</frontflipurl>";
 	public static final int xml_frontflipurl_end_length = xml_frontflipurl_end.length();
 	public static final String xml_backurl = "<backurl>";
 	public static final int xml_backurl_length = xml_backurl.length();
 	public static final String xml_backurl_end = "</backurl>";
 	public static final int xml_backurl_end_length = xml_backurl_end.length();
 	public static final String xml_backflipurl = "<backflipurl>";
 	public static final int xml_backflipurl_length = xml_backflipurl.length();
 	public static final String xml_backflipurl_end = "</backflipurl>";
 	public static final int xml_backflipurl_end_length = xml_backflipurl_end.length();
 	public static final String xml_loadingurl = "<loadingurl>";
 	public static final int xml_loadingurl_length = xml_loadingurl.length();
 	public static final String xml_loadingurl_end = "</loadingurl>";
 	public static final int xml_loadingurl_end_length = xml_loadingurl_end.length();
 	public static final String xml_loadingflipurl = "<loadingurlflip>";
 	public static final int xml_loadingflipurl_length = xml_loadingflipurl.length();
 	public static final String xml_loadingflipurl_end = "</loadingurlflip>";
 	public static final int xml_loadingflipurl_end_length = xml_loadingflipurl_end.length();
 	public static final String xml_value = "<value>";
 	public static final int xml_value_length = xml_value.length();
 	public static final String xml_value_end = "</value>";
 	public static final String xml_note = "<note>";
 	public static final int xml_note_length = xml_note.length();
 	public static final String xml_note_end = "</note>";
 	public static final int xml_note_end_length = xml_note_end.length();
 	public static final String xml_friend = "<friend>";
 	public static final int xml_friend_length = xml_friend.length();
 	public static final String xml_friend_end = "</friend>";
 	public static final int xml_friend_end_length = xml_friend_end.length();
 	public static final String xml_leader = "<leader>";
 	public static final int xml_leader_length = xml_leader.length();
 	public static final String xml_leader_end = "</leader>";
 	public static final int xml_leader_end_length = xml_leader_end.length();
 	public static final String xml_updated = "<updated>";
 	public static final int xml_updated_length = xml_updated.length();
 	public static final String xml_updated_end = "</updated>";
 	public static final int xml_updated_end_length = xml_updated_end.length();
 	public static final String xml_stats = "<stats>";
 	public static final int xml_stats_length = xml_stats.length();
 	public static final String xml_stats_end = "</stats>";
 	public static final int xml_stats_end_length = xml_stats_end.length();
 	public static final String xml_cardstats = "<cardstats>";
 	public static final int xml_cardstats_length = xml_cardstats.length();
 	public static final String xml_cardstats_end = "</cardstats>";
 	public static final int xml_cardstats_end_length = xml_cardstats_end.length();
 	public static final String xml_stat = "<stat "; //(attribute = description, value = value);
 	public static final int xml_stat_length = xml_stat.length();
 	public static final String xml_stat_end = "</stat>";
 	public static final int xml_stat_end_length = xml_stat_end.length();
 	public static final String xml_cardstat = "<cardstat "; //(attribute = description, value = value);
 	public static final int xml_cardstat_length = xml_cardstat.length();
 	public static final String xml_cardstat_end = "</cardstat>";
 	public static final int xml_cardstat_end_length = xml_cardstat_end.length();
 	public static final String xml_card_end = "</card>";
 	public static final int xml_card_end_length = xml_card_end.length();
 	
 	public static final String xml_val = "\">";
 	public static final int xml_val_length = xml_val.length();
 	public static final String xml_desc = "desc=\"";
 	public static final int xml_desc_length = xml_desc.length();
 	public static final String xml_top = "top=\"";
 	public static final int xml_top_length = xml_top.length();
 	public static final String xml_left = "left=\"";
 	public static final int xml_left_length = xml_left.length();
 	public static final String xml_width = "width=\"";
 	public static final int xml_width_length = xml_width.length();
 	public static final String xml_height = "height=\"";
 	public static final int xml_height_length = xml_height.length();
 	public static final String xml_frontorback = "frontorback=\"";
 	public static final int xml_frontorback_length = xml_frontorback.length();
 	public static final String xml_red = "red=\"";
 	public static final int xml_red_length = xml_red.length();
 	public static final String xml_green = "green=\"";
 	public static final int xml_green_length = xml_green.length();
 	public static final String xml_blue = "blue=\"";
 	public static final int xml_blue_length = xml_blue.length();
 	public static final String xml_ival = "ival=\"";
 	public static final int xml_ival_length = xml_ival.length();
 	public static final String xml_end = "\"";
 	public static final int xml_end_length = xml_end.length();
 	
 	public static String removeSpaces(String s) {
 	   s = s.replace('(', ' ').replace(')', ' ').replace('#', ' ').replace('*', ' ');
 	   s = s.replace('-', ' ');
 	   
 	   byte[] tmp = s.getBytes();
 	   int count = 0;
 	   for (int i = 0; i < tmp.length; i++) {
 		   if (tmp[i] == ' ') {} else {
 			   count++;
 		   }
 	   }
 	   byte[] ret = new byte[count];
 	   count=0;
 	   for (int i = 0; i < tmp.length; i++) {
 		   if (tmp[i] == ' ') {} else {
 				   ret[count++] = tmp[i];
 			   }
 		   }
 		   return (new String(ret).trim());
 	}
 }
 
