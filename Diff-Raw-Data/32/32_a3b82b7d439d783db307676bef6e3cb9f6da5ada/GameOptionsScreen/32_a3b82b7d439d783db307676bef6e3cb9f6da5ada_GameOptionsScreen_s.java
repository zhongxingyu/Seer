 package net.mytcg.topcar.ui;
 
 import net.mytcg.topcar.ui.custom.FixedButtonField;
 import net.mytcg.topcar.ui.custom.ListItemField;
 import net.mytcg.topcar.ui.custom.ListLabelField;
 import net.mytcg.topcar.util.Const;
 import net.mytcg.topcar.util.SettingsBean;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.UiApplication;
 
 public class GameOptionsScreen extends AppScreen implements FieldChangeListener{
 	private ListItemField exit = new ListItemField("Leave Game", -1, true, 0);
 	private ListItemField gamelog = new ListItemField("View Game Log", -1, true, 0);
 	private ListLabelField tmp = new ListLabelField("Empty");
 	private FixedButtonField back = new FixedButtonField(Const.back);
 	private int gameid = -1;
 	public GameOptionsScreen(int gameid)
 	{
 		super(null);
 		this.gameid = gameid;
 		bgManager.setStatusHeight(back.getContentHeight());
 		
 		exit.setChangeListener(this); 
 		gamelog.setChangeListener(this);
 		back.setChangeListener(this);
 		add(exit);
 		add(gamelog);
 	
 		addButton(new FixedButtonField(""));
 		addButton(new FixedButtonField(""));
 		addButton(back);
 	}
 	
 	public void process(String val) {
 		System.out.println("wawawa "+val);
 		if (!(isDisplaying())) {
 			int fromIndex;
 	    	if ((fromIndex = val.indexOf(Const.xml_result)) != -1) {
 	    		setText(val.substring(fromIndex+Const.xml_result_length, val.indexOf(Const.xml_result_end, fromIndex)));
 	    	} else if (((fromIndex = val.indexOf(Const.xml_logs)) != -1)) {
 	    		String date = "";
 	    		String description = "";
 	    		int endIndex = -1;
 	    		String log = "";
 	    		while ((fromIndex = val.indexOf(Const.xml_date)) != -1){
 	    			
 	    			endIndex = val.indexOf(Const.xml_log_end);
 	    			log = val.substring(fromIndex, endIndex+Const.xml_log_end_length);
 	    			fromIndex = log.indexOf(Const.xml_date);
 	    			
 	    			if ((fromIndex = log.indexOf(Const.xml_date)) != -1) {
 	    				date = log.substring(fromIndex+Const.xml_date_length, log.indexOf(Const.xml_date_end, fromIndex));
 	    			}
 	    			if ((fromIndex = log.indexOf(Const.xml_description)) != -1) {
 	    				description = log.substring(fromIndex+Const.xml_description_length, log.indexOf(Const.xml_description_end, fromIndex));
 	    			}
 	    			val = val.substring(val.indexOf(Const.xml_log_end)+Const.xml_log_end_length);
 	    			if(date.length() > 0){
 		    			synchronized(UiApplication.getEventLock()) {
 		    				tmp = new ListLabelField(date+": "+description);
 		        			add(tmp);
 		        		}
 	    			}
 	    		}
	    	} else {
	    		synchronized(UiApplication.getEventLock()) {
        			add(tmp);
        		}
	    	}
 	    	invalidate();
 	    	setDisplaying(true);
 		}		
 	}
 	
 	public void fieldChanged(Field f, int i) {
 		if (f == back) {
 			screen = null;
 			UiApplication.getUiApplication().popScreen(this);
 		}else if(f == gamelog){
 			synchronized(UiApplication.getEventLock()) {
 				back.setLabel("Continue");
 				try{
 					bgManager.deleteAll();
 				}catch(Exception e){};
     		}
 			doConnect(Const.viewgamelog+"&gameid="+gameid);
 		} else if (f == exit){
 			SettingsBean _instance = SettingsBean.getSettings();
 			_instance.leavegame = true;
 			SettingsBean.saveSettings(_instance);
 			screen = null;
 			UiApplication.getUiApplication().popScreen(this);
 		}
 	}
 }
