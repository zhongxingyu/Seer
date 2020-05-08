 //A simpler implementation of AndLess custom media interface
 //This uses the compiled lossless library from http://code.google.com/p/andless/
 //Allows playback of FLAC and other codecs on devices that do not support these natively
 package com.calcprogrammer1.calctunes;
 
 import java.io.DataOutputStream;
 import java.io.File;
 
 import android.os.Build;
 import android.os.Process;
 
 interface LosslessMediaCodecHandlerCallback
 {
     public void onCompletion();
 }
 
 public class LosslessMediaCodecHandler
 {
     static
     {
         System.loadLibrary("lossless");
         libInit(Integer.parseInt(Build.VERSION.SDK));
     }
     
     public static native int        audioInit(int ctx, int mode);   
     public static native boolean    audioExit(int ctx);
     public static native boolean    audioStop(int ctx);
     public static native boolean    audioPause(int ctx);
     public static native boolean    audioResume(int ctx);
     
     public static native int        audioGetDuration(int ctx);
     public static native int        audioGetCurPosition(int ctx);
     public static native boolean    audioSetVolume(int ctx, int vol);
     
     public static native int        alacPlay(int ctx,String file, int start);
     public static native int        flacPlay(int ctx,String file, int start);
     public static native int        apePlay(int ctx,String file, int start);
     public static native int        wavPlay(int ctx,String file, int start);
     public static native int        wvPlay(int ctx,String file, int start);
     public static native int        mpcPlay(int ctx,String file, int start);
     public static native int []     extractFlacCUE(String file);
     
     public static native int        wvDuration(int ctx,String file);
     public static native int        apeDuration(int ctx,String file);
     
     public static native boolean    libInit(int sdk);
     public static native boolean    libExit();
 
     public static final int LIBLOSSLESS_ERR_NOCTX = 1;
     public static final int LIBLOSSLESS_ERR_INV_PARM  =  2;
     public static final int LIBLOSSLESS_ERR_NOFILE = 3;
     public static final int LIBLOSSLESS_ERR_FORMAT = 4;
     public static final int LIBLOSSLESS_ERR_AU_GETCONF = 6;
     public static final int LIBLOSSLESS_ERR_AU_SETCONF = 7;
     public static final int LIBLOSSLESS_ERR_AU_BUFF = 8;
     public static final int LIBLOSSLESS_ERR_AU_SETUP = 9;
     public static final int LIBLOSSLESS_ERR_AU_START = 10;
     public static final int LIBLOSSLESS_ERR_IO_WRITE = 11;
     public static final int LIBLOSSLESS_ERR_IO_READ = 12;
     public static final int LIBLOSSLESS_ERR_DECODE = 13;
     public static final int LIBLOSSLESS_ERR_OFFSET = 14;
     public static final int LIBLOSSLESS_ERR_NOMEM = 15;
     
     public static final int MODE_NONE = 0;
     public static final int MODE_DIRECT = 1; 
     public static final int MODE_LIBMEDIA = 2;
     public static final int MODE_CALLBACK = 3;
     
     private static int ctx = 0;
     private static PlayThread th;
     private static boolean stopped = true;
     public static int tracklen;
     
     String file;
     private boolean paused;
     private int driver_mode = MODE_LIBMEDIA;    // driver mode in client preferences
     private boolean permsOkay = false;
 
     final private String MSM_DEVICE = "/dev/msm_pcm_out";
     // Ad hoc value. 0x2000 seems to be a maximum used by the driver. MSM datasheets needed. 
     private int volume = 0x1000;
     int startpos;
     
     LosslessMediaCodecHandlerCallback cb;
     
     private class PlayThread extends Thread
     {
         public void run()
         {
             stopped = false;
             Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
             try
             {               
                 if(file.endsWith(".ape") || file.endsWith(".APE"))
                 {
                     if(initAudioMode(driver_mode)) apePlay(ctx,file,0);
                 }
                 else if(file.endsWith(".flac") || file.endsWith(".FLAC"))
                 {
                     if(initAudioMode(driver_mode)) flacPlay(ctx,file,startpos);
                 }
                 else if(file.endsWith(".m4a") || file.endsWith(".M4A"))
                 {
                     if(initAudioMode(driver_mode)) alacPlay(ctx,file,0);
                 }
                 else if(file.endsWith(".wav") || file.endsWith(".WAV"))
                 {
                     if(initAudioMode(driver_mode)) wavPlay(ctx,file,0);
                 }
                 else if(file.endsWith(".wv") || file.endsWith(".WV"))
                 {
                     if(initAudioMode(driver_mode)) wvPlay(ctx,file,0);
                 }
                 else if(file.endsWith(".mpc") || file.endsWith(".MPC"))
                 {
                     if(initAudioMode(driver_mode)) mpcPlay(ctx,file,0);
                 }
                 if(cb != null && !stopped)
                 {
                       audioStop(ctx);
                       stopped = true;
                       cb.onCompletion();
                 }
             }
             catch(Exception e)
             { 
             }
         }
     }
     
     public void setCallback(LosslessMediaCodecHandlerCallback call)
     {
         cb = call;
     }
     
     private boolean initAudioMode(int mode)
     {
         if(mode == MODE_DIRECT && !permsOkay)
         {
             if(checkSetDevicePermissions())
             {
                 permsOkay = true;
             }
             else 
             {
                 return false;
             }
         }
         try
         {
             ctx = audioInit(ctx,mode);
         }
         catch(Exception e)
         {
             return false;
         }
         if(ctx == 0)
         {
             return false;
         }
         if(mode == MODE_DIRECT) audioSetVolume(ctx,volume);
         return true;
     }
     
     public boolean checkSetDevicePermissions()
     {
         java.lang.Process process = null;
         DataOutputStream os = null;
         try
         {
             File f = new File(MSM_DEVICE);
             if(f.canRead() && f.canWrite() /* && f1.canRead() && f1.canWrite() */)
             {
                 return true;
             }
             process = Runtime.getRuntime().exec("su");
             os = new DataOutputStream(process.getOutputStream());
             os.flush();
             os.writeBytes("chmod 0666 " + MSM_DEVICE + "\n");
             os.flush();
             os.writeBytes("exit\n");
             os.flush();
             process.waitFor();
         }
         catch (Exception e)
         { 
             return false; 
         }
         finally
         {
             try
             {
                 if(os != null) os.close();
                 process.destroy();
             }
             catch(Exception e){}
         }
         return true;
     }
     
     public void setDataSource(String source)
     {
         file = source;
     }
     
     public boolean stop()
     {
         stopped = true;
         if(th != null)
         { 
             if(ctx != 0)
             {
                 audioStop(ctx);
             }
 
             if(paused)
             {
                 paused = false;
             }
 
             try
             {
                 th.join();
             }
             catch(Exception e){}
             
             th = null;
         }
         return true;
     }
     
     public boolean start()
     {
        if(th != null)
        {
            if(!stopped)
            {
                if(paused)
                {
                    resume();
                }
            }
        }
        else
        {
            seekTo(0);
        }
         return true;
     }
     
     public boolean seekTo(int p)
     {
         stop();
         th = new PlayThread();
         startpos = p / 1000;
         th.start();
         return true;
     }
     
     public boolean pause()
     {
         int i = Process.getThreadPriority(Process.myTid()); 
         Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
         if(audioPause(ctx)) paused = true;
         Process.setThreadPriority(i);
         return paused == true;
     }
     
     public boolean resume()
     {
         int i = Process.getThreadPriority(Process.myTid()); 
         Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
         if(audioResume(ctx)) paused = false;
         Process.setThreadPriority(i);
         return paused == false;
     }
     
     public boolean dec_vol()
     {
         int i = Process.getThreadPriority(Process.myTid()); 
         Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
         if(volume >= 0x400)
         {
             volume -= 0x400;
             audioSetVolume(ctx, volume);
         }
         else if(volume != 0) audioSetVolume(ctx, 0);
         Process.setThreadPriority(i);
         return true;
     }
     
     public boolean inc_vol()
     {
         int i = Process.getThreadPriority(Process.myTid()); 
         Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
         if(volume <= 0x2000)
         {
             volume += 0x400;
             audioSetVolume(ctx, volume);
         }
         Process.setThreadPriority(i);
         return true;
     }
     
     public int getCurrentPosition()
     {
         if(!stopped)
         {
             return (startpos + audioGetCurPosition(ctx))*1000;
         }
         return 0;
     }
     
     public int getDuration()
     {
         if(!stopped)
         {
             return tracklen*1000;
         }
         return 0;
     }
     
     public boolean isPlaying()
     {
         if(!stopped)
         {
            return !paused;
         }
         else
         {
             return false;
         }
     }
     
     public static void updateTrackLen(int time)
     {
         tracklen = time;
     }
 }
