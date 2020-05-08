 package com.ahsgaming.valleyofbones.screens.panels;
 
 import com.ahsgaming.valleyofbones.TextureManager;
 import com.ahsgaming.valleyofbones.VOBGame;
 import com.ahsgaming.valleyofbones.screens.LevelScreen;
 import com.ahsgaming.valleyofbones.units.Prototypes;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Array;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jami
  * Date: 4/29/13
  * Time: 10:25 AM
  * To change this template use File | Settings | File Templates.
  */
 public class BuildPanel extends Panel {
     public static final String LOG = "BuildPanel";
 
     InfoBox tooltip;
 
     public BuildPanel(VOBGame game, LevelScreen levelScreen, String icon, Skin skin) {
         super(game, levelScreen, icon, skin);
 
         tooltip = new InfoBox(null, skin);
     }
 
     @Override
     public void update(float delta) {
         super.update(delta);
 
         // need to get all the buildings this guy can build
         Array<Prototypes.JsonProto> items = Prototypes.getPlayerCanBuild(game.getPlayer(), game.getNetController().getGameController());
         if (!items.equals(this.items)) {
             this.items = items;
             dirty = true;
         }
 

     }
 
     @Override
     public void rebuild() {
         super.rebuild();
 
 
         int x = 0, i = 0;
         int spacing = 4;
         for (Prototypes.JsonProto jp: items) {
             final Image btn;
             Sprite sp = TextureManager.getSpriteFromAtlas("assets", jp.image);
             btn = new Image(sp);
             this.addActor(btn);
             btn.setX(x);
             x += btn.getWidth() + spacing;
            if (!game.getPlayer().canBuild(jp.id, game.getNetController().getGameController()))
                btn.setColor(0.8f, 0.4f, 0.4f, 1.0f);
             buttonMap.put(btn, jp);
             btn.addListener(new ClickListener() {
                 @Override
                 public void clicked(InputEvent event, float x, float y) {
                     super.clicked(event, x, y);
 
                     buttonClicked(btn);
                 }
 
                 @Override
                 public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                     super.enter(event, x, y, pointer, fromActor);
 
                     entered(btn);
                 }
 
                 @Override
                 public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                     super.exit(event, x, y, pointer, toActor);
 
                     exited(btn);
                 }
             });
 
             Label hotkey = new Label(" ", skin, "small");
             hotkey.setPosition(btn.getX(), btn.getY());
             addActor(hotkey);
             if (jp.id.equals("marine-base"))
                 hotkey.setText("A");
             else if (jp.id.equals("saboteur-base"))
                 hotkey.setText("S");
             else if (jp.id.equals("tank-base"))
                 hotkey.setText("D");
             else if (jp.id.equals("sniper-base"))
                 hotkey.setText("F");
 
             i++;
         }
 
         icon.setPosition(x, 0);
         this.setWidth(x + icon.getWidth());
 
         if (expanded) setPosition(0, getY()); else setPosition(-1 * icon.getX(), getY());
     }
 
     @Override
     public boolean buttonClicked(Image button) {
         if (super.buttonClicked(button)) return true;
 
         Prototypes.JsonProto jp = buttonMap.get(button);
 
         levelScreen.setBuildMode(jp);
         return true;
     }
 
     public void entered(Image button) {
         if (!buttonMap.containsKey(button)) return;
 
         Prototypes.JsonProto jp = buttonMap.get(button);
         tooltip.setProto(jp);
         addActor(tooltip);
         tooltip.setPosition(button.getX(), button.getTop());
     }
 
     public void exited(Image button) {
         tooltip.remove();
     }
 }
