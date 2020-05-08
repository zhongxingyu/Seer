 package edu.umich.mihai.camera;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import april.jcam.ImageSource;
 import april.jmat.LinAlg;
 import april.tag.CameraUtil;
 import april.tag.TagDetection;
 import april.util.GetOpt;
 import april.vis.VisCamera;
 import april.vis.VisCanvas;
 import april.vis.VisChain;
 import april.vis.VisCircle;
 import april.vis.VisDataFillStyle;
 import april.vis.VisDataLineStyle;
 import april.vis.VisRectangle;
 import april.vis.VisWorld;
 
 public class InterCameraCalibrator
 {
     private JFrame jf;
     private VisWorld vw = new VisWorld();
     private VisCanvas vc = new VisCanvas(vw);
     private VisWorld.Buffer vbCameras = vw.getBuffer("cameras");
     private VisWorld.Buffer vbTags = vw.getBuffer("tags");
     private final double version = 0.1; 
 
     final double f = 477.5;
     final double tagSize = 0.1275;
     
     private HashMap<String, Integer> knownUrls = new HashMap<String, Integer>();
     private Camera cameras[];
     
     public InterCameraCalibrator(GetOpt opts) throws Exception
     {
         System.out.println("ICC-Constructor: starting imagereaders...");
         ArrayList<String> currentUrls = organizeURLS();
         cameras = new Camera[currentUrls.size()];
         
         setKnownUrls();
         for (int x = 0; x < cameras.length; x++)
         {
             try
             {
                 ImageReader ir = new ImageReader(currentUrls.get(x), opts.getString("resolution").contains("lo"), opts.getString("colors").contains("16"), opts.getInt("fps"));
                 ir.start();
                 cameras[x] = new Camera(ir, knownUrls.get(currentUrls.get(x)));
             } catch (Exception e)
             {
                 e.printStackTrace();
             }
         }
 
         System.out.println("ICC-Constructor: imagereaders started. aggregating tags...");
         
         //aggregate tag detections
         for(Camera camera : cameras)
         {
             System.out.println("ICC-Constructor: aggregating tags of camera " + camera.getIndex());
             camera.addDetections();
         }
         
         System.out.println("ICC-Constructor: tags aggregated. finding coordinates...");
         
         Arrays.sort(cameras, new CameraComparator());
         
         findCoordinates();
         
         System.out.println("ICC-Constructor: coordinates found. finding intercam pos...");
         
         findInterCamPos();
         
         System.out.println("ICC-Constructor: intercam pos found. setting up display..."); // XXX
         
         jf = new JFrame("Inter Camera Calibrater v" + version);
         jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         jf.setLayout(new BorderLayout());
         jf.add(vc, BorderLayout.CENTER);
         jf.setSize(1000, 500);
 
         System.out.println("ICC-Constructor: display set up. graphing tags/cameras..."); // XXX
         
         String output="";
         for(Camera cam : cameras)
         {
             Color color = (cam.getIndex()==11 ? Color.blue : Color.red);
             double[] pos = cam.getPosition();
             output+="camera: " + cam.getIndex() + "\n";
             output+="(x,y,z): " + pos[0] + ", " + pos[1] + ", " + pos[2] + "\n";
             output+="(r,p,y): " + pos[3] + ", " + pos[4] + ", " + pos[5] + "\n\n";
             double[][] camM = cam.getTransformationMatrix();
             vbCameras.addBuffered(new VisChain(camM, new VisCamera(color, 0.08)));
             ArrayList<TagDetection> tags = cam.getDetections();
             for(TagDetection tag : tags)
             {
                 double tagM[][] = CameraUtil.homographyToPose(f, f, tagSize, tag.homography);
                 vbTags.addBuffered(new VisChain(camM, tagM, new VisRectangle(tagSize, tagSize, 
                         new VisDataLineStyle(color, 2))));
             }
         }
 
         vbTags.switchBuffer();
         vbCameras.switchBuffer();
         jf.setVisible(true);
         JOptionPane.showMessageDialog(jf,output);
     }
     
     private void setKnownUrls()
     {
         knownUrls.put("dc1394://b09d01008b51b8", 0);
         knownUrls.put("dc1394://b09d01008b51ab", 1);
         knownUrls.put("dc1394://b09d01008b51b9", 2);
         knownUrls.put("dc1394://b09d01009a46a8", 3);
         knownUrls.put("dc1394://b09d01009a46b6", 4);
         knownUrls.put("dc1394://b09d01009a46bd", 5);
         
         knownUrls.put("dc1394://b09d01008c3f62", 10); // in lab
         knownUrls.put("dc1394://b09d01008c3f6a", 11); // has J on it
         knownUrls.put("dc1394://b09d01008e366c", 12); // unmarked
     }
 
     private ArrayList<String> organizeURLS()
     {
         ArrayList<String> urls = ImageSource.getCameraURLs();
         
         for(int x = 0; x < urls.size(); x++)
         {
             if(!urls.get(x).contains("dc1394"))
             {
                 urls.remove(x);
             }
         }
         
         return urls;
     }
     
 
     private void findCoordinates()
     {
         for(int cam = 1; cam < cameras.length; cam++)
         {
             if(cameras[cam].getTagCount() == 0)
             {
                 System.err.println("InterCameraCalibrator-findCoordinates: "+
                         "Unable to calculate intercamera coordinates due to a camera."+
                         " seeing no tags.");
                 System.exit(1);
             }
             
             for(int main = 0; main < cameras.length; main++)
             {
                 if(main == cam) continue;
                 
                 findCoordinates(main, cameras[main], cameras[cam]);
 
                 if(cameras[cam].isCertain())
                 {
                     break;
                 }
             }
            
             if(!cameras[cam].isCertain())
             {
                 System.err.println("InterCameraCalibrator-findCoordinates: "+
                     "Unable to calculate intercamera coordinates due to high uncertainty.");
                 System.exit(1);
             }
         }
     }
 
     private void findCoordinates(int main, Camera mainCam, Camera auxCam)
     {
         int mainIndex = 0;
         int auxIndex = 0;
         TagDetection auxTags[] = auxCam.getDetections().toArray(new TagDetection[1]);
         TagDetection mainTags[] = mainCam.getDetections().toArray(new TagDetection[1]);
         double mainM[][] = CameraUtil.homographyToPose(f, f, tagSize, mainTags[mainIndex].homography);
         double auxM[][];
         
         auxCam.setMain(main);
         auxCam.clearCoordinates();
         
        while(mainIndex < mainTags.length || auxIndex < auxTags.length)
         {
             auxM = CameraUtil.homographyToPose(f, f, tagSize, auxTags[auxIndex].homography);
             mainM = CameraUtil.homographyToPose(f, f, tagSize, mainTags[mainIndex].homography);
             
             if(auxTags[auxIndex].id == mainTags[mainIndex].id)
             {
                 auxCam.addCoordinates(LinAlg.matrixAB(mainM, LinAlg.inverse(auxM)));
                 mainIndex++;
                 auxIndex++;
             }
             else if (auxTags[auxIndex].id > mainTags[mainIndex].id)
             {
                 mainIndex++;
             }
             else
             {
                 auxIndex++;
             }
         }
     }
 
     private void findInterCamPos() throws Exception
     {
         cameras[0].setPosition(new double[] {0,0,0,0,0,0}, 0);
         for(int cam = 1; cam <cameras.length; cam++)
         {
             cameras[cam].setPosition();
             while(cameras[cam].getMain() != 0)
             {
                 if(cameras[cam].getMain() == cam) 
                 {
                     throw new Exception("Camera to camera calibration has halted"+
                     " due to a cycle in camera to camera positions");
                 }
                 
                 double[][] pos = cameras[cam].getTransformationMatrix();
                 double[][] posToOldMain= cameras[cameras[cam].getMain()].getTransformationMatrix(); 
                 double[][] posToNewMain = LinAlg.matrixAB(pos, posToOldMain); 
 
                 cameras[cam].setPosition(posToNewMain, cameras[cameras[cam].getMain()].getMain());
             }
         }
     }
 
     
     public static void main(String[] args) throws Exception
     {
         GetOpt opts = new GetOpt();
         
         opts.addBoolean('h', "help", false, "See this help screen");
         opts.addString('c', "colors", "gray8", "gray8 or gray16");
         opts.addInt('f', "fps", 15, "set the max fps to publish");
         opts.addString('l', "log", "default.log", "name of lcm log");
         opts.addString('d', "dir", "/tmp/imageLog/", "Path to save images");
         opts.addString('r', "resolution", "hi", "lo=380x240, hi=760x480");
         
         if (!opts.parse(args))
         {
             System.out.println("option error: " + opts.getReason());
         }
         
         if (opts.getBoolean("help"))
         {
             System.out.println("Usage: Calibrate relative positions of multiple cameras.");
             opts.doHelp();
             System.exit(1);
         }
         
         if(ImageSource.getCameraURLs().size() == 0)
         {
             System.out.println("No cameras found.  Are they plugged in?");
             System.exit(1);
         }
         
         new InterCameraCalibrator(opts);
     }
 }
