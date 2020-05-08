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
 
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.ActorEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.*;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import ua.org.dector.gcore.common.Settings;
 import ua.org.dector.gcore.game.TableScreen;
 import ua.org.dector.gcore.managers.SoundManager;
 import ua.org.dector.gcore.utils.ScreenMode;
 import ua.org.dector.space_lander.Lander;
 import ua.org.dector.space_lander.constants.Labels;
 import ua.org.dector.space_lander.constants.LanderSounds;
 
 import static ua.org.dector.space_lander.constants.UISizes.*;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class GraphicsOptionsScreen extends TableScreen<Lander> {
     private SelectBox sbxDisplayModes;
     private ScreenMode[] screenModes;
 
     private ScreenMode currentScreenMode;
     private boolean fullscreen;
 
     private TextButton btnSave;
     private TextButton btnBack;
 
     private boolean changed;
 
     public GraphicsOptionsScreen(Lander lander) {
         super(lander);
     }
 
     public void show() {
         final SoundManager soundManager = game.getSoundManager();
         final Settings settings = game.getSettings();
 
         Skin skin = game.getGraphics().getSkin();
         Table table = getTable();
 
         screenModes = ScreenMode.getAvailable();
         currentScreenMode = ScreenMode.getCurrent();
         fullscreen = settings.isFullscreen();
 
         sbxDisplayModes = new SelectBox(screenModes, skin);
         sbxDisplayModes.setSelection(currentScreenMode.toString());
         sbxDisplayModes.addListener(new ClickListener() {
             public void clicked(ActorEvent event, float x, float y) {
                 soundManager.play(LanderSounds.MENU_CLICK);
             }
         });
         sbxDisplayModes.addListener(new ChangeListener() {
             public void changed(ChangeEvent event, Actor actor) {
                 soundManager.play(LanderSounds.MENU_CLICK);
 
                 int modeSelected = sbxDisplayModes.getSelectionIndex();
                 currentScreenMode = screenModes[modeSelected];
 
                 setChanged();
             }
         });
 
         final CheckBox chkFullscreen = new CheckBox("", skin);
         chkFullscreen.setChecked(fullscreen);
         chkFullscreen.addListener(new ChangeListener() {
             public void changed(ChangeEvent event, Actor actor) {
                 soundManager.play(LanderSounds.MENU_CLICK);
 
                 fullscreen = chkFullscreen.isChecked();
 
                 setChanged();
             }
         });
 
         btnSave = new TextButton(Labels.OPTIONS$SAVE, skin);
         btnSave.setVisible(false);
         btnSave.addListener(new ClickListener() {
             public void clicked(ActorEvent event, float x, float y) {
                 soundManager.play(LanderSounds.MENU_CLICK);
 
                 settings.setScreenWidth(currentScreenMode.getWidth());
                 settings.setScreenHeight(currentScreenMode.getHeight());
                 settings.setFullscreen(fullscreen);

                 ScreenMode.setUp(currentScreenMode, fullscreen);
 
                 changed = false;
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
 
         table.defaults().spaceBottom(BOTTOM_SPACE);
         table.columnDefaults(3).padLeft(20);
         table.add(Labels.OPTIONS$GRAPHICS).spaceBottom(TITLE_BOTTOM_SPACE).colspan(4);
         table.row();
         table.add(Labels.OPTIONS$FULLSCREEN);
         table.add(chkFullscreen).colspan(2).left().uniform();
         table.row();
         table.add(Labels.OPTIONS$DISPLAY_MODE);
         table.add(sbxDisplayModes).colspan(2).left().uniform();
         table.row();
         table.add(btnSave).colspan(4).size(BUTTONS_WIDTH, BUTTONS_HEIGHT)
                 .fill().uniform();
         table.row();
         table.add(btnBack).colspan(4).size(BUTTONS_WIDTH, BUTTONS_HEIGHT)
                 .fill().uniform();
     }
 
     private void setChanged() {
         if (changed) return;
 
         changed = true;
         btnSave.setVisible(true);
         btnBack.setText(Labels.OPTIONS$CANCEL);
     }
 }
