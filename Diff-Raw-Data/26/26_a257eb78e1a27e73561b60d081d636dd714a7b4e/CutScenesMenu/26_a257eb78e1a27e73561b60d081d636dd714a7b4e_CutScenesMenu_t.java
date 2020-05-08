 /*
  The MIT License (MIT)
 
  Copyright (c) 2014 devnewton <devnewton@bci.im>
 
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
 
 import im.bci.jnuit.NuitToolkit;
 import im.bci.jnuit.animation.IAnimation;
 import im.bci.jnuit.animation.PlayMode;
 import im.bci.jnuit.background.TexturedBackground;
 import im.bci.jnuit.lwjgl.assets.IAssets;
 import im.bci.jnuit.widgets.Button;
 import im.bci.jnuit.widgets.Container;
 import im.bci.jnuit.widgets.Root;
 import im.bci.jnuit.widgets.Stack;
 import im.bci.jnuit.widgets.Widget;
 
 /**
  *
  * @author devnewton
  */
 public class CutScenesMenu extends Stack {
     
      final Container menu;
     CutScenesMenu(final NuitToolkit toolkit, final Root root, final Widget extrasMenu, final IAssets assets, final CutScenes cutScenes) {
         menu = new Container();
         menu.setBackground(new TexturedBackground(assets.getAnimations("background.png").getFirst().start(PlayMode.LOOP)));
        IAnimation buttonBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("1_normal");
        IAnimation buttonFocusedBackgroundAnimation = assets.getAnimations("menu_buttons.nanim.gz").getAnimationByName("1_survol");
 
         final Button cutscenesButton = new Button(toolkit, "cutscenes.menu.button.intro") {
             @Override
             public void onOK() {
                 CutScenesMenuDialog intro = new CutScenesMenuDialog(toolkit, assets);
                 cutScenes.createIntro(intro);
                 intro.setWidth(CutScenesMenu.this.getWidth());
                 intro.setHeight(CutScenesMenu.this.getHeight());
                 CutScenesMenu.this.show(intro);
             }
         };
         cutscenesButton.setX(110);
         cutscenesButton.setY(400);
         cutscenesButton.setWidth(400);
         cutscenesButton.setHeight(80);
         cutscenesButton.setBackground(new TexturedBackground(buttonBackgroundAnimation.start(PlayMode.LOOP)));
         cutscenesButton.setFocusedBackground(new TexturedBackground(buttonFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         cutscenesButton.setMustDrawFocus(false);
         menu.add(cutscenesButton);
         
         final Button creditsButton = new Button(toolkit, "cutscenes.menu.button.credits") {
             @Override
             public void onOK() {
                 CutScenesMenuDialog intro = new CutScenesMenuDialog(toolkit, assets);
                 cutScenes.createCredits(intro);
                 intro.setWidth(CutScenesMenu.this.getWidth());
                 intro.setHeight(CutScenesMenu.this.getHeight());
                 CutScenesMenu.this.show(intro);
             }
         };
         creditsButton.setX(110);
         creditsButton.setY(500);
         creditsButton.setWidth(400);
         creditsButton.setHeight(80);
         creditsButton.setBackground(new TexturedBackground(buttonBackgroundAnimation.start(PlayMode.LOOP)));
         creditsButton.setFocusedBackground(new TexturedBackground(buttonFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         creditsButton.setMustDrawFocus(false);
         menu.add(creditsButton);
 
         final Button backButton = new Button(toolkit, "cutscenes.menu.button.back") {
             @Override
             public void onOK() {
                 root.show(extrasMenu);
             }
         };
         backButton.setX(110);
         backButton.setY(700);
         backButton.setWidth(400);
         backButton.setHeight(80);
         backButton.setBackground(new TexturedBackground(buttonBackgroundAnimation.start(PlayMode.LOOP)));
         backButton.setFocusedBackground(new TexturedBackground(buttonFocusedBackgroundAnimation.start(PlayMode.LOOP)));
         backButton.setMustDrawFocus(false);
         menu.add(backButton);
         show(menu);
     }
     
     class CutScenesMenuDialog extends NedDialogue {
 
         public CutScenesMenuDialog(NuitToolkit toolkit, IAssets assets) {
             super(toolkit, assets);
         }
 
         @Override
         protected void onFinished() {
             CutScenesMenu.this.remove(this);
             CutScenesMenu.this.show(menu);
         }
         
     }
 
 }
