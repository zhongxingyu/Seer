 package com.nedap.healthcare.kadasterbagclient.api.model;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import com.nedap.healthcare.kadasterbagclient.api.AbstractSpringTest;
 import com.nedap.healthcare.kadasterbagclient.api.util.DateTimeUtil;
 
 public class AddressTest extends AbstractSpringTest {
 
     @Test
     public void testEquals() {
         final String countryCode = "countryCode";
         final String postalCode = "postalCode";
         final String city = "city";
         final String street = "street";
         final Integer number = 1;
         final String numberPostfix = "numberPostfix";
         final String latitude = "latitude";
         final String longitude = "longitude";
 
         final DateTime validFrom = DateTimeUtil.parse("20120512120000");
         final DateTime validTo = DateTimeUtil.parse("20120912120000");
         final DateTime creationDate = DateTime.now();
 
         final Address a1 = new Address();
         a1.setCity(city);
         a1.setCountryCode(countryCode);
         a1.setCreationDate(creationDate);
         a1.setId(1L);
         a1.setLatitude(latitude);
         a1.setLongitude(longitude);
         a1.setNumber(number);
         a1.setNumberPostfix(numberPostfix);
         a1.setPostalCode(postalCode);
         a1.setStreet(street);
         a1.setValidFrom(validFrom);
         a1.setValidTo(validTo);
 
         final Address a2 = new Address();
         a2.setCity(city);
         a2.setCountryCode(countryCode);
         a2.setCreationDate(creationDate);
         a2.setId(1L);
         a2.setLatitude(latitude);
         a2.setLongitude(longitude);
         a2.setNumber(number);
         a2.setNumberPostfix(numberPostfix);
         a2.setPostalCode(postalCode);
         a2.setStreet(street);
         a2.setValidFrom(validFrom);
         a2.setValidTo(validTo);
 
         takeSnapshot();
 
         // asserting
         assertTrue(!a1.equals(new Object()));
         assertTrue(a1.equals(a1));
         assertTrue(a1.equals(a2));
 
         a2.setCity("wrong");
         assertTrue(!a1.equals(a2));
         a2.setCity(null);
         assertTrue(!a1.equals(a2));
         a1.setCity(null);
         assertTrue(a1.equals(a2));
         a2.setCity(city);
         assertTrue(!a1.equals(a2));
         a1.setCity(city);
 
         a2.setCountryCode("wrong");
         assertTrue(!a1.equals(a2));
         a2.setCountryCode(null);
         assertTrue(!a1.equals(a2));
         a1.setCountryCode(null);
         assertTrue(a1.equals(a2));
         a2.setCountryCode(countryCode);
         assertTrue(!a1.equals(a2));
         a1.setCountryCode(countryCode);
 
        a2.setCreationDate(DateTime.now());
         assertTrue(!a1.equals(a2));
         a2.setCreationDate(null);
         assertTrue(!a1.equals(a2));
         a1.setCreationDate(null);
         assertTrue(a1.equals(a2));
         a2.setCreationDate(creationDate);
         assertTrue(!a1.equals(a2));
         a1.setCreationDate(creationDate);
 
         a2.setId(2L);
         assertTrue(!a1.equals(a2));
         a2.setId(null);
         assertTrue(!a1.equals(a2));
         a1.setId(null);
         assertTrue(a1.equals(a2));
         a2.setId(1L);
         assertTrue(!a1.equals(a2));
         a1.setId(1L);
 
         a2.setLatitude("wrong");
         assertTrue(!a1.equals(a2));
         a2.setLatitude(null);
         assertTrue(!a1.equals(a2));
         a1.setLatitude(null);
         assertTrue(a1.equals(a2));
         a2.setLatitude(latitude);
         assertTrue(!a1.equals(a2));
         a1.setLatitude(latitude);
 
         a2.setLongitude("wrong");
         assertTrue(!a1.equals(a2));
         a2.setLongitude(null);
         assertTrue(!a1.equals(a2));
         a1.setLongitude(null);
         assertTrue(a1.equals(a2));
         a2.setLongitude(longitude);
         assertTrue(!a1.equals(a2));
         a1.setLongitude(longitude);
 
         a2.setNumber(6);
         assertTrue(!a1.equals(a2));
         a2.setNumber(null);
         assertTrue(!a1.equals(a2));
         a1.setNumber(null);
         assertTrue(a1.equals(a2));
         a2.setNumber(number);
         assertTrue(!a1.equals(a2));
         a1.setNumber(number);
 
         a2.setNumberPostfix("wrong");
         assertTrue(!a1.equals(a2));
         a2.setNumberPostfix(null);
         assertTrue(!a1.equals(a2));
         a1.setNumberPostfix(null);
         assertTrue(a1.equals(a2));
         a2.setNumberPostfix(numberPostfix);
         assertTrue(!a1.equals(a2));
         a1.setNumberPostfix(numberPostfix);
 
         a2.setPostalCode("wrong");
         assertTrue(!a1.equals(a2));
         a2.setPostalCode(null);
         assertTrue(!a1.equals(a2));
         a1.setPostalCode(null);
         assertTrue(a1.equals(a2));
         a2.setPostalCode(postalCode);
         assertTrue(!a1.equals(a2));
         a1.setPostalCode(postalCode);
 
         a2.setStreet("wrong");
         assertTrue(!a1.equals(a2));
         a2.setStreet(null);
         assertTrue(!a1.equals(a2));
         a1.setStreet(null);
         assertTrue(a1.equals(a2));
         a2.setStreet(street);
         assertTrue(!a1.equals(a2));
         a1.setStreet(street);
 
         a2.setValidFrom(DateTimeUtil.parse("00000101120000"));
         assertTrue(!a1.equals(a2));
         a2.setValidFrom(null);
         assertTrue(!a1.equals(a2));
         a1.setValidFrom(null);
         assertTrue(a1.equals(a2));
         a2.setValidFrom(validFrom);
         assertTrue(!a1.equals(a2));
         a1.setValidFrom(validFrom);
 
         a2.setValidTo(DateTimeUtil.parse("00000101120000"));
         assertTrue(!a1.equals(a2));
         a2.setValidTo(null);
         assertTrue(!a1.equals(a2));
         a1.setValidTo(null);
         assertTrue(a1.equals(a2));
         a2.setValidTo(validTo);
         assertTrue(!a1.equals(a2));
         a1.setValidTo(validTo);
 
     }
 
     @Test
     public void testConstructorWithParams() {
         // data preparation
         final String countryCode = "countryCode";
         final String postalCode = "postalCode";
         final String city = "city";
         final String street = "street";
         final Integer number = 1;
         final String numberPostfix = "numberPostfix";
         final String latitude = "latitude";
         final String longitude = "longitude";
 
         final DateTime validFrom = DateTimeUtil.parse("20120512120000");
         final DateTime validTo = DateTimeUtil.parse("20120912120000");
         final DateTime creationDate = DateTime.now();
 
         final Address a1 = new Address();
         a1.setCity(city);
         a1.setCountryCode(countryCode);
         a1.setCreationDate(creationDate);
         a1.setLatitude(latitude);
         a1.setLongitude(longitude);
         a1.setNumber(number);
         a1.setNumberPostfix(numberPostfix);
         a1.setPostalCode(postalCode);
         a1.setStreet(street);
         a1.setValidFrom(validFrom);
         a1.setValidTo(validTo);
 
         // call method
         final Address a2 = new Address(countryCode, postalCode, city, street, number, numberPostfix, latitude,
                 longitude, validFrom, validTo, creationDate);
 
         // asserting
         assertTrue(a1.equals(a2));
     }
 }
