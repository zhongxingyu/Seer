 //#condition api.mm
 /*
  * Copyright (C) 2010 France Telecom
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package memoplayer;
 
 //#ifdef mm.gzip
 import com.tinyline.util.GZIPInputStream;
 //#endif
 
 import java.io.*;
 import javax.microedition.lcdui.*;
 import javax.microedition.media.*;
 import javax.microedition.media.control.*;
 
 //#ifdef jsr.amms
 import javax.microedition.amms.control.camera.CameraControl;
 //#endif
 
 //#ifdef jsr.75
 import javax.microedition.io.file.*;
 import javax.microedition.io.*;
 //#endif
 
 public class MediaObject implements PlayerListener, Runnable, Loadable {
 
     final static int AUDIO = 0;
     final static int VIDEO = 1;
 
     final static int PLAYBACK = 0;
     final static int RECORDING = 1;
     final static int SNAPSHOT = 2;
 
     String m_name; // the url of the media to be played (file://, http://, rstp;::)
     int m_type; // audio or video
     int m_volume = 80;
     int m_mode; // PLAYBACK for simple playback, CAPTURE for audio/video recording , SNAPSHOP for photo
     long m_pauseTime;
     Player m_player;
     VideoControl m_videoControl;
     VolumeControl m_volumeControl;
     int m_state;
     int m_srcWidth=-1;
     int m_srcHeight=-1;
     
     // for rewind or forward
     boolean m_bPaused = false;
     boolean m_bSeekMode = false;
     
     Canvas m_canvas;
     Decoder m_decoder;
     Region m_region;
     int m_rotation = 0;
     boolean m_error = false;
     String m_errorMessage;
 //#ifdef jsr.amms
     private CameraControl m_cameraControl;
     private RecordControl m_recordControl;
 //#endif
 
     int m_w, m_h;
     static String [] s_types;
 
     // a thread will run continously to manage player states through messages as defined below
     final static int MSG_IDLE     = 0; 
     final static int MSG_CREATE   = 1;
     final static int MSG_REALIZE  = 2;
     final static int MSG_PREFETCH = 3;
     final static int MSG_START    = 4;
     final static int MSG_STOP     = 5;
     final static int MSG_CLOSE    = 6;
     final static String [] s_msgNames = {
         "Idle", "Create", "Realize", "Prefetch", "Start", "Stop", "Close", "*BAD*" 
     };
     boolean m_running; // kept as true as long as the thread below must stay alive
     Thread m_thread; // a thread for will be checking for messages like realize, start stop ... to keep things synchronous
     int m_message = 0;
 
 
     MediaObject (int type, int mode) {
         m_region = new Region(); //FTE (0, 0, 176, 144);
         m_type = type;
         m_mode = mode;
         //printCapabilities ();
     }
 
     public void playerUpdate (Player player, java.lang.String event, java.lang.Object eventData) {
         if (event.equals (PlayerListener.SIZE_CHANGED) == true) {
         	VideoControl vc = (VideoControl)eventData;
         	m_srcWidth = vc.getSourceWidth();
         	m_srcHeight= vc.getSourceHeight();
         } else {
         	if(event.equals (PlayerListener.ERROR)) {
         		Logger.println ("[DBG] playerUpdate: "+event+" "+eventData);
         	} else { 
         		Logger.println ("[DBG] playerUpdate: "+event);
         	}
         }
 
         if (event.equals (PlayerListener.STARTED)) {
             setState (Loadable.PLAYING);
         } else if (event.equals (PlayerListener.END_OF_MEDIA)) {
             setState (Loadable.EOM);
         } else if (event.equals (PlayerListener.BUFFERING_STARTED)) {
            setState (Loadable.BUFFERING);
         } else if (event.equals (PlayerListener.BUFFERING_STOPPED)) {
             setState (Loadable.PLAYING);
         } else if (event.equals (PlayerListener.STOPPED)) {
         	if(m_bPaused==true)
         		setState (Loadable.PAUSED);
         	else
         		setState (Loadable.STOPPED);
         } else if (event.equals (PlayerListener.ERROR)) {
             closePlayer ();
             setErrorMessage (eventData.toString());
             setState (Loadable.ERROR);
         } else if (event.equals (PlayerListener.VOLUME_CHANGED)) {
         	if(m_volumeControl!=null)
         		Logger.println ("[DBG] new volume : "+m_volumeControl.getLevel ());
         }
         MiniPlayer.wakeUpCanvas();
     }
 
     public synchronized void postMessage (int msg) { 
         m_message = msg;
     }
 
     public synchronized int pickMessage () { 
         int msg = m_message;
         m_message = MSG_IDLE;
         return msg; 
     }
 
     // open the stream and display it on the specified canvas
     public void open (String name, Canvas c, Decoder decoder) {
     	if(decoder!=null) {
     		m_decoder = decoder;
     	}
         if (m_name == null || m_name.compareTo (name) != 0){
             m_pauseTime = 0; //nouveau flux:pas de reprise
         }
         setState (Loadable.OPENING);
         m_name = name;
         m_canvas = c;
         if (m_running) {
             Logger.println ("WARNING: MediaObject was already running");
         }
         closePlayer (); // cleanup if needed
         m_running = true;
         postMessage (MSG_CREATE); // trigger player creation
         new Thread (this).start ();
     }
 
     // just post a close message
     public void close () {
         postMessage (MSG_CLOSE);
     }
 
     // close the J2ME player and free any associated resources
     private void closePlayer () {
         m_running = false;
         try { Thread.sleep (100); } catch (Exception e) { ; } // be sure the thread will die if running
         if (m_player != null) {
             try {
                 int state = m_player.getState ();
                 switch (state) {
                 case Player.CLOSED: // nothing 
                     break;
                 case Player.STARTED: // stop, close
                     stop (); 
                 case Player.PREFETCHED:  //close, deallocate, start
                 case Player.REALIZED: // close, prefetch
                 case Player.UNREALIZED: //close, realise
                     m_player.removePlayerListener(this);
                     m_player.close ();
                 }
                 m_player = null;//
                 System.gc (); // get back freed memory
                 setState (Loadable.STOPPED);
             } catch (Exception e) { 
                 setErrorMessage ("Error during close "+e);
                 setState (Loadable.ERROR);
                 Logger.println ("Exception in MediaObject.closed: "+e);
             }
             m_srcWidth=-1;
             m_srcHeight=-1;
         }
     }
 
     public synchronized int getState () { return m_state; }
 
     public String getErrorMessage () { return m_errorMessage; }
 
     public void setErrorMessage (String msg) { m_errorMessage = msg; }
 
     // retrieve the current volume level
     public int getVolume () {
         if (m_player == null || m_volumeControl == null) {
             return 0;
         }
         return (m_volume = m_volumeControl.getLevel());
     }
 
     // set the volume level to use now or when opening the next stream
     public void setVolume (int vol) {
         m_volume = vol;
         if (m_player == null || m_volumeControl==null) {
             return;
         }
         //Logger.println ("MediaObject: set volume at "+vol);
         m_volumeControl.setLevel (m_volume);
     }
 
     public void setPause (boolean pause) {
         if (m_player != null) {
             try {
                 if (pause) {
                     m_player.stop();
                     m_bPaused = true;
                 } else {
                     m_player.start();
                     m_bPaused = false;
                 }
             } catch (Exception e) {
                 Logger.println (e.getMessage());
             }
         }
     }
 
     // return the timestamp currently played
     public int getCurrent () {
         //Logger.println ("current "+m_player.getMediaTime ());
         return (m_player != null ? FixFloat.micro2sec(m_player.getMediaTime ()) : 0);
     }
 
     // set media time position
     public long setMediaTimePos (long mediaTime) {
         long ret = -1;
         //Logger.println ("setMediaTimePos "+m_player.getMediaTime ());
         try {
           if( m_player != null ) {
             // check if not already in seek mode
             if( m_bSeekMode == false ) {
               if( m_state == Loadable.PLAYING ) {
                 m_bSeekMode = true;
                 m_player.stop();
                 // wait player to be in stop state
                 while( m_state != Loadable.STOPPED ) {
                   try {
                     Thread.sleep(10);
                   }
                   catch ( InterruptedException ie ) {
                     break;
                   }  
                 }
                 ret = m_player.setMediaTime(mediaTime);
                 m_player.start();
                 m_bSeekMode = false;
               }
               else if(m_bPaused) {
                 m_bSeekMode = true;
                 ret = m_player.setMediaTime(mediaTime);
                 m_player.start();
                 // wait player to be in play state
                 while( m_state != Loadable.PLAYING ) {
                   try {
                     Thread.sleep(10);
                   }
                   catch ( InterruptedException ie ) {
                     break;
                   }  
                 }
                 m_player.stop();
                 m_bSeekMode = false;
               }
             }
           }
 
         } catch (MediaException e) {
             // setMediaTime not supported on some media types
             Logger.println ("setMediaTime not supported on some media types");
         }
         return ret;
     }
 
     // return the duration of the stream or -1 if the duration is not available or -2 if no stream is open
     public int getDuration () {
         if (m_player == null) {
             return -2;
         }
         long d = m_player.getDuration ();
         if (d == Player.TIME_UNKNOWN) {
             return -1;
         } else {
             return FixFloat.micro2sec(d);
         }
     }
 
     public String getName () { return m_name; }
 
     final static void printInfo () {
         Logger.println ("microedition.media.version :"+System.getProperty ("microedition.media.version "));
         Logger.println ("supports.mixing :"+System.getProperty ("supports.mixing "));
         Logger.println ("supports.audio.capture :"+System.getProperty ("supports.audio.capture "));
         Logger.println ("supports.video.capture :"+System.getProperty ("supports.video.capture "));
         Logger.println ("supports.recording :"+System.getProperty ("supports.recording "));
         Logger.println ("audio.encodings :"+System.getProperty ("audio.encodings "));
         Logger.println ("video.encodings :"+System.getProperty ("video.encodings "));
         Logger.println ("video.snapshot.encodings :"+System.getProperty ("video.snapshot.encodings "));
         Logger.println ("streamable.contents :"+System.getProperty ("streamable.contents "));
     }
 
     final static void printCapabilities () {
         if (s_types == null) {
             s_types = Manager.getSupportedContentTypes (null);
         }
         Logger.println ("Supported types: ");
         for (int i = 0; i < s_types.length; i++) {
             Logger.println ("type: "+s_types[i]);
         }
         String [] protocols = Manager.getSupportedProtocols (null);
         Logger.println ("Supported protocols: ");
         for (int i = 0; i < protocols.length; i++) {
             Logger.println ("protocol: "+protocols[i]);
         }
     }
 
     synchronized void setState (int state) { m_state = state; }
 
     synchronized int getInternalState () { return m_state; }
 
     // show or hide the video
     void setVisible (boolean yes) {
         if (m_videoControl != null) {
             try {
                 m_videoControl.setVisible (yes);
             } catch (Exception e) {
                 Logger.println ("Exception durind MediaObject.setVisible "+yes);
             }
         }
     }
 
     // try to set the display mode using overlay and specified rotation when possible
     // rotation is an angle in degrees
     void setDisplayMode (int rotation) {
         if (m_type == VIDEO && m_videoControl != null) {
             int OVERLAY = 1<<8;
             int mode = 0;
             switch (rotation){
                 case 90:
                     mode=javax.microedition.lcdui.game.Sprite.TRANS_ROT90<<4;
                     break;
                 case 270:
                     mode=javax.microedition.lcdui.game.Sprite.TRANS_ROT270<<4;
                     break;
                 case 180:
                     mode=javax.microedition.lcdui.game.Sprite.TRANS_ROT180<<4;
                     break;
                 default:
                     mode=0;
             }
             int flag = OVERLAY | mode; //RC 13/10/07 uncomment rotation
 
             try {
                 m_videoControl.initDisplayMode (VideoControl.USE_DIRECT_VIDEO|flag, m_canvas);
                 MyCanvas.s_OverlaySupported = true;
             } catch (Exception e) {
                 Logger.println ("setDisplayMode: video overlay or rotation not supported, falling back to normal mode: "+e);
                 try {
                     m_videoControl.initDisplayMode (VideoControl.USE_DIRECT_VIDEO, m_canvas);
                 } catch (Exception ee) {
                     Logger.println ("setDisplayMode: USE_DIRECT_VIDEO mode not supported: "+ee);
                 }
             }
             setDisplayArea (m_region.x0, m_region.y0, m_region.x1, m_region.y1);
         }
     }
 
     void setDisplayArea (int x, int y, int w, int h) {
         //Logger.println ("MediaObject.setDisplayArea: "+x+", "+y+", "+w+", "+h);
         if (m_type == VIDEO && m_videoControl != null) {
             try {
                 m_videoControl.setDisplayLocation (x, y);
                 m_videoControl.setDisplaySize (w, h);
             } catch (Exception e){
                 Logger.println ("Exception in setDisplayArea ("+x+","+y+","+w+","+h+"): "+e);
                 e.printStackTrace ();
             }
         }
     }
 
     public void setRegion (int x, int y, int w, int h, int rotation) { // RC 13/10/07 fix the method behavior
         //Logger.println ("MediaObject.setRegion: "+x+", "+y+", "+w+", "+h);
         if (m_type == VIDEO && m_videoControl != null && (!m_region.equals2 (x, y, w, h) || rotation != m_rotation)){  
             if (getInternalState () == Loadable.PLAYING) {
                 if (rotation != m_rotation) {
                     m_rotation = rotation;
                     if (m_rotation == 90) { w++; h++; } // apparently a small bug when video is rotated
                     if (MovieRotator.setOrientation (m_player, rotation) == false) { // != 0 ? MovieRotator.LANDCSAPE : MovieRotator.PORTRAIT)) {
                         open (m_name, m_canvas, null);
                     } else {
                         setVisible (false);
                         setDisplayArea (x, y, w, h);
                         setVisible(true);
                     }
                 } else {
                     setDisplayArea (x, y, w, h);
                 }
             }
         }
         m_region.set (x, y, w, h); 
         m_rotation = rotation;
     }
 
     public void start () {
         if (m_player != null) {
             try {
                 m_player.start ();
                 setVisible (true);
                 setState (Loadable.PLAYING);
             } catch (Exception e) {
                 closePlayer ();
                 setErrorMessage (e.toString());
                 setState (Loadable.ERROR);
                 Logger.println ("Exception in MediaObject.start: "+e.getMessage());
             }
         }
     }
 
     
     public void stop () {
         if (m_player != null) {
             try {
                 if (m_type == VIDEO && m_videoControl != null) {
                     m_videoControl.setVisible (false);
                 }
 //#ifdef jsr.amms
                 if (m_mode == RECORDING && m_recordControl != null) {
                     m_recordControl.stopRecord();
                     m_recordControl.commit();
                     m_recordControl=null;
                 }
 //#endif
                 if (m_videoControl != null) { m_videoControl = null; }
                 if (m_volumeControl != null) { m_volumeControl = null; }
                 
                 if (m_player != null) {
                     if (m_player.getState () == Player.STARTED) {
                         m_player.stop (); 
                         m_player.deallocate ();
                     }
                 }
                 setState (Loadable.STOPPED);
             } catch (Exception e) {
                 setErrorMessage ("Error during stop "+e);
                 setState (Loadable.ERROR);
                 Logger.println ("Exception in MediaObject.stop: "+e);
             }
         }
     }
 
 
     boolean hasCapability (String s) {
         if (s_types == null) {
             s_types = Manager.getSupportedContentTypes (null);
         }
         for (int i = 0; i < s_types.length; i++) {
             if (s_types[i].equals (s)) {
                 return (true);
             }
         }
         return (false);
     }
 
     String getMimeType (String name, boolean isAudio) {
         if (isAudio) {
             if (name.endsWith ("3gp") || name.endsWith ("3GP")) {
                 if (hasCapability ("audio/3gpp")) {
                     return "audio/3gpp";
                 }
                 if (hasCapability ("audio/3gp")) {
                     return "audio/3gp";
                 }
             } else if (name.endsWith ("mp3") || name.endsWith ("MP3")) {
                 if (hasCapability ("audio/mp3")) {
                     return "audio/mp3";
                 }
                 if (hasCapability ("audio/mpeg3")) {
                     return "audio/mpeg3";
                 }
             } else if (name.endsWith ("m4a") || name.endsWith ("M4A")) {
                 if (hasCapability ("audio/m4a")) {
                     return "audio/m4a";
                 }
             } else if (name.endsWith ("mid") || name.endsWith ("MID")) {
                 if (hasCapability ("audio/midi")) {
                     return "audio/midi";
                 }
             } else if (name.endsWith ("amr") || name.endsWith ("AMR")) {
                 if (hasCapability ("audio/amr")) {
                     return "audio/amr";
                 }
             } else if (name.endsWith ("au") || name.endsWith ("AU")) {
                 if (hasCapability ("audio/basic")) {
                     return "audio/basic";
                 }
             } else if (name.endsWith ("wav") || name.endsWith ("WAV")) {
                 if (hasCapability ("audio/wav")) {
                     return "audio/wav";
                 } else if (hasCapability ("audio/x-wav")) {
                     return "audio/x-wav";
                 }
             }
             return "audio/mpeg";
         } else { //video
             if (name.endsWith ("3gp") || name.endsWith ("3GP")) {
                 if (hasCapability ("video/3gpp")) {
                     return "video/3gpp";
                 }
             } else if (name.endsWith ("mp4") || name.endsWith ("MP4")) {
                 if (hasCapability ("video/mpeg4")) {
                     return "video/mpeg4";
                 }
             }
             else if (name.endsWith ("mpg") || name.endsWith ("MPG")) {
                 if (hasCapability ("video/mpeg")) {
                     return "video/mpeg";
                 }
             }
             return "video/mpeg4";
         }
     }
 
     public byte [] getSnapshot (String format) {
         try {
 //#ifdef jsr.amms
             if (m_cameraControl != null) {
                 return (m_videoControl.getSnapshot (format));
             }
 //#else
             return (m_videoControl.getSnapshot (format));
 //#endif
         } catch (Exception e) {
             Logger.println ("Exception during getSnapshot: "+e);
         }
         return null;
     }
 
     // create a player according to the filename
     Player createPlayer () {
         try {
             if (m_name.startsWith ("file:///")) {
 //#ifdef jsr.75
                 FileConnection fc = (FileConnection) Connector.open(m_name);
                 if (!fc.exists()) {
                     m_errorMessage = "File does not exist";
                     throw new IOException ("File does not exist");
                 }
                 InputStream is = fc.openInputStream();
                 try { 
                     String mimeType = getMimeType (m_name, m_type == AUDIO); 
                     return Manager.createPlayer (is, mimeType); 
                 } catch (Exception e) { 
                     Logger.println ("warning MediaObject: previous open failed, trying 3gp format - " + m_name); 
                     return Manager.createPlayer (is, "video/3gpp"); 
                 }
 //#endif
             } else if (m_name.startsWith ("rtsp://") || m_name.startsWith ("http://")){
 
                 Player p = null;
                 try {
                   p = Manager.createPlayer (m_name);
                 } catch ( Exception me ) {
                   String str = me.toString();
                   Logger.println( str );
                   if(   str.startsWith("javax.microedition.media.MediaException: Not supported locator: rtsp://")
                       || str.startsWith("javax.microedition.media.MediaException: Cannot create a Player for:") ) {
                     try {
                       // try to launch external program
                       // Logger.println("Try to launch external program");
                       
                       MiniPlayer.openUrl(m_name);
                       
                     } catch ( Exception e ) {
                       Logger.println( e.toString() );
                       // external program not found
                       // Logger.println("External program not found");
                     }
                   }
                 }
                 return p;
                 
             } else if (m_name.startsWith ("capture://")){
                 try {
                     return Manager.createPlayer (m_name);
                 } catch (Exception e) {
                     if (m_name.equals ("capture://audio_video")) {
                         return Manager.createPlayer ("capture://video");
                     } else {
                         return Manager.createPlayer ("capture://audio");
                     }
                 }
             } else if (m_name.startsWith ("cache://")) {
                 String rmsRecord = m_name.substring(8);
                 Logger.println ("MediaObject.opening from cache: '"+rmsRecord+"'");
                 byte[] data = CacheManager.getManager().getByteRecord(rmsRecord);
                 if(data==null) {
                     setErrorMessage ("cannot open from cache, not found "+rmsRecord);
                     setState (Loadable.ERROR);
                     return null;
                 }
                 InputStream bais = new ByteArrayInputStream(data);
                 InputStream is = bais;
 //#ifdef mm.gzip
                 if (m_name.endsWith(".gz")) {
                     is = new GZIPInputStream(bais);
                 }
 //#endif
                 try {
                     String tempName = m_name;
 //#ifdef mm.gzip
                     if (m_name.endsWith(".gz")) {
                         tempName = m_name.substring(0, m_name.length()-3);
                     }
 //#endif
                     String mimeType = getMimeType (tempName, m_type == AUDIO);
                     return Manager.createPlayer (is, mimeType);
                 } catch (Exception e) {
                     return Manager.createPlayer (is, "video/3gpp");
                 }
             } else { // probably in m4m or jar
             	// try to look in m4m 
                 try {
 	            	if( m_decoder != null ) {
 	            		byte[] mmediaData = m_decoder.getMMedia(m_name);
 	            		if(mmediaData!=null) {
 	        	    		ByteArrayInputStream is = new ByteArrayInputStream(mmediaData);
 	            			String mimeType = getMimeType (m_name, m_type == AUDIO);
 	            			return Manager.createPlayer (is, mimeType);
 	            		}
 	            	}
                 } catch (Exception e) {
                     Logger.println ("Exception during player creation from m4m for "+m_name+" : "+e);
                 }
             	
             	// now look in jar
                 Logger.println ("MediaObject.opening from jar: '"+m_name+"'");
                 InputStream is = getClass ().getResourceAsStream ("/"+m_name);
                 
                 if(is==null) {
                     setErrorMessage ("not found in m4m or jar: "+m_name);
                     setState (Loadable.ERROR);
                     return null;
                 }
 
                 try {
                     String mimeType = getMimeType (m_name, m_type == AUDIO);
                     return Manager.createPlayer (is, mimeType);
                 } catch (Exception e) {
                     return Manager.createPlayer (is, "video/3gpp");
                 }
             }
         } catch (Exception e) {
             Logger.println ("Exception during player creation for "+m_name+" : "+e);
         }
     	Logger.println("Cannot create player for url: "+m_name);
         setErrorMessage ("Cannot create player for url: "+m_name);
         setState (Loadable.ERROR);
         return null;
     }
 
     void openControls () {
 //#ifdef jsr.amms
         m_recordControl = null;
 //#        m_cameraControl = null;
 //#endif
         m_videoControl = null;
         m_volumeControl = null;
         try {
             m_volumeControl = (VolumeControl)m_player.getControl("VolumeControl");
             setVolume (m_volume);
             
             if (m_type == VIDEO) {
                 m_videoControl = (VideoControl)m_player.getControl("VideoControl");                
 //#ifdef jsr.amms
                 if (m_mode == SNAPSHOT) {
                     m_cameraControl = (CameraControl)m_player.getControl("CameraControl");
                     if (m_cameraControl != null) {
                         Logger.println ("Video resolutions");
                         int [] res = m_cameraControl.getSupportedVideoResolutions();
                         for (int i = 0; i < res.length; i += 2) {
                             Logger.println ("    "+(i/2)+": ["+res[i]+" "+res[i+1]+"]");
                         }
                         Logger.println ("Image resolutions");
                         res = m_cameraControl.getSupportedStillResolutions();
                         for (int i = 0; i < res.length; i += 2) {
                             Logger.println ("    "+(i/2)+": ["+res[i]+" "+res[i+1]+"]");
                         }
                         Logger.println ("Encodings: "+System.getProperty ("video.snapshot.encodings"));
                     }
                 } if (m_mode == RECORDING) {
                     m_recordControl = (RecordControl)m_player.getControl("RecordControl");
                     if (m_recordControl != null) {
                         m_recordControl.setRecordLocation (System.getProperty("fileconn.dir.videos")+"video.3gp");
                         m_recordControl.startRecord();
                     }
                 }
 //#endif
             }
         } catch (Exception e) {
             Logger.println ("Exception during player controls creation for "+m_name+" : "+e);
         }
     }
 
     private void checkState (int state, String msg) {
         if (m_player.getState () != state) {
             closePlayer ();
             setErrorMessage ("Cannot "+msg+" player");
             setState (Loadable.ERROR);
         }
         Logger.println ("MediaObject: player "+msg+"ed");
     }
 
     public void run (){
         try {
             while (m_running) {
                 int msg = pickMessage ();
                 if (msg != MSG_IDLE) { Logger.println ("MediaObject.run: applying "+s_msgNames[msg]); }
                 switch (msg) {
                 case MSG_CREATE:
                     postMessage (MSG_REALIZE);
                     m_player = createPlayer ();
                     if (m_player == null) {
                         m_running = false;
                         return;
                     }
                     m_player.addPlayerListener (this);
                     Logger.println ("MediaObject: player created");
                     break;
                 case MSG_REALIZE:
                     postMessage (MSG_PREFETCH);
                     m_player.realize ();
                     checkState (Player.REALIZED, "realize");
                     break;
                 case MSG_PREFETCH:
                     postMessage (MSG_START);
                     m_player.prefetch ();
                     checkState (Player.PREFETCHED, "prefetch");
                     openControls ();
                     if (m_videoControl != null) {
                         m_w = m_videoControl.getSourceWidth ();
                         m_h = m_videoControl.getSourceHeight ();
                         setDisplayMode (m_rotation); 
                         setVisible (true);
                     }
                     break;
                 case MSG_START:
                     postMessage (MSG_IDLE);
                     m_player.start ();
                     setVisible (true);
                     checkState (Player.STARTED, "start");
                     break;
                 case MSG_STOP:
                     postMessage (MSG_IDLE);
                     setVisible (false);
                     m_player.stop ();
                     checkState (Player.PREFETCHED, "stop");
                     break;
                 case MSG_CLOSE:
                     postMessage (MSG_IDLE);
                     closePlayer ();
                     m_running = false;;
                 }
                 try { Thread.sleep (100); } catch (Exception e) { ; }
             }
 
         } catch (Exception e) {
         	String errorMsg = e.toString();
             Logger.println ("Exception in MediaObject.Run with "+m_name+" : "+errorMsg);
             closePlayer ();
             setErrorMessage (errorMsg);
             setState (Loadable.ERROR);
         }
     }
 }
