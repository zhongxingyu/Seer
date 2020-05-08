 package org.penguin.kayako;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class ApiRequestTests {
     
    @Test
    public void testSignatureGeneratesCorrectly() throws Exception {
         // arrange
         String apiSecret = "MDA4YzBiMWMtN2RiOC1hZTY0LTMxODgtMzE1MThjNmU5NDJlYTM1ZTgwY2YtYjA1ZS1jMzQ0LWY5MjktMzQ1ZjliMDA4ODIx";
         
         // act
         ApiRequest request = new ApiRequest(null, apiSecret, null).setSalt("3777329113");
         
         // assert
         Assert.assertEquals("gFhgQ1Gydk+DLsn6BnHO/eqs1KxPwqmlj2bRWfjFFYs=", request.getSignature());
     }
 }
