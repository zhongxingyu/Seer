 package net.mytcg.topcar.ui;
 
 import net.mytcg.topcar.http.ConnectionGet;
 import net.mytcg.topcar.ui.custom.BackgroundManager;
 import net.mytcg.topcar.ui.custom.ColorLabelField;
 import net.mytcg.topcar.ui.custom.HorizontalBackgroundManager;
 import net.mytcg.topcar.ui.custom.HorizontalGamePlayManager;
 import net.mytcg.topcar.ui.custom.HorizontalStatManager;
 import net.mytcg.topcar.ui.custom.VerticalGamePlayManager;
 import net.mytcg.topcar.ui.custom.VerticalStatManager;
 import net.mytcg.topcar.util.Const;
 import net.mytcg.topcar.util.SettingsBean;
 import net.rim.device.api.math.Fixed32;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.BitmapField;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.container.HorizontalFieldManager;
 import net.rim.device.api.ui.container.MainScreen;
 
 public class AppScreen extends MainScreen {
 	protected BackgroundManager bgManager = new BackgroundManager();
 	protected HorizontalBackgroundManager hbgManager = new HorizontalBackgroundManager();
 	protected VerticalStatManager vStatManager = new VerticalStatManager();
 	protected HorizontalStatManager hStatManager = new HorizontalStatManager();
 	protected VerticalGamePlayManager vGameManager = new VerticalGamePlayManager();
 	protected HorizontalGamePlayManager hGameManager = new HorizontalGamePlayManager();
 	protected BackgroundManager titleManager = new BackgroundManager(false) {
 		
 		/*public int getPreferredHeight() {
 			return logo.getHeight()+(Const.FONT);
 		}*/
 		/*public void paint(Graphics g) {
 			g.setColor(Color.RED);
 			Font _font = getFont();
 			_font = _font.derive(Font.PLAIN, Const.FONT-2);
 			g.setFont(_font);
 			setFont(_font);
 			//super.paint(g);
 			
 			int xPts1[] = {0,0,padding,padding};
 			int yPts1[] = {0,logoleft.getHeight(),logoright.getHeight(),0};
 			g.drawTexturedPath(xPts1,yPts1,null,null,0,0,Fixed32.ONE,0,0,Fixed32.ONE,logoleft);
 			
 			int xPts2[] = {padding+logo.getWidth(),padding+logo.getWidth(),getPreferredWidth(),getPreferredWidth()};
 			int yPts2[] = {0,logoright.getHeight(),logoright.getHeight(),0};
 			g.drawTexturedPath(xPts2,yPts2,null,null,0,0,Fixed32.ONE,0,0,Fixed32.ONE,logoright);
 			
 		}*/
 	};
 	
 	protected boolean navigationMovement(int dx, int dy, int status, int time)
     {
 	        this.invalidate(); 
 	        return super.navigationMovement(dx, dy, status, time);
 	}
 	
 	protected BackgroundManager statusManager = new BackgroundManager(false);
 	protected HorizontalFieldManager hManager1 = new HorizontalFieldManager();
 	
 	protected ColorLabelField status = new ColorLabelField("", LabelField.FIELD_HCENTER);
 	protected Bitmap logo = Const.getLogo();
 	protected Bitmap logoleft = Const.getLogoLeft();
 	protected Bitmap logoright = Const.getLogoRight();
 	private int padding = (int)(((double)(Const.getWidth()-logo.getWidth()))/2);
 	public boolean created = false;
 	protected AppScreen screen = null;
 	protected AppScreen parent = null;
 	
 	public void onUndisplay() {
 	}
 	public void onUnfocus() {
 	}
 	public void onVisibilityChange(boolean visible) {
 	}
 	public void focusRemove() {
 	}
 	protected void drawFocus(Graphics g, boolean x) {
 	}
 	
 	private boolean isDisplaying = false;
 	
 	public boolean isDisplaying() {
 		return isDisplaying;
 	}
 	public void setDisplaying(boolean display) {
 		isDisplaying = display;
 	}
 	
 	public void setText(String text) {
 		if ((text != null)&&(text.length() > 0)) {
 			final String msg = text;
 			UiApplication.getUiApplication().invokeLater(new Runnable()
 			{
 			      public void run()
 			     {
 			           synchronized(UiApplication.getEventLock()){
 			        	   status.setText("\n" + msg + "\n\n");
 							bgManager.invalidate();
 			           }
 			     }
 			});
 		} else {
 			UiApplication.getUiApplication().invokeLater(new Runnable()
 			{
 			      public void run()
 			     {
 			           synchronized(UiApplication.getEventLock()){
 			        	   status.setText("");
 							bgManager.invalidate();
 			           }
 			     }
 			});
 		}
 	}
 	public boolean onClose() {
 		Const.getConnection().setBusy();
 		close();
 		return true;
 	}
 	
 	public void close() {
 		synchronized(UiApplication.getEventLock()) {
 			screen = null;
 			UiApplication.getUiApplication().popScreen(this);
 		}
 	}
 	
 	public AppScreen(AppScreen parent) {
 		super();
 		add(titleManager);
 		add(bgManager);
 		this.parent = parent;
 		//title setup
 		titleManager.add(new BitmapField(logo, BitmapField.FIELD_HCENTER));
 		titleManager.add(status);
 		
 		bgManager.setTitleHeight(Const.getLogoHeight()+(Const.FONT-2));
 		
 		//status setup
 		statusManager.add(hManager1);
 		setStatus(statusManager);
 	}
 	public AppScreen(AppScreen parent, boolean noheader) {
 		super(NO_VERTICAL_SCROLL | NO_VERTICAL_SCROLLBAR);
 		if(!(Const.getPortrait())){
 			super.add(hStatManager);
 		}else{
 			super.add(vStatManager);
 		}
 		this.parent = parent;
 		
 		//status setup
 		statusManager.add(hManager1);
 		setStatus(statusManager);
 	}
 	
 	public AppScreen(boolean noheader) {
 		super();
 		
 		if(!(Const.getPortrait())){
 			super.add(hbgManager);
 		}else{
 			add(bgManager);
 		}
 		
 		this.parent = null;
 		
 		//status setup
 		statusManager.add(hManager1);
 		setStatus(statusManager);
 	}
 	
 	public AppScreen(boolean noheader, boolean gameplay) {
 		super(NO_VERTICAL_SCROLL | NO_VERTICAL_SCROLLBAR);
 		
 		add(bgManager);
 		
 		this.parent = null;
 		
 		//status setup
 		statusManager.add(hManager1);
 		setStatus(statusManager);
 	}
 	
 	public void pop() {
 		if (parent != null) {
 			UiApplication.getUiApplication().popScreen(this);
 			parent.pop();
 		} else if (parent == null) {
 			screen = null;
 		}
 	}
 	
 	public void addButton(Field field) {
 		hManager1.add(field);
 	}
 	
 	public void removeButton(Field field){
 		hManager1.delete(field) ;
 	}
 	
 	public void add(Field field) {
 		if(field instanceof BackgroundManager)
 			super.add(field);
 		else
 			bgManager.add(field);
 	}
 	
 	public void remove(Field field) {
 		if(field instanceof BackgroundManager)
 			super.delete(field);
 		else
 			bgManager.delete(field);
 	}
 	
 	public void addStat(Field field) {
 		if(!(Const.getPortrait())){
 			hStatManager.add(field);
 		}else{
 			vStatManager.add(field);
 		}
 		
 	}
 	
 	public void process(String val) {
 		synchronized(UiApplication.getEventLock()) {
 			int fromIndex = -1;
 			if ((fromIndex = val.indexOf(Const.xml_result)) != -1) {
 				setText(val.substring(fromIndex+Const.xml_result_length, val.indexOf(Const.xml_result_end, fromIndex)));
 	    	}
 		}
 	}
 	
 	public void doConnect(String url, boolean autoclean) {
 		setText("Attempting Connection, Please Wait...");
 		ConnectionGet cG = new ConnectionGet(url, this, autoclean);
 		cG.start();
 	}
 	public void doConnect(String url) {
 		setText("Attempting Connection, Please Wait...");
 		ConnectionGet cG = new ConnectionGet(url, this);
 		cG.start();
 	}
 	public void clear() {
 		bgManager.deleteAll();
 	}
 	
 	public Field getField(int index)
 	{ return bgManager.getField(index); }
 	public int getFieldCount()
 	{ return bgManager.getFieldCount(); }
 	public Field getFieldWithFocus()
 	{ return bgManager.getFieldWithFocus(); }
 	public int getFieldWithFocusIndex()
 	{ return bgManager.getFieldWithFocusIndex(); }
 	public void delete(Field field)
 	{ bgManager.delete(field); }
 	public void deleteAll()
 	{ bgManager.deleteAll(); }
 	public void deleteRange(int start, int count)
 	{ bgManager.deleteRange(start, count); }
 }
