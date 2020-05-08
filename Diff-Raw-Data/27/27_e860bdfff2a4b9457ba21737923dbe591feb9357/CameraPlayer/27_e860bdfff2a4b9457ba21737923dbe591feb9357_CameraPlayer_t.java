 package edu.umich.mihai.camera;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import javax.swing.JFrame;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import lcm.lcm.LCM;
 import lcm.lcm.LCMDataInputStream;
 import lcm.lcm.LCMSubscriber;
 import april.jcam.ImageConvert;
 import april.jmat.LinAlg;
 import april.util.GetOpt;
 import april.util.TimeUtil;
 import april.vis.VisCanvas;
 import april.vis.VisChain;
 import april.vis.VisImage;
 import april.vis.VisWorld;
 import april.vis.VisWorld.Buffer;
 import edu.umich.mihai.lcmtypes.image_path_t;
 
 /**
  * 
  * Plays back the video from a camera. Images are selected from the LCM message stream of image paths.
  *  The images themselves are saved onto the HDD at the location specified by the messages.
  * 
  * @author Mihai Bulic
  *
  */
 public class CameraPlayer implements LCMSubscriber
 {
     static LCM lcm = LCM.getSingleton();
     int width = 0;
     int height = 0;
     int rows;
     int columns;
     
     public BufferedImage newImage;
     
     VisWorld vw;
     VisCanvas vc;
     HashMap<Integer, VisWorld.Buffer> buffers = new HashMap<Integer, VisWorld.Buffer>();
     JFrame jf;
     
     public CameraPlayer(int rows, int columns)
     {
         this.rows = rows;
         this.columns = columns;
         
         vw = new VisWorld();
         vc = new VisCanvas(vw);
         jf = new JFrame("Camera player");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         jf.add(vc);
         jf.setSize(500, 500);
         jf.setVisible(true);
 
         lcm.subscribeAll(this);
         
         while(true)
         {
             TimeUtil.sleep(100);
         }
     }
     
     public CameraPlayer(int camera)
     {
         vw = new VisWorld();
         vc = new VisCanvas(vw);
         jf = new JFrame("Camera " + camera + " player");
         
         jf.add(vc);
         jf.setSize(500, 500);
         jf.setVisible(true);
 
         lcm.subscribe("cam"+camera, this);
 
         while(true)
         {
             TimeUtil.sleep(100);
         }
     }
 
     @Override
     public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
     {
         try
         {
             if (channel.contains("cam"))
             {
                 Pattern intsOnly = Pattern.compile("\\d+");
                 Matcher makeMatch = intsOnly.matcher(channel);
                 makeMatch.find();
                 int camera = Integer.parseInt(makeMatch.group());
                 
                image_path_t imagePath = new image_path_t(ins);
                byte[] buffer = new byte[752*480];
                new FileInputStream(new File(imagePath.img_path)).read(buffer);
                newImage = ImageConvert.convertToImage(imagePath.format,imagePath.width, imagePath.height, buffer);
                 
                 VisWorld.Buffer vb = (Buffer) (buffers.containsKey(camera) ? buffers.get(camera) : vw.getBuffer("cam"+camera));
                 vb.addBuffered(new VisChain(LinAlg.translate(new double[] {width*(camera%columns),-height*(camera/rows),0}), new VisImage(newImage)));
                 if(newImage.getWidth() != width || newImage.getHeight() != height)
                 {
                     width = newImage.getWidth();
                     height = newImage.getHeight();
                     vc.getViewManager().viewGoal.fit2D(new double[] { 0, 0 }, new double[] { width, height });
                 }
                 vb.switchBuffer();
                 
                 if(!buffers.containsKey(camera))
                 {
                     buffers.put(camera, vb);
                 }
             }
         } catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     public static void main(String[] args)
     {
         GetOpt opts = new GetOpt();
         
         opts.addBoolean('h', "help", false, "See this help screen");
         opts.addInt('k', "camera", 0, "index of camera for which to get images");
         opts.addBoolean('a', "all", true, "display images from all cameras");
         opts.addInt('x', "rows", 3, "number of rows in which to display camera images");
         opts.addInt('y', "columns", 3, "number of columns in which to display camera images");
         
         if (!opts.parse(args))
         {
             System.out.println("option error: " + opts.getReason());
         }
         
         if (opts.getBoolean("help"))
         {
             System.out.println("Usage: displays images from camera specified");  
             opts.doHelp();
             System.exit(1);
         }
         
         if(opts.getBoolean("all"))
         {
             new CameraPlayer(opts.getInt("rows"), opts.getInt("columns"));
         }
         else
         {
             new CameraPlayer(opts.getInt("camera"));
         }
     }
 }
