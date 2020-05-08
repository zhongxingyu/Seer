 /*
  * $Id: $
  *
  * Copyright (C) 2008 by Brockmann Consult (info@brockmann-consult.de)
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
 package org.esa.beam.chris.ui;
 
 import com.bc.ceres.core.ProgressMonitor;
 import org.esa.beam.chris.operators.*;
 import org.esa.beam.chris.util.BandFilter;
 import org.esa.beam.cluster.EMCluster;
 import org.esa.beam.cluster.EMClusterOp;
 import org.esa.beam.framework.datamodel.*;
 import org.esa.beam.framework.gpf.GPF;
 import org.esa.beam.framework.gpf.Operator;
 import org.esa.beam.framework.gpf.OperatorException;
 import org.esa.beam.framework.gpf.OperatorSpi;
 import org.esa.beam.framework.ui.product.ProductSceneImage;
 import org.esa.beam.framework.ui.product.ProductSceneView;
 import org.esa.beam.unmixing.Endmember;
 import org.esa.beam.unmixing.SpectralUnmixingOp;
 import org.esa.beam.util.jai.RasterDataNodeOpImage;
 import org.esa.beam.visat.VisatApp;
 
 import javax.media.jai.ImageLayout;
 import javax.media.jai.operator.MultiplyDescriptor;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.Raster;
 import java.awt.image.RenderedImage;
 import java.io.IOException;
 import java.util.*;
 import java.util.List;
 
 /**
  * Created by marcoz.
  *
  * @author marcoz
  * @version $Revision$ $Date$
  */
 public class CloudLabeler {
 
     private final Product radianceProduct;
     private Product reflectanceProduct;
     private Product featureProduct;
     private Product clusterProduct;
     private Product clusterMapProduct;
     private ProductSceneView rgbSceneView;
 
 
     public CloudLabeler(Product radianceProduct) {
         this.radianceProduct = radianceProduct;
     }
 
     public Band getClusterMapBand() {
         return clusterMapProduct.getBand("class_indices");
     }
 
     public Product getRadianceProduct() {
         return radianceProduct;
     }
 
     public Band getCloudProductBand() {
         return radianceProduct.getBand("cloud_product");
     }
 
     public void performClusterAnalysis(ProgressMonitor pm) throws OperatorException {
         try {
             pm.beginTask("Performing cluster analysis...", 1);
             // 1. Extract features
             reflectanceProduct = createReflectanceProduct(radianceProduct);
             featureProduct = createFeatureProduct(reflectanceProduct);
 
             // 2. Find clusters
             clusterProduct = createClusterProduct(featureProduct);
 
             // 3. Cluster labeling
             clusterMapProduct = createClusterMapProduct(clusterProduct);
         } finally {
             pm.done();
         }
     }
 
     public void createRgbSceneView() throws IOException {
 
         final RasterDataNode[] rgbBands = getRgbBands(radianceProduct);
         ProductSceneImage productSceneImage = ProductSceneImage.create(rgbBands[0], rgbBands[1], rgbBands[2],
                                                                        ProgressMonitor.NULL);
         rgbSceneView = new ProductSceneView(productSceneImage);
 
         assignImageInfo(rgbSceneView.getSourceImage());
     }
 
     public ProductSceneView getRgbSceneView() {
         return rgbSceneView;
     }
 
     public void assignImageInfo(RenderedImage rgbImage) {
         final Band[] sourceBands = findBands(clusterProduct, "probability");
         final int[] r = new int[sourceBands.length];
         final int[] g = new int[sourceBands.length];
         final int[] b = new int[sourceBands.length];
         final int[] clusterCount = new int[sourceBands.length];
 
         final RenderedImage image = getClusterMapBand().getImage();
         final Raster membershipImageData = image.getData();
         final Raster rgbImageData = rgbImage.getData();
         for (int y = 0; y < image.getHeight(); y++) {
             for (int x = 0; x < image.getWidth(); x++) {
                 final int clusterIndex = membershipImageData.getSample(x, y, 0);
                 r[clusterIndex] += rgbImageData.getSample(x, y, 0);
                 g[clusterIndex] += rgbImageData.getSample(x, y, 1);
                 b[clusterIndex] += rgbImageData.getSample(x, y, 2);
                 clusterCount[clusterIndex]++;
             }
         }
 
         for (int i = 0; i < clusterCount.length; i++) {
             if (clusterCount[i] > 0) {
                 r[i] /= clusterCount[i];
                 g[i] /= clusterCount[i];
                 b[i] /= clusterCount[i];
             }
         }
 
        final MetadataAttribute[] attributes = getClusterMapBand().getIndexCoding().getAttributes();
        final ColorPaletteDef.Point[] points = new ColorPaletteDef.Point[attributes.length];
        for (int index = 0; index < attributes.length; index++) {
            MetadataAttribute attribute = attributes[index];
            final int sample = attribute.getData().getElemInt();
            final Color color;
            if (attribute.getName().startsWith("cluster")) {
                color = new Color(r[index], g[index], b[index]);
            } else {
                color = Color.BLACK;
            }
            points[index] = new ColorPaletteDef.Point(sample, color, attribute.getName());
         }
         final ColorPaletteDef def = new ColorPaletteDef(points);
         final ImageInfo imageInfo = new ImageInfo(def);
         getClusterMapBand().setImageInfo(imageInfo);
     }
 
     public static RasterDataNode[] getRgbBands(Product product) {
         return new RasterDataNode[]{
                 findBandImage(product, 700.0f),
                 findBandImage(product, 546.0f),
                 findBandImage(product, 435.0f)
         };
     }
 
     private static Band findBandImage(Product product, float wavelength) {
         final Band[] bands = product.getBands();
 
         float minDist = Float.POSITIVE_INFINITY;
         Band bestBand = null;
 
         for (Band band : bands) {
             if (band.getSpectralBandIndex() != -1) {
                 float currentDist = Math.abs(wavelength - band.getSpectralWavelength());
                 if (minDist > currentDist) {
                     bestBand = band;
                     minDist = currentDist;
                 }
             }
         }
 
         return bestBand;
     }
 
     public void performLabelingStep(int[] rejectedIndexes) throws OperatorException {
         final Band[] probabilityBands = new Band[clusterMapProduct.getNumBands() - 1];
 
         int index = 0;
         for (Band band : clusterMapProduct.getBands()) {
             if (band.getName().startsWith("probability")) {
                 probabilityBands[index] = band;
                 ImageLayout imageLayout = RasterDataNodeOpImage.createSingleBandedImageLayout(band);
                 band.setImage(ClusterProbabilityOpImage.create(imageLayout, findBands(clusterProduct, "prob"),
                                                                index, rejectedIndexes));
                 index++;
             }
         }
         final Band clusterMapBand = getClusterMapBand();
         ImageLayout imageLayout = RasterDataNodeOpImage.createSingleBandedImageLayout(clusterMapBand);
         clusterMapBand.setImage(ClusterMapOpImage.create(imageLayout, probabilityBands));
     }
 
     public void performCloudProductComputation(int[] cloudClusterIndexes, int[] surfaceClusterIndexes,
                                                boolean computeAbundances,
                                                ProgressMonitor pm) throws OperatorException {
         pm.beginTask("Computing cloud product...", 1);
 
         try {
             if (computeAbundances && cloudClusterIndexes.length > 0) {
                 // 4. Cluster probabilities
                 final Product cloudProbabilityProduct = createCloudProduct(cloudClusterIndexes, false);
                 // 5. Endmember extraction
                 final Operator endmemberOp = new ExtractEndmembersOp(reflectanceProduct,
                                                                      featureProduct,
                                                                      clusterMapProduct,
                                                                      cloudClusterIndexes,
                                                                      surfaceClusterIndexes);
                 final Endmember[] endmembers = (Endmember[]) endmemberOp.getTargetProperty("endmembers");
                 final String[] reflectanceBandNames = (String[]) endmemberOp.getTargetProperty("reflectanceBandNames");
 
                 // 6. Cloud abundances
                 final Product cloudAbundancesProduct = createCloudAbundancesProduct(endmembers, reflectanceBandNames);
 
                 // 7. Cloud probability * cloud abundance
                 addCloudImageToInput(createCloudProductImage(cloudProbabilityProduct, cloudAbundancesProduct));
             } else {
                 final Product cloudProbabilityProduct = createCloudProduct(cloudClusterIndexes, true);
                 addCloudImageToInput(cloudProbabilityProduct.getBand("cloud_product").getImage());
             }
         } finally {
             pm.done();
         }
     }
 
     private void addCloudImageToInput(RenderedImage image) {
         Band targetBand = getCloudProductBand();
         final int width = image.getWidth();
         final int height = image.getHeight();
         if (targetBand == null) {
             targetBand = new Band("cloud_product", ProductData.TYPE_FLOAT64, width, height);
             targetBand.setSynthetic(true);
             targetBand.setDescription("Cloud product");
         }
         ProductData rasterData = targetBand.getRasterData();
         if (rasterData == null) {
             rasterData = targetBand.createCompatibleRasterData();
         }
         final Object data = rasterData.getElems();
         image.getData().getDataElements(0, 0, width, height, data);
         targetBand.setRasterData(rasterData);
         if (!radianceProduct.containsBand(targetBand.getName())) {
             radianceProduct.addBand(targetBand);
         }
         final VisatApp visatApp = VisatApp.getApp();
         final JInternalFrame targetBandFrame = visatApp.findInternalFrame(targetBand);
         if (targetBandFrame != null) {
             visatApp.updateImage((ProductSceneView) targetBandFrame.getContentPane());
         } else {
             visatApp.openProductSceneView(targetBand, "");
         }
 
     }
 
     private static RenderedImage createCloudProductImage(Product cloudProbabilityProduct,
                                                          Product cloudAbundancesProduct) {
         final RenderedImage cloudProbability = cloudProbabilityProduct.getBand("cloud_product").getImage();
         final RenderedImage cloudAbundance = cloudAbundancesProduct.getBand("cloud_abundance").getImage();
         return MultiplyDescriptor.create(cloudProbability, cloudAbundance, null);
     }
 
     private Product createCloudAbundancesProduct(Endmember[] endmembers, String[] reflectanceBandNames) {
         final Map<String, Object> parameterMap = new HashMap<String, Object>(3);
         parameterMap.put("sourceBandNames", reflectanceBandNames);
         parameterMap.put("endmembers", endmembers);
         parameterMap.put("unmixingModelName", "Fully Constrained LSU");
 
         return GPF.createProduct(OperatorSpi.getOperatorAlias(SpectralUnmixingOp.class),
                                  parameterMap, reflectanceProduct);
     }
 
     private Product createCloudProduct(int[] cloudClusterIndexes, boolean applyThreshold) {
         final String[] allBandNames = clusterMapProduct.getBandNames();
         final List<String> sourceBandNameList = new ArrayList<String>(allBandNames.length);
         for (int i = 0; i < allBandNames.length; ++i) {
             if (allBandNames[i].startsWith("probability")) {
                 if (isContained(i, cloudClusterIndexes)) {
                     sourceBandNameList.add(allBandNames[i]);
                 }
             }
         }
         final String[] sourceBandNames = sourceBandNameList.toArray(new String[sourceBandNameList.size()]);
 
         final Map<String, Object> parameterMap = new HashMap<String, Object>(2);
         parameterMap.put("sourceBands", sourceBandNames);
         parameterMap.put("targetBand", "cloud_product");
         parameterMap.put("applyThreshold", applyThreshold);
 
         return GPF.createProduct("chris.Accumulate", parameterMap, clusterMapProduct);
     }
 
     private Product createClusterProduct(Product featureProduct) {
         final int clusterCount = 14;
         final int iterationCount = 60;
         final boolean includeProbabilities = true;
         final List<String> sourceBandNameList = new ArrayList<String>(5);
         Collections.addAll(sourceBandNameList, "brightness_vis", "brightness_nir", "whiteness_vis", "whiteness_nir");
         if (featureProduct.getProductType().matches("CHRIS_M[15]_FEAT")) {
             sourceBandNameList.add("wv");
         }
         final String[] sourceBandNames = sourceBandNameList.toArray(new String[sourceBandNameList.size()]);
         final Comparator<EMCluster> clusterComparator = new Comparator<EMCluster>() {
             public int compare(EMCluster c1, EMCluster c2) {
                 return Double.compare(c2.getMean()[0], c1.getMean()[0]);
             }
         };
         final EMClusterOp clustersOp = new EMClusterOp(
                 featureProduct,
                 clusterCount,
                 iterationCount,
                 sourceBandNames,
                 includeProbabilities,
                 clusterComparator);
 
         return clustersOp.getTargetProduct();
     }
 
     private Product createClusterMapProduct(Product clusterProduct) {
         final Map<String, Object> emptyMap = Collections.emptyMap();
         return GPF.createProduct("chris.MakeClusterMap", emptyMap, clusterProduct);
     }
 
     private static Product createReflectanceProduct(Product radianceProduct) {
         final Map<String, Object> emptyMap = Collections.emptyMap();
         return GPF.createProduct("chris.ComputeReflectances",
                                  emptyMap,
                                  radianceProduct);
     }
 
     private static Product createFeatureProduct(Product reflectanceProduct) {
         final Map<String, Object> emptyMap = Collections.emptyMap();
         return GPF.createProduct("chris.ExtractFeatures",
                                  emptyMap,
                                  reflectanceProduct);
     }
 
     private static boolean isContained(int index, int[] indexes) {
         for (int i : indexes) {
             if (index == i) {
                 return true;
             }
         }
 
         return false;
     }
 
     public void disposeSourceProducts() {
         if (clusterMapProduct != null) {
             clusterMapProduct.dispose();
             clusterMapProduct = null;
         }
         if (clusterProduct != null) {
             clusterProduct.dispose();
             clusterProduct = null;
         }
         if (featureProduct != null) {
             featureProduct.dispose();
             featureProduct = null;
         }
         if (reflectanceProduct != null) {
             reflectanceProduct.dispose();
             reflectanceProduct = null;
         }
     }
 
     public ClusterProperties getClusterProperties() {
         final ClusterPropertiesExtractor propertiesExtractor = new ClusterPropertiesExtractor(featureProduct,
                                                                                               clusterMapProduct);
         try {
             return propertiesExtractor.extractClusterProperties(ProgressMonitor.NULL);
         } catch (IOException e) {
             throw new IllegalStateException("Could not extract cluster properties.", e);
         }
     }
 
     private static Band[] findBands(Product product, String prefix) {
         return findBands(product, prefix, new BandFilter() {
             @Override
             public boolean accept(Band band) {
                 return true;
             }
         });
     }
 
     private static Band[] findBands(Product product, String prefix, BandFilter filter) {
         final List<Band> bandList = new ArrayList<Band>();
 
         for (final Band band : product.getBands()) {
             if (band.getName().startsWith(prefix) && filter.accept(band)) {
                 bandList.add(band);
             }
         }
 
         return bandList.toArray(new Band[bandList.size()]);
     }
 }
