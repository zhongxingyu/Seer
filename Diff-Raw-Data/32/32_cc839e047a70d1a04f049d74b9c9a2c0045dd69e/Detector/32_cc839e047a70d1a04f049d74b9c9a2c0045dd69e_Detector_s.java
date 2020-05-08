 package com.oryzone.mvdetector;
 
 import com.oryzone.mvdetector.differenceStrategy.IDifferenceStrategy;
 import com.oryzone.mvdetector.detectorEvents.DetectorStateChangedEvent;
 import com.oryzone.mvdetector.detectorEvents.DetectorStateChangedListener;
 import java.awt.Dimension;
 import com.oryzone.mvdetector.detectorEvents.WarningSignalEvent;
 import com.oryzone.mvdetector.detectorEvents.WarningEndedEvent;
 import com.oryzone.mvdetector.detectorEvents.WarningStartedEvent;
 import com.oryzone.mvdetector.detectorEvents.WarningListener;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.event.EventListenerList;
 import java.util.Date;
 import com.googlecode.javacv.*;
 import com.oryzone.mvdetector.differenceStrategy.GreyscaleDifferenceStrategy;
 import com.oryzone.mvdetector.differenceStrategy.RgbDifferenceStrategy;
 import java.awt.Toolkit;
 import static com.googlecode.javacv.cpp.opencv_core.*;
 
 /**
  * Class that handles the whole logic of capturing the video stream and
  * processing it
  * 
  * @author Andrea Mangano, Luciano Mammino
  * @version 1.0
  */
 public class Detector extends Thread
 {
     /**
      * The current status of the detector
      */
     protected DetectorState state;
 
     /**
      * The options currently used by the detector
      */
     protected DetectorOptions options;
 
     /**
      * The stream frame grabber
      */
     protected FrameGrabber grabber;
     
     /**
      * The last acquired frame
      */
     protected IplImage lastFrame;
 
     /**
      * The frame used to display the video stream
      */
     protected CanvasFrame canvasFrame;
 
     /**
      * The image difference processor
      */
     protected ImageDifference imageDifference;
 
     /**
      * The current thread
      */
     protected Thread thread;
 
     /**
      * The date when the last warning occurred
      */
     protected Date warningActivationDate;
 
     /**
      * The date when the last warning period ended
      */
     protected Date warningDeactivationDate;
 
     /**
      * List used to store the attached listeners for the warning events
      */
     protected EventListenerList warningListeners;
     
     /**
      * List used to store the attached listener for state changed event
      */
     protected EventListenerList stateChangedListeners;
     
     
     protected boolean grabStarted = false;
     
     /**
      * Creates a new Detector instance with a given set of options
      * 
      * @param options
      *            the options that the detector should use
      */
     public Detector(DetectorOptions options)
     {
         
         this.options = options;
 	this.state = DetectorState.STOPPED;
         
         IDifferenceStrategy differenceStrategy;
         if(this.options.usingColoredDifference())
             differenceStrategy = new RgbDifferenceStrategy();
         else
             differenceStrategy = new GreyscaleDifferenceStrategy();
 	this.imageDifference = new ImageDifference(differenceStrategy);
         
         this.warningListeners = new EventListenerList();
         this.stateChangedListeners = new EventListenerList();
     }
 
     /**
      * Creates a new Detector instance using the default options
      */
     public Detector()
     {
 	this(new DetectorOptions());
     }
 
     /**
      * Gets the image difference instance
      * 
      * @return the current image difference instance
      */
     public ImageDifference getImageDifference()
     {
 	return this.imageDifference;
     }
 
     /**
      * gets the last captured frame
      * @return the last captured frame
      */
     public IplImage getLastFrame()
     {
         return this.lastFrame;
     }
 
     

     @Override
     public void run()
     {
         try
 	{
 	    this.grabber = new OpenCVFrameGrabber(0);
 
             this.canvasFrame = new CanvasFrame("Capturing");
             this.canvasFrame.setVisible(false);
             this.canvasFrame.setCanvasSize(this.options.getFrameDimension().width,
                                            this.options.getFrameDimension().height);
             Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
             this.canvasFrame.setLocation(dim.width/2 - this.options.getFrameDimension().width/2,
                                          dim.height/2 - this.options.getFrameDimension().height/2);
             this.grabber.setImageWidth(this.options.getFrameDimension().width);
             this.grabber.setImageHeight(this.options.getFrameDimension().height);
 
 	    this.setDetectorState(DetectorState.STARTED);
 	    this.grabber.start();
             this.grabStarted = true;
             this.canvasFrame.setVisible(true);
 
 	    this.lastFrame = this.grabber.grab();
 	    IplImage currImage = null;
 	    IplImage prevImage = null;
 
             this.setDetectorState(DetectorState.CAPTURING);
 	    while (this.grabStarted && (this.lastFrame = this.grabber.grab()) != null)
 	    {
 		if (currImage == null)
 		    currImage = this.lastFrame.clone();
 		else
 		{
 		    prevImage = currImage.clone();
 		    currImage = this.lastFrame.clone();
 		}
 
 		if (prevImage != null && currImage != null)
 		{
 		    this.imageDifference.setImages(prevImage, currImage);
 		    this.imageDifference.calculateDifference(prevImage,
 			    currImage);
 		    this.canvasFrame.showImage(this.imageDifference
 			    .getDiffImage());
 
                     this.handleWarning();
 		}
                 
 	    }
 	    this.setDetectorState(DetectorState.STOPPED);
 	} catch (Exception e)
 	{
 	    e.printStackTrace();
 	}
     }
 
     /**
      * Stops the detection
      */
     public void beforeStop()
     {
 	this.grabStarted = false;
         
         try
 	{
             this.grabber.stop();
             this.canvasFrame.setVisible(false);
             this.setDetectorState(DetectorState.STOPPED);
         } catch (Exception ex)
 	{
 	    Logger.getLogger(Detector.class.getName()).log(Level.SEVERE, null, ex);
 	}
     }
     
     @Override
     public void destroy()
     {
         this.canvasFrame.dispose();
         try
         {
             this.grabber.release();
         } catch (Exception ex)
         {
             Logger.getLogger(Detector.class.getName()).log(Level.SEVERE, null, ex);
         }
         finally
         {
             this.grabber = null;
             System.gc();
         }
     }
 
     /**
      * Gets the current detection status
      * @return the current status of the detector
      */
     public DetectorState getDetectorState()
     {
 	return this.state;
     }
     
     
     /**
      * Sets the current detector status and fires the state change event if needed
      * @param newState the new state
      */
     protected void setDetectorState(DetectorState newState)
     {
         if(this.state != newState)
         {
             DetectorState oldState = this.state;
             this.state = newState;
             this.fireDetectorStateChangedEvent(new DetectorStateChangedEvent(this, oldState, newState));
         }
     }
 
 
     /**
      * handles Warning
      */
     protected void handleWarning()
     {
         boolean isWarning = false;
 
         if(this.imageDifference.differencePercent >= this.options.getWarningSensibility())
         {
             this.warningActivationDate = new Date();
             this.fireWarningSignalEvent(new WarningSignalEvent(this));
             isWarning = true;
         }
 
         
         if(this.state == DetectorState.WARNING)
         {
             
             Date now = new Date();
 
 
             if( now.getTime() > this.warningActivationDate.getTime() + (this.options.getWarningDuration() * 1000) )
             {
                 //deactivate warning
                 this.warningDeactivationDate = new Date();
                 this.setDetectorState(DetectorState.CAPTURING);
                 this.fireWarningEndedEvent(new WarningEndedEvent(this));
             }
 
 
         }
         else
         {
             if(isWarning)
             {
                 //activate warning
                 this.setDetectorState(DetectorState.WARNING);
                 this.fireWarningStartedEvent(new WarningStartedEvent(this));
             }
 
         }
     }
 
 
     /**
      * Adds a new warning listener
      * @param listener the listener to add
      * @return the same instance of {@link Detector} for method chaining
      */
     public Detector addWarningListener(WarningListener listener)
     {
         this.warningListeners.add(WarningListener.class, listener);
         return this;
     }
 
 
     /**
      * Removes a previously attached warning listener
      * @param listener the previously attached listener
      * @return the same instance of {@link Detector} for method chaining
      */
     public Detector removeWarningListener(WarningListener listener)
     {
         this.warningListeners.remove(WarningListener.class, listener);
         return this;
     }
     
     
     /**
      * Add a new detector status changed listener
      * @param listener the new listener
      * @return the same instance of {@link Detector} for method chaining
      */
     public Detector addStateChangedListener(DetectorStateChangedListener listener)
     {
         this.stateChangedListeners.add(DetectorStateChangedListener.class, listener);
         return this;
     }
     
     
     /**
      * Removes a previously attached state changed listener
      * @param listener  the previously attached listener
      * @return the same instance of {@link Detector} for method chaining
      */
     public Detector removeStateChangedListener(DetectorStateChangedListener listener)
     {
         this.stateChangedListeners.remove(DetectorStateChangedListener.class, listener);
         return this;
     }
 
 
     /**
      * Fires the warning started event to all the attached listeners
      * @param event the event object
      */
     protected void fireWarningStartedEvent(WarningStartedEvent event)
     {
         WarningListener[] listeners = this.warningListeners.getListeners(WarningListener.class);
         for (int i = 0; i < listeners.length; i++)
         {
             listeners[i].onWarningStarted(event);
         }
     }
 
 
     /**
      * Fires the warning ended event to all the attached listeners
      * @param event the event object
      */
     protected void fireWarningEndedEvent(WarningEndedEvent event)
     {
         WarningListener[] listeners = this.warningListeners.getListeners(WarningListener.class);
         for (int i = 0; i < listeners.length; i++)
         {
             listeners[i].onWarningEnded(event);
         }
     }
 
 
     /**
      * Fires the warning ended event to all the attached listeners
      * @param event the event object
      */
     protected void fireWarningSignalEvent(WarningSignalEvent event)
     {
         WarningListener[] listeners = this.warningListeners.getListeners(WarningListener.class);
         for (int i = 0; i < listeners.length; i++)
         {
             listeners[i].onWarningSignal(event);
         }
     }
     
     
     /**
      * Fires the state changed event to all the attached listeners
      * @param event the event object to be passed
      */
     protected void fireDetectorStateChangedEvent(DetectorStateChangedEvent event)
     {
         DetectorStateChangedListener[] listeners = this.stateChangedListeners.getListeners(DetectorStateChangedListener.class);
         for(int i = 0; i < listeners.length; i++)
         {
             listeners[i].onDetectorStateChanged(event);
         }
     }
 
 
     /**
      * Enumerates all the possible status of the detector
      * 
      * @author Luciano Mammino, Andrea Mangano
      * @version 1.0
      */
     public enum DetectorState
     {
 	/**
 	 * Status acquired when the detector is stopped. It is the initial
 	 * status of the detector.
 	 */
 	STOPPED,
 
 	/**
 	 * Transitional status between {@link #STOPPED} and {@link #CAPTURING}
 	 */
 	STARTED,
 
 	/**
 	 * Status acquired when the stream is being processed
 	 */
 	CAPTURING,
 
 	/**
 	 * Status acquired for a certain amount of time when an abnormal
 	 * movement is detected
 	 */
 	WARNING
     }
 
 }
