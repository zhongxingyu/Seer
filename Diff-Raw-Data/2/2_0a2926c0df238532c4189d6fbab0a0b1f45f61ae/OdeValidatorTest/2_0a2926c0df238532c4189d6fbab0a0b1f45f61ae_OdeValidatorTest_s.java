 package com.google.code.qualitas.engines.ode.validation;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.google.code.qualitas.engines.api.validation.ValidationException;
 import com.google.code.qualitas.engines.ode.core.OdeBundle;
 
 public class OdeValidatorTest {
 
     private static String odePlatform;
     private static String odeHome;
     private static OdeBundle odeProcessBundle;
     private static OdeBundle odeProcessBundleError;
     private static OdeValidator odeProcessBundleValidator;
 
     @BeforeClass
     public static void setUpClass() throws IOException {    	
         odeProcessBundle = new OdeBundle();
         byte[] zippedArchive = FileUtils.readFileToByteArray(new File(
                 "src/test/resources/XhGPWWhile.zip"));
         odeProcessBundle.setBundle(zippedArchive);
         odeProcessBundle.setMainProcessQName(new QName("XhGPWWhile"));
 
         odeProcessBundleError = new OdeBundle();
         byte[] zippedArchiveError = FileUtils.readFileToByteArray(new File(
                 "src/test/resources/XhGPWWhileError.zip"));
         odeProcessBundleError.setBundle(zippedArchiveError);
         odeProcessBundleError.setMainProcessQName(new QName("XhGPWWhile"));
 
     	Properties properties = new Properties();
    	InputStream is = OdeValidatorTest.class.getResourceAsStream("/environment.properties");
     	properties.load(is);
     	
     	odePlatform = properties.getProperty("ode.platform");
     	odeHome = properties.getProperty("ode.home");
         
         odeProcessBundleValidator = new OdeValidator();
         odeProcessBundleValidator.setExternalToolHome(odeHome);
         odeProcessBundleValidator.setExternalToolPlatform(odePlatform);
     }
 
     @AfterClass
     public static void tearDownClass() throws IOException {
         odeProcessBundle.cleanUp();
         odeProcessBundleError.cleanUp();
     }
 
     @Test
     public void testSetExternalToolHome() {
         odeProcessBundleValidator.setExternalToolHome(odeHome);
     }
 
     @Test
     public void testSetExternalToolPlatform() {
         odeProcessBundleValidator.setExternalToolPlatform(odePlatform);
     }
 
     @Test
     public void testValidateODEArchive() throws ValidationException {
         odeProcessBundleValidator.validate(odeProcessBundle);
     }
 
     @Test
     public void testValidateODEArchiveString() throws ValidationException {
         odeProcessBundleValidator.validate(odeProcessBundle,
                 "XhGPWWhile");
     }
 
     @Test(expected = ValidationException.class)
     public void testValidateODEArchiveError() throws ValidationException {
         odeProcessBundleValidator.validate(odeProcessBundleError);
     }
 
 }
