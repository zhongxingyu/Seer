 package com.platypus.crw;
 
 import com.platypus.crw.data.SensorData;
 import com.platypus.crw.data.Twist;
 import com.platypus.crw.data.Utm;
 import com.platypus.crw.data.UtmPose;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import robotutils.Pose3D;
 import robotutils.Quaternion;
 
 /**
  * A simple simulation of an unmanned boat.
  * 
  * The vehicle is fixed on the ground (Z = 0.0), and can only turn along the
  * Z-axis and move along the X-axis (a unicycle motion model). Imagery and
  * sensor are simulated using simple artificial generator functions that produce
  * recognizable basic patterns.
  * 
  * Implementation of RosVehicleActionServer and RosVehicleActionClient
  * 
  * @author pkv
  * @author kss
  * 
  */
 public class SimpleBoatSimulator extends AbstractVehicleServer {
 
     private static final Logger logger = Logger.getLogger(SimpleBoatSimulator.class.getName());
     public static final int UPDATE_INTERVAL_MS = 100;
     
     public final SensorType[] _sensorTypes = new SensorType[3];
     public UtmPose _utmPose = new UtmPose(new Pose3D(476608.34, 4671214.40, 172.35, 0, 0, 0), new Utm(17, true));
     public Twist _velocity = new Twist();
     public UtmPose[] _waypoints = new UtmPose[0];
     
     protected final Object _captureLock = new Object();
     protected TimerTask _captureTask = null;
     
     protected final Object _navigationLock = new Object();
     protected TimerTask _navigationTask = null;
     
     protected final AtomicBoolean _isAutonomous = new AtomicBoolean(true);
     protected final Timer _timer = new Timer();
     
     protected final TimerTask _updateTask = new TimerTask() {
         final double dt = UPDATE_INTERVAL_MS / 1000.0;
         
         @Override
         public void run() {
             
             // Move in an arc with given velocity over time interval
             double yaw = _utmPose.pose.getRotation().toYaw();
             double x = _utmPose.pose.getX() + _velocity.dx() * Math.cos(yaw) * dt;
             double y = _utmPose.pose.getY() + _velocity.dx() * Math.sin(yaw) * dt;
             Quaternion q = Quaternion.fromEulerAngles(0, 0, yaw + _velocity.drz() * dt);
             _utmPose.pose = new Pose3D(x, y, _utmPose.pose.getZ(), q);
 
             // Send out pose updates
             UtmPose pose = _utmPose.clone();
             sendState(pose);
 
             // Send out velocity updates
             Twist velocity = _velocity.clone();
             sendVelocity(velocity);
 
             // Generate simulated sensor data
             SensorData reading = new SensorData();
             reading.data = new double[3];
             reading.type = SensorType.TE;
 
             Random random = new Random();
             reading.data[0] = (_utmPose.pose.getX()) + 10 * random.nextGaussian();
             reading.data[1] = (_utmPose.pose.getY());
             reading.data[2] = (_utmPose.pose.getZ());
 
             sendSensor(0, reading);
         }
     };
     
     public SimpleBoatSimulator() {
         // Start the internal update process
         _timer.scheduleAtFixedRate(_updateTask, 0, UPDATE_INTERVAL_MS);
     }
 
     @Override
     public byte[] captureImage(int width, int height) {
 
         // Create an image of the correct size
         BufferedImage image = new BufferedImage(width, height,
                 BufferedImage.TYPE_3BYTE_BGR);
         Graphics2D graphics = (Graphics2D) image.getGraphics();
         graphics.setPaint(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
         graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
 
         // Fill it with random noise!
         Random rand = new Random();
         int[] data = new int[width * height * 3];
         for (int i = 0; i < data.length; ++i) {
             data[i] = rand.nextInt();
         }
         
         // Copy raw data to the image's raster
         image.getRaster().setPixels(0, 0, width, height, data);
         
         return toCompressedImage(image);
     }
 
     @Override
     public SensorType getSensorType(int channel) {
         return _sensorTypes[channel];
     }
 
     @Override
     public UtmPose[] getWaypoints() {
         synchronized (_navigationLock) {
             return _waypoints;
         }
     }
 
     @Override
     public void setSensorType(int channel, SensorType type) {
         _sensorTypes[channel] = type;
     }
 
     @Override
     public void setPose(UtmPose state) {
         _utmPose = state.clone();
     }
 
     @Override
     public void startWaypoints(final UtmPose[] waypoints, final String controller) {
         logger.log(Level.INFO, "Starting waypoints: {0}", Arrays.toString(waypoints));
         
         // Create a waypoint navigation task
         TimerTask newNavigationTask = new TimerTask() {
             final double dt = (double) UPDATE_INTERVAL_MS / 1000.0;
 
             // Retrieve the appropriate controller in initializer
             VehicleController vc = SimpleBoatController.POINT_AND_SHOOT.controller;
             {
                 try {
                     vc = SimpleBoatController.valueOf(controller).controller;
                 } catch (IllegalArgumentException e) {
                     logger.log(Level.WARNING, "Unknown controller specified (using {0} instead): {1}", new Object[]{vc, controller});
                 }
             }
 
             @Override
             public void run() {
                 synchronized (_navigationLock) {
                     if (!_isAutonomous.get()) {
                         // If we are not autonomous, do nothing
                         sendWaypointUpdate(WaypointState.PAUSED);
                        return;
                     } else if (_waypoints.length == 0) {
                         // If we are finished with waypoints, stop in place
                         sendWaypointUpdate(WaypointState.DONE);
                         setVelocity(new Twist());
                         this.cancel();
                         _navigationTask = null;
                     } else {
                         // If we are still executing waypoints, use a 
                         // controller to figure out how to get to waypoint
                         // TODO: measure dt directly instead of approximating
                         vc.update(SimpleBoatSimulator.this, dt);
                         sendWaypointUpdate(WaypointState.GOING);
                     }
                 }
             }
         };
         
         synchronized (_navigationLock) {
             // Change waypoints to new set of waypoints
             _waypoints = Arrays.copyOf(waypoints, waypoints.length);
 
             // Cancel any previous navigation tasks
             if (_navigationTask != null)
                 _navigationTask.cancel();            
             
             // Schedule this task for execution
             _navigationTask = newNavigationTask;
             _timer.scheduleAtFixedRate(_navigationTask, 0, UPDATE_INTERVAL_MS);
         }
     }
 
     @Override
     public void stopWaypoints() {
         // Stop the thread that is doing the "navigation" by terminating its
         // navigation process, clear all the waypoints, and stop the vehicle.
         synchronized (_navigationLock) {
             if (_navigationTask != null) {
                 _navigationTask.cancel();
                 _navigationTask = null;
                 _waypoints = new UtmPose[0];
                 setVelocity(new Twist());
             }
         }
         sendWaypointUpdate(WaypointState.CANCELLED);
     }
 
     @Override
     public WaypointState getWaypointStatus() {
         synchronized (_navigationLock) {
             if (_waypoints.length > 0) {
                 return _isAutonomous.get() ? WaypointState.PAUSED : WaypointState.GOING;
             } else {
                 return WaypointState.DONE;
             }
         }
     }
 
     @Override
     public void startCamera(final int numFrames, final double interval, final int width, final int height) {
         logger.log(Level.INFO, "Starting capture: {0} ({1}x{2}) frames @ {3}s ", new Object[]{numFrames, width, height, interval});
         
         // Create a camera capture task
         TimerTask newCaptureTask = new TimerTask() {
             int iFrame = 0;
 
             @Override
             public void run() {
                 synchronized (_captureLock) {
                     // Take a new image and send it out
                     sendImage(captureImage(width, height));
                     iFrame++;
 
                     // If we exceed numFrames, we finished
                     if (numFrames > 0 && iFrame >= numFrames) {
                         sendCameraUpdate(CameraState.DONE);
                         this.cancel();
                         _captureTask = null;
                     } else {
                         sendCameraUpdate(CameraState.CAPTURING);
                     }
                 }
             }
         };
         
         synchronized (_captureLock) {
             // Cancel any previous capture tasks
             if (_captureTask != null)
                 _captureTask.cancel();
             
             // Schedule this task for execution
             _captureTask = newCaptureTask;
             _timer.scheduleAtFixedRate(_captureTask, 0, (long)(interval * 1000.0));
         }
     }
 
     @Override
     public void stopCamera() {
         // Stop the thread that sends out images by terminating its
         // navigation flag and then removing the reference to the old flag.
         synchronized (_captureLock) {
             if (_captureTask != null) { 
                 _captureTask.cancel();
                 _captureTask = null;
             }
         }
         sendCameraUpdate(CameraState.CANCELLED);
     }
 
     @Override
     public CameraState getCameraStatus() {
         synchronized (_captureLock) {
             if (_captureTask != null) {
                 return CameraState.CAPTURING;
             } else {
                 return CameraState.OFF;
             }
         }
     }
 
     @Override
     public UtmPose getPose() {
         return _utmPose.clone();
     }
 
     @Override
     public int getNumSensors() {
         return _sensorTypes.length;
     }
 
     @Override
     public void setVelocity(Twist velocity) {
         _velocity = velocity.clone();
     }
 
     @Override
     public Twist getVelocity() {
         return _velocity.clone();
     }
 
     @Override
     public boolean isAutonomous() {
         return _isAutonomous.get();
     }
 
     @Override
     public void setAutonomous(boolean auto) {
         _isAutonomous.set(auto);
         _velocity = new Twist(); // Reset velocity when changing modes
     }
 
     @Override
     public boolean isConnected() {
         // The simulated vehicle will always be connected
         return true;
     }
     
     /**
      * Terminates internal update processes and threads.
      */
     public void shutdown() {
         _timer.cancel();
         _timer.purge();
     }
 }
