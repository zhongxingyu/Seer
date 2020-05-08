 //    This file is part of RobotsVsAliensInSpace. 
 //
 //	  Author: Lovisa Irpa Helgadottir
 //
 //    RobotsCsAliensInSpace is free software: you can redistribute it and/or modify
 //    it under the terms of the GNU General Public License as published by
 //    the Free Software Foundation, either version 3 of the License, or
 //    (at your option) any later version.
 //
 //    RobotsCsAliensInSpace is distributed in the hope that it will be useful,
 //    but WITHOUT ANY WARRANTY; without even the implied warranty of
 //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //    GNU General Public License for more details.
 //
 //    You should have received a copy of the GNU General Public License
 //    along with RobotsCsAliensInSpace.  If not, see <http://www.gnu.org/licenses/>.
 package com.plovergames.robotsvsaliens;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.util.Log;
 
 import com.plovergames.framework.math.OverlapTester;
 import com.plovergames.framework.math.Vector2;
 
 
 public class World {
 	public interface WorldListener{
 		public void jump();
 		public void highJump();
 		public void hit();
 		public void coin();
 	}
 
 	public static final float WORLD_WIDTH =15;
 	public static final float WORLD_HEIGHT =10*20;
 	public static final int WORLD_STATE_RUNNING=0;
 	public static final int WORLD_STATE_NEXT_LEVEL=1;
 	public static final int WORLD_STATE_GAME_OVER=2;
 
 	public ControlPanel controlPanel; 
 	public SelfDestructButton button;
 	public Robot robot;
 
 	public List<Ship> ship;
 	public List <Alien> aliens;
 	public List <Laser> lasers;
 	public List <Airlock> airlocks;
 	public List <Conveyorbelt> conveyorbelts;
 	public List <Instructions> instructions; 
 	public List <Rotator> rotators;
 
 	public int state;
 	public boolean tutorial = false;
 	public float[] camPos;
 	public LevelLoader level;
 
 	private boolean wasOnBelt = false;
 	private boolean rotated = false;
 	private int robotOnBelt;
 	private int robotOnRotator;
 	private float oldDeltaTime=0.0f;
 	private Vector2 oldPosition; 
 	public World(LevelLoader level) throws XmlPullParserException, IOException{
 
 		this.level = level;
 		this.state = WORLD_STATE_RUNNING;
 		this.ship = new ArrayList<Ship>();
 		this.robot = new Robot();
 		this.button = new SelfDestructButton();
 		this.aliens =  new ArrayList<Alien>();
 		this.lasers = new ArrayList<Laser>();
 		this.airlocks = new ArrayList<Airlock>();
 		this.rotators = new ArrayList<Rotator>();
 		this.conveyorbelts = new ArrayList<Conveyorbelt>();
 		this.instructions = new ArrayList<Instructions>();//new Instructions(0,0,"");
 		this.controlPanel = new ControlPanel();
 
 		this.camPos = new float[2];
 		generatelevel();
 	}
 
 	public void generatelevel() throws XmlPullParserException, IOException{
 		ship.clear();
 		aliens.clear();
 		lasers.clear();
 		airlocks.clear();
 		conveyorbelts.clear();
 		instructions.clear();
 		rotators.clear();
 		robot = new Robot();
 		button = new SelfDestructButton();
 		level.loadLevel(ship,robot,button,aliens,lasers,airlocks,conveyorbelts,instructions,rotators,controlPanel);
 		oldPosition = new Vector2(robot.position.x,robot.position.y);
 		camPos[0]=WORLD_WIDTH/2;
 		camPos[1]=WORLD_HEIGHT/40;
 
 
 	}
 
 	public void update(float deltaTime){	
 		updateRobot(deltaTime);
 		updateAlien(deltaTime);
 		updateItems(deltaTime);     
 	}
 
 
 	private void updateRobot(float deltaTime){
 
 		if(controlPanel.active >8) return;
 
 		if(controlPanel.commands[controlPanel.currentPanel][controlPanel.active-1] == ControlPanel.NEXT_PANEL){
 			controlPanel.activePanel[controlPanel.currentPanel]=controlPanel.active+1;
 			controlPanel.currentPanel++;
 			controlPanel.active=1;
 			robot.setState(controlPanel.commands[controlPanel.currentPanel][controlPanel.active-1]);
 		}
 
 		if(controlPanel.commands[controlPanel.currentPanel][controlPanel.active-1] == ControlPanel.MOVE && !checkAtEdge() ){
 			oldDeltaTime = 0.0f;
 			robot.update(deltaTime);
 			if(oldPosition.dist(robot.position)>=1.0f){
 				//				Log.d("oldPosition",""+robot.position.x);
 				updateControlPanel();
 				oldPosition.set(robot.position);
 				if(controlPanel.active==9) controlPanel.end=true;
 
 			}
 
 		}
 		else{
 			oldDeltaTime+=deltaTime;
 
 			if(checkAtEdge())
 				robot.hitEdge();
 //				robot.state = Robot.ROBOT_STATE_STOP;
 			
 
 
 			if(oldDeltaTime >=1.0f){
 				rotated = false;
 				oldDeltaTime=0.0f;
 				updateControlPanel();	
 				oldPosition.set(robot.position);
 			}			
 		}			
 		if(checkAtButton()){
 			Log.d("checkAtButton","True");
 			controlPanel.paused=true;
 			state=WORLD_STATE_NEXT_LEVEL;
 			return;
 		}
 		if(checkAlienCollision()){ 
 			Log.d("checkAlienCollision()","True");
 			robot.state= Robot.ROBOT_STATE_DEAD;
 			//			controlPanel.end= true;
 		}
 		if(checkLaserCollision()){
 			Log.d("CheckLaserCollision","True");
 			//			if(robot.state == Robot.ROBOT_STATE_ACTIVE){
 			robot.state = Robot.ROBOT_HIT_BY_LASER; 
 			controlPanel.update();
 			//			}
 			//			robot.state = Robot.ROBOT_HIT_BY_LASER;
 			//			robot.update(deltaTime);
 		}
 		if(checkAirlockCollision()){
 			Log.d("checkAirlockCollision()","True");
 			robot.state = Robot.ROBOT_STATE_DEAD;
 		}
 
 		if(checkAtBelt(deltaTime)){
 			wasOnBelt = true;
 			controlPanel.paused=true;
 			Log.d("checkAtBelt","True");	
 		}
 		
 		if(checkOnRotator() && !rotated ){
 			Log.d("checkOnRotator","True");
 			if(rotators.get(robotOnRotator).direction== Rotator.COUNTER_CLOCKWISE){
 				robot.direction+=90;
 				robot.position.x = Math.round(robot.position.x)+0.1f;
 				robot.position.y = Math.round(robot.position.y);
 				robot.bounds.lowerLeft.set(robot.position).sub(robot.bounds.width/2, robot.bounds.height/2);
 				if(robot.direction >270) robot.direction =0;
 				rotated = true;
 				Log.d("direction",""+robot.direction);
 			}
 			else{
 				robot.direction-=90;
 				robot.position.x = Math.round(robot.position.x)+0.1f;
 				robot.position.y = Math.round(robot.position.y);
 				robot.bounds.lowerLeft.set(robot.position).sub(robot.bounds.width/2, robot.bounds.height/2);
 				if(robot.direction <0) robot.direction =270;
 				rotated = true;
 			}
 
 		}
 		
 		else controlPanel.paused = false;
 	}
 
 	private void updateControlPanel(){
 		controlPanel.update();
 //		if(robot.state==Robot.ROBOT_HIT_BY_LASER) controlPanel.update();
 		
 		robot.setState(controlPanel.commands[controlPanel.currentPanel][controlPanel.active-1]);
 	}
 
 
 	private boolean checkAtEdge(){
 		int len = ship.size();
 		if(controlPanel.paused) 
 			return false;
 		for(int i =0; i<len; i++){
 			Ship shippart = ship.get(i);
 			if(OverlapTester.onTopOfRectangles(robot.bounds,shippart.bounds,robot.direction))
 				return false;
 
 		}
 		return  true;
 	}
 	private boolean checkAtBelt(float deltaTime){
 		int len = conveyorbelts.size();
 		for(int i =0; i<len; i++){
 			Conveyorbelt belt = conveyorbelts.get(i);
			if(OverlapTester.overlapHalfXRectangles(robot.bounds, belt.bounds, robot.direction)){
 				robotOnBelt = i;
 				robot.onConveyorbelt(belt.direction,deltaTime);			
 				moveRobotOnBelt(robotOnBelt);			
 				return true;
 			}
 
 		}
 		if(wasOnBelt){
 			Log.d("End of belt","True");
 			robotAfterBelt(robotOnBelt);
 			wasOnBelt = false;
 			controlPanel.paused = false;
 			updateControlPanel();
 			//			
 		}
 		return false;
 	}
 
 	private void moveRobotOnBelt(int currentbelt){
 		Conveyorbelt belt = conveyorbelts.get(currentbelt);
 		int dir = Math.abs(belt.direction-robot.direction);
 		switch(dir){
 		case 0: 
 			oldPosition.set(belt.position);
 			//			Log.v("0",""+robot.bounds.lowerLeft.x+","+robot.bounds.lowerLeft.y);
 			break;
 		case 90:
 			oldPosition.set(belt.position);
 			//			Log.v("90",""+robot.bounds.lowerLeft.x+","+robot.bounds.lowerLeft.y);
 			break;
 		case 180:
 			oldPosition.set(belt.position);
 			//			Log.v("180",""+robot.bounds.lowerLeft.x+","+robot.bounds.lowerLeft.y);
 			break;
 		case 270: 
 			oldPosition.set(belt.position);
 			//			Log.v("270",""+robot.bounds.lowerLeft.x+","+robot.bounds.lowerLeft.y);
 			break;
 		default:
 			break;
 		}
 
 
 	}
 
 	private void robotAfterBelt(int currentbelt){
 		Conveyorbelt belt = conveyorbelts.get(currentbelt);
 		int dir = belt.direction;
 		switch(dir){
 		case 0: 
 			robot.position.x = belt.position.x+0.1f;
 			robot.position.y = belt.position.y+1.0f;
 			robot.bounds.lowerLeft.set(robot.position).sub(robot.bounds.width/2, robot.bounds.height/2);
 			oldPosition.set(robot.position);
 			Log.v("End of belt 0",""+robot.bounds.lowerLeft.x+","+robot.bounds.lowerLeft.y);
 			break;
 		case 90:
 			robot.position.x = belt.position.x-0.9f;
 			robot.position.y = belt.position.y;
 			robot.bounds.lowerLeft.set(robot.position).sub(robot.bounds.width/2, robot.bounds.height/2);
 			oldPosition.set(robot.position);
 			Log.v("End of belt 90",""+robot.bounds.lowerLeft.x+","+robot.bounds.lowerLeft.y);
 			break;
 		case 180:
 			robot.position.x = belt.position.x+0.1f;
 			robot.position.y = belt.position.y-1.0f;
 			robot.bounds.lowerLeft.set(robot.position).sub(robot.bounds.width/2, robot.bounds.height/2);
 			Log.v("End of belt 180",""+robot.bounds.lowerLeft.x+","+robot.bounds.lowerLeft.y);
 			oldPosition.set(robot.position);
 			break;
 		case 270: 
 			robot.position.x = belt.position.x+0.9f;
 			robot.position.y = belt.position.y;
 			robot.bounds.lowerLeft.set(robot.position).sub(robot.bounds.width/2, robot.bounds.height/2);
 			Log.v("End of belt 270",""+robot.bounds.lowerLeft.x+","+robot.bounds.lowerLeft.y);
 			oldPosition.set(robot.position);
 			break;
 		default:
 			break;
 
 		}
 
 	}
 
 	private boolean checkAtButton(){
 		if(OverlapTester.overlapHalfRectangles(robot.bounds, button.bounds))
 			return true;
 		return false;
 	}
 
 	private boolean checkAlienCollision(){
 		for(int i=0; i<aliens.size();i++){		
 			if(OverlapTester.overlapHalfRectangles(robot.bounds,aliens.get(i).bounds)){
 				aliens.get(i).velocity.set(0,0);
 				return true;}
 		}
 		return false;
 	}
 
 	private boolean checkLaserCollision(){
 		int len = lasers.size();
 		for(int i = 0; i<len; i++){
 			Laser laser = lasers.get(i);
 
 			//if(OverlapTester.overlapHalfRectangles(robot.bounds,laser.bounds))
 			if(laser.touched==0 && OverlapTester.overlapLaser(robot.bounds,laser.bounds)){
 				laser.touched= Math.abs(laser.touched-1);;
 				return true;
 			}
 			int lenbeam = laser.beam.size();
 			for(int j =0; j<lenbeam;j++){
 				//				if(OverlapTester.overlapHalfRectangles(robot.bounds,laser.beam.get(j).bounds))
 				if(laser.beam.get(j).touched == 0 && OverlapTester.overlapLaser(robot.bounds,laser.beam.get(j).bounds)){
 					laser.beam.get(j).touched =  Math.abs(laser.beam.get(j).touched-1);
 					return true;
 				}
 			}
 
 		}
 		return false;
 	}
 
 	private boolean checkAirlockCollision(){
 		for(int i=0; i<airlocks.size();i++){
 			if(OverlapTester.overlapHalfRectangles(robot.bounds, airlocks.get(i).bounds))
 				return true;
 		}return false;
 	}
 
 	private boolean checkOnRotator(){
 		for(int i=0; i<rotators.size();i++){
 			if(OverlapTester.overlapHalfRectangles(robot.bounds, rotators.get(i).bounds)){
 				robotOnRotator = i;
 				Log.d("Direction",""+robot.direction);
 				return true;
 				}
 				
 			}			
 		if(rotated)
 			rotated = false;
 		
 		return false;
 	}
 
 	private void updateAlien(float deltaTime){
 		for(int i=0; i<aliens.size();i++){
 			Alien alien = aliens.get(i);
 			if(!checkAlienAtEdge(alien))alien.atEdge = true; 
 			alien.update(deltaTime);
 		}
 
 	}
 	private boolean checkAlienAtEdge(Alien alien){
 		int len = ship.size();
 		for(int i =0; i<len; i++){
 			Ship shippart = ship.get(i);
 			int dir = alien.direction;
 			if(alien.velocity.x >0 || alien.velocity.y<0){
 				dir+=180;
 			}
 			if(OverlapTester.onTopOfRectangles(alien.bounds, shippart.bounds, dir))
 				return true;
 		}
 		return false;
 	}
 
 	private void updateItems(float deltaTime){
 		for(int i = 0; i<conveyorbelts.size();i++){
 			conveyorbelts.get(i).update(deltaTime);
 		}
 		for(int i = 0; i<rotators.size();i++){
 			rotators.get(i).update(deltaTime);
 		}
 	}
 }
