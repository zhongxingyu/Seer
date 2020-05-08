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
 
 import com.super2k.openglen.geometry.Vector3;
 import com.super2k.openglen.utils.JavaUtils;
 
 
 /**
  * Creates a Sinus based animation for the specified target, if rotation is used
  * then must use 3 or fewer elements.
  * @author Richard Sahlin
  *
  */
 public class SinusAnimation extends Animation3D {
 
     protected float[] mAmplitude;   //Amplitude for each value,
                                     //this is multiplied by the sine value.
     protected float[] mStartTime;   //Start time for each value
     protected float[] mEndTime;     //End time for each value
     protected float[] mTimeFactor;  //Time factor for each value, this is a speed factor -
                                     //smaller values than 1 will make it go slower,
                                     //larger values gives faster animation.
     protected float[] mTime;        //Time values for each destination after timefactor adjust
                                     //goes from mStartTime to mStartTime + (1/timefactor)
     protected float[] mSineAdd;     //Values added after sinus function,
                                     //before amplitude multiplication.
                                     //Eg a value of 0.5, will offset the curve + 0.5 * amplitude.
     protected int[] mDirection;
     /**
      * Store sinus values here and rotate before moving to target
      */
     protected float[] mSinus;
     /**
      * Increase for each axis that loop
      */
     protected int mTempCurrentLoop = 0;
 
     @Override
     protected void updateTime(float deltaTime, int index) {
         mTime[index] += deltaTime * mTimeFactor[index] * mDirection[index];
     }
 
     @Override
     protected void loopAnimation(int index) {
         if (mPingPong[index]) {
             //Count one loop as animation + animation in reverse
             if (mDirection[index] < 0) {
                 increaseCurrentLoop();
                if (mLoopCount != LOOP_INFINITE && mCurrentLoop >= mLoopCount) {
                    mFinished = true;
                    mTime[index] = mStartTime[index];
                    return;
                }
             }
             if(mDirection[index] < 0) {
                 mTime[index] = mStartTime[index] + (mStartTime[index] - mTime[index]);
             } else {
                 mTime[index] -= mTime[index] - mEndTime[index];
             }
             mDirection[index] = -mDirection[index];
         } else {
             mTime[index] -= mEndTime[index];
             increaseCurrentLoop();
            if (mLoopCount != LOOP_INFINITE && mCurrentLoop >= mLoopCount) {
                mFinished = true;
                mTime[index] = mEndTime[index];
            }
         }
     }
 
     /**
      * Internal method, call this when one axis has looped to keep track when all axes has looped.
      */
     protected void increaseCurrentLoop() {
         mTempCurrentLoop++;
         if (mTempCurrentLoop >= mStartTime.length) {
             mCurrentLoop++;
             mTempCurrentLoop = 0;
         }
     }
 
     @Override
     public void resetAnimation() {
         super.resetAnimation();
         mTempCurrentLoop = 0;
     }
 
     @Override
     public boolean animate(float timeDelta) {
 
         if (mFinished) {
             return false;
         }
         boolean end = false;
         int len = mStartTime.length;
         for (int i = 0; i < len; i++) {
             //Go through each destination
             updateTime(timeDelta, i);
             if (mDirection[i] > 0 && mTime[i] >= mEndTime[i] ||
                     mDirection[i] < 0 && mTime[i] <= mStartTime[i]) {
                 end = true;
                 loopAnimation(i);
             }
             if (mTime[i] >= mStartTime[i]) {
                 float offset = 0;
                 if (mSineAdd != null) {
                     offset = mSineAdd[i];
                 }
                 float value = mTime[i] - mStartTime[i];
                 float sin = (float) Math.sin(value);
                 mSinus[i] = (sin + offset) * mAmplitude[i];
             }
         }
         if (mSinus.length >= 2 && mZAxisRotation != 0) {
             Vector3.rotateZAxis(mSinus, mZAxisRotation);
         }
         for (int i = 0; i < len; i++) {
             if (!mRelative[i]) {
                 mTarget[i + mTargetIndex] = mSinus[i];
             } else {
                 mTarget[i + mTargetIndex] += mSinus[i];
             }
         }
         return end;
     }
 
     /**
      * Creates the time and starttime arrays.
      * TODO: Create as one array and index into it.
      * @param inValues Number of in-values.
      */
     protected void init(int inValues) {
         if (mTime == null || mTime.length < inValues) {
             mTime = new float[inValues];
             mStartTime = new float[inValues];
             mEndTime = new float[inValues];
             mSinus = new float[inValues];
             mSineAdd = new float[inValues];
             mTimeFactor = new float[inValues];
             mAmplitude = new float[inValues];
         } else {
             JavaUtils.clear(mTime,  0,  inValues);
         }
     }
 
     /**
      * Default constructor, used by factory
      */
     public SinusAnimation() {
         super();
         mType = AnimationID.SINUS_ID;
     }
 
     /**
      * Creates a sine animation, animation will loop at time 2 * PI radians.
      * The target values may have different sine offsets, speed and amplitude.
      * @param target The target for the animation, must have at least the same length as in values.
      * @param index Index into target array where target is
      * @param startTime Start time, idle time before sinewave. Affected by timeFactor.
      * @param amplitude The amplitude of output values
      * @param timeFactor Time factor, value of 1 is normal, lower values slower.
      * @param sineOffset Offset factor for sine.
      * @throws IllegalArgumentException If array sizes do not add up or is null, if index < 0
      */
     public SinusAnimation(float[] target, int index, float[] startTime, float[] amplitude, float[] timeFactor,
             float[] sineOffset) {
         setup(target, index, startTime, null, amplitude, timeFactor, sineOffset);
     }
 
     /**
      * Creates a sine animation, animation will loop at endTime
      * The target values may have different sine offsets, speed and amplitude.
      * @param target The target for the animation, must have at least the same length as in values.
      * @param index Index into target array where target is
      * @param startTime Start time, idle time before sinewave. Affected by timeFactor.
      * @param endTime Endtime (loop) of animation.
      * @param amplitude The amplitude of output values
      * @param timeFactor Time factor, value of 1 is normal, lower values slower.
      * @param sineOffset Offset factor for sine.
      * @throws IllegalArgumentException If array sizes do not add up or is null, if index < 0
      */
     public SinusAnimation(float[] target, int index, float[] startTime, float[] endTime, float[] amplitude,
             float[] timeFactor, float[] sineOffset) {
         setup(target, index, startTime, endTime, amplitude, timeFactor, sineOffset);
     }
 
     /**
      * Initializes the animation, how many values are used for target depends on the number of values in startTime
      * array, if it contains 2 values then 2 values are written to target.
      * @param target
      * @param index Index into target array where target values are.
      * @param startTime
      * @param endTime Endtime of animation, may be null. If so 2 * PI is used for one full sinewave.
      * @param amplitude
      * @param timeFactor
      * @param sineOffset
      * @throws IllegalArgumentException If array is null, if index is < 0
      */
     public void setup(float[] target, int index, float[] startTime, float[] endTime, float[] amplitude,
                         float[] timeFactor, float[] sineOffset) {
         setTarget(target, index);
         if (startTime == null || amplitude == null || timeFactor == null || sineOffset == null) {
             throw new IllegalArgumentException(INVALID_PARAMETER_STR);
         }
         int l = startTime.length;
         init(l);
         System.arraycopy(amplitude, 0, mAmplitude, 0, l);
         System.arraycopy(timeFactor, 0, mTimeFactor, 0, l);
         System.arraycopy(sineOffset, 0, mSineAdd, 0, l);
         System.arraycopy(endTime, 0, mEndTime, 0, l);
         System.arraycopy(startTime, 0, mStartTime, 0, l);
         if (mDirection == null || mDirection.length < l) {
             mDirection = new int[l];
         }
         for (int i = 0; i < l; i++) {
             mDirection[i] = 1;
         }
         mTempCurrentLoop = 0;
     }
 
     /**
      * Sets the timefactor for the specified index, this will affect the time factor
      * for target index-
      * @param index Target index to change time factor for.
      * @param factor Time factor
      * @throws IllegalArgumentException If index is outside time factor array.
      * Size of time factor array is decided when animation is created.
      */
     public void setTimeFactor(int index, float factor) {
         if (index > 0 || index >= mTimeFactor.length) {
             throw new IllegalArgumentException("Index out of range: " + index);
         }
         mTimeFactor[index] = factor;
     }
 
     /**
      * Sets the amplitude for the specified index, this will affect the amplitude
      * for target index.
      * @param index Target index to change amplitude for.
      * @param amplitude Amplitude value
      * @throws IllegalArgumentException If index is outside amplitude array.
      * Size of amplitude array is decided when animation is created.
      */
     public void setAmplitude(int index, float amplitude) {
         if (index < 0 || index >= mAmplitude.length) {
             throw new IllegalArgumentException("Index out of range: " + index);
         }
         mAmplitude[index] = amplitude;
     }
 
 
 }
