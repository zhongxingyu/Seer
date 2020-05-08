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
 package org.esa.beam.chris.operators;
 
 import org.esa.beam.chris.util.OpUtils;
 import org.esa.beam.framework.datamodel.Band;
 import org.esa.beam.framework.datamodel.IndexCoding;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.datamodel.ProductData;
 import org.esa.beam.framework.gpf.Operator;
 import org.esa.beam.framework.gpf.OperatorException;
 import org.esa.beam.framework.gpf.OperatorSpi;
 import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
 import org.esa.beam.framework.gpf.annotations.SourceProduct;
 import org.esa.beam.framework.gpf.annotations.TargetProduct;
 import org.esa.beam.util.jai.RasterDataNodeOpImage;
 
 import javax.media.jai.ImageLayout;
 import java.awt.image.RenderedImage;
 
 /**
  * todo - API doc
  *
  * @author Ralf Quast
  * @version $Revision$ $Date$
  */
 @OperatorMetadata(alias = "chris.MakeClusterMap",
                   version = "1.0",
                   authors = "Ralf Quast",
                   copyright = "(c) 2008 by Brockmann Consult",
                   description = "Makes the cluster membership map for clusters of features extracted from TOA reflectances.")
 public class MakeClusterMapOp extends Operator {
 
     @SourceProduct(alias = "source")
     private Product sourceProduct;
     @TargetProduct
     private Product targetProduct;
 
     private Band clusterMapBand;
 
     @Override
     public void initialize() throws OperatorException {
         int width = sourceProduct.getSceneRasterWidth();
         int height = sourceProduct.getSceneRasterHeight();
         targetProduct = new Product(sourceProduct.getName(), sourceProduct.getProductType(), width, height);
 
         try {
             final Band[] sourceBands = OpUtils.findBands(sourceProduct, "probability");
             final Band[] probabilityBands = new Band[sourceBands.length];
             for (int i = 0; i < sourceBands.length; ++i) {
                 final Band sourceBand = sourceBands[i];
 
                 Band targetBand = new ImageBand(sourceBand.getName(), sourceBand.getDataType(), width, height);
                 targetBand.setDescription(sourceBand.getDescription());
                 targetProduct.addBand(targetBand);
                 probabilityBands[i] = targetBand;
                 ImageLayout imageLayout = RasterDataNodeOpImage.createSingleBandedImageLayout(targetBand);
                 RenderedImage image = ClusterProbabilityOpImage.create(imageLayout,
                                                                        sourceBands, i,
                                                                        new int[0]);
                 targetBand.setImage(image);
             }
             clusterMapBand = new ImageBand("class_indices", ProductData.TYPE_INT16, width, height);
             clusterMapBand.setDescription("Class_indices");
             targetProduct.addBand(clusterMapBand);
 
             final IndexCoding indexCoding = new IndexCoding("Cluster_classes");
             for (int i = 0; i < sourceBands.length; i++) {
                 indexCoding.addIndex("class_" + (i + 1), i, "Cluster label");
             }
             targetProduct.getIndexCodingGroup().add(indexCoding);
             clusterMapBand.setSampleCoding(indexCoding);
             ImageLayout imageLayout = RasterDataNodeOpImage.createSingleBandedImageLayout(clusterMapBand);
             clusterMapBand.setImage(ClusterMapOpImage.create(imageLayout, probabilityBands));
         } catch (Throwable e) {
             throw new OperatorException(e);
         }
 
         setTargetProduct(targetProduct);
     }
 
     public static class Spi extends OperatorSpi {
 
         public Spi() {
             super(MakeClusterMapOp.class);
         }
     }
 }
