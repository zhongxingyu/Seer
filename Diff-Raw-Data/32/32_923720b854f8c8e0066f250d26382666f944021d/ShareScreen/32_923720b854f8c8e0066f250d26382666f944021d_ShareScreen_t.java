 package net.mytcg.topcar.ui;
 
 import net.mytcg.topcar.ui.custom.ColorLabelField;
 import net.mytcg.topcar.ui.custom.FixedButtonField;
 import net.mytcg.topcar.ui.custom.SexyEditField;
 import net.mytcg.topcar.ui.custom.ThumbnailField;
 import net.mytcg.topcar.util.Card;
 import net.mytcg.topcar.util.Const;
 import net.mytcg.topcar.util.SettingsBean;
 import net.rim.blackberry.api.pdap.BlackBerryContact;
 import net.rim.blackberry.api.pdap.BlackBerryContactList;
 import net.rim.device.api.io.Base64OutputStream;
 import net.rim.device.api.system.RadioInfo;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.EditField;
 
 public class ShareScreen extends AppScreen implements FieldChangeListener
 {
 	FixedButtonField exit = new FixedButtonField(Const.back);
 	FixedButtonField con = new FixedButtonField(Const.con);
 	FixedButtonField confirm = new FixedButtonField(Const.confirm);
 	SexyEditField number = new SexyEditField("", EditField.FILTER_NUMERIC, 36);
 	SexyEditField username = new SexyEditField("");
 	SexyEditField email = new SexyEditField("");
 	ColorLabelField lblTop = new ColorLabelField("");
 	ColorLabelField lblusername = new ColorLabelField("Share with Username");
 	ColorLabelField lblemail = new ColorLabelField("Share with Email");
 	ColorLabelField lblphonenumber = new ColorLabelField("Share with Phone Number");
 	ThumbnailField temp = null;
 	String method = "";
 	String friendDetail = "";
 	
 	private int sendSize = 160;
 	private int numsms = 0;
 	private String[] smstext = null;
 	//private String message = " would like to share his Mobidex Business Card with you. Go to http://dex.mytcg.net/m/ to download his Card.";
 	private String smsnumber = "";
 	private Card card = null;
 	
 	public ShareScreen(Card card, AppScreen screen)
 	{
 		super(screen);
 	
 		this.card = card;
 		temp = new ThumbnailField(card);
		temp.setSecondLabel(card.getQuality());
 		temp.setThirdLabel("Rating: "+ card.getRating());
 		
 		temp.setFocusable(false);
 		add(temp);
 		add(lblTop);
 		add(lblusername);
 		add(username);
 		add(lblemail);
 		add(email);
 		add(lblphonenumber);
 		add(number);
 		
 		bgManager.setStatusHeight(exit.getContentHeight());
 		
 		exit.setChangeListener(this);
 		con.setChangeListener(this);
 		confirm.setChangeListener(this);
 		
 		addButton(con);
 		addButton(new FixedButtonField(""));
 		addButton(exit);
 	}
 	
 	public void fieldChanged(Field f, int i) {
 		if (f == exit) {
 			screen = null;
 			UiApplication.getUiApplication().popScreen(this);
 		} else if (f == con) {
 			method = "";
 			friendDetail = "";
 			if (username.getText().equals("")
 				&& email.getText().equals("")
 				&& number.getText().equals("")) {
 				lblTop.setText(" Fill in at least one field.");
 			}
 			else {
 				if (!username.getText().equals(""))
 				{
 					friendDetail = username.getText();
 					method = "username";
 				}
 				else if (!email.getText().equals(""))
 				{
 					friendDetail = email.getText();
 					method = "email";
 				}
 				else if (!number.getText().equals(""))
 				{
 					friendDetail = number.getText();
 					method = "phone_number";
 				}
 				synchronized(UiApplication.getEventLock()) {
 					try{
 						bgManager.deleteAll();
 						hManager1.deleteAll();
 					}catch(Exception e){System.out.println(e);};
 					addButton(confirm);
 					addButton(new FixedButtonField(""));
 					addButton(exit);
 					lblTop.setText("");
 					add(lblTop);
 					add(new ColorLabelField(" \n Send " + card.getDesc() + " to " + friendDetail + "?\n\n\n"));
 					add(temp);
 				}
 			}
 		}  else if (f == confirm) {
 			SettingsBean _instance = SettingsBean.getSettings();
 			_instance.shared = true;
 			SettingsBean.saveSettings(_instance);
 			synchronized(UiApplication.getEventLock()) {
 				try{
 					hManager1.deleteAll();
 				}catch(Exception e){};
 				addButton(new FixedButtonField(""));
 				addButton(new FixedButtonField(""));
 				addButton(exit);
 			}
 			doConnect("tradecard="+card.getId()+"&trademethod="+method+"&detail="+friendDetail);
 		}
 	}
 	
 	public void process(String val) {
 		int fromIndex = -1;
 		System.out.println(val);
 		if ((fromIndex = val.indexOf(Const.xml_result)) != -1) {
 			synchronized(UiApplication.getEventLock()) {
 				lblTop.setText(" "+val.substring(fromIndex+Const.xml_result_length, val.indexOf(Const.xml_result_end, fromIndex)));
 			}
 		}
 		
 	}
 }
