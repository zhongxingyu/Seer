 /*
  * Copyright 2011, Erik Lund
  *
  * This file is part of Voxicity.
  *
  *  Voxicity is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Voxicity is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Voxicity.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package voxicity;
 
 import org.lwjgl.util.vector.Vector3f;
 
 public class Player
 {
 	String name = "Player";
 	Vector3f last_pos = new Vector3f();
 	Vector3f pos = new Vector3f( 0, 3, 0 );
 	Vector3f accel = new Vector3f();
 	Vector3f velocity = new Vector3f();

 	Vector3f look = new Vector3f( 0, 0, 1 );
 
	Vector3f forward = new Vector3f( 0, 0, 1 );
	Vector3f right = new Vector3f( 1, 0, 0 );

 	boolean flying = true;
 	boolean jumping = true;
 }
