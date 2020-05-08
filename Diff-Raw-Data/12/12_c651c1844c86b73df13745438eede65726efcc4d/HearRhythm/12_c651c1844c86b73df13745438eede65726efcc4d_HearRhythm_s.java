 /*
  * Copyright 2010 Andrew Prunicki
  * 
  * This file is part of Twinkle.
  * 
  * Twinkle is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Twinkle is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Twinkle.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.prunicki.suzuki.twinkle.game;
 
 import android.app.Activity;
 import android.view.View;
 
 import com.prunicki.suzuki.twinkle.GameButtonListener;
 import com.prunicki.suzuki.twinkle.GameRoundCallback;
 import com.prunicki.suzuki.twinkle.R;
 import com.prunicki.suzuki.twinkle.SoundPlayer;
 
 public class HearRhythm extends GameRound {
     int mRhythm;
     
     public HearRhythm(GameRoundCallback callback) {
         super(R.layout.hearrhythm, true, 4, callback);
         nextRhythm();
     }
 
     @Override
     public void onCreate(Activity activity) {
         super.onCreate(activity);
         
        mRhythm = -1;
         View[] buttons = mButtons;
         GameButtonListener[] listeners = mButtonListeners;
         GameRoundCallback callback = mCallback;
         
         int x = 0;
         buttons[x] = activity.findViewById(R.id.HearMissStopStop);
         listeners[x++] = new RhythmListener(SoundPlayer.MISSISSIPPI_STOP_STOP_RHYTHM, callback);
         buttons[x] = activity.findViewById(R.id.HearMissAlligator);
         listeners[x++] = new RhythmListener(SoundPlayer.MISSISSIPPI_ALLIGATOR_RHYTHM, callback);
         buttons[x] = activity.findViewById(R.id.HearDownPony);
         listeners[x++] = new RhythmListener(SoundPlayer.DOWN_PONY_UP_PONY_RHYTHM, callback);
         buttons[x] = activity.findViewById(R.id.HearIceCream);
         listeners[x++] = new RhythmListener(SoundPlayer.ICE_CREAM_SH_CONE_RHYTHM, callback);
         
         setListenersIntoButtons(buttons, listeners);
     }
 
     @Override
     protected void playNotes(SoundPlayer.PlayerCallback soundPlayerCallback) {
         mSoundPlayer.playRhythm(mRhythm, soundPlayerCallback);
     }
 
     void nextRhythm() {
         mRhythm = nextRandom(mRhythm);
     }
     
     private class RhythmListener extends GameButtonListener {
         private int mRhythm;
         
         private RhythmListener (int rhythm, GameRoundCallback callback) {
             super(callback);
             mRhythm = rhythm;
         }
 
         @Override
         protected boolean checkSuccess() {
             return mRhythm == HearRhythm.this.mRhythm;
         }
     }
 }
