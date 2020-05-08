 package com.mymed.tests.unit.bean;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.mymed.model.data.user.MPositionBean;
 
 public class MPositionBeanTest {
 
   private static MPositionBean positionBean;
   private static MPositionBean nullActual = null;
 
   @BeforeClass
  public static void setUpBeforeClass() {
     positionBean = new MPositionBean();
     positionBean.setUserID("USER_ID");
     positionBean.setCity("CITY");
     positionBean.setCountry("COUNTRY");
     positionBean.setLatitude("LATITUDE");
     positionBean.setLongitude("LONGITUDE");
     positionBean.setZipCode("06600");
     positionBean.setFormattedAddress("ADDRESS");
   }
 
   @AfterClass
  public static void tearDownAfterClass() {
     positionBean = null; // NOPMD
   }
 
   @Test
   public void attributeToMapTest() {
     try {
       final Map<String, byte[]> attributeMap = positionBean.getAttributeToMap();
       assertEquals(7, attributeMap.size());
     } catch (final Exception ex) {
       fail(ex.getMessage());
     }
   }
 
   @Test
   public void notEqualsTest() {
     final MPositionBean actual = new MPositionBean();
     actual.setUserID("NEW_USER_ID");
     actual.setCity("NEW_CITY");
     actual.setCountry("NEW_COUNTRY");
     actual.setLatitude("NEW_LATITUDE");
     actual.setLongitude("NEW_LONGITUDE");
     actual.setZipCode("06000");
 
     assertFalse("The beans are the same", positionBean.equals(actual));
   }
 
   @Test
   public void nullEqualsTest() {
     assertFalse("The beans are the same", positionBean.equals(nullActual)); // NOPMD
   }
 
   @Test
   public void cloneEqualsTest() {
     final MPositionBean actual = positionBean.clone();
     assertEquals("The beans are not the same", positionBean, actual);
   }
 }
