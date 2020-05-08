 package com.sl.xmlparser.image;
 
 import com.sl.xmlparser.config.Configuration;
 import com.sl.xmlparser.model.ProjectModel;
 import java.awt.AlphaComposite;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
 import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
 import javax.imageio.stream.ImageOutputStream;
 
 public class ImageResizer {
 
     private static final Logger logger = Logger.getLogger(ImageResizer.class.getCanonicalName());
     private Configuration configuration;
 
     public ImageResizer(Configuration configuration) {
         this.configuration = configuration;
     }
 
     public void resizeImages(List<ProjectModel> listOfModels) throws IOException {
         File outputDirectory = new File(configuration.getOutputDirectory());
 
         if (!outputDirectory.exists()) {
             throw new FileNotFoundException("Output directory does not exist!");
         }
 
         for (int i = 0; i < listOfModels.size(); ++i) {
            File imgDirectory = new File(outputDirectory, configuration.getOutputDirectoryPrefix() + Integer.toString(i));
             imgDirectory.mkdir();
 
             for (String imgMain : listOfModels.get(i).getImgMain()) {
                 File src = new File(String.format(configuration.getImgMainUriTemplateContant(), imgMain));
                 if (src.exists()) {
                     File dst = new File(imgDirectory, imgMain);
                     resizeAndSave(src.toURI(), dst, configuration.getOutputImgWidth(), configuration.getOutputImgHeight(), configuration.getOutputImgCompressionRatio());
                 } else {
                     logger.log(Level.WARNING, "Image {0} does not exist", src);
                 }
             }
 
             for (String imgProjection : listOfModels.get(i).getImgProjection()) {
                 File src = new File(String.format(configuration.getImgMainUriTemplateContant(), imgProjection));
                 if (src.exists()) {
                     File dst = new File(imgDirectory, imgProjection);
                     resizeAndSave(src.toURI(), dst, configuration.getOutputImgWidth(), configuration.getOutputImgHeight(), configuration.getOutputImgCompressionRatio());
                 } else {
                     logger.log(Level.WARNING, "Image {0} does not exist", src);
                 }
             }
 
             for (String imgLocation : listOfModels.get(i).getImgLocation()) {
                 File src = new File(String.format(configuration.getImgMainUriTemplateContant(), imgLocation));
                 if (src.exists()) {
                     File dst = new File(imgDirectory, imgLocation);
                     resizeAndSave(src.toURI(), dst, configuration.getOutputImgWidth(), configuration.getOutputImgHeight(), configuration.getOutputImgCompressionRatio());
                 } else {
                     logger.log(Level.WARNING, "Image {0} does not exist", src);
                 }
             }
 
             for (String imgElevation : listOfModels.get(i).getImgElevation()) {
                 File src = new File(String.format(configuration.getImgMainUriTemplateContant(), imgElevation));
                 if (src.exists()) {
                     File dst = new File(imgDirectory, imgElevation);
                     resizeAndSave(src.toURI(), dst, configuration.getOutputImgWidth(), configuration.getOutputImgHeight(), configuration.getOutputImgCompressionRatio());
                 } else {
                     logger.log(Level.WARNING, "Image {0} does not exist", src);
                 }
             }
         }
     }
 
     public void resizeAndSave(URI sourceUri, File destinationFile, int scaledWidth, int scaledHeight, float conpressionRatio) throws IOException {
         logger.log(Level.INFO, "Resizing image from URL {0} and saving to {1}. Resizing to {2} x {3}. Compression ratio: {4}", new Object[]{sourceUri.toURL(), destinationFile, scaledWidth, scaledHeight, conpressionRatio});
 
         BufferedImage sourceImg = ImageIO.read(sourceUri.toURL());
         BufferedImage destImg = createResizedCopy(sourceImg, scaledWidth, scaledHeight, conpressionRatio, true);
 
         ImageOutputStream imgOutStrm = ImageIO.createImageOutputStream(destinationFile);
 
         ImageWriter imgWrtr = ImageIO.getImageWritersByFormatName("jpg").next();
         imgWrtr.setOutput(imgOutStrm);
         ImageWriteParam jpgWrtPrm = imgWrtr.getDefaultWriteParam();
         jpgWrtPrm.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
         jpgWrtPrm.setCompressionQuality(conpressionRatio);
         
         imgWrtr.write(null, new IIOImage(destImg, null, null), jpgWrtPrm);
     }
 
     public BufferedImage createResizedCopy(Image originalImage,
             int scaledWidth, int scaledHeight, float compressionRatio,
             boolean preserveAlpha) {
         logger.log(Level.INFO, "Resizing image to {0} x {1}", new Object[]{scaledWidth, scaledHeight});
         int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
         BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
         Graphics2D g = scaledBI.createGraphics();
         if (preserveAlpha) {
             g.setComposite(AlphaComposite.Src);
         }
         
         g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
         g.dispose();
         return scaledBI;
     }
 }
