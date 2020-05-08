 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wms.map;
 
 import java.awt.image.RenderedImage;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 import javax.imageio.stream.ImageOutputStream;
 
 import org.geoserver.wms.WMS;
 import org.geoserver.wms.WMSMapContext;
 import org.geoserver.wms.request.GetMapRequest;
 import org.geotools.coverage.CoverageFactoryFinder;
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.coverage.grid.GridCoverageFactory;
 import org.geotools.gce.geotiff.GeoTiffWriter;
 import org.geotools.geometry.GeneralEnvelope;
 import org.geotools.image.palette.InverseColorMapOp;
 import org.geotools.util.logging.Logging;
 import org.vfny.geoserver.wms.WmsException;
 
 /**
  * Map producer for GeoTiff output format. It basically relies on the GeoTiff module of geotools.
  * 
  * 
  * @author Simone Giannecchini, GeoSolutions
  * 
  */
 public class GeoTIFFMapOutputFormat extends DefaultRasterMapOutputFormat {
 
     /** A logger for this class. */
     private static final Logger LOGGER = Logging.getLogger(GeoTIFFMapOutputFormat.class);
 
     private static final String IMAGE_GEOTIFF = "image/geotiff";
 
     private static final String IMAGE_GEOTIFF8 = "image/geotiff8";
 
     /** GridCoverageFactory. */
     private final static GridCoverageFactory factory = CoverageFactoryFinder
             .getGridCoverageFactory(null);
 
     private static final String[] OUTPUT_FORMATS = { IMAGE_GEOTIFF, IMAGE_GEOTIFF8 };
 
     /**
      * Constructo for a {@link GeoTIFFMapOutputFormat}.
      * 
      * @param wms
      *            that is asking us to encode the image.
      */
     public GeoTIFFMapOutputFormat(WMS wms) {
        super(IMAGE_GEOTIFF, OUTPUT_FORMATS, wms);
     }
 
     public void formatImageOutputStream(RenderedImage image, OutputStream outStream,
             WMSMapContext mapContext) throws WmsException, IOException {
         // crating a grid coverage
         final GridCoverage2D gc = factory.create("geotiff", image,
                 new GeneralEnvelope(mapContext.getAreaOfInterest()));
 
         // tiff
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.fine("Writing tiff image ...");
         }
 
         // get the one required by the GetMapRequest
         GetMapRequest request = mapContext.getRequest();
         final String format = request.getFormat();
         // do we want it to be 8 bits?
         InverseColorMapOp paletteInverter = mapContext.getPaletteInverter();
         if (IMAGE_GEOTIFF8.equalsIgnoreCase(format) || (paletteInverter != null)) {
             image = forceIndexed8Bitmask(image, paletteInverter);
         }
 
         // writing it out
         final ImageOutputStream imageOutStream = ImageIO.createImageOutputStream(outStream);
         if (imageOutStream == null) {
             throw new WmsException("Unable to create ImageOutputStream.");
         }
 
         GeoTiffWriter writer = null;
 
         // write it out
         try {
             writer = new GeoTiffWriter(imageOutStream);
             writer.write(gc, null);
         } finally {
             try {
                 imageOutStream.close();
             } catch (Throwable e) {
                 // eat exception to release resources silently
                 if (LOGGER.isLoggable(Level.FINEST))
                     LOGGER.log(Level.FINEST, "Unable to properly close output stream", e);
             }
 
             try {
                 if (writer != null)
                     writer.dispose();
             } catch (Throwable e) {
                 // eat exception to release resources silently
                 if (LOGGER.isLoggable(Level.FINEST))
                     LOGGER.log(Level.FINEST, "Unable to properly dispose writer", e);
             }
         }
 
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.fine("Writing tiff image done!");
         }
     }
 }
