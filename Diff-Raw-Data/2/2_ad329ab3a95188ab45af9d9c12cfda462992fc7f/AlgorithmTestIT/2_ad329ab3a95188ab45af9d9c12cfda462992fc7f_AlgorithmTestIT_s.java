 package gov.usgs.cida.gdp.wps.algorithm.it;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import net.opengis.wps.x100.ProcessDescriptionType;
 import net.opengis.wps.x100.ProcessDescriptionsDocument;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlOptions;
 import org.apache.xmlbeans.XmlValidationError;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 
 /**
  *
  * @author isuftin
  */
 public class AlgorithmTestIT {
 
    private String describeProcessPrepend = "http://localhost:9090/gdp-process-wps/WebProcessingService?Service=WPS&Request=DescribeProcess&Identifier=";
 
     public AlgorithmTestIT() {
     }
 
     @BeforeClass
     public static void setUpClass() {
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void validateFeatureCoverageIntersectionAlgorithmDescribeProcess() throws IOException, XmlException {
         String url = describeProcessPrepend + "gov.usgs.cida.gdp.wps.algorithm.FeatureCoverageIntersectionAlgorithm";
         HttpClient httpclient = new DefaultHttpClient();
         HttpGet httpget = new HttpGet(url);
         HttpResponse response = httpclient.execute(httpget);
         HttpEntity entity = response.getEntity();
 
         if (entity != null) {
             InputStream instream = entity.getContent();
             List<XmlValidationError> xmlValidationErrorList = new ArrayList<XmlValidationError>();
             ProcessDescriptionsDocument pdt = null;
             XmlOptions xmlOptions = new XmlOptions();
             xmlOptions.setErrorListener(xmlValidationErrorList);
             
             try {
                 pdt = ProcessDescriptionsDocument.Factory.parse(instream);
             } finally {
                 instream.close();
             }
 
             boolean validated = pdt.validate(xmlOptions);
 
             System.out.println("\n\nValidation Error Count: " + xmlValidationErrorList.size() + "\n\n");
             for (XmlValidationError err : xmlValidationErrorList) {
                 System.out.println(err.getMessage());
             }
 
             assertThat(validated, is(true));
 //          assertThat(AlgorithmUtil.processDescriptionIsValid(pdt), is(true));
         }
 
         // Alternatively, we can disregard everything above and just run:
 //        ProcessDescriptionType pdt = ProcessDescriptionType.Factory.parse(new URL(url));
 //        assertThat(AlgorithmUtil.processDescriptionIsValid(pdt), is(true));
 
     }
 }
