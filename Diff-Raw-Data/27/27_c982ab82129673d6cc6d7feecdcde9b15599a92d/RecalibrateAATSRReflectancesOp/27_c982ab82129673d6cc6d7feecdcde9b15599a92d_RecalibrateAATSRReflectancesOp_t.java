 /* 
  * Copyright (C) 2002-2008 by Brockmann Consult
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
 package org.esa.beam.aatsrrecalibration.operators;
 
 import com.bc.ceres.core.ProgressMonitor;
 import org.esa.beam.dataio.envisat.EnvisatConstants;
 import org.esa.beam.framework.datamodel.Band;
 import org.esa.beam.framework.datamodel.FlagCoding;
 import org.esa.beam.framework.datamodel.MetadataElement;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.datamodel.ProductData;
 import org.esa.beam.framework.gpf.Operator;
 import org.esa.beam.framework.gpf.OperatorException;
 import org.esa.beam.framework.gpf.OperatorSpi;
 import org.esa.beam.framework.gpf.Tile;
 import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
 import org.esa.beam.framework.gpf.annotations.Parameter;
 import org.esa.beam.framework.gpf.annotations.SourceProduct;
 import org.esa.beam.framework.gpf.annotations.TargetProduct;
 import org.esa.beam.util.ProductUtils;
 
 import java.awt.Rectangle;
 import java.io.File;
 
 /**
  * Operator for recalibrating AATSR reflectances.
  *
  * @author Olaf Danne
  * @version $Revision: 5849 $ $Date: 2009-07-02 15:07:05 +0200 (Do, 02 Jul 2009) $
  */
 @OperatorMetadata(alias = "recalibrateAATSRReflectances",
                   version = "1.0-SNAPSHOT",
                   authors = "Ralf Quast, Olaf Danne",
                   copyright = "(c) 2008 by Brockmann Consult",
                   description = "Recalibrate AATSR Reflectances.")
 public class RecalibrateAATSRReflectancesOp extends Operator {
     @SourceProduct(alias = "source",
                    label = "Name (AATSR L1b product)",
                    description = "Select an AATSR L1b product.")
     private Product sourceProduct;
     @TargetProduct(description = "The target product. Contains the recalibrated reflectances.")
     private Product targetProduct;
 
     @Parameter(defaultValue = "false",
                label = "Use own drift corrections table")
     boolean useOwnDriftTable;
 
     @Parameter(alias = "DRIFT_TABLE_FILE_PATH",
                defaultValue = "",
                description = "AATSR drift corrections table file",
                label = "Drift corrections table file")
     private File userDriftTablePath;
 
     @Parameter(defaultValue = "true",
                label = "Recalibrate nadir reflectance at 1600nm")
     boolean recalibrateNadir1600;
 
     @Parameter(defaultValue = "true",
                label = "Recalibrate nadir reflectance at 870nm")
     boolean recalibrateNadir0870;
 
     @Parameter(defaultValue = "true",
                label = "Recalibrate nadir reflectance at 670nm")
     boolean recalibrateNadir0670;
 
     @Parameter(defaultValue = "true",
                label = "Recalibrate nadir reflectance at 550nm")
     boolean recalibrateNadir0550;
 
     @Parameter(defaultValue = "true",
                label = "Recalibrate forward reflectance at 1600nm")
     boolean recalibrateFward1600;
 
     @Parameter(defaultValue = "true",
                label = "Recalibrate forward reflectance at 870nm")
     boolean recalibrateFward0870;
 
     @Parameter(defaultValue = "true",
                label = "Recalibrate forward reflectance at 670nm")
     boolean recalibrateFward0670;
 
     @Parameter(defaultValue = "true",
                label = "Recalibrate forward reflectance at 550nm")
     boolean recalibrateFward0550;
 
     //    private static final String INVALID_EXPRESSION = "l1_flags.INVALID or l1_flags.LAND_OCEAN";
     @SuppressWarnings("unused")
     private static final String INVALID_EXPRESSION = "";  // TBD
 
     public static final String CONFID_NADIR_FLAGS = "confid_flags_nadir";
     public static final String CONFID_FWARD_FLAGS = "confid_flags_fward";
     public static final String CLOUD_NADIR_FLAGS = "cloud_flags_nadir";
     public static final String CLOUD_FWARD_FLAGS = "cloud_flags_fward";
 
     private static final double SECONDS_PER_DAY = 86400;
 
     private String envisatLaunch = "01-MAR-2002 00:00:00";
     private String sensingStart;
 
     private Recalibration recalibration;
    private boolean isRecalibrated;
 
     @Override
     public void initialize() throws OperatorException {
 
         recalibration = new Recalibration(useOwnDriftTable, userDriftTablePath);
 
         if (sourceProduct != null) {
             // todo: check if a preferred tile size should be set...
             // sourceProduct.setPreferredTileSize(16, 16);
 
             createTargetProduct();
         }
 
         try {
             recalibration.readDriftTable();
         } catch (Exception e) {
             throw new OperatorException("Failed to load aux data:\n" + e.getMessage());
         }
 
         sensingStart = sourceProduct.getMetadataRoot()
                 .getElement("MPH").getAttribute("SENSING_START").getData().getElemString().substring(0, 20);
         recalibration.checkAcquisitionTimeRange(sensingStart);
     }
 
     /**
      * This method creates the target product
      */
     private void createTargetProduct() {
         String productType = sourceProduct.getProductType();
         String productName = sourceProduct.getName();
         int sceneWidth = sourceProduct.getSceneRasterWidth();
         int sceneHeight = sourceProduct.getSceneRasterHeight();
 
         targetProduct = new Product(productName, productType, sceneWidth, sceneHeight);
 
         // loop over bands and create them
         for (Band band : sourceProduct.getBands()) {
             if (!band.isFlagBand())
                 ProductUtils.copyBand(band.getName(), sourceProduct, targetProduct);
         }
         ProductUtils.copyTiePointGrids(sourceProduct, targetProduct);
         ProductUtils.copyGeoCoding(sourceProduct, targetProduct);
         ProductUtils.copyMetadata(sourceProduct, targetProduct);
         setFlagBands();
 
        MetadataElement mphSource = sourceProduct.getMetadataRoot().getElement("MPH");
        isRecalibrated = (mphSource.getAttribute("RECALIBRATED") != null &&
                mphSource.getAttributeString("RECALIBRATED") != null &&
                mphSource.getAttributeString("RECALIBRATED").equals("YES"));
 
 //        BandArithmeticOp bandArithmeticOp =
 //            BandArithmeticOp.createBooleanExpressionBand(INVALID_EXPRESSION, sourceProduct);
 //        Band invalidBand = bandArithmeticOp.getTargetProduct().getBandAt(0);
     }
 
     /**
      * This method sets up the flag bands for the target product
      */
     private void setFlagBands() {
         Band confidFlagNadirBand = targetProduct.addBand(CONFID_NADIR_FLAGS, ProductData.TYPE_INT16);
         Band confidFlagFwardBand = targetProduct.addBand(CONFID_FWARD_FLAGS, ProductData.TYPE_INT16);
         Band cloudFlagNadirBand = targetProduct.addBand(CLOUD_NADIR_FLAGS, ProductData.TYPE_INT16);
         Band cloudFlagFwardBand = targetProduct.addBand(CLOUD_FWARD_FLAGS, ProductData.TYPE_INT16);
 
         FlagCoding confidNadirFlagCoding = sourceProduct.getFlagCodingGroup().get(CONFID_NADIR_FLAGS);
         ProductUtils.copyFlagCoding(confidNadirFlagCoding, targetProduct);
         confidFlagNadirBand.setSampleCoding(confidNadirFlagCoding);
 
         FlagCoding confidFwardFlagCoding = sourceProduct.getFlagCodingGroup().get(CONFID_FWARD_FLAGS);
         ProductUtils.copyFlagCoding(confidFwardFlagCoding, targetProduct);
         confidFlagFwardBand.setSampleCoding(confidFwardFlagCoding);
 
         FlagCoding cloudNadirFlagCoding = sourceProduct.getFlagCodingGroup().get(CLOUD_NADIR_FLAGS);
         ProductUtils.copyFlagCoding(cloudNadirFlagCoding, targetProduct);
         cloudFlagNadirBand.setSampleCoding(cloudNadirFlagCoding);
 
         FlagCoding cloudFwardFlagCoding = sourceProduct.getFlagCodingGroup().get(CLOUD_FWARD_FLAGS);
         ProductUtils.copyFlagCoding(cloudFwardFlagCoding, targetProduct);
         cloudFlagFwardBand.setSampleCoding(cloudFwardFlagCoding);
     }
 
     private static boolean isTargetBandValid(Band targetBand) {
         return targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_1600_BAND_NAME) ||
                 targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_0870_BAND_NAME) ||
                 targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_0670_BAND_NAME) ||
                 targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_0550_BAND_NAME) ||
                 targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_1600_BAND_NAME) ||
                 targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_0870_BAND_NAME) ||
                 targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_0670_BAND_NAME) ||
                 targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_0550_BAND_NAME);
     }
 
     private boolean isTargetBandSelected(Band targetBand) {
         return (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_1600_BAND_NAME) &&
                 recalibrateNadir1600) ||
                 (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_0870_BAND_NAME) &&
                         recalibrateNadir0870) ||
                 (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_0670_BAND_NAME) &&
                         recalibrateNadir0670) ||
                 (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_0550_BAND_NAME) &&
                         recalibrateNadir0550) ||
                 (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_1600_BAND_NAME) &&
                         recalibrateFward1600) ||
                 (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_0870_BAND_NAME) &&
                         recalibrateFward0870) ||
                 (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_0670_BAND_NAME) &&
                         recalibrateFward0670) ||
                 (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_0550_BAND_NAME) &&
                         recalibrateFward0550);
     }
 
     private static int getChannelIndex(Band targetBand) {
         int channelIndex = -1;
 
         if (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_0550_BAND_NAME) ||
                 targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_0550_BAND_NAME)) {
             channelIndex = Recalibration.CHANNEL550;
         }
         if (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_0670_BAND_NAME) ||
                 targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_0670_BAND_NAME)) {
             channelIndex = Recalibration.CHANNEL670;
         }
         if (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_0870_BAND_NAME) ||
                 targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_0870_BAND_NAME)) {
             channelIndex = Recalibration.CHANNEL870;
         }
         if (targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_NADIR_1600_BAND_NAME) ||
                 targetBand.getName().equals(EnvisatConstants.AATSR_L1B_REFLEC_FWARD_1600_BAND_NAME)) {
             channelIndex = Recalibration.CHANNEL1600;
         }
 
         return channelIndex;
     }
 
     @Override
     public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {
 
         Rectangle rectangle = targetTile.getRectangle();
 
         pm.beginTask("Processing frame...", rectangle.height);
         try {
             Tile sourceTile = getSourceTile(sourceProduct.getBand(targetBand.getName()), rectangle, pm);
             if (targetBand.isFlagBand()) {
                 for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                     for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
                         if (pm.isCanceled()) {
                             break;
                         }
                         targetTile.setSample(x, y, sourceTile.getSampleInt(x, y));
                     }
                     pm.worked(1);
                 }
            } else if (!isRecalibrated && isTargetBandSelected(targetBand) && isTargetBandValid(targetBand)) {
                 // apply recalibration
 
 //				Tile isInvalid = getSourceTile(invalidBand, rectangle, pm); // TODO if necessary
 
                 String vc1Filename = sourceProduct.getMetadataRoot().getElement(
                         "DSD").getElement("DSD.31").getAttribute("FILE_NAME")
                         .getData().getElemString();
                 String gc1Filename = sourceProduct.getMetadataRoot().getElement(
                         "DSD").getElement("DSD.32").getAttribute("FILE_NAME")
                         .getData().getElemString();
 
                 final int ati = recalibration.getAcquisitionTimeIndex(sensingStart);
                 final double atiPrev = recalibration.getAcquisitionTimeIndexPrevious(ati);
                 final double atiNext = recalibration.getAcquisitionTimeIndexNext(ati);
                 final double sensingStartMillis = recalibration.getTimeInMillis(sensingStart);
                 int removeDriftCorrIndex = recalibration.getRemoveDriftCorrectionIndex(vc1Filename);
 
                 int iChannel = getChannelIndex(targetBand);
 
                 // acquisition time difference in days
                 double acquisitionTimeDiff = (recalibration.getTimeInMillis(sensingStart) - recalibration.getTimeInMillis(
                         envisatLaunch)) / (1.E3 * SECONDS_PER_DAY);
 
                 for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                     for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
                         if (pm.isCanceled()) {
                             break;
                         }
                         // TODO: activate if invalid flag has been defined
                         //					if (isInvalid.getSampleBoolean(x, y)) {
                         //						targetTile.setSample(x, y, 0);
                         //					} else {
                         final double reflectance = sourceTile.getSampleDouble(x, y);
 
                         // Correct for nonlinearity
                         double reflectanceCorrected1 = reflectance;
                         if (iChannel == Recalibration.CHANNEL1600) {
                             reflectanceCorrected1 = recalibration.getV16NonlinearityCorrectedReflectance(gc1Filename, reflectance);
                         }
                         // Remove existing long term drift
                         double reflectanceCorrected2 = recalibration.removeDriftCorrection(iChannel, removeDriftCorrIndex,
                                                                                            acquisitionTimeDiff, reflectanceCorrected1);
 
                         // Apply new long term drift corrections
                         double reflectanceCorrectedAll = recalibration.applyDriftCorrection(sensingStartMillis, ati, atiPrev, atiNext,
                                                                                             iChannel, reflectanceCorrected2);
 
                         targetTile.setSample(x, y, reflectanceCorrectedAll);
                     }
                     pm.worked(1);
                 }
                // flag target product as 'RECALIBRATED' in metadata
                MetadataElement mph = targetProduct.getMetadataRoot().getElement("MPH");
                mph.setAttributeString("RECALIBRATED", "YES");
             } else {
                 // band is either:
                 //		- brightness temperature
                 //		- reflectance which shall not be recalibrated
                 // --> just copy from source
                 for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                     for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
                         if (pm.isCanceled()) {
                             break;
                         }
                         targetTile.setSample(x, y, sourceTile.getSampleDouble(x, y));
                     }
                     pm.worked(1);
                 }
             }
         } catch (Exception e) {
            // flag target product as 'FAILED' in metadata
            MetadataElement mph = targetProduct.getMetadataRoot().getElement("MPH");
            mph.setAttributeString("RECALIBRATED", "FAILED");
             throw new OperatorException("AATSR Recalibration Failed: \n" + e.getMessage(), e);
         } finally {
             pm.done();
         }
     }
 
 
     public static class Spi extends OperatorSpi {
         public Spi() {
             super(RecalibrateAATSRReflectancesOp.class);
         }
     }
 }
