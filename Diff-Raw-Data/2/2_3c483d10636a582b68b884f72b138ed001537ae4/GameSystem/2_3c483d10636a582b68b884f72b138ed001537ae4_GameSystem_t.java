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
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.artemis.Entity;
 import com.artemis.ComponentMapper;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.VoidEntitySystem;
 import com.artemis.utils.ImmutableBag;
 
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Color;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Pushable;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Pusher;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Position;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Movable;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.BlockOnPlate;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.StopOnPlate;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Square;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Plate;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Boostable;
 import org.geekygoblin.nedetlesmaki.game.manager.EntityIndexManager;
 import org.geekygoblin.nedetlesmaki.game.utils.PosOperation;
 import org.geekygoblin.nedetlesmaki.game.utils.Mouvement;
 import org.geekygoblin.nedetlesmaki.game.constants.AnimationType;
 import org.geekygoblin.nedetlesmaki.game.Game;
 import org.geekygoblin.nedetlesmaki.game.constants.ColorType;
 
 /**
  *
  * @author natir
  */
 @Singleton
 public class GameSystem extends VoidEntitySystem {
 
     private EntityIndexManager index;
     private boolean run;
     
     @Mapper
     ComponentMapper<Pushable> pushableMapper;
     @Mapper
     ComponentMapper<Pusher> pusherMapper;
     @Mapper
     ComponentMapper<Position> positionMapper;
     @Mapper
     ComponentMapper<Movable> movableMapper;
     @Mapper
     ComponentMapper<Plate> plateMapper;
     @Mapper
     ComponentMapper<Color> colorMapper;
     @Mapper
     ComponentMapper<Boostable> boostMapper;
     @Mapper
     ComponentMapper<BlockOnPlate> blockOnPlateMapper;
     @Mapper
     ComponentMapper<StopOnPlate> stopOnPlateMapper;
 
     @Inject
     public GameSystem(EntityIndexManager index) {
         this.index = index;
         this.run = false;
     }
 
     @Override
     protected void processSystem() {
         if(this.run) {
             this.endOfLevel();
         }
     }
 
     public ArrayList<Mouvement> moveEntity(Entity e, Position dirP) {
         this.run = true;
         
         /*Check if move possible*/
         Position oldP = this.getPosition(e);
 
         ArrayList<Mouvement> mouv = new ArrayList<Mouvement>();
 
         for (int i = 0; i != this.getMovable(e); i++) {
             Position newP = PosOperation.sum(oldP, dirP);
 
            if (i > this.getBoost(e) - 1) {
                 e.getComponent(Pusher.class).setPusher(true);
             }
 
             if (this.positionIsVoid(newP)) {
                 Square s = index.getSquare(newP.getX(), newP.getY());
                 if (this.testStopOnPlate(e, s)) {
                     mouv.add(runValideMove(oldP, newP, e));
 
                     if (this.getBoost(e) != 20) {
                         e.getComponent(Pusher.class).setPusher(false);
                     }
 
                     return mouv;
                 }
                 if (!this.testBlockedPlate(e, s)) {
                     mouv.add(runValideMove(oldP, newP, e));
                 }
             } else {
                 if (this.isPusherEntity(e)) {
                     ArrayList<Entity> aNextE = index.getSquare(newP.getX(), newP.getY()).getWith(Pushable.class);
                     if (!aNextE.isEmpty()) {
                         Entity nextE = aNextE.get(0);
                         if (this.isPushableEntity(nextE)) {
                             ArrayList<Mouvement> recMouv = this.moveEntity(nextE, dirP);   
                             if(recMouv.size() != 0) {
                                 mouv.addAll(recMouv);
                                 mouv.add(runValideMove(oldP, newP, e));
                             }
                         }
                     }
                 }
 
                 if (this.getBoost(e) != 20) {
                     e.getComponent(Pusher.class).setPusher(false);
                 }
          
                 return mouv;
             }
         }
 
         return mouv;
     }
 
     private Mouvement runValideMove(Position oldP, Position newP, Entity e) {
         if (index.moveEntity(oldP.getX(), oldP.getY(), newP.getX(), newP.getY())) {
             Position diff = PosOperation.deduction(newP, oldP);
             Mouvement m = new Mouvement(e).addPosition(diff).addAnimation(AnimationType.no);
 
             if (e == ((Game) this.world).getNed()) {
                 if (diff.getX() > 0) {
                     m = new Mouvement(e).addPosition(diff).addAnimation(AnimationType.ned_right);
                 } else if (diff.getX() < 0) {
                     m = new Mouvement(e).addPosition(diff).addAnimation(AnimationType.ned_left);
                 } else if (diff.getY() > 0) {
                     m = new Mouvement(e).addPosition(diff).addAnimation(AnimationType.ned_down);
                 } else if (diff.getY() < 0) {
                     m = new Mouvement(e).addPosition(diff).addAnimation(AnimationType.ned_up);
                 }
             }
 
             e.getComponent(Position.class).setX(newP.getX());
             e.getComponent(Position.class).setY(newP.getY());
 
             return m;
         }
 
         return null;
     }
 
     private boolean testStopOnPlate(Entity eMove, Square obj) {
         if (obj == null) {
             return false;
         }
 
         ArrayList<Entity> array = obj.getWith(Plate.class);
 
         if (array.size() == 0) {
             return false;
         }
 
         Entity plate = obj.getWith(Plate.class).get(0);
         Plate p = plateMapper.getSafe(plate);
         StopOnPlate b = stopOnPlateMapper.getSafe(eMove);
 
         if (b == null) {
             return false;
         }
 
         if (p.isPlate()) {
             if (b.stop()) {
                 if (this.colorMapper.getSafe(plate).getColor() == this.colorMapper.getSafe(eMove).getColor() && this.colorMapper.getSafe(eMove).getColor() != ColorType.orange) {
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
 
         if (array.size() == 0) {
             return false;
         }
 
         Entity plate = obj.getWith(Plate.class).get(0);
         Plate p = plate.getComponent(Plate.class);
         BlockOnPlate b = blockOnPlateMapper.getSafe(eMove);
 
         if (b == null) {
             return false;
         }
 
         if (p.isPlate()) {
             if (b.block()) {
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean positionIsVoid(Position p) {
         Square s = index.getSquare(p.getX(), p.getY());
 
         if (s != null) {
             ArrayList<Entity> plate = s.getWith(Plate.class);
             ArrayList<Entity> all = s.getAll();
 
             if (all.size() == plate.size()) {
                 return true;
             } else {
                 return false;
             }
         }
 
         return true;
     }
 
     public boolean isPushableEntity(Entity e) {
         Pushable p = this.pushableMapper.getSafe(e);
 
         if (p != null) {
             if (p.isPushable()) {
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean isPusherEntity(Entity e) {
         Pusher p = this.pusherMapper.getSafe(e);
 
         if (p != null) {
             if (p.isPusher()) {
                 return true;
             }
         }
 
         return false;
     }
 
     public Position getPosition(Entity e) {
         Position p = this.positionMapper.getSafe(e);
 
         if (p != null) {
             return p;
         }
 
         return new Position(-1, -1);
     }
 
     public int getMovable(Entity e) {
         Movable m = this.movableMapper.getSafe(e);
 
         if (m != null) {
             return m.getNbCase();
         }
 
         return 0;
     }
 
     public int getBoost(Entity e) {
         Boostable b = this.boostMapper.getSafe(e);
 
         if (b != null) {
             return b.getNbCase();
         }
 
         return 20;
     }
 
     private void endOfLevel() {
         ImmutableBag<Entity> plateGroup = this.index.getAllPlate();
 
         for (int i = 0; i != plateGroup.size(); i++) {
             Entity platE = plateGroup.get(i);
 
             Plate plate = this.plateMapper.getSafe(platE);
             Position p = this.positionMapper.getSafe(platE);
             Color colorPlate = this.colorMapper.getSafe(platE);
 
             ArrayList<Entity> colorEntity = this.index.getSquare(p.getX(), p.getY()).getWith(Color.class);
 
             if (colorEntity.size() == 2) {
                 Color colorA = this.colorMapper.getSafe(colorEntity.get(0));
                 Color colorB = this.colorMapper.getSafe(colorEntity.get(1));
                 if (colorA != colorB && !plate.isPlate()) {
                     return;
                 }
             } else {
                 return;
             }
         }
 
         System.out.print("End of level\n");
     }
 }
