 package org.openhealthdata.validator;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.io.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: diego
  */
 public class ACheckRealXsdTest {
 
     @Test
     public void checkNotDummyFile() throws IOException {
         InputStream xsdFile = getClass().getResourceAsStream("/CCRV1.xsd");
         Assert.assertNotNull("CCRV1.xsd not found in src/main/resources", xsdFile);
         Writer writer = new StringWriter();
 
         char[] buffer = new char[1024];
         try {
             Reader reader = new BufferedReader(
                     new InputStreamReader(xsdFile, "UTF-8"));
             int n;
             while ((n = reader.read(buffer)) != -1) {
                 writer.write(buffer, 0, n);
             }
         } finally {
             xsdFile.close();
         }
 
        Assert.assertTrue("Dummy file found, replace CCRV1.xsd with your own licensed version", dummyContent.equals(writer.toString()));
 
     }
 
     private static final String dummyContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
             "<xs:schema xmlns=\"urn:astm-org:CCR\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:ccr=\"urn:astm-org:CCR\" targetNamespace=\"urn:astm-org:CCR\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\n" +
             "\t<!--E2369-05, Standard Specification for the Continuity of Care (CCR) - Final Version 1.0 (V1.0) November 7, 2005,  ASTM E31.28 CCR Subcommittee-->\n" +
             "\t<!--Copyright 2004-2005 ASTM, 100 Barr Harbor Drive, West Conshohocken, PA 19428-2959. All rights reserved.-->\n" +
             "\n" +
            "</xs:schema>";
 }
