 package c3i.jpgGen.server.singleJpg;
 
 
 import c3i.core.imageModel.shared.IBaseImageKey;
 import c3i.core.imageModel.shared.PngSegment;
 import c3i.core.imageModel.shared.PngSegments;
 import c3i.jpgGen.shared.Stats;
 import c3i.repo.server.Repos;
 import c3i.repo.server.SeriesRepo;
 import c3i.repo.server.rt.RtRepo;
 import com.google.common.io.Closeables;
 import com.google.common.io.Files;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.jgit.lib.ObjectLoader;
 import org.eclipse.jgit.lib.ObjectStream;
 import org.imgscalr.Scalr;
 
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
 import javax.imageio.stream.FileImageOutputStream;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 
 /**
  * Takes multiple pngs and turns them into a single jpg
  */
 public class BaseImageGenerator {
 
     public static final float QUALITY = 75F * .01F;
 
     private final IBaseImageKey baseImage;
     private final SeriesRepo seriesRepo;
 
     public BaseImageGenerator(final Repos repos, final IBaseImageKey baseImage) {
         this.baseImage = baseImage;
         this.seriesRepo = repos.getSeriesRepo(baseImage.getSeriesKey());
     }
 
     public void generate() {
         this.generate(null);
     }
 
     public void generate(Stats stats) {
         if (getOutputFile().exists()) return;
         BufferedImage combined = combinePngs(stats);
         BufferedImage scaled = maybeScale(combined, stats);
         writeBaseImage(stats, scaled);
     }
 
     private void writeBaseImage(Stats stats, BufferedImage scaled) {
         if (baseImage.isJpg()) {
             writeBaseJpg(scaled, stats);
         } else {
             writeBasePng(scaled, stats);
         }
     }
 
     private boolean includeBackground() {
         return baseImage.getProfile().includeBackgroundLayer();
     }
 
     private BufferedImage combinePngs(Stats stats) {
 
         long t1 = System.currentTimeMillis();
 
         BufferedImage newImage = null;
         Graphics2D newGraphics = null;
 
         PngSegments pngKeys = baseImage.getPngKeys();
 
         boolean zeroPngIsBackground = false;
 
         int i = 0;
         for (PngSegment pngKey : pngKeys.getPngs()) {
             BufferedImage layerPng = readSrcPng(pngKey.getShortSha(), stats);
 
             boolean background = layerPng.getTransparency() == Transparency.OPAQUE;
 
             if (newImage == null) {
                 zeroPngIsBackground = background;
 
                 int w = layerPng.getWidth();
                 int h = layerPng.getHeight();
 
                 int combinedImageType;
                if (includeBackground()) { //is final thing supposed to be a jpg
                     assert zeroPngIsBackground;
                     combinedImageType = BufferedImage.TYPE_INT_RGB;
                 } else {
                     combinedImageType = BufferedImage.TYPE_INT_ARGB;
                 }
 
                 newImage = new BufferedImage(w, h, combinedImageType);
                 newGraphics = newImage.createGraphics();

                //If layer zero is supposed to be a background
                //but it somehow has some transparency (like Tundra undercarriage)
                //then make sure those alpha portions are not accidentally turned black.
                if (includeBackground()) {
                    newGraphics.setPaint(Color.WHITE);
                    newGraphics.fillRect(0, 0, w, h);
                }


             }
 
             if (i != 0 && background) {
                 throw new IllegalStateException();
             }
 
             boolean skip = background && !includeBackground();
 
             if (!skip) {
                 int deltaY = pngKey.getDeltaY();
                 if (deltaY != 0) {
                     newGraphics.drawImage(layerPng, 0, deltaY, null);
                 } else {
                     newGraphics.drawImage(layerPng, 0, 0, null);
                 }
             }
 
             i++;
         }
 
 
         if (newGraphics != null) {
             newGraphics.dispose();
         }
 
         long t2 = System.currentTimeMillis();
 
         if (stats != null) {
             stats.combinePngsDeltaSum += (t2 - t1);
         }
 
         return newImage;
     }
 
     private static BufferedImage readSrcImage(InputStream is) {
         try {
             BufferedImage bi = ImageIO.read(is);
             return bi;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void writeBaseJpg(BufferedImage input, Stats stats) {
         long t1 = System.currentTimeMillis();
 
         Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("JPG");
         if (!iter.hasNext()) throw new IllegalStateException();
 
         ImageWriter writer = iter.next();
         ImageWriteParam iwp = writer.getDefaultWriteParam();
 
         iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
 
         iwp.setCompressionQuality(QUALITY);
 
         FileImageOutputStream output = null;
         try {
             output = newFileImageOs(getOutputFile());
             writer.setOutput(output);
             IIOImage image = new IIOImage(input, null, null);
             writeImage(writer, iwp, image);
         } catch (Exception e) {
             throw new RuntimeException(e);
         } finally {
             if (output != null) {
                 try {
                     output.close();
                 } catch (IOException e) {
                     log.warn("Problem closing file [" + getOutputFile() + "]");
                 }
             }
         }
 
 
         long t2 = System.currentTimeMillis();
         if (stats != null) {
             stats.writeBaseImageDeltaSum += (t2 - t1);
         }
 
     }
 
     private void writeBasePng(BufferedImage input, Stats stats) {
         long t1 = System.currentTimeMillis();
         try {
             File outputFile = getOutputFile();
             Files.createParentDirs(outputFile);
             ImageIO.write(input, "PNG", outputFile);
         } catch (IOException e) {
             throw new RuntimeException(e);
         } finally {
             long t2 = System.currentTimeMillis();
             if (stats != null) {
                 stats.writeBaseImageDeltaSum += (t2 - t1);
             }
         }
 
 
     }
 
     private static Log log = LogFactory.getLog(BaseImageGenerator.class);
 
     private File getOutputFile() {
         RtRepo genRepo = seriesRepo.getRtRepo();
         return genRepo.getBaseImageFileName(baseImage);
     }
 
     private void createParentDirs(File jpgFile) {
         try {
             Files.createParentDirs(jpgFile);
         } catch (IOException e) {
             throw new RuntimeException();
         }
     }
 
     private BufferedImage maybeScale(BufferedImage imageIn, Stats stats) {
 
         if (baseImage.getProfile().isStandard()) {
             return imageIn;
         }
 
         int wIn = imageIn.getWidth();
         int wOut = baseImage.getProfile().getWidth();
 
         if (wOut == wIn) {
             return imageIn;
         }
 
         long t1 = System.currentTimeMillis();
         BufferedImage retVal = Scalr.resize(imageIn, wOut);
         long t2 = System.currentTimeMillis();
 
         if (stats != null) {
             stats.maybeScalePngDeltaSum += (t2 - t1);
         }
         return retVal;
 
 
     }
 
 
     private static FileImageOutputStream newFileImageOs(File outFile) {
         try {
             Files.createParentDirs(outFile);
             return new FileImageOutputStream(outFile);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private static void writeImage(ImageWriter writer, ImageWriteParam iwp, IIOImage image) {
         try {
             writer.write(null, image, iwp);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private BufferedImage readSrcPng(String pngShortSha, Stats stats) {
         long t1 = System.currentTimeMillis();
         ObjectLoader objectLoader = seriesRepo.getSrcPngByShortSha(pngShortSha);
         ObjectStream is = null;
         try {
             is = objectLoader.openStream();
             return readSrcImage(is);
         } catch (IOException e) {
             throw new RuntimeException(e);
         } finally {
             Closeables.closeQuietly(is);
             long t2 = System.currentTimeMillis();
             if (stats != null) {
                 stats.readSrcPngDeltaSum += (t2 - t1);
             }
         }
     }
 
 
 }
