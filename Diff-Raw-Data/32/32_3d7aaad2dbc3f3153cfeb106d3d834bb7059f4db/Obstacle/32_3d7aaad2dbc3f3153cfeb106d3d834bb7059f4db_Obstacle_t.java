 package com.runnershigh;
 
 import android.graphics.Rect;
 
 public class Obstacle extends RHDrawable {
 	private Rect ObstacleRect;
 	public char ObstacleType; //s=slow, j=jumper //b=bonus
 	public boolean didTrigger;
 	private int radX;
 	private int radY;
 	public float centerX;
 	public float centerY;
 	private float angle;
 	private float orbitSpeed;
 	
 	
 	public Obstacle(float _x, float _y, float _z, float _width, float _height, char type){
 		super((int)_x, (int)_y, (int)_z, (int)_width, (int)_height);
 		
 		x=_x;
 		y=_y;
 		z=_z;
 		
 		ObstacleType=type;
 		ObstacleRect = new Rect ((int)x, (int)y, (int)x+(int)width, (int)y+(int)height);
 		
 		float textureCoordinates[] = { 0.0f, 1.0f, //
 				1.0f, 1.0f, //
 				0.0f, 0.0f, //
 				1.0f, 0.0f, //
 		};
 
 		short[] indices = new short[] { 0, 1, 2, 1, 3, 2 };
 
 		float[] vertices = new float[] { 0, 0, 0, width, 0, 0.0f, 0, height,
 				0.0f, width, height, 0.0f };
 
 		setIndices(indices);
 		setVertices(vertices);
 		setTextureCoordinates(textureCoordinates);
 		
 		didTrigger=false;
 		radX=radY=(int)(_width*2); //50
 		angle=0;
		orbitSpeed=_width/1000; //radX/1000; //0.05f; 
 		centerX=x;
 		centerY=y;
 	}
 	
 
 	public void setObstacleRect(float l, float r, float top, float bottom){
 		ObstacleRect.left=(int)l;
 		ObstacleRect.right=(int)r;
 		ObstacleRect.top=(int)top;
 		ObstacleRect.bottom=(int)bottom;
 	}
 	public void setObstacleRectRight(int r){
 		ObstacleRect.right=r;
 	}
 
 	public void updateObstacleRect(int levelPosition){
 		ObstacleRect.left -= levelPosition;
 		ObstacleRect.right -= levelPosition;
 	}
 	public boolean isClicked(float clickX, float clickY){
 		if(clickX <= x+width && clickX > x){
 			if(clickY <= y+height && clickY > y){
 				return true;
 			}
 		}
 		return false;
 	}
 	public void updateObstacleCircleMovement(){
 		x = centerX+(float)Math.cos(angle)*radX;
 		y = centerY+(float)Math.sin(angle)*radY;
 		angle += orbitSpeed;
 	}	
 }
