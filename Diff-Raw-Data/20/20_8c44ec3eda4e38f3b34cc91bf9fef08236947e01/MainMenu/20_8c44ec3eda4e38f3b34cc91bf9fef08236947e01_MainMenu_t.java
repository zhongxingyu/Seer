 /*
  The MIT License (MIT)
 
  Copyright (c) 2013 devnewton <devnewton@bci.im>
 
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
 
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
 
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
  */
 package org.geekygoblin.nedetlesmaki.game.components.ui;
 
 import com.artemis.Component;
 import com.artemis.managers.GroupManager;
 import im.bci.jnuit.NuitToolkit;
 import java.util.Arrays;
 
 import org.lwjgl.LWJGLException;
 
 import im.bci.jnuit.widgets.AudioConfigurator;
 import im.bci.jnuit.widgets.Button;
 import im.bci.jnuit.widgets.ControlsConfigurator;
 import im.bci.jnuit.widgets.Root;
 import im.bci.jnuit.widgets.Table;
 import im.bci.jnuit.widgets.VideoConfigurator;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import im.bci.jnuit.background.Background;
 import im.bci.jnuit.background.TexturedBackground;
 import im.bci.jnuit.NuitRenderer;
 import im.bci.jnuit.animation.PlayMode;
 import im.bci.jnuit.background.ColoredBackground;
 import org.geekygoblin.nedetlesmaki.game.Game;
 import org.geekygoblin.nedetlesmaki.game.Group;
 import org.geekygoblin.nedetlesmaki.game.MainLoop;
 import im.bci.jnuit.lwjgl.assets.IAssets;
 import im.bci.jnuit.widgets.Container;
 import org.geekygoblin.nedetlesmaki.game.components.Triggerable;
 import org.geekygoblin.nedetlesmaki.game.events.HideMenuTrigger;
 
 @Singleton
 public class MainMenu extends Component {
 
     private final Root root;
     private Container mainMenu;
     private VideoConfigurator videoConfigurator;
     private AudioConfigurator audioConfigurator;
     private Table optionsMenu;
     private ControlsConfigurator controls;
     private final LevelSelector levelSelector;
     private final MainLoop mainLoop;
     private final NuitToolkit toolkit;
     private final NuitRenderer nuitRenderer;
     private final IAssets assets;
     private final Game game;
     private final Provider<HideMenuTrigger> hideMenuTrigger;
 
     @Inject
     public MainMenu(MainLoop mainLoop, Game g, NuitToolkit toolkit, NuitRenderer nuitRenderer, IAssets assets, LevelSelector levelSelector, Provider<HideMenuTrigger> hideMenuTrigger) throws LWJGLException {
         this.mainLoop = mainLoop;
         this.toolkit = toolkit;
         this.nuitRenderer = nuitRenderer;
         this.assets = assets;
         this.game = g;
         this.hideMenuTrigger = hideMenuTrigger;
        root = new Root(toolkit);
        root.setBackground(new TexturedBackground(MainMenu.this.assets.getAnimations("menu.png").getFirst().start(PlayMode.LOOP)));
         this.levelSelector = levelSelector;
         root.add(levelSelector);
         initVideo();
         initAudio();
         initControls();
         initOptions();
         initMain();
     }
 
     private void initVideo() throws LWJGLException {
         videoConfigurator = new VideoConfigurator(toolkit) {
 
             @Override
             protected void changeVideoSettings() {
                 super.changeVideoSettings();
                 assets.setIcon("icon.png");
             }
 
             @Override
             protected void closeVideoSettings() {
                 root.show(optionsMenu);
             }
         };
         root.add(videoConfigurator);
     }
 
     private void initAudio() {
         audioConfigurator = new AudioConfigurator(toolkit) {
             @Override
             protected void closeAudioSettings() {
                 root.show(optionsMenu);
             }
         };
     }
 
     private void initMain() {
         mainMenu = new Table(toolkit);
         final Button startButton = new Button(toolkit, "main.menu.button.start") {
             @Override
             public void onOK() {
                 onStartGame();
             }
         };
         startButton.setX(961);
         startButton.setY(556);
         startButton.setWidth(1265 - 961);
         startButton.setHeight(620 - 556);
         mainMenu.add(startButton);
 
         final Button resumeButton = new Button(toolkit, "main.menu.button.resume") {
             @Override
             public void onOK() {
                if(!game.getManager(GroupManager.class).getEntities(Group.LEVEL).isEmpty()) {
                    game.addEntity(game.createEntity().addComponent(new Triggerable(hideMenuTrigger.get())));
                }
             }
         };
         resumeButton.setX(559);
         resumeButton.setY(607);
         resumeButton.setWidth(866 - 559);
         resumeButton.setHeight(670 - 607);
         mainMenu.add(resumeButton);
 
         final Button optionsButton = new Button(toolkit, "main.menu.button.options") {
             @Override
             public void onOK() {
                 root.show(optionsMenu);
             }
         };
         optionsButton.setX(668);
         optionsButton.setY(695);
         optionsButton.setWidth(907 - 668);
         optionsButton.setHeight(755 - 695);
         mainMenu.add(optionsButton);
 
         final Button quitButton = new Button(toolkit, "main.menu.button.quit") {
             @Override
             public void onOK() {
                 mainLoop.setCloseRequested(true);
             }
         };
         quitButton.setX(887);
         quitButton.setY(631);
         quitButton.setWidth(1160 - 887);
         quitButton.setHeight(691 - 631);
         mainMenu.add(quitButton);
         
         final Button extrasButton = new Button(toolkit, "main.menu.button.extras") {
             @Override
             public void onOK() {
             }
         };
         extrasButton.setX(1018);
         extrasButton.setY(720);
         extrasButton.setWidth(1238 - 1018);
         extrasButton.setHeight(766 - 720);
         mainMenu.add(extrasButton);
         root.add(mainMenu);
     }
 
     private void initOptions() {
         optionsMenu = new Table(toolkit);
         optionsMenu.defaults().expand();
         optionsMenu.cell(new Button(toolkit, "options.menu.button.video") {
             @Override
             public void onOK() {
                 root.show(videoConfigurator);
             }
         });
         optionsMenu.row();
         optionsMenu.cell(new Button(toolkit, "options.menu.button.audio") {
             @Override
             public void onOK() {
                 root.show(audioConfigurator);
             }
         });
         optionsMenu.row();
         optionsMenu.cell(new Button(toolkit, "options.menu.button.controls") {
             @Override
             public void onOK() {
                 root.show(controls);
             }
         });
         optionsMenu.row();
         optionsMenu.cell(new Button(toolkit, "options.menu.button.back") {
             @Override
             public void onOK() {
                 root.show(mainMenu);
             }
         });
         optionsMenu.row();
         root.add(optionsMenu);
     }
 
     private void initControls() {
         controls = new ControlsConfigurator(toolkit, Arrays.asList(toolkit.getMenuUp(), toolkit.getMenuDown(), toolkit.getMenuLeft(), toolkit.getMenuRight(), toolkit.getMenuOK(), toolkit.getMenuCancel()), null) {
             @Override
             public void onBack() {
                 root.show(optionsMenu);
             }
         };
         root.add(controls);
     }
 
     public void update() {
         root.update(game.getDelta());
     }
 
     public void draw() {
         nuitRenderer.render(root);
     }
 
     private void onStartGame() {
         root.show(levelSelector);
     }
 
     public void show() {
         root.show(mainMenu);
     }
 
     public void showLevelMenu() {
         root.show(levelSelector);
     }
 }
