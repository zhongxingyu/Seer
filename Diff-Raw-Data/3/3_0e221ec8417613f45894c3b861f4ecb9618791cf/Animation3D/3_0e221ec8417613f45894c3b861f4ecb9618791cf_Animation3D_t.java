 /* Copyright 2012 Richard Sahlin
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 package com.super2k.openglen.animation;
 
 import com.super2k.openglen.objects.PooledObject;
 import com.super2k.openglen.utils.JavaUtils;
 
 
 /**
  * Base class for animations.
  * @author Richard Sahlin
  *
  */
 public abstract class Animation3D implements PooledObject {
 
 
     protected final static String INVALID_PARAMETER_STR = "Invalid parameter, null parameter, or sizes do not match.";
 
     /**
      * Value to specify that animation should loop forever.
      */
     public final static int LOOP_INFINITE = -1;
     /**
      * Value to specify that animation should not play.
      */
     public final static int DISABLED = 0;
 
     /**
      * Value to specify that animation should play once.
      * For other loop counts use the number of times the animation should loop,
      * eg 4 to play 4 times.
      */
     public final static int ONCE = 1;
 
     protected final static float DEFAULT_TIMEFACTOR = 1f;
 
     /**
      * User defined animation type. Is preserved when exporting animations.
      */
     protected int mAnimationType;
 
     /**
      * The key for pooled animation, or NOT_POOLED_OBJECT if not pooled.
      */
     protected int mKey = NOT_POOLED_OBJECT;
 
     /**
      * Type of class, can be used to determine what class animation is for pooled objects.
      */
     protected int mType;
 
     /**
      * Keypoints in the time domain, this contains the time values in seconds.
      * An animation with 3 targets and 2 keys will have:
      * [start1,start2,start3,end1,end2,end3]
      */
     protected float[] mInput;
 
     /**
      * Keypoints in the target domain.
      * These are the values that will be used to calculate the targets values
      * The values for each axis (target) is put together, eg for an animation that targets the x and y position with
      * 3 keys will have the following data:
      * [x1,y1,x2,y2,x3,y3]
      */
     protected float[] mOutput;
 
     /**
      * The target array, this is normally a reference.
      */
     protected float[] mTarget;
 
     protected int mTargetIndex = 0;
     /**
      * Number of targets, this is the number of axes/values that shall be animated.
      */
     protected int mTargets;
 
     /**
      * Number of keys in the input/output domain.
      */
     protected int mKeys;
 
     /**
      * Index to the last key, this is mTargets * (mKeys-1)
      */
     protected int mLastKeyIndex;
 
     /**
      * May be used by subclasses to keep track of z axis rotation.
      */
     protected float mZAxisRotation;
 
     /**
      * Max number of times an animation shall loop.
      * If != LOOP_INFINITE then this value limits number of times the animation can loop.
      * When this loop count is reached the animation shall not animate further
      * and report true when isFinished is called.
      */
     protected int mLoopCount = LOOP_INFINITE;
     /**
      * Controls if animation values are absolute or added to existing values.
      * Set to true to make values be added to existing values, one value for each target/axis
      */
     protected boolean mRelative[];
     /**
      * Set to true to ping-pong animation, one value for each target/axis
      */
     protected boolean mPingPong[];
     /**
      * Array that may be used to copy the contents of the target
      */
     protected float[] mResetArray;
 
     /**
      * Factor for time, this can be used for slowmotion of speedup of animation.
      */
     protected float mTimeFactor = DEFAULT_TIMEFACTOR;
 
     /*
      * --------------------------------
      * Runtime variables.
      * --------------------------------
      */
 
     /**
      * Used to fetch current values
      */
     protected float[] mCurrentValues;
     protected float[] mCurrentTime;
     /**
      * Index in input/output keys where the current values are read
      */
     protected int[] mInputIndex;
     /**
      * Holding the finished status for each target.
      */
     protected boolean[] mFinishedFlags;
     protected int mCurrentLoop = 0;
 
     protected boolean mTempLoop;
     /**
      * Set to true when looping and pingpong is set, this will go backwards.
      */
     protected int[] mDirection;
     protected float mTempMaxtime;
     protected int mTempMaxIndex;
 
     /**
      * Return the time in seconds when this animation starts.
      * If animation has multiple axes/target then the lowest start time is used.
      * @return The first animation time for this animation
      */
     public float getAnimationStart() {
         float time = mInput[0];
         for (int i = 1; i < mTargets; i++) {
             if (time > mInput[i]) {
                 time = mInput[i];
             }
         }
         return time;
     }
 
     /**
      * Return the time in seconds when this animation ends.
      * If animation has multiple axes/targets then the highest end time is used.
      * @return The last animation time for this animation.
      */
     public float getAnimationEnd() {
         float time = mInput[mLastKeyIndex];
         for (int i = 1; i < mTargets; i++) {
             if (time < mInput[mLastKeyIndex + i]) {
                 time = mInput[mLastKeyIndex + i];
             }
         }
         return time;
     }
 
     /**
      * Returns a ref to the target (output) array, this contains all output keys packed together.
      * If 2 axes are animated, the first 2 values will be start values for the 2 axes.
      * You must check the number of targets that are used by calling getTargets() - the timekey array
      * may contain more values than are used.
      * Note! This will return a ref to the array containing the target keys, any changes made will be
      * reflected in the animation.
      * @return Array containing the output keys, array may be larger than used values.
      */
     public float[] getTargetKeys() {
         return mOutput;
     }
 
     /**
      * Returns a ref to the keys in the time domain, this contains all timekeys packed together.
      * If 2 axes are animated, the first 2 values will be start time for the 2 axes.
      * You must check the number of targets that are used by calling getTargets() - the timekey array
      * may contain more values than are used.
      * Note! This will return a ref to the array containing the target keys, any changes made will be
      * reflected in the animation.
      * @return Array containing the time keys, array may be larger than used values.
      */
     public float[] getTimeKeys() {
         return mInput;
     }
 
     /**
      * Returns a ref to the array containing the ping pong flags.
      * Check number of values animated, this array may contain more values than are used.
      * Note! This will return a ref to the array containing the target keys, any changes made will be
      * reflected in the animation.
      * @return Array containing the ping-pong flags
      */
     public boolean[] getPingPong() {
         return mPingPong;
     }
 
     /**
      * Returns a ref to the array containing relative flags.
      * Check number of values animated, this array may contain more values than are used.
      * Note! This will return a ref to the array containing the target keys, any changes made will be
      * reflected in the animation.
      * @return Array containing the relative flags
      */
     public boolean[] getRelative() {
         return mRelative;
     }
 
     /**
      * Return true of the animation is finished.
      * Note that the animation will not be considered as finished while loop is true.
      * @return True if the animation has finished (ie loop is not true and animation has reached end for all axes)
      */
     public boolean isFinished() {
         for (int i = 0; i < mTargets; i++) {
             if (!mFinishedFlags[i]) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Checks wether the animation has looped or not, returns number of times the animation
      * has looped.
      * This value is reset when calling resetAnimation()
      * @return Number of times the animation has looped, 0 or more.
      */
     public int getCurrentLoop() {
         return mCurrentLoop;
     }
 
     /**
      * Set the time of the animation,
      * this will perform a resetAnimation followed by a call to animate() with the specified time.
      * This means that the animation will be affected by the timefactor value.
      * @param time
      */
     public void setTime(float time) {
         resetAnimation();
         animate(time);
     }
 
     /**
      * Initializes an animation with 2 keys (start and end)
      * There will be 2 timeKeys and targetKeys in this animation,
      * the timeKeys will start at startTime and end at stopTime
      * @param target Array with target values that shall animate
      * @param index Index into target array where target values are
      * @param start Array with start values
      * @param stop Array with end values
      * @param startTime Array with start times, one value for each target, in seconds
      * @param endTime Array with end times, one value for each target, in seconds
      * @param loop Should the animation loop or not LOOP_INFINITE to loop forever,
      * @param relative Array with relative flags for each axis
      * @param pingPong Array with ping-pong flags for each axis
      * LOOP_DISABLED to disable looping, or number of times animation shall loop.
      * @throws IllegalArgumentException If length of arrays do not match or is null, if index is < 0
      */
     protected void setup(float[] target, int index, float[] start, float[] end, float startTime[], float[] endTime,
             int loop, boolean[] relative, boolean[] pingPong) {
         setTarget(target, index);
         if (end == null || start.length != end.length || target.length < start.length ||
                 target.length < start.length + index) {
             throw new IllegalArgumentException(INVALID_PARAMETER_STR);
         }
         mLoopCount = loop;
         create(start.length, 2);
         //Set output.
         System.arraycopy(start, 0, mOutput, 0, mTargets);
         System.arraycopy(end, 0, mOutput, mTargets, mTargets);
         //Set input
         System.arraycopy(startTime, 0, mInput, 0, mTargets);
         System.arraycopy(endTime, 0, mInput, mTargets, mTargets);
         init(relative, pingPong);
     }
 
     /**
      * Creates the storage needed for the specified number of targets and keys.
      * The target and key count is set in this class.
      * @param targets
      * @param keys
      */
     protected void create(int targets, int keys) {
         mKeys = keys;
         mTargets = targets;
         mLastKeyIndex = mTargets * (mKeys -1);
         if (mCurrentTime == null || mCurrentTime.length < targets) {
             mInput = new float[mKeys * mTargets];
             mOutput = new float[mKeys * mTargets];
             mInputIndex = new int[mTargets];
             mFinishedFlags = new boolean[mTargets];
             mRelative = new boolean[mTargets];
             mPingPong = new boolean[mTargets];
             mDirection = new int[mTargets];
             mCurrentTime = new float[mTargets];
             mCurrentValues = new float[mTargets];
         }
     }
 
     /**
      * Inits the animation to the relative and pingpong flags specified, the animation must have been
      * setup with corresponding number of targets.
      * @param relative Array with target number of boolean values for relative flag.
      * @param pingPong Array with target number of boolean values for pingpong flag.
      * @throws IllegalArgumentException If length of relative and pingpong arrays do not match the
      * number of output keys.
      */
     protected void init(boolean[] relative, boolean[] pingPong) {
 
         if (relative.length > mTargets || pingPong.length > mTargets) {
             throw new IllegalArgumentException("Sizes do not match.");
         }
         for (int i = 0; i < mTargets; i++) {
             mRelative[i] = relative[i];
             mPingPong[i] = pingPong[i];
             mDirection[i] = 1;
             mCurrentTime[i] = 0;
         }
     }
 
 
 
     /**
      * Sets the timefactor, this can be use for slowmotion of speedup of animations.
      * Note that this field should be used in the animate() method to alter the incoming delta value.
      * @param factor Timefactor for animation speed, 1.0 is normal speed - lower values means slower animation.
      */
     public void setTimeFactor(float factor) {
         mTimeFactor = factor;
     }
 
     /**
      * Set the way values are considered when writing back,
      * if relative set to true then animation values are added to existing transform values.
      * If false the values from the animation is set in the transform.
      * @param relative
      * @param index Index to the axis to set
      */
     public void setRelative(boolean relative, int index) {
         mRelative[index] = relative;
     }
 
     /**
      * Return the type of animation,
      * this is something that is understood by the client. The animation type is simply a way for clients
      * to mark tag animations with a specific type that can later be retrieved.
      * The animation type is preserved when exporting the animation.
      * @return The type of this animation or -1 if not set.
      */
     public int getAnimationType() {
         return mAnimationType;
     }
 
     /**
      * Returns the target index, this can for instance be used when copying one animation.
      * @return Index into target array where output values are written.
      */
     public int getTargetIndex() {
         return mTargetIndex;
     }
 
     /**
      * Sets a (new) target for the animation, only call this if you know what you are doing.
      * This will discard the previous target and replace it with the new, may not be called
      * on a released object.
      * If the target array length is not sufficient an exception will occur when animating.
      * Caller must make sure target size matches that of the animation.
      * This method is intended to be used when switching target to a different sprite object, eg
      * swapping the position target in one SpriteObject with the position target of another
      * SpriteObject.
      * @param target The new target array - must contain enough values for existing animation.
      * @param index Index into target where values are set.
      * @throws IllegalArgumentException If index < 0
      */
     public void setTarget(float[] target, int index) {
         if (index < 0) {
             throw new IllegalArgumentException("Invalid target index: " + index);
         }
         if (target == null) {
             throw new IllegalArgumentException("Invalid target null.");
         }
         mTarget = target;
         mTargetIndex = index;
     }
 
     /**
      * Set the (user defined) animation type for this animation.
      * This is client specific data that can be used to group animations together.
      * The animation type is saved when exporting the animation.
      * @param type
      */
     public void setAnimationType(int type) {
         mAnimationType = type;
     }
 
     /**
      * Sets the max number of times the animation may loop, if looping is enabled.
      * @param count LOOP_INFINITE to loop forever, LOOP_DISABLED to disable looping, or
      * number of times animation shall loop.
      */
     public void setLoopCount(int count) {
         mLoopCount = count;
     }
 
     /**
      * Sets the ping-pong flag, if enabled, and looping is enabled, the animation will go backwards when end is reached.
      * Each turn at beginning or end will count up loopcount.
      * @param flag True to enable ping-pong, remember to set loopcount as well.
      * @param index Index to axis to set.
      */
     public void setPingPong(boolean flag, int index) {
         mPingPong[index] = flag;
     }
     /**
      * Fetches the max number of times this animation shall loop.
      * @return LOOP_INFINITE to loop forever, LOOP_DISABLED to disable looping, or
      * number of times animation shall loop.
      */
     public int getLoopCount() {
         return mLoopCount;
     }
 
     /**
      * Internal method to update the time, temp time and temp index
      * @param deltaTime
      */
     protected void updateTime(float deltaTime, int index) {
         if (mDirection[index] < 0) {
             mCurrentTime[index] -= deltaTime * mTimeFactor;
             mTempMaxtime = mInput[index];
             mTempMaxIndex = 0;
         } else {
             mCurrentTime[index] += deltaTime * mTimeFactor;
             mTempMaxtime = mInput[mLastKeyIndex + index];
             mTempMaxIndex = 0;
         }
 
     }
 
     /**
      * Copies that latest values into the specified data array.
      * This can be used to calculate direction of animation.
      * @param data Array where values are stored, must have room for number of axes at index.
      * @param index Index into data array where values are stored
      */
     public void getValues(float[] data, int index) {
         for (int i = 0; i < mTargets; i++) {
             data[index++] = mCurrentValues[i];
         }
     }
 
     /**
      * Internal method
      * Prepares animation for looping.
      * Checks pingpong flag, updates reverse flag according.
      * Call this method when animation shall loop, current time will be correct after calling this.
      * @param index Index to animation elements, 0 and upwards
      */
     protected void loopAnimation(int index) {
         //Check for pingpong
         if (!mFinishedFlags[index]) {
             if (mPingPong[index]) {
                 if (mDirection[index] > 0) {
                     mCurrentTime[index] -= mCurrentTime[index] - mTempMaxtime;
                 } else {
                     mCurrentTime[index] = Math.abs(mCurrentTime[index]);
                     //Count one ping-pong from begin-end-begin as one loop.
                     mCurrentLoop++;
                 }
                 mDirection[index] = -mDirection[index];
             } else {
                 while (mCurrentTime[index] > mTempMaxtime )
                     mCurrentTime[index] -= mTempMaxtime;
                 mCurrentLoop++;
             }
         }
 
     }
 
 
     /**
      * Calculate the currentTime and currentIndex values.
      * @param timeDelta
      * @param index index of the axis (value) to animate
      * @return True if animation reached end
      */
     protected boolean calcCurrentIndex(float timeDelta, int index) {
         mTempLoop = false;
         updateTime(timeDelta, index);
 
         if ((mDirection[index] > 0 && mCurrentTime[index] > mTempMaxtime) ||
                 (mDirection[index] < 0 && mCurrentTime[index] < mTempMaxtime)) {
             //Reached end of animation
             if (mLoopCount == DISABLED) {
                 mFinishedFlags[index] = true;
                 mCurrentTime[index] = mTempMaxtime;
                 return true;
             } else {
                 //Looping is enabled
                 mInputIndex[index] = mTempMaxIndex + index;
                 if (mLoopCount != LOOP_INFINITE) {
                     if (mCurrentLoop >= mLoopCount -1) {
                         //End of animation.
                         mFinishedFlags[index] = true;
                        mCurrentLoop = mLoopCount;
                     }
                 }
                 loopAnimation(index);
             }
             mTempLoop = true;
         }
 
         while ((mInputIndex[index] < mLastKeyIndex) && mCurrentTime[index] > mInput[mInputIndex[index] + mTargets]) {
             mInputIndex[index] += mTargets;
             if (mInputIndex[index] >= mLastKeyIndex) {
                 mInputIndex[index] -= mTargets;
                 break;
             }
         }
 
         return mTempLoop;
     }
 
     /**
      * Restore target values. This is normally done at end of animation if reset flag is set.
      * If no target has been saved this method does nothing.
      */
     public void restoreTarget() {
         if (mResetArray != null) {
             System.arraycopy(mResetArray, 0, mTarget, mTargetIndex, mTargets);
         }
     }
 
     /**
      * Saves the target values, may not work on all animations.
      */
     public void saveTarget() {
         if (mResetArray == null || mResetArray.length < mTargets) {
             mResetArray = new float[mTargets];
         }
         System.arraycopy(mTarget, mTargetIndex, mResetArray, 0, mTargets);
     }
 
     /**
      * This resets the animation, resets the internal current time value.
      * The animate() method can be called after this to produce the animation from start.
      */
     public void resetAnimation() {
         JavaUtils.clear(mCurrentTime, 0, mTargets);
         for (int i = 0; i < mTargets; i++) {
             mDirection[i] = 1;
             mInputIndex[i] = i;
             mFinishedFlags[i] = false;
         }
         mCurrentLoop = 0;
     }
 
     /**
      * Returns the number of target values (axes) that are animated.
      * @return Number values/axes that are animated.
      */
     public int getTargetCount() {
         return mTargets;
     }
 
     /**
      * Returns the number of keys in the time/value domain. A value of 2 will have 2 keys (start and end) and
      * each key having targetcount number of values.
      * @return Number of keys in the time/value domain.
      */
     public int getKeyCount() {
         return mKeys;
     }
 
     /**
      * Animate the target axis, current time will be updated with the timeDelta.
      * The timeDelta will be multiplied by the timeFactor.
      * @param timeDelta The time delta from last call to this method (or to resetAnimation()).
      * @return True if this animation reached the end of the animation, regardless of looping.
      * use isFinished() to determine if the animation has stopped.
      */
     public abstract boolean animate(float timeDelta);
 
     /**
      * Returns true if this is a relative animation,
      * if relative then values are added/subtracted from previous values.
      * @index Index to axis to query
      * @return True if animation is relative, false for absolute values.
      */
     public boolean isRelative(int index) {
         return mRelative[index];
     }
 
     /**
      * Rotates the animation on the Z axis, note that not all animations support rotation.
      * Animations that do support z axis rotation will only rotate 3 values, animation is
      * setup for more than 3 values (eg RGBA) the result is undefined.
      * @param angle Rotation on Z axis
      * @throws IllegalArgumentException If animation does not support rotation along z axis.
      */
     public void rotateZ(float angle) {
         mZAxisRotation = angle;
     }
 
     @Override
     public void setKey(int key) {
         mKey = key;
     }
 
     @Override
     public int getKey() {
         return mKey;
     }
 
     @Override
     public void setType(int type) {
         mType = type;
     }
 
     @Override
     public int getType() {
         return mType;
     }
 
 
     @Override
     public void releaseObject() {
         mTarget = null;
         mResetArray = null;
         mInput = null;
         mOutput = null;
         mZAxisRotation = 0;
     }
 
     @Override
     public void createObject(Object obj) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void destroy() {
         // TODO Auto-generated method stub
 
     }
 
 }
