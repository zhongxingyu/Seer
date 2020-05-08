 package rgbdslam.test;
 
 import april.jmat.LinAlg;
 import april.util.Tic;
 import april.vis.*;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.image.*;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import rgbdslam.*;
 import rgbdslam.DescriptorMatcher.Match;
 
 /**
  *
  * @author pdaquino
  */
 public class DescriptorMatcherTest {
 
     // pass it the name of an image and it will compute the features in it
     public static void main(String[] args) throws IOException {
         //sillyTest(args);
         visualTest(args);
     }
     static VisWorld vw = new VisWorld();
     static VisLayer vl = new VisLayer(vw);
     static VisCanvas vc = new VisCanvas(vl);
     static JFrame jf = new JFrame("Kinect Demo");
 
     protected static void initFrame() {
         jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         jf.setLayout(new BorderLayout());
         jf.setSize(1280, 500);
         jf.add(vc, BorderLayout.CENTER);
 
         jf.setVisible(true);
     }
 
     protected static BufferedImage getRGBImage(String filename) throws IOException {
         BufferedImage im = ImageIO.read(new File(filename));
         BufferedImage rgbIm = new BufferedImage(im.getWidth(),
                 im.getHeight(), BufferedImage.TYPE_INT_RGB);
         rgbIm.getGraphics().drawImage(im, 0, 0, null);
         return rgbIm;
     }
 
     protected static int[] getImageAsIntArray(BufferedImage rgbIm) {
         return ((DataBufferInt) (rgbIm.getRaster().getDataBuffer())).getData();
     }
 
     protected static VisChain translate(double translation, VisObject... os) {
         Object[] objects = new Object[os.length + 1];
         objects[0] = LinAlg.translate(translation, 0);
         System.arraycopy(os, 0, objects, 1, os.length);
 
         return new VisChain(objects);
     }
 
     protected static void visualTest(String[] args) throws IOException {
         BufferedImage im1 = getRGBImage(args[0]);
         BufferedImage im2 = getRGBImage(args[1]);
 
         List<ImageFeature> features1 = OpenCV.extractFeatures(getImageAsIntArray(im1), im1.getWidth(), 300, OpenCV.DEFAULT_MIN_QUALITY, OpenCV.DEFAULT_MIN_DISTANCY, OpenCV.DEFAULT_BLOCK_SIZE);
         List<ImageFeature> features2 = OpenCV.extractFeatures(getImageAsIntArray(im2), im2.getWidth(), 300, OpenCV.DEFAULT_MIN_QUALITY, OpenCV.DEFAULT_MIN_DISTANCY, OpenCV.DEFAULT_BLOCK_SIZE);
         List<Match> matches = new DescriptorMatcher(features1, features2).match();
 
         initFrame();
         int imageSeparation = im1.getWidth() + 10;
         {
             VisWorld.Buffer vbIm = vw.getBuffer("image");
             vbIm.addBack(new VzImage(im1, VzImage.FLIP));
             vbIm.addBack(translate(imageSeparation, new VzImage(im2, VzImage.FLIP)));
 
             for (ImageFeature feature : features1) {
                 double[] xy = ImageUtil.flipXY(feature.xyAsDouble(), im1.getHeight());
                 vbIm.addBack(new VzPoints(new VisVertexData(xy),
                         new VzPoints.Style(Color.red, 5)));
             }
 
             for (ImageFeature feature : features2) {
                 double[] xy = ImageUtil.flipXY(feature.xyAsDouble(), im2.getHeight());
                 vbIm.addBack(translate(imageSeparation, new VzPoints(new VisVertexData(xy),
                         new VzPoints.Style(Color.blue, 5))));
             }
 
             ArrayList<double[]> correspondences = new ArrayList<double[]>();
 
             double avgDifference = 0;
             for (Match match : matches) {
                System.out.println(match.descriptorDistance);
                avgDifference += match.descriptorDistance;
                 double[] match1 = ImageUtil.flipXY(match.feature1.xyAsDouble(), im1.getHeight());
                 double[] match2 = ImageUtil.flipXY(match.feature2.xyAsDouble(), im2.getHeight());
                 match2[0] += imageSeparation;
                 correspondences.add(match1);
                 correspondences.add(match2);
             }
 
             System.out.println("Matches: " + matches.size() + ". Average error: " + avgDifference/matches.size());
             
             VisColorData vcd = new VisColorData();
             for (int i = 0; i < correspondences.size()/2; i++) {
                 int c = ColorUtil.randomColor(255).getRGB();
                 vcd.add(c);
                 vcd.add(c);
             }
 
             VzLines lines = new VzLines(new VisVertexData(correspondences),
                     VzLines.LINES,
                     new VzLines.Style(vcd, 2));
             vbIm.addBack(lines);
 
             vbIm.swap();
         }
         System.out.println("Press enter to exit");
         System.in.read();
     }
 
     protected static void sillyTest(String[] args) throws IOException {
         // XXX this is a pretty dumb test, but it's quick and checks if the
         // implementation is obviously wrong. however, it does not help a tiny
         // bit in assessing whether the matching is good enough. to do that
         // we'd need to 1) not use the same image twice 2) display the two
         // images side to side, with lines connecting matching features
 
         BufferedImage im = ImageIO.read(new File(args[0]));
         BufferedImage rgbIm = new BufferedImage(im.getWidth(),
                 im.getHeight(), BufferedImage.TYPE_INT_RGB);
         rgbIm.getGraphics().drawImage(im, 0, 0, null);
         int[] buf = ((DataBufferInt) (rgbIm.getRaster().getDataBuffer())).getData();
 
         OpenCV wrapper = new OpenCV();
         double timeMatching = 0;
         double timeExtraction = 0;
 
         // make n = 1 if you just want a quick correctness test
         int n = 100;
         for (int i = 0; i < n; i++) {
             Tic tic = new Tic();
             List<ImageFeature> features1 = OpenCV.extractFeatures(buf, im.getWidth());
             List<ImageFeature> features2 = OpenCV.extractFeatures(buf, im.getWidth());
             timeExtraction += tic.toc();
 
             System.out.println(features1.size() + " features found.");
 
             tic = new Tic();
             List<DescriptorMatcher.Match> matches = new DescriptorMatcher(features1, features2).match();
             for (DescriptorMatcher.Match match : matches) {
                 if (!Arrays.equals(match.feature1.xy(), match.feature2.xy())) {
                     System.err.println("Wrong match!");
                 }
             }
             timeMatching += tic.toc();
         }
 
         System.out.printf("Extraction: %.5f\nMatching: %.5f\n", timeExtraction / n, timeMatching / n);
     }
 }
