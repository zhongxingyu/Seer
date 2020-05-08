 /**
  * Java abstraction layer for Nao humanoid robot.
  * This is automatically generated code. Do not modify it!
  *
  * Copyright (C) 2011 Max Leuthäuser
  * Contact: s7060241@mail.zih.tu-dresden.de
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.qualitune;
 
 import com.aldebaran.proxy.*;
 import java.util.ArrayList;
 import java.io.*;
 

 public class Nao {
     static {
         try {
             // load library
             System.loadLibrary("JNaoQi");
         } catch (UnsatisfiedLinkError e) {
             // if we use .jar we first copy the library from package before load it
             loadBinaries();
         }
     }
 
     private static void loadBinaries() {
         ArrayList<String> bins = new ArrayList<String>() {{
             add("libJNaoQi.so");
         }};
 
         File f = null;
         for (String bin : bins) {
             InputStream in = ALMemoryProxy.class.getResourceAsStream(bin);
             byte[] buffer = new byte[1024];
             int read = -1;
             try {
                 String[] temp = bin.split("/");
                 f = new File("./", temp[temp.length - 1]);
                 FileOutputStream fos = new FileOutputStream(f);
 
                 while ((read = in.read(buffer)) != -1) {
                     fos.write(buffer, 0, read);
                 }
                 fos.close();
                 in.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         // now load the file
         System.load(f.getAbsolutePath());
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALAudioDeviceProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALAudioDeviceProxy}
      */
     public static ALAudioDeviceProxy createALAudioDevice(final String ip, final int port) {
         return new ALAudioDeviceProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALAudioPlayerProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALAudioPlayerProxy}
      */
     public static ALAudioPlayerProxy createALAudioPlayer(final String ip, final int port) {
         return new ALAudioPlayerProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALAudioSourceLocalizationProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALAudioSourceLocalizationProxy}
      */
     public static ALAudioSourceLocalizationProxy createALAudioSourceLocalization(final String ip, final int port) {
         return new ALAudioSourceLocalizationProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALBehaviorManagerProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALBehaviorManagerProxy}
      */
     public static ALBehaviorManagerProxy createALBehaviorManager(final String ip, final int port) {
         return new ALBehaviorManagerProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALBluetoothProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALBluetoothProxy}
      */
     public static ALBluetoothProxy createALBluetooth(final String ip, final int port) {
         return new ALBluetoothProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALBonjourProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALBonjourProxy}
      */
     public static ALBonjourProxy createALBonjour(final String ip, final int port) {
         return new ALBonjourProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALFaceDetectionProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALFaceDetectionProxy}
      */
     public static ALFaceDetectionProxy createALFaceDetection(final String ip, final int port) {
         return new ALFaceDetectionProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALFaceTrackerProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALFaceTrackerProxy}
      */
     public static ALFaceTrackerProxy createALFaceTracker(final String ip, final int port) {
         return new ALFaceTrackerProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALFileManagerProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALFileManagerProxy}
      */
     public static ALFileManagerProxy createALFileManager(final String ip, final int port) {
         return new ALFileManagerProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALFrameManagerProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALFrameManagerProxy}
      */
     public static ALFrameManagerProxy createALFrameManager(final String ip, final int port) {
         return new ALFrameManagerProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALFsrProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALFsrProxy}
      */
     public static ALFsrProxy createALFsr(final String ip, final int port) {
         return new ALFsrProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALInfraredProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALInfraredProxy}
      */
     public static ALInfraredProxy createALInfrared(final String ip, final int port) {
         return new ALInfraredProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALLandMarkDetectionProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALLandMarkDetectionProxy}
      */
     public static ALLandMarkDetectionProxy createALLandMarkDetection(final String ip, final int port) {
         return new ALLandMarkDetectionProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALLaserProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALLaserProxy}
      */
     public static ALLaserProxy createALLaser(final String ip, final int port) {
         return new ALLaserProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALLauncherProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALLauncherProxy}
      */
     public static ALLauncherProxy createALLauncher(final String ip, final int port) {
         return new ALLauncherProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALLedsProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALLedsProxy}
      */
     public static ALLedsProxy createALLeds(final String ip, final int port) {
         return new ALLedsProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALLoggerProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALLoggerProxy}
      */
     public static ALLoggerProxy createALLogger(final String ip, final int port) {
         return new ALLoggerProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALMemoryProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALMemoryProxy}
      */
     public static ALMemoryProxy createALMemory(final String ip, final int port) {
         return new ALMemoryProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALMotionProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALMotionProxy}
      */
     public static ALMotionProxy createALMotion(final String ip, final int port) {
         return new ALMotionProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALMotionRecorderProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALMotionRecorderProxy}
      */
     public static ALMotionRecorderProxy createALMotionRecorder(final String ip, final int port) {
         return new ALMotionRecorderProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALPreferencesProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALPreferencesProxy}
      */
     public static ALPreferencesProxy createALPreferences(final String ip, final int port) {
         return new ALPreferencesProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALPythonBridgeProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALPythonBridgeProxy}
      */
     public static ALPythonBridgeProxy createALPythonBridge(final String ip, final int port) {
         return new ALPythonBridgeProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALRedBallDetectionProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALRedBallDetectionProxy}
      */
     public static ALRedBallDetectionProxy createALRedBallDetection(final String ip, final int port) {
         return new ALRedBallDetectionProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALRedBallTrackerProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALRedBallTrackerProxy}
      */
     public static ALRedBallTrackerProxy createALRedBallTracker(final String ip, final int port) {
         return new ALRedBallTrackerProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALResourceManagerProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALResourceManagerProxy}
      */
     public static ALResourceManagerProxy createALResourceManager(final String ip, final int port) {
         return new ALResourceManagerProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALRobotPoseProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALRobotPoseProxy}
      */
     public static ALRobotPoseProxy createALRobotPose(final String ip, final int port) {
         return new ALRobotPoseProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALSensorsProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALSensorsProxy}
      */
     public static ALSensorsProxy createALSensors(final String ip, final int port) {
         return new ALSensorsProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALSentinelProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALSentinelProxy}
      */
     public static ALSentinelProxy createALSentinel(final String ip, final int port) {
         return new ALSentinelProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALSonarProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALSonarProxy}
      */
     public static ALSonarProxy createALSonar(final String ip, final int port) {
         return new ALSonarProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALSoundDetectionProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALSoundDetectionProxy}
      */
     public static ALSoundDetectionProxy createALSoundDetection(final String ip, final int port) {
         return new ALSoundDetectionProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALSpeechRecognitionProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALSpeechRecognitionProxy}
      */
     public static ALSpeechRecognitionProxy createALSpeechRecognition(final String ip, final int port) {
         return new ALSpeechRecognitionProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALTextToSpeechProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALTextToSpeechProxy}
      */
     public static ALTextToSpeechProxy createALTextToSpeech(final String ip, final int port) {
         return new ALTextToSpeechProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALVideoDeviceProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALVideoDeviceProxy}
      */
     public static ALVideoDeviceProxy createALVideoDevice(final String ip, final int port) {
         return new ALVideoDeviceProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALVisionRecognitionProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALVisionRecognitionProxy}
      */
     public static ALVisionRecognitionProxy createALVisionRecognition(final String ip, final int port) {
         return new ALVisionRecognitionProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link ALVisionToolboxProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link ALVisionToolboxProxy}
      */
     public static ALVisionToolboxProxy createALVisionToolbox(final String ip, final int port) {
         return new ALVisionToolboxProxy(ip, port);
     }
 
     /**
      * Factory method for creating a new instance of the
      * Nao module {@link DCMProxy}.
      *
      * @param ip   The IP address of the 
      * @param port The port where the Nao service is running.
      * @return a new instance of {@link DCMProxy}
      */
     public static DCMProxy createDCM(final String ip, final int port) {
         return new DCMProxy(ip, port);
     }
 }
