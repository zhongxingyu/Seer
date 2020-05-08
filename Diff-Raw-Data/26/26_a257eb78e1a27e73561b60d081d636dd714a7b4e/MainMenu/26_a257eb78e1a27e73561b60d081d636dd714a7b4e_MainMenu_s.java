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
 import im.bci.jnuit.background.TexturedBackground;
 import im.bci.jnuit.NuitRenderer;
 import im.bci.jnuit.animation.IAnimation;
 import im.bci.jnuit.animation.PlayMode;
 import org.geekygoblin.nedetlesmaki.game.Game;
 import org.geekygoblin.nedetlesmaki.game.Group;
 import org.geekygoblin.nedetlesmaki.game.MainLoop;
 import im.bci.jnuit.lwjgl.assets.IAssets;
 import im.bci.jnuit.widgets.Container;
 import org.geekygoblin.nedetlesmaki.game.components.IngameControls;
 import org.geekygoblin.nedetlesmaki.game.components.Triggerable;
 import org.geekygoblin.nedetlesmaki.game.events.HideMenuTrigger;
 
 @Singleton
 public class MainMenu extends Component {
 
     private final Root root;
     private Container mainMenu;
     private VideoConfigurator videoConfigurator;
     private AudioConfigurator audioConfigurator;
     private Table optionsMenu;
     private Container extrasMenu;
     private ControlsConfigurator menuControls, gameControls;
     private final LevelSelector levelSelector;
     private final MainLoop mainLoop;
     private final NuitToolkit toolkit;
     private final NuitRenderer nuitRenderer;
     private final IAssets assets;
     private final Game game;
     private final Provider<HideMenuTrigger> hideMenuTrigger;
 
     @Inject
     public MainMenu(MainLoop mainLoop, Game g, NuitToolkit toolkit, NuitRenderer nuitRenderer, IAssets assets, LevelSelector levelSelector, Provider<HideMenuTrigger> hideMenuTrigger, IngameControls ingameControls, CutScenes cutscenes) throws LWJGLException {
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
         initMenuControls();
         initGameControls(ingameControls);
         initOptions();
         initMain();
         initExtras(cutscenes);
         root.show(mainMenu);
     }
 
     private void initVideo() throws LWJGLException {
         videoConfigurator = new VideoConfigurator(toolkit) {
             @Override
             protected void changeVideoSettings() {
                 super.changeVideoSettings();
                 assets.setIcon("icon.png");
             }
         };
         root.add(videoConfigurator);
     }
 
     private void initAudio() {
         audioConfigurator = new AudioConfigurator(toolkit);
     }
 
     private void initMain() {
 
        IAnimation buttonClassicBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("moyen_normal");
        IAnimation buttonClassicFocusedBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("moyen_survol");
        IAnimation buttonSmallBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("petit_normal");
        IAnimation buttonSmallFocusedBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("petit_survol");
 
         mainMenu = new Container();
         final Button startButton = new Button(toolkit, "main.menu.button.start") {
             @Override
             public void onOK() {
                 onStartGame();
             }
         };
         startButton.setX(800);
         startButton.setY(800);
         startButton.setWidth(317);
         startButton.setHeight(74);
         startButton.setBackground(new TexturedBackground(buttonClassicBackgroundAnimation.start(PlayMode.LOOP)));
         startButton.setFocusedBackground(new TexturedBackground(buttonClassicFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         mainMenu.add(startButton);
 
         final Button resumeButton = new Button(toolkit, "main.menu.button.resume") {
             @Override
             public void onOK() {
                 if (!game.getManager(GroupManager.class).getEntities(Group.LEVEL).isEmpty()) {
                     game.addEntity(game.createEntity().addComponent(new Triggerable(hideMenuTrigger.get())));
                 }
             }
         };
         resumeButton.setX(800);
         resumeButton.setY(900);
         resumeButton.setWidth(317);
         resumeButton.setHeight(74);
         resumeButton.setBackground(new TexturedBackground(buttonClassicBackgroundAnimation.start(PlayMode.LOOP)));
         resumeButton.setFocusedBackground(new TexturedBackground(buttonClassicFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         mainMenu.add(resumeButton);
 
         final Button optionsButton = new Button(toolkit, "main.menu.button.options") {
             @Override
             public void onOK() {
                 root.show(optionsMenu);
             }
         };
         optionsButton.setX(1200);
         optionsButton.setY(800);
         optionsButton.setWidth(317);
         optionsButton.setHeight(74);
         optionsButton.setBackground(new TexturedBackground(buttonClassicBackgroundAnimation.start(PlayMode.LOOP)));
         optionsButton.setFocusedBackground(new TexturedBackground(buttonClassicFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         mainMenu.add(optionsButton);
 
         final Button quitButton = new Button(toolkit, "main.menu.button.quit") {
             @Override
             public void onOK() {
                 mainLoop.setCloseRequested(true);
             }
         };
         quitButton.setX(1200);
         quitButton.setY(900);
         quitButton.setWidth(317);
         quitButton.setHeight(74);
         quitButton.setBackground(new TexturedBackground(buttonClassicBackgroundAnimation.start(PlayMode.LOOP)));
         quitButton.setFocusedBackground(new TexturedBackground(buttonClassicFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         mainMenu.add(quitButton);
 
         final Button extrasButton = new Button(toolkit, "main.menu.button.extras") {
             @Override
             public void onOK() {
                 root.show(extrasMenu);
             }
         };
         extrasButton.setX(1600);
         extrasButton.setY(1000);
         extrasButton.setWidth(230);
         extrasButton.setHeight(54);
         extrasButton.setBackground(new TexturedBackground(buttonSmallBackgroundAnimation.start(PlayMode.LOOP)));
         extrasButton.setFocusedBackground(new TexturedBackground(buttonSmallFocusedBackgroundAnimation.start(PlayMode.LOOP)));
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
         optionsMenu.cell(new Button(toolkit, "options.menu.button.game.controls") {
             @Override
             public void onOK() {
                 root.show(gameControls);
             }
         });
         optionsMenu.row();
         optionsMenu.cell(new Button(toolkit, "options.menu.button.menu.controls") {
             @Override
             public void onOK() {
                 root.show(menuControls);
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
 
     private void initMenuControls() {
         menuControls = new ControlsConfigurator(toolkit, Arrays.asList(toolkit.getMenuUp(), toolkit.getMenuDown(), toolkit.getMenuLeft(), toolkit.getMenuRight(), toolkit.getMenuOK(), toolkit.getMenuCancel()), null) {
             @Override
             public void onBack() {
                 root.show(optionsMenu);
             }
         };
         root.add(menuControls);
     }
 
     private void initGameControls(IngameControls ingameControls) {
         gameControls = new ControlsConfigurator(toolkit, Arrays.asList(ingameControls.getUp().getAction(), ingameControls.getDown().getAction(), ingameControls.getLeft().getAction(), ingameControls.getRight().getAction(), ingameControls.getRewind().getAction(), ingameControls.getShowMenu().getAction()), null) {
             @Override
             public void onBack() {
                 root.show(optionsMenu);
             }
         };
         root.add(gameControls);
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
 
     private void initExtras(CutScenes cutscenes) {
         extrasMenu = new ExtrasMenu(toolkit, root, mainMenu, assets, cutscenes);
         root.add(extrasMenu);
     }
 }
