 /**
  *
  */
 package de.goldstift.photoo.util.images;
 
 import java.awt.Dimension;
 import java.awt.Image;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
 import org.springframework.integration.MessageRejectedException;
 import org.springframework.integration.support.MessageBuilder;
 import org.springframework.integration.transformer.MessageTransformationException;
 import org.springframework.integration.transformer.Transformer;
 
 /**
  * ImageScalingTransformer
  *
  * @author ahe
  * @since 18.10.2011 23:00:03
  */
 public class ImageScalingTransformer implements Transformer
 {
 
     private String defaultImageType = "jpg";
     private String previewImageDirectory;
     private String previewFilenameSuffix = "_preview";
     private String thumbnailImageDirectory;
     private String thumbnailFilenameSuffix = "_thumb";
     private Dimension defaultDimension = new Dimension(1024, 768);
 
     /*
      * (non-Javadoc)
      * @see
      * org.springframework.integration.transformer.Transformer#transform(org.springframework.integration
      * .Message)
      */
     @Override
     public Message<?> transform(Message<?> message)
     {
         String imageTargetFolder = (String)message.getHeaders().get("imageTargetFolder");
         String imageTargetFileSuffix = (String)message.getHeaders().get("imageTargetFileSuffix");
         Integer imageWidth = (Integer)message.getHeaders().get("imageTargetWidth");
         Integer imageHeight = (Integer)message.getHeaders().get("imageTargetHeight");
         if (imageWidth == null && imageHeight != null)
         {
             imageWidth = calculateProportion(defaultDimension.height, defaultDimension.width, imageHeight);
         } else if (imageWidth != null && imageHeight == null)
         {
             imageHeight = calculateProportion(defaultDimension.width, defaultDimension.height, imageWidth);
         } else if (imageWidth == null && imageHeight == null){
             imageWidth = defaultDimension.width;
             imageHeight = defaultDimension.height;
         }
 
         Object payload = message.getPayload();
         if (!(payload instanceof File))
         {
             throw new MessageRejectedException(message, "Message payload is not java.util.File");
         }
 
         File sourceFile = (File) payload;
         String imageTargetFileName = getTargetFileName(sourceFile, imageTargetFolder, imageTargetFileSuffix);
         try
         {
             File previewFile = scaleImageFile(sourceFile, new Dimension(imageWidth, imageHeight), imageTargetFileName);
             return MessageBuilder.withPayload(previewFile).copyHeadersIfAbsent(message.getHeaders()).build();
         }
         catch (FileNotFoundException e)
         {
             throw new MessageTransformationException(message, "Could not write to target file!", e);
         }
         catch (IOException e)
         {
             throw new MessageTransformationException(message, "An unexpected error reading the source image occurred!", e);
         }
     }
 
     /**
      * @param sourceFile - the source file to be used as draft
      * @param imageTargetFolder - the target folder
      * @param imageTargetFileSuffix - the suffix to add to the target file name
      * @return the newly created target filename
      */
     public String getTargetFileName(File sourceFile, String imageTargetFolder,
             String imageTargetFileSuffix)
     {
         // TODO
         return null;
     }
 
     public int calculateProportion(int sourceX, int sourceY, int targetX)
     {
         return Math.round((float)targetX/(float)sourceX*sourceY);
     }
 
     /**
      * @param sourceFile - the file containing the image
      * @param tDim - the dimension the image shall be scaled to
      * @param imageTargetFileName absolute path of the target file
      * @return the resulting image file
      * @throws IOException
      * @throws FileNotFoundException
      */
     public File scaleImageFile(final File sourceFile, final Dimension tDim, final String imageTargetFileName) throws IOException,
             FileNotFoundException
     {
        File targetFile = new File(imageTargetFileName);
        Image newImg = javax.imageio.ImageIO.read(sourceFile).getScaledInstance(tDim.width,
                 tDim.height, Image.SCALE_SMOOTH);
        java.awt.image.BufferedImage bim = new java.awt.image.BufferedImage(tDim.width,
                tDim.height, java.awt.image.BufferedImage.TYPE_INT_RGB);
         bim.createGraphics().drawImage(newImg, 0, 0, null);
        FileOutputStream fos = new FileOutputStream(targetFile, false);
        javax.imageio.ImageIO.write(bim, defaultImageType, fos);
         fos.close();
         return targetFile;
     }
 
 }
