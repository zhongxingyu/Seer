 package kinect;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 
 import april.jmat.*;
 import april.util.*;
 import april.vis.*;
 
 class KinectDemo
 {
     Kinect kinect = new Kinect();
 
     RenderThread rt;
     KinectThread kt;
 
     public KinectDemo(GetOpt opts)
     {
         rt = new RenderThread(opts);
         rt.start();
 
         kt = new KinectThread();
         kt.start();
     }
 
     class KinectThread extends Thread
     {
         int fps = 60;
         boolean closeFlag;
 
         public KinectThread()
         {
             closeFlag = false;
         }
 
         public void run()
         {
             System.out.println("Starting kinect thread");
             kinect.init();
             kinect.start();
 
             Kinect.Frame f = null;
             while (!closeFlag) {
                 f = kinect.getFrame();
                 if (f != null) {}
                     rt.render(f);
                 TimeUtil.sleep(1000/fps);
             }
 
             System.out.println("Buh-bye, now!");
             rt.allClear();
         }
 
         public void close()
         {
             closeFlag = true;
             kinect.stop();
             kinect.close();
         }
     }
 
     class RenderThread extends Thread
     {
         VisWorld vw;
         VisLayer vl;
         VisCanvas vc;
         boolean clearToClose = false;
 
         Kinect.Frame currFrame = null;
 
         GetOpt opts;
 
         public RenderThread(GetOpt opts)
         {
             this.opts = opts;
             System.out.println("Starting render thread");
             JFrame jf = new JFrame("Kinect Demo");
             jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             jf.addWindowListener(new WindowAdapter() {
                 synchronized public void windowClosing(WindowEvent e) {
                     System.out.println("Closing kinect...");
                     kt.close();
                     while (!clearToClose) {
                         try {
                             wait();
                         } catch (InterruptedException ex) {}
                     }
                 }
             });
             jf.setLayout(new BorderLayout());
             jf.setSize(1280, 500);
 
             vw = new VisWorld();
             vl = new VisLayer(vw);
             vc = new VisCanvas(vl);
             jf.add(vc, BorderLayout.CENTER);
 
             jf.setVisible(true);
         }
 
         synchronized public void allClear()
         {
             clearToClose = true;
             notifyAll();
         }
 
         synchronized public void render(Kinect.Frame frame)
         {
             currFrame = frame;
             notify();
         }
 
         synchronized public void run()
         {
             VisWorld.Buffer vbIm = vw.getBuffer("image");
             VisWorld.Buffer vbPts = vw.getBuffer("points");
 
             while (true) {
                 if (currFrame != null && !opts.getBoolean("point-cloud")) {
                     BufferedImage rgb = currFrame.makeRGB();
                     BufferedImage depth = currFrame.makeDepth();
 
                     double[] xy0 = new double[2];
                     double[] xy1 = new double[] {currFrame.rgbWidth, currFrame.rgbHeight};
                     double[] xy2 = new double[] {currFrame.rgbWidth, 0};
                     double[] xy3 = new double[] {currFrame.rgbWidth+currFrame.depthWidth, currFrame.depthHeight};
 
                     double[][] rgbvert = new double[][] {{0,0,0},
                                                          {currFrame.rgbWidth,0,0},
                                                          {currFrame.rgbWidth,currFrame.rgbHeight,0},
                                                          {0,currFrame.rgbHeight,0}};
                     double[][] depthvert = new double[][] {{currFrame.rgbWidth,0,0},
                                                            {currFrame.rgbWidth+currFrame.depthWidth,0,0},
                                                            {currFrame.rgbWidth+currFrame.depthWidth,currFrame.depthHeight,0},
                                                            {currFrame.rgbWidth,currFrame.depthHeight,0}};
                     double[][] texcoords = new double[][] {{0,1},
                                                            {1,1},
                                                            {1,0},
                                                            {0,0}};
 
                    double[] translate = new double[] {currFrame.rgbWidth, 0, 0};


                    /*vbIm.addBack(new VisImage(new VisTexture(rgb, false),
                                               rgbvert,
                                               texcoords,
                                               Color.white));
                     vbIm.addBack(new VisImage(new VisTexture(depth, false),
                                               depthvert,
                                               texcoords,
                                              Color.white));*/
                    vbIm.addBack(new VzImage(rgb, VzImage.FLIP));
                    vbIm.addBack(new VisChain(LinAlg.translate(translate),
                                              new VzImage(depth, VzImage.FLIP)));
 
                     vbIm.swap();
                 } else if (currFrame != null && opts.getBoolean("point-cloud")) {
                     ColorPointCloud pointCloud = new ColorPointCloud(currFrame);
                     VisVertexData vvd = new VisVertexData(pointCloud.points);
                     /*int[] colors = new int[pointCloud.numPoints()];
                     for (int i = 0; i < colors.length; i++) {
                         colors[i] = pointCloud.colors.get(i);
                     }
                     VisColorData vcd = new VisColorData(colors);*/
                     //VisConstantColor vcd = new VisConstantColor(Color.white);
 
                    //vbPts.addBack(new VisPoints(vvd, pointCloud.vcd, 1));
                    vbPts.addBack(new VzPoints(vvd, new VzPoints.Style(Color.white, 1)));
 
                     vbPts.swap();
                 }
 
                 try {
                     wait();
                 } catch (InterruptedException ex) {}
             }
         }
 
     }
 
     static public void main(String[] args)
     {
         GetOpt opts = new GetOpt();
         opts.addBoolean((char)0,"point-cloud",false,"Render colored point cloud");
         opts.addBoolean('h', "help", false, "Show this help screen");
 
         if (!opts.parse(args)) {
             System.err.println("ERR: Option failure = "+opts.getReason());
         }
 
         if (opts.getBoolean("help")) {
             opts.doHelp();
         }
 
         KinectDemo kd = new KinectDemo(opts);
     }
 }
