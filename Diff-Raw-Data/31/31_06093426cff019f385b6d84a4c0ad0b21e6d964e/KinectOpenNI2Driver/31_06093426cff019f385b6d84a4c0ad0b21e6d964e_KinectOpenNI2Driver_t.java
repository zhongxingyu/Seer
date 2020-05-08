 /**
  * Copyright (C) 2013 Colorado School of Mines
  *
  * This file is part of the Interface Software Development Kit (SDK).
  *
  * The InterfaceSDK is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * The InterfaceSDK is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the InterfaceSDK.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.mines.acmX.exhibit.input_services.hardware.drivers.openni2;
 
 import java.awt.Dimension;
 import edu.mines.acmX.exhibit.input_services.events.EventManager;
 import edu.mines.acmX.exhibit.input_services.events.EventType;
 import edu.mines.acmX.exhibit.input_services.events.InputReceiver;
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.DepthImageInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.HandTrackerInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.RGBImageInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.DriverHelper;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.DriverInterface;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import java.nio.ByteBuffer;
 import java.nio.ShortBuffer;
 
 import org.openni.Device;
 import org.openni.OpenNI;
 import org.openni.VideoStream;
 import org.openni.VideoFrameRef;
 import org.openni.SensorType;
 
 /**
  * Kinect driver that provides depth and rgb image functionality. Uses the
  * openni2 and nite library for communication to the kinect device.
  *
  * @author Matt Wesemann
  *
  */
 
 public class KinectOpenNI2Driver implements DriverInterface,
 		DepthImageInterface, RGBImageInterface, HandTrackerInterface {
 
 	private static Logger log = LogManager.getLogger(KinectOpenNI2Driver.class);
 	public static final EventManager evtMgr = EventManager.getInstance();
 	private static boolean init = false;
 	private boolean loaded;
 
 	private int imageWidth;
 	private int imageHeight;
 	private int depthWidth;
 	private int depthHeight;
 	private Device device;
 	private VideoStream colorStream;
 	private VideoStream depthStream;
 
 	public KinectOpenNI2Driver() {
 		loaded = false;
 		imageWidth = 0;
 		imageHeight = 0;
 		depthWidth = 0;
 		depthHeight = 0;
 		device = null;
 		colorStream = null;
 		depthStream = null;
 	}
 
 	@Override
 	public ShortBuffer getDepthImageData() {
 		DriverHelper.checkLoaded(this);
 		VideoFrameRef frame = depthStream.readFrame();
 		return frame.getData().asShortBuffer();
 	}
 
 	@Override
 	public void load() {
 		if(loaded)
 			destroy(); // reset driver
 
 		loaded = true;
 		if(!init){
 			init = true;
 			OpenNI.initialize();
 		}
 
 		device = Device.open();
 
 		colorStream = VideoStream.create(device, SensorType.COLOR);
 		colorStream.start();
 		depthStream = VideoStream.create(device, SensorType.DEPTH);
 		depthStream.start();
 
 		VideoFrameRef frameColor = colorStream.readFrame();
 		imageHeight = frameColor.getHeight();
 		imageWidth = frameColor.getWidth();
 
 		VideoFrameRef frameDepth = depthStream.readFrame();
 		depthHeight = frameDepth.getHeight();
 		depthWidth = frameDepth.getWidth();
 
 		EventManager.getInstance().fireEvent(EventType.VIEWPORT_DIMENSION,
 				new Dimension(depthWidth, depthHeight));
 	}
 
 	@Override
 	public boolean loaded(){
 		return loaded;
 	}
 
 	@Override
 	public int getDepthImageWidth() {
 		DriverHelper.checkLoaded(this);
 		return depthWidth;
 	}
 
 	@Override
 	public int getDepthImageHeight() {
 		DriverHelper.checkLoaded(this);
 		return depthHeight;
 	}
 
 	@Override
 	public boolean isAvailable() {
 		if(!loaded) {
 			try{
 				load();
 			} catch (Throwable t){
 				return false;
 			}
 		}
 		return device != null;
 	}
 
 	@Override
 	public void destroy() {
 		if(loaded){
 			colorStream.destroy();
 			depthStream.destroy();
 			device.close();
 
 			colorStream = null;
 			depthStream = null;
 			device = null;
			loaded = false;
 		}
 	}
 
 	@Override
 	public void updateDriver() {
 		DriverHelper.checkLoaded(this);
 		//To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public int getHandTrackingWidth() {
 		DriverHelper.checkLoaded(this);
 		return 0;  //To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public int getHandTrackingHeight() {
 		DriverHelper.checkLoaded(this);
 		return 0;  //To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public void registerHandCreated(InputReceiver r) {
 		//To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public void registerHandUpdated(InputReceiver r) {
 		//To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public void registerHandDestroyed(InputReceiver r) {
 		//To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public ByteBuffer getVisualData() {
 		DriverHelper.checkLoaded(this);
 		VideoFrameRef frame = colorStream.readFrame();
 		return frame.getData();
 	}
 
 	@Override
 	public int getRGBImageWidth() {
 		DriverHelper.checkLoaded(this);
 		return imageWidth;
 	}
 
 	@Override
 	public int getRGBImageHeight() {
 		DriverHelper.checkLoaded(this);
 		return imageHeight;
 	}
 }
