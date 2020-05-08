 package com.plovergames.robotsvsaliens;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.plovergames.framework.Input.TouchEvent;
 import com.plovergames.framework.math.OverlapTester;
 import com.plovergames.framework.math.Rectangle;
 import com.plovergames.framework.math.Vector2;
 
 import android.util.Log;
 
 
 public class ControlPanel  {
 	/*
 	Control Panel class,  Keep a list of actions and updates current action
 	*/
 
 	public static final int WAIT =0;
 	public static final int MOVE =1;
 	public static final int TURN_RIGHT =2;
 	public static final int TURN_LEFT =3;
 	
 	public int[] commands; 
 	public int active;
 	public boolean paused=false;
 	public boolean PopUpMenu = false; 
 	List <Rectangle> commandSelect;
 	public ControlPanel() {
 		this.commands = new int[8];
 		this.active=0;
 		commandSelect = new ArrayList<Rectangle>();
 		for(int i =0;i<8;i++)
 			commandSelect.add(new Rectangle(32+i*32,0,32,32));
 
 	}
 	
 
 	public void update(){
 		if(!paused){
 			active +=1;
 			if(active > 8) {
 				active=0;
 			}	
 		}
 	}
 	
 
 	public void menu(Vector2 touch){
 		for(int i = 0; i<8; i++){
 			Rectangle selected= commandSelect.get(i);
 			if(OverlapTester.pointInRectangle(selected, touch)){
 				commands[i]+=1;
 				if (commands[i]>3) commands[i]=0;
 			return;
 			}
 		}
 
 	}
 	
 
 	
 
 
 }
