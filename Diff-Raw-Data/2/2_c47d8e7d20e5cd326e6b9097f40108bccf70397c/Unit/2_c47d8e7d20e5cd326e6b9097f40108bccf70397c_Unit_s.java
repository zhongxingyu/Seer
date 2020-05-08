 /**
  * Copyright (c) 2011-2012 Henning Funke.
  * 
  * This file is part of Battlepath.
  *
  * Battlepath is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * Battlepath is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package entities;
 
 
 import java.util.ArrayList;
 
 import collision.CollisionSystem;
 import collision.Move;
 
 import util.Vector2D;
 
 import engine.GlobalInfo;
 import game.Game;
 
 
 public class Unit extends HealthEntity {
 
 	public ArrayList<Vector2D> path;
 	public Vector2D direction = new Vector2D(0,0);
 	double speed = 4;
 	public boolean actionmode = true;
 
 	
 	public Unit(Vector2D position, Game game) {
 		super(position,game);
 		health = 500;
 	}
 	
 	public void moveTo(Vector2D dest) {
 		path = game.pathPlanner.plan(pos, dest);
 	}
 	
 	public void setHealth(int h) {
 		if(h > 100) {
 			h = 100;
 		}
 		else if(h < 0) {
 			h = 0;
 		}
 		
 		health = h;
 	}
 	
 	public void shoot(Vector2D direction) {
 		game.entities.add(new Projectile(pos.add(direction.scalar(getRadius())), direction, game));
 	}
 	
 	public Vector2D velocityDt() {
 		return velocity.scalar(game.dt);
 	}
 	
 	public void process(double dt) {
 		
 		
 		
 		if(path != null && path.size() > 0) {
 			if(pos.distance(path.get(0)) < GlobalInfo.accuracy) {
 				path.remove(0);
 				velocity = new Vector2D(0,0);
 			}
 			if(path.size() > 0) {
 				Vector2D dest = path.get(0);
 				velocity = dest.subtract(pos).normalize().scalar(speed);
 			}
 			
 		}
 		move = new Move(this,dt);
 		
 
 	}
 
 	@Override
 	public double getRadius() {
		return 1.49;
 	}
 
 	@Override
 	public void collide(CollisionEntity e) {
 		
 	}
 
 }
