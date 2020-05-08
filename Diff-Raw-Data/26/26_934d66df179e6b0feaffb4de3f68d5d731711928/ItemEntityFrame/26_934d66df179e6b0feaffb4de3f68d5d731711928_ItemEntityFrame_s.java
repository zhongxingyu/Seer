 package com.bugfullabs.qube.game;
 
 import java.util.ArrayList;
 
 import org.anddev.andengine.entity.primitive.Line;
 import org.anddev.andengine.entity.scene.Scene;
 
 /**
  * (c) 2012 Bugful Labs
  * 
  * @author Grushenko
  * @author Bugful Labs
  * @email wojciech@bugfullabs.pl
  * 
  */	
 
 
 
 public class ItemEntityFrame{
 	
 	private int mTemplate;
 	
 	private int mX;
 	private int mY;
 	
 	private Scene mScene;
 	
 	private ArrayList<Line> mLines;
 	
 	public ItemEntityFrame(int x, int y, Scene scene, int pTemplate){
 		
 		this.mTemplate = pTemplate;
 		this.mX = round(x);
 		this.mY = round(y);
 		this.mScene = scene;
 		
 		this.mLines = new ArrayList<Line>();
 		
 		draw();
 		
 	}
 	
 	public ItemEntityFrame(Scene scene,int pTemplate){
 		this(0,0, scene, pTemplate);
 	}
 	
 	private void draw(){
 		
 		switch(mTemplate){
 		
 		
 		case ItemEntity.TEMPLATE_1:
 			mLines.add(0, new Line(mX, mY, mX+32, mY, 3));
 			mLines.add(1, new Line(mX+32, mY, mX+32, mY+32, 3));
 			mLines.add(2, new Line(mX+32, mY+32, mX, mY+32, 3));
 			mLines.add(3, new Line(mX, mY+32, mX, mY, 3));
 			
 			break;
 			
 		case ItemEntity.TEMPLATE_2_1:
 	
 			mLines.add(0, new Line(mX, mY, mX+64, mY, 3));
 			mLines.add(1, new Line(mX+64, mY, mX+64, mY+32, 3));
 			mLines.add(2, new Line(mX+64, mY+32, mX, mY+32, 3));
 			mLines.add(3, new Line(mX, mY+32, mX, mY, 3));
 			
 			break;
 				
 		case ItemEntity.TEMPLATE_2_2:
				
 			break;
 			
 		case ItemEntity.TEMPLATE_3:
 			
 			mLines.add(0, new Line(mX+32, mY, mX+64, mY, 3));
 			mLines.add(1, new Line(mX+64, mY, mX+64, mY+64, 3));
 			mLines.add(2, new Line(mX+64, mY+64, mX, mY+64, 3));
 			mLines.add(3, new Line(mX, mY+64, mX, mY+32, 3));
 			mLines.add(4, new Line(mX, mY+32, mX+32, mY+32, 3));
 			mLines.add(5, new Line(mX+32, mY+32, mX+32, mY, 3));
 			
 			break;
 		
 		case ItemEntity.TEMPLATE_4:
 			
 			mLines.add(0, new Line(mX, mY, mX+64, mY, 3));
 			mLines.add(1, new Line(mX+64, mY, mX+64, mY+64, 3));
 			mLines.add(2, new Line(mX+64, mY+64, mX, mY+64, 3));
 			mLines.add(3, new Line(mX, mY+64, mX, mY, 3));
 			
 			break;
 				
 		}
 		
 		for(int i = 0; i < mLines.size(); i++){
 			mScene.attachChild(mLines.get(i));
 		}
 		
 	}
 
 	public void setPosition(int x, int y){
 		
 		int rX = round(x);
 		int rY = round(y);
 		
 		int rX16 = round(x-16);
 		int rY16 = round(y-16);
 		
 		
 		switch(mTemplate){
 		
 		case ItemEntity.TEMPLATE_1:
 		mLines.get(0).setPosition(rX, rY, rX+32, rY);
 		mLines.get(1).setPosition(rX+32, rY, rX+32, rY+32);
 		mLines.get(2).setPosition(rX+32, rY+32, rX, rY+32);
 		mLines.get(3).setPosition(rX, rY+32, rX, rY);
 		break;
 		
 		
 		case ItemEntity.TEMPLATE_2_1:
 		mLines.get(0).setPosition(rX, rY, rX+64, rY);
 		mLines.get(1).setPosition(rX+64, rY, rX+64, rY+32);
 		mLines.get(2).setPosition(rX+64, rY+32, rX, rY+32);
 		mLines.get(3).setPosition(rX, rY+32, rX, rY);
 		break;
 
 		case ItemEntity.TEMPLATE_3:
 			
 			mLines.get(0).setPosition(rX16+32, rY16, rX16+64, rY16);
 			mLines.get(1).setPosition(rX16+64, rY16, rX16+64, rY16+64);
 			mLines.get(2).setPosition(rX16+64, rY16+64, rX16, rY16+64);
 			mLines.get(3).setPosition(rX16, rY16+64, rX16, rY16+32);
 			mLines.get(4).setPosition(rX16, rY16+32, rX16+32, rY16+32);
 			mLines.get(5).setPosition(rX16+32, rY16+32, rX16+32, rY16);	
 			
 		break;
 		
 		
 		case ItemEntity.TEMPLATE_4:
 		mLines.get(0).setPosition(rX16, rY16, rX16+64, rY16);
 		mLines.get(1).setPosition(rX16+64, rY16, rX16+64, rY16+64);
 		mLines.get(2).setPosition(rX16+64, rY16+64, rX16, rY16+64);
 		mLines.get(3).setPosition(rX16, rY16+64, rX16, rY16);
 		break;
 		
 		
 		
 		}
 	}
 	
 	public void remove(){
 		
 		for(int i = 0; i < mLines.size(); i++){
 			mLines.get(i).detachSelf();
 		}
 		mLines.clear();
 		
 	}
 	
 	
 	private int round(int n){
 		return (int)((n)/32)*32;
 	}
 	
 	
 }
