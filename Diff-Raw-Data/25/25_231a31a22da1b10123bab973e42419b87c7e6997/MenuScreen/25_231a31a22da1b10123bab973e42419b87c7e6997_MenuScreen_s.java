 package net.mytcg.topcar.ui;
 
 import java.util.Date;
 import java.util.Vector;
 
 import net.mytcg.topcar.ui.custom.FixedButtonField;
 import net.mytcg.topcar.ui.custom.MenuField;
 import net.mytcg.topcar.ui.custom.MenuThumbnailField;
 import net.mytcg.topcar.ui.custom.PageField;
 import net.mytcg.topcar.ui.custom.PageManager;
 import net.mytcg.topcar.util.Const;
 import net.mytcg.topcar.util.SettingsBean;
 import net.rim.device.api.io.http.HttpDateParser;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.TouchEvent;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.NullField;
 
 public class MenuScreen extends AppScreen implements FieldChangeListener
 {
 	FixedButtonField exit = new FixedButtonField(Const.back);
 
 	boolean initialcheck = true;
 	//ListItemField albums = new ListItemField("Empty", -1, false, 0);
 	//ListItemField play = new ListItemField("Empty", -1, false, 0);
 	//ListItemField decks = new ListItemField("Empty", -1, false, 0);
 	//ListItemField shop = new ListItemField("Empty", -1, false, 0);
 	//ListItemField auctions = new ListItemField("Empty", -1, false, 0);
 	//ListItemField redeem = new ListItemField("Empty", -1, false, 0);
 	//ListItemField balance = new ListItemField("Empty", -1, false, 0);
 	//ListItemField profile = new ListItemField("Empty", -1, false, 0);
 	//ListItemField notifications = new ListItemField("Empty", -1, false, 0);
 	//ListItemField rankings = new ListItemField("Empty", -1, false, 0);
 	//ListItemField friendranks = new ListItemField("Empty", -1, false, 0);
 	//ListItemField friends = new ListItemField("Empty", -1, false, 0);
 	//ListItemField invitefriend = new ListItemField("Empty", -1, false, 0);
 	//ListItemField logout = new ListItemField("Empty", -1, false, 0);
 	MenuThumbnailField albums;
 	MenuThumbnailField play;
 	MenuThumbnailField decks;
 	MenuThumbnailField shop;
 	MenuThumbnailField auctions;
 	MenuThumbnailField redeem;
 	MenuThumbnailField balance;
 	MenuThumbnailField profile;
 	MenuThumbnailField notifications;
 	MenuThumbnailField rankings;
 	MenuThumbnailField friendranks;
 	MenuThumbnailField friends;
 	MenuThumbnailField invitefriend;
 	MenuThumbnailField logout;
 	MenuField menu = new MenuField(Const.getAlbum());
 	Vector pages = new Vector();
 	Vector tempList = new Vector();
 	Vector temp = new Vector();
 	Vector dots = new Vector();
 	int currentPage = 0;
 	
 	public MenuScreen()
 	{
 		super(null);
 		SettingsBean _instance = SettingsBean.getSettings();
 		
 		bgManager.setStatusHeight(exit.getContentHeight());
 		bgManager.setArrowMode(true);
 		
 		if (screen == null) {
 			exit.setLabel(Const.exit);
 		}
 		
 		int listSize = (Const.getWidth()) / 54;
 		int listCounter = 0;
 		albums = new MenuThumbnailField(Const.getAlbumThumb(),Const.getAlbum(),menu);
 		play = new MenuThumbnailField(Const.getPlayThumb(), Const.getPlay(),menu);
 		decks = new MenuThumbnailField(Const.getDecksThumb(), Const.getDecks(),menu);
 		shop = new MenuThumbnailField(Const.getShopThumb(), Const.getShop(), menu);
 		auctions = new MenuThumbnailField(Const.getAuctionsThumb(), Const.getAuctions(),menu);
 		redeem = new MenuThumbnailField(Const.getRedeemThumb(), Const.getRedeem(),menu);
 		balance = new MenuThumbnailField(Const.getCreditsThumb(), Const.getCredits(),menu);
 		profile = new MenuThumbnailField(Const.getProfileThumb(), Const.getProfile(),menu);
 		notifications = new MenuThumbnailField(Const.getNotificationsThumb(), Const.getNotifications(),menu);
 		rankings = new MenuThumbnailField(Const.getRankingsThumb(), Const.getRankings(),menu);
 		friendranks = new MenuThumbnailField(Const.getFriendRanksThumb(), Const.getFriendRanks(),menu);
 		friends = new MenuThumbnailField(Const.getFriendsThumb(), Const.getFriends(),menu);
 		invitefriend  = new MenuThumbnailField(Const.getInviteThumb(), Const.getInvite(),menu);
 		logout = new MenuThumbnailField(Const.getLogoutThumb(),Const.getLogout(),menu);
 		
 		albums.setChangeListener(this);
 		play.setChangeListener(this);
 		decks.setChangeListener(this);
 		shop.setChangeListener(this);
 		auctions.setChangeListener(this);
 		redeem.setChangeListener(this);
 		balance.setChangeListener(this);
 		profile.setChangeListener(this);
 		notifications.setChangeListener(this);
 		rankings.setChangeListener(this);
 		friendranks.setChangeListener(this);
 		friends.setChangeListener(this);
 		invitefriend.setChangeListener(this);
 		logout.setChangeListener(this);
 		exit.setChangeListener(this);
 		
 		add(menu);
 		temp.addElement(albums);
 		temp.addElement(play);
 		temp.addElement(decks);
 		temp.addElement(shop);
 		temp.addElement(auctions);
 		temp.addElement(balance);
 		temp.addElement(profile);
 		temp.addElement(notifications);
 		temp.addElement(rankings);
 		temp.addElement(friendranks);
 		temp.addElement(friends);
 		temp.addElement(invitefriend);
 		temp.addElement(redeem);
 		temp.addElement(logout);
 		for(int i = 0; i < temp.size(); i++){
 			if(listCounter >= listSize){
 				pages.addElement(tempList);
 				tempList = new Vector();
 				listCounter=0;
 			}
 			tempList.addElement(temp.elementAt(i));
 			listCounter++;
 		}
 		pages.addElement(tempList);
 		
 		Field[] temp = new Field[((Vector)pages.elementAt(0)).size()];
 		((Vector)pages.elementAt(0)).copyInto(temp);
 		try{
 		hManager1.deleteAll();
 		for(int i = 0; i < temp.length; i++){
 			hManager1.add(temp[i]);
 		}
 		}catch(Exception e){}
 		//addButton(new FixedButtonField(""));
 		//addButton(new FixedButtonField(""));
 		//addButton(exit);
 		PageManager pm = new PageManager();
 		
 		for(int i = 0; i < pages.size();i++){
 			dots.addElement(new PageField());
 		}
 		((PageField)dots.elementAt(0)).setActive(true);
 		Field[] temp2 = new Field[dots.size()];
 		dots.copyInto(temp2);
 		pm.addAll(temp2);
 		statusManager.add(pm);
 		if(_instance.notifications == false){
 			doConnect("notedate=1");
 		}
 		this.invalidate();
 	}
 	protected boolean touchEvent(TouchEvent event) {
 		int x = event.getX(1);
 		int y = event.getY(1) - titleManager.getHeight();
 		if(event.getEvent() == TouchEvent.CLICK){
 			if(bgManager.checkLeftArrow(x, y)){
 				navigationMovement(-1, 0, 536870912, 5000);
 				return true;
 			}else if(bgManager.checkRightArrow(x, y)){
 				navigationMovement(1, 0, -1610612736, 5000);   
 				return true;
 			}
 		}
 		if(this.getFieldAtLocation(x, y)==-1){
 			return true;
 		}else if(this.getFieldAtLocation(x, y)==0){
 			if(bgManager.getFieldAtLocation(x, y)!=-1){
 				return super.touchEvent(event);
 			}
 			return true;
 		}
 		else{
 			return super.touchEvent(event);
 		}
 	}
 	public boolean navigationMovement(int dx, int dy, int status, int time) {
 		if(((MenuThumbnailField)((Vector)pages.elementAt(currentPage)).elementAt(0)).isFocus()&&dy == 0 && dx == -1){
 			if(pages.size() >1){
 				if((currentPage-1)<0){
 					currentPage = pages.size()-1;
 				}else{
 					currentPage--;
 				}
 				synchronized(UiApplication.getEventLock()) {
 					Field[] temp = new Field[((Vector)pages.elementAt(currentPage)).size()];
 	    			((Vector)pages.elementAt(currentPage)).copyInto(temp);
 	    			try{
 	    				hManager1.deleteAll();
 	    			}catch(Exception e){
 	    				
 	    			}
 	    			for(int i = 0; i < temp.length; i++){
 	    				hManager1.add(temp[i]);
 	    			}
 	    			NullField dummy = new NullField();
 	    			hManager1.add(dummy);
 	    			dummy.setFocus();
 	    			//((MenuThumbnailField)((Vector)pages.elementAt(currentPage)).elementAt(((Vector)pages.elementAt(currentPage)).size()-1)).setFocus();
 	    			for(int i = 0; i < dots.size();i++){
 	    				((PageField)dots.elementAt(i)).setActive(false);
 	    			}
 	    			((PageField)dots.elementAt(currentPage)).setActive(true);
 		    	}
 			}
 		}else if(((MenuThumbnailField)((Vector)pages.elementAt(currentPage)).elementAt(((Vector)pages.elementAt(currentPage)).size()-1)).isFocus()&&dy == 0 && dx == 1){
 			if(pages.size() >1){
 				if((currentPage+1)>=pages.size()){
 					currentPage = 0;
 				}else{
 					currentPage++;
 				}
 				synchronized(UiApplication.getEventLock()) {
 					Field[] temp = new Field[((Vector)pages.elementAt(currentPage)).size()];
 	    			((Vector)pages.elementAt(currentPage)).copyInto(temp);
 	    			try{
 	    				hManager1.deleteAll();
 	    			}catch(Exception e){
 	    				
 	    			}
 	    			for(int i = 0; i < temp.length; i++){
 	    				hManager1.add(temp[i]);
 	    			}
 	    			//((MenuThumbnailField)((Vector)pages.elementAt(currentPage)).elementAt(0)).setFocus();
 	    			for(int i = 0; i < dots.size();i++){
 	    				((PageField)dots.elementAt(i)).setActive(false);
 	    			}
 	    			((PageField)dots.elementAt(currentPage)).setActive(true);
 		    	}
 			}
 		}
 		return super.navigationMovement(dx, dy, status, time);
 		
 	}
 	
 	public void process(String val) {
 		int fromIndex;
 		if ((fromIndex = val.indexOf(Const.xml_notedate)) != -1) {
 			String notedate = val.substring(fromIndex+Const.xml_notedate_length, val.indexOf(Const.xml_notedate_end, fromIndex));
 			Date date = new Date(HttpDateParser.parse(notedate));
 			SettingsBean _instance = SettingsBean.getSettings();
 			if(date.getTime()/1000>_instance.getNoteLoaded()){
 				_instance.notifications = true;
 				synchronized(UiApplication.getEventLock()) {
 					//notifications.setLabel((_instance.notifications?"*":"")+Const.notification);
 				}
 				if(initialcheck){
 					initialcheck = false;
 					synchronized(UiApplication.getEventLock()) {
 						screen = new DetailScreen(this, Const.NOTIFICATIONSCREEN);
 						UiApplication.getUiApplication().pushScreen(screen);
 					}
 				}
 			}
 		}
 	}
 	
 	protected void onExposed() {
 		SettingsBean _instance = SettingsBean.getSettings();
 		synchronized(UiApplication.getEventLock()) {
 			//notifications.setLabel((_instance.notifications?"*":"")+Const.notification);
 		}
 		if(_instance.notifications == false){
 			doConnect("notedate=1");
 		}
		super.onExposed();
 	}
 	
 	public void fieldChanged(Field f, int i) {
 		if (f == exit) {
 			SettingsBean _instance = SettingsBean.getSettings();
 			_instance.lastloaded();
 			SettingsBean.saveSettings(_instance);
 			System.exit(0);
 		} else if(f == albums){
 			screen = new AlbumScreen(0);
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == play){
 			screen = new NewGameScreen();
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == decks){
 			screen = new DecksScreen();
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == shop){
 			screen = new ShopCategoriesScreen();
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == auctions){
 			screen = new AuctionMenuScreen();
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == redeem){
 			screen = new RedeemScreen(this);
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == balance){
 			screen = new DetailScreen(this, Const.BALANCESCREEN);
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == profile){
 			screen = new DetailScreen(this, Const.PROFILESCREEN);
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == notifications){
 			screen = new DetailScreen(this, Const.NOTIFICATIONSCREEN);
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == rankings){
 			screen = new RankingsCategoriesScreen(false);
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == friendranks){
 			screen = new RankingsCategoriesScreen(true);
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == friends){
 			screen = new DetailScreen(this, Const.FRIENDSSCREEN);
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == invitefriend){
 			screen = new InviteFriendScreen();
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else if(f == logout){
 			close();
 			Const.GOTOSCREEN = Const.LOGINSCREEN;
 			Const.FROMSCREEN = Const.LOGINSCREEN;
 			Const.app.previousScreen();
 			System.exit(0);
 			/*screen = new LogOutScreen();
 			UiApplication.getUiApplication().pushScreen(screen);*/
 		}
 	}
 }
