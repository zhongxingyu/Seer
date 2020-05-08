 package edu.umn.msi.tropix.proteomics.conversion.impl;
 
 import java.util.Iterator;
 
import org.apache.commons.math.util.MathUtils;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import edu.umn.msi.tropix.proteomics.conversion.Scan;
 import edu.umn.msi.tropix.proteomics.test.ProteomicsTests;
 
 public class MzmlScanIteratorTest {
 
   @Test(groups = "unit")
   public void testSimpleParsing() {
     final Iterator<Scan> iterator = new XmlPeakListParserImpl().parse(ProteomicsTests.getResourceAsStream("test.mzml"));
     final Scan scan1 = iterator.next();
     assert scan1.getMsLevel() == 1;
     Assert.assertEquals(scan1.getRt(), 449L);
     final Scan scan2 = iterator.next();
     assert scan2.getParentFileName().equals("arria002_hende433_083012_12169_iTQ_101101");
     assert scan2.getMsLevel() == 2;
    assert MathUtils.equals(scan2.getPrecursorMz(), 403.258544921875f, 0.0001);
     assert scan2.getPrecursorCharge() == 2;
     assert scan2.getNumber() == 1636;
     assert scan2.getIndex() == 1635;
   }
 
 }
