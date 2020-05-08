 /*
  * Copyright (C) 2010 Timothy Bourke
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc., 59
  * Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package mnemojojo;
 
 import java.lang.*;
 import java.io.IOException;
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
 import javax.microedition.lcdui.Alert;
 import javax.microedition.lcdui.AlertType;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.Font;
 
 import mnemogogo.mobile.hexcsv.Card;
 import mnemogogo.mobile.hexcsv.FindCardDirJ2ME;
 import mnemogogo.mobile.hexcsv.Progress;
 
 import gr.fire.browser.Browser;
 import gr.fire.browser.util.Page;
 import gr.fire.ui.FireTheme;
 import gr.fire.core.FireScreen;
 import gr.fire.core.KeyListener;
 import gr.fire.core.Component;
 import gr.fire.core.Theme;
 import gr.fire.util.Log;
 import gr.fire.util.FireConnector;
 import gr.fire.core.FireListener;
 
 // for buttons:
 import gr.fire.core.BoxLayout;
 import gr.fire.core.Container;
 import gr.fire.core.GridLayout;
 import gr.fire.ui.InputComponent;
 
 public class FireMIDlet
     extends Core
     implements CommandListener,
                gr.fire.core.CommandListener,
                KeyListener,
                FireListener
 {
     boolean initialized = false;
 
     StringBuffer path;
     int pathLen;
 
     Display display;
     HttpClient httpClient;
     Browser browser;
     FireScreen screen;
 
     Panel currentPanel;
     Card currentCard;
     String currentTitle;
 
     ProgressGauge progressGauge;
 
     Command cmdOk;
     Command cmdExit;
     Command cmdShow;
     Command cmdShowQ;
     Command cmdShowA;
     Command cmdReshow;
     Command cmdButton;
 
     private final boolean debug = false;
 
     private int current;
     private final int CARD_DIRS = 0;
     private final int ABOUT = 1;
     private final int QUESTION = 2;
     private final int ANSWER = 3;
     private final int WAIT = 4;
     private final int KEYMAP = 5;
 
     public FireMIDlet()
     {
         Log.showDebug = debug;
 
         display = Display.getDisplay(this);
 
         screen = FireScreen.getScreen(display);
         config.setScreen(screen);
 
         screen.setFullScreenMode(true);
         try {
             if (config.isBigScreen) {
                 screen.setTheme(new FireTheme("file://hires.properties"));
             } else {
                 screen.setTheme(new FireTheme("file://normal.properties"));
             }
         } catch (Exception e) {}
 
         screen.setFireListener(this);
 
         progressGauge = new ProgressGauge();
         progressHandler = (Progress)progressGauge;
 
         httpClient = new HttpClient(new FireConnector());
         browser = new Browser(httpClient);
         CardPanel.browser = browser;
         StatsPanel.browser = browser;
 
         cmdOk = new Command(okText, Command.OK, 1); 
         cmdExit = new Command(exitText, Command.EXIT, 5);
         cmdShow = new Command(showText, Command.ITEM, 1);
         cmdShowQ = new Command(closeText, Command.ITEM, 1);
         cmdShowA = new Command(closeText, Command.ITEM, 1);
         cmdReshow = new Command(closeText, Command.ITEM, 1);
         cmdButton = new Command("invisible", Command.OK, 1);
     }
 
     public void startApp()
         throws MIDletStateChangeException
     {
         if (!initialized) {
             if (config.leftSoftKey != 0) {
                 screen.leftSoftKey = config.leftSoftKey;
             }
             if (config.rightSoftKey != 0) {
                 screen.rightSoftKey = config.rightSoftKey;
             }
             showAbout();
             initialized = true;
         }
     }
 
     public void destroyApp(boolean unconditional)
     {
         saveCards();
         super.destroyApp(unconditional);
         screen.destroy();
         notifyDestroyed();
     }
 
     public void setCardDir(String cardpath)
         throws IOException
     {
         httpClient.setUrlPrefix(cardpath);
         path = new StringBuffer(cardpath);
         pathLen = path.length();
     }
 
     public void setCard(Card card, int numLeft)
         throws Exception, IOException
     {
         if (card != null) {
             currentTitle = card.categoryName() + "\t"
                                 + Integer.toString(numLeft);
         }
         currentCard = card;
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
 
     void showStats()
     {
         Panel statsPanel = (Panel)new StatsPanel(curCard, carddb, config);
 
         if (statsPanel != null) {
             statsPanel.setRightSoftKeyCommand(cmdReshow);
             statsPanel.setCommandListener(this);
             statsPanel.setKeyListener(this);
             screen.setCurrent(statsPanel);
         }
     }
 
     void showAbout()
     {
         AboutPanel aboutPanel = new AboutPanel(screen, versionInfo,
                                         this, cmdOk, cmdExit, config);
 
         // copy across the current configured values
         if ((config.cardpath != null)
             && (FindCardDirJ2ME.isCardDir(config.cardpath, null))) {
             aboutPanel.cardpath = config.cardpath;
         } else {
             config.cardpath = null;
             aboutPanel.cardpath = null;
         }
         aboutPanel.fontSize = config.fontSize;
         aboutPanel.cardsToLoad = config.cardsToLoad;
         aboutPanel.touchScreen = config.showButtons;
         aboutPanel.centerText = config.centerText;
         int i = 0;
         while (i < 6) {
             aboutPanel.keys[i] = config.gradeKey[i];
             ++i;
         }
         aboutPanel.keys[i++] = config.statKey;
         aboutPanel.keys[i++] = config.skipKey;
 
         current = ABOUT;
         screen.setCurrent(aboutPanel);
         aboutPanel.repaintControls();
     }
 
     void showQuestionScreen()
     {
         if (currentCard != null) {
             currentPanel = (Panel)new CardPanel(
                 curCard, true, currentTitle,
                 config, this, this,
                 cmdShow, cmdExit, cmdButton);
             screen.setCurrent(currentPanel);
             current = QUESTION;
         } else {
             showDone();
         }
     }
 
     void showAnswerScreen()
     {
         currentPanel = (Panel)new CardPanel(
             curCard, false, currentTitle,
             config, this, this,
             null, cmdExit, cmdButton);
         screen.setCurrent(currentPanel);
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
 
     public void updateFont(int fontsize)
     {
         Theme theme = screen.getTheme();
         Font cur_font = theme.getFontProperty("xhtml.font");
         Font new_font = Font.getFont(cur_font.getFace(),
                                      cur_font.getStyle(),
                                      fontsize);
         theme.setFontProperty("xhtml.font", new_font);
         screen.setTheme(theme);
     }
 
     /* FireListener methods */
 
     public void sizeChanged(int newWidth, int newHeight)
     {
         try {
             Panel panel = (Panel)screen.getCurrent();
             panel.screenSizeChanged(newWidth, newHeight);
 
         } catch (ClassCastException e) { }
 
         if ((currentPanel != null) && (currentPanel != screen.getCurrent())) {
             try {
                 Panel panel = (Panel)currentPanel;
                 panel.screenSizeChanged(newWidth, newHeight);
             } catch (ClassCastException e) { }
         }
     }
 
     public void hideNotify() { }
     public void showNotify() { }
 
     /* Respond to input methods */
 
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
         
         } else if (cmd.equals(cmdReshow)) {
             screen.setCurrent(currentPanel);
             currentPanel.validate();
 
         } else if (cmd.equals(cmdButton)) {
             String val = ((InputComponent)c).getValue();
             System.out.println("button: " + val);
 
             if (showAnswerText.equals(val)) {
                 showAnswerScreen();
 
             } else if (skipCardText.equals(val)) {
                 curCard.setSkip();
                 showNextQuestion();
 
             } else if (showStatsText.equals(val)) {
                 showStats();
 
             } else {
                 try {
                     doGrade(Integer.parseInt(val));
                     showNextQuestion();
                 } catch (NumberFormatException e) { }
             }
 
         }
 
         if (current == ABOUT) {
             // Save the settings on both Ok and Exit
             AboutPanel aboutPanel = (AboutPanel)c;
 
             if (aboutPanel.dirty) {
                 config.cardpath = aboutPanel.cardpath;
                 config.fontSize = aboutPanel.fontSize;
                 if (aboutPanel.cardsToLoad > 0) {
                     config.cardsToLoad = aboutPanel.cardsToLoad;
                 }
                 config.showButtons = aboutPanel.touchScreen;
                 config.centerText = aboutPanel.centerText;
                 int i = 0;
                 while (i < 6) {
                     config.gradeKey[i] = aboutPanel.keys[i];
                     ++i;
                 }
                 config.statKey = aboutPanel.keys[i++];
                 config.skipKey = aboutPanel.keys[i++];
 
                 config.leftSoftKey = screen.leftSoftKey;
                 config.rightSoftKey = screen.rightSoftKey;
 
                 config.save((Progress)progressGauge);
             }
         }
 
         if ((current == ABOUT) && (label.equals(okText))) {
 
             updateFont(config.fontSize);
 
             if (config.cardpath == null) {
                 showFatal(selectCarddir, false);
 
             } else {
                 loadCards();
                 //carddb.dumpCards();
                 showNextQuestion();
                 checkExportTime();
             }
 
         } else if (label.equals(showText)) {
             showAnswerScreen();
 
         } else if (label.equals(exitText)) {
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
 
         } else if (cmd.equals(cmdReshow)) {
             screen.setCurrent(currentPanel);
             currentPanel.validate();
             return;
         }
     }
 
     public void keyReleased(int code, Component src)
     {
         switch (current) {
         case QUESTION:
             if (code == config.skipKey) {
                 curCard.setSkip();
                 showNextQuestion();
 
             } else if (code == config.statKey) {
                 showStats();
             }
             break;
 
         case ANSWER:
             int grade = -1;
 
             for (int i=0; i < config.gradeKey.length; ++i) {
                 if (code == config.gradeKey[i]) {
                     grade = i;
                     break;
                 }
             }
 
             if (grade != -1) {
                 doGrade(grade);
                 showNextQuestion();
 
             } else if (code == config.statKey) {
                 showStats();
             }
         
         default:
             break;
         }
     }
 
     public void keyRepeated(int code, Component src) {  }
     public void keyPressed(int code, Component src) { }
 }
 
