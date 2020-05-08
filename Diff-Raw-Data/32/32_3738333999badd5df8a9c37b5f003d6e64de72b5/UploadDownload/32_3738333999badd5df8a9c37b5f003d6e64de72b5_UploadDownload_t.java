 
 package net.r0kit.brijj.demo;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.r0kit.brijj.FileTransfer;
 import net.r0kit.brijj.RemoteRequestProxy;
 
 public class UploadDownload extends RemoteRequestProxy {
     /**
      * Take 2 uploaded files and return an image based on them
      * @param uploadImage The uploaded image
      * @param uploadFile The uploaded file
      * @param color The selected color
      * @return A mangled image based on the 2 uploaded files
      */
   public UploadDownload(HttpServletRequest r, HttpServletResponse s) { super(r,s); }
   
     public FileTransfer uploadFiles(BufferedImage uploadImage, String uploadFile, String color)
     {
         uploadImage = scaleToSize(uploadImage);
         uploadImage = grafitiTextOnImage(uploadImage, uploadFile, color);
 
         return new FileTransfer(uploadImage, "png", request);
     }
 
    public void logError(String mth, Exception ex) {
      System.out.println(mth+": "+ex);
     }
     
     public FileTransfer downloadPdfFile(String contents) {
         if (contents == null || contents.length() == 0) contents = "[BLANK]";
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 /*        Document document = new Document();
         PdfWriter.getInstance(document, buffer);
         document.open();
         document.add(new Paragraph(contents));
         document.close();
         */
         return new FileTransfer("example.pdf", "application/pdf", buffer.toByteArray(), request);
     }
 
     public BufferedImage scaleToSize(BufferedImage uploadImage) {
         AffineTransform atx = new AffineTransform();
         atx.scale(200d / uploadImage.getWidth(), 200d / uploadImage.getHeight());
         // AffineTransformOp.TYPE_BILINEAR is very slow
         AffineTransformOp afop = new AffineTransformOp(atx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
         uploadImage = afop.filter(uploadImage, null);
         return uploadImage;
     }
 
     /**
      * And scrawl the text on the image in 10 rows of 21 chars
      * @param uploadImage The image to work on
      * @param uploadFile The text to write on the image
      * @param color The selected color
      * @return The altered image
      */
     private BufferedImage grafitiTextOnImage(BufferedImage uploadImage, String uploadFile, String color)
     {
         StringBuilder buffer = new StringBuilder();
         while (buffer.length() < 200)
         {
             buffer.append(" ");
             buffer.append(uploadFile);
         }
 
         Graphics2D g2d = uploadImage.createGraphics();
         for (int row = 0; row < 10; row++)
         {
             String output = null;
             if (buffer.length() > (row + 1) * CHARS_PER_LINE)
             {
                 output = buffer.substring(row * CHARS_PER_LINE, (row + 1) * CHARS_PER_LINE);
             }
             else
             {
                 output = buffer.substring(row * CHARS_PER_LINE);
             }
 
             g2d.setFont(new Font("Serif", Font.BOLD, 16));
             g2d.setColor(UploadDownload.decodeHtmlColorString(color));
             g2d.drawString(output, 5, (row + 1) * CHARS_PER_LINE);
         }
 
         return uploadImage;
     }
 
     public static Color decodeHtmlColorString(String colorString) {
         if (colorString.startsWith("#")) colorString = colorString.substring(1);
         if (colorString.endsWith(";")) colorString = colorString.substring(0, colorString.length() - 1);
         switch (colorString.length()) {
         case 6:
             return new Color(
                 Integer.parseInt(colorString.substring(0, 2), 16),
                 Integer.parseInt(colorString.substring(2, 4), 16),
                 Integer.parseInt(colorString.substring(4, 6), 16));
         case 3:
           return new Color(
               Integer.parseInt(colorString.substring(0, 1), 16),
               Integer.parseInt(colorString.substring(1, 2), 16),
               Integer.parseInt(colorString.substring(2, 3), 16));
         case 1:
             int g = Integer.parseInt(colorString.substring(0, 1), 16);
             return new Color(g,g,g);
         default:
             throw new IllegalArgumentException("Invalid color: " + colorString);
         }
     }
 
     private static final int CHARS_PER_LINE = 21;
 }
