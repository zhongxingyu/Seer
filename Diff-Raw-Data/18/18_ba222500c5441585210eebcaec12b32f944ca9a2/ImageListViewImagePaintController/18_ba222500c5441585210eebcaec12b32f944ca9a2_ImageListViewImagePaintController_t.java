 package de.sofd.viskit.controllers.cellpaint;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.color.ColorSpace;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.awt.image.Raster;
 import java.awt.image.RescaleOp;
 import java.awt.image.WritableRaster;
 import java.util.Map;
 
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 
 import com.sun.opengl.util.texture.TextureCoords;
 
 import de.sofd.util.FloatRange;
 import de.sofd.viskit.image3D.jogl.util.GLShader;
 import de.sofd.viskit.image3D.jogl.util.ShaderManager;
 import de.sofd.viskit.model.DicomImageListViewModelElement;
 import de.sofd.viskit.model.ImageListViewModelElement;
 import de.sofd.viskit.model.LookupTable;
 import de.sofd.viskit.ui.imagelist.ImageListViewCell;
 import de.sofd.viskit.ui.imagelist.JImageListView;
import javax.media.opengl.GLException;
 
 /**
  * Cell-painting controller that paints the image of the cell's model element
  * (cell.getDisplayedModelElement().getImage()) into the cell.
  * 
  * @author olaf
  */
 public class ImageListViewImagePaintController extends CellPaintControllerBase {
 
     static {
         ShaderManager.init("shader");
     }
 
     private GLShader rescaleShader;
     
     public ImageListViewImagePaintController() {
         this(null, JImageListView.PAINT_ZORDER_IMAGE);
     }
 
     public ImageListViewImagePaintController(JImageListView controlledImageListView) {
         super(controlledImageListView, JImageListView.PAINT_ZORDER_IMAGE);
     }
 
     public ImageListViewImagePaintController(JImageListView controlledImageListView, int zOrder) {
         super(controlledImageListView, zOrder);
     }
 
     public int getOriginalImageWidth(ImageListViewCell cell) {
         ImageListViewModelElement elt = cell.getDisplayedModelElement();
         if (elt instanceof DicomImageListViewModelElement) {
             // performance optimization for this case -- read the value from DICOM metadata instead of getting the image
             DicomImageListViewModelElement dicomElt = (DicomImageListViewModelElement) elt;
             return dicomElt.getDicomImageMetaData().getInt(Tag.Columns);
         } else if (elt.hasRawImage() && elt.isRawImagePreferable()){
             return elt.getRawImage().getWidth();
         } else {
             return elt.getImage().getWidth();
         }
     }
 
     public int getOriginalImageHeight(ImageListViewCell cell) {
         ImageListViewModelElement elt = cell.getDisplayedModelElement();
         if (elt instanceof DicomImageListViewModelElement) {
             // performance optimization for this case -- read the value from DICOM metadata instead of getting the image
             DicomImageListViewModelElement dicomElt = (DicomImageListViewModelElement) elt;
             return dicomElt.getDicomImageMetaData().getInt(Tag.Rows);
         } else if (elt.hasRawImage() && elt.isRawImagePreferable()){
             return elt.getRawImage().getHeight();
         } else {
             return elt.getImage().getHeight();
         }
     }
     
     ///////////// OpenGL rendering
     
     @Override
     protected void glDrawableInitialized(GLAutoDrawable glAutoDrawable) {
        initializeGLShader(glAutoDrawable.getGL().getGL2());
    }
    
    protected void initializeGLShader(GL2 gl) {
         try {
             ShaderManager.read(gl, "rescaleop");
             rescaleShader = ShaderManager.get("rescaleop");
             rescaleShader.addProgramUniform("preScale");
             rescaleShader.addProgramUniform("preOffset");
             rescaleShader.addProgramUniform("scale");
             rescaleShader.addProgramUniform("offset");
             rescaleShader.addProgramUniform("tex");
             rescaleShader.addProgramUniform("lutTex");
             rescaleShader.addProgramUniform("useLut");
         } catch (Exception e) {
            throw new RuntimeException("couldn't initialize GL shader: " + e.getLocalizedMessage(), e);
         }
     }

     @Override
     protected void paintGL(ImageListViewCell cell, GL2 gl, Map<String, Object> sharedContextData) {
         Dimension cellSize = cell.getLatestSize();
         gl.glPushMatrix();
         try {
             //gl.glLoadIdentity();
             gl.glTranslated(cellSize.getWidth() / 2, cellSize.getHeight() / 2, 0);
             gl.glTranslated(cell.getCenterOffset().getX(), cell.getCenterOffset().getY(), 0);
             gl.glScaled(cell.getScale(), cell.getScale(), 1);
             ImageTextureManager.TextureRef texRef = ImageTextureManager.bindImageTexture(gl, sharedContextData, cell.getDisplayedModelElement());
             LookupTable lut = cell.getLookupTable();
            rescaleShader.bind();  // TODO: rescaleShader's internal gl may be outdated here...? (but shaders are shared betw. contexts, so if it's outdated, we'll have other problems as well...)
             rescaleShader.bindUniform("tex", 1);
             if (lut != null) {
                 LookupTableTextureManager.bindLutTexture(gl, sharedContextData, cell.getLookupTable());
                 rescaleShader.bindUniform("lutTex", 2);
                 rescaleShader.bindUniform("useLut", true);
             } else {
                 rescaleShader.bindUniform("useLut", false);
             }
             rescaleShader.bindUniform("preScale", texRef.getPreScale());
             rescaleShader.bindUniform("preOffset", texRef.getPreOffset());
             {
                 FloatRange pxValuesRange = cell.getDisplayedModelElement().getPixelValuesRange();
                 float minGrayvalue = pxValuesRange.getMin();
                 float nGrayvalues = pxValuesRange.getDelta();
                 float wl = (cell.getWindowLocation() - minGrayvalue) / nGrayvalues;
                 float ww = cell.getWindowWidth() / nGrayvalues;
                 float scale = 1F/ww;
                 float offset = (ww/2-wl)*scale;
                 rescaleShader.bindUniform("scale", scale);
                 rescaleShader.bindUniform("offset", offset);
                 //rescaleShader.bindUniform("scale", 1.0F);
                 //rescaleShader.bindUniform("offset", 0.0F);
             }
             // TODO: (GL_TEXTURE_ENV is ignored because a frag shader is active) make the compositing mode configurable (replace/combine)
             gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
             TextureCoords coords = texRef.getCoords();
             gl.glColor3f(0, 1, 0);
             float w2 = (float) getOriginalImageWidth(cell) / 2, h2 = (float) getOriginalImageHeight(cell) / 2;
             gl.glBegin(GL2.GL_QUADS);
             // TODO: wrong orientation here? check visually!
             gl.glTexCoord2f(coords.left(), coords.top());
             gl.glVertex2f(-w2, h2);
             gl.glTexCoord2f(coords.right(), coords.top());
             gl.glVertex2f( w2,  h2);
             gl.glTexCoord2f(coords.right(), coords.bottom());
             gl.glVertex2f( w2, -h2);
             gl.glTexCoord2f(coords.left(), coords.bottom());
             gl.glVertex2f(-w2, -h2);
             gl.glEnd();
             ImageTextureManager.unbindCurrentImageTexture(gl);
             rescaleShader.unbind();
         } finally {
             gl.glPopMatrix();
         }
     }
     
 
 
 
     ///////////// Java2D rendering
     
     @Override
     protected void paintJ2D(ImageListViewCell cell, Graphics2D g2d) {
         //give the render* methods a Graphics2D whose coordinate system
         //(and eventually, clipping) is already relative to the area in
         //which the image should be drawn
         Graphics2D userGraphics = (Graphics2D)g2d.create();
         Point2D imageOffset = getImageOffset(cell);
         userGraphics.transform(AffineTransform.getTranslateInstance(imageOffset.getX(), imageOffset.getY()));
 
         // render the image
         BufferedImageOp scaleImageOp = new AffineTransformOp(getDicomToUiTransform(cell), AffineTransformOp.TYPE_BILINEAR);
         userGraphics.drawImage(getWindowedImage(cell), scaleImageOp, 0, 0);
     }
     
     public Point2D getImageOffset(ImageListViewCell cell) {
         Point2D imgSize = getScaledImageSize(cell);
         Dimension latestSize = cell.getLatestSize();
         return new Point2D.Double((latestSize.width + 2 * cell.getCenterOffset().getX() - (int) imgSize.getX()) / 2,
                                   (latestSize.height + 2 * cell.getCenterOffset().getY() - (int) imgSize.getY()) / 2);
     }
 
     public Point2D getScaledImageSize(ImageListViewCell cell) {
         return getDicomToUiTransform(cell).transform(new Point2D.Double(getOriginalImageWidth(cell), getOriginalImageHeight(cell)), null);
     }
     
     protected AffineTransform getDicomToUiTransform(ImageListViewCell cell) {
         double z = cell.getScale();
         return AffineTransform.getScaleInstance(z, z);
     }
 
     protected BufferedImage getWindowedImage(ImageListViewCell displayedCell) {
         // TODO: caching of windowed images, probably using
         //       displayedCell.getDisplayedModelElement().getImageKey() and the windowing parameters as the cache key
         BufferedImage srcImg = displayedCell.getDisplayedModelElement().getImage();
         BufferedImage windowedImage;
         // TODO: use the model element's RawImage instead of the BufferedImage when possible
         ///*
         if (srcImg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
             windowedImage = windowMonochrome(displayedCell, srcImg, displayedCell.getWindowLocation(), displayedCell.getWindowWidth());
         } else if (srcImg.getColorModel().getColorSpace().isCS_sRGB()) {
             windowedImage = windowRGB(srcImg, displayedCell.getWindowLocation(), displayedCell.getWindowWidth());
         } else {
             throw new IllegalStateException("don't know how to window image with color space " + srcImg.getColorModel().getColorSpace());
             // TODO: do something cleverer here? Like, create windowedImage
             //    with a color space that's "compatible" to srcImg (using
             //    some createCompatibleImage() method in BufferedImage or elsewhere),
             //    window all bands of that, and let the JRE figure out how to draw the result?
         }
         //*/
         //windowedImage = windowWithRasterOp(srcImg, windowLocation, windowWidth);
         //windowedImage = srcImg;
 
         return windowedImage;
     }
 
 
     private BufferedImage windowMonochrome(ImageListViewCell displayedCell, BufferedImage srcImg, float windowLocation, float windowWidth) {
         BufferedImage destImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_INT_RGB);
 
         boolean isSigned = false;
         int minValue = 0;
         {
             // hack: try to determine signedness and minValue from DICOM metadata if available --
             // the BufferedImage's metadata don't contain that information reliably.
             // Only works for some special cases
             ImageListViewModelElement elt = displayedCell.getDisplayedModelElement();
             if (elt instanceof DicomImageListViewModelElement) {
                 DicomImageListViewModelElement delt = (DicomImageListViewModelElement) elt;
                 DicomObject imgMetadata = delt.getDicomImageMetaData();
                 int bitsAllocated = imgMetadata.getInt(Tag.BitsAllocated);
                 isSigned = (1 == imgMetadata.getInt(Tag.PixelRepresentation));
                 if (isSigned && (bitsAllocated > 0)) {
                     minValue = -(1<<(bitsAllocated-1));
                 }
             }
         }
 
 
         final int windowedImageGrayscalesCount = 256;  // for BufferedImage.TYPE_INT_RGB
         float scale = windowedImageGrayscalesCount/windowWidth;
         float offset = (windowWidth/2 - windowLocation)*scale;
         if (! (srcImg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY)) {
             throw new IllegalArgumentException("source image must be grayscales");
         }
         Raster srcRaster = srcImg.getRaster();
         if (srcRaster.getNumBands() != 1) {
             throw new IllegalArgumentException("source image must be grayscales");
         }
         WritableRaster resultRaster = destImg.getRaster();
         for (int x = 0; x < srcImg.getWidth(); x++) {
             for (int y = 0; y < srcImg.getHeight(); y++) {
                 int srcGrayValue = srcRaster.getSample(x, y, 0);
                 if (isSigned) {
                     srcGrayValue = (int)(short)srcGrayValue;  // will only work for 16-bit signed...
                 }
                 float destGrayValue = scale * srcGrayValue + offset;
                 // clamp
                 if (destGrayValue < 0) {
                     destGrayValue = 0;
                 } else if (destGrayValue >= windowedImageGrayscalesCount) {
                     destGrayValue = windowedImageGrayscalesCount - 1;
                 }
                 resultRaster.setSample(x, y, 0, destGrayValue);
                 resultRaster.setSample(x, y, 1, destGrayValue);
                 resultRaster.setSample(x, y, 2, destGrayValue);
             }
         }
         return destImg;
     }
 
 
     /*
     private void getImageMetadata() {
         DicomObject imgMetadata = displayedCell.getDisplayedModelElement().getDicomImageMetaData();
         int bitsAllocated = imgMetadata.getInt(Tag.BitsAllocated);
         if (bitsAllocated <= 0) {
         }
         int bitsStored = imgMetadata.getInt(Tag.BitsStored);
         if (bitsStored <= 0) {
         }
         boolean isSigned = (1 == imgMetadata.getInt(Tag.PixelRepresentation));
     }
      */
 
     private BufferedImage windowRGB(BufferedImage srcImg, float windowLocation, float windowWidth) {
         BufferedImage destImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_INT_RGB);
         final int windowedImageBandValuesCount = 256;  // for BufferedImage.TYPE_INT_RGB
         float scale = windowedImageBandValuesCount/windowWidth;
         float offset = (windowWidth/2-windowLocation)*scale;
         if (! srcImg.getColorModel().getColorSpace().isCS_sRGB()) {
             throw new IllegalArgumentException("source image must be RGB");
         }
         Raster srcRaster = srcImg.getRaster();
         if (srcRaster.getNumBands() != 3) {
             throw new IllegalArgumentException("source image must be RGB");
         }
         WritableRaster resultRaster = destImg.getRaster();
         for (int x = 0; x < srcImg.getWidth(); x++) {
             for (int y = 0; y < srcImg.getHeight(); y++) {
                 for (int band = 0; band < 3; band++) {
                     int srcGrayValue = srcRaster.getSample(x, y, band);
                     float destGrayValue = scale * srcGrayValue + offset;
                     // clamp
                     if (destGrayValue < 0) {
                         destGrayValue = 0;
                     } else if (destGrayValue >= windowedImageBandValuesCount) {
                         destGrayValue = windowedImageBandValuesCount - 1;
                     }
                     resultRaster.setSample(x, y, band, destGrayValue);
                 }
             }
         }
         return destImg;
     }
 
     private BufferedImage windowWithRasterOp(BufferedImage srcImg, float windowLocation, float windowWidth) {
         //final int windowedImageBandValuesCount = 256;  // for BufferedImage.TYPE_INT_RGB
         //final float windowedImageBandValuesCount = 1.0F;
         //final float windowedImageBandValuesCount = 65535F;
         //final float windowedImageBandValuesCount = 4095F;
         final float windowedImageBandValuesCount = (1 << srcImg.getColorModel().getComponentSize(0)) - 1;
         float scale = windowedImageBandValuesCount/windowWidth;
         float offset = (windowWidth/2-windowLocation)*scale;
         RescaleOp rescaleOp = new RescaleOp(scale, offset, null);
         return rescaleOp.filter(srcImg, null);
     }
 }
