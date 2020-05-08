 package com.discretesoftworks.framework;
 
 import android.graphics.PointF;
 
 public class MovingObject extends GameObject{
 	
 	private float dx, dy, dz;
 	
 	private float speed;
 	private float speedMult;
 	private float dir;
 	
 	private float acceleration = .005f;
 	
 	private boolean autoMove;
 	
 	public MovingObject(float x, float y, float z, float width, float height, String objectName){
 		super(x,y,z,width,height,objectName);
 		dx = dy = dz = 0f;
 		autoMove = false;
 		speed = 0f;
 		speedMult = 1f;
 		dir = 0f;
 	}
 	
 	protected Path optimizePath(Path path){
 		int k = 0;
 		PointF a, b;
 		a = new PointF(getX(),getY());
 		int i = 0;
 		while (i < path.length()){
 			for (int j = i+2; j < path.length(); j++){
 				b = path.getPoint(j);
 				if (checkLineCollision(a.x,a.y,b.x,b.y) < 0f){
 					k += 1;
 					path.remove(path.getPoint(j-1));
 					j-=1;
 				}
 			}
 			a = path.getPoint(i++);
 		}
 		System.out.println("Removed "+k+" points.");
 		return path;
 	}
 	
 	protected Path findPath(float endX, float endY, float dDir, int maxTries){
 		Path pathL = new Path();
 		Path pathR = new Path();
 		Path[] paths = {pathL, pathR};
 		
 		//System.out.println("==================== THE LITTLE PATH FINDER THAT COULD ====================");
 		
 		int found = -1;
 		PointF pointL = new PointF(getX(),getY());
 		PointF pointR = new PointF(getX(),getY());
 		PointF[] points = {pointL, pointR};
 		int tries = 0;
 		while (found == -1 && tries < maxTries && paths[0] != null && paths[1] != null){
 			tries += 1;
 			//System.out.println("RUN THROUGH - NUMBER "+tries);
 			for (int i = 0; i < 2; i++) {
 				float dist = checkLineCollision(points[i].x,points[i].y,endX,endY);
 				if (dist == -1f)
 					found = i;
 				else if (paths[i] != null){
 					//System.out.println("Distance: "+dist);
 					
 					float dir = Directional.pointDirection(points[i].x, points[i].y, endX, endY) + dDir;
 					float ex = points[i].x + Directional.lengthDirX(dir, dist);
 					float ey = points[i].y + Directional.lengthDirY(dir, dist);
 					
 					int tries2 = 1;
 					//System.out.println("Attempting to find a new partial path.");
 					int rotationTries = (int)(360 / Math.abs(dDir)) + 1;
 					
 					while ((dist = checkLineCollision(points[i].x, points[i].y, ex, ey) + .4f) > 0f && tries2 <= rotationTries){
 						tries2 += 1;
 						
 						dir += dDir * (i == 0 ? -1 : 1);
 						ex = points[i].x + Directional.lengthDirX(dir, dist);
 						ey = points[i].y + Directional.lengthDirY(dir, dist);
 					}
 					if (tries2 <= rotationTries){
 						points[i] = new PointF(ex,ey);
 						paths[i].add(points[i], dist);
 					}
 					else {
 						System.out.println("Could not find new straight line.");
 						paths[i] = null;
 					}
 				}
 			}
 		}
 		for (int i = 0; i < 2; i++){
 			if (paths[i] != null)
 				paths[i].add(new PointF(endX,endY), 0);
 		}
 		if (found == -1){
 			System.out.println("Could not find path.");
 			return null;
 		}
 		System.out.println("Returning path "+found);
 		return paths[found];
 	}
 	
 	protected float checkLineCollision(float startX, float startY, float endX, float endY){
 		float dist = .4f;
 		float sx = startX;
 		float sy = startY;
 		float dir = Directional.pointDirection(sx, sy, endX, endY);
 		float dx = Directional.lengthDirX(dir,dist);
 		float dy = Directional.lengthDirY(dir,dist);
 		int tries = 0;
 		while (placeFree(sx, sy) && Directional.pointDistance(sx, sy, endX, endY) >= dist/2f && tries < 40){
 			tries += 1;
 			sx += dx;
 			sy += dy;
 		}
 		
 		if (!placeFree(sx, sy) || Directional.pointDistance(sx, sy, endX, endY) >= dist){
 			//System.out.println("Found a collision in "+tries+" tries.");
 			float newdist = Directional.pointDistance(startX,startY,sx,sy);
 			if (newdist < dist)
 				return dist;
 			return newdist;
 		}
 		//System.out.println("Safe path in "+tries+" iterations.");
 		return -1f;
 	}
 	
 	public void setAutoMove(boolean m){
 		autoMove = m;
 	}
 	
 	public boolean getAutoMove(){
 		return autoMove;
 	}
 	
 	public void setSpeed(float speed){
 		this.speed = speed;
 	}
 	
 	public float getSpeed(){
 		return speed;
 	}
 	
 	public void setSpeedMult(float speedMult){
 		this.speedMult = speedMult;
 	}
 	
 	public float getSpeedMult(){
 		return speedMult;
 	}
 	
 	public void setDir(float dir){
 		this.dir = dir;
		super.setDir(dir);
 	}
 	
 	public float getDir(){
 		return dir;
 	}
 	
 	public void update(float deltaTime){
 		//moveWithoutCollisions();
 		moveCheckCollisions(deltaTime);
 		super.update(deltaTime);
 	}
 	
 	public void presetMovements(float deltaTime){
 		if (speed == 0f){
 			setdx(0);
 			setdy(0);
 		}
 		else {
 			dx = Directional.lengthDirX(dir, speed*speedMult*deltaTime);
 			dy = Directional.lengthDirY(dir, speed*speedMult*deltaTime);
 		}
 	}
 	
 	public void moveWithoutCollisions(float deltaTime){
 		if (autoMove)
 			presetMovements(deltaTime);
 		
 		changeX(getdx());
 		changeY(getdy());
 		changeZ(getdz());
 	}
 	
 	public void moveCheckCollisions(float deltaTime){
 		if (autoMove)
 			presetMovements(deltaTime);
 		
 		if (getdz() != 0f){
 			changeZ(getdz());
 			float moveZ = getdz() > 0 ? -acceleration : acceleration;
 			while (Directional.checkAllCollisions(this, GameRenderer.s_instance.solidObjects) != null){
 				changeZ(moveZ);
 				setdz(0);
 			}
 		}
 		
 		if (getdy() != 0f){
 			changeY(getdy());
 			float moveY = getdy() > 0 ? -acceleration : acceleration;
 			while (Directional.checkAllCollisions(this, GameRenderer.s_instance.solidObjects) != null){
 				changeY(moveY);
 				setdy(0);
 			}
 		}
 		
 		if (getdx() != 0f){
 			changeX(getdx());
 			float moveX = getdx() > 0 ? -acceleration : acceleration;
 			while (Directional.checkAllCollisions(this, GameRenderer.s_instance.solidObjects) != null){
 				changeX(moveX);
 				setdx(0);
 			}
 		}
 	}
 	
 	public void setdx(float dx){
 		this.dx = dx;
 	}
 	
 	public void setdy(float dy){
 		this.dy = dy;
 	}
 	
 	public void setdz(float dz){
 		this.dz = dz;
 	}
 	
 	public float getdx(){
 		return dx;
 	}
 	
 	public float getdy(){
 		return dy;
 	}
 	
 	public float getdz(){
 		return dz;
 	}
 	
 	public void changedx(float ddx){
 		dx += ddx;
 	}
 	
 	public void changedy(float ddy){
 		dy += ddy;
 	}
 	
 	public void changedz(float ddz){
 		dz += ddz;
 	}
 	
 }
