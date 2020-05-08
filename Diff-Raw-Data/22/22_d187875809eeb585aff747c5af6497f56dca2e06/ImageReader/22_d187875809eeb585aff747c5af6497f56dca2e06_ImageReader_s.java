 package mihai.camera;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import magic.camera.util.SyncErrorDetector;
 import mihai.util.CameraException;
 import mihai.util.ConfigException;
 import mihai.util.Util;
 import april.config.Config;
 import april.config.ConfigFile;
 import april.jcam.ImageSource;
 import april.jcam.ImageSourceFormat;
 
 /**
  * Sets camera to a certain resolution, color format, and framerate and reads off images (byte[]) and handles this for all subscribed listeners
  * 
  * @author Mihai Bulic
  *
  */
 public class ImageReader extends Thread
 {
 	private Config config;
 	
     private ArrayList<Listener> Listeners = new ArrayList<Listener>();
 
     private int id;
     private String url;
     private ImageSource isrc;
     private ImageSourceFormat ifmt;
     private SyncErrorDetector sync;
 
     private boolean run;
     private boolean done = false;
     private Object lock = new Object(); 
     
     public interface Listener
     {
         public void handleImage(byte[] image, long timeStamp, int camera);
     }
     
     public ImageReader(String url) throws CameraException, IOException, ConfigException
     {
     	this(new ConfigFile(System.getenv("APRIL_CONFIG")+ File.separator +"camera.config"), url);
     }
     
     public ImageReader(boolean loRes, boolean color16, int maxfps, String url) throws CameraException, IOException, ConfigException
     {
     	this(setConfig(loRes, color16, maxfps), url);
     }
     
     public ImageReader(Config config, String url) throws CameraException, IOException, ConfigException
     {
         Util.verifyConfig(config);
 
     	run = Util.isValidUrl(config, url);
     	if(run)
     	{
     	    this.url = url;
     	    this.config = config;
     	    
     	    boolean loRes = config.requireBoolean("loRes");
     	    boolean color16 = config.requireBoolean("color16");
     	    int maxfps = config.requireInt("fps");
     	    id = config.getChild(Util.getSubUrl(config, url)).requireInt("id");
     	    
     	    if (maxfps > (loRes ? CameraException.MAX_LO_RES : CameraException.MAX_HI_RES)) throw new CameraException(CameraException.FPS);
     	    setIsrc(loRes, color16, maxfps, url);
 
     	    config = config.getRoot().getChild("sync");
 	        Util.verifyConfig(config);
     	    sync = new SyncErrorDetector(config);
     	}
     }
 
     public void run()
     {
 		isrc.start();
     	
 //        boolean firstTime = true;
 //        double initTime = 0;
 //        double lastTimestamp = 0;
 //        int rollOverCounter = 0;
 //        double[] times;
         
         while (run)
         {
             byte imageBuffer[] = isrc.getFrame();
             
             if(imageBuffer != null)
             {
                 sync.addTimePointGreyFrame(imageBuffer);
 //                times = sync.getTimes();
 //
 //                if(firstTime)
 //                {
 //                    firstTime = false;
 //                    initTime = times[times.length-1];
 //                }
 //                
                 int status = sync.verify();
                 if(status == SyncErrorDetector.SYNC_GOOD)
                 {
 //                    long timestamp = (long)(times[times.length-1] - initTime); // TODO use time sync stuff
 //                    if(lastTimestamp > timestamp)
 //                    {
 //                        rollOverCounter++;
 //                    }
 //                    lastTimestamp = timestamp;
 //                    timestamp += 128*rollOverCounter;
                     
                     for (Listener listener : Listeners)
                     {
                        listener.handleImage(imageBuffer, 0, id);
                     }
                 } 
                 else if (status == SyncErrorDetector.RECOMMEND_ACTION)
                 {
                 	try
                     {
                         toggleImageSource(isrc);
                     } catch (ConfigException e)
                     {
                         e.printStackTrace();
                     }
 //                	firstTime = true;
                 }
 	        }
     	}
         
         isrc.stop();
         synchronized(lock)
         {
             done = true;
             lock.notify();
         }
     }
 
     private static Config setConfig(boolean loRes, boolean color16, int maxfps) throws IOException
     {
     	Config config = new ConfigFile("$CONFIG/camera.config");
 
     	config.setBoolean("loRes", loRes);
     	config.setBoolean("color16", color16);
     	config.setInt("maxfps", maxfps);
     	
     	return config;
     }
     
     public int getCameraId()
     {
     	return id;
     }
     
     private void setIsrc(boolean loRes, boolean color16, int fps, String urls) throws IOException
     {
         isrc = ImageSource.make(urls);
         
         int features[] = config.getInts("isrc_feature");
         for(int x = 0; x < features.length; x++)
         {
         	isrc.setFeatureValue(x, features[x]);
         }
 
         // 760x480 8 = 0, 760x480 16 = 1, 380x240 8 = 2, 380x240 16 = 3
         // converts booleans to 1/0 and combines them into an int
         isrc.setFormat(Integer.parseInt("" + (loRes ? 1 : 0) + (color16 ? 1 : 0), 2));
         isrc.setFeatureValue(11, fps); // frame-rate, idx=11
 
         ifmt = isrc.getCurrentFormat();
     }
     
     private void toggleImageSource(ImageSource isrc) throws ConfigException
     {
         isrc.stop();
         int currentFormat = isrc.getCurrentFormatIndex();
         isrc.setFormat(currentFormat);
 
         isrc.start();
         config = config.getRoot().getChild("sync");
         Util.verifyConfig(config);
         sync = new SyncErrorDetector(config);
     }
 
     public String getUrl()
     {
         return url;
     }
     
     public int getWidth()
     {
         return ifmt.width;
     }
     
     public int getHeight()
     {
         return ifmt.height;
     }
     
     public String getFormat()
     {
         return ifmt.format;
     }
     
     public void addListener(Listener listener)
     {
         Listeners.add(listener);
     }
     
     public void kill() throws InterruptedException
     {
         run = false;
         synchronized(lock)
         {
             while(!done)
             {
                 lock.wait();
             }
         }
     }
     
     public boolean isGood()
     {
     	return run;
     }
     
     public void setFramerate(int fps)
     {
 		isrc.setFeatureValue(11, fps); // frame-rate, idx=11
     }
     
     public void setFormat(boolean loRes, boolean color16)
     {
         isrc.stop();
         isrc.setFormat(Integer.parseInt("" + (loRes ? 1 : 0) + (color16 ? 1 : 0), 2));
         ifmt = isrc.getCurrentFormat();
         isrc.start();
     }
 }
