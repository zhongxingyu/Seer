 package org.opengeo.usgs;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.data.simple.SimpleFeatureSource;
 import org.opengis.feature.simple.SimpleFeature;
 import org.w3c.dom.Document;
 
 public class NHDPointTest extends USGSTestSupport {
 
     public NHDPointTest() {
         // load test pg data
         setRunDBSetup(true);
     }
     
     public void testInsertsFail() throws Exception {
         
         // nhdpoint MustIntersect rules
        List<String> files = Arrays.asList(
                 "xml/rule-1-fail.xml",
                 "xml/rule-2-fail.xml",
                 "xml/rule-3-fail.xml");
         
         for (String file : files) {
             String code = file.split("-")[1];
             assertViolatesRule(file, code, "js:MustIntersect");
         }
 
         // nhdpoint MustIntersectEndpoint rules
        files = Arrays.asList(
                 "xml/rule-5-fail.xml");
         
         for (String file : files) {
             String code = file.split("-")[1];
             assertViolatesRule(file, code, "js:MustIntersectEndpoint");
         }
 
     }
     public void testInsertsQueue() throws Exception {
         SimpleFeatureSource exceptions = getFeatureSource(new QName("nhdexceptions"));
         assertEquals("no exceptions initially", 0, exceptions.getFeatures().size());
 
         String[] files = { "xml/rule-1-queue.xml", "xml/rule-2-queue.xml" };
         
         for (int i=0; i<files.length; ++i) {
             String file = files[i];
             
             Document dom = postRequest(file);
             assertNotNull(file, dom);
 
             String handle = xpath.evaluate("//wfs:InsertResults/wfs:Feature/@handle", dom);
             assertEquals(file, "nhdmetadata.15", handle);
 
             SimpleFeatureCollection fc = exceptions.getFeatures();
             assertEquals(file + " one exception", i+1, fc.size());
 
             SimpleFeatureIterator it = fc.features();
             SimpleFeature feature = null;
             while (it.hasNext()) {
                 feature = it.next();
             }
             it.close();
             assertNotNull(file, feature);
 
             String id = (String) feature.getAttribute("metadataid");
             assertNotNull(file, id);
             id = id.trim();  // TODO update test dump to use varchar in NHDExceptions 
             assertEquals(file + " correct metadataid", "nhdmetadata.15", id);
         }
 
     }
 
     public void testInsertsPass() throws Exception {
         
         // transactions that pass MustIntersect
        List<String> files = Arrays.asList(
                 "xml/rule-2-pass.xml",
                 "xml/rule-5-pass.xml");
         
         for (String file : files) {
             Document dom = postRequest(file);
             assertNotNull(dom);
             
             String inserted = xpath.evaluate("//wfs:totalInserted/text()", dom);
             assertEquals(file, "1", inserted);
             
         }
 
     }
     
 
 }
