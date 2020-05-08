 package org.opengeo.usgs;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.opengis.feature.simple.SimpleFeature;
 import org.w3c.dom.Document;
 
 public class NHDFlowlineTest extends USGSTestSupport {
 
     public NHDFlowlineTest() {
         // load test pg data
         setRunDBSetup(true);
     }
     
     public void testInsertsFail() throws Exception {
         
         // nhdflowline MustNotCross rules
         List<String> files = Arrays.asList("xml/rule-7-fail.xml");
         
         for (String file : files) {
             String code = file.split("-")[1];
             assertViolatesRule(file, code, "js:MustNotCross");
         }
 
         // nhdflowline PipelineMustHaveVerticalRelationship rules
         files = Arrays.asList("xml/rule-6-fail.xml");
         
         for (String file : files) {
             String code = file.split("-")[1];
             assertViolatesRule(file, code, "js:PipelineMustHaveVerticalRelationship");
         }
 
     }
 
     public void testInsertPipelineAutoCorrect() throws Exception {
 
         SimpleFeatureSource relationships = getFeatureSource(new QName("nhdverticalrelationship"));
         assertEquals("no relationships initially", 0, relationships.getFeatures().size());
         
         Document dom = postRequest("xml/rule-6-correct.xml");
         assertNotNull(dom);
         
         String inserted = xpath.evaluate("//wfs:totalInserted/text()", dom);
         assertEquals("pipeline inserted", "1", inserted);
         
         SimpleFeatureCollection fc = relationships.getFeatures();
         assertEquals("one relationship", 1, fc.size());
 
         SimpleFeatureIterator it = fc.features();
         SimpleFeature feature = it.next();
         it.close();
 
         assertEquals("above", "nhdflowline.374", feature.getAttribute("Above_Permanent_Identifier"));
         assertEquals("below", "nhdflowline.100", feature.getAttribute("Below_Permanent_Identifier"));
     }
 }
