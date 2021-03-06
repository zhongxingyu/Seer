 package org.esa.beam.dataio.smos;
 
 import static junit.framework.Assert.*;
 import org.junit.Test;
 
 public class BandDescriptorRegistryTest {
 
     @Test
     public void getDescriptorsFor_DBL_SM_XXXX_AUX_ECMWF__0200() {
         final String formatName = "DBL_SM_XXXX_AUX_ECMWF__0200";
         final BandDescriptors descriptors = BandDescriptorRegistry.getInstance().getDescriptors(formatName);
         assertEquals(38, descriptors.asList().size());
 
         final BandDescriptor descriptor = descriptors.getDescriptor("RR");
         assertNotNull(descriptor);
 
         assertEquals("RR", descriptor.getBandName());
         assertEquals("Rain_Rate", descriptor.getMemberName());
         assertTrue(descriptor.hasTypicalMin());
         assertTrue(descriptor.hasTypicalMax());
         assertFalse(descriptor.isCyclic());
         assertTrue(descriptor.hasFillValue());
         assertFalse(descriptor.getValidPixelExpression().isEmpty());
         assertEquals("RR.raw != -99999.0 && RR.raw != -99998.0", descriptor.getValidPixelExpression());
         assertFalse(descriptor.getUnit().isEmpty());
         assertFalse(descriptor.getDescription().isEmpty());
         assertEquals(0.0, descriptor.getTypicalMin(), 0.0);
        assertEquals("m 3h-1", descriptor.getUnit());
 
         final FlagDescriptors flagDescriptors = descriptors.getDescriptor("F1").getFlagDescriptors();
         assertNotNull(flagDescriptors);
         assertSame(FlagDescriptorRegistry.getInstance().getDescriptors("DBL_SM_XXXX_AUX_ECMWF__0200_F1.txt"),
                    flagDescriptors);
 
         descriptor.getFlagDescriptors();
     }
 
 }
