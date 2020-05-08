 package com.rockontrol.yaogan.service;
 
 import java.io.IOException;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.yaogan.gis.mgr.SimpleDataStoreManagerImpl;
 
 public class EcoFactorComputeServiceTest {
    private final EcoFactorComputeServiceImpl service = new EcoFactorComputeServiceImpl();
 
    @Before
    public void prepare() {
       System.setProperty("yaogan.gis.shapefile.home", "f:/shapeFile");
 
       service.setDataStoreManager(new SimpleDataStoreManagerImpl());
    }
 
    @Test
    public void testComputeAbioWholeRegion() throws IOException {
 
      double result = service.computeAbio("shapeFile\\地类\\2010/平朔_地类_2010.shp");
       System.out.println(result);
    }
 
    @Test
    public void testComputeAvegWholeRegion() throws IOException {
      double result = service.computeAveg("shapeFile\\地类\\2010/平朔_地类_2010.shp");
       System.out.println(result);
    }
 
    @Test
   public void testComputeAeroWholeRegion() {
      // double result = service.computeAsus(fractureFilePath, collapseFilePath,
      // boundaryFilePath, water_descrement)
    }
 }
