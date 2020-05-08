 /** Copyright (c) 2012 Jake Willoughby, Dan Willoughby
 
     This file is part of tankatar.
 
 tankatar is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 any later version.
 
 tankatar is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with tankatar.  See gpl3.txt. If not, see <http://www.gnu.org/licenses/>.
 */
 package com.github.onionjake.tankatar.core;
 
 import static playn.core.PlayN.*;
 
 import playn.core.Game;
 import playn.core.Surface;
 import playn.core.Image;
 import playn.core.GroupLayer;
 import playn.core.ImageLayer;
 import playn.core.Keyboard;
 import playn.core.Pointer;
 import playn.core.Sound;
 import playn.core.Layer.Util;
 import pythagoras.f.AbstractPoint;
 import playn.core.util.Callback;
 
 import org.jbox2d.dynamics.World;
 import org.jbox2d.common.Vec2;
 import java.util.ArrayList;
 
 public class Tankatar implements Game, Keyboard.Listener {
 
   private Sound ding;
   private float frameAlpha;
   private float touchVectorX, touchVectorY;
   private GroupLayer worldLayer;
   private Coordinate touchPosition;
   private TankatarWorld world;
 
 
   private boolean controlLeft, controlRight, controlUp, controlDown, controlShoot;
 
   private ArrayList<Tank> players = new ArrayList<Tank>();
 
   @Override
   public void init() {
    touchPosition = new Coordinate(0,0,0);
 
     worldLayer = graphics().createGroupLayer();
 
     world = new TankatarWorld(worldLayer);
     players.add(world.newPlayer());
     
     graphics().rootLayer().add(worldLayer);
 
     keyboard().setListener(this);
     pointer().setListener(new Pointer.Listener() {
       @Override
       public void onPointerEnd(Pointer.Event event) {
         //touchVectorX = touchVectorY = 0;
         pythagoras.f.Point p = Util.screenToLayer(worldLayer, event.x() ,event.y());
         touchPosition = new Coordinate(p.x,p.y,0);
         controlShoot = false;
       }
       @Override
       public void onPointerDrag(Pointer.Event event) {
        // touchMove(event.x(), event.y());
         pythagoras.f.Point p = Util.screenToLayer(worldLayer, event.x() ,event.y());
         touchPosition = new Coordinate(p.x,p.y,0);
         controlShoot = true;
       }
       @Override
       public void onPointerStart(Pointer.Event event) {
        // touchMove(event.x(), event.y());
         pythagoras.f.Point p = Util.screenToLayer(worldLayer, event.x() ,event.y());
         touchPosition = new Coordinate(p.x,p.y,0);
         controlShoot = true;
       }
     });
 
     ding = assets().getSound("ding");
 
     net().post("http://localhost:4567/players/fred", "", new Callback<String>() {
       @Override
       public void onSuccess(String json) {
         System.out.println(json);
       }
 
       @Override
       public void onFailure(Throwable error) {
         System.err.println("bad request");
       }
     });
 
     net().get("http://localhost:4567/players", new Callback<String>() {
       @Override
       public void onSuccess(String json) {
         System.out.println(json);
       }
 
       @Override
       public void onFailure(Throwable error) {
         System.err.println("bad request");
       }
     });
   }
 
   @Override
   public void paint(float alpha) {
   }
 
   @Override
   public void update(float delta) {
     for (Tank t:players) {
       t.setAcceleration(0,0,0);
 
       if (t.isResting()) {
         // Keyboard control.
         if (controlLeft) {
           t.ax = -50.0;
         }
         if (controlRight) {
           t.ax = 50.0;
         }
         if (controlUp) {
           t.ay = -50.0;
         }
         if (controlDown) {
           t.ay = 50.0;
         }
         if (controlShoot)
           players.get(0).shoot(touchPosition);
 
         // Mouse Control.
         t.ax += touchVectorX;
         t.ay += touchVectorY;
       }
     }
     world.update(delta/100);
   }
 
   @Override
   public void onKeyDown(Keyboard.Event event) {
     System.out.println("Key Down");
     switch (event.key()) {
       case SPACE:
         controlShoot = true;
         break;
       case LEFT:
         controlLeft = true;
         break;
       case UP:
         controlUp = true;
         break;
       case RIGHT:
         controlRight = true;
         break;
       case DOWN:
         controlDown = true;
         break;
       case A:
         controlLeft = true;
         break;
       case W:
         controlUp = true;
         break;
       case D:
         controlRight = true;
         break;
       case S:
         controlDown = true;
         break;
     }
   }
 
   @Override
   public void onKeyTyped(Keyboard.TypedEvent event) {
   }
 
   @Override
   public void onKeyUp(Keyboard.Event event) {
     switch (event.key()) {
       case LEFT:
         controlLeft = false;
         break;
       case UP:
         controlUp = false;
         break;
       case RIGHT:
         controlRight = false;
         break;
       case DOWN:
         controlDown = false;
         break;
       case SPACE:
         controlShoot = false;
         break;
       case A:
         controlLeft = false;
         break;
       case W:
         controlUp = false;
         break;
       case D:
         controlRight = false;
         break;
       case S:
         controlDown = false;
         break;
     }
   }
 
   private void touchMove(float x, float y) {
     float cx = graphics().screenWidth() / 2;
     float cy = graphics().screenHeight() / 2;
 
     // Acceleration of touch
     touchVectorX = (x - cx) * 40.0f / cx;
     touchVectorY = (y - cy) * 40.0f / cy;
   }
 
   @Override
   public int updateRate() {
     return 25;
   }
 }
