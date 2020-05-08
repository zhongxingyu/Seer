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
 package org.esa.beam.chris.ui;
 
 import com.bc.ceres.core.ProgressMonitor;
 import com.bc.ceres.core.SubProgressMonitor;
 import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
 import org.esa.beam.framework.dataio.ProductIO;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.gpf.GPF;
 import org.esa.beam.framework.gpf.OperatorException;
 import org.esa.beam.framework.ui.AppContext;
import org.esa.beam.gpf.operators.standard.WriteOp;
 import org.esa.beam.util.io.FileUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 /**
  * Noise reduction swing worker.
  * <p/>
  * Performs the noise reduction for CHRIS images. Note that each source product
  * is disposed as soon as possible in order to save memory.
  *
  * @author Ralf Quast
  * @version $Revision$ $Date$
  */
 class NoiseReductionSwingWorker extends ProgressMonitorSwingWorker<Object, Product> {
 
     private final Map<Product, File> sourceProductTargetFileMap;
     private final Product[] destripingFactorsSourceProducts;
 
     private final Map<String, Object> destripingFactorsParameterMap;
     private final Map<String, Object> dropoutCorrectionParameterMap;
 
     private final File destripingFactorsTargetFile;
     private final String targetFormatName;
 
     private final boolean addTargetProductsToAppContext;
     private final AppContext appContext;
 
     /**
      * Creates a new instance of this class.
      *
      * @param sourceProductTargetFileMap    the mapping of source products onto target
      *                                      files for the destriped and dropout-corrected
      *                                      products.
      * @param destripingFactorsSourceProducts
      *                                      the source products used for calculating
      *                                      the destriping factors.
      * @param destripingFactorsParameterMap the parameter map used for calculating the
      *                                      destriping factors.
      * @param dropoutCorrectionParameterMap the parameter map used for calculating the
      *                                      dropout correction.
      * @param destripingFactorsTargetFile   the target file for storing the destriping
      *                                      factors.
      * @param targetFileFormat              the target fle format.
      * @param appContext                    the application context.
      * @param addTargetProductsToAppContext {@code true} when the target products should
      *                                      be added to the application context.
      */
     public NoiseReductionSwingWorker(Map<Product, File> sourceProductTargetFileMap,
                                      Product[] destripingFactorsSourceProducts,
                                      Map<String, Object> destripingFactorsParameterMap,
                                      Map<String, Object> dropoutCorrectionParameterMap,
                                      File destripingFactorsTargetFile,
                                      String targetFileFormat,
                                      AppContext appContext,
                                      boolean addTargetProductsToAppContext) {
         super(appContext.getApplicationWindow(), "Noise Reduction");
 
         this.sourceProductTargetFileMap = sourceProductTargetFileMap;
         this.destripingFactorsSourceProducts = destripingFactorsSourceProducts;
 
         this.destripingFactorsParameterMap = destripingFactorsParameterMap;
         this.dropoutCorrectionParameterMap = dropoutCorrectionParameterMap;
 
         this.destripingFactorsTargetFile = destripingFactorsTargetFile;
         this.targetFormatName = targetFileFormat;
 
         this.appContext = appContext;
         this.addTargetProductsToAppContext = addTargetProductsToAppContext;
     }
 
     @Override
     protected Object doInBackground(ProgressMonitor pm) throws Exception {
         Product destripingFactorsProduct = null;
 
         try {
             pm.beginTask("Performing noise reduction...", 50 + sourceProductTargetFileMap.size() * 10);
             destripingFactorsProduct = GPF.createProduct("chris.ComputeDestripingFactors",
                                                          destripingFactorsParameterMap,
                                                          destripingFactorsSourceProducts);
 
             try {
                 WriteOp.writeProduct(destripingFactorsProduct, destripingFactorsTargetFile, targetFormatName,
                                      new SubProgressMonitor(pm, 50));
             } finally {
                 destripingFactorsProduct.dispose();
                 for (final Product sourceProduct : destripingFactorsSourceProducts) {
                     if (!sourceProductTargetFileMap.keySet().contains(sourceProduct)) {
                         disposeSourceProductIfNotUsedInAppContext(sourceProduct);
                     }
                 }
             }
 
             try {
                 destripingFactorsProduct = ProductIO.readProduct(destripingFactorsTargetFile);
             } catch (IOException e) {
                 throw new OperatorException(MessageFormat.format(
                         "Cannot read file ''{0}''.", destripingFactorsTargetFile), e);
             }
 
             for (final Map.Entry<Product, File> entry : sourceProductTargetFileMap.entrySet()) {
                 final Product sourceProduct = entry.getKey();
                 final File targetFile = entry.getValue();
 
                 performNoiseReduction(sourceProduct, destripingFactorsProduct, targetFile,
                                       new SubProgressMonitor(pm, 10));
             }
         } finally {
             if (destripingFactorsProduct != null) {
                 destripingFactorsProduct.dispose();
             }
             pm.done();
         }
 
         return null;
     }
 
     @Override
     protected void process(List<Product> products) {
         if (addTargetProductsToAppContext) {
             for (Product product : products) {
                 appContext.getProductManager().addProduct(product);
             }
         }
     }
 
     @Override
     protected void done() {
         try {
             get();
         } catch (InterruptedException e) {
             // ignore
         } catch (ExecutionException e) {
             appContext.handleError(e.getMessage(), e.getCause());
         }
     }
 
     private void performNoiseReduction(Product sourceProduct,
                                        Product destripingFactorsProduct,
                                        File targetFile,
                                        ProgressMonitor pm) throws IOException {
         final HashMap<String, Product> sourceProductMap = new HashMap<String, Product>(5);
         sourceProductMap.put("sourceProduct", sourceProduct);
         sourceProductMap.put("factorProduct", destripingFactorsProduct);
 
         Product destripedProduct = null;
 
         try {
             destripedProduct = GPF.createProduct("chris.ApplyDestripingFactors",
                                                  new HashMap<String, Object>(0),
                                                  sourceProductMap);
             final Product dropoutCorrectedProduct = GPF.createProduct("chris.CorrectDropouts",
                                                                       dropoutCorrectionParameterMap,
                                                                       destripedProduct);
 
             dropoutCorrectedProduct.setName(FileUtils.getFilenameWithoutExtension(targetFile));
             writeProduct(dropoutCorrectedProduct, targetFile, addTargetProductsToAppContext, pm);
         } finally {
             if (destripedProduct != null) {
                 destripedProduct.dispose();
             }
             disposeSourceProductIfNotUsedInAppContext(sourceProduct);
         }
     }
 
     private void writeProduct(final Product targetProduct, final File targetFile, boolean openInApp,
                               ProgressMonitor pm) throws IOException {
         try {
             pm.beginTask("Writing " + targetProduct.getName() + "...", openInApp ? 100 : 95);
 
             try {
                 WriteOp.writeProduct(targetProduct, targetFile, targetFormatName, new SubProgressMonitor(pm, 95));
             } finally {
                 targetProduct.dispose();
             }
 
             if (openInApp) {
                 final Product product = ProductIO.readProduct(targetFile);
                 if (product != null) {
                     publish(product);
                 }
                 pm.worked(5);
             }
         } finally {
             pm.done();
         }
     }
 
     private void disposeSourceProductIfNotUsedInAppContext(Product sourceProduct) {
         boolean dispose = true;
         for (final Product product : appContext.getProductManager().getProducts()) {
             if (sourceProduct == product) {
                 dispose = false;
                 break;
             }
         }
         if (dispose) {
             sourceProduct.dispose();
         }
     }
 }
