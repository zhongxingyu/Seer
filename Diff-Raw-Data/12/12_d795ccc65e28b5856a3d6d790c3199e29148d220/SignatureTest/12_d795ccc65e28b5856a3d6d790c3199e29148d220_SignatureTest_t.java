 package com.google.code.geocoder;
 
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.security.InvalidKeyException;
 
 public class SignatureTest extends Geocoder {
     protected static Geocoder geocoder;
 
     @BeforeClass
     public static void setUp() throws InvalidKeyException {
         geocoder = new Geocoder("clientID", "vNIXE0xscrmjlyV-12Nj_BvUPaw=");
     }
 
     @Test
     public void testSignature() {
        geocoder.addClientIdAndSignURL(new StringBuilder("Some data to test re-usage of signer: bla-bla-000"));
        geocoder.addClientIdAndSignURL(new StringBuilder("Some data to test re-usage of signer: bla-bla-001"));
        geocoder.addClientIdAndSignURL(new StringBuilder("Some data to test re-usage of signer: bla-bla-002"));
        geocoder.addClientIdAndSignURL(new StringBuilder("Some data to test re-usage of signer: bla-bla-003"));
        geocoder.addClientIdAndSignURL(new StringBuilder("Some data to test re-usage of signer: bla-bla-004"));
        geocoder.addClientIdAndSignURL(new StringBuilder("Some data to test re-usage of signer: bla-bla-005"));
 
        StringBuilder url = new StringBuilder("/maps/api/geocode/json?address=New+York&sensor=false");
        geocoder.addClientIdAndSignURL(url);
         Assert.assertEquals("/maps/api/geocode/json?address=New+York&sensor=false&client=clientID&signature=KrU1TzVQM7Ur0i8i7K3huiw3MsA=", url.toString());
     }
 }
