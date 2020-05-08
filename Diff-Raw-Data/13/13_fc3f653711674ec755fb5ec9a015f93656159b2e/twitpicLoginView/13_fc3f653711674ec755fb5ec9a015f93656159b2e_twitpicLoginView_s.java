 package net.exiva.twitpic;
 
 import danger.app.Application;
 import danger.app.Event;
 import danger.app.Timer;
 
 import danger.ui.AlertWindow;
 import danger.ui.Button;
 import danger.ui.ProgressBar;
 import danger.ui.ScreenWindow;
 import danger.ui.TextField;
 
 public class twitpicLoginView extends ScreenWindow implements Resources, Commands {
 	private static Button login;
 	private static TextField username, password;
 	private static ProgressBar throbber;
 	private static Timer mTimer;
 	
 	public twitpicLoginView() {
 		mTimer = new Timer(200, true, this, 1);
 	}
 
 	public void onDecoded() {
 		username = (TextField)this.getDescendantWithID(ID_USERNAME);
 		password = (TextField)this.getDescendantWithID(ID_PASSWORD);
 		throbber = (ProgressBar)this.getChildWithID(ID_THROBBER);
 		login = (Button)this.getDescendantWithID(ID_LOGIN_BUTTON);
 	}
 
 	public static twitpicLoginView create() {
 		twitpicLoginView me = (twitpicLoginView) Application.getCurrentApp().getResources().getScreen(ID_LOGIN_SCREEN, null);
 		return me;
 	}
 
 	public void setLogin(String user, String pass) {
 		((TextField)this.getDescendantWithID(ID_USERNAME)).setText(user);
 		((TextField)this.getDescendantWithID(ID_PASSWORD)).setText(pass);
 	}
 
 	public void showAbout() {
 		AlertWindow about = getApplication().getAlert(ID_ABOUT, this);
 		about.show();
 	}
 
 	public static void enableInput() {
 		username.enable();
 		password.enable();
 		throbber.hide();
 		mTimer.stop();
 		login.enable();
 	}
 
 	public void disableInput() {
 		username.disable();
 		password.disable();
 		throbber.show();
 		mTimer.start();
 		login.disable();
 	}
 
 	public static void stopThrobber() {
 		mTimer.stop();
 		throbber.hide();
 	}
 
 	public boolean receiveEvent(Event e) {
 		switch (e.type) {
 			case Event.EVENT_TIMER: {
 				if (e.data==1) {
 					throbber.advanceState();
 				}
 				return false;
 			}
 			case EVENT_STORE_LOGIN: {
				twitpic.checkAuth(username.toString(), password.toString());
				disableInput();
 				return true;
 			}
 			case ABOUT: {
 				showAbout();
 				return true;
 			}
 			default:
 			break;
 		}
 		return super.receiveEvent(e);
 	}
 
     public boolean eventWidgetUp(int inWidget, Event e) {
 		switch (inWidget) {
 			case Event.DEVICE_BUTTON_CANCEL:
 			Application.getCurrentApp().returnToLauncher();
 			return true;
 
 			case Event.DEVICE_BUTTON_BACK:
 			Application.getCurrentApp().returnToLauncher();
 			return true;
 		}
 		return super.eventWidgetUp(inWidget, e);
 	}
 }
