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
 package edu.mines.acmX.exhibit.input_services.hardware.drivers.openni;
 
 import java.awt.Dimension;
 import java.nio.ByteBuffer;
 import java.nio.ShortBuffer;
 
 import org.OpenNI.ActiveHandEventArgs;
 import org.OpenNI.Context;
 import org.OpenNI.DepthGenerator;
 import org.OpenNI.DepthMap;
 import org.OpenNI.DepthMetaData;
 import org.OpenNI.GeneralException;
 import org.OpenNI.GestureGenerator;
 import org.OpenNI.GestureRecognizedEventArgs;
 import org.OpenNI.HandsGenerator;
 import org.OpenNI.IObservable;
 import org.OpenNI.IObserver;
 import org.OpenNI.ImageGenerator;
 import org.OpenNI.ImageMetaData;
 import org.OpenNI.InactiveHandEventArgs;
 import org.OpenNI.StatusException;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.mines.acmX.exhibit.input_services.events.EventManager;
 import edu.mines.acmX.exhibit.input_services.events.EventType;
 import edu.mines.acmX.exhibit.input_services.events.InputReceiver;
 import edu.mines.acmX.exhibit.input_services.hardware.HardwareManager;
 import edu.mines.acmX.exhibit.input_services.hardware.HardwareManagerManifestException;
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.DepthImageInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.HandTrackerInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.RGBImageInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.DriverInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.InvalidConfigurationFileException;
 import edu.mines.acmX.exhibit.stdlib.graphics.HandPosition;
 import edu.mines.acmX.exhibit.stdlib.input_processing.tracking.HandTrackingUtilities;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.DriverException;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.DriverHelper;
 
 /**
  * Kinect driver that provides depth and rgb image functionality. Uses the
  * openni the library for communication to the kinect device.
  * 
  * @author Aakash Shah
  * @author Ryan Stauffer
  * 
  */
 public class KinectOpenNIDriver implements DriverInterface,
 		DepthImageInterface, RGBImageInterface, HandTrackerInterface {
 
 	private static Logger log = LogManager.getLogger(KinectOpenNIDriver.class);
 	public static final EventManager evtMgr = EventManager.getInstance();
 
 	private Context context;
 	private boolean loaded;
 
 	private DepthGenerator depthGen;
 	private ImageGenerator imageGen;
 	private GestureGenerator gestureGen;
 	private HandsGenerator handsGen;
 
 	private int imageWidth;
 	private int imageHeight;
 
 	private int depthWidth;
 	private int depthHeight;
 
 	public KinectOpenNIDriver() {
 		context = null;
 		loaded = false;
 
 		depthGen = null;
 		imageGen = null;
 		gestureGen = null;
 		handsGen = null;
 
 		imageWidth = 0;
 		imageHeight = 0;
 
 		depthWidth = 0;
 		depthHeight = 0;
 	}
 
 	@Override
 	public void load() throws InvalidConfigurationFileException, DriverException {
 
 		if(loaded)
 			destroy(); //reset driver
 
 		loaded = true;
 		try {
 			HardwareManager hm = HardwareManager.getInstance();
 			String canName = KinectOpenNIDriver.class.getCanonicalName();
 			if (!hm.hasConfigFile(canName)) {
 				throw new InvalidConfigurationFileException("Missing " +
 						canName +
 						" config file");
 			}
 			OpenNIContextSingleton.setConfigurationFile(hm.getConfigFile(canName));
 			context = OpenNIContextSingleton.getContext();
 		} catch (HardwareManagerManifestException e) {
 			// We should never be here, and it should be caught by the
 			// ModuleManager
 			log.error("HardwareManager Manifest Exception");
 		} catch (OpenNIConfigurationException e){
 			throw new InvalidConfigurationFileException("Invalid OpenNI configuration file.");
 		}  catch (GeneralException e){
 			throw new DriverException("Exception in OpenNI");
 		}
 
 		try{
 			gestureGen = GestureGenerator.create(context);
 			gestureGen.addGesture("Wave");
 			gestureGen.getGestureRecognizedEvent().addObserver(
 					new GestureRecognized());
 
 			handsGen = HandsGenerator.create(context);
 			handsGen.getHandCreateEvent().addObserver(new HandCreated());
 			handsGen.getHandUpdateEvent().addObserver(new HandUpdated());
 			handsGen.getHandDestroyEvent().addObserver(new HandDestroyed());
 
 			depthGen = DepthGenerator.create(context);
 			DepthMetaData depthMD = depthGen.getMetaData();
 
 			context.startGeneratingAll();
 
 			imageGen = ImageGenerator.create(context);
 
 			ImageMetaData imageMD = imageGen.getMetaData();
 			imageWidth = imageMD.getFullXRes();
 			imageHeight = imageMD.getFullYRes();
 
 			depthWidth = depthMD.getFullXRes();
 			depthHeight = depthMD.getFullYRes();
 
 			EventManager.getInstance().fireEvent(EventType.VIEWPORT_DIMENSION,
 					new Dimension(depthWidth, depthHeight));
 
 		} catch (GeneralException e){
 			throw new DriverException("Exception in OpenNI");
 		}
 	}
 
 	@Override
 	public boolean loaded(){
 		return loaded;
 	}
 
 
 	//HandTrackerInterface
 	/**
 	 * This updates all the nodes being observed by the context for any
 	 * available data. This should be called whenever updated information is
 	 * desired.
 	 */
 	public void updateDriver() {
 		DriverHelper.checkLoaded(this);
 
 		try {
 			context.waitAnyUpdateAll();
 		} catch (StatusException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public int getHandTrackingWidth() {
 		DriverHelper.checkLoaded(this);
 		return depthWidth;
 	}
 	
 	public int getHandTrackingHeight() {
 		DriverHelper.checkLoaded(this);
 		return depthHeight;
 	}
 	
 	class GestureRecognized implements IObserver<GestureRecognizedEventArgs> {
 
 		@Override
 		public void update(IObservable<GestureRecognizedEventArgs> observable,
 				GestureRecognizedEventArgs args) {
 
 			try {
 				handsGen.StartTracking(args.getEndPosition());
 
 			} catch (StatusException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	class HandCreated implements IObserver<ActiveHandEventArgs> {
 
 		@Override
 		public void update(IObservable<ActiveHandEventArgs> obs,
 				ActiveHandEventArgs e) {
 
 			HandPosition pos = new HandPosition(e.getId(),
 					HandTrackingUtilities.convertOpenNIPoint(depthGen,
 							e.getPosition()));
 			evtMgr.fireEvent(EventType.HAND_CREATED, pos);
 		}
 
 	}
 
 	class HandUpdated implements IObserver<ActiveHandEventArgs> {
 
 		@Override
 		public void update(IObservable<ActiveHandEventArgs> obs,
 				ActiveHandEventArgs e) {
 
 			HandPosition pos = new HandPosition(e.getId(),
 					HandTrackingUtilities.convertOpenNIPoint(depthGen,
 							e.getPosition()));
 
 			evtMgr.fireEvent(EventType.HAND_UPDATED, pos);
 
 		}
 	}
 
 	class HandDestroyed implements IObserver<InactiveHandEventArgs> {
 
 		@Override
 		public void update(IObservable<InactiveHandEventArgs> obs,
 				InactiveHandEventArgs e) {
 			evtMgr.fireEvent(EventType.HAND_DESTROYED, e.getId());
 		}
 
 	}
 
 	// DriverInterface
 	/**
 	 * The Kinect openni device is considered available if an openni context can
 	 * be created.
 	 */
 	public boolean isAvailable() {
 		boolean ret = true;
 		try {
 			if(!loaded) {
 				load();
 			}
 			context = OpenNIContextSingleton.getContext();
 		} catch (GeneralException e) {
 			ret = false;
 		} catch (OpenNIConfigurationException e) {
 			ret = false;
 		} catch (Throwable t){
 			ret = false; // load threw an exception;
 		}
 		return ret;
 	}
 
 	/**
 	 * Ends tracking on the hand tracker as well ensures that all generators are
 	 * stopped on the context singleton.
 	 */
 	public void destroy() {
 		if(loaded)  {
 			try {
 				handsGen.StopTrackingAll();
 				context.stopGeneratingAll();
 
 				// Remove all receivers connected to this driver
 				EventManager.getInstance().removeReceivers(EventType.HAND_CREATED);
 				EventManager.getInstance().removeReceivers(EventType.HAND_UPDATED);
 				EventManager.getInstance().removeReceivers(EventType.HAND_DESTROYED);
 
 			} catch (StatusException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// DepthDataInterface
 	public ShortBuffer getDepthImageData() {
 		DriverHelper.checkLoaded(this);
 		DepthMetaData depthMD = depthGen.getMetaData();
 
 		DepthMap dm = depthMD.getData();
 		ShortBuffer data = dm.createShortBuffer();
 		data.rewind();
 		return data;
 	}
 
 	public int getRGBImageWidth() {
 		DriverHelper.checkLoaded(this);
 		return imageWidth;
 	}
 
 	public int getRGBImageHeight() {
 		DriverHelper.checkLoaded(this);
 		return imageHeight;
 	}
 
 	// RGBImageInterface
 	public ByteBuffer getVisualData() {
 		DriverHelper.checkLoaded(this);
 		ImageMetaData imageMD = imageGen.getMetaData();
 		ByteBuffer rgbBuffer = imageMD.getData().createByteBuffer();
 		return rgbBuffer;
 	}
 
 	public int getDepthImageWidth() {
 		DriverHelper.checkLoaded(this);
 		return depthWidth;
 	}
 
 	public int getDepthImageHeight() {
 		DriverHelper.checkLoaded(this);
 		return depthHeight;
 	}
 
 	/**
 	 * Registers a hand created event given an input receiver
 	 * 
 	 * @param r
 	 *            the input receiver
 	 */
 	public void registerHandCreated(InputReceiver r) {
 		EventManager.getInstance().registerReceiver(EventType.HAND_CREATED, r);
 	}
 
 	/**
 	 * Registers a hand updated event given an input receiver
 	 * 
 	 * @param r
 	 *            the input receiver
 	 */
 	public void registerHandUpdated(InputReceiver r) {
 		EventManager.getInstance().registerReceiver(EventType.HAND_UPDATED, r);
 	}
 
 	/**
 	 * Registers a hand destroyed event given an input receiver
 	 * 
 	 * @param r
 	 *            the input receiver
 	 */
 	public void registerHandDestroyed(InputReceiver r) {
 		EventManager.getInstance()
 				.registerReceiver(EventType.HAND_DESTROYED, r);
 	}
 
 	public void registerViewportDimension(InputReceiver r) {
 		EventManager.getInstance().registerReceiver(
 				EventType.VIEWPORT_DIMENSION, r);
 	}
 }
