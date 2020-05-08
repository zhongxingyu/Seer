 /*
  * This file is part of LinkJVM.
  *
  * Java Framework for the KIPR Link
  * Copyright (C) 2014 LinkJVM
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package linkjvm.high.vision;
 
 import linkjvm.low.factory.JNIController;
 import linkjvm.low.vision.Device;
 import linkjvm.low.vision.IntPoint2;
 import linkjvm.low.vision.IntRectangle;
 
 /**
  * 
  * @author Markus Klein
  *
  */
 public class ImageProcessor {
 	private Device jniDevice;
 	private int channel;
 	
 	/**
 	 * 
 	 * @param cameraConfig
 	 * @param channel
 	 */
 	public ImageProcessor(CameraConfig cameraConfig, int channel){
 		jniDevice = JNIController.getInstance().getCameraFactory().getInstance(cameraConfig);
 		jniDevice.setConfig(cameraConfig.getChannelConfig());
 		this.channel = channel;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean openCamera(){
 		return jniDevice.open();
 	}
 	
 	/**
 	 * 
 	 * @param deviceNum
 	 * @return
 	 */
 	public boolean openCamera(int deviceNum){
 		return jniDevice.open(deviceNum);
 	}
 	
 	/**
 	 * 
 	 */
 	public void close(){
 		jniDevice.close();
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public long getObjectCount(){
 		return jniDevice.channels().get(channel).objects().size();
 	}
 	
 	/**
 	 * 
 	 * @param object
 	 * @return
 	 */
 	public double getObjectConfidence(int object){
 		return jniDevice.channels().get(channel).objects().get(object).confidence();
 	}
 	
 	/**
 	 * 
 	 * @param object
 	 * @return
 	 */
 	public Point2 getCentroid(int object){
 		return toHighPoint(jniDevice.channels().get(channel).objects().get(object).centroid());
 	}
 	
 	/**
 	 * 
 	 * @param object
 	 * @return
 	 */
 	public Rectangle getBoundingBox(int object){
 		IntRectangle jniRectangle = jniDevice.channels().get(channel).objects().get(object).boundingBox();
 		return toHighRectangle(jniRectangle);
 	}
 	
	private Point2 toHighPoint(IntPoint2 point){
		IntPoint2 jniPoint = point;
		Point2 ret = new Point2(jniPoint.x(), jniPoint.y()); 
		jniPoint.delete();
		return ret;
 	}
 	
	private Rectangle toHighRectangle(IntRectangle rect){
		IntRectangle jniRectangle = rect;
 		Rectangle ret = new Rectangle(jniRectangle.width(), jniRectangle.height(), jniRectangle.x(), jniRectangle.y(), toHighPoint(jniRectangle.center()));
		jniRectangle.delete();
 		return ret;
 	}
 }
