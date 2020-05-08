 /**
  * Copyright (c) 2013 Robert Maupin
  * 
  * This software is provided 'as-is', without any express or implied
  * warranty. In no event will the authors be held liable for any damages
  * arising from the use of this software.
  * 
  * Permission is granted to anyone to use this software for any purpose,
  * including commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  * 
  *    1. The origin of this software must not be misrepresented; you must not
  *    claim that you wrote the original software. If you use this software
  *    in a product, an acknowledgment in the product documentation would be
  *    appreciated but is not required.
  * 
  *    2. Altered source versions must be plainly marked as such, and must not be
  *    misrepresented as being the original software.
  * 
  *    3. This notice may not be removed or altered from any source
  *    distribution.
  */
 package roboflight.core;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 import roboflight.Robot;
 import roboflight.core.db.ClassInfo;
 import roboflight.core.db.RobotDatabase;
 
 public class Engine {
 	
 	private BattleRunner current;
 	private RobotDatabase database;
 	
 	public Engine() {
 		//test directory
 		database = new RobotDatabase();
 		
 		if("eclipse".equals(System.getProperty("environment"))) {
 			database.addDirectory("../RoboflightRobots/bin");
 		} else {
 			database.addDirectory("robots");
 		}
 		
 		database.rebuildDatabase();
 	}
 	
 	public RobotDatabase getDatabase() {
 		return database;
 	}
 	
 	public void startBattle(ClassInfo[] classes) {
 		Robot[] robots = new Robot[classes.length];
 		
 		//load robots
 		for(int i=0;i<classes.length;++i) {
 			ClassInfo info = classes[i];
 			try {
 				//I think it goes something like this
 				URLClassLoader loader = new URLClassLoader(
 						new URL[] { info.parent.toURI().toURL() },
 						ClassLoader.getSystemClassLoader());
 				
 				Class<?> robot = loader.loadClass(info.toString());
 				
 				robots[i] = (Robot)robot.newInstance();
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		startBattle(robots);
 	}
 	
 	public void startBattle(Robot[] robots) {
 		stopCurrentBattle();
 		current = BattleRunner.create(robots);
 		current.start();
 	}
 	
 	public void stopCurrentBattle() {
 		if(current != null) {
 			//shut it down
 			try {
 				//to stop it quickly
 				current.setFPS(BattleRunner.START_FPS);
 				
 				//unpause it (or it will never stop)
 				if(current.isPaused())
 					current.setPaused(false);
 				
 				current.stop();
 			} catch(InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			current = null;
 		}
 	}
 	
 	public BattleRunner getCurrentBattle() {
 		return current;
 	}
 	
 }
