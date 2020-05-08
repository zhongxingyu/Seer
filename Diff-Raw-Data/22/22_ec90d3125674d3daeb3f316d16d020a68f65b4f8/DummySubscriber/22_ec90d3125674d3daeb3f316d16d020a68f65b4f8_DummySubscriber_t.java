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
 
 import javax.vecmath.Matrix4d;
 import javax.vecmath.Quat4d;
 import javax.vecmath.Vector3d;
 
 import edu.tum.cs.ias.knowrob.prolog.PrologInterface;
 import ros.*;
 import ros.pkg.geometry_msgs.msg.Pose;
 import ros.pkg.knowrob_perception_tutorial.msg.ObjectDetection;
 
 
 /**
  * Subscriber to KnowRob tutorial dummy object detections publisher
  * 
  * @author Moritz Tenorth, tenorth@cs.uni-bremen.de
  *
  */
 public class DummySubscriber {
 
 	static Boolean rosInitialized = false;
 	static Ros ros;
 	static NodeHandle n;
 
 	Subscriber.QueueingCallback<ObjectDetection> callback;
 	Subscriber<ObjectDetection> sub;
 
 	Thread listenToObjDetections;
 	Thread updateKnowRobObjDetections;
 
 
 	/**
 	 * Constructor: initializes the ROS environment
 	 *
 	 * @param node_name A unique node name
 	 */
 	public DummySubscriber(String node_name) {
 
 		initRos(node_name);
 		callback = new Subscriber.QueueingCallback<ObjectDetection>();
 	}
 
 
 	/**
 	 * Start threads that listen to object detections and add the 
 	 * data to KnowRob
 	 */
 	public void startObjDetectionsListener() {
 
 		listenToObjDetections = new Thread( new ListenerThread() );
 		listenToObjDetections.start();
 
 		updateKnowRobObjDetections = new Thread( new UpdateKnowrobThread() );
 		updateKnowRobObjDetections.start();
 	}
 
 
 	/**
 	 * Initialize the ROS environment if it has not yet been initialized
 	 *
 	 * @param node_name A unique node name
 	 */
 	protected static void initRos(String node_name) {
 
 		ros = Ros.getInstance();
 
 		if(!Ros.getInstance().isInitialized()) {
 			ros.init(node_name);
 		}
 		n = ros.createNodeHandle();
 
 	}
 
 
 	/**
 	 * Thread for listening to the object detections; puts the 
 	 * results into a QueuingCallback buffer for further processing
 	 *
 	 * @author Moritz Tenorth, tenorth@cs.uni-bremen.de
 	 *
 	 */
 	public class ListenerThread implements Runnable {
 
 		@Override 
 		public void run() {
 
 			try {
 				sub = n.subscribe("/dummy_object_detections", new ObjectDetection(), callback, 10);
 				n.spin();
 				sub.shutdown();
 
 			} catch(RosException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 	/**
 	 * Read perceptions from the QueueingCallback buffer and create the
 	 * corresponding object representations in KnowRob.
 	 *
 	 * @author Moritz Tenorth, tenorth@cs.uni-bremen.de
 	 *
 	 */
 	public class UpdateKnowrobThread implements Runnable {
 
 		@Override 
 		public void run() {
 
 			ObjectDetection obj;
 
 			try {
 
 				while (n.isValid()) {
 
 					obj = callback.pop();
 
 					Matrix4d p = quaternionToMatrix(obj.pose.pose);					
 					String q = "create_object_perception(" +
								"'http://ias.cs.tum.edu/kb/knowrob.owl#"+obj.type+"', [" 
 								+ p.m00 + ","+ p.m01 + ","+ p.m02 + ","+ p.m03 + ","
 								+ p.m10 + ","+ p.m11 + ","+ p.m12 + ","+ p.m13 + ","
 								+ p.m20 + ","+ p.m21 + ","+ p.m22 + ","+ p.m23 + ","
								+ p.m30 + ","+ p.m31 + ","+ p.m32 + ","+ p.m33 +
 								"], ['DummyObjectDetection'], ObjInst)";
 
					// uncomment to see the resulting query printed to the KnowRob console
					//System.err.println(q);
					
 					PrologInterface.executeQuery(q);
 					n.spinOnce();
 				}
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 	/**
 	 * Utility method: convert a ROS pose into a Java vecmath 4x4 pose matrix
 	 *
 	 * @param p Pose (ROS geometry_msgs)
 	 * @return 4x4 pose matrix
 	 */
 	protected static Matrix4d quaternionToMatrix(Pose p) {
 
 		return new Matrix4d(new Quat4d(p.orientation.x, p.orientation.y, p.orientation.z, p.orientation.w), 
 				new Vector3d(p.position.x, p.position.y, p.position.z), 1.0);
 	}
 }
