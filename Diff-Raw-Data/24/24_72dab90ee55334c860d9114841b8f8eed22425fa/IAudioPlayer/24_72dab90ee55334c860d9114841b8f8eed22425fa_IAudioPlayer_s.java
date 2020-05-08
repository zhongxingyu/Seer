 /*
    Copyright 2012 Mikhail Chabanov
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package mc.lib.audio;
 
 import java.io.File;
 import java.io.InputStream;
 
 import mc.lib.interfaces.OnCompleteListener;
 import mc.lib.interfaces.OnProgressListener;
 import android.content.res.AssetFileDescriptor;
 import android.net.Uri;
 
 public interface IAudioPlayer
 {
     /**
      * Play audio from resource.
      * 
      * @param resId
      */
     void play(int resId);
 
     /**
      * Play audio from file.
      * 
      * @param filePath
      */
     void play(String filePath);
 
     /**
      * Play audio from file.
      * 
      * @param path
      */
     void play(File path);
 
     /**
      * Play audio from URI.
      * 
      * @param uri
      */
     void play(Uri uri);
 
     /**
      * Play audio from stream.
      * 
      * @param is
      */
     void play(InputStream is);
 
     /**
      * Play audio from file.
      * 
      * @param afd
      */
     void play(AssetFileDescriptor afd);
 
     /**
      * Stop playing audio.
      */
     void stop();
 
     /**
      * Pause playing audio. Afterwards you can continue playing from the same position by calling resume method.
      */
     void pause();
 
     /**
      * If method pause was called previously then resume start playing from last playing position.
      */
     void resume();
 
     /**
      * Check if audio playing now.
      * 
      * @return true if audio playing now
      */
     boolean isPlaying();
 
     /**
      * Set playing volume.
      * 
      * @param volume
      */
     void setVolume(float volume);
 
     /**
      * 
      * @return true if playing repeatedly
      */
     boolean getRepeat();
 
     /**
      * If you set true then the same audio playing repeatedly until you stop playing or call other play method.
      * 
      * @param repeat
      */
     void setRepeat(boolean repeat);
 
     /**
      * 
      * @return total duration of audio playing now
      */
     int getDuration();
 
     /**
      * 
      * @return OnCompleteListener or null if listener not set
      */
    OnCompleteListener getOnCompleteListener();
 
     /**
      * Allow to set OnCompleteListener to notify app when playing complete.
      * 
      * @param listener
      */
    void setOnCompleteListener(OnCompleteListener listener);
 
     /**
      * 
      * @return OnProgressListener or null if listener not set
      */
     OnProgressListener getOnProgressListener();
 
     /**
      * Allow to set OnProgressListener to notify app when playing progress is made.
      * 
      * @param listener
      */
     void setOnProgressListener(OnProgressListener listener);
 }
