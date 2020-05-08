 /*
  * Copyright (c) 2009 Timothy Bourke
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the "BSD License" which is distributed with the
  * software in the file LICENSE.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the BSD
  * License for more details.
  */
 
 /*
  * TODO:
  *  - sign and run on phone (get rid of security warnings)
  *  - does not stop at end of new cards!
  *  
  */
 package mnemojojo;
 
 import java.lang.*;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.io.InputStream;
 import java.io.ByteArrayInputStream;
 import javax.microedition.io.Connector;
 import javax.microedition.io.HttpConnection;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.List;
 import javax.microedition.lcdui.Choice;
 import javax.microedition.lcdui.Gauge;
 import javax.microedition.lcdui.Alert;
 import javax.microedition.lcdui.AlertType;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.Font;
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 import mnemogogo.mobile.hexcsv.Card;
 import mnemogogo.mobile.hexcsv.HexCsv;
 import mnemogogo.mobile.hexcsv.FindCardDir;
 import mnemogogo.mobile.hexcsv.Progress;
 import mnemogogo.mobile.hexcsv.Unpack;
 
 import gr.fire.browser.Browser;
 import gr.fire.browser.util.Page;
 import gr.fire.ui.FireTheme;
 import gr.fire.core.*;
 import gr.fire.util.Log;
 import gr.fire.ui.ProgressbarAnimation;
 
 public class FireMIDlet
     extends Core
     implements CommandListener,
 	       gr.fire.core.CommandListener,
 	       KeyListener,
 	       gr.fire.core.KeyMapper,
 	       Runnable,
 	       Progress
 {
     boolean initialized = false;
 
     StringBuffer path;
     int pathLen;
 
     Display display;
     Browser browser;
     FireScreen screen;
 
     Panel questionPanel;
     Panel answerPanel;
 
     List activeList;
 
     private ProgressbarAnimation progressGauge=null;
     private int progressValue = 0;
     private int progressTotal = 0;
 
     Command cmdOk;
     Command cmdExit;
     Command cmdShow;
     Command cmdShowQ;
     Command cmdShowA;
 
     private final boolean debug = false;
 
     private int current;
     private final int CARD_DIRS = 0;
     private final int ABOUT = 1;
     private final int QUESTION = 2;
     private final int ANSWER = 3;
     private final int WAIT = 4;
 
     private final int TASK_NONE = 0;
     private final int TASK_LOAD = 1;
     private int task = TASK_NONE;
 
     public FireMIDlet()
     {
 	Log.showDebug = debug;
 
 	display = Display.getDisplay(this);
 
 	// initialize a browser instance
 	screen = FireScreen.getScreen(display);
 	screen.setFullScreenMode(true);
 	try {
 	    screen.setTheme(new FireTheme("file://theme.properties"));
 	} catch (Exception e) {}
 
 	browser = new Browser();
 
 	cmdOk = new Command(okText, Command.OK, 1); 
 	cmdExit = new Command(exitText, Command.EXIT, 5);
 	cmdShow = new Command(showText, Command.ITEM, 1);
 	cmdShowQ = new Command(closeText, Command.ITEM, 1);
 	cmdShowA = new Command(closeText, Command.ITEM, 1);
     }
 
     public void startApp()
 	throws MIDletStateChangeException
     {
 	if (!initialized) {
 	    showAbout();
 	    initialized = true;
 	}
     }
 
     public void destroyApp(boolean unconditional)
     {
 	startWait(savingText, 1, 0);
 	saveCards();
 	screen.destroy();
 	notifyDestroyed();
     }
 
     public void setCardDir(String cardPath)
 	throws IOException
     {
 	path = new StringBuffer(cardPath);
 	path.append("cards/");
 	pathLen = path.length();
     }
 
     private Panel makePage(String contents)
     {
 	try {
 	    ByteArrayInputStream in =
 		new ByteArrayInputStream(contents.getBytes("UTF-8"));
 	    Page page = browser.loadPage(in, "UTF-8");
 	    in.close();
 
 	    Panel panel = new Panel(page.getPageContainer(),
 		Panel.HORIZONTAL_SCROLLBAR | Panel.VERTICAL_SCROLLBAR, true);
 	    panel.setCommandListener(this);
 	    panel.setDragScroll(true);
 
 	    return panel;
 	} catch (Exception e) {
 	    showFatal(e.toString(), false);
 	}
 
 	return null;
     }
 
     private Panel loadPage(String pagePath)
     {
 	try {
 	    Page page = browser.loadPage(pagePath, HttpConnection.GET, null, null);
 
 	    Panel panel = new Panel(page.getPageContainer(),
 		Panel.HORIZONTAL_SCROLLBAR | Panel.VERTICAL_SCROLLBAR, true);
 	    panel.setCommandListener(this);
 	    panel.setDragScroll(true);
 
 	    return panel;
 	} catch (Exception e) {
 	    showFatal(e.toString(), false);
 	}
 
 	return null;
     }
 
     public void setCard(Card card, int numLeft)
 	throws Exception, IOException
     {
 	path.delete(pathLen, path.length());
 	path.append("Q");
 	card.appendSerial(path);
 	path.append(".htm");
 
 	String curTitle = card.categoryName() + "\t"
 			    + Integer.toString(numLeft);
 
 	questionPanel = loadPage(path.toString());
 	questionPanel.setLeftSoftKeyCommand(cmdShow);
 	questionPanel.setRightSoftKeyCommand(cmdExit);
 	questionPanel.setKeyListener(this);
 	questionPanel.setLabel(curTitle);
 
 	path.setCharAt(pathLen, 'A');
 	answerPanel = loadPage(path.toString());
 	answerPanel.setRightSoftKeyCommand(cmdExit);
 	answerPanel.setKeyMapper(this);
 	answerPanel.setKeyListener(this);
 	answerPanel.setLabel(curTitle);
     }
 
     void checkExportTime()
     {
 	int days_left = carddb.daysLeft();
 	byte icon;
 	String msg;
 
 	if (days_left < 0) {
 	    msg = updateOverdueText;
 	    icon = gr.fire.ui.Alert.TYPE_WARNING;
 	} else if (days_left == 0) {
 	    msg = updateTodayText;
 	    icon = gr.fire.ui.Alert.TYPE_INFO;
 	} else {
 	    return;
 	}
 
 	screen.showAlert(msg, icon,
 			 gr.fire.ui.Alert.USER_SELECTED_OK, cmdShowQ, this);
     }
 
     void showFatal(String msg, boolean exit)
     {
 	Command cmdAfter = null;
 	if (exit) {
 	    cmdAfter = cmdExit;
 	}
 
 	screen.showAlert(msg, gr.fire.ui.Alert.TYPE_ERROR,
 			 gr.fire.ui.Alert.USER_SELECTED_OK, cmdAfter, this);
     }
 
     void showDone()
     {
 	String msg = doneText;
 	screen.showAlert(msg,
 			 gr.fire.ui.Alert.TYPE_INFO,
 			 gr.fire.ui.Alert.USER_SELECTED_OK,
 			 cmdExit, this);
     }
 
     public void startOperation(int length)
     {
 	progressValue = 0;
 	progressTotal = length;
     }
 
     public void updateOperation(int delta)
     {
 	progressValue += delta;
 	if (progressGauge != null) {
 	    progressGauge.progress((100 * progressValue) / progressTotal);
 	}
     }
 
     void startWait(String msg, int length, int initial)
     {
 	showGauge(msg);
     }
 
     private void showCardDirList()
     {
 	int selected = -1;
 
 	startWait(lookingText, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
 	String[] dbs = FindCardDir.checkStandard();
 	if (dbs == null) {
 	    dbs = FindCardDir.list();
 	}
 
 	if (dbs == null || dbs.length == 0) {
 	    showFatal(nocardsText, true);
 	    return;
 	}
 
 	activeList = new List(openTitle, Choice.IMPLICIT);
 	for (int i=0; i < dbs.length; ++i) {
 	    activeList.append(dbs[i].substring(7), null);
 	    if (config.cardPath.equals(dbs[i])) {
 		selected = i;
 	    }
 	}
 
 	Command selectCommand = new Command(openText, Command.ITEM, 1);
 	activeList.setSelectCommand(selectCommand);
 
 	if (selected > 0) {
 	    activeList.setSelectedIndex(selected, true);
 	}
 
 	activeList.addCommand(
 	    new Command(exitText, Command.EXIT, 2));
 	activeList.setCommandListener(this);
 
 	display.setCurrent(activeList);
 	current = CARD_DIRS;
     }
 
     private void addStatRow(StringBuffer msg, String name, String stat)
     {
 	msg.append("<tr><td>");
 	msg.append(name);
 	msg.append("</td><td>");
 	msg.append(stat);
 	msg.append("</td></tr>");
     }
 
     private void addStatRow(StringBuffer msg, String name, int stat)
     {
 	addStatRow(msg, name, Integer.toString(stat));
     }
 
     private void addStatRow(StringBuffer msg, String name, float stat)
     {
 	addStatRow(msg, name, Float.toString(stat));
     }
 
     void showStats(int returnTo)
     {
 	StringBuffer msg = new StringBuffer("<body><p><table>");
 	addStatRow(msg, "", config.cardPath);
 	addStatRow(msg, daysRemainingText + ":", carddb.daysLeft());
 	msg.append("</table>");
 	if (curCard != null) {
 	    msg.append("<br/><br/><table>");
 	    addStatRow(msg, gradeText + ":", curCard.grade);
 	    addStatRow(msg, easinessText + ":", curCard.feasiness());
 	    addStatRow(msg, repetitionsText + ":", curCard.repetitions());
 	    addStatRow(msg, lapsesText + ":", curCard.lapses);
 	    addStatRow(msg, daysSinceLastText + ":",
 		curCard.daysSinceLastRep(carddb.days_since_start));
 	    addStatRow(msg, daysUntilNextText + ":",
 		curCard.daysUntilNextRep(carddb.days_since_start));
 	    msg.append("</table>");
 	}
 	msg.append("</p></body>");
 
 	Panel statPanel = makePage(msg.toString());
 
 	if (statPanel != null) {
 	    if (returnTo == ANSWER) {
 		statPanel.setRightSoftKeyCommand(cmdShowA);
 	    } else {
 		statPanel.setRightSoftKeyCommand(cmdShowQ);
 	    }
 
 	    statPanel.setKeyMapper(this);
 	    statPanel.setLabel(statisticsText);
 	    screen.setCurrent(statPanel);
 	}
     }
 
     void showAbout()
     {
 	Panel aboutPanel = loadPage("file://about.html");
 	aboutPanel.setLabel(versionInfo);
 	aboutPanel.setKeyMapper(this);
 
 	aboutPanel.setLeftSoftKeyCommand(cmdOk);
 	aboutPanel.setRightSoftKeyCommand(cmdExit);
 	aboutPanel.setKeyListener(this);
 
 	if (aboutPanel != null) {
 	    screen.setCurrent(aboutPanel);
 	}
 	current = ABOUT;
     }
 
     void showQuestionScreen()
     {
	if (questionPanel != null) {
	    screen.setCurrent(questionPanel);
	    current = QUESTION;
	} else {
	    showDone();
	}
     }
 
     void showAnswerScreen()
     {
 	screen.setCurrent(answerPanel);
 	current = ANSWER;
     }
 
     private void showNextQuestion()
     {
 	if (nextQuestion()) {
 	    //System.out.println(carddb.toString());
 	    showQuestionScreen();
 	    startThinking();
 	} else {
 	    showDone();
 	}
     }
 
     private void unpackDatabase()
     {
 	/*
 	String path = config.cardPath + "cards.db";
 
 	try {
 	    FileConnection c = (FileConnection)Connector.open(path, Connector.READ);
 	    if (c.lastModified() > config.cards_mtime) {
 		startWait(unpackingText, 1, 0);
 		startOperation((int)c.fileSize());
 		config.cards_mtime = c.lastModified();
 
 		Unpack u = new Unpack(this);
 		u.unpack(c.openDataInputStream(), "file://");
 	    }
 	    c.close();
 	} catch (IOException e) {}
 	*/
     }
 
     public void commandAction(javax.microedition.lcdui.Command cmd, Component c)
     {
 	String label = cmd.getLabel();
 
 	if (cmd.equals(cmdShowQ)) {
 	    showQuestionScreen();
 	    unpauseThinking();
 	    return;
 
 	} else if (cmd.equals(cmdShowA)) {
 	    showAnswerScreen();
 	    return;
 	}
 
 	if (current == ABOUT && label.equals(okText)) {
 	    if (config.cardPath.equals("")
 		|| !(FindCardDir.isCardDir(new StringBuffer(config.cardPath))))
 	    {
 		showCardDirList();
 	    } else {
 		startWait(loadingText, 1, 0);
 		loadCards();
 		unpackDatabase();
 		//carddb.dumpCards();
 		showNextQuestion();
 		checkExportTime();
 	    }
 
 	} else if (label.equals(showText)) {
 	    showAnswerScreen();
 
 	} else if (label.equals(exitText)) {
 	    startWait(savingText, 1, 0);
 	    saveCards();
 	    notifyDestroyed();
 	}
     }
 
     public void commandAction(javax.microedition.lcdui.Command cmd, Displayable dis)
     {
 	String title = dis.getTitle();
 	String label = cmd.getLabel();
 
	if (cmd.equals(cmdShowQ)) {
	    showQuestionScreen();
	    unpauseThinking();
	    return;

	} else if (cmd.equals(cmdShowA)) {
	    showAnswerScreen();
	    return;
	}

 	if (label.equals(exitText)) {
 	    startWait(savingText, 1, 0);
 	    saveCards();
 	    notifyDestroyed();
 	    return;
 
 	} else if (label.equals(openText)) {
 	    task = TASK_LOAD;
 	    showAbout();
 	    startWait(loadingText, 1, 0);
 	    display.callSerially(this);
 	    return;
 	}
     }
 
     public int mapKey(int keyCode)
     {
 	switch (keyCode)
 	{
 	case FireScreen.KEY_NUM0: return keyCode;
 	case FireScreen.KEY_NUM1: return keyCode;
 	case FireScreen.KEY_NUM2: return keyCode;
 	case FireScreen.KEY_NUM3: return keyCode;
 	case FireScreen.KEY_NUM4: return keyCode;
 	case FireScreen.KEY_NUM5: return keyCode;
 
 	case FireScreen.KEY_NUM6: return FireScreen.UP;
 	case FireScreen.KEY_NUM7: return FireScreen.LEFT;
 	case FireScreen.KEY_NUM8: return FireScreen.RIGHT;
 	case FireScreen.KEY_NUM9: return FireScreen.DOWN;
 
 	case FireScreen.KEY_STAR: return keyCode;
 	case FireScreen.KEY_POUND: return keyCode;
 
 	default: return screen.getGameAction(keyCode);
 	}
     }
 
     public void keyReleased(int code, Component src)
     {
 	switch (current) {
 	case ABOUT:
 	    switch (code)
 	    {
 	    case FireScreen.KEY_STAR:
 		showCardDirList();
 		break;
 	    }
 	    break;
 
 	case QUESTION:
 	    switch (code)
 	    {
 	    case FireScreen.KEY_STAR:
 		curCard.skip = true;
 		showNextQuestion();
 		break;
 
 	    case FireScreen.KEY_POUND:
 		showStats(QUESTION);
 		break;
 	    }
 	    break;
 
 	case ANSWER:
 	    int grade = -1;
 
 	    switch(code)
 	    {
 	    case FireScreen.KEY_NUM0: grade = 0; break;
 	    case FireScreen.KEY_NUM1: grade = 1; break;
 	    case FireScreen.KEY_NUM2: grade = 2; break;
 	    case FireScreen.KEY_NUM3: grade = 3; break;
 	    case FireScreen.KEY_NUM4: grade = 4; break;
 	    case FireScreen.KEY_NUM5: grade = 5; break;
 	    case FireScreen.KEY_POUND: showStats(ANSWER); break;
 	    }
 
 	    if (grade != -1) {
 		doGrade(grade);
 		showNextQuestion();
 	    }
 
 	    break;
 	
 	default:
 	    break;
 	}
     }
 
     public void keyRepeated(int code, Component src) {	}
     public void keyPressed(int code, Component src) { }
 
     public void run()
     {
 	switch (task) {
 	case TASK_LOAD:
 	    StringBuffer file = new StringBuffer("file://");
 	    file.append(activeList.getString(activeList.getSelectedIndex()));
 	    String newCardPath = file.toString();
 	    activeList = null;
 
 	    config.cardPath = newCardPath;
 	    config.save();
 
 	    try {
 		//setCardDir("file://");
 		setCardDir(config.cardPath.toString());
 	    } catch (IOException e) {
 		showFatal(e.toString(), true);
 		return;
 	    }
 
 	    loadCards();
 	    showNextQuestion();
 
 	    checkExportTime();
 	    unpackDatabase();
 
 	    task = TASK_NONE;
 	    break;
 
 	default:
 	    break;
 	}
     }
 
     private void showGauge(String message)
     {
 	if (progressGauge != null) {
 	    hideGauge();
 	}
 	
 	progressGauge = new ProgressbarAnimation(message);
 	
 	Font font = FireScreen.getTheme().getFontProperty("titlebar.font");
 	int sw = screen.getWidth();
 	int mw = font.stringWidth(message);
 
 	progressGauge.setWidth(sw);
 	progressGauge.setHeight(font.getHeight());
 	screen.addComponent(progressGauge, 6);
     }
     
     private void hideGauge()
     {
 	if (progressGauge != null) {
 	    screen.removeComponent(progressGauge);
 	    progressGauge=null;
 	}
     }
 }
 
