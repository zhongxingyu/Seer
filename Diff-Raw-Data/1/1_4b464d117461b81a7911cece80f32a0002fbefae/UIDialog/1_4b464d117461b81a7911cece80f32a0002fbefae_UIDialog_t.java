 /*
  ##################################################################
  #                     GNU BACKGAMMON MOBILE                      #
  ##################################################################
  #                                                                #
  #  Authors: Domenico Martella - Davide Saurino                   #
  #  E-mail: info@alcacoop.it                                      #
  #  Date:   19/12/2012                                            #
  #                                                                #
  ##################################################################
  #                                                                #
  #  Copyright (C) 2012   Alca Societa' Cooperativa                #
  #                                                                #
  #  This file is part of GNU BACKGAMMON MOBILE.                   #
  #  GNU BACKGAMMON MOBILE is free software: you can redistribute  # 
  #  it and/or modify it under the terms of the GNU General        #
  #  Public License as published by the Free Software Foundation,  #
  #  either version 3 of the License, or (at your option)          #
  #  any later version.                                            #
  #                                                                #
  #  GNU BACKGAMMON MOBILE is distributed in the hope that it      #
  #  will be useful, but WITHOUT ANY WARRANTY; without even the    #
  #  implied warranty of MERCHANTABILITY or FITNESS FOR A          #
  #  PARTICULAR PURPOSE.  See the GNU General Public License       #
  #  for more details.                                             #
  #                                                                #
  #  You should have received a copy of the GNU General            #
  #  Public License v3 along with this program.                    #
  #  If not, see <http://http://www.gnu.org/licenses/>             #
  #                                                                #
  ##################################################################
 */
 
 package it.alcacoop.backgammon.ui;
 
 import it.alcacoop.backgammon.GnuBackgammon;
 import it.alcacoop.backgammon.fsm.BaseFSM;
 import it.alcacoop.backgammon.fsm.BaseFSM.Events;
 import it.alcacoop.backgammon.logic.MatchState;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.Window;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
 
 
 public final class UIDialog extends Window {
 
   private Table t1, t2, t3;
   private TextButton bContinue;
   private TextButton bYes;
   private TextButton bNo;
   private TextButton bCancel;
   private TextButton bExport;
   
   private Label label;
   private Drawable background;
   private ClickListener cl;
   
   private static UIDialog instance;
   
   private BaseFSM.Events evt;
   private boolean quitWindow = false;
   private boolean optionsWindow = false;
   private boolean leaveWindow = false;
   
   private GameOptionsTable opts;
   
   public static boolean dialogOpened = false;
   
   
   static {
     instance = new UIDialog();
   }
   
   private UIDialog() {
     super("", GnuBackgammon.skin);
     setModal(true);
     setMovable(false);
     
     cl = new ClickListener(){
       public void clicked(InputEvent event, float x, float y) {
         final String s;
         if (event.getTarget() instanceof Label) {
           s = ((Label)event.getTarget()).getText().toString().toUpperCase();
         } else {
           s = ((TextButton)event.getTarget()).getText().toString().toUpperCase();
         }
         hide(new Runnable(){
           @Override
           public void run() {
             instance.remove();
             if (leaveWindow) {
               GnuBackgammon.fsm.processEvent(instance.evt, s);
               return;
             }
             
             boolean ret = s.equals("YES")||s.equals("OK");
             
             if ((instance.quitWindow)&&(ret)) {
               Gdx.app.exit();
             } else {
               GnuBackgammon.fsm.processEvent(instance.evt, ret);
               if (instance.optionsWindow) opts.savePrefs();
             }
           }
         });
       };
     };
     
     label = new Label("", GnuBackgammon.skin);
     
     TextButtonStyle tl = GnuBackgammon.skin.get("button", TextButtonStyle.class);
     
     bYes = new TextButton("Yes", tl);
     bYes.addListener(cl);
     bNo = new TextButton("No", tl);
     bNo.addListener(cl);
     bContinue = new TextButton("Ok", tl);
     bContinue.addListener(cl);
     bCancel = new TextButton("Cancel", tl);
     bCancel.addListener(cl);
     bExport = new TextButton("Export Match", tl);
     bExport.addListener(new ClickListener(){
       @Override
       public void clicked(InputEvent event, float x, float y) {
         GnuBackgammon.Instance.myRequestHandler.shareMatch(GnuBackgammon.Instance.rec);
         super.clicked(event, x, y);
       }
     });
 
     background = GnuBackgammon.skin.getDrawable("default-window");
     setBackground(background);
     
     opts = new GameOptionsTable(false, cl);
     
     t1 = new Table();
     t1.setFillParent(true);
     t1.add(label).fill().expand().center();
     
     t2 = new Table();
     t2.setFillParent(true);
     t2.add().colspan(2).expand();
     t2.add(bContinue).fill().expand();
     t2.add().colspan(2).expand();
     
     t3 = new Table();
     t3.setFillParent(true);
     t3.add().expand();
     t3.add(bNo).fill().expand();
     t3.add().expand();
     t3.add(bYes).fill().expand();
     t3.add().expand();
     
     setColor(1,1,1,0);
     
   }
   
   private void setText(String t) {
     label.setText(t);
   }
   
   private void hide(Runnable r) {
     dialogOpened = false;
     addAction(Actions.sequence(
         Actions.fadeOut(0.3f),
         Actions.run(r)
     ));
   }
   
   public static void getYesNoDialog(BaseFSM.Events evt, String text, Stage stage) {
     getYesNoDialog(evt, text, 1, stage);
   }
   public static void getYesNoDialog(BaseFSM.Events evt, String text, float alpha, Stage stage) {
     dialogOpened = true;
     instance.quitWindow = false;
     instance.optionsWindow = false;
     instance.leaveWindow = false;
     instance.evt = evt;
     instance.remove();
     instance.setText(text);
     
     float height = stage.getHeight()*0.4f;
     float width = stage.getWidth()*0.6f;
     
     instance.clear();
     instance.setWidth(width);
     instance.setHeight(height);
     instance.setX((stage.getWidth()-width)/2);
     instance.setY((stage.getHeight()-height)/2);
     
     instance.row().padTop(width/25);
     instance.add(instance.label).colspan(5).expand().align(Align.center);
     
     instance.row().pad(width/25);
     instance.add();
     instance.add(instance.bNo).fill().expand().height(height*0.25f).width(width/4);
     instance.add();
     instance.add(instance.bYes).fill().expand().height(height*0.25f).width(width/4);
     instance.add();
     
     stage.addActor(instance);
     instance.addAction(Actions.alpha(alpha, 0.3f));
   }
   
   public static void getContinueDialog(BaseFSM.Events evt, String text, Stage stage) {
     getContinueDialog(evt, text, 1, stage);
   }
   public static void getContinueDialog(BaseFSM.Events evt, String text, float alpha, Stage stage) {
     dialogOpened = true;
     instance.quitWindow = false;
     instance.optionsWindow = false;
     instance.leaveWindow = false;
     instance.evt = evt;
     instance.remove();
     instance.setText(text);
     
     float height = stage.getHeight()*0.4f;
     float width = stage.getWidth()*0.6f;
     
     instance.clear();
     instance.setWidth(width);
     instance.setHeight(height);
     instance.setX((stage.getWidth()-width)/2);
     instance.setY((stage.getHeight()-height)/2);
     
     instance.row().padTop(width/25);
     instance.add(instance.label).colspan(3).expand().align(Align.center);
     
     instance.row().pad(width/25);
     instance.add();
     instance.add(instance.bContinue).fill().expand().height(height*0.25f).width(width/4);
     instance.add();
     
     stage.addActor(instance);
     instance.addAction(Actions.alpha(alpha, 0.3f));
   }
  
   
   public static void getEndGameDialog(BaseFSM.Events evt, String text, String text1, String score1, String score2, Stage stage) {
     getEndGameDialog(evt, text, text1, score1, score2, 1, stage);
   }
   public static void getEndGameDialog(BaseFSM.Events evt, String text, String text1, String score1, String score2, float alpha, Stage stage) {
     dialogOpened = true;
     instance.quitWindow = false;
     instance.optionsWindow = false;
     instance.leaveWindow = false;
     instance.evt = evt;
     instance.remove();
     
     float height = stage.getHeight()*0.6f;
     float width = stage.getWidth()*0.6f;
     
     instance.clear();
     instance.setWidth(width);
     instance.setHeight(height);
     instance.setX((stage.getWidth()-width)/2);
     instance.setY((stage.getHeight()-height)/2);
     
     instance.row().padTop(width/25);
     instance.add().expand();
     instance.add(text1).colspan(2).expand().align(Align.center);
     instance.add().expand();
     
     instance.row();
     instance.add().expand();
     instance.add("Overall Score " + text).colspan(2).expand().align(Align.center);
     instance.add().expand();
     instance.row();
     instance.add().expand();
     instance.add(score1).expand().align(Align.center);
     instance.add(score2).expand().align(Align.center);
     instance.add().expand();
     
     Table t1 = new Table();
     t1.row().expand().fill();
     t1.add();
     t1.add(instance.bContinue).colspan(2).fill().expand().height(height*0.15f).width(width/3);
     if ((MatchState.anScore[0]>=MatchState.nMatchTo||MatchState.anScore[1]>=MatchState.nMatchTo)&&(MatchState.matchType==0)) {
       t1.add();
       t1.add(instance.bExport).colspan(2).fill().expand().height(height*0.15f).width(width/3);
     }
     t1.add();
     instance.row();
     instance.add(t1).colspan(4).fill().padBottom(width/25);
     
     stage.addActor(instance);
     instance.addAction(Actions.alpha(alpha, 0.3f));
   }
   
   
   public static void getFlashDialog(BaseFSM.Events evt, String text, Stage stage) {
     getFlashDialog(evt, text, 1, stage);
   }
   public static void getFlashDialog(BaseFSM.Events evt, String text, float alpha, Stage stage) {
     dialogOpened = true;
     instance.quitWindow = false;
     instance.optionsWindow = false;
     instance.leaveWindow = false;
     instance.evt = evt;
     instance.remove();
     instance.setText(text);
     
     float height = stage.getHeight()*0.3f;
     float width = stage.getWidth()*0.6f;
     
     instance.clear();
     instance.setWidth(width);
     instance.setHeight(height);
     instance.setX((stage.getWidth()-width)/2);
     instance.setY((stage.getHeight()-height)/2);
     
     instance.add(instance.label).expand().align(Align.center);
     
     stage.addActor(instance);
     instance.addAction(Actions.sequence(
         Actions.alpha(alpha, 0.3f),
         Actions.delay(1.5f),
         Actions.fadeOut(0.3f),
         Actions.run(new Runnable() {
           @Override
           public void run() {
             instance.remove();
            dialogOpened = false;
             GnuBackgammon.fsm.processEvent(instance.evt, true);
           }
         })
     ));
   }
   
   public static void getQuitDialog(Stage stage) {
     getQuitDialog(1, stage);
   }
   public static void getQuitDialog(float alpha, Stage stage) {
     dialogOpened = true;
     instance.quitWindow = true;
     instance.optionsWindow = false;
     instance.leaveWindow = false;
     instance.remove();
     instance.setText("Really quit the game?");
     
     float height = stage.getHeight()*0.4f;
     float width = stage.getWidth()*0.5f;
     
     instance.clear();
     instance.setWidth(width);
     instance.setHeight(height);
     instance.setX((stage.getWidth()-width)/2);
     instance.setY((stage.getHeight()-height)/2);
     
     instance.row().padTop(width/25);
     instance.add(instance.label).colspan(5).expand().align(Align.center);
     
     instance.row().pad(width/25);
     instance.add();
     instance.add(instance.bNo).fill().expand().height(height*0.25f).width(width/4);
     instance.add();
     instance.add(instance.bYes).fill().expand().height(height*0.25f).width(width/4);
     instance.add();
     
     stage.addActor(instance);
     instance.addAction(Actions.alpha(alpha, 0.3f));
   }
 
   
   public static void getLeaveDialog(BaseFSM.Events evt, float alpha, Stage stage) {
     dialogOpened = true;
     instance.quitWindow = false;
     instance.optionsWindow = false;
     instance.leaveWindow = true;
     instance.evt = evt;
     instance.remove();
     
     instance.setText("You are leaving current match.");
     
     float height = stage.getHeight()*0.45f;
     float width = stage.getWidth()*0.6f;
     
     instance.clear();
     instance.setWidth(width);
     instance.setHeight(height);
     instance.setX((stage.getWidth()-width)/2);
     instance.setY((stage.getHeight()-height)/2);
     
     instance.row().padTop(width/25);
     instance.add(instance.label).colspan(7).expand().align(Align.center);
     instance.row().padTop(width/45);
     instance.add(new Label("Do you want to save it?", GnuBackgammon.skin)).colspan(7).expand().align(Align.center);
     
     instance.row().padTop(width/25);
     instance.add();
     instance.add(instance.bYes).fill().expand().height(height*0.25f).width(width/4.5f);
     instance.add();
     instance.add(instance.bNo).fill().expand().height(height*0.25f).width(width/4.5f);
     instance.add();
     instance.add(instance.bCancel).fill().expand().height(height*0.25f).width(width/4.5f);
     instance.add();
     
     instance.row().padBottom(width/35);
     instance.add();
     
     
     stage.addActor(instance);
     instance.addAction(Actions.alpha(alpha, 0.3f));
   }
   
   public static void getHelpDialog(Stage stage, Boolean cb) {
     getHelpDialog(1, stage, cb);
   }
   public static void getHelpDialog(float alpha, Stage stage, Boolean cb) {
     dialogOpened = true;
     instance.evt = Events.NOOP;
     instance.quitWindow = false;
     instance.leaveWindow = false;
     instance.optionsWindow = false;
     instance.remove();
     Label l = new Label(
         "GAME TYPE\n" +
         "You can choose two game type, and several options:\n" +
         "Backgammon - usual starting position\n" +
         "Nackgammon - Nack's starting position, attenuates lucky starting roll\n" +
         "Doubling Cube: use or not the doubling cube, with or without Crawford rule\n\n" +
         "START TURN\n" +
         "If cube isn't available, dices are rolled automatically,\n" +
         "else you must click on 'Double' or 'Roll' button\n\n" +
         "MOVING MECHANIC\n" +
         "You can choose two moves mechanic (Options->Move Logic):\n" +
         "TAP - once you rolled dices, select the piece you would move.\n" +
         "If legal moves for that piece are available, they will be shown.\n" +
         "Click an available point and the piece will move there.\n" +
         "AUTO - click on a piece and it moves automatically to destination point.\n" +
         "Bigger dice is played first. You can change dice order clicking on dices\n\n" +
         "You can cancel your moves in current hand just clicking the UNDO button\n" +
         "in the game options menu popup.\n\n" +
         "END TURN\n" +
         "When you finish your turn, click again the dices to take back them and change turn.\n"
     , GnuBackgammon.skin);
     l.setWrap(true);
     
     ScrollPane sc = new ScrollPane(l,GnuBackgammon.skin);
     sc.setFadeScrollBars(false);
     
     float height = stage.getHeight()*0.75f;
     float width = stage.getWidth()*0.9f;
     
     instance.clear();
     instance.row().padTop(width/25);
     instance.add(sc).colspan(3).expand().fill().align(Align.center).padTop(width/25).padLeft(width/35).padRight(width/35);
     
     instance.row().pad(width/25);
     instance.add();
     instance.add(instance.bContinue).fill().expand().height(height*0.15f).width(width/4);
     instance.add();
     
     instance.setWidth(width);
     instance.setHeight(height);
     instance.setX((stage.getWidth()-width)/2);
     instance.setY((stage.getHeight()-height)/2);
     
     stage.addActor(instance);
     instance.addAction(Actions.alpha(alpha, 0.3f));
   }
   
   
   public static void getOptionsDialog(Stage stage) {
     getOptionsDialog(1, stage);
   }
   public static void getOptionsDialog(float alpha, Stage stage) {
     dialogOpened = true;
     instance.evt = Events.NOOP;
     instance.quitWindow = false;
     instance.leaveWindow = false;
     instance.optionsWindow = true;
     instance.remove();
     
     instance.opts.initFromPrefs();
     
     float width = stage.getWidth()*0.75f;
     float height = stage.getHeight()*0.92f;
     
     instance.clear();
     instance.setWidth(width);
     instance.setHeight(height);
     instance.setX((stage.getWidth()-width)/2);
     instance.setY((stage.getHeight()-height)/2);
 
     instance.add(instance.opts).expand().fill();
     
     stage.addActor(instance);
     instance.addAction(Actions.alpha(alpha, 0.3f));
   }
   
   public static void setButtonsStyle(String b) {
     instance.bContinue.setStyle(GnuBackgammon.skin.get("button-"+b, TextButtonStyle.class));
     instance.bYes.setStyle(GnuBackgammon.skin.get("button-"+b, TextButtonStyle.class));
     instance.bNo.setStyle(GnuBackgammon.skin.get("button-"+b, TextButtonStyle.class));
     instance.bCancel.setStyle(GnuBackgammon.skin.get("button-"+b, TextButtonStyle.class));
     instance.opts.setButtonsStyle(b);
   }
 }
