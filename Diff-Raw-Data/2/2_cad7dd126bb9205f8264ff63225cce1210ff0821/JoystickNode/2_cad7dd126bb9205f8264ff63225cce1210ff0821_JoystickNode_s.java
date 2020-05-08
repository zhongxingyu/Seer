 /*********************************************************************
  *
  * Software License Agreement (BSD License)
  *
  *  Copyright (c) 2013, Willow Garage, Inc.
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without
  *  modification, are permitted provided that the following conditions
  *  are met:
  *
  *   * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *   * Neither the name of Willow Garage, Inc. nor the names of its
  *     contributors may be used to endorse or promote products derived
  *     from this software without specific prior written permission.
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  *  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  *  ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *  POSSIBILITY OF SUCH DAMAGE.
  *
  *********************************************************************/
 
 //Note: This class is a modification of the code snippets for class
 //"InputDeviceState" provided by nVidia at 
 //http://docs.nvidia.com/tegra/data/How_To_Support_Android_Game_Controllers.html
 
 package org.ros.android.shield_teleop;
 
 import android.util.Log;
 import android.view.InputDevice;
 import android.util.SparseIntArray;
 import android.view.InputDevice.MotionRange;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 
 import org.ros.namespace.GraphName;
 import org.ros.node.AbstractNodeMain;
 import org.ros.node.ConnectedNode;
 import org.ros.node.topic.Publisher;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import sensor_msgs.Joy;
 
 /**
 * This class encapsulates behavior of a nvidia Shield Joystick. Some other class
 * which can receive events (e.g. an android Activity or View) should make 
 * an object of this class a member and also override
 * dispatch*Event callbacks. As events are received, the callbacks should
 * invoke the appropriate methods in this class. 
 *
 * This class will evaluate the events to check that they are indeed
 * joystick events. If so, it will form sensor_msgs/Joy messages and
 * publish them.
 *
 * This class attempts to have exact same encoding for Joy messages
 * as the ps3joy + joy package on desktop linux.
 */
 public class JoystickNode extends AbstractNodeMain
 {
     private InputDevice         device_;
     private String              joystickTopic_;
     private Publisher<Joy>      joystickPublisher_;
     private int[]               axes_;
     private float[]             axesValues_;
     private SparseIntArray      keys_;
     private boolean             isInitialized_;
     private ConnectedNode       connectedNode_;
     private int                 messageSequenceNumber_;
 
     //Key codes of buttons on SHIELD
     private final int SHIELD_X_BUTTON           = 99;
     private final int SHIELD_Y_BUTTON           = 100;
     private final int SHIELD_B_BUTTON           = 97;
     private final int SHIELD_A_BUTTON           = 96;
     private final int SHIELD_L1_BUTTON          = 102;
     private final int SHIELD_R1_BUTTON          = 103;
     private final int SHIELD_LEFT_STICK_BUTTON  = 106;
     private final int SHIELD_RIGHT_STICK_BUTTON = 107;
 
     //Indices into axesValues_ array
     private final int SHIELD_LEFT_STICK_X_AXIS_INDEX = 0;  //Left = -1.0, Right = 1.0
     private final int SHIELD_LEFT_STICK_Y_AXIS_INDEX = 1;  //Up = -1.0, Down = 1.0
     private final int SHIELD_L2_AXIS_INDEX = 2;            //Neutral = 0, Pressed = 1.0
     private final int SHIELD_RIGHT_STICK_X_AXIS_INDEX = 3; //Left = -1.0, Right = 1.0
     private final int SHIELD_RIGHT_STICK_Y_AXIS_INDEX = 4; //Up = -1.0, Right = 1.0
     private final int SHIELD_R2_AXIS_INDEX = 5;            //Neutral = 0, Pressed = 1.0
     private final int SHIELD_DPAD_X_AXIS_INDEX = 6;        //Left = -1.0, Right = 1.0 -- Note: This seems to be binary, not continuous
     private final int SHIELD_DPAD_Y_AXIS_INDEX = 7;        //Up = -1.0, Down = 1.0 -- Note: This seems to be binary, not continuous
 
     /**
     * The constructor for this class
     */
     public JoystickNode(String joystickTopic)
     /*************************************************************************/
     {
         isInitialized_ = false;
         messageSequenceNumber_ = 0;
         joystickTopic_ = joystickTopic;
     }
 
     /**
     * Indicates if the intializeDevice() call has been invoked.
     */
     public boolean isInitialized()
     /*************************************************************************/
     {
         return isInitialized_;
     }
 
     /**
     * Called the first time the user actually hits one of the joystick buttons.
     * @param device - the Android InputDevice instance representing the
     * joystick
     */
     public void initializeDevice(InputDevice device)
     /*************************************************************************/
     {
         device_ = device;
 
         //Determine number of axis available
         int numAxes = 0;
         for (MotionRange range : device.getMotionRanges())
         {
             if ((range.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) 
                 numAxes += 1;
         }
 
         //Allocate storage for axes and key values
         axes_        = new int[numAxes];          //Each value indicates axis ID
         axesValues_  = new float[numAxes];        //One-to-one correspondence with axes_, contains latest value for each axis
         keys_        = new SparseIntArray();      //Maps buttonID to current state.
 
         //Determine axis IDs and store into axes_ member
         int i = 0;
         for (MotionRange range : device.getMotionRanges())
         {
             if ((range.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) 
             {
                axes_[i++] = range.getAxis();
             }
         }    
 
         isInitialized_ = true;    
     }
 
     /**
     * Required by rosjava's AbstractNodeMain class
     */
     @Override
     public GraphName getDefaultNodeName()
     /*************************************************************************/
     {
         return GraphName.of("ShieldTeleop/JoystickNode");
     }
 
     /**
     * Invoked by rosjava framework when this node connects to a ROS master
     */
     @Override
     public void onStart(ConnectedNode connectedNode)
     /*************************************************************************/
     {
         super.onStart(connectedNode);
         connectedNode_ = connectedNode;
         Log.d("JoystickNode", "JoystickNode: Connected to ROS master. Creating publisher object...");
         joystickPublisher_ = connectedNode.newPublisher(joystickTopic_, sensor_msgs.Joy._TYPE);
 
         Timer publisherTimer = new Timer();
         publisherTimer.schedule(new TimerTask() { //Note: This is a very interesting feature of Java, anonymous classes! Overriding a method for an object of type TimerTask without having to subclass explicitly!
             @Override
             public void run()
             {
                 publishLatestJoystickMessage();
             }
         }, 0, 100);
     }
 
     /**
     * Normalizes joystick values between -1.0 and 1.0. Though Android guarantees
     * this, it is a useful function if one finds that the joystick values are not
     * correct. Doesn't do any harm to call it.
     */
     public static float processAxis(InputDevice.MotionRange range, float axisvalue) 
     /*************************************************************************/
     {
         float absAxisValue = Math.abs(axisvalue);
         float deadZone = range.getFlat();
         if (absAxisValue <= deadZone)
         {
             return 0.0f;
         }
         float normalizedValue;
         if (axisvalue < 0.0f) 
         {
             normalizedValue = absAxisValue / range.getMin();
         } 
         else 
         {
             normalizedValue = absAxisValue / range.getMax();
         }
 
         return normalizedValue;
     } 
 
     /**
     * Determines if a key code corresponds to a joystick button or not
     */
     private static boolean isGameKey(int keyCode) 
     /*************************************************************************/
     {
         switch (keyCode) 
         {
             case KeyEvent.KEYCODE_DPAD_UP:
             case KeyEvent.KEYCODE_DPAD_DOWN:
             case KeyEvent.KEYCODE_DPAD_LEFT:
             case KeyEvent.KEYCODE_DPAD_RIGHT:
                 return true;
             default:
                 return KeyEvent.isGamepadButton(keyCode);
         }
     }
 
     /**
     * Computes the axes component of a sensor_msgs/Joy message using PS3
     * controller as reference
     */
     private synchronized float[] computePS3AxesArrayFromShieldData()
     /*************************************************************************/
     {
         final int NUM_PS3_AXES = 27;
         float[] ps3Axes = new float[NUM_PS3_AXES];
 
         ps3Axes[0]  = -axesValues_[SHIELD_LEFT_STICK_X_AXIS_INDEX]; //Left Stick X Axis (1 = left, right = -1)
         ps3Axes[1]  = -axesValues_[SHIELD_LEFT_STICK_Y_AXIS_INDEX]; //Left Stick Y Axis (1 = up, down = -1)
         ps3Axes[2]  = -axesValues_[SHIELD_RIGHT_STICK_X_AXIS_INDEX]; //Right Stick X Axis (1 = left, right = -1)
         ps3Axes[3]  = -axesValues_[SHIELD_RIGHT_STICK_Y_AXIS_INDEX]; //Right Stick Y Axis (1 = up, down = -1)
         ps3Axes[4]  = 0.0f; //? (0)
         ps3Axes[5]  = 0.0f; //? (0)
         ps3Axes[6]  = 0.0f; //? (0)
 
         //TODO: WHERE IS DPAD_X_AXIS LEFT?? The controller I had was broken
         ps3Axes[7]  = 0.0f; //? (0)
         ps3Axes[8]  = axesValues_[SHIELD_DPAD_Y_AXIS_INDEX] <= -1.0f ? -1.0f : 1.0f; //DPad Up (1.0 neutral, -1.0 depressed)
         ps3Axes[9]  = axesValues_[SHIELD_DPAD_X_AXIS_INDEX] >= 1.0f ? -1.0f : 1.0f; //DPad Right (1.0 neutral, -1.0 depressed)
         ps3Axes[10] = axesValues_[SHIELD_DPAD_Y_AXIS_INDEX] >= 1.0f ? -1.0f : 1.0f; //DPad Down (1.0 neutral, -1.0 depressed)
         ps3Axes[11] = 0.0f; //? (0)
         ps3Axes[12] = -normalize(axesValues_[SHIELD_L2_AXIS_INDEX], 0, 1); //L2 (Far back Left), (1.0 neutral, -1.0 depressed)
         ps3Axes[13] = -normalize(axesValues_[SHIELD_R2_AXIS_INDEX], 0, 1); //R2 (Far back Right), (1.0 neutral, -1.0 depressed)
         ps3Axes[14] = keys_.get(SHIELD_L1_BUTTON, 0) == 0 ? 1.0f : -1.0f; //L1 (Front back Left), (1.0 neutral, -1.0 depressed)
         ps3Axes[15] = keys_.get(SHIELD_R1_BUTTON, 0) == 0 ? 1.0f : -1.0f; //R1 (Front back Right), (1.0 neutral, -1.0 depressed)
         ps3Axes[16] = keys_.get(SHIELD_Y_BUTTON, 0) == 0 ? 1.0f: -1.0f; //Triangle (Up), (1.0 neutral, -1.0 depressed)
         ps3Axes[17] = keys_.get(SHIELD_B_BUTTON, 0) == 0 ? 1.0f: -1.0f; //Circle (Right), (1.0 neutral, -1.0 depressed)
         ps3Axes[18] = keys_.get(SHIELD_A_BUTTON, 0) == 0 ? 1.0f: -1.0f; //X (Down), (1.0 neutral, -1.0 depressed)
         ps3Axes[19] = keys_.get(SHIELD_X_BUTTON, 0) == 0 ? 1.0f: -1.0f; //Square (Left), (1.0 neutral, -1.0 depressed)
         ps3Axes[20] = 0.0f; //? (0)
         ps3Axes[21] = 0.0f; //? (0)
         ps3Axes[22] = 0.0f; //? (0)
         ps3Axes[23] = 0.0f; //? (0)
         ps3Axes[24] = 0.0f; //? (0)
         ps3Axes[25] = 0.0f; //Controller Accelerometer/Gyro or something
         ps3Axes[26] = 0.0f; //? (0)
 
 
         return ps3Axes;
     }
 
     /**
     * Computes the buttons component of a sensor_msgs/Joy message using PS3
     * controller as reference
     */
     private synchronized int[] computerPS3ButtonsArrayFromShieldData()
     /*************************************************************************/
     {
         final int NUM_PS3_BUTTONS = 19;
 
 
         int[] ps3Buttons = new int[NUM_PS3_BUTTONS];
         ps3Buttons[0] = 0; //Select Button
         ps3Buttons[1] = keys_.get(SHIELD_LEFT_STICK_BUTTON, 0); //Stick Left
         ps3Buttons[2] = keys_.get(SHIELD_RIGHT_STICK_BUTTON, 0); //Stick Right
         ps3Buttons[3] = 0; //Start
 
         ps3Buttons[4] = axesValues_[SHIELD_DPAD_Y_AXIS_INDEX] <= -1.0f ? 1 : 0; //DPad Up
         ps3Buttons[5] = axesValues_[SHIELD_DPAD_X_AXIS_INDEX] >= 1.0f ? 1 : 0; //DPad Right
         ps3Buttons[6] = axesValues_[SHIELD_DPAD_Y_AXIS_INDEX] >= 1.0f ? 1 : 0; //DPad Down
        ps3Buttons[7] = 0; //DPad Left //TODO: This needs to figured out!
 
         ps3Buttons[8]  = roundToZeroIfNecessary(axesValues_[SHIELD_L2_AXIS_INDEX]) <= 0.01 ? 0 : 1; //Rear Left 2  (L2) (Far back Left)
         ps3Buttons[9]  = roundToZeroIfNecessary(axesValues_[SHIELD_R2_AXIS_INDEX]) <= 0.01 ? 0 : 1; //Rear Right 2 (R2) (Far back right)
         ps3Buttons[10] = keys_.get(SHIELD_L1_BUTTON, 0); //Rear Left 1
         ps3Buttons[11] = keys_.get(SHIELD_R1_BUTTON, 0); //Rear Right 1
         ps3Buttons[12] = keys_.get(SHIELD_Y_BUTTON, 0); //Triangle (up)
         ps3Buttons[13] = keys_.get(SHIELD_B_BUTTON, 0); //Circle (right)
         ps3Buttons[14] = keys_.get(SHIELD_A_BUTTON, 0); //X (down)
         ps3Buttons[15] = keys_.get(SHIELD_X_BUTTON, 0); //Square (left)
         ps3Buttons[16] = 0; //Pairing Button
         ps3Buttons[17] = 0; //?
         ps3Buttons[18] = 0; //?
 
         return ps3Buttons;
     }
 
     /**
     * Populates a sensor_msgs/Joy message with the current joystick state
     * and publishes it. This method attempts to mimic the properties of a
     * PS3 controller
     */
     public synchronized void publishLatestJoystickMessage()
     /*************************************************************************/
     {
         if (joystickPublisher_ == null || !isInitialized())
             return;
 
         sensor_msgs.Joy joyMsg = joystickPublisher_.newMessage();
 
         joyMsg.getHeader().setFrameId("None");
         joyMsg.getHeader().setSeq(messageSequenceNumber_++);
         joyMsg.getHeader().setStamp(connectedNode_.getCurrentTime());
 
         joyMsg.setAxes(computePS3AxesArrayFromShieldData());
         joyMsg.setButtons(computerPS3ButtonsArrayFromShieldData());
 
         joystickPublisher_.publish(joyMsg);
     }
 
     /**
     * This method should be called from a holding widget that can intercept
     * Android input events when a joystick button is pressed
     */
     public boolean onKeyDown(KeyEvent event) 
     /*************************************************************************/
     {
 
         int keyCode = event.getKeyCode();
         Log.d("JoystickNode", "JoystickNode: onKeyDown with keyCode = " + Integer.toString(keyCode));
         if (event.getRepeatCount() == 0) 
         {
             if (isGameKey(keyCode)) 
             {
                 keys_.put(keyCode, 1);
                 publishLatestJoystickMessage();
                 return true;
             }
         }
         return false;
     }
 
     /**
     * This method should be called from a holding widget that can intercept
     * Android input events when a joystick button is released
     */
     public boolean onKeyUp(KeyEvent event) 
     /*************************************************************************/
     {
         int keyCode = event.getKeyCode();
         Log.d("JoystickNode", "JoystickNode: onKeyUp with keyCode = " + Integer.toString(keyCode));
         if (isGameKey(keyCode)) 
         {
             keys_.put(keyCode, 0);
             publishLatestJoystickMessage();
             return true;
         }
         return false;
     }
 
     /**
     * Rounds a number to zero if it's close enough to zero. Mainly used
     * used to make sure fields in sensor_msgs/Joy don't look like
     * 1.336346346E-10
     */
     private float roundToZeroIfNecessary(float val)
     /*************************************************************************/
     {
         final float UPPER_THRESHOLD = 0.01f;
         if (Math.abs(val) < UPPER_THRESHOLD)
             return 0.0f;
         return val;
     }
 
     /**
     * This method should be called from a holding widget that can intercept
     * Android input events when a joystick axis control is manipulated
     */
     public boolean onJoystickMotion(MotionEvent event) 
     /*************************************************************************/
     {
         if ((event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) == 0)
         {
             return false;
         }
 
         //Update the latest axis value for each axis. The MotionEvent object contains the state of each axis.
         for (int i = 0; i < axes_.length; i++)
         {
             int axisId = axes_[i];
             float axisVal = roundToZeroIfNecessary(event.getAxisValue(axisId));
             axesValues_[i] = axisVal;
         }
 
         publishLatestJoystickMessage();
 
         return true;
     } 
 
     /**
     * Normalizes a values between -1 and 1 where -1 corresponds to scaleMin and
     * 1 corresponds to scaleMax. scaleMin can be larger than scaleMax if we want
     * to invert things
     */
     private static float normalize(float val, float scaleMin, float scaleMax)
     /*****************************************************************************/
     {
        float toReturn = (((val - scaleMin) / (scaleMax - scaleMin)) * 2.0f) - 1.0f;
        if(toReturn < -1.0f)
           return(-1.0f);
        else if (toReturn > 1)
           return(1.0f);
        else
           return(toReturn);
     }
 
 
 
 }
