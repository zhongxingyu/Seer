 /*
 * Copyright 2004-2011 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions an
 * limitations under the License.
 */ 
 
 package org.icemobile.client.android;
 
 import android.content.Context;
 import android.util.Log;
 import android.media.MediaRecorder;
 import android.media.MediaPlayer;
 import android.os.Environment;
 import java.io.IOException;
 import java.io.File;
 import java.lang.reflect.Field;
import android.os.Build;
 
 public class AudioRecorder {
 
     private Context context;
     private boolean recording;
     private MediaRecorder mRecorder;
     private File audioFile;
     private UtilInterface utilInterface;
     private boolean cantStop;
     private boolean stopPending;
 
     public AudioRecorder (Context context, UtilInterface util) {
 	this.context = context;
 	utilInterface = util;
 	recording = false;
 	audioFile = new File(utilInterface.getTempPath(), "micCapture.mpga");
     }
 
     public String toggleMic(int maxDuration) {
 	if (recording) {
 	    stopRecording();
 	} else {
 	    startRecording(maxDuration);
 	}
         return audioFile.getAbsolutePath();
     }
 
     private void startRecording(int maxDuration) {
         try {
             mRecorder = null;
             try {
		// Can't use AAC on Samsung Tablets 3.1;
		String model = Build.MODEL;
		String version = Build.VERSION.RELEASE;
		if (model.contains("GT-P7")  && 
		    version.equals("3.1")) {
                    mRecorder = getMediaRecorder(MediaRecorder.AudioEncoder.DEFAULT);
		} else {
		    Field aacField = MediaRecorder.AudioEncoder.class
                         .getField("AAC");
		    if (null != aacField)  {
			//set to AAC from android-10 for better iPhone compatibility
			mRecorder = getMediaRecorder(aacField.getInt(null));
			if (maxDuration > 0) {
			    mRecorder.setMaxDuration(maxDuration*1000);
			}
		    }
		}
             } catch (Throwable t)  {
                 Log.d("ICEaudio", "AAC setup failed");
             }
             mRecorder.prepare();
             Log.d("ICEaudio", "AAC audio encoding ");
         } catch (Exception e) {
             Log.e("ICEaudio", "prepare() failed for AAC");
             try {
                 mRecorder = getMediaRecorder(
                         MediaRecorder.AudioEncoder.DEFAULT );
                 if (maxDuration > 0) {
                     mRecorder.setMaxDuration(maxDuration*1000);
                 }
                 mRecorder.prepare();
                 Log.d("ICEaudio", "DEFAULT audio encoding ");
             } catch (IOException x) {
                 Log.e("ICEaudio", "prepare() failed for DEFAULT");
             }
         }
 
         //record in a separate thread so that the UI is not blocked
 	Thread t = new Thread(new Runnable()  {
             public void run()  {
 		stopPending = false;
 		cantStop = true;
                 mRecorder.start();
 		try {
 		    Thread.currentThread().sleep(10);
 		} catch (Exception e) {
 		    Log.e("ICEaudio", e.toString());
 		}
 		cantStop = false;
 		if (stopPending) {
 		    stopRecording();
 		}
             }
         });
         t.start();
 	recording = true;
     }
 
     MediaRecorder getMediaRecorder(int encoder)  {
         MediaRecorder recorder = new MediaRecorder();
         recorder.reset();
         recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
         recorder.setAudioEncoder(encoder);
         recorder.setOutputFile(audioFile.getAbsolutePath());
         return recorder;
     }
 
     private void stopRecording() {
 	if (cantStop) {
 	    stopPending = true;
 	} else {
 	    mRecorder.stop();
 	    mRecorder.release();
 	    mRecorder = null;
 	    recording = false;
 	    stopPending = false;
 	}
     }
 }
