 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 
 import april.jmat.*;
 import april.jmat.geom.*;
 
 import april.jcam.*;
 
 import april.util.*;
 import april.tag.*;
 import robot.lcm.*;
 
 
 public class TagTest
 {
     static final double APRIL_CODE_PIXEL_HEIGHT = 244;
     static final double PIXEL_RANGE = 20;
 
     ImageSource is;
 
     TagFamily tf;
     TagDetector detector;
 
     // usage: TagTest <imagesource> [tagfamily class]
     public static void main(String args[])
     {
         GetOpt opts  = new GetOpt();
         opts.addBoolean('h',"help",false,"See this help screen");
         opts.addString('u',"url","","Camera url");
         opts.addString('t',"tagfamily","april.tag.Tag36h11","Tag family");
 
         if (!opts.parse(args)) {
             System.out.println("option error: "+opts.getReason());
         }
 
         String url = opts.getString("url");
         String tagfamily = opts.getString("tagfamily");
 
         if (opts.getBoolean("help") || url.isEmpty()){
             System.out.println("Usage:");
             opts.doHelp();
             System.exit(1);
         }
 
         try {
             ImageSource is = ImageSource.make(url);
             TagFamily tf = (TagFamily) ReflectUtil.createObject(tagfamily);
 
             TagTest tt = new TagTest(is, tf);
 
         } catch (IOException ex) {
             System.out.println("Ex: "+ex);
         }
     }
 
     public TagTest(ImageSource is, TagFamily tf)
     {
         this.is = is;
         this.tf = tf;
 
         detector = new TagDetector(tf);
 
         ImageSourceFormat ifmt = is.getCurrentFormat();
 
         new RunThread().start();
 
     }
 
 
     class RunThread extends Thread
     {
         public void run()
         {
             is.start();
             ImageSourceFormat fmt = is.getCurrentFormat();
 
             detector = new TagDetector(tf);
 
             //These parameters are set based on April TagDetector documentation
             //and tweaked with a little experimentation
 
             //The two values below are zero because segDecimate is enabled
             //Both should be 0.8 if segDecimate is false
             detector.segSigma = 0.0;
             detector.sigma = 0.0;
             //minMag can go from 0.001 to 0.01 the higher the number the faster
             //the image processes
             detector.minMag = 0.01;
             detector.maxEdgeCost = 0.52359;
             detector.magThresh = 12000.0;
             detector.thetaThresh = 100.0;
             //Lower values are faster
             detector.WEIGHT_SCALE = 30;
             detector.segDecimate = true;
             detector.debug = false;
 
             //TODO: Figure out of these are needed
             /*detector.debugSegments  = vw.getBuffer("segments");
             detector.debugQuads     = vw.getBuffer("quads");
             detector.debugSamples   = vw.getBuffer("samples");
             detector.debugLabels    = vw.getBuffer("labels");*/
 
             MotorPublisher mp = new MotorPublisher();
             MotorSpeed ms = new MotorSpeed();
 
             while (true) {
                 FrameData frmd = is.getFrame();
                 if (frmd == null)
                     continue;
 
                 BufferedImage im = ImageConvert.convertToImage(frmd);
 
                 //Not sure what this does but this is the number set in the
                 //test program and it works so I'm going to use it
                 tf.setErrorRecoveryBits(1);
 
                 //TODO: Move this into a debug check (measures execution time)
                 Tic tic = new Tic();
 
                 //TODO: Use this detection array to get location of tags
                 //Import TagDetection class fields include:
                 //cxy: (center of tag in pixel in tag coordinates)
                 //rotation: (How many 90 degree rotations to align with code)
                 ArrayList<TagDetection> detections = detector.process(im, new double[] {im.getWidth()/2.0, im.getHeight()/2.0});
                 double dt = tic.toc();
 
                 for (TagDetection d : detections) {
                     System.out.print("CenterX: " + d.cxy[0] + "   CenterY: " + d.cxy[1]);
                     double yPix = d.cxy[1];
                     if (yPix < (APRIL_CODE_PIXEL_HEIGHT - PIXEL_RANGE)){
                         ms.rightMotor = -1;
                         ms.leftMotor = -1;
                         System.out.println("   Driving Backwards!");
                     }
                     else if (yPix > (APRIL_CODE_PIXEL_HEIGHT - PIXEL_RANGE)){
                         ms.rightMotor = 1;
                         ms.leftMotor = 1;
                         System.out.println("   Driving Forwards!");
                     }
                     else{
                         System.out.println("   In a good range!");
                     }
                 }
             }
         }
     }
 }
