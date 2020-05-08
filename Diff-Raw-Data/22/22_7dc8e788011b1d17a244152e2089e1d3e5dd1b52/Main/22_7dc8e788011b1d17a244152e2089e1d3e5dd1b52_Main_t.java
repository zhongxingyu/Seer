 package com.tung;
 
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 
 public class Main extends MIDlet {
 
 	public static final char NEWLINE = '\n';
 
 	protected Result result;
 	
 	private Display display;
 	
 	protected EnterResultForm enterResultForm;
 	protected ResultSender resultSender;
 	protected ResultView homeResultView, visitorResultView;
 	protected ResultList resultList;
 	protected MatchInfo matchInfo;
 	
 	protected Navigation navigation;
 	
 	public Main() {
 		display = Display.getDisplay(this);
 		
 		navigation = new Navigation();
 		
 		result = new Result();
 		matchInfo = new MatchInfo(display, this);
 		
 		resultSender = new ResultSender(display, result, this);
 		homeResultView = new HomeResultView(result, display, this);
 		visitorResultView = new VisitorResultView(result, display, this);
 		resultList = new ResultList(display, this);
 		
 		enterResultForm = new EnterResultForm(display, result, this);
 	}
	
 	protected void destroyApp(boolean arg0) throws MIDletStateChangeException { }
 
 	protected void pauseApp() { }
 
 	protected void startApp() throws MIDletStateChangeException {
 		display.setCurrent(enterResultForm);
 	}
 
 }
