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
 import java.util.Collections;
 
 import com.artemis.Entity;
 import com.artemis.utils.ImmutableBag;
 
 import org.geekygoblin.nedetlesmaki.game.components.visual.Sprite;
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
 import org.geekygoblin.nedetlesmaki.game.constants.ColorType;
 
 /**
  *
  * @author natir
  */
 @Singleton
 public class GameSystem {
 
     private final EntityIndexManager index;
     public boolean end;
 
     @Inject
     public GameSystem(EntityIndexManager index) {
         this.index = index;
         this.end = false;
     }
 
     public ArrayList<Mouvement> moveEntity(Entity e, Position dirP, float baseBefore, boolean nedPush) {
         Entity nedEntity = this.index.getNed();
 
         Position oldP = this.index.getPosition(e);
 
         ArrayList<Mouvement> mouv = new ArrayList();
 
         float destroyBefore = baseBefore;
 
         for (int i = 0; i != this.index.getMovable(e); i++) {
             float animTime = this.calculateAnimationTime(0.6f, i);
             Position newP = PosOperation.sum(oldP, dirP);
 
             if (i > this.index.getBoost(e) - 1) {
                 e.getComponent(Pusher.class).setPusher(true);
             }
 
             if (this.index.positionIsVoid(newP)) {
                 Square s = index.getSquare(newP.getX(), newP.getY());
                 if (this.testStopOnPlate(e, s)) {
                     mouv.addAll(this.runValideMove(dirP, e, false, baseBefore, animTime, i, this.index.isBoosted(e), nedPush));
                     if (this.index.getBoost(e) != 20) {
                         e.getComponent(Pusher.class).setPusher(false);
                     }
 
                     return mouv;
                 }
                 if (!this.testBlockedPlate(e, s)) {
                     mouv.addAll(runValideMove(dirP, e, false, baseBefore, animTime, i, this.index.isBoosted(e), nedPush));
                     baseBefore = 0;
                 }
             } else {
                if (this.index.isStairs(newP) && e.equals(nedEntity)) {
                     mouv.addAll(nedMoveOnStairs(dirP, e, animTime));
                     if (!mouv.isEmpty()) {
                         this.end = true;
                     }
                 }
                 if (this.index.isPusherEntity(e)) {
                     ArrayList<Entity> aNextE = index.getSquare(newP.getX(), newP.getY()).getWith(Pushable.class);
                     if (!aNextE.isEmpty()) {
                         Entity nextE = aNextE.get(0);
                         if (this.index.isPushableEntity(nextE)) {
                             if (this.index.isDestroyer(e)) {
                                 if (this.index.isDestroyable(nextE)) {
                                     mouv.addAll(destroyMove(nextE, dirP, destroyBefore + this.beforeTime(0.6f, i), animTime));
                                     mouv.addAll(runValideMove(dirP, e, false, this.beforeTime(0.6f, i), animTime, i, this.index.isBoosted(e), nedPush));
                                 } else {
                                     ArrayList<Mouvement> recMouv = this.moveEntity(nextE, dirP, baseBefore + this.beforeTime(0.6f, i), e == nedEntity);
                                     if (!recMouv.isEmpty()) {
                                         mouv.addAll(recMouv);
                                         mouv.addAll(runValideMove(dirP, e, true, this.beforeTime(0.6f, i), animTime, i, this.index.isBoosted(e), nedPush));
                                     }
                                 }
                             } else {
                                 ArrayList<Mouvement> recMouv = this.moveEntity(nextE, dirP, baseBefore + this.beforeTime(0.6f, i), e == nedEntity);
                                 if (!recMouv.isEmpty()) {
                                     mouv.addAll(recMouv);
                                     if (!this.index.isCatchNed(nextE)) {
                                         mouv.addAll(runValideMove(dirP, e, true, baseBefore, animTime, i, this.index.isBoosted(e), nedPush));
                                         if (recMouv.size() > 2) {
                                             mouv.add(new Mouvement(nedEntity).setAnimation(this.nedWaitBoostChoice(dirP)).saveMouvement());
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
 
                 if (this.index.isBoosted(e)) {
                     mouv.add(new Mouvement(e).setAnimation(this.getMakiAnimation(true, -1, dirP, ColorType.blue)).saveMouvement());
                 }
 
                 if (this.index.nedIsCatched(e) && nedPush) {
                     mouv.add(new Mouvement(nedEntity).setAnimation(this.getFlyAnimation(-1, dirP)).saveMouvement());
                 }
 
                 if (this.index.getBoost(e) != 20) {
                     e.getComponent(Pusher.class).setPusher(false);
                 }
 
                 if (mouv.size() == 3 && this.isBeginFly(mouv.get(1).getAnimation(0)) && this.isEndFly(mouv.get(2).getAnimation(0))) {
                     mouv.remove(2);
                     mouv.remove(1);
                     mouv.add(new Mouvement(nedEntity).setPosition(dirP).setAnimation(this.getNedAnimation(dirP, 0, true, false)).setAnimationTime(this.calculateAnimationTime(0.6f, 0)).saveMouvement());
                 }
 
                 if (mouv.size() == 1 && this.isEndFly(mouv.get(0).getAnimation(0))) {
                     mouv.remove(0);
                 }
 
                 return mouv;
             }
         }
 
         if (this.index.isBoosted(e)) {
             mouv.add(new Mouvement(e).setAnimation(this.getMakiAnimation(true, -1, dirP, ColorType.blue)).saveMouvement());
         }
 
         if (this.index.nedIsCatched(e)) {
             mouv.add(new Mouvement(this.index.getNed()).setAnimation(this.getFlyAnimation(-1, dirP)).setAnimationTime(this.calculateAnimationTime(baseBefore, 2)).saveMouvement());
         }
 
         if (mouv.size() == 3 && this.isBeginFly(mouv.get(1).getAnimation(0)) && this.isEndFly(mouv.get(2).getAnimation(0))) {
             mouv.remove(2);
             mouv.remove(1);
             mouv.add(new Mouvement(nedEntity).setPosition(dirP).setAnimation(this.getNedAnimation(dirP, 0, true, false)).setAnimationTime(this.calculateAnimationTime(0.6f, 0)).saveMouvement());
         }
 
         if (mouv.size() == 1 && this.isEndFly(mouv.get(0).getAnimation(0))) {
             mouv.remove(0);
         }
 
        return mouv;
     }
 
     private boolean isBeginFly(AnimationType a) {
         return a.equals(AnimationType.fly_start_down) || a.equals(AnimationType.fly_start_left) || a.equals(AnimationType.fly_start_right) || a.equals(AnimationType.fly_start_up);
     }
 
     private boolean isEndFly(AnimationType a) {
         return a.equals(AnimationType.fly_stop_down) || a.equals(AnimationType.fly_stop_left) || a.equals(AnimationType.fly_stop_right) || a.equals(AnimationType.fly_stop_up);
     }
 
     private ArrayList<Mouvement> runValideMove(Position diff, Entity e, boolean push, float bw, float aT, int pas, boolean boosted, boolean pusherIsNed) {
         Position oldP = this.index.getPosition(e);
         Position newP = PosOperation.sum(oldP, diff);
 
         ArrayList<Mouvement> m = new ArrayList();
 
         if (index.moveEntity(oldP.getX(), oldP.getY(), newP.getX(), newP.getY())) {
             if (e == this.index.getNed()) {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(this.getNedAnimation(diff, pas, push, boosted)).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
             } else {
                 if (makiMoveOnePlate(newP, e)) {
                     if (actualIsColorPlate(oldP, e)) {
                         this.index.getSquare(oldP.getX(), oldP.getY()).getWith(Plate.class).get(0).getComponent(Plate.class).setMaki(false);
                         this.index.getSquare(newP.getX(), newP.getY()).getWith(Plate.class).get(0).getComponent(Plate.class).setMaki(true);
                         m.addAll(makiPlateMove(oldP, newP, e, true, aT, bw, true));
                     } else {
                         m.addAll(makiPlateMove(oldP, newP, e, true, aT, bw, false));
                     }
 
                     m.addAll(this.tryPlate());
                 } else if (makiMoveOutPlate(oldP, e)) {
                     m.addAll(makiPlateMove(oldP, newP, e, false, aT, bw, actualIsColorPlate(oldP, e)));
 
                     m.addAll(this.tryPlate());
                 } else {
                     m.add(new Mouvement(e).setPosition(diff).setAnimation(this.getMakiAnimation(boosted, pas, diff, this.index.getColorType(e))).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
                 }
 
                 Entity ned = this.index.getNed();
                 if (this.index.isCatchNed(e) && pusherIsNed) {
                     if (index.moveEntity(oldP.getX() - diff.getX(), oldP.getY() - diff.getY(), oldP.getX(), oldP.getY())) {
                         if (pusherIsNed) {
                             m.add(new Mouvement(ned).setPosition(diff).setAnimation(this.getFlyAnimation(pas, diff)).setBeforeWait(bw).setAnimationTime(aT).saveMouvement());
 
                             this.index.getCatchNed(e).nedCatched(true);
 
                             ned.getComponent(Position.class).setX(oldP.getX());
                             ned.getComponent(Position.class).setY(oldP.getY());
                         }
                     }
                 }
             }
 
             e.getComponent(Position.class).setX(newP.getX());
             e.getComponent(Position.class).setY(newP.getY());
 
             return m;
         }
 
         return m;
     }
 
     private AnimationType getNedAnimation(Position diff, int pas, boolean push, boolean boosted) {
         if (diff.getX() > 0) {
             if (push) {
                 return AnimationType.ned_push_right;
             } else {
                 return AnimationType.ned_right;
             }
         } else if (diff.getX() < 0) {
             if (push) {
                 return AnimationType.ned_push_left;
             } else {
                 return AnimationType.ned_left;
             }
         } else if (diff.getY() > 0) {
             if (push) {
                 return AnimationType.ned_push_down;
             } else {
                 return AnimationType.ned_down;
             }
         } else if (diff.getY() < 0) {
             if (push) {
                 return AnimationType.ned_push_up;
             } else {
                 return AnimationType.ned_up;
             }
         } else {
             return this.getMakiAnimation(boosted, pas, diff, ColorType.no);
         }
     }
 
     private ArrayList<Mouvement> nedMoveOnStairs(Position diff, Entity e, float aT) {
         Position newP = PosOperation.sum(this.index.getPosition(e), diff);
 
         ArrayList<Mouvement> m = new ArrayList();
 
         if (e == this.index.getNed()) {
             if (diff.getX() > 0) {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_mount_stairs_right).setAnimationTime(aT).saveMouvement());
             } else if (diff.getX() < 0) {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_mount_stairs_left).setAnimationTime(aT).saveMouvement());
             } else if (diff.getY() > 0) {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_mount_stairs_down).setAnimationTime(aT).saveMouvement());
             } else if (diff.getY() < 0) {
                 m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.ned_mount_stairs_up).setAnimationTime(aT).saveMouvement());
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
 
     private ArrayList<Mouvement> makiPlateMove(Position oldP, Position newP, Entity e, boolean getOne, float aT, float bT, boolean actualIsPlate) {
         ArrayList<Mouvement> m = new ArrayList();
         Position diff = PosOperation.deduction(newP, oldP);
 
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
 
         if (plateC.getColor() == makiC.getColor()) {
             if (plateC.getColor() == ColorType.green) {
                 if (getOne && !actualIsPlate) {
                     m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.maki_green_one).setAnimationTime(aT).setBeforeWait(bT).saveMouvement());
                 } else if (!getOne && actualIsPlate) {
                     m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.maki_green_out).setAnimationTime(aT).setBeforeWait(bT).saveMouvement());
                 } else {
                     m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.no).setAnimationTime(aT).setBeforeWait(bT).saveMouvement());
                 }
             } else if (plateC.getColor() == ColorType.orange) {
                 if (getOne && !actualIsPlate) {
                     m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.maki_orange_one).setAnimationTime(aT).setBeforeWait(bT).saveMouvement());
                 } else if (!getOne && actualIsPlate) {
                     m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.maki_orange_out).setAnimationTime(aT).setBeforeWait(bT).saveMouvement());
                 } else {
                     m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.no).setAnimationTime(aT).setBeforeWait(bT).saveMouvement());
                 }
             } else if (plateC.getColor() == ColorType.blue) {
                 if (getOne && !actualIsPlate) {
                     m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.maki_blue_one).setAnimationTime(aT).setBeforeWait(bT).saveMouvement());
                 } else if (!getOne && actualIsPlate) {
                     m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.maki_blue_out).setAnimationTime(aT).setBeforeWait(bT).saveMouvement());
                 } else {
                     m.add(new Mouvement(e).setPosition(diff).setAnimation(AnimationType.no).setAnimationTime(aT).setBeforeWait(bT).saveMouvement());
                 }
             }
         }
 
         return m;
     }
 
     public ArrayList<Mouvement> destroyMove(Entity e, Position diff, float bT, float aT) {
         ArrayList<Mouvement> preM = this.moveEntity(e, diff, bT, false);
 
         if (preM.isEmpty()) {
             preM.add(new Mouvement(e).setPosition(new Position(0, 0)).setAnimation(AnimationType.box_boom).setBeforeWait(bT).setAnimationTime(aT).saveMouvement());
         } else {
             preM.add(new Mouvement(e).setPosition(new Position(0, 0)).setAnimation(AnimationType.box_destroy).setBeforeWait(bT).setAnimationTime(aT).saveMouvement());
         }
 
         return preM;
     }
 
     private AnimationType getMakiAnimation(boolean boosted, int pas, Position diff, ColorType makiColor) {
 
         if (boosted) {
             if (diff.getX() > 0) {
                 if (pas < 0) {
                     return AnimationType.boost_stop_right;
                 } else if (pas == 3) {
                     return AnimationType.boost_start_right;
                 } else if (pas > 3) {
                     return AnimationType.boost_loop_right;
                 }
             } else if (diff.getX() < 0) {
                 if (pas < 0) {
                     return AnimationType.boost_stop_left;
                 } else if (pas == 3) {
                     return AnimationType.boost_start_left;
                 } else if (pas > 3) {
                     return AnimationType.boost_loop_left;
                 }
             } else if (diff.getY() > 0) {
                 if (pas < 0) {
                     return AnimationType.boost_stop_down;
                 } else if (pas == 3) {
                     return AnimationType.boost_start_down;
                 } else if (pas > 3) {
                     return AnimationType.boost_loop_down;
                 }
             } else if (diff.getY() < 0) {
                 if (pas < 0) {
                     return AnimationType.boost_stop_up;
                 } else if (pas == 3) {
                     return AnimationType.boost_start_up;
                 } else if (pas > 3) {
                     return AnimationType.boost_loop_up;
                 }
             }
         }
 
         if (makiColor == ColorType.orange) {
             return AnimationType.maki_orange_no;
         } else {
             return AnimationType.no;
         }
     }
 
     private AnimationType getFlyAnimation(int pas, Position diff) {
 
         if (diff.getX() > 0) {
             if (pas < 0) {
                 return AnimationType.fly_stop_right;
             } else if (pas == 0) {
                 return AnimationType.fly_start_right;
             } else if (pas > 0) {
                 return AnimationType.fly_loop_right;
             }
         } else if (diff.getX() < 0) {
             if (pas < 0) {
                 return AnimationType.fly_stop_left;
             } else if (pas == 0) {
                 return AnimationType.fly_start_left;
             } else if (pas > 0) {
                 return AnimationType.fly_loop_left;
             }
         } else if (diff.getY() > 0) {
             if (pas < 0) {
                 return AnimationType.fly_stop_down;
             } else if (pas == 0) {
                 return AnimationType.fly_start_down;
             } else if (pas > 0) {
                 return AnimationType.fly_loop_down;
             }
         } else if (diff.getY() < 0) {
             if (pas < 0) {
                 return AnimationType.fly_stop_up;
             } else if (pas == 0) {
                 return AnimationType.fly_start_up;
             } else if (pas > 0) {
                 return AnimationType.fly_loop_up;
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
 
     private boolean actualIsColorPlate(Position nextnextP, Entity maki) {
         Square s = this.index.getSquare(nextnextP.getX(), nextnextP.getY());
 
         if (s == null) {
             return false;
         }
 
         ArrayList<Entity> plates = s.getWith(Plate.class);
         if (plates.isEmpty()) {
             return false;
         }
 
         if (this.index.getColor(maki) == null) {
             return false;
         }
 
         if (this.index.getColorType(maki) == this.index.getColorType(plates.get(0))) {
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
 
     private ArrayList<Mouvement> tryPlate() {
         ImmutableBag<Entity> plateGroup = this.index.getAllPlate();
 
         ImmutableBag<Entity> stairsGroup = this.index.getAllStairs();
         Entity stairs = stairsGroup.get(0);
         Stairs stairsS = this.index.getStairs(stairs);
 
         for (int i = 0; i != plateGroup.size(); i++) {
             Entity plateE = plateGroup.get(i);
 
             Plate plate = this.index.getPlate(plateE);
 
             if (!plate.haveMaki()) {
                 if (stairsS.isOpen()) {
                     stairsS.setStairs(false);
 
                     stairsAnimation(stairs, stairsS, false);
                 }
 
                 return new ArrayList<>();
             }
         }
 
         if (stairsS.isOpen()) {
             return new ArrayList<>();
         }
 
         stairsS.setStairs(true);
 
         return stairsAnimation(stairs, stairsS, true);
     }
 
     private ArrayList<Mouvement> stairsAnimation(Entity stairs, Stairs stairsS, boolean open) {
         ArrayList<Mouvement> tmpm = new ArrayList();
 
         switch (stairsS.getDir()) {
             case 1:
                 if (open) {
                     tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_open_up).setAnimationTime(0.6f).saveMouvement());
                 } else {
                     tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_close_up).setAnimationTime(0.6f).saveMouvement());
                 }
                 break;
             case 2:
                 if (open) {
                     tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_open_down).setAnimationTime(0.6f).saveMouvement());
                 } else {
                     tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_close_down).setAnimationTime(0.6f).saveMouvement());
                 }
                 break;
             case 3:
                 if (open) {
                     tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_open_left).setAnimationTime(0.6f).saveMouvement());
                 } else {
                     tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_close_left).setAnimationTime(0.6f).saveMouvement());
                 }
                 break;
             case 4:
                 if (open) {
                     tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_open_right).setAnimationTime(0.6f).saveMouvement());
                 } else {
                     tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_close_right).setAnimationTime(0.6f).saveMouvement());
                 }
                 break;
             default:
                 if (open) {
                     tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_open_up).setAnimationTime(0.6f).saveMouvement());
                 } else {
                     tmpm.add(new Mouvement(stairs).setAnimation(AnimationType.stairs_close_up).setAnimationTime(0.6f).saveMouvement());
                 }
         }
 
         return tmpm;
     }
 
     AnimationType nedWaitBoostChoice(Position diff) {
 
         if (diff.getX() > 0) {
             return AnimationType.ned_waits_boost_right;
         } else if (diff.getX() < 0) {
             return AnimationType.ned_waits_boost_left;
         } else if (diff.getY() > 0) {
             return AnimationType.ned_waits_boost_down;
         } else if (diff.getY() < 0) {
             return AnimationType.ned_waits_boost_up;
         }
 
         return AnimationType.no;
     }
 
     public boolean endOfLevel() {
         ImmutableBag<Entity> stairsGroup = this.index.getAllStairs();
 
         Entity ned = this.index.getNed();
         Position nedP = this.index.getPosition(ned);
         Sprite nedS = this.index.getSprite(ned);
 
         Entity stairs = stairsGroup.get(0);
         Position stairsP = this.index.getPosition(stairs);
         Stairs stairsS = this.index.getStairs(stairs);
 
         if (stairsS.isOpen() && PosOperation.equale(nedP, stairsP)) {
             if (nedS.getPlay().getName().length() > 9) {
                 if (nedS.getPlay().getName().substring(0, 9).equals("ned_mount") && nedS.getPlay().isStopped()) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     public void removeMouv() {
         boolean reCall = false;
 
         ArrayList<Mouvement> head = this.index.pop();
 
         Collections.reverse(head);
 
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
 
                 if (invertAnim == AnimationType.maki_blue_out || invertAnim == AnimationType.maki_orange_out || invertAnim == AnimationType.maki_green_out) {
                     this.index.getSquare(current.getX(), current.getY()).getWith(Plate.class).get(0).getComponent(Plate.class).setMaki(false);
                 }
 
                 this.index.moveEntity(current.getX(), current.getY(), current.getX() + diff.getX(), current.getY() + diff.getY());
                 head.get(i).getEntity().getComponent(Position.class).setX(current.getX() + diff.getX());
                 head.get(i).getEntity().getComponent(Position.class).setY(current.getY() + diff.getY());
 
                 if (invertAnim == AnimationType.stairs_open_down || invertAnim == AnimationType.stairs_open_up || invertAnim == AnimationType.stairs_open_left || invertAnim == AnimationType.stairs_open_right) {
                     reCall = true;
                 }
             }
         }
 
         Collections.reverse(rm);
 
         this.index.setRemove(rm);
 
         if (reCall) {
             this.removeMouv();
         }
 
         this.tryPlate();
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
         } else if (base == AnimationType.box_boom) {
             return AnimationType.box_create;
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
         } else if (base == AnimationType.stairs_close_up) {
             return AnimationType.stairs_open_up;
         } else if (base == AnimationType.stairs_close_down) {
             return AnimationType.stairs_open_down;
         } else if (base == AnimationType.stairs_close_left) {
             return AnimationType.stairs_open_left;
         } else if (base == AnimationType.stairs_close_right) {
             return AnimationType.stairs_open_right;
         } else if (base == AnimationType.stairs_open_up) {
             return AnimationType.stairs_close_up;
         } else if (base == AnimationType.stairs_open_down) {
             return AnimationType.stairs_close_down;
         } else if (base == AnimationType.stairs_open_left) {
             return AnimationType.stairs_close_left;
         } else if (base == AnimationType.stairs_open_right) {
             return AnimationType.stairs_close_right;
         } else if (base == AnimationType.boost_loop_up) {
             return AnimationType.no;
         } else if (base == AnimationType.boost_loop_down) {
             return AnimationType.no;
         } else if (base == AnimationType.boost_loop_left) {
             return AnimationType.no;
         } else if (base == AnimationType.boost_loop_right) {
             return AnimationType.no;
         } else if (base == AnimationType.boost_start_up) {
             return AnimationType.no;
         } else if (base == AnimationType.boost_start_down) {
             return AnimationType.no;
         } else if (base == AnimationType.boost_start_left) {
             return AnimationType.no;
         } else if (base == AnimationType.boost_start_right) {
             return AnimationType.no;
         } else if (base == AnimationType.boost_stop_up) {
             return AnimationType.no;
         } else if (base == AnimationType.boost_stop_down) {
             return AnimationType.no;
         } else if (base == AnimationType.boost_stop_left) {
             return AnimationType.no;
         }
 
         return base;
     }
 
     float calculateAnimationTime(float base, int mul) {
         if (mul > 0) {
             return base / 3f;
         } else {
             return base;
         }
     }
 
     float beforeTime(float base, int mul) {
         float ret = 0;
 
         for (int i = 0; i != mul; i++) {
             ret += this.calculateAnimationTime(base, i);
         }
 
         return ret;
     }
 }
