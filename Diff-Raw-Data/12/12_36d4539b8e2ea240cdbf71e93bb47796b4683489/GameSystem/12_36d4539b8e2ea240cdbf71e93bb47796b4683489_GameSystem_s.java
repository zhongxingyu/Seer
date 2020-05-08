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
 import com.google.inject.Singleton;
 
 import java.util.ArrayList;
 
 import com.artemis.Entity;
 import com.artemis.systems.VoidEntitySystem;
 import com.artemis.utils.ImmutableBag;
 import com.google.inject.Provider;
 
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Color;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Pushable;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Pusher;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Position;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Square;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Plate;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Stairs;
 import org.geekygoblin.nedetlesmaki.game.manager.EntityIndexManager;
 import org.geekygoblin.nedetlesmaki.game.utils.PosOperation;
 import org.geekygoblin.nedetlesmaki.game.utils.Mouvement;
 import org.geekygoblin.nedetlesmaki.game.constants.AnimationType;
 import org.geekygoblin.nedetlesmaki.game.Game;
 import org.geekygoblin.nedetlesmaki.game.components.Triggerable;
 import org.geekygoblin.nedetlesmaki.game.constants.ColorType;
 import org.geekygoblin.nedetlesmaki.game.events.ShowLevelMenuTrigger;
 
 /**
  *
  * @author natir
  */
 @Singleton
 public class GameSystem extends VoidEntitySystem {
 
     private final Provider<ShowLevelMenuTrigger> showLevelMenuTrigger;
     private final EntityIndexManager index;
     private boolean run;
 
     @Inject
     public GameSystem(EntityIndexManager index, Provider<ShowLevelMenuTrigger> showLevelMenuTrigger) {
         this.showLevelMenuTrigger = showLevelMenuTrigger;
         this.index = index;
         this.run = false;
     }
 
     @Override
     protected void processSystem() {
         if (this.run) {
             this.tryPlate();
         }
     }
 
     public ArrayList<Mouvement> moveEntity(Entity e, Position dirP, float baseBefore) {
         this.run = true;
 
         Position oldP = this.index.getPosition(e);
 
         ArrayList<Mouvement> mouv = new ArrayList();
 
         for (int i = 0; i != this.index.getMovable(e); i++) {
             float animTime = this.calculateAnimationTime(0.5f, i);
             Position newP = PosOperation.sum(oldP, dirP);
 
             if (i > this.index.getBoost(e) - 1) {
                 e.getComponent(Pusher.class).setPusher(true);
             }
 
             if (this.index.positionIsVoid(newP)) {
                 Square s = index.getSquare(newP.getX(), newP.getY());
                 if (this.testStopOnPlate(e, s)) {
                     mouv.addAll(this.runValideMove(oldP, newP, e, false, baseBefore, animTime, i, this.index.isBoosted(e)));
 
                     if (this.index.getBoost(e) != 20) {
                         e.getComponent(Pusher.class).setPusher(false);
                     }
 
                     return mouv;
                 }
                 if (!this.testBlockedPlate(e, s)) {
                     mouv.addAll(runValideMove(oldP, newP, e, false, baseBefore, animTime, i, this.index.isBoosted(e)));
                 }
             } else {
                 if (this.index.isStairs(newP)) {
                     mouv.addAll(nedMoveOnStairs(oldP, newP, e, animTime));
                     if (!mouv.isEmpty()) {
                         this.endOfLevel();
                     }
                 }
                 if (this.index.isPusherEntity(e)) {
                     ArrayList<Entity> aNextE = index.getSquare(newP.getX(), newP.getY()).getWith(Pushable.class);
                     if (!aNextE.isEmpty()) {
                         Entity nextE = aNextE.get(0);
                         if (this.index.isPushableEntity(nextE)) {
                             if (this.index.isDestroyer(e)) {
                                 if (this.index.isDestroyable(nextE)) {
                                     mouv.addAll(destroyMove(nextE, dirP, this.beforeTime(0.5f, i), animTime));
                                     mouv.addAll(runValideMove(oldP, newP, e, false, baseBefore, animTime, i, this.index.isBoosted(e)));
                                 } else {
                                     ArrayList<Mouvement> recMouv = this.moveEntity(nextE, dirP, this.beforeTime(0.5f, i));
                                     if (!recMouv.isEmpty()) {
                                         mouv.addAll(recMouv);
                                         mouv.addAll(runValideMove(oldP, newP, e, true, baseBefore, animTime, i, this.index.isBoosted(e)));
                                     }
                                 }
                             } else {
                                 ArrayList<Mouvement> recMouv = this.moveEntity(nextE, dirP, 0);
                                 if (!recMouv.isEmpty()) {
                                     mouv.addAll(recMouv);
                                     mouv.addAll(runValideMove(oldP, newP, e, true, baseBefore, animTime, i, this.index.isBoosted(e)));
                                 }
                             }
                         }
                     }
                 }
 
                mouv.add(new Mouvement(e).setAnimation(this.getValideAnimation(this.index.isBoosted(e), -1, dirP)).saveMouvement());
                 
                 if (this.index.getBoost(e) != 20) {
                     e.getComponent(Pusher.class).setPusher(false);
                 }
 
                 return mouv;
             }
 
             baseBefore = 0;
         }
         
        mouv.add(new Mouvement(e).setAnimation(this.getValideAnimation(this.index.isBoosted(e), -1, dirP)).saveMouvement());
        return mouv;
     }
 
     private ArrayList<Mouvement> runValideMove(Position oldP, Position newP, Entity e, boolean push, float bw, float aT, int pas, boolean boosted) {
         Position diff = PosOperation.deduction(newP, oldP);
 
         ArrayList<Mouvement> m = new ArrayList();
 
         if (index.moveEntity(oldP.getX(), oldP.getY(), newP.getX(), newP.getY())) {
             if (e == ((Game) this.world).getNed()) {
                 if (diff.getX() > 0) {
                     if (push) {
                         m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_push_right).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
                     } else {
                         m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_right).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
                     }
                 } else if (diff.getX() < 0) {
                     if (push) {
                         m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_push_left).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
                     } else {
                         m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_left).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
                     }
                 } else if (diff.getY() > 0) {
                     if (push) {
                         m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_push_down).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
                     } else {
                         m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_down).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
                     }
                 } else if (diff.getY() < 0) {
                     if (push) {
                         m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_push_up).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
                     } else {
                         m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_up).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
                     }
                 } else {
                     m.add(new Mouvement(e).setPosition(diff).setAnimation(this.getValideAnimation(boosted, pas, diff)).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
                 }
             } else {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(this.getValideAnimation(boosted, pas, diff)).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
             }
 
             if (makiMoveOnePlate(newP, e)) {
                 m.addAll(makiPlateMove(oldP, newP, e, true, aT));
             }
 
             if (makiMoveOutPlate(oldP, e)) {
                 m.addAll(makiPlateMove(oldP, newP, e, false, aT));
 
                 if (makiMoveOnePlate(newP, e)) {
                     m.addAll(makiPlateMove(oldP, newP, e, true, aT));
                 }
             }
 
             e.getComponent(Position.class).setX(newP.getX());
             e.getComponent(Position.class).setY(newP.getY());
 
             return m;
         }
 
         return m;
     }
 
     private ArrayList<Mouvement> nedMoveOnStairs(Position oldP, Position newP, Entity e, float aT) {
         Position diff = PosOperation.deduction(newP, oldP);
 
         ArrayList<Mouvement> m = new ArrayList();
 
         if (e == ((Game) this.world).getNed()) {
             if (diff.getX() > 0) {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_right).setAnimationTime(aT).saveMouvement());
             } else if (diff.getX() < 0) {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_left).setAnimationTime(aT).saveMouvement());
             } else if (diff.getY() > 0) {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_down).setAnimationTime(aT).saveMouvement());
             } else if (diff.getY() < 0) {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_up).setAnimationTime(aT).saveMouvement());
             } else {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.no).setAnimationTime(aT).saveMouvement());
             }
         } else {
             if (m.isEmpty()) {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.no).setAnimationTime(aT).saveMouvement());
             }
         }
 
         e.getComponent(Position.class).setX(newP.getX());
         e.getComponent(Position.class).setY(newP.getY());
 
         return m;
     }
 
     private ArrayList<Mouvement> makiPlateMove(Position oldP, Position newP, Entity e, boolean getOne, float aT) {
         ArrayList<Mouvement> m = new ArrayList();
 
         Square obj;
 
         if (getOne) {
             obj = index.getSquare(newP.getX(), newP.getY());
         } else {
             obj = index.getSquare(oldP.getX(), oldP.getY());
         }
 
         if (obj == null) {
             return m;
 
         }
 
         ArrayList<Entity> plates = obj.getWith(Plate.class);
 
         if (plates.isEmpty()) {
             return m;
         }
 
         Entity plate = plates.get(0);
 
         Color plateC = this.index.getColor(plate);
         Color makiC = this.index.getColor(e);
 
         Position diff = PosOperation.deduction(newP, oldP);
 
         if (plateC.getColor() == makiC.getColor()) {
             if (plateC.getColor() == ColorType.green) {
                 if (getOne) {
                     m.add(new Mouvement(e).setPosition(new Position(0, 0)).setAnimation(AnimationType.maki_green_one).setAnimationTime(aT).saveMouvement());
                 } else {
                     m.add(new Mouvement(e).setPosition(new Position(0, 0)).setAnimation(AnimationType.maki_green_out).setAnimationTime(aT).saveMouvement());
                 }
             } else if (plateC.getColor() == ColorType.orange) {
                 if (getOne) {
                     m.add(new Mouvement(e).setPosition(new Position(0, 0)).setAnimation(AnimationType.maki_orange_one).setAnimationTime(aT).saveMouvement());
                 } else {
                     m.add(new Mouvement(e).setPosition(new Position(0, 0)).setAnimation(AnimationType.maki_orange_out).setAnimationTime(aT).saveMouvement());
                 }
             } else if (plateC.getColor() == ColorType.blue) {
                 if (getOne) {
                     m.add(new Mouvement(e).setPosition(new Position(0, 0)).setAnimation(AnimationType.maki_blue_one).setAnimationTime(aT).saveMouvement());
                 } else {
                     m.add(new Mouvement(e).setPosition(new Position(0, 0)).setAnimation(AnimationType.maki_blue_out).setAnimationTime(aT).saveMouvement());
                 }
             }
         }
 
         return m;
     }
 
     public ArrayList<Mouvement> destroyMove(Entity e, Position diff, float bT, float aT) {
         ArrayList<Mouvement> preM = this.moveEntity(e, diff, bT);
 
         if (preM.isEmpty()) {
             preM.add(new Mouvement(e).setPosition(new Position(0, 0)).setAnimation(AnimationType.box_destroy).setBeforeWait(bT).setAnimationTime(aT).saveMouvement());
         } else {
             preM.add(new Mouvement(e).setPosition(new Position(0, 0)).setAnimation(AnimationType.box_destroy).setAnimationTime(aT).saveMouvement());
         }
 
         return preM;
     }
 
     private AnimationType getValideAnimation(boolean boosted, int pas, Position diff) {
 
         if (boosted) {
             if (diff.getX() > 0) {
                 if(pas < 0) {
                     return AnimationType.boost_stop_right;
                 } else if(pas == 3) {
                     return AnimationType.boost_start_right;
                 } else if(pas > 3) {
                     return AnimationType.boost_loop_right;
                 }
             } else if (diff.getX() < 0) {
                 if(pas < 0) {
                     return AnimationType.boost_stop_left;
                 } else if(pas == 3) {
                     return AnimationType.boost_start_left;
                 } else if(pas > 3) {
                     return AnimationType.boost_loop_left;
                 }
             } else if (diff.getY() > 0) {
                  if(pas < 0) {
                     return AnimationType.boost_stop_down;
                 } else if(pas == 3) {
                     return AnimationType.boost_start_down;
                 } else if(pas > 3) {
                     return AnimationType.boost_loop_down;
                 }
             } else if (diff.getY() < 0) {
                     if(pas < 0) {
                     return AnimationType.boost_stop_up;
                 } else if(pas == 3) {
                     return AnimationType.boost_start_up;
                 } else if(pas > 3) {
                     return AnimationType.boost_loop_up;
                 }
             }
         }
         
         return AnimationType.no;
     }
 
     public boolean makiMoveOnePlate(Position newP, Entity e) {
         Square s = this.index.getSquare(newP.getX(), newP.getY());
 
         if (s == null) {
             return false;
         }
 
         ArrayList<Entity> plates = s.getWith(Plate.class);
 
         if (plates.isEmpty()) {
             return false;
         }
 
         if (this.index.getColor(e) == null) {
             return false;
         }
 
         if (this.index.getColorType(e) == this.index.getColorType(plates.get(0))) {
             plates.get(0).getComponent(Plate.class).setMaki(true);
             return true;
         }
 
         return false;
     }
 
     public boolean makiMoveOutPlate(Position oldP, Entity e) {
         Square s = this.index.getSquare(oldP.getX(), oldP.getY());
 
         if (s == null) {
             return false;
         }
 
         ArrayList<Entity> plates = s.getWith(Plate.class);
         if (plates.isEmpty()) {
             return false;
         }
 
         if (this.index.getColor(e) == null) {
             return false;
         }
 
         if (this.index.getColorType(e) == this.index.getColorType(plates.get(0))) {
             plates.get(0).getComponent(Plate.class).setMaki(false);
             return true;
         }
 
         return false;
     }
 
     private boolean testStopOnPlate(Entity eMove, Square obj) {
         if (obj == null) {
             return false;
 
         }
 
         ArrayList<Entity> array = obj.getWith(Plate.class);
 
         if (array.isEmpty()) {
             return false;
         }
 
         Entity plate = obj.getWith(Plate.class).get(0);
         Plate p = this.index.getPlate(plate);
         boolean block = this.index.stopOnPlate(eMove);
 
         if (!block) {
             return false;
         }
 
         if (p.isPlate()) {
             if (block) {
                 if (this.index.getColorType(plate) == this.index.getColorType(eMove) && this.index.getColorType(eMove) != ColorType.orange) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     private boolean testBlockedPlate(Entity eMove, Square obj) {
         if (obj == null) {
             return false;
 
         }
 
         ArrayList<Entity> array = obj.getWith(Plate.class);
 
         if (array.isEmpty()) {
             return false;
         }
 
         Entity plate = obj.getWith(Plate.class).get(0);
         Plate p = plate.getComponent(Plate.class);
         boolean block = this.index.isBlockOnPlate(eMove);
 
         if (!block) {
             return false;
         }
 
         if (p.isPlate()) {
             if (block) {
                 return true;
             }
         }
 
         return false;
     }
 
     private void tryPlate() {
         ImmutableBag<Entity> plateGroup = this.index.getAllPlate();
 
         ImmutableBag<Entity> stairsGroup = this.index.getAllStairs();
         Entity stairs = stairsGroup.get(0);
         Stairs stairsS = this.index.getStairs(stairs);
 
         if (stairsS.isOpen()) {
             return;
         }
 
         for (int i = 0; i != plateGroup.size(); i++) {
             Entity plateE = plateGroup.get(i);
 
             Plate plate = this.index.getPlate(plateE);
 
             if (!plate.haveMaki()) {
                 return;
             }
         }
 
         stairsS.setStairs(true);
 
         ArrayList<Mouvement> tmpm = new ArrayList();
 
         switch (stairsS.getDir()) {
             case 1:
                 tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_up).setAnimationTime(0.5f).saveMouvement());
                 break;
             case 2:
                 tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_down).setAnimationTime(0.5f).saveMouvement());
                 break;
             case 3:
                 tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_left).setAnimationTime(0.5f).saveMouvement());
                 break;
             case 4:
                 tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_right).setAnimationTime(0.5f).saveMouvement());
                 break;
             default:
                 tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_up).setAnimationTime(0.5f).saveMouvement());
         }
 
         this.index.addMouvement(tmpm);
     }
 
     private void endOfLevel() {
         ImmutableBag<Entity> stairsGroup = this.index.getAllStairs();
 
         Entity ned = this.index.getNed();
         Position nedP = this.index.getPosition(ned);
 
         Entity stairs = stairsGroup.get(0);
         Position stairsP = this.index.getPosition(stairs);
         Stairs stairsS = this.index.getStairs(stairs);
 
         if (stairsS.isOpen() && PosOperation.equale(nedP, stairsP)) {
             if (world.getSystem(SpritePuppetControlSystem.class).getActives().isEmpty()) {
                 world.addEntity(world.createEntity().addComponent(new Triggerable(showLevelMenuTrigger.get())));
 
                 this.run = false;
             }
         }
     }
 
     public void removeMouv() {
         ArrayList<Mouvement> head = this.index.pop();
 
         if (head == null) {
             return;
         }
 
         ArrayList<Mouvement> rm = new ArrayList<>();
 
         for (int i = 0; i != head.size(); i++) {
             for (int j = 0; j != head.get(i).size(); j++) {
                 Position headP = head.get(i).getPosition(j);
 
                 if (headP == null) {
                     return;
                 }
 
                 Position diff = PosOperation.deduction(new Position(0, 0), headP);
                 Position current = this.index.getPosition(head.get(i).getEntity());
 
                 if (current == null) {
                     return;
                 }
 
                 AnimationType invertAnim = this.invertAnimation(head.get(i).getAnimation(j));
 
                 rm.add(new Mouvement(head.get(i).getEntity()).setAnimation(invertAnim).setPosition(diff).setAnimationTime(head.get(i).getAnimationTime(j)).saveMouvement());
 
                 this.index.moveEntity(current.getX(), current.getY(), current.getX() + diff.getX(), current.getY() + diff.getY());
                 head.get(i).getEntity().getComponent(Position.class).setX(current.getX() + diff.getX());
                 head.get(i).getEntity().getComponent(Position.class).setY(current.getY() + diff.getY());
             }
         }
 
         this.index.setRemove(rm);
     }
 
     private AnimationType invertAnimation(AnimationType base) {
         if (base == AnimationType.no) {
             return AnimationType.no;
         } else if (base == AnimationType.ned_right) {
             return AnimationType.ned_right;
         } else if (base == AnimationType.ned_left) {
             return AnimationType.ned_left;
         } else if (base == AnimationType.ned_down) {
             return AnimationType.ned_down;
         } else if (base == AnimationType.ned_up) {
             return AnimationType.ned_up;
         } else if (base == AnimationType.ned_push_right) {
             return AnimationType.ned_push_right;
         } else if (base == AnimationType.ned_push_left) {
             return AnimationType.ned_push_left;
         } else if (base == AnimationType.ned_push_down) {
             return AnimationType.ned_push_down;
         } else if (base == AnimationType.ned_push_up) {
             return AnimationType.ned_push_up;
         } else if (base == AnimationType.box_destroy) {
             return AnimationType.box_create;
         } else if (base == AnimationType.box_create) {
             return AnimationType.box_destroy;
         } else if (base == AnimationType.maki_green_one) {
             return AnimationType.maki_green_out;
         } else if (base == AnimationType.maki_orange_one) {
             return AnimationType.maki_orange_out;
         } else if (base == AnimationType.maki_blue_one) {
             return AnimationType.maki_blue_out;
         } else if (base == AnimationType.maki_green_out) {
             return AnimationType.maki_green_one;
         } else if (base == AnimationType.maki_orange_out) {
             return AnimationType.maki_orange_one;
         } else if (base == AnimationType.maki_blue_out) {
             return AnimationType.maki_blue_one;
         } else if (base == AnimationType.disable_entity) {
             return AnimationType.disable_entity;
         } else if (base == AnimationType.stairs_up) {
             return AnimationType.stairs_up;
         } else if (base == AnimationType.stairs_down) {
             return AnimationType.stairs_down;
         } else if (base == AnimationType.stairs_left) {
             return AnimationType.stairs_left;
         } else if (base == AnimationType.stairs_right) {
             return AnimationType.stairs_right;
         }
 
         return base;
     }
 
     float calculateAnimationTime(float base, int mul) {
         return base * ((float) Math.pow(0.5, mul / 2));
     }
 
     float beforeTime(float base, int mul) {
         float ret = 0;
 
         for (int i = 0; i != mul; i++) {
             ret += this.calculateAnimationTime(base, i);
         }
 
         return ret;
     }
 }
