 /********************************************************************************
  * Copyright (c) 2009 Regents of the University of Minnesota
  *
  * This Software was written at the Minnesota Supercomputing Institute
  * http://msi.umn.edu
  *
  * All rights reserved. The following statement of license applies
  * only to this file, and and not to the other files distributed with it
  * or derived therefrom.  This file is made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Minnesota Supercomputing Institute - initial API and implementation
  *******************************************************************************/
 
 package edu.umn.msi.tropix.proteomics.conversion.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.math.util.MathUtils;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.google.common.collect.Lists;
 
 import edu.umn.msi.tropix.common.io.FileUtils;
 import edu.umn.msi.tropix.common.io.FileUtilsFactory;
 import edu.umn.msi.tropix.common.io.IOUtils;
 import edu.umn.msi.tropix.common.io.IOUtilsFactory;
 import edu.umn.msi.tropix.proteomics.conversion.MzXMLToMGFConverter.MgfConversionOptions;
 import edu.umn.msi.tropix.proteomics.conversion.Scan;
 import edu.umn.msi.tropix.proteomics.test.ProteomicsTests;
 import edu.umn.msi.tropix.proteomics.test.VerifyUtils;
 
 public class MzXMLToMGFConverterStreamingImplTest {
   private static final FileUtils FILE_UTILS = FileUtilsFactory.getInstance();
   private static final IOUtils IO_UTILS = IOUtilsFactory.getInstance();
 
   private File destination;
 
   @BeforeMethod(groups = "unit")
   public void setupDestination() {
     setDestination(FILE_UTILS.createTempFile());
   }
 
   @AfterMethod(groups = "unit")
   public void deleteDestination() {
     FILE_UTILS.deleteQuietly(getDestination());
   }
 
   @Test(groups = "unit")
   public void testMzXMLToMFG() throws IOException {
     convertResource("validMzXML.mzxml", null);
     final FileInputStream desinationInputStream = FILE_UTILS.getFileInputStream(getDestination());
     try {
       VerifyUtils.verifyMGF(desinationInputStream);
     } finally {
       IO_UTILS.closeQuietly(desinationInputStream);
     }
   }
 
   @Test(groups = "unit")
   public void testMrr() throws Exception {
     convertResource("readw.mzXML", null);
     final Iterator<Scan> scanIter = loadConvertedScans();
     final List<Scan> num78Scans = Lists.newArrayList();
     Scan scan1993 = null;
     while(scanIter.hasNext()) {
       final Scan scan = scanIter.next();
       int scanNumber = scan.getNumber();
       if(scanNumber > 1993) {
         break;
       } else if(scanNumber == 78) {
         num78Scans.add(scan);
       } else if(scanNumber == 1993) {
         assert scan1993 == null; // Make sure it doesn't get set twice.
         scan1993 = scan;
       }
     }
     assert num78Scans.size() == 2 : num78Scans.size();
     final Scan scan1 = num78Scans.get(0), scan2 = num78Scans.get(1);
     assert scan1.getAlt() == 78;
     assert scan1.getMsLevel() == 2;
    assert MathUtils.equals(scan1.getPrecursorMz(), 592.810000f, 0.0001) : scan1.getPrecursorMz();
    assert MathUtils.equals(scan1.getPrecursorIntensity(), 1012.2700f, 0.0001) : scan1.getPrecursorIntensity();
     assert scan1.getPrecursorCharge() == 2;
     assert scan2.getPrecursorCharge() == 3;
     assert MathUtils.equals(scan1.getPeaks(), scan2.getPeaks());
     assert MathUtils.equals(scan1.getPeaks()[0], 355.107788d) : scan1.getPeaks()[0];
   }
 
   private Iterator<Scan> loadConvertedScans() {
     final MgfParser mgfParser = new MgfParser();
     final Iterator<Scan> scanIter = mgfParser.parserMgf(FILE_UTILS.getFileInputStream(getDestination()));
     return scanIter;
   }
 
   private void convertResource(final String resourceName, final MgfConversionOptions options) {
     final InputStream mzxml = ProteomicsTests.getResourceAsStream(resourceName);
     final MzXMLToMGFConverterStreamingImpl converter = new MzXMLToMGFConverterStreamingImpl();
     final FileOutputStream destinationOutputStream = FILE_UTILS.getFileOutputStream(getDestination());
     try {
       converter.mzxmlToMGF(mzxml, destinationOutputStream, options);
     } finally {
       IO_UTILS.closeQuietly(destinationOutputStream);
     }
   }
 
   public void setDestination(final File destination) {
     this.destination = destination;
   }
 
   public File getDestination() {
     return destination;
   }
 
 }
