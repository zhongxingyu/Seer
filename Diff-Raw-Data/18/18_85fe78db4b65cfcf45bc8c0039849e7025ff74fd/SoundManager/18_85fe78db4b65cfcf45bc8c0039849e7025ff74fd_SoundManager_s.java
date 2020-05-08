 /*==============================================================================
             Copyright (c) 2010-2011 QUALCOMM Incorporated.
             All Rights Reserved.
             Qualcomm Confidential and Proprietary
 ==============================================================================*/
 
 package com.qualcomm.QCARSamples.ImageTargets;
 
 import java.util.ArrayList;
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.util.Log;
 
 public class SoundManager{
 
     // SoundPool is used for short sound effects
     private SoundPool mSoundPool;
     
     
     // An array of sound clip ids
     //public ArrayList<Integer> mSoundIds;
     public ArrayList<Integer> mSoundIds;
     
     
     /** Initialize the sound pool and load our sound clips. */
     public SoundManager(Context context)
     {
     	mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
     	mSoundIds = new ArrayList<Integer>();
     	mSoundIds.add(mSoundPool.load(context, R.raw.arrow, 1));
     	mSoundIds.add(mSoundPool.load(context, R.raw.snowball, 1));
     	mSoundIds.add(mSoundPool.load(context, R.raw.cannon, 1));
     	mSoundIds.add(mSoundPool.load(context, R.raw.harp, 1));
     }
     
     
     /** Play a sound with the given index and volume. */
     public void playSound(int soundIndex, float volume)
     {
     	//Log.v("TEST","PLAYING SOUND");
 		
         if (soundIndex < 0 || soundIndex >= mSoundIds.size()) {
             DebugLog.LOGE("Sound id " + soundIndex + " out of range");
             return;
         }
         
         mSoundPool.play(mSoundIds.get(soundIndex), volume, volume, 1, 0, 1.0f);
     }
     /** Play a sound with the given index and volume. */
 }
