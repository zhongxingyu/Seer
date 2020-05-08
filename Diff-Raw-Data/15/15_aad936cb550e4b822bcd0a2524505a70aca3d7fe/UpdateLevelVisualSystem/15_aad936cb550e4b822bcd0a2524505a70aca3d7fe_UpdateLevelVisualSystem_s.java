 /*
  * Copyright © 2013, Pierre Marijon <pierre@marijon.fr>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
  * copies of the Software, and to permit persons to whom the Software is 
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in 
  * all copies or substantial portions of the Software.
  *
  * The Software is provided "as is", without warranty of any kind, express or 
  * implied, including but not limited to the warranties of merchantability, 
  * fitness for a particular purpose and noninfringement. In no event shall the 
  * authors or copyright holders X be liable for any claim, damages or other 
  * liability, whether in an action of contract, tort or otherwise, arising from,
  * out of or in connection with the software or the use or other dealings in the
  * Software.
  */
 package org.geekygoblin.nedetlesmaki.game.systems;
 
 import com.google.inject.Inject;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.VoidEntitySystem;
 
 import im.bci.jnuit.animation.IAnimationCollection;
 import im.bci.jnuit.animation.PlayMode;
 
 import org.geekygoblin.nedetlesmaki.game.manager.EntityIndexManager;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Plate;
 import org.geekygoblin.nedetlesmaki.game.components.visual.Sprite;
 import org.geekygoblin.nedetlesmaki.game.components.visual.SpritePuppetControls;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Position;
 import org.geekygoblin.nedetlesmaki.game.constants.AnimationType;
 import im.bci.jnuit.lwjgl.assets.IAssets;
 import org.geekygoblin.nedetlesmaki.game.utils.Mouvement;
 
 import org.lwjgl.util.vector.Vector3f;
 
 /**
  *
  * @author natir
  */
 public class UpdateLevelVisualSystem extends VoidEntitySystem {
 
     @Mapper
     ComponentMapper<Sprite> spriteMapper;
     @Mapper
     ComponentMapper<Plate> plateMapper;
     @Mapper
     ComponentMapper<Position> positionMapper;
     
     private final IAssets assets;
     private int nbIndexSaved;
     private final EntityIndexManager index;
 
     @Inject
     public UpdateLevelVisualSystem(IAssets assets, EntityIndexManager indexSystem) {
         this.assets = assets;
         this.nbIndexSaved = 0;
         this.index = indexSystem;
     }
 
     @Override
     protected void processSystem() {
 
         ArrayList<Mouvement> rm = index.getRemove();
         if (rm != null) {
             for (int i = 0; i != rm.size(); i++) {
                 for (int j = 0; j != rm.get(i).size(); j++) {
                     System.out.print("Remove move");
                     System.out.print(rm.get(i).getEntity());
                     System.out.printf(" position : %d %d, Animation : ", rm.get(i).getPosition(j).getX(), rm.get(i).getPosition(j).getY());
                     System.out.print(rm.get(i).getAnimation(j));
                     System.out.print("\n");
                     this.moveSprite(rm.get(i).getEntity(), rm.get(i).getPosition(j), rm.get(i).getAnimation(j), rm.get(i).getBeforeWait(j));
                 }
             }
             
             this.index.setRemove(null);
         }
 
         if (index.sizeOfStack() > nbIndexSaved) {
            nbIndexSaved = index.sizeOfStack();
             ArrayList<Mouvement> change = this.index.getChangement();
 
             if (change != null) {
                 for (int i = 0; i != change.size(); i++) {
                     for (int j = 0; j != change.get(i).size(); j++) {
                         System.out.print("Normal move");
                         System.out.print(change.get(i).getEntity());
                         System.out.printf(" position : %d %d, Animation : ", change.get(i).getPosition(j).getX(), change.get(i).getPosition(j).getY());
                         System.out.print(change.get(i).getAnimation(j));
                         System.out.print("\n");
                         this.moveSprite(change.get(i).getEntity(), change.get(i).getPosition(j), change.get(i).getAnimation(j), change.get(i).getBeforeWait(j));
                     }
                 }
             }
        } 
     }
 
     private void moveSprite(Entity e, Position diff, AnimationType a, float waitBefore) {
 
         Sprite sprite = e.getComponent(Sprite.class);
         Vector3f pos = sprite.getPosition();
         SpritePuppetControls updatable = new SpritePuppetControls(sprite);
         Position current = positionMapper.getSafe(e);
         
         if(current == null) {
             return ;
         }
         
         Position p = new Position((int)pos.x + diff.getY(), (int)pos.y + diff.getX());
 
         IAnimationCollection nedAnim = this.assets.getAnimations("ned.nanim.gz");
         IAnimationCollection makiAnim = this.assets.getAnimations("maki.nanim.gz");
         IAnimationCollection plateAnim = this.assets.getAnimations("plate.nanim.gz");
         IAnimationCollection boxAnim = this.assets.getAnimations("box.nanim.gz");
         IAnimationCollection stairsAnim = this.assets.getAnimations("stairs.nanim.gz");
 
         if (a == AnimationType.no) {
             updatable.moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_right) {
             updatable.startAnimation(nedAnim.getAnimationByName("walk_right"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_left) {
             updatable.startAnimation(nedAnim.getAnimationByName("walk_left"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_down) {
             updatable.startAnimation(nedAnim.getAnimationByName("walk_down"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_up) {
             updatable.startAnimation(nedAnim.getAnimationByName("walk_up"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_push_right) {
             updatable.startAnimation(nedAnim.getAnimationByName("push_right"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_push_left) {
             updatable.startAnimation(nedAnim.getAnimationByName("push_left"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_push_down) {
             updatable.startAnimation(nedAnim.getAnimationByName("push_down"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_push_up) {
             updatable.startAnimation(nedAnim.getAnimationByName("push_up"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.box_destroy) {
             updatable.waitDuring(waitBefore)
                     .startAnimation(boxAnim.getAnimationByName("destroy"))
                     .waitAnimation();
             e.removeComponent(sprite);
         } else if (a == AnimationType.maki_green_one) {
             updatable.moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .startAnimation(makiAnim.getAnimationByName("maki_green_one"))
                     .waitAnimation();
         } else if (a == AnimationType.maki_orange_one) {
             updatable.moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .startAnimation(makiAnim.getAnimationByName("maki_orange_one"))
                     .stopAnimation();
         } else if (a == AnimationType.maki_blue_one) {
             updatable.moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .startAnimation(makiAnim.getAnimationByName("maki_blue_one"))
                     .stopAnimation();
         } else if (a == AnimationType.maki_green_out) {
             updatable.moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .startAnimation(makiAnim.getAnimationByName("maki_green_out"))
                     .stopAnimation();
         } else if (a == AnimationType.maki_orange_out) {
             updatable.moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .startAnimation(makiAnim.getAnimationByName("maki_orange_out"))
                     .stopAnimation();
         } else if (a == AnimationType.maki_blue_out) {
             updatable.moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .startAnimation(makiAnim.getAnimationByName("maki_blue_out"))
                     .stopAnimation();
         } else if (a == AnimationType.clean_green_plate) {
             updatable.startAnimation(plateAnim.getAnimationByName("clean_green_plate"))
                     .stopAnimation();
         } else if (a == AnimationType.clean_orange_plate) {
             updatable.startAnimation(plateAnim.getAnimationByName("clean_orange_plate"))
                     .stopAnimation();
         } else if (a == AnimationType.maki_blue_out) {
             updatable.startAnimation(plateAnim.getAnimationByName("clean_blue_plate"))
                     .stopAnimation();
         } else if (a == AnimationType.disable_entity) {
             e.disable();
         } else if (a == AnimationType.stairs_up) {
             updatable.startAnimation(stairsAnim.getAnimationByName("stairs_up"), PlayMode.ONCE)
                     .waitAnimation();
         } else if (a == AnimationType.stairs_down) {
             updatable.startAnimation(stairsAnim.getAnimationByName("stairs_down"), PlayMode.ONCE)
                     .waitAnimation();
         } else if (a == AnimationType.stairs_left) {
             updatable.startAnimation(stairsAnim.getAnimationByName("stairs_left"), PlayMode.ONCE)
                     .waitAnimation();
         } else if (a == AnimationType.stairs_right) {
             updatable.startAnimation(stairsAnim.getAnimationByName("stairs_right"), PlayMode.ONCE)
                     .waitAnimation();
         }
 
         e.addComponent(updatable);
         e.changedInWorld();
     }
 }
