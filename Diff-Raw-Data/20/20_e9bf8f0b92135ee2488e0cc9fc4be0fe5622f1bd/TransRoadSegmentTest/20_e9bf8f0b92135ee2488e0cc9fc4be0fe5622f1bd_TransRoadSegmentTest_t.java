 package org.opengeo.usgs;
 
import javax.xml.namespace.QName;

import org.geotools.data.simple.SimpleFeatureSource;
 import org.w3c.dom.Document;
 
 public class TransRoadSegmentTest extends USGSTestSupport {
 
     public TransRoadSegmentTest() {
         // load test pg data
         setRunDBSetup(true);
     }
     
     public void testInsertsFail() throws Exception {
         
         assertViolatesRule("xml/rule-18-fail.xml", "18", "js:MustTouch");
         assertViolatesRule("xml/rule-19-fail.xml", "19", "js:MustIntersectEndpoint");
 
     }
 
     public void testInsertsPass() throws Exception {
         
         String[] files = {
                 "xml/rule-18-pass.xml",
                 "xml/rule-19-pass.xml"};
         
         for (String file : files) {
             Document dom = postRequest(file);
             assertNotNull(dom);
             
             String inserted = xpath.evaluate("//wfs:totalInserted/text()", dom);
             assertEquals(file, "1", inserted);
             
         }
 
     }
 
    public void testInsertRoadAutoCorrectEndpoint() throws Exception {

        SimpleFeatureSource exceptions = getFeatureSource(new QName("NHDExceptions"));
        assertNotNull(exceptions);
        assertEquals("no exceptions initially", 0, exceptions.getFeatures().size());

        Document dom = postRequest("xml/rule-19-correct.xml");
        assertNotNull(dom);

        String inserted = xpath.evaluate("//wfs:totalInserted/text()", dom);
        assertEquals("road inserted", "1", inserted);

        assertEquals("no exceptions after", 0, exceptions.getFeatures().size());

    }
    
 }
