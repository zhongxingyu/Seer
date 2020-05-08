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
 
 import java.io.ByteArrayInputStream;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.Font;
 
 import mnemogogo.mobile.hexcsv.Card;
 
 import gr.fire.browser.Browser;
 import gr.fire.browser.util.Page;
 import gr.fire.core.FireScreen;
 import gr.fire.core.KeyListener;
 import gr.fire.core.CommandListener;
 
 // for buttons:
 import gr.fire.core.BoxLayout;
 import gr.fire.core.Container;
 import gr.fire.core.GridLayout;
 import gr.fire.ui.InputComponent;
 
 public class CardPanel
     extends Panel
 {
     protected static Panel showButtons;
     protected static Panel gradeButtons;
     public static Browser browser;
 
     protected static final int BUTTONS_NONE = 0;
     protected static final int BUTTONS_SHOW = 1;
     protected static final int BUTTONS_GRADE = 2;
 
     protected Card card;
     protected boolean showQuestion;
     protected int buttonMode;
     protected Configuration config;
     protected Panel htmlSubPanel;
 
     protected static final String nocardloadedText
             = "Unexpected error: card not loaded.";
     protected static final String showAnswerText = "Show";
     protected static final String showStatsText = "Stats";
     protected static final String skipCardText = "Skip";
 
     protected static final String soundMarker = "<img src=\"res://sound.png\"/><br/>";
 
     public CardPanel(Card card,
                      boolean showQuestion,
                      String title,
                      Configuration config,
                      CommandListener cmdListen,
                      KeyListener keyListen,
                      Command cmdLeft, Command cmdRight, Command cmdButton)
     {
         super((Container)null,
               (config.showButtons?Panel.NO_SCROLLBAR:Panel.VERTICAL_SCROLLBAR),
               true,
               config);
 
         this.showQuestion = showQuestion;
         if (!config.showButtons) {
             buttonMode = BUTTONS_NONE;
         } else if (showQuestion) {
             buttonMode = BUTTONS_SHOW;
         } else {
             buttonMode = BUTTONS_GRADE;
         }
         this.card = card;
         this.config = config;
 
         if (showButtons == null) {
             showButtons = makeShowButtons(cmdListen, keyListen,
                                           cmdLeft, cmdRight, cmdButton);
         }
         if (gradeButtons == null) {
             gradeButtons = makeGradeButtons(cmdListen, keyListen,
                                             cmdLeft, cmdRight, cmdButton);
         }
 
         setCommandListener(cmdListen);
         setKeyListener(keyListen);
         setLeftSoftKeyCommand(cmdLeft);
         setRightSoftKeyCommand(cmdRight);
         setLabel(title);
 
         if (buttonMode == BUTTONS_NONE) {
             htmlSubPanel = null;
             setDragScroll(true);
         } else {
             htmlSubPanel = new Panel(null, Panel.VERTICAL_SCROLLBAR,
                                      false, config);
             htmlSubPanel.setCommandListener(cmdListen);
             htmlSubPanel.setDragScroll(true);
             htmlSubPanel.setKeyListener(keyListen);
             htmlSubPanel.setLeftSoftKeyCommand(cmdLeft);
             htmlSubPanel.setRightSoftKeyCommand(cmdRight);
         }
 
         makeDisplay();
     }
 
     protected void makeDisplay()
     {
         Page html = makePage(makeCardHtml(!showQuestion).toString());
 
         if (htmlSubPanel == null) {
             set(html.getPageContainer());
             return;
         } else {
             htmlSubPanel.set(html.getPageContainer());
         }
 
         // Shift to the constructor
         Container controls = new Container(new BoxLayout(BoxLayout.Y_AXIS));
         controls.add(htmlSubPanel);
 
         switch (buttonMode) {
         case BUTTONS_SHOW:
             controls.add(showButtons);
             break;
 
         case BUTTONS_GRADE:
             controls.add(gradeButtons);
             break;
         }
 
         set(controls);
         scrollPanel = htmlSubPanel;
     }
 
     private Page makePage(String contents)
     {
         try {
             ByteArrayInputStream in =
                 new ByteArrayInputStream(contents.getBytes("UTF-8"));
             Page page = browser.loadPage(in, "UTF-8");
             in.close();
 
             return page;
         } catch (Exception e) { }
 
         return null;
     }
 
     StringBuffer makeCardHtml(boolean includeAnswer)
     {
         StringBuffer html = new StringBuffer(
             "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<body>");
 
         String question = card.getQuestion();
         String answer = card.getAnswer();
 
         boolean qsounds = !(includeAnswer && card.getOverlay()) && card.hasQuestionSounds();
         boolean asounds = includeAnswer && card.hasAnswerSounds();
 
         if (config.centerText) {
             html.append("<center>");
         }
 
         if (question == null || answer == null) {
             html.append(nocardloadedText);
 
         } else if (includeAnswer) {
             if (!card.getOverlay()) {
                 if (!config.showButtons && !config.autoPlay && qsounds) {
                     html.append(soundMarker);
                 }
                 html.append(question);
                 html.append("<hr/>");
             }
 
             if (!config.showButtons && !config.autoPlay && asounds) {
                 html.append(soundMarker);
             }
             html.append(answer);
 
         } else {
             if (!config.showButtons && !config.autoPlay && qsounds) {
                 html.append(soundMarker);
             }
             html.append(question);
         }
 
         if (config.showButtons && (qsounds || asounds))
         {
             html.append("<br/><form>");
             html.append("<input type=\"submit\" value=\"Replay sounds\"/>");
             html.append("</form>");
         }
 
         if (config.centerText) {
             html.append("</center>");
         }
 
         html.append("</body>");
 
         return html;
     }
 
     // from Fire demo: SimpleCalc.java
     private Panel makeButtonRow(String symbols[],
                                 CommandListener cmdListen,
                                 KeyListener keyListen,
                                 Command cmdLeft, Command cmdRight,
                                 Command cmdButton)
     {
         InputComponent button;
 
         Font buttonFont;
         int buttonHeight;
         int rows = 1;
         int cols = symbols.length;
 
         if (config.isBigScreen) {
             buttonFont = Font.getFont(Font.FACE_SYSTEM,
                                       Font.STYLE_BOLD,
                                       Font.SIZE_LARGE);
             buttonHeight = buttonFont.getHeight() * 4;
             if (symbols.length >= 6) {
                 rows = 2;
                 cols = symbols.length / 2;
                 buttonHeight *= 2;
             }
         } else {
             buttonFont = Font.getFont(Font.FACE_SYSTEM,
                                       Font.STYLE_BOLD,
                                       Font.SIZE_MEDIUM);
             buttonHeight = buttonFont.getHeight() * 2;
         }
 
         Container pad = new Container(new GridLayout(rows, cols));
         
         for(int i = 0; i<symbols.length; ++i) {
             button = new InputComponent(InputComponent.BUTTON);
             button.setValue(symbols[i]); 
             button.setCommandListener(cmdListen);
             button.setKeyListener(keyListen);
             button.setCommand(cmdButton);
             button.setLeftSoftKeyCommand(cmdLeft);
             button.setRightSoftKeyCommand(cmdRight);
             button.setForegroundColor(
                 FireScreen.getTheme().getIntProperty("button.fg.color"));
             button.setBackgroundColor(
                 FireScreen.getTheme().getIntProperty("button.bg.color"));
             button.setFont(buttonFont);
             button.setLayout(FireScreen.CENTER | FireScreen.VCENTER);
             pad.add(button);
         }
         
         Panel padPane = new Panel(pad, Panel.NO_SCROLLBAR, false);              
         padPane.setShowBackground(true);
         padPane.setBackgroundColor(
             FireScreen.getTheme().getIntProperty("titlebar.bg.color"));
         padPane.setPrefSize(FireScreen.getScreen().getWidth(), buttonHeight);
 
         return padPane;
     }
 
     private Panel makeGradeButtons(
         CommandListener cmdListen, KeyListener keyListen,
         Command cmdLeft, Command cmdRight, Command cmdButton)
     {
         String buttons[] = {"0", "1", "2", "3", "4", "5"};
         return makeButtonRow(buttons, cmdListen, keyListen,
                              cmdLeft, cmdRight, cmdButton);
     }
 
     private Panel makeShowButtons(
         CommandListener cmdListen, KeyListener keyListen,
         Command cmdLeft, Command cmdRight, Command cmdButton)
     {
         String buttons[] = {skipCardText, showAnswerText, showStatsText};
         return makeButtonRow(buttons, cmdListen, keyListen,
                              cmdLeft, cmdRight, cmdButton);
     }
 
     public void screenSizeChanged(int newWidth, int newHeight)
     {
         super.screenSizeChanged(newWidth, newHeight);
 
         if (gradeButtons != null) {
             int[] size = gradeButtons.getPrefSize();
             if ((size != null) && (size[1] >= 0)) {
                 gradeButtons.setPrefSize(FireScreen.getScreen().getWidth(),
                                          size[1]);
             }
         }
 
         if (showButtons != null) {
             int[] size = showButtons.getPrefSize();
             if ((size != null) && (size[1] >= 0)) {
                 showButtons.setPrefSize(FireScreen.getScreen().getWidth(),
                                         size[1]);
             }
         }
 
         makeDisplay();
     }
 }
 
