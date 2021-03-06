 /*
  * Copyright (C) 2002-2007 by ?
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation. This program is distributed in the hope it will
  * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.esa.nest.gpf;
 
 import com.bc.ceres.core.ProgressMonitor;
 import org.esa.beam.framework.datamodel.*;
 import org.esa.beam.framework.gpf.Operator;
 import org.esa.beam.framework.gpf.OperatorException;
 import org.esa.beam.framework.gpf.OperatorSpi;
 import org.esa.beam.framework.gpf.Tile;
 import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
 import org.esa.beam.framework.gpf.annotations.SourceProduct;
 import org.esa.beam.framework.gpf.annotations.TargetProduct;
 import org.esa.beam.framework.gpf.annotations.Parameter;
 import org.esa.beam.util.ProductUtils;
 import org.esa.beam.util.math.MathUtils;
 import org.esa.nest.datamodel.AbstractMetadata;
 
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Original SAR images generally appears with inherent speckle noise. Multi-look integration is one category
  * of methods to reduce this inherent speckle noise. The frequency-domain method consis of
  *
  * (1) dividing the bandwidth of the azimuth spectrum of the image into L segments (called looks),
  * (2) forming L independent images from these spectra, and
  * (3) incoherently averaing them.
  *
  * There is also a time-domain method which produce the multi-looked image by averaging the single look image
  * with a small sliding window.
  *
  * This operator implements the simple time-domain method. The multi-looked image is produced according to a
  * user specified factor and a default factor determined by range and azimuth spacings of the original image.
  * As a result, image with equal pixel spacing is produced.
  */
 
 @OperatorMetadata(alias="Multilook",
         description="Produces Multilooked image averaging the power across a number of lines in both the azimuth and range directions")
 public final class MultilookOp extends Operator {
 
     @SourceProduct(alias="source")
     private Product sourceProduct;
     @TargetProduct
     private Product targetProduct;
 
     @Parameter(description = "The list of source bands.", alias = "sourceBands", itemAlias = "band",
             sourceProductId="source", label="Source Bands")
     String[] sourceBandNames;
 
     @Parameter(description = "The user defined multi-look factor", interval = "[2, *)", defaultValue = "2",
                 label="Multi-look Factor")
     private int multiLookFactor;
 
     @Parameter(defaultValue="Currently, detection for complex data is performed without any resampling", label="Note")
     String note;
 
     private Band targetBand;
 
     private MetadataElement absRoot;
 
     private String sampleType;
     private String missionType;
 
     private boolean srgrFlag;
 
     private int numAzimuthLooks;
     private int numRangeLooks;
     private int azimuthFactor;
     private int rangeFactor;
     private int sourceImageWidth;
     private int sourceImageHeight;
     private int targetImageWidth;
     private int targetImageHeight;
 
     private double rangeSpacing;
     private double azimuthSpacing;
     private double incidenceAngleAtCentreRangePixel; // in degree
 
     private HashMap<String, String[]> targetBandNameToSourceBandName;
 
     static final int AMPLITUDE = 0;
     static final int INTENSITY = 1;
     static final int COMPLEX = 2;
     static final int PHASE = 3;
     static final int INTENSITY_DB = 4;
 
     /**
      * Initializes this operator and sets the one and only target product.
      * <p>The target product can be either defined by a field of type {@link org.esa.beam.framework.datamodel.Product} annotated with the
      * {@link org.esa.beam.framework.gpf.annotations.TargetProduct TargetProduct} annotation or
      * by calling {@link #setTargetProduct} method.</p>
      * <p>The framework calls this method after it has created this operator.
      * Any client code that must be performed before computation of tile data
      * should be placed here.</p>
      *
      * @throws org.esa.beam.framework.gpf.OperatorException
      *          If an error occurs during operator initialisation.
      * @see #getTargetProduct()
      */
     @Override
     public void initialize() throws OperatorException {
 
         absRoot = getAbstractedMetadata(sourceProduct);
 
         getSampleType();
 
         getMissionType();
 
         getSRGRFlag();
 
         getRangeAzimuthSpacing();
 
         getRangeAzimuthLooks();
 
         getSourceImageDimension();
 
         if (!srgrFlag) {
             getIncidenceAngleAtCentreRangePixel();
         }
 
         computeRangeAzimuthMultiLookFactors();
 
         createTargetProduct();
     }
 
     /**
      * Called by the framework in order to compute a tile for the given target band.
      * <p>The default implementation throws a runtime exception with the message "not implemented".</p>
      *
      * @param targetBand The target band.
      * @param targetTile The current tile associated with the target band to be computed.
      * @param pm         A progress monitor which should be used to determine computation cancelation requests.
      * @throws org.esa.beam.framework.gpf.OperatorException
      *          If an error occurs during computation of the target raster.
      */
     @Override
     public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {
 
         Rectangle targetTileRectangle = targetTile.getRectangle();
         int tx0 = targetTileRectangle.x;
         int ty0 = targetTileRectangle.y;
         int tw  = targetTileRectangle.width;
         int th  = targetTileRectangle.height;
 
         int x0 = tx0 * rangeFactor;
         int y0 = ty0 * azimuthFactor;
         int w  = tw * rangeFactor;
         int h  = th * azimuthFactor;
         Rectangle sourceTileRectangle = new Rectangle(x0, y0, w, h);
 
         //System.out.println("tx0 = " + tx0 + ", ty0 = " + ty0 + ", tw = " + tw + ", th = " + th);
         //System.out.println("x0 = " + x0 + ", y0 = " + y0 + ", w = " + w + ", h = " + h);
 
         Tile sourceRaster1;
         Tile sourceRaster2 = null;
         String[] srcBandNames = targetBandNameToSourceBandName.get(targetBand.getName());
         Band sourceBand1;
         if (srcBandNames.length == 1) {
             sourceBand1 = sourceProduct.getBand(srcBandNames[0]);
             sourceRaster1 = getSourceTile(sourceBand1, sourceTileRectangle, pm);
         } else {
             sourceBand1 = sourceProduct.getBand(srcBandNames[0]);
             Band sourceBand2 = sourceProduct.getBand(srcBandNames[1]);
             sourceRaster1 = getSourceTile(sourceBand1, sourceTileRectangle, pm);
             sourceRaster2 = getSourceTile(sourceBand2, sourceTileRectangle, pm);
         }
 
         final int bandUnit = getSourceBandUnit(sourceBand1);
 
         computeMultiLookImageUsingTimeDomainMethod(tx0, ty0, tw, th, sourceRaster1, sourceRaster2, targetTile, bandUnit);
     }
 
     /**
      * Get abstracted metadata.
      */
     public static MetadataElement getAbstractedMetadata(Product sourceProduct) {
 
         final MetadataElement abstractedMetadata = sourceProduct.getMetadataRoot().getElement("Abstracted Metadata");
         if (abstractedMetadata == null) {
             throw new OperatorException("Abstracted Metadata not found");
         }
         return abstractedMetadata;
     }
 
     /**
      * Get the sample type.
      */
     void getSampleType() {
 
         MetadataAttribute sampleTypeAttr = absRoot.getAttribute(AbstractMetadata.SAMPLE_TYPE);
         if (sampleTypeAttr == null) {
             throw new OperatorException(AbstractMetadata.SAMPLE_TYPE + " not found");
         }
 
         sampleType = sampleTypeAttr.getData().getElemString();
         System.out.println("Sample type is " + sampleType);
     }
 
     /**
      * Get the mission type.
      */
     void getMissionType() {
 
         MetadataAttribute missionTypeAttr = absRoot.getAttribute(AbstractMetadata.MISSION);
         if (missionTypeAttr == null) {
             throw new OperatorException(AbstractMetadata.MISSION + " not found");
         }
 
         missionType = missionTypeAttr.getData().getElemString();
         System.out.println("Mission is " + missionType);
     }
 
     /**
      * Get srgr flag.
      */
     void getSRGRFlag() {
 
         MetadataAttribute attr = absRoot.getAttribute(AbstractMetadata.srgr_flag);
         if (attr == null) {
             throw new OperatorException(AbstractMetadata.srgr_flag + " not found");
         }
 
         srgrFlag = attr.getData().getElemBoolean();
         System.out.println("SRGR flag is " + srgrFlag);
     }
 
     /**
      * Get the range and azimuth spacings (in meter).
      */
     void getRangeAzimuthSpacing() {
 
         MetadataAttribute rangeSpacingAttr = absRoot.getAttribute(AbstractMetadata.range_spacing);
         if (rangeSpacingAttr == null) {
             throw new OperatorException(AbstractMetadata.range_spacing + " not found");
         }
 
         MetadataAttribute azimuthSpacingAttr = absRoot.getAttribute(AbstractMetadata.azimuth_spacing);
         if (azimuthSpacingAttr == null) {
             throw new OperatorException(AbstractMetadata.azimuth_spacing + " not found");
         }
 
         rangeSpacing = rangeSpacingAttr.getData().getElemFloat();
         azimuthSpacing = azimuthSpacingAttr.getData().getElemFloat();
         System.out.println("Range spacing is " + rangeSpacing);
         System.out.println("Azimuth spacing is " + azimuthSpacing);
     }
 
     /**
      * Get azimuth and range looks.
      */
     void getRangeAzimuthLooks() {
 
         MetadataAttribute azimuthLooksAttr = absRoot.getAttribute(AbstractMetadata.azimuth_looks);
         if (azimuthLooksAttr == null) {
             throw new OperatorException(AbstractMetadata.azimuth_looks + " not found");
         }
 
         MetadataAttribute rangeLooksAttr = absRoot.getAttribute(AbstractMetadata.range_looks);
         if (rangeLooksAttr == null) {
             throw new OperatorException(AbstractMetadata.range_looks + " not found");
         }
 
         numAzimuthLooks = azimuthLooksAttr.getData().getElemInt();
         numRangeLooks = rangeLooksAttr.getData().getElemInt();
         System.out.println("Azimuth looks is " + numAzimuthLooks);
         System.out.println("Range looks is " + numRangeLooks);
     }
 
     /**
      * Get source image dimension.
      */
     void getSourceImageDimension() {
         sourceImageWidth = sourceProduct.getSceneRasterWidth();
         sourceImageHeight = sourceProduct.getSceneRasterHeight();
         System.out.println("Source image width = " + sourceImageWidth);
         System.out.println("Source image height = " + sourceImageHeight);
     }
 
     /**
      * Get incidence angle at centre range pixel (in degree).
      */
     void getIncidenceAngleAtCentreRangePixel() {
 
         int x = sourceImageWidth / 2;
         int y = sourceImageHeight / 2;
         TiePointGrid incidenceAngle = getIncidenceAngle();
         if(incidenceAngle == null)
             throw new OperatorException("incidence_angle tie point grid not found in product");
         
         incidenceAngleAtCentreRangePixel = incidenceAngle.getPixelFloat(x + 0.5f, y + 0.5f);
     }
 
     /**
      * Get incidence angle tie point grid.
      *
      * @return srcTPG The incidence angle tie point grid.
      */
     TiePointGrid getIncidenceAngle() {
 
         for (int i = 0; i < sourceProduct.getNumTiePointGrids(); i++) {
             TiePointGrid srcTPG = sourceProduct.getTiePointGridAt(i);
             if (srcTPG.getName().equals("incident_angle")) {
                 return srcTPG;
             }
         }
 
         return null;
     }
 
     /**
      * Compute range and azimuth multi-look factors.
      */
     void computeRangeAzimuthMultiLookFactors() {
 
         double groundRangeSpacing;
         if (srgrFlag) {
             groundRangeSpacing = rangeSpacing;
         } else {
             groundRangeSpacing = rangeSpacing / Math.sin(incidenceAngleAtCentreRangePixel*MathUtils.DTOR);
         }
 
         if (groundRangeSpacing < azimuthSpacing) {
 
             azimuthFactor = multiLookFactor;
             rangeFactor = ((int)(azimuthSpacing / groundRangeSpacing + 0.5))*azimuthFactor;
 
         } else if (groundRangeSpacing > azimuthSpacing) {
 
             rangeFactor = multiLookFactor;
             azimuthFactor = ((int)(groundRangeSpacing / azimuthSpacing + 0.5))*rangeFactor;
 
         } else {
 
             azimuthFactor = multiLookFactor;
             rangeFactor = multiLookFactor;
         }
 
         System.out.println("Range factor = " + rangeFactor);
         System.out.println("Azimuth factor = " + azimuthFactor);
     }
 
     /**
      * Create target product.
      */
     void createTargetProduct() {
 
         targetImageWidth = sourceImageWidth / rangeFactor;
         targetImageHeight = sourceImageHeight / azimuthFactor;
 
         targetProduct = new Product(sourceProduct.getName(),
                                     sourceProduct.getProductType(),
                                     targetImageWidth,
                                     targetImageHeight);
 
         addSelectedBands();
 
         ProductUtils.copyMetadata(sourceProduct, targetProduct);
         ProductUtils.copyTiePointGrids(sourceProduct, targetProduct);
         ProductUtils.copyFlagCodings(sourceProduct, targetProduct);
         ProductUtils.copyGeoCoding(sourceProduct, targetProduct);
         targetProduct.setStartTime(sourceProduct.getStartTime());
         targetProduct.setEndTime(sourceProduct.getEndTime());
 
         updateTargetProductMetadata();
     }
 
     /**
      * Update metadata in the target product.
      */
     void updateTargetProductMetadata() {
 
         MetadataElement abs = targetProduct.getMetadataRoot().getElement("Abstracted Metadata");
         if (abs == null) {
             throw new OperatorException("Abstracted Metadata not found");
         }
 
         MetadataAttribute azimuthLooksAttr = abs.getAttribute(AbstractMetadata.azimuth_looks);
         if (azimuthLooksAttr == null) {
             throw new OperatorException(AbstractMetadata.azimuth_looks + " not found");
         }
         azimuthLooksAttr.getData().setElemFloat(numAzimuthLooks*azimuthFactor);
 
         MetadataAttribute rangeLooksAttr = abs.getAttribute(AbstractMetadata.range_looks);
         if (rangeLooksAttr == null) {
             throw new OperatorException(AbstractMetadata.range_looks + " not found");
         }
         rangeLooksAttr.getData().setElemFloat(numRangeLooks*rangeFactor);
 
         MetadataAttribute azimuthSpacingAttr = abs.getAttribute(AbstractMetadata.azimuth_spacing);
         if (azimuthSpacingAttr == null) {
             throw new OperatorException(AbstractMetadata.azimuth_spacing + " not found");
         }
         azimuthSpacingAttr.getData().setElemFloat((float)(azimuthSpacing*azimuthFactor));
 
         MetadataAttribute rangeSpacingAttr = abs.getAttribute(AbstractMetadata.range_spacing);
         if (rangeSpacingAttr == null) {
             throw new OperatorException(AbstractMetadata.range_spacing + " not found");
         }
         rangeSpacingAttr.getData().setElemFloat((float)(rangeSpacing*rangeFactor));
 
         MetadataAttribute lineTimeIntervalAttr = abs.getAttribute(AbstractMetadata.line_time_interval);
         if (lineTimeIntervalAttr == null) {
             throw new OperatorException(AbstractMetadata.line_time_interval + " not found");
         }
         float oldLineTimeInterval = lineTimeIntervalAttr.getData().getElemFloat();
         lineTimeIntervalAttr.getData().setElemFloat(oldLineTimeInterval*azimuthFactor);
 
         MetadataAttribute firstLineTimeAttr = abs.getAttribute(AbstractMetadata.first_line_time);
         if (firstLineTimeAttr == null) {
             throw new OperatorException(AbstractMetadata.first_line_time + " not found");
         }
         String oldFirstLineTime = firstLineTimeAttr.getData().getElemString();
         int idx = oldFirstLineTime.lastIndexOf(':') + 1;
         String oldSecondsStr = oldFirstLineTime.substring(idx);
         double oldSeconds = Double.parseDouble(oldSecondsStr);
         double newSeconds = oldSeconds + oldLineTimeInterval*((azimuthFactor - 1)/2.0);
         String newFirstLineTime = String.valueOf(oldFirstLineTime.subSequence(0, idx)) + newSeconds + "000000";
         abs.removeAttribute(firstLineTimeAttr);
         abs.addAttribute(new MetadataAttribute(
                 AbstractMetadata.first_line_time, ProductData.createInstance(newFirstLineTime.substring(0,27)), false));        
     }
 
     private void addSelectedBands() {
 
         if (sourceBandNames == null || sourceBandNames.length == 0) {
             Band[] bands = sourceProduct.getBands();
             ArrayList<String> bandNameList = new ArrayList<String>(sourceProduct.getNumBands());
             for (Band band : bands) {
                 bandNameList.add(band.getName());
             }
             sourceBandNames = bandNameList.toArray(new String[bandNameList.size()]);
         }
 
         Band[] sourceBands = new Band[sourceBandNames.length];
         for (int i = 0; i < sourceBandNames.length; i++) {
             String sourceBandName = sourceBandNames[i];
             Band sourceBand = sourceProduct.getBand(sourceBandName);
             if (sourceBand == null) {
                 throw new OperatorException("Source band not found: " + sourceBandName);
             }
             sourceBands[i] = sourceBand;
         }
 
         String targetBandName;
         targetBandNameToSourceBandName = new HashMap<String, String[]>();
         for (int i = 0; i < sourceBands.length; i++) {
 
             Band srcBand = sourceBands[i];
             String unit = srcBand.getUnit();
             if(unit == null) {
                 throw new OperatorException("band "+srcBand.getName()+" requires a unit");
             }
 
             String targetUnit = "";
 
             if (unit.contains("phase")) {
 
                 continue;
 
             } else if (unit.contains("imaginary")) {
 
                 throw new OperatorException("Real and imaginary bands should be selected in pairs");
 
             } else if (unit.contains("real")) {
 
                 if (i == sourceBands.length - 1) {
                     throw new OperatorException("Real and imaginary bands should be selected in pairs");
                 }
                 String nextUnit = sourceBands[i+1].getUnit();
                 if (nextUnit == null || !nextUnit.contains("imaginary")) {
                     throw new OperatorException("Real and imaginary bands should be selected in pairs");
                 }
                 String[] srcBandNames = new String[2];
                 srcBandNames[0] = srcBand.getName();
                 srcBandNames[1] = sourceBands[i+1].getName();
                 final String pol = getPolarizationFromBandName(srcBandNames[0]);
                 if (pol != null) {
                     targetBandName = "Amplitude_" + pol.toUpperCase();
                 } else {
                     targetBandName = "Amplitude";
                 }
                 ++i;
                 if(targetProduct.getBand(targetBandName) == null) {
                     targetBandNameToSourceBandName.put(targetBandName, srcBandNames);
                     targetUnit = "amplitude";
                 }
 
            } else if (unit.contains("db")) {
                throw new OperatorException("Please convert dB band " + srcBand.getName() +
                        " to linear before applying Multi-looking operator");
             } else {
 
                 String[] srcBandNames = {srcBand.getName()};
                 targetBandName = srcBand.getName();
                 if(targetProduct.getBand(targetBandName) == null) {
                     targetBandNameToSourceBandName.put(targetBandName, srcBandNames);
                     targetUnit = unit;
                 }
             }
 
             if(targetProduct.getBand(targetBandName) == null) {
 
                 Band targetBand = new Band(targetBandName,
                                            ProductData.TYPE_FLOAT64,
                                            targetImageWidth,
                                            targetImageHeight);
 
                 targetBand.setUnit(targetUnit);
                 targetProduct.addBand(targetBand);
             }
         }
     }
 
     static String getPolarizationFromBandName(String bandName) {
 
         final int idx = bandName.lastIndexOf('_');
         if (idx != -1) {
             final String pol = bandName.substring(idx+1).toLowerCase();
             if (!pol.contains("hh") && !pol.contains("vv") && !pol.contains("hv") && !pol.contains("vh")) {
                 return null;
             } else {
                 return pol;
             }
         } else {
             return null;
         }
     }
 
     /**
      * Compute multi-looked image using time domain method.
      *
      * @param tx0 The x coordinate of the upper left point in the current target tile.
      * @param ty0 The y coordinate of the upper left point in the current target tile.
      * @param tw  The width of the current target tile.
      * @param th  The height of the current target tile.
      * @param sourceRaster1 The source raster for the 1st band.
      * @param sourceRaster2 The source raster for the 2nd band.
      * @param targetTile The current target tile associated with the target band to be computed.
      * @param bandUnit Integer indicating the unit of source data.
      */
     void computeMultiLookImageUsingTimeDomainMethod(
             int tx0, int ty0, int tw, int th, Tile sourceRaster1, Tile sourceRaster2, Tile targetTile, int bandUnit) {
 
         ProductData trgData = targetTile.getDataBuffer();
 
         double meanValue;
         final int maxy = ty0 + th;
         final int maxx = tx0 + tw;
         for (int ty = ty0; ty < maxy; ty++) {
             for (int tx = tx0; tx < maxx; tx++) {
                 meanValue = getMeanValue(tx, ty, sourceRaster1, sourceRaster2, bandUnit);
                 trgData.setElemDoubleAt(targetTile.getDataBufferIndex(tx, ty), meanValue);
             }
         }
     }
 
     /**
      * Compute the mean value of pixels of the source image in the sliding window.
      *
      * @param tx The x coordinate of a pixel in the current target tile.
      * @param ty The y coordinate of a pixel in the current target tile.
      * @param sourceRaster1 The source raster for the 1st band.
      * @param sourceRaster2 The source raster for the 2nd band.
      * @param bandUnit Integer indicating the unit of source data.
      * @return The mean value.
      */
     double getMeanValue(int tx, int ty, Tile sourceRaster1, Tile sourceRaster2, int bandUnit) {
 
         final int xStart = tx * rangeFactor;
         final int yStart = ty * azimuthFactor;
         final int xEnd = xStart + rangeFactor;
         final int yEnd = yStart + azimuthFactor;
 
         final ProductData srcData1 = sourceRaster1.getDataBuffer();
         ProductData srcData2 = null;
         if(sourceRaster2 != null)
             srcData2 = sourceRaster2.getDataBuffer();
 
         double meanValue = 0.0;
         for (int y = yStart; y < yEnd; y++) {
             for (int x = xStart; x < xEnd; x++) {
 
                 final int index = sourceRaster1.getDataBufferIndex(x, y);
 
                 if (bandUnit == INTENSITY_DB) {
 
                     final double dn = srcData1.getElemDoubleAt(index);
                     meanValue += Math.pow(10, dn / 10.0); // dB to linear
 
                 } else if (bandUnit == AMPLITUDE || bandUnit == INTENSITY) {
 
                     meanValue += srcData1.getElemDoubleAt(index);
 
                 } else { // COMPLEX
 
                     final double i = srcData1.getElemDoubleAt(index);
                     final double q = srcData2.getElemDoubleAt(index);
                     meanValue += Math.sqrt(i*i + q*q);
                 }
             }
         }
         meanValue /= rangeFactor * azimuthFactor;
         if (bandUnit == INTENSITY_DB) {
             meanValue = 10.0*Math.log10(meanValue); // linear to dB
         }
 
         return meanValue;
     }
 
     public static int getSourceBandUnit(Band sourceBand) {
 
         String  unit =  sourceBand.getUnit();
         if (unit.contains("amplitude")) {
             return AMPLITUDE;
         } else if (unit.contains("intensity")) {
             return INTENSITY;
         } else if (unit.contains("phase")) {
             return PHASE;
         } else if (unit.equals("intensity_db")) {
             return INTENSITY_DB;
         } else {
             return COMPLEX;
         }
     }
 
     /**
      * The SPI is used to register this operator in the graph processing framework
      * via the SPI configuration file
      * {@code META-INF/services/org.esa.beam.framework.gpf.OperatorSpi}.
      * This class may also serve as a factory for new operator instances.
      * @see org.esa.beam.framework.gpf.OperatorSpi#createOperator()
      * @see org.esa.beam.framework.gpf.OperatorSpi#createOperator(java.util.Map, java.util.Map)
      */
     public static class Spi extends OperatorSpi {
         public Spi() {
             super(MultilookOp.class);
         }
     }
 }
