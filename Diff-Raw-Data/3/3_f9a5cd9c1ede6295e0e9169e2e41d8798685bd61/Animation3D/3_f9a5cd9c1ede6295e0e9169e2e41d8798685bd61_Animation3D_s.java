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
      * Keypoints in the time domain
      */
     protected float[] mInput;
 
     /**
      * Keypoints in the target domain.
      */
     protected float[] mOutput;
 
     /**
      * The target array, this is normally a reference.
      */
     protected float[] mTarget;
 
     protected int mOutputIndex = 0;
     protected int mOutputStride = 0;
     protected int mOutputStartIndex = 0;
     protected int mTargetIndex = 0;
     /**
      * Number of targets
      */
     protected int mTargets;
 
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
      * Set to true to make values be added to existing values.
      */
     protected boolean mRelative[];
     /**
      * Set to true to ping-pong animation
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
 
     protected float[] mCurrentTime;
     protected int mInputIndex = 0;
     protected boolean mFinished = false;
     protected int mCurrentLoop = 0;
 
     protected boolean mTempLoop;
     /**
      * Set to true when looping and pingpong is set, this will go backwards.
      */
     protected boolean[] mReverse;
     protected float mTempMaxtime;
     protected int mTempMaxIndex;
 
     /**
      * Return the time in seconds when this animation starts.
      * @return
      */
     public float getAnimationStart() {
         return mInput[0];
     }
 
     /**
      * Return the time in seconds when this animation ends.
      * @return
      */
     public float getAnimationEnd() {
         return mInput[mInput.length-1];
     }
 
     /**
      * Return true of the animation is finished.
      * Note that the animation will not be considered as finished while loop is true.
      * @return True if the animation has finished (ie loop is not true and animation has reached end)
      */
     public boolean isFinished() {
         return mFinished;
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
      * Inits the animation with the specified number of targets and flags.
      * @param targets Number of target values (axes)
      * @param relative Array with target number of boolean values for relative flag.
      * @param pingPong Array with target number of boolean values for pingpong flag.
      */
     public void init(int targets, boolean[] relative, boolean[] pingPong) {
         mTargets = targets;
         if (mRelative == null || mRelative.length < targets) {
             mRelative = new boolean[targets];
             mPingPong = new boolean[targets];
             mReverse = new boolean[targets];
             mCurrentTime = new float[targets];
         }
         for (int i = 0; i < targets; i++) {
             mRelative[i] = relative[i];
             mPingPong[i] = pingPong[i];
             mReverse[i] = false;
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
         if (mReverse[index]) {
             mCurrentTime[index] -= deltaTime * mTimeFactor;
             mTempMaxtime = mInput[0];
             mTempMaxIndex = 0;
         } else {
             mCurrentTime[index] += deltaTime * mTimeFactor;
             mTempMaxtime = mInput[mInput.length-1];
             mTempMaxIndex = mInput.length - 2;
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
         if (!mFinished) {
             if (mPingPong[index]) {
                 if (!mReverse[index]) {
                     mCurrentTime[index] -= mCurrentTime[index] - mTempMaxtime;
                 } else {
                     mCurrentTime[index] = Math.abs(mCurrentTime[index]);
                     //Count one ping-pong from begin-end-begin as one loop.
                     mCurrentLoop++;
                 }
                 mReverse[index] = !mReverse[index];
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
     boolean calcCurrentIndex(float timeDelta, int index) {
         mTempLoop = false;
         updateTime(timeDelta, 0);
 
         if ((!mReverse[index] && mCurrentTime[index] > mTempMaxtime) ||
                 (mReverse[index] && mCurrentTime[index] < mTempMaxtime)) {
             //Reached end of animation
             if (mLoopCount == DISABLED) {
                 mFinished = true;
                 mInputIndex = mTempMaxIndex;
                 mCurrentTime[index] = mTempMaxtime;
                 return true;
             } else {
                 //Looping is enabled
                 mInputIndex = mTempMaxIndex;
                 if (mLoopCount != LOOP_INFINITE) {
                     if (mCurrentLoop >= mLoopCount -1) {
                         //End of animation.
                         mFinished = true;
                         mCurrentTime[index] = mTempMaxtime;
                     }
                 }
                 loopAnimation(index);
             }
 
             mTempLoop = true;
         }
 
        while (mCurrentTime[index] > mInput[mInputIndex + 1])
             mInputIndex++;
 
         mOutputIndex = mInputIndex * mOutputStride + mOutputStartIndex;
 
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
         mCurrentTime[0] = 0;
         mInputIndex = 0;
         mFinished = false;
         mCurrentLoop = 0;
     }
 
     public void reset() {
         for (int i = 0; i < mOutputStride; i++) {
             mCurrentTime[i] = 0;
             mReverse[i] = false;
         }
         mInputIndex = 0;
         mFinished = false;
         mOutputIndex = 0;
         mOutputStride = 0;
         mOutputStartIndex = 0;
         mTimeFactor = DEFAULT_TIMEFACTOR;
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
 //        mReverse = false;
     }
 
     @Override
     public void createObject(Object obj) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void destroyObject(Object obj) {
         // TODO Auto-generated method stub
 
     }
 
 }
