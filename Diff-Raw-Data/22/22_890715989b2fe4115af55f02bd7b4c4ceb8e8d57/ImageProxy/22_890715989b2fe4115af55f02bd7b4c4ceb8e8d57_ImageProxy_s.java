 package lge.res.images;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.Externalizable;
 import java.io.File;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.lang.ref.SoftReference;
 import java.util.Map;
 import java.util.Properties;
 
 import lge.res.ResourceCache;
 import lge.util.FileUtils;
 import lge.util.Hash;
 import lge.util.PathFinder;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ImageProxy implements Externalizable {
   public static final long serialVersionUID = 1L;
   private static final Logger LOGGER = LoggerFactory.getLogger(ImageProxy.class);
   
   public static enum ProxyProps {
     ALIGN_X, // LEFT, CENTER, RIGHT
     ALIGN_Y, // TOP, CENTER, BOTTOM
     POS_X, // double; in percent; left to right
     POS_Y, // double; in percent; top to bottom
     SCALE_X, // double; in percent of available x resolution
     SCALE_Y, // double; in percent of available y resolution
     ROTATION, // double; 0 - 360 degrees of clockwise rotation
     ATTRIBUTE; // String; additional attribute
   }
   
   private ImageKey key;
   private File imageFile;
   private SoftReference<BufferedImage> image;
   private SoftReference<BufferedImage> nullImage;
   private int imageSizeX;
   private int imageSizeY;
   private File initProxyFile;
   private File modProxyFile;
   
   private final Properties initPlacement = new Properties();
   private Properties placement = new Properties();
   
   private String alignX = null;
   private String alignY = null;
   private Double posX = null;
   private Double posY = null;
   private Double scaleX = null;
   private Double scaleY = null;
   private Double rotation = null;
   
   private String attribute = null;
   
   /**
    * for load/save
    */
   public ImageProxy() {
   }
   
   @Override
   public void writeExternal(final ObjectOutput out) throws IOException {
     ImageKey.IO.write(this.key, out);
     out.writeObject(this.imageFile);
     // do not save soft references
     out.writeInt(this.imageSizeX);
     out.writeInt(this.imageSizeY);
     out.writeObject(this.initProxyFile);
     out.writeObject(this.modProxyFile);
     out.writeInt(this.initPlacement.size());
     for (final Map.Entry<Object, Object> entry : this.initPlacement.entrySet()) {
       out.writeObject(entry.getKey());
       out.writeObject(entry.getValue());
     }
     out.writeInt(this.placement.size());
     for (final Map.Entry<Object, Object> entry : this.placement.entrySet()) {
       out.writeObject(entry.getKey());
       out.writeObject(entry.getValue());
     }
     out.writeObject(this.alignX);
     out.writeObject(this.alignY);
     out.writeObject(this.posX);
     out.writeObject(this.posY);
     out.writeObject(this.scaleX);
     out.writeObject(this.scaleY);
     out.writeObject(this.rotation);
     out.writeObject(this.attribute);
   }
   
   @Override
   public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
     this.key = ImageKey.IO.read(in);
     this.imageFile = (File) in.readObject();
     this.image = new SoftReference<>(null);
     this.nullImage = new SoftReference<>(null);
     this.imageSizeX = in.readInt();
     this.imageSizeY = in.readInt();
     this.initProxyFile = (File) in.readObject();
     this.modProxyFile = (File) in.readObject();
     final int initPlacementSize = in.readInt();
     for (int i = 0; i < initPlacementSize; i++) {
       this.initPlacement.put(in.readObject(), in.readObject());
     }
     final int placementSize = in.readInt();
     for (int i = 0; i < placementSize; i++) {
       this.placement.put(in.readObject(), in.readObject());
     }
     this.alignX = (String) in.readObject();
     this.alignY = (String) in.readObject();
     this.posX = (Double) in.readObject();
     this.posY = (Double) in.readObject();
     this.scaleX = (Double) in.readObject();
     this.scaleY = (Double) in.readObject();
     this.rotation = (Double) in.readObject();
     this.attribute = (String) in.readObject();
   }
   
   private ImageProxy(final File imageProxyFile, final File imageFile, final BufferedImage image) {
     this.key = null;
     setPlacement(imageProxyFile);
     this.initProxyFile = imageProxyFile;
     FileUtils.readPropsFromFile(this.initPlacement, this.initProxyFile);
     this.modProxyFile = PathFinder.toModFile(imageProxyFile);
     
     this.imageFile = imageFile;
     this.image = new SoftReference<>(image);
     this.imageSizeX = image.getWidth();
     this.imageSizeY = image.getHeight();
   }
   
   ImageProxy(final ImageKey key) {
     this.key = key;
     this.nullImage = new SoftReference<>(makeNullImage());
     this.imageFile = null;
     this.initProxyFile = null;
     this.modProxyFile = null;
     this.imageSizeX = 800;
     this.imageSizeY = 600;
   }
   
   private BufferedImage makeNullImage() {
     final BufferedImage nI = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
     final Graphics graphics = nI.getGraphics();
     graphics.setColor(Color.WHITE);
     graphics.fillRect(0, 0, nI.getWidth(), nI.getHeight());
     graphics.setColor(Color.BLACK);
     if (this.key != null) {
       graphics.drawString(this.key.getClass().getSimpleName() + "." + this.key.name(), 20, 20);
     } else {
       graphics.drawString("Image missing!", 20, 20);
     }
     graphics.drawString("Press Ctrl-i to add image", 20, 40);
     return nI;
   }
   
   private void setPlacement(final File imageProxyFile) {
     this.placement.clear();
     FileUtils.readPropsFromFile(this.placement, imageProxyFile);
     _setPlacement(this.placement);
   }
   
   private void _setPlacement(final Properties placementProps) {
     this.alignX = null;
     this.alignY = null;
     this.posX = null;
     this.posY = null;
     this.scaleX = null;
     this.scaleY = null;
     this.rotation = null;
     
     this.alignX = placementProps.getProperty(ProxyProps.ALIGN_X.name(), "CENTER");
     this.alignY = placementProps.getProperty(ProxyProps.ALIGN_Y.name(), "CENTER");
     final String posXString = placementProps.getProperty(ProxyProps.POS_X.name());
     final String posYString = placementProps.getProperty(ProxyProps.POS_Y.name());
     final String sizeXString = placementProps.getProperty(ProxyProps.SCALE_X.name());
     final String sizeYString = placementProps.getProperty(ProxyProps.SCALE_Y.name());
     final String rotationString = placementProps.getProperty(ProxyProps.ROTATION.name());
     
     if (posXString != null) {
       this.posX = Double.valueOf(posXString);
       if (this.alignX.equals("CENTER")) {
         this.alignX = "LEFT";
       }
     }
     
     if (posYString != null) {
       this.posY = Double.valueOf(posYString);
       if (this.alignY.equals("CENTER")) {
         this.alignY = "TOP";
       }
     }
     
     if (sizeXString != null) {
       this.scaleX = Double.valueOf(sizeXString);
     }
     
     if (sizeYString != null) {
       this.scaleY = Double.valueOf(sizeYString);
     }
     
     if (rotationString != null) {
       this.rotation = Double.valueOf(rotationString);
     }
     
     this.attribute = placementProps.getProperty(ProxyProps.ATTRIBUTE.name());
   }
   
   public void setPlacement(final Properties placement) {
     this.placement = placement;
     _setPlacement(placement);
   }
   
   public void writeProxyFile() {
     // only write proxy file, if we have a destination file
     if (this.modProxyFile != null) {
       
       // initial file was already a mod file
       if (this.modProxyFile.equals(this.initProxyFile)) {
         // TODO read core file properties, to compare
         FileUtils.writePropsToFile(this.placement, this.modProxyFile, null);
       }
       
       // initial file was core file
       else {
         // there is currently no change from the core file, so we delete the mod file
         if (this.initPlacement.equals(this.placement)) {
           this.modProxyFile.delete();
         }
         
         // placement has changed, so we write the mod file
         else {
           FileUtils.writePropsToFile(this.placement, this.modProxyFile, null);
         }
       }
     }
   }
   
   public Properties getPlacement() {
     return this.placement;
   }
   
   static ImageProxy getInstance(final File imageProxyFile) {
     if (isMarkedAsDeleted(imageProxyFile)) {
       return null;
     }
     
     File imageFile = PathFinder.imageProxyToCoreReal(imageProxyFile);
     LOGGER.debug("core/res file location: {}", imageFile.getAbsolutePath());
     if (!imageFile.canRead()) {
       // real image is not readable in core sub-directory
       LOGGER.debug("{} not found; trying mod location.", imageFile.getAbsolutePath());
       imageFile = PathFinder.toModFile(imageFile);
       LOGGER.debug("mod/res file location: {}", imageFile.getAbsolutePath());
       if (!imageFile.canRead()) {
         // real image is not readable in mod sub-directory
         LOGGER.warn("Cannot read actual image file for proxy " + imageProxyFile.getAbsolutePath());
         return null;
       }
     }
     LOGGER.debug("Reading image file {}", imageFile.getAbsolutePath());
     final BufferedImage image = ResourceCache.getRawImage(imageFile);
     return new ImageProxy(imageProxyFile, imageFile, image);
   }
   
   public static boolean isMarkedAsDeleted(final File imageProxyFile) {
     final Properties props = new Properties();
     FileUtils.readPropsFromFile(props, imageProxyFile);
     return props.containsKey("Deleted");
   }
   
   /**
    * @param modDir
    * @param alternativeImageFile
    * @return null, if it fails to create new file
    */
   static ImageProxy createNew(final File modDir, final File alternativeImageFile) {
     if (alternativeImageFile.canRead()) {
       final String fileNameInResDir = Hash.calculateSHA1(alternativeImageFile);
       if (fileNameInResDir == null) {
         LOGGER.error("Unable to determine filename for new file");
         return null;
       }
       if (!modDir.isDirectory()) {
         if (!modDir.mkdirs()) {
           LOGGER.error("Unable to create required directory " + modDir.getAbsolutePath());
           return null;
         }
       }
       final String newFileName = fileNameInResDir + getExtension(alternativeImageFile);
       final File proxyFile = PathFinder.getProxyFile(modDir, newFileName);
       try {
         if (!proxyFile.canRead()) {
           if (!proxyFile.createNewFile()) {
             LOGGER.error("Cannot create proxy file " + proxyFile.getAbsolutePath());
             return null;
           }
         }
       } catch (final IOException e) {
         LOGGER.error("Cannot create proxy file " + proxyFile.getAbsolutePath(), e);
         return null;
       }
       
       File resFile = PathFinder.getCoreImageFile(newFileName);
       if (!resFile.exists()) {
         resFile = PathFinder.getModImageFile(newFileName);
         if (!resFile.exists()) {
           if (!FileUtils.copyFile(alternativeImageFile, resFile)) {
             LOGGER.error("Failed to copy " + alternativeImageFile.getAbsolutePath() + " to "
                 + resFile.getAbsolutePath());
             return null;
           }
         }
       }
       return getInstance(proxyFile);
     }
     return null;
   }
   
   private static String getExtension(final File fileName) {
     final String simpleName = fileName.getName();
     final int lastIndex = simpleName.lastIndexOf('.');
     if (lastIndex == -1) {
       return "";
     }
     return simpleName.substring(lastIndex);
   }
   
   protected void draw(final Graphics2D g2d, final int width, final int height) {
     final BufferedImage img = getImage();
     img.setAccelerationPriority(1.0f);
     g2d.drawImage(img, getTransformation(width, height), null);
     img.setAccelerationPriority(0.5f);
   }
   
   BufferedImage getImage() {
     if (this.imageFile == null) {
       BufferedImage bufferedImage = this.nullImage.get();
       if (bufferedImage == null) {
         bufferedImage = makeNullImage();
         this.nullImage = new SoftReference<>(bufferedImage);
       }
       return bufferedImage;
     }
     
     BufferedImage bufferedImage = this.image.get();
     if (bufferedImage == null) {
       bufferedImage = ResourceCache.getRawImage(this.imageFile);
       if (bufferedImage == null) {
         LOGGER.error("Failed to read previously readable image " + this.imageFile.getAbsolutePath());
         return makeNullImage();
       } else {
         this.image = new SoftReference<>(bufferedImage);
       }
     }
     return bufferedImage;
   }
   
   public void cache() {
     final BufferedImage bufferedImage = getImage();
     ResourceCache.cacheRawImage(this.imageFile, bufferedImage);
     
   }
   
   AffineTransform getTransformation(final int canvasWidth, final int canvasHeight) {
     final AffineTransform at = new AffineTransform(); // start off with an identity transformation
     
     if (this.imageFile == null) {
       at.translate(canvasWidth / 4.0d, canvasHeight / 4.0d);
       at.scale(canvasWidth / (200 * 2.0d), canvasHeight / (50 * 8.0d));
       return at;
     }
     
     // determine scaling
     final double sx = determineScaling(canvasWidth, canvasHeight, this.imageSizeX, this.imageSizeY, this.scaleX,
         this.scaleY);
     final double sy = determineScaling(canvasHeight, canvasWidth, this.imageSizeY, this.imageSizeX, this.scaleY,
         this.scaleX);
     
     // determine translation
     final double tx = determineTranslation(canvasWidth, sx * this.imageSizeX, this.alignX, this.posX);
     final double ty = determineTranslation(canvasHeight, sy * this.imageSizeY, this.alignY, this.posY);
     
     // rotate
     
     // apply in reverse order
     if (this.rotation != null) {
       at.rotate((this.rotation.doubleValue() * 2.0d * Math.PI) / 360.0d, tx + (sx * this.imageSizeX) / 2.0, ty
           + (sy * this.imageSizeY) / 2.0);
     }
     at.translate(tx, ty);
     at.scale(sx, sy);
     
     return at;
   }
   
   private static double determineScaling(final int thisDimCanvas, final int otherDimCanvas, final int thisDimImage,
       final int otherDimImage, final Double thisScale, final Double otherScale) {
     if (thisScale != null) {
       // use explicit scaling given for this dimension
       return determineScaling(thisDimCanvas, thisDimImage, thisScale);
     } else {
       if (otherScale != null) {
         // use explicit scaling given for other dimension for this dimension as well
         return determineScaling(thisDimCanvas, thisDimImage, otherScale);
       } else {
         // no explicit scaling given in either dimension; scale uniformly for best fit
         final double s1 = (double) thisDimCanvas / (double) thisDimImage;
         final double s2 = (double) otherDimCanvas / (double) otherDimImage;
         
         return s1 < s2 ? s1 : s2;
       }
     }
   }
   
   private static double determineScaling(final int dimCanvas, final int dimImage, final Double scale) {
     return ((double) dimCanvas / (double) dimImage) * scale.doubleValue() / 100.0d;
   }
   
   private static double determineTranslation(final int dimCanvas, final double dimImage, final String align,
       final Double pos) {
     if (align.equals("TOP") || align.equals("LEFT")) {
       if (pos == null) {
         return 0.0d;
       } else {
         return (dimCanvas * pos.doubleValue()) / 100.0d;
       }
     } else if (align.equals("CENTER")) {
       // if pos != null => align != CENTER, so pos must be null here
       return (dimCanvas - dimImage) / 2;
     } else /* align == BOTTOM or RIGHT */{
       double posVal = 0.0D;
       if (pos != null) {
         posVal = pos.doubleValue();
       }
       return dimCanvas - dimImage - ((dimCanvas * posVal) / 100.0d);
     }
   }
   
   public void setDeleted() {
     final Properties deletedPlacement = new Properties();
     deletedPlacement.put("Deleted", "true");
     setPlacement(deletedPlacement);
     writeProxyFile();
   }
   
   public String getAttribute() {
     return this.attribute;
   }
 }
