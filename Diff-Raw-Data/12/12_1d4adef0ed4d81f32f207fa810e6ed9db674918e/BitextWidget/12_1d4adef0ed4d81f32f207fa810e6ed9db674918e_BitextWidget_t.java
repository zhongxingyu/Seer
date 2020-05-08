 /**
  * ********
  * Copyright © 2010-2012 Olanto Foundation Geneva
  *
  * This file is part of myCAT.
  *
  * myCAT is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Affero General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * myCAT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with myCAT. If not, see <http://www.gnu.org/licenses/>.
  *
  *********
  */
 package org.olanto.TranslationText.client;
 
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.google.gwt.event.dom.client.*;
 import com.google.gwt.i18n.client.HasDirection.Direction;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.*;
 import java.util.ArrayList;
 
 /**
  * panneau du concordancier
  */
 public class BitextWidget extends Composite {
 
     private VerticalPanel mainWidget = new VerticalPanel();
     private HorizontalPanel hS = new HorizontalPanel();
     private HorizontalPanel hT = new HorizontalPanel();
     private Grid textAreaGrid = new Grid(2, 3);
     private Button NextHitS = new Button(GuiMessageConst.TA_BTN_NXT);
     private Button PreviousHitS = new Button(GuiMessageConst.TA_BTN_PVS);
     private Button AlignS = new Button(GuiMessageConst.TA_BTN_ALGN);
     private Button schS = new Button(GuiMessageConst.TA_BTN_SRCH);
     private Button orgnS = new Button(GuiMessageConst.TA_BTN_OGN);
     private Button AlignT = new Button(GuiMessageConst.TA_BTN_ALGN);
     private Button schT = new Button(GuiMessageConst.TA_BTN_SRCH);
     private Button orgnT = new Button(GuiMessageConst.TA_BTN_OGN);
     public Button save = new Button(GuiMessageConst.TA_BTN_SAVE);
     private Button NextHitT = new Button(GuiMessageConst.TA_BTN_NXT);
     private Button PreviousHitT = new Button(GuiMessageConst.TA_BTN_PVS);
     public TextArea sourceTextArea = new TextArea();
     private TextArea targetTextArea = new TextArea();
     private Label msg;
     public int indexHS = 0;
     private int indexS = 0;
     private int indexT = 0;
     // indexes de la dernière occurence du mot recherchÃ© dans le contenu du codument
     private int curIndS = 0;
     private int curIndT = 0;
     private TranslateServiceAsync rpcS;
     // Matrices (nombre de lignes, position du top, correction, position en pixel)
     private int[][] resultS;
     private int[][] resultT;
     private int[][] PositionsS;
     private int[][] PositionsT;
     public ArrayList<String> words;
     private GwtIntMap Map;
     private GwtAlignBiText Align;
     private PopupPanel pp = new PopupPanel(false);
     private PopupPanel pSch = new PopupPanel(false);
     private TextBox SchArea = new TextBox();
     private Button SchBtn = new Button(GuiMessageConst.TA_BTN_SEARCH);
     private Button CclBtn = new Button(GuiMessageConst.TA_BTN_CCL);
     private String contentS = "";
     private String contentT = "";
     private int height = 0;
     private int height1 = 0;
     private int pixS = GuiConstant.TA_LINE_HEIGHT;
     private int pposS = 0;
     private int pposT = 0;
     private boolean SchS = true;
     public int queryLength = 0;
    public String search = "", langsrc = "", langtgt = "";
     private static String features = "menubar=no, location=no, resizable=yes, scrollbars=yes, status=no";
     private Utility Utility = new Utility();
     private static final int H_Unit = 30;
     private static final int CHAR_W = GuiConstant.CHARACTER_WIDTH;
     private static final int PP_H_MIN = GuiConstant.PP_H_MIN;
     private static final int PP_H_MAX = GuiConstant.PP_H_MAX;
     private int pos = 0;
     private float magicS, magicT;
     private boolean remSpace = false;
 
     public BitextWidget(Label msg) {
         rpcS = RpcInit.initRpc();
         this.msg = msg;
         SetHeader();
     }
 
     private void SetHeader() {
         initWidget(mainWidget);
         mainWidget.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
         mainWidget.add(textAreaGrid);
         textAreaGrid.setStyleName("headerNav");
         hS.add(PreviousHitS);
         hS.add(NextHitS);
         hS.add(AlignS);
         hS.add(schS);
 
         hT.add(PreviousHitT);
         hT.add(NextHitT);
         hT.add(AlignT);
         hT.add(schT);
 
         if (GuiConstant.ORIGINAL_ON) {
             hS.add(orgnS);
             hT.add(orgnT);
         }
         if (GuiConstant.SAVE_ON) {
             hS.add(save);
         }
 
         hS.setVisible(true);
         hT.setVisible(true);
 
         textAreaGrid.setWidget(0, 0, hS);
         textAreaGrid.setWidget(0, 2, hT);
         textAreaGrid.setWidget(1, 0, sourceTextArea);
         textAreaGrid.setWidget(1, 2, targetTextArea);
 
         setbuttonstyle(NextHitS, NextHitS.getText().length() * CHAR_W, H_Unit);
         setbuttonstyle(PreviousHitS, PreviousHitS.getText().length() * CHAR_W, H_Unit);
         setbuttonstyle(AlignS, AlignS.getText().length() * CHAR_W, H_Unit);
         setbuttonstyle(schS, schS.getText().length() * CHAR_W, H_Unit);
         setbuttonstyle(orgnS, orgnS.getText().length() * CHAR_W, H_Unit);
         setbuttonstyle(save, save.getText().length() * CHAR_W, H_Unit);
 
         setbuttonstyle(NextHitT, NextHitT.getText().length() * CHAR_W, H_Unit);
         setbuttonstyle(PreviousHitT, PreviousHitT.getText().length() * CHAR_W, H_Unit);
         setbuttonstyle(AlignT, AlignT.getText().length() * CHAR_W, H_Unit);
         setbuttonstyle(schT, schT.getText().length() * CHAR_W, H_Unit);
         setbuttonstyle(orgnT, orgnT.getText().length() * CHAR_W, H_Unit);
 
         sourceTextArea.setCursorPos(0);
         sourceTextArea.setVisible(true);
         sourceTextArea.setEnabled(true);
         sourceTextArea.setReadOnly(true);
         sourceTextArea.setStyleName("gwt-Textarea");
         sourceTextArea.getElement().setAttribute("spellCheck", "false");
         sourceTextArea.setCharacterWidth(MainEntryPoint.IMeasures.TA_TEXTAREA_WIDTH);
         sourceTextArea.setVisibleLines(MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT);
         sourceTextArea.setHeight("" + pixS * MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT + "px");
 
         targetTextArea.setCursorPos(0);
         targetTextArea.setVisible(true);
         targetTextArea.setEnabled(true);
         targetTextArea.setReadOnly(true);
         targetTextArea.setStyleName("gwt-Textarea");
         targetTextArea.getElement().setAttribute("spellCheck", "false");
         targetTextArea.setCharacterWidth(MainEntryPoint.IMeasures.TA_TEXTAREA_WIDTH);
         targetTextArea.setVisibleLines(MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT);
         targetTextArea.setHeight("" + pixS * MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT + "px");
 
         pp.setAnimationEnabled(true);
         pp.setAutoHideEnabled(true);
         pp.setStyleName("focusPanel");
         pp.add(new HTML("&nbsp;"));
         ClickHandler hidepanT = new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 pp.hide();
                 sourceTextArea.setFocus(true);
             }
         };
         pp.sinkEvents(Event.ONCLICK);
         pp.addHandler(hidepanT, ClickEvent.getType());
 
         pSch.setAnimationEnabled(true);
         pSch.setAutoHideEnabled(true);
         pSch.setStyleName("searchPanel");
         VerticalPanel vpSch = new VerticalPanel();
         HorizontalPanel hpSch = new HorizontalPanel();
         vpSch.setWidth("400px");
         hpSch.setWidth("400px");
         vpSch.add(SchArea);
         vpSch.setCellHorizontalAlignment(SchArea, HorizontalPanel.ALIGN_CENTER);
         vpSch.add(hpSch);
         hpSch.add(SchBtn);
         hpSch.setCellHorizontalAlignment(SchBtn, HorizontalPanel.ALIGN_LEFT);
         hpSch.add(CclBtn);
         hpSch.setCellHorizontalAlignment(SchBtn, HorizontalPanel.ALIGN_RIGHT);
         pSch.add(vpSch);
 
         setbuttonstyle(SchBtn, SchBtn.getText().length() * CHAR_W, H_Unit);
         setbuttonstyle(CclBtn, CclBtn.getText().length() * CHAR_W, H_Unit);
         SchArea.setWidth("400px");
         SchArea.setStyleName("gwt-srch-text");
     }
 
     public void DrawEffects() {
         if (GuiConstant.ORIGINAL_ON) {
             hS.add(orgnS);
             hT.add(orgnT);
         }
         if (GuiConstant.SAVE_ON) {
             hS.add(save);
         }
 
         hS.setVisible(true);
         hT.setVisible(true);
     }
 
     public void setbuttonstyle(Button b, int w, int h) {
         b.setStyleName("x-btn-click");
         b.setPixelSize(w, h);
     }
 
     /**
      * on return, draw the data to screen
      */
     public void testserver(String greeting) {
 
         rpcS.myMethod(greeting, new AsyncCallback<String>() {
             @Override
             public void onFailure(Throwable caught) {
                 Window.alert(GuiMessageConst.MSG_1);
             }
 
             @Override
             public void onSuccess(String result) {
                 Window.alert(result);
             }
         });
     }
 
     public void reset() {
         pp.hide();
         pos = 0;
         curIndS = 0;
         curIndT = 0;
         sourceTextArea.setText("");
         targetTextArea.setText("");
         PositionsS = null;
         PositionsT = null;
         words = null; // to reset the search tokens
         PreviousHitS.removeAllListeners();
         NextHitS.removeAllListeners();
         AlignS.removeAllListeners();
         schS.removeAllListeners();
         orgnS.removeAllListeners();
         save.removeAllListeners();
 
         PreviousHitT.removeAllListeners();
         NextHitT.removeAllListeners();
         AlignT.removeAllListeners();
         schT.removeAllListeners();
         orgnT.removeAllListeners();
 
         CclBtn.removeAllListeners();
         SchBtn.removeAllListeners();
 
         sourceTextArea.setCharacterWidth(MainEntryPoint.IMeasures.TA_TEXTAREA_WIDTH);
         sourceTextArea.setVisibleLines(MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT);
         targetTextArea.setCharacterWidth(MainEntryPoint.IMeasures.TA_TEXTAREA_WIDTH);
         targetTextArea.setVisibleLines(MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT);
 
     }
 
     public void setVariables() {
         sourceTextArea.setCharacterWidth(MainEntryPoint.IMeasures.TA_TEXTAREA_WIDTH);
         sourceTextArea.setVisibleLines(MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT);
         targetTextArea.setCharacterWidth(MainEntryPoint.IMeasures.TA_TEXTAREA_WIDTH);
         targetTextArea.setVisibleLines(MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT);
         targetTextArea.setEnabled(true);
         PreviousHitT.enable();
         NextHitT.enable();
         AlignT.enable();
         schT.enable();
         orgnT.enable();
         AlignS.enable();
 
         sourceTextArea.setText(Align.source.content);
         targetTextArea.setText(Align.target.content);
 
         // Matrice (nombre de lignes, position du top, correction, position en pixel)
         resultS = Align.source.positions;
         resultT = Align.target.positions;
         if (GuiConstant.EXACT_FLG) {
             contentS = Align.source.content;
             contentT = Align.target.content;
         } else {
             contentS = Align.source.content.toLowerCase();
             contentT = Align.target.content.toLowerCase();
         }
 // check for the case of having the overall in case sensitive and the inside without case sensitive
         height1 = targetTextArea.getElement().getScrollHeight();
         height = sourceTextArea.getElement().getScrollHeight();
         pposS = sourceTextArea.getOffsetWidth() - pixS;
         pposT = targetTextArea.getOffsetWidth() - pixS;
 
         float scrollines = (float) ((float) height) / pixS;
         int totlinesS = (resultS[resultS.length - 1][3] + resultS[resultS.length - 1][0]);
         magicS = (float) ((float) (scrollines - totlinesS) / scrollines) + 1f;
 
         float scrollines1 = (float) ((float) height1) / pixS;
         int totlinesT = (resultT[resultT.length - 1][3] + resultT[resultT.length - 1][0]);
         magicT = (float) ((float) (scrollines1 - totlinesT) / scrollines1) + 1f;
     }
 
     public void showpanel(boolean source, int hight, int index) {
         if (hight < PP_H_MIN) {
             hight = PP_H_MIN;
         }
         if (hight > PP_H_MAX) {
             hight = PP_H_MAX;
         }
         int lineNum, restOfLines;
 
         if (source) {
             restOfLines = (resultS[resultS.length - 1][3] - resultS[index][3]);
             if (resultS[index][3] < (sourceTextArea.getVisibleLines() / 2)) {
                 lineNum = resultS[index][3];
             } else {
                 if (restOfLines > (sourceTextArea.getVisibleLines() / 2)) {
                     lineNum = (sourceTextArea.getVisibleLines() / 2 - 2);
                 } else {
                     if (restOfLines > 0) {
                         lineNum = sourceTextArea.getVisibleLines() - restOfLines - 2;
                     } else {
                         lineNum = sourceTextArea.getVisibleLines() - (sourceTextArea.getVisibleLines() / 4) - 2;
                     }
                 }
             }
 
             if (langsrc.contains("AR")) {
                 pp.setPopupPosition(sourceTextArea.getAbsoluteLeft() + pixS, (int) (lineNum * pixS) + sourceTextArea.getAbsoluteTop());
             } else {
                 pp.setPopupPosition(sourceTextArea.getAbsoluteLeft() - 2, (int) (lineNum * pixS) + sourceTextArea.getAbsoluteTop());
             }
             if (((lineNum + hight) * pixS) < sourceTextArea.getOffsetHeight()) {
                 pp.setPixelSize(pposS, (int) pixS * hight);
             } else {
                 pp.setPixelSize(pposS, sourceTextArea.getOffsetHeight() - (int) (pixS * lineNum));
             }
             pp.show();
         } else {
             restOfLines = (resultT[resultT.length - 1][3] - resultT[index][3]);
             if (resultT[index][3] < (targetTextArea.getVisibleLines() / 2)) {
                 lineNum = resultT[index][3];
             } else {
                 if (restOfLines > (targetTextArea.getVisibleLines() / 2)) {
                     lineNum = (targetTextArea.getVisibleLines() / 2 - 2);
                 } else {
                     if (restOfLines > 0) {
                         lineNum = targetTextArea.getVisibleLines() - restOfLines - 2;
                     } else {
                         lineNum = targetTextArea.getVisibleLines() - (targetTextArea.getVisibleLines() / 4) - 2;
                     }
                 }
             }
 //            Window.alert("restOfLines = " + restOfLines
 //                    + "\nlineNum = " + lineNum
 //                    + "\nsourceTextArea.getAbsoluteTop() = " + targetTextArea.getAbsoluteTop()
 //                    + "\nPixT = " + pixS
 //                    + "\n(lineNum * pixS) + targetTextArea.getAbsoluteTop() = " + ((lineNum * pixS) + targetTextArea.getAbsoluteTop()));
             if (langtgt.contains("AR")) {
                 pp.setPopupPosition(targetTextArea.getAbsoluteLeft() + pixS, (int) (lineNum * pixS) + targetTextArea.getAbsoluteTop());
             } else {
                 pp.setPopupPosition(targetTextArea.getAbsoluteLeft() - 2, (int) (lineNum * pixS) + targetTextArea.getAbsoluteTop());
             }
 
             if (((lineNum + hight) * pixS) < targetTextArea.getOffsetHeight()) {
                 pp.setPixelSize(pposT, (int) pixS * hight);
             } else {
                 pp.setPixelSize(pposT, targetTextArea.getOffsetHeight() - (int) (pixS * lineNum));
             }
             pp.show();
         }
     }
 
     public void setNetScapePos(int idxS, int idxT, int h) {
         int lin = resultS[idxS][3] - h + resultS[idxS][0] / 2;
         int lin1 = resultT[idxT][3] - h + resultT[idxT][0] / 2;
         lin = (lin > 0) ? lin : 0;
         lin1 = (lin1 > 0) ? lin1 : 0;
 
         float frtop1 = lin1 * pixS * magicT;
         float frtop = lin * pixS * magicS;
         int posf = (frtop > height) ? height : (int) frtop;
         int posf1 = (frtop1 > height1) ? height1 : (int) frtop1;
         sourceTextArea.getElement().setScrollTop(posf);
         targetTextArea.getElement().setScrollTop(posf1);
         sourceTextArea.setFocus(true);
         if (GuiConstant.DEBUG_ON) {
             Window.alert("-------- Source Area--------"
                     + "\nNumber of lines to remove (h/2)= " + h
                     + "\nNumber of previous lines = " + resultS[idxS][3]
                     + "\nLineNum = " + idxS
                     + "\nPixel height practical = " + height
                     + "\nLine numbers practical = " + (height / pixS)
                     + "\nLine numbers theoretical = " + (resultS[resultS.length - 1][3] + resultS[resultS.length - 1][0])
                     + "\nLine numbers in current after wrap (theoretical) = " + resultS[idxS][0]
                     + "\nPixel scroll " + frtop
                     + "\n-------- Target Area--------"
                     + "\nNumber of previous lines = " + resultT[idxT][3]
                     + "\nLineNum = " + idxT
                     + "\nLine numbers in current after wrap (theoretical) = " + resultT[idxT][0]
                     + "\nPixel height practical = " + height1
                     + "\nPixel scroll " + frtop1);
         }
     }
 
     public void setNetScapePosT(int idxS, int idxT, int h) {
         int lin = resultS[idxS][3] - h + resultS[idxS][0] / 2;
         int lin1 = resultT[idxT][3] - h + resultT[idxT][0] / 2;
         lin = (lin > 0) ? lin : 0;
         lin1 = (lin1 > 0) ? lin1 : 0;
 
         float frtop1 = lin1 * pixS * magicT;
         float frtop = lin * pixS * magicS;
         int posf = (frtop > height) ? height : (int) frtop;
         int posf1 = (frtop1 > height1) ? height1 : (int) frtop1;
         targetTextArea.getElement().setScrollTop(posf1);
         sourceTextArea.getElement().setScrollTop(posf);
         targetTextArea.setFocus(true);
     }
 
     public void nextHitS() {
         pp.hide();
         if ((PositionsS.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsS.length);
         } else if (curIndS == (PositionsS.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         }
 
         if (curIndS < PositionsS.length) {
             indexS = PositionsS[curIndS][0];
             indexT = Map.from[indexS];
             showpanel(false, resultT[indexT][0] + 1, indexT);
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 pos = PositionsS[curIndS][1] + resultS[indexS][1] + indexS;
                 int idx = resultS[indexS][2] + indexS;
                 int idxt = resultT[indexT][2] + indexT;
                 sourceTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(idxt);
                 sourceTextArea.setCursorPos(idx);
             } else {
                 setNetScapePos(indexS, indexT, (sourceTextArea.getVisibleLines() / 2));
                 pos = PositionsS[curIndS][1] + resultS[indexS][1];
             }
             sourceTextArea.setSelectionRange(pos, PositionsS[curIndS][2]);
         }
         sourceTextArea.setFocus(true);
     }
 
     public void previousHitS() {
         pp.hide();
         if ((PositionsS.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == (PositionsS.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsS.length);
         }
         if (curIndS >= 0) {
             indexS = PositionsS[curIndS][0];
             indexT = Map.from[indexS];
             showpanel(false, resultT[indexT][0] + 1, indexT);
 
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 pos = PositionsS[curIndS][1] + resultS[indexS][1] + indexS;
                 int idx = resultS[indexS][2] + indexS;
                 int idxt = resultT[indexT][2] + indexT;
                 sourceTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(idxt);
                 sourceTextArea.setCursorPos(idx);
             } else {
                 setNetScapePos(indexS, indexT, (sourceTextArea.getVisibleLines() / 2));
                 pos = PositionsS[curIndS][1] + resultS[indexS][1];
             }
             sourceTextArea.setSelectionRange(pos, PositionsS[curIndS][2]);
         }
         sourceTextArea.setFocus(true);
     }
 
     public void nextHitT() {
 
         pp.hide();
         if (curIndT == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsT.length);
         }
         if (curIndT == (PositionsT.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsT.length);
         }
         if ((PositionsT.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsT.length);
         }
 
         if (curIndT < PositionsT.length) {
             indexT = PositionsT[curIndT][0];
             indexS = Map.to[indexT];
             showpanel(true, resultS[indexS][0] + 1, indexS);
 
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 pos = PositionsT[curIndT][1] + resultT[indexT][1] + indexT;
                 int idx = resultS[indexS][2] + indexS;
                 int idxt = resultT[indexT][2] + indexT;
                 sourceTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(0);
                 sourceTextArea.setCursorPos(idx);
                 targetTextArea.setCursorPos(idxt);
             } else {
                 setNetScapePosT(indexS, indexT, (targetTextArea.getVisibleLines() / 2));
                 pos = PositionsT[curIndT][1] + resultT[indexT][1];
             }
             targetTextArea.setSelectionRange(pos, PositionsT[curIndT][2]);
         }
         targetTextArea.setFocus(true);
     }
 
     public void previousHitT() {
         pp.hide();
         if ((PositionsS.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndT == (PositionsS.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndT == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsS.length);
         }
 
         if (curIndT >= 0) {
             indexT = PositionsS[curIndT][0];
             indexS = Map.to[indexT];
             showpanel(true, resultS[indexS][0] + 1, indexS);
 
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 pos = PositionsS[curIndT][1] + resultT[indexT][1] + indexT;
                 int idx = resultS[indexS][2] + indexS;
                 int idxt = resultT[indexT][2] + indexT;
                 sourceTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(0);
                 sourceTextArea.setCursorPos(idx);
                 targetTextArea.setCursorPos(idxt);
             } else {
                 setNetScapePosT(indexS, indexT, (targetTextArea.getVisibleLines() / 2));
                 pos = PositionsS[curIndT][1] + resultT[indexT][1];
             }
             targetTextArea.setSelectionRange(pos, PositionsS[curIndT][2]);
         }
         targetTextArea.setFocus(true);
     }
 
     public void ClearHitsEvents() {
         curIndS = 0;
         curIndT = 0;
         PositionsS = null;
         PreviousHitS.removeAllListeners();
         NextHitS.removeAllListeners();
         PreviousHitT.removeAllListeners();
         NextHitT.removeAllListeners();
     }
 
     public void AddHitsEventsS() {
 
         // Handler of the going to the next line in the source text
         NextHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
 
                 if (curIndS < PositionsS.length - 1) {
                     curIndS++;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     nextHitS();
                 } else {
                     nextHitS();
                 }
             }
         });
 
         // Handler of the going to the previous line in the source text
         PreviousHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 if (curIndS > 0) {
                     curIndS--;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     previousHitS();
                 } else {
                     previousHitS();
                 }
             }
         });
     }
 
     public void AddHitsEventsSAO() {
 
         // Handler of the going to the next line in the source text
         NextHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
 
                 if (curIndS < PositionsS.length - 1) {
                     curIndS++;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     nextHitS();
                 } else {
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     nextHitS();
                 }
             }
         });
 
         // Handler of the going to the previous line in the source text
         PreviousHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 if (curIndS > 0) {
                     curIndS--;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     previousHitS();
                 } else {
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     previousHitS();
                 }
             }
         });
     }
 
     public void AddHitsEventsT() {
 
         // Handler of the going to the next line in the source text
         NextHitT.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
 
                 if (curIndT < PositionsT.length - 1) {
                     curIndT++;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndT) + " / " + PositionsT.length);
                     nextHitT();
                 } else {
                     nextHitT();
                 }
             }
         });
 
         // Handler of the going to the previous line in the source text
         PreviousHitT.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 if (curIndT > 0) {
                     curIndT--;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndT) + " / " + PositionsT.length);
                     previousHitT();
                 } else {
                     previousHitT();
                 }
             }
         });
     }
 
     public void AddHitsEventsTAO() {
 
         // Handler of the going to the next line in the source text
         NextHitT.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
 
                 if (curIndT < PositionsT.length - 1) {
                     curIndT++;
                     setMessage("info", words.get(PositionsT[curIndT][3]) + " : " + GuiMessageConst.MSG_36 + PositionsT[curIndT][4] + " / " + PositionsT[curIndT][5] + "  --- (/ " + PositionsT.length + ")");
                     nextHitT();
                 } else {
                     setMessage("info", words.get(PositionsT[curIndT][3]) + " : " + GuiMessageConst.MSG_36 + PositionsT[curIndT][4] + " / " + PositionsT[curIndT][5] + "  --- (/ " + PositionsT.length + ")");
                     nextHitT();
                 }
             }
         });
 
         // Handler of the going to the previous line in the source text
         PreviousHitT.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 if (curIndT > 0) {
                     curIndT--;
                     setMessage("info", words.get(PositionsT[curIndT][3]) + " : " + GuiMessageConst.MSG_36 + PositionsT[curIndT][4] + " / " + PositionsT[curIndT][5] + "  --- (/ " + PositionsT.length + ")");
                     previousHitT();
                 } else {
                     setMessage("info", words.get(PositionsT[curIndT][3]) + " : " + GuiMessageConst.MSG_36 + PositionsT[curIndT][4] + " / " + PositionsT[curIndT][5] + "  --- (/ " + PositionsT.length + ")");
 
                     previousHitT();
                 }
             }
         });
     }
 
     public void AddOtherEvents() {
 
         // Handler of the going to align the line in the source text
         AlignS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 pp.hide();
                 int posi = sourceTextArea.getCursorPos();
                 int len = sourceTextArea.getSelectedText().length();
                 indexS = Utility.getInd(posi, resultS);
 //                Window.alert("Got index source: " + indexS);
                 indexT = Map.from[indexS];
 //                Window.alert("Got index target: " + indexT);
 
                 showpanel(false, resultT[indexT][0] + 1, indexT);
 
                 if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                     int idx = resultS[indexS][2];
                     int idxt = resultT[indexT][2];
                     sourceTextArea.setCursorPos(0);
                     targetTextArea.setCursorPos(0);
                     sourceTextArea.setCursorPos(idx);
                     targetTextArea.setCursorPos(idxt);
                 } else {
                     setNetScapePos(indexS, indexT, (sourceTextArea.getVisibleLines() / 2));
                 }
                 if (len > 1) {
                     sourceTextArea.setSelectionRange(posi, len);
                 } else {
                     sourceTextArea.setSelectionRange(posi, 1);
                 }
                 sourceTextArea.setFocus(true);
             }
         });
 
         // Handler of the going to align the line in the target text
         AlignT.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 pp.hide();
                 int posi = targetTextArea.getCursorPos();
                 int len = targetTextArea.getSelectedText().length();
                 indexT = Utility.getInd(posi, resultT);
                 indexS = Map.to[indexT];
                 showpanel(true, resultS[indexS][0] + 1, indexS);
 
                 if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                     int idx = resultS[indexS][2];
                     int idxt = resultT[indexT][2];
                     sourceTextArea.setCursorPos(0);
                     targetTextArea.setCursorPos(0);
                     sourceTextArea.setCursorPos(idx);
                     targetTextArea.setCursorPos(idxt);
                 } else {
                     setNetScapePosT(indexS, indexT, (targetTextArea.getVisibleLines() / 2));
                 }
                 if (len > 1) {
                     targetTextArea.setSelectionRange(posi, len);
                 } else {
                     targetTextArea.setSelectionRange(posi, 1);
                 }
                 targetTextArea.setFocus(true);
             }
         });
 
         schS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 setMessage("info", GuiMessageConst.MSG_7);
                 SchS = true;
                 SchArea.setText("");
                 pSch.setPopupPosition(sourceTextArea.getAbsoluteLeft() + sourceTextArea.getOffsetWidth() / 8, sourceTextArea.getAbsoluteTop());
                 pSch.show();
                 SchArea.setFocus(true);
                 SchBtn.removeAllListeners();
                 SchBtn.addListener(Events.OnClick, new Listener<BaseEvent>() {
                     @Override
                     public void handleEvent(BaseEvent be) {
                         ClearHitsEvents();
                         words = null;
 
                         search = SchArea.getText().trim();
                         queryLength = search.length();
                         if ((search.contains(" AND "))
                                 || (search.contains(" OR "))) {
                             words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             getPositionsSAO(resultS, contentS, words, queryLength);
                         } else if (search.contains(" NEAR ")) {
                             words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             if (GuiConstant.TA_HILITE_OVER_CR) {
                                 getPositionsNearSCR(contentS, words, queryLength);
                             } else {
                                 getPositionsNearS(resultS, contentS, words, queryLength);
                             }
                         } else if ((search.contains("*"))) {
                             rpcS.getExpandTerms(search, new AsyncCallback<String[]>() {
                                 @Override
                                 public void onFailure(Throwable caught) {
                                     setMessage("error", GuiMessageConst.MSG_3);
                                 }
 
                                 @Override
                                 public void onSuccess(String[] result) {
                                     words = Utility.getWildCharQueryWords(result, MainEntryPoint.stopWords);
                                     getPositionsSAO(resultS, contentS, words, queryLength);
                                 }
                             });
                         } else {
                             if (search.startsWith("\"")) {
                                 words = Utility.getexactWords(search);
                             } else {
                                 words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             }
                             if (GuiConstant.TA_HILITE_OVER_CR) {
                                 getPositionsSCR(contentS, words, queryLength);
                             } else {
                                 getPositionsS(resultS, contentS, words, queryLength);
                             }
                         }
                     }
                 });
             }
         });
         save.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 setMessage("info", GuiMessageConst.MSG_35);
                 MyCatDownload.downloadZipFromServlet(Align.source.uri, msg);
             }
         });
 
         orgnS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 rpcS.getOriginalUrl(Align.source.uri, new AsyncCallback<String>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         setMessage("error", GuiMessageConst.MSG_4);
                     }
 
                     @Override
                     public void onSuccess(String result) {
 //                        Window.alert("request : " + result);
                         Window.open(result, "Original", features);
                        }
                 });
             }
         });
 
         orgnT.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 rpcS.getOriginalUrl(Align.target.uri, new AsyncCallback<String>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         setMessage("error", GuiMessageConst.MSG_5);
                     }
 
                     @Override
                     public void onSuccess(String result) {
 //                        Window.alert("request : " + result);
                         Window.open(result, "Original", features);
                     }
                 });
             }
         });
 
         CclBtn.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 pSch.hide();
             }
         });
 
         schT.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 setMessage("info", GuiMessageConst.MSG_6);
                 SchS = false;
                 SchArea.setText("");
                 pSch.setPopupPosition(targetTextArea.getAbsoluteLeft() + targetTextArea.getOffsetWidth() / 8, targetTextArea.getAbsoluteTop());
                 pSch.show();
                 SchArea.setFocus(true);
                 SchBtn.removeAllListeners();
                 SchBtn.addListener(Events.OnClick, new Listener<BaseEvent>() {
                     @Override
                     public void handleEvent(BaseEvent be) {
                         ClearHitsEvents();
                         words = null;
 
                         search = SchArea.getText().trim();
                         queryLength = search.length();
                         if ((search.contains(" AND "))
                                 || (search.contains(" OR "))) {
                             words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             getPositionsTAO(resultT, contentT, words, queryLength);
                         } else if (search.contains(" NEAR ")) {
                             words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             if (GuiConstant.TA_HILITE_OVER_CR) {
                                 getPositionsNearTCR(contentT, words, queryLength);
                             } else {
                                 getPositionsNearT(resultT, contentT, words, queryLength);
                             }
                         } else if ((search.contains("*"))) {
                             rpcS.getExpandTerms(search, new AsyncCallback<String[]>() {
                                 @Override
                                 public void onFailure(Throwable caught) {
                                     setMessage("error", GuiMessageConst.MSG_3);
                                 }
 
                                 @Override
                                 public void onSuccess(String[] result) {
                                     words = Utility.getWildCharQueryWords(result, MainEntryPoint.stopWords);
                                     getPositionsTAO(resultT, contentT, words, queryLength);
                                 }
                             });
                         } else {
                             if (search.startsWith("\"")) {
                                 words = Utility.getexactWords(search);
                             } else {
                                 words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             }
                             if (GuiConstant.TA_HILITE_OVER_CR) {
                                 getPositionsTCR(contentT, words, queryLength);
                             } else {
                                 getPositionsT(resultT, contentT, words, queryLength);
                             }
                         }
                     }
                 });
             }
         });
 
         SchArea.addKeyPressHandler(new KeyPressHandler() {
             @Override
             public void onKeyPress(KeyPressEvent event) {
 
                 if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                     if (SchS) {
                         ClearHitsEvents();
                         words = null;
 
                         search = SchArea.getText().trim();
                         queryLength = search.length();
                         if ((search.contains(" AND "))
                                 || (search.contains(" OR "))) {
                             words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             getPositionsSAO(resultS, contentS, words, queryLength);
                         } else if (search.contains(" NEAR ")) {
                             words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             if (GuiConstant.TA_HILITE_OVER_CR) {
                                 getPositionsNearSCR(contentS, words, queryLength);
                             } else {
                                 getPositionsNearS(resultS, contentS, words, queryLength);
                             }
                         } else if ((search.contains("*"))) {
                             rpcS.getExpandTerms(search, new AsyncCallback<String[]>() {
                                 @Override
                                 public void onFailure(Throwable caught) {
                                     setMessage("error", GuiMessageConst.MSG_3);
                                 }
 
                                 @Override
                                 public void onSuccess(String[] result) {
                                     words = Utility.getWildCharQueryWords(result, MainEntryPoint.stopWords);
                                     getPositionsSAO(resultS, contentS, words, queryLength);
                                 }
                             });
                         } else {
                             if (search.startsWith("\"")) {
                                 words = Utility.getexactWords(search);
                             } else {
                                 words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             }
                             if (GuiConstant.TA_HILITE_OVER_CR) {
                                 getPositionsSCR(contentS, words, queryLength);
                             } else {
                                 getPositionsS(resultS, contentS, words, queryLength);
                             }
                         }
                     } else {
                         ClearHitsEvents();
                         words = null;
 
                         search = SchArea.getText().trim();
                         queryLength = search.length();
                         if ((search.contains(" AND "))
                                 || (search.contains(" OR "))) {
                             words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             getPositionsTAO(resultT, contentT, words, queryLength);
                         } else if (search.contains(" NEAR ")) {
                             words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             if (GuiConstant.TA_HILITE_OVER_CR) {
                                 getPositionsNearTCR(contentT, words, queryLength);
                             } else {
                                 getPositionsNearT(resultT, contentT, words, queryLength);
                             }
                         } else if ((search.contains("*"))) {
                             rpcS.getExpandTerms(search, new AsyncCallback<String[]>() {
                                 @Override
                                 public void onFailure(Throwable caught) {
                                     setMessage("error", GuiMessageConst.MSG_3);
                                 }
 
                                 @Override
                                 public void onSuccess(String[] result) {
                                     words = Utility.getWildCharQueryWords(result, MainEntryPoint.stopWords);
                                     getPositionsTAO(resultT, contentT, words, queryLength);
                                 }
                             });
                         } else {
                             if (search.startsWith("\"")) {
                                 words = Utility.getexactWords(search);
                             } else {
                                 words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             }
                             if (GuiConstant.TA_HILITE_OVER_CR) {
                                 getPositionsTCR(contentT, words, queryLength);
                             } else {
                                 getPositionsT(resultT, contentT, words, queryLength);
                             }
                         }
                     }
                 }
             }
         });
     }
 
     public void setVariablesMono() {
 
         sourceTextArea.setCharacterWidth(MainEntryPoint.IMeasures.TA_TEXTAREA_WIDTH);
         sourceTextArea.setVisibleLines(MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT);
 
         sourceTextArea.setText(Align.source.content);
 
         targetTextArea.setEnabled(false);
         PreviousHitT.disable();
         NextHitT.disable();
         AlignT.disable();
         schT.disable();
         orgnT.disable();
         AlignS.disable();
 
         // Matrice (nombre de lignes, position du top, correction, position en pixel)
         resultS = Align.source.positions;
         if (GuiConstant.EXACT_FLG) {
             contentS = Align.source.content;
         } else {
             contentS = Align.source.content.toLowerCase();
         }
 
         height = sourceTextArea.getElement().getScrollHeight();
         pposS = sourceTextArea.getOffsetWidth() - (int) pixS;
 
         float scrollines = (float) ((float) height) / pixS;
         int totlinesS = (resultS[resultS.length - 1][3] + resultS[resultS.length - 1][0]);
         magicS = (float) ((float) (scrollines - totlinesS) / scrollines) + 1f;
     }
 
     public void setNetScapePosMono(int idxS, int h) {
         int lin = resultS[idxS][3] - h + resultS[idxS][0] / 2;
         float frtop = lin * pixS;
         int posf = (frtop > height) ? height : (int) frtop;
         sourceTextArea.setFocus(true);
         sourceTextArea.getElement().setScrollTop(posf);
     }
 
     public void nextHitMono() {
         pp.hide();
         if ((PositionsS.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsS.length);
         } else if (curIndS == (PositionsS.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         }
 
         if (curIndS < PositionsS.length) {
             indexS = PositionsS[curIndS][0];
 
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 pos = PositionsS[curIndS][1] + resultS[indexS][1] + indexS;
                 int idx = resultS[indexS][2] + indexS;
                 sourceTextArea.setCursorPos(0);
                 sourceTextArea.setCursorPos(idx);
             } else {
                 setNetScapePosMono(indexS, (sourceTextArea.getVisibleLines() / 2));
                 pos = PositionsS[curIndS][1] + resultS[indexS][1];
             }
             sourceTextArea.setSelectionRange(pos, PositionsS[curIndS][2]);
         }
         sourceTextArea.setFocus(true);
     }
 
     public void previousHitMono() {
         pp.hide();
         if ((PositionsS.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == (PositionsS.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsS.length);
         }
         if (curIndS >= 0) {
             indexS = PositionsS[curIndS][0];
 
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 pos = PositionsS[curIndS][1] + resultS[indexS][1] + indexS;
                 int idx = resultS[indexS][2] + indexS;
                 sourceTextArea.setCursorPos(0);
                 sourceTextArea.setCursorPos(idx);
             } else {
                 setNetScapePosMono(indexS, (sourceTextArea.getVisibleLines() / 2));
                 pos = PositionsS[curIndS][1] + resultS[indexS][1];
             }
             sourceTextArea.setSelectionRange(pos, PositionsS[curIndS][2]);
         }
         sourceTextArea.setFocus(true);
     }
 
     public void AddHitsEventsMono() {
 
         // Handler of the going to the next line in the source text
         NextHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
 
                 if (curIndS < PositionsS.length - 1) {
                     curIndS++;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     nextHitMono();
                 } else {
                     nextHitMono();
                 }
             }
         });
 
         // Handler of the going to the previous line in the source text
         PreviousHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 if (curIndS > 0) {
                     curIndS--;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     previousHitMono();
                 } else {
                     previousHitMono();
                 }
             }
         });
     }
 
     public void AddHitsEventsMonoAO() {
 
         // Handler of the going to the next line in the source text
         NextHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
 
                 if (curIndS < PositionsS.length - 1) {
                     curIndS++;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     nextHitMono();
                 } else {
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     nextHitMono();
                 }
             }
         });
 
         // Handler of the going to the previous line in the source text
         PreviousHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 if (curIndS > 0) {
                     curIndS--;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     previousHitMono();
                 } else {
                     previousHitMono();
                 }
             }
         });
     }
 
     public void AddOtherEventsMono() {
 
         // Handler of the going to align the line in the source text
         schS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 setMessage("info", GuiMessageConst.MSG_7);
                 pSch.setPopupPosition(sourceTextArea.getAbsoluteLeft() + sourceTextArea.getOffsetWidth() / 8, sourceTextArea.getAbsoluteTop());
                 pSch.show();
                 SchArea.setFocus(true);
                 SchBtn.removeAllListeners();
                 SchBtn.addListener(Events.OnClick, new Listener<BaseEvent>() {
                     @Override
                     public void handleEvent(BaseEvent be) {
                         ClearHitsEvents();
                         words = null;
 
                         search = SchArea.getText().trim();
                         queryLength = search.length();
                         if ((search.contains(" AND "))
                                 || (search.contains(" OR "))) {
                             words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             getPositionsMonoAO(resultS, contentS, words, queryLength);
                         } else if (search.contains(" NEAR ")) {
                             words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             if (GuiConstant.TA_HILITE_OVER_CR) {
                                 getPositionsNearMonoCR(contentS, words, queryLength);
                             } else {
                                 getPositionsNearMono(resultS, contentS, words, queryLength);
                             }
                         } else if ((search.contains("*"))) {
                             rpcS.getExpandTerms(search, new AsyncCallback<String[]>() {
                                 @Override
                                 public void onFailure(Throwable caught) {
                                     setMessage("error", GuiMessageConst.MSG_3);
                                 }
 
                                 @Override
                                 public void onSuccess(String[] result) {
                                     words = Utility.getWildCharQueryWords(result, MainEntryPoint.stopWords);
                                     getPositionsMonoAO(resultS, contentS, words, queryLength);
                                 }
                             });
                         } else {
                             if (search.startsWith("\"")) {
                                 words = Utility.getexactWords(search);
                             } else {
                                 words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                             }
                             if (GuiConstant.TA_HILITE_OVER_CR) {
                                 getPositionsMonoCR(contentS, words, queryLength);
                             } else {
                                 getPositionsMono(resultS, contentS, words, queryLength);
                             }
                         }
                     }
                 });
             }
         });
         save.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 setMessage("info", GuiMessageConst.MSG_35);
                 MyCatDownload.downloadZipFromServlet(Align.source.uri, msg);
             }
         });
 
         orgnS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 rpcS.getOriginalUrl(Align.source.uri, new AsyncCallback<String>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         setMessage("error", GuiMessageConst.MSG_4);
                     }
 
                     @Override
                     public void onSuccess(String result) {
 //                        Window.alert("request : " + result);
                         Window.open(result, "Original", features);
                     }
                 });
             }
         });
 
         CclBtn.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 pSch.hide();
             }
         });
 
         SchArea.addKeyPressHandler(new KeyPressHandler() {
             @Override
             public void onKeyPress(KeyPressEvent event) {
 
                 if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                     ClearHitsEvents();
                     words = null;
                     search = SchArea.getText().trim();
                     queryLength = search.length();
                     if ((search.contains(" AND "))
                             || (search.contains(" OR "))) {
                         words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                         getPositionsMonoAO(resultS, contentS, words, queryLength);
                     } else if (search.contains(" NEAR ")) {
                         words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                         if (GuiConstant.TA_HILITE_OVER_CR) {
                             getPositionsNearMonoCR(contentS, words, queryLength);
                         } else {
                             getPositionsNearMono(resultS, contentS, words, queryLength);
                         }
                     } else if ((search.contains("*"))) {
                         rpcS.getExpandTerms(search, new AsyncCallback<String[]>() {
                             @Override
                             public void onFailure(Throwable caught) {
                                 setMessage("error", GuiMessageConst.MSG_3);
                             }
 
                             @Override
                             public void onSuccess(String[] result) {
                                 words = Utility.getWildCharQueryWords(result, MainEntryPoint.stopWords);
                                 getPositionsMonoAO(resultS, contentS, words, queryLength);
                             }
                         });
                     } else {
                         if (search.startsWith("\"")) {
                             words = Utility.getexactWords(search);
                         } else {
                             words = Utility.getQueryWords(search, MainEntryPoint.stopWords);
                         }
                         if (GuiConstant.TA_HILITE_OVER_CR) {
                             getPositionsMonoCR(contentS, words, queryLength);
                         } else {
                             getPositionsMono(resultS, contentS, words, queryLength);
                         }
                     }
 
                 }
             }
         });
     }
 
     public void getTextContent(String file, String langS, String langT, String Query) {
         langtgt = langT;
         langsrc = langS;
 //        Window.alert("getting the content of the file: "+file);
         if (langT.contains("AR")) {
             targetTextArea.setDirection(Direction.RTL);
         } else {
             targetTextArea.setDirection(Direction.LTR);
         }
         if (langS.contains("AR")) {
             sourceTextArea.setDirection(Direction.RTL);
         } else {
             sourceTextArea.setDirection(Direction.LTR);
         }
         if ((GuiConstant.REMOVE_AGLUTINATED_SPACE) && (GuiConstant.AGLUTINATED_LANG_LIST.contains(langT))) {
             remSpace = true;
         } else {
             remSpace = false;
         }
 //        Window.alert("" + remSpace);
         // remote procedure call to the server to get the content of the text areas
         rpcS.getContent(file, langS, langT, Query, sourceTextArea.getCharacterWidth(), sourceTextArea.getVisibleLines(), remSpace, new AsyncCallback<GwtAlignBiText>() {
             @Override
             public void onFailure(Throwable caught) {
                 setMessage("error", GuiMessageConst.MSG_56);
             }
 
             @Override
             public void onSuccess(GwtAlignBiText result) {
                 Align = result;
                 setMessage("info", GuiMessageConst.MSG_9 + Align.source.uri);
                 if ((Align.target.content.contains("** ERROR")) || (Align.target == null)) {
                     SetMonoTextBehaviour();
                     sourceTextArea.setFocus(true);
                 } else {
                     SetBiTextBehaviour();
                     sourceTextArea.setFocus(true);
                 }
             }
         });
     }
 
     public void SetBiTextBehaviour() {
         Map = Align.map;
 
         ClearHitsEvents();
         setVariables();
         AddOtherEvents();
 
         curIndS = 0;
         curIndT = 0;
         PositionsS = null;
         PositionsT = null;
 
         if ((MainEntryPoint.QUERY.contains(" AND "))
                 || (MainEntryPoint.QUERY.contains(" OR "))
                 || (MainEntryPoint.QUERY.contains("*"))) {
             getPositionsSAO(resultS, contentS, words, queryLength);
         } else if (MainEntryPoint.QUERY.contains(" NEAR ")) {
             if (GuiConstant.TA_HILITE_OVER_CR) {
                 getPositionsNearSCR(contentS, words, queryLength);
             } else {
                 getPositionsNearS(resultS, contentS, words, queryLength);
             }
         } else {
             if (GuiConstant.TA_HILITE_OVER_CR) {
                 getPositionsSCR(contentS, words, queryLength);
             } else {
                 getPositionsS(resultS, contentS, words, queryLength);
             }
         }
     }
 
     public void SetMonoTextBehaviour() {
         ClearHitsEvents();
         setVariablesMono();
         AddOtherEventsMono();
 
         curIndS = 0;
         PositionsS = null;
         if ((MainEntryPoint.QUERY.contains(" AND "))
                 || (MainEntryPoint.QUERY.contains(" OR "))
                 || (MainEntryPoint.QUERY.contains("*"))) {
             getPositionsMonoAO(resultS, contentS, words, queryLength);
         } else if (MainEntryPoint.QUERY.contains(" NEAR ")) {
             if (GuiConstant.TA_HILITE_OVER_CR) {
                 getPositionsNearMonoCR(contentS, words, queryLength);
             } else {
                 getPositionsNearMono(resultS, contentS, words, queryLength);
             }
         } else {
             if (GuiConstant.TA_HILITE_OVER_CR) {
                 getPositionsMonoCR(contentS, words, queryLength);
             } else {
                 getPositionsMono(resultS, contentS, words, queryLength);
             }
         }
     }
 
     public void getPositionsS(int[][] posit, String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getQueryWordsPos(posit, content, Query, queryLn, GuiConstant.EXACT_FLG, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsS = result;
                     if (PositionsS[0][0] > -1) {
                         pSch.hide();
                         AddHitsEventsS();
                         nextHitS();
                         sourceTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsT(int[][] posit, String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getQueryWordsPos(posit, content, Query, queryLn, GuiConstant.EXACT_FLG, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsT = result;
                     if (PositionsT[0][0] > -1) {
                         pSch.hide();
                         AddHitsEventsT();
                         nextHitT();
                         targetTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsSAO(int[][] posit, String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getQueryWordsPosAO(posit, content, Query, queryLn, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsS = result;
                     if (PositionsS[0][0] > -1) {
                         pSch.hide();
                         AddHitsEventsSAO();
                         setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                         nextHitS();
                         sourceTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsTAO(int[][] posit, String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getQueryWordsPosAO(posit, content, Query, queryLn, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsT = result;
                     if (PositionsT[0][0] > -1) {
                         pSch.hide();
                         AddHitsEventsTAO();
                         setMessage("info", words.get(PositionsT[curIndT][3]) + " : " + GuiMessageConst.MSG_36 + PositionsT[curIndT][4] + " / " + PositionsT[curIndT][5] + "  --- (/ " + PositionsT.length + ")");
                         nextHitT();
                         targetTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsMono(int[][] posit, String content, ArrayList<String> Query, int queryLn) {
 //        Window.alert("gestMono PositionsS");
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getQueryWordsPos(posit, content, Query, queryLn, GuiConstant.EXACT_FLG, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsS = result;
                     if (PositionsS[0][0] > -1) {
                         pSch.hide();
                         AddHitsEventsMono();
                         nextHitMono();
                         sourceTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsMonoAO(int[][] posit, String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getQueryWordsPosAO(posit, content, Query, queryLn, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsS = result;
                     if (PositionsS[0][0] > -1) {
                         pSch.hide();
                         AddHitsEventsMonoAO();
                         setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                         nextHitMono();
                         sourceTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsSCR(String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getRefWordsPos(content, Query, queryLn, GuiConstant.REF_FACTOR, GuiConstant.REF_MIN_LN, GuiConstant.EXACT_FLG, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsS = result;
                     if (PositionsS[0][0] > -1) {
                         if (words.size() > GuiConstant.MAX_SEARCH_SIZE) {
                             setMessage("warning", GuiMessageConst.MSG_34);
                         }
                         pSch.hide();
                         AddHitsEventsSCR();
                         nextHitSCR();
                         sourceTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsTCR(String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getRefWordsPos(content, Query, queryLn, GuiConstant.REF_FACTOR, GuiConstant.REF_MIN_LN, GuiConstant.EXACT_FLG, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsT = result;
                     if (PositionsT[0][0] > -1) {
                         if (words.size() > GuiConstant.MAX_SEARCH_SIZE) {
                             setMessage("warning", GuiMessageConst.MSG_34);
                         }
                         pSch.hide();
                         AddHitsEventsTCR();
                         nextHitTCR();
                         targetTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsNearSCR(String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getHitPosNearCR(content, Query, queryLn, GuiConstant.REF_FACTOR, GuiConstant.NEAR_DISTANCE, GuiConstant.TA_NEAR_AVG_TERM_CHAR, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsS = result;
                     if (PositionsS[0][0] > -1) {
                         if (words.size() > GuiConstant.MAX_SEARCH_SIZE) {
                             setMessage("warning", GuiMessageConst.MSG_34);
                         }
                         pSch.hide();
                         AddHitsEventsSCR();
                         nextHitSCR();
                         sourceTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsNearS(int[][] posit, String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getHitPosNear(posit, content, Query, queryLn, GuiConstant.REF_FACTOR, GuiConstant.NEAR_DISTANCE, GuiConstant.TA_NEAR_AVG_TERM_CHAR, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsS = result;
                     if (PositionsS[0][0] > -1) {
                         if (words.size() > GuiConstant.MAX_SEARCH_SIZE) {
                             setMessage("warning", GuiMessageConst.MSG_34);
                         }
                         pSch.hide();
                         AddHitsEventsS();
                         nextHitS();
                         sourceTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsNearTCR(String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getHitPosNearCR(content, Query, queryLn, GuiConstant.REF_FACTOR, GuiConstant.NEAR_DISTANCE, GuiConstant.TA_NEAR_AVG_TERM_CHAR, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsT = result;
                     if (PositionsT[0][0] > -1) {
                         if (words.size() > GuiConstant.MAX_SEARCH_SIZE) {
                             setMessage("warning", GuiMessageConst.MSG_34);
                         }
                         pSch.hide();
                         AddHitsEventsTCR();
                         nextHitTCR();
                         targetTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsNearT(int[][] posit, String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getHitPosNear(posit, content, Query, queryLn, GuiConstant.REF_FACTOR, GuiConstant.NEAR_DISTANCE, GuiConstant.TA_NEAR_AVG_TERM_CHAR, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsT = result;
                     if (PositionsS[0][0] > -1) {
                         if (words.size() > GuiConstant.MAX_SEARCH_SIZE) {
                             setMessage("warning", GuiMessageConst.MSG_34);
                         }
                         pSch.hide();
                         AddHitsEventsT();
                         nextHitT();
                         targetTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsNearMonoCR(String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getHitPosNearCR(content, Query, queryLn, GuiConstant.REF_FACTOR, GuiConstant.NEAR_DISTANCE, GuiConstant.TA_NEAR_AVG_TERM_CHAR, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsS = result;
                     if (PositionsS[0][0] > -1) {
                         if (words.size() > GuiConstant.MAX_SEARCH_SIZE) {
                             setMessage("warning", GuiMessageConst.MSG_34);
                         }
                         pSch.hide();
                         AddHitsEventsMonoCR();
                         nextHitMonoCR();
                         sourceTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsNearMono(int[][] posit, String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getHitPosNear(posit, content, Query, queryLn, GuiConstant.REF_FACTOR, GuiConstant.NEAR_DISTANCE, GuiConstant.TA_NEAR_AVG_TERM_CHAR, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsS = result;
                     if (PositionsS[0][0] > -1) {
                         if (words.size() > GuiConstant.MAX_SEARCH_SIZE) {
                             setMessage("warning", GuiMessageConst.MSG_34);
                         }
                         pSch.hide();
                         AddHitsEventsMono();
                         nextHitMono();
                         sourceTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void getPositionsMonoCR(String content, ArrayList<String> Query, int queryLn) {
         if ((!Query.isEmpty()) && !(Query == null)) {
             rpcS.getRefWordsPos(content, Query, queryLn, GuiConstant.REF_FACTOR, GuiConstant.REF_MIN_LN, GuiConstant.EXACT_FLG, new AsyncCallback<int[][]>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     setMessage("error", GuiMessageConst.MSG_10);
                 }
 
                 @Override
                 public void onSuccess(int[][] result) {
                     ClearHitsEvents();
                     PositionsS = result;
                     if (PositionsS[0][0] > -1) {
                         if (words.size() > GuiConstant.MAX_SEARCH_SIZE) {
                             setMessage("warning", GuiMessageConst.MSG_34);
                         }
                         pSch.hide();
                         AddHitsEventsMonoCR();
                         nextHitMonoCR();
                         sourceTextArea.setFocus(true);
                     } else {
                         setMessage("error", GuiMessageConst.MSG_33);
                     }
                 }
             });
         }
     }
 
     public void AddHitsEventsMonoCR() {
 
         // Handler of the going to the next line in the source text
         NextHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
 
                 if (curIndS < PositionsS.length - 1) {
                     curIndS++;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     nextHitMonoCR();
                 } else {
                     nextHitMonoCR();
                 }
             }
         });
 
         // Handler of the going to the previous line in the source text
         PreviousHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 if (curIndS > 0) {
                     curIndS--;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     previousHitMonoCR();
                 } else {
                     previousHitMonoCR();
                 }
             }
         });
     }
 
     public void AddHitsEventsSCR() {
 
         // Handler of the going to the next line in the source text
         NextHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
 
                 if (curIndS < PositionsS.length - 1) {
                     curIndS++;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     nextHitSCR();
                 } else {
                     nextHitSCR();
                 }
             }
         });
 
         // Handler of the going to the previous line in the source text
         PreviousHitS.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 if (curIndS > 0) {
                     curIndS--;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndS) + " / " + PositionsS.length);
                     previousHitSCR();
                 } else {
                     previousHitSCR();
                 }
             }
         });
     }
 
     public void AddHitsEventsTCR() {
 
         // Handler of the going to the next line in the source text
         NextHitT.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
 
                 if (curIndT < PositionsT.length - 1) {
                     curIndT++;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndT) + " / " + PositionsT.length);
                     nextHitTCR();
                 } else {
                     nextHitTCR();
                 }
             }
         });
 
         // Handler of the going to the previous line in the source text
         PreviousHitT.addListener(Events.OnClick, new Listener<BaseEvent>() {
             @Override
             public void handleEvent(BaseEvent be) {
                 if (curIndT > 0) {
                     curIndT--;
                     setMessage("info", GuiMessageConst.MSG_36 + (1 + curIndT) + " / " + PositionsT.length);
                     previousHitTCR();
                 } else {
                     previousHitTCR();
                 }
             }
         });
     }
 
     public void nextHitMonoCR() {
         if ((PositionsS.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsS.length);
         } else if (curIndS == (PositionsS.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         }
         if (words.size() > 1000) {
             setMessage("warning", GuiMessageConst.MSG_34);
         }
         if (curIndS < PositionsS.length) {
             pos = PositionsS[curIndS][0];
             indexS = Utility.getInd(pos, resultS);
 
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 if (pos > 0) {
                     pos += indexS;
                 }
                 indexS = Utility.getInd(pos, resultS);
                 int idx = resultS[indexS][2];
                 sourceTextArea.setCursorPos(0);
                 sourceTextArea.setCursorPos(idx);
             } else {
                 setNetScapePosMono(indexS, (sourceTextArea.getVisibleLines() / 2));
             }
             sourceTextArea.setSelectionRange(pos, PositionsS[curIndS][1]);
         }
         sourceTextArea.setFocus(true);
     }
 
     public void previousHitMonoCR() {
         if ((PositionsS.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == (PositionsS.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsS.length);
         }
         if (curIndS >= 0) {
             pos = PositionsS[curIndS][0];
             indexS = Utility.getInd(pos, resultS);
 
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 if (pos > 0) {
                     pos += indexS;
                 }
                 indexS = Utility.getInd(pos, resultS);
                 int idx = resultS[indexS][2];
                 sourceTextArea.setCursorPos(0);
                 sourceTextArea.setCursorPos(idx);
             } else {
                 setNetScapePosMono(indexS, (sourceTextArea.getVisibleLines() / 2));
             }
             sourceTextArea.setSelectionRange(pos, PositionsS[curIndS][1]);
         }
         sourceTextArea.setFocus(true);
     }
 
     public void nextHitTCR() {
         pp.hide();
         if ((PositionsT.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsT.length);
         } else if (curIndT == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsT.length);
         } else if (curIndT == (PositionsT.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsT.length);
         }
         if (words.size() > 1000) {
             setMessage("warning", GuiMessageConst.MSG_34);
         }
         if (curIndT < PositionsT.length) {
             pos = PositionsT[curIndT][0];
             indexT = Utility.getInd(pos, resultT);
             indexS = Map.to[indexT];
             int idxlast = Map.to[Utility.getInd(pos + queryLength / 4, resultT)];
             showpanel(true, Utility.getln(indexS, idxlast, resultS), indexS);
 
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 if (pos > 0) {
                     pos += indexT;
                 }
                 indexT = Utility.getInd(pos, resultT);
                 indexS = Map.to[indexT];
                 int idx = resultS[indexS][2];
                 int idxt = resultT[indexT][2];
                 sourceTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(0);
                 sourceTextArea.setCursorPos(idx);
                 targetTextArea.setCursorPos(idxt);
             } else {
                 setNetScapePosT(indexS, indexT, (targetTextArea.getVisibleLines() / 2));
             }
             targetTextArea.setSelectionRange(pos, PositionsT[curIndT][1]);
         }
         targetTextArea.setFocus(true);
     }
 
     public void previousHitTCR() {
         pp.hide();
         if ((PositionsT.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsT.length);
         } else if (curIndT == (PositionsT.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsT.length);
         } else if (curIndT == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsT.length);
         }
 
         if (curIndT >= 0) {
             pos = PositionsT[curIndT][0];
             indexT = Utility.getInd(pos, resultT);
             indexS = Map.to[indexT];
             int idxlast = Map.to[Utility.getInd(pos + queryLength / 4, resultT)];
             showpanel(true, Utility.getln(indexS, idxlast, resultS), indexS);
 
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 if (pos > 0) {
                     pos += indexT;
                 }
                 indexT = Utility.getInd(pos, resultT);
                 indexS = Map.to[indexT];
                 int idx = resultS[indexS][2];
                 int idxt = resultT[indexT][2];
                 sourceTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(0);
                 sourceTextArea.setCursorPos(idx);
                 targetTextArea.setCursorPos(idxt);
             } else {
                 setNetScapePosT(indexS, indexT, (targetTextArea.getVisibleLines() / 2));
             }
             targetTextArea.setSelectionRange(pos, PositionsT[curIndS][1]);
         }
         targetTextArea.setFocus(true);
     }
 
     public void nextHitSCR() {
         pp.hide();
         if ((PositionsS.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsS.length);
         } else if (curIndS == (PositionsS.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         }
         if (words.size() > 1000) {
             setMessage("warning", GuiMessageConst.MSG_34);
         }
         if (curIndS < PositionsS.length) {
             pos = PositionsS[curIndS][0];
             indexS = Utility.getInd(pos, resultS);
             indexT = Map.from[indexS];
             int idxlast = Map.from[Utility.getInd(pos + queryLength / 4, resultS)];
             showpanel(false, Utility.getln(indexT, idxlast, resultT), indexT);
 
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 if (pos > 0) {
                     pos += indexS;
                 }
                 indexS = Utility.getInd(pos, resultS);
                 indexT = Map.from[indexS];
                 int idx = resultS[indexS][2];
                 int idxt = resultT[indexT][2];
                 sourceTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(idxt);
                 sourceTextArea.setCursorPos(idx);
             } else {
                 setNetScapePos(indexS, indexT, (sourceTextArea.getVisibleLines() / 2));
             }
             sourceTextArea.setSelectionRange(pos, PositionsS[curIndS][1]);
         }
         sourceTextArea.setFocus(true);
     }
 
     public void previousHitSCR() {
         pp.hide();
         if ((PositionsS.length - 1 == 0)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == (PositionsS.length - 1)) {
             setMessage("info", GuiMessageConst.MSG_38 + " / " + PositionsS.length);
         } else if (curIndS == 0) {
             setMessage("info", GuiMessageConst.MSG_37 + " / " + PositionsS.length);
         }
         if (curIndS >= 0) {
             pos = PositionsS[curIndS][0];
             indexS = Utility.getInd(pos, resultS);
             indexT = Map.from[indexS];
             int idxlast = Map.from[Utility.getInd(pos + queryLength / 4, resultS)];
             showpanel(false, Utility.getln(indexT, idxlast, resultT), indexT);
 
             if ((Window.Navigator.getUserAgent().contains("MSIE 7.0")) || (Window.Navigator.getUserAgent().contains("MSIE 8.0"))) {
                 if (pos > 0) {
                     pos += indexS;
                 }
                 indexS = Utility.getInd(pos, resultS);
                 indexT = Map.from[indexS];
                 int idx = resultS[indexS][2];
                 int idxt = resultT[indexT][2];
                 sourceTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(0);
                 targetTextArea.setCursorPos(idxt);
                 sourceTextArea.setCursorPos(idx);
             } else {
                 setNetScapePos(indexS, indexT, (sourceTextArea.getVisibleLines() / 2));
             }
             sourceTextArea.setSelectionRange(pos, PositionsS[curIndS][1]);
         }
         sourceTextArea.setFocus(true);
     }
 
     public void setMessage(String type, String message) {
         msg.setStyleName("gwt-TA-" + type.toLowerCase());
         msg.setText(message);
     }
 
     public void updateSize() {
         sourceTextArea.setCharacterWidth(MainEntryPoint.IMeasures.TA_TEXTAREA_WIDTH);
         sourceTextArea.setVisibleLines(MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT);
         targetTextArea.setCharacterWidth(MainEntryPoint.IMeasures.TA_TEXTAREA_WIDTH);
         targetTextArea.setVisibleLines(MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT);
         sourceTextArea.setHeight("" + pixS * MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT + "px");
         targetTextArea.setHeight("" + pixS * MainEntryPoint.IMeasures.TA_TEXTAREA_HEIGHT + "px");
     }
 }
