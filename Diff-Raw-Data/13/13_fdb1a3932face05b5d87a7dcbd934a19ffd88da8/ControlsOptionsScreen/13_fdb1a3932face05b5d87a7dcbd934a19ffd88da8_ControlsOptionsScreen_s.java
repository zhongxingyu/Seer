 /**
  * Copyright (c) 2012, dector (dector9@gmail.com)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *   - Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  *   - Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package ua.org.dector.space_lander.screens.menu;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.*;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.ui.*;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import ua.org.dector.gcore.common.Settings;
 import ua.org.dector.gcore.game.TableScreen;
 import ua.org.dector.gcore.managers.PreferencesManager;
 import ua.org.dector.gcore.managers.SoundManager;
 import ua.org.dector.gcore.utils.Utils;
 import ua.org.dector.space_lander.Lander;
 import ua.org.dector.space_lander.constants.Labels;
 import ua.org.dector.space_lander.constants.LanderSounds;
 import ua.org.dector.space_lander.controls.LanderControls;
 
 import static ua.org.dector.space_lander.constants.UISizes.*;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class ControlsOptionsScreen extends TableScreen<Lander> {
     private int speedUpKey;
     private int rotateLeftKey;
     private int rotateRightKey;
     private int pauseKey;
     private int restartKey;
 
     private TextButton btnSave;
     private TextButton btnBack;
 
     private boolean changed;
 
     public ControlsOptionsScreen(Lander lander) {
         super(lander);
     }
 
     public void show() {
         final SoundManager soundManager = game.getSoundManager();
         final Settings settings = game.getSettings();
 
         speedUpKey      = LanderControls.SPEED_UP;
         rotateLeftKey   = LanderControls.ROTATE_LEFT;
         rotateRightKey  = LanderControls.ROTATE_RIGHT;
         pauseKey        = LanderControls.PAUSE;
         restartKey      = LanderControls.RESTART;
 
         Skin skin = game.getGraphics().getSkin();
         Table table = getTable();
 
         final TextButton btnSpeedupOption = new TextButton("", skin);
         updateTextAt(btnSpeedupOption, speedUpKey);
         btnSpeedupOption.addListener(new ClickListener() {
             public void clicked(ActorEvent event, float x, float y) {
                 getNewKey(Labels.SPEED_UP, btnSpeedupOption, Control.SPEED_UP);
             }
         });
 
         final TextButton btnRotateLeftOption = new TextButton("", skin);
         updateTextAt(btnRotateLeftOption, rotateLeftKey);
         btnRotateLeftOption.addListener(new ClickListener() {
             public void clicked(ActorEvent event, float x, float y) {
                 getNewKey(Labels.ROTATE_LEFT, btnRotateLeftOption, Control.ROTATE_LEFT);
             }
         });
 
         final TextButton btnRotateRightOption = new TextButton("", skin);
         updateTextAt(btnRotateRightOption, rotateRightKey);
         btnRotateRightOption.addListener(new ClickListener() {
             public void clicked(ActorEvent event, float x, float y) {
                 getNewKey(Labels.ROTATE_RIGHT, btnRotateRightOption, Control.ROTATE_RIGHT);
             }
         });
 
         final TextButton btnPauseOption = new TextButton("", skin);
         updateTextAt(btnPauseOption, pauseKey);
         btnPauseOption.addListener(new ClickListener() {
             public void clicked(ActorEvent event, float x, float y) {
                 getNewKey(Labels.PAUSE, btnPauseOption, Control.PAUSE);
             }
         });
 
         final TextButton btnRestartOption = new TextButton("", skin);
         updateTextAt(btnRestartOption, restartKey);
         btnRestartOption.addListener(new ClickListener() {
             public void clicked(ActorEvent event, float x, float y) {
                 getNewKey(Labels.RESTART, btnRestartOption, Control.RESTART);
             }
         });
 
         Table controlsTable = new Table(skin);
         controlsTable.defaults().padBottom(BOTTOM_SPACE);
         controlsTable.add(Labels.SPEED_UP);
         controlsTable.add(btnSpeedupOption).colspan(2);
         controlsTable.row();
         controlsTable.add(Labels.ROTATE_LEFT);
         controlsTable.add(btnRotateLeftOption).colspan(2);
         controlsTable.row();
         controlsTable.add(Labels.ROTATE_RIGHT);
         controlsTable.add(btnRotateRightOption).colspan(2);
         controlsTable.row();
         controlsTable.add(Labels.PAUSE);
         controlsTable.add(btnPauseOption).colspan(2);
         controlsTable.row();
         controlsTable.add(Labels.RESTART);
         controlsTable.add(btnRestartOption).colspan(2);
 
         ScrollPane controlsPane = new ScrollPane(controlsTable);
 
         btnSave = new TextButton(Labels.OPTIONS$SAVE, skin);
         btnSave.setVisible(false);
         btnSave.addListener(new ClickListener() {
             public void clicked(ActorEvent event, float x, float y) {
                 soundManager.play(LanderSounds.MENU_CLICK);
 
                 LanderControls.SPEED_UP     = speedUpKey;
                 LanderControls.ROTATE_LEFT  = rotateLeftKey;
                 LanderControls.ROTATE_RIGHT = rotateRightKey;
                 LanderControls.PAUSE        = pauseKey;
                 LanderControls.RESTART      = restartKey;
 
                 LanderControls.store(settings.getPrefs());
 
                 changed = false;
                 btnSave.setVisible(false);
                 btnBack.setText(Labels.OPTIONS$BACK);
             }
         });
 
         btnBack = new TextButton(Labels.OPTIONS$BACK, skin);
         btnBack.addListener(new ClickListener() {
             public void clicked(ActorEvent event, float x, float y) {
                 soundManager.play(LanderSounds.MENU_CLICK);
 
                 game.setScreen(new OptionsScreen(game));
             }
         });
 
         table.add(Labels.OPTIONS$CONTROLS).spaceBottom(TITLE_BOTTOM_SPACE).colspan(3);
         table.row();
         table.add(controlsPane).fill().padBottom(BOTTOM_SPACE);
         table.row();
         table.add(btnSave).colspan(3).size(BUTTONS_WIDTH, BUTTONS_HEIGHT)
                 .fill().uniform();
         table.row();
         table.add(btnBack).colspan(3).size(BUTTONS_WIDTH, BUTTONS_HEIGHT)
                 .fill().uniform();
 
         table.debug();
     }
 
     private void setChanged() {
         if (changed) return;
 
         changed = true;
         btnSave.setVisible(true);
         btnBack.setText(Labels.OPTIONS$CANCEL);
     }
 
     private static String getKeyString(int key) {
         return "Key " + key;
     }
 
     private void updateTextAt(TextButton button, int key) {
         button.setText(getKeyString(key));
     }
 
     private void setUpInput(Control control, int key) {
         setChanged();
 
         switch (control) {
             case SPEED_UP:      speedUpKey = key; break;
             case ROTATE_LEFT:   rotateLeftKey = key; break;
             case ROTATE_RIGHT:  rotateRightKey = key; break;
             case PAUSE:         pauseKey = key; break;
             case RESTART:       restartKey = key; break;
         }
     }
 
     public void getNewKey(String controlText,
                           final TextButton button,
                           final Control control) {
         game.getSoundManager().play(LanderSounds.MENU_CLICK);
 
         int screenWidth = Gdx.graphics.getWidth();
         int screenHeight = Gdx.graphics.getHeight();
 
         Pixmap p = new Pixmap(
                 Utils.toPowerOfTwo(screenWidth),
                 Utils.toPowerOfTwo(screenHeight),
                 Pixmap.Format.RGBA8888
         );
 
         p.setColor(0, 0, 0, .9f);
         p.fill();
 
         TextureRegion tex = new TextureRegion(
                 new Texture(p),
                 screenWidth,
                 screenHeight
         );
 
         p.dispose();
 
         final Group group = new Group();
 
         Image darkBack = new Image(tex);
         darkBack.addListener(new ActorListener() {
             public boolean keyDown(ActorEvent event, int keycode) {
                 game.getSoundManager().play(LanderSounds.MENU_CLICK);
 
                 setUpInput(control, keycode);
                 updateTextAt(button, keycode);
 
                 disableKeyboardInput(group);
                 return true;
             }
 
             public boolean touchDown(ActorEvent event, float x, float y,
                                      int pointer, int button) {
                 game.getSoundManager().play(LanderSounds.MENU_CLICK);
 
                 disableKeyboardInput(group);
                 return true;
             }
         });
 
         String textStr = String.format("%s \"%s\" action",
                 Labels.PRESS_NEW_KEY_FOR, controlText);
         Label textLabel = new Label(textStr, game.getGraphics().getSkin());
         textLabel.addListener(new ClickListener() {
             public void clicked(ActorEvent event, float x, float y) {
                 disableKeyboardInput(group);
             }
         });
 
         Table textTable = new Table(game.getGraphics().getSkin());
         textTable.setPosition(screenWidth / 2, screenHeight / 2);
         textTable.add(textLabel);
 
         group.addActor(darkBack);
         group.addActor(textTable);
         getStage().addActor(group);
         getStage().setKeyboardFocus(darkBack);
     }
 
     private void disableKeyboardInput(Group group) {
         group.addAction(Actions.removeActor());
         getStage().setKeyboardFocus(null);
        
     }
 
     static enum Control {
         SPEED_UP, ROTATE_LEFT, ROTATE_RIGHT, PAUSE, RESTART
     }
 }
