 package net.mytcg.topcar.ui;
 
 import java.util.Vector;
 
 import net.rim.device.api.io.Base64OutputStream;
import net.mytcg.topcar.ui.custom.BackgroundManager;
 import net.mytcg.topcar.ui.custom.ColorLabelField;
 import net.mytcg.topcar.ui.custom.FixedButtonField;
 import net.mytcg.topcar.ui.custom.HorizontalGamePlayManager;
 import net.mytcg.topcar.ui.custom.ImageField;
import net.mytcg.topcar.ui.custom.ListItemField;
 import net.mytcg.topcar.ui.custom.SexyEditField;
 import net.mytcg.topcar.ui.custom.StatField;
 import net.mytcg.topcar.ui.custom.VerticalGamePlayManager;
 import net.mytcg.topcar.util.Card;
 import net.mytcg.topcar.util.Const;
 import net.mytcg.topcar.util.SettingsBean;
 import net.mytcg.topcar.util.Stat;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.UiApplication;
 
 
 public class GamePlayScreen extends AppScreen implements FieldChangeListener
 {
 	private FixedButtonField friendback = new FixedButtonField(Const.exit);
 	private FixedButtonField flips = new FixedButtonField(Const.flip);
 	private FixedButtonField play = new FixedButtonField(Const.play);
 	private FixedButtonField options = new FixedButtonField(Const.options);
 	private FixedButtonField con = new FixedButtonField(Const.con);
 	private SexyEditField username = new SexyEditField("");
 	private ColorLabelField user = new ColorLabelField("");
 	private ColorLabelField opponent = new ColorLabelField("");
 	
 	private HorizontalGamePlayManager opphgamemanager = new HorizontalGamePlayManager();
 	private VerticalGamePlayManager oppvgamemanager = new VerticalGamePlayManager();
 	private boolean flip = true;
 	private Card card1 = null;
 	private Card card2 = null;
 	private String phase = "";
 	private String active = "";
 	private String lastmove = "";
 	private String lastmove64 = "";
 	private int categoryId = -1;
 	private int newGameType = -1;
 	private int gameid = -1;
 	private StatField [] uistats;
 	private StatField [] oppuistats;
 	private Vector stats = new Vector();
 	private Vector oppstats = new Vector();
 	
 	public GamePlayScreen(boolean newGame, int categoryId, int newGameType, boolean againstFriend) {
 		super(true,true);
 		this.categoryId = categoryId;
 		this.newGameType = newGameType;
 		play.setChangeListener(this);
 		con.setChangeListener(this);
 		flips.setChangeListener(this);
 		options.setChangeListener(this);
 		friendback.setChangeListener(this);
 		if(newGame){
 			phase = "newgame";
 			if(againstFriend){
 				user = new ColorLabelField(" Enter the username of the person you want to play against");
 				add(user);
 				add(username);
 				addButton(play);
 				addButton(new FixedButtonField(""));
 				addButton(friendback);
 			}else{
 				setText("Initialising new game...");
 				doConnect(Const.startnewgame+"&categoryid="+categoryId+"&newgametype="+newGameType+Const.height+Const.getCardHeight()+"&width=250");
 				addButton(new FixedButtonField(""));
 				addButton(new FixedButtonField(""));
 				addButton(options);
 			}
 		}else{
 			phase = "loadgame";
 			gameid = categoryId;
 			doConnect(Const.loadgame+"&gameid="+gameid+Const.height+Const.getCardHeight()+"&width=250");
 			addButton(new FixedButtonField(""));
 			addButton(new FixedButtonField(""));
 			addButton(options);
 		}
 	}
 	
 	public void process(String val) {
 		//System.out.println("PLEI "+val);
 		int fromIndex;
 		if(phase.equals("newgame")){
 			if (((fromIndex = val.indexOf(Const.xml_game)) != -1)) {
 	    		gameid = -1;
 	    		String gcurl = "";
 	    		String gcurlflip = "";
 	    		int endIndex = -1;
 	    		String game = "";
 	    			
 	    		endIndex = val.indexOf(Const.xml_game_end);
 	    		game = val.substring(fromIndex, endIndex+Const.xml_game_end_length);
 	    		fromIndex = game.indexOf(Const.xml_gameid);
 	    		
 	    		try {
 	    			gameid = Integer.parseInt(game.substring(fromIndex+Const.xml_gameid_length, game.indexOf(Const.xml_gameid_end, fromIndex)));
 	    		} catch (Exception e) {
 	    			gameid = -1;
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_gcurl)) != -1) {
 	    			gcurl = game.substring(fromIndex+Const.xml_gcurl_length, game.indexOf(Const.xml_gcurl_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_gcurlflip)) != -1) {
 	    			gcurlflip = game.substring(fromIndex+Const.xml_gcurlflip_length, game.indexOf(Const.xml_gcurlflip_end, fromIndex));
 	    		}
 	    		val = val.substring(val.indexOf(Const.xml_game_end)+Const.xml_game_end_length);
 	    		if(gameid != -1){
 		    		synchronized(UiApplication.getEventLock()) {
 		    			if(!(Const.getPortrait())){
 		    				add(new ImageField(gcurlflip));
 		    			}else{
 		    				add(new ImageField(gcurl));
 		    			}
 		        	}
 		    		setText("Loading game...");
 		    		phase = "loadgame";
 		    		doConnect(Const.loadgame+"&gameid="+gameid+Const.height+Const.getCardHeight()+"&width=250");
 	    		}
 			}
 		} else if (phase.equals("loadgame")){
 			if(((fromIndex = val.indexOf(Const.xml_game)) != -1)){
 				lastmove = "";
 				lastmove64 = "";
 				String oppcards = "";
 				String oppname = "";
 				String usercards = "";
 				String username = "";
 	    		String statdesc = "";
 	    		int statid = -1;
 	    		int categorystatid = -1;
 	    		int statival = -1;
 				int stattop = 0;
 	    		int statleft = 0;
 	    		int statwidth = 0;
 	    		int statheight = 0;
 	    		int statfrontorback = 0;
 	    		int statcolorred = 0;
 	    		int statcolorgreen = 0;
 	    		int statcolorblue = 0;
 	    		String stattype = "";
 				int cardid = -1;
 				int gameplaycardid = -1;
 				String fronturl = "";
 				String frontflipurl = "";
 				String backurl = "";
 				String backflipurl = "";
 				int oppcardid = -1;
 				int oppgameplaycardid = -1;
 				String oppfronturl = "";
 				String oppfrontflipurl = "";
 				String oppbackurl = "";
 				String oppbackflipurl = "";
 	    		String gcurl = "";
 	    		String gcurlflip = "";
 	    		String explanation = "";
 	    		int endIndex = -1;
 	    		String game = "";
 	    			
 	    		endIndex = val.indexOf(Const.xml_game_end);
 	    		game = val.substring(fromIndex, endIndex+Const.xml_game_end_length);
 	    		
 	    		if ((fromIndex = game.indexOf(Const.xml_active)) != -1) {
 	    			active = game.substring(fromIndex+Const.xml_active_length, game.indexOf(Const.xml_active_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_explanation)) != -1) {
 	    			explanation = game.substring(fromIndex+Const.xml_explanation_length, game.indexOf(Const.xml_explanation_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_lastmove)) != -1) {
 	    			lastmove = game.substring(fromIndex+Const.xml_lastmove_length, game.indexOf(Const.xml_lastmove_end, fromIndex));
 	    			try{
 	    				lastmove64 = new String(Base64OutputStream.encode(lastmove.getBytes(), 0, lastmove.length(), false, false), "UTF-8");
 	    			}catch(Exception e){};
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_oppcards)) != -1) {
 	    			oppcards = game.substring(fromIndex+Const.xml_oppcards_length, game.indexOf(Const.xml_oppcards_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_oppname)) != -1) {
 	    			oppname = game.substring(fromIndex+Const.xml_oppname_length, game.indexOf(Const.xml_oppname_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_usercards)) != -1) {
 	    			usercards = game.substring(fromIndex+Const.xml_usercards_length, game.indexOf(Const.xml_usercards_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_username)) != -1) {
 	    			username = game.substring(fromIndex+Const.xml_username_length, game.indexOf(Const.xml_username_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_cardstats)) != -1){
 	    			stats = new Vector();
 	    			int k=0;
     				while ((fromIndex = game.indexOf(Const.xml_cardstat)) != -1 && k < 6){
     					k++;
     					statdesc = "";
     	    			statival = -1;
     	    			stattype = "";
     	    			statid = -1;
     	    			categorystatid = -1;
     	    			stattype = "";
 						if((fromIndex = game.indexOf(Const.xml_top)) != -1){
     						try{
     							stattop = Integer.parseInt(game.substring(fromIndex+Const.xml_top_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_top_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_left)) != -1){
     						try{
     							statleft = Integer.parseInt(game.substring(fromIndex+Const.xml_left_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_left_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_width)) != -1){
     						try{
     							statwidth = Integer.parseInt(game.substring(fromIndex+Const.xml_width_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_width_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_height)) != -1){
     						try{
     							statheight = Integer.parseInt(game.substring(fromIndex+Const.xml_height_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_height_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_frontorback)) != -1){
     						try{
     							statfrontorback = Integer.parseInt(game.substring(fromIndex+Const.xml_frontorback_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_frontorback_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_red)) != -1){
     						try{
     							statcolorred = Integer.parseInt(game.substring(fromIndex+Const.xml_red_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_red_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_green)) != -1){
     						try{
     							statcolorgreen = Integer.parseInt(game.substring(fromIndex+Const.xml_green_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_green_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_blue)) != -1){
     						try{
     							statcolorblue = Integer.parseInt(game.substring(fromIndex+Const.xml_blue_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_blue_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if ((fromIndex = game.indexOf(Const.xml_ival)) != -1) {
     						try {
     							statival = Integer.parseInt(game.substring(fromIndex+Const.xml_ival_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_ival_length)));
     						} catch (Exception e) {
     							statival = -1;
     						}
     					}
     					if ((fromIndex = game.indexOf(Const.xml_stattype)) != -1) {
     						stattype = game.substring(fromIndex+Const.xml_stattype_length, game.indexOf(Const.xml_stattype_end, fromIndex));
     					}
     					if ((fromIndex = game.indexOf(Const.xml_statdescription)) != -1) {
     						statdesc = game.substring(fromIndex+Const.xml_statdescription_length, game.indexOf(Const.xml_statdescription_end, fromIndex));
     					}
     					if ((fromIndex = game.indexOf(Const.xml_cardstatid)) != -1) {
     						try {
     							statid = Integer.parseInt(game.substring(fromIndex+Const.xml_cardstatid_length, game.indexOf(Const.xml_cardstatid_end, fromIndex)));
     						} catch (Exception e) {
     							statid = -1;
     						}
     					}
     					if ((fromIndex = game.indexOf(Const.xml_categorystatid)) != -1) {
     						try {
     							categorystatid = Integer.parseInt(game.substring(fromIndex+Const.xml_categorystatid_length, game.indexOf(Const.xml_categorystatid_end, fromIndex)));
     						} catch (Exception e) {
     							categorystatid = -1;
     						}
     					}
     					Stat s = new Stat(statdesc, "", statival, stattop, statleft, statwidth, statheight, statfrontorback, statcolorred, statcolorgreen, statcolorblue);
     					s.setStatType(stattype);
     					s.setStatId(statid);
     					s.setCategoryStatId(categorystatid);
     					stats.addElement(s);
     					game = game.substring(game.indexOf(Const.xml_cardstat_end)+Const.xml_cardstat_end_length);
     				}
     			}
 	    		if ((fromIndex = game.indexOf(Const.xml_cardid)) != -1) {
 	    			try {
 	        			cardid = Integer.parseInt(game.substring(fromIndex+Const.xml_cardid_length, game.indexOf(Const.xml_cardid_end, fromIndex)));
 	        		} catch (Exception e) {
 	        			cardid = -1;
 	        		}
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_gameplaycardid)) != -1) {
 	    			try {
 	    				gameplaycardid = Integer.parseInt(game.substring(fromIndex+Const.xml_gameplaycardid_length, game.indexOf(Const.xml_gameplaycardid_end, fromIndex)));
 	        		} catch (Exception e) {
 	        			gameplaycardid = -1;
 	        		}
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_fronturl)) != -1) {
 	    			fronturl = game.substring(fromIndex+Const.xml_fronturl_length, game.indexOf(Const.xml_fronturl_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_frontflipurl)) != -1) {
 	    			frontflipurl = game.substring(fromIndex+Const.xml_frontflipurl_length, game.indexOf(Const.xml_frontflipurl_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_backurl)) != -1) {
 	    			backurl = game.substring(fromIndex+Const.xml_backurl_length, game.indexOf(Const.xml_backurl_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_backflipurl)) != -1) {
 	    			backflipurl = game.substring(fromIndex+Const.xml_backflipurl_length, game.indexOf(Const.xml_backflipurl_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_cardstats)) != -1){
 	    			oppstats = new Vector();
     				while ((fromIndex = game.indexOf(Const.xml_cardstat)) != -1){
     					statdesc = "";
     	    			statival = -1;
     	    			stattype = "";
     	    			statid = -1;
     	    			categorystatid = -1;
     	    			stattype = "";
 						if((fromIndex = game.indexOf(Const.xml_top)) != -1){
     						try{
     							stattop = Integer.parseInt(game.substring(fromIndex+Const.xml_top_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_top_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_left)) != -1){
     						try{
     							statleft = Integer.parseInt(game.substring(fromIndex+Const.xml_left_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_left_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_width)) != -1){
     						try{
     							statwidth = Integer.parseInt(game.substring(fromIndex+Const.xml_width_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_width_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_height)) != -1){
     						try{
     							statheight = Integer.parseInt(game.substring(fromIndex+Const.xml_height_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_height_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_frontorback)) != -1){
     						try{
     							statfrontorback = Integer.parseInt(game.substring(fromIndex+Const.xml_frontorback_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_frontorback_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_red)) != -1){
     						try{
     							statcolorred = Integer.parseInt(game.substring(fromIndex+Const.xml_red_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_red_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_green)) != -1){
     						try{
     							statcolorgreen = Integer.parseInt(game.substring(fromIndex+Const.xml_green_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_green_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if((fromIndex = game.indexOf(Const.xml_blue)) != -1){
     						try{
     							statcolorblue = Integer.parseInt(game.substring(fromIndex+Const.xml_blue_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_blue_length)));
     						} catch(Exception e){
     							
     						}
     					}
     					if ((fromIndex = game.indexOf(Const.xml_ival)) != -1) {
     						try {
     							statival = Integer.parseInt(game.substring(fromIndex+Const.xml_ival_length, game.indexOf(Const.xml_end, fromIndex+Const.xml_ival_length)));
     						} catch (Exception e) {
     							statival = -1;
     						}
     					}
     					if ((fromIndex = game.indexOf(Const.xml_stattype)) != -1) {
     						stattype = game.substring(fromIndex+Const.xml_stattype_length, game.indexOf(Const.xml_stattype_end, fromIndex));
     					}
     					if ((fromIndex = game.indexOf(Const.xml_statdescription)) != -1) {
     						statdesc = game.substring(fromIndex+Const.xml_statdescription_length, game.indexOf(Const.xml_statdescription_end, fromIndex));
     					}
     					if ((fromIndex = game.indexOf(Const.xml_cardstatid)) != -1) {
     						try {
     							statid = Integer.parseInt(game.substring(fromIndex+Const.xml_cardstatid_length, game.indexOf(Const.xml_cardstatid_end, fromIndex)));
     						} catch (Exception e) {
     							statid = -1;
     						}
     					}
     					if ((fromIndex = game.indexOf(Const.xml_categorystatid)) != -1) {
     						try {
     							categorystatid = Integer.parseInt(game.substring(fromIndex+Const.xml_categorystatid_length, game.indexOf(Const.xml_categorystatid_end, fromIndex)));
     						} catch (Exception e) {
     							categorystatid = -1;
     						}
     					}
     					Stat s = new Stat(statdesc, "", statival, stattop, statleft, statwidth, statheight, statfrontorback, statcolorred, statcolorgreen, statcolorblue);
     					s.setStatType(stattype);
     					s.setStatId(statid);
     					s.setCategoryStatId(categorystatid);
     					oppstats.addElement(s);
     					game = game.substring(game.indexOf(Const.xml_cardstat_end)+Const.xml_cardstat_end_length);
     				}
     			}
 	    		if ((fromIndex = game.indexOf(Const.xml_cardid)) != -1) {
 	    			try {
 	        			oppcardid = Integer.parseInt(game.substring(fromIndex+Const.xml_cardid_length, game.indexOf(Const.xml_cardid_end, fromIndex)));
 	        		} catch (Exception e) {
 	        			oppcardid = -1;
 	        		}
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_gameplaycardid)) != -1) {
 	    			try {
 	    				oppgameplaycardid = Integer.parseInt(game.substring(fromIndex+Const.xml_gameplaycardid_length, game.indexOf(Const.xml_gameplaycardid_end, fromIndex)));
 	        		} catch (Exception e) {
 	        			oppgameplaycardid = -1;
 	        		}
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_fronturl)) != -1) {
 	    			oppfronturl = game.substring(fromIndex+Const.xml_fronturl_length, game.indexOf(Const.xml_fronturl_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_frontflipurl)) != -1) {
 	    			oppfrontflipurl = game.substring(fromIndex+Const.xml_frontflipurl_length, game.indexOf(Const.xml_frontflipurl_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_backurl)) != -1) {
 	    			oppbackurl = game.substring(fromIndex+Const.xml_backurl_length, game.indexOf(Const.xml_backurl_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_backflipurl)) != -1) {
 	    			oppbackflipurl = game.substring(fromIndex+Const.xml_backflipurl_length, game.indexOf(Const.xml_backflipurl_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_gcurl)) != -1) {
 	    			gcurl = game.substring(fromIndex+Const.xml_gcurl_length, game.indexOf(Const.xml_gcurl_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_gcurlflip)) != -1) {
 	    			gcurlflip = game.substring(fromIndex+Const.xml_gcurlflip_length, game.indexOf(Const.xml_gcurlflip_end, fromIndex));
 	    		}
 	    		if ((fromIndex = game.indexOf(Const.xml_phase)) != -1) {
 	    			phase = game.substring(fromIndex+Const.xml_phase_length, game.indexOf(Const.xml_phase_end, fromIndex));
 	    		}
 	    		card1 = new Card(cardid, "", 1, "", fronturl, backurl, "", 0, stats);
 	    		card1.setGamePlayerCardId(gameplaycardid);
 	    		card1.setFrontFlipurl(frontflipurl);
 	    		card1.setBackFlipurl(backflipurl);
 				card2 = new Card(oppcardid, "", 1, "", oppfronturl, oppbackurl, "", 0, oppstats);
 				card2.setGamePlayerCardId(oppgameplaycardid);
 				card2.setFrontFlipurl(oppfrontflipurl);
 	    		card2.setBackFlipurl(oppbackflipurl);
 	    		if(active.equals("1")&&phase.equals("stat")){
 	    			synchronized(UiApplication.getEventLock()) {
 		        		bgManager.deleteAll();
 		        		hbgManager.deleteAll();
 		        		vGameManager.deleteAll();
 		        		hGameManager.deleteAll();
 		        		oppvgamemanager.deleteAll();
 		        		opphgamemanager.deleteAll();
 		        		if(!(Const.getPortrait())){
 							vGameManager.drawbox = false;
 			    			oppvgamemanager.drawbox = false;
 						}else{
 							hGameManager.drawbox = false;
 			    			opphgamemanager.drawbox = false;
 						}
 		        		user = new ColorLabelField(" User: "+usercards+" cards, selecting stat");
 		        		opponent = new ColorLabelField(" Opponent: "+oppcards+" cards, waiting");
 		        		if(!(Const.getPortrait())){
 		        			bgManager.add(user);
 			    			vGameManager.setStatusHeight(options.getContentHeight());
 			    			vGameManager.setUrl(card1.getBackurl());
 			    			System.out.println("setting vGameManager url");
 			    			hbgManager.add(vGameManager);
 			    			oppvgamemanager = new VerticalGamePlayManager();
 			    			oppvgamemanager.setUrl(gcurl);
 			    			hbgManager.add(oppvgamemanager);
 			    			try{
 			    			bgManager.add(hbgManager);
 			    			}catch(Exception e){System.out.println(e.toString());};
 			    			bgManager.add(opponent);
 			    		}else{
 			    			bgManager.add(opponent);
 			    			opphgamemanager = new HorizontalGamePlayManager();
 			    			opphgamemanager.setUrl(gcurl);
 			    			bgManager.add(opphgamemanager);
 			    			hGameManager.setStatusHeight(options.getContentHeight());
 			    			hGameManager.setUrl(card1.getBackurl());
 			    			System.out.println("setting hGameManager url");
 			    			bgManager.add(hGameManager);
 			    			bgManager.add(user);
 			    		}
 		        		uistats = new StatField [stats.size()];
 		        		for(int i = 0; i < stats.size(); i++){
 		        			if(!(Const.getPortrait())){
 		        				uistats[i] = new StatField ((Stat)stats.elementAt(i), vGameManager.image);
 		        			}else{
 		        				uistats[i] = new StatField ((Stat)stats.elementAt(i), hGameManager.image);
 		        			}
 		        			uistats[i].gamestat = true;
 		        			uistats[i].flip = 1;
 		        			uistats[i].setChangeListener(this);
 		        			if(!(Const.getPortrait())){
 		        				vGameManager.add(uistats[i]);
 		        			}else{
 		        				hGameManager.add(uistats[i]);
 		        			}
 		        		}
 		        		oppuistats = new StatField [oppstats.size()];
 		        		for(int i = 0; i < oppstats.size(); i++){
 		        			if(!(Const.getPortrait())){
 		        				oppuistats[i] = new StatField ((Stat)oppstats.elementAt(i), oppvgamemanager.image);
 		        			}else{
 		        				oppuistats[i] = new StatField ((Stat)oppstats.elementAt(i), opphgamemanager.image);
 		        			}
 		        			oppuistats[i].gamestat = true;
 		        			oppuistats[i].flip = 1;
 		        			oppuistats[i].setFocusable(false);
 		        			if(!(Const.getPortrait())){
 		        				oppvgamemanager.add(oppuistats[i]);
 		        			}else{
 		        				opphgamemanager.add(oppuistats[i]);
 		        			}
 		        		}
 		        	}
 				} else if(active.equals("0")&&phase.equals("stat")){
 	    			synchronized(UiApplication.getEventLock()) {
 		        		bgManager.deleteAll();
 		        		hbgManager.deleteAll();
 		        		vGameManager.deleteAll();
 		        		hGameManager.deleteAll();
 		        		if(!(Const.getPortrait())){
 							vGameManager.drawbox = false;
 			    			oppvgamemanager.drawbox = false;
 						}else{
 							hGameManager.drawbox = false;
 			    			opphgamemanager.drawbox = false;
 						}
 		        		user = new ColorLabelField(" User: "+usercards+" cards, waiting");
 		        		opponent = new ColorLabelField(" Opponent: "+oppcards+" cards, selecting stat");
 		        		if(!(Const.getPortrait())){
 		        			bgManager.add(user);
 			    			vGameManager.setStatusHeight(options.getContentHeight());
 			    			vGameManager.setUrl(card1.getBackurl());
 			    			System.out.println("setting vGameManager url");
 			    			hbgManager.add(vGameManager);
 			    			oppvgamemanager = new VerticalGamePlayManager();
 			    			oppvgamemanager.setUrl(card2.getBackurl());
 			    			hbgManager.add(oppvgamemanager);
 			    			bgManager.add(hbgManager);
 			    			bgManager.add(opponent);
 			    		}else{
 			    			bgManager.add(opponent);
 			    			opphgamemanager = new HorizontalGamePlayManager();
 			    			opphgamemanager.setUrl(card2.getBackurl());
 			    			bgManager.add(opphgamemanager);
 			    			hGameManager.setStatusHeight(options.getContentHeight());
 			    			hGameManager.setUrl(card1.getBackurl());
 			    			System.out.println("setting hGameManager url");
 			    			bgManager.add(hGameManager);
 			    			bgManager.add(user);
 			    			
 			    		}
 		        		uistats = new StatField [stats.size()];
 		        		for(int i = 0; i < stats.size(); i++){
 		        			if(!(Const.getPortrait())){
 		        				uistats[i] = new StatField ((Stat)stats.elementAt(i), vGameManager.image);
 		        			}else{
 		        				uistats[i] = new StatField ((Stat)stats.elementAt(i), hGameManager.image);
 		        			}
 		        			uistats[i].gamestat = true;
 		        			uistats[i].flip = 1;
 		        			uistats[i].setFocusable(false);
 		        			if(!(Const.getPortrait())){
 		        				vGameManager.add(uistats[i]);
 		        			}else{
 		        				hGameManager.add(uistats[i]);
 		        			}
 		        		}
 		        		oppuistats = new StatField [oppstats.size()];
 		        		for(int i = 0; i < oppstats.size(); i++){
 		        			if(!(Const.getPortrait())){
 		        				oppuistats[i] = new StatField ((Stat)oppstats.elementAt(i), oppvgamemanager.image);
 		        			}else{
 		        				oppuistats[i] = new StatField ((Stat)oppstats.elementAt(i), opphgamemanager.image);
 		        			}
 		        			oppuistats[i].gamestat = true;
 		        			oppuistats[i].flip = 1;
 		        			oppuistats[i].setFocusable(false);
 		        			if(!(Const.getPortrait())){
 		        				oppvgamemanager.add(oppuistats[i]);
 		        			}else{
 		        				opphgamemanager.add(oppuistats[i]);
 		        			}
 		        		}
 		        	}
 	    			phase = "loadgame";
 					//try{
 					//	Thread.sleep(4000);
 					//}catch(Exception e){};
 					System.out.println(Const.continuegame+"&gameid="+gameid+"&lastmove="+lastmove64+Const.height+Const.getCardHeight()+"&width=250");
 		    		doConnect(Const.continuegame+"&gameid="+gameid+"&lastmove="+lastmove64+Const.height+Const.getCardHeight()+"&width=250");
 				} else if(phase.equals("oppmove")){
 					phase = "loadgame";
 					if ((fromIndex = game.indexOf(Const.xml_categorystatid)) != -1) {
 						try {
 							categorystatid = Integer.parseInt(game.substring(fromIndex+Const.xml_categorystatid_length, game.indexOf(Const.xml_categorystatid_end, fromIndex)));
 						} catch (Exception e) {
 							categorystatid = -1;
 						}
 					}
 					if(categorystatid!=-1){
 						boolean draw = false;
 						for(int u = 0; u < 5; u++){
 							draw = !draw;
 							synchronized(UiApplication.getEventLock()) {
 								uistats[categorystatid-1].draw(draw);
 								oppuistats[categorystatid-1].draw(draw);
 								if(((Stat)stats.elementAt(categorystatid-1)).getVal()>((Stat)oppstats.elementAt(categorystatid-1)).getVal()){
 									if(!(Const.getPortrait())){
 						    			vGameManager.draw(draw,"GREEN");
 						    			oppvgamemanager.draw(draw,"RED");
 									}else{
 										hGameManager.draw(draw,"GREEN");
 						    			opphgamemanager.draw(draw,"RED");
 									}
 								}else if(((Stat)stats.elementAt(categorystatid-1)).getVal()<((Stat)oppstats.elementAt(categorystatid-1)).getVal()){
 									if(!(Const.getPortrait())){
 										vGameManager.draw(draw,"RED");
 						    			oppvgamemanager.draw(draw,"GREEN");
 									}else{
 										hGameManager.draw(draw,"RED");
 						    			opphgamemanager.draw(draw,"GREEN");
 									}
 								}else{
 									if(!(Const.getPortrait())){
 										vGameManager.draw(draw,"YELLOW");
 						    			oppvgamemanager.draw(draw,"YELLOW");
 									}else{
 										hGameManager.draw(draw,"YELLOW");
 						    			opphgamemanager.draw(draw,"YELLOW");
 									}
 								}
 							}
 							if(u != 4){
 								try{
 									Thread.sleep(1000);
 								}catch(Exception e){};
 							}
 						}
 					}
 					doConnect(Const.loadgame+"&gameid="+gameid+Const.height+Const.getCardHeight()+"&width=250");
 					//doConnect(Const.continuegame+"&gameid="+gameid+"&lastmove="+lastmove64+Const.height+Const.getCardHeight()+"&width=250");
 				} else if(phase.equals("lfm")){
 					try{
 						Thread.sleep(4000);
 					}catch(Exception e){};
 					phase = "loadgame";
 					doConnect(Const.loadgame+"&gameid="+gameid+Const.height+Const.getCardHeight()+"&width=250");
 				}
 				else if(phase.equals("result")){
 					synchronized(UiApplication.getEventLock()) {
 		        		bgManager.deleteAll();
 		        		hbgManager.deleteAll();
 		        		vGameManager.deleteAll();
 		        		hGameManager.deleteAll();
 		        		try{	
 							hManager1.deleteAll();
 						}catch(Exception e){};
 		        		user = new ColorLabelField(explanation);
 		        		add(new ColorLabelField("Match Results:"));
 		        		add(user);
 		        		addButton(con);
 		        		addButton(options);
 		        		addButton(new FixedButtonField(""));
 					}
 				}
 			}
 		} 
     	invalidate();
 	}
 	
 	protected void onExposed() {
 		if(SettingsBean.getSettings().leavegame){
 			SettingsBean _instance = SettingsBean.getSettings();
 			_instance.leavegame = false;
 			SettingsBean.saveSettings(_instance);
 			UiApplication.getUiApplication().popScreen(this);
 		}
 	}
 	
 	public void fieldChanged(Field f, int i) {
 		System.out.println("FIELD: "+f.toString()+ " i "+i);
 		if (f == con || f == friendback) {
 			screen = null;
 			UiApplication.getUiApplication().popScreen(this);
 		} else if ((f == flips)) {
 			flip = !flip;
 			for(int j = 0; j < uistats.length; j++){
 				if(flip){
 					uistats[j].flip = 1;
 				}else{
 					uistats[j].flip = 0;
 				}
 			}
 			if (flip) {
 				if(!(Const.getPortrait())){
 					vGameManager.setUrl(card1.getBackurl());
 					vGameManager.invalidate();
 				}else{
 					hGameManager.setUrl(card1.getBackurl());
 					hGameManager.invalidate();
 					
 				}
 			} else {
 				if(!(Const.getPortrait())){
 					vGameManager.setUrl(card1.getFronturl());
 					vGameManager.invalidate();
 				}else{
 					hGameManager.setUrl(card1.getFronturl());
 					hGameManager.invalidate();
 				}
 			}
 		} else if (f == play){
 			if(username.getText().length() == 0){
 				user.setText("Please enter a username");
 			}else if(username.getText().equals(SettingsBean.getSettings().getUsername())){
 				user.setText("You cannot play against yourself");
 			}else{
 				synchronized(UiApplication.getEventLock()) {
 					try{	
 						remove(user);
 						remove(username);
 						hManager1.deleteAll();
 					}catch(Exception e){};
 					addButton(new FixedButtonField(""));
 					addButton(new FixedButtonField(""));
 					addButton(options);
 				}
 				user.setText("Initialising new game...");
 				String friend64="";
 				try{
 					friend64 = new String(Base64OutputStream.encode(username.getText().getBytes(), 0, username.getText().length(), false, false), "UTF-8");
 				}catch(Exception e){};
 				doConnect(Const.startnewgame+"&categoryid="+categoryId+"&newgametype="+newGameType+"&friend="+friend64+"&height="+Const.height+Const.getCardHeight()+"&width=250");
 			}
 		} else if (f == options){
 			screen = new GameOptionsScreen(gameid);
 			UiApplication.getUiApplication().pushScreen(screen);
 		} else{
 			System.out.println("wawawa active "+ active + " phase "+phase);
 			System.out.println("stats.size() "+ stats.size() + " oppuistats "+oppuistats.length);
 			if(active.equals("1")&&phase.equals("stat")){
 				for(int j = 0; j < stats.size(); j++){
 					Stat temp = (Stat)stats.elementAt(j);
 					if((temp.getFrontOrBack()==0&&!flip)||(temp.getFrontOrBack()==1&&flip)){
 						if(f == uistats[j]){
 							if(!(Const.getPortrait())){
 								oppvgamemanager.setUrl(card2.getBackurl());
 							}else{
 								opphgamemanager.setUrl(card2.getBackurl());
 							}
 							boolean draw = false;
 							for(int h = 0; h < 5;h++){
 								draw = !draw;
 								synchronized(UiApplication.getEventLock()) {
 									oppuistats[j].draw(draw);
 									uistats[j].draw(draw);
 									if(((Stat)stats.elementAt(j)).getVal()>((Stat)oppstats.elementAt(j)).getVal()){
 										if(!(Const.getPortrait())){
 							    			vGameManager.draw(draw,"GREEN");
 							    			oppvgamemanager.draw(draw,"RED");
 										}else{
 											hGameManager.draw(draw,"GREEN");
 							    			opphgamemanager.draw(draw,"RED");
 										}
 									}else if(((Stat)stats.elementAt(j)).getVal()<((Stat)oppstats.elementAt(j)).getVal()){
 										if(!(Const.getPortrait())){
 											vGameManager.draw(draw,"RED");
 							    			oppvgamemanager.draw(draw,"GREEN");
 										}else{
 											hGameManager.draw(draw,"RED");
 							    			opphgamemanager.draw(draw,"GREEN");
 										}
 									}else{
 										if(!(Const.getPortrait())){
 											vGameManager.draw(draw,"YELLOW");
 							    			oppvgamemanager.draw(draw,"YELLOW");
 										}else{
 											hGameManager.draw(draw,"YELLOW");
 							    			opphgamemanager.draw(draw,"YELLOW");
 										}
 									}
 								}
 								if(h != 4){
 									try{
 										Thread.sleep(1000);
 									}catch(Exception e){};
 								}
 							}
 							System.out.println(Const.selectstat+"&gameid="+gameid+"&statid="+temp.getStatId()+Const.height+Const.getCardHeight()+"&width=250");
 							phase = "loadgame";
 							doConnect(Const.selectstat+"&gameid="+gameid+"&statid="+temp.getStatId()+Const.height+Const.getCardHeight()+"&width=250");
 						}
 					}
 				}
 			}
 		}
 	}
 }
