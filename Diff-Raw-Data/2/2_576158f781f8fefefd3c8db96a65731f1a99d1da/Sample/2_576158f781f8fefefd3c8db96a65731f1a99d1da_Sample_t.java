 package com.innovatrics.iseglib;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A sample class demonstrating the usage of the Connector.
  * @author Martin Vysny
  */
 public class Sample {
 
     public static void main(String[] args) throws Exception {
         if (args == null || args.length != 1) {
             System.out.println("Usage: sample slap_image.png");
             System.exit(-1);
         }
         final SegLib lib = SegLib.getInstance();
         lib.init();
         System.out.println("Using SegLib SDK, version: " + lib.getVersion());
         final byte[] in = read(args[0]);
         final RawImage img = lib.convertToRaw(in, in.length, SegLibImageFormatEnum.PNG);
         System.out.println("Slap image: " + img);
         final int resolution = 500;
         final int minimumFingersCount = 2;
         final int maximumFingersCount = 4;
         final int expectedFingersCount = 4;
         final int maxRotation = 40;
         final int outWidth = 400;
         final int outHeight = 500;
        final SegmentationResult segs = lib.segmentFingerprints(img, resolution, expectedFingersCount, minimumFingersCount, maximumFingersCount, maxRotation, 0, outWidth, outHeight, (byte) 255);
         write(segs.boxedBmpImage, "slap_out.bmp");
         System.out.println("Segmented image saved as slap_out.bmp");
         System.out.println("Detected fingers count: " + segs.segmentedFingersCount);
         System.out.println("Detected global rotation angle: " + segs.globalAngle);
         if (segs.segmentedFingersCount < expectedFingersCount) {
             System.out.println("Slap image does not contain all fingers.");
             if (segs.feedback.contains(SegInfoEnum.MissingFingerUnknown)) {
                 System.out.println("Missing unknown finger(s).");
             }
             if (segs.feedback.contains(SegInfoEnum.LeftHand)) {
                 if (segs.feedback.contains(SegInfoEnum.MissingFinger1)) {
                     System.out.println("Missing left little finger.");
                 }
                 if (segs.feedback.contains(SegInfoEnum.MissingFinger2)) {
                     System.out.println("Missing left ring finger.");
                 }
                 if (segs.feedback.contains(SegInfoEnum.MissingFinger3)) {
                     System.out.println("Missing left middle finger.");
                 }
                 if (segs.feedback.contains(SegInfoEnum.MissingFinger4)) {
                     System.out.println("Missing left index finger.");
                 }
             }
             if (segs.feedback.contains(SegInfoEnum.RightHand)) {
                 if (segs.feedback.contains(SegInfoEnum.MissingFinger1)) {
                     System.out.println("Missing right index finger.");
                 }
                 if (segs.feedback.contains(SegInfoEnum.MissingFinger2)) {
                     System.out.println("Missing right middle finger.");
                 }
                 if (segs.feedback.contains(SegInfoEnum.MissingFinger3)) {
                     System.out.println("Missing right ring finger.");
                 }
                 if (segs.feedback.contains(SegInfoEnum.MissingFinger4)) {
                     System.out.println("Missing right little finger.");
                 }
             }
         }
         if (segs.feedback.contains(SegInfoEnum.LeftHand)) {
             System.out.println("Detected hand position: LEFT HAND");
         }
         if (segs.feedback.contains(SegInfoEnum.RightHand)) {
             System.out.println("Detected hand position: RIGHT HAND");
         }
         for (int i = 0; i < segs.segmentedFingersCount; i++) {
             final SegmentedFingerprint fp = segs.fingerprints[i];
             System.out.println("Finger #" + (i + 1));
             System.out.println("Cointaining rectangle: " + fp.roundingBox);
             final int intensity = lib.getImageIntensity(new RawImage(outWidth, outHeight, fp.rawImage.rawImage));
 
             System.out.println("Image intensity: " + intensity);
             if (intensity < SegLib.INTENSITY_THRESHOLD_TOO_LIGHT) {
                 System.out.println("Image too light, low pressure or dry finger.");
             }
             if (intensity > SegLib.INTENSITY_THRESHOLD_TOO_DARK) {
                 System.out.println("Image too dark, too high pressure or wet finger.");
             }
             final byte[] bmp = lib.convertRawToImage(new RawImage(outWidth, outHeight, fp.rawImage.rawImage), SegLibImageFormatEnum.BMP, 0);
             final String s = "finger" + (i + 1) + ".bmp";
             write(bmp, s);
             System.out.println("Image saved as " + s);
         }
     }
 
     private static byte[] read(String name) throws IOException {
         final ByteArrayOutputStream result = new ByteArrayOutputStream();
         final InputStream in = new FileInputStream(name);
         copy(in, result);
         result.close();
         return result.toByteArray();
     }
 
     private static void write(final byte[] buf, String name) throws IOException {
         final OutputStream out = new FileOutputStream(name);
         try {
             copy(new ByteArrayInputStream(buf), out);
         } finally {
             closeQuietly(out);
         }
     }
     private static final int BUFFER_SIZE = 8192;
 
     public static void copy(InputStream in, OutputStream out) throws IOException {
         try {
             final byte[] buffer = new byte[BUFFER_SIZE];
             int read;
             while ((read = in.read(buffer)) >= 0) {
                 if (out != null) {
                     out.write(buffer, 0, read);
                 }
             }
         } finally {
             closeQuietly(in);
         }
     }
 
     public static void closeQuietly(final Closeable c) {
         if (c == null) {
             return;
         }
         try {
             c.close();
         } catch (Exception ex) {
             log.log(Level.INFO, "Failed to close a Closeable", ex);
         }
     }
     private static final Logger log = Logger.getLogger(Sample.class.getName());
 }
