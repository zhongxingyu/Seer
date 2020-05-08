 package hudson.plugins.analysis.test;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import hudson.plugins.analysis.core.AbstractHealthDescriptor;
 import hudson.plugins.analysis.core.HealthDescriptor;
 
 import org.junit.Test;
 
 /**
  * Abstract test case for {@link AbstractHealthDescriptor}.
  *
  * @author Ulli Hafner
  */
 public abstract class AbstractHealthDescriptorTest extends AbstractEnglishLocaleTest {
     /** Error message. */
    protected static final String WRONG_DESCRIPTION = "Wrong description";
    /** Error message. */
     private static final String WRONG_MINIMUM_ANNOTATIONS = "Wrong minimum annotations";
     /** Error message. */
     private static final String WRONG_VALUE_OF_IS_HEALTHY_REPORT_ENABLED = "Wrong value of isHealthyReportEnabled";
     /** Error message. */
     private static final String WRONG_VALUE_OF_IS_THRESHOLD_ENABLED = "Wrong value of isThresholdEnabled";
 
     /**
      * Tests the method {@link AbstractHealthDescriptor#isThresholdEnabled()}.
      */
     @Test
     public void testThresholds() {
         assertTrue(WRONG_VALUE_OF_IS_THRESHOLD_ENABLED, createHealthDescriptor("0", "", "").isThresholdEnabled());
         assertTrue(WRONG_VALUE_OF_IS_THRESHOLD_ENABLED, createHealthDescriptor("1", "", "").isThresholdEnabled());
         assertTrue(WRONG_VALUE_OF_IS_THRESHOLD_ENABLED, createHealthDescriptor("100", "", "").isThresholdEnabled());
 
         assertFalse(WRONG_VALUE_OF_IS_THRESHOLD_ENABLED, createHealthDescriptor("-1", "", "").isThresholdEnabled());
         assertFalse(WRONG_VALUE_OF_IS_THRESHOLD_ENABLED, createHealthDescriptor("", "", "").isThresholdEnabled());
         assertFalse(WRONG_VALUE_OF_IS_THRESHOLD_ENABLED, createHealthDescriptor(null, "", "").isThresholdEnabled());
     }
 
     /**
      * Tests the method {@link AbstractHealthDescriptor#isThresholdEnabled()}.
      */
     @Test
     public void testHealthyThresholds() {
         assertTrue(WRONG_VALUE_OF_IS_HEALTHY_REPORT_ENABLED, createHealthDescriptor("", "0", "1").isHealthyReportEnabled());
         assertTrue(WRONG_VALUE_OF_IS_HEALTHY_REPORT_ENABLED, createHealthDescriptor("", "1", "2").isHealthyReportEnabled());
         assertTrue(WRONG_VALUE_OF_IS_HEALTHY_REPORT_ENABLED, createHealthDescriptor("", "10", "20").isHealthyReportEnabled());
 
         assertFalse(WRONG_VALUE_OF_IS_HEALTHY_REPORT_ENABLED, createHealthDescriptor("", "0", "0").isHealthyReportEnabled());
         assertFalse(WRONG_VALUE_OF_IS_HEALTHY_REPORT_ENABLED, createHealthDescriptor("", "1", "1").isThresholdEnabled());
         assertFalse(WRONG_VALUE_OF_IS_HEALTHY_REPORT_ENABLED, createHealthDescriptor("", "2", "1").isThresholdEnabled());
         assertFalse(WRONG_VALUE_OF_IS_HEALTHY_REPORT_ENABLED, createHealthDescriptor("", "2", "").isThresholdEnabled());
         assertFalse(WRONG_VALUE_OF_IS_HEALTHY_REPORT_ENABLED, createHealthDescriptor("", "", "2").isThresholdEnabled());
         assertFalse(WRONG_VALUE_OF_IS_HEALTHY_REPORT_ENABLED, createHealthDescriptor("", null, "2").isThresholdEnabled());
     }
 
     /**
      * Tests the method {@link AbstractHealthDescriptor#getMinimumAnnotations}.
      */
     @Test
     public void testConversionOfThresholds() {
         assertEquals(WRONG_MINIMUM_ANNOTATIONS, 0, createHealthDescriptor("0", "", "").getMinimumAnnotations());
         assertEquals(WRONG_MINIMUM_ANNOTATIONS, 1, createHealthDescriptor("1", "", "").getMinimumAnnotations());
         assertEquals(WRONG_MINIMUM_ANNOTATIONS, 100, createHealthDescriptor("100", "", "").getMinimumAnnotations());
     }
 
 
     /**
      * Tests the method {@link AbstractHealthDescriptor#getHealthyAnnotations()}
      * and {@link AbstractHealthDescriptor#getUnHealthyAnnotations()}.
      */
     @Test
     public void testConversionOfHealthiness() {
         assertEquals("Wrong healthy annotations", 1, createHealthDescriptor("0", "1", "2").getHealthyAnnotations());
         assertEquals("Wrong unhealthy annotations", 2, createHealthDescriptor("0", "1", "2").getUnHealthyAnnotations());
     }
 
     /**
      * Tests the method {@link AbstractHealthDescriptor#getHealthyAnnotations()}.
      */
     @Test(expected = IllegalArgumentException.class)
     public void verifyContractOfHealthy() {
         createHealthDescriptor("0", "-1", "0").getHealthyAnnotations();
     }
 
     /**
      * Tests the method {@link AbstractHealthDescriptor#getHealthyAnnotations()}.
      */
     @Test(expected = IllegalArgumentException.class)
     public void verifyContractOfUnHealthy() {
         createHealthDescriptor("0", "0", "-1").getUnHealthyAnnotations();
     }
 
     /**
      * Create a health descriptor mock that should be used as a basis for the
      * concrete {@link AbstractHealthDescriptor} sub type.
      *
      * @param threshold
      *            Annotations threshold to be reached if a build should be
      *            considered as unstable.
      * @param healthy
      *            Report health as 100% when the number of open tasks is less
      *            than this value
      * @param unHealthy
      *            Report health as 0% when the number of open tasks is greater
      *            than this value
      * @return the descriptor under test
      */
     private AbstractHealthDescriptor createHealthDescriptor(final String threshold, final String healthy, final String unHealthy) {
         HealthDescriptor healthDescriptor = mock(HealthDescriptor.class);
         when(healthDescriptor.getThreshold()).thenReturn(threshold);
         when(healthDescriptor.getHealthy()).thenReturn(healthy);
         when(healthDescriptor.getUnHealthy()).thenReturn(unHealthy);
 
         return createHealthDescriptor(healthDescriptor);
     }
 
     /**
      * Factory method to create the health descriptor under test.
      *
      * @param healthDescriptor
      *            the basis health descriptor mock
      * @return the descriptor under test
      */
     protected abstract AbstractHealthDescriptor createHealthDescriptor(HealthDescriptor healthDescriptor);
 }
 
