 package net.mytcg.dex.ui;
 
 import net.mytcg.dex.ui.custom.ColorLabelField;
 import net.mytcg.dex.ui.custom.FixedButtonField;
 import net.mytcg.dex.ui.custom.SexyEditField;
 import net.mytcg.dex.util.Const;
 import net.mytcg.dex.util.SettingsBean;
 import net.rim.device.api.io.Base64OutputStream;
 import net.rim.device.api.system.KeyListener;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.UiApplication;
 
 public class SearchScreen extends AppScreen implements FieldChangeListener, KeyListener
 {
 	FixedButtonField back = new FixedButtonField(Const.back);
 	FixedButtonField search = new FixedButtonField(Const.search);
 	SexyEditField number = new SexyEditField("enter search term");
 	ColorLabelField lbl = new ColorLabelField("You can search by name, company or even email. Any detail on a Mobidex card will help you find it.", ColorLabelField.FIELD_HCENTER);
 	
 	boolean fresh = true;
 
 	public boolean keyChar(char key, int status, int time)
 	{
 		if (fresh) {
 			number.setText("");
 			fresh = !fresh;
 		}
 		return super.keyChar(key, status, time);
 	}
 	public boolean keyDown(int keyCode, int time) 
 	{
 		//System.out.println("keyDown");
 		return super.keyDown(keyCode, time);
 	}
 	public boolean keyRepeat(int keyCode, int time) 
 	{
 		//System.out.println("keyRepeat");
 		return super.keyRepeat(keyCode, time);
 	}
 	public boolean keyStatus(int keyCode, int time) 
 	{
 		//System.out.println("keyStatus");
 		return super.keyStatus(keyCode, time);
 	}
 	public boolean keyUp(int keyCode, int time) 
 	{
 		//System.out.println("keyUp");
 		return super.keyUp(keyCode, time);
 	}
 	
 	public SearchScreen(AppScreen screen)
 	{
 		super(screen);
 		
 		
 		add(number);
 		add(lbl);
 		
 		bgManager.setStatusHeight(back.getContentHeight());
 		
 		back.setChangeListener(this);
 		search.setChangeListener(this);
 		number.setChangeListener(this);
 		
 		addButton(search);
 		addButton(new FixedButtonField(""));
 		addButton(back);
 	}
 	
 	public void process(String val) {
 		synchronized(UiApplication.getEventLock()) {
 			
 			//screen = null;
 			//UiApplication.getUiApplication().popScreen(this);
 			
 			screen = new AlbumListScreen(val);
 			UiApplication.getUiApplication().pushScreen(screen);
 		}
 	}
 	
 	public void fieldChanged(Field f, int i) {
 		if (f == back) {
 			
 			screen = null;
 			UiApplication.getUiApplication().popScreen(this);
 			
 			//screen = new AlbumScreen();
 			//UiApplication.getUiApplication().pushScreen(screen);
 		} else if (f == search) {
 			String search64="";
 			try {
 				search64 = new String(Base64OutputStream.encode(number.getText().getBytes(), 0, number.getText().length(), false, false), "UTF-8");
 			} catch (Exception e) {
 				
 			}
			doConnect(Const.seek+search64+Const.height+Const.getCardHeight()+Const.jpg+Const.bbheight+Const.getAppHeight()+Const.width+Const.getCardWidth()+Const.second+SettingsBean.getSettings().getLoaded());
 		} else if (f == number) {
 			System.out.println("test");
 		}
 	}
 }
