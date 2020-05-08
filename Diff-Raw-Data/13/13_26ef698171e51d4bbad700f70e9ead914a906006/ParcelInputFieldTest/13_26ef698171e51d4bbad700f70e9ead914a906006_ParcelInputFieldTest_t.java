 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package de.cismet.cids.custom.wupp.client.alkis;
 
 import java.util.HashMap;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author mroncoroni
  */
 public class ParcelInputFieldTest {
 
     private ParcelInputField field;
     private InputConfig config;
 
     public ParcelInputFieldTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
         config = new InputConfig();
         config.setDelimiter1('-');
         config.setDelimiter2('/');
         config.setMaxLenTxtArea(4);
         config.setMaxLenTxtLand(3);
         config.setMaxLenTxtLandParcelNumerator(5);
         config.setMaxLenTxtLandParcelDenominator(4);
         HashMap<Integer, String> gemarkung = new HashMap<Integer, String>();
         gemarkung.put(3001, "Barmen");
         gemarkung.put(3135, "Elberfeld");
         gemarkung.put(3267, "Ronsdorf");
        gemarkung.put(3276, "Schöller");
         gemarkung.put(3277, "Vohwinkel");
        gemarkung.put(3278, "Dönberg");
         gemarkung.put(3279, "Cronenberg");
         gemarkung.put(3485, "Beyenburg");
         gemarkung.put(3486, "Langerfeld");
        gemarkung.put(3487, "Nächstebreck");
         config.setAreaClearMap(gemarkung);
         HashMap<String, Integer> umsetzung = new HashMap<String, Integer>();
         umsetzung.put("b", 3001);
         umsetzung.put("ba", 3001);
 
         umsetzung.put("e", 3135);
         umsetzung.put("el", 3135);
 
         umsetzung.put("r", 3267);
         umsetzung.put("ro", 3267);
 
         umsetzung.put("s", 3276);
         umsetzung.put("sc", 3276);
 
         umsetzung.put("v", 3277);
         umsetzung.put("vo", 3277);
 
         umsetzung.put("d", 3278);
         umsetzung.put("do", 3278);
        umsetzung.put("dö", 3278);
 
         umsetzung.put("c", 3279);
         umsetzung.put("cr", 3279);
 
         umsetzung.put("be", 3485);
 
         umsetzung.put("l", 3486);
         umsetzung.put("la", 3486);
 
         umsetzung.put("n", 3487);
         umsetzung.put("na", 3487);
        umsetzung.put("nä", 3487);
         config.setConversionMap(umsetzung);
         field = new ParcelInputField(config);
     }
 
     @After
     public void tearDown() {
         config = null;
         field = null;
     }
 
     /**
      * Test of setDistrictNumber method, of class ParcelInputField.
      */
     @Test
     public void testSetDistrictNumber() {
         System.out.println("setDistrictNumber");
         String districtNumber = "3001";
         ParcelInputField instance = field;
         instance.setDistrictNumber(districtNumber);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals(districtNumber, instance.getDistrictNumber());
     }
 
     @Test
     public void testSetDistrictNumberShort() {
         System.out.println("setDistrictNumber");
         String districtNumber = "30";
         ParcelInputField instance = field;
         instance.setDistrictNumber(districtNumber);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals(null, instance.getDistrictNumber());
     }
 
     @Test
     public void testSetDistrictNumberShort2() {
         System.out.println("setDistrictNumber");
         String districtNumber = "ba";
         ParcelInputField instance = field;
         instance.setDistrictNumber(districtNumber);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("3001", instance.getDistrictNumber());
     }
 
     @Test
     public void testSetDistrictNumberLong() {
         System.out.println("setDistrictNumber");
         String districtNumber = "30017435";
         ParcelInputField instance = field;
         instance.setDistrictNumber(districtNumber);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("3001", instance.getDistrictNumber());
     }
 
     @Test
     public void testSetDistrictNumberWrong() {
         System.out.println("setDistrictNumber");
         String districtNumber = "sadfh";
         ParcelInputField instance = field;
         instance.setDistrictNumber(districtNumber);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals(null, instance.getDistrictNumber());
     }
 
     /**
      * Test of setCurrentParcel method, of class ParcelInputField.
      */
     @Test
     public void testSetCurrentParcel() {
         System.out.println("setCurrentParcel");
         String currentParcel = "3135-002-00004/0003";
         ParcelInputField instance = field;
         instance.setCurrentParcel(currentParcel);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals(currentParcel, instance.getCurrentParcel());
     }
 
     @Test
     public void testSetCurrentParcelOverflow() {
         System.out.println("setCurrentParcel");
         String currentParcel = "3135-002-00004/00030";
         ParcelInputField instance = field;
         instance.setCurrentParcel(currentParcel);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("3135-002-00004/0003", instance.getCurrentParcel());
     }
 
     @Test
     public void testSetCurrentParcelDelimiterAtEnd() {
         System.out.println("setCurrentParcel");
         String currentParcel = "3135-002-00004/00030-";
         ParcelInputField instance = field;
         instance.setCurrentParcel(currentParcel);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("3135-002-00004/0003", instance.getCurrentParcel());
     }
 
     @Test
     public void testSetCurrentParcelShort() {
         System.out.println("setCurrentParcel");
         String currentParcel = "3135-002-04";
         ParcelInputField instance = field;
         instance.setCurrentParcel(currentParcel);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("3135-002-00004", instance.getCurrentParcel());
     }
 
     @Test
     public void testSetCurrentParcelShort2() {
         System.out.println("setCurrentParcel");
         String currentParcel = "ba-1-3";
         ParcelInputField instance = field;
         instance.setCurrentParcel(currentParcel);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("3001-001-00003", instance.getCurrentParcel());
     }
 
     /**
      * Test of setParcelDenominator method, of class ParcelInputField.
      */
     @Test
     public void testSetParcelDenominator() {
         System.out.println("setParcelDenominator");
         String parcelDenominator = "0002";
         ParcelInputField instance = field;
         instance.setParcelDenominator(parcelDenominator);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("0002", instance.getParcelDenominator());
     }
 
     @Test
     public void testSetParcelDenominatorShort() {
         System.out.println("setParcelDenominator");
         String parcelDenominator = "2";
         ParcelInputField instance = field;
         instance.setParcelDenominator(parcelDenominator);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("0002", instance.getParcelDenominator());
     }
 
     @Test
     public void testSetParcelDenominatorLong() {
         System.out.println("setParcelDenominator");
         String parcelDenominator = "00020005";
         ParcelInputField instance = field;
         instance.setParcelDenominator(parcelDenominator);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("0002", instance.getParcelDenominator());
     }
 
     @Test
     public void testSetParcelDenominatorWrong() {
         System.out.println("setParcelDenominator");
         String parcelDenominator = "abced";
         ParcelInputField instance = field;
         instance.setParcelDenominator(parcelDenominator);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("", instance.getParcelDenominator());
     }
 
     /**
      * Test of setParcelNumber method, of class ParcelInputField.
      */
     @Test
     public void testSetParcelNumber() {
         System.out.println("setParcelNumber");
         String parcelNumber = "001";
         ParcelInputField instance = field;
         instance.setParcelNumber(parcelNumber);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals(parcelNumber, instance.getParcelNumber());
     }
 
     @Test
     public void testSetParcelNumberShort() {
         System.out.println("setParcelNumber");
         String parcelNumber = "1";
         ParcelInputField instance = field;
         instance.setParcelNumber(parcelNumber);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("001", instance.getParcelNumber());
     }
 
     @Test
     public void testSetParcelNumberShortLong() {
         System.out.println("setParcelNumber");
         String parcelNumber = "10002";
         ParcelInputField instance = field;
         instance.setParcelNumber(parcelNumber);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("100", instance.getParcelNumber());
     }
 
     @Test
     public void testSetParcelNumberShortWrong() {
         System.out.println("setParcelNumber");
         String parcelNumber = "abc";
         ParcelInputField instance = field;
         instance.setParcelNumber(parcelNumber);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("000", instance.getParcelNumber());
     }
 
     /**
      * Test of setParcelNumerator method, of class ParcelInputField.
      */
     @Test
     public void testSetParcelNumerator() {
         System.out.println("setParcelNumerator");
         String parcelNumerator = "00003";
         ParcelInputField instance = field;
         instance.setParcelNumerator(parcelNumerator);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals(parcelNumerator, instance.getParcelNumerator());
     }
 
     @Test
     public void testSetParcelNumeratorShort() {
         System.out.println("setParcelNumerator");
         String parcelNumerator = "3";
         ParcelInputField instance = field;
         instance.setParcelNumerator(parcelNumerator);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("00003", instance.getParcelNumerator());
     }
 
     @Test
     public void testSetParcelNumeratorLong() {
         System.out.println("setParcelNumerator");
         String parcelNumerator = "00003674";
         ParcelInputField instance = field;
         instance.setParcelNumerator(parcelNumerator);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("00003", instance.getParcelNumerator());
     }
 
     @Test
     public void testSetParcelNumeratorWrong() {
         System.out.println("setParcelNumerator");
         String parcelNumerator = "abdvr";
         ParcelInputField instance = field;
         instance.setParcelNumerator(parcelNumerator);
         // TODO review the generated test code and remove the default call to fail.
         assertEquals("00000", instance.getParcelNumerator());
     }
 }
