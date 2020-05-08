 /*
  * cognet chat app
  *
  * Copyright 2003, Brian Swetland <swetland@frotz.net>
  * See LICENSE for redistribution terms
  *
  */
 package org.twodot.cognet;
 
 import danger.app.Event;
 import danger.app.ResourceDatabase;
 import danger.app.Application;
 
 import java.util.Date;
 import danger.util.format.DateFormat;
 
 import danger.ui.*;
 import danger.audio.Meta;
 import danger.util.DEBUG;
 
 public class ChatWindow extends ScreenWindow implements Resources, Events
 {
 	public ChatWindow() {
 		//default constructor for rsrc decoding
 		super();
 		app = (Cognet)Application.getCurrentApp();
 		//setFullScreen(true);
 	}
 
 	public void onDecoded() {
 		history = (ChatHistory)this.getDescendantWithID(kChatHistory);
		// Scrollbar scrollbar = (Scrollbar)this.getDescendantWithID(ID_ABOUT_TEXT_SCROLLBAR);
 		line = (CommandLine)this.getDescendantWithID(kCommandLine);
 		this.setIcon(app.getBundle().getSmallIcon());
		ScrollView sv = (ScrollView)this.getDescendantWithID(ID_SCROLL_BAR);
		sv.calcBottom();
		sv.invalidate();
 	}
 
 	public static ChatWindow newChatWindow(String ctxt, String key, Font font, boolean smileys) {
 		//public ChatWindow(Engine e, String ctxt, String key) {
 		smileys = false;
 		ResourceDatabase rdb = Application.getCurrentApp().getResources();
 		ChatWindow w = (ChatWindow)rdb.getScreen(kChatWindow);
 		w.setFont(font);
 		w.setEmoticons(smileys);
 
 		w.context = ctxt;
 		w.key = key;
 		w.setTitle(ctxt);
 
 		if(key.charAt(0) == '%') w.special = true;
 		if(key.charAt(0) == '#') w.channel = true;
 		if(key.charAt(0) == '&') w.channel = true;
 
 		return w;
 	}
 
 	public void show() {
 		//todo: somethin here instead of null check
 		if (app.engine != null)
 			app.engine.MakeActive(this);
 		super.show();
 	}
 
 	public final void adjustActionMenuState(Menu menu) {
 		app.MakeMenu(getActionMenu(),this);
 		}
 
 	public void ScrollList(int count) {
 		if(count < 0) {
 			count = -count;
 			for(int i = 0; i < count; i++) history.ScrollUp();
 		} else {
 			for(int i = 0; i < count; i++) history.ScrollDown();
 		}
 		history.invalidate();
 	}
 
 	public boolean eventWidgetUp(int widget, Event event) {
 		switch(widget) {
 			case Event.DEVICE_WHEEL:
 				ScrollList(-1);
 			return true;
 			case Event.DEVICE_WHEEL_PAGE_UP:
 				ScrollList(-8);
 			return true;
 			case Event.DEVICE_WHEEL_PAGE_DOWN:
 				ScrollList(8);
 			return true;
 			case Event.DEVICE_BUTTON_BACK:
 				getApplication().returnToLauncher();
 			return true;
 			default:
 			return super.eventWidgetUp(widget,event);
 		}
 	}
 
 	public boolean eventWidgetDown(int widget, Event event) {
 		switch(widget) {
 			case Event.DEVICE_WHEEL:
 				ScrollList(1);
 			return true;
 			case Event.DEVICE_WHEEL_PAGE_DOWN:
 				ScrollList(8);
 			return true;
 			case Event.DEVICE_BUTTON_LEFT_SHOULDER:
 				prev.show();
 			return true;
 			case Event.DEVICE_BUTTON_RIGHT_SHOULDER:
 				next.show();
 			return true;
 			case Event.DEVICE_NAVIGATION:
 				if(event.getVerticalCount() > 0) {
 					ScrollList(1);
 				} else {
 					ScrollList(-1);
 				}
 			return true;
 			default:
 			return super.eventWidgetDown(widget,event);
 		}
 	}
 
 	public boolean receiveEvent(Event e) {
 		//e.DebugPrint();
 		switch(e.type){
 			case kMessage:
 			case kTempMessage:
 				String s = (String) e.argument;
 				//s = s.replace(BOLD,SPACE);
 				//s = s.replace(UNDERLINE,SPACE);
 				//s = s.replace(COLOR,SPACE);
 				if (e.type == kMessage)
 					history.AddChannel(s);
 				else
 					history.AddTemporaryLines(s,0);
 				//DEBUG.p("S was: "+s);
 				history.invalidate();
 				if(!active) {
 					if(!mailbox) {
 						//DEBUG.p("new pending notifications here");
 						mailbox = true;
 						app.engine.UpdateAlerts();
 						//app.setChooserFolderLabel("vomits");
 					}
 					//if(!channel && !special){
 						//}
 					}
 
 					if (!app.fgapp) {
 						long timeNow = System.currentTimeMillis();
 						if ((CognetSettings.getDoMarquee()) && (timeNow >= (lastMarquee + (1000 * marqueeTimeout)))) {
 							NotificationManager.marqueeAlertNotify(new MarqueeAlert(s, app.getBitmap(kSpeechBubble),1));
 							lastMarquee = timeNow;
 						}
 						app.Notify();
 					}
 
 					Date date = new Date();
 					setSubTitle("Last Message " + DateFormat.withFormat("h:mma", date));
 					return true;
 			case kClear:
 				history.Clear();
 				history.invalidate();
 			return true;
 
 			case kChatWindowSetFont:
 				this.setFont((Font)e.argument);
 			return true;
 
 			case kChatWindowSetSmileys:
 				this.setEmoticons(false);
 			return true;
 
 			case kChatWindowSetBubbles:
 				history.setBubbles((boolean[])e.argument);
 			return true;
 
 			case kChatWindowSetReadOnly:
 				line.setEnabled(e.what == 0);
 				line.setEditable(e.what == 0);
 				line.invalidate();
 			return true;
 
 			case CommandLine.kCommandLineCommand:
 				app.engine.Command(this.key,(String)e.argument);
 			return true;
 
 			case kChatWindowClearTemporaryMessages:
 				history.clearTemoraryLines();
 			return true;
 		}
 		return super.receiveEvent(e);
 	}
 
 	void MSG(String s, boolean temporary) {
 		int msg = kMessage;
 		if (temporary) msg = kTempMessage;
 		Event e = new Event(this, msg);
 		e.argument = s;
 		//jb: switched to low priority event to prevent freeze on resync.
		// getDefaultListenerer().sendLowPriorityEvent(e);
		getDefaultListener().sendLowPriorityEvent(e);
 	}
 
 	void MSG(String s) {
 		MSG(s, false);
 	}
 
 	void CLR() {
		// getListener().sendEvent(new Event(this, kClear));
		getDefaultListener().sendEvent(new Event(this, kClear));
 	}
 
 	void URL(String URL, String Category, String Name) {
 		links.AddLink(URL, Category, Name);
 		app.MakeMenu(getActionMenu(),this);
 	}
 
 	void SaveQuick(char c) {
 		app.engine.SetQuick(context, c);
 		try {
 			app.engine.Write("save", "quick/" + c, context);
 		} catch (Exception e) {
 			// ignore save error
 		}
 	}
 
 	void toggleFullScreen() {
 		setFullScreen(!isFullScreen());
 		DEBUG.p("toggle fs");
 		//resize(getWidth(), getHeight());
 		invalidate();
 	}
 
 	public boolean eventShortcut(char c, Event e) {
 		switch(c){
 			case 84: // 'T'
 				setFullScreen(!isFullScreen());
 				invalidate();
 			return true;
 			
 			case 'n':
 				next.show();
 			return true;
 			case 'p':
 				prev.show();
 			return true;
 			//case 'l':
 				//app.engine.login.show();
 			//return true;
 			case ',':
 				ChatWindow cw = app.engine.NextMailbox(this);
 				if(cw != null) cw.show();
 			return true;
 			case '!': SaveQuick('1'); return true;
 			case '@': SaveQuick('2'); return true;
 			case '#': SaveQuick('3'); return true;
 			case '$': SaveQuick('4'); return true;
 			case '%': SaveQuick('5'); return true;
 			case '^': SaveQuick('6'); return true;
 			case '&': SaveQuick('7'); return true;
 			case '*': SaveQuick('8'); return true;
 			case '(': SaveQuick('9'); return true;
 			case ')': SaveQuick('0'); return true;
 			default:
 		    System.err.println("Got shortcut " + c);
 			return super.eventShortcut(c, e);
 		}
 	}
 
 	Cognet app;
 	ChatHistory history;
 	CommandLine line;
 	//Engine engine;
 	String context;
 	String key;
 
 	void Remove() {
 		next.prev = prev;
 		prev.next = next;
 		next = this;
 		prev = this;
 		hide();
 	}
 
 	void SetQuickKey(char quickkey) {
 		this.quickkey = quickkey;
 		if(quickkey != '\0'){
 			setTitle(context);
 		} else {
 			setTitle(context);
 		}
 	}
 
 	private void setFont(Font f) {
 		if (f != null) {
 			history.setPlainFont(f);
 			history.rebreak();
 			history.invalidate();
 		}
 	}
 
 	private void setEmoticons(boolean onOff) {
 		history.setEmoticons(onOff);
 	}
 
 	char quickkey;
 	boolean active;
 	boolean mailbox;
 	boolean special;
 	boolean channel;
 	long lastMarquee;
 	public final int marqueeTimeout = 60;		// one minute
 	public LinkMenu links = new LinkMenu();
 	ChatWindow next, prev;
 	public static final int kChatWindowActive = 450;
 	public static final int kChatWindowSetFont = 451;
 	public static final int kChatWindowSetSmileys = 452;
 	public static final int kChatWindowSetBubbles = 453;
 	public static final int kChatWindowSetReadOnly = 454;
 	public static final int kChatWindowClearTemporaryMessages = 455;
 	public static final int kMessage = 1;
 	public static final int kClear = 2;
 	public static final int kTempMessage = 3;
 
 	public static class CommandLine extends EditText {
 		public CommandLine() {
 			scrollback = new String[SCROLLBACK_BUFFER_SIZE];
 			insert = 0;
 			consume = 1;
 			this.setAutoText(true);
 			this.setAutoCap(true);
 			this.setSpellCheckEnabled(true);
 		}
 
 		public boolean eventWidgetUp(int i, Event event) {
 			boolean handled = false;
 
 			switch (event.what) {
 				case Event.DEVICE_ARROW_UP:
 				//if we're not currently scrolling back, then
 				//store current text in scrollback before moving up one.
 				if (insert == consume) {
 					scrollback[insert] = toString();
 				}
 				int prev = modulo(consume-1, SCROLLBACK_BUFFER_SIZE);
 				if (prev != insert & scrollback[prev] != null) {
 					consume = prev;
 					setText(scrollback[consume]);
 					handled = true;
 					} else
 						Meta.play(Meta.BEEP_COMMAND_REJECTED);
 					break;
 
 				case Event.DEVICE_ARROW_DOWN:
 				if (insert == consume)
 					Meta.play(Meta.BEEP_COMMAND_REJECTED);
 				else {
 					consume = modulo(consume+1, SCROLLBACK_BUFFER_SIZE);
 					setText(scrollback[consume]);
 					handled = true;
 				}
 				break;
 			}
 			return handled || super.eventWidgetUp(i, event);
         }
 
 		public boolean eventKeyUp(char key, Event event) {
 			if((key == '\n') || (key == '\r')){
 				String val = toString();
 				this.getParent().sendEvent(kCommandLineCommand,0,0, val);
 				int next = modulo(insert+1, SCROLLBACK_BUFFER_SIZE);
 				if (val.endsWith("\n") || val.endsWith("\r"))
 					val = val.substring(0, val.length()-1);
 				scrollback[insert] = val;
 				consume = insert = next;
 				this.clear();
 				return true;
 			} else {
 				return super.eventKeyUp(key, event);
 			}
 		}
 
 		public int modulo(int x, int b) {
 			return ((x%b)+b) % b;
 		}
 
 		public static final int kCommandLineCommand = 600;
 		public static final int SCROLLBACK_BUFFER_SIZE = 20;
 		int insert;
 		int consume;
 		String scrollback[];
 	}
 
 	public static final char BOLD = 0x02;
 	public static final char UNDERLINE = 0x1F;
 	public static final char COLOR = 0x03;
 	public static final char SPACE = 0x20;
 		Engine engine;
 			ChatWindow menuWindow;
 }
