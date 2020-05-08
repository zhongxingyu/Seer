 /*
  * Copyright (C) 2012 by Moritz Tenorth
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.tum.cs.ias.knowrob.tutorial;
 
 
 import java.util.Random;
 
 import ros.*;
 import ros.communication.Time;
import ros.pkg.geometry_msgs.msg.Quaternion;
 import ros.pkg.knowrob_perception_tutorial.msg.ObjectDetection;
 
 
 /**
  * Simple publisher that publishes object detections at random positions on a topic
  * 
  * @author Moritz Tenorth, tenorth@cs.uni-bremen.de
  *
  */
 public class DummyPublisher {
 
 	static Boolean rosInitialized = false;
 	static Ros ros;
 	static NodeHandle n;
 	static Random rand;
 
 	static String[] obj_types = new String[]{"Cup", "DinnerPlate", "TableKnife", "DinnerFork", "SoupSpoon", "DrinkingBottle"};
 
 
 
 	/**
 	 * Create dummy object detections of random type at random positions
 	 * 
 	 * @return Object detection message
 	 */
 	private static ObjectDetection generateDummyObjectDetection() {
 
 		ObjectDetection obj =  new ObjectDetection();
 
 		obj.type = obj_types[rand.nextInt(6)];
 
 		obj.pose.header.frame_id = "map";
 		obj.pose.header.stamp = Time.now();
 
 		obj.pose.pose.position.x = rand.nextDouble() * 3;
 		obj.pose.pose.position.y = rand.nextDouble() * 3;
 		obj.pose.pose.position.z = rand.nextDouble() * 3;
 
		obj.pose.pose.orientation.w = 1;
		obj.pose.pose.orientation.x = 1;
		obj.pose.pose.orientation.y = 1;
		obj.pose.pose.orientation.z = 1;
		
		
 		return obj;
 		
 	}
 
 
 	public static void main(String[] args) {
 
 		Publisher<ObjectDetection> pub = null;
 		
 		ros = Ros.getInstance();
 
 		if(!Ros.getInstance().isInitialized()) {
 			ros.init("knowrob_tutorial_dummy_publisher");
 		}
 		n = ros.createNodeHandle();
 		
 		rand = new Random(Time.now().nsecs);
 		
 		try {
 
 			// create publisher for the "/dummy_object_detections" topic
 			pub = n.advertise("/dummy_object_detections", new ObjectDetection(), 100);
 			
 			while(n.ok()) {
 				
 				// generate fake object detections using the generateDummyObjectDetection 
 				// method and publish on the topic every second
 				
 				ObjectDetection obj = generateDummyObjectDetection();
 				pub.publish(obj);
 				
 				System.out.println("detected " + obj.type);
 				
 				Thread.sleep(1000);
 			}
 			
 		} catch (RosException e) {
 			e.printStackTrace();
 			
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			
 		} finally {
 			if(pub!=null)
 				pub.shutdown();
 		}
 	}
 }
