 package com.percussion.pso.transform;
 
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.percussion.extension.IPSItemInputTransformer;
 import com.percussion.extension.IPSRequestPreProcessor;
 import com.percussion.extension.PSExtensionProcessingException;
 import com.percussion.extension.PSParameterMismatchException;
 import com.percussion.extensions.general.PSFileInfo;
 import com.percussion.security.PSAuthorizationException;
 import com.percussion.server.IPSRequestContext;
 import com.percussion.server.PSRequestValidationException;
 import com.percussion.util.PSPurgableTempFile;
 
 
 /**
  * generates a fixed size thumbnail from an uploaded image. 
  * 
  * <p>Title: PSOThumbnailGenerator </p>
  * <p>Description: A Pre-exit that can be used on an Image Content editor to automatically
  * generate a thumbnail for the full image.
  * </p>
  * Modernized to use the ImageIO libraries. The new libraries use biCubic interpolation for 
  * higher image quality.
  * <p>
  * This version is enhanced to handle both PSPurgableTempFile instances and Base64 encoded
  * strings.  
  * <p>Copyright: Copyright (c) 2002, 2008</p>
  * <p>Company: Percussion Software </p>
  * @author Prasad Bandaru
  * @author DavidBenua
  * @version 2.0
  */
 
 public class PSOThumbnailGenerator extends PSFileInfo
    implements IPSItemInputTransformer, IPSRequestPreProcessor {
  
    private static Log log = LogFactory.getLog(PSOThumbnailGenerator.class); 
    
    public PSOThumbnailGenerator() {
    }
 
  
    /**
     * Pre processes the current request to generate the thumbnail automatically
     * The exit should be registered with 4 parameters. <p>
     * 1. SourceFieldName - Field name of the source image <BR>
     * 2. ThumbFieldName - Field name of the thumbnail image <BR>
     * 3. MaxDimension - Value of the Max Dimension.
     * 4. ThumbPrefix - Prefix added to the full size image name so that the filename
     * of the generated thumbnail is PREFIFX + FullSizeImageName
     * @param params - parameters
     * @param request - IPSRequestContext
     * @throws PSAuthorizationException
     * @throws PSRequestValidationException
     * @throws PSParameterMismatchException
     * @throws PSExtensionProcessingException
     */
    public void preProcessRequest(Object[] params, IPSRequestContext request)
       throws PSAuthorizationException, PSRequestValidationException,
              PSParameterMismatchException, PSExtensionProcessingException
    {
       if (params == null || params.length < 4)
           throw new PSParameterMismatchException("Required Parameters are missing");
 
       String emsg = ""; 
       String sourceFieldName = params[0].toString();
       if(StringUtils.isBlank(sourceFieldName))
       {
          emsg = "Source Field is required"; 
          log.error(emsg); 
          throw new IllegalArgumentException(emsg); 
       }
       String thumbFieldName = params[1].toString();
       if(StringUtils.isBlank(thumbFieldName))
       {
          emsg = "Thumbnail Field is required"; 
          log.error(emsg); 
          throw new IllegalArgumentException(emsg);
       }
       int maxDimension = Integer.parseInt(params[2].toString());
       
       String thumb_prefix = params[3].toString();
       if(StringUtils.isBlank(thumb_prefix))
       {
          emsg = "Thumbnail prefix is required"; 
          log.error(emsg); 
          throw new IllegalArgumentException(emsg);
       }
 
       try
       {
          Object obj = request.getParameterObject(sourceFieldName);
          if(obj != null && obj instanceof PSPurgableTempFile){
             PSPurgableTempFile temp = (PSPurgableTempFile) obj;
             String parent_mimetype =  temp.getSourceContentType();
             String source_filename =  temp.getSourceFileName();
             temp.deleteOnExit();
             InputStream imageStream = new FileInputStream(temp); 
             
             PSPurgableTempFile thumbFile = makeThumbFile(imageStream, source_filename, thumb_prefix, 
                   parent_mimetype, maxDimension);            
 
             if(thumbFile != null)
             {
                   request.setParameter(thumbFieldName, thumbFile);
             }
          }
          else{
             if (obj!=null && obj instanceof String)
             {
                String imageStr = (String) obj; 
                byte[] imageBytes = imageStr.getBytes();  
                String parent_mimetype = request.getParameter(sourceFieldName + "_type"); 
                String source_filename = request.getParameter(sourceFieldName + "_filename"); 
                if(imageBytes.length >0 && Base64.isArrayByteBase64(imageBytes) &&
                      StringUtils.isNotBlank(parent_mimetype) && StringUtils.isNotBlank(source_filename))
                {  
                   InputStream imageStream = new ByteArrayInputStream(Base64.decodeBase64(imageBytes));
                   PSPurgableTempFile thumbFile = makeThumbFile(imageStream, source_filename, thumb_prefix, 
                         parent_mimetype, maxDimension);            
 
                   if(thumbFile != null)
                   {
                         request.setParameter(thumbFieldName, thumbFile);
                         request.setParameter(thumbFieldName + "_type", thumbFile.getSourceContentType()); 
                         request.setParameter(thumbFieldName + "_filename", StringUtils.substringBeforeLast(thumbFile.getName(),".")); 
                         request.setParameter(thumbFieldName + "_ext", ".JPG");
                   }                  
                   else
                   {
                      emsg = "error processing thumbnail for " + source_filename;
                      log.error(emsg); 
                   }
                }
                else
                {
                   log.info("Unable to process thumbnail from base64 encoded String"); 
                   log.info("image file name is " + source_filename); 
                   log.info("mimetype is " + parent_mimetype);
                   log.info("data starts with " + StringUtils.substring(imageStr,0, 50)); 
                }
             }
          }
       } catch (Exception ex)
       {
          log.error("Unexpected Exception processing thumbnail " + ex,ex);
       }
       super.preProcessRequest(params, request);
    }
 
    public PSPurgableTempFile makeThumbFile(InputStream imageStream, String source_filename, String thumb_prefix, String parent_mimetype, int maxDimension )
    {
       log.debug("processing file " + source_filename);
       String fullImageFileName = StringUtils.contains(source_filename, File.separator) ? 
             StringUtils.substringAfterLast(source_filename, File.separator) : source_filename;
             
       String thumb_filename = thumb_prefix + fullImageFileName;
       if (parent_mimetype.indexOf("image") >= 0)
       {
          try{
             //String thumb_ContentType = temp.getSourceContentType();
             PSPurgableTempFile thumb_temp = new PSPurgableTempFile(thumb_filename,
                                                "", null, null,
                                                 "image/jpeg", null);
             thumb_temp.deleteOnExit();
             OutputStream thumbStream = new FileOutputStream(thumb_temp);
             
             createThumbnail(thumbStream, imageStream, maxDimension);
             logMessage("Size of the thumbnail created = " +
                        thumb_temp.length(), null);
            return thumb_temp; 
          }
          catch(IOException ioe){
             log.error("unable to create thumbnail " + thumb_filename, ioe); 
          }
       }
       else{
          log.info("The uploaded file is not an image, type = " +
             parent_mimetype);
       }
       return null;
    }
    /**
     * Reads an image in a file and creates a thumbnail in another file. 
     * Modified slightly to use ImageIO instead of Sun JPEG CODEC.  
     * @param outstream
     * @param instream
     * @param maxDim The width and height of the thumbnail must be maxDim pixels or less.
     */
    public void createThumbnail(OutputStream outstream, InputStream instream,  int maxDim) throws IOException
    {
       try {
          // Get the image from a file.
          BufferedImage inImage = ImageIO.read(instream);
          // Determine the scale.
          double scale = (double)maxDim/(double)inImage.getHeight(null);
          if (inImage.getWidth() > inImage.getHeight())
          {
             scale = (double)maxDim/(double)inImage.getWidth(null);
          }
          // Determine size of new image.
          // One of them should equal maxDim.
          int scaledW = (int)(scale*inImage.getWidth(null));
          int scaledH = (int)(scale*inImage.getHeight(null));
 
          // Create an image buffer in which to paint on.
          BufferedImage outImage = new BufferedImage(scaledW, scaledH,
                                                     BufferedImage.TYPE_INT_RGB);
          
          // Paint image.
          Graphics2D g2d = outImage.createGraphics();
          g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
          g2d.drawImage(inImage, 0, 0, scaledW, scaledH, null);
          //g2d.dispose();
 
          // JPEG-encode the image and write to output
          ImageIO.write(outImage, "jpeg", outstream); 
          
       }
       catch (IOException e) {
          log.error("Could not create thumbnail " + e.getMessage(), e);
       }
    }
 
 
    private void logMessage(String msg, IPSRequestContext req){
       log.info(msg);
       if(req != null)
       {
          req.printTraceMessage(msg);
       }
    }
 }
