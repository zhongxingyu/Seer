 package org.apache.maven.plugins;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.GraphicsEnvironment;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
import java.awt.image.Raster;
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 
 public abstract class AbstractImage {
 
     int height;
     int width;
     String type;
     String filename;
     File outputDirectory;
     Log log;
     Map<String, String> copyright;
     Map<String, String>[] textFields;
     Map<String, String>[] imageOverlays;
     boolean insertVersion;
     boolean trimSnapshot;
     Map<String, String> versionDetails;
 
     public abstract BufferedImage createImage() throws MojoExecutionException;
 
     public String imageType() {
 
         final String classname = this.getClass().getName();
         return classname.substring(classname.lastIndexOf(".") + 1);
     }
 
     public void addCopyText(final Graphics2D g2d) throws MojoExecutionException {
         log.info("Adding the copyright text...");
         // Eventually, this should inject a year in front of the company name.
         // We should also be testing to see if the font selected by the user
         // exists.
         // Any and all fonts should be given the option in the configuration to
         // be bold or italic.
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setPaint(Color.black);
         final Font f = new Font(copyright.get("font"), Font.PLAIN, Integer.parseInt(copyright
                 .get("fontsize")));
         if (doesFontExist(f.getName())) {
             g2d.setFont(f);
         } else {
             throw new MojoExecutionException("Invalid font name! " + f.getName());
         }
         if (Boolean.valueOf(copyright.get("shadow"))) {
             addShadow(g2d, copyright.get("text"), Integer.parseInt(copyright.get("x")),
                     Integer.parseInt(copyright.get("y")));
         }
         if (copyright.get("color") != null) {
             final String[] rgbVals = copyright.get("color").split(",");
             final Color color = new Color(Integer.parseInt(rgbVals[0]),
                     Integer.parseInt(rgbVals[1]), Integer.parseInt(rgbVals[2]));
             g2d.setColor(color);
         } else {
             g2d.setColor(Color.black);
         }
         g2d.drawString(copyright.get("text"), Integer.parseInt(copyright.get("x")),
                 Integer.parseInt(copyright.get("y")));
     }
 
     public void addImages(final Graphics2D g2d) throws MojoExecutionException {
         log.info("Adding the overlay images...");
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
         for (int x = 0; x < imageOverlays.length; x++) {
 
             try {
                 BufferedImage img = ImageIO.read(new File(imageOverlays[x].get("imagePath")
                         .toString()));

                AffineTransform at = new AffineTransform();
                 final int xcoord = Integer.parseInt(imageOverlays[x].get("x"));
                 final int ycoord = Integer.parseInt(imageOverlays[x].get("y"));
                 int scale = 0;
 
                 int imageHeight = img.getHeight();
                 int imageWidth = img.getWidth();
                 if (imageOverlays[x].get("scaling") != null) {
                     scale = Integer.parseInt(imageOverlays[x].get("scaling"));
                     if (scale > 1) {
                         imageHeight = img.getHeight() / scale;
                         imageWidth = img.getWidth() / scale;
                     } else {
                         throw new MojoExecutionException("Sorry - we only accept POSITIVE scaling integers.  You entered " +
                                 scale);
                     }
 
                 }
                 if (imageOverlays[x].get("rotation") != null)
                 {
                     int rotate = Integer.parseInt(imageOverlays[x].get("rotation"));
                     log.info("rotating the image..." + rotate);
                    at.translate(0.5 * img.getHeight(), 0.5 * img.getWidth());
                    at.rotate(rotate);
                    at.translate(-0.5 * img.getHeight(), -0.5 * img.getWidth());
                    AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                     img = op.filter(img, null);
                 }
                 if (Boolean.valueOf(imageOverlays[x].get("shadow"))) {
                     g2d.setColor(new Color(0, 0, 0, 100));
                     g2d.fillRect(xcoord + 2, ycoord + 2, imageWidth, imageHeight);
                 }
                 g2d.drawImage(img, xcoord, ycoord, imageWidth, imageHeight, null);
 
             } catch (final IOException e) {
                 throw new MojoExecutionException("Could not add the image(s) you've specified: "
                         + '\n' + '\t' + "\"" + imageOverlays[x].get("imagePath") + "\" " + '\n'
                         + '\t' + e.getMessage());
             }
         }
     }
 
     public boolean doesFontExist(final String fontname) {
         /*
          * The standard font that the graphic designer used is an OpenType Font.
          * 
          * Apparently java doesn't play well with those (well, at least 1.5
          * versions).
          */
         final Font[] fontlist = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
         for (int x = 0; x < fontlist.length; x++) {
             if (fontlist[x].getName().equalsIgnoreCase(fontname)) {
                 return true;
             }
         }
         log.error("The font specified " + fontname
                 + " does not exist on this system, please select one of the following:");
         for (int x = 0; x < fontlist.length; x++) {
             log.error(fontlist[x].getName());
         }
         return false;
 
     }
 
     public void addText(final Graphics2D g2d) throws MojoExecutionException {
         log.info("Adding the additional text...");
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
         for (int x = 0; x < textFields.length; x++) {
             final int fontsize = Integer.parseInt(textFields[x].get("fontsize"));
             final Font f = new Font(textFields[x].get("font"), Font.PLAIN, fontsize);
             if (doesFontExist(f.getName())) {
                 g2d.setFont(f);
             } else {
                 throw new MojoExecutionException("Invalid font name! " + f.getName());
             }
 
             if (Boolean.valueOf(textFields[x].get("shadow"))) {
                 addShadow(g2d, textFields[x].get("text"), Integer.parseInt(textFields[x].get("x")),
                         Integer.parseInt(textFields[x].get("y")));
             }
 
             if (textFields[x].get("color") != null) {
                 final String[] rgbVals = textFields[x].get("color").split(",");
                 final Color color = new Color(Integer.parseInt(rgbVals[0]),
                         Integer.parseInt(rgbVals[1]), Integer.parseInt(rgbVals[2]));
                 g2d.setColor(color);
             } else {
                 g2d.setColor(Color.black);
             }
             g2d.drawString(textFields[x].get("text"), Integer.parseInt(textFields[x].get("x")),
                     Integer.parseInt(textFields[x].get("y")));
         }
     }
 
     public void addShadow(final Graphics2D g2d, final String text, final int x, final int y) {
         // we should probably allow alpha channel config everywhere we're
         // setting the color.
         g2d.setPaint(new Color(0, 0, 0, 100));
         g2d.drawString(text, (x + 2), (y + 2));
     }
 
     public void writeImage(final BufferedImage rendImage, final String imageType)
             throws IOException {
         final File file = new File(outputDirectory.getAbsolutePath() + File.separator + filename
                 + "." + imageType);
         ImageIO.write(rendImage, imageType, file);
         log.info("You can find the " + this.imageType() + " " + imageType + " version here "
                 + file.getAbsolutePath());
     }
 
     public void execute(BufferedImage rendImage) throws MojoExecutionException {
         // Create an image to save
         rendImage = this.createImage();
         // Write generated image to a file
         try {
             if (!type.equalsIgnoreCase("png") && !type.equalsIgnoreCase("jpg")
                     && !type.equalsIgnoreCase("both")) {
                 throw new MojoExecutionException(
                         "The Image type provided is not a supported type: " + type);
             }
             // Save as PNG
             if (type.equalsIgnoreCase("png") || type.equalsIgnoreCase("both")) {
                 writeImage(rendImage, "png");
             }
             // Save as JPEG
             if (type.equalsIgnoreCase("jpg") || type.equalsIgnoreCase("both")) {
                 writeImage(rendImage, "jpg");
             }
         } catch (final IOException e) {
             throw new MojoExecutionException("Error generating the file " + e.getMessage());
         }
     }
 }
