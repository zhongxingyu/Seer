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
 
 import java.io.IOException;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.midlet.MIDletStateChangeException;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Font;
 
 import mnemogogo.mobile.hexcsv.Card;
 import mnemogogo.mobile.hexcsv.FindCardDirJ2ME;
 import mnemogogo.mobile.hexcsv.Progress;
 
 import gr.fire.browser.Browser;
 import gr.fire.ui.FireTheme;
 import gr.fire.core.FireScreen;
 import gr.fire.core.KeyListener;
 import gr.fire.core.Component;
 import gr.fire.core.Theme;
 import gr.fire.util.Log;
 import gr.fire.util.FireConnector;
 import gr.fire.core.FireListener;
 
 // for buttons:
 import gr.fire.ui.InputComponent;
 
 public class FireMIDlet
     extends Core
     implements CommandListener,
                gr.fire.core.CommandListener,
                KeyListener,
                // net.rim.device.api.system.KeyListener, /* BlackBerry */
                FireListener
 {
     boolean initialized = false;
 
     StringBuffer path;
     int pathLen;
 
     Display display;
     HttpClient httpClient;
     Browser browser;
     FireScreen screen;
     SoundPlayer player;
 
     Panel currentPanel;
     Card currentCard;
     boolean answerPlayed = false;
     String currentTitle;
 
     ProgressGauge progressGauge;
 
     Command cmdOk;
     Command cmdExit;
     Command cmdLearnAhead;
     Command cmdShow;
     Command cmdShowQ;
     Command cmdShowA;
     Command cmdReshow;
     Command cmdButton;
 
     private final boolean debug = false;
 
     private int current;
     private static final int ABOUT = 1;
     private static final int QUESTION = 2;
     private static final int ANSWER = 3;
 
     /* BlackBerry */
     public static final boolean blackberry = false;
 
     public FireMIDlet()
     {
         Log.showDebug = debug;
         // FindCardDirJ2ME.debug = true;
 
         display = Display.getDisplay(this);
 
         screen = FireScreen.getScreen(display);
         config.setScreen(screen);
 
         screen.setFullScreenMode(true);
         try {
             if (blackberry) {
                 FireScreen.setTheme(new FireTheme("file://res/blackberry.properties"));
             } else if (config.isBigScreen) {
                 FireScreen.setTheme(new FireTheme("file://hires.properties"));
             } else {
                 FireScreen.setTheme(new FireTheme("file://normal.properties"));
             }
         } catch (Exception e) {}
 
         screen.setFireListener(this);
         // net.rim.device.api.system.Application.getApplication().addKeyListener(this); /* BlackBerry */
 
 
         progressGauge = new ProgressGauge();
         progressHandler = (Progress)progressGauge;
 
         httpClient = new HttpClient(new FireConnector());
         browser = new Browser(httpClient);
         browser.setListener(this);
         CardPanel.browser = browser;
         StatsPanel.browser = browser;
 
         cmdOk = new Command(okText, Command.OK, 1); 
         cmdExit = new Command(exitText, Command.EXIT, 5);
         cmdLearnAhead = new Command("invisible", Command.OK, 1);
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
                 FireScreen.leftSoftKey = config.leftSoftKey;
             }
             if (config.rightSoftKey != 0) {
                 FireScreen.rightSoftKey = config.rightSoftKey;
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
         if (carddb.canLearnAhead()) {
             screen.showAlert(askLearnAheadText,
                              gr.fire.ui.Alert.TYPE_YESNO,
                              gr.fire.ui.Alert.USER_SELECTED_NO,
                              cmdLearnAhead, this);
         } else {
             screen.showAlert(doneText,
                              gr.fire.ui.Alert.TYPE_INFO,
                              gr.fire.ui.Alert.USER_SELECTED_OK,
                              cmdExit, this);
         }
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
         aboutPanel.autoPlay = config.autoPlay;
         int i = 0;
         while (i < 6) {
             aboutPanel.keys[i] = config.gradeKey[i];
             ++i;
         }
         aboutPanel.keys[i++] = config.statKey;
         aboutPanel.keys[i++] = config.skipKey;
         aboutPanel.keys[i++] = config.replayKey;
 
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
         pauseThinking();
         if (config.autoPlay) {
             queueAnswerSounds();
         }
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
             answerPlayed = false;
             if (config.autoPlay) {
                 queueQuestionSounds();
             }
             showQuestionScreen();
             startThinking();
         } else {
             showDone();
         }
     }
 
     public void updateFont(int fontsize)
     {
         Theme theme = FireScreen.getTheme();
         Font cur_font = theme.getFontProperty("xhtml.font");
         Font new_font = Font.getFont(cur_font.getFace(),
                                      cur_font.getStyle(),
                                      fontsize);
         theme.setFontProperty("xhtml.font", new_font);
         FireScreen.setTheme(theme);
     }
 
     protected void queueQuestionSounds()
     {
         if (curCard != null && player != null) {
             player.queue(curCard.getQuestionSounds());
         }
     }
 
     protected void queueAnswerSounds()
     {
         answerPlayed = true;
         if (curCard != null && player != null) {
             player.queue(curCard.getAnswerSounds());
         }
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
 
         } else if (cmd.equals(cmdLearnAhead)) {
             gr.fire.ui.Alert alert = (gr.fire.ui.Alert) c;
             switch (alert.getUserSelection()) {
             case gr.fire.ui.Alert.USER_SELECTED_YES:
                 carddb.learnAhead();
                 showNextQuestion();
                 break;
 
             case gr.fire.ui.Alert.USER_SELECTED_NO:
                 destroyApp(true);
                 break;
             }
             return;
         }
 
         if (current == ABOUT) {
             // Save the settings on both Ok and Exit
             AboutPanel aboutPanel;
             try {
                 aboutPanel = (AboutPanel)c;
             } catch (ClassCastException e) {
                 // Ok to a dialog box (e.g. no new cards to review)
                 return;
             }
 
             if (aboutPanel.dirty) {
                 config.cardpath = aboutPanel.cardpath;
                 config.fontSize = aboutPanel.fontSize;
                 if (aboutPanel.cardsToLoad > 0) {
                     config.cardsToLoad = aboutPanel.cardsToLoad;
                 }
                 config.showButtons = aboutPanel.touchScreen;
                 config.centerText = aboutPanel.centerText;
                 config.autoPlay = aboutPanel.autoPlay;
                 int i = 0;
                 while (i < 6) {
                     config.gradeKey[i] = aboutPanel.keys[i];
                     ++i;
                 }
                 config.statKey = aboutPanel.keys[i++];
                 config.skipKey = aboutPanel.keys[i++];
                 config.replayKey = aboutPanel.keys[i++];
 
                 config.leftSoftKey = FireScreen.leftSoftKey;
                 config.rightSoftKey = FireScreen.rightSoftKey;
 
                 config.save((Progress)progressGauge);
             }
         }
 
         if ((current == ABOUT) && (label.equals(okText))) {
 
             updateFont(config.fontSize);
 
             if (config.cardpath == null) {
                 showFatal(selectCarddir, false);
 
             } else {
                 loadCards();
                 player = new SoundPlayer(config.cardpath);
                 showNextQuestion();
                 checkExportTime();
             }
 
         } else if (label.equals(showText)) {
             showAnswerScreen();
 
         } else if (label.equals(exitText)) {
             saveCards();
             notifyDestroyed();
 
         } else if (label.equals("form")) {
             InputComponent src = (InputComponent) c;
 
             if ("Replay sounds".equals(src.getValue())) {
                if (!curCard.getOverlay()
                    && (current != ANSWER || answerPlayed))
                 {
                     queueQuestionSounds();
                 }
                 if (current == ANSWER) {
                     queueAnswerSounds();
                 }
             }
         }
     }
 
     public void commandAction(javax.microedition.lcdui.Command cmd, Displayable dis)
     {
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
 
             } else if (code == config.replayKey) {
                 queueQuestionSounds();
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
 
             } else if (code == config.replayKey) {
                if (!curCard.getOverlay() && answerPlayed)
                {
                     queueQuestionSounds();
                 }
                 queueAnswerSounds();
             }
         
         default:
             break;
         }
     }
 
     public void keyRepeated(int code, Component src) {  }
     public void keyPressed(int code, Component src) { }
 
     /* Blackberry specific */
     /* Interface: net.rim.device.api.system.KeyListener */
     public boolean keyChar(char key, int status, int time) {
         return false;
     }
 
     public boolean keyDown(int keycode, int time)
     {
         /* BlackBerry */
         /*
         int keypad = net.rim.device.api.ui.Keypad.key(keycode);
 
         if (keypad == net.rim.device.api.ui.Keypad.KEY_MENU) {
             screen.triggerLeftSoftKey();
             return true;
         } else if (keypad == net.rim.device.api.ui.Keypad.KEY_ESCAPE) {
             screen.triggerRightSoftKey();
             return true;
         }
         */
 
         return false;
     }
 
     public boolean keyRepeat(int keycode, int time) {
         return false;
     }
 
     public boolean keyStatus(int keycode, int time) {
         return false;
     }
 
     public boolean keyUp(int keycode, int time) {
         /* BlackBerry */
         /*
         int keypad = net.rim.device.api.ui.Keypad.key(keycode);
 
         if (keypad == net.rim.device.api.ui.Keypad.KEY_MENU) {
             return true;
         } else if (keypad == net.rim.device.api.ui.Keypad.KEY_ESCAPE) {
             return true;
         }
         */
         return false;
     }
 }
 
