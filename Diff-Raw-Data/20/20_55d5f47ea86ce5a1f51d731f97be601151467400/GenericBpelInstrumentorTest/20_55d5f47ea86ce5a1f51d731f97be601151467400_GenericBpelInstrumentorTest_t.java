 package com.googlecode.qualitas.internal.bpel.instrumentation;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.googlecode.qualitas.engines.api.core.Bundle;
 import com.googlecode.qualitas.engines.api.core.Entry;
 import com.googlecode.qualitas.engines.api.instrumentation.InstrumentationException;
 import com.googlecode.qualitas.engines.api.validation.ValidationException;
 import com.googlecode.qualitas.engines.api.validation.Validator;
 import com.googlecode.qualitas.engines.ode.core.OdeBundle;
 import com.googlecode.qualitas.engines.ode.instrumentation.OdeInstrumentor;
 import com.googlecode.qualitas.engines.ode.validation.OdeValidator;
 
 public class GenericBpelInstrumentorTest {
 
     private static Bundle bundle;
 
     private static Validator validator;
 
     private static GenericBpelInstrumentor instrumentor;
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         byte[] contents = FileUtils.readFileToByteArray(new File("src/test/resources/XhGPWWhile.zip"));
         QName mainProcessQName = new QName("http://examples.bpel.nuntius.xh.org/xhGPWWhile",
                 "XhGPWWhile");
 
         bundle = new OdeBundle();
         bundle.setBundle(contents);
         bundle.setMainProcessQName(mainProcessQName);
         
         OdeInstrumentor odeInstrumentor = new OdeInstrumentor();
         odeInstrumentor.instrument(bundle);
         
         Properties properties = new Properties();
         properties.load(GenericBpelInstrumentor.class
                 .getResourceAsStream("/qualitas-engines-ode-validation.properties"));
 
         validator = new OdeValidator();
         validator.setExternalToolHome(properties.getProperty("ode.home"));
         validator.setExternalToolPlatform(properties.getProperty("ode.platform"));
         
         instrumentor = new GenericBpelInstrumentor();
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
         bundle.cleanUp();
     }
 
     @Test
     public void testIsSupported() {
         boolean isSupported = instrumentor.isSupported(bundle.getProcessType());
         Assert.assertTrue(isSupported);
     }
 
     @Test
     public void testInstrument() throws InstrumentationException, IOException {
         instrumentor.instrument(bundle);
         
         Entry wsdl = bundle.getEntry("QualitasExecutionMonitorService.wsdl");
         Entry artifacts = bundle.getEntry("QualitasExecutionMonitorServiceArtifacts.wsdl");
         
         Assert.assertNotNull(wsdl);
         Assert.assertNotNull(artifacts);
        
        try {
            validator.validate(bundle);
        } catch (ValidationException e) {
            Assert.fail("Validation failed " + e.getMessage());
        }
 
     }
 
 }
