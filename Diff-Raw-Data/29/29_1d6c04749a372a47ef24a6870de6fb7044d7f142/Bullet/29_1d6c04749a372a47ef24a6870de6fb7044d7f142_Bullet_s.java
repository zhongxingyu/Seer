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
 /**
  * Bullets
  */
 public class Bullet extends TObject {
 
   /**
    * Create a bullet for tank with starting position x,y,z
    * @param Tank, x, y, z
    */
  public Bullet(Tank t, Coordinate c) {
    super(assets().getImage("bullet.png"));
    setPos(t.x,t.y,t.z);
     setVelocity(3,3,3);
   }
     
  }
