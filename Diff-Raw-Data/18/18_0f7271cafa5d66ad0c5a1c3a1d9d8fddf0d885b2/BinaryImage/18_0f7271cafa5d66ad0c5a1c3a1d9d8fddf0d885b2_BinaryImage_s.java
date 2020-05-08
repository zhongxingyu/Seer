 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.common;
 
 import java.awt.image.RenderedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.apache.log4j.Logger;
 
 import sun.awt.image.ToolkitImage;
 
 /**
  * This class represents an image.
  * <p>
  * Provided property: icon.
  * <p>
  * See {@link cz.cuni.mff.peckam.java.origamist.common.jaxb.BinaryImage} for other provided properties.
  * 
  * @author Martin Pecka
  */
 public class BinaryImage extends cz.cuni.mff.peckam.java.origamist.common.jaxb.BinaryImage
 {
     /** The image this class holds. */
     protected transient ImageIcon icon          = null;
 
     /** The icon property. */
     public static final String    ICON_PROPERTY = "icon:cz.cuni.mff.peckam.java.origamist.common.BinaryImage";
 
     /** The default type of images. */
     public static String          DEFAULT_TYPE  = "jpg";
 
     /**
      * Return the content as an ImageIcon.
      * 
      * @return the content as an ImageIcon.
      */
     @XmlTransient
     public ImageIcon getImageIcon()
     {
        if (icon == null)
             icon = new ImageIcon(value);
         return icon;
     }
 
     public boolean isImageNonEmpty()
     {
         return value != null && value.length > 0 && getImageIcon().getIconWidth() > 0;
     }
 
     @Override
     public void setValue(byte[] value)
     {
         setValue(value, true);
     }
 
     protected void setValue(byte[] value, boolean setIcon)
     {
         super.setValue(value);
         if (setIcon) {
             icon = null;
             setImageIcon(getImageIcon(), false);
         }
     }
 
     /**
      * Set the image from the given ImageIcon
      * 
      * @param icon The image to set
      */
     public void setImageIcon(ImageIcon icon)
     {
         setImageIcon(icon, true);
     }
 
     /**
      * Set the image from the given ImageIcon
      * 
      * @param icon The image to set
      * @param setValue If true, set also value to the bytes corresponding to this image.
      */
     public void setImageIcon(ImageIcon icon, boolean setValue)
     {
         if (icon == null) {
             if (setValue)
                 setValue(new byte[0], false);
             ImageIcon oldIcon = this.icon;
             this.icon = null;
             support.firePropertyChange(ICON_PROPERTY, oldIcon, null);
             return;
         }
 
         RenderedImage image = null;
         if (icon.getImage() instanceof RenderedImage) {
             image = (RenderedImage) icon.getImage();
         } else if (icon.getImage() instanceof ToolkitImage) {
             image = ((ToolkitImage) icon.getImage()).getBufferedImage();
         }
 
         if (image == null) {
             if (setValue)
                 setValue(new byte[0], false);
             ImageIcon oldIcon = this.icon;
             this.icon = null;
             support.firePropertyChange(ICON_PROPERTY, oldIcon, null);
             return;
         }
 
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         try {
             ImageIO.write(image, type != null ? type.replaceAll("[^/]*/", "") : DEFAULT_TYPE, os);
             if (setValue)
                 setValue(os.toByteArray(), false);
             ImageIcon oldIcon = this.icon;
             this.icon = icon;
             support.firePropertyChange(ICON_PROPERTY, oldIcon, icon);
         } catch (IOException e) {
             Logger.getLogger("application").warn("IO error while setting a thumbnail of origami.", e);
         } finally {
             try {
                 os.close();
             } catch (IOException e) {}
         }
     }
 }
